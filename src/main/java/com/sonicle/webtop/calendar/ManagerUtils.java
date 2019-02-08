/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
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
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.VEventObject;
import com.sonicle.webtop.calendar.bol.VVEvent;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventAttachment;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.SchedEvent;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.mail.internet.InternetAddress;
import net.fortuna.ical4j.model.Recur;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
	
	public static String buildEventUid(int eventId, String internetName) {
		String id = IdentifierUtils.getUUIDTimeBased(true) + "." + String.valueOf(eventId);
		return ICalendarUtils.buildUid(DigestUtils.md5Hex(id), internetName);
	}
	
	public static String buildHref(String publicUid) {
		return publicUid + ".ics";
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
		UserProfile.Data ud = WT.getUserData(profileId);
		InternetAddress ia = InternetAddressUtils.toInternetAddress(ud.getEmail().getAddress(), ud.getDisplayName());
		return ia.toString();
	}
	
	static Calendar createCalendar(OCalendar src) {
		return (src == null) ? null : fillCalendar(new Calendar(), src);
	}
	
	static Calendar fillCalendar(Calendar tgt, OCalendar src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCalendarId(src.getCalendarId());
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setProvider(EnumUtils.forSerializedName(src.getProvider(), Calendar.Provider.class));
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.forSerializedName(src.getSync(), Calendar.Sync.class));
			tgt.setIsDefault(src.getIsDefault());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setDefaultBusy(src.getBusy());
			tgt.setDefaultReminder(src.getReminder());
			tgt.setNotifyOnExtUpdate(src.getNotifyOnExtUpdate());
			tgt.setParameters(src.getParameters());
			tgt.setRemoteSyncFrequency(src.getRemoteSyncFrequency());
			tgt.setRemoteSyncTimestamp(src.getRemoteSyncTimestamp());
			tgt.setRemoteSyncTag(src.getRemoteSyncTag());
		}
		return tgt;
	}
	
	static OCalendar createOCalendar(Calendar src) {
		return (src == null) ? null : fillOCalendar(new OCalendar(), src);
	}
	
	static OCalendar fillOCalendar(OCalendar tgt, Calendar src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCalendarId(src.getCalendarId());
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setProvider(EnumUtils.toSerializedName(src.getProvider()));
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.toSerializedName(src.getSync()));
			tgt.setIsDefault(src.getIsDefault());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setBusy(src.getDefaultBusy());
			tgt.setReminder(src.getDefaultReminder());
			tgt.setNotifyOnExtUpdate(src.getNotifyOnExtUpdate());
			tgt.setParameters(src.getParameters());
			tgt.setRemoteSyncFrequency(src.getRemoteSyncFrequency());
			tgt.setRemoteSyncTimestamp(src.getRemoteSyncTimestamp());
			tgt.setRemoteSyncTag(src.getRemoteSyncTag());
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
	
	static EventObject fillEventCalObject(EventObject tgt, Event src) {
		if ((tgt != null) && (src != null)) {
			tgt.setEventId(src.getEventId());
			tgt.setCalendarId(src.getCalendarId());
			tgt.setRevisionStatus(src.getRevisionStatus());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setHref(src.getHref());
		}
		return tgt;
	}
	
	static <T extends EventObject> T fillEventCalObject(T tgt, VEventObject src) {
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
	
	static Event createEvent(OEvent src) {
		return (src == null) ? null : fillEvent(new Event(), src);
	}
	
	static <T extends Event> T fillEvent(T tgt, OEvent src) {
		if ((tgt != null) && (src != null)) {
			tgt.setEventId(src.getEventId());
			tgt.setCalendarId(src.getCalendarId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), Event.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			//fill.setRevisionSequence(with.getRevisionSequence());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setReadOnly(src.getReadOnly());
			tgt.setStartDate(src.getStartDate());
			tgt.setEndDate(src.getEndDate());
			tgt.setTimezone(src.getTimezone());
			tgt.setAllDay(src.getAllDay());
			tgt.setOrganizer(src.getOrganizer());
			tgt.setTitle(src.getTitle());
			tgt.setDescription(src.getDescription());
			tgt.setLocation(src.getLocation());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setBusy(src.getBusy());
			tgt.setReminder(src.getReminder());
			tgt.setHref(src.getHref());
			tgt.setEtag(src.getEtag());
			tgt.setActivityId(src.getActivityId());
			tgt.setMasterDataId(src.getMasterDataId());
			tgt.setStatMasterDataId(src.getStatMasterDataId());
			tgt.setCausalId(src.getCausalId());
		}
		return tgt;
	}
	
	/*
	static void fillEventWithDefaults(Event fill) {
		if (fill != null) {
			if (StringUtils.isBlank(fill.getPublicUid())) {
				fill.setPublicUid(buildEventUid(fill.getEventId(), WT.getDomainInternetName(getTargetProfileId().getDomainId())));
			}
			if (fill.getReadOnly() == null) fill.setReadOnly(false);
			if (StringUtils.isBlank(fill.getOrganizer())) fill.setOrganizer(buildOrganizer());
		}
	}
	*/
	
	static OEvent createOEvent(Event src) {
		return (src == null) ? null : fillOEvent(new OEvent(), src);
	}
	
	static OEvent fillOEvent(OEvent tgt, Event src) {
		if ((tgt != null) && (src != null)) {
			tgt.setEventId(src.getEventId());
			tgt.setCalendarId(src.getCalendarId());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setReadOnly(src.getReadOnly());
			tgt.setStartDate(src.getStartDate());
			tgt.setEndDate(src.getEndDate());
			tgt.setTimezone(src.getTimezone());
			tgt.setAllDay(src.getAllDay());
			tgt.setOrganizer(src.getOrganizer());
			tgt.setTitle(src.getTitle());
			tgt.setDescription(src.getDescription());
			tgt.setLocation(src.getLocation());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setBusy(src.getBusy());
			tgt.setReminder(src.getReminder());
			tgt.setHref(src.getHref());
			tgt.setEtag(src.getEtag());
			tgt.setActivityId(src.getActivityId());
			tgt.setMasterDataId(src.getMasterDataId());
			tgt.setStatMasterDataId(src.getStatMasterDataId());
			tgt.setCausalId(src.getCausalId());
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
	
	static OEventAttendee createOEventAttendee(EventAttendee src) {
		return (src == null) ? null : fillOEventAttendee(new OEventAttendee(), src);
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
	
	static <T extends SchedEvent> T fillSchedEvent(T tgt, VVEvent src) {
		if ((tgt != null) && (src != null)) {
			tgt.setEventId(src.getEventId());
			tgt.setCalendarId(src.getCalendarId());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setStartDate(src.getStartDate());
			tgt.setEndDate(src.getEndDate());
			tgt.setTimezone(src.getTimezone());
			tgt.setAllDay(src.getAllDay());
			tgt.setOrganizer(src.getOrganizer());
			tgt.setTitle(src.getTitle());
			tgt.setDescription(src.getDescription());
			tgt.setLocation(src.getLocation());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setBusy(src.getBusy());
			tgt.setReminder(src.getReminder());
			tgt.setCalendarDomainId(src.getCalendarDomainId());
			tgt.setCalendarUserId(src.getCalendarUserId());
			tgt.setSeriesEventId(src.getSeriesEventId());
			tgt.setAttendeesCount(src.getAttendeesCount());
			tgt.setNotifyableAttendeesCount(src.getNotifyableAttendeesCount());
			tgt.setRecurInfo(src.isEventRecurring(), src.isEventBroken());
		}
		return tgt;
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
	
	static OEventAttachment createOTaskAttachment(EventAttachment src) {
		if (src == null) return null;
		return fillOContactAttachment(new OEventAttachment(), src);
	}
	
	static <T extends OEventAttachment> T fillOContactAttachment(T tgt, EventAttachment src) {
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
	
	/**
	 * Fills passed dates hashmap within ones coming from specified event.
	 * If event starts on 21 Apr and ends on 25 Apr, 21->25 dates will be added to the set.
	 * @param dates
	 * @param event 
	 */
	static void pushDatesBetweenStartEnd(HashSet<DateTime> dates, DateTime startDate, DateTime endDate) {
		int days = CalendarUtils.calculateLengthInDays(startDate, endDate) +1;
		DateTime date = startDate.withTimeAtStartOfDay();
		for(int count = 1; count <= days; count++) {
			dates.add(date);
			date = date.plusDays(1);
		}
	}
}
