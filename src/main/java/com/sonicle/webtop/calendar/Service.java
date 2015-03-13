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
import com.sonicle.commons.web.json.JsPayloadRecord;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.bol.js.JsCalEventDate;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent1;
import com.sonicle.webtop.calendar.bol.js.JsTreeCalendar;
import com.sonicle.webtop.calendar.bol.js.JsTreeCalendarList;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import static com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS;
import com.sonicle.webtop.core.bol.js.JsSimple;
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

	@Override
	public void initialize() {
		env = getEnv();
		UserProfile profile = env.getProfile();
		manager = new CalendarManager(getManagerEnv());
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
			}	

		} catch(SQLException ex) {
			//TODO: gestire errore
		} finally {
			DbUtils.closeQuietly(con);
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
			UserProfile up = env.getProfile();
			con = getConnection();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				if(node.equals("root")) {
					
					MyCalendarGroup myGroup = null;
					SharedCalendarGroup sharedGroup = null;
					for(CalendarGroup group : calendarGroups.values()) {
						if(group instanceof MyCalendarGroup) { // Adds group as Mine
							myGroup = (MyCalendarGroup)group;
							child = createCalendarGroupNode(myGroup, false);
							child.setChecked(true);
							children.add(child.setExpanded(true));
							
						} else if(group instanceof SharedCalendarGroup) { // Adds group as Shared
							sharedGroup = (SharedCalendarGroup)group;
							child = createCalendarGroupNode(sharedGroup, true);
							child.setChecked(false);
							children.add(child);
						}
					}

				} else {
					UserProfile.Id upId = new UserProfile.Id(node);
					List<OCalendar> cals = manager.getCalendars(con, upId);
					
					for(OCalendar cal : cals) children.add(createCalendarNode(node, cal));
				}
				new JsonResult("children", children).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				JsListPayload<JsTreeCalendarList> pl = ServletUtils.getPayloadAsList(request, JsTreeCalendarList.class);
				CalendarDAO cdao = CalendarDAO.getInstance();
				
				JsPayloadRecord record = null;
				JsTreeCalendar data = null;
				FieldsMap fmap = null;
				
				for(JsTreeCalendar cal : pl.data) {
					cdao.updateVisible(con, cal.id, cal._visible);
				}
				
				/*
				for(int i=0; i<pl.records.size(); i++) {
					record = pl.records.get(i);
					data = pl.data.get(i);
					fmap = new FieldsMap();
					if(record.containsKey("calColor")) fmap.put(CALENDARS.COLOR, data.calColor);
					if(record.containsKey("calVisible")) fmap.put(CALENDARS.VISIBLE, data.calVisible);
					
					cdao.update(con, data.id, fmap);
				}
				*/
				
				new JsonResult().printTo(out);
				
				/*
				JsPayload<JsTreeCal> pl = ServletUtils.getPayload2(request, JsTreeCal.class);
				
				FieldsMap fmap = new FieldsMap();
				if(pl.contains("calColor")) fmap.put(CALENDARS.COLOR, pl.data.calColor);
				if(pl.contains("calVisible")) fmap.put(CALENDARS.SHOW_EVENTS, pl.data.calVisible);
				
				CalendarsDAO cdao = CalendarsDAO.getInstance();
				cdao.update(con, pl.data.id, fmap);
				new JsonResult().printTo(out);
				*/
				
			} else if(crud.equals(Crud.DELETE)) {
				JsListPayload<JsTreeCalendarList> pl = ServletUtils.getPayloadAsList(request, JsTreeCalendarList.class);
				
				CalendarDAO cdao = CalendarDAO.getInstance();
				for(JsTreeCalendar data : pl.data) {
					cdao.delete(con, data.id);
				}
				new JsonResult().printTo(out);
				
				/*
				JsPayload<JsTreeCal> pl = ServletUtils.getPayload2(request, JsTreeCal.class);
				CalendarsDAO cdao = CalendarsDAO.getInstance();
				cdao.delete(con, pl.data.id);
				new JsonResult().printTo(out);
				*/
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageCalendarsTree", ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processGetGroupCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
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

			//TODO: il client passa una lista di gruppi, controllare se si è abilitati alla visualizzazione
			String[] groups = new String[]{"matteo.albinola@sonicleldap"};
			
			/*
			CalendarGroup group = null;
			HashSet<String> dates = new HashSet<>();
			for(String groupKey : groups) {
				group = calendarGroups.get(groupKey);
				grpEvts = manager.getEventsDates2(con, group, fromDate, toDate);
				for(OEvent evt : grpEvts) {
					dates.add(JsCalEvent.toYmdWithZone(evt.getFromDate(), up.getTimeZone()));
				}
			}
			
			Iterator it = dates.iterator();
			while (it.hasNext()) {
				items.add(new JsCalEventDate((String)it.next()));
			}
			
			new JsonResult("dates", items).printTo(out);
			*/
			
			CalendarGroup group = null;
			List<DateTime> dates = null;
			for(String groupKey : groups) {
				group = calendarGroups.get(groupKey);
				dates = manager.getEventsDates(con, group, fromDate, toDate);
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
				String start = ServletUtils.getStringParameter(request, "startDate", true);
				String end = ServletUtils.getStringParameter(request, "endDate", true);
				DateTime fromDate = OEvent.parseYmdHmsWithZone(start, "00:00:00", up.getTimeZone());
				DateTime toDate = OEvent.parseYmdHmsWithZone(end, "23:59:59", up.getTimeZone());
				
				//TODO: il client passa una lista di gruppi, controllare se si è abilitati alla visualizzazione
				String[] groups = new String[]{"matteo.albinola@sonicleldap"};
				
				CalendarGroup group = null;
				List<CalendarManager.GroupEvents> grpEvts = null;
				for(String groupKey : groups) {
					group = calendarGroups.get(groupKey);
					grpEvts = manager.getEvents(con, group, fromDate, toDate);
					for(CalendarManager.GroupEvents ge : grpEvts) {
						for(OEvent evt : ge.events) {
							items.add(new JsSchedulerEvent(evt, ge.calendar, up.getTimeZone()));
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
	
	public void processManageEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		OEvent event = null;
		JsEvent item = null;
		
		try {
			UserProfile up = env.getProfile();
			EventDAO edao = EventDAO.getInstance();
			con = getConnection();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", true);
				
				event = edao.select(con, id);
				item = new JsEvent(event, up.getTimeZone());
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				JsPayload<JsEvent> pl = ServletUtils.getPayload(request, JsEvent.class);
				
				event = new OEvent(pl.data, cus.getWorkdayStart(), cus.getWorkdayEnd());
				event.setEventId(edao.getSequence(con).intValue());
				edao.insert(con, event);
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
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private ExtTreeNode createCalendarGroupNode(MyCalendarGroup group, boolean leaf) {
		return createCalendarGroupNode(group.getId(), lookupResource(CalendarLocaleKey.MY_CALENDARS), leaf, group.getDomainId(), group.getUserId());
	}
	
	private ExtTreeNode createCalendarGroupNode(SharedCalendarGroup group, boolean leaf) {
		return createCalendarGroupNode(group.getId(), group.getDisplayName(), leaf, group.getDomainId(), group.getUserId());
	}
	
	private ExtTreeNode createCalendarGroupNode(String id, String text, boolean leaf, String domainId, String userId) {
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_nodeType", "group");
		node.put("_domainId", domainId);
		node.put("_userId", userId);
		return node;
	}
	
	private ExtTreeNode createCalendarNode(String groupId, OCalendar cal) {
		ExtTreeNode node = new ExtTreeNode(cal.getCalendarId(), cal.getName(), true);
		node.put("_nodeType", "calendar");
		node.put("_groupId", groupId);
		node.put("_domainId", cal.getDomainId());
		node.put("_userId", cal.getUserId());
		node.setIconClass("wt-palette-" + cal.getHexColor());
		node.setChecked(cal.getVisible());
		node.put("_builtIn", cal.getBuiltIn());
		node.put("_color", cal.getColor());
		node.put("_visible", cal.getVisible());
		return node;
	}
    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    public void processGetPersonalCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            Connection con=null;
            try{
                String userId=this.env.getProfile().getUserId();    //TODO: far passare dal client
                String domainId=this.env.getProfile().getDomainId();
                con=this.getConnection();
                OCalendar defaultCalendar=CalendarDAO.getInstance().selectDefaultPersonalCalendar(con, domainId, userId);
                if (defaultCalendar==null) {
                    defaultCalendar=new OCalendar();
                    int calendar_id=CalendarDAO.getInstance().getSequence(con).intValue();
                    defaultCalendar.setCalendarId(calendar_id);
                    //defaultCalendar.setName(DEFAULT_PERSONAL_CALENDAR_NAME);
                    defaultCalendar.setColor(DEFAULT_PERSONAL_CALENDAR_COLOR);
                    defaultCalendar.setDomainId(domainId);
                    defaultCalendar.setUserId(userId);
                    CalendarDAO.getInstance().insertPersonalCalendar(con, defaultCalendar);
                }
                List<OCalendar> calendars = new ArrayList<>();
                calendars.add(defaultCalendar);
                calendars.addAll(CalendarDAO.getInstance().selectNoBuiltInByDomainUser(con, domainId, userId));
                //String raw = JsonResult.gson.toJson(calendars);
                new JsonResult(calendars).printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }
                
        }
        
        public void processSavePersonalCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            String domainId=this.env.getProfile().getDomainId();
            Connection con=null;
            try{
				JsPayload<OCalendar> pl = ServletUtils.getPayload(request, OCalendar.class);
				
                con=this.getConnection();
                if (pl.data.getCalendarId()==null || pl.data.getCalendarId()==0){
                    int calendar_id=CalendarDAO.getInstance().getSequence(con).intValue();
                    pl.data.setCalendarId(calendar_id);
                    pl.data.setDomainId(domainId);
                    pl.data.setUserId(this.env.getProfile().getUserId());   //TODO: far passare dal client
                    if (pl.data.getIsDefault()) CalendarDAO.getInstance().resetPersonalDefaultCalendar(con, domainId, pl.data.getUserId());
                    CalendarDAO.getInstance().insertPersonalCalendar(con, pl.data);
                }else{
                    pl.data.setDomainId(domainId);
                    if (pl.data.getIsDefault()) CalendarDAO.getInstance().resetPersonalDefaultCalendar(con, domainId, pl.data.getUserId());
                    CalendarDAO.getInstance().updatePersonalCalendar(con, pl.data.getCalendarId(),pl.data);
                    //TODO: Aggiornare calendari degli eventi
                }
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
        }
        
        public void processDeletePersonalCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            String calendarId = request.getParameter("calendarId");
            String default_ = request.getParameter("default_");
            String userId = request.getParameter("userId");
            Connection con=null;
            try{
                con=this.getConnection();
                String domainId=this.env.getProfile().getDomainId();
                int calendar_id=Integer.parseInt(calendarId);
                CalendarDAO.getInstance().deletePersonalCalendar(con, calendar_id);
                if (default_!=null && default_.equals("true"))  CalendarDAO.getInstance().resetPersonalDefaultCalendarToWebTop(con, domainId, userId);
                //TODO: Cancellare eventi
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
        }
        
        
        
     public void processGetViewReminder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        JsSimple vel;
        ArrayList gveResult = new ArrayList();
        for (int i = 0; i <= 19; i++) {
            vel = new JsSimple();
            if (i == 0) {
                vel.id = null;
                vel.desc = " ";
            } else if (i == 1) {
                vel.id = "0";
                vel.desc = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 2) {
                vel.id = "5";
                vel.desc = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 3) {
                vel.id = "10";
                vel.desc = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 4) {
                vel.id = "15";
                vel.desc = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 5) {
                vel.id = "30";
                vel.desc = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 6) {
                vel.id = "60";
                vel.desc = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 7) {
                vel.id = "120";
                vel.desc = "2 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 8) {
                vel.id = "180";
                vel.desc = "3 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 9) {
                vel.id = "240";
                vel.desc = "4 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 10) {
                vel.id = "300";
                vel.desc = "5 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 11) {
                vel.id = "360";
                vel.desc = "6 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 12) {
                vel.id = "420";
                vel.desc = "7 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 13) {
                vel.id = "480";
                vel.desc = "8 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 14) {
                vel.id = "540";
                vel.desc = "9 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 15) {
                vel.id = "600";
                vel.desc = "10 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 16) {
                vel.id = "660";
                vel.desc = "11 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 17) {
                vel.id = "720";
                vel.desc = "12 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 18) {
                vel.id = "1080";
                vel.desc = "18 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 19) {
                vel.id = "1440";
                vel.desc = "24 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            }
            gveResult.add(vel);
        }
        new JsonResult(gveResult).printTo(out);
    }
     
    public void processCheckPersonalCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            String calendarId = request.getParameter("calendarId");
            String calVisible = request.getParameter("calVisible");
            Connection con=null;
            try{
                con=this.getConnection();
                boolean showEv=(calVisible!=null && calVisible.equals("true"))?true:false;
                int cId=Integer.parseInt(calendarId);
                CalendarDAO.getInstance().checkPersonalCalendar(con, cId,showEv);
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
            
    }
    
    public void processViewOnlyPersonalCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            String calendarId = request.getParameter("calendarId");
            String userId = request.getParameter("userId");
            Connection con=null;
            try{
                con=this.getConnection();
                int cId=Integer.parseInt(calendarId);
                String domainId=this.env.getProfile().getDomainId();
                CalendarDAO.getInstance().viewNothingPersonalCalendar(con, domainId,userId);
                CalendarDAO.getInstance().viewOnlyPersonalCalendar(con, cId);
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
            
    }
}
