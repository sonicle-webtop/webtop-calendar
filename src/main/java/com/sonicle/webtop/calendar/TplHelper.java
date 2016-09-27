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
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.MapItemList;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.calendar.bol.model.EventBase;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.util.NotificationHelper;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.mail.internet.AddressException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class TplHelper {
	private static final String SERVICE_ID = "com.sonicle.webtop.calendar";
	
	public static String buildEventInvitationEmailSubject(Locale locale, String dateFormat, String timeFormat, String crud, EventBase event) {
		DateTimeFormatter fmt = DateTimeUtils.createFormatter(dateFormat + " " + timeFormat, DateTimeZone.forID(event.getTimezone()));
		StringBuilder sb = new StringBuilder();
		
		sb.append(StringUtils.abbreviate(event.getTitle(), 30));
		sb.append(" @ ");
		
		//TODO: solo l'orario se date coincidono!!!
		
		sb.append(fmt.print(event.getStartDate()));
		sb.append(" - ");
		sb.append(fmt.print(event.getEndDate()));
		//sb.append(" (");
		//sb.append(event.getTimezone());
		//sb.append(")");
		
		String subjectFmt = WT.lookupResource(SERVICE_ID, locale, MessageFormat.format(CalendarLocale.INVITATION_SUBJECT_X, crud));
		return NotificationHelper.buildSubject(locale, SERVICE_ID, MessageFormat.format(subjectFmt, sb.toString()));
	}
	
	public static String buildEventInvitationBodyTpl(Locale locale, String dateFormat, String timeFormat, EventBase event, String recipientEmail, String servicePublicUrl) throws IOException, TemplateException, AddressException {
		MapItem i18n = new MapItem();
		i18n.put("whenStart", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_START));
		i18n.put("whenEnd", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_END));
		i18n.put("where", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE));
		i18n.put("whereMap", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE_MAP));
		i18n.put("organizer", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_ORGANIZER));
		i18n.put("who", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHO));
		i18n.put("going", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING));
		i18n.put("goingYes", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING_YES));
		i18n.put("goingMaybe", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING_MAYBE));
		i18n.put("goingNo", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING_NO));
		i18n.put("view", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_VIEW));
		
		DateTimeFormatter fmt = DateTimeUtils.createFormatter(dateFormat + " " + timeFormat, DateTimeZone.forID(event.getTimezone()));
		MapItem evt = new MapItem();
		evt.put("title", StringUtils.defaultIfBlank(event.getTitle(), ""));
		evt.put("description", StringUtils.defaultIfBlank(event.getDescription(), ""));
		evt.put("timezone", event.getTimezone());
		evt.put("startDate", fmt.print(event.getStartDate()));
		evt.put("endDate", fmt.print(event.getEndDate()));
		evt.put("location", StringUtils.defaultIfBlank(event.getLocation(), null));
		evt.put("locationUrl", TplHelper.buildGoogleMapsUrl(event.getLocation())); 
		evt.put("organizer", StringUtils.defaultIfBlank(event.getOrganizerCN(), event.getOrganizerAddress()));
		
		String recipientAttendeeId = null;
		MapItemList evtAtts = new MapItemList();
		for(EventAttendee attendee : event.getAttendees()) {
			MapItem item = new MapItem();
			String cn = attendee.getCN();
			String address = attendee.getAddress();
			if(StringUtils.equals(address, recipientEmail)) recipientAttendeeId = attendee.getAttendeeId();
			item.put("cn", StringUtils.isBlank(cn) ? null : cn);
			item.put("address", StringUtils.isBlank(address) ? null : address);
			evtAtts.add(item);
		}
		
		String viewUrl = CalendarManager.buildEventPublicUrl(servicePublicUrl, event.getPublicUid());
		
		MapItem vars = new MapItem();
		vars.put("i18n", i18n);
		vars.put("event", evt);
		vars.put("eventAttendees", evtAtts);
		vars.put("replyYesUrl", (recipientAttendeeId == null) ? null : CalendarManager.buildEventReplyPublicUrl(servicePublicUrl, event.getPublicUid(), recipientAttendeeId, "yes"));
		vars.put("replyMaybeUrl", (recipientAttendeeId == null) ? null : CalendarManager.buildEventReplyPublicUrl(servicePublicUrl, event.getPublicUid(), recipientAttendeeId, "maybe"));
		vars.put("replyNoUrl", (recipientAttendeeId == null) ? null : CalendarManager.buildEventReplyPublicUrl(servicePublicUrl, event.getPublicUid(), recipientAttendeeId, "no"));
		vars.put("viewUrl", viewUrl);
		
		return WT.buildTemplate(SERVICE_ID, "tpl/email/eventInvitation-body.html", vars);
	}
	
	public static String buildInvitationTpl(Locale locale, String crud, String source, String recipientEmail, String bodyHeader, String customBodyHtml, String becauseString) throws IOException, TemplateException {
		MapItem notMap = new MapItem();
		notMap.putAll(createInvitationTplStrings(locale, crud, source, recipientEmail, bodyHeader, customBodyHtml, becauseString));
		MapItem map = new MapItem();
		map.put("recipientEmail", StringUtils.defaultString(recipientEmail));
		return NotificationHelper.buildCustomBodyTpl(notMap, map);
	}
	
	public static Map<String, String> createInvitationTplStrings(Locale locale, String crud, String source, String recipientEmail, String bodyHeader, String customBody, String becauseString) {
		HashMap<String, String> map = new HashMap<>();
		if(StringUtils.equals(crud, "update")) {
			map.put("greenMessage", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_CRUD_UPDATE));
		} else if(StringUtils.equals(crud, "delete")) {
			map.put("redMessage", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_CRUD_DELETE));
		}
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("customBody", StringUtils.defaultString(customBody));
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_HEADER), source));
		String foomsg = MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_MESSAGE), recipientEmail, becauseString);
		foomsg += "<br>"+WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_DISCLAIMER);
		map.put("footerMessage", foomsg);
		return map;
	}
	
	public static String buildGoogleMapsUrl(String s) {
		return "https://maps.google.com/maps?q=" + StringUtils.defaultString(LangUtils.encodeURL(s));
	}
}
