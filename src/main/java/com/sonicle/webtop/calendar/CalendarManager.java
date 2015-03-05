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

import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.js.JsCalEvent;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author malbinola
 */
public class CalendarManager {
	
	private final static CalendarManager INSTANCE = new CalendarManager();

	public static CalendarManager getInstance() {
		return INSTANCE;
	}
	
	public LinkedHashMap<String, CalendarGroup> getCalendarGroups(Connection con, UserProfile.Id profileId) {
		LinkedHashMap<String, CalendarGroup> groups = new LinkedHashMap();
		MyCalendarGroup myGroup = null;
		SharedCalendarGroup sharedGroup = null;
		
		// Defines personal group
		myGroup = new MyCalendarGroup(profileId);
		groups.put(myGroup.getId(), myGroup);
		
		// TODO: recuperare realmente i calendari condivisi
		String[] ids = new String[]{"gabriele.bulfon@sonicleldap", "raffaele.fullone@sonicleldap", "sergio.decillis@sonicleldap"};
		
		UserDAO udao = UserDAO.getInstance();
		OUser user = null;
		UserProfile.Id inProfileId = null;
		for(String id : ids) {
			inProfileId = new UserProfile.Id(id);
			user = udao.selectByDomainUser(con, inProfileId.getDomainId(), inProfileId.getUserId());
			if(user != null) {
				sharedGroup = new SharedCalendarGroup(user);
				groups.put(sharedGroup.getId(), sharedGroup);
			}
		}
		
		return groups;
	}
	
	public List<OCalendar> getCalendars(Connection con, UserProfile.Id user) {
		//ArrayList<OCalendar> cals = new ArrayList();
		CalendarDAO cdao = CalendarDAO.getInstance();
		
		return cdao.selectByDomainUser(con, user.getDomainId(), user.getUserId());
		/*
		OCalendar bical = cdao.selectBuiltInByDomainUser(con, user.getDomainId(), user.getUserId());
		if(bical == null) {
			//TODO: aggiungere il calendario built-in se non presente
		}
		
		cals.add(bical);
		cals.addAll(cdao.selectNoBuiltInByDomainUser(con, user.getDomainId(), user.getUserId()));
		return cals;
		*/
	}
	
	public List<JsCalEvent> getEvents(Connection con, CalendarGroup group, String from, String to, TimeZone userTz) {
		ArrayList<JsCalEvent> events = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		List<OCalendar> cals = cdao.selectVisibleByDomainUser(con, group.getDomainId(), group.getUserId());
		List<OEvent> evts = null;
		for(OCalendar cal : cals) {
			evts = edao.selectByCalendarFromTo(con, cal.getCalendarId());
			for(OEvent evt : evts) {
				events.add(new JsCalEvent(evt, cal, userTz));
			}
		}
		
		return events;
		
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
}
