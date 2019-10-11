/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.rest.v1;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.CalendarUtils;
import com.sonicle.webtop.calendar.EventObjectOutputType;
import com.sonicle.webtop.calendar.NotFoundException;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.EventObjectWithBean;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import com.sonicle.webtop.calendar.model.ShareRootCalendar;
import com.sonicle.webtop.calendar.swagger.v1.api.EasApi;
import com.sonicle.webtop.calendar.swagger.v1.model.ApiError;
import com.sonicle.webtop.calendar.swagger.v1.model.SyncEvent;
import com.sonicle.webtop.calendar.swagger.v1.model.SyncEventData;
import com.sonicle.webtop.calendar.swagger.v1.model.SyncEventDataAttendee;
import com.sonicle.webtop.calendar.swagger.v1.model.SyncEventStat;
import com.sonicle.webtop.calendar.swagger.v1.model.SyncEventUpdate;
import com.sonicle.webtop.calendar.swagger.v1.model.SyncFolder;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.model.SharePerms;
import com.sonicle.webtop.core.model.SharePermsElements;
import com.sonicle.webtop.core.model.SharePermsFolder;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Eas extends EasApi {
	private static final Logger logger = LoggerFactory.getLogger(Eas.class);
	private static final String DEFAULT_ETAG = "19700101000000000";
	private static final DateTimeFormatter ETAG_FMT = DateTimeUtils.createFormatter("yyyyMMddHHmmssSSS", DateTimeZone.UTC);
	private static final DateTimeFormatter ISO_DATE_FMT = DateTimeUtils.createFormatter("yyyyMMdd", DateTimeZone.UTC);
	private static final DateTimeFormatter ISO_DATETIME_FMT = DateTimeUtils.createFormatter("yyyyMMdd'T'HHmmss'Z'", DateTimeZone.UTC);
	
	@Override
	public Response getFolders() {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		CalendarManager manager = getManager();
		List<SyncFolder> items = new ArrayList<>();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getFolders()", currentProfileId);
		}
		
		try {
			Map<Integer, Calendar> cals = manager.listCalendars();
			Map<Integer, DateTime> revisions = manager.getCalendarsLastRevision(cals.keySet());
			for (Calendar cal : cals.values()) {
				if (cal.isProviderRemote()) continue;
				if (Calendar.Sync.OFF.equals(cal.getSync())) continue;
				
				items.add(createSyncFolder(currentProfileId, cal, revisions.get(cal.getCalendarId()), null, ShareFolderCalendar.realElementsPerms(cal.getSync())));
			}
			
			List<ShareRootCalendar> shareRoots = manager.listIncomingCalendarRoots();
			for (ShareRootCalendar shareRoot : shareRoots) {
				Map<Integer, ShareFolderCalendar> folders = manager.listIncomingCalendarFolders(shareRoot.getShareId());
				revisions = manager.getCalendarsLastRevision(folders.keySet());
				Map<Integer, CalendarPropSet> props = manager.getCalendarCustomProps(folders.keySet());
				
				for (ShareFolderCalendar folder : folders.values()) {
					Calendar cal = folder.getCalendar();
					if (cal.isProviderRemote()) continue;
					CalendarPropSet calProps = props.get(cal.getCalendarId());
					if (Calendar.Sync.OFF.equals(calProps.getSyncOrDefault(Calendar.Sync.OFF))) continue;
					
					items.add(createSyncFolder(currentProfileId, cal, revisions.get(cal.getCalendarId()), folder.getPerms(), folder.getRealElementsPerms(calProps.getSync())));
				}
			}
			
			return respOk(items);
			
		} catch(Throwable t) {
			logger.error("[{}] getFolders()", currentProfileId, t);
			return respError(t);
		}
	}

	@Override
	public Response getMessagesStats(Integer folderId, String cutoffDate) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getMessagesStats({}, {})", RunContext.getRunProfileId(), folderId, cutoffDate);
		}
		
		try {
			Calendar cal = manager.getCalendar(folderId);
			if (cal == null) return respErrorBadRequest();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			DateTime since = DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, cutoffDate);
			if (since == null) DateTimeUtils.now().minusDays(30).withTimeAtStartOfDay();
			
			List<SyncEventStat> items = new ArrayList<>();
			List<EventObject> evtobjs = manager.listEventObjects(folderId, since, EventObjectOutputType.STAT);
			for (EventObject evtobj : evtobjs) {
				items.add(createSyncEventStat(evtobj));
			}
			return respOk(items);
			
		} catch(Throwable t) {
			logger.error("[{}] getMessagesStats({})", RunContext.getRunProfileId(), folderId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response getMessage(Integer folderId, Integer id) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getMessage({}, {})", RunContext.getRunProfileId(), folderId, id);
		}
		
		try {
			Calendar cal = manager.getCalendar(folderId);
			if (cal == null) return respErrorBadRequest();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			EventObjectWithBean evtobj = (EventObjectWithBean)manager.getEventObject(folderId, id, EventObjectOutputType.BEAN);
			if (evtobj != null) {
				return respOk(createSyncEvent(evtobj));
			} else {
				return respErrorNotFound();
			}
			
		} catch(Throwable t) {
			logger.error("[{}] getMessage({}, {})", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}

	@Override
	public Response addMessage(Integer folderId, SyncEventUpdate body) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] addMessage({}, ...)", RunContext.getRunProfileId(), folderId);
			logger.debug("{}", body);
		}
		
		try {
			Event newEvent = mergeEvent(new Event(), body.getData());
			newEvent.setCalendarId(folderId);
			
			Event event = manager.addEvent(newEvent);
			EventObject evtobj = manager.getEventObject(folderId, event.getEventId(), EventObjectOutputType.STAT);
			if (evtobj == null) return respErrorNotFound();
			
			return respOkCreated(createSyncEventStat(evtobj));
			
		} catch(Throwable t) {
			logger.error("[{}] addMessage({}, ...)", RunContext.getRunProfileId(), folderId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateMessage(Integer folderId, Integer id, SyncEventUpdate body) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] updateMessage({}, {}, ...)", RunContext.getRunProfileId(), folderId, id);
			logger.debug("{}", body);
		}
		
		try {
			Event event = manager.getEvent(id);
			if (event == null) return respErrorNotFound();
			
			mergeEvent(event, body.getData());
			manager.updateEvent(event, true);
			
			ArrayList<EventObject> evtobjs = new ArrayList<>();
			EventObject evtobj = manager.getEventObject(folderId, id, EventObjectOutputType.STAT);
			if (evtobj == null) return respErrorNotFound();
			evtobjs.add(evtobj);
			
			// Here we do not support creating broken events related to the
			// main event series (re-attach unavailable). Instance exceptions 
			// should have been created above as date exception into the main
			// event; so simply create exceptions as new events.
			List<SyncEventData> eventExceptions = body.getExceptions();
			if ((eventExceptions != null) && !eventExceptions.isEmpty()) {
				for (SyncEventData eventException : eventExceptions) {
					Event newEvent = mergeEvent(new Event(), eventException);
					newEvent.setCalendarId(folderId);
					newEvent = manager.addEvent(newEvent);
					
					evtobj = manager.getEventObject(folderId, newEvent.getEventId(), EventObjectOutputType.STAT);
					if (evtobj == null) return respErrorNotFound();
					evtobjs.add(evtobj);
				}
			}
			
			return respOk(createSyncEventStats(evtobjs));
			
		} catch(Throwable t) {
			logger.error("[{}] updateMessage({}, {}, ...)", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteMessage(Integer folderId, Integer id) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] deleteMessage({}, {})", RunContext.getRunProfileId(), folderId, id);
		}
		
		try {
			manager.deleteEvent(id, true);
			return respOkNoContent();
			
		} catch(NotFoundException ex) {
			return respErrorNotFound();
		} catch(Throwable t) {
			logger.error("[{}] deleteMessage({}, {})", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}
	
	private SyncFolder createSyncFolder(UserProfileId currentProfileId, Calendar cal, DateTime lastRevisionTimestamp, SharePerms folderPerms, SharePerms elementPerms) {
		String displayName = cal.getName();
		if (!currentProfileId.equals(cal.getProfileId())) {
			UserProfile.Data owud = WT.getUserData(cal.getProfileId());
			//String apn = LangUtils.abbreviatePersonalName(false, owud.getDisplayName());
			displayName = "[" + owud.getDisplayName() + "] " + displayName;
		}
		//String ownerUsername = owud.getProfileEmailAddress();
		
		return new SyncFolder()
				.id(cal.getCalendarId())
				.displayName(displayName)
				.etag(buildEtag(lastRevisionTimestamp))
				.deflt(cal.getIsDefault())
				.foAcl((folderPerms == null) ? SharePermsFolder.full().toString() : folderPerms.toString())
				.elAcl((elementPerms == null) ? SharePermsElements.full().toString() : elementPerms.toString())
				.ownerId(cal.getProfileId().toString());
	}
	
	private List<SyncEventStat> createSyncEventStats(Collection<EventObject> evtobjs) {
		ArrayList<SyncEventStat> stats = new ArrayList<>(evtobjs.size());
		evtobjs.forEach(evtobj -> stats.add(createSyncEventStat(evtobj)));
		return stats;
	}
	
	private SyncEventStat createSyncEventStat(EventObject evtobj) {
		return new SyncEventStat()
				.id(evtobj.getEventId())
				.etag(buildEtag(evtobj.getRevisionTimestamp()));
	}
	
	private SyncEvent createSyncEvent(EventObjectWithBean evtobj) {
		Event event = evtobj.getEvent();
		
		ArrayList<String> exDates = null;
		if (event.hasExcludedDates()) {
			exDates = new ArrayList<>();
			for (LocalDate ld : event.getExcludedDates()) {
				exDates.add(DateTimeUtils.print(ISO_DATE_FMT, ld));
			}
		}
		
		ArrayList<SyncEventDataAttendee> saas = new ArrayList<>();
		for (EventAttendee attendee : event.getAttendees()) {
			saas.add(createSyncEventDataAttendee(attendee));
		}
		
		CalendarUtils.EventBoundary eventBoundary = CalendarUtils.getEventBoundary(event);
		return new SyncEvent()
				.id(evtobj.getEventId())
				.etag(buildEtag(evtobj.getRevisionTimestamp()))
				.start(DateTimeUtils.print(ISO_DATETIME_FMT, eventBoundary.start))
				.end(DateTimeUtils.print(ISO_DATETIME_FMT, eventBoundary.end))
				.tz(event.getTimezone())
				.allDay(event.getAllDay())
				.organizer(event.getOrganizer())
				.title(event.getTitle())
				.description(event.getDescription())
				.location(event.getLocation())
				.prvt(event.getIsPrivate())
				.busy(event.getBusy())
				.reminder(Event.Reminder.getMinutes(event.getReminder()))
				.recRule(event.hasRecurrence() ? event.getRecurrenceRule() : null)
				.recStart(event.hasRecurrence() ? DateTimeUtils.print(ISO_DATE_FMT, event.getRecurrenceStartDate()) : null)
				.exDates(exDates)
				.attendees(saas);
	}
	
	private SyncEventDataAttendee createSyncEventDataAttendee(EventAttendee attendee) {
		return new SyncEventDataAttendee()
				.address(attendee.getRecipient())
				.type(EnumUtils.toSerializedName(attendee.getRecipientType()))
				.role(EnumUtils.toSerializedName(attendee.getRecipientRole()))
				.status(EnumUtils.toSerializedName(attendee.getResponseStatus()));
	}
	
	private <T extends Event> T mergeEvent(T tgt, SyncEventData src) {
		boolean isNew = tgt.getEventId() == null;
		
		tgt.setStartDate(DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getStart()));
		tgt.setEndDate(DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getEnd()));
		tgt.setTimezone(src.getTz());
		tgt.setAllDay(src.isAllDay());
		tgt.setTitle(src.getTitle());
		tgt.setDescription(src.getDescription());
		tgt.setLocation(src.getLocation());
		tgt.setIsPrivate(src.isPrvt());
		tgt.setBusy(src.isBusy());
		tgt.setReminder(Event.Reminder.valueOf(src.getReminder()));
		
		if (!StringUtils.isBlank(src.getRecRule())) {
			LocalDate startDate = tgt.getStartDate().withZone(tgt.getDateTimeZone()).toLocalDate();
			LinkedHashSet<LocalDate> exDates = null;
			if ((src.getExDates() != null) && (!src.getExDates().isEmpty())) {
				exDates = new LinkedHashSet<>();
				for (String sdate : src.getExDates()) {
					LocalDate ld = DateTimeUtils.parseLocalDate(ISO_DATE_FMT, sdate);
					if (ld != null) exDates.add(ld);
				}
			}
			tgt.setRecurrence(src.getRecRule(), startDate, exDates);
		}
		
		if (isNew || tgt.getAttendees().isEmpty()) {
			for (SyncEventDataAttendee satt : src.getAttendees()) {
				tgt.getAttendees().add(mergeEventAttendee(new EventAttendee(), satt));
			}
		} else {
			// We do not have ids here, so we have to compare elements using recipient address!
			// Firstly, organize old attendees into an map using address as key
			LinkedHashMap<String, EventAttendee> oldAtts = new LinkedHashMap<>();
			tgt.getAttendees().forEach(att -> oldAtts.put(att.getRecipientAddress(), att));
			
			// Then, evaluate added and updated elements by adding them to the collection
			for (SyncEventDataAttendee satt : src.getAttendees()) {
				InternetAddress ia = InternetAddressUtils.toInternetAddress(satt.getAddress());
				if (ia == null || ia.getAddress() == null) continue;
				
				EventAttendee oldAtt = oldAtts.remove(ia.getAddress());
				if (oldAtt == null) {
					tgt.getAttendees().add(mergeEventAttendee(new EventAttendee(), satt));
				} else {
					int index = tgt.getAttendees().indexOf(oldAtt);
					tgt.getAttendees().set(index, mergeEventAttendee(oldAtt, satt));
				}
			}
			// Finally, remaining elements are elements to be removed!
			tgt.getAttendees().removeAll(oldAtts.values());
		}
		
		return tgt;
	}
	
	private EventAttendee mergeEventAttendee(EventAttendee tgt, SyncEventDataAttendee src) {
		boolean isNew = tgt.getAttendeeId()== null;
		
		if (isNew) {
			InternetAddress ia = InternetAddressUtils.toInternetAddress(src.getAddress());
			if (ia != null) tgt.setRecipient(src.getAddress());
			tgt.setRecipientType(EnumUtils.forSerializedName(src.getType(), EventAttendee.RecipientType.INDIVIDUAL, EventAttendee.RecipientType.class));
			tgt.setRecipientRole(EnumUtils.forSerializedName(src.getType(), EventAttendee.RecipientRole.REQUIRED, EventAttendee.RecipientRole.class));
			tgt.setResponseStatus(EnumUtils.forSerializedName(src.getType(), EventAttendee.ResponseStatus.NEEDS_ACTION, EventAttendee.ResponseStatus.class));
			tgt.setNotify(true);
			
		} else {
			EventAttendee.RecipientType rcptType = EnumUtils.forSerializedName(src.getType(), EventAttendee.RecipientType.class);
			if (rcptType != null) tgt.setRecipientType(rcptType);
			EventAttendee.RecipientRole rcptRole = EnumUtils.forSerializedName(src.getType(), EventAttendee.RecipientRole.class);
			if (rcptRole != null) tgt.setRecipientRole(rcptRole);
			EventAttendee.ResponseStatus respStatus = EnumUtils.forSerializedName(src.getType(), EventAttendee.ResponseStatus.class);
			if (respStatus != null) tgt.setResponseStatus(respStatus);
		}
		
		return tgt;
	}
	
	private String buildEtag(DateTime revisionTimestamp) {
		if (revisionTimestamp != null) {
			return ETAG_FMT.print(revisionTimestamp);
		} else {
			return DEFAULT_ETAG;
		}
	}
	
	private CalendarManager getManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private CalendarManager getManager(UserProfileId targetProfileId) {
		return (CalendarManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
				.code(status.getStatusCode())
				.description(message);
	}
}
