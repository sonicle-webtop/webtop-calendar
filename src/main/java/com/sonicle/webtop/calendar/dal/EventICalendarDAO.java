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

import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ICALENDARS;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class EventICalendarDAO extends BaseDAO {
	private final static EventICalendarDAO INSTANCE = new EventICalendarDAO();
	public static EventICalendarDAO getInstance() {
		return INSTANCE;
	}
	
	public boolean hasICalendarById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(EVENTS_ICALENDARS)
			.where(
				EVENTS_ICALENDARS.EVENT_ID.equal(eventId)
			)
			.fetchOne(0, Integer.class) == 1;
	}
	
	public String selectRawDataById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS_ICALENDARS.RAW_DATA
			)
			.from(EVENTS_ICALENDARS)
			.where(
				EVENTS_ICALENDARS.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(String.class);
	}
	
	public int insert(Connection con, String eventId, String rawData) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(EVENTS_ICALENDARS)
			.set(EVENTS_ICALENDARS.EVENT_ID, eventId)
			.set(EVENTS_ICALENDARS.RAW_DATA, rawData)
			.execute();
	}
	
	public int update(Connection con, String eventId, String rawData) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS_ICALENDARS)
			.set(EVENTS_ICALENDARS.RAW_DATA, rawData)
			.where(EVENTS_ICALENDARS.EVENT_ID.equal(eventId))
			.execute();
	}
	
	public int upsert(Connection con, String eventId, String rawData) throws DAOException {
		int ret = update(con, eventId, rawData);
		if (ret == 0) ret = insert(con, eventId, rawData);
		return ret;
	}
	
	public int deleteById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_ICALENDARS)
			.where(
				EVENTS_ICALENDARS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
}
