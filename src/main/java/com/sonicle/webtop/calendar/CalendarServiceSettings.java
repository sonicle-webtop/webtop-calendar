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

import static com.sonicle.webtop.calendar.CalendarSettings.*;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.core.sdk.BaseServiceSettings;
import org.joda.time.LocalTime;

/**
 *
 * @author malbinola
 */
public class CalendarServiceSettings extends BaseServiceSettings {

	public CalendarServiceSettings(String serviceId, String domainId) {
		super(serviceId, domainId);
	}
	
	public boolean getCalendarRemoteAutoSyncEnabled() {
		return getBoolean(CALENDAR_REMOTE_AUTOSYNC_ENABLED, false);
	}
	
	public boolean getCalendarRemoteAutoSyncOnlyWhenOnline() {
		return getBoolean(CALENDAR_REMOTE_AUTOSYNC_ONLYWHENONLINE, true);
	}
	
	public boolean getDavCalendarDeleteEnabled() {
		return getBoolean(DAV_CALENDAR_DELETE_ENABLED, false);
	}
	
	public boolean getEventStatisticFieldsVisible() {
        return getBoolean(EVENT_STATISTIC_FIELDS_VISIBLE, true);
    }
	
	public Calendar.Sync getDefaultCalendarSync() {
		return getEnum(DEFAULT_PREFIX + CALENDAR_SYNC, Calendar.Sync.OFF, Calendar.Sync.class);
	}
	
	public String getDefaultView() {
		return getString(DEFAULT_PREFIX + VIEW, "w5");
	}
	
	public Integer getDefaultSchedulerTimeResolution() {
		return getInteger(DEFAULT_PREFIX + SCHEDULER_TIMERESOLUTION, 30);
	}
	
	public LocalTime getDefaultWorkdayStart() {
		return getTime(DEFAULT_PREFIX + WORKDAY_START, "09:00", "HH:mm");
	}
	
	public LocalTime getDefaultWorkdayEnd() {
		return getTime(DEFAULT_PREFIX + WORKDAY_END, "18:00", "HH:mm");
	}
	
	public String getDefaultEventReminderDelivery() {
		return getString(DEFAULT_PREFIX + EVENT_REMINDER_DELIVERY, EVENT_REMINDER_DELIVERY_APP);
	}
}
