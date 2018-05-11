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
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.CalendarServiceSettings;
import com.sonicle.webtop.calendar.model.EventCalObject;
import com.sonicle.webtop.calendar.model.EventCalObjectChanged;
import com.sonicle.webtop.calendar.swagger.v1.api.CaldavApi;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObject;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectChanged;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectNew;
import com.sonicle.webtop.calendar.swagger.v1.model.CalObjectsChanges;
import com.sonicle.webtop.calendar.swagger.v1.model.Calendar;
import com.sonicle.webtop.calendar.swagger.v1.model.CalendarNew;
import com.sonicle.webtop.calendar.swagger.v1.model.CalendarUpdate;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTNotFoundException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import net.fortuna.ical4j.data.ParserException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class CalDav extends CaldavApi {
	private static final String DEFAULT_ETAG = "19700101000000";
	private static final DateTimeFormatter ETAG_FORMATTER = DateTimeUtils.createFormatter("yyyyMMddHHmmss", DateTimeZone.UTC);
	
	@Override
	public Response getCalendars() {
		CalendarManager manager = getManager();
		List<Calendar> items = new ArrayList<>();
		
		try {
			Map<Integer, com.sonicle.webtop.calendar.model.Calendar> cats = manager.listCalendars();
			Map<Integer, DateTime> revisions = manager.getCalendarsLastRevision(cats.keySet());
			for (com.sonicle.webtop.calendar.model.Calendar cal : cats.values()) {
				if (cal.isProviderRemote()) continue;
				items.add(createCalendar(cal, revisions.get(cal.getCalendarId())));
			}
			return respOk(items);
			
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response getCalendar(Integer calendarId) {
		CalendarManager manager = getManager();
		
		try {
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorNotFound();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			Map<Integer, DateTime> revisions = manager.getCalendarsLastRevision(Arrays.asList(cal.getCalendarId()));
			return respOk(createCalendar(cal, revisions.get(cal.getCalendarId())));
			
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response addCalendar(CalendarNew body) {
		CalendarManager manager = getManager();
		
		try {
			com.sonicle.webtop.calendar.model.Calendar cal = new com.sonicle.webtop.calendar.model.Calendar();
			cal.setName(body.getDisplayName());
			cal.setDescription(body.getDescription());
			cal = manager.addCalendar(cal);
			return respOkCreated(createCalendar(cal, null));
			
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response updateCalendar(Integer calendarId, CalendarUpdate body) {
		CalendarManager manager = getManager();
		
		try {
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorNotFound();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			if (body.getUpdatedFields().contains("displayName")) {
				cal.setName(body.getDisplayName());
			}
			if (body.getUpdatedFields().contains("description")) {
				cal.setDescription(body.getDescription());
			}
			if (body.getUpdatedFields().contains("color")) {
				cal.setColor(body.getColor());
			}
			manager.updateCalendar(cal);
			return respOk();
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response deleteCalendar(Integer calendarId) {
		CalendarManager manager = getManager();
		
		try {
			CalendarServiceSettings css = new CalendarServiceSettings(SERVICE_ID, RunContext.getRunProfileId().getDomainId());
			if (css.getDavCalendarDeleteEnabled()) {
				manager.deleteCalendar(calendarId);
				return respOkNoContent();
			} else {
				return respErrorNotAllowed();
			}
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(WTException ex) {
			return respError(ex);
		}
	}
	
	@Override
	public Response getCalObjects(Integer calendarId, List<String> hrefs) {
		CalendarManager manager = getManager();
		List<CalObject> items = new ArrayList<>();
		
		try {
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorBadRequest();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			if ((hrefs == null) || hrefs.isEmpty()) {
				List<EventCalObject> calObjs = manager.listEventCalObjects(calendarId);
				for (EventCalObject calObj : calObjs) {
					items.add(createCalObject(calObj));
				}
				return respOk(items);
				
			} else {
				List<EventCalObject> calObjs = manager.getEventCalObjects(calendarId, hrefs);
				for (EventCalObject calObj : calObjs) {
					items.add(createCalObject(calObj));
				}
				return respOk(items);
			}
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response getCalObjectsChanges(Integer calendarId, String syncToken, Integer limit) {
		CalendarManager manager = getManager();
		
		try {
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorBadRequest();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			Map<Integer, DateTime> revisions = manager.getCalendarsLastRevision(Arrays.asList(calendarId));
			if (revisions.isEmpty()) return respErrorNotFound();
			
			DateTime since = null;
			if (!StringUtils.isBlank(syncToken)) {
				since = ETAG_FORMATTER.parseDateTime(syncToken);
				if (since == null) return respErrorBadRequest();
			}
			
			CollectionChangeSet<EventCalObjectChanged> changes = manager.listEventCalObjectsChanges(calendarId, since, limit);
			return respOk(createCalObjectsChanges(revisions.get(calendarId), changes));
			
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response getCalObject(Integer calendarId, String href) {
		CalendarManager manager = getManager();
		
		try {
			com.sonicle.webtop.calendar.model.Calendar cal = manager.getCalendar(calendarId);
			if (cal == null) return respErrorBadRequest();
			if (cal.isProviderRemote()) return respErrorBadRequest();
			
			EventCalObject calObj = manager.getEventCalObject(calendarId, href);
			if (calObj != null) {
				return respOk(createCalObjectWithData(calObj));
			} else {
				return respErrorNotFound();
			}
			
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response addCalObject(Integer calendarId, CalObjectNew body) {
		CalendarManager manager = getManager();
		
		try {
			// Manager's call is already ro protected for remoteProviders
			net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body.getIcalendar());
			manager.addEventCalObject(calendarId, body.getHref(), iCalendar);
			return respOk();
			
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response updateCalObject(Integer calendarId, String href, String body) {
		CalendarManager manager = getManager();
		
		try {
			// Manager's call is already ro protected for remoteProviders
			net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body);
			manager.updateEventCalObject(calendarId, href, iCalendar);
			return respOkNoContent();
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(WTException ex) {
			return respError(ex);
		}
	}

	@Override
	public Response deleteCalObject(Integer calendarId, String href) {
		CalendarManager manager = getManager();
		
		try {
			manager.deleteEventCalObject(calendarId, href);
			return respOkNoContent();
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(WTException ex) {
			return respError(ex);
		}
	}
	
	private Calendar createCalendar(com.sonicle.webtop.calendar.model.Calendar cal, DateTime lastRevisionTimestamp) {
		return new Calendar()
				.id(cal.getCalendarId())
				.displayName(cal.getName())
				.description(cal.getDescription())
				.color(cal.getColor())
				.syncToken(buildEtag(lastRevisionTimestamp));
	}
	
	private CalObject createCalObject(EventCalObject calObject) {
		return new CalObject()
				.id(calObject.getEventId())
				.uid(calObject.getPublicUid())
				.href(calObject.getHref())
				.lastModified(calObject.getRevisionTimestamp().withZone(DateTimeZone.UTC).getMillis()/1000)
				.etag(buildEtag(calObject.getRevisionTimestamp()))
				.size(calObject.getSize());
	}
	
	private CalObject createCalObjectWithData(EventCalObject calObject) {
		return createCalObject(calObject)
				.icalendar(calObject.getIcalendar());
	}
	
	private CalObjectChanged createCalObjectChanged(EventCalObjectChanged calObject) {
		return new CalObjectChanged()
				.id(calObject.getEventId())
				.href(calObject.getHref())
				.etag(buildEtag(calObject.getRevisionTimestamp()));
	}
	
	private CalObjectsChanges createCalObjectsChanges(DateTime lastRevisionTimestamp, LangUtils.CollectionChangeSet<EventCalObjectChanged> changes) {
		ArrayList<CalObjectChanged> inserted = new ArrayList<>();
		for (EventCalObjectChanged calObj : changes.inserted) {
			inserted.add(createCalObjectChanged(calObj));
		}
		
		ArrayList<CalObjectChanged> updated = new ArrayList<>();
		for (EventCalObjectChanged calObj : changes.updated) {
			updated.add(createCalObjectChanged(calObj));
		}
		
		ArrayList<CalObjectChanged> deleted = new ArrayList<>();
		for (EventCalObjectChanged calObj : changes.deleted) {
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
		return (CalendarManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
	}
}
