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
package com.sonicle.webtop.calendar.bol.model;

import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.core.util.JRHelper;
import java.awt.Image;
import java.util.Date;

/**
 *
 * @author malbinola
 */
public class RBAgendaEvent {
	public Integer calendarId;
	public String calendarName;
	public String calendarColor;
	public Image calendarColorImage;
	public String eventKey;
	public Integer eventId;
	public Date startDate;
	public Date endDate;
	public Boolean allDay;
	public String timezone;
	public String title;
	public String location;
	public Integer spanLeft;
	public Integer spanRight;
	
	public RBAgendaEvent(OCalendar calendar, SchedulerEventInstance event, Integer spanLeft, Integer spanRight) {
		this.calendarId = calendar.getCalendarId();
		this.calendarName = calendar.getName();
		this.calendarColor = calendar.getHexColor();
		this.calendarColorImage = JRHelper.colorAsImage(calendar.getHexColor());
		this.eventKey = event.getKey();
		this.eventId = event.getEventId();
		this.startDate = event.getStartDate().toDate();
		this.endDate = event.getEndDate().toDate();
		this.timezone = event.getTimezone();
		this.allDay = event.getAllDay();
		this.title = event.getTitle();
		this.location = event.getLocation();
		this.spanLeft = spanLeft;
		this.spanRight = spanRight;
	}
	
	public Integer getCalendarId() {
		return calendarId;
	}

	public String getCalendarName() {
		return calendarName;
	}

	public String getCalendarColor() {
		return calendarColor;
	}
	
	public Image getCalendarColorImage() {
		return calendarColorImage;
	}

	public String getEventKey() {
		return eventKey;
	}

	public Integer getEventId() {
		return eventId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getTimezone() {
		return timezone;
	}

	public Boolean getAllDay() {
		return allDay;
	}

	public String getTitle() {
		return title;
	}

	public String getLocation() {
		return location;
	}

	public Integer getSpanLeft() {
		return spanLeft;
	}

	public Integer getSpanRight() {
		return spanRight;
	}
}
