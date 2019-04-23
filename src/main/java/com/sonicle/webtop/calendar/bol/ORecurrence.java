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
package com.sonicle.webtop.calendar.bol;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.webtop.calendar.model.EventRecurrence;
import com.sonicle.webtop.calendar.jooq.tables.pojos.Recurrences;
import com.sonicle.webtop.core.sdk.WTException;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 *
 * @author malbinola
 */
public class ORecurrence extends Recurrences {
	
	public void set(Recur recur, LocalDate recurStartDate, DateTime eventStartDate, DateTime eventEndDate, DateTimeZone eventTimezone) {
		DateTime newStart = (recurStartDate != null) ? recurStartDate.toDateTimeAtStartOfDay(eventTimezone) : eventStartDate;
		setStartDate(newStart);
		if (ICal4jUtils.recurHasCount(recur)) {
			DateTime untilDate = ICal4jUtils.calculateRecurrenceEnd(recur, newStart, eventTimezone);
			setUntilDate(untilDate.withTimeAtStartOfDay().plusDays(1));
		} else if (ICal4jUtils.recurHasUntilDate(recur)) {
			DateTime untilDate = ICal4jUtils.toJodaDateTime(recur.getUntil(), DateTimeZone.UTC).withZone(eventTimezone);
			setUntilDate(untilDate.withTimeAtStartOfDay().plusDays(1));
		} else {
			setUntilDate(ICal4jUtils.ifiniteDate(eventTimezone));
		}
		setRule(recur.toString());
	}
	
	public void updateUntilDate(LocalDate localDate, LocalTime localTime, DateTimeZone timezone) {
		Recur recur = getRecur();
		ICal4jUtils.setRecurUntilDate(recur, localDate.toDateTime(localTime, timezone));
		setRule(recur.toString());
		setUntilDate(localDate.toDateTimeAtStartOfDay(timezone).plusDays(1));
	}
	
	public Recur getRecur() {
		return ICal4jUtils.parseRRule(getRule());
	}
	
