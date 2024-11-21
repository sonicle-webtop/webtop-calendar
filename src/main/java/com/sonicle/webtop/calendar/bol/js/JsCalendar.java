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
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarBase;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;
import org.joda.time.DateTimeZone;

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
	public Boolean isPrivate;
	public Boolean busy;
	public Integer reminder;
	public Boolean notifyOnExtUpdate;
	public String remoteUrl;
	public String remoteUsername;
	public String remotePassword;
	public Short remoteSyncFrequency;
	public String remoteLastSync;
	
	public JsCalendar(Calendar cal, DateTimeZone utz) {
		calendarId = cal.getCalendarId();
		domainId = cal.getDomainId();
		userId = cal.getUserId();
		builtIn = cal.getBuiltIn();
		provider = EnumUtils.toSerializedName(cal.getProvider());
		name = cal.getName();
		description = cal.getDescription();
		color = cal.getColor();
		sync = EnumUtils.toSerializedName(cal.getSync());
		isPrivate = cal.getIsPrivate();
		busy = cal.getDefaultBusy();
		reminder = cal.getDefaultReminder();
		notifyOnExtUpdate = cal.getNotifyOnExtUpdate();
		
		if (cal.isProviderRemote()) {
			CalendarRemoteParameters params = cal.getParametersAsObject(new CalendarRemoteParameters(), CalendarRemoteParameters.class);
			remoteUrl = URIUtils.toString(params.url);
			remoteUsername = params.username;
			remotePassword = params.password;
			remoteSyncFrequency = cal.getRemoteSyncFrequency();
			if (cal.getRemoteSyncTimestamp() != null) {
				remoteLastSync = DateTimeUtils.createYmdHmsFormatter(utz).print(cal.getRemoteSyncTimestamp());
			}
		}
	}
	
	public CalendarBase createCalendarForInsert() {
		return createCalendarForUpdate();
	}
	
	public CalendarBase createCalendarForUpdate() {
		CalendarBase item = new CalendarBase();
		item.setDomainId(domainId);
		item.setUserId(userId);
		item.setBuiltIn(builtIn);
		item.setProvider(EnumUtils.forSerializedName(provider, CalendarBase.Provider.class));
		item.setName(name);
		item.setDescription(description);
		item.setColor(color);
		item.setSync(EnumUtils.forSerializedName(sync, CalendarBase.Sync.class));
		item.setIsPrivate(isPrivate);
		item.setDefaultBusy(busy);
		item.setDefaultReminder(reminder);
		item.setNotifyOnExtUpdate(notifyOnExtUpdate);
		if (item.isProviderRemote()) {
			CalendarRemoteParameters params = new CalendarRemoteParameters();
			params.url = URIUtils.createURIQuietly(remoteUrl);
			params.username = remoteUsername;
			params.password = remotePassword;
			item.setParameters(LangUtils.serialize(params, CalendarRemoteParameters.class));
			item.setRemoteSyncFrequency(remoteSyncFrequency);
		}
		return item;
	}
}
