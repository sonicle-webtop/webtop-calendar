/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.calendar.jooq;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Calendar extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = 1820854398;

	/**
	 * The reference instance of <code>calendar</code>
	 */
	public static final Calendar CALENDAR = new Calendar();

	/**
	 * No further instances allowed
	 */
	private Calendar() {
		super("calendar");
	}

	@Override
	public final java.util.List<org.jooq.Sequence<?>> getSequences() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getSequences0());
		return result;
	}

	private final java.util.List<org.jooq.Sequence<?>> getSequences0() {
		return java.util.Arrays.<org.jooq.Sequence<?>>asList(
			com.sonicle.webtop.calendar.jooq.Sequences.SEQ_CALENDARS,
			com.sonicle.webtop.calendar.jooq.Sequences.SEQ_EVENTS,
			com.sonicle.webtop.calendar.jooq.Sequences.SEQ_RECURRENCES);
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS,
			com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS,
			com.sonicle.webtop.calendar.jooq.tables.Events.EVENTS,
			com.sonicle.webtop.calendar.jooq.tables.EventsAttachments.EVENTS_ATTACHMENTS,
			com.sonicle.webtop.calendar.jooq.tables.EventsAttachmentsData.EVENTS_ATTACHMENTS_DATA,
			com.sonicle.webtop.calendar.jooq.tables.EventsAttendees.EVENTS_ATTENDEES,
			com.sonicle.webtop.calendar.jooq.tables.EventsIcalendars.EVENTS_ICALENDARS,
			com.sonicle.webtop.calendar.jooq.tables.EventsTags.EVENTS_TAGS,
			com.sonicle.webtop.calendar.jooq.tables.Recurrences.RECURRENCES,
			com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken.RECURRENCES_BROKEN);
	}
}
