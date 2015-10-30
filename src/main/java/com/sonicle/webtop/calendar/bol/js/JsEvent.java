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
package com.sonicle.webtop.calendar.bol.js;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.calendar.bol.model.Recurrence;
import java.util.ArrayList;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsEvent {
	public static final String REC_TYPE_NONE = "_";
	public static final String REC_DAILY_TYPE_DAY = "1";
	public static final String REC_DAILY_TYPE_FERIALI = "2";
	
	public String id;
	
	public Integer eventId;
	public Integer calendarId;
	public Integer recurrenceId;
	public String startDate;
	public String endDate;
	public String timezone;
	public Boolean allDay;
	public String title;
	public String description;
	public String location;
	public Boolean isPrivate;
	public Boolean busy;
	public Integer reminder;
	public Integer activityId;
	public String customerId;
	public String statisticId;
	public Integer causalId;
	
	public String rrEndsMode;
	public Integer rrRepeatTimes;
	public String rrUntilDate;
	public String rrType;
	public String rrDailyType;
	public Integer rrDailyFreq;
	public Integer rrWeeklyFreq;
	public Boolean rrWeeklyDay1;
	public Boolean rrWeeklyDay2;
	public Boolean rrWeeklyDay3;
	public Boolean rrWeeklyDay4;
	public Boolean rrWeeklyDay5;
	public Boolean rrWeeklyDay6;
	public Boolean rrWeeklyDay7;
	public Integer rrMonthlyFreq;
	public Integer rrMonthlyDay;
	public Integer rrYearlyFreq;
	public Integer rrYearlyDay;
	
	public ArrayList<JsAttendee> attendees;
	
	// Read-only fields
	public String _recurringInfo;
	public String _profileId;
	
	public JsEvent(Event event, String profileId) {
		DateTimeZone eventTz = DateTimeZone.forID(event.getTimezone());
		DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter(eventTz);
		
		id = event.getKey();
		eventId = event.getEventId();
		calendarId = event.getCalendarId();
		startDate = ymdhmsZoneFmt.print(event.getStartDate());
		endDate = ymdhmsZoneFmt.print(event.getEndDate());
		timezone = event.getTimezone();
		allDay = event.getAllDay();
		title = event.getTitle();
		description = event.getDescription();
		location = event.getLocation();
		isPrivate = event.getIsPrivate();
		busy = event.getBusy();
		reminder = event.getReminder();
		activityId = event.getActivityId();
		customerId = event.getCustomerId();
		statisticId = event.getStatisticId();
		causalId = event.getCausalId();
		
		Recurrence rec = event.getRecurrence();
		if(rec == null) {
			rrType = REC_TYPE_NONE;
			rrDailyType = REC_DAILY_TYPE_DAY;
			rrEndsMode = Recurrence.ENDS_MODE_NEVER;
		} else {
			if(rec.getType().equals(Recurrence.TYPE_DAILY) || rec.getType().equals(Recurrence.TYPE_DAILY_FERIALI)) {
				rrType = Recurrence.TYPE_DAILY;
				rrDailyType = REC_DAILY_TYPE_DAY;
			} else if(rec.getType().equals(Recurrence.TYPE_DAILY_FERIALI)) {
				rrType = Recurrence.TYPE_DAILY;
				rrDailyType = REC_DAILY_TYPE_FERIALI;
			} else {
				rrType = rec.getType();
				rrDailyType = REC_DAILY_TYPE_DAY;
			}
			rrDailyFreq = rec.getDailyFreq();
			rrWeeklyFreq = rec.getWeeklyFreq();
			rrWeeklyDay1 = rec.getWeeklyDay1();
			rrWeeklyDay2 = rec.getWeeklyDay2();
			rrWeeklyDay3 = rec.getWeeklyDay3();
			rrWeeklyDay4 = rec.getWeeklyDay4();
			rrWeeklyDay5 = rec.getWeeklyDay5();
			rrWeeklyDay6 = rec.getWeeklyDay6();
			rrWeeklyDay7 = rec.getWeeklyDay7();
			rrMonthlyFreq = rec.getMonthlyFreq();
			rrMonthlyDay = rec.getMonthlyDay();
			rrYearlyFreq = rec.getYearlyFreq();
			rrYearlyDay = rec.getYearlyDay();
			rrEndsMode = rec.getEndsMode();
			rrRepeatTimes = rec.getRepeatTimes();
			rrUntilDate = ymdhmsZoneFmt.print(rec.getUntilDate());
		}
		
		attendees = new ArrayList<>();
		JsAttendee attendee = null;
		for(EventAttendee att : event.getAttendees()) {
			attendee = new JsAttendee();
			attendee._fk = id;
			attendee.attendeeId = att.getAttendeeId();
			attendee.recipient = att.getRecipient();
			attendee.recipientType = att.getRecipientType();
			attendee.responseStatus = att.getResponseStatus();
			attendee.notify = att.getNotify();
			attendees.add(attendee);
		}
		//attendees.addAll(event.getAttendees());
		
		// Read-only fields
		_recurringInfo = event.getRecurringInfo().toString();
		_profileId = profileId;
	}
	
	public static Event buildEvent(JsEvent jse, LocalTime workdayStart, LocalTime workdayEnd) {
		Event event = new Event();
		
		event.setKey(jse.id);
		event.setEventId(jse.eventId);
		event.setCalendarId(jse.calendarId);
		// Incoming fields are in a precise timezone, so we need to instantiate
		// the formatter specifying the right timezone to use.
		// Then DateTime objects are automatically translated to UTC
		DateTimeZone eventTz = DateTimeZone.forID(jse.timezone);
		event.setStartDate(CalendarManager.parseYmdHmsWithZone(jse.startDate, eventTz));
		event.setEndDate(CalendarManager.parseYmdHmsWithZone(jse.endDate, eventTz));
		event.setTimezone(jse.timezone);
		event.setAllDay(jse.allDay);
		adjustTimes(event, workdayStart, workdayEnd);
		event.setTitle(jse.title);
		event.setDescription(jse.description);
		event.setLocation(jse.location);
		event.setIsPrivate(jse.isPrivate);
		event.setBusy(jse.busy);
		event.setReminder(jse.reminder);
		event.setActivityId(jse.activityId);
		event.setCustomerId(jse.customerId);
		event.setStatisticId(jse.statisticId);
		event.setCausalId(jse.causalId);
		
		if(!jse.rrType.equals(REC_TYPE_NONE)) {
			Recurrence rec = new Recurrence();
			
			if(jse.rrType.equals(Recurrence.TYPE_DAILY)) {
				if(jse.rrDailyType.equals(REC_DAILY_TYPE_DAY)) {
					rec.setType(Recurrence.TYPE_DAILY);
				} else if(jse.rrDailyType.equals(REC_DAILY_TYPE_FERIALI)) {
					rec.setType(Recurrence.TYPE_DAILY_FERIALI);
				}
			} else {
				rec.setType(jse.rrType);
			}
			rec.setDailyFreq(jse.rrDailyFreq);
			rec.setWeeklyFreq(jse.rrWeeklyFreq);
			rec.setWeeklyDay1(jse.rrWeeklyDay1);
			rec.setWeeklyDay2(jse.rrWeeklyDay2);
			rec.setWeeklyDay3(jse.rrWeeklyDay3);
			rec.setWeeklyDay4(jse.rrWeeklyDay4);
			rec.setWeeklyDay5(jse.rrWeeklyDay5);
			rec.setWeeklyDay6(jse.rrWeeklyDay6);
			rec.setWeeklyDay7(jse.rrWeeklyDay7);
			rec.setMonthlyFreq(jse.rrMonthlyFreq);
			rec.setMonthlyDay(jse.rrMonthlyDay);
			rec.setYearlyFreq(jse.rrYearlyFreq);
			rec.setYearlyDay(jse.rrYearlyDay);
			rec.setEndsMode(jse.rrEndsMode);
			rec.setRepeatTimes(jse.rrRepeatTimes);
			rec.setUntilDate((jse.rrUntilDate != null) ? CalendarManager.parseYmdHmsWithZone(jse.rrUntilDate, eventTz) : null);
			event.setRecurrence(rec);
		}
		
		EventAttendee attendee = null;
		for(JsAttendee jsa : jse.attendees) {
			attendee = new EventAttendee();
			attendee.setAttendeeId(jsa.attendeeId);
			attendee.setRecipient(jsa.recipient);
			attendee.setRecipientType(jsa.recipientType);
			attendee.setResponseStatus(jsa.responseStatus);
			attendee.setNotify(jsa.notify);
			event.getAttendees().add(attendee);
		}
		return event;
	}
	
	private static void adjustTimes(Event event, LocalTime workdayStart, LocalTime workdayEnd) {
		event.setEndDate(DateTimeUtils.ceilTimeAtEndOfDay(event.getEndDate()));
		// Ensure start < end
		if(event.getEndDate().compareTo(event.getStartDate()) < 0) {
			// Swap dates...
			DateTime dt = event.getEndDate();
			event.setEndDate(event.getStartDate());
			event.setStartDate(dt);
		}
		// Correct midnight end time
		if(DateTimeUtils.isMidnight(event.getEndDate()) && DateTimeUtils.isDayBefore(event.getStartDate(), event.getEndDate())) {
			event.setEndDate(DateTimeUtils.withTimeAtEndOfDay(event.getStartDate()));
		}
		// Force allDay hours
		if(event.getAllDay()) {
			event.setStartDate(event.getStartDate().withTime(workdayStart));
			event.setEndDate(event.getEndDate().withTime(workdayEnd));
		}
	}
}
