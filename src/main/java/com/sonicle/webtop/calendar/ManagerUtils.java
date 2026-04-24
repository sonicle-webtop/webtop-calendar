/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar;

import com.sonicle.commons.Base58;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OCalendarPropSet;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventAttachment;
import com.sonicle.webtop.calendar.bol.OEventAttendee;
import com.sonicle.webtop.calendar.bol.OEventCustomValue;
import com.sonicle.webtop.calendar.bol.VEventAttachmentWithBytes;
import com.sonicle.webtop.calendar.bol.VEventLookup;
import com.sonicle.webtop.calendar.bol.VEventObject;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarBase;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventAlertLookup;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttachmentWithBytes;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventBase;
import com.sonicle.webtop.calendar.model.EventEx;
import com.sonicle.webtop.calendar.model.EventLookup;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.mail.internet.InternetAddress;
import net.sf.qualitycheck.Check;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 *
 * @author malbinola
 */
public class ManagerUtils {
	
	public static final String CALDAV_CALENDAR_URL = "/calendars/{0}/{1}";
	public static final String CALENDAR_LINK_CALDAV = "calDav";
	
	public static String getProductName() {
		return WT.getPlatformName() + " Calendar";
	}
	
	static int toOffset(int page, int limit) {
		return limit * (page-1);
	}
	
	public static int decodeAsCalendarId(String calendarPublicUid) throws WTException {
		try {
			return Integer.valueOf(new String(Base58.decode(calendarPublicUid)));
		} catch(RuntimeException ex) { // Not a Base58 input
			throw new WTException(ex, "Invalid calendar UID encoding");
		}
	}
	
	public static String encodeAsCalendarUid(int calendarId) {
		return Base58.encode(StringUtils.leftPad(String.valueOf(calendarId), 10, "0").getBytes());
	}
	
	public static String buildEventUid(String eventId, String internetName) {
		Check.notEmpty(eventId, "eventId");
		String id = IdentifierUtils.getUUIDTimeBased(true) + "." + String.valueOf(eventId);
		return ICalendarUtils.buildUid(DigestUtils.md5Hex(id), internetName);
	}
	
	public static String buildHref(String eventId, String internetName) {
		Check.notEmpty(eventId, "eventId");
		String id = Base58.encode(StringUtils.leftPad(String.valueOf(eventId), 10, "0").getBytes());
		return buildHref(ICalendarUtils.buildUid(id, internetName));
	}
	
	public static String buildHref(String uniqueId) {
		return uniqueId + ".ics";
	}
	
	/*
	static String buildEventUid(int eventId, String internetName) {
		return buildEventUid(IdentifierUtils.getUUIDTimeBased(true), eventId, internetName);
	}
	
	static String buildEventUid(String timeBasedPart, int eventId, String internetName) {
		return buildEventUid(timeBasedPart, DigestUtils.md5Hex(String.valueOf(eventId)), internetName);
	}
	
	static String buildEventUid(String timeBasedPart, String eventPart, String internetName) {
		// Generates the uid joining a dynamic time-based string with one 
		// calculated from the real event id. This may help in subsequent phases
		// especially to determine if the event is original or is coming from 
		// an invitation.
		return ICalendarUtils.buildUid(timeBasedPart + "." + eventPart, internetName);
	}
	*/
	
	public static String buildOrganizer(UserProfileId profileId) {
		UserProfile.Data pdata = WT.getProfileData(profileId);
		InternetAddress ia = InternetAddressUtils.toInternetAddress(pdata.getEmail().getAddress(), pdata.getDisplayName());
		return ia.toString();
	}
	
	static Calendar createCalendar(OCalendar src) {
		return (src == null) ? null : fillCalendar(new Calendar(), src);
	}
	
