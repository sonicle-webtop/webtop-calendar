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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.VSchedulerEvent;
import com.sonicle.webtop.calendar.bol.model.SchedulerEvent;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEvent.RevisionInfo;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.OPostponedReminder;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import com.sonicle.webtop.calendar.bol.SharedCalendarGroup;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.calendar.bol.model.EventKey;
import com.sonicle.webtop.calendar.bol.model.Recurrence;
import com.sonicle.webtop.calendar.bol.model.ReminderGenId;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.calendar.dal.EventAttendeeDAO;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.dal.PostponedReminderDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceBrokenDAO;
import com.sonicle.webtop.calendar.dal.RecurrenceDAO;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CalendarManager {
	public static final Logger logger = WT.getLogger(CalendarManager.class);
	private static final String SHARE_RESOURCE_CALENDARS = "CALENDARS";
	private static final String EVENT_NORMAL = "normal";
	private static final String EVENT_BROKEN = "broken";
	private static final String EVENT_RECURRING = "recurring";
	public static final String TARGET_THIS = "this";
	public static final String TARGET_SINCE = "since";
	public static final String TARGET_ALL = "all";
	private final ServiceManifest manifest;
	private final String userLabel;

	public CalendarManager(ServiceManifest manifest, String userLabel) {
		this.manifest = manifest;
		this.userLabel = userLabel;
	}
	
	private RevisionInfo createRevisionInfo() {
		return new RevisionInfo("WT", userLabel);
	}
	
	public String getCalendarGroupId(int calendarId) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			CalendarDAO cdao = CalendarDAO.getInstance();
			OCalendar cal = cdao.select(con, calendarId);
			if(cal == null) throw new WTException("Unable to retrieve calendar [{}]", calendarId);
			return new UserProfile.Id(cal.getDomainId(), cal.getUserId()).toString();
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public LinkedHashMap<String, CalendarGroup> getCalendarGroups(Connection con, UserProfile.Id profileId) {
		LinkedHashMap<String, CalendarGroup> groups = new LinkedHashMap();
		MyCalendarGroup myGroup = null;
		SharedCalendarGroup sharedGroup = null;
		
		// Defines personal group
		myGroup = new MyCalendarGroup(profileId);
		groups.put(myGroup.getId(), myGroup);
		
		// Reads incoming shares as calendar groups
		Connection coreCon = null;
		try {
			coreCon = WT.getCoreConnection();
			ShareDAO sdao = ShareDAO.getInstance();
			List<OShare> shares = sdao.selectIncomingByServiceDomainUserResource(coreCon, WT.getServiceId(this.getClass()), profileId.getDomainId(), profileId.getUserId(), SHARE_RESOURCE_CALENDARS);
			
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
			DbUtils.closeQuietly(coreCon);
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
	
	public List<DateTime> getEventsDates(CalendarGroup group, Integer[] calendars, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws Exception {
		Connection con = null;
		HashSet<DateTime> dates = new HashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(manifest);
			
			// Lists desired calendars (tipically visibles) coming from passed list
			// Passed ids should belong to referenced group, this is ensured using 
			// domainId and userId parameters in below query.
			List<OCalendar> cals = cdao.selectByDomainUserIn(con, group.getDomainId(), group.getUserId(), calendars);
			List<SchedulerEvent> expEvents = null;
			for(OCalendar cal : cals) {
				
				for(VSchedulerEvent se : edao.viewDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					addExpandedEventDates(dates, se);
				}
				for(VSchedulerEvent se : edao.viewRecurringDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					expEvents = CalendarManager.this.calculateRecurringInstances(con, new SchedulerEvent(se), fromDate, toDate, userTz);
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
	
	public SchedulerEvent viewEvent(Integer eventId) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			return viewEvent(con, eventId);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private SchedulerEvent viewEvent(Connection con, Integer eventId) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent se = edao.view(con, eventId);
		se.updateCalculatedFields();
		return new SchedulerEvent(se);
	}
	
	public LinkedHashSet<String> calculateAvailabilitySpans(int minRange, UserProfile.Id profileId, DateTime fromDate, DateTime toDate, DateTimeZone userTz, boolean busy) throws Exception {
		Connection con = null;
		LinkedHashSet<String> hours = new LinkedHashSet<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(manifest);
			
			// Lists desired calendars by profile
			List<OCalendar> cals = cdao.selectByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			List<SchedulerEvent> sevs = new ArrayList<>();
			for(OCalendar cal : cals) {
				for(VSchedulerEvent se : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				for(VSchedulerEvent se : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
			}
			
			DateTime startDt, endDt;
			List<SchedulerEvent> recInstances = null;
			for(SchedulerEvent se : sevs) {
				if(se.getBusy() != busy) continue; // Ignore unwanted events...
				
				if(se.getRecurrenceId() == null) {
					startDt = se.getStartDate().withZone(userTz);
					endDt = se.getEndDate().withZone(userTz);
					hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
				} else {
					recInstances = calculateRecurringInstances(se, fromDate, toDate, userTz);
					for(SchedulerEvent recInstance : recInstances) {
						startDt = recInstance.getStartDate().withZone(userTz);
						endDt = recInstance.getEndDate().withZone(userTz);
						hours.addAll(generateTimeSpans(minRange, startDt.toLocalDate(), endDt.toLocalDate(), startDt.toLocalTime(), endDt.toLocalTime(), userTz));
					}
				}
			}
		
		} finally {
			DbUtils.closeQuietly(con);
		}
		return hours;
	}
	
	public ArrayList<String> generateTimeSpans(int minRange, LocalDate fromDate, LocalDate toDate, LocalTime fromTime, LocalTime toTime, DateTimeZone tz) {
		ArrayList<String> hours = new ArrayList<>();
		DateTimeFormatter ymdhmZoneFmt = DateTimeUtils.createYmdHmFormatter(tz);
		
		LocalDate date = fromDate;
		DateTime instant = null, boundaryInstant = null;
		while(date.compareTo(toDate) <= 0) {
			instant = new DateTime(tz).withDate(date).withTime(fromTime).withMinuteOfHour(0);
			instant = instant.withSecondOfMinute(0).withMillisOfSecond(0);
			boundaryInstant = new DateTime(tz).withDate(date).withTime(toTime);
			boundaryInstant = boundaryInstant.withSecondOfMinute(0).withMillisOfSecond(0);
			
			while(instant.compareTo(boundaryInstant) < 0) {
				hours.add(ymdhmZoneFmt.print(instant));
				instant = instant.plusMinutes(minRange);
			}
			date = date.plusDays(1);
		}
		
		return hours;
	}
	
	
	
	
	
	
	public static DateTime parseYmdHmsWithZone(String date, String time, DateTimeZone tz) {
		return parseYmdHmsWithZone(date + " " + time, tz);
	}
	
	public static DateTime parseYmdHmsWithZone(String dateTime, DateTimeZone tz) {
		String dt = StringUtils.replace(dateTime, "T", " ");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(tz);
		return formatter.parseDateTime(dt);
	}
	
	
	
	public List<GroupEvents> viewEvents(CalendarGroup group, Integer[] calendars, DateTime fromDate, DateTime toDate) throws Exception {
		UserProfile.Id profileId = new UserProfile.Id(group.getDomainId(), group.getUserId());
		return viewEvents(profileId, calendars, fromDate, toDate);
	}
	
	public List<GroupEvents> viewEvents(UserProfile.Id pid, Integer[] calendars, DateTime fromDate, DateTime toDate) throws Exception {
		Connection con = null;
		ArrayList<GroupEvents> grpEvts = new ArrayList<>();
		CalendarDAO cdao = CalendarDAO.getInstance();
		EventDAO edao = EventDAO.getInstance();
		
		try {
			con = WT.getConnection(manifest);
			
			// Lists desired calendars (tipically visibles) coming from passed list
			// Passed ids should belong to referenced group, this is ensured using 
			// domainId and userId parameters in below query.
			List<OCalendar> cals = cdao.selectByDomainUserIn(con, pid.getDomainId(), pid.getUserId(), calendars);
			List<SchedulerEvent> sevs = null;
			for(OCalendar cal : cals) {
				sevs = new ArrayList<>();
				for(VSchedulerEvent se : edao.viewByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				for(VSchedulerEvent se : edao.viewRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					se.updateCalculatedFields();
					sevs.add(new SchedulerEvent(se));
				}
				grpEvts.add(new GroupEvents(cal, sevs));
			}
			return grpEvts;
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SchedulerEvent> viewExpiredEvents(Connection con, DateTime fromDate, DateTime toDate) throws Exception {
		List<SchedulerEvent> sevs = new ArrayList<>();
		EventDAO edao = EventDAO.getInstance();
		
		for(VSchedulerEvent se : edao.viewExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			se.updateCalculatedFields();
			sevs.add(new SchedulerEvent(se));
		}
		/*
		for(VSchedulerEvent se : edao.viewRecurringExpiredForUpdateByFromTo(con, fromDate, toDate)) {
			se.updateCalculatedFields();
			sevs.add(new SchedulerEvent(se));
		}
		*/
		return sevs;
	}
	
	public List<SchedulerEvent> calculateRecurringInstances(SchedulerEvent recurringEvent, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			return calculateRecurringInstances(con, recurringEvent, fromDate, toDate, userTz);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event readEvent(String eventKey) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(manifest);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			EventAttendeeDAO adao = EventAttendeeDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			VSchedulerEvent se = edao.view(con, ekey.eventId);
			se.updateCalculatedFields(); //??????????????????????
			if(se == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
			
			//Event event = new Event();
			//event.fillFrom(evt);
			//event.id = eventGid;
			//event.eventId = evt.getEventId();
			
			Event event = null;
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				event = createEvent(eventKey, Event.RecurringInfo.SINGLE, se);
				event.setAttendees(createEventAttendeeList(adao.selectByEvent(con, se.getEventId())));
				
			} else if(type.equals(EVENT_RECURRING)) {
				ORecurrence rec = rdao.select(con, se.getRecurrenceId());
				if(rec == null) throw new WTException("Unable to retrieve recurrence [{}]", ekey.originalEventId);
				
				event = createEvent(eventKey, Event.RecurringInfo.RECURRING, se);
				int eventDays = calculateEventLengthInDays(se);
				event.setStartDate(event.getStartDate().withDate(ekey.atDate));
				event.setEndDate(event.getEndDate().withDate(event.getStartDate().plusDays(eventDays).toLocalDate()));
				event.fillFrom(rec);
				
			} else if(type.equals(EVENT_BROKEN)) {
				//TODO: recuperare il record ricorrenza per verifica?
				event = createEvent(eventKey, Event.RecurringInfo.BROKEN, se);
				event.setAttendees(createEventAttendeeList(adao.selectByEvent(con, se.getEventId())));
			}
			
			return event;
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addEvent(Event event) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			doEventInsert(con, event, true, true);
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void editEvent(String target, Event event, TimeZone userTz) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(event.getKey());
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Updates the event with new data
					//--original.fillFrom(event);
					//--original.setStatus(OEvent.STATUS_MODIFIED);
					//--original.setRevisionInfo(createRevisionInfo());
					//--edao.update(con, original);
					doEventUpdate(con, original, event, true);
				}
				
			} else if(type.equals(EVENT_BROKEN)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Updates broken event (follow eventId) with new data
					OEvent oevt = edao.select(con, ekey.eventId);
					if(oevt == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
					//--oevt.fillFrom(event);
					//--oevt.setStatus(OEvent.STATUS_MODIFIED);
					//--oevt.setRevisionInfo(createRevisionInfo());
					//--edao.update(con, oevt);
					doEventUpdate(con, oevt, event, true);
				}
				
			} else if(type.equals(EVENT_RECURRING)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - Inserts new broken event
					//--OEvent oevt = new OEvent();
					//--oevt.fillFrom(event);
					//--oevt.setRecurrenceId(null);
					//--oevt.setStatus(OEvent.STATUS_NEW);
					//--oevt.setRevisionInfo(createRevisionInfo());
					//--oevt.setEventId(edao.getSequence(con).intValue());
					//--edao.insert(con, oevt);
					OEvent oevt = doEventInsert(con, event, false, false);
					
					// 2 - Marks recurring event date inserting a broken record
					ORecurrenceBroken orb = new ORecurrenceBroken();
					orb.setEventId(original.getEventId());
					orb.setRecurrenceId(original.getRecurrenceId());
					orb.setEventDate(ekey.atDate);
					orb.setNewEventId(oevt.getEventId());
					rbdao.insert(con, orb);
					// 3 - Updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionInfo());
					
				} else if(target.equals(TARGET_SINCE)) {
					// 1 - Resize original recurrence (sets until date at the day before date)
					ORecurrence orec = rdao.select(con, original.getRecurrenceId());
					if(orec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					DateTime until = orec.getUntilDate();
					orec.applyEndUntil(ekey.atDate.minusDays(1).toDateTimeAtStartOfDay(), DateTimeZone.forID(original.getTimezone()), true);
					rdao.update(con, orec);
					// 2 - Updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionInfo());
					// 3 - Insert new event adjusting recurrence a bit
					event.rrEndsMode = Event.ENDS_MODE_UNTIL;
					event.rrUntilDate = until;
					doEventInsert(con, event, true, false);
					
				} else if(target.equals(TARGET_ALL)) {
					// 1 - Updates recurring event data (dates must be preserved) (+revision)
					event.setStartDate(event.getStartDate().withDate(original.getStartDate().toLocalDate()));
					event.setEndDate(event.getEndDate().withDate(original.getEndDate().toLocalDate()));
					//--original.fillFrom(event);
					//--original.setStatus(OEvent.STATUS_MODIFIED);
					//--original.setRevisionInfo(createRevisionInfo());
					//--edao.update(con, original);
					doEventUpdate(con, original, event, false);
					// 2 - Updates recurrence data
					ORecurrence orec = rdao.select(con, original.getRecurrenceId());
					orec.fillFrom(event, original.getStartDate(), original.getEndDate(), original.getTimezone());
					rdao.update(con, orec);
				}
			}
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void copyEvent(String eventKey, DateTime startDate, DateTime endDate) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			Event event = readEvent(eventKey);
			if(event == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			event.setStartDate(startDate);
			event.setEndDate(endDate);
			
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			doEventInsert(con, event, true, true);
			DbUtils.commitQuietly(con);
			
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEvent(String eventKey, DateTime startDate, DateTime endDate, String title) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL) || type.equals(EVENT_BROKEN)) {
				// 1 - Updates event's dates/times (+revision)
				OEvent evt = edao.select(con, ekey.eventId);
				if(evt == null) throw new WTException("Unable to retrieve event [{}]", ekey.eventId);
				evt.setStartDate(startDate);
				evt.setEndDate(endDate);
				evt.setTitle(title);
				evt.setStatus(OEvent.STATUS_MODIFIED);
				evt.setRevisionInfo(createRevisionInfo());
				edao.update(con, evt);
				
			} else {
				throw new WTException("Unable to move recurring event instance [{}]", ekey.eventId);
			}
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteEvent(String target, String eventKey) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(target.equals(TARGET_THIS)) {
					if(!ekey.eventId.equals(original.getEventId())) throw new Exception("In this case both ids must be equals");
					deleteEvent(con, ekey.eventId);
				}
				
			} else if(type.equals(EVENT_BROKEN)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - logically delete newevent (broken one)
					deleteEvent(con, ekey.eventId);
					// 2 - updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionInfo());
				}
				
			} else if(type.equals(EVENT_RECURRING)) {
				if(target.equals(TARGET_THIS)) {
					// 1 - inserts a broken record (without new event) on deleted date
					ORecurrenceBroken rb = new ORecurrenceBroken();
					rb.setEventId(original.getEventId());
					rb.setRecurrenceId(original.getRecurrenceId());
					rb.setEventDate(ekey.atDate);
					rb.setNewEventId(null);
					rbdao.insert(con, rb);
					// 2 - updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionInfo());
					
				} else if(target.equals(TARGET_SINCE)) {
					// 1 - resize original recurrence (sets until date at the day before deleted date)
					ORecurrence rec = rdao.select(con, original.getRecurrenceId());
					if(rec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					rec.setUntilDate(ekey.atDate.toDateTimeAtStartOfDay().minusDays(1));
					rec.updateRRule(DateTimeZone.forID(original.getTimezone()));
					rdao.update(con, rec);
					// 2 - updates revision of original event
					edao.updateRevision(con, original.getEventId(), createRevisionInfo());
					
				} else if(target.equals(TARGET_ALL)) {
					// 1 - logically delete original event
					deleteEvent(con, ekey.eventId);
				}
			}
			
			/*
			if(target.equals("this")) {
				if(original.getRecurrenceId() == null) { // Normal event
					if(!gid.eventId.equals(original.getEventId())) throw new Exception("In this case both ids must be equals");
					deleteEvent(con, gid.eventId);
					
				} else { // Event linked to a recurrence
					if(gid.eventId.equals(original.getEventId())) { // Recurring event
						// 1 - inserts a broken record (without new event) on deleted date
						ORecurrenceBroken rb = new ORecurrenceBroken();
						rb.setEventId(original.getEventId());
						rb.setRecurrenceId(original.getRecurrenceId());
						rb.setEventDate(gid.atDate);
						rb.setNewEventId(null);
						rbdao.insert(con, rb);
						// 2 - updates revision of original event
						edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
						
					} else { // Broken event
						// 1 - logically delete newevent (broken one)
						deleteEvent(con, gid.eventId);
						// 2 - updates revision of original event
						edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
					}
				}
			} else if(target.equals("since")) {
				if(gid.eventId.equals(original.getEventId())) { // Recurring event
					// 1 - resize original recurrence (sets until date at the day before deleted date)
					ORecurrence rec = rdao.select(con, original.getRecurrenceId());
					if(rec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					rec.setUntilDate(gid.atDate.toDateTimeAtStartOfDay().minusDays(1));
					rec.updateRRule(DateTimeZone.forID(original.getTimezone()));
					rdao.update(con, rec);
					// 2 - updates revision of original event
					edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
				}
				
			} else if(target.equals("all")) {
				if(original.getRecurrenceId() != null) { // We process only event linked to a recurrence
					if(gid.eventId.equals(original.getEventId())) {
						// 1 - logically delete original event
						deleteEvent(con, gid.eventId);
					}
				}
			}
			*/
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteEvent(Connection con, Integer eventId) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		edao.logicDelete(con, eventId, createRevisionInfo());
		//TODO: cancellare reminder
		//TODO: se ricorrenza, eliminare tutte le broken dove newid!=null ??? Non servi più dato che verifico il D dell'evento ricorrente
	}
	
	public void restoreEvent(String eventKey) throws Exception {
		Connection con = null;
		
		try {
			// Scheduler IDs always contains an eventId reference.
			// For broken events extracted eventId is not equal to extracted
			// originalEventId (event generator or database event), since
			// it belongs to the recurring event
			EventKey ekey = new EventKey(eventKey);
			if(ekey.originalEventId.equals(ekey.eventId)) throw new Exception("Cannot restore an event that is not broken");
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			
			// 1 - removes the broken record
			rbdao.deleteByNewEvent(con, ekey.eventId);
			// 2 - logically delete broken event
			deleteEvent(con, ekey.eventId);
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OPostponedReminder> getExpiredPostponedReminders(Connection con, DateTime greaterInstant) throws Exception {
		PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
		return prdao.selectExpiredForUpdateByInstant(con, greaterInstant);
	}
	
	public boolean deletePostponedReminder(Connection con, Integer eventId, DateTime remindOn) {
		PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
		return (prdao.delete(con, eventId, remindOn) == 1);
	}
	
	public void postponeReminder(String eventKey, String reminderId, int minutes) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			ReminderGenId rid = new ReminderGenId(reminderId);
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
			
			OEvent original = edao.select(con, ekey.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", ekey.originalEventId);
			
			String type = guessEventType(ekey, original);
			if(type.equals(EVENT_NORMAL)) {
				if(original.getReminder() == null) throw new WTException("Event has not an active reminder [{}]", ekey.originalEventId);
				prdao.insert(con, new OPostponedReminder(ekey.originalEventId, rid.remindOn.plusMinutes(minutes)));
				
			} else if(type.equals(EVENT_BROKEN)) {
				OEvent broken = edao.select(con, ekey.eventId);
				if(broken == null) throw new WTException("Unable to retrieve broken event [{}]", ekey.eventId);
				if(broken.getReminder() == null) throw new WTException("Broken event has not an active reminder [{}]", ekey.originalEventId);
				prdao.insert(con, new OPostponedReminder(ekey.eventId, rid.remindOn.plusMinutes(minutes)));
				
			} else if(type.equals(EVENT_RECURRING)) {
				//TODO: gestire i reminder per gli eventi ricorrenti
			}
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void snoozeReminder(String eventKey, String reminderId) throws Exception {
		Connection con = null;
		
		try {
			EventKey ekey = new EventKey(eventKey);
			ReminderGenId rid = new ReminderGenId(reminderId);
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			PostponedReminderDAO prdao = PostponedReminderDAO.getInstance();
			
			prdao.delete(con, ekey.eventId, rid.remindOn);
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Event readEventByPublicUid(String eventPublicUid) throws Exception {
		String eventKey = eventKeyByPublicUid(eventPublicUid);
		if(eventKey == null) throw new WTException("Unable to find event with UID [{0}]", eventPublicUid);
		return readEvent(eventKey);
	}
	
	private SchedulerEvent viewEventByUid(Connection con, String eventPublicUid) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		VSchedulerEvent ve = edao.viewByPublicUid(con, eventPublicUid);
		if(ve == null) return null;
		ve.updateCalculatedFields();
		return new SchedulerEvent(ve);
	}
	
	public String eventKeyByPublicUid(String eventPublicUid) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			SchedulerEvent ve = viewEventByUid(con, eventPublicUid);
			return (ve == null) ? null : EventKey.buildKey(ve.getEventId(), ve.getOriginalEventId());
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<EventAttendee> getAttendees(Integer eventId, boolean notifiedOnly) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			return getAttendees(con, eventId, notifiedOnly);
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<EventAttendee> getAttendees(Connection con, Integer eventId, boolean notifiedOnly) throws Exception {
		List<OEventAttendee> attendees = null;
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		
		if(notifiedOnly) {
			attendees = eadao.selectByEventNotify(con, eventId, true);
		} else {
			attendees = eadao.selectByEvent(con, eventId);
		}
		return createEventAttendeeList(attendees);
	}
	
	public Event updateAttendeeReply(String eventUid, String attendeeUid, String response) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			SchedulerEvent ve = viewEventByUid(con, eventUid);
			
			EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
			int ret = eadao.updateAttendeeResponse(con, attendeeUid, ve.getEventId(), response);
			
			return (ret == 1) ? readEvent(ve.getKey()) : null;
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Computes event length in days.
	 * For events that starts and ends in same date, returned lenght will be 0.
	 * @param event
	 * @return 
	 */
	private int calculateEventLengthInDays(OEvent event) {
		return Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
	}
	
	/**
	 * Fills passed dates hashmap within ones coming from specified event.
	 * If event starts on 21 Apr and ends on 25 Apr, 21->25 dates will be added to the set.
	 * @param dates
	 * @param event 
	 */
	private void addExpandedEventDates(HashSet<DateTime> dates, OEvent event) {
		int days = calculateEventLengthInDays(event)+1;
		DateTime date = event.getStartDate().withTimeAtStartOfDay();
		for(int count = 1; count <= days; count++) {
			dates.add(date);
			date = date.plusDays(1);
		}
	}
	
	private List<SchedulerEvent> calculateRecurringInstances(Connection con, SchedulerEvent event, DateTime fromDate, DateTime toDate, DateTimeZone userTz) throws Exception {
		ArrayList<SchedulerEvent> events = new ArrayList<>();
		ORecurrence rec = null;
		List<ORecurrenceBroken> brokenRecs;
		HashMap<String, ORecurrenceBroken> brokenDates;
		PeriodList periods = null;
		
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
			rec.setRule(rec.buildRRule(DateTimeZone.UTC).getValue());
			rdao.updateRRule(con, rec.getRecurrenceId(), rec.getRule());
		}
		
		try {
			// Calculate event length in order to generate events like original one
			int eventDays = calculateEventLengthInDays(event);
			
			// Calcutate recurrence set for required dates range
			RRule rr = new RRule(rec.getRule());
			periods = ICal4jUtils.calculateRecurrenceSet(event.getStartDate(), event.getEndDate(), rec.getStartDate(), rr, fromDate, toDate, userTz);
			
			// Recurrence start is useful to skip undesired dates at beginning.
			// If event does not starts at recurrence real beginning (eg. event
			// start on MO but first recurrence begin on WE), ical4j lib includes 
			// those dates in calculated recurrence set, as stated in RFC 
			// (http://tools.ietf.org/search/rfc5545#section-3.8.5.3).
			LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(event.getStartDate(), rr.getRecur(), userTz).toLocalDate(); //TODO: valutare se salvare la data già aggiornata
			LocalDate rrEnd = rec.getUntilDate().toLocalDate();
			
			// Iterates returned recurring periods and builds cloned events...
			SchedulerEvent recEvent;
			LocalDate perStart, perEnd;
			DateTime newStart, newEnd;
			for(net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
				perStart = ICal4jUtils.toJodaDateTime(per.getStart()).toLocalDate();
				perEnd = ICal4jUtils.toJodaDateTime(per.getEnd()).toLocalDate();
				
				if(brokenDates.containsKey(perStart.toString())) continue; // Skip broken dates...
				if((perStart.compareTo(rrStart) >= 0) && (perEnd.compareTo(rrEnd) <= 0)) { // Skip unwanted dates at beginning
					newStart = event.getStartDate().withDate(perStart);
					newEnd = event.getEndDate().withDate(newStart.plusDays(eventDays).toLocalDate());
					// Generate cloned event like original one
					recEvent = cloneEvent(event, newStart, newEnd);
					recEvent.setKey(EventKey.buildKey(event.getEventId(), event.getEventId(), perStart));
					events.add(recEvent);
				}
			}
			return events;
			
		} catch(Exception ex) {
			throw ex;
		}
	}
	
	private String guessEventType(EventKey eventKey, OEvent original) {
		if(original.getRecurrenceId() == null) { // Normal event
			return EVENT_NORMAL;
		} else { // Event linked to a recurrence
			if(eventKey.eventId.equals(original.getEventId())) { // Recurring event
				return EVENT_RECURRING;
			} else { // Broken event
				return EVENT_BROKEN;
			}
		}
	}
	
	private OEvent doEventUpdate(Connection con, OEvent oevt, Event event, boolean attendees) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		
		oevt.fillFrom(event);
		oevt.setStatus(OEvent.STATUS_MODIFIED);
		oevt.setRevisionInfo(createRevisionInfo());
		
		eadao.selectByEvent(con, oevt.getEventId());
		
		if(attendees) {
			List<EventAttendee> fromList = createEventAttendeeList(eadao.selectByEvent(con, oevt.getEventId()));
			CollectionChangeSet<EventAttendee> changeSet = LangUtils.getCollectionChanges(fromList, event.getAttendees());
			
			OEventAttendee oatt = null;
			for(EventAttendee att : changeSet.created) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				oatt.setAttendeeId(WT.generateUUID());
				oatt.setEventId(oevt.getEventId());
				eadao.insert(con, oatt);
			}
			for(EventAttendee att : changeSet.updated) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				eadao.update(con, oatt);
			}
			for(EventAttendee att : changeSet.deleted) eadao.delete(con, att.getAttendeeId());
		}
		
		
		/*
		eadao.deleteByEvent(con, oevt.getEventId());
		if(attendees && (data.getAttendees() != null)) {
			OEventAttendee oatt = null;
			for(EventAttendee att : data.getAttendees()) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				oatt.setAttendeeId(UUID.randomUUID().toString());
				oatt.setEventId(oevt.getEventId());
				eadao.insert(con, oatt);
			}
		}
		*/
		
		edao.update(con, oevt);
		return oevt;
	}
	
	private OEvent doEventInsert(Connection con, Event event, boolean recurrence, boolean attendees) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		RecurrenceDAO rdao = RecurrenceDAO.getInstance();
		EventAttendeeDAO eadao = EventAttendeeDAO.getInstance();
		
		OEvent oevt = new OEvent();
		oevt.fillFrom(event);
		oevt.setEventId(edao.getSequence(con).intValue());
		oevt.setPublicUid(WT.generateUUID());
		oevt.setStatus(OEvent.STATUS_NEW);
		oevt.setRevisionInfo(createRevisionInfo());
		
		if(attendees && (event.getAttendees() != null)) {
			OEventAttendee oatt = null;
			for(EventAttendee att : event.getAttendees()) {
				oatt = new OEventAttendee();
				oatt.fillFrom(att);
				oatt.setAttendeeId(WT.generateUUID());
				oatt.setEventId(oevt.getEventId());
				eadao.insert(con, oatt);
			}
		}
		
		ORecurrence orec = null;
		if(recurrence && Event.hasRecurrence(event)) {
			orec = new ORecurrence();
			orec.fillFrom(event, oevt.getStartDate(), oevt.getEndDate(), oevt.getTimezone());
			orec.setRecurrenceId(rdao.getSequence(con).intValue());
			rdao.insert(con, orec);
		}
		
		if(orec != null) {
			oevt.setRecurrenceId(orec.getRecurrenceId());
		} else {
			oevt.setRecurrenceId(null);
		}
		edao.insert(con, oevt);
		return oevt;
	}
	
	private SchedulerEvent cloneEvent(SchedulerEvent source, DateTime newStart, DateTime newEnd) {
		SchedulerEvent event = new SchedulerEvent(source);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	private Event createEvent(String key, Event.RecurringInfo recurringInfo, VSchedulerEvent se) {
		Event event = new Event(key, recurringInfo, se);
		event.setEventId(se.getEventId());
		event.setPublicUid(se.getPublicUid());
		event.setCalendarId(se.getCalendarId());
		event.setStartDate(se.getStartDate());
		event.setEndDate(se.getEndDate());
		event.setTimezone(se.getTimezone());
		event.setAllDay(se.getAllDay());
		event.setTitle(se.getTitle());
		event.setDescription(se.getDescription());
		event.setLocation(se.getLocation());
		event.setIsPrivate(se.getIsPrivate());
		event.setBusy(se.getBusy());
		event.setReminder(se.getReminder());
		event.setActivityId(se.getActivityId());
		event.setCustomerId(se.getCustomerId());
		event.setStatisticId(se.getStatisticId());
		event.setCausalId(se.getCausalId());
		return event;
	}
	
	private Recurrence createEventRecurrence(ORecurrence recurrence) {
		Recurrence rec = new Recurrence();
		
		rec.setUntilDate(recurrence.getUntilDate());
		if(recurrence.getRepeat() != null) {
			rec.setEndsMode(Recurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(recurrence.getRepeat());
		} else {
			rec.setRepeatTimes(null);
			if(rec.getUntilDate().compareTo(ICal4jUtils.ifiniteDate()) == 0) {
				rec.setEndsMode(Recurrence.ENDS_MODE_NEVER);
			} else {
				rec.setEndsMode(Recurrence.ENDS_MODE_UNTIL);
			}
		}
		if(recurrence.getType().equals(Recurrence.TYPE_DAILY_FERIALI)) {
			rec.setType(Recurrence.TYPE_DAILY);
			rec.setDailyType(Recurrence.DAILY_TYPE_FERIALI);
		} else {
			if(recurrence.getType().equals(Recurrence.TYPE_DAILY)) {
				rec.setType(Recurrence.TYPE_DAILY);
				rec.setDailyType(Recurrence.DAILY_TYPE_DAY);
			} else {
				rec.setType(recurrence.getType());
				rec.setDailyType(null);
			}
		}
		rec.setDailyFreq(recurrence.getDailyFreq());
		rec.setWeeklyFreq(recurrence.getWeeklyFreq());
		rec.setWeeklyDay1(recurrence.getWeeklyDay_1());
		rec.setWeeklyDay2(recurrence.getWeeklyDay_2());
		rec.setWeeklyDay3(recurrence.getWeeklyDay_3());
		rec.setWeeklyDay4(recurrence.getWeeklyDay_4());
		rec.setWeeklyDay5(recurrence.getWeeklyDay_5());
		rec.setWeeklyDay6(recurrence.getWeeklyDay_6());
		rec.setWeeklyDay7(recurrence.getWeeklyDay_7());
		rec.setMonthlyFreq(recurrence.getMonthlyFreq());
		rec.setMonthlyDay(recurrence.getMonthlyDay());
		rec.setYearlyFreq(recurrence.getYearlyFreq());
		rec.setYearlyDay(recurrence.getYearlyDay());
		return rec;
	}
	
	private EventAttendee createEventAttendee(OEventAttendee attendee) {
		EventAttendee att = new EventAttendee();
		att.setAttendeeId(attendee.getAttendeeId());
		att.setRecipient(attendee.getRecipient());
		att.setRecipientType(attendee.getRecipientType());
		att.setResponseStatus(attendee.getResponseStatus());
		att.setNotify(attendee.getNotify());
		return att;
	}
	
	private List<EventAttendee> createEventAttendeeList(List<OEventAttendee> attendees) {
		ArrayList<EventAttendee> atts = new ArrayList<>();
		for(OEventAttendee attendee : attendees) {
			atts.add(createEventAttendee(attendee));
		}
		return atts;
	}
	
	public static class GroupEvents {
		public final OCalendar calendar;
		public final List<SchedulerEvent> events;
		
		public GroupEvents(OCalendar calendar, List<SchedulerEvent> events) {
			this.calendar = calendar;
			this.events = events;
		}
	}
}
