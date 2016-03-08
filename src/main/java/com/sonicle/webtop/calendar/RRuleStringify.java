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

import com.sonicle.commons.time.DateTimeUtils;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class RRuleStringify {
	private DateTimeZone timezone = null;
	private String dateFormat = null;
	private String timeFormat = null;
	
	private String everyOneDay = "Ogni giorno";
	private String everyXDays = "Ogni {0} giorni";
	private String everyWorkDay = "Ogni giorno feriale";
	private String everyOneWeek = "Ogni settimana";
	private String everyXWeeks = "Ogni {0} settimane";
	private String everyOneMonth = "Ogni mese";
	private String everyXMonths = "Ogni {0} mesi";
	private String everyOneYear = "Ogni anno";
	private String everyXYears = "Ogni {0} anni";
	private String forOneTime = "per una volta";
	private String forXTimes = "per {0} volte";
	private String untilDate = "fino al {0}";
	private String dayMo = "Lun";
	private String dayTu = "Mar";
	private String dayWe = "Mer";
	private String dayTh = "Gio";
	private String dayFr = "Ven";
	private String daySa = "Sab";
	private String daySu = "Dom";
	private String monthJan = "Gen";
	private String monthFeb = "Feb";
	private String monthMar = "Mar";
	private String monthApr = "Apr";
	private String monthMay = "Mag";
	private String monthJun = "Giu";
	private String monthJul = "Lug";
	private String monthAug = "Ago";
	private String monthSep = "Set";
	private String monthOct = "Ott";
	private String monthNov = "Nov";
	private String monthDec = "Dic";
	private String byOrdinalFirst = "1°";
	private String byOrdinalSecond = "2°";
	private String byOrdinalThird = "2°";
	private String byOrdinalX = "{0}°";
	private String fmtOnDay = "di {0}";
	private String fmtInMonth = "in {0}";
	private String fmtOn = "il {0}";
	private String fmtOnOrdinalDay = "il {0} giorno";
	private final WeekDay[] workDays = new WeekDay[]{WeekDay.MO, WeekDay.TU, WeekDay.WE, WeekDay.TH, WeekDay.FR};
	
	public RRuleStringify() {
		timezone = DateTimeZone.UTC;
		dateFormat = "yyyy-MM-dd";
		timeFormat = "HH:mm";
	}
	
	public DateTimeZone getTimeZone() {
		return this.timezone;
	}
	
	public void setTimeZone(DateTimeZone timezone) {
		this.timezone = timezone;
	}
	
	public String getDateFormat() {
		return this.dateFormat;
	}
	
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	public String getTimeFormat() {
		return this.timeFormat;
	}
	
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}
	
	public String toHumanReadableText(String rr) throws ParseException {
		return toHumanReadableText(new RRule(rr));
	}
	
	public String toHumanReadableText(RRule rr) {
		Recur recur = rr.getRecur();
		StringBuilder sb = new StringBuilder();
		
		if(recur.getFrequency().equals(Recur.DAILY)) {
			if(recur.getInterval() > 1) { // every x days
				sb.append(MessageFormat.format(everyXDays, recur.getInterval()));
				sb.append(" ");
			} else { // every day
				sb.append(everyOneDay);
				sb.append(" ");
			}
			
			appendEndText(sb, recur);
			
		} else if(recur.getFrequency().equals(Recur.WEEKLY)) {
			if(isByWorkDays(recur.getDayList())) { // every work day
				sb.append(everyWorkDay);
				sb.append(" ");
			} else {
				if(recur.getInterval() > 1) { // every x weeks
					sb.append(MessageFormat.format(everyXWeeks, recur.getInterval()));
					sb.append(" ");
				} else { // every week
					sb.append(everyOneWeek);
					sb.append(" ");
				}
				sb.append(formatIfNotEmpty(fmtOnDay, byDayText(recur.getDayList())));
				sb.append(" ");
			}
			
			appendEndText(sb, recur);
			
		} else if(recur.getFrequency().equals(Recur.MONTHLY)) {
			if(recur.getInterval() > 1) {
				sb.append(MessageFormat.format(everyXMonths, recur.getInterval()));
				sb.append(" ");
			} else {
				sb.append(everyOneMonth);
				sb.append(" ");
			}
			
			if(!recur.getMonthDayList().isEmpty()) { // on the 1st,2nd,3rd day...
				sb.append(formatIfNotEmpty(fmtOnOrdinalDay, byMonthDayAsOrdinalText(recur.getMonthDayList())));
				sb.append(" ");
			} else {
				//TODO: gestire caso "primo(secondo, ..ultimo) venerdì di ogni X mese"
			}
			
			appendEndText(sb, recur);
			
		} else if(recur.getFrequency().equals(Recur.YEARLY)) {
			if(recur.getInterval() > 1) { // every x years
				sb.append(MessageFormat.format(everyXYears, recur.getInterval()));
				sb.append(" ");
			} else { // every year
				sb.append(everyOneYear);
				sb.append(" ");
			}
			
			if(!recur.getMonthDayList().isEmpty()) { // in Gen,Feb,...
				sb.append(formatIfNotEmpty(fmtInMonth, byMonthText(recur.getMonthList())));
				sb.append(" ");
			}
			if(!recur.getMonthDayList().isEmpty()) { // on 1,2,3...
				sb.append(formatIfNotEmpty(fmtOn, byMonthDayText(recur.getMonthDayList())));
				sb.append(" ");
			}
			
			appendEndText(sb, recur);
		}
		
		return sb.toString();
	}
	
	private void appendEndText(StringBuilder sb, Recur recur) {
		if(recur.getCount() > 0) {
			sb.append(timesText(recur.getCount()));
		} else if(recur.getUntil() != null) {
			sb.append(untilText(recur.getUntil()));
		}
	}
	
	private String untilText(Date date) {
		DateTimeFormatter fmt = DateTimeUtils.createFormatter(dateFormat, timezone);
		return MessageFormat.format(untilDate, fmt.print(ICal4jUtils.toJodaDateTime(date, DateTimeZone.UTC)));
	}
	
	private String byDayText(WeekDayList days) {
		ArrayList<String> arr = new ArrayList<>();
		if(days.contains(WeekDay.MO)) arr.add(dayMo);
		if(days.contains(WeekDay.TU)) arr.add(dayTu);
		if(days.contains(WeekDay.WE)) arr.add(dayWe);
		if(days.contains(WeekDay.TH)) arr.add(dayTh);
		if(days.contains(WeekDay.FR)) arr.add(dayFr);
		if(days.contains(WeekDay.SA)) arr.add(daySa);
		if(days.contains(WeekDay.SU)) arr.add(daySu);
		return arr.isEmpty() ? "" : StringUtils.join(arr, ",");
	}
	
	private String byMonthText(NumberList months) {
		ArrayList<String> arr = new ArrayList<>();
		if(months.contains(1)) arr.add(monthJan);
		if(months.contains(2)) arr.add(monthFeb);
		if(months.contains(3)) arr.add(monthMar);
		if(months.contains(4)) arr.add(monthApr);
		if(months.contains(5)) arr.add(monthMay);
		if(months.contains(6)) arr.add(monthJun);
		if(months.contains(7)) arr.add(monthJul);
		if(months.contains(8)) arr.add(monthAug);
		if(months.contains(9)) arr.add(monthSep);
		if(months.contains(10)) arr.add(monthOct);
		if(months.contains(11)) arr.add(monthNov);
		if(months.contains(12)) arr.add(monthDec);
		return StringUtils.join(arr, ",");
	}
	
	private String byMonthDayText(NumberList monthDays) {
		ArrayList<String> arr = new ArrayList<>();
		for(Object o : monthDays) {
			int monthDay = (Integer)o;
			arr.add(String.valueOf(monthDay));
		}
		return arr.isEmpty() ? "" : StringUtils.join(arr, ",");
	}
	
	private String byMonthDayAsOrdinalText(NumberList monthDays) {
		ArrayList<String> arr = new ArrayList<>();
		for(Object o : monthDays) {
			int monthDay = (Integer)o;
			if(monthDay == 1) {
				arr.add(byOrdinalFirst);
			} else if(monthDay == 2) {
				arr.add(byOrdinalSecond);
			} else if(monthDay == 3) {
				arr.add(byOrdinalThird);
			} else {
				arr.add(MessageFormat.format(byOrdinalX, monthDay));
			}
		}
		return arr.isEmpty() ? "" : StringUtils.join(arr, ",");
	}
	
	public String timesText(int count) {
		if(count == 1) {
			return forOneTime;
		} else if(count > 1) {
			return MessageFormat.format(forXTimes, count);
		} else {
			return "";
		}
	}
	
	private boolean isByWorkDays(WeekDayList days) {
		return days.containsAll(Arrays.asList(workDays)) && (days.size() == workDays.length);
	}	
	
	private String formatIfNotEmpty(String pattern, String string) {
		return StringUtils.isEmpty(string) ? string : MessageFormat.format(pattern, string);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public String intervalToText(Recur recur) {
		int interval = recur.getInterval();
		switch(recur.getFrequency()) {
			case Recur.DAILY:
				if(interval > 1) {
					return MessageFormat.format(everyXDays, interval);
				} else {
					return everyOneDay;
				}
			case Recur.WEEKLY:
				if(interval > 1) {
					return MessageFormat.format(everyXWeeks, interval);
				} else {
					return everyOneWeek;
				}
			case Recur.MONTHLY:
				return "";
			case Recur.YEARLY:
				return "";
			default:
				return "";
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static String frequencyToText(String frequency, int interval) {
		switch(frequency) {
			case Recur.DAILY:
				return (interval == 1) ? "ogni giorno" : MessageFormat.format("ogni {0} giorni", interval);
			case Recur.WEEKLY:
				return (interval == 1) ? "ogni settimana" : MessageFormat.format("ogni {0} settimana", interval);
			case Recur.MONTHLY:
				return "";
			case Recur.YEARLY:
				return "";
			default:
				return null;
		}
	}
}
