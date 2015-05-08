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
package com.sonicle.webtop.calendar.bol.js;

import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsEventBase {
	public String id;
	
	public Integer eventId;
	public Integer calendarId;
	public Integer recurrenceId;
	public String startDate;
	public String endDate;
	public String timezone;
	public Boolean allDay;
	public String title;
	public String description;
	public String location;
	public Boolean isPrivate;
	public Boolean busy;
	public Integer reminder;
	
	public String rrEndsMode;
	public Integer rrRepeatTimes;
	public String rrUntilDate;
	public String rrType;
	public String rrDaylyType;
	public Integer rrDaylyFreq;
	public Integer rrWeeklyFreq;
	public Boolean rrWeeklyDay1;
	public Boolean rrWeeklyDay2;
	public Boolean rrWeeklyDay3;
	public Boolean rrWeeklyDay4;
	public Boolean rrWeeklyDay5;
	public Boolean rrWeeklyDay6;
	public Boolean rrWeeklyDay7;
	public Integer rrMonthlyFreq;
	public Integer rrMonthlyDay;
	public Integer rrYearlyFreq;
	public Integer rrYearlyDay;
	
	// Read-only fields
	public String _recurringInfo;
	public String _groupId;
	
	public static String toYmdHmsWithZone(DateTime dt, TimeZone tz) {
		return toYmdHmsWithZone(dt, DateTimeZone.forTimeZone(tz));
	}
	
	public static String toYmdHmsWithZone(DateTime dt, DateTimeZone tz) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(tz);
		return dtf.print(dt);
	}
	
	public static DateTime parseYmdHmsWithZone(String date, String time, TimeZone tz) {
		return parseYmdHmsWithZone(date, time, DateTimeZone.forTimeZone(tz));
	}
	
	public static DateTime parseYmdHmsWithZone(String date, String time, DateTimeZone tz) {
		return parseYmdHmsWithZone(date + " " + time, tz);
	}
	
	public static DateTime parseYmdHmsWithZone(String dateTime, DateTimeZone tz) {
		String dt = StringUtils.replace(dateTime, "T", " ");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(tz);
		return formatter.parseDateTime(dt);
	}
}
