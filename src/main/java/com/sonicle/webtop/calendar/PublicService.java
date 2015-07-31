/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.calendar;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.servlet.ServletHelper;
import freemarker.template.Template;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	
	private CalendarManager manager;

	@Override
	public void initialize() {
		manager = new CalendarManager(getId(), getRunContext());
	}

	@Override
	public void cleanup() {
		manager = null;
	}
	
	@Override
	public void processDefault(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String context = extractContext(request.getPathInfo());
		
		if(context.equals("event")) {
			processEvent(request, response);
		} else {
			throw new WTException("Invalid context [{0}]", context);
		}
	}
	
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

				Event event = manager.updateAttendeeReply(eid, aid, resp);
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
				
				Event event = manager.readEventByPublicUid(eid);
				if(event == null) throw new EventNotFoundException();
				List<EventAttendee> atts = manager.getAttendees(event.getEventId(), true);
				
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
		
		Template tpl = WT.loadTemplate(getId(), template);
		tpl.process(tplMap, response.getWriter());
	}
	
	private String getOrganizer(UserProfile.Id profileId) {
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
	
	private String buildWhenString(Event se) {
		UserProfile.Id profileId = new UserProfile.Id(se.getCalendarProfileId());
		CoreUserSettings cus = new CoreUserSettings(profileId.getDomainId(), profileId.getUserId());
		String pattern = cus.getShortDateFormat() + " " + cus.getShortTimeFormat();
		DateTimeZone etz = DateTimeZone.forID(se.getTimezone());
		DateTimeFormatter dtFmt = DateTimeUtils.createFormatter(pattern, etz);
		return MessageFormat.format("{0} - {1}", dtFmt.print(se.getStartDate()), dtFmt.print(se.getEndDate()));
	}
	
	private Map buildEventReplyMap(Event event) {
		Map map = new HashMap();
		String when = buildWhenString(event);
		String organizer = getOrganizer(new UserProfile.Id(event.getCalendarProfileId()));
		
		map.put("title", event.getTitle());
		map.put("when", when);
		map.put("timezone", event.getTimezone());
		map.put("location", event.getLocation());
		map.put("organizer", organizer);
		
		return map;
	}
	
	private Map buildEventViewMap(Event event) {
		Map map = new HashMap();
		String when = buildWhenString(event);
		String organizer = getOrganizer(new UserProfile.Id(event.getCalendarProfileId()));
		
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
}
