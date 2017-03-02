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
package com.sonicle.webtop.calendar;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.calendar.bol.js.JsPubEvent;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.bol.js.JsWTSPublic;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class PublicService extends BasePublicService {
	public static final Logger logger = WT.getLogger(PublicService.class);
	public static final String PUBPATH_CONTEXT_EVENT = "event";
	
	private CalendarManager manager;
	
	@Override
	public void initialize() throws Exception {
		manager = (CalendarManager)WT.getServiceManager(SERVICE_ID);
	}

	@Override
	public void cleanup() throws Exception {
		manager = null;
	}
	
	private WebTopSession getWts() {
		return getEnv().getWebTopSession();
	}
	
	@Override
	public void processDefaultAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PublicPath path = new PublicPath(request.getPathInfo());
		WebTopSession wts = getEnv().getWebTopSession();
		
		try {
			try {
				if(path.getContext().equals(PUBPATH_CONTEXT_EVENT)) {
					EventUrlPath eventUrlPath = new EventUrlPath(path.getRemainingPath());

					Event event = null;
					if(!StringUtils.isBlank(eventUrlPath.getPublicUid())) {
						
						if(eventUrlPath.isActionReply()) {
							String aid = ServletUtils.getStringParameter(request, "aid", true);
							String resp = ServletUtils.getStringParameter(request, "resp", true);
							
							String responseStatus = toResponseStatus(resp);
							if(responseStatus == null) throw new WTException("Invalid resp [{0}]", resp);
							
							event = manager.updateEventAttendeeResponse(eventUrlPath.getPublicUid(), aid, responseStatus);
							
						} else {
							event = manager.getEvent(eventUrlPath.getPublicUid());
						}
					}
					
					if(event == null) {
						logger.trace("Event not found [{}]", eventUrlPath.getPublicUid());
						writeErrorPage(request, response, wts, "eventnotfound");
						
					} else {
						Calendar calendar = manager.getCalendar(event.getCalendarId());
						writeEventPage(request, response, wts, "Event", calendar, event);
					}

				} else {
					logger.trace("Invalid context [{}]", path.getContext());
					writeErrorPage(request, response, wts, "badrequest");
				}
				
			} catch(Exception ex) {
				writeErrorPage(request, response, wts, "badrequest");
				//logger.trace("Error", t);
			}
		} catch(Throwable t) {
			logger.error("Unexpected error", t);
		}
	}
	
	private String toResponseStatus(String resp) {
		if(resp.equals("yes")) {
			return EventAttendee.RESPONSE_STATUS_ACCEPTED;
		} else if(resp.equals("no")) {
			return EventAttendee.RESPONSE_STATUS_DECLINED;
		} else if(resp.equals("maybe")) {
			return EventAttendee.RESPONSE_STATUS_TENTATIVE;
		} else {
			return null;
		}
	}
	
	private String buildEventData(Calendar calendar, Event event) {
		JsPubEvent js = new JsPubEvent();
		js.id = 1;
		js.title = event.getTitle();
		js.when = buildWhenString(calendar, event);
		js.timezone = event.getTimezone();
		js.where = event.getLocation();
		js.whereUrl = TplHelper.buildGoogleMapsUrl(event.getLocation());
		js.calendar = calendar.getName();
		js.organizer = event.getOrganizer();
		//js.organizer = buildOrganizer(new UserProfileId(event.getCalendarProfileId()));
		js.attendees = buildAttendees(js.id, event);
		return JsonResult.GSON.toJson(js);
	}
	
	private String buildWhenString(Calendar calendar, Event event) {
		CoreUserSettings cus = new CoreUserSettings(calendar.getProfileId());
		String pattern = cus.getShortDateFormat() + " " + cus.getShortTimeFormat();
		DateTimeZone etz = DateTimeZone.forID(event.getTimezone());
		DateTimeFormatter dtFmt = DateTimeUtils.createFormatter(pattern, etz);
		return MessageFormat.format("{0} - {1}", dtFmt.print(event.getStartDate()), dtFmt.print(event.getEndDate()));
	}
	
	private String buildOrganizer(UserProfileId organizerPid) {
		return StringUtils.defaultString(WT.getUserData(organizerPid).getDisplayName(), organizerPid.toString());
	}
	
	private ArrayList<JsPubEvent.Attendee> buildAttendees(int id, Event event) {
		ArrayList<JsPubEvent.Attendee> attendees = new ArrayList<>();
		for(EventAttendee attendee : event.getAttendees()) {
			attendees.add(new JsPubEvent.Attendee(id, attendee));
		}
		return attendees;
	}
	
	private void writeEventPage(HttpServletRequest request, HttpServletResponse response, WebTopSession wts, String view, Calendar calendar, Event event) throws IOException, TemplateException {
		writeEventPage(request, response, wts, view, calendar, event, new JsWTSPublic.Vars());
	}
	
	private void writeEventPage(HttpServletRequest request, HttpServletResponse response, WebTopSession wts, String view, Calendar calendar, Event event, JsWTSPublic.Vars vars) throws IOException, TemplateException {
		vars.put("view", view);
		vars.put("eventData", buildEventData(calendar, event));
		writePage(response, wts, vars, ServletUtils.getBaseURL(request));
	}
	
	private void writeErrorPage(HttpServletRequest request, HttpServletResponse response, WebTopSession wts, String reskey) throws IOException, TemplateException {
		JsWTSPublic.Vars vars = new JsWTSPublic.Vars();
		vars.put("view", "Error");
		vars.put("reskey", reskey);
		writePage(response, wts, vars, ServletUtils.getBaseURL(request));
	}
	
	public static class EventUrlPath extends UrlPathTokens {
		public final static String TOKEN_REPLY = "reply";
			
		public EventUrlPath(String remainingPath) {
			super(StringUtils.split(remainingPath, "/", 2));
		}
		
		public String getPublicUid() {
			return getTokenAt(0);
		}
		
		public String getAction() {
			return getTokenAt(1);
		}
		
		public boolean isActionReply() {
			return StringUtils.equals(getAction(), TOKEN_REPLY);
		}
	}
	
	
	
	
	/*
	private void processEvent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map tplMap = new HashMap();
		String template = null;
		
		Locale locale = ServletHelper.homogenizeLocale(request);
		String action = ServletUtils.getStringParameter(request, "action", true);
		
		tplMap.put("urlPublic", getPublicResourcesBaseUrl());
		try {
			if(action.equals("reply")) {
				String eid = ServletUtils.getStringParameter(request, "eid", true);
				String aid = ServletUtils.getStringParameter(request, "aid", true);
				String reply = ServletUtils.getStringParameter(request, "reply", true);

				// Translates replies...
				String resp = null;
				if(reply.equals("yes")) {
					resp = EventAttendee.RESPONSE_STATUS_ACCEPTED;
				} else if(reply.equals("no")) {
					resp = EventAttendee.RESPONSE_STATUS_DECLINED;
				} else if(reply.equals("maybe")) {
					resp = EventAttendee.RESPONSE_STATUS_TENTATIVE;
				} else {
					throw new WTException("Invalid reply provided. Valid options are: 'yes', 'no' and 'maybe'.");
				}

				EventBase event = manager.updateEventAttendeeReply(eid, aid, resp);
				if(event == null) throw new EventNotFoundException();
				
				//TODO: inviare email all'organizzatore con la notifica della risposta
				
				template = "event_reply.html";
				tplMap.put("reply", reply);
				tplMap.put("event", buildEventReplyMap(event));
				Map i18n = buildStringsMap(locale, new String[]{
					CalendarLocale.PUB_WHEN,
					CalendarLocale.PUB_LOCATION,
					CalendarLocale.PUB_MAP,
					CalendarLocale.PUB_ORGANIZER,
					CalendarLocale.PUB_CONFIRM_YES,
					CalendarLocale.PUB_CONFIRM_NO,
					CalendarLocale.PUB_CONFIRM_MAYBE,
					CalendarLocale.PUB_SIGNATURE
				});
				tplMap.put("i18n", i18n);

			} else if(action.equals("view")) {
				String eid = ServletUtils.getStringParameter(request, "eid", true);
				
				EventBase event = manager.getEventByPublicUid(eid);
				if(event == null) throw new EventNotFoundException();
				List<EventAttendee> atts = manager.listEventAttendees(event.getEventId(), true);
				
				template = "event_view.html";
				tplMap.put("event", buildEventViewMap(event));
				tplMap.put("attendees", buildEventAttendeesMap(atts));
				Map i18n = buildStringsMap(locale, new String[]{
					CalendarLocale.PUB_SUMMARY,
					CalendarLocale.PUB_WHEN,
					CalendarLocale.PUB_LOCATION,
					CalendarLocale.PUB_MAP,
					CalendarLocale.PUB_ATTENDEES,
					CalendarLocale.PUB_ORGANIZER,
					CalendarLocale.PUB_RESPONSE_UNKNOWN,
					CalendarLocale.PUB_RESPONSE_DECLINED,
					CalendarLocale.PUB_RESPONSE_TENTATIVE,
					CalendarLocale.PUB_RESPONSE_ACCEPTED,
					CalendarLocale.PUB_SIGNATURE
				});
				tplMap.put("i18n", i18n);
			}
		} catch(EventNotFoundException ex) {
			template = "event_notfound.html";
			Map i18n = buildStringsMap(locale, new String[]{
				CalendarLocale.PUB_NOTFOUND_TITLE,
				CalendarLocale.PUB_NOTFOUND_MESSAGE,
				CalendarLocale.PUB_SIGNATURE
			});
			tplMap.put("i18n", i18n);
		}
		
		Template tpl = WT.loadTemplate(SERVICE_ID, template);
		tpl.process(tplMap, response.getWriter());
	}
	
	private String getOrganizer(UserProfileId profileId) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			OUser user = udao.selectByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			return StringUtils.defaultString(user.getDisplayName(), profileId.toString());
			
		} catch(Exception ex) {
			return "";
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String buildWhenString99(EventBase se) {
		UserProfileId profileId = new UserProfileId(se.getCalendarProfileId());
		CoreUserSettings cus = new CoreUserSettings(profileId);
		String pattern = cus.getShortDateFormat() + " " + cus.getShortTimeFormat();
		DateTimeZone etz = DateTimeZone.forID(se.getTimezone());
		DateTimeFormatter dtFmt = DateTimeUtils.createFormatter(pattern, etz);
		return MessageFormat.format("{0} - {1}", dtFmt.print(se.getStartDate()), dtFmt.print(se.getEndDate()));
	}
	
	private Map buildEventReplyMap(EventBase event) {
		Map map = new HashMap();
		String when = buildWhenString99(event);
		String organizer = getOrganizer(new UserProfileId(event.getCalendarProfileId()));
		
		map.put("title", event.getTitle());
		map.put("when", when);
		map.put("timezone", event.getTimezone());
		map.put("location", event.getLocation());
		map.put("organizer", organizer);
		
		return map;
	}
	
	private Map buildEventViewMap(EventBase event) {
		Map map = new HashMap();
		String when = buildWhenString99(event);
		String organizer = getOrganizer(new UserProfileId(event.getCalendarProfileId()));
		
		map.put("title", event.getTitle());
		map.put("when", when);
		map.put("timezone", event.getTimezone());
		map.put("location", event.getLocation());
		map.put("organizer", organizer);
		
		return map;
	}
	
	private List<Map> buildEventAttendeesMap(List<EventAttendee> attendees) {
		List<Map> list = new ArrayList<>();
		
		Map attMap = null;
		for(EventAttendee att : attendees) {
			attMap = new HashMap();
			//TODO: completare il campo email con l'indirizzo di posta
			attMap.put("name", att.getRecipient());
			attMap.put("response", att.getResponseStatus());
			list.add(attMap);
		}
		
		return list;
	}
	
	private Map<String, String> buildStringsMap(Locale locale, String[] keys) {
		HashMap<String, String> map = new HashMap<>();
		for(String key : keys) {
			map.put(LangUtils.camelize(key), lookupResource(locale, key, true));
		}		
		return map;
	}
	
	public static class EventNotFoundException extends WTException {
		public EventNotFoundException() {
			super();
		}
		public EventNotFoundException(String message, Object... arguments) {
			super(message, arguments);
		}
		public EventNotFoundException(Throwable cause, String message, Object... arguments) {
			super(cause, message, arguments);
		}
	}
	*/
}
