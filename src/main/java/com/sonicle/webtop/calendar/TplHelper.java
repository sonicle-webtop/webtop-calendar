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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.MapItemList;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventFootprint;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.util.EmailNotification;
import com.sonicle.webtop.core.util.RRuleStringify;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.helpers.MessageFormatter;

/**
 *
 * @author malbinola
 */
public class TplHelper {
	private static final String SERVICE_ID = "com.sonicle.webtop.calendar";
	
	public static String findMatchingMeetingUrl(Map<String, String> meetingProviders, String url) {
		for (Map.Entry<String, String> entry : meetingProviders.entrySet()) {
			if (StringUtils.startsWithIgnoreCase(url, entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	private static String buildEventTitle(Locale locale, String dateFormat, String timeFormat, EventFootprint event) {
		DateTimeZone etz = DateTimeZone.forID(event.getTimezone());
		DateTimeFormatter fmt = JodaTimeUtils.createFormatter(dateFormat + " " + timeFormat, etz);
		StringBuilder sb = new StringBuilder();
		
		sb.append(StringUtils.abbreviate(event.getTitle(), 30));
		sb.append(" @");
		
		if (!StringUtils.isEmpty(event.getRecurrenceRule())) {
			RRuleStringify.Strings strings = WT.getRRuleStringifyStrings(locale);
			RRuleStringify rrs = new RRuleStringify(strings, etz);
			sb.append(" (");
			sb.append(rrs.toHumanReadableFrequencyQuietly(event.getRecurrenceRule()));
			sb.append(")");
		}
		
		//TODO: solo l'orario se date coincidono!!!
		sb.append(" ");
		sb.append(fmt.print(event.getStartDate()));
		sb.append(" - ");
		sb.append(fmt.print(event.getEndDate()));
		
		return sb.toString();
	}
	
	public static String buildEventModificationTitle(Locale locale, EventFootprint event, String crud) {
		DateTimeZone etz = DateTimeZone.forID(event.getTimezone());
		StringBuilder sb = new StringBuilder();
		
		sb.append(StringUtils.abbreviate(event.getTitle(), 30));
		sb.append(" @");
		
		if (!StringUtils.isEmpty(event.getRecurrenceRule())) {
			RRuleStringify.Strings strings = WT.getRRuleStringifyStrings(locale);
			RRuleStringify rrs = new RRuleStringify(strings, etz);
			sb.append(" (");
			sb.append(rrs.toHumanReadableFrequencyQuietly(event.getRecurrenceRule()));
			sb.append(")");
		}
		
		String SUJECT_KEY = MessageFormatter.format(CalendarLocale.EMAIL_EVENTMODIFICATION_SUBJECT_X, crud).getMessage();
		return MessageFormat.format(WT.lookupResource(SERVICE_ID, locale, SUJECT_KEY), sb.toString());
	}
	
	public static String buildEventInvitationTitle(Locale locale, String dateFormat, String timeFormat, EventFootprint event, String crud) {
		String title = buildEventTitle(locale, dateFormat, timeFormat, event);
		String SUJECT_KEY = MessageFormat.format(CalendarLocale.EMAIL_INVITATION_SUBJECT_X, crud);
		return MessageFormat.format(WT.lookupResource(SERVICE_ID, locale, SUJECT_KEY), title);
	}
	
	public static String buildEventReminderTitle(Locale locale, String dateFormat, String timeFormat, EventFootprint event) {
		String title = buildEventTitle(locale, dateFormat, timeFormat, event);
		return MessageFormat.format(WT.lookupResource(SERVICE_ID, locale, CalendarLocale.EMAIL_REMINDER_SUBJECT), title);
	}
	
	public static String buildResponseUpdateTitle(Locale locale, Event event, EventAttendee attendee) {
		String pattern;
		if (EventAttendee.ResponseStatus.ACCEPTED.equals(attendee.getResponseStatus())) {
			pattern = WT.lookupResource(SERVICE_ID, locale, CalendarLocale.EMAIL_RESPONSEUPDATE_SUBJECT_ACCEPTED);
		} else if (EventAttendee.ResponseStatus.TENTATIVE.equals(attendee.getResponseStatus())) {
			pattern = WT.lookupResource(SERVICE_ID, locale, CalendarLocale.EMAIL_RESPONSEUPDATE_SUBJECT_TENTATIVE);
		} else if (EventAttendee.ResponseStatus.DECLINED.equals(attendee.getResponseStatus())) {
			pattern = WT.lookupResource(SERVICE_ID, locale, CalendarLocale.EMAIL_RESPONSEUPDATE_SUBJECT_DECLINED);
		} else {
			pattern = WT.lookupResource(SERVICE_ID, locale, CalendarLocale.EMAIL_RESPONSEUPDATE_SUBJECT_OTHER);
		}
		return MessageFormat.format(pattern, event.getTitle());
	}
	
	public static String buildEventModificationBody(EventFootprint event, Locale locale, String dateFormat, String timeFormat, Map<String, String> meetingProviders) throws IOException, TemplateException, AddressException {
		MapItem i18n = new MapItem();
		i18n.put("whenStart", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_START));
		i18n.put("whenEnd", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_END));
		i18n.put("where", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE));
		i18n.put("whereMap", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE_MAP));
		i18n.put("howToJoin", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_HOWTOJIN_MEETING));
		
