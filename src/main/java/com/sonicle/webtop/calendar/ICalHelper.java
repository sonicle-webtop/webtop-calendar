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

import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.core.util.ICalendarUtils;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 *
 * @author malbinola
 */
public class ICalHelper {
	
	/*
	public static ArrayList<ParseResult> parseICal(LogEntries log, InputStream is, org.joda.time.DateTimeZone defaultTz) throws ParserException, IOException {
		// See http://www.kanzaki.com/docs/ical/
		ArrayList<ParseResult> retults = new ArrayList<>();
		Calendar cal = ICalendarUtils.parseRelaxed(is);
		VEvent ve = null;
		LogEntries velog = null;

		for (Iterator xi = cal.getComponents().iterator(); xi.hasNext();) {
			Component component = (Component) xi.next();
			if (component instanceof VEvent) {
				ve = (VEvent)component;
				velog = new LogEntries();
				try {
					retults.add(ICalHelper.parseVEvent(velog, ve, defaultTz));
					if(!velog.isEmpty()) {
						log.addMaster(new MessageLogEntry(LogEntry.LEVEL_WARN, "VEVENT ['{1}', {0}]", ve.getUid(), ve.getSummary()));
						log.addAll(velog);
					}
				} catch(Throwable t) {
					log.addMaster(new MessageLogEntry(LogEntry.LEVEL_ERROR, "VEVENT ['{1}', {0}]. Reason: {3}", ve.getUid(), ve.getSummary(), t.getMessage()));
				}
			}
		}
		return retults;
	}
	*/
	
