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

import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.WT;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author malbinola
 */
public class TplHelper {
	
	public static Map<String, String> generateFooterI18nStrings(String serviceId, Locale locale, String source, String recipientEmail, String becauseString) {
		HashMap<String, String> map = new HashMap<>();
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_FOOTER_HEADER), source));
		map.put("footerMessage", MessageFormat.format(WT.lookupCoreResource(locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_MESSAGE), recipientEmail, becauseString));
		map.put("footerDisclaimer", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_FOOTER_DISCLAIMER));
		return map;
	}
	
	public static Map<String, String> generateEventI18nStrings(String serviceId, Locale locale) {
		HashMap<String, String> map = new HashMap<>();
		map.put("whenStart", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_WHEN_START));
		map.put("whenEnd", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_WHEN_END));
		map.put("where", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_WHERE));
		map.put("whereMap", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_WHERE_MAP));
		map.put("calendar", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_CALENDAR));
		map.put("who", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_WHO));
		map.put("whoOrganizer", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_WHO_ORGANIZER));
		map.put("going", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_GOING));
		map.put("goingYes", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_GOING_YES));
		map.put("goingMaybe", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_GOING_MAYBE));
		map.put("goingNo", WT.lookupResource(serviceId, locale, CalendarLocale.TPL_INVITATION_GOING_NO));
		return map;
	}
}
