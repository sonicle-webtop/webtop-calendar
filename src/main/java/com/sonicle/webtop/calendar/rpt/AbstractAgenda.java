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
package com.sonicle.webtop.calendar.rpt;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.calendar.CalendarManager;
import com.sonicle.webtop.calendar.bol.model.CalendarFolderData;
import com.sonicle.webtop.calendar.bol.model.RBAgendaEvent;
import com.sonicle.webtop.calendar.bol.VVEventInstance;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.calendar.model.FolderEventInstances;
import com.sonicle.webtop.calendar.model.FolderEvents;
import com.sonicle.webtop.calendar.IEvent;
import com.sonicle.webtop.calendar.model.SchedEventInstance;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 *
 * @author malbinola
 */
public abstract class AbstractAgenda extends AbstractReport {
	//protected HashMap<Integer, CalendarFolderData> folderData;
	abstract Collection<?> createBeanCollection(Data data);

	public AbstractAgenda(ReportConfig config) {
		super(config);
		//this.folderData = new HashMap<>();
	}
	
	/*
	public void addFolderData(int calendarId, CalendarFolderData folderData) {
		this.folderData.put(calendarId, folderData);
	}
	*/
	
	public void setDataSource(CalendarManager manager, DateTime fromDate, DateTime toDate, DateTimeZone utz, List<FolderEventInstances> folderEvents) throws WTException {
		int days = -1;
		if(DateTimeUtils.isEndOfDay(toDate, true)) {
			days = Days.daysBetween(fromDate, toDate).getDays()+1;
		} else if(DateTimeUtils.isMidnight(toDate)) {
			days = Days.daysBetween(fromDate, toDate).getDays();
		}
		
		// Expands all events
		HashMap<Integer, Calendar> calendars = new HashMap<>();
		ArrayList<SchedEventInstance> events = new ArrayList<>();
		for(FolderEventInstances fei : folderEvents) {
			calendars.put(fei.folder.getCalendarId(), fei.folder);
			events.addAll(fei.instances);
		}
		
		// Sorts events by their startDate
		Collections.sort(events, new Comparator<SchedEventInstance>() {
			@Override
			public int compare(final SchedEventInstance se1, final SchedEventInstance se2) {
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
		for(SchedEventInstance sei : events) {
			for(int i=0; i<days; i++) {
				dayDateFrom = fromDate.plusDays(i);
				if(isInDay(utz, dayDateFrom, sei)) {
					Calendar calendar = calendars.get(sei.getCalendarId());
					boolean spanning = true;
					Integer spanLeft = null, spanRight  = null;
					if(!sei.getAllDay() && startsInDay(utz, dayDateFrom, sei) && endsInDay(utz, dayDateFrom, sei)) {
						spanning = false;
					} else {
						if(startsInDay(utz, dayDateFrom, sei)) {
							spanRight = DateTimeUtils.datesBetween(dayDateFrom, sei.getEndDate().withZone(utz));
						}
						if(endsInDay(utz, dayDateFrom, sei)) {
							spanLeft = DateTimeUtils.datesBetween(sei.getStartDate().withZone(utz), dayDateFrom);
						}
						if(!startsInDay(utz, dayDateFrom, sei) && !endsInDay(utz, dayDateFrom, sei)) {
							spanLeft = DateTimeUtils.datesBetween(sei.getStartDate().withZone(utz), dayDateFrom);
							spanRight = DateTimeUtils.datesBetween(dayDateFrom, sei.getEndDate().withZone(utz));
						}
					}
					if(spanning) {
						daysSpanningEvents.get(i).add(new RBAgendaEvent(calendar, sei, spanLeft, spanRight));
					} else {
						daysEvents.get(i).add(new RBAgendaEvent(calendar, sei, spanLeft, spanRight));
					}
				}
			}
		}
		
		setDataSource(createBeanCollection(new Data(utz, fromDate.toLocalDate(), toDate.minusDays(1).toLocalDate(), dayDates, daysSpanningEvents, daysEvents)));
	}
	
	private boolean startsInDay(DateTimeZone utz, DateTime dayDate, IEvent event) {
		return DateTimeUtils.startsInDay(dayDate, event.getStartDate().withZone(utz));
	}
	
	private boolean endsInDay(DateTimeZone utz, DateTime dayDate, IEvent event) {
		return DateTimeUtils.endsInDay(dayDate, event.getEndDate().withZone(utz));
	}
	
	private boolean isInDay(DateTimeZone utz, DateTime dayDate, IEvent event) {
		// NB: dayDate must be at midnight!!
		DateTime dayDateTo = dayDate.plusDays(1);
		DateTime start = event.getStartDate().withZone(utz);
		DateTime end = event.getEndDate().withZone(utz);
		
		if (startsInDay(utz, dayDate, event)) return true;
		if (endsInDay(utz, dayDate, event)) return true;
		if ((start.compareTo(dayDate) <= 0) && (end.compareTo(dayDateTo) >= 0)) return true;
		return false;
	}
	
	protected static class Data {
		DateTimeZone utz;
		LocalDate fromDate;
		LocalDate toDate;
		ArrayList<Date> dayDates;
		ArrayList<ArrayList<RBAgendaEvent>> daysSpanningEvents;
		ArrayList<ArrayList<RBAgendaEvent>> daysEvents;
		
		public Data(DateTimeZone utz, LocalDate fromDate, LocalDate toDate, ArrayList<Date> dayDates, ArrayList<ArrayList<RBAgendaEvent>> daysSpanningEvents, ArrayList<ArrayList<RBAgendaEvent>> daysEvents) {
			this.utz = utz;
			this.fromDate = fromDate;
			this.toDate = toDate;
			this.dayDates = dayDates;
			this.daysSpanningEvents = daysSpanningEvents;
			this.daysEvents = daysEvents;
		}
	} 
}
