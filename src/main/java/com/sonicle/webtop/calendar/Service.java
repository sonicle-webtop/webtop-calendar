/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.calendar;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.extjs.ExtFieldMeta;
import com.sonicle.commons.web.json.extjs.ExtGridColumnMeta;
import com.sonicle.commons.web.json.extjs.ExtGridMetaData;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.calendar.CalendarUserSettings.CheckedFolders;
import com.sonicle.webtop.calendar.CalendarUserSettings.CheckedRoots;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.model.SchedulerEvent;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.js.JsAttendee;
import com.sonicle.webtop.calendar.bol.js.JsAttendee.JsAttendeeList;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEventDate;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsCalendarLkp;
import com.sonicle.webtop.calendar.bol.js.JsErpExportStart;
import com.sonicle.webtop.calendar.bol.js.JsFolderNode;
import com.sonicle.webtop.calendar.bol.js.JsFolderNode.JsFolderNodeList;
import com.sonicle.webtop.calendar.bol.js.JsSharing;
import com.sonicle.webtop.calendar.bol.model.CalendarFolder;
import com.sonicle.webtop.calendar.bol.model.CalendarRoot;
import com.sonicle.webtop.calendar.bol.model.EventKey;
import com.sonicle.webtop.calendar.bol.model.MyCalendarFolder;
import com.sonicle.webtop.calendar.bol.model.MyCalendarRoot;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.bol.model.SharePermsRoot;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.LogEntries;
import com.sonicle.webtop.core.util.LogEntry;
import com.sonicle.webtop.core.util.MessageLogEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	public static final Logger logger = WT.getLogger(Service.class);
	
	private CalendarManager manager;
	private CalendarServiceSettings ss;
	private CalendarUserSettings us;
	
	public static final String ERP_EXPORT_FILENAME = "events_{0}-{1}-{2}.{3}";
	
	private final LinkedHashMap<String, CalendarRoot> roots = new LinkedHashMap<>();
	private CheckedRoots checkedRoots = null;
	private CheckedFolders checkedFolders = null;
	private ErpExportWizard erpWizard = null;

	@Override
	public void initialize() {
		UserProfile profile = getEnv().getProfile();
		manager = new CalendarManager(getId(), getRunContext());
		ss = new CalendarServiceSettings(getId());
		us = new CalendarUserSettings(getId(), profile.getId(), ss);
		
		try {
			initFolders();
		} catch(Exception ex) {
			logger.error("initFolders", ex);
		}
	}
	
	@Override
	public void cleanup() {
		checkedFolders.clear();
		checkedFolders = null;
		checkedRoots.clear();
		checkedRoots = null;
		us = null;
		ss = null;
		manager = null;
	}
	
	@Override
	public HashMap<String, Object> returnClientOptions() {
		DateTimeFormatter hmf = DateTimeUtils.createHmFormatter();
		HashMap<String, Object> co = new HashMap<>();
		co.put("view", us.getCalendarView());
		co.put("workdayStart", hmf.print(us.getWorkdayStart()));
		co.put("workdayEnd", hmf.print(us.getWorkdayEnd()));
		return co;
	}
	
	private void initFolders() throws Exception {
		UserProfile.Id pid = getEnv().getProfile().getId();
		synchronized(roots) {
			roots.clear();
			roots.put(MyCalendarRoot.SHARE_ID, new MyCalendarRoot(pid));
			for(CalendarRoot root : manager.listIncomingCalendarRoots()) {
				roots.put(root.getShareId(), root);
			}

			checkedRoots = us.getCheckedCalendarRoots();
			// If empty, adds MyNode checked by default!
			if(checkedRoots.isEmpty()) {
				checkedRoots.add(MyCalendarRoot.SHARE_ID);
				us.setCheckedCalendarRoots(checkedRoots);
			}
			checkedFolders = us.getCheckedCalendarFolders();
		}
	}
	
	public void processManageFoldersTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				
				if(node.equals("root")) { // Node: root -> list roots
					for(CalendarRoot root : roots.values()) {
						children.add(createRootNode(root));
					}
				} else { // Node: folder -> list folders (calendars)
					CalendarRoot root = roots.get(node);
					
					if(root instanceof MyCalendarRoot) {
						for(OCalendar cal : manager.listCalendars()) {
							MyCalendarFolder folder = new MyCalendarFolder(node, cal);
							children.add(createFolderNode(folder, root.getPerms()));
						}
					} else {
						for(CalendarFolder folder : manager.listIncomingCalendarFolders(node)) {
							children.add(createFolderNode(folder, root.getPerms()));
						}
					}
				}
				new JsonResult("children", children).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for(JsFolderNode node : pl.data) {
					if(node._type.equals(JsFolderNode.TYPE_ROOT)) {
						toggleCheckedRoot(node.id, node._visible);
						
					} else if(node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						toggleCheckedFolder(Integer.valueOf(cid.getToken(1)), node._visible);
					}
				}
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for(JsFolderNode node : pl.data) {
					if(node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						manager.deleteCalendar(Integer.valueOf(cid.getToken(1)));
					}
				}
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageCalendarsTree", ex);
		}
	}
	
	public void processLookupCalendarRoots(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			Boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", true);
			
			for(CalendarRoot root : roots.values()) {
				if(root instanceof MyCalendarRoot) {
					UserProfile up = getEnv().getProfile();
					items.add(new JsSimple(up.getStringId(), up.getDisplayName()));
				} else {
					//TODO: se writableOnly verificare che il gruppo condiviso sia scrivibile
					items.add(new JsSimple(root.getOwnerProfileId().toString(), root.getDescription()));
				}
			}
			
			new JsonResult("roots", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in action LookupCalendarRoots", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupCalendarFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCalendarLkp> items = new ArrayList<>();
		
		try {
			for(CalendarRoot root : roots.values()) {
				if(root instanceof MyCalendarRoot) {
					for(OCalendar cal : manager.listCalendars()) {
						items.add(new JsCalendarLkp(cal));
					}
				} else {
					for(CalendarFolder folder : manager.listIncomingCalendarFolders(root.getShareId())) {
						if(!folder.getElementsPerms().implies("CREATE")) continue;
						items.add(new JsCalendarLkp(folder.getCalendar()));
					}
				}
			}
			new JsonResult("folders", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in action LookupCalendarFolders", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				Sharing sharing = manager.getSharing(id);
				String description = buildSharingPath(sharing);
				new JsonResult(new JsSharing(sharing, description)).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, Sharing> pl = ServletUtils.getPayload(request, Sharing.class);
				
				manager.updateSharing(pl.data);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		OCalendar item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", true);
				
				item = manager.getCalendar(id);
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OCalendar> pl = ServletUtils.getPayload(request, OCalendar.class);
				
				item = manager.addCalendar(pl.data);
				toggleCheckedFolder(item.getCalendarId(), true);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OCalendar> pl = ServletUtils.getPayload(request, OCalendar.class);
				
				manager.updateCalendar(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OCalendar> pl = ServletUtils.getPayload(request, OCalendar.class);
				
				manager.deleteCalendar(pl.data.getCalendarId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageCalendars", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processGetSchedulerDates(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		ArrayList<JsSchedulerEventDate> items = new ArrayList<>();
		
		try {
			con = getConnection();
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			DateTimeFormatter ymdZoneFmt = DateTimeUtils.createYmdFormatter(utz);
			
			// Defines boundaries
			String start = ServletUtils.getStringParameter(request, "startDate", true);
			String end = ServletUtils.getStringParameter(request, "endDate", true);
			DateTime fromDate = CalendarManager.parseYmdHmsWithZone(start, "00:00:00", up.getTimeZone());
			DateTime toDate = CalendarManager.parseYmdHmsWithZone(end, "23:59:59", up.getTimeZone());
			
			// Get events for each visible group
			Integer[] checked = getCheckedFolders();
			List<DateTime> dates = null;
			for(CalendarRoot root : getCheckedRoots()) {
				dates = manager.listEventsDates(root, checked, fromDate, toDate, utz);
				for(DateTime dt : dates) {
					items.add(new JsSchedulerEventDate(ymdZoneFmt.print(dt)));
				}
			}
			new JsonResult("dates", items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in action GetSchedulerDates", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processManageEventsScheduler(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		ArrayList<JsSchedulerEvent> items = new ArrayList<>();
		
		try {
			con = getConnection();
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String from = ServletUtils.getStringParameter(request, "startDate", true);
				String to = ServletUtils.getStringParameter(request, "endDate", true);
				
				// Defines view boundary 
				DateTime fromDate = CalendarManager.parseYmdHmsWithZone(from, "00:00:00", up.getTimeZone());
				DateTime toDate = CalendarManager.parseYmdHmsWithZone(to, "23:59:59", up.getTimeZone());
				
				// Get events for each visible folder
				JsSchedulerEvent jse = null;
				List<SchedulerEvent> recInstances = null;
				List<CalendarManager.CalendarEvents> calEvts = null;
				Integer[] checked = getCheckedFolders();
				for(CalendarRoot root : getCheckedRoots()) {
					calEvts = manager.listSchedulerEvents(root, checked, fromDate, toDate);
					// Iterates over calendar->events
					for(CalendarManager.CalendarEvents ce : calEvts) {
						for(SchedulerEvent evt : ce.events) {
							if(evt.getRecurrenceId() == null) {
								jse = new JsSchedulerEvent(ce.calendar, evt, up.getId(), utz);
								items.add(jse);
							} else {
								recInstances = manager.calculateRecurringInstances(evt, fromDate, toDate, utz);
								for(SchedulerEvent recInstance : recInstances) {
									jse = new JsSchedulerEvent(ce.calendar, recInstance, up.getId(), utz);
									items.add(jse);
								}
							}
						}
					}
				}
				new JsonResult("events", items).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsSchedulerEvent> pl = ServletUtils.getPayload(request, JsSchedulerEvent.class);
				
				DateTimeZone etz = DateTimeZone.forID(pl.data.timezone);
				DateTime newStart = CalendarManager.parseYmdHmsWithZone(pl.data.startDate, etz);
				DateTime newEnd = CalendarManager.parseYmdHmsWithZone(pl.data.endDate, etz);
				manager.copyEvent(EventKey.buildKey(pl.data.eventId, pl.data.originalEventId), newStart, newEnd);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsSchedulerEvent.Update> pl = ServletUtils.getPayload(request, JsSchedulerEvent.Update.class);
				
				DateTimeZone etz = DateTimeZone.forID(pl.data.timezone);
				DateTime newStart = CalendarManager.parseYmdHmsWithZone(pl.data.startDate, etz);
				DateTime newEnd = CalendarManager.parseYmdHmsWithZone(pl.data.endDate, etz);
				manager.updateEvent(pl.data.id, newStart, newEnd, pl.data.title);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				String uid = ServletUtils.getStringParameter(request, "id", true);
				String target = ServletUtils.getStringParameter(request, "target", "this");
				
				manager.deleteEvent(target, uid);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("restore")) {
				String uid = ServletUtils.getStringParameter(request, "id", true);
				
				manager.restoreEvent(uid);
				new JsonResult().printTo(out);
			} else if(crud.equals("search")) {
				String query = ServletUtils.getStringParameter(request, "query", true);
				
				List<CalendarManager.CalendarEvents> calEvts = null;
				Integer[] checked = getCheckedFolders();
				for(CalendarRoot root : getCheckedRoots()) {
					calEvts = manager.searchSchedulerEvents(root.getOwnerProfileId(), checked, "%"+query+"%");
					// Iterates over calendar->events
					for(CalendarManager.CalendarEvents ce : calEvts) {
						for(SchedulerEvent evt : ce.events) {
							if(evt.getRecurrenceId() == null) {
								items.add(new JsSchedulerEvent(ce.calendar, evt, up.getId(), utz));
							}
						}
					}
				}
				new JsonResult("events", items).printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageEventsScheduler", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processManageEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsEvent item = null;
		
		try {
			UserProfile up = getEnv().getProfile();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				Event evt = manager.getEvent(id);
				UserProfile.Id ownerId = manager.getCalendarOwner(evt.getCalendarId());
				item = new JsEvent(evt, ownerId.toString());
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				//TODO: verificare che il calendario supporti la scrittura (specialmente per quelli condivisi)
				
				Event evt = JsEvent.buildEvent(pl.data);
				CoreManager core = WT.getCoreManager(getRunContext());
				evt.setOrganizer(core.getUserCompleteEmailAddress(up.getId()));
				
				manager.addEvent(evt);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				String target = ServletUtils.getStringParameter(request, "target", "this");
				Payload<MapItem, JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				Event evt = JsEvent.buildEvent(pl.data);
				manager.editEvent(target, evt, up.getTimeZone());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageEvents", ex);
			new JsonResult(false, "Error").printTo(out);	
		}
	}
	
	public void processManageGridEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSchedulerEvent> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String query = ServletUtils.getStringParameter(request, "query", true);
				
				List<CalendarManager.CalendarEvents> calEvts = null;
				Integer[] checked = getCheckedFolders();
				for(CalendarRoot root : getCheckedRoots()) {
					calEvts = manager.searchSchedulerEvents(root.getOwnerProfileId(), checked, "%"+query+"%");
					// Iterates over calendar->events
					for(CalendarManager.CalendarEvents ce : calEvts) {
						for(SchedulerEvent evt : ce.events) {
							if(evt.getRecurrenceId() == null) {
								items.add(new JsSchedulerEvent(ce.calendar, evt, up.getId(), utz));
							}
						}
					}
				}
				new JsonResult("events", items).printTo(out);
			}
		
		} catch(Exception ex) {
			logger.error("Error in action ManageGridEvents", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
	
	public void processErpExportWizard(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String step = ServletUtils.getStringParameter(request, "step", true);
			if(step.equals("start")) {
				Payload<MapItem, JsErpExportStart> pl = ServletUtils.getPayload(request, JsErpExportStart.class);
				DateTimeFormatter ymd = DateTimeUtils.createYmdFormatter(up.getTimeZone());
				
				erpWizard = new ErpExportWizard();
				erpWizard.fromDate = ymd.parseDateTime(pl.data.fromDate).withTimeAtStartOfDay();
				erpWizard.toDate = DateTimeUtils.withTimeAtEndOfDay(ymd.parseDateTime(pl.data.toDate));
				
				new JsonResult().printTo(out);
				
			} else if(step.equals("end")) {
				File file = WT.createTempFile();
				LogEntries log = new LogEntries();
				DateTimeFormatter ymd = DateTimeUtils.createFormatter("yyyyMMdd", up.getTimeZone());
				DateTimeFormatter ymdhms = DateTimeUtils.createFormatter("yyyy-MM-dd HH:mm:ss", up.getTimeZone());
				
				try (FileOutputStream fos = new FileOutputStream(file)) {
					log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Started on {0}", ymdhms.print(new DateTime())));
					manager.exportEvents(log, up.getDomainId(), erpWizard.fromDate, erpWizard.toDate, fos);
					log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Ended on {0}", ymdhms.print(new DateTime())));
					erpWizard.file = file;
					erpWizard.filename = MessageFormat.format(ERP_EXPORT_FILENAME, up.getDomainId(), ymd.print(erpWizard.fromDate), ymd.print(erpWizard.fromDate), "csv");
					log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "File ready: {0}", erpWizard.filename));
					log.addMaster(new MessageLogEntry(LogEntry.LEVEL_INFO, "Operation completed succesfully"));
					new JsonResult(log.print()).printTo(out);
					
				} catch(Exception ex1) {
					ex1.printStackTrace();
					new JsonResult(log.print()).setSuccess(false).printTo(out);
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ErpExportWizard", ex);
			new JsonResult(false, "Error").printTo(out);	
		}
	}
	
	public void processErpExportWizard(HttpServletRequest request, HttpServletResponse response) {
		UserProfile up = getEnv().getProfile();
		try {
			try(FileInputStream fis = new FileInputStream(erpWizard.file)) {
				ServletUtils.writeFileStream(response, erpWizard.filename, fis, false);
			}
			
		} catch(Exception ex) {
			//TODO: logging
			ex.printStackTrace();
		} finally {
			erpWizard = null;
		}
	}
	
	public void processGetPlanning(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		ArrayList<MapItem> items = new ArrayList<>();
		CoreUserSettings cus = new CoreUserSettings(getEnv().getProfileId());
		
		try {
			String eventStartDate = ServletUtils.getStringParameter(request, "startDate", true);
			String eventEndDate = ServletUtils.getStringParameter(request, "endDate", true);
			String timezone = ServletUtils.getStringParameter(request, "timezone", true);
			JsAttendeeList attendees = ServletUtils.getObjectParameter(request, "attendees", new JsAttendeeList(), JsAttendeeList.class);
			
			// Parses string parameters
			DateTimeZone eventTz = DateTimeZone.forID(timezone);
			DateTime eventStartDt = CalendarManager.parseYmdHmsWithZone(eventStartDate, eventTz);
			DateTime eventEndDt = CalendarManager.parseYmdHmsWithZone(eventEndDate, eventTz);
			
			UserProfile up = getEnv().getProfile();
			DateTimeZone profileTz = up.getTimeZone();
			
			LocalTime localStartTime = eventStartDt.toLocalTime();
			LocalTime localEndTime = eventEndDt.toLocalTime();
			LocalTime fromTime = DateTimeUtils.min(localStartTime, us.getWorkdayStart());
			LocalTime toTime = DateTimeUtils.max(localEndTime, us.getWorkdayEnd());
			
			// Defines useful date/time formatters
			DateTimeFormatter ymdhmFmt = DateTimeUtils.createYmdHmFormatter();
			DateTimeFormatter tFmt = DateTimeUtils.createFormatter(cus.getShortTimeFormat());
			DateTimeFormatter dFmt = DateTimeUtils.createFormatter(cus.getShortDateFormat());
			
			ArrayList<String> hours = manager.generateTimeSpans(60, eventStartDt.toLocalDate(), eventEndDt.toLocalDate(), us.getWorkdayStart(), us.getWorkdayEnd(), profileTz);
			
			// Generates fields and columnsInfo dynamically
			ArrayList<ExtFieldMeta> fields = new ArrayList<>();
			ArrayList<ExtGridColumnMeta> colsInfo = new ArrayList<>();
			
			ExtGridColumnMeta col = null;
			fields.add(new ExtFieldMeta("recipient"));
			colsInfo.add(new ExtGridColumnMeta("recipient"));
			for(String hourKey : hours) {
				LocalDateTime ldt = ymdhmFmt.parseLocalDateTime(hourKey);
				fields.add(new ExtFieldMeta(hourKey));
				col = new ExtGridColumnMeta(hourKey, tFmt.print(ldt));
				col.put("date", dFmt.print(ldt));
				col.put("overlaps", DateTimeUtils.between(ldt, eventStartDt.toLocalDateTime(), eventEndDt.toLocalDateTime()));
				colsInfo.add(col);
			}
			
			// Collects attendees availability...
			OUser user = null;
			UserProfile.Id profileId = null;
			LinkedHashSet<String> busyHours = null;
			MapItem item = null;
			for(JsAttendee attendee : attendees) {
				item = new MapItem();
				item.put("recipient", attendee.recipient);
				
				user = guessUserByAttendee(attendee.recipient);
				if(user != null) {
					profileId = new UserProfile.Id(user.getDomainId(), user.getUserId());
					busyHours = manager.calculateAvailabilitySpans(60, profileId, eventStartDt.withTime(fromTime), eventEndDt.withTime(toTime), eventTz, true);
					for(String hourKey : hours) {
						if(busyHours.contains(hourKey)) {
							item.put(hourKey, "busy");
						} else {
							item.put(hourKey, "free");
						}
					}
				} else {
					for(String hourKey : hours) {
						item.put(hourKey, "unknown");
					}
				}
				
				items.add(item);
			}
			
			ExtGridMetaData meta = new ExtGridMetaData(true);
			meta.setFields(fields);
			meta.setColumnsInfo(colsInfo);
			new JsonResult(items, meta, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in action ManageEvents", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processICalImportUploadStream(HttpServletRequest request, InputStream uploadStream) throws Exception {
		UserProfile up = getEnv().getProfile();
		Integer calendarId = ServletUtils.getIntParameter(request, "calendarId", true);
		manager.importICal(calendarId, uploadStream, up.getTimeZone());
	}
	
	private OUser guessUserByAttendee(String recipient) {
		Connection con = null;
		
		try {
			//TODO: gestire definitivamente il campo attendee.recipient... lookup per email???
			UserProfile.Id profileId = new UserProfile.Id(recipient);
			
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			return udao.selectByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
		
		} catch(WTRuntimeException ex) {
			return null;
		} catch(Exception ex) {
			logger.error("Error guessing user from attendee", ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String buildSharingPath(Sharing sharing) throws WTException {
		StringBuilder sb = new StringBuilder();
		
		// Root description part
		CompositeId cid = new CompositeId().parse(sharing.getId());
		if(roots.containsKey(cid.getToken(0))) {
			CalendarRoot root = roots.get(cid.getToken(0));
			if(root instanceof MyCalendarRoot) {
				sb.append(lookupResource(CalendarLocale.CALENDARS_MY));
			} else {
				sb.append(root.getDescription());
			}
		}
		
		// Folder description part
		if(sharing.getLevel() == 1) {
			int calId = Integer.valueOf(cid.getToken(1));
			OCalendar calendar = manager.getCalendar(calId);
			sb.append("/");
			sb.append((calendar != null) ? calendar.getName() : cid.getToken(1));
		}
		
		return sb.toString();
	}
	
	private List<CalendarRoot> getCheckedRoots() {
		ArrayList<CalendarRoot> checked = new ArrayList<>();
		for(CalendarRoot root : roots.values()) {
			if(!checkedRoots.contains(root.getShareId())) continue; // Skip folder if not visible
			checked.add(root);
		}
		return checked;
	}
	
	private Integer[] getCheckedFolders() {
		return checkedFolders.toArray(new Integer[checkedFolders.size()]);
	}
	
	private void toggleCheckedRoot(String shareId, boolean checked) {
		synchronized(roots) {
			if(checked) {
				checkedRoots.add(shareId);
			} else {
				checkedRoots.remove(shareId);
			}
			us.setCheckedCalendarRoots(checkedRoots);
		}
	}
	
	private void toggleCheckedFolder(int folderId, boolean checked) {
		synchronized(roots) {
			if(checked) {
				checkedFolders.add(folderId);
			} else {
				checkedFolders.remove(folderId);
			}
			us.setCheckedCalendarFolders(checkedFolders);
		}
	}
	
	private ExtTreeNode createRootNode(CalendarRoot root) {
		if(root instanceof MyCalendarRoot) {
			return createRootNode(root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), lookupResource(CalendarLocale.CALENDARS_MY), false, "wtcal-icon-root-my-xs").setExpanded(true);
		} else {
			return createRootNode(root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), root.getDescription(), false, "wtcal-icon-root-incoming-xs");
		}
	}
	
	private ExtTreeNode createRootNode(String id, String pid, String rights, String text, boolean leaf, String iconClass) {
		boolean visible = checkedRoots.contains(id);
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_type", JsFolderNode.TYPE_ROOT);
		node.put("_pid", pid);
		node.put("_rrights", rights);
		node.put("_visible", visible);
		node.setIconClass(iconClass);
		node.setChecked(visible);
		return node;
	}
	
	private ExtTreeNode createFolderNode(CalendarFolder folder, SharePermsRoot rootPerms) {
		OCalendar cal = folder.getCalendar();
		String id = new CompositeId(folder.getShareId(), cal.getCalendarId()).toString();
		boolean visible = checkedFolders.contains(cal.getCalendarId());
		ExtTreeNode node = new ExtTreeNode(id, cal.getName(), true);
		node.put("_type", JsFolderNode.TYPE_FOLDER);
		node.put("_pid", cal.getProfileId().toString());
		node.put("_rrights", rootPerms.toString());
		node.put("_frights", folder.getPerms().toString());
		node.put("_erights", folder.getElementsPerms().toString());
		node.put("_calId", cal.getCalendarId());
		node.put("_builtIn", cal.getBuiltIn());
		node.put("_default", cal.getIsDefault());
		node.put("_color", cal.getColor());
		node.put("_visible", visible);
		node.put("_isPrivate", cal.getIsPrivate());
		node.put("_busy", cal.getBusy());
		node.put("_reminder", cal.getReminder());
		
		List<String> classes = new ArrayList<>();
		if(cal.getIsDefault()) classes.add("wtcal-tree-default");
		if(!folder.getElementsPerms().implies("CREATE") 
				&& !folder.getElementsPerms().implies("UPDATE")
				&& !folder.getElementsPerms().implies("DELETE")) classes.add("wtcal-tree-readonly");
		node.setCls(StringUtils.join(classes, " "));
		
		node.setIconClass("wt-palette-" + cal.getHexColor());
		node.setChecked(visible);
		return node;
	}
	
	private static class ErpExportWizard {
		public DateTime fromDate;
		public DateTime toDate;
		public File file;
		public String filename;
	}
}
