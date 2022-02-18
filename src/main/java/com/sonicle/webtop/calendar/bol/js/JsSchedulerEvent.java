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
package com.sonicle.webtop.calendar.bol.js;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.CalendarUtils;
import com.sonicle.webtop.calendar.TplHelper;
import com.sonicle.webtop.calendar.bol.model.MyShareRootCalendar;
import com.sonicle.webtop.calendar.bol.VVEventInstance;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import com.sonicle.webtop.calendar.model.ShareRootCalendar;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.core.model.Meeting;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsSchedulerEvent {
	public String id;
	public Integer eventId;
	public Integer originalEventId;
	public Integer calendarId;
	public String calendarName;
	public String startDate;
	public String endDate;
	public String timezone;
	public Boolean isAllDay;
	public String title;
	public String color;
	public String location;
	public String meeting;
	public Boolean isPrivate;
	public Integer reminder;
	public Boolean isReadOnly;
	public Boolean hideData;
	public Boolean hasTz;
	public Boolean hasAtts;
	public Boolean isNtf;
	public Boolean isRecurring;
	public Boolean isBroken;
	public Boolean hasCmts;
	public String tags;
	public String folderName;
	public String _owner;
	public String _rights;
	public String _profileId;
	public String description;
	
	public JsSchedulerEvent() {}
	
	public JsSchedulerEvent(ShareRootCalendar root, ShareFolderCalendar folder, CalendarPropSet folderProps, SchedEventInstance event, UserProfileId profileId, DateTimeZone profileTz, Pattern meetingUrlPattern) {
		DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter(profileTz);
		Calendar calendar = folder.getCalendar();
		
		// Determine if keep event data private
		/*
		boolean keepDataPrivate = false;
		if (event.getIsPrivate()) {
			if (!calendar.getProfileId().equals(profileId)) {
				keepDataPrivate = true;
			}
		}
		*/
		
		id = event.getKey();
		eventId = event.getEventId();
		originalEventId = event.getEventId();
		calendarId = event.getCalendarId();
		calendarName = calendar.getName();
		
		CalendarUtils.EventBoundary eventBoundary = CalendarUtils.toEventBoundaryForPreview(event.getAllDay(), event.getStartDate(), event.getEndDate(), event.getDateTimeZone());
		isAllDay = eventBoundary.allDay;
		startDate = ymdhmsZoneFmt.print(eventBoundary.start);
		endDate = ymdhmsZoneFmt.print(eventBoundary.end);
		timezone = event.getTimezone();
		
		title = event.getTitle();
		color = calendar.getColor();
		if (folderProps != null) color = folderProps.getColorOrDefault(color);
		location = event.getLocation();
		meeting = Meeting.extractMeetingUrl(meetingUrlPattern, event.getLocation(), event.getDescription());
		isPrivate = event.getIsPrivate();
		reminder = (event.getReminder() == null) ? -1 : event.getReminder().getMinutesValue();
		//TODO: gestire eventi readonly...(utenti admin devono poter editare)
		isReadOnly = calendar.isProviderRemote() || event.isCensorized(); // Maybe it's better to call this isLocked (no actions will be avail in client scheduler component)
		hasTz = !event.getDateTimeZone().getID().equals(profileTz.getID()) && !DateTimeUtils.isTimeZoneCompatible(event.getDateTimeZone(), profileTz, event.getStartDate());
		hasAtts = event.hasAttendees();
		isNtf = event.hasNotifyableAttendees();
		isRecurring = event.isEventRecurring();
		isBroken = event.isEventBroken();
		hasCmts = !StringUtils.isBlank(event.getDescription());
		tags = event.getTags();
		
		if (hasCmts) {
			description = event.getDescription().length() > 200 ? event.getDescription().substring(0, 200) : event.getDescription();
		}
		
		folderName = calendar.getName();
		_owner = (root instanceof MyShareRootCalendar) ? "" : root.getDescription();
		_rights = event.isCensorized() ? "" : ("m" + folder.getElementsPerms().toString());
		_profileId = calendar.getProfileId().toString();
	}
	
	public JsSchedulerEvent(ShareRootCalendar rootFolder, ShareFolderCalendar folder, CalendarPropSet folderProps, VVEventInstance event, UserProfileId profileId, DateTimeZone profileTz) {
		DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter(profileTz);
		Calendar calendar = folder.getCalendar();
		
		// Determine if keep event data private
		boolean keepDataPrivate = false;
		if (event.getIsPrivate()) {
			if (!calendar.getProfileId().equals(profileId)) {
				keepDataPrivate = true;
			}
		}
		
		id = event.getKey();
		eventId = event.getEventId();
		originalEventId = event.getEventId();
		calendarId = event.getCalendarId();
		calendarName = calendar.getName();
		
		// Source field is already in UTC, we need only to display it
		// in the timezone choosen by user in his settings.
		// Formatter will be instantiated specifying desired timezone.
		startDate = ymdhmsZoneFmt.print(event.getStartDate());
		endDate = ymdhmsZoneFmt.print(event.getEndDate());
		timezone = event.getTimezone();
		isAllDay = event.getAllDay();
		
		title = (keepDataPrivate) ? "***" : event.getTitle();
		color = calendar.getColor();
		if (folderProps != null) color = folderProps.getColorOrDefault(color);
		location = (keepDataPrivate) ? "" : event.getLocation();
		isPrivate = event.getIsPrivate();
		reminder = (event.getReminder() == null) ? -1 : event.getReminder();
		//TODO: gestire eventi readonly...(utenti admin devono poter editare)
		isReadOnly = event.getReadOnly() || keepDataPrivate;
		hasTz = !event.getDateTimeZone().getID().equals(profileTz.getID()) && !DateTimeUtils.isTimeZoneCompatible(event.getDateTimeZone(), profileTz, event.getStartDate());
		hasAtts = event.hasAttendees();
		isNtf = event.hasNotifyableAttendees();
		isRecurring = event.isEventRecurring();
		isBroken = event.isEventBroken();
		hasCmts = !StringUtils.isBlank(event.getDescription());
		
		if (!keepDataPrivate && hasCmts) {
			description = event.getDescription().length() > 200 ? event.getDescription().substring(0, 200) : event.getDescription();
		}
		
		folderName = calendar.getName();
		_owner = (rootFolder instanceof MyShareRootCalendar) ? "" : rootFolder.getDescription();
		_rights = folder.getElementsPerms().toString();
		_profileId = calendar.getProfileId().toString();
	}
	
	public static class Update {
		public String id;
		public String startDate;
		public String endDate;
		public String timezone;
		public String title;
	}
}
