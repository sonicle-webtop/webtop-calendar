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

import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.ICal4jUtils;
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
	
	public void fillFrom(Event event, DateTime eventStartDate, DateTime eventEndDate, String eventTimeZone) {
		DateTimeZone etz = DateTimeZone.forID(eventTimeZone);
		
		setStartDate(eventStartDate);
		
		if(StringUtils.equals(event.rrType, Event.TYPE_DAILY)) {
			setType(event.rrType);
			if(StringUtils.equals(event.rrDailyType, Event.DAILY_TYPE_DAY)) {
				setDailyFreq(event.rrDailyFreq);
			} else if(StringUtils.equals(event.rrDailyType, Event.DAILY_TYPE_FERIALI)) {
				setType(Event.TYPE_DAILY_FERIALI);
			} else {
				setDailyFreq(null);
			}
		} else {
			// Reset fields...
			setDailyFreq(null);
		}
			
		if(StringUtils.equals(event.rrType, Event.TYPE_WEEKLY)) {
			setType(event.rrType);
			setWeeklyFreq(event.rrWeeklyFreq);
			setWeeklyDay_1(event.rrWeeklyDay1);
			setWeeklyDay_2(event.rrWeeklyDay2);
			setWeeklyDay_3(event.rrWeeklyDay3);
			setWeeklyDay_4(event.rrWeeklyDay4);
			setWeeklyDay_5(event.rrWeeklyDay5);
			setWeeklyDay_6(event.rrWeeklyDay6);
			setWeeklyDay_7(event.rrWeeklyDay7);
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
		
		if(StringUtils.equals(event.rrType, Event.TYPE_MONTHLY)) {
			setType(event.rrType);
			setMonthlyFreq(event.rrMonthlyFreq);
			setMonthlyDay(event.rrMonthlyDay);
			
		} else {
			// Reset fields...
			setMonthlyFreq(null);
			setMonthlyDay(null);
		}
		
		if(StringUtils.equals(event.rrType, Event.TYPE_YEARLY)) {
			setType(event.rrType);
			setYearlyFreq(event.rrYearlyFreq);
			setYearlyDay(event.rrYearlyDay);
			setStartDate(eventStartDate.withMonthOfYear(event.rrYearlyFreq).withDayOfMonth(event.rrYearlyDay));
			
		} else {
			// Reset fields...
			setYearlyFreq(null);
			setYearlyDay(null);
		}
		
		RRule rr = null;
		if(StringUtils.equals(event.rrEndsMode, Event.ENDS_MODE_NEVER)) {
			rr = applyEndNever(etz, false);
			
		} else if(StringUtils.equals(event.rrEndsMode, Event.ENDS_MODE_REPEAT)) {
			rr = applyEndRepeat(event.rrRepeatTimes, eventStartDate, eventEndDate, etz, false);
			//TODO: completare implementazione repeat
			
		} else if(StringUtils.equals(event.rrEndsMode, Event.ENDS_MODE_UNTIL)) {
			rr = applyEndUntil(event.rrUntilDate, etz, false);
		}
		
		setRule(rr.getValue());
	}
	
	public RRule applyEndNever(DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(null);
		setUntilDate(ICal4jUtils.ifiniteDate(etz));
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	public RRule applyEndRepeat(int repeatTimes, DateTime eventStartDate, DateTime eventEndDate, DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(repeatTimes);
		rr = buildRRule(etz);
		setUntilDate(ICal4jUtils.calculateRecurrenceEnd(eventStartDate, eventEndDate, rr, DateTimeZone.UTC));
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	public RRule applyEndUntil(DateTime untilDate, DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(null);
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
			
			if(StringUtils.equals(getType(), "D")) {
				rec.setFrequency(Recur.DAILY);
				rec.setInterval(getDailyFreq());
			
			} else if(StringUtils.equals(getType(), "F")) {
				rec.setFrequency(Recur.WEEKLY);
				rec.setInterval(1);
				rec.getDayList().add(WeekDay.MO);
				rec.getDayList().add(WeekDay.TU);
				rec.getDayList().add(WeekDay.WE);
				rec.getDayList().add(WeekDay.TH);
				rec.getDayList().add(WeekDay.FR);
			
			} else if(StringUtils.equals(getType(), "W")) {
				rec.setFrequency(Recur.WEEKLY);
				rec.setInterval(getWeeklyFreq());
				if(getWeeklyDay_1()) rec.getDayList().add(WeekDay.MO);
				if(getWeeklyDay_2()) rec.getDayList().add(WeekDay.TU);
				if(getWeeklyDay_3()) rec.getDayList().add(WeekDay.WE);
				if(getWeeklyDay_4()) rec.getDayList().add(WeekDay.TH);
				if(getWeeklyDay_5()) rec.getDayList().add(WeekDay.FR);
				if(getWeeklyDay_6()) rec.getDayList().add(WeekDay.SA);
				if(getWeeklyDay_7()) rec.getDayList().add(WeekDay.SU);
				
			} else if(StringUtils.equals(getType(), "M")) {
				rec.setFrequency(Recur.MONTHLY);
				rec.setInterval(getYearlyFreq());
				rec.getMonthDayList().add(getMonthlyDay());
				
			} else if(StringUtils.equals(getType(), "Y")) {
				rec.setFrequency(Recur.YEARLY);
				rec.setInterval(1); // GUI is not currently able to handle different value
				rec.getMonthList().add(getYearlyFreq());
				
			} else {
				throw new WTException("Unknown recurrence type [{0}]", getType());
			}
			
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
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return new RRule(rec);
	}
}
