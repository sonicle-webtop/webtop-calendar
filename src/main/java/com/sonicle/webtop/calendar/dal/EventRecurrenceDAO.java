/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.dal;

import com.sonicle.webtop.calendar.bol.OEventRecurrence;
import com.sonicle.webtop.calendar.bol.OEventRecurrenceEx;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_RECURRENCES_EX;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Set;
import org.joda.time.LocalDate;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class EventRecurrenceDAO extends BaseDAO {
	private final static EventRecurrenceDAO INSTANCE = new EventRecurrenceDAO();
	public static EventRecurrenceDAO getInstance() {
		return INSTANCE;
	}
	
	public boolean existsRecurrenceByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(EVENTS_RECURRENCES)
			.where(
				EVENTS_RECURRENCES.EVENT_ID.equal(eventId)
			)
			.fetchOne(0, int.class) == 1;
	}
	
	public OEventRecurrence selectRecurrenceByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS_RECURRENCES)
			.where(
				EVENTS_RECURRENCES.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(OEventRecurrence.class);
	}
	
	public int insertRecurrence(Connection con, OEventRecurrence item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(EVENTS_RECURRENCES)
			.set(EVENTS_RECURRENCES.EVENT_ID, item.getEventId())
			.set(EVENTS_RECURRENCES.START, item.getStart())
			.set(EVENTS_RECURRENCES.UNTIL, item.getUntil())
			.set(EVENTS_RECURRENCES.RULE, item.getRule())
			.execute();
	}
	
	public int updateRecurrence(Connection con, OEventRecurrence item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS_RECURRENCES)
			.set(EVENTS_RECURRENCES.START, item.getStart())
			.set(EVENTS_RECURRENCES.UNTIL, item.getUntil())
			.set(EVENTS_RECURRENCES.RULE, item.getRule())
			.where(EVENTS_RECURRENCES.EVENT_ID.equal(item.getEventId()))
			.execute();
	}
	
	public int deleteRecurrenceByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_RECURRENCES)
			.where(
				EVENTS_RECURRENCES.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public boolean existsRecurrenceExByEventDate(Connection con, String eventId, LocalDate date) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(EVENTS_RECURRENCES_EX)
			.where(
				EVENTS_RECURRENCES_EX.EVENT_ID.equal(eventId)
				.and(EVENTS_RECURRENCES_EX.DATE.equal(date))
			)
			.fetchOne(0, int.class) == 1;
	}
	
	public Set<LocalDate> selectRecurrenceExByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS_RECURRENCES_EX.DATE
			)
			.from(EVENTS_RECURRENCES_EX)
			.where(
				EVENTS_RECURRENCES_EX.EVENT_ID.equal(eventId)
			)
			.fetchSet(EVENTS_RECURRENCES_EX.DATE);
	}
	
	public int insertRecurrenceEx(Connection con, OEventRecurrenceEx item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(EVENTS_RECURRENCES_EX)
			.set(EVENTS_RECURRENCES_EX.EVENT_ID, item.getEventId())
			.set(EVENTS_RECURRENCES_EX.DATE, item.getDate())
			.execute();
	}
	
	public int insertRecurrenceEx(Connection con, String eventId, LocalDate date) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(EVENTS_RECURRENCES_EX)
			.set(EVENTS_RECURRENCES_EX.EVENT_ID, eventId)
			.set(EVENTS_RECURRENCES_EX.DATE, date)
			.execute();
	}
	
	public int[] batchInsertRecurrenceEx(Connection con, String eventId, Collection<LocalDate> dates) throws DAOException {
		if (dates.isEmpty()) return new int[0];
		DSLContext dsl = getDSL(con);
		BatchBindStep batch = dsl.batch(
			dsl.insertInto(EVENTS_RECURRENCES_EX, 
				EVENTS_RECURRENCES_EX.EVENT_ID, 
				EVENTS_RECURRENCES_EX.DATE
			)
			.values((String)null, null)
		);
		for (LocalDate date : dates) {
			batch.bind(
				eventId,
				date
			);
		}
		return batch.execute();
	}
	
	public int deleteRecurrenceExByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_RECURRENCES_EX)
			.where(
				EVENTS_RECURRENCES_EX.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int deleteRecurrenceExByEventDates(Connection con, String eventId, Collection<LocalDate> dates) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_RECURRENCES_EX)
			.where(
				EVENTS_RECURRENCES_EX.EVENT_ID.equal(eventId)
				.and(EVENTS_RECURRENCES_EX.DATE.in(dates))
			)
			.execute();
	}
}
