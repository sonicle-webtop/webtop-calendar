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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import java.util.ArrayList;
import java.util.HashSet;
import org.joda.time.LocalTime;

/**
 *
 * @author malbinola
 */
public class CalendarUserSettings extends BaseUserSettings {
	
	public CalendarUserSettings(String domainId, String userId, String serviceId) {
        super(domainId, userId, serviceId);
    }
	
	/**
	 * [string]
	 * Calendar view (d:day, w:week, w5:workweek, m:month)
	 */
	public static final String VIEW = "view";
	public static final String DEFAULT_VIEW = "w5";
	/**
	 * [int]
	 * Calendar start day (0:sunday, 1:monday)
	 */
	public static final String START_DAY = "startday";
	public static final int DEFAULT_START_DAY = 1;
	/**
	 * [string]
	 * Workday hours start time
	 */
	public static final String WORKDAY_START = "workday.start";
	public static final String DEFAULT_WORKDAY_START = "09:00";
	/**
	 * [string]
	 * Workday hours end time
	 */
	public static final String WORKDAY_END = "workday.end";
	public static final String DEFAULT_WORKDAY_END = "18:00";
	/**
	 * [string]
	 * Selected calendar group.
	 */
	public static final String SELECTED_CALENDAR_GROUP = "calendargroups.selected";
	/**
	 * [string[]]
	 * List of checked (or visible) calendar groups.
	 */
	public static final String CHECKED_CALENDAR_GROUPS = "calendargroups.checked";
	
	/**
	 * [int[]]
	 * List of checked (or visible) calendars.
	 */
	public static final String CHECKED_CALENDARS = "calendars.checked";
	
	
	public String getCalendarView() {
		return getString(VIEW, DEFAULT_VIEW);
	}
	
	public boolean setCalendarView(String value) {
		return setString(VIEW, value);
	}
	
	public Integer getCalendarStartDay() {
		return getInteger(START_DAY, DEFAULT_START_DAY);
	}
	
	public boolean setCalendarStartDay(Integer value) {
		return setInteger(START_DAY, value);
	}
	
	public LocalTime getWorkdayStart() {
		return getTime(WORKDAY_START, DEFAULT_WORKDAY_START, "HH:mm");
	}
	
	public boolean setWorkdayStart(String value) {
		return setString(WORKDAY_START, value);
	}
	
	public LocalTime getWorkdayEnd() {
		return getTime(WORKDAY_END, DEFAULT_WORKDAY_END, "HH:mm");
	}
	
	public boolean setWorkdayEnd(String value) {
		return setString(WORKDAY_END, value);
	}
	
	public String getSelectedCalendarGroup() {
		return getString(SELECTED_CALENDAR_GROUP, null);
	}
	
	public boolean setSelectedCalendarGroup(String value) {
		return setString(SELECTED_CALENDAR_GROUP, value);
	}
	
	public CheckedCalendarGroups getCheckedCalendarGroups() {
		return getObject(CHECKED_CALENDAR_GROUPS, new CheckedCalendarGroups(), CheckedCalendarGroups.class);
	}
	
	public boolean setCheckedCalendarGroups(CheckedCalendarGroups value) {
		return setObject(CHECKED_CALENDAR_GROUPS, value, CheckedCalendarGroups.class);
	}
	
	public CheckedCalendars getCheckedCalendars() {
		return getObject(CHECKED_CALENDARS, new CheckedCalendars(), CheckedCalendars.class);
	}
	
	public boolean setCheckedCalendars(CheckedCalendars value) {
		return setObject(CHECKED_CALENDARS, value, CheckedCalendars.class);
	}
	
	public static class CheckedCalendarGroups extends HashSet<String> {
		public CheckedCalendarGroups() {
			super();
		}
		
		public static CheckedCalendarGroups fromJson(String value) {
			return JsonResult.gson.fromJson(value, CheckedCalendarGroups.class);
		}
		
		public static String toJson(CheckedCalendarGroups value) {
			return JsonResult.gson.toJson(value, CheckedCalendarGroups.class);
		}
	}
	
	public static class CheckedCalendars extends HashSet<Integer> {
		public CheckedCalendars() {
			super();
		}
		
		public static CheckedCalendars fromJson(String value) {
			return JsonResult.gson.fromJson(value, CheckedCalendars.class);
		}
		
		public static String toJson(CheckedCalendars value) {
			return JsonResult.gson.toJson(value, CheckedCalendarGroups.class);
		}
	}
}
