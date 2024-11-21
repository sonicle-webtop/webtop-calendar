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
package com.sonicle.webtop.calendar.dal;

import com.sonicle.webtop.calendar.bol.ORecurrence;
import static com.sonicle.webtop.calendar.jooq.Sequences.SEQ_RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.CALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES;
import com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 *
 * @author malbinola
 */
public class RecurrenceDAO extends BaseDAO {
	
	private final static RecurrenceDAO INSTANCE = new RecurrenceDAO();

	public static RecurrenceDAO getInstance() {
		return INSTANCE;
	}
	
	public Long getSequence(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		Long nextID = dsl.nextval(SEQ_RECURRENCES);
		return nextID;
	}
	
	public ORecurrence select(Connection con, Integer recurrenceId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(RECURRENCES)
			.where(
				RECURRENCES.RECURRENCE_ID.equal(recurrenceId)
			)
			.fetchOneInto(ORecurrence.class);
	}
	
	public ORecurrence selectByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(RECURRENCES)
			.where(
				RECURRENCES.RECURRENCE_ID.equal(
					DSL.select(
						EVENTS.RECURRENCE_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.EVENT_ID.equal(eventId)
					)
				)
			)
			.fetchOneInto(ORecurrence.class);
	}
	
	public int insert(Connection con, ORecurrence item) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesRecord record = dsl.newRecord(RECURRENCES, item);
		return dsl
			.insertInto(RECURRENCES)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, ORecurrence item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(RECURRENCES)
			.set(RECURRENCES.START_DATE, item.getStartDate())
			.set(RECURRENCES.UNTIL_DATE, item.getUntilDate())
			.set(RECURRENCES.REPEAT, item.getRepeat())
			.set(RECURRENCES.PERMANENT, item.getPermanent())
			.set(RECURRENCES.TYPE, item.getType())
			.set(RECURRENCES.DAILY_FREQ, item.getDailyFreq())
			.set(RECURRENCES.WEEKLY_FREQ, item.getWeeklyFreq())
			.set(RECURRENCES.WEEKLY_DAY_1, item.getWeeklyDay_1())
			.set(RECURRENCES.WEEKLY_DAY_2, item.getWeeklyDay_2())
			.set(RECURRENCES.WEEKLY_DAY_3, item.getWeeklyDay_3())
			.set(RECURRENCES.WEEKLY_DAY_4, item.getWeeklyDay_4())
			.set(RECURRENCES.WEEKLY_DAY_5, item.getWeeklyDay_5())
			.set(RECURRENCES.WEEKLY_DAY_6, item.getWeeklyDay_6())
			.set(RECURRENCES.WEEKLY_DAY_7, item.getWeeklyDay_7())
			.set(RECURRENCES.MONTHLY_FREQ, item.getMonthlyFreq())
			.set(RECURRENCES.MONTHLY_DAY, item.getMonthlyDay())
			.set(RECURRENCES.YEARLY_FREQ, item.getYearlyFreq())
			.set(RECURRENCES.YEARLY_DAY, item.getYearlyDay())
			.set(RECURRENCES.RULE, item.getRule())
			.where(RECURRENCES.RECURRENCE_ID.equal(item.getRecurrenceId()))
			.execute();
	}
	
	public int updateRRule(Connection con, Integer recurrenceId, String rr) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(RECURRENCES)
			.set(RECURRENCES.RULE, rr)
			.where(
				RECURRENCES.RECURRENCE_ID.equal(recurrenceId)
			)
			.execute();
	}
	
	public int deleteById(Connection con, int recurrenceId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(RECURRENCES)
			.where(
				RECURRENCES.RECURRENCE_ID.equal(recurrenceId)
			)
			.execute();
	}
	
	public int deleteByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(RECURRENCES)
			.where(
				RECURRENCES.RECURRENCE_ID.in(
					DSL.select(
						EVENTS.RECURRENCE_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.EVENT_ID.equal(eventId)
						.and(EVENTS.RECURRENCE_ID.isNotNull())
					)
				)				
			)
			.execute();
	}
	
	public int deleteByCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(RECURRENCES)
			.where(
				RECURRENCES.RECURRENCE_ID.in(
					DSL.select(
						EVENTS.RECURRENCE_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.CALENDAR_ID.equal(calendarId)
						.and(EVENTS.REVISION_STATUS.isNotNull())
						.and(EVENTS.RECURRENCE_ID.isNotNull())
					)
				)				
			)
			.execute();
	}
}
