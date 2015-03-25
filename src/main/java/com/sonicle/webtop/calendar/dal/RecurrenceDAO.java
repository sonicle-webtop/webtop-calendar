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

import com.sonicle.webtop.calendar.bol.ORecurrence;
import com.sonicle.webtop.calendar.bol.ORecurrenceBroken;
import static com.sonicle.webtop.calendar.jooq.Sequences.SEQ_RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES_BROKEN;
import com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import org.joda.time.LocalDate;
import org.jooq.DSLContext;

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
	
	public int insert(Connection con, ORecurrence item) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesRecord record = dsl.newRecord(RECURRENCES, item);
		return dsl
			.insertInto(RECURRENCES)
			.set(record)
			.execute();
	}
	
	public ORecurrenceBroken selectBroken(Connection con, Integer recurrenceId, Integer eventId, LocalDate eventDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(RECURRENCES_BROKEN)
			.where(
					RECURRENCES_BROKEN.RECURRENCE_ID.equal(recurrenceId)
					.and(RECURRENCES_BROKEN.EVENT_ID.equal(eventId))
					.and(RECURRENCES_BROKEN.EVENT_DATE.equal(eventDate))
			)
			.fetchOneInto(ORecurrenceBroken.class);
	}
}
