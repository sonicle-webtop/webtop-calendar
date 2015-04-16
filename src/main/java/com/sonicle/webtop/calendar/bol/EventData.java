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

import com.sonicle.webtop.calendar.ICal4jUtils;
import org.joda.time.DateTime;

/**
 *
 * @author malbinola
 */
public class EventData {
	
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
	
	public Integer calendarId;
	public DateTime startDate;
	public DateTime endDate;
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
	public DateTime rrUntilDate;
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
	
	public EventData() {
		rrType = TYPE_NONE;
		rrDaylyType = DAILY_TYPE_DAY;
		rrEndsMode = ENDS_MODE_NEVER;
	}
	
	public void fillFrom(OEvent evt) {
		calendarId = evt.getCalendarId();
		startDate = evt.getStartDate();
		endDate = evt.getEndDate();
		timezone = evt.getTimezone();
		allDay = evt.getAllDay();
		title = evt.getTitle();
		description = evt.getDescription();
		location = evt.getLocation();
		isPrivate = evt.getIsPrivate();
		busy = evt.getBusy();
		reminder = evt.getReminder();
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
			rrDaylyType = DAILY_TYPE_FERIALI;
		} else {
			if(rec.getType().equals(TYPE_DAILY)) {
				rrType = TYPE_DAILY;
				rrDaylyType = DAILY_TYPE_DAY;
			} else {
				rrType = rec.getType();
				rrDaylyType = null;
			}
		}
		rrDaylyFreq = rec.getDaylyFreq();
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
	}
	
	public boolean hasRecurrence(EventData data) {
		return !data.rrType.equals(EventData.TYPE_NONE);
	}
}
