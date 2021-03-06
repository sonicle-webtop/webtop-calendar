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
package com.sonicle.webtop.calendar;

import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import jakarta.mail.internet.AddressException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

/**
 * TODO: Organize this like ICalendarParser...
 * @author malbinola
 */
public class ICalHelper {
	
	/*
	public static Calendar toCalendar(Method method, String prodId, Event event) throws WTException {
		return toCalendar(method, prodId, Arrays.asList(event));
	}
	
	public static Calendar toCalendar(String prodId, Collection<Event> events) throws WTException {
		return toCalendar(null, prodId, events);
	}
	
	public static Calendar toCalendar(Method calMethod, String prodId, Collection<Event> events) throws WTException {
		//org.joda.time.DateTime now = DateTimeUtils.now();
		Calendar ical = null;
		if (calMethod == null) {
			ical = ICalendarUtils.newCalendar(prodId, null);
		} else {
			ensureMethodRequestOrCancel(calMethod);
			ical = ICalendarUtils.newCalendar(prodId, calMethod);
		}
		
		for(Event event : events) {
			ical.getComponents().add(toVEvent(calMethod, event));
		}
		return ical;
	}
	
	public static VEvent toVEvent(Method calMethod, Event event) throws WTException {
		Date start = ICal4jUtils.toIC4jDateTimeUTC(event.getStartDate());
		Date end = ICal4jUtils.toIC4jDateTimeUTC(event.getEndDate());
		VEvent ve = new VEvent(start, end, event.getTitle());
		
		// Status: meeting status
		if (Method.REQUEST.equals(calMethod)) {
			ICal4jUtils.addProperty(ve, Status.VEVENT_CONFIRMED);
		} else if (Method.CANCEL.equals(calMethod)) {
			ICal4jUtils.addProperty(ve, Status.VEVENT_CANCELLED);
		}
		
		// Uid: globally unique identifier
		// http://www.kanzaki.com/docs/ical/uid.html
		ICal4jUtils.addProperty(ve, new Uid(event.getPublicUid()));
		
		// LastModified: the date and time of the last information revision
		// http://www.kanzaki.com/docs/ical/lastModified.html
		ICal4jUtils.addProperty(ve, new LastModified(ICal4jUtils.createDateTime(event.getRevisionTimestamp().withZone(DateTimeZone.UTC))));
		
		// Sequence: the revision sequence number
		// http://www.kanzaki.com/docs/ical/sequence.html
		ICal4jUtils.addProperty(ve, new Sequence(0));
		
		// Description: the complete description
		// http://www.kanzaki.com/docs/ical/description.html
		ICal4jUtils.addProperty(ve, new Description(event.getDescription()));
		
		// Location: the intended venue for the activity
		// http://www.kanzaki.com/docs/ical/location.html
		if (!StringUtils.isEmpty(event.getLocation())) {
			ICal4jUtils.addProperty(ve, new Location(event.getLocation()));
		}
		
		// Transparency: defines whether an event is transparent or not to busy time searches
		// http://www.kanzaki.com/docs/ical/transp.html
		ICal4jUtils.addProperty(ve, event.getBusy() ? Transp.OPAQUE : Transp.TRANSPARENT);
		
		// Organizer
		String mailto = "mailto:" + event.getOrganizerAddress();
		Organizer organizer = new Organizer(URI.create(mailto));
		if (!StringUtils.isBlank(event.getOrganizerCN())) {
			organizer.getParameters().add(new Cn(event.getOrganizerCN()));
		}
		ve.getProperties().add(organizer);
		
		// Attendees
		for(EventAttendee attendee : event.getAttendees()) {
			try {
				if(attendee.hasEmailRecipient()) {
					ve.getProperties().add(toAttendee(calMethod, attendee));
				}
			} catch(AddressException ex) {
				
			}
		}
		
		// Recerrence
		if (event.hasRecurrence()) {
			try {
				ve.getProperties().add(exportEventRecurrence(event));
			} catch(ParseException ex) {
				throw new WTException(ex, "Unable to add recurrence");
			}
		}
		
		return ve;
	}
	
	public static Attendee toAttendee(Method calMethod, EventAttendee attendee) throws AddressException {
		Attendee att = new Attendee();
		ParameterList params = att.getParameters();
		
		// CN and email: attendee main details
		String mailto = "mailto:" + attendee.getAddress();
		att.setCalAddress(URI.create(mailto));
		if (!StringUtils.isBlank(attendee.getCN())) {
			params.add(new Cn(attendee.getCN()));
		}
		
		// CuType: calendar user type (INDIVIDUAL, RESOURCE, etc...)
		// http://www.kanzaki.com/docs/ical/cutype.html
		params.add(recipientTypeToCuType(attendee.getRecipientType()));
		
		// Role: attendee participation role
		// http://www.kanzaki.com/docs/ical/role.html
		params.add(recipientRoleToRole(attendee.getRecipientRole()));
		
		// PartStat: participation status for the calendar user
		// http://www.kanzaki.com/docs/ical/partstat.html
		params.add(responseStatusToPartStat(attendee.getResponseStatus()));
		
		if (Method.REQUEST.equals(calMethod)) {
			params.add(Rsvp.TRUE);
		}
		return att;
	}
	
	public static RRule exportEventRecurrence(Event event) throws ParseException {
		return new RRule(event.getRecurrenceRule());
	}
	
	public static Attendee exportEventAttendee(boolean method, EventAttendee attendee) throws Exception {
		Attendee att = new Attendee();
		ParameterList params = att.getParameters();
		
		// CN and email: attendee main details
		String mailto = "mailto:" + attendee.getAddress();
		att.setCalAddress(URI.create(mailto));
		if (!StringUtils.isBlank(attendee.getCN())) {
			params.add(new Cn(attendee.getCN()));
		}
		
		// CuType: calendar user type (INDIVIDUAL, RESOURCE, etc...)
		// http://www.kanzaki.com/docs/ical/cutype.html
		params.add(recipientTypeToCuType(attendee.getRecipientType()));
		
		// Role: attendee participation role
		// http://www.kanzaki.com/docs/ical/role.html
		params.add(recipientRoleToRole(attendee.getRecipientRole()));
		
		// PartStat: participation status for the calendar user
		// http://www.kanzaki.com/docs/ical/partstat.html
		params.add(responseStatusToPartStat(attendee.getResponseStatus()));
		
		if (method) params.add(Rsvp.TRUE);
		
		return att;
	}
	
	public static Role recipientRoleToRole(String recipientRole) {
		if (StringUtils.equals(recipientRole, EventAttendee.RECIPIENT_ROLE_CHAIR)) {
			return Role.CHAIR;
		} else if (StringUtils.equals(recipientRole, EventAttendee.RECIPIENT_ROLE_REQUIRED)) {
			return Role.REQ_PARTICIPANT;
		} else if (StringUtils.equals(recipientRole, EventAttendee.RECIPIENT_ROLE_OPTIONAL)) {
			return Role.OPT_PARTICIPANT;
		} else {
			return Role.REQ_PARTICIPANT;
		}
	}
	
	public static String roleToRecipientRole(Role role) {
		if (role != null) {
			if (role.equals(Role.CHAIR)) {
				return EventAttendee.RECIPIENT_ROLE_CHAIR;
			} else if (role.equals(Role.REQ_PARTICIPANT)) {
				return EventAttendee.RECIPIENT_ROLE_REQUIRED;
			} else if (role.equals(Role.REQ_PARTICIPANT)) {
				return EventAttendee.RECIPIENT_ROLE_OPTIONAL;
			}
		}
		return EventAttendee.RECIPIENT_ROLE_OPTIONAL;
	}
	
	public static CuType recipientTypeToCuType(String recipientType) {
		if (StringUtils.equals(recipientType, EventAttendee.RECIPIENT_TYPE_INDIVIDUAL)) {
			return CuType.INDIVIDUAL;
		} else if (StringUtils.equals(recipientType, EventAttendee.RECIPIENT_TYPE_RESOURCE)) {
			return CuType.RESOURCE;
		} else {
			return CuType.INDIVIDUAL;
		}
	}
	
	public static String cuTypeToRecipientType(CuType cuType) {
		if (cuType != null) {
			if (cuType.equals(CuType.INDIVIDUAL)) {
				return EventAttendee.RECIPIENT_TYPE_INDIVIDUAL;
			} else if (cuType.equals(CuType.RESOURCE)) {
				return EventAttendee.RECIPIENT_TYPE_RESOURCE;
			} else if (cuType.equals(CuType.ROOM)) {
				return EventAttendee.RECIPIENT_TYPE_RESOURCE;
			}
		}
		return EventAttendee.RECIPIENT_TYPE_RESOURCE;
	}
	
	public static PartStat responseStatusToPartStat(String responseStatus) {
		if (StringUtils.equals(responseStatus, EventAttendee.RESPONSE_STATUS_ACCEPTED)) {
			return PartStat.ACCEPTED;
		} else if (StringUtils.equals(responseStatus, EventAttendee.RESPONSE_STATUS_TENTATIVE)) {
			return PartStat.TENTATIVE;
		} else if (StringUtils.equals(responseStatus, EventAttendee.RESPONSE_STATUS_DECLINED)) {
			return PartStat.DECLINED;
		} else {
			return PartStat.NEEDS_ACTION;
		}
	}
	
	public static String partStatToResponseStatus(PartStat partStat) {
		if (partStat.equals(PartStat.ACCEPTED)) {
			return EventAttendee.RESPONSE_STATUS_ACCEPTED;
		} else if (partStat.equals(PartStat.TENTATIVE)) {
			return EventAttendee.RESPONSE_STATUS_TENTATIVE;
		} else if (partStat.equals(PartStat.DECLINED)) {
			return EventAttendee.RESPONSE_STATUS_DECLINED;
		} else {
			return EventAttendee.RESPONSE_STATUS_NEEDSACTION;
		}
	}
	
	private static void ensureMethodRequestOrCancel(Method method) throws WTException {
		if (!Method.REQUEST.equals(method) && !Method.CANCEL.equals(method)) {
			throw new WTException("Invalid method: only REQUEST or CANCEL are supported");
		}
	}
	*/
}
