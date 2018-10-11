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
package com.sonicle.webtop.calendar.bol;

import com.sonicle.webtop.core.sdk.UserProfileId;

/**
 *
 * @author malbinola
 */
public class VVEvent extends OEvent {
	protected String calendarDomainId;
	protected String calendarUserId;
	protected Integer seriesEventId;
	protected Integer attendeesCount;
	protected Integer notifyableAttendeesCount;
	
	public VVEvent() {
		super();
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
	
	public UserProfileId getCalendarProfileId() {
		return new UserProfileId(calendarDomainId, calendarUserId);
	}
	
	public Integer getSeriesEventId() {
		return seriesEventId;
	}

	public void setSeriesEventId(Integer seriesEventId) {
		this.seriesEventId = seriesEventId;
	}
	
	public Integer getAttendeesCount() {
		return attendeesCount;
	}

	public void setAttendeesCount(Integer attendeesCount) {
		this.attendeesCount = attendeesCount;
	}
	
	public Integer getNotifyableAttendeesCount() {
		return notifyableAttendeesCount;
	}

	public void setNotifyableAttendeesCount(Integer notifyableAttendeesCount) {
		this.notifyableAttendeesCount = notifyableAttendeesCount;
	}

	public boolean hasAttendees() {
		return (attendeesCount != null) && (attendeesCount > 0);
	}
	
	public boolean hasNotifyableAttendees() {
		return (notifyableAttendeesCount != null) && (notifyableAttendeesCount > 0);
	}
	
	public boolean isEventRecurring() {
		return getRecurrenceId() != null;
	}
	
	public boolean isEventBroken() {
		return getSeriesEventId() != null;
		/*
		if (isEventRecurring()) {
			// Checking seriesEventId is enought but we prevent any problems
			// returning false in case of recurring events!
			return false;
		} else {
			return getSeriesEventId() != null;
		}
		*/
	}
}
