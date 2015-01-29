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
import com.sonicle.commons.web.JsPayload;
import com.sonicle.commons.web.JsPayloadFields;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.js.JsTreeCal;
import com.sonicle.webtop.calendar.dal.CalendarsDAO;
import static com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.dal.BaseDAO.FieldsMap;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BasicEnvironment;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jooq.Field;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	
	public static final Logger logger = BaseService.getLogger(Service.class);

	private BasicEnvironment env = null;
	private CalendarUserSettings cus;
	public final String DEFAULT_PERSONAL_CALENDAR_NAME = "WebTop";
	public final String DEFAULT_PERSONAL_CALENDAR_COLOR = "#FFFFFF";

	@Override
	public void initialize() {
		logger.debug("CalendarService.ID={}", getId());
		env = getEnv();
		UserProfile profile = env.getProfile();
		cus = new CalendarUserSettings(profile.getDomainId(), profile.getUserId(), getId());
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
					child = new ExtTreeNode(up.getId(), lookupResource(CalendarLocaleKey.MY_CALENDARS), false);
					children.add(child.setExpanded(true).setChecked(true));
					//TODO: aggiungere i calendari condivisi (rimuovere quelli fake)
					child = new ExtTreeNode("raffaele.fullone@sonicleldap", "Raffaele Fullone", true);
					children.add(child.setChecked(true));
					child = new ExtTreeNode("gabriele.bulfon@sonicleldap", "Gabriele Bulfon", true);
					children.add(child.setChecked(true));
					child = new ExtTreeNode("sergio.decillis@sonicleldap", "Sergio De Cillis", true);
					children.add(child.setChecked(true));

				} else {
					CalendarsDAO cdao = CalendarsDAO.getInstance();
					OCalendar defcal = cdao.selectDefaultByDomainUser(con, up.getDomainId(), up.getUserId());
					if(defcal == null) {
						//TODO: aggiungere il calendario di default se non presente
					}
					List<OCalendar> cals = cdao.selectNoDefaultByDomainUser(con, up.getDomainId(), up.getUserId());
					
					children.add(createCalendarNode(defcal));
					for(OCalendar cal : cals) {
						children.add(createCalendarNode(cal));
					}
					
				}
				new JsonResult("children", children).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				JsPayload<JsTreeCal> pl = ServletUtils.getPayload2(request, JsTreeCal.class);
				
				FieldsMap fmap = new FieldsMap();
				if(pl.contains("calColor")) fmap.put(CALENDARS.COLOR, pl.data.calColor);
				if(pl.contains("calShowEvents")) fmap.put(CALENDARS.SHOW_EVENTS, pl.data.calShowEvents);
				
				CalendarsDAO cdao = CalendarsDAO.getInstance();
				cdao.update(con, pl.data.id, fmap);
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageCalendarsTree", ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ExtTreeNode createCalendarNode(OCalendar cal) {
		ExtTreeNode node = new ExtTreeNode(String.valueOf(cal.getCalendarId()), cal.getName(), true, "wtcal-icon-cal-color");
		node.setChecked(cal.getShowEvents());
		node.put("calColor", cal.getColor());
		node.put("calShowEvents", cal.getShowEvents());
		return node;
	}
    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    public void processGetPersonalCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            Connection con=null;
            try{
                String userId=this.env.getProfile().getUserId();    //TODO: far passare dal client
                String domainId=this.env.getProfile().getDomainId();
                con=this.getConnection();
                OCalendar defaultCalendar=CalendarsDAO.getInstance().selectDefaultPersonalCalendar(con, domainId, userId);
                if (defaultCalendar==null) {
                    defaultCalendar=new OCalendar();
                    int calendar_id=CalendarsDAO.getInstance().getSequence(con).intValue();
                    defaultCalendar.setCalendarId(calendar_id);
                    defaultCalendar.setName(DEFAULT_PERSONAL_CALENDAR_NAME);
                    defaultCalendar.setColor(DEFAULT_PERSONAL_CALENDAR_COLOR);
                    defaultCalendar.setDomainId(domainId);
                    defaultCalendar.setUserId(userId);
                    CalendarsDAO.getInstance().insertPersonalCalendar(con, defaultCalendar);
                }
                List<OCalendar> calendars = new ArrayList<OCalendar>();
                calendars.add(defaultCalendar);
                calendars.addAll(CalendarsDAO.getInstance().selectNoDefaultByDomainUser(con, domainId, userId));
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
                OCalendar newCalendar = ServletUtils.getPayload(request,OCalendar.class);
                con=this.getConnection();
                if (newCalendar.getCalendarId()==null || newCalendar.getCalendarId()==0){
                    int calendar_id=CalendarsDAO.getInstance().getSequence(con).intValue();
                    newCalendar.setCalendarId(calendar_id);
                    newCalendar.setDomainId(domainId);
                    newCalendar.setUserId(this.env.getProfile().getUserId());   //TODO: far passare dal client
                    if (newCalendar.getDefault()) CalendarsDAO.getInstance().resetPersonalDefaultCalendar(con, domainId, newCalendar.getUserId());
                    CalendarsDAO.getInstance().insertPersonalCalendar(con, newCalendar);
                }else{
                    newCalendar.setDomainId(domainId);
                    if (newCalendar.getDefault()) CalendarsDAO.getInstance().resetPersonalDefaultCalendar(con, domainId, newCalendar.getUserId());
                    CalendarsDAO.getInstance().updatePersonalCalendar(con, newCalendar.getCalendarId(),newCalendar);
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
                CalendarsDAO.getInstance().deletePersonalCalendar(con, calendar_id);
                if (default_!=null && default_.equals("true"))  CalendarsDAO.getInstance().resetPersonalDefaultCalendarToWebTop(con, domainId, userId);
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
            String showEvents = request.getParameter("showEvents");
            Connection con=null;
            try{
                con=this.getConnection();
                boolean showEv=(showEvents!=null && showEvents.equals("true"))?true:false;
                int cId=Integer.parseInt(calendarId);
                CalendarsDAO.getInstance().checkPersonalCalendar(con, cId,showEv);
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
                CalendarsDAO.getInstance().viewNothingPersonalCalendar(con, domainId,userId);
                CalendarsDAO.getInstance().viewOnlyPersonalCalendar(con, cId);
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
            
    }
}
