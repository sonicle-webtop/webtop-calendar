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
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ManagerEnvironment;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CalendarManager {
	
	public static final Logger logger = BaseService.getLogger(CalendarManager.class);
	private final static String SERVICE_ID = "com.sonicle.webtop.calendar";
	private final static String RESOURCE_CALENDARS = "CALENDARS";
	private final ManagerEnvironment env;
	
	public CalendarManager(ManagerEnvironment env) {
		this.env = env;
	}
	
	public LinkedHashMap<String, CalendarGroup> getCalendarGroups(Connection con, UserProfile.Id profileId) {
		LinkedHashMap<String, CalendarGroup> groups = new LinkedHashMap();
		MyCalendarGroup myGroup = null;
		SharedCalendarGroup sharedGroup = null;
		
		// Defines personal group
		myGroup = new MyCalendarGroup(profileId);
		groups.put(myGroup.getId(), myGroup);
		
		// Reads incoming shares as calendar groups
		Connection ccon = null;
		try {
			ccon = env.getCoreConnection();
			ShareDAO sdao = ShareDAO.getInstance();
			List<OShare> shares = sdao.selectIncomingByServiceDomainUserResource(ccon, SERVICE_ID, profileId.getDomainId(), profileId.getUserId(), RESOURCE_CALENDARS);
			
			UserDAO udao = UserDAO.getInstance();
			OUser user = null;
			UserProfile.Id inProfileId = null;
			for(OShare share : shares) {
				inProfileId = new UserProfile.Id(share.getDomainId(), share.getUserId());
				user = udao.selectByDomainUser(con, inProfileId.getDomainId(), inProfileId.getUserId());
				if(user != null) {
					sharedGroup = new SharedCalendarGroup(user);
					groups.put(sharedGroup.getId(), sharedGroup);
				}
			}
			
		} catch(SQLException ex) {
			logger.error("Unable to get incoming shares", ex);
		} finally {
			DbUtils.closeQuietly(ccon);
		}
		
		return groups;
	}
	
	public List<OCalendar> getCalendars(Connection con, UserProfile.Id user) {
		CalendarDAO cdao = CalendarDAO.getInstance();
		return cdao.selectByDomainUser(con, user.getDomainId(), user.getUserId());
	}
	
	
	
	public List<OEvent> getEventsDates2(Connection con, CalendarGroup group, DateTime fromDate, DateTime toDate) {
		ArrayList<OEvent> grpEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		List<OCalendar> cals = cdao.selectVisibleByDomainUser(con, group.getDomainId(), group.getUserId());
		List<OEvent> evts = null;
		for(OCalendar cal : cals) {
			evts = edao.selectDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate);
			
			
			grpEvts.addAll(evts);
		}
		return grpEvts;
	}
	
	public List<DateTime> getEventsDates(Connection con, CalendarGroup group, DateTime fromDate, DateTime toDate) {
		HashSet<DateTime> dates = new HashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		List<OCalendar> cals = cdao.selectVisibleByDomainUser(con, group.getDomainId(), group.getUserId());
		List<OEvent> evts = null;
		DateTime date = null;
		DateTime to = null;
		for(OCalendar cal : cals) {
			evts = edao.selectDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate);
			for(OEvent evt : evts) {
				date = evt.getFromDate().withTimeAtStartOfDay();
				to = evt.getToDate().withTimeAtStartOfDay();
				while(date.compareTo(to) <= 0) {
					dates.add(date);
					date = date.plusDays(1);
				}
			}
		}
		
		return new ArrayList<>(dates);
	}
	
	public List<GroupEvents> getEvents(Connection con, CalendarGroup group, DateTime fromDate, DateTime toDate) {
		ArrayList<GroupEvents> grpEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		List<OCalendar> cals = cdao.selectVisibleByDomainUser(con, group.getDomainId(), group.getUserId());
		List<OEvent> evts = null;
		for(OCalendar cal : cals) {
			evts = edao.selectByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate);
			grpEvts.add(new GroupEvents(cal, evts));
		}
		return grpEvts;
		
		/*
		MyCalendarGroup myGroup = null;
		SharedCalendarGroup sharedGroup = null;
		if(group instanceof MyCalendarGroup) {
			myGroup = (MyCalendarGroup)group;



		} else if(group instanceof SharedCalendarGroup) {
			sharedGroup = (SharedCalendarGroup)group;

		}
		*/
	}
	
	public static class GroupEvents {
		public final OCalendar calendar;
		public final List<OEvent> events;
		
		public GroupEvents(OCalendar calendar, List<OEvent> events) {
			this.calendar = calendar;
			this.events = events;
		}
	}
}
