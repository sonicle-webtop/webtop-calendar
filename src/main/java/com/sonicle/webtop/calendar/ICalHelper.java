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
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
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
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class ICalHelper {
	
	public static ArrayList<Event> parseICal(InputStream is) throws ParserException, IOException, Exception {
		// See http://www.kanzaki.com/docs/ical/
		ArrayList<Event> events = new ArrayList<>();
		CalendarBuilder builder = new CalendarBuilder();
		Calendar cal = builder.build(is);
		VEvent ve = null;
		Event event = null;

		for (Iterator xi = cal.getComponents().iterator(); xi.hasNext();) {
			Component component = (Component) xi.next();
			if (component instanceof VEvent) {
				ve = (VEvent)component;
				ICalHelper.parseVEvent(ve);
				events.add(event);
			}
		}
		return events;
	}
	
	public static void parseVEvent(VEvent ve) throws Exception {
		Event event = new Event();
		// See hhttp://www.kanzaki.com/docs/ical/vevent.html
		
		event.setPublicUid(ve.getUid().getValue());
		
		// Extracts and converts date-times
		DtStart start = ve.getStartDate();
		TimeZone startTz = defaultTimeZone(start.getTimeZone());
		org.joda.time.DateTime dtStart = ICal4jUtils.fromICal4jDate(start.getDate(), startTz);
		
		DtEnd end = ve.getEndDate();
		TimeZone endTz = null;
		org.joda.time.DateTime dtEnd = null;
		if(end == null) { // EndDate can be null
			endTz = startTz;
			dtEnd = dtStart.toDateTime();
		} else {
			endTz = defaultTimeZone(end.getTimeZone());
			dtEnd = ICal4jUtils.fromICal4jDate(end.getDate(), endTz).withZone(dtStart.getZone());
		}
		if(dtStart.compareTo(dtEnd) > 0) throw new WTException("StartDate [{0}] is not before event EndDate [{1}]", start.toString(), end.toString());
		
		// Apply dates to event
		event.setTimezone(dtStart.getZone().getID());
		if(isAllDay(dtStart, dtEnd, ve.getTransparency())) {
			// Tune-up endDate if we are reading an all-day event
			event.setAllDay(true);
			event.setStartDate(dtStart);
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
		event.setBusy(false);
		event.setReminder(null);
		event.setActivityId(null);
		event.setCustomerId(null);
		event.setStatisticId(null);
		event.setCausalId(null);
		
		// Extract recurrence
		RRule rr = (RRule)ve.getProperty(Property.RRULE);
		if(rr != null) {
			parseVEventRRule(rr, event);
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
	}
	
	public static void parseVEventRRule(RRule rr, Event event) throws Exception {
		//TODO: completare il parsing della RRule
		
		String freq = rr.getRecur().getFrequency();
		if(freq.equals(Recur.DAILY)) {
			
			
			
		} else if(freq.equals(Recur.WEEKLY)) {
			
			
			
		} else if(freq.equals(Recur.MONTHLY)) {
			
			
			
		} else if(freq.equals(Recur.YEARLY)) {
			
			
			
		} else { // Frequency type not yet supported...skip RR!
			return;
		}
		
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
                        WeekDayList dayList = rrule.getRecur().getDayList();
                        if (!dayList.isEmpty()){ //ricorrenza giornaliera feriale
                            recurr_type="F";
                            dayly1="true";
                        }else{
                            recurr_type="D"; //ricorrenza giornaliera con intervallo  
                            dayly2="true";
                            dayly_freq=String.valueOf(rrule.getRecur().getInterval());
                            if (dayly_freq.equals("-1")) dayly_freq="1";
                        }
                        dayly_recurrence="true";
                    }else if (recurr_type.equals("WEEKLY")){
                        recurr_type="W";
                        weekly_freq=String.valueOf(rrule.getRecur().getInterval());
                        if (weekly_freq.equals("-1")) weekly_freq="1";
                        WeekDayList dayList = rrule.getRecur().getDayList();
                        if (!dayList.isEmpty()){
                            for (Object o:dayList){
                                WeekDay weekday=(WeekDay)o;
                                if (weekday.getDay().equals("MO")) weekly_day1="true";
                                if (weekday.getDay().equals("TU")) weekly_day2="true"; 
                                if (weekday.getDay().equals("WE")) weekly_day3="true";
                                if (weekday.getDay().equals("TH")) weekly_day4="true";
                                if (weekday.getDay().equals("FR")) weekly_day5="true";
                                if (weekday.getDay().equals("SA")) weekly_day6="true";
                                if (weekday.getDay().equals("SU")) weekly_day7="true";
                            }
                        }
                        weekly_recurrence="true";
                    }else if (recurr_type.equals("MONTHLY")){
                        recurr_type="M";
                        monthly_month=String.valueOf(rrule.getRecur().getInterval());
                        NumberList monthDayList = rrule.getRecur().getMonthDayList();
                        for (Object o:monthDayList){
                            monthly_day=String.valueOf((Integer)o);
                        }
                        monthly_recurrence="true";
                    }else if (recurr_type.equals("YEARLY")){
                        recurr_type="Y";
                        NumberList monthList = rrule.getRecur().getMonthList();
                        for (Object o:monthList){
                            yearly_month=String.valueOf((Integer)o);
                        }
                        NumberList monthDayList = rrule.getRecur().getMonthDayList();
                        for (Object o:monthDayList){
                            yearly_day=String.valueOf((Integer)o);
                        }
                        yearly_recurrence="true";
                    }
                    
                    if (rrule.getRecur().getCount()!=-1)
                        repeat=String.valueOf(rrule.getRecur().getCount());
                   
                   
                }
                //fine ricorrenza
                boolean isplanning=false;
                PropertyList pl=vevent.getProperties("ATTENDEE");
                boolean attendees=false;
                description=description.trim();
                if (pl!=null) {
                    for(Object op: pl) {
                        Property p=(Property)op;
                        Parameter pcn=p.getParameter("CN");
                        if (pcn!=null) {
                            String cn=pcn.getValue();
                            String vcn[]=cn.split(":");
                            if (vcn.length>0) {
                                if (!attendees) {
                                    if (description.length()>0) {
                                        if (!description.endsWith("\n")) description+="\n";
                                        description+="\n";
                                    }
                                }
                                attendees=true;
                                Planning partecipant=new Planning("false",vcn[0],"N","none","true");
                                partecipantList.add(partecipant);
                                isplanning=true;
                            }
                            
                        }
                    }
                }
                
                
            
                
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
	
	private static TimeZone defaultTimeZone(TimeZone tz) {
		return (tz == null) ? ICal4jUtils.getTimeZone("UTC") : tz;
	}
	
	private static boolean isAllDay(org.joda.time.DateTime start, org.joda.time.DateTime end, Transp transparency) {
		// Checks if a date range can be considered as an all-day event.
		
		org.joda.time.DateTime dt = null;
		dt = start.withZone(DateTimeZone.UTC);
		if(!dt.isEqual(dt.withTimeAtStartOfDay())) return false;
		dt = end.withZone(DateTimeZone.UTC);
		if(!dt.isEqual(dt.withTimeAtStartOfDay())) return false;
		if(!StringUtils.equals(transparency.getValue(), "TRANSP")) return false;
		return true;
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
		if(Event.hasRecurrence(event)) {
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
		return new RRule(event.rrRule);
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
