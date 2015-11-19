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

import com.sonicle.webtop.calendar.bol.OPostponedReminder;
import com.sonicle.webtop.calendar.bol.model.SchedulerEvent;
import com.sonicle.webtop.calendar.dal.EventDAO;
import com.sonicle.webtop.calendar.msg.ReminderMessage;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobServiceTask;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class JobService extends BaseJobService {
	private static final Logger logger = WT.getLogger(JobService.class);
	
	CalendarServiceSettings css;
	CalendarUserSettings cus;
	CalendarManager manager;
	
	@Override
	public void initialize() {
		css = new CalendarServiceSettings(getId());
		cus = new CalendarUserSettings(getId(), new UserProfile.Id("*", "*"), css);
		manager = new CalendarManager(getId(), getRunContext());
	}
	
	@Override
	public void cleanup() {
		manager = null;
		cus = null;
	}
	
	@Override
	public List<TaskDefinition> returnTasks() {
		ArrayList<TaskDefinition> jobs = new ArrayList<>();
		
		// CalendarJob
		Trigger trigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0,5,10,15,20,25,30,35,40,45,50,55 * * * ?"))
				.build();
		jobs.add(new TaskDefinition(CalendarJob.class, trigger));
		
		return jobs;
	}
	
	public static class CalendarJob extends BaseJobServiceTask {
		private JobService jobService = null;
		private final HashMap<String, Boolean> notifyByEmailCache = new HashMap<>();
		
		@Override
		public void setJobService(BaseJobService value) {
			// This method is automatically called by scheduler engine
			// while instantiating this task.
			jobService = (JobService)value;
		}
		
		@Override
		public void executeWork() {
			Connection con = null;
			notifyByEmailCache.clear();
			
			try {
				DateTime now = DateTime.now(DateTimeZone.UTC);
				DateTime from = now.withTimeAtStartOfDay();
				
				con = WT.getConnection(jobService.getId());
				con.setAutoCommit(false);
				
				try {
					DateTime remindOn = null;
					List<SchedulerEvent> events = jobService.manager.listExpiredSchedulerEvents(con, from, from.plusDays(7));
					for(SchedulerEvent event : events) {
						//TODO: implementare gestione reminder anche per le ricorrenze
						remindOn = event.getStartDate().withZone(DateTimeZone.UTC).minusMinutes(event.getReminder());
						if(now.compareTo(remindOn) >= 0) handleReminder(con, event, now, remindOn);
					}
					con.commit();
					
				} catch(Exception ex1) {
					logger.error("[ReminderTask] Error handling expiredEvents", ex1);
					con.rollback();
				}
				
				try {
					SchedulerEvent prevent = null;
					List<OPostponedReminder> prems = jobService.manager.getExpiredPostponedReminders(con, now);
					for(OPostponedReminder prem : prems) {
						prevent = jobService.manager.getSchedulerEvent(prem.getEventId());
						handleReminder(con, prevent, now, prem.getRemindOn());
						jobService.manager.deletePostponedReminder(con, prem.getEventId(), prem.getRemindOn());
					}
					con.commit();
					
				} catch(Exception ex1) {
					logger.error("[ReminderTask] Error handling expiredPostponed", ex1);
					con.rollback();
				}
				
			} catch(Exception ex) {
				logger.error("[ReminderTask] Error executing work", ex);
			}
		}
		
		public void handleReminder(Connection con, SchedulerEvent event, DateTime now, DateTime remindOn) {
			UserProfile.Id profileId = new UserProfile.Id(event.getCalendarDomainId(), event.getCalendarUserId());
			EventDAO edao = EventDAO.getInstance();
			
			int ret = edao.updateRemindedOnIfNull(con, event.getEventId(), now);
			if(ret == 1) {
				boolean notifyByEmail;
				if(notifyByEmailCache.containsKey(profileId.toString())) {
					notifyByEmail = notifyByEmailCache.get(profileId.toString());
				} else {
					CalendarUserSettings cus = new CalendarUserSettings(jobService.getId(), profileId, jobService.css);
					notifyByEmail = cus.getReminderByEmail();
					notifyByEmailCache.put(profileId.toString(), notifyByEmail);
				}
				
				if(notifyByEmail) {
					//TODO: notifica tramite email
				} else {
					WT.nofity(profileId, new ReminderMessage(jobService.getId(), remindOn, event, DateTimeZone.UTC), true);
				}
			}
		}
	}
}
