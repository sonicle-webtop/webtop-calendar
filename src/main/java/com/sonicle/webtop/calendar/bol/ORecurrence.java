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

import com.sonicle.webtop.calendar.ICal4jUtils;
import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.jooq.tables.pojos.Recurrences;
import com.sonicle.webtop.core.sdk.WTException;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.lang.StringUtils;
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
	
	public void fillFrom(JsEvent jse, OEvent event) {
		DateTimeZone etz = DateTimeZone.forID(event.getTimezone());
		
		setStartDate(event.getStartDate());
		
		if(StringUtils.equals(jse.rrType, "D")) {
			setType(jse.rrType);
			if(StringUtils.equals(jse.rrDaylyType, "1")) {
				setDaylyFreq(jse.rrDaylyFreq);
			} else if(StringUtils.equals(jse.rrDaylyType, "2")) {
				setType("F");
			} else {
				setDaylyFreq(null);
			}
		} else {
			// Reset fields...
			setDaylyFreq(null);
		}
			
		if(StringUtils.equals(jse.rrType, "W")) {
			setType(jse.rrType);
			setWeeklyFreq(jse.rrWeeklyFreq);
			setWeeklyDay_1(jse.rrWeeklyDay1);
			setWeeklyDay_2(jse.rrWeeklyDay2);
			setWeeklyDay_3(jse.rrWeeklyDay3);
			setWeeklyDay_4(jse.rrWeeklyDay4);
			setWeeklyDay_5(jse.rrWeeklyDay5);
			setWeeklyDay_6(jse.rrWeeklyDay6);
			setWeeklyDay_7(jse.rrWeeklyDay7);
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
		
		if(StringUtils.equals(jse.rrType, "M")) {
			setType(jse.rrType);
			setMonthlyFreq(jse.rrMonthlyFreq);
			setMonthlyDay(jse.rrMonthlyDay);
			
		} else {
			// Reset fields...
			setMonthlyFreq(null);
			setMonthlyDay(null);
		}
		
		if(StringUtils.equals(jse.rrType, "Y")) {
			setType(jse.rrType);
			setYearlyFreq(jse.rrYearlyFreq);
			setYearlyDay(jse.rrYearlyDay);
			setStartDate(event.getStartDate().withMonthOfYear(jse.rrYearlyFreq).withDayOfMonth(jse.rrYearlyDay));
			
		} else {
			// Reset fields...
			setYearlyFreq(null);
			setYearlyDay(null);
		}
		
		if(StringUtils.equals(jse.rrEndsMode, "never")) {
			setRepeat(null);
			setUntilDate(ICal4jUtils.ifiniteDate(etz));
			
		} else if(StringUtils.equals(jse.rrEndsMode, "repeat")) {
			setRepeat(jse.rrRepeatTimes);
			RRule rr = asRRule(etz);
			setUntilDate(ICal4jUtils.calculateRecurrenceEnd(event.getStartDate(), event.getEndDate(), rr, DateTimeZone.UTC));
			//TODO: completare implementazione repeat
			
		} else if(StringUtils.equals(jse.rrEndsMode, "until")) {
			//TODO: controllare che until > event.end ?
			setRepeat(null);
			setUntilDate(OEvent.parseYmdHmsWithZone(jse.rrUntilDate, etz).withTime(0, 0, 0, 0));
		}
		
		setRule(asRRule(etz).getValue());
	}
	
	public RRule asRRule(DateTimeZone etz) {
		Recur rec = null;
		
		try {
			rec = new Recur();
			
			if(StringUtils.equals(getType(), "D")) {
				rec.setFrequency(Recur.DAILY);
				rec.setInterval(getDaylyFreq());
			
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
					rec.setUntil(ICal4jUtils.toICal4jDateTime(getUntilDate(), etz));
				}
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return new RRule(rec);
	}
	

}
