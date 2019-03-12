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

import com.sonicle.commons.web.json.JsonResult;
import static com.sonicle.webtop.calendar.CalendarSettings.*;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.HashSet;
import org.joda.time.LocalTime;

/**
 *
 * @author malbinola
 */
public class CalendarUserSettings extends BaseUserSettings {
	private CalendarServiceSettings css;
	
	public CalendarUserSettings(String serviceId, UserProfileId profileId) {
		super(serviceId, profileId);
		css = new CalendarServiceSettings(serviceId, profileId.getDomainId());
	}
	
	 public String getView() {
		String value = getString(VIEW, null);
		if(value != null) return value;
		return css.getDefaultView();
	}
	
	public boolean setView(String value) {
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
	
	public String getEventReminderDelivery() {
		String value = getString(EVENT_REMINDER_DELIVERY, null);
		if(value != null) return value;
		return css.getDefaultEventReminderDelivery();
	}
	
	public boolean setEventReminderDelivery(String value) {
		return setString(EVENT_REMINDER_DELIVERY, value);
	}
	
	public InactiveRoots getInactiveCalendarRoots() {
		return getObject(INACTIVE_CALENDAR_ROOTS, new InactiveRoots(), InactiveRoots.class);
	}
	
	public boolean setInactiveCalendarRoots(InactiveRoots value) {
		return setObject(INACTIVE_CALENDAR_ROOTS, value, InactiveRoots.class);
	}
	
	public InactiveFolders getInactiveCalendarFolders() {
		return getObject(INACTIVE_CALENDAR_FOLDERS, new InactiveFolders(), InactiveFolders.class);
	}
	
	public boolean setInactiveCalendarFolders(InactiveFolders value) {
		return setObject(INACTIVE_CALENDAR_FOLDERS, value, InactiveFolders.class);
	}
	
	public static class InactiveRoots extends HashSet<String> {
		public InactiveRoots() {
			super();
		}
		
		public static InactiveRoots fromJson(String value) {
			return JsonResult.gson.fromJson(value, InactiveRoots.class);
		}
		
		public static String toJson(InactiveRoots value) {
			return JsonResult.gson.toJson(value, InactiveRoots.class);
		}
	}
	
	public static class InactiveFolders extends HashSet<Integer> {
		public InactiveFolders() {
			super();
		}
		
		public static InactiveFolders fromJson(String value) {
			return JsonResult.gson.fromJson(value, InactiveFolders.class);
		}
		
		public static String toJson(InactiveFolders value) {
			return JsonResult.gson.toJson(value, InactiveFolders.class);
		}
	}
}
