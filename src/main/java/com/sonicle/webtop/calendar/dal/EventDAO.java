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

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.calendar.bol.OEvent;
import com.sonicle.webtop.calendar.bol.OEventBoundary;
import com.sonicle.webtop.calendar.bol.OEventBoundarySeries;
import com.sonicle.webtop.calendar.bol.OEventInstanceInfo;
import com.sonicle.webtop.calendar.bol.VEventObject;
import com.sonicle.webtop.calendar.bol.VEventHrefSync;
import com.sonicle.webtop.calendar.bol.VEventLookup;
import com.sonicle.webtop.calendar.bol.VEventObjectChanged;
import com.sonicle.webtop.calendar.bol.VEventBounds;
import static com.sonicle.webtop.calendar.jooq.Tables.CALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ATTACHMENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ATTENDEES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_CUSTOM_VALUES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_ICALENDARS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_RECURRENCES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_RECURRENCES_EX;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_TAGS;
import static com.sonicle.webtop.calendar.jooq.Tables.HISTORY_EVENTS;
import com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord;
import com.sonicle.webtop.calendar.model.Event;
import com.sonicle.webtop.calendar.model.EventBase;
import com.sonicle.webtop.calendar.model.EventInstanceId;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Param;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;
import com.sonicle.webtop.calendar.model.EventBounds;
import com.sonicle.webtop.calendar.model.EventBoundsSeries;
import net.sf.qualitycheck.Check;

/**
 *
 * @author malbinola
 */
public class EventDAO extends BaseDAO {
	private final static EventDAO INSTANCE = new EventDAO();
	public static EventDAO getInstance() {
		return INSTANCE;
	}
	
	public static Condition createEventsChangedNewOrModifiedCondition() {
		return HISTORY_EVENTS.CHANGE_TYPE.equal(BaseDAO.CHANGE_TYPE_CREATION)
			.or(HISTORY_EVENTS.CHANGE_TYPE.equal(BaseDAO.CHANGE_TYPE_UPDATE));
	}
	
	public static Condition createEventsChangedSinceUntilCondition(DateTime since, DateTime until) {
		return HISTORY_EVENTS.CHANGE_TIMESTAMP.greaterThan(since)
			.and(HISTORY_EVENTS.CHANGE_TIMESTAMP.lessThan(until));
	}
	
	private Field[] getVEventObjectFields(boolean stat) {
		if (stat) {
			return new Field[]{
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.SERIES_EVENT_ID,
				EVENTS.SERIES_INSTANCE_ID,
				EVENTS.REVISION_STATUS,
				EVENTS.REVISION_TIMESTAMP,
				EVENTS.CREATION_TIMESTAMP,
				EVENTS.PUBLIC_UID,
				EVENTS.HREF
			};
		} else {
			//return EVENTS.fields();
			return new Field[]{
				EVENTS.EVENT_ID,
				EVENTS.CALENDAR_ID,
				EVENTS.SERIES_EVENT_ID,
				EVENTS.SERIES_INSTANCE_ID,
				EVENTS.REVISION_STATUS,
				EVENTS.REVISION_TIMESTAMP,
				EVENTS.REVISION_SEQUENCE,
				EVENTS.CREATION_TIMESTAMP,
				EVENTS.ORGANIZER,
				EVENTS.ORGANIZER_ID,
				EVENTS.START,
				EVENTS.END,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY,
				EVENTS.TITLE,
				EVENTS.LOCATION,
				EVENTS.DESCRIPTION_TYPE,
				EVENTS.DESCRIPTION,
				EVENTS.VISIBILITY,
				EVENTS.TRANSPARENCY,
				EVENTS.STATUS,
				EVENTS.REMINDER,
				EVENTS.PUBLIC_UID,
				EVENTS.HREF
			};
		}
	}
	
