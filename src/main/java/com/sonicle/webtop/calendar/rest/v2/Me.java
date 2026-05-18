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
import com.sonicle.commons.beans.ItemsListResult;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.time.DateTimeWindow;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.EventObjectOutputType;
import com.sonicle.webtop.calendar.ICalendarManager.EventGetOption;
import com.sonicle.webtop.calendar.ICalendarManager.EventNotifyOption;
import com.sonicle.webtop.calendar.ICalendarManager.EventUpdateOption;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarBase;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventEx;
import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventInstanceId;
import com.sonicle.webtop.calendar.model.EventLookupInstance;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.UpdateEventTarget;
import com.sonicle.webtop.calendar.swagger.v2.api.MeApi;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendar;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendarBase;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiCalendarsResult;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiError;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEvent;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventEx;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventInstance;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventQuick;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventResponse;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventsResult;
import com.sonicle.webtop.calendar.swagger.v2.model.ApiEventsResultDelta;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.model.Delta;
import com.sonicle.webtop.core.sdk.BaseRestApiUtils;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Me extends MeApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(Me.class);
	
	private CalendarManager getManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private CalendarManager getManager(UserProfileId targetProfileId) {
		CalendarManager manager = (CalendarManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
		manager.setSoftwareName("rest");
		return manager;
	}

	@Override
	public Response listCalendars(String _filter, String _select, String _orderBy, Integer _pageNo, Integer _pageSize, Boolean _returnCount) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listCalendars()", RunContext.getRunProfileId());
		}
		
		try {
			boolean returnFullCount = _returnCount == null ? false : _returnCount;
			ItemsListResult<Calendar> result = manager.listCalendars(_filter, BaseRestApiUtils.parseSortInfo(_orderBy), _pageNo, BaseRestApiUtils.pageSizeOrDefault(_pageNo, _pageSize), returnFullCount);
			Map<Integer, DateTime> itemsLastRevisionMap = manager.getCalendarsItemsLastRevision(
				result.items.stream()
					.map((calendar) -> {
						return calendar.getCalendarId();
					})
					.collect(Collectors.toList())
			);
			Integer defaultCalendarId = manager.getDefaultCalendarId();
			return respOk(ApiUtils.fillApiCalendarsResult(new ApiCalendarsResult(), BaseRestApiUtils.parseStringSet(_select), result, defaultCalendarId, itemsLastRevisionMap));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listCalendars()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response getCalendar(String calendarId) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getCalendar({})", RunContext.getRunProfileId(), calendarId);
		}
		
		try {
			Calendar calendar = manager.getCalendar(ApiUtils.parseCalendar(calendarId));
			if (calendar == null) return respErrorNotFound();
			Map<Integer, DateTime> itemsLastRevisionMap = manager.getCalendarsItemsLastRevision(Arrays.asList(calendar.getCalendarId()));
			Integer defaultCalendarId = manager.getDefaultCalendarId();
			return respOk(ApiUtils.fillApiCalendar(new ApiCalendar(), null, calendar, defaultCalendarId, itemsLastRevisionMap.get(calendar.getCalendarId())));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getCalendar({})", RunContext.getRunProfileId(), calendarId, t);
			return respError(t);
		}
	}

	@Override
	public Response addCalendar(String userId, ApiCalendarBase body) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addCalendar({})", RunContext.getRunProfileId(), userId);
		}
		
		try {
			CalendarBase calendar = ApiUtils.fillCalendarBase(new CalendarBase(), null, body);
			calendar.setUserId(userIdOrDefault(userId));
			Calendar newCalendar = manager.addCalendar(calendar);
			Integer defaultCalendarId = manager.getDefaultCalendarId();
			return respOkCreated(ApiUtils.fillApiCalendar(new ApiCalendar(), null, newCalendar, defaultCalendarId, null));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] addCalendar({})", RunContext.getRunProfileId(), userId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateCalendar(String calendarId, ApiCalendarBase body) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateCalendar({})", RunContext.getRunProfileId(), calendarId);
		}
		
		try {
			Calendar calendar = manager.getCalendar(ApiUtils.parseCalendar(calendarId));
			if (calendar == null) return respErrorNotFound();
			
			ApiUtils.fillCalendarBase(calendar, null, body);
			manager.updateCalendar(calendar.getCalendarId(), calendar);
			return respOk();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] updateCalendar({})", RunContext.getRunProfileId(), calendarId, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteCalendar(String calendarId) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteCalendar({})", RunContext.getRunProfileId(), calendarId);
		}
		
		try {
			manager.deleteCalendar(ApiUtils.parseCalendar(calendarId));
			return respOkNoContent();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] deleteCalendar({})", RunContext.getRunProfileId(), calendarId, t);
			return respError(t);
		}
	}

	@Override
	public Response listCalendarEvents(String calendarId, String _filter, String _select, String _orderBy, Integer _pageNo, Integer _pageSize, Boolean _returnCount) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listCalendarEvents({})", RunContext.getRunProfileId(), calendarId);
		}
		
		try {
			boolean returnFullCount = _returnCount == null ? false : _returnCount;
			ItemsListResult<EventObject> result = manager.listEventObjects2(Arrays.asList(ApiUtils.parseCalendar(calendarId)), null, _filter, _pageNo, BaseRestApiUtils.pageSizeOrDefault(_pageNo, _pageSize), returnFullCount, EventObjectOutputType.BEAN);
			return respOk(ApiUtils.fillApiEventsResult(new ApiEventsResult(), BaseRestApiUtils.parseStringSet(_select), result));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listCalendarEvents({})", RunContext.getRunProfileId(), calendarId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response addCalendarEvent(String calendarId, ApiEventEx body) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addCalendarEvent({})", RunContext.getRunProfileId(), calendarId);
		}
		
		try {
			EventEx event = ApiUtils.fillEventEx(new EventEx(), null, body);
			event.setCalendarId(ApiUtils.parseCalendar(calendarId));
			Event newEvent = manager.addEvent(event);
			return respOkCreated(ApiUtils.fillApiEvent(new ApiEvent(), null, newEvent));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] addCalendarEvent({})", RunContext.getRunProfileId(), calendarId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response listCalendarEventsDelta(String calendarId, String _syncToken, String _select) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listCalendarEventsDelta({}, {})", RunContext.getRunProfileId(), calendarId, _syncToken);
		}
		
		//x-field-extra-annotation
		//x-class-extra-annotation
		// do not serialize nulls
		//@JsonInclude(JsonInclude.Include.NON_NULL)
		
		try {
			Delta<EventObject> changes = manager.listEventObjectsDelta(ApiUtils.parseCalendar(calendarId), _syncToken, EventObjectOutputType.BEAN);
			return respOk(ApiUtils.fillApiEventsResultDelta(new ApiEventsResultDelta(), BaseRestApiUtils.parseStringSet(_select), changes));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listCalendarEventsDelta({}, {})", RunContext.getRunProfileId(), calendarId, _syncToken, t);
			return respError(t);
		}
	}
	
	@Override
	public Response listEventInstances(String calendarIds, String rangeStart, String rangeEnd, Boolean sort, String _filter, String _select) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listEventInstances({})", RunContext.getRunProfileId(), calendarIds);
		}
		
		try {
			DateTimeWindow dateTimeWindow = DateTimeWindow.builder().with(BaseRestApiUtils.parseDateTimeISO(rangeStart), BaseRestApiUtils.parseDateTimeISO(rangeEnd)).build();
			List<EventLookupInstance> instances = manager.listEventInstances(BaseRestApiUtils.parseIntegerSet(calendarIds), dateTimeWindow, _filter, sort, DateTimeZone.UTC);
			return respOk(ApiUtils.fillEventLkpInstanceList(new ArrayList<>(instances.size()), BaseRestApiUtils.parseStringSet(_select), instances));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listEventInstances({})", RunContext.getRunProfileId(), calendarIds, t);
			return respError(t);
		}
	}

	@Override
	public Response listEventInstancesDates(String calendarIds, String rangeStart, String rangeEnd, String _filter) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listEventInstancesDates({})", RunContext.getRunProfileId(), calendarIds);
		}
		
		try {
			DateTimeWindow dateTimeWindow = DateTimeWindow.builder().with(BaseRestApiUtils.parseDateTimeISO(rangeStart), BaseRestApiUtils.parseDateTimeISO(rangeEnd)).build();
			Set<LocalDate> dates = manager.listEventInstancesDates(BaseRestApiUtils.parseIntegerSet(calendarIds), dateTimeWindow, _filter, DateTimeZone.UTC);
			return respOk(ApiUtils.fillLocalDateList(new LinkedHashSet<>(dates.size()), dates));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listEventInstancesDates({})", RunContext.getRunProfileId(), calendarIds, t);
			return respError(t);
		}
	}

	@Override
	public Response addEvent(String calendarId, ApiEventEx body) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addEvent({})", RunContext.getRunProfileId(), calendarId);
		}
		
		try {
			EventEx event = ApiUtils.fillEventEx(new EventEx(), null, body);
			event.setCalendarId(ApiUtils.parseCalendar(calendarId));
			Event newEvent = manager.addEvent(event);
			return respOkCreated(ApiUtils.fillApiEvent(new ApiEvent(), null, newEvent));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] addEvent({})", RunContext.getRunProfileId(), calendarId, t);
			return respError(t);
		}
	}

	@Override
	public Response getEventInstance(String eventInstanceId, Integer getOptions, String _select) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getEventInstance({})", RunContext.getRunProfileId(), eventInstanceId);
		}
		
		try {
			EventInstanceId iid = EventInstanceId.parse(eventInstanceId);
			BitFlags<EventGetOption> getOpts = BitFlags.newFrom(EventGetOption.class, getOptions);
			EventInstance event = manager.getEventInstance(iid, getOpts);
			
			return respOk(ApiUtils.fillApiEventInstance(new ApiEventInstance(), BaseRestApiUtils.parseStringSet(_select), event));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getEventInstance({})", RunContext.getRunProfileId(), eventInstanceId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response updateEventInstance(String eventInstanceId, Boolean modifySince, Integer updateOptions, String notify, String _update, ApiEventEx body) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateEventInstance({})", RunContext.getRunProfileId(), eventInstanceId);
		}
		
		try {
			EventInstanceId iid = EventInstanceId.parse(eventInstanceId);
			BitFlags<EventUpdateOption> updateOpts = BitFlags.newFrom(EventUpdateOption.class, updateOptions)
				.unset(EventUpdateOption.ATTENDEE_RESPONSE);
			BitFlags<EventNotifyOption> notifyOpts = ApiUtils.parseEventNotifyOption(notify);
			BitFlags<EventGetOption> getOpts = EventGetOption.parseEventUpdateOptions(updateOpts);
			EventInstance event = manager.getEventInstance(iid, getOpts);
			if (event == null) return respErrorNotFound();
			
			ApiUtils.fillEventEx(event, BaseRestApiUtils.parseStringSet(_update), body);
			
			UpdateEventTarget target = ApiUtils.toUpdateEventTarget(iid, modifySince);
			manager.updateEventInstance(target, iid, event, updateOpts, notifyOpts);
			return respOk();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] updateEventInstance({})", RunContext.getRunProfileId(), eventInstanceId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateEventInstanceQuick(String eventInstanceId, Boolean modifySince, String notify, ApiEventQuick body) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateEventInstanceQuick({})", RunContext.getRunProfileId(), eventInstanceId);
		}
		
		try {
			EventInstanceId iid = EventInstanceId.parse(eventInstanceId);
			BitFlags<EventNotifyOption> notifyOpts = ApiUtils.parseEventNotifyOption(notify);
			
			DateTime newStart = JodaTimeUtils.parseDateTimeISO(body.getStart());
			DateTime newEnd = JodaTimeUtils.parseDateTimeISO(body.getEnd());
			String newTitle = body.getTitle();
			
			UpdateEventTarget target = ApiUtils.toUpdateEventTarget(iid, modifySince);
			manager.updateEventInstanceQuick(target, iid, newStart, newEnd, newTitle, notifyOpts);
			return respOk();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] updateEventInstanceQuick({})", RunContext.getRunProfileId(), eventInstanceId, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteEventInstance(String eventInstanceId, Boolean modifySince, String notify) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteEventInstance({})", RunContext.getRunProfileId(), eventInstanceId);
		}
		
		try {
			EventInstanceId iid = EventInstanceId.parse(eventInstanceId);
			BitFlags<EventNotifyOption> notifyOpts = ApiUtils.parseEventNotifyOption(notify);
			
			UpdateEventTarget target = ApiUtils.toUpdateEventTarget(iid, modifySince);
			manager.deleteEventInstance(target, iid, notifyOpts);
			return respOk();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] deleteEventInstance({})", RunContext.getRunProfileId(), eventInstanceId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response updateEventInstanceResponse(String eventInstanceId, ApiEventResponse apiEventResponse) {
		CalendarManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateEventInstanceResponse({})", RunContext.getRunProfileId(), eventInstanceId);
		}
		
		try {
			EventInstanceId iid = EventInstanceId.parse(eventInstanceId);
			boolean notifyOrganizer = true;
			if (apiEventResponse.getNotify() != null) notifyOrganizer = apiEventResponse.getNotify();
			
			EventAttendee.ResponseStatus status = EnumUtils.forSerializedName(apiEventResponse.getStatus(), EventAttendee.ResponseStatus.class);
			manager.updateEventInstanceAttendeeResponse(iid, status, null, notifyOrganizer);
			return respOk();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] updateEventInstanceResponse({})", RunContext.getRunProfileId(), eventInstanceId, t);
			return respError(t);
		}
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
			.code(status.getStatusCode())
			.description(message);
	}
}
