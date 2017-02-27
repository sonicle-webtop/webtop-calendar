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
package com.sonicle.webtop.calendar.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.Sync;

/**
 *
 * @author malbinola
 */
public class JsCalendar {
	public Integer calendarId;
	public String domainId;
	public String userId;
	public Boolean builtIn;
	public String name;
	public String description;
	public String color;
	public String sync;
	public Boolean isPrivate;
	public Boolean isDefault;
	public Boolean busy;
	public Integer reminder;
	public Boolean invitation;
	
	public JsCalendar(Calendar cal) {
		calendarId = cal.getCalendarId();
		domainId = cal.getDomainId();
		userId = cal.getUserId();
		builtIn = cal.getBuiltIn();
		name = cal.getName();
		description = cal.getDescription();
		color = cal.getColor();
		sync = EnumUtils.getValue(cal.getSync());
		isPrivate = cal.getIsPrivate();
		isDefault = cal.getIsDefault();
		busy = cal.getDefaultBusy();
		reminder = cal.getDefaultReminder();
		invitation = cal.getDefaultSendInvitation();
	}
	
	public static Calendar buildFolder(JsCalendar js) {
		Calendar cat = new Calendar();
		cat.setCalendarId(js.calendarId);
		cat.setDomainId(js.domainId);
		cat.setUserId(js.userId);
		cat.setBuiltIn(js.builtIn);
		cat.setName(js.name);
		cat.setDescription(js.description);
		cat.setColor(js.color);
		cat.setSync(EnumUtils.forValue(Sync.class, js.sync));
		cat.setIsPrivate(js.isPrivate);
		cat.setIsDefault(js.isDefault);
		cat.setDefaultBusy(js.busy);
		cat.setDefaultReminder(js.reminder);
		cat.setDefaultSendInvitation(js.invitation);
		return cat;
	}
}
