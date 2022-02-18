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
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.calendar.CalendarUtils;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldDefs;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldValue;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.model.CustomPanel;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.util.ICal4jUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import net.fortuna.ical4j.model.Recur;
import org.joda.time.DateTime;
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
	public String tags;
	public ArrayList<Attachment> attachments;
	public ArrayList<ObjCustomFieldValue> cvalues;
	public String _recurringInfo; // Read-only
	public String _profileId; // Read-only
	public String _cfdefs; // Read-only
	
	public JsEvent(UserProfileId ownerPid, EventInstance event, Collection<CustomPanel> customPanels, Map<String, CustomField> customFields, String profileLanguageTag, DateTimeZone profileTz) {
		DateTimeZone eventTz = DateTimeZone.forID(event.getTimezone());
		DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter(eventTz);
		
		id = event.getKey();
		eventId = event.getEventId();
		calendarId = event.getCalendarId();
		
		CalendarUtils.EventBoundary eventBoundary = CalendarUtils.toEventBoundaryForRead(event.getAllDay(), event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
		startDate = ymdhmsZoneFmt.print(eventBoundary.start);
		endDate = ymdhmsZoneFmt.print(eventBoundary.end);
		timezone = eventBoundary.timezone.getID();
		allDay = eventBoundary.allDay;
		
		title = event.getTitle();
		description = event.getDescription();
		location = event.getLocation();
		isPrivate = event.getIsPrivate();
		busy = event.getBusy();
		reminder = EventInstance.Reminder.getMinutesValue(event.getReminder());
		activityId = event.getActivityId();
		masterDataId = event.getMasterDataId();
		statMasterDataId = event.getStatMasterDataId();
		causalId = event.getCausalId();
		rrule = event.getRecurrenceRule();
		
		/*
		Recur recur = ICal4jUtils.parseRRule(rrule);
		if (ICal4jUtils.recurHasUntilDate(recur)) {
			DateTime newUntil = ICal4jUtils.fromICal4jDateUTC(recur.getUntil()).withZoneRetainFields(eventTz);
			ICal4jUtils.setRecurUntilDate(recur, newUntil);
			rrule = recur.toString();
		}
		*/
		
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
		
		tags = new CompositeId(event.getTags()).toString();
		
		attachments = new ArrayList<>();
		for (EventAttachment att : event.getAttachments()) {
			Attachment jsa = new Attachment();
			jsa.id = att.getAttachmentId();
			//jsatt.lastModified = DateTimeUtils.printYmdHmsWithZone(att.getRevisionTimestamp(), profileTz);
			jsa.name = att.getFilename();
			jsa.size = att.getSize();
			attachments.add(jsa);
		}
		
		cvalues = new ArrayList<>();
		ArrayList<ObjCustomFieldDefs.Panel> panels = new ArrayList<>();
		for (CustomPanel panel : customPanels) {
			panels.add(new ObjCustomFieldDefs.Panel(panel, profileLanguageTag));
		}
		ArrayList<ObjCustomFieldDefs.Field> fields = new ArrayList<>();
		for (CustomField field : customFields.values()) {
			CustomFieldValue cvalue = null;
			if (event.hasCustomValues()) {
				cvalue = event.getCustomValues().get(field.getFieldId());
			}
			cvalues.add(cvalue != null ? new ObjCustomFieldValue(field.getType(), cvalue, profileTz) : new ObjCustomFieldValue(field.getType(), field.getFieldId()));
			fields.add(new ObjCustomFieldDefs.Field(field, profileLanguageTag));
		}
		
		// Read-only fields
		_recurringInfo = EnumUtils.toSerializedName(event.getRecurInfo());
		_profileId = ownerPid.toString();
		_cfdefs = LangUtils.serialize(new ObjCustomFieldDefs(panels, fields), ObjCustomFieldDefs.class);
	}
	
	public EventInstance toEventInstance(DateTimeZone profileTz) {
		EventInstance event = new EventInstance();
		event.setKey(id);
		event.setEventId(eventId);
		event.setCalendarId(calendarId);
		
		// Incoming fields are in a precise timezone, so we need to instantiate
		// the formatter specifying the right timezone to use.
		// Then DateTime objects are automatically translated to UTC
		DateTimeZone eventTz = DateTimeZone.forID(timezone);
		DateTime eventStart = DateTimeUtils.parseYmdHmsWithZone(startDate, eventTz);
		DateTime eventEnd = DateTimeUtils.parseYmdHmsWithZone(endDate, eventTz);
		
		CalendarUtils.EventBoundary eventBoundary = CalendarUtils.toEventBoundaryForWrite(allDay, eventStart, eventEnd, eventTz);
		event.setDatesAndTimes(eventBoundary.allDay, eventBoundary.timezone.getID(), eventBoundary.start, eventBoundary.end);
		event.setTitle(title);
		event.setDescription(description);
		event.setLocation(location);
		event.setIsPrivate(isPrivate);
		event.setBusy(busy);
		event.setReminder(EventInstance.Reminder.valueOf(reminder));
		event.setActivityId(activityId);
		event.setMasterDataId(masterDataId);
		event.setStatMasterDataId(statMasterDataId);
		event.setCausalId(causalId);
		
		// Fix recur until-date timezone: due to we do not have tz-db in client
		// we cannot move until-date into the right zone directly in browser, 
		// so we need to change it here if necessary.
		Recur recur = ICal4jUtils.parseRRule(rrule);
		if (ICal4jUtils.adjustRecurUntilDate(recur, event.getStartDate().withZone(eventTz).toLocalTime(), eventTz)) {
			rrule = recur.toString();
		}
		event.setRecurrence(rrule, DateTimeUtils.parseLocalDate(DateTimeUtils.createYmdFormatter(eventTz), rstart), null);
		
		for (JsEvent.Attendee jsa : attendees) {
			EventAttendee attendee = new EventAttendee();
			attendee.setAttendeeId(jsa.attendeeId);
			attendee.setRecipient(jsa.recipient);
			attendee.setRecipientType(EnumUtils.forSerializedName(jsa.recipientType, EventAttendee.RecipientType.class));
			attendee.setRecipientRole(EnumUtils.forSerializedName(jsa.recipientRole, EventAttendee.RecipientRole.class));
			attendee.setResponseStatus(EnumUtils.forSerializedName(jsa.responseStatus, EventAttendee.ResponseStatus.class));
			attendee.setNotify(jsa.notify);
			event.getAttendees().add(attendee);
		}
		
		event.setTags(new LinkedHashSet<>(new CompositeId().parse(tags).getTokens()));
		
		ArrayList<CustomFieldValue> customValues = new ArrayList<>();
		for (ObjCustomFieldValue jscfv : cvalues) {
			customValues.add(jscfv.toCustomFieldValue(profileTz));
		}
		event.setCustomValues(customValues);
		// Attachment needs to be treated outside this class in order to have complete access to their streams
		return event;
	}
	
	
	/*
	public static EventInstance buildEventInstance(JsEvent js) {
		EventInstance event = new EventInstance();
		event.setKey(js.id);
		event.setEventId(js.eventId);
		event.setCalendarId(js.calendarId);
		
		// Incoming fields are in a precise timezone, so we need to instantiate
		// the formatter specifying the right timezone to use.
		// Then DateTime objects are automatically translated to UTC
		DateTimeZone eventTz = DateTimeZone.forID(js.timezone);
		DateTime eventStart = DateTimeUtils.parseYmdHmsWithZone(js.startDate, eventTz);
		DateTime eventEnd = DateTimeUtils.parseYmdHmsWithZone(js.endDate, eventTz);
		
		CalendarUtils.EventBoundary eventBoundary = CalendarUtils.toEventBoundaryForWrite(js.allDay, eventStart, eventEnd, eventTz);
		event.setDatesAndTimes(eventBoundary.allDay, eventBoundary.timezone.getID(), eventBoundary.start, eventBoundary.end);
		
		//event.setDatesAndTimes(
		//		js.allDay,
		//		js.timezone,
		//		DateTimeUtils.parseYmdHmsWithZone(js.startDate, eventTz),
		//		DateTimeUtils.parseYmdHmsWithZone(js.endDate, eventTz)
		//);
		
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
		
		// Fix recur until-date timezone: due to we do not have tz-db in client
		// we cannot move until-date into the right zone directly in browser, 
		// so we need to change it here if necessary.
		Recur recur = ICal4jUtils.parseRRule(js.rrule);
		if (ICal4jUtils.adjustRecurUntilDate(recur, event.getStartDate().withZone(eventTz).toLocalTime(), eventTz)) {
			js.rrule = recur.toString();
		}
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
		
		event.setTags(new LinkedHashSet<>(new CompositeId().parse(js.tags).getTokens()));
		
		// Attachment needs to be treated outside this class in order to have complete access to their streams
		
		return event;
	}
	*/
	
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
				return JsonResult.gson().fromJson(value, List.class);
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
