/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.dal;

import com.sonicle.webtop.calendar.bol.OEventICalendar;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ICALENDARS;
import com.sonicle.webtop.calendar.jooq.tables.records.EventsIcalendarsRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 *
 * @author malbinola
 */
public class EventICalendarDAO extends BaseDAO {
	private final static EventICalendarDAO INSTANCE = new EventICalendarDAO();
	public static EventICalendarDAO getInstance() {
		return INSTANCE;
	}
	
	public boolean hasICalendarById(Connection con, int eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(EVENTS_ICALENDARS)
			.where(
				EVENTS_ICALENDARS.EVENT_ID.equal(eventId)
			)
			.fetchOne(0, Integer.class) == 1;
	}
	
	public OEventICalendar selectById(Connection con, int eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS_ICALENDARS)
			.where(
				EVENTS_ICALENDARS.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(OEventICalendar.class);
	}
	
	public int insert(Connection con, OEventICalendar item) throws DAOException {
		DSLContext dsl = getDSL(con);
		EventsIcalendarsRecord record = dsl.newRecord(EVENTS_ICALENDARS, item);
		return dsl
			.insertInto(EVENTS_ICALENDARS)
			.set(record)
			.execute();
	}
	
	public int deleteById(Connection con, int eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_ICALENDARS)
			.where(
				EVENTS_ICALENDARS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int deleteByCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_ICALENDARS)
			.where(
				EVENTS_ICALENDARS.EVENT_ID.in(
					DSL.select(
						EVENTS.EVENT_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.CALENDAR_ID.equal(calendarId)
					)
				)				
			)
			.execute();
	}
}
