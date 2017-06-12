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
package com.sonicle.webtop.calendar.bol.model;

import com.sonicle.webtop.calendar.model.EventInstance;
import com.sonicle.webtop.calendar.model.EventAttendee;
import com.sonicle.webtop.calendar.RRuleStringify;
import com.sonicle.webtop.calendar.model.Calendar;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.model.Activity;
import com.sonicle.webtop.core.model.Causal;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.JRHelper;
import java.awt.Image;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author malbinola
 */
public class RBEventDetail {
	public Integer calendarId;
	public String calendarName;
	public String calendarColor;
	public Image calendarColorImage;
	public String eventKey;
	public Integer eventId;
	public Date startDate;
	public Date endDate;
	public String timezone;
	public Boolean allDay;
	public String title;
	public String description;
	public String location;
	public String recurrenceType;
	public String recurrenceDescription;
	public Boolean isPrivate;
	public Boolean isBusy;
	public Integer reminder;
	public Integer activityId;
	public String activityDescription;
	public String masterDataId;
	public String masterDataDescription;
	public String statMasterDataId;
	public String statMasterDataDescription;
	public Integer causalId;
	public String causalDescription;
	public String organizer;
	public ArrayList<Attendee> attendees;
	
	public RBEventDetail(CoreManager coreMgr, RRuleStringify rrStringify, Calendar calendar, EventInstance event) throws WTException {
		this.calendarId = event.getCalendarId();
		this.calendarName = calendar.getName();
		this.calendarColor = calendar.getColor();
		this.calendarColorImage = JRHelper.colorAsImage(Calendar.getHexColor(calendar.getColor()));
		this.eventKey = event.getKey();
		this.eventId = event.getEventId();
		this.startDate = event.getStartDate().toDate();
		this.endDate = event.getEndDate().toDate();
		this.timezone = event.getTimezone();
		this.allDay = event.getAllDay();
		this.title = event.getTitle();
		this.description = event.getDescription();
		this.location = event.getLocation();
		this.recurrenceType = null;
		this.recurrenceDescription = null;
		if(event.getRecurrence() != null) {
			this.recurrenceType = event.getRecurrence().getType();
			try {
				this.recurrenceDescription = rrStringify.toHumanReadableText(event.getRecurrence().getRRule());
			} catch(ParseException ex) {}
		}
		this.isPrivate = event.getIsPrivate();
		this.isBusy = event.getBusy();
		this.reminder = event.getReminder();
		this.activityId = event.getActivityId();
		this.activityDescription = lookupActivityDescription(coreMgr, this.activityId);
		this.masterDataId = event.getMasterDataId();
		this.masterDataDescription = lookupMasterDataDescription(coreMgr, this.masterDataId);
		this.statMasterDataId = event.getStatMasterDataId();
		this.statMasterDataDescription = lookupMasterDataDescription(coreMgr, this.statMasterDataId);
		this.causalId = event.getCausalId();
		this.causalDescription = lookupCausalDescription(coreMgr, this.causalId);
		this.organizer = event.getOrganizer();
		
		if(event.hasAttendees()) {
			attendees = new ArrayList<>();
			for(EventAttendee att : event.getAttendees()) {
				attendees.add(new Attendee(att));
			}
		}
	}
	
	private String lookupActivityDescription(CoreManager core, Integer activityId) throws WTException {
		if(activityId == null) return null;
		Activity act = core.getActivity(activityId);
		return (act != null) ? act.getDescription() : null;
	}
	
	private String lookupCausalDescription(CoreManager core, Integer causalId) throws WTException {
		if(causalId == null) return null;
		Causal cau = core.getCausal(causalId);
		return (cau != null) ? cau.getDescription() : null;
	}
	
	private String lookupMasterDataDescription(CoreManager coreMgr, String masterDataId) throws WTException {
		if (masterDataId == null) return null;
		MasterData md = coreMgr.getMasterData(masterDataId);
		return (md != null) ? md.getDescription() : null;
	}
	
	public Integer getCalendarId() {
		return calendarId;
	}

	public String getCalendarName() {
		return calendarName;
	}

	public String getCalendarColor() {
		return calendarColor;
	}
	
	public Image getCalendarColorImage() {
		return calendarColorImage;
	}

	public String getEventKey() {
		return eventKey;
	}

	public Integer getEventId() {
		return eventId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getTimezone() {
		return timezone;
	}

	public Boolean getAllDay() {
		return allDay;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}
	
	public String getRecurrenceType() {
		return recurrenceType;
	}
	
	public String getRecurrenceDescription() {
		return recurrenceDescription;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public Boolean getIsBusy() {
		return isBusy;
	}

	public Integer getReminder() {
		return reminder;
	}

	public Integer getActivityId() {
		return activityId;
	}
	
	public String getActivityDescription() {
		return activityDescription;
	}

	public String getMasterDataId() {
		return masterDataId;
	}
	
	public String getMasterDataDescription() {
		return masterDataDescription;
	}

	public String getStatMasterDataId() {
		return statMasterDataId;
	}
	
	public String getStatMasterDataDescription() {
		return statMasterDataDescription;
	}

	public Integer getCausalId() {
		return causalId;
	}
	
	public String getCausalDescription() {
		return causalDescription;
	}
	
	public String getOrganizer() {
		return organizer;
	}
	
	public List<Attendee> getAttendees() {
		return attendees;
	}
	
	public static class Attendee {
		public String recipient;
		public String recipientType;
		public String recipientRole;
		
		public Attendee(EventAttendee att) {
			recipient = att.getRecipient();
			recipientType = att.getRecipientType();
			recipientRole = att.getRecipientRole();
		}
		
		public String getRecipient() {
			return recipient;
		}
		
		public String getRecipientType() {
			return recipientType;
		}
	}
}
