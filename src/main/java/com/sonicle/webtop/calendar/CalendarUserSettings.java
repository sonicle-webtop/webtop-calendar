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

import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.util.HashSet;
import org.joda.time.LocalTime;

/**
 *
 * @author malbinola
 */
public class CalendarUserSettings extends BaseUserSettings {
	
	public CalendarUserSettings(UserProfile.Id profileId, String serviceId) {
		super(profileId, serviceId);
	}
	
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
	 * [boolean]
	 * Workday hours end time
	 */
	public static final String REMINDER_BY_EMAIL = "reminder.byemail";
	public static final Boolean DEFAULT_REMINDER_BY_EMAIL = false;
	
	/**
	 * [string]
	 * Selected folder root node.
	 */
	public static final String SELECTED_ROOT = "roots.selected";
	
	/**
	 * [string[]]
	 * List of checked (or visible) folder root nodes.
	 */
	public static final String CHECKED_ROOTS = "roots.checked";
	
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
	
	public LocalTime getWorkdayStart() {
		return getTime(WORKDAY_START, DEFAULT_WORKDAY_START, "HH:mm");
	}
	
	public boolean setWorkdayStart(LocalTime value) {
		return setTime(WORKDAY_START, value, "HH:mm");
	}
	
	public LocalTime getWorkdayEnd() {
		return getTime(WORKDAY_END, DEFAULT_WORKDAY_END, "HH:mm");
	}
	
	public boolean setWorkdayEnd(LocalTime value) {
		return setTime(WORKDAY_END, value, "HH:mm");
	}
	
	public Boolean getReminderByEmail() {
		return getBoolean(REMINDER_BY_EMAIL, DEFAULT_REMINDER_BY_EMAIL);
	}
	
	public boolean setReminderByEmail(Boolean value) {
		return setBoolean(REMINDER_BY_EMAIL, value);
	}
	
	public String getSelectedRoot() {
		return getString(SELECTED_ROOT, null);
	}
	
	public boolean setSelectedRoot(String value) {
		return setString(SELECTED_ROOT, value);
	}
	
	public CheckedRoots getCheckedRoots() {
		return getObject(CHECKED_ROOTS, new CheckedRoots(), CheckedRoots.class);
	}
	
	public boolean setCheckedRoots(CheckedRoots value) {
		return setObject(CHECKED_ROOTS, value, CheckedRoots.class);
	}
	
	public CheckedFolders getCheckedFolders() {
		return getObject(CHECKED_CALENDARS, new CheckedFolders(), CheckedFolders.class);
	}
	
	public boolean setCheckedFolders(CheckedFolders value) {
		return setObject(CHECKED_CALENDARS, value, CheckedFolders.class);
	}
	
	public static class CheckedRoots extends HashSet<String> {
		public CheckedRoots() {
			super();
		}
		
		public static CheckedRoots fromJson(String value) {
			return JsonResult.gson.fromJson(value, CheckedRoots.class);
		}
		
		public static String toJson(CheckedRoots value) {
			return JsonResult.gson.toJson(value, CheckedRoots.class);
		}
	}
	
	public static class CheckedFolders extends HashSet<Integer> {
		public CheckedFolders() {
			super();
		}
		
		public static CheckedFolders fromJson(String value) {
			return JsonResult.gson.fromJson(value, CheckedFolders.class);
		}
		
		public static String toJson(CheckedFolders value) {
			return JsonResult.gson.toJson(value, CheckedFolders.class);
		}
	}
}
