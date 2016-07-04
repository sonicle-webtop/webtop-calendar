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

/**
 *
 * @author malbinola
 */
public class CalendarSettings {
	
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
}
