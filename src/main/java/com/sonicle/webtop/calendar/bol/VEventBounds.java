/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.bol;

import com.sonicle.webtop.core.util.ICal4jUtils;
import net.fortuna.ical4j.model.Recur;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class VEventBounds {
	protected String eventId;
	protected String seriesEventId;
	protected String seriesInstanceId;
	protected String publicId;
	protected Integer calendarId;
	protected DateTime start;
	protected DateTime end;
	protected String timezone;
	protected Boolean allDay;
	protected DateTime recurrenceStart;
	protected String recurrenceRule;
	protected String calendarDomainId;
	protected String calendarUserId;

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getSeriesEventId() {
		return seriesEventId;
	}

	public void setSeriesEventId(String seriesEventId) {
		this.seriesEventId = seriesEventId;
	}

	public String getSeriesInstanceId() {
		return seriesInstanceId;
	}

	public void setSeriesInstanceId(String seriesInstanceId) {
		this.seriesInstanceId = seriesInstanceId;
	}

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public Integer getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Integer calendarId) {
		this.calendarId = calendarId;
	}

	public DateTime getStart() {
		return start;
	}

	public void setStart(DateTime start) {
		this.start = start;
	}

	public DateTime getEnd() {
		return end;
	}

	public void setEnd(DateTime end) {
		this.end = end;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public Boolean getAllDay() {
		return allDay;
	}

	public void setAllDay(Boolean allDay) {
		this.allDay = allDay;
	}

	public DateTime getRecurrenceStart() {
		return recurrenceStart;
	}

	public void setRecurrenceStart(DateTime recurrenceStart) {
		this.recurrenceStart = recurrenceStart;
	}

	public String getRecurrenceRule() {
		return recurrenceRule;
	}

	public void setRecurrenceRule(String recurrenceRule) {
		this.recurrenceRule = recurrenceRule;
	}

	public String getCalendarDomainId() {
		return calendarDomainId;
	}

	public void setCalendarDomainId(String calendarDomainId) {
		this.calendarDomainId = calendarDomainId;
	}

	public String getCalendarUserId() {
		return calendarUserId;
	}

	public void setCalendarUserId(String calendarUserId) {
		this.calendarUserId = calendarUserId;
	}
	
	public DateTimeZone getTimezoneObject() {
		return DateTimeZone.forID(getTimezone());
	}
	
	public boolean hasRecurrence() {
		return getRecurrenceStart() != null;
	}
	
	public Recur getRecurrenceObject() {
		return ICal4jUtils.parseRRule(getRecurrenceRule());
	}
}
