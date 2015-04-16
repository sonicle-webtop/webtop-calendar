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
import com.sonicle.commons.web.json.JsListPayload;
import com.sonicle.commons.web.json.JsPayload;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.calendar.CalendarUserSettings.CheckedCalendarGroups;
import com.sonicle.webtop.calendar.CalendarUserSettings.CheckedCalendars;
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.Event;
import com.sonicle.webtop.calendar.bol.SchedulerEvent;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEventDate;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsTreeCalendar;
import com.sonicle.webtop.calendar.bol.js.JsTreeCalendar.JsTreeCalendarList;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BasicEnvironment;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
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
	private CheckedCalendars checkedCalendars = null;

	@Override
	public void initialize() {
		env = getEnv();
		UserProfile profile = env.getProfile();
		manager = new CalendarManager(getManifest(), profile.getStringId());
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
				
				checkedCalendars = cus.getCheckedCalendars();
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
	
	private void toggleCheckedCalendar(int calendarId, boolean checked) {
		synchronized(calendarGroups) {
			if(checked) {
				checkedCalendars.add(calendarId);
			} else {
				checkedCalendars.remove(calendarId);
			}
			cus.setCheckedCalendars(checkedCalendars);
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
		hm.put("workdayStart", cus.getWorkdayStart());
		hm.put("workdayEnd", cus.getWorkdayEnd());
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
							child = createCalendarGroupNode(sharedGroup, false);
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
					if(cal._nodeType.equals(JsTreeCalendar.TYPE_GROUP)) {
						toggleCheckedCalendarGroup(cal._groupId, cal._visible);
					} else if(cal._nodeType.equals(JsTreeCalendar.TYPE_CALENDAR)) {
						toggleCheckedCalendar(Integer.valueOf(cal.id), cal._visible);
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
				cdao.insert(con, pl.data);
				toggleCheckedCalendar(pl.data.getCalendarId(), true);
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
	
	public void processGetEventDates(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		ArrayList<JsSchedulerEventDate> items = new ArrayList<>();
		List<OEvent> grpEvts = null;
		
		try {
			con = getConnection();
			UserProfile up = env.getProfile();
			
			// Defines boundaries
			String start = ServletUtils.getStringParameter(request, "startDate", true);
			String end = ServletUtils.getStringParameter(request, "endDate", true);
			DateTime fromDate = JsEvent.parseYmdHmsWithZone(start, "00:00:00", up.getTimeZone());
			DateTime toDate = JsEvent.parseYmdHmsWithZone(end, "23:59:59", up.getTimeZone());
			
			// Get events for each visible group
			Integer[] checked;
			List<DateTime> dates = null;
			for(CalendarGroup group : calendarGroups.values()) {
				if(!checkedCalendarGroups.contains(group.getId())) continue; // Skip if not visible
				
				checked = checkedCalendars.toArray(new Integer[checkedCalendars.size()]);
				dates = manager.getEventsDates(group, checked, fromDate, toDate, up.getTimeZone());
				for(DateTime dt : dates) {
					items.add(new JsSchedulerEventDate(JsSchedulerEvent.toYmdWithZone(dt, up.getTimeZone())));
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
				DateTime fromDate = JsEvent.parseYmdHmsWithZone(from, "00:00:00", up.getTimeZone());
				DateTime toDate = JsEvent.parseYmdHmsWithZone(to, "23:59:59", up.getTimeZone());
				
				// Get events for each visible group
				Integer[] checked;
				JsSchedulerEvent jse = null;
				List<SchedulerEvent> expEvents = null;
				List<CalendarManager.GroupEvents> grpEvts = null;
				for(CalendarGroup group : calendarGroups.values()) {
					if(!checkedCalendarGroups.contains(group.getId())) continue; // Skip group if not visible
					
					checked = checkedCalendars.toArray(new Integer[checkedCalendars.size()]);
					grpEvts = manager.getEvents(group, checked, fromDate, toDate);
					for(CalendarManager.GroupEvents ge : grpEvts) {
						for(SchedulerEvent evt : ge.events) {
							if(evt.getRecurrenceId() == null) {
								
								jse = new JsSchedulerEvent(ge.calendar, evt, up.getId(), up.getTimeZone());
								items.add(jse);
							} else {
								expEvents = manager.expandRecurringEvent(ge.calendar, evt, fromDate, toDate, up.getTimeZone());
								for(SchedulerEvent expEvent : expEvents) {
									jse = new JsSchedulerEvent(ge.calendar, expEvent, up.getId(), up.getTimeZone());
									items.add(jse);
								}
							}
						}
					}
				}
				new JsonResult("events", items).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				JsPayload<JsSchedulerEvent> pl = ServletUtils.getPayload(request, JsSchedulerEvent.class);
				
				DateTimeZone etz = DateTimeZone.forID(pl.data.timezone);
				DateTime newStart = JsEvent.parseYmdHmsWithZone(pl.data.startDate, etz);
				DateTime newEnd = JsEvent.parseYmdHmsWithZone(pl.data.endDate, etz);
				manager.moveEvent(pl.data.id, newStart, newEnd);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				String uid = ServletUtils.getStringParameter(request, "id", true);
				String target = ServletUtils.getStringParameter(request, "target", true);
				
				manager.deleteEvent(target, uid);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("restore")) {
				String uid = ServletUtils.getStringParameter(request, "id", true);
				
				manager.restoreEvent(uid);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageEventsView", ex);
			new JsonResult(false, "Error").printTo(out);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processManageEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		JsEvent item = null;
		
		try {
			UserProfile up = env.getProfile();
			con = getConnection();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				Event evt = manager.getEvent(id);
				item = new JsEvent(manager.getEvent(id), manager.getCalendarGroupId(evt.calendarId));
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				JsPayload<JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				Event evt = JsEvent.buildEvent(pl.data, cus.getWorkdayStart(), cus.getWorkdayEnd());
				manager.insertEvent(evt);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				String target = ServletUtils.getStringParameter(request, "target", true);
				JsPayload<JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				System.out.println("TAERGET: "+target);
				Event evt = JsEvent.buildEvent(pl.data, cus.getWorkdayStart(), cus.getWorkdayEnd());
				manager.updateEvent(target, evt, up.getTimeZone());
				new JsonResult().printTo(out);
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
		node.put("_visible", visible);
		node.setIconClass(iconClass);
		node.setChecked(visible);
		return node;
	}
	
	private ExtTreeNode createCalendarNode(String groupId, OCalendar cal) {
		boolean visible = checkedCalendars.contains(cal.getCalendarId());
		ExtTreeNode node = new ExtTreeNode(cal.getCalendarId(), cal.getName(), true);
		node.put("_nodeType", "calendar");
		node.put("_groupId", groupId);
		node.put("_builtIn", cal.getBuiltIn());
		node.put("_default", cal.getIsDefault());
		node.put("_color", cal.getColor());
		node.put("_visible", visible);
		node.put("_isPrivate", cal.getIsPrivate());
		node.put("_busy", cal.getBusy());
		node.put("_reminder", cal.getReminder());
		node.setIconClass("wt-palette-" + cal.getHexColor());
		node.setChecked(visible);
		return node;
	}
}
