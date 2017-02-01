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

import com.sonicle.webtop.calendar.bol.VSchedulerEvent;
import com.sonicle.webtop.calendar.bol.OEvent;
import static com.sonicle.webtop.calendar.jooq.Sequences.SEQ_EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.CALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES_BROKEN;
import com.sonicle.webtop.calendar.jooq.tables.Events;
import com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken;
import com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.List;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.Field;

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
	
	public OEvent selectById(Connection con, Integer eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS)
			.where(
					EVENTS.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(OEvent.class);
	}
	
	public Integer selectIdByPublicUid(Connection con, String publicUid) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(EVENTS.EVENT_ID)
			.from(EVENTS)
			.where(
					EVENTS.PUBLIC_UID.equal(publicUid)
			)
			.fetchOne(0, Integer.class);
	}
	
	public int insert(Connection con, OEvent item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		OEvent.ensureCoherence(item);
		item.setRevisionStatus(OEvent.REV_STATUS_NEW);
		item.setRevisionTimestamp(revisionTimestamp);
		EventsRecord record = dsl.newRecord(EVENTS, item);
		return dsl
			.insertInto(EVENTS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OEvent item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		OEvent.ensureCoherence(item);
		item.setRevisionStatus(OEvent.REV_STATUS_MODIFIED);
		item.setRevisionTimestamp(revisionTimestamp);
		return dsl
			.update(EVENTS)
			.set(EVENTS.CALENDAR_ID, item.getCalendarId())
			.set(EVENTS.RECURRENCE_ID, item.getRecurrenceId())
			.set(EVENTS.START_DATE, item.getStartDate())
			.set(EVENTS.END_DATE, item.getEndDate())
			.set(EVENTS.TIMEZONE, item.getTimezone())
			.set(EVENTS.ALL_DAY, item.getAllDay())
			.set(EVENTS.TITLE, item.getTitle())
			.set(EVENTS.DESCRIPTION, item.getDescription())
			.set(EVENTS.LOCATION, item.getLocation())
			.set(EVENTS.IS_PRIVATE, item.getIsPrivate())
			.set(EVENTS.BUSY, item.getBusy())
			.set(EVENTS.REMINDER, item.getReminder())
			.set(EVENTS.READ_ONLY, item.getReadOnly())
			.set(EVENTS.ACTIVITY_ID, item.getActivityId())
			.set(EVENTS.CUSTOMER_ID, item.getCustomerId())
			.set(EVENTS.STATISTIC_ID, item.getStatisticId())
			.set(EVENTS.CAUSAL_ID, item.getCausalId())
			.set(EVENTS.ORGANIZER, item.getOrganizer())
			.set(EVENTS.REVISION_STATUS, item.getRevisionStatus())
			.set(EVENTS.REVISION_TIMESTAMP, item.getRevisionTimestamp())
			.where(
				EVENTS.EVENT_ID.equal(item.getEventId())
			)
			.execute();
	}
	
	public int updateCalendar(Connection con, int eventId, int calendarId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.CALENDAR_ID,calendarId)
			.set(EVENTS.REVISION_STATUS, OEvent.REV_STATUS_MODIFIED)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRevision(Connection con, int eventId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRevisionStatus(Connection con, int eventId, String revisionStatus, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, revisionStatus)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRemindedOn(Connection con, int eventId, DateTime remindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REMINDED_ON, remindedOn)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRemindedOnIfNull(Connection con, int eventId, DateTime remindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REMINDED_ON, remindedOn)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(EVENTS.REMINDED_ON.isNull())
			)
			.execute();
	}
	
	public int logicDeleteById(Connection con, int eventId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, OEvent.REV_STATUS_DELETED)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int logicDeleteByCalendarId(Connection con, int calendarId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, OEvent.REV_STATUS_DELETED)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
			)
			.execute();
	}
	
	public VSchedulerEvent view(Connection con, int eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesBroken rbk = RECURRENCES_BROKEN.as("rbk");
		Events eve = EVENTS.as("eve");
		Field<Integer> originalEventId = dsl
			.select(eve.EVENT_ID)
			.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
			.where(
				rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve.REVISION_STATUS.notEqual(OEvent.REV_STATUS_DELETED))
			)
			.asField("original_event_id");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				originalEventId,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
			)
			.fetchOneInto(VSchedulerEvent.class);
	}
	
	public VSchedulerEvent viewByPublicUid(Connection con, String publicUid) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesBroken rbk = RECURRENCES_BROKEN.as("rbk");
		Events eve = EVENTS.as("eve");
		Field<Integer> originalEventId = dsl
			.select(eve.EVENT_ID)
			.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
			.where(
				rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve.REVISION_STATUS.notEqual(OEvent.REV_STATUS_DELETED))
			)
			.asField("original_event_id");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				originalEventId,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.PUBLIC_UID.equal(publicUid)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
			)
			.fetchOneInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> viewDatesByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesBroken rbk = RECURRENCES_BROKEN.as("rbk");
		Events eve = EVENTS.as("eve");
		Field<Integer> originalEventId = dsl
			.select(eve.EVENT_ID)
			.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
			.where(
				rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve.REVISION_STATUS.notEqual(OEvent.REV_STATUS_DELETED))
			)
			.asField("original_event_id");
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ORGANIZER,
				EVENTS.REVISION_TIMESTAMP,
				originalEventId,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
				//field("false", Boolean.class).as("is_recurring")
				/*
				field(
					exists(
							selectOne()
							.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
							.where(
									rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
									.and(eve.STATUS.notEqual(OEvent.STATUS_DELETED))
							)
					)
				).as("is_broken")
				*/
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(
					EVENTS.START_DATE.between(fromDate, toDate) // Events that start in current range
					.or(EVENTS.END_DATE.between(fromDate, toDate)) // Events that end in current range
					.or(EVENTS.START_DATE.lessThan(fromDate).and(EVENTS.END_DATE.greaterThan(toDate))) // Events that start before and end after
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> viewByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesBroken rbk = RECURRENCES_BROKEN.as("rbk");
		Events eve = EVENTS.as("eve");
		
		Field<Integer> originalEventId = dsl
			.select(eve.EVENT_ID)
			.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
			.where(
				rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve.REVISION_STATUS.notEqual(OEvent.REV_STATUS_DELETED))
			)
			.asField("original_event_id");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				originalEventId,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
				/*
				DSL.coalesce(
						dsl.select(eve.EVENT_ID)
						.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
						.where(
								rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
								.and(eve.STATUS.notEqual(OEvent.STATUS_DELETED))
						), 
				EVENTS.EVENT_ID).as("original_event_id")
				*/
				//field("false", Boolean.class).as("is_recurring")
				/*
				field(
					exists(
							selectOne()
							.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
							.where(
									rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
									.and(eve.STATUS.notEqual(OEvent.STATUS_DELETED))
							)
					)
				).as("is_broken"),
				*/
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(
					EVENTS.START_DATE.between(fromDate, toDate) // Events that start in current range
					.or(EVENTS.END_DATE.between(fromDate, toDate)) // Events that end in current range
					.or(EVENTS.START_DATE.lessThan(fromDate).and(EVENTS.END_DATE.greaterThan(toDate))) // Events that start before and end after
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> searchByCalendarQuery(Connection con, Integer calendarId, String query) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesBroken rbk = RECURRENCES_BROKEN.as("rbk");
		Events eve = EVENTS.as("eve");
		
		Field<Integer> originalEventId = dsl
			.select(eve.EVENT_ID)
			.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
			.where(
				rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve.REVISION_STATUS.notEqual(OEvent.REV_STATUS_DELETED))
			)
			.asField("original_event_id");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				originalEventId,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(
					EVENTS.TITLE.likeIgnoreCase(query)
					.or(EVENTS.LOCATION.likeIgnoreCase(query))
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> viewRecurringDatesByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ORGANIZER,
				EVENTS.REVISION_TIMESTAMP,
				EVENTS.EVENT_ID.as("original_event_id"), // For recurring events, originalEventId is always equal to eventId
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(EVENTS.RECURRENCE_ID.isNotNull())
				.and(
					RECURRENCES.START_DATE.between(fromDate, toDate) // Recurrences that start in current range
					.or(RECURRENCES.UNTIL_DATE.between(fromDate, toDate)) // Recurrences that end in current range
					.or(RECURRENCES.START_DATE.lessThan(fromDate).and(RECURRENCES.UNTIL_DATE.greaterThan(toDate))) // Recurrences that start before and end after
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> viewRecurringByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
			.select(EVENTS.fields())
			.select(
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				EVENTS.EVENT_ID.as("original_event_id")
				//field("true", Boolean.class).as("is_recurring")
				//field("false", Boolean.class).as("is_broken")
			)
			/*
			.select(field(
					exists(
							selectOne()
							.from(RECURRENCES_BROKEN)
							.where(RECURRENCES_BROKEN.RECURRENCE_ID.equal(EVENTS.RECURRENCE_ID))
					)
			).as("hasBrokenRecurrences"))
			.select(
					field("false", Boolean.class)
					.as("hasPlanning")
			)
			*/
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(EVENTS.RECURRENCE_ID.isNotNull())
				.and(
					RECURRENCES.START_DATE.between(fromDate, toDate) // Recurrences that start in current range
					.or(RECURRENCES.UNTIL_DATE.between(fromDate, toDate)) // Recurrences that end in current range
					.or(RECURRENCES.START_DATE.lessThan(fromDate).and(RECURRENCES.UNTIL_DATE.greaterThan(toDate))) // Recurrences that start before and end after
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> searchRecurringByCalendarQuery(Connection con, Integer calendarId, String query) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				EVENTS.EVENT_ID.as("original_event_id"),
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(EVENTS.RECURRENCE_ID.isNotNull())
				.and(
					EVENTS.TITLE.likeIgnoreCase(query)
					.or(EVENTS.LOCATION.likeIgnoreCase(query))
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> viewExpiredForUpdateByFromTo(Connection con, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		RecurrencesBroken rbk = RECURRENCES_BROKEN.as("rbk");
		Events eve = EVENTS.as("eve");
		Field<Integer> originalEventId = dsl
			.select(eve.EVENT_ID)
			.from(rbk.join(eve).on(rbk.EVENT_ID.equal(eve.EVENT_ID)))
			.where(
				rbk.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve.REVISION_STATUS.notEqual(OEvent.REV_STATUS_DELETED))
			)
			.asField("original_event_id");
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.TITLE,
				EVENTS.REMINDER,
				EVENTS.ORGANIZER,
				EVENTS.REVISION_TIMESTAMP,
				originalEventId,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.REMINDER.isNotNull().and(EVENTS.REMINDED_ON.isNull())
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(
					EVENTS.START_DATE.between(fromDate, toDate) // Events that start in current range
					.or(EVENTS.END_DATE.between(fromDate, toDate)) // Events that end in current range
					.or(EVENTS.START_DATE.lessThan(fromDate).and(EVENTS.END_DATE.greaterThan(toDate))) // Events that start before and end after
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.forUpdate()
			.fetchInto(VSchedulerEvent.class);
	}
	
	public List<VSchedulerEvent> viewRecurringExpiredForUpdateByFromTo(Connection con, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.REMINDER,
				EVENTS.ORGANIZER,
				EVENTS.EVENT_ID.as("original_event_id"), // For recurring events, originalEventId is always equal to eventId
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.REMINDER.isNotNull().and(EVENTS.REMINDED_ON.isNull())
				.and(
					EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_NEW)
					.or(EVENTS.REVISION_STATUS.equal(OEvent.REV_STATUS_MODIFIED))
				)
				.and(
					RECURRENCES.START_DATE.between(fromDate, toDate) // Recurrences that start in current range
					.or(RECURRENCES.UNTIL_DATE.between(fromDate, toDate)) // Recurrences that end in current range
					.or(RECURRENCES.START_DATE.lessThan(fromDate).and(RECURRENCES.UNTIL_DATE.greaterThan(toDate))) // Recurrences that start before and end after
				)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.forUpdate()
			.fetchInto(VSchedulerEvent.class);
	}
}
