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
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.calendar.bol.OCalendars;
import com.sonicle.webtop.calendar.dal.CalendarsDAO;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BasicEnvironment;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	
	public static final Logger logger = BaseService.getLogger(Service.class);
	
	private BasicEnvironment environment = null;
        UserProfile profile = null;
        public final String DEFAULT_PERSONAL_CALENDAR_NAME="WebTop";
        public final String DEFAULT_PERSONAL_CALENDAR_COLOR="#FFFFFF";
        
	@Override
	public void initialize() {
		logger.debug("CalendarService.ID={}",getId());
                this.environment = getEnv();
                this.profile = environment.getProfile();
	}

	public void processDelPiffero(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		logger.debug("DELPIFFERO: CalendarService.ID={}",getId());
	}
	
	@Override
	public void cleanup() {
		
	}
        @Override
	public HashMap<String, Object> returnClientOptions() {
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("startDay","1");    
		return map;
	}
        
    public void processGetPersonalCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            Connection con=null;
            try{
                String userId=this.environment.getProfile().getUserId();    //TODO: far passare dal client
                String domainId=this.environment.getProfile().getDomainId();
                con=this.getConnection();
                OCalendars defaultCalendar=CalendarsDAO.getInstance().selectDefaultPersonalCalendar(con, domainId, userId);
                if (defaultCalendar==null) {
                    defaultCalendar=new OCalendars();
                    int calendar_id=CalendarsDAO.getInstance().getSequence(con).intValue();
                    defaultCalendar.setCalendarId(calendar_id);
                    defaultCalendar.setName(DEFAULT_PERSONAL_CALENDAR_NAME);
                    defaultCalendar.setColor(DEFAULT_PERSONAL_CALENDAR_COLOR);
                    defaultCalendar.setDomainId(domainId);
                    defaultCalendar.setUserId(userId);
                    CalendarsDAO.getInstance().insertPersonalCalendar(con, defaultCalendar);
                }
                List<OCalendars> calendars = new ArrayList<OCalendars>();
                calendars.add(defaultCalendar);
                calendars.addAll(CalendarsDAO.getInstance().selectPersonalCalendars(con, domainId, userId));
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
            String domainId=this.environment.getProfile().getDomainId();
            Connection con=null;
            try{
                OCalendars newCalendar = ServletUtils.getPayload(request,OCalendars.class);
                con=this.getConnection();
                if (newCalendar.getCalendarId()==null || newCalendar.getCalendarId()==0){
                    int calendar_id=CalendarsDAO.getInstance().getSequence(con).intValue();
                    newCalendar.setCalendarId(calendar_id);
                    newCalendar.setDomainId(domainId);
                    newCalendar.setUserId(this.environment.getProfile().getUserId());   //TODO: far passare dal client
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
                String domainId=this.environment.getProfile().getDomainId();
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
                vel.description = " ";
            } else if (i == 1) {
                vel.id = "0";
                vel.description = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 2) {
                vel.id = "5";
                vel.description = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 3) {
                vel.id = "10";
                vel.description = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 4) {
                vel.id = "15";
                vel.description = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 5) {
                vel.id = "30";
                vel.description = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 6) {
                vel.id = "60";
                vel.description = vel.id + " " + lookupResource(CalendarLocaleKey.REMINDER_MINUTES);
            } else if (i == 7) {
                vel.id = "120";
                vel.description = "2 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 8) {
                vel.id = "180";
                vel.description = "3 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 9) {
                vel.id = "240";
                vel.description = "4 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 10) {
                vel.id = "300";
                vel.description = "5 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 11) {
                vel.id = "360";
                vel.description = "6 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 12) {
                vel.id = "420";
                vel.description = "7 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 13) {
                vel.id = "480";
                vel.description = "8 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 14) {
                vel.id = "540";
                vel.description = "9 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 15) {
                vel.id = "600";
                vel.description = "10 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 16) {
                vel.id = "660";
                vel.description = "11 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 17) {
                vel.id = "720";
                vel.description = "12 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 18) {
                vel.id = "1080";
                vel.description = "18 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
            } else if (i == 19) {
                vel.id = "1440";
                vel.description = "24 " + lookupResource(CalendarLocaleKey.REMINDER_HOURS);
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
                String domainId=this.environment.getProfile().getDomainId();
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
