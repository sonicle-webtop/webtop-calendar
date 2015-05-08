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

import com.sonicle.commons.web.json.CrudItems;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.bol.CalendarGroup;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import java.util.ArrayList;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsEvent {
	
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
	
	public String rrEndsMode;
	public Integer rrRepeatTimes;
	public String rrUntilDate;
	public String rrType;
	public String rrDaylyType;
	public Integer rrDaylyFreq;
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
	public String _groupId;
	
	public JsEvent(Event event, String calendarGroupId) {
		id = event.key;
		eventId = event.eventId;
		calendarId = event.getCalendarId();
		DateTimeZone eventTz = DateTimeZone.forID(event.getTimezone());
		startDate = CalendarManager.toYmdHmsWithZone(event.getStartDate(), eventTz);
		endDate = CalendarManager.toYmdHmsWithZone(event.getEndDate(), eventTz);
		timezone = event.getTimezone();
		allDay = event.getAllDay();
		title = event.getTitle();
		description = event.getDescription();
		location = event.getLocation();
		isPrivate = event.getIsPrivate();
		busy = event.getBusy();
		reminder = event.getReminder();
		
		rrEndsMode = event.rrEndsMode;
		rrRepeatTimes = event.rrRepeatTimes;
		rrUntilDate = CalendarManager.toYmdHmsWithZone(event.rrUntilDate, eventTz);
		rrType = event.rrType;
		rrDaylyType = event.rrDaylyType;
		rrDaylyFreq = event.rrDaylyFreq;
		rrWeeklyFreq = event.rrWeeklyFreq;
		rrWeeklyDay1 = event.rrWeeklyDay1;
		rrWeeklyDay2 = event.rrWeeklyDay2;
		rrWeeklyDay3 = event.rrWeeklyDay3;
		rrWeeklyDay4 = event.rrWeeklyDay4;
		rrWeeklyDay5 = event.rrWeeklyDay5;
		rrWeeklyDay6 = event.rrWeeklyDay6;
		rrWeeklyDay7 = event.rrWeeklyDay7;
		rrMonthlyFreq = event.rrMonthlyFreq;
		rrMonthlyDay = event.rrMonthlyDay;
		rrYearlyFreq = event.rrYearlyFreq;
		rrYearlyDay = event.rrYearlyDay;
		
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
		_groupId = calendarGroupId;
	}
	
	public static Event buildEvent(JsEvent jse, LocalTime workdayStart, LocalTime workdayEnd) {
		Event event = new Event();
		
		event.key = jse.id;
		event.eventId = jse.eventId;
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
		
		event.rrEndsMode = jse.rrEndsMode;
		event.rrRepeatTimes = jse.rrRepeatTimes;
		event.rrUntilDate = (jse.rrUntilDate != null) ? CalendarManager.parseYmdHmsWithZone(jse.rrUntilDate, eventTz) : null;
		event.rrType = jse.rrType;
		event.rrDaylyType = jse.rrDaylyType;
		event.rrDaylyFreq = jse.rrDaylyFreq;
		event.rrWeeklyFreq = jse.rrWeeklyFreq;
		event.rrWeeklyDay1 = jse.rrWeeklyDay1;
		event.rrWeeklyDay2 = jse.rrWeeklyDay2;
		event.rrWeeklyDay3 = jse.rrWeeklyDay3;
		event.rrWeeklyDay4 = jse.rrWeeklyDay4;
		event.rrWeeklyDay5 = jse.rrWeeklyDay5;
		event.rrWeeklyDay6 = jse.rrWeeklyDay6;
		event.rrWeeklyDay7 = jse.rrWeeklyDay7;
		event.rrMonthlyFreq = jse.rrMonthlyFreq;
		event.rrMonthlyDay = jse.rrMonthlyDay;
		event.rrYearlyFreq = jse.rrYearlyFreq;
		event.rrYearlyDay = jse.rrYearlyDay;
		
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
		// Ensure start < end
		if(event.getEndDate().compareTo(event.getStartDate()) < 0) {
			// Swap dates...
			DateTime dt = event.getEndDate();
			event.setEndDate(event.getStartDate());
			event.setStartDate(dt);
		}
		// Force allDay hours
		if(event.getAllDay()) {
			event.setStartDate(event.getStartDate().withTime(workdayStart));
			event.setEndDate(event.getEndDate().withTime(workdayEnd));
		}
	}
	
	public static DateTime parseYmdHmsWithZone(String date, String time, TimeZone tz) {
		return CalendarManager.parseYmdHmsWithZone(date, time, DateTimeZone.forTimeZone(tz));
	}
}