	/*
	public static ParseResult parseVEvent(LogEntries log, VEvent ve, org.joda.time.DateTimeZone defaultTz) throws Exception {
		Event event = new Event();
		ArrayList<LocalDate> excludedDates = null;
		LocalDate overwritesRecurringInstance = null;
		// See http://www.kanzaki.com/docs/ical/vevent.html
		
		event.setPublicUid(ve.getUid().getValue());

		// Extracts and converts date-times
		DtStart start = ve.getStartDate();
		TimeZone startTz = guessTimeZone(start.getTimeZone(), defaultTz);
		org.joda.time.DateTime dtStart = ICal4jUtils.fromICal4jDate(start.getDate(), startTz);

		DtEnd end = ve.getEndDate();
		TimeZone endTz = null;
		org.joda.time.DateTime dtEnd = null;
		if(end == null) { // EndDate can be null
			endTz = startTz;
			dtEnd = dtStart.toDateTime();
		} else {
			endTz = startTz;
			dtEnd = ICal4jUtils.fromICal4jDate(end.getDate(), endTz).withZone(dtStart.getZone());
		}
		if(dtStart.compareTo(dtEnd) > 0) throw new WTException("StartDate [{0}] is not before event EndDate [{1}]", start.toString(), end.toString());

		// Apply dates to event
		event.setTimezone(dtStart.getZone().getID());
		if(isAllDay(dtStart, dtEnd)) {
			// Tune-up endDate if we are reading an all-day event
			event.setAllDay(true);
			event.setStartDate(dtStart.withTimeAtStartOfDay());
			event.setEndDate(DateTimeUtils.withTimeAtEndOfDay(dtStart));
		} else {
			event.setAllDay(false);
			event.setStartDate(dtStart);
			event.setEndDate(dtEnd);
		}

		// Title
		if(ve.getSummary() != null) {
			event.setTitle(StringUtils.defaultString(ve.getSummary().getValue()));
		} else {
			event.setTitle("");
			log.add(new MessageLogEntry(LogEntry.LEVEL_WARN, "Event has no title"));
		}

		// Description
		if(ve.getDescription() != null) {
			event.setDescription(StringUtils.defaultString(ve.getDescription().getValue()));
		} else {
			event.setDescription(null);
		}

		// Location
		if(ve.getLocation() != null) {
			event.setLocation(StringUtils.defaultString(ve.getLocation().getValue()));
		} else {
			event.setLocation(null);
		}

		event.setIsPrivate(false);

		// Busy flag
		if(ve.getTransparency() != null) {
			String transparency = ve.getTransparency().getValue();
			event.setBusy(!StringUtils.equals(transparency, "TRANSPARENT"));
		} else {
			event.setBusy(false);
		}

		// Others...
		event.setReminder(null);
		event.setActivityId(null);
		event.setCustomerId(null);
		event.setStatisticId(null);
		event.setCausalId(null);

		// Extract recurrence (real definition or reference to a previous instance)
		RRule rr = (RRule)ve.getProperty(Property.RRULE);
		if(rr != null) {
			event.setRecurrence(parseVEventRRule(log, rr, dtStart.getZone()));
		} else {
			RecurrenceId recurrenceId = (RecurrenceId)ve.getProperty(Property.RECURRENCE_ID);
			if(recurrenceId != null) {
				overwritesRecurringInstance = new LocalDate(recurrenceId.getDate());
			}
		}
		
		// Extract exDates
		PropertyList exDates = ve.getProperties(Property.EXDATE);
		if(!exDates.isEmpty()) {
			excludedDates = new ArrayList<>();
			for(Object o: exDates) {
				excludedDates.addAll(parseVEventExDate(log, (ExDate)o));
			}
		}
		
		// Extracts organizer
		Organizer org = (Organizer)ve.getProperty(Property.ORGANIZER);
		if(org != null) {
			try {
				event.setOrganizer(parseVEventOrganizer(log, org));
			} catch(Exception ex) {
				log.add(new MessageLogEntry(LogEntry.LEVEL_WARN, ex.getMessage()));
			}
		}

		// Extracts partecipants
		PropertyList atts = ve.getProperties(Property.ATTENDEE);
		if(!atts.isEmpty()) {
			ArrayList<EventAttendee> attendees = new ArrayList<>();
			for(Object o: atts) {
				try {
					attendees.add(parseVEventAttendee(log, (Attendee)o));
				} catch(Exception ex) {
					log.add(new MessageLogEntry(LogEntry.LEVEL_WARN, ex.getMessage()));
				}
			}
			event.setAttendees(attendees);
		}

		return new ParseResult(event, excludedDates, overwritesRecurringInstance);
	}
	
	*/
	/*
	public static Recurrence parseVEventRRule(LogEntries log, RRule rr, org.joda.time.DateTimeZone etz) throws Exception {
		Recurrence rec = new Recurrence();
		
		Recur recur = rr.getRecur();
		String freq = recur.getFrequency();
		if(freq.equals(Recur.DAILY)) {
			WeekDayList dayList = recur.getDayList();
			if(!dayList.isEmpty()) {
				rec.setType(Recurrence.TYPE_DAILY_FERIALI);
			} else {
				rec.setType(Recurrence.TYPE_DAILY);
				int dfreq = (recur.getInterval() == -1) ? 1 : recur.getInterval();
				rec.setDailyFreq(dfreq);
			}
		} else if(freq.equals(Recur.WEEKLY)) {
			rec.setType(Recurrence.TYPE_WEEKLY);
			
			int wfreq = (recur.getInterval() == -1) ? 1 : recur.getInterval();
			rec.setWeeklyFreq(wfreq);
			rec.setWeeklyDay1(false);
			rec.setWeeklyDay2(false);
			rec.setWeeklyDay3(false);
			rec.setWeeklyDay4(false);
			rec.setWeeklyDay5(false);
			rec.setWeeklyDay6(false);
			rec.setWeeklyDay7(false);
			
			WeekDayList dayList = recur.getDayList();
			if(!dayList.isEmpty()) {
				for(Object o : dayList) {
					WeekDay weekday = (WeekDay)o;
					if(weekday.equals(WeekDay.MO)) rec.setWeeklyDay1(true);
					if(weekday.equals(WeekDay.TU)) rec.setWeeklyDay2(true);
					if(weekday.equals(WeekDay.WE)) rec.setWeeklyDay3(true);
					if(weekday.equals(WeekDay.TH)) rec.setWeeklyDay4(true);
					if(weekday.equals(WeekDay.FR)) rec.setWeeklyDay5(true);
					if(weekday.equals(WeekDay.SA)) rec.setWeeklyDay6(true);
					if(weekday.equals(WeekDay.SU)) rec.setWeeklyDay7(true);
				}
			}
		} else if(freq.equals(Recur.MONTHLY)) {
			rec.setType(Recurrence.TYPE_MONTHLY);
			
			int mfreq = recur.getInterval();
			rec.setMonthlyFreq(mfreq);
			
			NumberList monthDayList = recur.getMonthDayList();
			for(Object o : monthDayList) {
				rec.setMonthlyDay((Integer)o);
			}
		} else if(freq.equals(Recur.YEARLY)) {
			rec.setType(Recurrence.TYPE_YEARLY);
			
			NumberList monthList = recur.getMonthList();
			for(Object o : monthList) {
				rec.setYearlyFreq((Integer)o);
			}
			
			NumberList monthDayList = recur.getMonthDayList();
			for(Object o : monthDayList) {
				rec.setYearlyDay((Integer)o);
			}
		} else { // Frequency type not yet supported...skip RR!
			return null;
		}
		
		if(recur.getCount() != -1) {
			rec.setEndsMode(Recurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(recur.getCount());
		} else if(recur.getUntil() == null) {
			rec.setEndsMode(Recurrence.ENDS_MODE_NEVER);
		} else {
			org.joda.time.DateTime dt = new org.joda.time.DateTime(recur.getUntil(), etz);
			rec.setEndsMode(Recurrence.ENDS_MODE_UNTIL);
			rec.setUntilDate(dt.withTimeAtStartOfDay());
		}
		
		return rec;
	}
	
	public static List<LocalDate> parseVEventExDate(LogEntries log, ExDate ex) throws Exception {
		ArrayList<LocalDate> dates = new ArrayList<>();
		Iterator it = ex.getDates().iterator();
		while(it.hasNext()) {
			dates.add(new LocalDate(it.next()));
		}
		return dates;
	}
	
	public static String parseVEventOrganizer(LogEntries log, Organizer org) throws Exception {
		InternetAddress ia = null;
		// See http://www.kanzaki.com/docs/ical/organizer.html
		
		// Evaluates organizer details
		// Extract email and common name (CN)
		// Eg: CN=Henry Cabot:MAILTO:hcabot@host2.com -> drop ":MAILTO:"
		URI uri = org.getCalAddress();
		Cn cn = (Cn)org.getParameter(Parameter.CN);
		if(uri != null) {
			String address = uri.getSchemeSpecificPart();
			ia = new InternetAddress(address, (cn == null) ? address : cn.getValue());
		} else {
			throw new WTException("Organizer must be valid [{0}]", org.toString());
			//log.add(new MessageLogEntry(LogEntry.LEVEL_WARN, "Organizer must have a valid address [{0}]", organizer.toString()));
		}
		
		return ia.toString();
	}
	
	public static EventAttendee parseVEventAttendee(LogEntries log, Attendee att) throws Exception {
		EventAttendee attendee = new EventAttendee();
		// See http://www.kanzaki.com/docs/ical/attendee.html
		
		// Evaluates attendee details
		// Extract email and common name (CN)
		// Eg: CN=Henry Cabot:MAILTO:hcabot@host2.com -> drop ":MAILTO:"
		URI uri = att.getCalAddress();
		Cn cn = (Cn)att.getParameter(Parameter.CN);
		if(uri != null) {
			String address = uri.getSchemeSpecificPart();
			attendee.setRecipient(new InternetAddress(address, (cn == null) ? address : cn.getValue()).toString());
		} else {
			throw new WTException("Attendee must be valid [{0}]", att.toString());
			//log.add(new MessageLogEntry(LogEntry.LEVEL_WARN, "Attendee must have a valid address [{0}]", attendee.toString()));
		}
		
		// Evaluates attendee role
		Role role = (Role)att.getParameter(Parameter.ROLE);
		if(role != null) {
			if(role.equals(Role.REQ_PARTICIPANT)) {
				attendee.setRecipientType(EventAttendee.RECIPIENT_TYPE_NECESSARY);
			} else {
				attendee.setRecipientType(EventAttendee.RECIPIENT_TYPE_OPTIONAL);
			}
		} else {
			attendee.setRecipientType(EventAttendee.RECIPIENT_TYPE_OPTIONAL);
		}
		
		// Evaluates attendee response status
		PartStat partstat = (PartStat)att.getParameter(Parameter.PARTSTAT);
		if(partstat != null) {
			if(partstat.equals(PartStat.ACCEPTED)) {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_ACCEPTED);
			} else if(partstat.equals(PartStat.TENTATIVE)) {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_TENTATIVE);
			} else if(partstat.equals(PartStat.DECLINED)) {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_DECLINED);
			} else {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_UNKNOWN);
			}
		} else {
			attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_UNKNOWN);
		}
		
		attendee.setNotify(false);
		return attendee;
	}
	*/
	/*
	private static TimeZone guessTimeZone(TimeZone tz, org.joda.time.DateTimeZone defaultTz) {
		if(tz == null) return ICal4jUtils.getTimeZone(defaultTz.getID());
		
		// During iCal import we can found non standard timezones coming from
		// custom/private timezone database implementation.
		// If we make a call to registry.getTimeZone() it ensures that a 
		// cleared timezone will be returned.
		// Calling .getTimeZone() using "/inverse.ca/20091015_1/Europe/Rome" 
		// we get back "Europe/Rome"; thats we are looking for.
		TimeZone guessedTz = ICal4jUtils.getTimeZone(tz.getID());
		return (guessedTz != null) ? guessedTz : ICal4jUtils.getTimeZone(defaultTz.getID());
	}
	
	private static boolean isAllDay(org.joda.time.DateTime start, org.joda.time.DateTime end) {
		// Checks if a date range can be considered as an all-day event.
		
		org.joda.time.DateTime dt = null;
		dt = start.withZone(org.joda.time.DateTimeZone.UTC);
		if(!dt.isEqual(dt.withTimeAtStartOfDay())) return false;
		dt = end.withZone(org.joda.time.DateTimeZone.UTC);
		if(!dt.isEqual(dt.withTimeAtStartOfDay())) return false;
		if(start.plusDays(1).getDayOfMonth() != end.getDayOfMonth()) return false;
		return true;
	}
	*/
	
