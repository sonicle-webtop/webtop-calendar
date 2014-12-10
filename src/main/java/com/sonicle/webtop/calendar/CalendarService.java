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
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.calendar.bol.OCalendars;
import com.sonicle.webtop.calendar.dal.CalendarsDAO;
import com.sonicle.webtop.calendar.jooq.tables.Calendars;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.sdk.BasicEnvironment;
import com.sonicle.webtop.core.sdk.Environment;
import com.sonicle.webtop.core.sdk.Service;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CalendarService extends Service {
	
	public static final Logger logger = Service.getLogger(CalendarService.class);
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
        public HashMap<String, Object> returnInitialSettings() {
            HashMap<String,Object> map=new HashMap<String,Object>();
            map.put("startDay","1");    
            return map;
	}
        
        public void processGetPersonalCalendars(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            Connection con=null;
            try{
                String userId=this.environment.getProfile().getUserId();
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
        
        public void processInsertPersonalCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            
            //ServletUtils.
            String user_id = request.getParameter("user_id");
            String calendar_name = request.getParameter("calendar_name");
            String description = request.getParameter("description");
            String color = request.getParameter("color");
            String private_value = request.getParameter("private_value");
            String busy = request.getParameter("busy");
            String sync = request.getParameter("sync");
            String default_value = request.getParameter("default_value");
            String default_reminder = request.getParameter("default_reminder");
            String default_send_invite = request.getParameter("default_send_invite");
            String domainId=this.environment.getProfile().getDomainId();
            Connection con=null;
            try{
                con=this.getConnection();       
                OCalendars newCalendar=new OCalendars();
                int calendar_id=CalendarsDAO.getInstance().getSequence(con).intValue();
                newCalendar.setCalendarId(calendar_id);
                newCalendar.setName(calendar_name);
                newCalendar.setDescription(description);
                newCalendar.setColor(color);
                boolean private_value_b=(private_value!=null && private_value.equals("true"))?true:false;
                newCalendar.setPrivate(private_value_b);
                boolean busy_b=(busy!=null && busy.equals("true"))?true:false;
                newCalendar.setPrivate(busy_b);
                boolean sync_b=(sync!=null && sync.equals("true"))?true:false;
                newCalendar.setSync(sync_b);
                boolean default_value_b=(default_value!=null && default_value.equals("true"))?true:false;
                newCalendar.setDefault(default_value_b);
                if (default_value_b) CalendarsDAO.getInstance().resetPersonalDefaultCalendar(con, domainId, user_id);
                Integer default_reminder_b=(default_reminder!=null)?Integer.parseInt(default_reminder):null;
                newCalendar.setDefaultReminder(default_reminder_b);
                boolean default_send_invite_b=(default_send_invite!=null && default_send_invite.equals("true"))?true:false;
                newCalendar.setDefaultSendInvite(default_send_invite_b);
                newCalendar.setDomainId(domainId);
                newCalendar.setUserId(user_id);
                CalendarsDAO.getInstance().insertPersonalCalendar(con, newCalendar);
                
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
        }
        
        public void processDeletePersonalCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            String calendarid = request.getParameter("calendar_id");
            Connection con=null;
            try{
                con=this.getConnection();       
                int calendar_id=Integer.parseInt(calendarid);
                CalendarsDAO.getInstance().deletePersonalCalendar(con, calendar_id);
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
        }
        
        public void processUpdatePersonalCalendar(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
            String user_id = request.getParameter("user_id");
            String calendar_name = request.getParameter("calendar_name");
            String description = request.getParameter("description");
            String color = request.getParameter("color");
            String private_value = request.getParameter("private_value");
            String busy = request.getParameter("busy");
            String sync = request.getParameter("sync");
            String default_value = request.getParameter("default_value");
            String default_reminder = request.getParameter("default_reminder");
            String default_send_invite = request.getParameter("default_send_invite");
            String calendarid = request.getParameter("calendar_id");
            String domainId=this.environment.getProfile().getDomainId();
            Connection con=null;
            try{
                con=this.getConnection();       
                OCalendars newCalendar=new OCalendars();
                int calendar_id=Integer.parseInt(calendarid);
                newCalendar.setName(calendar_name);
                newCalendar.setDescription(description);
                newCalendar.setColor(color);
                boolean private_value_b=(private_value!=null && private_value.equals("true"))?true:false;
                newCalendar.setPrivate(private_value_b);
                boolean busy_b=(busy!=null && busy.equals("true"))?true:false;
                newCalendar.setPrivate(busy_b);
                boolean sync_b=(sync!=null && sync.equals("true"))?true:false;
                newCalendar.setSync(sync_b);
                boolean default_value_b=(default_value!=null && default_value.equals("true"))?true:false;
                newCalendar.setDefault(default_value_b);
                if (default_value_b) CalendarsDAO.getInstance().resetPersonalDefaultCalendar(con, domainId, user_id);
                Integer default_reminder_b=(default_reminder!=null)?Integer.parseInt(default_reminder):null;
                newCalendar.setDefaultReminder(default_reminder_b);
                boolean default_send_invite_b=(default_send_invite!=null && default_send_invite.equals("true"))?true:false;
                newCalendar.setDefaultSendInvite(default_send_invite_b);
                newCalendar.setDomainId(domainId);
                newCalendar.setUserId(user_id);
                CalendarsDAO.getInstance().updatePersonalCalendar(con, calendar_id,newCalendar);
                
                new JsonResult().printTo(out);
            }catch(Exception ex){
                ex.printStackTrace();
                new JsonResult(false).printTo(out);
            }finally{
                DbUtils.closeQuietly(con);
            }   
        }
        
}
