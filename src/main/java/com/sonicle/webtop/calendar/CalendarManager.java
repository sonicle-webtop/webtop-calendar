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

import com.rits.cloning.Cloner;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
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
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
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
				date = evt.getStartDate().withTimeAtStartOfDay();
				to = evt.getEndDate().withTimeAtStartOfDay();
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
			evts.addAll(edao.selectRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate));
			grpEvts.add(new GroupEvents(cal, evts));
		}
		return grpEvts;
	}
	
	public List<JsSchedulerEvent> expandRecurringEvent(Connection con, OCalendar cal, OEvent event, DateTime fromDate, DateTime toDate, TimeZone userTz) {
		ArrayList<JsSchedulerEvent> events = new ArrayList<>();
		ORecurrence rr = null;
		OEvent newEvent = null;
		
		RecurrenceDAO rrdao = RecurrenceDAO.getInstance();
		
		if(event.getRecurrenceId() == null) return null;
		rr = rrdao.select(con, event.getRecurrenceId());
		if(rr == null) return null;
		
		int eventDays = Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
		
		DateTime date = fromDate, startDt, endDt;
		while((date.compareTo(toDate) <= 0) && (date.compareTo(rr.getUntilDate()) <= 0)) {
			if(legacyGetEventsInDate(con, event, rr, fromDate, toDate)) {
				startDt = event.getStartDate().withDate(date.toLocalDate());
				endDt = event.getEndDate().withDate(startDt.plusDays(eventDays).toLocalDate());
				
				newEvent = new Cloner().deepClone(event);
				newEvent.setStartDate(startDt);
				newEvent.setEndDate(endDt);
				events.add(new JsSchedulerEvent(newEvent, cal, userTz));
			}
			date = date.plusDays(1);
		}
		return events;
	}
	
	public boolean checkUntilDate(ORecurrence rr) {
		return rr.getUntilDate().compareTo(rr.getStartDate()) >= 0;
	}
	
	public boolean legacyGetEventsInDate(Connection con, OEvent event, ORecurrence rr, DateTime fromDate, DateTime toDate) {
		boolean success = false;
		
		RecurrenceDAO rrdao = RecurrenceDAO.getInstance();
		
		ORecurrenceBroken rrb = rrdao.selectBroken(con, event.getEventId(), rr.getRecurrenceId(), fromDate.toLocalDate());
		if(rrb != null) return false;
		
		int dd = fromDate.getDayOfMonth();
		int mm = fromDate.getMonthOfYear()+1;
		int yyyy = fromDate.getYear();
		String until_day = String.valueOf(toDate.getDayOfMonth());
		String until_month = String.valueOf(toDate.getMonthOfYear()+1);
		String until_year = String.valueOf(toDate.getYear());
		String eldd = null;
		String elmm = null;
		String elyyyy = null;

		Calendar day = Calendar.getInstance();
		day.set(yyyy, mm - 1, dd, 0, 0, 0);
		day.setFirstDayOfWeek(Calendar.MONDAY);

		int repeat = Integer.parseInt(String.valueOf(rr.getRepeat()));
		
		if (checkUntilDate(rr) || repeat != 0) {
			Calendar untilDate = Calendar.getInstance();
			untilDate.set(Integer.parseInt(until_year), Integer.parseInt(until_month) - 1, Integer.parseInt(until_day), 0, 0, 0);
			untilDate.setFirstDayOfWeek(Calendar.MONDAY);
			untilDate.clear(Calendar.MILLISECOND);
			Calendar eventDate = Calendar.getInstance();
			eventDate.set(rr.getStartDate().getYear(), rr.getStartDate().getMonthOfYear()-1, rr.getStartDate().getDayOfMonth(), 0, 0, 0);
			eventDate.setFirstDayOfWeek(Calendar.MONDAY);
			eventDate.clear(Calendar.MILLISECOND);
			int d = eventDate.get(Calendar.DAY_OF_MONTH);
			int m = eventDate.get(Calendar.MONTH);
			int y = eventDate.get(Calendar.YEAR);
			
			if (rr.getType().equals("D")) {

				// Ogni N GG
				int step = rr.getDaylyFreq();
				while (untilDate.after(eventDate) || untilDate.equals(eventDate) || repeat > 0) {
					d = eventDate.get(Calendar.DAY_OF_MONTH);
					m = eventDate.get(Calendar.MONTH);
					y = eventDate.get(Calendar.YEAR);
					
					eldd = d + "";
					elmm = (m + 1) + "";
					elyyyy = y + "";

					if (d == day.get(Calendar.DAY_OF_MONTH) && m == day.get(Calendar.MONTH) && y == day.get(Calendar.YEAR)) {
						return true;
					}
					d += step;
					eventDate.set(y, m, d);
					if ((--repeat) == 0) {
						break;
					}
				}
			} else if (rr.getType().equals("F")) {
				// Giorni Feriali

				while (untilDate.after(eventDate) || untilDate.equals(eventDate) || repeat > 0) {
					d = eventDate.get(Calendar.DAY_OF_MONTH);
					m = eventDate.get(Calendar.MONTH);
					y = eventDate.get(Calendar.YEAR);
					int dow = eventDate.get(Calendar.DAY_OF_WEEK) - 1;
					if (dow <= 0) {
						dow = 7;
					}
					if (dow >= 1 && dow <= 5) {

						eldd = d + "";
						elmm = (m + 1) + "";
						elyyyy = y + "";
						eventDate.set(y, m, d);

						if (d == day.get(Calendar.DAY_OF_MONTH) && m == day.get(Calendar.MONTH) && y == day.get(Calendar.YEAR)) {
							return true;
						}
					}
					d++;
					eventDate.set(y, m, d);
					if ((--repeat) == 0) {
						break;
					}
				}

			} else if (rr.getType().equals("W")) {
				int step = rr.getWeeklyFreq() * 7;

				while (untilDate.after(eventDate) || untilDate.equals(eventDate) || repeat > 0) {
					int dtemp = eventDate.get(Calendar.DAY_OF_MONTH);
					int mtemp = eventDate.get(Calendar.MONTH);
					int ytemp = eventDate.get(Calendar.YEAR);
					for (int i = 1; i <= 7; i++) {
						d = eventDate.get(Calendar.DAY_OF_MONTH);
						m = eventDate.get(Calendar.MONTH);
						y = eventDate.get(Calendar.YEAR);
						int dow = eventDate.get(Calendar.DAY_OF_WEEK) - 1;
						if (dow <= 0) {
							dow = 7;
						}
						Boolean dx = null;
						if (dow == 1) {
							dx = rr.getWeeklyDay_1();
						}
						if (dow == 2) {
							dx = rr.getWeeklyDay_2();
						}
						if (dow == 3) {
							dx = rr.getWeeklyDay_3();
						}
						if (dow == 4) {
							dx = rr.getWeeklyDay_4();
						}
						if (dow == 5) {
							dx = rr.getWeeklyDay_5();
						}
						if (dow == 6) {
							dx = rr.getWeeklyDay_6();
						}
						if (dow == 7) {
							dx = rr.getWeeklyDay_7();
						}
						if (dx != null && dx) {

							eldd = d + "";
							elmm = (m + 1) + "";
							elyyyy = y + "";
							eventDate.set(y, m, d);

							if (d == day.get(Calendar.DAY_OF_MONTH) && m == day.get(Calendar.MONTH) && y == day.get(Calendar.YEAR)) {
								return true;
							}
						}
						d++;
						eventDate.set(y, m, d);
						if (untilDate.before(eventDate)) {
							break;
						}
					}
					dtemp += step;
					eventDate.set(ytemp, mtemp, dtemp);
					if ((--repeat) == 0) {
						break;
					}
				}
			} else if (rr.getType().equals("M")) {
				int last = 32;
				int pday = rr.getMonthlyDay();
				int step = rr.getMonthlyFreq();

				boolean add = true;
				while (untilDate.after(eventDate) || untilDate.equals(eventDate) || repeat > 0) {
					d = eventDate.get(Calendar.DAY_OF_MONTH);
					m = eventDate.get(Calendar.MONTH);
					y = eventDate.get(Calendar.YEAR);
					if (add && pday != last && pday == d) {

						eldd = d + "";
						elmm = (m + 1) + "";
						elyyyy = y + "";
						eventDate.set(y, m, d);

						if (d == day.get(Calendar.DAY_OF_MONTH) && m == day.get(Calendar.MONTH) && y == day.get(Calendar.YEAR)) {
							return true;
						}
					}
					if (add && pday == last && d == eventDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {
						d = eventDate.get(Calendar.DAY_OF_MONTH);
						m = eventDate.get(Calendar.MONTH);
						y = eventDate.get(Calendar.YEAR);
						
						eldd = d + "";
						elmm = (m + 1) + "";
						elyyyy = y + "";

						if (d == day.get(Calendar.DAY_OF_MONTH) && m == day.get(Calendar.MONTH) && y == day.get(Calendar.YEAR)) {
							return true;
						}

					}
					if (add) {
						if (pday != last) {
							m += step;
							eventDate.set(y, m, pday);
						} else if (add && pday == last) {
							m += step;
							eventDate.set(y, m, 1);
							eventDate.set(y, m, eventDate.getActualMaximum(Calendar.DAY_OF_MONTH));
						}
					} else {
						if (pday != last) {
							eventDate.set(y, m, pday);
						} else if (add && pday == last) {
							eventDate.set(y, m, 1);
							eventDate.set(y, m, eventDate.getActualMaximum(Calendar.DAY_OF_MONTH));
						}
					}
					if (eventDate.get(Calendar.MONTH) != m) {
						add = false;
					} else {
						add = true;
					}

					if (d == day.get(Calendar.DAY_OF_MONTH) && m == day.get(Calendar.MONTH) && y == day.get(Calendar.YEAR)) {
						return true;
					}
					if ((--repeat) == 0) {
						break;
					}
				}
			} else if (rr.getType().equals("Y")) {
				int pday = rr.getYearlyDay();
				int pmon = rr.getYearlyFreq() - 1;
				if (pday > 0 && pday < 32 && pmon >= 0) {

					while (untilDate.after(eventDate) || untilDate.equals(eventDate) || repeat > 0) {

						d = eventDate.get(Calendar.DAY_OF_MONTH);
						m = eventDate.get(Calendar.MONTH);
						y = eventDate.get(Calendar.YEAR);
						if (m < pmon || (m == pmon && d < pday)) {
							d = pday;
							m = pmon;
							y++;
							eventDate.set(y, m, d);
							continue;
						}
						if (d == pday && m == pmon) {
							d = eventDate.get(Calendar.DAY_OF_MONTH);
							m = eventDate.get(Calendar.MONTH);
							y = eventDate.get(Calendar.YEAR);
							
							eldd = d + "";
							elmm = (m + 1) + "";
							elyyyy = y + "";

							if (d == day.get(Calendar.DAY_OF_MONTH) && m == day.get(Calendar.MONTH) && y == day.get(Calendar.YEAR)) {
								return true;
							}

						}
						y++;
						d = pday;
						m = pmon;
						eventDate.set(y, m, d);
						if ((--repeat) == 0) {
							break;
						}
					}
				}
			}

		}
		return success;
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
