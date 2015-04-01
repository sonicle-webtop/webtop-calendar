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

import com.sonicle.webtop.calendar.bol.OEvent;
import static com.sonicle.webtop.calendar.jooq.Sequences.SEQ_EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES_BROKEN;
import com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.List;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 *
 * @author malbinola
 */
public class EventDAO extends BaseDAO {
	
	private final static EventDAO INSTANCE = new EventDAO();

	public static EventDAO getInstance() {
		return INSTANCE;
	}
	
	public Long getSequence(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		Long nextID = dsl.nextval(SEQ_EVENTS);
		return nextID;
	}
	
	public OEvent select(Connection con, Integer eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS)
			.where(
					EVENTS.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(OEvent.class);
	}
	
	public List<OEvent> selectDatesByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
				.select(
						EVENTS.EVENT_ID,
						EVENTS.RECURRENCE_ID,
						EVENTS.START_DATE,
						EVENTS.END_DATE,
						EVENTS.TIMEZONE
				)
				.from(EVENTS)
				.where(
						EVENTS.CALENDAR_ID.equal(calendarId)
						.and(
							EVENTS.START_DATE.between(fromDate, toDate) // Events that start in current range
							.or(EVENTS.END_DATE.between(fromDate, toDate)) // Events that end in current range
							.or(EVENTS.START_DATE.lessThan(fromDate).and(EVENTS.END_DATE.greaterThan(toDate))) // Events that start before and end after
						)
						.and(
							EVENTS.STATUS.equal("N")
							.or(EVENTS.STATUS.equal("M"))
						)
						.and(EVENTS.RECURRENCE_ID.isNull())
				)
				.orderBy(
						EVENTS.START_DATE
				)
				.fetchInto(OEvent.class);
	}
	
	public List<OEvent> selectByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
				.select(
						EVENTS.fields()
				)
				/*
				.select(
						DSL.select(DSL.val(false))
						.asField("hasBrokenRecurrences")
				)
				*/
				.from(EVENTS)
				.where(
						EVENTS.CALENDAR_ID.equal(calendarId)
						.and(
							EVENTS.START_DATE.between(fromDate, toDate) // Events that start in current range
							.or(EVENTS.END_DATE.between(fromDate, toDate)) // Events that end in current range
							.or(EVENTS.START_DATE.lessThan(fromDate).and(EVENTS.END_DATE.greaterThan(toDate))) // Events that start before and end after
						)
						.and(
							EVENTS.STATUS.equal("N")
							.or(EVENTS.STATUS.equal("M"))
						)
						.and(EVENTS.RECURRENCE_ID.isNull())
				)
				.orderBy(
						EVENTS.START_DATE
				)
				.fetchInto(OEvent.class);
	}
	
	public List<OEvent> selectRecurringDatesByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
				.select(
						EVENTS.EVENT_ID,
						EVENTS.RECURRENCE_ID,
						EVENTS.START_DATE,
						EVENTS.END_DATE,
						EVENTS.TIMEZONE
				)
				/*
				.select(
						DSL.select(DSL.field("COUNT(*)>0"))
						.from(RECURRENCES_BROKEN)
						.where(RECURRENCES_BROKEN.RECURRENCE_ID.equal(EVENTS.RECURRENCE_ID))
						.asField("hasBrokenRecurrences")
				)
				*/
				.from(EVENTS.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID)))
				.where(
						EVENTS.CALENDAR_ID.equal(calendarId)
						.and(
							RECURRENCES.START_DATE.between(fromDate, toDate) // Recurrences that start in current range
							.or(RECURRENCES.UNTIL_DATE.between(fromDate, toDate)) // Recurrences that end in current range
							.or(RECURRENCES.START_DATE.lessThan(fromDate).and(RECURRENCES.UNTIL_DATE.greaterThan(toDate))) // Recurrences that start before and end after
						)
						.and(
							EVENTS.STATUS.equal("N")
							.or(EVENTS.STATUS.equal("M"))
						)
				)
				.orderBy(
						EVENTS.START_DATE
				)
				.fetchInto(OEvent.class);
	}
	
	public List<OEvent> selectRecurringByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
				.select(EVENTS.fields())
				.from(EVENTS.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID)))
				.where(
						EVENTS.CALENDAR_ID.equal(calendarId)
						.and(
							RECURRENCES.START_DATE.between(fromDate, toDate) // Recurrences that start in current range
							.or(RECURRENCES.UNTIL_DATE.between(fromDate, toDate)) // Recurrences that end in current range
							.or(RECURRENCES.START_DATE.lessThan(fromDate).and(RECURRENCES.UNTIL_DATE.greaterThan(toDate))) // Recurrences that start before and end after
						)
						.and(
							EVENTS.STATUS.equal("N")
							.or(EVENTS.STATUS.equal("M"))
						)
				)
				.orderBy(
						EVENTS.START_DATE
				)
				.fetchInto(OEvent.class);
	}
	
	public int insert(Connection con, OEvent item) throws DAOException {
		DSLContext dsl = getDSL(con);
		EventsRecord record = dsl.newRecord(EVENTS, item);
		return dsl
			.insertInto(EVENTS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OEvent item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.CALENDAR_ID, item.getCalendarId())
			.set(EVENTS.START_DATE, item.getStartDate())
			.set(EVENTS.END_DATE, item.getEndDate())
			.set(EVENTS.TIMEZONE, item.getTimezone())
			.set(EVENTS.ALL_DAY, item.getAllDay())
			.set(EVENTS.TITLE, item.getTitle())
			.set(EVENTS.LOCATION, item.getLocation())
			.set(EVENTS.DESCRIPTION, item.getDescription())
			.where(
				EVENTS.EVENT_ID.equal(item.getEventId())
			)
			.execute();
	}
}