	public EventBounds selectBounds(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return (EventBounds)dsl
			.select(
				EVENTS.ALL_DAY,
				EVENTS.START,
				EVENTS.END,
				EVENTS.TIMEZONE	
			)
			.from(EVENTS)
			.where(
				EVENTS.EVENT_ID.in(eventId)
			)
			.fetchOneInto(OEventBoundary.class);
	}
	
	public EventBoundsSeries selectBoundsSeries(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return (EventBoundsSeries)dsl
			.select(
				EVENTS.ALL_DAY,
				EVENTS.START,
				EVENTS.END,
				EVENTS.TIMEZONE,
				EVENTS_RECURRENCES.START.as("recurrence_start"),
				EVENTS_RECURRENCES.UNTIL.as("recurrence_until"),
				EVENTS_RECURRENCES.RULE.as("recurrence_rule")
			)
			.select(
				field(exists(
					selectOne()
					.from(EVENTS_RECURRENCES_EX)
					.where(
						EVENTS_RECURRENCES_EX.EVENT_ID.equal(eventId)
					)
				)).as("recurrence_has_ex_dates")
			)
			.from(EVENTS)
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(OEventBoundarySeries.class);
	}
	
	public OEventInstanceInfo selectInstanceInfo(Connection con, EventInstanceId instanceId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl.select(
				field(exists(
					selectOne()
					.from(EVENTS_RECURRENCES)
					.where(
						EVENTS_RECURRENCES.EVENT_ID.equal(instanceId.getEventId())
					)
				)).as("has_recurrence"),
				field(
					select(
						EVENTS.EVENT_ID
					)
					.from(EVENTS)
					.where(
						EVENTS.SERIES_EVENT_ID.equal(instanceId.getEventId())
						.and(EVENTS.SERIES_INSTANCE_ID.equal(instanceId.getInstance()))
						.and(
							EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
							.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
						)
					)
				).as("event_id_by_instance"),
				coalesce(
					field(
						select(
							EVENTS.TIMEZONE
						)
						.from(EVENTS)
						.where(
							EVENTS.SERIES_EVENT_ID.equal(instanceId.getEventId())
							.and(EVENTS.SERIES_INSTANCE_ID.equal(instanceId.getInstance()))
							.and(
								EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
								.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
							)
						)
					),
					field(
						select(
							EVENTS.TIMEZONE
						)
						.from(EVENTS)
						.where(
							EVENTS.EVENT_ID.equal(instanceId.getEventId())
						)
					)
				).as("timezone"),
				field(
					select(
						EVENTS.VISIBILITY
					)
					.from(EVENTS)
					.where(
						EVENTS.EVENT_ID.equal(instanceId.getEventId())
					)
				).as("visibility")
			).fetchOneInto(OEventInstanceInfo.class);
	}
	
