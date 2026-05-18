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
package com.sonicle.webtop.calendar.rest.v2;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.beans.ItemsListResult;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.webtop.calendar.ICalendarManager.EventNotifyOption;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarBase;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventBase;
import com.sonicle.webtop.calendar.model.EventEx;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventInstanceId;
import com.sonicle.webtop.calendar.model.EventLookup;
import com.sonicle.webtop.calendar.model.EventLookupInstance;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.EventObjectWithBean;
import com.sonicle.webtop.calendar.model.EventRecurrence;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendar;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendarBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendarsResult;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEvent;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventChanged;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventEx;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventInstance;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventLkp;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventLkpInstance;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventOrganizer;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventRecurrence;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventsResult;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventsResultDelta;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiOwnerInfo;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiRecipient;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import com.sonicle.webtop.core.model.ChangedItem;
import com.sonicle.webtop.core.model.Delta;
import com.sonicle.webtop.core.sdk.BaseRestApiUtils;
import static com.sonicle.webtop.core.sdk.BaseRestApiUtils.shouldSet;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 *
 * @author malbinola
 */
public class ApiUtils {
	
	public static boolean isRecurringInstance(final EventInstance.Type instanceType) {
		return EnumUtils.isOneOf(instanceType, EventInstance.Type.MASTER, EventInstance.Type.OCCURRENCE, EventInstance.Type.EXCEPTION);
	}
	
	public static UpdateEventTarget toUpdateEventTarget(final EventInstanceId iid, final boolean modifySince) {
		if (!iid.hasNoInstance()) {
			return modifySince ? UpdateEventTarget.SINCE_INSTANCE : UpdateEventTarget.THIS_INSTANCE;
		}
		return UpdateEventTarget.WHOLE_SERIES;
	}
	
	public static BitFlags<EventNotifyOption> parseEventNotifyOption(final String notify) {
		if ("none".equals(notify)) {
			return BitFlags.noneOf(EventNotifyOption.class);
		} else if ("individual".equals(notify)) {
			return BitFlags.with(EventNotifyOption.NOTIFY_INDIVIDUAL_ATTENDEE);
		} else if ("resource".equals(notify)) {
			return BitFlags.with(EventNotifyOption.NOTIFY_RESOURCE_ATTENDEE);
		} else {
			return BitFlags.allOf(EventNotifyOption.class);
		}
	}
	
	public static String asCalendarId(final Integer calendarId) {
		return (calendarId != null) ? String.valueOf(calendarId) : null;
	}
	
	public static int parseCalendar(final String calendarId) throws WTParseException {
		try {
			return Integer.valueOf(calendarId);
		} catch (NumberFormatException ex) {
			throw new WTParseException(ex);
		}
	}
	
	public static List<String> printExcludedDates(final Set<LocalDate> excludedDates) {
		return excludedDates.stream()
			.map(date -> JodaTimeUtils.print(JodaTimeUtils.ISO_LOCALDATE_FMT, date))
			.collect(Collectors.toList());
	}
	
	public static Set<LocalDate> parseExcludedDates(final List<String> excludedDates) {
		return excludedDates.stream()
			.map(date -> JodaTimeUtils.parseLocalDate(JodaTimeUtils.ISO_LOCALDATE_FMT, date))
			.collect(Collectors.toSet());
	}
	
	public static InternetAddress parseApiRecipient(final ApiRecipient recipient) {
		if (recipient == null) return null;
		return InternetAddressUtils.toInternetAddress(recipient.getAddress(), recipient.getName());
	}
	