	static <T extends Calendar> T fillCalendar(T tgt, OCalendar src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCalendarId(src.getCalendarId());
		}
		fillCalendar((CalendarBase)tgt, src);
		return tgt;
	}
	
	static <T extends CalendarBase> T fillCalendar(T tgt, OCalendar src) {
		if ((tgt != null) && (src != null)) {
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setProvider(EnumUtils.forSerializedName(src.getProvider(), Calendar.Provider.class));
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.forSerializedName(src.getSync(), Calendar.Sync.class));
			tgt.setDefaultVisibility(EnumUtils.forSerializedName(src.getDefVisibility(), EventBase.Visibility.class));
			tgt.setDefaultTransparency(EnumUtils.forSerializedName(src.getDefTransparency(), EventBase.Transparency.class));
			tgt.setDefaultReminder(src.getDefReminder());
			tgt.setNotifyOnExtUpdate(src.getNotifyOnExtUpdate());
			tgt.setParameters(src.getParameters());
			tgt.setRemoteSyncFrequency(src.getRemoteSyncFrequency());
			tgt.setRemoteSyncTimestamp(src.getRemoteSyncTimestamp());
			tgt.setRemoteSyncTag(src.getRemoteSyncTag());
		}
		return tgt;
	}
	
	static OCalendar createOCalendar(CalendarBase src) {
		return (src == null) ? null : fillOCalendar(new OCalendar(), src);
	}
	
	static OCalendar fillOCalendar(OCalendar tgt, CalendarBase src) {
		if ((tgt != null) && (src != null)) {
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setProvider(EnumUtils.toSerializedName(src.getProvider()));
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.toSerializedName(src.getSync()));
			tgt.setDefVisibility(EnumUtils.toSerializedName(src.getDefaultVisibility()));
			tgt.setDefTransparency(EnumUtils.toSerializedName(src.getDefaultTransparency()));
			tgt.setDefReminder(src.getDefaultReminder());
			tgt.setNotifyOnExtUpdate(src.getNotifyOnExtUpdate());
			tgt.setParameters(src.getParameters());
			tgt.setRemoteSyncFrequency(src.getRemoteSyncFrequency());
			tgt.setRemoteSyncTimestamp(src.getRemoteSyncTimestamp());
			tgt.setRemoteSyncTag(src.getRemoteSyncTag());
		}
		return tgt;
	}
	
	static OCalendar fillOCalendarWithDefaults(OCalendar tgt, UserProfileId targetProfile, CalendarServiceSettings ss) {
		if (tgt != null) {
			if (tgt.getDomainId() == null) tgt.setDomainId(targetProfile.getDomainId());
			if (tgt.getUserId() == null) tgt.setUserId(targetProfile.getUserId());
			if (tgt.getBuiltIn() == null) tgt.setBuiltIn(false);
			if (StringUtils.isBlank(tgt.getProvider())) tgt.setProvider(EnumUtils.toSerializedName(Calendar.Provider.LOCAL));
			if (StringUtils.isBlank(tgt.getColor())) tgt.setColor("#F3F4F6");
			if (StringUtils.isBlank(tgt.getSync())) tgt.setSync(EnumUtils.toSerializedName(ss.getDefaultCalendarSync()));
			if (tgt.getDefVisibility()== null) tgt.setDefVisibility(EnumUtils.toSerializedName(EventBase.Visibility.PUBLIC));
			if (tgt.getDefTransparency()== null) tgt.setDefTransparency(EnumUtils.toSerializedName(EventBase.Transparency.OPAQUE));
			//if (tgt.getEventInvitation() == null) tgt.setEventInvitation(false);
			//if (tgt.getNotifyOnSelfUpdate() == null) tgt.setNotifyOnSelfUpdate(false); // Not yet supported!
			if (tgt.getNotifyOnExtUpdate() == null) tgt.setNotifyOnExtUpdate(false);
		}
		return tgt;
	}
	
	static CalendarPropSet createCalendarPropSet(OCalendarPropSet src) {
		return  (src == null) ? null : fillCalendarPropSet(new CalendarPropSet(), src);
	}
	
	static CalendarPropSet fillCalendarPropSet(CalendarPropSet tgt, OCalendarPropSet src) {
		if ((tgt != null) && (src != null)) {
			tgt.setHidden(src.getHidden());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.forSerializedName(src.getSync(), Calendar.Sync.class));
		}
		return tgt;
	}
	
	static OCalendarPropSet createOCalendarPropSet(CalendarPropSet src) {
		return (src == null) ? null : fillOCalendarPropSet(new OCalendarPropSet(), src);
	}
	
	static OCalendarPropSet fillOCalendarPropSet(OCalendarPropSet fill, CalendarPropSet with) {
		if ((fill != null) && (with != null)) {
			fill.setHidden(with.getHidden());
			fill.setColor(with.getColor());
			fill.setSync(EnumUtils.toSerializedName(with.getSync()));
		}
		return fill;
	}
	
	static <T extends EventObject> T fillEventObject(T tgt, VEventObject src) {
		if ((tgt != null) && (src != null)) {
			tgt.setEventId(src.getEventId());
			tgt.setCalendarId(src.getCalendarId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), Event.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setHref(src.getHref());
		}
		return tgt;
	}
	
	static <T extends EventLookup> T fillEventLookup(T tgt, VEventLookup src) {
		fillEvent((EventBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setTags(src.getTags());
			tgt.setHasRecurrence(src.getHasRecurrence());
			tgt.setAttendeesCount(src.getAttendeesCount());
			tgt.setNotifyableAttendeesCount(src.getNotifyableAttendeesCount());
			tgt.setCalendarName(src.getCalendarName());
			tgt.setCalendarDomainId(src.getCalendarDomainId());
			tgt.setCalendarUserId(src.getCalendarUserId());
		}
		return tgt;
	}
	
	static <T extends EventAlertLookup> T fillEventAlertLookup(T tgt, VEventLookup src) {
		fillEvent((EventBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setRemindedAt(src.getRemindedAt());
			tgt.setHasRecurrence(src.getHasRecurrence());
			tgt.setCalendarName(src.getCalendarName());
			tgt.setCalendarDomainId(src.getCalendarDomainId());
			tgt.setCalendarUserId(src.getCalendarUserId());
		}
		return tgt;
	}
	
	static <T extends Event> T fillEvent(T tgt, OEvent src) {
		fillEvent((EventEx)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setEventId(src.getEventId());
			tgt.setSeriesEventId(src.getSeriesEventId());
			tgt.setSeriesInstanceId(src.getSeriesInstanceId());
		}
		return tgt;
	}
	
	static <T extends EventEx> T fillEvent(T tgt, OEvent src) {
		fillEvent((EventBase)tgt, src);
		if ((tgt != null) && (src != null)) {}
		return tgt;
	}
	
	static <T extends EventBase> T fillEvent(T tgt, OEvent src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCalendarId(src.getCalendarId());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), Event.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setRowStatus(EnumUtils.forSerializedName(src.getRowStatus(), EventBase.RowStatus.class));
			tgt.setStatus(EnumUtils.forSerializedName(src.getStatus(), EventBase.Status.class));
			tgt.setOrganizer(src.getOrganizer());
			tgt.setOrganizerId(src.getOrganizerId());
			tgt.setTitle(src.getTitle());
			tgt.setLocation(src.getLocation());
			tgt.setDescriptionType(EnumUtils.forSerializedName(src.getDescriptionType(), EventBase.BodyType.class));
			tgt.setDescription(src.getDescription());
			tgt.setTimezone(src.getTimezone());
			tgt.setAllDay(src.getAllDay());
			tgt.setStart(src.getStart());
			tgt.setEnd(src.getEnd());
			tgt.setVisibility(EnumUtils.forSerializedName(src.getVisibility(), EventBase.Visibility.class));
			tgt.setTransparency(EnumUtils.forSerializedName(src.getTransparency(), EventBase.Transparency.class));
			tgt.setHref(src.getHref());
			tgt.setEtag(src.getEtag());
			tgt.setReminder(Event.Reminder.valueOf(src.getReminder()));
		}
		return tgt;
	}
	
	static OEvent fillOEventWithDefaultsForInsert(OEvent tgt, UserProfileId targetProfile, DateTime defaultTimestamp) {
		if (tgt != null) {
			if (StringUtils.isBlank(tgt.getPublicUid())) {
				tgt.setPublicUid(ManagerUtils.buildEventUid(tgt.getEventId(), WT.getPrimaryDomainName(targetProfile.getDomainId())));
			}
			if (tgt.getRevisionTimestamp()== null) tgt.setRevisionTimestamp(defaultTimestamp);
			if (tgt.getRevisionSequence() == null) tgt.setRevisionSequence(0);
			if (tgt.getCreationTimestamp() == null) tgt.setCreationTimestamp(defaultTimestamp);
			if (tgt.getRowStatus()== null) tgt.setRowStatus(EnumUtils.toSerializedName(EventBase.RowStatus.DEFAULT));
			if (tgt.getStatus() == null) tgt.setStatus(EnumUtils.toSerializedName(EventBase.Status.CONFIRMED));
			if (StringUtils.isBlank(tgt.getOrganizer())) {
				tgt.setOrganizer(buildOrganizer(targetProfile));
				tgt.setOrganizerId(targetProfile.getUserId());
			}
			if (tgt.getTimezone() == null) {
				UserProfile.Data pdata = WT.getProfileData(targetProfile);
				if (pdata != null) tgt.setTimezone(pdata.getTimeZoneId());
			}
			if (tgt.getDescriptionType() == null) tgt.setDescriptionType(EnumUtils.toSerializedName(EventBase.BodyType.TEXT));
			if (StringUtils.isBlank(tgt.getHref())) tgt.setHref(ManagerUtils.buildHref(tgt.getEventId(), WT.getPrimaryDomainName(targetProfile.getDomainId())));
		}
		return tgt;
	}
	
	static OEvent fillOEventWithDefaultsForUpdate(OEvent tgt, DateTime defaultTimestamp) {
		if (tgt != null) {
			if (tgt.getDescriptionType()== null) tgt.setDescriptionType(EnumUtils.toSerializedName(EventBase.BodyType.TEXT));
		}
		return tgt;
	}
	
	static OEvent fillOEvent(OEvent tgt, EventEx src) {
		fillOEvent(tgt, (EventBase)src);
		if ((tgt != null) && (src != null)) {
			
		}
		return tgt;
	}
	
	static OEvent fillOEvent(OEvent tgt, EventBase src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCalendarId(src.getCalendarId());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setRevisionStatus(EnumUtils.toSerializedName(src.getRevisionStatus()));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			tgt.setOrganizer(src.getOrganizer());
			tgt.setOrganizerId(src.getOrganizerId());
			tgt.setStart(src.getStart());
			tgt.setEnd(src.getEnd());
			tgt.setTimezone(src.getTimezone());
			tgt.setAllDay(src.getAllDay());
			tgt.setTitle(src.getTitle());
			tgt.setLocation(src.getLocation());
			tgt.setDescriptionType(EnumUtils.toSerializedName(src.getDescriptionType()));
			tgt.setDescription(src.getDescription());
			tgt.setVisibility(EnumUtils.toSerializedName(src.getVisibility()));
			tgt.setTransparency(EnumUtils.toSerializedName(src.getTransparency()));
			tgt.setStatus(EnumUtils.toSerializedName(src.getStatus()));
			tgt.setReminder(Event.Reminder.getMinutesValue(src.getReminder()));
			tgt.setHref(src.getHref());
			tgt.setEtag(src.getEtag());
		}
		return tgt;
	}
	
	static EventAttendee createEventAttendee(OEventAttendee src) {
		return (src == null) ? null : fillEventAttendee(new EventAttendee(), src);
	}
	
	static EventAttendee fillEventAttendee(EventAttendee tgt, OEventAttendee src) {
		if ((tgt != null) && (src != null)) {
			tgt.setAttendeeId(src.getAttendeeId());
			tgt.setRecipient(src.getRecipient());
			tgt.setRecipientType(EnumUtils.forSerializedName(src.getRecipientType(), EventAttendee.RecipientType.class));
			tgt.setRecipientRole(EnumUtils.forSerializedName(src.getRecipientRole(), EventAttendee.RecipientRole.class));
			tgt.setResponseStatus(EnumUtils.forSerializedName(src.getResponseStatus(), EventAttendee.ResponseStatus.class));
			tgt.setResponseTimestamp(src.getResponseTimestamp());
			tgt.setNotify(src.getNotify());
		}
		return tgt;
	}
	
	static boolean validateForInsert(EventAttendee src) {
		if (StringUtils.isBlank(src.getRecipient())) return false;
		if (src.getRecipientType() == null) return false;
		if (src.getRecipientRole() == null) return false;
		if (src.getResponseStatus() == null) return false;
		if (src.getNotify() == null) return false;
		return true;
	}
	
	static boolean validateForUpdate(EventAttendee src) {
		if (StringUtils.isBlank(src.getAttendeeId())) return false;
		if (StringUtils.isBlank(src.getRecipient())) return false;
		if (src.getRecipientType() == null) return false;
		//if (src.getRecipientRole() == null) return false;
		if (src.getResponseStatus() == null) return false;
		if (src.getNotify() == null) return false;
		return true;
	}
	
	static OEventAttendee fillOEventAttendee(OEventAttendee tgt, EventAttendee src) {
		if ((tgt != null) && (src != null)) {
			tgt.setAttendeeId(src.getAttendeeId());
			tgt.setRecipient(src.getRecipient());
			tgt.setRecipientType(EnumUtils.toSerializedName(src.getRecipientType()));
			tgt.setRecipientRole(EnumUtils.toSerializedName(src.getRecipientRole()));
			tgt.setResponseStatus(EnumUtils.toSerializedName(src.getResponseStatus()));
			tgt.setNotify(src.getNotify());
		}
		return tgt;
	}
	
	static List<EventAttendee> createEventAttendeeList(List<OEventAttendee> attendees) {
		ArrayList<EventAttendee> atts = new ArrayList<>();
		for (OEventAttendee attendee : attendees) {
			atts.add(createEventAttendee(attendee));
		}
		return atts;
	}
	
	static List<EventAttachment> createEventAttachmentList(List<OEventAttachment> items) {
		ArrayList<EventAttachment> list = new ArrayList<>(items.size());
		for (OEventAttachment item : items) {
			list.add(createEventAttachment(item));
		}
		return list;
	}
	
	static EventAttachment createEventAttachment(OEventAttachment src) {
		if (src == null) return null;
		return fillEventAttachment(new EventAttachment(), src);
	}
	
	static <T extends EventAttachment> T fillEventAttachment(T tgt, OEventAttachment src) {
		if ((tgt != null) && (src != null)) {
			tgt.setAttachmentId(src.getEventAttachmentId());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setFilename(src.getFilename());
			tgt.setSize(src.getSize());
			tgt.setMediaType(src.getMediaType());
		}
		return tgt;
	}
	
	static List<EventAttachment> createEventAttachmentListWithBytes(List<VEventAttachmentWithBytes> items) {
		ArrayList<EventAttachment> list = new ArrayList<>(items.size());
		for (VEventAttachmentWithBytes item : items) {
			list.add(createEventAttachmentWithBytes(item));
		}
		return list;
	}
	
	static EventAttachment createEventAttachmentWithBytes(VEventAttachmentWithBytes src) {
		if (src == null) return null;
		return fillEventAttachment(new EventAttachmentWithBytes(src.getBytes()), src);
	}
	
	static OEventAttachment createOEventAttachment(EventAttachment src) {
		if (src == null) return null;
		return fillOEventAttachment(new OEventAttachment(), src);
	}
	
	static <T extends OEventAttachment> T fillOEventAttachment(T tgt, EventAttachment src) {
		if ((tgt != null) && (src != null)) {
			tgt.setEventAttachmentId(src.getAttachmentId());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setFilename(src.getFilename());
			tgt.setSize(src.getSize());
			tgt.setMediaType(src.getMediaType());
		}
		return tgt;
	}
	
	static Map<String, CustomFieldValue> createCustomValuesMap(List<OEventCustomValue> items) {
		LinkedHashMap<String, CustomFieldValue> map = new LinkedHashMap<>(items.size());
		for (OEventCustomValue item : items) {
			map.put(item.getCustomFieldId(), createCustomValue(item));
		}
		return map;
	}
	
	static CustomFieldValue createCustomValue(OEventCustomValue src) {
		if (src == null) return null;
		return fillCustomFieldValue(new CustomFieldValue(), src);
	}
	
	static <T extends CustomFieldValue> T fillCustomFieldValue(T tgt, OEventCustomValue src) {
		if ((tgt != null) && (src != null)) {
			tgt.setFieldId(src.getCustomFieldId());
			tgt.setStringValue(src.getStringValue());
			tgt.setNumberValue(src.getNumberValue());
			tgt.setBooleanValue(src.getBooleanValue());
			tgt.setDateValue(src.getDateValue());
			tgt.setTextValue(src.getTextValue());
		}
		return tgt;
	}
	
	static <T extends OEventCustomValue> T fillOEventCustomValue(T tgt, CustomFieldValue src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCustomFieldId(src.getFieldId());
			tgt.setStringValue(src.getStringValue());
			tgt.setNumberValue(src.getNumberValue());
			tgt.setBooleanValue(src.getBooleanValue());
			tgt.setDateValue(src.getDateValue());
			tgt.setTextValue(src.getTextValue());
		}
		return tgt;
	}
}
