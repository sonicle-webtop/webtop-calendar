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
package com.sonicle.webtop.calendar.dal;

import com.sonicle.webtop.calendar.bol.OEventAttendee;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ATTENDEES;
import com.sonicle.webtop.calendar.jooq.tables.records.EventsAttendeesRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.List;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class EventAttendeeDAO extends BaseDAO {
	
	private final static EventAttendeeDAO INSTANCE = new EventAttendeeDAO();

	public static EventAttendeeDAO getInstance() {
		return INSTANCE;
	}
	
	public List<OEventAttendee> selectByEvent(Connection con, Integer eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
				.select()
				.from(EVENTS_ATTENDEES)
				.where(
						EVENTS_ATTENDEES.EVENT_ID.equal(eventId)
				)
				.orderBy(
						EVENTS_ATTENDEES.RECIPIENT.asc()
				)
				.fetchInto(OEventAttendee.class);
	}
	
	public List<OEventAttendee> selectByEventNotify(Connection con, Integer eventId, boolean notify) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
				.select()
				.from(EVENTS_ATTENDEES)
				.where(
						EVENTS_ATTENDEES.EVENT_ID.equal(eventId)
						.and(EVENTS_ATTENDEES.NOTIFY.equal(notify))
				)
				.orderBy(
						EVENTS_ATTENDEES.RECIPIENT.asc()
				)
				.fetchInto(OEventAttendee.class);
	}
	
	public int insert(Connection con, OEventAttendee item) throws DAOException {
		DSLContext dsl = getDSL(con);
		EventsAttendeesRecord record = dsl.newRecord(EVENTS_ATTENDEES, item);
		return dsl
			.insertInto(EVENTS_ATTENDEES)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OEventAttendee item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS_ATTENDEES)
			.set(EVENTS_ATTENDEES.RECIPIENT, item.getRecipient())
			.set(EVENTS_ATTENDEES.RECIPIENT_TYPE, item.getRecipientType())
			.set(EVENTS_ATTENDEES.RESPONSE_STATUS, item.getResponseStatus())
			.set(EVENTS_ATTENDEES.NOTIFY, item.getNotify())
			.where(EVENTS_ATTENDEES.ATTENDEE_ID.equal(item.getAttendeeId()))
			.execute();
	}
	
	public int updateAttendeeResponse(Connection con, String attendeeId, Integer eventId, String response) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS_ATTENDEES)
			.set(EVENTS_ATTENDEES.RESPONSE_STATUS, response)
			.where(
				EVENTS_ATTENDEES.ATTENDEE_ID.equal(attendeeId)
				.and(EVENTS_ATTENDEES.EVENT_ID.equal(eventId)))
			.execute();
	}
	
	public int delete(Connection con, String attendeeId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_ATTENDEES)
			.where(EVENTS_ATTENDEES.ATTENDEE_ID.equal(attendeeId))
			.execute();
	}
	
	public int deleteByEvent(Connection con, Integer eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_ATTENDEES)
			.where(EVENTS_ATTENDEES.EVENT_ID.equal(eventId))
			.execute();
	}
}
