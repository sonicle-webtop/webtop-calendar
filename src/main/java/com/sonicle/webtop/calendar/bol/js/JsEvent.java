/* 
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.calendar.model.BaseEvent;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventInstance;
import java.util.ArrayList;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
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
	public Integer activityId;
	public String masterDataId;
	public String statMasterDataId;
	public Integer causalId;
	public String rrule;
	public String rstart;
	public ArrayList<Attendee> attendees;
	public ArrayList<Attachment> attachments;
	
	// Read-only fields
	public String _recurringInfo;
	public String _profileId;
	
	public JsEvent(EventInstance event, String ownerPid) {
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
		reminder = EventInstance.Reminder.getMinutes(event.getReminder());
		activityId = event.getActivityId();
		masterDataId = event.getMasterDataId();
		statMasterDataId = event.getStatMasterDataId();
		causalId = event.getCausalId();
		rrule = event.getRecurrenceRule();
		LocalDate rrs = null;
		if (event.getRecurrenceStartDate() != null) {
			rrs = event.getRecurrenceStartDate();
		} else {
			rrs = event.getStartDate().withZone(eventTz).toLocalDate();
		}
		rstart = DateTimeUtils.print(DateTimeUtils.createYmdFormatter(), rrs);
		
		attendees = new ArrayList<>();
		for (EventAttendee att : event.getAttendees()) {
			Attendee jsa = new Attendee();
			jsa.attendeeId = att.getAttendeeId();
			jsa.recipient = att.getRecipient();
			jsa.recipientType = EnumUtils.toSerializedName(att.getRecipientType());
			jsa.recipientRole = EnumUtils.toSerializedName(att.getRecipientRole());
			jsa.responseStatus = EnumUtils.toSerializedName(att.getResponseStatus());
			jsa.notify = att.getNotify();
			attendees.add(jsa);
		}
		
		attachments = new ArrayList<>();
		for (EventAttachment att : event.getAttachments()) {
			Attachment jsa = new Attachment();
			jsa.id = att.getAttachmentId();
			//jsatt.lastModified = DateTimeUtils.printYmdHmsWithZone(att.getRevisionTimestamp(), profileTz);
			jsa.name = att.getFilename();
			jsa.size = att.getSize();
			attachments.add(jsa);
		}
		
		// Read-only fields
		_recurringInfo = EnumUtils.toSerializedName(event.getRecurInfo());
		_profileId = ownerPid;
	}
	
	public static EventInstance buildEventInstance(JsEvent js) {
		EventInstance event = new EventInstance();
		event.setKey(js.id);
		event.setEventId(js.eventId);
		event.setCalendarId(js.calendarId);
		
		// Incoming fields are in a precise timezone, so we need to instantiate
		// the formatter specifying the right timezone to use.
		// Then DateTime objects are automatically translated to UTC
		DateTimeZone eventTz = DateTimeZone.forID(js.timezone);
		event.setDatesAndTimes(
				js.allDay,
				js.timezone,
				DateTimeUtils.parseYmdHmsWithZone(js.startDate, eventTz),
				DateTimeUtils.parseYmdHmsWithZone(js.endDate, eventTz)
		);
		
		event.setTitle(js.title);
		event.setDescription(js.description);
		event.setLocation(js.location);
		event.setIsPrivate(js.isPrivate);
		event.setBusy(js.busy);
		event.setReminder(EventInstance.Reminder.valueOf(js.reminder));
		event.setActivityId(js.activityId);
		event.setMasterDataId(js.masterDataId);
		event.setStatMasterDataId(js.statMasterDataId);
		event.setCausalId(js.causalId);
		event.setRecurrence(js.rrule, DateTimeUtils.parseLocalDate(DateTimeUtils.createYmdFormatter(eventTz), js.rstart), null);
		
		for (JsEvent.Attendee jsa : js.attendees) {
			EventAttendee attendee = new EventAttendee();
			attendee.setAttendeeId(jsa.attendeeId);
			attendee.setRecipient(jsa.recipient);
			attendee.setRecipientType(EnumUtils.forSerializedName(jsa.recipientType, EventAttendee.RecipientType.class));
			attendee.setRecipientRole(EnumUtils.forSerializedName(jsa.recipientRole, EventAttendee.RecipientRole.class));
			attendee.setResponseStatus(EnumUtils.forSerializedName(jsa.responseStatus, EventAttendee.ResponseStatus.class));
			attendee.setNotify(jsa.notify);
			event.getAttendees().add(attendee);
		}
		
		// Attachment needs to be treated outside this class in order to have complete access to their streams
		
		return event;
	}
	
	/*
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
	*/
	
	public static class Attendee {
		public String attendeeId;
		public String recipient;
		public String recipientType;
		public String recipientRole;
		public String responseStatus;
		public Boolean notify;
		
		public static class List extends ArrayList<Attendee> {
			public static List fromJson(String value) {
				return JsonResult.gson.fromJson(value, List.class);
			}
		}
	}
	
	public static class Attachment {
		public String id;
		public String name;
		public Long size;
		public String _uplId;
	}
}