	public static boolean isBusy(Transp transparency) {
		return !StringUtils.equals(transparency.getValue(), "TRANSPARENT");
	}
	
	public static Calendar exportICal(String prodId, ArrayList<Event> events) throws Exception {
		return exportICal(prodId, false, events);
	}
	
	public static Calendar exportICal(String prodId, boolean methodCancel, ArrayList<Event> events) throws Exception {
		//org.joda.time.DateTime now = DateTimeUtils.now();
		Calendar ical = null;
		if (methodCancel) {
			ical = ICalendarUtils.newCalendar(prodId, Method.CANCEL);
		} else {
			ical = ICalendarUtils.newCalendar(prodId, Method.REQUEST);
		}
		
		for(Event event : events) {
			ical.getComponents().add(exportEvent(event));
		}
		
		return ical;
	}
	
	public static VEvent exportEvent(Event event) throws Exception {
		org.joda.time.DateTimeZone etz = org.joda.time.DateTimeZone.forID(event.getTimezone());
		Date start = ICal4jUtils.toICal4jDateTime(event.getStartDate(), etz);
		Date end = ICal4jUtils.toICal4jDateTime(event.getEndDate(), etz);
		VEvent ve = new VEvent(start, end, event.getTitle());
		
		//TODO: consider TRANSP:TRANSPARENT
		
		// LastModified
		ICal4jUtils.addProperty(ve, new LastModified(ICal4jUtils.createDateTime(event.getRevisionTimestamp().withZone(DateTimeZone.UTC))));
		ICal4jUtils.addProperty(ve, new Sequence(0));
		
		// Uid
		ICal4jUtils.addProperty(ve, new Uid(event.getPublicUid()));
		
		// Description
		ICal4jUtils.addProperty(ve, new Description(event.getDescription()));
		
		// Location
		if(!StringUtils.isEmpty(event.getLocation())) {
			ICal4jUtils.addProperty(ve, new Location(event.getLocation()));
		}
		
		// Organizer
		String mailto = MessageFormat.format("mailto:{0}", event.getOrganizerAddress());
		Organizer organizer = new Organizer(URI.create(mailto));
		if(!StringUtils.isBlank(event.getOrganizerCN())) {
			organizer.getParameters().add(new Cn(event.getOrganizerCN()));
		}
		ve.getProperties().add(organizer);
		
		// Recerrence
		if(event.hasRecurrence()) {
			ve.getProperties().add(exportEventRecurrence(event));
		}
		
		// Attendees
		for(EventAttendee attendee : event.getAttendees()) {
			try {
				if(attendee.hasEmailRecipient()) {
					ve.getProperties().add(exportEventAttendee(attendee));
				}
			} catch(URISyntaxException | AddressException ex) {
				/* Do nothing...*/
			}
		}
		
		return ve;
	}
	
