/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.bol.js;

import com.sonicle.commons.flags.BitFlagsEnum;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.webtop.calendar.CalendarUtils;
import com.sonicle.webtop.calendar.bol.model.MyCalendarFSOrigin;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarFSFolder;
import com.sonicle.webtop.calendar.model.CalendarFSOrigin;
import com.sonicle.webtop.calendar.model.CalendarPropSet;
import com.sonicle.webtop.calendar.model.EventBounds;
import com.sonicle.webtop.calendar.model.EventLookupInstance;
import com.sonicle.webtop.core.sdk.UserProfileId;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsGridEvent {
	public String id;
	public String oid;
	public String calendarId;
	public String calendarName;
	public String color;
	public String org;
	public Boolean allDay;
	public String start;
	public String end;
	public String timezone;
	public String title;
	public String location;
	public Boolean isPrivate;
	public String tags;
	public Long flags;
	public String _owPid;
	public String _orDN;
	public String _foPerms;
	public String _itPerms;
	
	public JsGridEvent(CalendarFSOrigin origin, CalendarFSFolder folder, CalendarPropSet folderProps, EventLookupInstance event, UserProfileId profileId, DateTimeZone profileTz) {
		DateTimeFormatter ymdhmsZoneFmt = JodaTimeUtils.createFormatterYMDHMS(profileTz);
		Calendar calendar = folder.getCalendar();
		
		id = event.getId().toString();
		oid = event.getOriginalEventId();
		calendarId = String.valueOf(event.getCalendarId());
		calendarName = calendar.getName();
		color = calendar.getColor();
		if (folderProps != null) color = folderProps.getColorOrDefault(color);
		org = event.getOrganizerCN();
		
		EventBounds eventBoundary = CalendarUtils.toEventBounds(event);
		allDay = eventBoundary.isAllDay();
		start = ymdhmsZoneFmt.print(eventBoundary.getStart());
		end = ymdhmsZoneFmt.print(eventBoundary.getEnd());
		timezone = event.getTimezone();
		
		title = event.getTitle();
		location = event.getLocation();
		isPrivate = event.isVisibilityPrivate();
		tags = event.getTags();
		
		flags = JsSchedulerEvent.computeFlags(calendar, event, profileTz);
		_owPid = calendar.getProfileId().toString();
		_orDN = (origin instanceof MyCalendarFSOrigin) ? "" : origin.getDisplayName();
        _foPerms = folder.getPermissions().getFolderPermissions().toString();
		_itPerms = folder.getPermissions().getItemsPermissions().toString();
		//_rights = event.isCensorized() ? "" : ("m" + folder.getPermissions().getItemsPermissions().toString());
	}
}
