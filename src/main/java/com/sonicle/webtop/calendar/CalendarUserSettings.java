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
	private CalendarServiceSettings css;
	
	public CalendarUserSettings(String serviceId, UserProfile.Id profileId) {
		super(serviceId, profileId);
		css = new CalendarServiceSettings(serviceId);
	}
	
	/**
	 * [string][default]
	 * Calendar view (d:day, w:week, w5:workweek, m:month)
	 */
	public static final String VIEW = "view";
	
	/**
	 * [string][default]
	 * Workday hours start time
	 */
	public static final String WORKDAY_START = "workday.start";
	
	/**
	 * [string][default]
	 * Workday hours end time
	 */
	public static final String WORKDAY_END = "workday.end";
	
	/**
	 * [boolean]
	 * Workday hours end time
	 */
	public static final String REMINDER_BY_EMAIL = "reminder.byemail";
	
	/**
	 * [string]
	 * Selected folder root node.
	 */
	public static final String SELECTED_CALENDAR_ROOTS = "calendar.roots.selected";
	
	/**
	 * [string[]]
	 * List of checked (or visible) folder root nodes.
	 */
	public static final String CHECKED_CALENDAR_ROOTS = "calendar.roots.checked";
	
	/**
	 * [int[]]
	 * List of checked (or visible) calendars.
	 */
	public static final String CHECKED_CALENDAR_FOLDERS = "calendar.folders.checked";
	
	
	public String getCalendarView() {
		String value = getString(VIEW, null);
		if(value != null) return value;
		return css.getDefaultView();
	}
	
	public boolean setCalendarView(String value) {
		return setString(VIEW, value);
	}
	
	public LocalTime getWorkdayStart() {
		LocalTime value = getTime(WORKDAY_START, (LocalTime)null, "HH:mm");
		if(value != null) return value;
		return css.getDefaultWorkdayStart();
	}
	
	public boolean setWorkdayStart(LocalTime value) {
		return setTime(WORKDAY_START, value, "HH:mm");
	}
	
	public LocalTime getWorkdayEnd() {
		LocalTime value = getTime(WORKDAY_END, (LocalTime)null, "HH:mm");
		if(value != null) return value;
		return css.getDefaultWorkdayEnd();
	}
	
	public boolean setWorkdayEnd(LocalTime value) {
		return setTime(WORKDAY_END, value, "HH:mm");
	}
	
	public Boolean getReminderByEmail() {
		return getBoolean(REMINDER_BY_EMAIL, css.getDefaultReminderByEmail());
	}
	
	public boolean setReminderByEmail(Boolean value) {
		return setBoolean(REMINDER_BY_EMAIL, value);
	}
	
	/*
	public String getSelectedRoot() {
		return getString(SELECTED_CALENDAR_ROOTS, null);
	}
	
	public boolean setSelectedRoot(String value) {
		return setString(SELECTED_CALENDAR_ROOTS, value);
	}
	*/
	
	public CheckedRoots getCheckedCalendarRoots() {
		return getObject(CHECKED_CALENDAR_ROOTS, new CheckedRoots(), CheckedRoots.class);
	}
	
	public boolean setCheckedCalendarRoots(CheckedRoots value) {
		return setObject(CHECKED_CALENDAR_ROOTS, value, CheckedRoots.class);
	}
	
	public CheckedFolders getCheckedCalendarFolders() {
		return getObject(CHECKED_CALENDAR_FOLDERS, new CheckedFolders(), CheckedFolders.class);
	}
	
	public boolean setCheckedCalendarFolders(CheckedFolders value) {
		return setObject(CHECKED_CALENDAR_FOLDERS, value, CheckedFolders.class);
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
