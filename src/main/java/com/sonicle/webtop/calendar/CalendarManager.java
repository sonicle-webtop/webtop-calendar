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
import com.sonicle.commons.web.JsonUtils;
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.SchedulerEvent;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceBrokenDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BaseServiceManager;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CalendarManager extends BaseServiceManager {
	
	public static final Logger logger = BaseService.getLogger(CalendarManager.class);
	private final static String SERVICE_ID = "com.sonicle.webtop.calendar";
	private final static String RESOURCE_CALENDARS = "CALENDARS";

	public CalendarManager(ServiceManifest manifest) {
		super(manifest);
	}
	
	public void initilizeUser(UserProfile.Id profileId) throws Exception {
		
		// Adds built-in calendar
	}
	
	public void cleanupUser(UserProfile.Id profileId, boolean deep) {
		
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
			ccon = WT.getCoreConnection();
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
	
	public List<OCalendar> getCalendars(UserProfile.Id user) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			CalendarDAO cdao = CalendarDAO.getInstance();
			return cdao.selectByDomainUser(con, user.getDomainId(), user.getUserId());
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DateTime> getEventsDates(CalendarGroup group, DateTime fromDate, DateTime toDate, TimeZone userTz) throws Exception {
		Connection con = null;
		HashSet<DateTime> dates = new HashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(manifest);
			List<OCalendar> cals = cdao.selectVisibleByDomainUser(con, group.getDomainId(), group.getUserId());
			List<OEvent> events = null;
			List<SchedulerEvent> expEvents = null;
			for(OCalendar cal : cals) {
				
				for(OEvent oe : edao.selectLiveDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					addExpandedEventDates(dates, oe);
				}
				for(OEvent oe : edao.selectLiveRecurringDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					expEvents = expandRecurringEvent(con, cal, new SchedulerEvent(oe), fromDate, toDate, userTz);
					for(SchedulerEvent expEvent : expEvents) {
						addExpandedEventDates(dates, expEvent);
					}
				}
			}
			
		} finally {
			DbUtils.closeQuietly(con);
		}
		return new ArrayList<>(dates);
	}
	
	private int calculateEventLengthInDays(OEvent event) {
		return Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays() +1;
	}
	
	private void addExpandedEventDates(HashSet<DateTime> dates, OEvent event) {
		int days = calculateEventLengthInDays(event);
		DateTime date = event.getStartDate().withTimeAtStartOfDay();
		for(int count = 1; count <= days; count++) {
			dates.add(date);
			date = date.plusDays(1);
		}
	}
	
	public List<GroupEvents> getEvents(CalendarGroup group, DateTime fromDate, DateTime toDate) throws Exception {
		Connection con = null;
		ArrayList<GroupEvents> grpEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(manifest);
			List<OCalendar> cals = cdao.selectVisibleByDomainUser(con, group.getDomainId(), group.getUserId());
			List<SchedulerEvent> evts = null;
			for(OCalendar cal : cals) {
				evts = new ArrayList<>();
				for(OEvent oe : edao.selectLiveByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					evts.add(new SchedulerEvent(oe));
				}
				for(OEvent oe : edao.selectLiveRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					evts.add(new SchedulerEvent(oe));
				}
				grpEvts.add(new GroupEvents(cal, evts));
			}
			return grpEvts;
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SchedulerEvent> expandRecurringEvent(OCalendar cal, SchedulerEvent event, DateTime fromDate, DateTime toDate, TimeZone userTz) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			return expandRecurringEvent(con, cal, event, fromDate, toDate, userTz);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<SchedulerEvent> expandRecurringEvent(Connection con, OCalendar cal, SchedulerEvent event, DateTime fromDate, DateTime toDate, TimeZone userTz) throws Exception {
		ArrayList<SchedulerEvent> events = new ArrayList<>();
		ORecurrence rec = null;
		List<ORecurrenceBroken> brokenRecs;
		HashMap<String, ORecurrenceBroken> brokenDates;
		PeriodList periods = null;
		
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
		
		// Retrieves reccurence and broken dates (if any)
		if(event.getRecurrenceId() == null) throw new WTException("Specified event [{}] does not have a recurrence set", event.getEventId());
		rec = rdao.select(con, event.getRecurrenceId());
		if(rec == null) throw new WTException("Unable to retrieve recurrence [{}]", event.getRecurrenceId());
		brokenRecs = rbdao.selectByEventRecurrence(con, event.getEventId(), event.getRecurrenceId());
		
		// Builds a hashset of broken dates for increasing performances
		brokenDates = new HashMap<>();
		for(ORecurrenceBroken brokenRec : brokenRecs) {
			brokenDates.put(brokenRec.getEventDate().toString(), brokenRec);
		}
		
		// If not present, updates rrule
		if(StringUtils.isEmpty(rec.getRule())) {
			rec.setRule(rec.asRRule(DateTimeZone.UTC).getValue());
			rdao.updateRRule(con, rec.getRecurrenceId(), rec.getRule());
		}
		
		try {
			DateTimeZone utz = DateTimeZone.forTimeZone(userTz);
			
			// Calcutate recurrence set for required dates range
			RRule rr = new RRule(rec.getRule());
			periods = ICal4jUtils.calculateRecurrenceSet(event.getStartDate(), event.getEndDate(), rec.getStartDate(), rr, fromDate, toDate, utz);
			
			// Recurrence start is useful to skip undesired dates at beginning.
			// If event does not starts at recurrence real beginning (eg. event
			// start on MO but first recurrence begin on WE), ical4j lib includes 
			// those dates in calculated recurrence set, as stated in RFC 
			// (http://tools.ietf.org/search/rfc5545#section-3.8.5.3).
			DateTime rrStart = ICal4jUtils.calculateRecurrenceStart(event.getStartDate(), rr.getRecur(), utz);
			
			// Calculate event length on order to generate events like original one
			int eventDays = Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
			
			// Iterates returned recurring periods and builds cloned events...
			SchedulerEvent recEvent;
			OEvent newEvent;
			ORecurrenceBroken brokenRec;
			LocalDate startDate;
			DateTime perStart, newStart, newEnd;
			for(net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
				perStart = ICal4jUtils.toJodaDateTime(per.getStart());
				
				if(perStart.compareTo(rrStart) >= 0) { // Skip unwanted dates at beginning
					startDate = perStart.toLocalDate();
					newStart = event.getStartDate().withDate(startDate);
					newEnd = event.getEndDate().withDate(newStart.plusDays(eventDays).toLocalDate());
					brokenRec = brokenDates.get(startDate.toString());
					if(brokenRec == null) {
						// Generate cloned event like original one
						recEvent = cloneEvent(event, newStart, newEnd);
						recEvent.setId(buildRecurrenceEventId(event.getEventId(), startDate));
						recEvent.setIsRecurring(true);
						events.add(recEvent);
						
					} else {
						// Date is broken, look for newEvent...
						newEvent = edao.selectLive(con, brokenRec.getNewEventId());
						if(newEvent != null) {
							recEvent = cloneEvent(new SchedulerEvent(newEvent), newStart, newEnd);
							recEvent.setId(buildRecurrenceEventId(event.getEventId(), startDate));
							recEvent.setIsRecurring(true);
							recEvent.setIsBroken(true); // Signal that this event is breaking recurrence!
							events.add(recEvent);
							
						} else {
							// No live event found, maybe it was deleted... so do nothing!
						}
					}
				}
			}
			return events;
			
		} catch(Exception ex) {
			throw ex;
		}
	}
	
	private String buildRecurrenceEventId(Integer originalEventId, LocalDate date) {
		return JsonUtils.buildRecId(originalEventId, date.toString("yyyyMMdd"));
	}
	
	private SchedulerEvent cloneEvent(SchedulerEvent source, DateTime newStart, DateTime newEnd) {
		SchedulerEvent event = new SchedulerEvent(source);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	
	
	
	
	
	
	
	
	
	
	/*
	public List<JsSchedulerEvent> expandRecurringEvent__old(Connection con, OCalendar cal, OEvent event, DateTime fromDate, DateTime toDate, TimeZone userTz) {
		ArrayList<JsSchedulerEvent> events = new ArrayList<>();
		ORecurrence rr = null;
		OEvent newEvent = null;
		
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		
		if(event.getRecurrenceId() == null) return null;
		rr = rdao.select(con, event.getRecurrenceId());
		if(rr == null) return null;
		
		// If not present, updates rrule
		if(StringUtils.isEmpty(rr.getRule())) {
			rr.setRule(rr.asRRule(DateTimeZone.UTC).getValue());
			rdao.updateRRule(con, rr.getRecurrenceId(), rr.getRule());
		}
		
		
		
		try {
			DateTimeZone utz = DateTimeZone.forTimeZone(userTz);
			
			
			CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
			VEvent ve = new VEvent(ICal4jUtils.toICal4jDateTime(event.getStartDate(), utz), ICal4jUtils.toICal4jDateTime(event.getEndDate(), utz), event.getTitle());
			//ve.getProperties().add(new RRule("FREQ=DAILY;INTERVAL=7;UNTIL=20150501T000000Z"));
			RRule rrule = new RRule("FREQ=WEEKLY;INTERVAL=2;BYDAY=TU,WE;COUNT=2");
			ve.getProperties().add(rrule);
			
			Recur recur = rrule.getRecur();
			DateTime rrStart = ICal4jUtils.calculateRecurrenceStart(event.getStartDate(), recur, utz);
			System.out.println(rrStart.toString());
			
			//net.fortuna.ical4j.model.Date dd = recur.getNextDate(ICalUtils.toICal4jDateTime(event.getStartDate(), utz), ICalUtils.toICal4jDateTime(event.getStartDate().minusDays(1), utz));
			//System.out.println(dd.toString());
			
			//DateList dates = recur.getDates(ICalUtils.toICalDate(event.getStartDate(), dtz), ICalUtils.toICalDate(event.getEndDate(), dtz), Value.DATE_TIME);
			//for (Iterator it = dates.iterator(); it.hasNext();) {
			//	net.fortuna.ical4j.model.Date recurrence = (net.fortuna.ical4j.model.Date) it.next();
			//	System.out.println(recurrence.toString());
			//}
			
			
			//net.fortuna.ical4j.model.Period per = ICalUtils.toICal4jPeriod(fromDate, toDate, utz);
			//net.fortuna.ical4j.model.Period startPeriod = ICalUtils.toICal4jPeriod(event.getStartDate(), event.getEndDate(), utz);
			//if(per.intersects(startPeriod)) {
			//	System.out.println("eliminare il primo");	
			//}
			
			PeriodList pl = ve.calculateRecurrenceSet(ICal4jUtils.toICal4jPeriod(fromDate, toDate, utz));
			DateTime start, end;
			for(net.fortuna.ical4j.model.Period p : (Iterable<net.fortuna.ical4j.model.Period>) pl) {
				start = ICal4jUtils.toJodaDateTime(p.getStart());
				end = ICal4jUtils.toJodaDateTime(p.getEnd());
				if(start.compareTo(rrStart) >= 0) {
					System.out.println(event.getTitle()+": "+start.toString()+" -> "+end.toString());
				}
				//System.out.println(event.getTitle()+": "+p.getStart().toString()+" -> "+p.getEnd().toString());
			}
			
		} catch(Exception exxx) {
			
		}
		
		
		int eventDays = Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
		
		DateTime date = fromDate, startDt, endDt;
		while((date.compareTo(toDate) <= 0) && (date.compareTo(rr.getUntilDate()) <= 0)) {
			if(legacyGetEventsInDate(con, event, rr, date, toDate)) {
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
	*/
	
	
	
	
	public static class GroupEvents {
		public final OCalendar calendar;
		public final List<SchedulerEvent> events;
		
		public GroupEvents(OCalendar calendar, List<SchedulerEvent> events) {
			this.calendar = calendar;
			this.events = events;
		}
	}
}
