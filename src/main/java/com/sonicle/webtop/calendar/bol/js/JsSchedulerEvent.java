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

import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.OEvent;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsSchedulerEvent {
	
	public Integer eventId;
	public Integer calendarId;
	public String startDate;
	public String endDate;
	public String timezone;
	public Boolean isAllDay;
	public String title;
	public String color;
	public String location;
	public Boolean isPrivate;
	public String reminder;
	public String notes = "";
	public String url = "";
	
	public JsSchedulerEvent() {
		
	}
	
	public JsSchedulerEvent(OEvent event, OCalendar calendar, TimeZone userTz) {
		eventId = event.getEventId();
		calendarId = event.getCalendarId();
		
		// Source field is already in UTC, we need only to display it
		// in the timezone choosen by user in his settings.
		// Formatter will be instantiated specifying desired timezone.
		startDate = toYmdHmsWithZone(event.getStartDate(), userTz);
		endDate = toYmdHmsWithZone(event.getEndDate(), userTz);
		timezone = (userTz.getID().equals(event.getTimezone())) ? null : event.getTimezone();
		isAllDay = event.getAllDay();
		
		title = (!event.getIsPrivate()) ? event.getTitle() : "***";
		color = calendar.getColor();
		location = event.getLocation();
		isPrivate = event.getIsPrivate();
		//TODO: gestire eventi readonly...(utenti admin devono poter editare)
		//isReadOnly = event.getReadonly();
	}
	
	public static String toYmdHmsWithZone(DateTime dt, TimeZone tz) {
		return toYmdHmsWithZone(dt, DateTimeZone.forTimeZone(tz));
	}
	
	public static String toYmdHmsWithZone(DateTime dt, DateTimeZone tz) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(tz);
		return dtf.print(dt);
	}
	
	public static String toYmdWithZone(DateTime dt, TimeZone tz) {
		return toYmdWithZone(dt, DateTimeZone.forTimeZone(tz));
	}
	
	public static String toYmdWithZone(DateTime dt, DateTimeZone tz) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(tz);
		return dtf.print(dt);
	}
	
	
	
	
	
	
	
	
	/*
	private void jdkTime(OEvent event, OCalendar calendar, TimeZone userTz) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(userTz);
		
		start = sdf.format(event.getFrom());
		end = sdf.format(event.getTo());
		tz = (userTz.getID().equals(event.getTimezone())) ? null : event.getTimezone();
		
	}
	
	private void jodaTime(OEvent event, OCalendar calendar, TimeZone userTz) {
		DateTimeZone utc = DateTimeZone.UTC;
		DateTimeZone etz = DateTimeZone.forID(event.getTimezone());
		DateTimeZone utz = DateTimeZone.forTimeZone(userTz);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(utz);
		start = formatter.print(event.getFrom());
		end = formatter.print(event.getTo());
		tz = (userTz.getID().equals(event.getTimezone())) ? null : event.getTimezone();
	}
	
	
	
	
	
	public final Date convert(Timestamp ts, TimeZone tsTz, TimeZone newTz) {
		return convert2(ts, tsTz, newTz);
		//return convert(ts, DateTimeZone.forTimeZone(tsTz), DateTimeZone.forTimeZone(newTz));
	}
	
	public final Date convert(Timestamp ts, DateTimeZone tsTz, DateTimeZone newTz) {
		DateTime from = new DateTime(ts.getTime(), tsTz);
		return from.toDateTime(newTz).toDate();
	}
	
	public Date convert2(Timestamp ts, TimeZone fromTz, TimeZone toTz) {
		Calendar fromCalendar = new GregorianCalendar();
		fromCalendar.setTimeZone(fromTz);
		fromCalendar.setTimeInMillis(ts.getTime());
		
		Calendar toCalendar = new GregorianCalendar();
		toCalendar.setTimeZone(toTz);
		toCalendar.setTimeInMillis(fromCalendar.getTimeInMillis());
		
		if(toTz.inDaylightTime(toCalendar.getTime()) && fromTz.hasSameRules(toTz)) {
			toCalendar.setTimeInMillis(fromCalendar.getTimeInMillis() + toTz.getDSTSavings());
		}
		return toCalendar.getTime();
	}
	*/
}
