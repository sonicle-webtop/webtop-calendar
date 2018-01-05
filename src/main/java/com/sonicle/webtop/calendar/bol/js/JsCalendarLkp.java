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

import com.sonicle.webtop.calendar.bol.model.CalendarFolderData;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.ShareFolderCalendar;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class JsCalendarLkp {
	public Integer calendarId;
	public String domainId;
	public String userId;
	public String name;
	public String color;
	public Boolean isPrivate;
	public Boolean isDefault;
	public Boolean busy;
	public Integer reminder;
	public Boolean invitation;
	public Boolean _writable;
	
	public JsCalendarLkp(ShareFolderCalendar folder) {
		final Calendar cal = folder.getCalendar();
		calendarId = cal.getCalendarId();
		domainId = cal.getDomainId();
		userId = cal.getUserId();
		name = cal.getName();
		color = cal.getColor();
		isPrivate = cal.getIsPrivate();
		isDefault = cal.getIsDefault();
		busy = cal.getDefaultBusy();
		reminder = cal.getDefaultReminder();
		invitation = cal.getDefaultSendInvitation();
		if (folder.getData() != null) {
			CalendarFolderData data = (CalendarFolderData)folder.getData();
			if (!StringUtils.isBlank(data.color)) color = data.color;
		}
		_writable = folder.getElementsPerms().implies("CREATE");
	}
}