	public static CalendarBase fillCalendarBase(final CalendarBase tgt, final Set<String> fields2set, final ApiCalendarBase src) {
		if (shouldSet(fields2set, "provider")) {
			tgt.setProvider(EnumUtils.forName(src.getProvider(), CalendarBase.Provider.class));
			if (CalendarBase.Provider.WEBCAL.equals(tgt.getProvider()) || CalendarBase.Provider.CALDAV.equals(tgt.getProvider())) {
				tgt.setParametersAsObject(CalendarRemoteParameters.fromJson(src.getProviderParams()), CalendarRemoteParameters.class);
			}
		}
		if (shouldSet(fields2set, "name")) tgt.setName(src.getName());
		if (shouldSet(fields2set, "description")) tgt.setDescription(src.getDescription());
		if (shouldSet(fields2set, "color")) tgt.setColor(src.getColor());
		if (shouldSet(fields2set, "easSync")) tgt.setSync(EnumUtils.forName(src.getEasSync(), CalendarBase.Sync.class));
		if (shouldSet(fields2set, "defVisibility")) tgt.setDefaultVisibility(EnumUtils.forSerializedName(src.getDefVisibility(), EventBase.Visibility.class));
		if (shouldSet(fields2set, "defTransparency")) tgt.setDefaultTransparency(EnumUtils.forSerializedName(src.getDefTransparency(), EventBase.Transparency.class));
		if (shouldSet(fields2set, "defReminder")) tgt.setDefaultReminder(EventBase.Reminder.getMinutesValue(EventBase.Reminder.valueOf(src.getDefReminder())));
		if (shouldSet(fields2set, "remoteSyncFrequency")) tgt.setRemoteSyncFrequency(BaseRestApiUtils.toShort(src.getRemoteSyncFrequency()));
		return tgt;
	}
	
	public static ApiCalendarsResult fillApiCalendarsResult(final ApiCalendarsResult tgt, final Set<String> fields2set, final ItemsListResult<Calendar> result, final Map<Integer, DateTime> itemsLastRevisionMap) {
		tgt.setTotalCount(result.getFullCount());
		ArrayList<ApiCalendar> items = new ArrayList<>(result.getItems().size());
		for (Calendar item : result.getItems()) {
			DateTime itemsLastRevision = (itemsLastRevisionMap != null) ? itemsLastRevisionMap.get(item.getCalendarId()) : null;
			items.add(fillApiCalendar(new ApiCalendar(), fields2set, item, itemsLastRevision));
		}
		tgt.items(items);
		return tgt;
	}
	
