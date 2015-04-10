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
import com.sonicle.webtop.calendar.bol.Event;
import com.sonicle.webtop.calendar.bol.DecoEvent;
import com.sonicle.webtop.calendar.bol.SchedulerEvent;
import com.sonicle.webtop.calendar.bol.MyCalendarGroup;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEvent.RevisionInfo;
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
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CalendarManager extends BaseServiceManager {
	
	public static final Logger logger = BaseService.getLogger(CalendarManager.class);
	private final static String SERVICE_ID = "com.sonicle.webtop.calendar";
	private final static String RESOURCE_CALENDARS = "CALENDARS";
	private final String deviceLabel;
	private final String userLabel;

	public CalendarManager(ServiceManifest manifest, String userLabel) {
		super(manifest);
		this.deviceLabel = "WT";
		this.userLabel = userLabel;
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
			List<SchedulerEvent> expEvents = null;
			for(OCalendar cal : cals) {
				
				for(DecoEvent le : edao.selectLiveDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					le.updateCalculatedFields();
					addExpandedEventDates(dates, le);
				}
				for(DecoEvent le : edao.selectLiveRecurringDatesByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					le.updateCalculatedFields();
					expEvents = expandRecurringEvent(con, cal, new SchedulerEvent(le), fromDate, toDate, userTz);
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
		return Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
	}
	
	private void addExpandedEventDates(HashSet<DateTime> dates, OEvent event) {
		int days = calculateEventLengthInDays(event)+1;
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
			List<SchedulerEvent> sevs = null;
			for(OCalendar cal : cals) {
				sevs = new ArrayList<>();
				for(DecoEvent le : edao.selectLiveByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					le.updateCalculatedFields();
					sevs.add(new SchedulerEvent(le));
				}
				for(DecoEvent le : edao.selectLiveRecurringByCalendarFromTo(con, cal.getCalendarId(), fromDate, toDate)) {
					le.updateCalculatedFields();
					sevs.add(new SchedulerEvent(le));
				}
				grpEvts.add(new GroupEvents(cal, sevs));
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
			LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(event.getStartDate(), rr.getRecur(), utz).toLocalDate(); //TODO: valutare se salvare la data già aggiornata
			LocalDate rrEnd = rec.getUntilDate().toLocalDate();
			
			// Calculate event length in order to generate events like original one
			int eventDays = calculateEventLengthInDays(event);
			
			// Iterates returned recurring periods and builds cloned events...
			SchedulerEvent recEvent;
			ORecurrenceBroken brokenRec;
			LocalDate perStart, perEnd;
			DateTime newStart, newEnd;
			for(net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
				perStart = ICal4jUtils.toJodaDateTime(per.getStart()).toLocalDate();
				perEnd = ICal4jUtils.toJodaDateTime(per.getEnd()).toLocalDate();
				
				if((perStart.compareTo(rrStart) >= 0) && (perEnd.compareTo(rrEnd) <= 0)) { // Skip unwanted dates at beginning
					newStart = event.getStartDate().withDate(perStart);
					newEnd = event.getEndDate().withDate(newStart.plusDays(eventDays).toLocalDate());
					brokenRec = brokenDates.get(perStart.toString());
					if(brokenRec == null) {
						// Generate cloned event like original one
						recEvent = cloneEvent(event, newStart, newEnd);
						recEvent.setId(SchedulerEvent.buildId(event.getEventId(), event.getEventId(), perStart));
						events.add(recEvent);
					}
				}
			}
			return events;
			
		} catch(Exception ex) {
			throw ex;
		}
	}
	
	public void getEvent(String eventUid) throws Exception {
		Connection con = null;
		
		try {
			SchedulerEvent.EventUID uid = new SchedulerEvent.EventUID(eventUid);
			con = WT.getConnection(manifest);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			
			OEvent evt = edao.select(con, uid.eventId);
			
			
			
			
		} catch(Exception ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void insertEvent(Event event) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			
			OEvent evt = new OEvent();
			evt.fillFrom(event);
			evt.setStatus(OEvent.STATUS_NEW);
			
			if(ORecurrence.hasRecurrence(event)) {
				ORecurrence rec = new ORecurrence();
				rec.fillFrom(event, evt.getStartDate(), evt.getEndDate(), evt.getTimezone());
				rec.setRecurrenceId(rdao.getSequence(con).intValue());
				rdao.insert(con, rec);
				evt.setRecurrenceId(rec.getRecurrenceId());
			}
			
			evt.setEventId(edao.getSequence(con).intValue());
			evt.setRevisionInfo(new RevisionInfo(deviceLabel, userLabel));
			edao.insert(con, evt);
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateEvent(Event event) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			
			OEvent evt = new OEvent();
			evt.fillFrom(event);
			evt.setStatus(OEvent.STATUS_NEW);
			
			if(ORecurrence.hasRecurrence(event)) {
				ORecurrence rec = new ORecurrence();
				rec.fillFrom(event, evt.getStartDate(), evt.getEndDate(), evt.getTimezone());
				rec.setRecurrenceId(rdao.getSequence(con).intValue());
				rdao.insert(con, rec);
				evt.setRecurrenceId(rec.getRecurrenceId());
			}
			
			evt.setEventId(edao.getSequence(con).intValue());
			evt.setRevisionInfo(new RevisionInfo(deviceLabel, userLabel));
			edao.insert(con, evt);
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteEvent(String target, String eventUid) throws Exception {
		Connection con = null;
		
		try {
			SchedulerEvent.EventUID uid = new SchedulerEvent.EventUID(eventUid);
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			EventDAO edao = EventDAO.getInstance();
			RecurrenceDAO rdao = RecurrenceDAO.getInstance();
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			OEvent original = edao.select(con, uid.originalEventId);
			if(original == null) throw new WTException("Unable to retrieve original event [{}]", uid.originalEventId);
			
			if(target.equals("this")) {
				if(original.getRecurrenceId() == null) { // Classic event
					if(!uid.eventId.equals(original.getEventId())) throw new Exception("In this case both ids must be equals");
					deleteEvent(con, uid.eventId);
					
				} else { // Event linked to a recurrence
					if(uid.eventId.equals(original.getEventId())) { // Recurring event
						// 1 - inserts a broken record (without new event) on deleted date
						ORecurrenceBroken rb = new ORecurrenceBroken();
						rb.setEventId(original.getEventId());
						rb.setRecurrenceId(original.getRecurrenceId());
						rb.setEventDate(uid.atDate);
						rb.setNewEventId(null);
						rbdao.insert(con, rb);
						// 2 - updates revision of original event
						edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
						
					} else { // Broken event
						// 1 - logically delete newevent (broken one)
						deleteEvent(con, uid.eventId);
						// 2 - updates revision of original event
						edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
					}
				}
			} else if(target.equals("since")) {
				if(uid.eventId.equals(original.getEventId())) { // Recurring event
					// 1 - sets until date at the day before deleted date
					ORecurrence orec = rdao.select(con, original.getRecurrenceId());
					if(orec == null) throw new WTException("Unable to retrieve original event's recurrence [{}]", original.getRecurrenceId());
					orec.setUntilDate(uid.atDate.toDateTimeAtStartOfDay().minusDays(1));
					orec.updateRRule(DateTimeZone.forID(original.getTimezone()));
					rdao.update(con, orec);
					// 2 - updates revision of original event
					edao.updateRevision(con, original.getEventId(), new RevisionInfo(deviceLabel, userLabel));
				}
				
			} else if(target.equals("all")) {
				if(original.getRecurrenceId() != null) { // We process only event linked to a recurrence
					if(uid.eventId.equals(original.getEventId())) {
						// 1 - logically delete original event
						deleteEvent(con, uid.eventId);
					}
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
	
	public void deleteEvent(Connection con, Integer eventId) throws Exception {
		EventDAO edao = EventDAO.getInstance();
		edao.logicDelete(con, eventId, new RevisionInfo(deviceLabel, userLabel));
		//TODO: cancellare reminder
		//TODO: se ricorrenza, eliminare tutte le broken dove newid!=null ??? Non servi più dato che verifico il D dell'evento ricorrente
	}
	
	public void restoreEvent(String eventUid) throws Exception {
		Connection con = null;
		
		try {
			// Scheduler IDs always contains an eventId reference.
			// For broken events extracted eventId is not equal to extracted
			// originalEventId (event generator or database event), since
			// it belongs to the recurring event
			SchedulerEvent.EventUID uid = new SchedulerEvent.EventUID(eventUid);
			if(uid.originalEventId.equals(uid.eventId)) throw new Exception("Cannot restore an event that is not broken");
			con = WT.getConnection(manifest);
			con.setAutoCommit(false);
			
			RecurrenceBrokenDAO rbdao = RecurrenceBrokenDAO.getInstance();
			
			// 1 - removes the broken record
			rbdao.deleteByNewEvent(con, uid.eventId);
			// 2 - logically delete broken event
			deleteEvent(con, uid.eventId);
			
			con.commit();
			
		} catch(Exception ex) {
			con.rollback();
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private SchedulerEvent cloneEvent(SchedulerEvent source, DateTime newStart, DateTime newEnd) {
		SchedulerEvent event = new SchedulerEvent(source);
		event.setStartDate(newStart);
		event.setEndDate(newEnd);
		return event;
	}
	
	/*
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
			LocalDate rrStart = ICal4jUtils.calculateRecurrenceStart(event.getStartDate(), rr.getRecur(), utz).toLocalDate(); //TODO: valutare se salvare la data già aggiornata
			LocalDate rrEnd = rec.getUntilDate().toLocalDate();
			
			// Calculate event length in order to generate events like original one
			int eventDays = Days.daysBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate()).getDays();
			
			// Iterates returned recurring periods and builds cloned events...
			SchedulerEvent recEvent;
			//LiveEvent newEvent;
			ORecurrenceBroken brokenRec;
			LocalDate perStart, perEnd, startDate;
			DateTime newStart, newEnd;
			for(net.fortuna.ical4j.model.Period per : (Iterable<net.fortuna.ical4j.model.Period>) periods) {
				perStart = ICal4jUtils.toJodaDateTime(per.getStart()).toLocalDate();
				perEnd = ICal4jUtils.toJodaDateTime(per.getEnd()).toLocalDate();
				
				if((perStart.compareTo(rrStart) >= 0) && (perEnd.compareTo(rrEnd) <= 0)) { // Skip unwanted dates at beginning
					//startDate = perStart.toLocalDate();
					newStart = event.getStartDate().withDate(perStart);
					newEnd = event.getEndDate().withDate(newStart.plusDays(eventDays).toLocalDate());
					brokenRec = brokenDates.get(perStart.toString());
					if(brokenRec == null) {
						// Generate cloned event like original one
						recEvent = cloneEvent(event, newStart, newEnd);
						recEvent.setId(SchedulerEvent.buildId(event.getEventId(), event.getEventId(), perStart));
						events.add(recEvent);
						
					} else {
						// Date is broken, look for newEvent...
						//newEvent = edao.selectLive(con, brokenRec.getNewEventId());
						//if(newEvent != null) {
						//	recEvent = cloneEvent(new SchedulerEvent(newEvent), newStart, newEnd);
						//	recEvent.setId(SchedulerEvent.buildId(newEvent.getEventId(), event.getEventId(), startDate));
						//	//recEvent.setIsRecurring(true);
						//	//recEvent.setIsBroken(true); // Signal that this event is breaking recurrence!
						//	events.add(recEvent);
						//	
						//} else {
						//	// No live event found, maybe it was deleted... so do nothing!
						//}
					}
				}
			}
			return events;
			
		} catch(Exception ex) {
			throw ex;
		}
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
