/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.calendar.jooq;

/**
 * A class modelling foreign key relationships between tables of the <code>calendar</code> 
 * schema
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.Identity<com.sonicle.webtop.calendar.jooq.tables.records.CalendarsRecord, java.lang.Integer> IDENTITY_CALENDARS = Identities0.IDENTITY_CALENDARS;
	public static final org.jooq.Identity<com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord, java.lang.Integer> IDENTITY_EVENTS = Identities0.IDENTITY_EVENTS;
	public static final org.jooq.Identity<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> IDENTITY_RECURRENCES = Identities0.IDENTITY_RECURRENCES;

	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.CalendarPropsRecord> CALENDAR_PROPS_PKEY = UniqueKeys0.CALENDAR_PROPS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.CalendarsRecord> CALENDARS_PKEY1 = UniqueKeys0.CALENDARS_PKEY1;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_PKEY = UniqueKeys0.EVENTS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsRecord> EVENTS_ATTACHMENTS_PKEY = UniqueKeys0.EVENTS_ATTACHMENTS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsDataRecord> EVENTS_ATTACHMENTS_DATA_PKEY = UniqueKeys0.EVENTS_ATTACHMENTS_DATA_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttendeesRecord> EVENTS_PLANNING_PKEY = UniqueKeys0.EVENTS_PLANNING_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord> EVENTS_CUSTOM_VALUES_PKEY = UniqueKeys0.EVENTS_CUSTOM_VALUES_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsIcalendarsRecord> EVENTS_ICALENDARS_PKEY = UniqueKeys0.EVENTS_ICALENDARS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsTagsRecord> EVENTS_TAGS_PKEY = UniqueKeys0.EVENTS_TAGS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord> RECURRENCES_PKEY = UniqueKeys0.RECURRENCES_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord> RECURRENCES_BROKEN_PKEY = UniqueKeys0.RECURRENCES_BROKEN_PKEY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_ATTACHMENTS__EVENTS_ATTACHMENTS_EVENT_ID_FKEY = ForeignKeys0.EVENTS_ATTACHMENTS__EVENTS_ATTACHMENTS_EVENT_ID_FKEY;
	public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsDataRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsRecord> EVENTS_ATTACHMENTS_DATA__EVENTS_ATTACHMENTS_DATA_EVENT_ATTACHMENT_ID_FKEY = ForeignKeys0.EVENTS_ATTACHMENTS_DATA__EVENTS_ATTACHMENTS_DATA_EVENT_ATTACHMENT_ID_FKEY;
	public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttendeesRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_ATTENDEES__EVENTS_ATTENDEES_EVENT_ID_FKEY = ForeignKeys0.EVENTS_ATTENDEES__EVENTS_ATTENDEES_EVENT_ID_FKEY;
	public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_CUSTOM_VALUES__EVENTS_CUSTOM_VALUES_EVENT_ID_FKEY = ForeignKeys0.EVENTS_CUSTOM_VALUES__EVENTS_CUSTOM_VALUES_EVENT_ID_FKEY;
	public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsIcalendarsRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_ICALENDARS__EVENTS_ICALENDARS_EVENT_ID_FKEY = ForeignKeys0.EVENTS_ICALENDARS__EVENTS_ICALENDARS_EVENT_ID_FKEY;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class Identities0 extends org.jooq.impl.AbstractKeys {
		public static org.jooq.Identity<com.sonicle.webtop.calendar.jooq.tables.records.CalendarsRecord, java.lang.Integer> IDENTITY_CALENDARS = createIdentity(com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS, com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS.CALENDAR_ID);
		public static org.jooq.Identity<com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord, java.lang.Integer> IDENTITY_EVENTS = createIdentity(com.sonicle.webtop.calendar.jooq.tables.Events.EVENTS, com.sonicle.webtop.calendar.jooq.tables.Events.EVENTS.EVENT_ID);
		public static org.jooq.Identity<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> IDENTITY_RECURRENCES = createIdentity(com.sonicle.webtop.calendar.jooq.tables.Recurrences.RECURRENCES, com.sonicle.webtop.calendar.jooq.tables.Recurrences.RECURRENCES.RECURRENCE_ID);
	}

	private static class UniqueKeys0 extends org.jooq.impl.AbstractKeys {
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.CalendarPropsRecord> CALENDAR_PROPS_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS, com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.DOMAIN_ID, com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.USER_ID, com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.CALENDAR_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.CalendarsRecord> CALENDARS_PKEY1 = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS, com.sonicle.webtop.calendar.jooq.tables.Calendars.CALENDARS.CALENDAR_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.Events.EVENTS, com.sonicle.webtop.calendar.jooq.tables.Events.EVENTS.EVENT_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsRecord> EVENTS_ATTACHMENTS_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.EventsAttachments.EVENTS_ATTACHMENTS, com.sonicle.webtop.calendar.jooq.tables.EventsAttachments.EVENTS_ATTACHMENTS.EVENT_ATTACHMENT_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsDataRecord> EVENTS_ATTACHMENTS_DATA_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.EventsAttachmentsData.EVENTS_ATTACHMENTS_DATA, com.sonicle.webtop.calendar.jooq.tables.EventsAttachmentsData.EVENTS_ATTACHMENTS_DATA.EVENT_ATTACHMENT_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttendeesRecord> EVENTS_PLANNING_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.EventsAttendees.EVENTS_ATTENDEES, com.sonicle.webtop.calendar.jooq.tables.EventsAttendees.EVENTS_ATTENDEES.ATTENDEE_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord> EVENTS_CUSTOM_VALUES_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues.EVENTS_CUSTOM_VALUES, com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues.EVENTS_CUSTOM_VALUES.EVENT_ID, com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues.EVENTS_CUSTOM_VALUES.CUSTOM_FIELD_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsIcalendarsRecord> EVENTS_ICALENDARS_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.EventsIcalendars.EVENTS_ICALENDARS, com.sonicle.webtop.calendar.jooq.tables.EventsIcalendars.EVENTS_ICALENDARS.EVENT_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsTagsRecord> EVENTS_TAGS_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.EventsTags.EVENTS_TAGS, com.sonicle.webtop.calendar.jooq.tables.EventsTags.EVENTS_TAGS.EVENT_ID, com.sonicle.webtop.calendar.jooq.tables.EventsTags.EVENTS_TAGS.TAG_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord> RECURRENCES_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.Recurrences.RECURRENCES, com.sonicle.webtop.calendar.jooq.tables.Recurrences.RECURRENCES.RECURRENCE_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord> RECURRENCES_BROKEN_PKEY = createUniqueKey(com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken.RECURRENCES_BROKEN, com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken.RECURRENCES_BROKEN.EVENT_ID, com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken.RECURRENCES_BROKEN.RECURRENCE_ID, com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken.RECURRENCES_BROKEN.EVENT_DATE);
	}

	private static class ForeignKeys0 extends org.jooq.impl.AbstractKeys {
		public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_ATTACHMENTS__EVENTS_ATTACHMENTS_EVENT_ID_FKEY = createForeignKey(com.sonicle.webtop.calendar.jooq.Keys.EVENTS_PKEY, com.sonicle.webtop.calendar.jooq.tables.EventsAttachments.EVENTS_ATTACHMENTS, com.sonicle.webtop.calendar.jooq.tables.EventsAttachments.EVENTS_ATTACHMENTS.EVENT_ID);
		public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsDataRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsRecord> EVENTS_ATTACHMENTS_DATA__EVENTS_ATTACHMENTS_DATA_EVENT_ATTACHMENT_ID_FKEY = createForeignKey(com.sonicle.webtop.calendar.jooq.Keys.EVENTS_ATTACHMENTS_PKEY, com.sonicle.webtop.calendar.jooq.tables.EventsAttachmentsData.EVENTS_ATTACHMENTS_DATA, com.sonicle.webtop.calendar.jooq.tables.EventsAttachmentsData.EVENTS_ATTACHMENTS_DATA.EVENT_ATTACHMENT_ID);
		public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsAttendeesRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_ATTENDEES__EVENTS_ATTENDEES_EVENT_ID_FKEY = createForeignKey(com.sonicle.webtop.calendar.jooq.Keys.EVENTS_PKEY, com.sonicle.webtop.calendar.jooq.tables.EventsAttendees.EVENTS_ATTENDEES, com.sonicle.webtop.calendar.jooq.tables.EventsAttendees.EVENTS_ATTENDEES.EVENT_ID);
		public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_CUSTOM_VALUES__EVENTS_CUSTOM_VALUES_EVENT_ID_FKEY = createForeignKey(com.sonicle.webtop.calendar.jooq.Keys.EVENTS_PKEY, com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues.EVENTS_CUSTOM_VALUES, com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues.EVENTS_CUSTOM_VALUES.EVENT_ID);
		public static final org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsIcalendarsRecord, com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> EVENTS_ICALENDARS__EVENTS_ICALENDARS_EVENT_ID_FKEY = createForeignKey(com.sonicle.webtop.calendar.jooq.Keys.EVENTS_PKEY, com.sonicle.webtop.calendar.jooq.tables.EventsIcalendars.EVENTS_ICALENDARS, com.sonicle.webtop.calendar.jooq.tables.EventsIcalendars.EVENTS_ICALENDARS.EVENT_ID);
	}
}
