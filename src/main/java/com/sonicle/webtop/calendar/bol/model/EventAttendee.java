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

import java.net.InetAddress;
import java.util.ArrayList;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public class EventAttendee {
	public static final String RECIPIENT_TYPE_NECESSARY = "N";
	public static final String RECIPIENT_TYPE_OPTIONAL = "O";
	public static final String RECIPIENT_TYPE_RESOURCE = "R";
	public static final String RESPONSE_STATUS_UNKNOWN = "unknown";
	public static final String RESPONSE_STATUS_DECLINED = "declined";
	public static final String RESPONSE_STATUS_TENTATIVE = "tentative";
	public static final String RESPONSE_STATUS_ACCEPTED = "accepted";
	public static final String RESPONSE_STATUS_NONE = "none"; // Synonym of unknown/needsAction
	public static final String RESPONSE_STATUS_REFUSED = "refused"; // Synonym of declined
	
	protected String attendeeId;
	//protected String displayName;
	protected String recipient;
	protected String recipientType;
	protected String responseStatus;
	protected Boolean notify;
	
	public EventAttendee() {
		
	}

	public String getAttendeeId() {
		return attendeeId;
	}

	public void setAttendeeId(String value) {
		attendeeId = value;
	}
	
	/*
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String value) {
		displayName = value;
	}
	*/

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String value) {
		recipient = value;
	}

	public String getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(String value) {
		recipientType = value;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String value) {
		responseStatus = value;
	}

	public Boolean getNotify() {
		return notify;
	}

	public void setNotify(Boolean value) {
		notify = value;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(getAttendeeId())
			.append(getRecipient())
			.append(getRecipientType())
			.append(getResponseStatus())
			.append(getNotify())
			.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EventAttendee == false) return false;
		if(this == obj) return true;
		final EventAttendee otherObject = (EventAttendee) obj;
		return new EqualsBuilder()
			.append(getAttendeeId(), otherObject.getAttendeeId())
			.isEquals();
	}
	
	public boolean isResource() {
		return StringUtils.equals(getRecipientType(), RECIPIENT_TYPE_RESOURCE);
	}
	
	public boolean hasEmailRecipient() {
		try {
			InternetAddress email = new InternetAddress(getRecipient());
			return true;
		} catch(Exception ex) {
			return false;
		}
	}
	
	public class AttendeeList extends ArrayList<EventAttendee> {
		public AttendeeList() {
			super();
		}
	}
}