/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.bol;

import com.sonicle.webtop.core.sdk.UserProfileId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class VExpEvent {
	protected String eventId;
	protected Integer calendarId;
	protected Integer recurrenceId;
	protected DateTime startDate;
	protected DateTime endDate;
	protected String timezone;
	protected Boolean allDay;
	protected String title;
	protected Integer reminder;
	protected DateTime remindedOn;
	protected String calendarDomainId;
	protected String calendarUserId;
	protected String seriesEventId;

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public Integer getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Integer calendarId) {
		this.calendarId = calendarId;
	}

	public Integer getRecurrenceId() {
		return recurrenceId;
	}

	public void setRecurrenceId(Integer recurrenceId) {
		this.recurrenceId = recurrenceId;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
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
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getReminder() {
		return reminder;
	}

	public void setReminder(Integer reminder) {
		this.reminder = reminder;
	}

	public DateTime getRemindedOn() {
		return remindedOn;
	}

	public void setRemindedOn(DateTime remindedOn) {
		this.remindedOn = remindedOn;
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

	public String getSeriesEventId() {
		return seriesEventId;
	}

	public void setSeriesEventId(String seriesEventId) {
		this.seriesEventId = seriesEventId;
	}
	
	public DateTimeZone getDateTimeZone() {
		return DateTimeZone.forID(getTimezone());
	}
	
	public UserProfileId getCalendarProfileId() {
		return new UserProfileId(calendarDomainId, calendarUserId);
	}
}