	public String selectOnlineIdBySeriesInstance(Connection con, String seriesEventkId, String seriesInstance) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.EVENT_ID
			)
			.from(EVENTS)
			.where(
				EVENTS.SERIES_EVENT_ID.in(seriesEventkId)
				.and(EVENTS.SERIES_INSTANCE_ID.in(seriesInstance))
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOne(0, String.class);
	}
	
	public OEvent selectEventById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS)
			.where(EVENTS.EVENT_ID.equal(eventId))
			.fetchOneInto(OEvent.class);
	}
	
	public Integer selectCalendarById(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS.CALENDAR_ID
			)
			.from(EVENTS)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.fetchOneInto(Integer.class);
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
	
	public OEvent selectOnlineEventById(Connection con, String eventId) throws DAOException {
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
	
	public List<String> selectOnlineIdsByPublicUid(Connection con, String publicUid) throws DAOException {
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
	
	public List<String> selectOnlineIdsByCalendarsPublicUid(Connection con, Collection<Integer> calendarIds, String publicUid) throws DAOException {
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
	
	public List<String> selectOnlineIdsByCalendarHrefs(Connection con, int calendarId, String href) throws DAOException {
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
				HISTORY_EVENTS.CALENDAR_ID,
				DSL.max(HISTORY_EVENTS.CHANGE_TIMESTAMP)
			)
			.from(HISTORY_EVENTS)
			.where(
				HISTORY_EVENTS.CALENDAR_ID.in(calendarIds)
			)
			.groupBy(
				HISTORY_EVENTS.CALENDAR_ID
			)
			.fetchMap(HISTORY_EVENTS.CALENDAR_ID, DSL.max(HISTORY_EVENTS.CHANGE_TIMESTAMP));
	}
	
	public int insertEvent(Connection con, OEvent item, DateTime revisionTimestamp) throws DAOException {
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
	
	public int updateEvent(Connection con, OEvent item, DateTime revisionTimestamp, boolean clearRemindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.ensureCoherence();
		item.setRevisionStatus(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED));
		item.setRevisionTimestamp(revisionTimestamp);
		
		UpdateSetMoreStep update = dsl
			.update(EVENTS)
			.set(EVENTS.CALENDAR_ID, item.getCalendarId())
			.set(EVENTS.REVISION_STATUS, item.getRevisionStatus())
			.set(EVENTS.REVISION_TIMESTAMP, item.getRevisionTimestamp())
			.set(EVENTS.START, item.getStart())
			.set(EVENTS.END, item.getEnd())
			.set(EVENTS.TIMEZONE, item.getTimezone())
			.set(EVENTS.ALL_DAY, item.getAllDay())
			.set(EVENTS.TITLE, item.getTitle())
			.set(EVENTS.LOCATION, item.getLocation())
			.set(EVENTS.DESCRIPTION_TYPE, item.getDescriptionType())
			.set(EVENTS.DESCRIPTION, item.getDescription())
			.set(EVENTS.VISIBILITY, item.getVisibility())
			.set(EVENTS.TRANSPARENCY, item.getTransparency())
			.set(EVENTS.REMINDER, item.getReminder())
			.set(EVENTS.ETAG, item.getEtag());
		
		if (clearRemindedOn) {
			update = update
				.set(EVENTS.REMINDED_AT, (DateTime)null);
		}
		
		return update
			.where(
				EVENTS.EVENT_ID.equal(item.getEventId())
			)
			.execute();
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
			.set(EVENTS.REMINDED_AT, remindedOn)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
			)
			.execute();
	}
	
	public int updateRemindedOnIfNull(Connection con, String eventId, DateTime remindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REMINDED_AT, remindedOn)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(EVENTS.REMINDED_AT.isNull())
			)
			.execute();
	}
	
	public List<String> updateOnlineTitlesBySeries(Connection con, String seriesEventd, String oldSubject, String newSubject, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<EventsRecord> result = dsl
			.update(EVENTS)
			.set(EVENTS.TITLE, newSubject)
			.set(EVENTS.REVISION_STATUS, EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED))
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.SERIES_EVENT_ID.equal(seriesEventd)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.TITLE.equal(oldSubject))
			)
			.returning(
				EVENTS.EVENT_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getEventId())
			.collect(Collectors.toList());
	}
	
	public List<String> updateCategoryBySeries(Connection con, String seriesEventId, int calendarId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<EventsRecord> result = dsl
			.update(EVENTS)
			.set(EVENTS.CALENDAR_ID, calendarId)
			.set(EVENTS.REVISION_STATUS, EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED))
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(seriesEventId)
					.or(EVENTS.SERIES_EVENT_ID.equal(seriesEventId))
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(EVENTS.CALENDAR_ID.notEqual(calendarId))
			)
			.returning(
				EVENTS.EVENT_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getEventId())
			.collect(Collectors.toList());
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
		final String DELETED = EnumUtils.toSerializedName(Event.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, DELETED)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(EVENTS.REVISION_STATUS.notEqual(DELETED))
			)
			.execute();
	}
	
	public Set<String> logicDeleteBySeries(Connection con, String seriesEventId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Event.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		Result<EventsRecord> result = dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, DELETED)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(	
				EVENTS.SERIES_EVENT_ID.equal(seriesEventId)
				.and(EVENTS.REVISION_STATUS.notEqual(DELETED))
			)
			.returning(
				EVENTS.EVENT_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getEventId())
			.collect(Collectors.toSet());
	}
	
	public int logicDeleteByCalendar(Connection con, int calendarId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Event.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		return dsl
			.update(EVENTS)
			.set(EVENTS.REVISION_STATUS, DELETED)
			.set(EVENTS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(EVENTS.REVISION_STATUS.notEqual(DELETED))
			)
			.execute();
	}
	
	public int countOnlineEventObjectsByCalendarSinceCondition(Connection con, Collection<Integer> calendarIds, DateTime since, Condition condition) throws DAOException, WTException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (since != null) ? DSL.value(since) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, null);
		
		return dsl
			.selectCount()
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(overlaps)
				.and(filterCndt)
			)
			.fetchOne(0, Integer.class);
	}
	
	public void lazy_viewOnlineEventObjectsByCalendarSinceCondition(Connection con, Collection<Integer> calendarIds, DateTime since, Condition condition, boolean statFields, int limit, int offset, VEventObject.Consumer consumer) throws DAOException, WTException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		Param<DateTime> rangeFromPar = (since != null) ? DSL.value(since) : null;
		Condition overlapsCndt = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, null)
			.isTrue();
		
		/*
		Condition overlapsCndt = DSL.trueCondition();
		if (since != null) {
			Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
				.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, DSL.value(since), null);
			overlapsCndt = overlaps.isTrue();
		}
		*/
		
		/*
		// New field: overlaps
		Param<DateTime> rangeFromPar = (since != null) ? DSL.value(since) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, null);
		*/
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(EVENTS_RECURRENCES.EVENT_ID, true, false).as("has_recurrence");
		
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
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_ATTACHMENTS)
				.where(EVENTS_ATTACHMENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_attachments");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar");
		
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_CUSTOM_VALUES)
				.where(EVENTS_CUSTOM_VALUES.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_custom_values");
		
		Cursor<Record> cursor = dsl
			.select(
				getVEventObjectFields(statFields)
			)
			.select(
				tags,
				hasRecurrence,
				attendeesCount,
				notifyableAttendeesCount,
				hasAttachments,
				hasICalendar,
				hasCustomValues
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.leftOuterJoin(EVENTS_ICALENDARS).on(EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(overlapsCndt)
				//.and(overlaps)
				.and(filterCndt)
			)
			.orderBy(
				EVENTS.EVENT_ID.asc()
			)
			.limit(limit)
			.offset(offset)
			.fetchLazy();

		try {
			for(;;) {
				VEventObject veo = cursor.fetchNextInto(VEventObject.class);
				if (veo == null) break;
				consumer.consume(veo, con);
			}
		} finally {
			cursor.close();
		}
	}
	
	public VEventObject viewEventObjectById(Connection con, Collection<Integer> calendarId, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition calCndt = DSL.trueCondition();
		if (calendarId != null && !calendarId.isEmpty()) {
			calCndt = EVENTS.CALENDAR_ID.in(calendarId);
		}
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(EVENTS_RECURRENCES.EVENT_ID, true, false).as("has_recurrence");
		
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
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_ATTACHMENTS)
				.where(EVENTS_ATTACHMENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_attachments");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar");
		
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_CUSTOM_VALUES)
				.where(EVENTS_CUSTOM_VALUES.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_custom_values");
		
		return dsl
			.select(
				getVEventObjectFields(false)
			)
			.select(
				tags,
				hasRecurrence,
				attendeesCount,
				notifyableAttendeesCount,
				hasAttachments,
				hasICalendar,
				hasCustomValues
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.leftOuterJoin(EVENTS_ICALENDARS).on(EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID))
			.where(
				EVENTS.EVENT_ID.equal(eventId)
				.and(calCndt)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(VEventObject.class);
	}
	
	public Map<String, List<VEventObject>> viewOnlineEventObjectsByCalendar(Connection con, boolean stat, int calendarId) throws DAOException {
		return viewOnlineEventObjectsByCalendarHrefsSince(con, stat, calendarId, null, null);
	}
	
	public Map<String, List<VEventObject>> viewOnlineEventObjectsByCalendarHrefs(Connection con, boolean stat, int calendarId, Collection<String> hrefs) throws DAOException {
		return viewOnlineEventObjectsByCalendarHrefsSince(con, stat, calendarId, hrefs, null);
	}
	
	public Map<String, List<VEventObject>> viewOnlineEventObjectsByCalendarSince(Connection con, boolean stat, int calendarId, DateTime since) throws DAOException {
		return viewOnlineEventObjectsByCalendarHrefsSince(con, stat, calendarId, null, since);
	}
	
	public Map<String, List<VEventObject>> viewOnlineEventObjectsByCalendarHrefsSince(Connection con, boolean stat, int calendarId, Collection<String> hrefs, DateTime since) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition inHrefsCndt = DSL.trueCondition();
		if (hrefs != null) {
			inHrefsCndt = EVENTS.HREF.in(hrefs);
		}
		
		Param<DateTime> rangeFromPar = (since != null) ? DSL.value(since) : null;
		Condition overlapsCndt = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, null)
			.isTrue();
		
		/*
		Condition overlapsCndt = DSL.trueCondition();
		if (since != null) {
			Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
				.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, DSL.value(since), null);
			overlapsCndt = overlaps.isTrue();
		}
		*/
		
		/*
		// New field: overlaps
		Param<DateTime> rangeFromPar = (since != null) ? DSL.value(since) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, null);
		*/
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(EVENTS_RECURRENCES.EVENT_ID, true, false).as("has_recurrence");
		
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
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_ATTACHMENTS)
				.where(EVENTS_ATTACHMENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_attachments");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar");
	
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_CUSTOM_VALUES)
				.where(EVENTS_CUSTOM_VALUES.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_custom_values");
		
		return dsl
			.select(
				getVEventObjectFields(stat)
			)
			.select(
				tags,
				hasRecurrence,
				attendeesCount,
				notifyableAttendeesCount,
				hasAttachments,
				hasICalendar,
				hasCustomValues
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.leftOuterJoin(EVENTS_ICALENDARS).on(EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID))
			.where(
				EVENTS.CALENDAR_ID.equal(calendarId)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(inHrefsCndt)
				.and(overlapsCndt)
				//.and(overlaps)
			)
			.orderBy(
				EVENTS.EVENT_ID.asc()
			)
			.fetchGroups(EVENTS.HREF, VEventObject.class);
	}
	
	public void lazy_viewChangedEventObjects(Connection con, Collection<Integer> calendarIds, Condition condition, boolean statFields, int limit, int offset, VEventObjectChanged.Consumer consumer) throws DAOException, WTException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(EVENTS_RECURRENCES.EVENT_ID, true, false).as("has_recurrence");
		
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
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_ATTACHMENTS)
				.where(EVENTS_ATTACHMENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_attachments");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar");
	
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_CUSTOM_VALUES)
				.where(EVENTS_CUSTOM_VALUES.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_custom_values");
		
		Cursor<Record> cursor = dsl
			.select(
				HISTORY_EVENTS.CHANGE_TIMESTAMP,
				HISTORY_EVENTS.CHANGE_TYPE
			)
			.select(
				getVEventObjectFields(statFields)
			)
			.select(
				tags,
				hasRecurrence,
				attendeesCount,
				notifyableAttendeesCount,
				hasAttachments,
				hasICalendar,
				hasCustomValues
			)
			.distinctOn(HISTORY_EVENTS.EVENT_ID)
			.from(HISTORY_EVENTS)
			.leftOuterJoin(EVENTS).on(HISTORY_EVENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
			.leftOuterJoin(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.leftOuterJoin(EVENTS_ICALENDARS).on(EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID))
			.where(
				HISTORY_EVENTS.CALENDAR_ID.in(calendarIds)
				.and(filterCndt)
			)
			.orderBy(
				HISTORY_EVENTS.EVENT_ID.asc(),
				HISTORY_EVENTS.ID.desc()
			)
			.limit(limit)
			.offset(offset)
			.fetchLazy();
		
		try {
			for(;;) {
				VEventObjectChanged vco = cursor.fetchNextInto(VEventObjectChanged.class);
				if (vco == null) break;
				consumer.consume(vco, con);
			}
		} finally {
			cursor.close();
		}
	}
	
	public int countByCalendarCondition(Connection con, Collection<Integer> calendarIds, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		return dsl
			.selectCount()
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.where(
				EVENTS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(
					filterCndt
				)
			)
			.fetchOne(0, Integer.class);
	}
	
	public boolean existByCalendarCondition(Connection con, Collection<Integer> calendarIds, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
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
				.and(
					filterCndt
				)
			)
		);
	}
	
	public List<VEventLookup> viewOnlineByCalendarRangeCondition(Connection con, Collection<Integer> calendarIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		//https://gitlab.com/davical-project/davical/-/blob/master/dba/rrule_functions.sql
		//https://github.com/volkanunsal/postgres-rrule
		//SELECT * FROM rrule_event_instances_range('2021-04-20 18:16:24+02'::timestamptz, 'FREQ=DAILY;COUNT=5', '2021-04-21 00:00:00+02'::timestamptz, '2021-04-29 00:00:00+02'::timestamptz, 100)
		
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (rangeFrom != null) ? DSL.value(rangeFrom) : null;
		Param<DateTime> rangeToPar = (rangeTo != null) ? DSL.value(rangeTo) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, rangeToPar);
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(EVENTS_RECURRENCES.EVENT_ID, true, false).as("has_recurrence");
		
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
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_ATTACHMENTS)
				.where(EVENTS_ATTACHMENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_attachments");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar");
	
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_CUSTOM_VALUES)
				.where(EVENTS_CUSTOM_VALUES.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_custom_values");
		
		return dsl
			.select(
				getVEventObjectFields(false)
			)
			.select(
				tags,
				hasRecurrence,
				attendeesCount,
				notifyableAttendeesCount,
				hasAttachments,
				hasICalendar,
				hasCustomValues,
				CALENDARS.NAME.as("calendar_name"),
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.leftOuterJoin(EVENTS_ICALENDARS).on(EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID))
			.where(
				CALENDARS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(overlaps)
				.and(filterCndt)
			)
			.orderBy(
				EVENTS.EVENT_ID
			)
			.fetchInto(VEventLookup.class);
	}
	
	
	public List<VEventBounds> viewOnlineBoundsByCalendarRangeCondition(Connection con, Collection<Integer> calendarIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (rangeFrom != null) ? DSL.value(rangeFrom) : null;
		Param<DateTime> rangeToPar = (rangeTo != null) ? DSL.value(rangeTo) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, rangeToPar);
		
		return dsl
			.select(
				EVENTS.EVENT_ID,
				EVENTS.SERIES_EVENT_ID,
				EVENTS.SERIES_INSTANCE_ID,
				EVENTS.PUBLIC_UID.as("public_id"),
				EVENTS.CALENDAR_ID,
				EVENTS.START,
				EVENTS.END,
				EVENTS.TIMEZONE,
				EVENTS.ALL_DAY
			)
			.select(
				EVENTS_RECURRENCES.START.as("recurrence_start"),
				EVENTS_RECURRENCES.RULE.as("recurrence_rule")
			)
			.select(
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.where(
				CALENDARS.CALENDAR_ID.in(calendarIds)
				.and(
					EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				)
				.and(overlaps)
				.and(filterCndt)
			)
			.orderBy(
				EVENTS.EVENT_ID
			)
			.fetchInto(VEventBounds.class);
	}
	
	public List<VEventLookup> viewOnlineExpiredByRangeForUpdate(Connection con, DateTime rangeFrom, DateTime rangeTo) {
		DSLContext dsl = getDSL(con);
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (rangeFrom != null) ? DSL.value(rangeFrom) : null;
		Param<DateTime> rangeToPar = (rangeTo != null) ? DSL.value(rangeTo) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(EVENTS.START, EVENTS.END, EVENTS_RECURRENCES.RULE, rangeFromPar, rangeToPar);
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(EVENTS_TAGS.TAG_ID, "|"))
			.from(EVENTS_TAGS)
			.where(
				EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
			).asField("tags");
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(EVENTS_RECURRENCES.EVENT_ID, true, false).as("has_recurrence");
		
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
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_ATTACHMENTS)
				.where(EVENTS_ATTACHMENTS.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_attachments");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(EVENTS_ICALENDARS.EVENT_ID, true, false).as("has_icalendar");
	
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(EVENTS_CUSTOM_VALUES)
				.where(EVENTS_CUSTOM_VALUES.EVENT_ID.equal(EVENTS.EVENT_ID))
			)).as("has_custom_values");
		
		return dsl
			.select(
				getVEventObjectFields(false)
			)
			.select(
				EVENTS.REMINDER,
				EVENTS.REMINDED_AT // Important for recurring events, see WHERE part below!
			)
			.select(
				tags,
				hasRecurrence,
				attendeesCount,
				notifyableAttendeesCount,
				hasAttachments,
				hasICalendar,
				hasCustomValues,
				CALENDARS.NAME.as("calendar_name"),
				CALENDARS.DOMAIN_ID.as("calendar_domain_id"),
				CALENDARS.USER_ID.as("calendar_user_id")
			)
			.from(EVENTS)
			.join(CALENDARS).on(EVENTS.CALENDAR_ID.equal(CALENDARS.CALENDAR_ID))
			.leftOuterJoin(EVENTS_RECURRENCES).on(EVENTS.EVENT_ID.equal(EVENTS_RECURRENCES.EVENT_ID))
			.leftOuterJoin(EVENTS_ICALENDARS).on(EVENTS.EVENT_ID.equal(EVENTS_ICALENDARS.EVENT_ID))
			.where(
				EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.NEW))
					.or(EVENTS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Event.RevisionStatus.MODIFIED)))
				.and(EVENTS.START.isNotNull())
				.and(
					EVENTS.REMINDER.isNotNull().and(
						EVENTS.EVENT_ID.isNull().and(EVENTS.REMINDED_AT.isNull()) // Normal events: REMINDED_AT must be null!
						.or(EVENTS.EVENT_ID.isNotNull()) // Recurring events: REMINDED_AT can be null or not, calling code will check it!
					)
				)
				.and(overlaps)
			)
			.fetchInto(VEventLookup.class);
	}
	
	public List<String> selectOnlineIdsByCategoryHrefs(Connection con, int calendarId, String href) throws DAOException {
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
	
	public Map<Integer, DateTime> selectMaxRevTimestampByCategories(Connection con, Collection<Integer> calendarIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				HISTORY_EVENTS.CALENDAR_ID,
				DSL.max(HISTORY_EVENTS.CHANGE_TIMESTAMP)
			)
			.from(HISTORY_EVENTS)
			.where(
				HISTORY_EVENTS.CALENDAR_ID.in(calendarIds)
			)
			.groupBy(
				HISTORY_EVENTS.CALENDAR_ID
			)
			.fetchMap(HISTORY_EVENTS.CALENDAR_ID, DSL.max(HISTORY_EVENTS.CHANGE_TIMESTAMP));
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
	
	public static Condition createTransparencyCondition(EventBase.Transparency transparency) {
		Check.notNull(transparency, "transparency");
		return EVENTS.TRANSPARENCY.equal(EnumUtils.toSerializedName(transparency));
	}
}
