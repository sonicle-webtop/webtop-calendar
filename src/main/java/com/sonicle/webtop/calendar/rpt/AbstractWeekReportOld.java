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
package com.sonicle.webtop.calendar.rpt;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.calendar.bol.model.RBAgendaEvent;
import com.sonicle.webtop.calendar.bol.model.BeanDayAgenda;
import com.sonicle.webtop.calendar.bol.model.BeanWeekAgenda;
import com.sonicle.webtop.calendar.bol.model.SchedulerEvent;
import com.sonicle.webtop.core.io.AbstractReport;
import com.sonicle.webtop.core.io.ReportConfig;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 *
 * @author malbinola
 */
public abstract class AbstractWeekReportOld extends AbstractReport {

	public AbstractWeekReportOld(ReportConfig config) {
		super(config);
	}
	
	public void setDataSource(CalendarManager manager, DateTime fromDate, DateTime toDate, DateTimeZone utz, List<CalendarManager.CalendarEvents> calendarEvents) throws WTException {
		int days = -1;
		if(DateTimeUtils.isEndOfDay(toDate, true)) {
			days = Days.daysBetween(fromDate, toDate).getDays()+1;
		} else if(DateTimeUtils.isMidnight(toDate)) {
			days = Days.daysBetween(fromDate, toDate).getDays();
		}
		
		// Expands all events
		HashMap<Integer, OCalendar> calendars = new HashMap<>();
		ArrayList<SchedulerEvent> events = new ArrayList<>();
		for(CalendarManager.CalendarEvents ce : calendarEvents) {
			calendars.put(ce.calendar.getCalendarId(), ce.calendar);
			for(SchedulerEvent se : ce.events) {
				if(se.getRecurrenceId() == null) {
					events.add(se);
				} else {
					events.addAll(manager.calculateRecurringInstances(se, fromDate, toDate, utz));
				}
			}
		}
		
		// Sorts events by their startDate
		Collections.sort(events, new Comparator<SchedulerEvent>() {
			@Override
			public int compare(final SchedulerEvent se1, final SchedulerEvent se2) {
				return se1.getStartDate().compareTo(se2.getStartDate());
			}
		});
		
		DateTime dayDateFrom = null;
		ArrayList<Date> dayDates = new ArrayList<>();
		ArrayList<ArrayList<RBAgendaEvent>> daysSpanningEvents = new ArrayList<>();
		ArrayList<ArrayList<RBAgendaEvent>> daysEvents = new ArrayList<>();
		
		// Prepare structures...
		for(int i=0; i<days; i++) {
			dayDateFrom = fromDate.plusDays(i);
			dayDates.add(dayDateFrom.toDate());
			daysSpanningEvents.add(new ArrayList<RBAgendaEvent>());
			daysEvents.add(new ArrayList<RBAgendaEvent>());
		}
		
		// Arranges events by day...
		for(SchedulerEvent se : events) {
			for(int i=0; i<days; i++) {
				dayDateFrom = fromDate.plusDays(i);
				if(isInDay(utz, dayDateFrom, se)) {
					OCalendar calendar = calendars.get(se.getCalendarId());
					boolean spanning = true;
					Integer spanLeft = null, spanRight  = null;
					if(!se.getAllDay() && startsInDay(utz, dayDateFrom, se) && endsInDay(utz, dayDateFrom, se)) {
						spanning = false;
					} else {
						if(startsInDay(utz, dayDateFrom, se)) {
							spanRight = DateTimeUtils.datesBetween(dayDateFrom, se.getEndDate().withZone(utz));
						}
						if(endsInDay(utz, dayDateFrom, se)) {
							spanLeft = DateTimeUtils.datesBetween(se.getStartDate().withZone(utz), dayDateFrom);
						}
						if(!startsInDay(utz, dayDateFrom, se) && !endsInDay(utz, dayDateFrom, se)) {
							spanLeft = DateTimeUtils.datesBetween(se.getStartDate().withZone(utz), dayDateFrom);
							spanRight = DateTimeUtils.datesBetween(dayDateFrom, se.getEndDate().withZone(utz));
						}
					}
					if(spanning) {
						daysSpanningEvents.get(i).add(new RBAgendaEvent(calendar, se, spanLeft, spanRight));
					}
					daysEvents.get(i).add(new RBAgendaEvent(calendar, se, spanLeft, spanRight));
				}
			}
		}
		ArrayList<BeanDayAgenda> dayItems = new ArrayList<>();
		for(int i=0; i<dayDates.size(); i++) {
			dayItems.add(new BeanDayAgenda(dayDates.get(i), daysSpanningEvents.get(i), daysEvents.get(i)));
		}
		
		ArrayList<BeanWeekAgenda> items = new ArrayList<>();
		items.add(new BeanWeekAgenda(fromDate.toDate(), toDate.toDate(), utz.getID(), dayItems));
		setDataSource(new JRBeanCollectionDataSource(items));
	}
	
	private boolean startsInDay(DateTimeZone utz, DateTime dayDate, SchedulerEvent se) {
		return DateTimeUtils.startsInDay(dayDate, se.getStartDate().withZone(utz));
		// NB: dayDate must be at midnight!!
		//DateTime dayDateTo = dayDate.plusDays(1);
		//DateTime start = se.getStartDate().withZone(utz);
		//return (start.compareTo(dayDate) >= 0) && start.isBefore(dayDateTo);
	}
	
	private boolean endsInDay(DateTimeZone utz, DateTime dayDate, SchedulerEvent se) {
		return DateTimeUtils.endsInDay(dayDate, se.getEndDate().withZone(utz));
		// NB: dayDate must be at midnight!!
		//DateTime dayDateTo = dayDate.plusDays(1);
		//DateTime end = se.getEndDate().withZone(utz);
		//return end.isAfter(dayDate) && end.isBefore(dayDateTo); // We need to exclude midnight!!
	}
	
	private boolean isInDay(DateTimeZone utz, DateTime dayDate, SchedulerEvent se) {
		// NB: dayDate must be at midnight!!
		DateTime dayDateTo = dayDate.plusDays(1);
		DateTime start = se.getStartDate().withZone(utz);
		DateTime end = se.getEndDate().withZone(utz);
		
		if(startsInDay(utz, dayDate, se)) return true;
		if(endsInDay(utz, dayDate, se)) return true;
		if((start.compareTo(dayDate) <= 0) && (end.compareTo(dayDateTo) >= 0)) return true;
		return false;
	}
	
	
	
	
	
	private boolean isMultiDay(DateTimeZone utz, SchedulerEvent se) {
		LocalDate start = se.getStartDate().withZone(utz).toLocalDate();
		LocalDate end = se.getEndDate().withZone(utz).toLocalDate();
		return start.compareTo(end) != 0;
		//return Minutes.minutesBetween(se.getStartDate(), se.getEndDate()).getMinutes() > 1440;
	}
}
