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
package com.sonicle.webtop.calendar.bol;

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.calendar.ICal4jUtils;
import com.sonicle.webtop.calendar.bol.model.Recurrence;
import com.sonicle.webtop.calendar.jooq.tables.pojos.Recurrences;
import com.sonicle.webtop.core.sdk.WTException;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class ORecurrence extends Recurrences {
	
	public ORecurrence() {
		super();
	}
	
	public void fillFrom(Recurrence rec, DateTime eventStartDate, DateTime eventEndDate, String eventTimeZone) {
		DateTimeZone etz = DateTimeZone.forID(eventTimeZone);
		
		setStartDate(eventStartDate);
		
		if(StringUtils.equals(rec.getType(), Recurrence.TYPE_DAILY)) {
			setType(rec.getType());
			setDailyFreq(rec.getDailyFreq());
		} else if(StringUtils.equals(rec.getType(), Recurrence.TYPE_DAILY_FERIALI)) {
			setType(rec.getType());
		} else {
			// Reset fields...
			setDailyFreq(null);
		}
			
		if(StringUtils.equals(rec.getType(), Recurrence.TYPE_WEEKLY)) {
			setType(rec.getType());
			setWeeklyFreq(rec.getWeeklyFreq());
			setWeeklyDay_1(LangUtils.coalesce(rec.getWeeklyDay1(), false));
			setWeeklyDay_2(LangUtils.coalesce(rec.getWeeklyDay2(), false));
			setWeeklyDay_3(LangUtils.coalesce(rec.getWeeklyDay3(), false));
			setWeeklyDay_4(LangUtils.coalesce(rec.getWeeklyDay4(), false));
			setWeeklyDay_5(LangUtils.coalesce(rec.getWeeklyDay5(), false));
			setWeeklyDay_6(LangUtils.coalesce(rec.getWeeklyDay6(), false));
			setWeeklyDay_7(LangUtils.coalesce(rec.getWeeklyDay7(), false));
		} else {
			// Reset fields...
			setWeeklyFreq(null);
			setWeeklyDay_1(null);
			setWeeklyDay_2(null);
			setWeeklyDay_3(null);
			setWeeklyDay_4(null);
			setWeeklyDay_5(null);
			setWeeklyDay_6(null);
			setWeeklyDay_7(null);
		}
		
		if(StringUtils.equals(rec.getType(), Recurrence.TYPE_MONTHLY)) {
			setType(rec.getType());
			setMonthlyFreq(rec.getMonthlyFreq());
			setMonthlyDay(rec.getMonthlyDay());
			
		} else {
			// Reset fields...
			setMonthlyFreq(null);
			setMonthlyDay(null);
		}
		
		if(StringUtils.equals(rec.getType(), Recurrence.TYPE_YEARLY)) {
			setType(rec.getType());
			setYearlyFreq(rec.getYearlyFreq());
			setYearlyDay(rec.getYearlyDay());
			setStartDate(eventStartDate.withMonthOfYear(rec.getYearlyFreq()).withDayOfMonth(rec.getYearlyDay()));
			
		} else {
			// Reset fields...
			setYearlyFreq(null);
			setYearlyDay(null);
		}
		
		RRule rr = null;
		if(StringUtils.equals(rec.getEndsMode(), Recurrence.ENDS_MODE_NEVER)) {
			rr = applyEndNever(etz, false);
		} else if(StringUtils.equals(rec.getEndsMode(), Recurrence.ENDS_MODE_REPEAT)) {
			rr = applyEndRepeat(rec.getRepeatTimes(), eventStartDate, eventEndDate, etz, false);
		} else if(StringUtils.equals(rec.getEndsMode(), Recurrence.ENDS_MODE_UNTIL)) {
			rr = applyEndUntil(rec.getUntilDate(), etz, false);
		}
		setRule(rr.getValue());
	}
	
	public RRule applyEndNever(DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(null);
		setPermanent(true);
		setUntilDate(ICal4jUtils.ifiniteDate(etz));
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	public RRule applyEndRepeat(int repeatTimes, DateTime eventStartDate, DateTime eventEndDate, DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(repeatTimes);
		setPermanent(false);
		rr = buildRRule(etz);
		setUntilDate(ICal4jUtils.calculateRecurrenceEnd(eventStartDate, eventEndDate, rr, DateTimeZone.UTC));
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	public RRule applyEndUntil(DateTime untilDate, DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(null);
		setPermanent(false);
		setUntilDate(untilDate.withTimeAtStartOfDay());
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	public void updateRRule(DateTimeZone etz) {
		setRule(buildRRule(etz).getValue());
	}
	
	public RRule buildRRule(DateTimeZone etz) {
		Recur rec = null;
		
		try {
			rec = new Recur();
			
			if(StringUtils.equals(getType(), Recurrence.TYPE_DAILY)) {
				rec.setFrequency(Recur.DAILY);
				rec.setInterval(getDailyFreq());
			
			} else if(StringUtils.equals(getType(), Recurrence.TYPE_DAILY_FERIALI)) {
				rec.setFrequency(Recur.WEEKLY);
				rec.setInterval(1);
				rec.getDayList().add(WeekDay.MO);
				rec.getDayList().add(WeekDay.TU);
				rec.getDayList().add(WeekDay.WE);
				rec.getDayList().add(WeekDay.TH);
				rec.getDayList().add(WeekDay.FR);
			
			} else if(StringUtils.equals(getType(), Recurrence.TYPE_WEEKLY)) {
				rec.setFrequency(Recur.WEEKLY);
				rec.setInterval(getWeeklyFreq());
				if(getWeeklyDay_1()) rec.getDayList().add(WeekDay.MO);
				if(getWeeklyDay_2()) rec.getDayList().add(WeekDay.TU);
				if(getWeeklyDay_3()) rec.getDayList().add(WeekDay.WE);
				if(getWeeklyDay_4()) rec.getDayList().add(WeekDay.TH);
				if(getWeeklyDay_5()) rec.getDayList().add(WeekDay.FR);
				if(getWeeklyDay_6()) rec.getDayList().add(WeekDay.SA);
				if(getWeeklyDay_7()) rec.getDayList().add(WeekDay.SU);
				
			} else if(StringUtils.equals(getType(), Recurrence.TYPE_MONTHLY)) {
				rec.setFrequency(Recur.MONTHLY);
				rec.setInterval(getMonthlyFreq());
				rec.getMonthDayList().add(getMonthlyDay());
				
			} else if(StringUtils.equals(getType(), Recurrence.TYPE_YEARLY)) {
				rec.setFrequency(Recur.YEARLY);
				rec.setInterval(1); // GUI is not currently able to handle different value
				rec.getMonthList().add(getYearlyFreq());
				rec.getMonthDayList().add(getYearlyDay());
				
			} else {
				throw new WTException("Unknown recurrence type [{0}]", getType());
			}
			
			if(isEndRepeat()) {
				rec.setCount(getRepeat());
			} else if(isEndUntil()) {
				// We need to sum 1day to defined until date, for rrule untilDate is not inclusive!
				rec.setUntil(ICal4jUtils.toICal4jDateTime(getUntilDate().plusDays(1), etz));
			} else if(isEndNever()) {
				rec.setUntil(ICal4jUtils.toICal4jDateTime(ICal4jUtils.ifiniteDate(etz), etz));
			} else {
				throw new WTException("Unknown ends mode combination");
			}
			
			/*
			if((getPermanent() != null) && getPermanent()) {
				rec.setUntil(ICal4jUtils.toICal4jDateTime(ICal4jUtils.ifiniteDate(etz), etz));
			} else {
				if((getRepeat() != null) && (getRepeat() > 0)) {
					rec.setCount(getRepeat());
				} else {
					// We need to sum 1day to defined until date, for rrule untilDate is not inclusive!
					rec.setUntil(ICal4jUtils.toICal4jDateTime(getUntilDate().plusDays(1), etz));
				}
			}
			*/
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return new RRule(rec);
	}
	
	public boolean isEndNever() {
		return getPermanent() && (getRepeat() == null);
	}
	
	public boolean isEndRepeat() {
		return !getPermanent() && (getRepeat() > 0);
	}
	
	public boolean isEndUntil() {
		return !getPermanent() && (getRepeat() == null);
	}
}
