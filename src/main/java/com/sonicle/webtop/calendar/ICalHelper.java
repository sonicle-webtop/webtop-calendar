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

import com.sonicle.webtop.calendar.bol.model.Event;
import com.sonicle.webtop.calendar.bol.model.EventAttendee;
import com.sonicle.webtop.calendar.bol.model.Recurrence;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ICalHelper {
	
	public static ArrayList<Event> parseICal(InputStream is, org.joda.time.DateTimeZone defaultTz) throws ParserException, IOException, Exception {
		// See http://www.kanzaki.com/docs/ical/
		ArrayList<Event> events = new ArrayList<>();
		CalendarBuilder builder = new CalendarBuilder();
		Calendar cal = builder.build(is);
		VEvent ve = null;

		for (Iterator xi = cal.getComponents().iterator(); xi.hasNext();) {
			Component component = (Component) xi.next();
			if (component instanceof VEvent) {
				ve = (VEvent)component;
				events.add(ICalHelper.parseVEvent(ve, defaultTz));
			}
		}
		return events;
	}
	
	public static Event parseVEvent(VEvent ve, org.joda.time.DateTimeZone defaultTz) throws Exception {
		Event event = new Event();
		// See hhttp://www.kanzaki.com/docs/ical/vevent.html
		
		event.setPublicUid(ve.getUid().getValue());
		
		// Extracts and converts date-times
		DtStart start = ve.getStartDate();
		TimeZone startTz = defaultTimeZone(start.getTimeZone(), defaultTz.getID());
		org.joda.time.DateTime dtStart = ICal4jUtils.fromICal4jDate(start.getDate(), startTz);
		
		DtEnd end = ve.getEndDate();
		TimeZone endTz = null;
		org.joda.time.DateTime dtEnd = null;
		if(end == null) { // EndDate can be null
			endTz = startTz;
			dtEnd = dtStart.toDateTime();
		} else {
			endTz = startTz;
			dtEnd = ICal4jUtils.fromICal4jDate(end.getDate(), endTz).withZone(dtStart.getZone());
		}
		if(dtStart.compareTo(dtEnd) > 0) throw new WTException("StartDate [{0}] is not before event EndDate [{1}]", start.toString(), end.toString());
		
		// Apply dates to event
		event.setTimezone(dtStart.getZone().getID());
		if(isAllDay(dtStart, dtEnd)) {
			// Tune-up endDate if we are reading an all-day event
			event.setAllDay(true);
			event.setStartDate(dtStart.withTime(0, 0, 0, 0));
			event.setEndDate(dtStart.withTime(23, 59, 59, 999));
		} else {
			event.setAllDay(false);
			event.setStartDate(dtStart);
			event.setEndDate(dtEnd);
		}
		
		event.setTitle(StringUtils.defaultString(ve.getSummary().getValue()));
		event.setDescription(ve.getDescription().getValue());
		event.setLocation(StringUtils.defaultString(ve.getLocation().getValue()));
		event.setIsPrivate(false);
		event.setBusy(ICalHelper.isBusy(ve.getTransparency()));
		event.setReminder(null);
		event.setActivityId(null);
		event.setCustomerId(null);
		event.setStatisticId(null);
		event.setCausalId(null);
		
		// Extract recurrence
		RRule rr = (RRule)ve.getProperty(Property.RRULE);
		if(rr != null) {
			event.setRecurrence(parseVEventRRule(rr, dtStart.getZone()));
		}
		
		// Extracts attendees
		PropertyList atts = ve.getProperties(Property.ATTENDEE);
		if(!atts.isEmpty()) {
			ArrayList<EventAttendee> attendees = new ArrayList<>();
			for(Object o: atts) {
				attendees.add(parseVEventAttendee((Attendee)o));
			}
			event.setAttendees(attendees);
		}
		
		return event;
	}
	
	public static Recurrence parseVEventRRule(RRule rr, org.joda.time.DateTimeZone etz) throws Exception {
		Recurrence rec = new Recurrence();
		
		Recur recur = rr.getRecur();
		String freq = recur.getFrequency();
		if(freq.equals(Recur.DAILY)) {
			WeekDayList dayList = recur.getDayList();
			if(!dayList.isEmpty()) {
				rec.setType(Recurrence.TYPE_DAILY_FERIALI);
			} else {
				rec.setType(Recurrence.TYPE_DAILY);
				int dfreq = (recur.getInterval() == -1) ? 1 : recur.getInterval();
				rec.setDailyFreq(dfreq);
			}
		} else if(freq.equals(Recur.WEEKLY)) {
			rec.setType(Recurrence.TYPE_WEEKLY);
			
			int wfreq = (recur.getInterval() == -1) ? 1 : recur.getInterval();
			rec.setWeeklyFreq(wfreq);
			rec.setWeeklyDay1(false);
			rec.setWeeklyDay2(false);
			rec.setWeeklyDay3(false);
			rec.setWeeklyDay4(false);
			rec.setWeeklyDay5(false);
			rec.setWeeklyDay6(false);
			rec.setWeeklyDay7(false);
			
			WeekDayList dayList = recur.getDayList();
			if(!dayList.isEmpty()) {
				for(Object o : dayList) {
					WeekDay weekday = (WeekDay)o;
					if(weekday.equals(WeekDay.MO)) rec.setWeeklyDay1(true);
					if(weekday.equals(WeekDay.TU)) rec.setWeeklyDay2(true);
					if(weekday.equals(WeekDay.WE)) rec.setWeeklyDay3(true);
					if(weekday.equals(WeekDay.TH)) rec.setWeeklyDay4(true);
					if(weekday.equals(WeekDay.FR)) rec.setWeeklyDay5(true);
					if(weekday.equals(WeekDay.SA)) rec.setWeeklyDay6(true);
					if(weekday.equals(WeekDay.SU)) rec.setWeeklyDay7(true);
				}
			}
		} else if(freq.equals(Recur.MONTHLY)) {
			rec.setType(Recurrence.TYPE_MONTHLY);
			
			int mfreq = recur.getInterval();
			rec.setMonthlyFreq(mfreq);
			
			NumberList monthDayList = recur.getMonthDayList();
			for(Object o : monthDayList) {
				rec.setMonthlyDay((Integer)o);
			}
		} else if(freq.equals(Recur.YEARLY)) {
			rec.setType(Recurrence.TYPE_YEARLY);
			
			NumberList monthList = recur.getMonthList();
			for(Object o : monthList) {
				rec.setYearlyFreq((Integer)o);
			}
			
			NumberList monthDayList = recur.getMonthDayList();
			for(Object o : monthDayList) {
				rec.setYearlyDay((Integer)o);
			}
		} else { // Frequency type not yet supported...skip RR!
			return null;
		}
		
		if(recur.getCount() != -1) {
			rec.setEndsMode(Recurrence.ENDS_MODE_REPEAT);
			rec.setRepeatTimes(recur.getCount());
		} else if(recur.getUntil() == null) {
			rec.setEndsMode(Recurrence.ENDS_MODE_NEVER);
		} else {
			org.joda.time.DateTime dt = new org.joda.time.DateTime(recur.getUntil(), etz);
			rec.setUntilDate(dt.withTimeAtStartOfDay());
		}
		
		return rec;
		
		/*
                //ricorrenza
                boolean recurrence=false;
                String recurr_type="";
                String until_date_dd="";
                String until_date_mm="";
                String until_date_yyyy="";
                String dayly_freq="";
                String weekly_freq="";
                String weekly_day1="false";
                String weekly_day2="false"; 
                String weekly_day3="false";
                String weekly_day4="false";
                String weekly_day5="false";
                String weekly_day6="false";
                String weekly_day7="false";
                String monthly_month="";
                String monthly_day="";
                String yearly_day="";
                String yearly_month="";
                String repeat="0";
                String permanent="";
                RRule rrule = (RRule) vevent.getProperties().getProperty(Property.RRULE);
                if (rrule!=null){
                    recurrence=true;
                    recurr_type=rrule.getRecur().getFrequency();    //frequenza
                    
                    java.util.Date until =rrule.getRecur().getUntil();  //data di fine
                    if (until!=null){
                        Calendar u=Calendar.getInstance();
                        u.setTime(until);
                        until_date_dd=u.get(Calendar.DAY_OF_MONTH)+"";
                        until_date_mm=u.get(Calendar.MONTH)+1+"";
                        until_date_yyyy=u.get(Calendar.YEAR)+"";
                    }else{
                        permanent="true";
                    }
                    
                    if (recurr_type.equals("DAILY")){
                        
                    }else if (recurr_type.equals("WEEKLY")){

                    }else if (recurr_type.equals("MONTHLY")){
                        
                    }else if (recurr_type.equals("YEARLY")){
                        
                    }
                    
                    if (rrule.getRecur().getCount()!=-1)
                        repeat=String.valueOf(rrule.getRecur().getCount());
                   
                   
                }
                //fine ricorrenza
                
                String recurr_id="";
                String none_recurrence="true";
                if (recurrence){
                    recurr_id=getRecurrId(con);
                    if (permanent != null && permanent.equals("true")) { //se permanente fisso la data 31/12/2022
                        until_date_dd = "31";
                        until_date_mm = "12";
                        until_date_yyyy = "2100";

                    }
                    saveRecurrence(recurr_id,
                                dayly_recurrence,
                                weekly_recurrence,
                                monthly_recurrence,
                                yearly_recurrence,
                                dayly1,
                                dayly_freq,
                                dayly2,
                                weekly_freq,
                                weekly_day1,
                                weekly_day2,
                                weekly_day3,
                                weekly_day4,
                                weekly_day5,
                                weekly_day6,
                                weekly_day7,
                                monthly_day,
                                monthly_month,
                                yearly_day,
                                yearly_month,
                                until_date_yyyy,
                                until_date_mm,
                                until_date_dd,
                                permanent,
                                Integer.parseInt(repeat),
                                sdd+"",
                                smm+"",
                                syyyy+"");
                    none_recurrence="false";
                }
                String repeat_times=repeat.equals("0")?"true":"false";
			*/
	}
	
	public static EventAttendee parseVEventAttendee(Attendee att) throws Exception {
		EventAttendee attendee = new EventAttendee();
		// See http://www.kanzaki.com/docs/ical/attendee.html
		
		// Evaluates attendee details
		// Extract email and common name (CN)
		// Eg: CN=Henry Cabot:MAILTO:hcabot@host2.com -> drop ":MAILTO:"
		URI uri = att.getCalAddress();
		Cn cn = (Cn)att.getParameter(Parameter.CN);
		if((uri != null) && (cn != null)) {
			InternetAddress email = new InternetAddress(uri.getSchemeSpecificPart(), cn.getValue());
			attendee.setRecipient(email.toString());
		} else if(uri != null) {
			attendee.setRecipient(uri.getSchemeSpecificPart());
		} else {
			throw new WTException("Attendee must have a valid address [{0}]", attendee.toString());
		}
		
		// Evaluates attendee role
		Role role = (Role)att.getParameter(Parameter.ROLE);
		if(role != null) {
			if(role.equals(Role.REQ_PARTICIPANT)) {
				attendee.setRecipientType(EventAttendee.RECIPIENT_TYPE_NECESSARY);
			} else {
				attendee.setRecipientType(EventAttendee.RECIPIENT_TYPE_OPTIONAL);
			}
		} else {
			attendee.setRecipientType(EventAttendee.RECIPIENT_TYPE_OPTIONAL);
		}
		
		// Evaluates attendee response status
		PartStat partstat = (PartStat)att.getParameter(Parameter.PARTSTAT);
		if(partstat != null) {
			if(partstat.equals(PartStat.ACCEPTED)) {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_ACCEPTED);
			} else if(partstat.equals(PartStat.TENTATIVE)) {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_TENTATIVE);
			} else if(partstat.equals(PartStat.DECLINED)) {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_DECLINED);
			} else {
				attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_UNKNOWN);
			}
		} else {
			attendee.setResponseStatus(EventAttendee.RESPONSE_STATUS_UNKNOWN);
		}
		
		attendee.setNotify(false);
		
		return attendee;
	}
	
	private static TimeZone defaultTimeZone(TimeZone tz, String defaultTzId) {
		return (tz != null) ? tz : ICal4jUtils.getTimeZone(defaultTzId);
	}
	
	private static boolean isAllDay(org.joda.time.DateTime start, org.joda.time.DateTime end) {
		// Checks if a date range can be considered as an all-day event.
		
		org.joda.time.DateTime dt = null;
		dt = start.withZone(org.joda.time.DateTimeZone.UTC);
		if(!dt.isEqual(dt.withTimeAtStartOfDay())) return false;
		dt = end.withZone(org.joda.time.DateTimeZone.UTC);
		if(!dt.isEqual(dt.withTimeAtStartOfDay())) return false;
		if(start.plusDays(1).getDayOfMonth() != end.getDayOfMonth()) return false;
		return true;
	}
	
	public static boolean isBusy(Transp transparency) {
		return !StringUtils.equals(transparency.getValue(), "TRANSPARENT");
	}
	
	public static void exportICal(OutputStream os, ArrayList<Event> events) throws Exception {
		// Calendar container
		Calendar ical = new Calendar();
		ical.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
		ical.getProperties().add(Version.VERSION_2_0);
		ical.getProperties().add(CalScale.GREGORIAN);
		ical.getProperties().add(Method.REQUEST);
		
		for(Event event : events) {
			ical.getComponents().add(exportEvent(event));
		}
		
		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(ical, os);
	}
	
	public static VEvent exportEvent(Event event) throws Exception {
		org.joda.time.DateTimeZone etz = org.joda.time.DateTimeZone.forID(event.getTimezone());
		Date start = ICal4jUtils.toICal4jDateTime(event.getStartDate(), etz);
		Date end = ICal4jUtils.toICal4jDateTime(event.getEndDate(), etz);
		VEvent ve = new VEvent(start, end, event.getTitle());
		
		//Uid
		ve.getProperties().add(new Uid(event.getPublicUid()));
		
		// Description
		ve.getProperties().add(new Description(event.getDescription()));
		
		// Location
		if(!StringUtils.isEmpty(event.getLocation())) {
			ve.getProperties().add(new Location(event.getLocation()));
		}
		
		// Organizer
		Organizer organizer = new Organizer(URI.create("mailto:dev1@mycompany.com"));
		organizer.getParameters().add(new Cn("Organizer Name"));
		ve.getProperties().add(organizer);
		
		// Recerrence
		if(event.hasRecurrence()) {
			ve.getProperties().add(exportEventRecurrence(event));
		}
		
		// Attendees
		for(EventAttendee attendee : event.getAttendees()) {
			try {
				if(attendee.hasEmailRecipient()) {
					ve.getProperties().add(exportEventAttendee(attendee));
				}
			} catch(URISyntaxException | AddressException ex) {
				/* Do nothing...*/
			}
		}
		
		return ve;
	}
	
	public static RRule exportEventRecurrence(Event event) throws ParseException {
		return new RRule(event.getRecurrence().getRRule());
	}
	
	public static Attendee exportEventAttendee(EventAttendee attendee) throws Exception {
		Attendee att = new Attendee();
		
		// Evaluates attendee details
		// Joins email and common name (CN)
		InternetAddress email = new InternetAddress(attendee.getRecipient());
		att.setCalAddress(new URI(MessageFormat.format("mailto:{0}", email.getAddress())));
		if(!StringUtils.isEmpty(email.getPersonal())) {
			att.getParameters().add(new Cn(email.getPersonal()));
		}
		
		// Evaluates attendee role
		String rpcType = attendee.getRecipientType();
		if(rpcType.equals(EventAttendee.RECIPIENT_TYPE_NECESSARY)) {
			att.getParameters().add(Role.REQ_PARTICIPANT);
		} else {
			att.getParameters().add(Role.OPT_PARTICIPANT);
		}
		
		// Evaluates attendee response status
		String status = attendee.getResponseStatus();
		if(status.equals(EventAttendee.RESPONSE_STATUS_ACCEPTED)) {
			att.getParameters().add(PartStat.ACCEPTED);
		} else if(status.equals(EventAttendee.RESPONSE_STATUS_TENTATIVE)) {
			att.getParameters().add(PartStat.TENTATIVE);
		} else if(status.equals(EventAttendee.RESPONSE_STATUS_DECLINED)) {
			att.getParameters().add(PartStat.DECLINED);
		} else {
			att.getParameters().add(PartStat.NEEDS_ACTION);
		}
		
		return att;
	}
}
