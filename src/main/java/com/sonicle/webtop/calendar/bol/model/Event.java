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

import com.sonicle.webtop.calendar.ICal4jUtils;
import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.VSchedulerEvent;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author malbinola
 */
public class Event {
	protected String key;
	protected RecurringInfo recurringInfo; // Readonly
	protected String calendarProfileId; // Readonly
	protected Integer eventId;
	protected String publicUid;
	protected Integer calendarId;
	protected DateTime startDate;
	protected DateTime endDate;
	protected String timezone;
	protected Boolean allDay;
	protected String title;
	protected String description;
	protected String location;
	protected Boolean isPrivate;
	protected Boolean busy;
	protected Integer reminder;
	protected Integer activityId;
	protected String customerId;
	protected String statisticId;
	protected Integer causalId;
	protected Recurrence recurrence;
	protected List<EventAttendee> attendees = new ArrayList<>();
	
	public Event() {
		
	}
	
	public Event(String key, RecurringInfo recurringInfo, VSchedulerEvent sevent) {
		this();
		this.key = key;
		this.recurringInfo = recurringInfo;
		this.calendarProfileId = new UserProfile.Id(sevent.getCalendarDomainId(), sevent.getCalendarUserId()).toString();
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String value) {
		key = value;
	}
	
	public RecurringInfo getRecurringInfo() {
		return recurringInfo;
	}
	
	public Integer getEventId() {
		return eventId;
	}
	
	public void setEventId(Integer value) {
		eventId = value;
	}
	
	public String getPublicUid() {
		return publicUid;
	}
	
	public void setPublicUid(String value) {
		publicUid = value;
	}
	
	public Integer getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Integer value) {
		calendarId = value;
	}
	
	public String getCalendarProfileId() {
		return calendarProfileId;
	}

	public DateTime getStartDate() {
		return startDate;
	}
	
	public void setStartDate(DateTime value) {
		startDate = value;
	}

	public DateTime getEndDate() {
		return endDate;
	}
	
	public void setEndDate(DateTime value) {
		endDate = value;
	}

	public String getTimezone() {
		return timezone;
	}
	
	public void setTimezone(String value) {
		timezone = value;
	}

	public Boolean getAllDay() {
		return allDay;
	}
	
	public void setAllDay(boolean value) {
		allDay = value;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String value) {
		title = value;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String value) {
		description = value;
	}

	public String getLocation() {
		return location;
	}
	
	public void setLocation(String value) {
		location = value;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}
	
	public void setIsPrivate(boolean value) {
		isPrivate = value;
	}

	public Boolean getBusy() {
		return busy;
	}
	
	public void setBusy(boolean value) {
		busy = value;
	}

	public Integer getReminder() {
		return reminder;
	}
	
	public void setReminder(Integer value) {
		reminder = value;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getStatisticId() {
		return statisticId;
	}

	public void setStatisticId(String statisticId) {
		this.statisticId = statisticId;
	}

	public Integer getCausalId() {
		return causalId;
	}

	public void setCausalId(Integer causalId) {
		this.causalId = causalId;
	}
	
	public Recurrence getRecurrence() {
		return recurrence;
	}
	
	public void setRecurrence(Recurrence value) {
		recurrence = value;
	}
	
	public List<EventAttendee> getAttendees() {
		return attendees;
	}
	
	public void setAttendees(List<EventAttendee> value) {
		attendees = value;
	}
	
	public boolean hasRecurrence() {
		return (recurrence != null);
	}
	
	public boolean hasAttendees() {
		return ((attendees != null) && !attendees.isEmpty());
	}
	
	public EventAttendee getOrganizer() {
		if(!hasAttendees()) return null;
		for(EventAttendee attendee : getAttendees()) {
			if(attendee.getRecipientType().equals(EventAttendee.RECIPIENT_TYPE_ORGANIZER)) return attendee;
		}
		return null;
	}
	
	public static enum RecurringInfo {
		SINGLE {
			@Override
			public String toString() {
				return "single";
			}
		},
		BROKEN {
			@Override
			public String toString() {
				return "broken";
			}
		},
		RECURRING {
			@Override
			public String toString() {
				return "recurring";
			}
		}
	}
}
