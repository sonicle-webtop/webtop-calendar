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
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;

/**
 *
 * @author malbinola
 */
public class JsCalendar {
	public Integer calendarId;
	public String domainId;
	public String userId;
	public Boolean builtIn;
	public String provider;
	public String name;
	public String description;
	public String color;
	public String sync;
	public Boolean isDefault;
	public Boolean isPrivate;
	public Boolean busy;
	public Integer reminder;
	public Boolean invitation;
	public String remoteUrl;
	public String remoteUsername;
	public String remotePassword;
	
	public JsCalendar(Calendar cal) {
		calendarId = cal.getCalendarId();
		domainId = cal.getDomainId();
		userId = cal.getUserId();
		builtIn = cal.getBuiltIn();
		provider = EnumUtils.toSerializedName(cal.getProvider());
		name = cal.getName();
		description = cal.getDescription();
		color = cal.getColor();
		sync = EnumUtils.toSerializedName(cal.getSync());
		isDefault = cal.getIsDefault();
		isPrivate = cal.getIsPrivate();
		busy = cal.getDefaultBusy();
		reminder = cal.getDefaultReminder();
		invitation = cal.getDefaultSendInvitation();
		
		CalendarRemoteParameters params = LangUtils.deserialize(cal.getParameters(), new CalendarRemoteParameters(), CalendarRemoteParameters.class);
		remoteUrl = URIUtils.toString(params.url);
		remoteUsername = params.username;
		remotePassword = params.password;
	}
	
	public static Calendar createCalendar(JsCalendar js, String origParameters) {
		Calendar cal = new Calendar();
		cal.setCalendarId(js.calendarId);
		cal.setDomainId(js.domainId);
		cal.setUserId(js.userId);
		cal.setBuiltIn(js.builtIn);
		cal.setProvider(EnumUtils.forSerializedName(js.provider, Calendar.Provider.class));
		cal.setName(js.name);
		cal.setDescription(js.description);
		cal.setColor(js.color);
		cal.setSync(EnumUtils.forSerializedName(js.sync, Calendar.Sync.class));
		cal.setIsDefault(js.isDefault);
		cal.setIsPrivate(js.isPrivate);
		cal.setDefaultBusy(js.busy);
		cal.setDefaultReminder(js.reminder);
		cal.setDefaultSendInvitation(js.invitation);
		
		if (cal.isProviderRemote()) {
			CalendarRemoteParameters params = LangUtils.deserialize(origParameters, new CalendarRemoteParameters(), CalendarRemoteParameters.class);
			params.url = URIUtils.createURIQuietly(js.remoteUrl);
			params.username = js.remoteUsername;
			params.password = js.remotePassword;
			cal.setParameters(LangUtils.serialize(params, CalendarRemoteParameters.class));
		} else {
			cal.setParameters(null);
		}
		
		return cal;
	}
}
