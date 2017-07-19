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

import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.interfaces.IControllerHandlesProfiles;
import com.sonicle.webtop.core.sdk.interfaces.IControllerHandlesReminders;
import java.util.List;
import org.joda.time.DateTime;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CalendarController extends BaseController implements IControllerHandlesProfiles, IControllerHandlesReminders {
	public static final Logger logger = WT.getLogger(CalendarController.class);
	
	public CalendarController() {
		super();
	}
	
	@Override
	public void addProfile(UserProfileId profileId) throws WTException {
		CalendarManager manager = new CalendarManager(true, profileId);
		
		// Adds built-in calendar
		try {
			Calendar cal = manager.addBuiltInCalendar();
			if (cal != null) setCategoryCheckedState(profileId, cal.getCalendarId(), true);
		} catch(WTException ex) {
			throw ex;
		}
	}
	
	@Override
	public void removeProfile(UserProfileId profileId, boolean deep) throws WTException {
		CalendarManager manager = new CalendarManager(false, profileId);
		manager.eraseData(deep);
	}
	
	@Override
	public void upgradeProfile(UserProfileId profileId, ServiceVersion current, ServiceVersion lastSeen) throws WTException {
		
	}

	@Override
	public List<BaseReminder> returnReminders(DateTime now) {
		CalendarManager manager = new CalendarManager(true, RunContext.getRunProfileId());
		return manager.getRemindersToBeNotified(now);
	}
	
	private void setCategoryCheckedState(UserProfileId profileId, int calendarId, boolean checked) {
		CalendarUserSettings tus = new CalendarUserSettings(SERVICE_ID, profileId);
		CalendarUserSettings.CheckedFolders cf = tus.getCheckedCalendarFolders();
		if (checked) {
			cf.add(calendarId);
		} else {
			cf.remove(calendarId);
		}
		tus.setCheckedCalendarFolders(cf);
	}
}
