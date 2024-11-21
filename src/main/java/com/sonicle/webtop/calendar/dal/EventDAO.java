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

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.calendar.bol.VVEvent;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventInfo;
import com.sonicle.webtop.calendar.bol.VEventObject;
import com.sonicle.webtop.calendar.bol.VEventHrefSync;
import com.sonicle.webtop.calendar.bol.VEventFootprint;
import com.sonicle.webtop.calendar.bol.VEventObjectChange;
import com.sonicle.webtop.calendar.bol.VExpEvent;
import static com.sonicle.webtop.calendar.jooq.Sequences.SEQ_EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.CALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ATTACHMENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ATTENDEES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_CUSTOM_VALUES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ICALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_TAGS;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES_BROKEN;
import static com.sonicle.webtop.calendar.jooq.tables.CalendarsChanges.CALENDARS_CHANGES;
import com.sonicle.webtop.calendar.jooq.tables.Events;
import com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken;
import com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectLimitStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;

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
	
	public Integer selectCalendarId(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.CALENDAR_ID
			)
			.from(EVENTS)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.fetchOne(0, Integer.class);
	}
	
	public Map<String, Integer> selectCalendarsByIds(Connection con, Collection<String> eventIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID
			)
			.from(EVENTS)
			.where(
				EVENTS.EVENT_ID.in(eventIds)
			)
			.fetchMap(EVENTS.EVENT_ID, EVENTS.CALENDAR_ID);
	}
	
	public Integer selectRecurrenceId(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.RECURRENCE_ID
			)
			.from(EVENTS)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.fetchOne(0, Integer.class);
	}
	
	public OEvent selectById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(OEvent.class);
	}
	
	public OEvent selectAliveById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS)
			.where(
					EVENTS.EVENT_ID.equal(eventId)
					.and(
						EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
						.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
					)
			)
			.fetchOneInto(OEvent.class);
	}
	
	public List<String> selectAliveIdsByPublicUid(Connection con, String publicUid) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(EVENTS.EVENT_ID)
			.from(EVENTS)
			.where(
					EVENTS.PUBLIC_UID.equal(publicUid)
					.and(
						EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
						.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
					)
			)
			.orderBy(
				EVENTS.CREATION_TIMESTAMP.asc(),
				EVENTS.EVENT_ID.asc()
			)
			.fetchInto(String.class);
	}
	
	public List<String> selectAliveIdsByCalendarsPublicUid(Connection con, Collection<Integer> calendarIds, String publicUid) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(EVENTS.EVENT_ID)
			.from(EVENTS)
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(EVENTS.PUBLIC_UID.equal(publicUid))
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.orderBy(
				EVENTS.CREATION_TIMESTAMP.asc(),
				EVENTS.EVENT_ID.asc()
			)
			.fetchInto(String.class);
	}
	
	public String selectAliveSeriesEventIdById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
			.select(
				EVENTS.EVENT_ID
			)
			.from(RECURRENCES_BROKEN.join(EVENTS).on(RECURRENCES_BROKEN.EVENT_ID.equal(EVENTS.EVENT_ID)))
			.where(
				RECURRENCES_BROKEN.NEW_EVENT_ID.equal(eventId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			).fetchOneInto(String.class);
	}
	
	public List<String> selectAliveIdsByCalendarHrefs(Connection con, int calendarId, String href) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.HREF.equal(href))
			)
			.orderBy(
				EVENTS.EVENT_ID.asc()
			)
			.fetchInto(String.class);
	}
	
	public Map<String, List<String>> selectHrefsByByCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.HREF
			)
			.from(EVENTS)
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.orderBy(
				EVENTS.EVENT_ID.asc()
			)
			.fetchGroups(EVENTS.HREF, EVENTS.EVENT_ID);
	}
	
	public Map<Integer, DateTime> selectMaxRevTimestampByCalendars(Connection con, Collection<Integer> calendarIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				CALENDARS_CHANGES.CALENDAR_ID,
				DSL.max(CALENDARS_CHANGES.TIMESTAMP)
			)
			.from(CALENDARS_CHANGES)
			.where(
				CALENDARS_CHANGES.CALENDAR_ID.in(calendarIds)
			)
			.groupBy(
				CALENDARS_CHANGES.CALENDAR_ID
			)
			.fetchMap(CALENDARS_CHANGES.CALENDAR_ID, DSL.max(CALENDARS_CHANGES.TIMESTAMP));
	}
	
	public int insert(Connection con, OEvent item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.ensureCoherence();
		item.setRevisionStatus(EnumUtils.toSerializedName(Event.RevisionStatus.NEW));
		item.setRevisionTimestamp(revisionTimestamp);
		item.setRevisionSequence(0);
		item.setCreationTimestamp(revisionTimestamp);
		EventsRecord record = dsl.newRecord(EVENTS, item);
		return dsl
			.insertInto(EVENTS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OEvent item, DateTime revisionTimestamp, boolean clearRemindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.ensureCoherence();
		item.setRevisionStatus(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED));
		item.setRevisionTimestamp(revisionTimestamp);
		
		if (clearRemindedOn) {
			return dsl
				.update(EVENTS)
				.set(EVENTS.CALENDAR_ID, item.getCalendarId())
				.set(EVENTS.REVISION_STATUS, item.getRevisionStatus())
				.set(EVENTS.REVISION_TIMESTAMP, item.getRevisionTimestamp())
				.set(EVENTS.RECURRENCE_ID, item.getRecurrenceId())
				.set(EVENTS.START_DATE, item.getStartDate())
				.set(EVENTS.END_DATE, item.getEndDate())
				.set(EVENTS.TIMEZONE, item.getTimezone())
				.set(EVENTS.ALL_DAY, item.getAllDay())
				.set(EVENTS.ORGANIZER, item.getOrganizer())
				.set(EVENTS.TITLE, item.getTitle())
				.set(EVENTS.DESCRIPTION, item.getDescription())
				.set(EVENTS.LOCATION, item.getLocation())
				.set(EVENTS.IS_PRIVATE, item.getIsPrivate())
				.set(EVENTS.BUSY, item.getBusy())
				.set(EVENTS.REMINDER, item.getReminder())
				.set(EVENTS.ETAG, item.getEtag())
				.set(EVENTS.ACTIVITY_ID, item.getActivityId())
				.set(EVENTS.MASTER_DATA_ID, item.getMasterDataId())
				.set(EVENTS.STAT_MASTER_DATA_ID, item.getStatMasterDataId())
				.set(EVENTS.CAUSAL_ID, item.getCausalId())
				.set(EVENTS.REMINDED_ON, (DateTime)null)
				.where(
					EVENTS.EVENT_ID.equal(item.getEventId())
				)
				.execute();
		} else {
			return dsl
				.update(EVENTS)
				.set(EVENTS.CALENDAR_ID, item.getCalendarId())
				.set(EVENTS.REVISION_STATUS, item.getRevisionStatus())
				.set(EVENTS.REVISION_TIMESTAMP, item.getRevisionTimestamp())
				.set(EVENTS.RECURRENCE_ID, item.getRecurrenceId())
				.set(EVENTS.START_DATE, item.getStartDate())
				.set(EVENTS.END_DATE, item.getEndDate())
				.set(EVENTS.TIMEZONE, item.getTimezone())
				.set(EVENTS.ALL_DAY, item.getAllDay())
				.set(EVENTS.ORGANIZER, item.getOrganizer())
				.set(EVENTS.TITLE, item.getTitle())
				.set(EVENTS.DESCRIPTION, item.getDescription())
				.set(EVENTS.LOCATION, item.getLocation())
				.set(EVENTS.IS_PRIVATE, item.getIsPrivate())
				.set(EVENTS.BUSY, item.getBusy())
				.set(EVENTS.REMINDER, item.getReminder())
				.set(EVENTS.ETAG, item.getEtag())
				.set(EVENTS.ACTIVITY_ID, item.getActivityId())
				.set(EVENTS.MASTER_DATA_ID, item.getMasterDataId())
				.set(EVENTS.STAT_MASTER_DATA_ID, item.getStatMasterDataId())
				.set(EVENTS.CAUSAL_ID, item.getCausalId())
				.where(
					EVENTS.EVENT_ID.equal(item.getEventId())
				)
				.execute();
		}
	}
	
	public int updateCalendar(Connection con, String eventId, int calendarId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.CALENDAR_ID,calendarId)
			.set(EVENTS.REVISION_STATUS, EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED))
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRevision(Connection con, String eventId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRevisionStatus(Connection con, String eventId, String revisionStatus, DateTime revisionTimestamp) throws DAOException {
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
	
	public int updateRemindedOn(Connection con, String eventId, DateTime remindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REMINDED_ON, remindedOn)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRemindedOnIfNull(Connection con, String eventId, DateTime remindedOn) throws DAOException {
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
	
	public List<OEvent> selectHandleInvitationByRevision(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.REVISION_STATUS
			)
			.from(EVENTS)
			.where(
					EVENTS.HANDLE_INVITATION.equal(true)
			)
			.fetchInto(OEvent.class);
	}
	
	public int updateHandleInvitationIn(Connection con, Collection<Integer> eventIds, boolean handleInvitation) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.HANDLE_INVITATION, handleInvitation)
			.where(
				EVENTS.EVENT_ID.in(eventIds)
			)
			.execute();
	}
	
	public int deleteById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int deleteByCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS)
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
			)
			.execute();
	}
	
	public int logicDeleteById(Connection con, String eventId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, EnumUtils.toSerializedName(Event.RevisionStatus.DELETED))
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int logicDeleteByCalendar(Connection con, int calendarId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, EnumUtils.toSerializedName(Event.RevisionStatus.DELETED))
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
			)
			.execute();
	}
	
	public int deleteBrokenOrphansByEventId(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS)
			.where(
				EVENTS.EVENT_ID.in(
					DSL.select(
						RECURRENCES_BROKEN.NEW_EVENT_ID
					)
					.from(RECURRENCES_BROKEN)
					.where(
						RECURRENCES_BROKEN.EVENT_ID.equal(eventId)
					)
				)				
			)
			.execute();
	}
	
	public OEventInfo selectOEventInfoById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: targets the eventId of the original series event
		RecurrencesBroken rbk1 = RECURRENCES_BROKEN.as("rbk1");
		Events eve1 = EVENTS.as("eve1");
		Field<String> seriesEventId = DSL
			.select(eve1.EVENT_ID)
			.from(rbk1.join(eve1).on(rbk1.EVENT_ID.equal(eve1.EVENT_ID)))
			.where(
				rbk1.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve1.REVISION_STATUS.notEqual(EnumUtils.toSerializedName(Event.RevisionStatus.DELETED)))
			).asField("linked_event_id");
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY,
				EVENTS.IS_PRIVATE
			)
			.select(
				seriesEventId
			)
			.from(EVENTS)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(OEventInfo.class);
	}
	
	public VVEvent viewById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: targets the eventId of the original series event
		RecurrencesBroken rbk1 = RECURRENCES_BROKEN.as("rbk1");
		Events eve1 = EVENTS.as("eve1");
		Field<String> seriesEventId = DSL
			.select(eve1.EVENT_ID)
			.from(rbk1.join(eve1).on(rbk1.EVENT_ID.equal(eve1.EVENT_ID)))
			.where(
				rbk1.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve1.REVISION_STATUS.notEqual(EnumUtils.toSerializedName(Event.RevisionStatus.DELETED)))
			).asField("series_event_id");
		
		// New field: attendees count
		Field<Integer> attendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
			)
		).as("attendees_count");
		
		// New field: notifyable attendees count
		Field<Integer> notifyableAttendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(EVENTS_ATTENDEES.NOTIFY.isTrue())
			)
		).as("notifyable_attendees_count");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				tags,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId,
				attendeesCount,
				notifyableAttendeesCount
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(VVEvent.class);
	}
	
	public VVEvent viewByPublicUid(Connection con, String publicUid) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: targets the eventId of the original series event
		RecurrencesBroken rbk1 = RECURRENCES_BROKEN.as("rbk1");
		Events eve1 = EVENTS.as("eve1");
		Field<Integer> seriesEventId = DSL
			.select(eve1.EVENT_ID)
			.from(rbk1.join(eve1).on(rbk1.EVENT_ID.equal(eve1.EVENT_ID)))
			.where(
				rbk1.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve1.REVISION_STATUS.notEqual(EnumUtils.toSerializedName(Event.RevisionStatus.DELETED)))
			).asField("series_event_id");
		
		// New field: attendees count
		Field<Integer> attendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
			)
		).as("attendees_count");
		
		// New field: notifyable attendees count
		Field<Integer> notifyableAttendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(EVENTS_ATTENDEES.NOTIFY.isTrue())
			)
		).as("notifyable_attendees_count");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				tags,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId,
				attendeesCount,
				notifyableAttendeesCount
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.PUBLIC_UID.equal(publicUid)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(VVEvent.class);
	}
	
	public List<VVEvent> viewDatesByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = toRangeCondition(fromDate, toDate, false);
		// New field: targets the eventId of the original series event
		RecurrencesBroken rbk1 = RECURRENCES_BROKEN.as("rbk1");
		Events eve1 = EVENTS.as("eve1");
		Field<Integer> seriesEventId = DSL
			.select(eve1.EVENT_ID)
			.from(rbk1.join(eve1).on(rbk1.EVENT_ID.equal(eve1.EVENT_ID)))
			.where(
				rbk1.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve1.REVISION_STATUS.notEqual(EnumUtils.toSerializedName(Event.RevisionStatus.DELETED)))
			).asField("series_event_id");
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY,
				EVENTS.ORGANIZER,
				EVENTS.REVISION_TIMESTAMP
			)
			.select(
				// Ignore tags list field, it's not necessary for getting dates (see this method name)
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId
				// Ignore attendees count field, it's not necessary for getting dates (see this method name)
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(rangeCndt)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VVEvent.class);
	}
	
	public List<VVEvent> viewRecurringDatesByCalendarFromTo(Connection con, Integer calendarId, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = toRangeCondition(fromDate, toDate, true);
		// New field: targets the eventId of the original series event
		// NB: recurring events cannot have a reference to a master series event
		Field<Integer> seriesEventId = value(null, Integer.class).as("series_event_id");
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY,
				EVENTS.ORGANIZER,
				EVENTS.REVISION_TIMESTAMP
			)
			.select(
				// Ignore tags list field, it's not necessary for getting dates (see this method name)
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId
				// Ignore attendees count field, it's not necessary for getting dates (see this method name)
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.RECURRENCE_ID.isNotNull())
				.and(rangeCndt)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VVEvent.class);
	}
	
	
	public List<VEventFootprint> viewRangesByCalendarRangeCondition(Connection con, Collection<Integer> calendarIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = toRangeCondition(rangeFrom, rangeTo, false);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.PUBLIC_UID,
				EVENTS.CALENDAR_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY
			)
			.select(
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VEventFootprint.class);
	}
	
	public List<VEventFootprint> viewRecurringRangesByCalendarRangeCondition(Connection con, Collection<Integer> calendarIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = toRangeCondition(rangeFrom, rangeTo, true);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.PUBLIC_UID,
				EVENTS.CALENDAR_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY
			)
			.select(
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.RECURRENCE_ID.isNotNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VEventFootprint.class);
	}
	
	public void lazy_viewOnlineEventObjects(Connection con, int calendarId, VEventObject.Consumer consumer) throws DAOException, WTException {
		DSLContext dsl = getDSL(con);
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
				DSL.selectOne()
						.from(EVENTS_ATTACHMENTS)
						.where(EVENTS_ATTACHMENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
				)).as("has_attachments");
		
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
				DSL.selectOne()
						.from(EVENTS_CUSTOM_VALUES)
						.where(EVENTS_CUSTOM_VALUES.EVENT_ID.equal(EVENTS.EVENT_ID))
				)).as("has_custom_values");
		
		Cursor<Record> cr = dsl
			.select(
				EVENTS.fields()
			)
			.select(
				tags,
				hasAttachments,
				hasCustomValues
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.orderBy(
				EVENTS.EVENT_ID.asc()
			)
			.fetchLazy();

		try {
			for(;;) {
				VEventObject veo = cr.fetchNextInto(VEventObject.class);
				if (veo == null) break;

				consumer.consume(veo, con);
			}
		} finally {
			cr.close();
		}
		
	}
	
	
	public boolean existByCalendarTypeCondition(Connection con, Collection<Integer> calendarIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = toRangeCondition(rangeFrom, rangeTo, false);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		return dsl.fetchExists(
			dsl.selectOne()
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
		);
	}
	
	public List<VVEvent> viewByCalendarRangeCondition(Connection con, int calendarId, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		return viewByCalendarRangeCondition(con, Arrays.asList(calendarId), rangeFrom, rangeTo, condition);
	}
	
	public List<VVEvent> viewByCalendarRangeCondition(Connection con, Collection<Integer> calendarIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = toRangeCondition(rangeFrom, rangeTo, false);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: targets the eventId of the original series event
		RecurrencesBroken rbk1 = RECURRENCES_BROKEN.as("rbk1");
		Events eve1 = EVENTS.as("eve1");
		Field<Integer> seriesEventId = DSL
			.select(eve1.EVENT_ID)
			.from(rbk1.join(eve1).on(rbk1.EVENT_ID.equal(eve1.EVENT_ID)))
			.where(
				rbk1.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve1.REVISION_STATUS.notEqual(EnumUtils.toSerializedName(Event.RevisionStatus.DELETED)))
			).asField("series_event_id");
		
		// New field: attendees count
		Field<Integer> attendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
			)
		).as("attendees_count");
		
		// New field: notifyable attendees count
		Field<Integer> notifyableAttendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(EVENTS_ATTENDEES.NOTIFY.isTrue())
			)
		).as("notifyable_attendees_count");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				tags,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId,
				attendeesCount,
				notifyableAttendeesCount
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.RECURRENCE_ID.isNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VVEvent.class);
	}
	
	public List<VVEvent> viewRecurringByCalendarRangeCondition(Connection con, int calendarId, DateTime fromDate, DateTime toDate, Condition condition) throws DAOException {
		return viewRecurringByCalendarRangeCondition(con, Arrays.asList(calendarId), fromDate, toDate, condition);
	}
	
	public List<VVEvent> viewRecurringByCalendarRangeCondition(Connection con, Collection<Integer> calendarIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = toRangeCondition(rangeFrom, rangeTo, true);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: targets the eventId of the original series event
		// NB: recurring events cannot have a reference to a master series event
		Field<Integer> seriesEventId = value(null, Integer.class).as("series_event_id");
		
		// New field: attendees count
		Field<Integer> attendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
			)
		).as("attendees_count");
		
		// New field: notifyable attendees count
		Field<Integer> notifyableAttendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(EVENTS_ATTENDEES.NOTIFY.isTrue())
			)
		).as("notifyable_attendees_count");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				tags,
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId,
				attendeesCount,
				notifyableAttendeesCount
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.RECURRENCE_ID.isNotNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				EVENTS.START_DATE
			)
			.fetchInto(VVEvent.class);
	}
	
	public List<VExpEvent> viewExpiredForUpdateByFromTo(Connection con, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: targets the eventId of the original series event
		RecurrencesBroken rbk1 = RECURRENCES_BROKEN.as("rbk1");
		Events eve1 = EVENTS.as("eve1");
		Field<Integer> seriesEventId = DSL
			.select(eve1.EVENT_ID)
			.from(rbk1.join(eve1).on(rbk1.EVENT_ID.equal(eve1.EVENT_ID)))
			.where(
				rbk1.NEW_EVENT_ID.equal(EVENTS.EVENT_ID)
				.and(eve1.REVISION_STATUS.notEqual(EnumUtils.toSerializedName(Event.RevisionStatus.DELETED)))
			).asField("series_event_id");
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY,
				EVENTS.TITLE,
				EVENTS.REMINDER,
				EVENTS.REMINDED_ON
			)
			.select(
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.REMINDER.isNotNull().and(EVENTS.REMINDED_ON.isNull()) // Note this NULL test on REMINDED_ON field!!!
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
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
			.fetchInto(VExpEvent.class);
	}
	
	public List<VExpEvent> viewRecurringExpiredForUpdateByFromTo(Connection con, DateTime fromDate, DateTime toDate) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// NB: recurring events cannot have a reference to a master series event
		Field<Integer> seriesEventId = value(null, Integer.class).as("series_event_id");
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.RECURRENCE_ID,
				EVENTS.START_DATE,
				EVENTS.END_DATE,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY,
				EVENTS.TITLE,
				EVENTS.REMINDER,
				EVENTS.REMINDED_ON
			)
			.select(
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id"),
				seriesEventId
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.join(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
			.where(
				EVENTS.REMINDER.isNotNull()
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
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
			.fetchInto(VExpEvent.class);
	}
	
	public VEventObject viewCalObjectById(Connection con, int calendarId, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: attendees count
		Field<Integer> attendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
			)
		).as("attendees_count");
		
		return dsl
			.select(
				EVENTS.fields()
			)
			.select(
				attendeesCount,
				DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_ICALENDARS).on(
				EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID)
			)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(EVENTS.CALENDAR_ID.equal(calendarId))
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(VEventObject.class);
	}
	
	public Map<String, List<VEventObject>> viewCalObjectsByCalendar(Connection con, int calendarId) throws DAOException {
		return viewCalObjectsByCalendarHrefsSince(con, calendarId, null, null);
	}
	
	public Map<String, List<VEventObject>> viewCalObjectsByCalendarHrefs(Connection con, int calendarId, Collection<String> hrefs) throws DAOException {
		return viewCalObjectsByCalendarHrefsSince(con, calendarId, hrefs, null);
	}
	
	public Map<String, List<VEventObject>> viewCalObjectsByCalendarSince(Connection con, int calendarId, DateTime since) throws DAOException {
		return viewCalObjectsByCalendarHrefsSince(con, calendarId, null, since);
	}
	
	public Map<String, List<VEventObject>> viewCalObjectsByCalendarHrefsSince(Connection con, int calendarId, Collection<String> hrefs, DateTime since) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: attendees count
		Field<Integer> attendeesCount = DSL.field(
			selectCount()
			.from(EVENTS_ATTENDEES)
			.where(
				EVENTS_ATTENDEES.EVENT_ID.equal(EVENTS.EVENT_ID)
			)
		).as("attendees_count");
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar");
	
		Condition inHrefsCndt = DSL.trueCondition();
		if (hrefs != null) {
			inHrefsCndt = EVENTS.HREF.in(hrefs);
		}
		
		Condition rangeCndt = null;
		if (since != null) {
			rangeCndt = EVENTS.END_DATE.greaterOrEqual(since)
				.or(RECURRENCES.RECURRENCE_ID.isNotNull()
					.and(RECURRENCES.UNTIL_DATE.greaterOrEqual(since).or(RECURRENCES.UNTIL_DATE.isNull()))	
				);
		}
		
		if (rangeCndt == null) {
			return dsl
				.select(
					EVENTS.fields()
				)
				.select(
					attendeesCount,
					hasICalendar
				)
				.from(EVENTS)
				.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
				.leftOuterJoin(EVENTS_ICALENDARS).on(
					EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID)
				)
				.where(
					EVENTS.CALENDAR_ID.equal(calendarId)
					.and(
						EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
						.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
					)
					.and(inHrefsCndt)
				)
				.orderBy(
					EVENTS.EVENT_ID.asc()
				)
				.fetchGroups(EVENTS.HREF, VEventObject.class);
		} else {
			return dsl
				.select(
					EVENTS.fields()
				)
				.select(
					attendeesCount,
					hasICalendar
				)
				.from(EVENTS)
				.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
				.leftOuterJoin(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
				.leftOuterJoin(EVENTS_ICALENDARS).on(
					EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID)
				)
				.where(
					EVENTS.CALENDAR_ID.equal(calendarId)
					.and(
						EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
						.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
					)
					.and(inHrefsCndt)
					.and(rangeCndt)
				)
				.orderBy(
					EVENTS.EVENT_ID.asc()
				)
				.fetchGroups(EVENTS.HREF, VEventObject.class);
		}
	}
	
	public List<VEventObjectChange> viewChangedObjectsByCalendarSince(Connection con, int calendarId, DateTime since, int limit) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition cndtSince = DSL.trueCondition();
		if (since != null) {
			cndtSince = CALENDARS_CHANGES.TIMESTAMP.greaterThan(since);
		}
		
		Table<?> t1 = DSL.select(
				CALENDARS_CHANGES.EVENT_ID,
				EVENTS.HREF,
				CALENDARS_CHANGES.TIMESTAMP,
				CALENDARS_CHANGES.OPERATION
			)
			.distinctOn(CALENDARS_CHANGES.EVENT_ID)
			.from(CALENDARS_CHANGES)
			.innerJoin(EVENTS).on(CALENDARS_CHANGES.EVENT_ID.equal(EVENTS.EVENT_ID))
			.where(
				CALENDARS_CHANGES.CALENDAR_ID.equal(calendarId)
				.and(cndtSince)
			)
			.orderBy(CALENDARS_CHANGES.EVENT_ID, CALENDARS_CHANGES.TIMESTAMP.desc()).asTable("t1");
		
		SelectLimitStep step = dsl
			.select()
			.from(t1)
			.orderBy(t1.field(CALENDARS_CHANGES.TIMESTAMP.getName()).desc());
		
		if (limit > -1) {
			return step
				.limit(limit)
				.fetchInto(VEventObjectChange.class);
		} else {
			return step
				.fetchInto(VEventObjectChange.class);
		}
	}
	
	public Map<String, VEventHrefSync> viewHrefSyncDataByCalendar(Connection con, int calendarId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.HREF,
				EVENTS.ETAG
			)
			.from(EVENTS)
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.fetchMap(EVENTS.HREF, VEventHrefSync.class);
	}
	
	public static Condition toBusyCondition() {
		return EVENTS.BUSY.equal(true);
	}
	
	private static Condition toRangeCondition(DateTime rangeFrom, DateTime rangeTo, boolean recurring) {
		Condition cndt = DSL.trueCondition();
		if ((rangeFrom != null) && (rangeTo != null)) {
			if (recurring) {
				cndt = RECURRENCES.START_DATE.between(rangeFrom, rangeTo) // Recurrences that start in current range
					.or(RECURRENCES.UNTIL_DATE.between(rangeFrom, rangeTo)) // Recurrences that end in current range
					.or(RECURRENCES.START_DATE.lessThan(rangeFrom).and(RECURRENCES.UNTIL_DATE.greaterThan(rangeTo))); // Recurrences that start before and end after
			} else {
				cndt = EVENTS.START_DATE.between(rangeFrom, rangeTo) // Events that start in current range
					.or(EVENTS.END_DATE.between(rangeFrom, rangeTo)) // Events that end in current range
					.or(EVENTS.START_DATE.lessThan(rangeFrom).and(EVENTS.END_DATE.greaterThan(rangeTo))); // Events that start before and end after
			}
		}
		return cndt;
	}
}