	public static ApiCalendar fillApiCalendar(final ApiCalendar tgt, final Set<String> fields2set, final Calendar src, final DateTime itemsRevisionTimestamp) {
		fillApiCalendarBase(tgt, fields2set, src);
		tgt.id(String.valueOf(src.getCalendarId()));
		tgt.etag(BaseRestApiUtils.buildETag(src.getRevisionTimestamp()));
		tgt.itemsETag(BaseRestApiUtils.buildETag(itemsRevisionTimestamp));
		tgt.createdAt(JodaTimeUtils.printISO(src.getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.printISO(src.getRevisionTimestamp()));
		tgt.owner(fillApiOwnerInfo(new ApiOwnerInfo(), src.getProfileId()));
		return tgt;
	}
	
	public static ApiCalendarBase fillApiCalendarBase(final ApiCalendarBase tgt, final Set<String> fields2set, final CalendarBase src) {
		if (shouldSet(fields2set, "provider")) {
			tgt.setProvider(ApiCalendarBase.ProviderEnum.fromValue(EnumUtils.toSerializedName(src.getProvider())));
			tgt.setProviderParams(src.getParameters());
		}
		if (shouldSet(fields2set, "builtIn")) tgt.setBuiltIn(src.getBuiltIn());
		if (shouldSet(fields2set, "name")) tgt.setName(src.getName());
		if (shouldSet(fields2set, "description")) tgt.setDescription(src.getDescription());
		if (shouldSet(fields2set, "color")) tgt.setColor(src.getColor());
		if (shouldSet(fields2set, "easSync")) tgt.setEasSync(EnumUtils.forName(src.getSync(), ApiCalendarBase.EasSyncEnum.class));
		if (shouldSet(fields2set, "defVisibility")) tgt.setDefVisibility(ApiCalendarBase.DefVisibilityEnum.fromValue(EnumUtils.toSerializedName(src.getDefaultVisibility())));
		if (shouldSet(fields2set, "defTransparency")) tgt.setDefTransparency(ApiCalendarBase.DefTransparencyEnum.fromValue(EnumUtils.toSerializedName(src.getDefaultTransparency())));
		if (shouldSet(fields2set, "defReminder")) tgt.setDefReminder(src.getDefaultReminder());
		if (shouldSet(fields2set, "remoteSyncFrequency")) tgt.setRemoteSyncFrequency(BaseRestApiUtils.toInteger(src.getRemoteSyncFrequency()));
		if (shouldSet(fields2set, "remoteSyncTimestamp")) tgt.setRemoteSyncTimestamp(JodaTimeUtils.print(JodaTimeUtils.ISO_DATETIME_FMT, src.getRemoteSyncTimestamp()));
		if (shouldSet(fields2set, "remoteSyncToken")) tgt.setRemoteSyncToken(src.getRemoteSyncTag());
		return tgt;
	}
	
	public static EventEx fillEventEx(final EventEx tgt, final Set<String> fields2set, final ApiEventEx src) {
		fillEventBase(tgt, fields2set, src);
		if (src.getRecurrence() != null) {
			ApiEventRecurrence aer = src.getRecurrence();
			tgt.setRecurrence(new EventRecurrence(aer.getRrule(), JodaTimeUtils.parseDateTimeISO(aer.getStart()), parseExcludedDates(aer.getExDates())));
		}
		return tgt;
	}
	
	public static EventBase fillEventBase(final EventBase tgt, final Set<String> fields2set, final ApiEventBase src) {
		if (shouldSet(fields2set, "publicUid")) tgt.setPublicUid(src.getPublicUid());
		if (shouldSet(fields2set, "rowStatus")) tgt.setRowStatus(EnumUtils.forName(src.getVisibility(), EventBase.RowStatus.class));
		if (shouldSet(fields2set, "status")) tgt.setStatus(EnumUtils.forName(src.getStatus(), EventBase.Status.class));
		if (shouldSet(fields2set, "organizer")) {
			tgt.setOrganizer(null);
			tgt.setOrganizerId(null);
			if (src.getOrganizer() != null) {
				InternetAddress ia = parseApiRecipient(src.getOrganizer().getEmailAddress());
				if (ia != null && InternetAddressUtils.isAddressValid(ia)) {
					tgt.setOrganizer(ia.toString());
					tgt.setOrganizer(src.getOrganizer().getUserId());
				}
				
			}
		}
		if (shouldSet(fields2set, "timezone")) tgt.setTimezone(src.getTimezone());
		if (shouldSet(fields2set, "allDay")) tgt.setAllDay(src.getAllDay());
		if (shouldSet(fields2set, "start")) tgt.setStart(JodaTimeUtils.parseDateTimeISO(src.getStart()));
		if (shouldSet(fields2set, "end")) tgt.setEnd(JodaTimeUtils.parseDateTimeISO(src.getEnd()));
		if (shouldSet(fields2set, "title")) tgt.setTitle(src.getTitle());
		if (shouldSet(fields2set, "location")) tgt.setLocation(src.getLocation());
		if (shouldSet(fields2set, "descriptionType")) tgt.setDescriptionType(EnumUtils.forSerializedName(src.getDescriptionType(), EventBase.BodyType.class));
		if (shouldSet(fields2set, "description")) tgt.setDescription(src.getDescription());
		if (shouldSet(fields2set, "visibility")) tgt.setVisibility(EnumUtils.forSerializedName(src.getVisibility(), EventBase.Visibility.class));
		if (shouldSet(fields2set, "transparency")) tgt.setTransparency(EnumUtils.forSerializedName(src.getTransparency(), EventBase.Transparency.class));
		if (shouldSet(fields2set, "href")) tgt.setHref(src.getHref());
		if (shouldSet(fields2set, "reminder")) tgt.setReminder(EventBase.Reminder.valueOf(src.getReminder()));
		return tgt;
	}
	
	public static ApiEventsResult fillApiEventsResult(final ApiEventsResult tgt, final Set<String> fields2set, final ItemsListResult<EventObject> result) {
		tgt.setTotalCount(result.getFullCount());
		ArrayList<ApiEvent> items = new ArrayList<>(result.getItems().size());
		for (EventObject item : result.getItems()) {
			items.add(fillApiEvent(new ApiEvent(), fields2set, (EventObjectWithBean)item));
		}
		tgt.items(items);
		return tgt;
	}
	
	public static ApiEventsResultDelta fillApiEventsResultDelta(final ApiEventsResultDelta tgt, final Set<String> fields2set, final Delta<EventObject> changes) {
		tgt.setNextSyncToken(changes.getNextSyncToken());
		ArrayList<ApiEventChanged> items = new ArrayList<>(changes.getItems().size());
		for (ChangedItem<EventObject> item : changes.getItems()) {
			items.add(fillApiContactChanged(item.getChangeType(), new ApiEventChanged(), fields2set, (EventObjectWithBean)item.getObject()));
		}
		tgt.items(items);
		return tgt;
	}
	
	public static ApiEventChanged fillApiContactChanged(final ChangedItem.ChangeType changeType, final ApiEventChanged tgt, final Set<String> fields2set, final EventObjectWithBean src) {
		if (changeType.equals(ChangedItem.ChangeType.ADDED)) {
			tgt.$added(true);
		} else if (changeType.equals(ChangedItem.ChangeType.DELETED)) {
			tgt.$deleted(true);
		} else {
			tgt.$updated(true);
		}
		fillApiEventBase(tgt, fields2set, src.getEvent());
		tgt.id(String.valueOf(src.getEventId()));
		tgt.etag(BaseRestApiUtils.buildETag(src.getEvent().getRevisionTimestamp()));
		tgt.createdAt(JodaTimeUtils.printISO(src.getEvent().getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.printISO(src.getEvent().getRevisionTimestamp()));
		return tgt;
	}
	
	public static Set<String> fillLocalDateList(final Set<String> tgt, final Set<LocalDate> items) {
		for (LocalDate item : items) {
			tgt.add(JodaTimeUtils.print(JodaTimeUtils.ISO_LOCALDATE_FMT, item));
		}
		return tgt;
	}
	
	public static List<ApiEventLkpInstance> fillEventLkpInstanceList(final List<ApiEventLkpInstance> tgt, final Set<String> fields2set, final List<EventLookupInstance> items) {
		for (EventLookupInstance item : items) {
			tgt.add(fillApiEventLkpInstance(new ApiEventLkpInstance(), fields2set, item));
		}
		return tgt;
	}
	
	public static ApiEvent fillApiEvent(final ApiEvent tgt, final Set<String> fields2set, final EventObjectWithBean src) {
		fillApiEventBase(tgt, fields2set, src.getEvent());
		tgt.id(String.valueOf(src.getEventId()));
		tgt.etag(BaseRestApiUtils.buildETag(src.getRevisionTimestamp()));
		tgt.createdAt(JodaTimeUtils.printISO(src.getEvent().getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.printISO(src.getRevisionTimestamp()));
		return tgt;
	}
	
	public static ApiEvent fillApiEvent(final ApiEvent tgt, final Set<String> fields2set, final Event src) {
		fillApiEventEx(tgt, fields2set, src);
		tgt.id(src.getEventId());
		tgt.etag(BaseRestApiUtils.buildETag(src.getRevisionTimestamp()));
		tgt.createdAt(JodaTimeUtils.printISO(src.getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.printISO(src.getRevisionTimestamp()));
		return tgt;
	}
	
	public static ApiEventLkpInstance fillApiEventLkpInstance(final ApiEventLkpInstance tgt, final Set<String> fields2set, final EventLookupInstance src) {
		EventInstance.Type type = src.getType();
		fillApiEventLkp(tgt, fields2set, src);
		tgt.iid(src.getId().toString());
		tgt.instanceType(EnumUtils.toSerializedName(type));
		tgt.originalEventId(src.getOriginalEventId());
		tgt.seriesEventId(isRecurringInstance(type) ? src.getOriginalEventId() : null);
		tgt.etag(BaseRestApiUtils.buildETag(src.getRevisionTimestamp()));
		tgt.createdAt(JodaTimeUtils.printISO(src.getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.printISO(src.getRevisionTimestamp()));
		return tgt;
	}
	
	public static ApiEventLkp fillApiEventLkp(final ApiEventLkp tgt, final Set<String> fields2set, final EventLookup src) {
		fillApiEventBase(tgt, fields2set, src);
		if (shouldSet(fields2set, "tags")) tgt.tags(src.getTags());
		tgt.hasRecurrence(src.getHasRecurrence());
		tgt.attendeesCount(src.getAttendeesCount());
		tgt.notifyableAttendeesCount(src.getNotifyableAttendeesCount());
		if (shouldSet(fields2set, "calendarId")) tgt.calendarId(asCalendarId(src.getCalendarId()));
		if (shouldSet(fields2set, "calendarName")) tgt.calendarName(src.getCalendarName());
		if (shouldSet(fields2set, "calendarDomainId")) tgt.calendarDomainId(src.getCalendarDomainId());
		if (shouldSet(fields2set, "calendarUserId")) tgt.calendarUserId(src.getCalendarUserId());
		return tgt;
	}
	
	public static ApiEventInstance fillApiEventInstance(final ApiEventInstance tgt, final Set<String> fields2set, final EventInstance src) {
		EventInstance.Type type = src.getType();
		fillApiEventEx(tgt, fields2set, src);
		tgt.iid(src.getId().toString());
		tgt.instanceType(EnumUtils.toSerializedName(type));
		tgt.originalEventId(src.getOriginalEventId());
		tgt.seriesEventId(isRecurringInstance(type) ? src.getOriginalEventId() : null);
		tgt.etag(BaseRestApiUtils.buildETag(src.getRevisionTimestamp()));
		tgt.createdAt(JodaTimeUtils.printISO(src.getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.printISO(src.getRevisionTimestamp()));
		return tgt;
	}
	
	public static ApiEvent fillApiEvent(final ApiEvent tgt, final Set<String> fields2set, final EventInstance src) {
		fillApiEventEx(tgt, fields2set, src);
		tgt.id(String.valueOf(src.getCalendarId()));
		tgt.etag(BaseRestApiUtils.buildETag(src.getRevisionTimestamp()));
		tgt.createdAt(JodaTimeUtils.printISO(src.getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.printISO(src.getRevisionTimestamp()));
		return tgt;
	}
	
	public static ApiEventEx fillApiEventEx(final ApiEventEx tgt, final Set<String> fields2set, final EventEx src) {
		fillApiEventBase(tgt, fields2set, src);
		if (src.hasRecurrence()) tgt.recurrence(fillApiEventRecurrence(new ApiEventRecurrence(), src.getRecurrence()));
		return tgt;
	}
	
	public static ApiEventBase fillApiEventBase(final ApiEventBase tgt, final Set<String> fields2set, final EventBase src) {
		if (shouldSet(fields2set, "publicUid")) tgt.publicUid(src.getPublicUid());
		tgt.rowStatus(EnumUtils.forName(src.getRowStatus(), ApiEventBase.RowStatusEnum.class));
		tgt.status(EnumUtils.forName(src.getStatus(), ApiEventBase.StatusEnum.class));
		tgt.organizer(fillApiEventOrganizer(new ApiEventOrganizer(), src.getOrganizerInternetAddress(), src.getOrganizerId()));
		tgt.timezone(src.getTimezone());
		tgt.allDay(src.getAllDay());
		tgt.start(JodaTimeUtils.printISO(src.getStart()));
		tgt.end(JodaTimeUtils.printISO(src.getEnd()));
		tgt.title(src.getTitle());
		if (shouldSet(fields2set, "location")) tgt.location(src.getLocation());
		if (shouldSet(fields2set, "description")) {
			tgt.descriptionType(EnumUtils.forName(src.getDescriptionType(), ApiEventBase.DescriptionTypeEnum.class));
			tgt.description(src.getDescription());
		}
		if (shouldSet(fields2set, "visibility")) tgt.visibility(ApiEventBase.VisibilityEnum.fromValue(EnumUtils.toSerializedName(src.getVisibility())));
		if (shouldSet(fields2set, "transparency")) tgt.transparency(ApiEventBase.TransparencyEnum.fromValue(EnumUtils.toSerializedName(src.getTransparency())));
		if (shouldSet(fields2set, "href")) tgt.href(src.getHref());
		if (shouldSet(fields2set, "reminder")) tgt.reminder(EventBase.Reminder.getMinutesValue(src.getReminder()));
		return tgt;
	}
	
	public static ApiEventRecurrence fillApiEventRecurrence(final ApiEventRecurrence tgt, final EventRecurrence eventRecurrence) {
		tgt.rrule(eventRecurrence.getRule());
		tgt.exDates(printExcludedDates(eventRecurrence.getExcludedDatesOrEmpty()));
		return tgt;
	}
	
	public static ApiEventOrganizer fillApiEventOrganizer(final ApiEventOrganizer tgt, final InternetAddress organizer, final String organizerId) {
		tgt.emailAddress(fillApiRecipient(new ApiRecipient(), organizer));
		tgt.userId(organizerId);
		return tgt;
	}
	
	public static ApiRecipient fillApiRecipient(final ApiRecipient tgt, final InternetAddress internetAddress) {
		if (internetAddress != null) tgt.address(internetAddress.getAddress());
		if (internetAddress != null) tgt.name(internetAddress.getPersonal());
		return tgt;
	}
	
	public static ApiOwnerInfo fillApiOwnerInfo(final ApiOwnerInfo tgt, final UserProfileId profileId) {
		tgt.userId(profileId.getUserId());
		UserProfile.Data ud = WT.getProfileData(profileId);
		tgt.emailAddress(ud.getPersonalEmailAddress());
		tgt.displayName(ud.getDisplayName());
		return tgt;
	}
}
