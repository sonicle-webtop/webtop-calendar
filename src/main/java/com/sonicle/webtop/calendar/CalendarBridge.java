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
package com.sonicle.webtop.calendar;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.dal.CalendarDAO;
import com.sonicle.webtop.core.RunContext;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.BaseDAO.CrudInfo;
import com.sonicle.webtop.core.sdk.BaseBridge;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.Connection;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CalendarBridge extends BaseBridge {
	public static final Logger logger = WT.getLogger(CalendarBridge.class);
	
	public CalendarBridge(RunContext context) {
		super(context);
	}
	
	@Override
	public void initializeProfile(UserProfile.Id profileId) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			CalendarDAO cdao = CalendarDAO.getInstance();
			
			// Adds built-in calendar
			OCalendar cal = cdao.selectBuiltInByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			if(cal == null) {
				cal = new OCalendar();
				cal.setDomainId(profileId.getDomainId());
				cal.setUserId(profileId.getUserId());
				cal.setBuiltIn(true);
				cal.setName("WebTop");
				cal.setDescription("");
				cal.setColor("#FFFFFF");
				cal.setIsPrivate(false);
				cal.setBusy(false);
				cal.setReminder(null);
				cal.setSync(true);
				cal.setInvitation(false);
				cal.setIsDefault(true);
				cal.setBusy(false);
				cal.setCalendarId(cdao.getSequence(con).intValue());
				cdao.insert(con, cal, createUpdateInfo(profileId));
			}
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private CrudInfo createUpdateInfo(UserProfile.Id profileId) {
		return new CrudInfo("WT", profileId.toString());
	}
	
	@Override
	public void cleanupProfile(UserProfile.Id profileId, boolean deep) {
		//TODO: implementare cleanup utente
	}
}
