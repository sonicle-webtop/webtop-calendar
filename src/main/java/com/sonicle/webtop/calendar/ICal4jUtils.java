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

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryImpl;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class ICal4jUtils {
	
	private final static TimeZoneRegistry tzRegistry = new TimeZoneRegistryImpl();
	
	public static org.joda.time.DateTime calculateRecurrenceStart(org.joda.time.DateTime eventStart, RRule rrule, org.joda.time.DateTimeZone tz) {
		return calculateRecurrenceStart(eventStart, rrule.getRecur(), tz);
	}
	
	public static org.joda.time.DateTime calculateRecurrenceStart(org.joda.time.DateTime eventStart, Recur recur, org.joda.time.DateTimeZone tz) {
		Date d = recur.getNextDate(ICal4jUtils.toICal4jDateTime(eventStart, tz), ICal4jUtils.toICal4jDateTime(eventStart.minusDays(1), tz));
		return toJodaDateTime(d, tz);
	}
	
	public static org.joda.time.DateTime calculateRecurrenceEnd(org.joda.time.DateTime eventStart, org.joda.time.DateTime eventEnd, RRule rr, org.joda.time.DateTimeZone tz) {
		VEvent vevent = new VEvent(ICal4jUtils.toICal4jDateTime(eventStart, tz), ICal4jUtils.toICal4jDateTime(eventEnd, tz), "");
		vevent.getProperties().add(rr);
		PeriodList periods = vevent.calculateRecurrenceSet(ICal4jUtils.toICal4jPeriod(eventStart, ifiniteDate(), tz));
		if((periods == null) || periods.isEmpty()) return null;
		Period last = (Period)periods.toArray()[periods.size()-1];
		return toJodaDateTime(last.getEnd(), tz);
	}
	
	public static PeriodList calculateRecurrenceSet(org.joda.time.DateTime eventStart, org.joda.time.DateTime eventEnd, org.joda.time.DateTime recStart, RRule rr, org.joda.time.DateTime from, org.joda.time.DateTime to, org.joda.time.DateTimeZone tz) {
		org.joda.time.DateTime start, end;
		
		if(eventStart.isEqual(recStart)) {
			start = eventStart;
			end = eventEnd;
		} else {
			int eventDays = org.joda.time.Days.daysBetween(eventStart.toLocalDate(), eventEnd.toLocalDate()).getDays();
			start = eventStart.withDate(recStart.toLocalDate());
			end = eventEnd.withDate(start.plusDays(eventDays).toLocalDate());
		}
		
		VEvent vevent = new VEvent(ICal4jUtils.toICal4jDateTime(start, tz), ICal4jUtils.toICal4jDateTime(end, tz), "");
		vevent.getProperties().add(rr);
		return vevent.calculateRecurrenceSet(ICal4jUtils.toICal4jPeriod(from, to, tz));
	}
	
	public static org.joda.time.DateTime toJodaDateTime(DateTime date) {
		return new org.joda.time.DateTime(date, DateTimeZone.forID(date.getTimeZone().getID()));
	}
	
	public static org.joda.time.DateTime toJodaDateTime(Date date, org.joda.time.DateTimeZone tz) {
		return new org.joda.time.DateTime(date, tz);
	}
	
	public static Period toICal4jPeriod(org.joda.time.DateTime start, org.joda.time.DateTime end, org.joda.time.DateTimeZone tz) {
		return new Period(toICal4jDateTime(start, tz), toICal4jDateTime(end, tz));
	}
	
	public static DateTime toICal4jDateTime(org.joda.time.DateTime dateTime, org.joda.time.DateTimeZone timezone) {
		DateTime dt = new DateTime(dateTime.toDate());
		if (timezone != null) {
			dt.setTimeZone(tzRegistry.getTimeZone(timezone.getID()));
		}
		return dt;
	}
	
	public static org.joda.time.DateTime ifiniteDate() {
		return ifiniteDate(org.joda.time.DateTimeZone.UTC);
	}
	
	public static org.joda.time.DateTime ifiniteDate(org.joda.time.DateTimeZone tz) {
		return new org.joda.time.DateTime(2100, 12, 31, 0, 0, 0, tz);
	}
	
	/*
	public static void calculateRecurrenceStart(VEvent event) {
		final DtStart start = (DtStart) event.getProperty(Property.DTSTART);
		DateProperty end = (DateProperty) event.getProperty(Property.DTEND);
		Duration duration = (Duration) event.getProperty(Property.DURATION);
		
		final DateTime startMinusDuration = new DateTime(period.getStart());
	}
	
	public static void calculateRecurrenceStart(RRule rrule, org.joda.time.DateTime startDate, org.joda.time.DateTime endDate, org.joda.time.DateTimeZone timezone) {
		
		DateTime start = toICalDate(startDate, timezone);
		DateTime end = toICalDate(endDate, timezone);
		Dur dur = new Dur(start, end);
		DateTime startMinusDuration = start;
		startMinusDuration.setTime(dur.negate().getTime(start).getTime());
		
		final Value startValue = (Value) start.getParameter(Parameter.VALUE);
		
		final DateList rruleDates = rrule.getRecur().getDates(start.getDate(), new Period(startMinusDuration, end), startValue);
	}
	*/
}