	public static RRule exportEventRecurrence(Event event) throws ParseException {
		return new RRule(event.getRecurrence().getRRule());
	}
	
	public static Attendee exportEventAttendee(EventAttendee attendee) throws Exception {
		Attendee att = new Attendee();
		ParameterList params=att.getParameters();
		// Evaluates attendee details
		// Joins email and common name (CN)
		String mailto = MessageFormat.format("mailto:{0}", attendee.getAddress());
		att.setCalAddress(URI.create(mailto));
		if(!StringUtils.isBlank(attendee.getCN())) {
			params.add(new Cn(attendee.getCN()));
		}
		
		// Evaluates attendee role
		String rpcType = attendee.getRecipientType();
		if(StringUtils.equals(rpcType, EventAttendee.RECIPIENT_TYPE_NECESSARY)) {
			params.add(Role.REQ_PARTICIPANT);
		} else {
			params.add(Role.OPT_PARTICIPANT);
		}
		
		//TODO: added for email invite
		//need to separate export and email interaction
		params.add(CuType.INDIVIDUAL);
		params.add(Rsvp.TRUE);
		
		// Evaluates attendee response status
		params.add(responseStatusToPartStat(attendee.getResponseStatus()));
		
		return att;
	}
	
	public static PartStat responseStatusToPartStat(String responseStatus) {
		if(StringUtils.equals(responseStatus, EventAttendee.RESPONSE_STATUS_ACCEPTED)) {
			return PartStat.ACCEPTED;
		} else if(StringUtils.equals(responseStatus, EventAttendee.RESPONSE_STATUS_TENTATIVE)) {
			return PartStat.TENTATIVE;
		} else if(StringUtils.equals(responseStatus, EventAttendee.RESPONSE_STATUS_DECLINED)) {
			return PartStat.DECLINED;
		} else {
			return PartStat.NEEDS_ACTION;
		}
	}
	
	public static String partStatToResponseStatus(PartStat partStat) {
		if (partStat.equals(PartStat.ACCEPTED)) {
			return EventAttendee.RESPONSE_STATUS_ACCEPTED;
		} else if (partStat.equals(PartStat.TENTATIVE)) {
			return EventAttendee.RESPONSE_STATUS_TENTATIVE;
		} else if (partStat.equals(PartStat.DECLINED)) {
			return EventAttendee.RESPONSE_STATUS_DECLINED;
		} else {
			return EventAttendee.RESPONSE_STATUS_NEEDSACTION;
		}
	}
	
	public static class ParseResult {
		Event event;
		ArrayList<LocalDate> excludedDates;
		LocalDate overwritesRecurringInstance;
		
		public ParseResult(Event event, ArrayList<LocalDate> excludedDates, LocalDate overwritesRecurringInstance) {
			this.event = event;
			this.excludedDates = excludedDates;
			this.overwritesRecurringInstance = overwritesRecurringInstance;
		}
	}
}
