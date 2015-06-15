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
	public static final String TYPE_NONE = "_";
	public static final String TYPE_DAILY = "D";
	public static final String TYPE_DAILY_FERIALI = "F";
	public static final String TYPE_WEEKLY = "W";
	public static final String TYPE_MONTHLY = "M";
	public static final String TYPE_YEARLY = "Y";
	public static final String DAILY_TYPE_DAY = "1";
	public static final String DAILY_TYPE_FERIALI = "2";
	public static final String ENDS_MODE_NEVER = "never";
	public static final String ENDS_MODE_REPEAT = "repeat";
	public static final String ENDS_MODE_UNTIL = "until";
	
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
	//protected Recurrence recurrence;
	protected List<EventAttendee> attendees = new ArrayList<>();
	public String rrEndsMode;
	public Integer rrRepeatTimes;
	public DateTime rrUntilDate;
	public String rrType;
	public String rrDailyType;
	public Integer rrDailyFreq;
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
	public String rrRule;
	
	public Event() {
		rrType = TYPE_NONE;
		rrDailyType = DAILY_TYPE_DAY;
		rrEndsMode = ENDS_MODE_NEVER;
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
	
	/*
	public Recurrence getRecurrence() {
		return recurrence;
	}
	
	public void setRecurrence(Recurrence value) {
		recurrence = value;
	}
	*/
	
	public List<EventAttendee> getAttendees() {
		return attendees;
	}
	
	public void setAttendees(List<EventAttendee> value) {
		attendees = value;
	}
	
	public void fillFrom(ORecurrence rec) {
		rrUntilDate = rec.getUntilDate();
		if(rec.getRepeat() != null) {
			rrEndsMode = ENDS_MODE_REPEAT;
			rrRepeatTimes = rec.getRepeat();
		} else {
			rrRepeatTimes = null;
			if(rrUntilDate.compareTo(ICal4jUtils.ifiniteDate()) == 0) {
				rrEndsMode = ENDS_MODE_NEVER;
			} else {
				rrEndsMode = ENDS_MODE_UNTIL;
			}
		}
		if(rec.getType().equals(TYPE_DAILY_FERIALI)) {
			rrType = TYPE_DAILY;
			rrDailyType = DAILY_TYPE_FERIALI;
		} else {
			if(rec.getType().equals(TYPE_DAILY)) {
				rrType = TYPE_DAILY;
				rrDailyType = DAILY_TYPE_DAY;
			} else {
				rrType = rec.getType();
				rrDailyType = null;
			}
		}
		rrDailyFreq = rec.getDailyFreq();
		rrWeeklyFreq = rec.getWeeklyFreq();
		rrWeeklyDay1 = rec.getWeeklyDay_1();
		rrWeeklyDay2 = rec.getWeeklyDay_2();
		rrWeeklyDay3 = rec.getWeeklyDay_3();
		rrWeeklyDay4 = rec.getWeeklyDay_4();
		rrWeeklyDay5 = rec.getWeeklyDay_5();
		rrWeeklyDay6 = rec.getWeeklyDay_6();
		rrWeeklyDay7 = rec.getWeeklyDay_7();
		rrMonthlyFreq = rec.getMonthlyFreq();
		rrMonthlyDay = rec.getMonthlyDay();
		rrYearlyFreq = rec.getYearlyFreq();
		rrYearlyDay = rec.getYearlyDay();
		rrRule = rec.getRule();
	}
	
	public static boolean hasRecurrence(Event event) {
		return !event.rrType.equals(Event.TYPE_NONE);
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
