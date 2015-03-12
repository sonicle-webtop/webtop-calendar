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
package com.sonicle.webtop.calendar.bol;

import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.bol.js.JsSchedulerEvent1;
import com.sonicle.webtop.calendar.jooq.tables.pojos.Events;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public class OEvent extends Events {
	
	public OEvent() {
		super();
	}
	
	public OEvent(JsEvent js, String workdayStart, String workdayEnd) {
		super();
		setEventId(js.eventId);
		setCalendarId(js.calendarId);
		
		// Adjust times
		if(js.allDay) {
			js.fromTime = MessageFormat.format("{0}:00", workdayStart);
			js.toTime = MessageFormat.format("{0}:00", workdayEnd);
		}
		
		// Incoming fields are in precise timezone, so we need to instantiate
		// the formatter specifying the right timezone to use. Then DateTime
		// objects are automatically translated to UTC
		DateTimeZone etz = DateTimeZone.forID(js.timezone);
		setFromDate(parseYmdHmsWithZone(js.fromDate, etz));
		setToDate(parseYmdHmsWithZone(js.toDate, etz));
		
		setTitle(js.title);
		setAllDay(js.allDay);
		setTimezone(js.timezone);
		setLocation(js.location);
	}
	
	public void updateDates(String fromDate, String toDate, TimeZone userTz) {
		DateTimeZone utz = DateTimeZone.forTimeZone(userTz);
		DateTimeZone etz = DateTimeZone.forID(getTimezone());
		setFromDate(parseYmdHmsWithZone(fromDate, utz).toDateTime(etz));
		setToDate(parseYmdHmsWithZone(toDate, utz).toDateTime(etz));
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
	
	public static DateTime parseYmdHmWithZone(String date, String time, DateTimeZone tz) {
		return parseYmdHmsWithZone(date, time + ":00", tz);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	private void initWithJoda(JsEvent js) {
		DateTimeZone utc = DateTimeZone.UTC;
		DateTimeZone etz = DateTimeZone.forID(js.timezone);
		DateTimeZone utz = DateTimeZone.forTimeZone(TimeZone.getDefault());
		
		DateTime dtFrom = joinDatePartsWithJoda(js.fromDate, js.fromTime, etz);
		setFromDate(dtFrom);
		//setFrom(dtFrom.toDateTime(utz));
		
		DateTime dtTo = joinDatePartsWithJoda(js.toDate, js.toTime, etz);
		setToDate(dtTo);
		//setTo(dtTo.toDateTime(utz));
	}
	
	private DateTime joinDatePartsWithJoda(String date, String time, DateTimeZone tz) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(tz);
		return formatter.parseDateTime(date + " " + time);
	}
	
	private void initWithJava(JsEvent js) {
		
	}
	
	private Date joinDatePartsWithJava(String date, String time, TimeZone tz) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sdf.setTimeZone(tz);
			return sdf.parse(date + " " + time);
			
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}	
	}
	*/
}
