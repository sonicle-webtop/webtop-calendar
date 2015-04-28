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
package com.sonicle.webtop.calendar.bol.model;

import org.joda.time.DateTime;

/**
 *
 * @author malbinola
 */
public class Recurrence {
	
	public static final String TYPE_NONE = "_";
	public static final String TYPE_DAILY = "D";
	public static final String TYPE_DAILY_FERIALI = "F";
	public static final String TYPE_WEEKLY = "W";
	public static final String TYPE_MONTHLY = "M";
	public static final String TYPE_YEARLY = "Y";
	public static final String DAILY_TYPE_DAY = "1";
	public static final String DAILY_TYPE_FERIALI = "2";
	public static final String ENDS_MODE_NEVER = "never";
	public static final String ENDS_MODE_REPEAT = "repeat";
	public static final String ENDS_MODE_UNTIL = "until";
	
	protected String endsMode;
	protected Integer repeatTimes;
	protected DateTime untilDate;
	protected String type;
	protected String dailyType;
	protected Integer dailyFreq;
	protected Integer weeklyFreq;
	protected Boolean weeklyDay1;
	protected Boolean weeklyDay2;
	protected Boolean weeklyDay3;
	protected Boolean weeklyDay4;
	protected Boolean weeklyDay5;
	protected Boolean weeklyDay6;
	protected Boolean weeklyDay7;
	protected Integer monthlyFreq;
	protected Integer monthlyDay;
	protected Integer yearlyFreq;
	protected Integer yearlyDay;
	
	public Recurrence() {
		
	}

	public String getEndsMode() {
		return endsMode;
	}

	public void setEndsMode(String value) {
		endsMode = value;
	}

	public Integer getRepeatTimes() {
		return repeatTimes;
	}

	public void setRepeatTimes(Integer value) {
		repeatTimes = value;
	}

	public DateTime getUntilDate() {
		return untilDate;
	}

	public void setUntilDate(DateTime value) {
		untilDate = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		type = value;
	}

	public String getDaylyType() {
		return dailyType;
	}

	public void setDaylyType(String value) {
		dailyType = value;
	}

	public Integer getDaylyFreq() {
		return dailyFreq;
	}

	public void setDaylyFreq(Integer value) {
		dailyFreq = value;
	}

	public Integer getWeeklyFreq() {
		return weeklyFreq;
	}

	public void setWeeklyFreq(Integer value) {
		weeklyFreq = value;
	}

	public Boolean getWeeklyDay1() {
		return weeklyDay1;
	}

	public void setWeeklyDay1(Boolean value) {
		weeklyDay1 = value;
	}

	public Boolean getWeeklyDay2() {
		return weeklyDay2;
	}

	public void setWeeklyDay2(Boolean value) {
		weeklyDay2 = value;
	}

	public Boolean getWeeklyDay3() {
		return weeklyDay3;
	}

	public void setWeeklyDay3(Boolean value) {
		weeklyDay3 = value;
	}

	public Boolean getWeeklyDay4() {
		return weeklyDay4;
	}

	public void setWeeklyDay4(Boolean value) {
		weeklyDay4 = value;
	}

	public Boolean getWeeklyDay5() {
		return weeklyDay5;
	}

	public void setWeeklyDay5(Boolean value) {
		weeklyDay5 = value;
	}

	public Boolean getWeeklyDay6() {
		return weeklyDay6;
	}

	public void setWeeklyDay6(Boolean value) {
		weeklyDay6 = value;
	}

	public Boolean getWeeklyDay7() {
		return weeklyDay7;
	}

	public void setWeeklyDay7(Boolean value) {
		weeklyDay7 = value;
	}

	public Integer getMonthlyFreq() {
		return monthlyFreq;
	}

	public void setMonthlyFreq(Integer value) {
		monthlyFreq = value;
	}

	public Integer getMonthlyDay() {
		return monthlyDay;
	}

	public void setMonthlyDay(Integer value) {
		monthlyDay = value;
	}

	public Integer getYearlyFreq() {
		return yearlyFreq;
	}

	public void setYearlyFreq(Integer value) {
		yearlyFreq = value;
	}

	public Integer getYearlyDay() {
		return yearlyDay;
	}

	public void setYearlyDay(Integer value) {
		yearlyDay = value;
	}
}
