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
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.JsonUtils;
import com.sonicle.commons.web.json.JsListPayload;
import com.sonicle.commons.web.json.JsPayload;
import com.sonicle.commons.web.json.JsPayloadRecord;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.calendar.CalendarUserSettings.CheckedCalendarGroups;
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.bol.js.JsCalEventDate;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent1;
import com.sonicle.webtop.calendar.bol.js.JsTreeCalendar;
import com.sonicle.webtop.calendar.bol.js.JsTreeCalendar.JsTreeCalendarList;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import static com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsValue;
import com.sonicle.webtop.core.dal.BaseDAO.FieldsMap;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BasicEnvironment;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jooq.Field;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	
	public static final Logger logger = BaseService.getLogger(Service.class);
	
	private BasicEnvironment env = null;
	private CalendarManager manager;
	private CalendarUserSettings cus;
	
	
	public final String DEFAULT_PERSONAL_CALENDAR_COLOR = "#FFFFFF";
	
	private final LinkedHashMap<String, CalendarGroup> calendarGroups = new LinkedHashMap<>();
	private CheckedCalendarGroups checkedCalendarGroups = null;

	@Override
	public void initialize() {
		env = getEnv();
		UserProfile profile = env.getProfile();
		manager = new CalendarManager(getManifest());
		cus = new CalendarUserSettings(profile.getDomainId(), profile.getUserId(), getId());
		
		// Loads available groups
		initCalendarGroups();
	}
	
	private void initCalendarGroups() {
		Connection con = null;
		
		try {
			con = getConnection();
			UserProfile.Id pid = env.getProfile().getId();
			synchronized(calendarGroups) {
				calendarGroups.clear();
				calendarGroups.putAll(manager.getCalendarGroups(con, pid));
				checkedCalendarGroups = cus.getCheckedCalendarGroups();
				if(checkedCalendarGroups.isEmpty()) {
					// If empty, adds MyGroup checked by default!
					checkedCalendarGroups.add(pid.toString());
					cus.setCheckedCalendarGroups(checkedCalendarGroups);
				}
			}	

		} catch(SQLException ex) {
			logger.error("Error initializing calendar groups", ex);
			//TODO: gestire errore
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void toggleCheckedCalendarGroup(String groupId, boolean checked) {
		synchronized(calendarGroups) {
			if(checked) {
				checkedCalendarGroups.add(groupId);
			} else {
				checkedCalendarGroups.remove(groupId);
			}
			cus.setCheckedCalendarGroups(checkedCalendarGroups);
		}
	}
	
	@Override
	public void cleanup() {
		
	}
	
	@Override
	public HashMap<String, Object> returnClientOptions() {
		UserProfile profile = env.getProfile();
		HashMap<String, Object> hm = new HashMap<>();
		hm.put("view", cus.getCalendarView());
		hm.put("startDay", cus.getCalendarStartDay());
		return hm;
	}
	
	public void processManageCalendarsTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		ExtTreeNode child = null;
		
		try {
			con = getConnection();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				if(node.equals("root")) { // Node: root -> list groups
					
					MyCalendarGroup myGroup = null;
					SharedCalendarGroup sharedGroup = null;
					for(CalendarGroup group : calendarGroups.values()) {
						if(group instanceof MyCalendarGroup) { // Adds group as Mine
							myGroup = (MyCalendarGroup)group;
							child = createCalendarGroupNode(myGroup, false);
							children.add(child.setExpanded(true));
							
						} else if(group instanceof SharedCalendarGroup) { // Adds group as Shared
							sharedGroup = (SharedCalendarGroup)group;
							child = createCalendarGroupNode(sharedGroup, true);
							children.add(child);
						}
					}

				} else { // Node: group -> list group's calendars
					UserProfile.Id upId = new UserProfile.Id(node);
					List<OCalendar> cals = manager.getCalendars(upId);
					
					for(OCalendar cal : cals) children.add(createCalendarNode(node, cal));
				}
				new JsonResult("children", children).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				JsListPayload<JsTreeCalendarList> pl = ServletUtils.getPayloadAsList(request, JsTreeCalendarList.class);
				CalendarDAO cdao = CalendarDAO.getInstance();
				
				for(JsTreeCalendar cal : pl.data) {
					if(cal._nodeType.equals("group")) {
						toggleCheckedCalendarGroup(cal.id, cal._visible);
					} else if(cal._nodeType.equals("calendar")) {
						cdao.updateVisible(con, Integer.valueOf(cal.id), cal._visible);
					}
				}
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				JsListPayload<JsTreeCalendarList> pl = ServletUtils.getPayloadAsList(request, JsTreeCalendarList.class);
				
				CalendarDAO cdao = CalendarDAO.getInstance();
				for(JsTreeCalendar cal : pl.data) {
					if(cal._nodeType.equals("calendar")) {
						cdao.delete(con, Integer.valueOf(cal.id));
					}
				}
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageCalendarsTree", ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processGetEventDates(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		ArrayList<JsCalEventDate> items = new ArrayList<>();
		List<OEvent> grpEvts = null;
		
		try {
			con = getConnection();
			UserProfile up = env.getProfile();
			
			// Defines boundaries
			String start = ServletUtils.getStringParameter(request, "startDate", true);
			String end = ServletUtils.getStringParameter(request, "endDate", true);
			DateTime fromDate = OEvent.parseYmdHmsWithZone(start, "00:00:00", up.getTimeZone());
			DateTime toDate = OEvent.parseYmdHmsWithZone(end, "23:59:59", up.getTimeZone());
			
			// Get events for each visible group
			List<DateTime> dates = null;
			for(CalendarGroup group : calendarGroups.values()) {
				if(!checkedCalendarGroups.contains(group.getId())) continue; // Skip if not visible
				
				dates = manager.getEventsDates(group, fromDate, toDate, up.getTimeZone());
				for(DateTime dt : dates) {
					items.add(new JsCalEventDate(JsSchedulerEvent.toYmdWithZone(dt, up.getTimeZone())));
				}
			}
			new JsonResult("dates", items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageEventsView", ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processManageEventsScheduler(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		ArrayList<JsSchedulerEvent> items = new ArrayList<>();
		
		try {
			con = getConnection();
			UserProfile up = env.getProfile();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String from = ServletUtils.getStringParameter(request, "startDate", true);
				String to = ServletUtils.getStringParameter(request, "endDate", true);
				DateTime fromDate = OEvent.parseYmdHmsWithZone(from, "00:00:00", up.getTimeZone());
				DateTime toDate = OEvent.parseYmdHmsWithZone(to, "23:59:59", up.getTimeZone());
				
				// Get events for each visible group
				JsSchedulerEvent jse = null;
				List<OEvent> expEvents = null;
				List<CalendarManager.GroupEvents> grpEvts = null;
				for(CalendarGroup group : calendarGroups.values()) {
					if(!checkedCalendarGroups.contains(group.getId())) continue; // Skip if not visible
					
					grpEvts = manager.getEvents(con, group, fromDate, toDate);
					for(CalendarManager.GroupEvents ge : grpEvts) {
						for(OEvent evt : ge.events) {
							if(evt.getRecurrenceId() == null) {
								jse = new JsSchedulerEvent(evt, ge.calendar, up.getTimeZone());
								items.add(jse);
							} else {
								expEvents = manager.expandRecurringEvent(con, ge.calendar, evt, fromDate, toDate, up.getTimeZone());
								int count = 1;
								for(OEvent expEvent : expEvents) {
									jse = new JsSchedulerEvent(expEvent, ge.calendar, up.getTimeZone());
									jse.id = JsonUtils.buildRecId(expEvent.getEventId(), count);
									jse.isRecurring = true;
									items.add(jse);
									count++;
								}
								//TODO: valutare se generalizzare la chiamata costruendo qui il JsSchedulerEvent  
								//items.addAll(manager.expandRecurringEvent2(con, ge.calendar, evt, fromDate, toDate, up.getTimeZone()));
							}
						}
					}
				}
				
				new JsonResult("events", items).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				JsPayload<JsSchedulerEvent> pl = ServletUtils.getPayload(request, JsSchedulerEvent.class);
				
				EventDAO edao = EventDAO.getInstance();
				OEvent event = edao.select(con, pl.data.eventId);
				if(event == null) throw new Exception("Unable to get desired event");
				
				event.updateDates(pl.data.startDate, pl.data.endDate, up.getTimeZone());
				edao.update(con, event);
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageEventsView", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processManageCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		OCalendar item = null;
		
		try {
			CalendarDAO cdao = CalendarDAO.getInstance();
			con = getConnection();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", true);
				
				item = cdao.select(con, id);
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				JsPayload<OCalendar> pl = ServletUtils.getPayload(request, OCalendar.class);
				
				pl.data.setCalendarId(cdao.getSequence(con).intValue());
				pl.data.setBuiltIn(false);
				pl.data.setVisible(true);
				cdao.insert(con, pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				JsPayload<OCalendar> pl = ServletUtils.getPayload(request, OCalendar.class);
				
				cdao.update(con, pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				JsPayload<OCalendar> pl = ServletUtils.getPayload(request, OCalendar.class);
				
				cdao.delete(con, pl.data.getCalendarId());
				//TODO: cancellare eventi collegati
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageCalendars", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processGetCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		List<OCalendar> items = null;
		
		try {
			CalendarDAO cdao = CalendarDAO.getInstance();
			con = getConnection();
			
			String groupId = ServletUtils.getStringParameter(request, "groupId", true);
			if(calendarGroups.containsKey(groupId)) {
				CalendarGroup group = calendarGroups.get(groupId);
				items = cdao.selectByDomainUser(con, group.getDomainId(), group.getUserId());
			} else {
				items = new ArrayList<>();
			}
			new JsonResult("calendars", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error executing action GetCalendars", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processManageEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		OEvent event = null;
		ORecurrence rec = null;
		JsEvent item = null;
		
		try {
			UserProfile up = env.getProfile();
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rrdao = RecurrenceDAO.getInstance();
			con = getConnection();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", true);
				
				event = edao.select(con, id);
				item = new JsEvent(event, up.getTimeZone());
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				JsPayload<JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				event = new OEvent();
				event.fillFrom(pl.data, cus.getWorkdayStart(), cus.getWorkdayEnd());
				event.setEventId(edao.getSequence(con).intValue());
				
				if(!StringUtils.isEmpty(pl.data.rrType)) {
					rec = new ORecurrence();
					rec.fillFrom(pl.data, event);
					rec.setRecurrenceId(rrdao.getSequence(con).intValue());
				}
				
				try {
					con.setAutoCommit(false);
					
					if(rec != null) {
						rrdao.insert(con, rec);
						event.setRecurrenceId(rec.getRecurrenceId());
					}
					edao.insert(con, event);
					
					con.commit();
					
				} catch(Exception ex1) {
					con.rollback();
					throw ex1;
				}
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				/*
				JsPayload<JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				edao.update(con, pl.data);
				new JsonResult().printTo(out);
				*/
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageEvents", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private ExtTreeNode createCalendarGroupNode(MyCalendarGroup group, boolean leaf) {
		return createCalendarGroupNode(group.getId(), lookupResource(CalendarLocaleKey.MY_CALENDARS), leaf, group.getDomainId(), group.getUserId(), "wtcal-icon-calendar-my");
	}
	
	private ExtTreeNode createCalendarGroupNode(SharedCalendarGroup group, boolean leaf) {
		return createCalendarGroupNode(group.getId(), group.getDisplayName(), leaf, group.getDomainId(), group.getUserId(), "wtcal-icon-calendar-shared");
	}
	
	private ExtTreeNode createCalendarGroupNode(String id, String text, boolean leaf, String domainId, String userId, String iconClass) {
		boolean visible = checkedCalendarGroups.contains(id);
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_nodeType", "group");
		node.put("_groupId", id);
		node.put("_domainId", domainId);
		node.put("_userId", userId);
		node.put("_visible", visible);
		node.setIconClass(iconClass);
		node.setChecked(visible);
		return node;
	}
	
	private ExtTreeNode createCalendarNode(String groupId, OCalendar cal) {
		ExtTreeNode node = new ExtTreeNode(cal.getCalendarId(), cal.getName(), true);
		node.put("_nodeType", "calendar");
		node.put("_groupId", groupId);
		node.put("_domainId", cal.getDomainId());
		node.put("_userId", cal.getUserId());
		node.put("_builtIn", cal.getBuiltIn());
		node.put("_default", cal.getIsDefault());
		node.put("_color", cal.getColor());
		node.put("_visible", cal.getVisible());
		node.setIconClass("wt-palette-" + cal.getHexColor());
		node.setChecked(cal.getVisible());
		return node;
	}
}
