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

import com.sonicle.webtop.core.sdk.BaseUserSettings;

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
	/**
	 * [int]
	 * Calendar start day (0:sunday, 1:monday)
	 */
	public static final String START_DAY = "startDay";
	/**
	 * [string]
	 * Workday hours start time
	 */
	public static final String WORKDAY_START = "workdayStart";
	/**
	 * [string]
	 * Workday hours end time
	 */
	public static final String WORKDAY_END = "workdayEnd";
	
	public String getCalendarView() {
		return getUserSetting(VIEW, "w5");
	}
	
	public boolean setCalendarView(String value) {
		return setUserSetting(VIEW, value);
	}
	
	public Integer getCalendarStartDay() {
		return getUserSetting(START_DAY, 1);
	}
	
	public boolean setCalendarStartDay(Integer value) {
		return setUserSetting(START_DAY, value);
	}
	
	public String getWorkdayStart() {
		//TODO: verificare formattazione
		return getUserSetting(WORKDAY_START, "09:00");
	}
	
	public boolean setWorkdayStart(String value) {
		return setUserSetting(WORKDAY_START, value);
	}
	
	public String getWorkdayEnd() {
		//TODO: verificare formattazione
		return getUserSetting(WORKDAY_END, "18:00");
	}
	
	public boolean setWorkdayEnd(String value) {
		return setUserSetting(WORKDAY_END, value);
	}
}
