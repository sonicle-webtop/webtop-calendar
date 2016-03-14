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

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author malbinola
 */
public class RBAgendaWeek5 {
	public String timezone;
	public Date fromDate;
	public Date toDate;
	public Date day1Date;
	public ArrayList<RBAgendaEvent> day1SpanningEvents;
	public ArrayList<RBAgendaEvent> day1Events;
	public Date day2Date;
	public ArrayList<RBAgendaEvent> day2SpanningEvents;
	public ArrayList<RBAgendaEvent> day2Events;
	public Date day3Date;
	public ArrayList<RBAgendaEvent> day3SpanningEvents;
	public ArrayList<RBAgendaEvent> day3Events;
	public Date day4Date;
	public ArrayList<RBAgendaEvent> day4SpanningEvents;
	public ArrayList<RBAgendaEvent> day4Events;
	public Date day5Date;
	public ArrayList<RBAgendaEvent> day5SpanningEvents;
	public ArrayList<RBAgendaEvent> day5Events;
	
	public RBAgendaWeek5(String timezone, Date fromDate, Date toDate, ArrayList<Date> dayDates, ArrayList<ArrayList<RBAgendaEvent>> daySpanningEvents, ArrayList<ArrayList<RBAgendaEvent>> dayEvents) {
		this.timezone = timezone;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.day1Date = dayDates.get(0);
		this.day1SpanningEvents = daySpanningEvents.get(0);
		this.day1Events = dayEvents.get(0);
		this.day2Date = dayDates.get(1);
		this.day2SpanningEvents = daySpanningEvents.get(1);
		this.day2Events = dayEvents.get(1);
		this.day3Date = dayDates.get(2);
		this.day3SpanningEvents = daySpanningEvents.get(2);
		this.day3Events = dayEvents.get(2);
		this.day4Date = dayDates.get(3);
		this.day4SpanningEvents = daySpanningEvents.get(3);
		this.day4Events = dayEvents.get(3);
		this.day5Date = dayDates.get(4);
		this.day5SpanningEvents = daySpanningEvents.get(4);
		this.day5Events = dayEvents.get(4);
	}

	public String getTimezone() {
		return timezone;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public Date getDay1Date() {
		return day1Date;
	}

	public ArrayList<RBAgendaEvent> getDay1SpanningEvents() {
		return day1SpanningEvents;
	}

	public ArrayList<RBAgendaEvent> getDay1Events() {
		return day1Events;
	}

	public Date getDay2Date() {
		return day2Date;
	}

	public ArrayList<RBAgendaEvent> getDay2SpanningEvents() {
		return day2SpanningEvents;
	}

	public ArrayList<RBAgendaEvent> getDay2Events() {
		return day2Events;
	}

	public Date getDay3Date() {
		return day3Date;
	}

	public ArrayList<RBAgendaEvent> getDay3SpanningEvents() {
		return day3SpanningEvents;
	}

	public ArrayList<RBAgendaEvent> getDay3Events() {
		return day3Events;
	}

	public Date getDay4Date() {
		return day4Date;
	}

	public ArrayList<RBAgendaEvent> getDay4SpanningEvents() {
		return day4SpanningEvents;
	}

	public ArrayList<RBAgendaEvent> getDay4Events() {
		return day4Events;
	}

	public Date getDay5Date() {
		return day5Date;
	}

	public ArrayList<RBAgendaEvent> getDay5SpanningEvents() {
		return day5SpanningEvents;
	}

	public ArrayList<RBAgendaEvent> getDay5Events() {
		return day5Events;
	}
}
