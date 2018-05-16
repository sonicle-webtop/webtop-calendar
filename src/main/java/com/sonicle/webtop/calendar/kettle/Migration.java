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
package com.sonicle.webtop.calendar.kettle;

import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.model.EventRecurrence;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class Migration {
	
	public static ORecurrence buildRecurrence(
			Date eventStart, Date eventEnd, String eventTimezone,
			Date startDate, Date untilDate, Integer repeat,
			Boolean permanent, String type,
			Integer dailyFreq,
			Integer weeklyFreq, Boolean weeklyDay1, Boolean weeklyDay2,
			Boolean weeklyDay3, Boolean weeklyDay4, Boolean weeklyDay5,
			Boolean weeklyDay6, Boolean weeklyDay7,
			Integer monthlyFreq, Integer monthlyDay,
			Integer yearlyFreq, Integer yearlyDay) {
		
		DateTime dtEventStart = new DateTime(eventStart, DateTimeZone.UTC);
		DateTime dtEventEnd = new DateTime(eventEnd, DateTimeZone.UTC);
		DateTime dtUntilDate = (untilDate != null) ? new DateTime(untilDate, DateTimeZone.UTC) : null;
		
		// Emulates the orec object coming from wt5 db
		ORecurrence orec = new ORecurrence();
		orec.setStartDate(dtEventStart);
		orec.setUntilDate(dtUntilDate);
		orec.setRepeat(repeat);
		orec.setPermanent(permanent);
		orec.setType(type);
		orec.setDailyFreq(dailyFreq);
		orec.setWeeklyFreq(weeklyFreq);
		orec.setWeeklyDay_1(weeklyDay1);
		orec.setWeeklyDay_2(weeklyDay2);
		orec.setWeeklyDay_3(weeklyDay3);
		orec.setWeeklyDay_4(weeklyDay4);
		orec.setWeeklyDay_5(weeklyDay5);
		orec.setWeeklyDay_6(weeklyDay6);
		orec.setWeeklyDay_7(weeklyDay7);
		orec.setMonthlyFreq(monthlyFreq);
		orec.setMonthlyDay(monthlyDay);
		orec.setYearlyFreq(yearlyFreq);
		orec.setYearlyDay(yearlyDay);
		
		EventRecurrence rec = createEventRecurrence(orec);
		orec.__fillFrom(true, rec, dtEventStart, dtEventEnd, eventTimezone);
		return orec;
	}
	
	private static EventRecurrence createEventRecurrence(ORecurrence orec) {
		
		/**
		 * NB: aggiornare anche l'implementazione in Migration.java
		 */
		
		if (orec == null) return null;
		EventRecurrence rec = new EventRecurrence();
		rec.setType(orec.getType());
		rec.setDailyFreq(orec.getDailyFreq());
		rec.setWeeklyFreq(orec.getWeeklyFreq());
		rec.setWeeklyDay1(orec.getWeeklyDay_1());
		rec.setWeeklyDay2(orec.getWeeklyDay_2());
		rec.setWeeklyDay3(orec.getWeeklyDay_3());
		rec.setWeeklyDay4(orec.getWeeklyDay_4());
		rec.setWeeklyDay5(orec.getWeeklyDay_5());
		rec.setWeeklyDay6(orec.getWeeklyDay_6());
		rec.setWeeklyDay7(orec.getWeeklyDay_7());
		rec.setMonthlyFreq(orec.getMonthlyFreq());
		rec.setMonthlyDay(orec.getMonthlyDay());
		rec.setYearlyFreq(orec.getYearlyFreq());
		rec.setYearlyDay(orec.getYearlyDay());
		
		rec.setUntilDate(orec.getUntilDate());
		if(orec.isEndRepeat()) {
			rec.setEndsMode(EventRecurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(orec.getRepeat());
		} else if(orec.isEndUntil()) {
			rec.setEndsMode(EventRecurrence.ENDS_MODE_UNTIL);
		} else if(orec.isEndNever()) {
			rec.setEndsMode(EventRecurrence.ENDS_MODE_NEVER);
		} else {
			throw new RuntimeException("Unable to set a valid endMode");
		}
		
		rec.setRRule(orec.getRule());
		return rec;
	}
}
