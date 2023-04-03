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
package com.sonicle.webtop.calendar.rest.v1;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.CalendarLocale;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.CalendarServiceSettings;
import com.sonicle.webtop.calendar.EventObjectOutputType;
import com.sonicle.webtop.calendar.ManagerUtils;
import com.sonicle.webtop.calendar.model.CalendarFSFolder;
import com.sonicle.webtop.calendar.model.CalendarFSOrigin;
import com.sonicle.webtop.calendar.model.EventObject;
import com.sonicle.webtop.calendar.model.EventObjectChanged;
import com.sonicle.webtop.calendar.model.EventObjectWithICalendar;
import com.sonicle.webtop.calendar.swagger.v1.api.CaldavApi;
import com.sonicle.webtop.calendar.swagger.v1.model.ApiError;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObject;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectChanged;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectNew;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectsChanges;
import com.sonicle.webtop.calendar.swagger.v1.model.Calendar;
import com.sonicle.webtop.calendar.swagger.v1.model.CalendarNew;
import com.sonicle.webtop.calendar.swagger.v1.model.CalendarUpdate;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Response;
import net.fortuna.ical4j.data.ParserException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class CalDav extends CaldavApi {
	private static final Logger logger = LoggerFactory.getLogger(CalDav.class);
	private static final String DEFAULT_ETAG = "19700101000000000";
	private static final DateTimeFormatter ETAG_FORMATTER = DateTimeUtils.createFormatter("yyyyMMddHHmmssSSS", DateTimeZone.UTC);
	
	@Override
	public Response getCalendars() {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		CalendarManager manager = getManager();
		List<Calendar> items = new ArrayList<>();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getCalendars()", currentProfileId);
		}
		
		try {
			Map<Integer, com.sonicle.webtop.calendar.model.Calendar> calendars = manager.listMyCalendars();
			Map<Integer, DateTime> revisions = manager.getCalendarsLastRevision(calendars.keySet());
			for (com.sonicle.webtop.calendar.model.Calendar calendar : calendars.values()) {
				if (calendar.isProviderRemote()) continue;
				items.add(createCalendar(currentProfileId, calendar, false, revisions.get(calendar.getCalendarId()), FolderShare.Permissions.full()));
			}
			
			for (CalendarFSOrigin origin : manager.listIncomingCalendarOrigins().values()) {
				Map<Integer, CalendarFSFolder> folders = manager.listIncomingCalendarFolders(origin);
				revisions = manager.getCalendarsLastRevision(folders.keySet());
				for (CalendarFSFolder folder : folders.values()) {
					com.sonicle.webtop.calendar.model.Calendar calendar = folder.getCalendar();
					if (calendar.isProviderRemote()) continue;
					items.add(createCalendar(currentProfileId, calendar, origin.isResource(), revisions.get(calendar.getCalendarId()), folder.getPermissions()));
				}
			}
			
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] getCalendars()", ex, currentProfileId);
			return respError(ex);
		}
	}

	@Override
	public Response getCalendar(String calendarUid) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getCalendar({})", currentProfileId, calendarUid);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			com.sonicle.webtop.calendar.model.Calendar calendar = manager.getCalendar(calendarId);
			if (calendar == null) return respErrorNotFound();
			if (calendar.isProviderRemote()) return respErrorBadRequest();
			
			Map<Integer, DateTime> revisions = manager.getCalendarsLastRevision(Arrays.asList(calendar.getCalendarId()));
			CalendarFSOrigin origin = manager.getIncomingCalendarOriginByFolderId(calendarId);
			if (origin != null) {
				Map<Integer, CalendarFSFolder> folders = manager.listIncomingCalendarFolders(origin);
				return respOk(createCalendar(currentProfileId, calendar, origin.isResource(), revisions.get(calendar.getCalendarId()), folders.get(calendarId).getPermissions()));
			} else {
				return respOk(createCalendar(currentProfileId, calendar, false, revisions.get(calendar.getCalendarId()), FolderShare.Permissions.full()));
			}
			
		} catch(Exception ex) {
			logger.error("[{}] getCalendar({})", ex, currentProfileId, calendarUid);
			return respError(ex);
		}
	}

	@Override
	public Response addCalendar(CalendarNew body) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] addCalendar(...)", currentProfileId);
			logger.debug("{}", body);
		}
		
		try {
			com.sonicle.webtop.calendar.model.Calendar calendar = new com.sonicle.webtop.calendar.model.Calendar();
			calendar.setName(body.getDisplayName());
			calendar.setDescription(body.getDescription());
			calendar = manager.addCalendar(calendar);
			// Calendars are always added in currentProfile so we do not handle perms here (passing null = full rights)
			return respOkCreated(createCalendar(currentProfileId, calendar, false, null, FolderShare.Permissions.full()));
			
		} catch(Exception ex) {
			logger.error("[{}] addCalendar(...)", ex, currentProfileId);
			return respError(ex);
		}
	}

	@Override
	public Response updateCalendar(String calendarUid, CalendarUpdate body) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] updateCalendar({}, ...)", RunContext.getRunProfileId(), calendarUid);
			logger.debug("{}", body);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			com.sonicle.webtop.calendar.model.Calendar calendar = manager.getCalendar(calendarId);
			if (calendar == null) return respErrorNotFound();
			if (calendar.isProviderRemote()) return respErrorBadRequest();
			
			if (body.getUpdatedFields().contains("displayName")) {
				calendar.setName(body.getDisplayName());
			}
			if (body.getUpdatedFields().contains("description")) {
				calendar.setDescription(body.getDescription());
			}
			if (body.getUpdatedFields().contains("color")) {
				calendar.setColor(body.getColor());
			}
			manager.updateCalendar(calendar);
			return respOk();
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Exception ex) {
			logger.error("[{}] updateCalendar({}, ...)", ex, RunContext.getRunProfileId(), calendarUid);
			return respError(ex);
		}
	}

	@Override
	public Response deleteCalendar(String calendarUid) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] deleteCalendar({})", RunContext.getRunProfileId(), calendarUid);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			CalendarServiceSettings css = new CalendarServiceSettings(SERVICE_ID, RunContext.getRunProfileId().getDomainId());
			if (css.getDavCalendarDeleteEnabled()) {
				manager.deleteCalendar(calendarId);
				return respOkNoContent();
			} else {
				return respErrorNotAllowed();
			}
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Exception ex) {
			logger.error("[{}] deleteCalendar({})", ex, RunContext.getRunProfileId(), calendarUid);
			return respError(ex);
		}
	}
	
	@Override
	public Response getCalObjects(String calendarUid, List<String> hrefs) {
		CalendarManager manager = getManager();
		List<CalObject> items = new ArrayList<>();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getCalObjects({})", RunContext.getRunProfileId(), calendarUid);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorBadRequest();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			if ((hrefs == null) || hrefs.isEmpty()) {
				List<EventObject> calObjs = manager.listEventObjects(calendarId, EventObjectOutputType.ICALENDAR);
				for (EventObject calObj : calObjs) {
					items.add(createCalObjectWithData((EventObjectWithICalendar)calObj));
				}
				return respOk(items);
				
			} else {
				List<EventObjectWithICalendar> calObjs = manager.getEventObjectsWithICalendar(calendarId, hrefs);
				for (EventObject calObj : calObjs) {
					items.add(createCalObjectWithData((EventObjectWithICalendar)calObj));
				}
				return respOk(items);
			}
			
		} catch(Exception ex) {
			logger.error("[{}] getCalObjects({})", ex, RunContext.getRunProfileId(), calendarUid);
			return respError(ex);
		}
	}

	@Override
	public Response getCalObjectsChanges(String calendarUid, String syncToken, Integer limit) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getCalObjectsChanges({}, {}, {})", RunContext.getRunProfileId(), calendarUid, syncToken, limit);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorNotFound();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			Map<Integer, DateTime> revisions = manager.getCalendarsLastRevision(Arrays.asList(calendarId));
			
			DateTime since = null;
			if (!StringUtils.isBlank(syncToken)) {
				since = ETAG_FORMATTER.parseDateTime(syncToken);
				if (since == null) return respErrorBadRequest();
			}
			
			CollectionChangeSet<EventObjectChanged> changes = manager.listEventObjectsChanges(calendarId, since, limit);
			return respOk(createCalObjectsChanges(revisions.get(calendarId), changes));
			
		} catch(Exception ex) {
			logger.error("[{}] getCalObjectsChanges({}, {}, {})", ex, RunContext.getRunProfileId(), calendarUid, syncToken, limit);
			return respError(ex);
		}
	}

	@Override
	public Response getCalObject(String calendarUid, String href) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getCalObject({}, {})", RunContext.getRunProfileId(), calendarUid, href);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorBadRequest();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			EventObjectWithICalendar calObj = manager.getEventObjectWithICalendar(calendarId, href);
			if (calObj != null) {
				return respOk(createCalObjectWithData(calObj));
			} else {
				return respErrorNotFound();
			}
			
		} catch(Exception ex) {
			logger.error("[{}] getCalObject({}, {})", ex, RunContext.getRunProfileId(), calendarUid, href);
			return respError(ex);
		}
	}

	@Override
	public Response addCalObject(String calendarUid, CalObjectNew body) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] addCalObject({}, ...)", RunContext.getRunProfileId(), calendarUid);
			logger.debug("{}", body);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			// Manager's call is already ro protected for remoteProviders
			net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body.getIcalendar());
			manager.addEventObject(calendarId, body.getHref(), iCalendar);
			return respOk();
			
		} catch(Exception ex) {
			logger.error("[{}] addCalObject({}, ...)", ex, RunContext.getRunProfileId(), calendarUid);
			return respError(ex);
		}
	}

	@Override
	public Response updateCalObject(String calendarUid, String href, String body) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] updateCalObject({}, {}, ...)", RunContext.getRunProfileId(), calendarUid, href);
			logger.debug("{}", body);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			// Manager's call is already ro protected for remoteProviders
			net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body);
			manager.updateEventObject(calendarId, href, iCalendar);
			return respOkNoContent();
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Exception ex) {
			logger.error("[{}] updateCalObject({}, {}, ...)", ex, RunContext.getRunProfileId(), calendarUid, href);
			return respError(ex);
		}
	}

	@Override
	public Response deleteCalObject(String calendarUid, String href) {
		CalendarManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] deleteCalObject({}, {})", RunContext.getRunProfileId(), calendarUid, href);
		}
		
		try {
			int calendarId = ManagerUtils.decodeAsCalendarId(calendarUid);
			manager.deleteEventObject(calendarId, href);
			return respOkNoContent();
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Exception ex) {
			logger.error("[{}] deleteCalObject({}, {})", ex, RunContext.getRunProfileId(), calendarUid, href);
			return respError(ex);
		}
	}
	
	private Calendar createCalendar(UserProfileId currentProfileId, com.sonicle.webtop.calendar.model.Calendar cal, boolean isResource, DateTime lastRevisionTimestamp, FolderShare.Permissions permissions) {
		UserProfile.Data owud = WT.getUserData(cal.getProfileId());
		
		String displayName = cal.getName();
		if (isResource) {
			displayName = "[" + WT.lookupResource(SERVICE_ID, getLocale(currentProfileId), CalendarLocale.CALENDAR_TYPE_RESOURCE) + "] " + owud.getDisplayName();
		} else if (!currentProfileId.equals(cal.getProfileId())) {
			//String apn = LangUtils.abbreviatePersonalName(false, owud.getDisplayName());
			displayName = "[" + owud.getDisplayName() + "] " + displayName;
		}
		String ownerUsername = owud.getProfileEmailAddress();
		
		return new Calendar()
				.id(cal.getCalendarId())
				.uid(ManagerUtils.encodeAsCalendarUid(cal.getCalendarId()))
				.displayName(displayName)
				.description(cal.getDescription())
				.color(cal.getColor())
				.syncToken(buildEtag(lastRevisionTimestamp))
				.aclFol(permissions.getFolderPermissions().toString())
				.aclEle(permissions.getItemsPermissions().toString())
				.ownerUsername(ownerUsername);
	}
	
	private CalObject createCalObject(EventObjectWithICalendar calObject) {
		return new CalObject()
				.id(calObject.getEventId())
				.uid(calObject.getPublicUid())
				.href(calObject.getHref())
				.lastModified(calObject.getRevisionTimestamp().withZone(DateTimeZone.UTC).getMillis()/1000)
				.etag(buildEtag(calObject.getRevisionTimestamp()))
				.size(calObject.getSize());
	}
	
	private CalObject createCalObjectWithData(EventObjectWithICalendar calObject) {
		return createCalObject(calObject)
				.icalendar(calObject.getIcalendar());
	}
	
	private CalObjectChanged createCalObjectChanged(EventObjectChanged calObject) {
		return new CalObjectChanged()
				.id(calObject.getEventId())
				.href(calObject.getHref())
				.etag(buildEtag(calObject.getRevisionTimestamp()));
	}
	
	private CalObjectsChanges createCalObjectsChanges(DateTime lastRevisionTimestamp, LangUtils.CollectionChangeSet<EventObjectChanged> changes) {
		ArrayList<CalObjectChanged> inserted = new ArrayList<>();
		for (EventObjectChanged calObj : changes.inserted) {
			inserted.add(createCalObjectChanged(calObj));
		}
		
		ArrayList<CalObjectChanged> updated = new ArrayList<>();
		for (EventObjectChanged calObj : changes.updated) {
			updated.add(createCalObjectChanged(calObj));
		}
		
		ArrayList<CalObjectChanged> deleted = new ArrayList<>();
		for (EventObjectChanged calObj : changes.deleted) {
			deleted.add(createCalObjectChanged(calObj));
		}
		
		return new CalObjectsChanges()
				.syncToken(buildEtag(lastRevisionTimestamp))
				.inserted(inserted)
				.updated(updated)
				.deleted(deleted);
	}
	
	private String buildEtag(DateTime revisionTimestamp) {
		if (revisionTimestamp != null) {
			return ETAG_FORMATTER.print(revisionTimestamp);
		} else {
			return DEFAULT_ETAG;
		}
	}
	
	private net.fortuna.ical4j.model.Calendar parseICalendar(String s) throws WTException {
		try {
			return ICalendarUtils.parse(s);
		} catch(IOException | ParserException ex) {
			throw new WTException(ex, "Unable to parse icalendar data");
		}
	}
	
	private CalendarManager getManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private CalendarManager getManager(UserProfileId targetProfileId) {
		CalendarManager manager = (CalendarManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
		manager.setSoftwareName("rest-caldav");
		return manager;
	}
	
	private Locale getLocale(UserProfileId profileId) {
		UserProfile.Data ud = WT.getProfileData(profileId);
		return (ud != null) ? ud.getLocale() : WT.LOCALE_ENGLISH;
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
				.code(status.getStatusCode())
				.description(message);
	}
}
