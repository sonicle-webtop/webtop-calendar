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

import com.sonicle.webtop.calendar.bol.js.JsEvent;
import com.sonicle.webtop.calendar.jooq.tables.pojos.Recurrences;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class ORecurrence extends Recurrences {
	
	public ORecurrence() {
		super();
	}
	
	public ORecurrence(int recurrenceId) {
		super();
		setRecurrenceId(recurrenceId);
	}
	
	public void doTimeChecks() {
		
		// Force time
		setUntilDate(getUntilDate().withTime(0, 0, 0, 0));
	}
	
	public void fillFrom(JsEvent jse, OEvent event) {
		DateTimeZone etz = DateTimeZone.forID(event.getTimezone());
		
		setType(jse.rrType);
		
		if(StringUtils.equals(jse.rrEndsMode, "never")) {
			setUntilDate(OEvent.parseYmdHmsWithZone("2100-12-31 00:00:00", etz));
		} else if(StringUtils.equals(jse.rrEndsMode, "repeat")) {
			//TODO: completare implementazione repeat
		} else if(StringUtils.equals(jse.rrEndsMode, "until")) {
			//TODO: controllare che until > event.end ?
			setUntilDate(OEvent.parseYmdHmsWithZone(jse.rrUntilDate, etz));
		}
		
		setStartDate(event.getStartDate());
		
		if(StringUtils.equals(jse.rrType, "D")) {
			if(StringUtils.equals(jse.rrDaylyType, "1")) {
				setDaylyFreq(jse.rrDaylyFreq);
			} else {
				setDaylyFreq(null);
			}
		} else {
			// Reset fields...
			setDaylyFreq(null);
		}
			
		if(StringUtils.equals(jse.rrType, "W")) {
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
			setMonthlyFreq(jse.rrMonthlyFreq);
			setMonthlyDay(jse.rrMonthlyDay);
		} else {
			// Reset fields...
			setMonthlyFreq(null);
			setMonthlyDay(null);
		}
		
		if(StringUtils.equals(jse.rrType, "Y")) {
			setYearlyFreq(jse.rrYearlyFreq);
			setYearlyDay(jse.rrYearlyDay);
		} else {
			// Reset fields...
			setYearlyFreq(null);
			setYearlyDay(null);
		}
		
		doTimeChecks();
	}
}
