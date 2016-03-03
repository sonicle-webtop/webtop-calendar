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

import com.sonicle.webtop.calendar.bol.OCalendar;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.JRHelper;
import java.awt.Image;
import java.util.Date;

/**
 *
 * @author malbinola
 */
public class EventBean {
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
	public Boolean isPrivate;
	public Boolean isBusy;
	public Integer reminder;
	public Integer activityId;
	public String activityDescription;
	public String customerId;
	public String customerDescription;
	public String statisticId;
	public String statisticDescription;
	public Integer causalId;
	public String causalDescription;
	
	public EventBean(CoreManager core, OCalendar calendar, Event event) throws WTException {
		this.calendarId = event.getCalendarId();
		this.calendarName = calendar.getName();
		this.calendarColor = calendar.getHexColor();
		this.calendarColorImage = JRHelper.colorAsImage(calendar.getHexColor());
		this.eventKey = event.getKey();
		this.eventId = event.getEventId();
		this.startDate = event.getStartDate().toDate();
		this.endDate = event.getEndDate().toDate();
		this.timezone = event.getTimezone();
		this.allDay = event.getAllDay();
		this.title = event.getTitle();
		this.description = event.getDescription();
		this.location = event.getLocation();
		this.isPrivate = event.getIsPrivate();
		this.isBusy = event.getBusy();
		this.reminder = event.getReminder();
		this.activityId = event.getActivityId();
		this.activityDescription = lookupActivity(core, this.activityId);
		this.customerId = event.getCustomerId();
		this.customerDescription = lookupCustomer(core, this.customerId);
		this.statisticId = event.getStatisticId();
		this.statisticDescription = lookupCustomer(core, this.statisticId);
		this.causalId = event.getCausalId();
		this.causalDescription = lookupCausal(core, this.causalId);
	}
	
	private String lookupActivity(CoreManager core, Integer activityId) throws WTException {
		if(activityId == null) return null;
		OActivity activity = core.getActivity(activityId);
		return (activity != null) ? activity.getDescription() : null;
	}
	
	private String lookupCustomer(CoreManager core, String customerId) throws WTException {
		if(customerId == null) return null;
		OCustomer customer = core.getCustomer(customerId);
		return (customer != null) ? customer.getDescription() : null;
	}
	
	private String lookupCausal(CoreManager core, Integer causalId) throws WTException {
		if(causalId == null) return null;
		OCausal causal = core.getCausal(causalId);
		return (causal != null) ? causal.getDescription() : null;
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

	public String getCustomerId() {
		return customerId;
	}
	
	public String getCustomerDescription() {
		return customerDescription;
	}

	public String getStatisticId() {
		return statisticId;
	}
	
	public String getStatisticDescription() {
		return statisticDescription;
	}

	public Integer getCausalId() {
		return causalId;
	}
	
	public String getCausalDescription() {
		return causalDescription;
	}
}
