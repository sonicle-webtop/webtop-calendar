/*
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.bg;

import com.sonicle.webtop.calendar.BackgroundService;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.CalendarServiceSettings;
import com.sonicle.webtop.calendar.ConcurrentSyncException;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.sdk.BaseBackgroundServiceTask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class RemoteCalendarSyncTask extends BaseBackgroundServiceTask {
	private static final Logger LOGGER = (Logger)LoggerFactory.getLogger(RemoteCalendarSyncTask.class);
	
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public void executeWork(JobExecutionContext jec, TaskContext context) throws Exception {
		BackgroundService bs = ((BackgroundService)getBackgroundService(jec));
		SessionManager sesMgr = WebTopApp.getInstance().getSessionManager();
		CalendarManager calMgr = (CalendarManager)WT.getServiceManager(bs.SERVICE_ID);
		
		Map<String, Boolean> crseCache = new HashMap<>();
		Map<String, Boolean> rsowoCache = new HashMap<>();
		List<Calendar> cals = calMgr.listRemoteCalendarsToBeSynchronized();
		for (Calendar cal : cals) {
			if (shouldStop()) break; // Speed-up shutdown process!
			if (!isCalendarRemoteSyncEnabled(bs.SERVICE_ID, crseCache, cal.getDomainId())) continue; // Skip if sync is disabled!
			if (isRemoteSyncOnlyWhenOnline(bs.SERVICE_ID, rsowoCache, cal.getDomainId()) && !sesMgr.isOnline(cal.getProfileId())) continue; // Skip offline profiles!

			LOGGER.debug("Checking calendar [{}, {}]", cal.getCalendarId(), cal.getName());
			if (isSyncNeeded(cal, context.getExecuteInstant())) {
				LOGGER.debug("Sync required. Last sync at: {}", cal.getRemoteSyncTimestamp());
				try {
					CalendarManager manager = (CalendarManager)WT.getServiceManager(bs.SERVICE_ID, true, cal.getProfileId());
					manager.syncRemoteCalendar(cal.getCalendarId(), false);
				} catch (ConcurrentSyncException ex1) {
					LOGGER.trace("Remote sync skipped", ex1);
				} catch (Throwable t) {
					LOGGER.error("Unable to run remote sync", t);
				}
			} else {
				LOGGER.debug("Sync not needed. Last sync at: {}", cal.getRemoteSyncTimestamp());
			}
		}
	}
	
	private boolean isCalendarRemoteSyncEnabled(String serviceId, Map<String, Boolean> cache, String domainId) {
		if (!cache.containsKey(domainId)) {
			CalendarServiceSettings css = new CalendarServiceSettings(serviceId, domainId);
			cache.put(domainId, css.getCalendarRemoteAutoSyncEnabled());
		}
		return cache.get(domainId);
	}
	
	private boolean isRemoteSyncOnlyWhenOnline(String serviceId, Map<String, Boolean> cache, String domainId) {
		if (!cache.containsKey(domainId)) {
			CalendarServiceSettings css = new CalendarServiceSettings(serviceId, domainId);
			cache.put(domainId, css.getCalendarRemoteAutoSyncOnlyWhenOnline());
		}
		return cache.get(domainId);
	}

	private boolean isSyncNeeded(Calendar cal, DateTime now) {
		if (cal.getRemoteSyncTimestamp() == null) return true;
		return Minutes.minutesBetween(cal.getRemoteSyncTimestamp(), now).getMinutes() >= cal.getRemoteSyncFrequency();
	}
}
