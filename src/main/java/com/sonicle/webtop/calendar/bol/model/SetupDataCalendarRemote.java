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
package com.sonicle.webtop.calendar.bol.model;

import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.CalendarRemoteParameters;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.net.URI;

/**
 *
 * @author malbinola
 */
public class SetupDataCalendarRemote {
	protected String profileId;
	protected Calendar.Provider provider;
	protected String name;
	protected String color;
	protected URI url;
	protected String username;
	protected String password;
	protected Short syncFrequency;
	
	public SetupDataCalendarRemote() {}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}
	
	public Calendar.Provider getProvider() {
		return provider;
	}
	
	public void setProvider(Calendar.Provider provider) {
		this.provider = provider;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public Short getSyncFrequency() {
		return this.syncFrequency;
	}
	
	public void setSyncFrequency(Short syncFrequency) {
		this.syncFrequency = syncFrequency;
	}
	
	public Calendar toCalendar() {
		Calendar cal = new Calendar();
		if (profileId != null) cal.setProfileId(new UserProfileId(profileId));
		cal.setBuiltIn(false);
		cal.setProvider(provider);
		cal.setName(name);
		cal.setColor(color);
		cal.setSync(Calendar.Sync.OFF);
		cal.setParametersAsObject(toCalendarRemoteParameters(), CalendarRemoteParameters.class);
		cal.setRemoteSyncFrequency(syncFrequency);
		return cal;
	}
	
	public CalendarRemoteParameters toCalendarRemoteParameters() {
		CalendarRemoteParameters params = new CalendarRemoteParameters();
		params.url = url;
		params.username = username;
		params.password = password;
		return params;
	}
}
