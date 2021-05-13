/*
 * Copyright (C) 2017 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2017 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.bol.js;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.TplHelper;
import com.sonicle.webtop.calendar.bol.model.MyShareRootCalendar;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import com.sonicle.webtop.calendar.model.ShareRootCalendar;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.core.model.Meeting;
import java.util.Map;
import java.util.regex.Pattern;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsPletEvents {
	public String id;
	public Integer eventId;
	public Integer calendarId;
	public String calendarName;
    public String calendarColor;
	public String startDate;
	public String endDate;
	public String timezone;
	public Boolean isAllDay;
	public String title;
	public String location;
	public String meeting;
	public String _owner;
	public String _frights;
	public String _erights;
	
	public JsPletEvents(ShareRootCalendar root, ShareFolderCalendar folder, SchedEventInstance event, DateTimeZone profileTz, Pattern meetingUrlPattern) {
		DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter(profileTz);
		final Calendar calendar = folder.getCalendar();
		
		id = event.getKey();
		eventId = event.getEventId();
		calendarId = event.getCalendarId();
		calendarName = calendar.getName();
		calendarColor = calendar.getColor();
		
		// Source field is already in UTC, we need only to display it
		// in the timezone choosen by user in his settings.
		// Formatter will be instantiated specifying desired timezone.
		startDate = ymdhmsZoneFmt.print(event.getStartDate());
		endDate = ymdhmsZoneFmt.print(event.getEndDate());
		timezone = event.getTimezone();
		isAllDay = event.getAllDay();
		
		title = event.getTitle();
		location = event.getLocation();
		meeting = Meeting.extractMeetingUrl(meetingUrlPattern, event.getLocation(), event.getDescription());
		
		_owner = (root instanceof MyShareRootCalendar) ? "" : root.getDescription();
		_frights = folder.getElementsPerms().toString();
		_erights = folder.getElementsPerms().toString();
	}
}
