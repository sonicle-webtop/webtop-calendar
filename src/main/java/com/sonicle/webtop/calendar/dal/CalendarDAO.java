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

import com.sonicle.webtop.calendar.bol.OCalendar;
import static com.sonicle.webtop.calendar.jooq.Sequences.SEQ_CALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.CALENDARS;
import com.sonicle.webtop.calendar.jooq.tables.records.CalendarsRecord;
import com.sonicle.webtop.core.bol.Owner;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.List;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class CalendarDAO extends BaseDAO {
	private final static CalendarDAO INSTANCE = new CalendarDAO();
	public static CalendarDAO getInstance() {
		return INSTANCE;
	}

	public Long getSequence(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		Long nextID = dsl.nextval(SEQ_CALENDARS);
		return nextID;
	}
	
	public boolean existByIdProfile(Connection con, Integer calendarId, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(CALENDARS)
			.where(
				CALENDARS.CALENDAR_ID.equal(calendarId)
				.and(CALENDARS.DOMAIN_ID.equal(domainId))
				.and(CALENDARS.USER_ID.equal(userId))
			)
			.fetchOne(0, Integer.class) == 1;
	}
	
	public Owner selectOwnerById(Connection con, Integer calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.CALENDAR_ID.equal(calendarId)
			)
			.fetchOneInto(Owner.class);
	}
	
	public OCalendar selectById(Connection con, Integer calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.CALENDAR_ID.equal(calendarId)
			)
			.fetchOneInto(OCalendar.class);
	}
	
	public List<OCalendar> selectByDomain(Connection con, String domainId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
			)
			.orderBy(
				CALENDARS.BUILT_IN.desc(),
				CALENDARS.NAME.asc()
			)
			.fetchInto(OCalendar.class);
	}
	
	public List<OCalendar> selectByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
			)
			.orderBy(
				CALENDARS.BUILT_IN.desc(),
				CALENDARS.NAME.asc()
			)
			.fetchInto(OCalendar.class);
	}
	
	public List<OCalendar> selectByProfileIn(Connection con, String domainId, String userId, Integer[] calendars) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
				.and(CALENDARS.CALENDAR_ID.in(calendars))
			)
			.orderBy(
				CALENDARS.BUILT_IN.desc(),
				CALENDARS.NAME.asc()
			)
			.fetchInto(OCalendar.class);
	}
	
	public OCalendar selectBuiltInByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
				.and(CALENDARS.BUILT_IN.equal(true))
			)
			.fetchOneInto(OCalendar.class);
	}

	public List<OCalendar> selectNoBuiltInByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
				.and(CALENDARS.BUILT_IN.equal(false))
			)
			.orderBy(CALENDARS.NAME)
			.fetchInto(OCalendar.class);
	}
	
	public int insert(Connection con, OCalendar item) throws DAOException {
		DSLContext dsl = getDSL(con);
		CalendarsRecord record = dsl.newRecord(CALENDARS, item);
		return dsl
			.insertInto(CALENDARS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OCalendar item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(CALENDARS)
			.set(CALENDARS.NAME, item.getName())
			.set(CALENDARS.DESCRIPTION, item.getDescription())
			.set(CALENDARS.COLOR, item.getColor())
			.set(CALENDARS.IS_PRIVATE, item.getIsPrivate())
			.set(CALENDARS.BUSY, item.getBusy())
			.set(CALENDARS.REMINDER, item.getReminder())
			.set(CALENDARS.SYNC, item.getSync())
			.set(CALENDARS.INVITATION, item.getInvitation())
			.set(CALENDARS.IS_DEFAULT, item.getIsDefault())
			.where(
				CALENDARS.CALENDAR_ID.equal(item.getCalendarId())
			)
			.execute();
	}
	
	public int update(Connection con, int calendarId, FieldsMap fieldValues) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(CALENDARS)
			.set(fieldValues)
			.where(
				CALENDARS.CALENDAR_ID.equal(calendarId)
			)
			.execute();
	}
	
	public int resetIsDefaultByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(CALENDARS)
			.set(CALENDARS.IS_DEFAULT, false)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
			)
			.execute();
	}
	
	public int deleteById(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CALENDARS)
			.where(
				CALENDARS.CALENDAR_ID.equal(calendarId)
			)
			.execute();
	}
	
	public int deleteByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
			)
			.execute();
	}
}
