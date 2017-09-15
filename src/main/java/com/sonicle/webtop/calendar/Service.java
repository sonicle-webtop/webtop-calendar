/* 
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.DispositionType;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.ServletUtils.StringArray;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.extjs.FieldMeta;
import com.sonicle.commons.web.json.extjs.GridColumnMeta;
import com.sonicle.commons.web.json.extjs.GridMetadata;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.calendar.CalendarUserSettings.CheckedFolders;
import com.sonicle.webtop.calendar.CalendarUserSettings.CheckedRoots;
import com.sonicle.webtop.calendar.bol.model.SchedulerEventInstance;
import com.sonicle.webtop.calendar.bol.js.JsAttendee;
import com.sonicle.webtop.calendar.bol.js.JsAttendee.JsAttendeeList;
import com.sonicle.webtop.calendar.bol.js.JsCalendar;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEventDate;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsCalendarLkp;
import com.sonicle.webtop.calendar.bol.js.JsFolderNode;
import com.sonicle.webtop.calendar.bol.js.JsFolderNode.JsFolderNodeList;
import com.sonicle.webtop.calendar.bol.js.JsSharing;
import com.sonicle.webtop.calendar.bol.model.CalendarFolderData;
import com.sonicle.webtop.calendar.model.CalendarFolder;
import com.sonicle.webtop.calendar.model.CalendarRoot;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.bol.model.RBEventDetail;
import com.sonicle.webtop.calendar.model.EventKey;
import com.sonicle.webtop.calendar.bol.model.MyCalendarFolder;
import com.sonicle.webtop.calendar.bol.model.MyCalendarRoot;
import com.sonicle.webtop.calendar.bol.model.SetupDataCalendarRemote;
import com.sonicle.webtop.calendar.io.EventICalFileReader;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;
import com.sonicle.webtop.calendar.msg.RemoteSyncResult;
import com.sonicle.webtop.calendar.rpt.AbstractAgenda;
import com.sonicle.webtop.calendar.rpt.RptAgendaSummary;
import com.sonicle.webtop.calendar.rpt.RptEventsDetail;
import com.sonicle.webtop.calendar.rpt.RptAgendaWeek5;
import com.sonicle.webtop.calendar.rpt.RptAgendaWeek7;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsWizardData;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
import com.sonicle.webtop.core.sdk.AsyncActionCollection;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BaseServiceAsyncAction;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.LogEntries;
import com.sonicle.webtop.core.util.LogEntry;
import com.sonicle.webtop.core.util.MessageLogEntry;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
	private final HashMap<String, ArrayList<CalendarFolder>> foldersByRoot = new HashMap<>();
	private final LinkedHashMap<Integer, CalendarFolder> folders = new LinkedHashMap<>();
	private final AsyncActionCollection<Integer, SyncRemoteCalendarAA> syncRemoteCalendarAAs = new AsyncActionCollection<>();
	private CheckedRoots checkedRoots = null;
	private CheckedFolders checkedFolders = null;
	private ErpExportWizard erpWizard = null;

	@Override
	public void initialize() throws Exception {
		UserProfile up = getEnv().getProfile();
		manager = (CalendarManager)WT.getServiceManager(SERVICE_ID, false, up.getId());
		ss = new CalendarServiceSettings(SERVICE_ID, up.getDomainId());
		us = new CalendarUserSettings(SERVICE_ID, up.getId());
		initFolders();
	}
	
	@Override
	public void cleanup() throws Exception {
		synchronized(syncRemoteCalendarAAs) {
			syncRemoteCalendarAAs.clear();
		}
		checkedFolders.clear();
		checkedFolders = null;
		checkedRoots.clear();
		checkedRoots = null;
		folders.clear();
		foldersByRoot.clear();
		roots.clear();
		us = null;
		ss = null;
		manager = null;
	}
	
	@Override
	public ServiceVars returnServiceVars() {
		DateTimeFormatter hmf = DateTimeUtils.createHmFormatter();
		ServiceVars co = new ServiceVars();
		co.put("defaultCalendarSync", EnumUtils.toSerializedName(ss.getDefaultCalendarSync()));
		co.put("view", us.getView());
		co.put("workdayStart", hmf.print(us.getWorkdayStart()));
		co.put("workdayEnd", hmf.print(us.getWorkdayEnd()));
		return co;
	}
	
	private WebTopSession getWts() {
		return getEnv().getWebTopSession();
	}
	
	private void runSyncRemoteCalendar(int calendarId, String calendarName, boolean full) throws WTException {
		synchronized(syncRemoteCalendarAAs) {
			if (syncRemoteCalendarAAs.containsKey(calendarId)) {
				logger.debug("SyncRemoteCalendar run skipped [{}]", calendarId);
				return;
			}
			final SyncRemoteCalendarAA aa = new SyncRemoteCalendarAA(calendarId, calendarName, full);
			syncRemoteCalendarAAs.put(calendarId, aa);
			aa.start(RunContext.getSubject(), RunContext.getRunProfileId());
		}
	}
	
	private void initFolders() throws Exception {
		UserProfileId pid = getEnv().getProfile().getId();
		synchronized(roots) {
			updateRootFoldersCache();
			updateFoldersCache();

			checkedRoots = us.getCheckedCalendarRoots();
			// If empty, adds MyNode checked by default!
			if(checkedRoots.isEmpty()) {
				checkedRoots.add(MyCalendarRoot.SHARE_ID);
				us.setCheckedCalendarRoots(checkedRoots);
			}
			checkedFolders = us.getCheckedCalendarFolders();
		}
	}
	
	private void updateRootFoldersCache() throws WTException {
		UserProfileId pid = getEnv().getProfile().getId();
		synchronized(roots) {
			roots.clear();
			MyCalendarRoot myRoot = new MyCalendarRoot(pid);
			roots.put(myRoot.getShareId(), myRoot);
			for(CalendarRoot root : manager.listIncomingCalendarRoots()) {
				roots.put(root.getShareId(), root);
			}
		}
	}
	
	private void updateFoldersCache() throws WTException {
		synchronized(roots) {
			foldersByRoot.clear();
			folders.clear();
			for(CalendarRoot root : roots.values()) {
				foldersByRoot.put(root.getShareId(), new ArrayList<CalendarFolder>());
				if(root instanceof MyCalendarRoot) {
					for(Calendar cal : manager.listCalendars()) {
						final MyCalendarFolder fold = new MyCalendarFolder(root.getShareId(), cal);
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(cal.getCalendarId(), fold);
					}
				} else {
					for(CalendarFolder fold : manager.listIncomingCalendarFolders(root.getShareId()).values()) {
						final int calId = fold.getCalendar().getCalendarId();
						fold.setData(us.getCalendarFolderData(calId));
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(calId, fold);
					}
				}
			}
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
						for(Calendar cal : manager.listCalendars()) {
							MyCalendarFolder folder = new MyCalendarFolder(node, cal);
							children.add(createFolderNode(folder, root.getPerms()));
						}
					} else {
						/*
						HashMap<Integer, CalendarFolder> folds = manager.listIncomingCalendarFolders(root.getShareId());
						for(CalendarFolder fold : folds.values()) {
							children.add(createFolderNode(fold, root.getPerms()));
						}
						*/
						if(foldersByRoot.containsKey(root.getShareId())) {
							for(CalendarFolder fold : foldersByRoot.get(root.getShareId())) {
								final ExtTreeNode etn = createFolderNode(fold, root.getPerms());
								if (etn != null) children.add(etn);
							}
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
			logger.error("Error in ManageCalendarsTree", ex);
		}
	}
	
	public void processUpdateCheckedFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String rootId = ServletUtils.getStringParameter(request, "rootId", true);
			Boolean checked = ServletUtils.getBooleanParameter(request, "checked", true);
			
			synchronized(roots) {
				ArrayList<Integer> catIds = new ArrayList<>();
				for(CalendarFolder fold : foldersByRoot.get(rootId)) {
					catIds.add(fold.getCalendar().getCalendarId());
				}
				toggleCheckedFolders(catIds.toArray(new Integer[catIds.size()]), checked);
			}
			
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in UpdateCheckedFolders", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupCalendarRoots(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			Boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", true);
			
			synchronized(roots) {
				for(CalendarRoot root : roots.values()) {
					if(root instanceof MyCalendarRoot) {
						UserProfile up = getEnv().getProfile();
						items.add(new JsSimple(up.getStringId(), up.getDisplayName()));
					} else {
						//TODO: se writableOnly verificare che il gruppo condiviso sia scrivibile
						items.add(new JsSimple(root.getOwnerProfileId().toString(), root.getDescription()));
					}
				}
			}
			
			new JsonResult("roots", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupCalendarRoots", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupCalendarFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCalendarLkp> items = new ArrayList<>();
		
		try {
			synchronized(roots) {
				for(CalendarRoot root : roots.values()) {
					if (foldersByRoot.containsKey(root.getShareId())) {
						for(CalendarFolder fold : foldersByRoot.get(root.getShareId())) {
							items.add(new JsCalendarLkp(fold));
						}
					}
				}
			}
			
			new JsonResult("folders", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupCalendarFolders", ex);
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
			logger.error("Error in ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageHiddenCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String rootId = ServletUtils.getStringParameter(request, "rootId", true);
				if (rootId.equals(MyCalendarRoot.SHARE_ID)) throw new WTException();
				
				ArrayList<JsSimple> items = new ArrayList<>();
				synchronized(roots) {
					for(CalendarFolder fold : foldersByRoot.get(rootId)) {
						CalendarFolderData data = (CalendarFolderData)fold.getData();
						if (data != null) {
							if ((data.hidden != null) && data.hidden) {
								items.add(new JsSimple(fold.getCalendar().getCalendarId(), fold.getCalendar().getName()));
							}
						}
					}
				}
				new JsonResult(items).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Integer categoryId = ServletUtils.getIntParameter(request, "calendarId", true);
				Boolean hidden = ServletUtils.getBooleanParameter(request, "hidden", false);
				
				updateCalendarFolderVisibility(categoryId, hidden);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				
				HashSet<String> pids = new HashSet<>();
				synchronized(roots) {
					for(String id : ids) {
						int categoryId = Integer.valueOf(id);
						CalendarFolder fold = folders.get(categoryId);
						if (fold != null) {
							updateCalendarFolderVisibility(categoryId, null);
							pids.add(fold.getCalendar().getProfileId().toString());
						}
					}
				}
				new JsonResult(pids).printTo(out);
			}
			
		} catch(Exception ex) {
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Calendar item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				
				item = manager.getCalendar(id);
				new JsonResult(new JsCalendar(item)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				item = manager.addCalendar(JsCalendar.createCalendar(pl.data, null));
				updateFoldersCache();
				toggleCheckedFolder(item.getCalendarId(), true);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				item = manager.getCalendar(pl.data.calendarId);
				if (item == null) throw new WTException("Calendar not found [{0}]", pl.data.calendarId);
				manager.updateCalendar(JsCalendar.createCalendar(pl.data, item.getParameters()));
				updateFoldersCache();
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsCalendar> pl = ServletUtils.getPayload(request, JsCalendar.class);
				
				manager.deleteCalendar(pl.data.calendarId);
				updateFoldersCache();
				toggleCheckedFolder(pl.data.calendarId, false);
				new JsonResult().printTo(out);
				
			} else if (crud.equals("sync")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				boolean full = ServletUtils.getBooleanParameter(request, "full", false);
				
				item = manager.getCalendar(id);
				if (item == null) throw new WTException("Calendar not found [{0}]", id);
				runSyncRemoteCalendar(id, item.getName(), full);
				
				new JsonResult().printTo(out);
			}
			
		} catch(Throwable t) {
			logger.error("Error in ManageCalendars", t);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processSetupCalendarRemote(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = getEnv().getWebTopSession();
		final String PROPERTY_PREFIX = "setupremotecalendar-";
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals("s1")) {
				String tag = ServletUtils.getStringParameter(request, "tag", true);
				String profileId = ServletUtils.getStringParameter(request, "profileId", true);
				String provider = ServletUtils.getStringParameter(request, "provider", true);
				String url = ServletUtils.getStringParameter(request, "url", true);
				String username = ServletUtils.getStringParameter(request, "username", null);
				String password = ServletUtils.getStringParameter(request, "password", null);
				
				Calendar.Provider remoteProvider = EnumUtils.forSerializedName(provider, Calendar.Provider.class);
				URI uri = URIUtils.createURI(url);
				
				CalendarManager.ProbeCalendarRemoteUrlResult result = manager.probeCalendarRemoteUrl(remoteProvider, uri, username, password);
				if (result == null) throw new WTException("URL problem");
				
				SetupDataCalendarRemote setup = new SetupDataCalendarRemote();
				setup.setProfileId(profileId);
				setup.setProvider(remoteProvider);
				setup.setUrl(uri);
				setup.setUsername(username);
				setup.setPassword(password);
				setup.setName(result.displayName);
				wts.setProperty(SERVICE_ID, PROPERTY_PREFIX+tag, setup);
				
				new JsonResult(setup).printTo(out);
				
			} else if(crud.equals("s2")) {
				String tag = ServletUtils.getStringParameter(request, "tag", true);
				String name = ServletUtils.getStringParameter(request, "name", true);
				String color = ServletUtils.getStringParameter(request, "color", true);
				
				wts.hasPropertyOrThrow(SERVICE_ID, PROPERTY_PREFIX+tag);
				SetupDataCalendarRemote setup = (SetupDataCalendarRemote) wts.getProperty(SERVICE_ID, PROPERTY_PREFIX+tag);
				setup.setName(name);
				setup.setColor(color);
				
				Calendar cal = manager.addCalendar(setup.toCalendar());
				wts.clearProperty(SERVICE_ID, PROPERTY_PREFIX+tag);
				updateFoldersCache();
				toggleCheckedFolder(cal.getCalendarId(), true);
				runSyncRemoteCalendar(cal.getCalendarId(), cal.getName(), true); // Starts a full-sync
				
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in SetupStoreFtp", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processSetCalendarColor(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String color = ServletUtils.getStringParameter(request, "color", null);

			updateCalendarFolderColor(id, color);

			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processGetSchedulerDates(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSchedulerEventDate> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			DateTimeFormatter ymdZoneFmt = DateTimeUtils.createYmdFormatter(utz);
			
			// Defines boundaries
			String start = ServletUtils.getStringParameter(request, "startDate", true);
			String end = ServletUtils.getStringParameter(request, "endDate", true);
			DateTime fromDate = DateTimeUtils.parseYmdHmsWithZone(start, "00:00:00", up.getTimeZone());
			DateTime toDate = DateTimeUtils.parseYmdHmsWithZone(end, "23:59:59", up.getTimeZone());
			
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
			logger.error("Error in GetSchedulerDates", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
	
	public void processManageEventsScheduler(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSchedulerEvent> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String from = ServletUtils.getStringParameter(request, "startDate", true);
				String to = ServletUtils.getStringParameter(request, "endDate", true);
				
				// Defines view boundary 
				DateTime fromDate = DateTimeUtils.parseYmdHmsWithZone(from, "00:00:00", up.getTimeZone());
				DateTime toDate = DateTimeUtils.parseYmdHmsWithZone(to, "23:59:59", up.getTimeZone());
				
				// Get events for each visible folder
				List<SchedulerEventInstance> recInstances = null;
				List<CalendarManager.CalendarEvents> foldEvents = null;
				Integer[] checked = getCheckedFolders();
				for(CalendarRoot root : getCheckedRoots()) {
					foldEvents = manager.listSchedulerEvents(root, checked, fromDate, toDate);
					
					for(CalendarManager.CalendarEvents ce : foldEvents) {
						CalendarFolder fold = folders.get(ce.calendar.getCalendarId());
						if(fold == null) continue;
						
						for(SchedulerEventInstance evt : ce.events) {
							if(evt.getRecurrenceId() == null) {
								items.add(new JsSchedulerEvent(root, fold, evt, up.getId(), utz));
							} else {
								recInstances = manager.calculateRecurringInstances(evt, fromDate, toDate, utz);
								for(SchedulerEventInstance recInstance : recInstances) {
									items.add(new JsSchedulerEvent(root, fold, recInstance, up.getId(), utz));
								}
							}
						}
					}
				}
				new JsonResult("events", items).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsSchedulerEvent> pl = ServletUtils.getPayload(request, JsSchedulerEvent.class);
				
				DateTimeZone etz = DateTimeZone.forID(pl.data.timezone);
				DateTime newStart = DateTimeUtils.parseYmdHmsWithZone(pl.data.startDate, etz);
				DateTime newEnd = DateTimeUtils.parseYmdHmsWithZone(pl.data.endDate, etz);
				manager.cloneEventInstance(EventKey.buildKey(pl.data.eventId, pl.data.originalEventId), newStart, newEnd);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsSchedulerEvent.Update> pl = ServletUtils.getPayload(request, JsSchedulerEvent.Update.class);
				
				DateTimeZone etz = DateTimeZone.forID(pl.data.timezone);
				DateTime newStart = DateTimeUtils.parseYmdHmsWithZone(pl.data.startDate, etz);
				DateTime newEnd = DateTimeUtils.parseYmdHmsWithZone(pl.data.endDate, etz);
				manager.updateEventInstance(pl.data.id, newStart, newEnd, pl.data.title);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				String uid = ServletUtils.getStringParameter(request, "id", true);
				String target = ServletUtils.getStringParameter(request, "target", "this");
				
				manager.deleteEventInstance(target, uid);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("restore")) {
				String uid = ServletUtils.getStringParameter(request, "id", true);
				
				manager.restoreEventInstance(uid);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("search")) {
				String query = ServletUtils.getStringParameter(request, "query", true);
				
				List<CalendarManager.CalendarEvents> calEvts = null;
				Integer[] checked = getCheckedFolders();
				for(CalendarRoot root : getCheckedRoots()) {
					calEvts = manager.searchSchedulerEvents(root.getOwnerProfileId(), checked, "%"+query+"%");
					// Iterates over calendar->events
					for(CalendarManager.CalendarEvents ce : calEvts) {
						CalendarFolder fold = folders.get(ce.calendar.getCalendarId());
                        if (fold == null) continue;
						
						for(SchedulerEventInstance evt : ce.events) {
							if(evt.getRecurrenceId() == null) {
								items.add(new JsSchedulerEvent(root, fold, evt, up.getId(), utz));
							}
						}
					}
				}
				new JsonResult("events", items).printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageEventsScheduler", ex);
			new JsonResult(false, "Error").printTo(out);	
		}
	}
	
	public void processManageEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsEvent item = null;
		
		try {
			UserProfile up = getEnv().getProfile();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String eventKey = ServletUtils.getStringParameter(request, "id", true);
				
				EventInstance evt = manager.getEventInstance(eventKey);
				UserProfileId ownerId = manager.getCalendarOwner(evt.getCalendarId());
				item = new JsEvent(evt, ownerId.toString());
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				//TODO: verificare che il calendario supporti la scrittura (specialmente per quelli condivisi)
				
				EventInstance evt = JsEvent.buildEventInstance(pl.data);
				CoreManager core = WT.getCoreManager();
				evt.setOrganizer(core.getUserData().getFullEmailAddress());
				
				manager.addEvent(evt);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				String target = ServletUtils.getStringParameter(request, "target", "this");
				Payload<MapItem, JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				EventInstance evt = JsEvent.buildEventInstance(pl.data);
				manager.updateEventInstance(target, evt);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.MOVE)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				Integer calendarId = ServletUtils.getIntParameter(request, "targetCalendarId", true);
				boolean copy = ServletUtils.getBooleanParameter(request, "copy", false);
				
				manager.moveEventInstance(copy, id, calendarId);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageEvents", ex);
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
						CalendarFolder fold = folders.get(ce.calendar.getCalendarId());
                        if (fold == null) continue;
						
						for(SchedulerEventInstance evt : ce.events) {
							if(evt.getRecurrenceId() == null) {
								items.add(new JsSchedulerEvent(root, fold, evt, up.getId(), utz));
							}
						}
					}
				}
				new JsonResult("events", items).printTo(out);
			}
		
		} catch(Exception ex) {
			logger.error("Error in ManageGridEvents", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
	
	public void processExportForErp(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String op = ServletUtils.getStringParameter(request, "op", true);
			if (op.equals("do")) {
				String fromDate = ServletUtils.getStringParameter(request, "fromDate", true);
				String toDate = ServletUtils.getStringParameter(request, "toDate", true);
				
				DateTimeFormatter ymd = DateTimeUtils.createYmdFormatter(up.getTimeZone());
				erpWizard = new ErpExportWizard();
				erpWizard.fromDate = ymd.parseDateTime(fromDate).withTimeAtStartOfDay();
				erpWizard.toDate = DateTimeUtils.withTimeAtEndOfDay(ymd.parseDateTime(toDate));
				
				LogEntries log = new LogEntries();
				File file = WT.createTempFile();
				
				try {
					DateTimeFormatter ymd2 = DateTimeUtils.createFormatter("yyyyMMdd", up.getTimeZone());
					DateTimeFormatter ymdhms = DateTimeUtils.createFormatter("yyyy-MM-dd HH:mm:ss", up.getTimeZone());
					
					try (FileOutputStream fos = new FileOutputStream(file)) {
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Started on {0}", ymdhms.print(new DateTime())));
						manager.exportEvents(log, erpWizard.fromDate, erpWizard.toDate, fos);
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Ended on {0}", ymdhms.print(new DateTime())));
						erpWizard.file = file;
						erpWizard.filename = MessageFormat.format(ERP_EXPORT_FILENAME, up.getDomainId(), ymd2.print(erpWizard.fromDate), ymd2.print(erpWizard.fromDate), "csv");
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "File ready: {0}", erpWizard.filename));
						log.addMaster(new MessageLogEntry(LogEntry.Level.INFO, "Operation completed succesfully"));
						new JsonResult(new JsWizardData(log.print())).printTo(out);
					}
				} catch(Throwable t) {
					logger.error("Error generating export", t);
					file.delete();
					new JsonResult(new JsWizardData(log.print())).setSuccess(false).printTo(out);
				}	
			}
			
		} catch(Exception ex) {
			logger.error("Error in ExportForErp", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processExportForErp(HttpServletRequest request, HttpServletResponse response) {
		try {
			try(FileInputStream fis = new FileInputStream(erpWizard.file)) {
				OutputStream os = response.getOutputStream();
				ServletUtils.setFileStreamHeaders(response, "application/octet-stream", DispositionType.ATTACHMENT, erpWizard.filename);
				ServletUtils.setContentLengthHeader(response, erpWizard.file.length());
				IOUtils.copy(fis, os);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ExportForErp", ex);
		} finally {
			erpWizard = null;
		}
	}
	
	public void processGetPlanning(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreUserSettings cus = getEnv().getCoreUserSettings();
		CoreManager core = WT.getCoreManager();
		ArrayList<MapItem> items = new ArrayList<>();
		Connection con = null;
		
		try {
			String eventStartDate = ServletUtils.getStringParameter(request, "startDate", true);
			String eventEndDate = ServletUtils.getStringParameter(request, "endDate", true);
			String timezone = ServletUtils.getStringParameter(request, "timezone", true);
			JsAttendeeList attendees = ServletUtils.getObjectParameter(request, "attendees", new JsAttendeeList(), JsAttendeeList.class);
			
			// Parses string parameters
			DateTimeZone eventTz = DateTimeZone.forID(timezone);
			DateTime eventStartDt = DateTimeUtils.parseYmdHmsWithZone(eventStartDate, eventTz);
			DateTime eventEndDt = DateTimeUtils.parseYmdHmsWithZone(eventEndDate, eventTz);
			
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
			ArrayList<FieldMeta> fields = new ArrayList<>();
			ArrayList<GridColumnMeta> colsInfo = new ArrayList<>();
			
			GridColumnMeta col = null;
			fields.add(new FieldMeta("recipient"));
			colsInfo.add(new GridColumnMeta("recipient"));
			for(String hourKey : hours) {
				LocalDateTime ldt = ymdhmFmt.parseLocalDateTime(hourKey);
				fields.add(new FieldMeta(hourKey));
				col = new GridColumnMeta(hourKey, tFmt.print(ldt));
				col.put("date", dFmt.print(ldt));
				col.put("overlaps", DateTimeUtils.between(ldt, eventStartDt.toLocalDateTime(), eventEndDt.toLocalDateTime()));
				colsInfo.add(col);
			}
			
			// Collects attendees availability...
			OUser user = null;
			UserProfileId profileId = null;
			LinkedHashSet<String> busyHours = null;
			MapItem item = null;
			for(JsAttendee attendee : attendees) {
				item = new MapItem();
				item.put("recipient", attendee.recipient);
				
				user = guessUserByAttendee(core, attendee.recipient);
				if (user != null) {
					profileId = new UserProfileId(user.getDomainId(), user.getUserId());
					busyHours = manager.calculateAvailabilitySpans(60, profileId, eventStartDt.withTime(fromTime), eventEndDt.withTime(toTime), eventTz, true);
					for(String hourKey : hours) {
						item.put(hourKey, busyHours.contains(hourKey) ? "busy" : "free");
					}
				} else {
					for(String hourKey : hours) {
						item.put(hourKey, "unknown");
					}
				}
				items.add(item);
			}
			
			GridMetadata meta = new GridMetadata(true);
			meta.setFields(fields);
			meta.setColumnsInfo(colsInfo);
			new JsonResult(items, meta, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in ManageEvents", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processImportEventsFromICal(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String uploadId = ServletUtils.getStringParameter(request, "uploadId", true);
			String op = ServletUtils.getStringParameter(request, "op", true);
			
			UploadedFile upl = getUploadedFile(uploadId);
			if(upl == null) throw new WTException("Uploaded file not found [{0}]", uploadId);
			File file = new File(WT.getTempFolder(), upl.getUploadId());
			
			EventICalFileReader rea = new EventICalFileReader(up.getTimeZone());
			
			if(op.equals("do")) {
				Integer calendarId = ServletUtils.getIntParameter(request, "calendarId", true);
				String mode = ServletUtils.getStringParameter(request, "importMode", true);
				
				LogEntries log = manager.importEvents(calendarId, rea, file, mode);
				removeUploadedFile(uploadId);
				new JsonResult(new JsWizardData(log.print())).printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ImportContactsFromICal", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processPrintScheduler(HttpServletRequest request, HttpServletResponse response) {
		ByteArrayOutputStream baos = null;
		UserProfile up = getEnv().getProfile();
		
		try {
			String filename = ServletUtils.getStringParameter(request, "filename", "print");
			String view = ServletUtils.getStringParameter(request, "view", "w5");
			String from = ServletUtils.getStringParameter(request, "startDate", true);
			
			DateTime startDate = DateTimeUtils.parseYmdHmsWithZone(from, "00:00:00", up.getTimeZone());
			
			ReportConfig.Builder builder = reportConfigBuilder();
			DateTime fromDate = null, toDate = null;
			AbstractAgenda rpt = null;
			if(view.equals("d")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(1).withTimeAtStartOfDay();
				rpt = new RptAgendaSummary(builder.build(), 1);
				
			} else if(view.equals("w5")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(5).withTimeAtStartOfDay();
				rpt = new RptAgendaWeek5(builder.build());
				
			} else if(view.equals("w")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(7).withTimeAtStartOfDay();
				rpt = new RptAgendaWeek7(builder.build());
				
			} else if(view.equals("dw")) {
				fromDate = startDate.withTimeAtStartOfDay();
				toDate = startDate.plusDays(14).withTimeAtStartOfDay();
				rpt = new RptAgendaSummary(builder.build(), 14);
				
			} else if(view.equals("m")) {
				if(startDate.getDayOfMonth() == 1) {
					fromDate = startDate.withTimeAtStartOfDay();
				} else {
					fromDate = startDate.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();
				}
				int days = fromDate.dayOfMonth().getMaximumValue();
				toDate = fromDate.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay();
				rpt = new RptAgendaSummary(builder.build(), days);
				
			} else {
				throw new WTException("View not supported [{0}]", view);
			}
			
			// Get events for each visible folder
			List<CalendarManager.CalendarEvents> foldEvents = new ArrayList<>();
			Integer[] checked = getCheckedFolders();
			for(CalendarRoot root : getCheckedRoots()) {
				foldEvents.addAll(manager.listSchedulerEvents(root, checked, fromDate, toDate));
			}
			rpt.setDataSource(manager, fromDate, toDate, up.getTimeZone(), foldEvents);
			
			baos = new ByteArrayOutputStream();
			WT.generateReportToStream(rpt, AbstractReport.OutputType.PDF, baos);
			ServletUtils.setContentDispositionHeader(response, "inline", filename + ".pdf");
			ServletUtils.writeContent(response, baos, "application/pdf");
			
		} catch(Exception ex) {
			logger.error("Error in PrintScheduler", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		} finally {
			IOUtils.closeQuietly(baos);
		}
	}
	
	public void processPrintEventsDetail(HttpServletRequest request, HttpServletResponse response) {
		ArrayList<RBEventDetail> items = new ArrayList<>();
		ByteArrayOutputStream baos = null;
		CoreManager core = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			String filename = ServletUtils.getStringParameter(request, "filename", "print");
			StringArray keys = ServletUtils.getObjectParameter(request, "keys", StringArray.class, true);
			RRuleStringify rrs = new RRuleStringify();
			rrs.setTimeZone(up.getTimeZone());
			//rrs.setDateFormat();
			//rrs.setTimeFormat();
			
			EventInstance event = null;
			Calendar calendar = null;
			for(String key : keys) {
				event = manager.getEventInstance(key);
				calendar = manager.getCalendar(event.getCalendarId());
				items.add(new RBEventDetail(core, rrs, calendar, event));
			}
			
			ReportConfig.Builder builder = reportConfigBuilder();
			RptEventsDetail rpt = new RptEventsDetail(builder.build());
			rpt.setDataSource(items);
			
			baos = new ByteArrayOutputStream();
			WT.generateReportToStream(rpt, AbstractReport.OutputType.PDF, baos);
			ServletUtils.setContentDispositionHeader(response, "inline", filename + ".pdf");
			ServletUtils.writeContent(response, baos, "application/pdf");
			
		} catch(Exception ex) {
			logger.error("Error in PrintContacts", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		} finally {
			IOUtils.closeQuietly(baos);
		}
	}
	
	private ReportConfig.Builder reportConfigBuilder() {
		UserProfile.Data ud = getEnv().getProfile().getData();
		CoreUserSettings cus = getEnv().getCoreUserSettings();
		return new ReportConfig.Builder()
				.useLocale(ud.getLocale())
				.useTimeZone(ud.getTimeZone().toTimeZone())
				.dateFormatShort(cus.getShortDateFormat())
				.dateFormatLong(cus.getLongDateFormat())
				.timeFormatShort(cus.getShortTimeFormat())
				.timeFormatLong(cus.getLongTimeFormat())
				.generatedBy(WT.getPlatformName() + " " + lookupResource(CalendarLocale.SERVICE_NAME))
				.printedBy(ud.getDisplayName());
	}
	
	private OUser guessUserByAttendee(CoreManager core, String recipient) {
		Connection con = null;
		
		try {
			InternetAddress ia = MailUtils.buildInternetAddress(recipient);
			if (ia == null) return null;
			List<UserProfileId> pids = core.listUserIdsByEmail(recipient);
			if (pids.isEmpty()) return null;
			UserProfileId pid = pids.get(0);
			
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			return udao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
		
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
			Calendar calendar = manager.getCalendar(calId);
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
		toggleCheckedRoots(new String[]{shareId}, checked);
	}
	
	private void toggleCheckedRoots(String[] shareIds, boolean checked) {
		synchronized(roots) {
			for(String shareId : shareIds) {
				if(checked) {
					checkedRoots.add(shareId);
				} else {
					checkedRoots.remove(shareId);
				}
			}	
			us.setCheckedCalendarRoots(checkedRoots);
		}
	}
	
	private void toggleCheckedFolder(Integer folderId, boolean checked) {
		toggleCheckedFolders(new Integer[]{folderId}, checked);
	}
	
	private void toggleCheckedFolders(Integer[] folderIds, boolean checked) {
		synchronized(roots) {
			for(int folderId : folderIds) {
				if(checked) {
					checkedFolders.add(folderId);
				} else {
					checkedFolders.remove(folderId);
				}
			}
			us.setCheckedCalendarFolders(checkedFolders);
		}
	}
	
	private void updateCalendarFolderColor(int calendarId, String color) {
		synchronized(roots) {
			CalendarFolderData data = us.getCalendarFolderData(calendarId);
			data.color = color;
			if (!data.isNull()) {
				us.setCalendarFolderData(calendarId, data);
			} else {
				us.clearCalendarFolderData(calendarId);
			}
			
			// Update internal cache
			CalendarFolder folder = folders.get(calendarId);
			if (!(folder instanceof MyCalendarFolder)) {
				((CalendarFolderData)folder.getData()).update(data);
			}
		}
	}
	
	private void updateCalendarFolderVisibility(int calendarId, Boolean hidden) {
		synchronized(roots) {
			CalendarFolderData data = us.getCalendarFolderData(calendarId);
			data.hidden = hidden;
			if (!data.isNull()) {
				us.setCalendarFolderData(calendarId, data);
			} else {
				us.clearCalendarFolderData(calendarId);
			}
			
			// Update internal cache
			CalendarFolder folder = folders.get(calendarId);
			if (!(folder instanceof MyCalendarFolder)) {
				((CalendarFolderData)folder.getData()).update(data);
			}
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
		Calendar cal = folder.getCalendar();
		String id = new CompositeId().setTokens(folder.getShareId(), cal.getCalendarId()).toString();
		String color = cal.getColor();
		boolean visible = checkedFolders.contains(cal.getCalendarId());
		
		if (folder.getData() != null) {
			CalendarFolderData data = (CalendarFolderData)folder.getData();
			if ((data.hidden != null) && data.hidden) return null;
			if (!StringUtils.isBlank(data.color)) color = data.color;
		}
		
		ExtTreeNode node = new ExtTreeNode(id, cal.getName(), true);
		node.put("_type", JsFolderNode.TYPE_FOLDER);
		node.put("_pid", cal.getProfileId().toString());
		node.put("_rrights", rootPerms.toString());
		node.put("_frights", folder.getPerms().toString());
		node.put("_erights", folder.getElementsPerms().toString());
		node.put("_calId", cal.getCalendarId());
		node.put("_builtIn", cal.getBuiltIn());
		node.put("_provider", EnumUtils.toSerializedName(cal.getProvider()));
		node.put("_default", cal.getIsDefault());
		node.put("_color", Calendar.getHexColor(color));
		node.put("_visible", visible);
		node.put("_isPrivate", cal.getIsPrivate());
		node.put("_defBusy", cal.getDefaultBusy());
		node.put("_defReminder", cal.getDefaultReminder());
		
		List<String> classes = new ArrayList<>();
		if(cal.getIsDefault()) classes.add("wtcal-tree-default");
		if(!folder.getElementsPerms().implies("CREATE") 
				&& !folder.getElementsPerms().implies("UPDATE")
				&& !folder.getElementsPerms().implies("DELETE")) classes.add("wtcal-tree-readonly");
		node.setCls(StringUtils.join(classes, " "));
		
		node.setIconClass("wt-palette-" + Calendar.getHexColor(color));
		node.setChecked(visible);
		return node;
	}
	
	private class SyncRemoteCalendarAA extends BaseServiceAsyncAction {
		private final int calendarId;
		private final String calendarName;
		private final boolean full;
		
		public SyncRemoteCalendarAA(int calendarId, String calendarName, boolean full) {
			super();
			setName(this.getClass().getSimpleName());
			this.calendarId = calendarId;
			this.calendarName = calendarName;
			this.full = full;
		}

		@Override
		public void executeAction() {
			try {
				manager.syncRemoteCalendar(calendarId, full);
				this.completed();
				getWts().notify(new RemoteSyncResult()
					.setCalendarId(calendarId)
					.setCalendarName(calendarName)
					.setSuccess(true)
				);
				
			} catch(WTException ex) {
				logger.error("Remote sync failed", ex);
				getWts().notify(new RemoteSyncResult()
					.setCalendarId(calendarId)
					.setCalendarName(calendarName)
					.setThrowable(ex, true)
				);
			} finally {
				synchronized(syncRemoteCalendarAAs) {
					syncRemoteCalendarAAs.remove(calendarId);
				}
			}
		}
	}
	
	private static class ErpExportWizard {
		public DateTime fromDate;
		public DateTime toDate;
		public File file;
		public String filename;
	}
}
