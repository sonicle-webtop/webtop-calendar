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

import com.google.gson.annotations.SerializedName;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.commons.web.json.CId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.calendar.CalendarUtils;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventEx;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventInstanceId;
import com.sonicle.webtop.calendar.model.EventRecurrence;
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
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import com.sonicle.webtop.calendar.model.EventBounds;

/**
 *
 * @author malbinola
 */
public class JsEvent {
	public String id;
	public String eventId;
	public Integer calendarId;
	public String start;
	public String end;
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
	public String _profileId; // Read-only
	public String _cfdefs; // Read-only
	
	public JsEvent(UserProfileId ownerPid, EventInstance event, Collection<CustomPanel> customPanels, Map<String, CustomField> customFields, String profileLanguageTag, DateTimeZone profileTz) {
		DateTimeZone eventTz = DateTimeZone.forID(event.getTimezone());
		DateTimeFormatter ymdhmsZoneFmt = JodaTimeUtils.createFormatterYMDHMS(eventTz);
		
		id = event.getId().toString();
		eventId = event.getOriginalEventId();
		calendarId = event.getCalendarId();
		
		EventBounds eventBoundary = CalendarUtils.toEventBoundsForUIRead(event.getAllDay(), event.getStart(), event.getEnd(), event.getTimezoneObject());
		start = ymdhmsZoneFmt.print(eventBoundary.getStart());
		end = ymdhmsZoneFmt.print(eventBoundary.getEnd());
		timezone = eventBoundary.getTimezoneObject().getID();
		allDay = eventBoundary.isAllDay();
		
		title = event.getTitle();	
		description = event.getDescription();
		location = event.getLocation();
		isPrivate = event.isVisibilityPrivate();
		busy = event.isTransparencyOpaque();
		reminder = EventInstance.Reminder.getMinutesValue(event.getReminder());
		rrule = event.hasRecurrence() ? event.getRecurrence().getRule() : null;
		
		/*
		Recur recur = ICal4jUtils.parseRRule(rrule);
		if (ICal4jUtils.recurHasUntilDate(recur)) {
			DateTime newUntil = ICal4jUtils.fromICal4jDateUTC(recur.getUntil()).withZoneRetainFields(eventTz);
			ICal4jUtils.setRecurUntilDate(recur, newUntil);
			rrule = recur.toString();
		}
		*/
		
		LocalDate rrs = null;
		if (event.hasRecurrence()) {
			rrs = event.getRecurrence().getStart().withZone(eventTz).toLocalDate();
		} else {
			rrs = event.getStart().withZone(eventTz).toLocalDate();
		}
		rstart = JodaTimeUtils.print(JodaTimeUtils.createFormatterYMD(), rrs);
		
		attendees = new ArrayList<>();
		for (EventAttendee att : event.getAttendeesOrEmpty()) {
			Attendee jsa = new Attendee();
			jsa.attendeeId = att.getAttendeeId();
			jsa.recipient = att.getRecipient();
			jsa.recipientType = EnumUtils.toSerializedName(att.getRecipientType());
			jsa.recipientRole = EnumUtils.toSerializedName(att.getRecipientRole());
			jsa.responseStatus = EnumUtils.toSerializedName(att.getResponseStatus());
			jsa.notify = att.getNotify();
			attendees.add(jsa);
		}
		
		this.tags = CId.build(event.getTags()).toString();
		
		attachments = new ArrayList<>();
		for (EventAttachment att : event.getAttachmentsOrEmpty()) {
			Attachment jsa = new Attachment();
			jsa.id = att.getAttachmentId();
			//jsatt.lastModified = JodaTimeUtils.printYMDHMS(profileTz, att.getRevisionTimestamp());
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
		_profileId = ownerPid.toString();
		_cfdefs = LangUtils.serialize(new ObjCustomFieldDefs(panels, fields), ObjCustomFieldDefs.class);
	}
	
	public EventEx createEventForAdd(DateTimeZone profileTz) {
		DateTimeZone tz = LangUtils.coalesce(JodaTimeUtils.parseTimezone(timezone), profileTz);
		DateTimeFormatter fmtYmdHms = JodaTimeUtils.createFormatterYMDHMS(tz);
		
		EventEx item = new EventEx();
		item.setCalendarId(calendarId);
		
		// Incoming fields are in a precise timezone, so we need to instantiate
		// the formatter specifying the right timezone to use.
		// Then DateTime objects are automatically translated to UTC
		DateTime dtStart = JodaTimeUtils.parseDateTime(fmtYmdHms, start);
		DateTime dtEnd = JodaTimeUtils.parseDateTime(fmtYmdHms, end);
		EventBounds eventBoundary = CalendarUtils.toEventBoundsForUIWrite(allDay, dtStart, dtEnd, tz);
		item.setDatesAndTimes(eventBoundary.isAllDay(), eventBoundary.getTimezoneObject().getID(), eventBoundary.getStart(), eventBoundary.getEnd());
		
		item.setTitle(title);
		item.setLocation(location);
		item.setDescriptionType(EventEx.BodyType.TEXT);
		item.setDescription(description);
		item.setVisibility(isPrivate ? EventEx.Visibility.PRIVATE : EventEx.Visibility.PUBLIC);
		item.setTransparency(busy ? EventEx.Transparency.OPAQUE : EventEx.Transparency.TRANSPARENT);
		item.setReminder(EventEx.Reminder.valueOf(reminder));
		
		if (!StringUtils.isBlank(rrule)) {
			LocalTime lt = item.getStart().toLocalTime();
			// Fix recur until-date timezone: due to we do not have tz-db in client
			// we cannot move until-date into the right zone directly in browser, 
			// so we need to change it here if necessary.
			Recur recur = ICal4jUtils.parseRRule(rrule);
			if (ICal4jUtils.adjustRecurUntilDate(recur, lt, tz)) {
				rrule = recur.toString();
			}
			//item.setRecurrence(new EventRecurrence(rrule, JodaTimeUtils.parseLocalDate(fmtYmd, rstart)));
			item.setRecurrence(new EventRecurrence(rrule, item.getStart()));
		}
		
		item.setAttendees(new ArrayList<>());
		for (JsEvent.Attendee jsa : attendees) {
			EventAttendee attendee = new EventAttendee();
			attendee.setAttendeeId(jsa.attendeeId);
			attendee.setRecipient(jsa.recipient);
			attendee.setRecipientType(EnumUtils.forSerializedName(jsa.recipientType, EventAttendee.RecipientType.class));
			attendee.setRecipientRole(EnumUtils.forSerializedName(jsa.recipientRole, EventAttendee.RecipientRole.class));
			attendee.setResponseStatus(EnumUtils.forSerializedName(jsa.responseStatus, EventAttendee.ResponseStatus.class));
			attendee.setNotify(jsa.notify);
			item.getAttendees().add(attendee);
		}
		
		item.setTags(new LinkedHashSet<>(new CId(tags).getTokens()));
		
		ArrayList<CustomFieldValue> customValues = new ArrayList<>();
		for (ObjCustomFieldValue jscfv : cvalues) {
			customValues.add(jscfv.toCustomFieldValue(profileTz));
		}
		item.setCustomValues(customValues);
		// Attachment needs to be treated outside this class in order to have complete access to their streams
		return item;
	}
	
	public EventEx createEventForUpdate(DateTimeZone profileTz) {
		return createEventForAdd(profileTz);
	}
	
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
