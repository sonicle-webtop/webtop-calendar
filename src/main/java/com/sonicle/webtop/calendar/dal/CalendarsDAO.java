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

import com.sonicle.webtop.calendar.bol.OCalendars;
import static com.sonicle.webtop.calendar.jooq.Sequences.SEQ_CALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.CALENDARS;
import com.sonicle.webtop.calendar.jooq.tables.records.CalendarsRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import static com.sonicle.webtop.core.jooq.Tables.USERS;
import java.sql.Connection;
import java.util.List;
import org.jooq.DSLContext;


/**
 *
 * @author sergio
 */
public class CalendarsDAO extends BaseDAO{
    
    private final static String DEFAULT_CALENDAR_NAME="WebTop";
    private final static CalendarsDAO INSTANCE = new CalendarsDAO();
	public static CalendarsDAO getInstance() {
		return INSTANCE;
	}
	
        public Long getSequence(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		Long nextID =  dsl.nextval(SEQ_CALENDARS);
                return nextID;
	}
        
	public List<OCalendars> selectPersonalCalendars(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
                                .and(CALENDARS.NAME.notEqual(DEFAULT_CALENDAR_NAME))
                                
			)
			.fetchInto(OCalendars.class);
	}
        
        public OCalendars selectDefaultPersonalCalendar(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl.
			select()
			.from(CALENDARS)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId)
                                .and(CALENDARS.NAME.equal("WebTop")))
			)
			.fetchOneInto(OCalendars.class);
	}
        
        public int insertPersonalCalendar(Connection con, OCalendars item) throws DAOException {
		DSLContext dsl = getDSL(con);
		CalendarsRecord record = dsl.newRecord(CALENDARS, item);
		return dsl
			.insertInto(CALENDARS)
			.set(record)
			.execute();
	}
        
        
        public int deletePersonalCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CALENDARS)
			.where(CALENDARS.CALENDAR_ID.equal(calendarId))
			.execute();
	}
        
        public int resetPersonalDefaultCalendar(Connection con,String domainId,String userId){
                DSLContext dsl = getDSL(con);
                return dsl.update(CALENDARS)
			.set(CALENDARS.DEFAULT, true)
			.where(
				CALENDARS.DOMAIN_ID.equal(domainId)
				.and(CALENDARS.USER_ID.equal(userId))
                                .and(CALENDARS.DEFAULT.equal(true))
			)
			.execute();
        }
        
        public int updatePersonalCalendar(Connection con, int calendarId,OCalendars item) throws DAOException {
		DSLContext dsl = getDSL(con);
		CalendarsRecord record = dsl.newRecord(CALENDARS, item);
		return dsl
			.update(CALENDARS)
			.set(record)
                        .where(CALENDARS.CALENDAR_ID.equal(calendarId))
			.execute();
	}
        
}