		MapItem evt = new MapItem();
		fillEventDates(evt, event.getTimezone(), event.getStartDate(), event.getEndDate(), dateFormat, timeFormat);
		fillEventOccurs(evt, event.getTimezone(), event.getRecurrenceRule(), locale);
		fillEventLocation(evt, event.getLocation(), locale, meetingProviders); 
		
		MapItem vars = new MapItem();
		vars.put("i18n", i18n);
		vars.put("event", evt);
		
		return WT.buildTemplate(SERVICE_ID, "tpl/email/eventModification-body.html", vars);
	}
	
	public static String buildTplEventInvitationBody(String crud, Event event, String recipientEmail, Locale locale, String dateFormat, String timeFormat, Map<String, String> meetingProviders, String servicePublicUrl) throws IOException, TemplateException, AddressException {
		MapItem i18n = new MapItem();
		i18n.put("whenStart", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_START));
		i18n.put("whenEnd", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_END));
		i18n.put("where", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE));
		i18n.put("whereMap", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE_MAP));
		i18n.put("howToJoin", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_HOWTOJIN_MEETING));
		i18n.put("organizer", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_ORGANIZER));
		i18n.put("who", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHO));
		i18n.put("going", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING));
		i18n.put("goingToAll", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOINGTOALL));
		i18n.put("goingYes", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING_YES));
		i18n.put("goingMaybe", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING_MAYBE));
		i18n.put("goingNo", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_GOING_NO));
		i18n.put("view", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_VIEW));
		
		MapItem evt = new MapItem();
		evt.put("title", StringUtils.defaultIfBlank(event.getTitle(), ""));
		evt.put("description", StringUtils.defaultIfBlank(event.getDescription(), null));
		fillEventDates(evt, event.getTimezone(), event.getStartDate(), event.getEndDate(), dateFormat, timeFormat);
		fillEventOccurs(evt, event.getTimezone(), event.getRecurrenceRule(), locale);
		fillEventLocation(evt, event.getLocation(), locale, meetingProviders);
		evt.put("organizer", StringUtils.defaultIfBlank(event.getOrganizerCN(), event.getOrganizerAddress()));
		
		String recipientAttendeeId = null;
		MapItemList evtAtts = new MapItemList();
		for (EventAttendee attendee : event.getAttendees()) {
			MapItem item = new MapItem();
			String cn = null;
			String address = null;
			InternetAddress iaRecipient = attendee.getRecipientInternetAddress();
			
			if (iaRecipient != null) {
				cn = iaRecipient.getPersonal();
				address = iaRecipient.getAddress();
			}
			
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
		if(!StringUtils.equals(crud, "delete")) {
			vars.put("replyYesUrl", (recipientAttendeeId == null) ? null : CalendarManager.buildEventReplyPublicUrl(servicePublicUrl, event.getPublicUid(), recipientAttendeeId, "yes"));
			vars.put("replyMaybeUrl", (recipientAttendeeId == null) ? null : CalendarManager.buildEventReplyPublicUrl(servicePublicUrl, event.getPublicUid(), recipientAttendeeId, "maybe"));
			vars.put("replyNoUrl", (recipientAttendeeId == null) ? null : CalendarManager.buildEventReplyPublicUrl(servicePublicUrl, event.getPublicUid(), recipientAttendeeId, "no"));
			vars.put("viewUrl", viewUrl);
		}
		
		return WT.buildTemplate(SERVICE_ID, "tpl/email/eventInvitation-body.html", vars);
	}
	
	public static String buildTplResponseUpdateBody(Event event, Locale locale, String dateFormat, String timeFormat, Map<String, String> meetingProviders, String servicePublicUrl) throws IOException, TemplateException, AddressException {
		MapItem i18n = new MapItem();
		i18n.put("whenStart", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_START));
		i18n.put("whenEnd", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_END));
		i18n.put("where", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE));
		i18n.put("whereMap", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE_MAP));
		i18n.put("howToJoin", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_HOWTOJIN_MEETING));
		i18n.put("view", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_VIEW));
		
		MapItem evt = new MapItem();
		evt.put("title", StringUtils.defaultIfBlank(event.getTitle(), ""));
		fillEventDates(evt, event.getTimezone(), event.getStartDate(), event.getEndDate(), dateFormat, timeFormat);
		fillEventOccurs(evt, event.getTimezone(), event.getRecurrenceRule(), locale);
		fillEventLocation(evt, event.getLocation(), locale, meetingProviders);
		
		String viewUrl = CalendarManager.buildEventPublicUrl(servicePublicUrl, event.getPublicUid());
		
		MapItem vars = new MapItem();
		vars.put("i18n", i18n);
		vars.put("event", evt);
		vars.put("viewUrl", viewUrl);
		
		return WT.buildTemplate(SERVICE_ID, "tpl/email/responseUpdate-body.html", vars);
	}
	
	public static String buildEventInvitationHtml(Locale locale, String bodyHeader, String customBodyHtml, String source, String because, String recipientEmail, String crud) throws IOException, TemplateException {
		EmailNotification.BecauseBuilder builder = new EmailNotification.BecauseBuilder()
				.withCustomBody(bodyHeader, customBodyHtml);

		if (StringUtils.equals(crud, "update")) {
			builder.greenMessage(WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_MSG_UPDATED));
		} else if(StringUtils.equals(crud, "delete")) {
			builder.redMessage(WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_MSG_DELETED));
		}
		if (StringUtils.equals(crud, "create")) {
			builder.customFooterHeaderPattern(WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_HEADER));
		}
		
		String fooMsgPatt = builder.getDefaultFooterMessagePattern(locale);
		fooMsgPatt += "<br>"+WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_DISCLAIMER);
		builder.customFooterMessagePattern(fooMsgPatt);
		
		return builder.build(locale, source, because, recipientEmail).write();
	}
	
	public static String buildTplEventReminderBody(Event event, Locale locale, String dateFormat, String timeFormat, Map<String, String> meetingProviders) throws IOException, TemplateException, AddressException {
		MapItem i18n = new MapItem();
		i18n.put("whenStart", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_START));
		i18n.put("whenEnd", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHEN_END));
		i18n.put("where", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE));
		i18n.put("whereMap", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHERE_MAP));
		i18n.put("howToJoin", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_HOWTOJIN_MEETING));
		i18n.put("organizer", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_ORGANIZER));
		i18n.put("who", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_WHO));
		
		MapItem evt = new MapItem();
		evt.put("title", StringUtils.defaultIfBlank(event.getTitle(), ""));
		evt.put("description", StringUtils.defaultIfBlank(event.getDescription(), null));
		fillEventDates(evt, event.getTimezone(), event.getStartDate(), event.getEndDate(), dateFormat, timeFormat);
		fillEventLocation(evt, event.getLocation(), locale, meetingProviders);
		evt.put("organizer", StringUtils.defaultIfBlank(event.getOrganizerCN(), event.getOrganizerAddress()));
		
		MapItemList evtAtts = new MapItemList();
		for (EventAttendee attendee : event.getAttendees()) {
			MapItem item = new MapItem();
			InternetAddress iaRecipient = attendee.getRecipientInternetAddress();
			String cn = null;
			String address = null;
			
			if (iaRecipient != null) {
				cn = iaRecipient.getPersonal();
				address = iaRecipient.getAddress();
			}
			
			item.put("cn", StringUtils.isBlank(cn) ? null : cn);
			item.put("address", StringUtils.isBlank(address) ? null : address);
			evtAtts.add(item);
		}
		
		MapItem vars = new MapItem();
		vars.put("i18n", i18n);
		vars.put("event", evt);
		vars.put("eventAttendees", evtAtts);
		
		return WT.buildTemplate(SERVICE_ID, "tpl/email/eventInvitation-body.html", vars);
	}
	
	private static void fillEventDates(MapItem item, String eventTimezone, DateTime eventStart, DateTime eventEnd, String dateFormat, String timeFormat) {
		DateTimeZone etz = DateTimeZone.forID(eventTimezone);
		DateTimeFormatter fmt = JodaTimeUtils.createFormatter(dateFormat + " " + timeFormat, etz);
		item.put("timezone", eventTimezone);
		item.put("startDate", fmt.print(eventStart));
		item.put("endDate", fmt.print(eventEnd));
	}
	
	private static void fillEventOccurs(MapItem item, String eventTimezone, String eventRecurrenceRule, Locale locale) {
		DateTimeZone etz = DateTimeZone.forID(eventTimezone);
		item.put("occurs", null);
		if (!StringUtils.isBlank(eventRecurrenceRule)) {
			RRuleStringify.Strings strings = WT.getRRuleStringifyStrings(locale);
			RRuleStringify rrs = new RRuleStringify(strings, etz);
			item.put("occurs", rrs.toHumanReadableTextQuietly(eventRecurrenceRule));
		}
	}
	
	private static void fillEventLocation(MapItem item, String eventLocation, Locale locale, Map<String, String> meetingProviders) {
		String location = StringUtils.defaultIfBlank(eventLocation, null);
		String meetingProvider = findMatchingMeetingUrl(meetingProviders, location);
		if (meetingProvider != null) {
			item.put("meeting", location);
			item.put("meetingLinkName", location);
			item.put("meetingLinkUrl", StringUtils.defaultString(location));
			item.put("joinMeetingOn", WT.lookupFormattedResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_JOIN_MEETING, meetingProvider));
			
		} else {
			item.put("location", StringUtils.defaultIfBlank(location, null));
			item.put("locationUrl", TplHelper.buildGoogleMapsUrl(location));
		}
	}
	
	/*
	public static String buildInvitationTpl(Locale locale, String source, String recipientEmail, String bodyHeader, String customBodyHtml, String becauseString, String crud) throws IOException, TemplateException {
		MapItem notMap = new MapItem();
		notMap.putAll(createInvitationTplStrings(locale, source, recipientEmail, bodyHeader, customBodyHtml, becauseString, crud));
		MapItem map = new MapItem();
		map.put("recipientEmail", StringUtils.defaultString(recipientEmail));
		return NotificationHelper.builTpl(notMap, map);
	}
	
	public static Map<String, String> createInvitationTplStrings(Locale locale, String source, String recipientEmail, String bodyHeader, String customBody, String becauseString, String crud) {
		HashMap<String, String> map = new HashMap<>();
		if(StringUtils.equals(crud, "update")) {
			map.put("greenMessage", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_MSG_UPDATED));
		} else if(StringUtils.equals(crud, "delete")) {
			map.put("redMessage", WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_MSG_DELETED));
		}
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("customBody", StringUtils.defaultString(customBody));
		if(StringUtils.equals(crud, "create")) {
			map.put("footerHeader", MessageFormat.format(WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_HEADER), source));
		} else {
			map.put("footerHeader", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_HEADER), source));
		}
		String foomsg = MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_MESSAGE), recipientEmail, becauseString);
		foomsg += "<br>"+WT.lookupResource(SERVICE_ID, locale, CalendarLocale.TPL_EMAIL_INVITATION_FOOTER_DISCLAIMER);
		map.put("footerMessage", foomsg);
		return map;
	}
	
	public static String buildEventInvitationEmailSubject(Locale locale, String dateFormat, String timeFormat, Event event, String crud) {
		DateTimeZone etz = DateTimeZone.forID(event.getTimezone());
		DateTimeFormatter fmt = JodaTimeUtils.createFormatter(dateFormat + " " + timeFormat, etz);
		StringBuilder sb = new StringBuilder();
		
		sb.append(StringUtils.abbreviate(event.getTitle(), 30));
		sb.append(" @");
		
		if (!StringUtils.isEmpty(event.getRecurrenceRule())) {
			RRuleStringify.Strings strings = WT.getRRuleStringifyStrings(locale);
			RRuleStringify rrs = new RRuleStringify(strings, etz);
			sb.append(" (");
			sb.append(rrs.toHumanReadableFrequencyQuietly(event.getRecurrenceRule()));
			sb.append(")");
		}
		
		//TODO: solo l'orario se date coincidono!!!
		sb.append(" ");
		sb.append(fmt.print(event.getStartDate()));
		sb.append(" - ");
		sb.append(fmt.print(event.getEndDate()));
		
		String pattern = WT.lookupResource(SERVICE_ID, locale, MessageFormat.format(CalendarLocale.EMAIL_INVITATION_SUBJECT_X, crud));
		return NotificationHelper.buildSubject(locale, SERVICE_ID, MessageFormat.format(pattern, sb.toString()));
	}
	*/
	
	public static String buildGoogleMapsUrl(String s) {
		return "https://maps.google.com/maps?q=" + StringUtils.defaultString(LangUtils.encodeURL(s));
	}
}
