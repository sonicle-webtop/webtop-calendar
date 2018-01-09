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

import com.sonicle.webtop.calendar.bol.OCalendarPropSet;
import static com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS;
import com.sonicle.webtop.calendar.jooq.tables.records.CalendarPropsRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class CalendarPropsDAO extends BaseDAO {
	private final static CalendarPropsDAO INSTANCE = new CalendarPropsDAO();
	public static CalendarPropsDAO getInstance() {
		return INSTANCE;
	}
	
	public Map<Integer, OCalendarPropSet> selectByProfileCalendarIn(Connection con, String domainId, String userId, Collection<Integer> calendarIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDAR_PROPS)
			.where(
				CALENDAR_PROPS.DOMAIN_ID.equal(domainId)
				.and(CALENDAR_PROPS.USER_ID.equal(userId))
				.and(CALENDAR_PROPS.CALENDAR_ID.in(calendarIds))
			)
			.fetchMap(CALENDAR_PROPS.CALENDAR_ID, OCalendarPropSet.class);
	}
	
	public OCalendarPropSet selectByProfileCalendar(Connection con, String domainId, String userId, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDAR_PROPS)
			.where(
				CALENDAR_PROPS.DOMAIN_ID.equal(domainId)
				.and(CALENDAR_PROPS.USER_ID.equal(userId))
				.and(CALENDAR_PROPS.CALENDAR_ID.equal(calendarId))
			)
			.fetchOneInto(OCalendarPropSet.class);
	}
	
	public int insert(Connection con, OCalendarPropSet item) throws DAOException {
		DSLContext dsl = getDSL(con);
		CalendarPropsRecord record = dsl.newRecord(CALENDAR_PROPS, item);
		return dsl
			.insertInto(CALENDAR_PROPS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OCalendarPropSet item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(CALENDAR_PROPS)
			.set(CALENDAR_PROPS.HIDDEN, item.getHidden())
			.set(CALENDAR_PROPS.COLOR, item.getColor())
			.set(CALENDAR_PROPS.SYNC, item.getSync())
			.where(
				CALENDAR_PROPS.DOMAIN_ID.equal(item.getDomainId())
				.and(CALENDAR_PROPS.USER_ID.equal(item.getUserId()))
				.and(CALENDAR_PROPS.CALENDAR_ID.equal(item.getCalendarId()))
			)
			.execute();
	}
	
	public int deleteByCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CALENDAR_PROPS)
			.where(
				CALENDAR_PROPS.CALENDAR_ID.equal(calendarId)
			)
			.execute();
	}
	
	public int deleteByDomain(Connection con, String domainId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CALENDAR_PROPS)
			.where(
				CALENDAR_PROPS.DOMAIN_ID.equal(domainId)
			)
			.execute();
	}
	
	public int deleteByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CALENDAR_PROPS)
			.where(
				CALENDAR_PROPS.DOMAIN_ID.equal(domainId)
				.and(CALENDAR_PROPS.USER_ID.equal(userId))
			)
			.execute();
	}
	
	public int deleteByProfileCalendar(Connection con, String domainId, String userId, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CALENDAR_PROPS)
			.where(
				CALENDAR_PROPS.DOMAIN_ID.equal(domainId)
				.and(CALENDAR_PROPS.USER_ID.equal(userId))
				.and(CALENDAR_PROPS.CALENDAR_ID.equal(calendarId))
			)
			.execute();
	}
	
}