	public LocalDate getLocalStartDate(DateTimeZone eventTimezone) {
		return getStartDate().withZone(eventTimezone).toLocalDate();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
	public void __fillFrom(boolean setRule, EventRecurrence rec, DateTime eventStartDate, DateTime eventEndDate, String eventTimeZone) {
		DateTimeZone etz = DateTimeZone.forID(eventTimeZone);
		
		setStartDate(eventStartDate);
		
		if(StringUtils.equals(rec.getType(), EventRecurrence.TYPE_DAILY)) {
			setType(rec.getType());
			setDailyFreq(rec.getDailyFreq());
		} else if(StringUtils.equals(rec.getType(), EventRecurrence.TYPE_DAILY_FERIALI)) {
			setType(rec.getType());
		} else {
			// Reset fields...
			setDailyFreq(null);
		}
			
		if(StringUtils.equals(rec.getType(), EventRecurrence.TYPE_WEEKLY)) {
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
		
		if(StringUtils.equals(rec.getType(), EventRecurrence.TYPE_MONTHLY)) {
			setType(rec.getType());
			setMonthlyFreq(rec.getMonthlyFreq());
			setMonthlyDay(rec.getMonthlyDay());
			
		} else {
			// Reset fields...
			setMonthlyFreq(null);
			setMonthlyDay(null);
		}
		
		if(StringUtils.equals(rec.getType(), EventRecurrence.TYPE_YEARLY)) {
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
		if(StringUtils.equals(rec.getEndsMode(), EventRecurrence.ENDS_MODE_NEVER)) {
			rr = __applyEndNever(etz, setRule);
		} else if(StringUtils.equals(rec.getEndsMode(), EventRecurrence.ENDS_MODE_REPEAT)) {
			rr = __applyEndRepeat(rec.getRepeatTimes(), eventStartDate, eventEndDate, etz, setRule);
		} else if(StringUtils.equals(rec.getEndsMode(), EventRecurrence.ENDS_MODE_UNTIL)) {
			rr = __applyEndUntil(rec.getUntilDate(), etz, setRule);
		} else {
			throw new RuntimeException("Recurrence end-mode unknown or undefined");
		}
		setRule(rr.getValue());
	}
	
	@Deprecated
	public RRule __applyEndNever(DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(null);
		setPermanent(true);
		setUntilDate(ICal4jUtils.ifiniteDate(etz));
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	@Deprecated
	public RRule __applyEndRepeat(int repeatTimes, DateTime eventStartDate, DateTime eventEndDate, DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(repeatTimes);
		setPermanent(false);
		rr = buildRRule(etz);
		setUntilDate(ICal4jUtils.calculateRecurrenceEnd(eventStartDate, eventEndDate, rr, DateTimeZone.UTC));
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	@Deprecated
	public RRule __applyEndUntil(DateTime untilDate, DateTimeZone etz, boolean setRule) {
		RRule rr = null;
		setRepeat(null);
		setPermanent(false);
		setUntilDate(untilDate.withTimeAtStartOfDay());
		rr = buildRRule(etz);
		if(setRule) setRule(rr.getValue());
		return rr;
	}
	
	@Deprecated
	public void updateRRule(DateTimeZone etz) {
		setRule(buildRRule(etz).getValue());
	}
	
	@Deprecated
	public RRule buildRRule(DateTimeZone etz) {
		Recur rec = null;
		
		try {
			rec = new Recur();
			
			if(StringUtils.equals(getType(), EventRecurrence.TYPE_DAILY)) {
				rec.setFrequency(Recur.DAILY);
				rec.setInterval(getDailyFreq());
			
			} else if(StringUtils.equals(getType(), EventRecurrence.TYPE_DAILY_FERIALI)) {
				rec.setFrequency(Recur.WEEKLY);
				rec.setInterval(1);
				rec.getDayList().add(WeekDay.MO);
				rec.getDayList().add(WeekDay.TU);
				rec.getDayList().add(WeekDay.WE);
				rec.getDayList().add(WeekDay.TH);
				rec.getDayList().add(WeekDay.FR);
			
			} else if(StringUtils.equals(getType(), EventRecurrence.TYPE_WEEKLY)) {
				rec.setFrequency(Recur.WEEKLY);
				rec.setInterval(getWeeklyFreq());
				if(getWeeklyDay_1()) rec.getDayList().add(WeekDay.MO);
				if(getWeeklyDay_2()) rec.getDayList().add(WeekDay.TU);
				if(getWeeklyDay_3()) rec.getDayList().add(WeekDay.WE);
				if(getWeeklyDay_4()) rec.getDayList().add(WeekDay.TH);
				if(getWeeklyDay_5()) rec.getDayList().add(WeekDay.FR);
				if(getWeeklyDay_6()) rec.getDayList().add(WeekDay.SA);
				if(getWeeklyDay_7()) rec.getDayList().add(WeekDay.SU);
				
			} else if(StringUtils.equals(getType(), EventRecurrence.TYPE_MONTHLY)) {
				rec.setFrequency(Recur.MONTHLY);
				rec.setInterval(getMonthlyFreq());
				rec.getMonthDayList().add(getMonthlyDay());
				
			} else if(StringUtils.equals(getType(), EventRecurrence.TYPE_YEARLY)) {
				rec.setFrequency(Recur.YEARLY);
				rec.setInterval(1); // GUI is not currently able to handle different value
				rec.getMonthList().add(getYearlyFreq());
				rec.getMonthDayList().add(getYearlyDay());
				
			} else {
				throw new WTException("Unknown recurrence type [{0}]", getType());
			}
			
			if (isEndRepeat()) {
				rec.setCount(getRepeat());
			} else if(isEndUntil()) {
				final DateTime lastTimeOfDay = DateTimeUtils.withTimeAtEndOfDay(getUntilDate());
				rec.setUntil(ICal4jUtils.toIC4jDateTimeUTC(lastTimeOfDay));
			} else if(isEndNever()) {
				//TODO: change end logic: rrule without until date means infinite repeat!
				rec.setUntil(ICal4jUtils.toIC4jDateTimeUTC(ICal4jUtils.ifiniteDate(etz)));
			} else {
				throw new WTException("Unknown ends-mode");
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return new RRule(rec);
	}
	
	@Deprecated
	public boolean isEndNever() {
		return getPermanent() && (getRepeat() == null);
	}
	
	@Deprecated
	public boolean isEndRepeat() {
		return !getPermanent() && ((getRepeat() != null) && (getRepeat() > 0));
	}
	
	@Deprecated
	public boolean isEndUntil() {
		return !getPermanent() && (((getRepeat() == null) || (getRepeat() == 0)) && (getUntilDate() != null));
	}
}
