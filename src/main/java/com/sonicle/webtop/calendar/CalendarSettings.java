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
package com.sonicle.webtop.calendar;

/**
 *
 * @author malbinola
 */
public class CalendarSettings {
	
	/**
	 * [system+domain]
	 * [boolean](false)
	 * Enable/Disable remote calendars auto-sync functionality. Defaults to `false`.
	 */
	public static final String CALENDAR_REMOTE_AUTOSYNC_ENABLED = "calendar.remote.autosync.enabled";
	
	/**
	 * [system+domain]
	 * [boolean](true)
	 * Enable/Disable remote auto-sync only when calendar's owner is online. Defaults to `true`.
	 */
	public static final String CALENDAR_REMOTE_AUTOSYNC_ONLYWHENONLINE = "calendar.remote.autosync.onlywhenonline";
	
	/**
	 * [system+domain]
	 * [boolean](false)
	 * Enable/Disable calendars deletions through DAV rest-api interface. Defaults to `false`.
	 */
	public static final String DAV_CALENDAR_DELETE_ENABLED = "dav.calendar.delete.enabled";
	
	/**
	 * [][default]
	 * [enum {O:OFF, R:READ, W:WRITE}] (O)
	 * The default value of the sync field for new calendars.
	 */
	public static final String CALENDAR_SYNC = "calendar.sync";
	
	/**
	 * [user][default]
	 * [string]
	 * Calendar view ("d" day, "w5" work week, "w" week, "dw" double-week, "m" month)
	 */
	public static final String VIEW = "view";
	
	/**
	 * [user][default]
	 * [string]
	 * Workday hours start time
	 */
	public static final String WORKDAY_START = "workday.start";
	
	/**
	 * [user][default]
	 * [string]
	 * Workday hours end time
	 */
	public static final String WORKDAY_END = "workday.end";
	
	/**
	 * [user][default]
	 * [string]
	 * Set anniversary reminder delivery mode
	 */
	public static final String EVENT_REMINDER_DELIVERY = "event.reminder.delivery";
	public static final String EVENT_REMINDER_DELIVERY_APP = "app";
	public static final String EVENT_REMINDER_DELIVERY_EMAIL = "email";
	
	/**
	 * [user]
	 * [string]
	 * Selected folder root node.
	 */
	public static final String SELECTED_CALENDAR_ROOTS = "calendar.roots.selected";
	
	/**
	 * [user]
	 * [string[]]
	 * List of checked (or visible) folder root nodes.
	 */
	public static final String CHECKED_CALENDAR_ROOTS = "calendar.roots.checked";
	
	/**
	 * [user]
	 * [int[]]
	 * List of checked (or visible) calendars.
	 */
	public static final String CHECKED_CALENDAR_FOLDERS = "calendar.folders.checked";
	
	/**
	 * [user]
	 * [int[]]
	 * List of checked (or visible) calendars.
	 */
	public static final String VISIBLE_STATIC_FIELDS_EVENT = "event.statistic.fields.visible";
}
