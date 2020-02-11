/*
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.dal;

import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_TAGS;
import com.sonicle.webtop.calendar.jooq.tables.EventsTags;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Set;
import org.jooq.DSLContext;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author malbinola
 */
public class EventTagDAO extends BaseDAO {
	private final static EventTagDAO INSTANCE = new EventTagDAO();
	public static EventTagDAO getInstance() {
		return INSTANCE;
	}
	
	public Set<String> selectTagsByEvent(Connection con, int eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS_TAGS.TAG_ID
			)
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(eventId)
			)
			.orderBy(
				EVENTS_TAGS.TAG_ID.asc()
			)
			.fetchSet(EVENTS_TAGS.TAG_ID);
	}
	
	public int insert(Connection con, int eventId, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(EVENTS_TAGS)
			.set(EVENTS_TAGS.EVENT_ID, eventId)
			.set(EVENTS_TAGS.TAG_ID, tagId)
			.execute();
	}
	
	public int insertByCalendar(Connection con, int calendarId, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		EventsTags et1 = EVENTS_TAGS.as("et1");
		return dsl
			.insertInto(EVENTS_TAGS)
			.select(
				select(
					EVENTS.EVENT_ID,
					val(tagId, String.class).as("tag_id")
				)
				.from(EVENTS)
				.where(
					EVENTS.CALENDAR_ID.equal(calendarId)
					.and(EVENTS.EVENT_ID.notIn(
						select(
							et1.EVENT_ID
						)
						.from(et1)
						.where(
							et1.EVENT_ID.equal(EVENTS.EVENT_ID)
							.and(et1.TAG_ID.equal(tagId))
						)
					))
				)
			)
			.execute();
	}
	
	public int insertByCalendarsEvents(Connection con, Collection<Integer> calendarIds, Collection<Integer> eventIds, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		EventsTags et1 = EVENTS_TAGS.as("et1");
		return dsl
			.insertInto(EVENTS_TAGS)
			.select(
				select(
					EVENTS.EVENT_ID,
					val(tagId, String.class).as("tag_id")
				)
				.from(EVENTS)
				.where(
					EVENTS.CALENDAR_ID.in(calendarIds)
					.and(EVENTS.EVENT_ID.in(eventIds))
					.and(EVENTS.EVENT_ID.notIn(
						select(
							et1.EVENT_ID
						)
						.from(et1)
						.where(
							et1.EVENT_ID.in(eventIds)
							.and(et1.TAG_ID.equal(tagId))
						)
					))
				)
			)
			.execute();
	}
	
	public int delete(Connection con, int eventId, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(eventId)
				.and(EVENTS_TAGS.TAG_ID.equal(tagId))
			)
			.execute();
	}
	
	public int deleteByTask(Connection con, int eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int deleteByCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.in(
					select(
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
	
	public int deleteByCalendarTags(Connection con, int calendarId, Collection<String> tagIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.in(
					select(
						EVENTS.EVENT_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.CALENDAR_ID.equal(calendarId)
					)
				)
				.and(EVENTS_TAGS.TAG_ID.in(tagIds))
			)
			.execute();
	}
	
	public int deleteByCalendarsEvents(Connection con, Collection<Integer> calendarIds, Collection<Integer> eventIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.in(
					select(
						EVENTS.EVENT_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.EVENT_ID.in(eventIds)
						.and(EVENTS.CALENDAR_ID.in(calendarIds))
					)
				)
			)
			.execute();
	}
	
	public int deleteByCalendarsEventsTags(Connection con, Collection<Integer> calendarIds, Collection<Integer> eventIds, Collection<String> tagIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_TAGS)
			.where(
					EVENTS_TAGS.EVENT_ID.in(
					select(
						EVENTS.EVENT_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.EVENT_ID.in(eventIds)
							.and(EVENTS.CALENDAR_ID.in(calendarIds))
					)
				)
				.and(EVENTS_TAGS.TAG_ID.in(tagIds))
			)
			.execute();
	}
}
