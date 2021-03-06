/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.calendar.jooq.tables.records;

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
public class EventsRecord extends org.jooq.impl.UpdatableRecordImpl<com.sonicle.webtop.calendar.jooq.tables.records.EventsRecord> {

	private static final long serialVersionUID = 1081524256;

	/**
	 * Setter for <code>calendar.events.event_id</code>.
	 */
	public void setEventId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>calendar.events.event_id</code>.
	 */
	public java.lang.Integer getEventId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>calendar.events.calendar_id</code>.
	 */
	public void setCalendarId(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>calendar.events.calendar_id</code>.
	 */
	public java.lang.Integer getCalendarId() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>calendar.events.recurrence_id</code>.
	 */
	public void setRecurrenceId(java.lang.Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>calendar.events.recurrence_id</code>.
	 */
	public java.lang.Integer getRecurrenceId() {
		return (java.lang.Integer) getValue(2);
	}

	/**
	 * Setter for <code>calendar.events.start_date</code>.
	 */
	public void setStartDate(org.joda.time.DateTime value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>calendar.events.start_date</code>.
	 */
	public org.joda.time.DateTime getStartDate() {
		return (org.joda.time.DateTime) getValue(3);
	}

	/**
	 * Setter for <code>calendar.events.end_date</code>.
	 */
	public void setEndDate(org.joda.time.DateTime value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>calendar.events.end_date</code>.
	 */
	public org.joda.time.DateTime getEndDate() {
		return (org.joda.time.DateTime) getValue(4);
	}

	/**
	 * Setter for <code>calendar.events.timezone</code>.
	 */
	public void setTimezone(java.lang.String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>calendar.events.timezone</code>.
	 */
	public java.lang.String getTimezone() {
		return (java.lang.String) getValue(5);
	}

	/**
	 * Setter for <code>calendar.events.all_day</code>.
	 */
	public void setAllDay(java.lang.Boolean value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>calendar.events.all_day</code>.
	 */
	public java.lang.Boolean getAllDay() {
		return (java.lang.Boolean) getValue(6);
	}

	/**
	 * Setter for <code>calendar.events.title</code>.
	 */
	public void setTitle(java.lang.String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>calendar.events.title</code>.
	 */
	public java.lang.String getTitle() {
		return (java.lang.String) getValue(7);
	}

	/**
	 * Setter for <code>calendar.events.description</code>.
	 */
	public void setDescription(java.lang.String value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>calendar.events.description</code>.
	 */
	public java.lang.String getDescription() {
		return (java.lang.String) getValue(8);
	}

	/**
	 * Setter for <code>calendar.events.location</code>.
	 */
	public void setLocation(java.lang.String value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>calendar.events.location</code>.
	 */
	public java.lang.String getLocation() {
		return (java.lang.String) getValue(9);
	}

	/**
	 * Setter for <code>calendar.events.is_private</code>.
	 */
	public void setIsPrivate(java.lang.Boolean value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>calendar.events.is_private</code>.
	 */
	public java.lang.Boolean getIsPrivate() {
		return (java.lang.Boolean) getValue(10);
	}

	/**
	 * Setter for <code>calendar.events.busy</code>.
	 */
	public void setBusy(java.lang.Boolean value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>calendar.events.busy</code>.
	 */
	public java.lang.Boolean getBusy() {
		return (java.lang.Boolean) getValue(11);
	}

	/**
	 * Setter for <code>calendar.events.reminder</code>.
	 */
	public void setReminder(java.lang.Integer value) {
		setValue(12, value);
	}

	/**
	 * Getter for <code>calendar.events.reminder</code>.
	 */
	public java.lang.Integer getReminder() {
		return (java.lang.Integer) getValue(12);
	}

	/**
	 * Setter for <code>calendar.events.read_only</code>.
	 */
	public void setReadOnly(java.lang.Boolean value) {
		setValue(13, value);
	}

	/**
	 * Getter for <code>calendar.events.read_only</code>.
	 */
	public java.lang.Boolean getReadOnly() {
		return (java.lang.Boolean) getValue(13);
	}

	/**
	 * Setter for <code>calendar.events.revision_status</code>.
	 */
	public void setRevisionStatus(java.lang.String value) {
		setValue(14, value);
	}

	/**
	 * Getter for <code>calendar.events.revision_status</code>.
	 */
	public java.lang.String getRevisionStatus() {
		return (java.lang.String) getValue(14);
	}

	/**
	 * Setter for <code>calendar.events.revision_timestamp</code>.
	 */
	public void setRevisionTimestamp(org.joda.time.DateTime value) {
		setValue(15, value);
	}

	/**
	 * Getter for <code>calendar.events.revision_timestamp</code>.
	 */
	public org.joda.time.DateTime getRevisionTimestamp() {
		return (org.joda.time.DateTime) getValue(15);
	}

	/**
	 * Setter for <code>calendar.events.public_uid</code>.
	 */
	public void setPublicUid(java.lang.String value) {
		setValue(16, value);
	}

	/**
	 * Getter for <code>calendar.events.public_uid</code>.
	 */
	public java.lang.String getPublicUid() {
		return (java.lang.String) getValue(16);
	}

	/**
	 * Setter for <code>calendar.events.reminded_on</code>.
	 */
	public void setRemindedOn(org.joda.time.DateTime value) {
		setValue(17, value);
	}

	/**
	 * Getter for <code>calendar.events.reminded_on</code>.
	 */
	public org.joda.time.DateTime getRemindedOn() {
		return (org.joda.time.DateTime) getValue(17);
	}

	/**
	 * Setter for <code>calendar.events.activity_id</code>.
	 */
	public void setActivityId(java.lang.Integer value) {
		setValue(18, value);
	}

	/**
	 * Getter for <code>calendar.events.activity_id</code>.
	 */
	public java.lang.Integer getActivityId() {
		return (java.lang.Integer) getValue(18);
	}

	/**
	 * Setter for <code>calendar.events.master_data_id</code>.
	 */
	public void setMasterDataId(java.lang.String value) {
		setValue(19, value);
	}

	/**
	 * Getter for <code>calendar.events.master_data_id</code>.
	 */
	public java.lang.String getMasterDataId() {
		return (java.lang.String) getValue(19);
	}

	/**
	 * Setter for <code>calendar.events.stat_master_data_id</code>.
	 */
	public void setStatMasterDataId(java.lang.String value) {
		setValue(20, value);
	}

	/**
	 * Getter for <code>calendar.events.stat_master_data_id</code>.
	 */
	public java.lang.String getStatMasterDataId() {
		return (java.lang.String) getValue(20);
	}

	/**
	 * Setter for <code>calendar.events.causal_id</code>.
	 */
	public void setCausalId(java.lang.Integer value) {
		setValue(21, value);
	}

	/**
	 * Getter for <code>calendar.events.causal_id</code>.
	 */
	public java.lang.Integer getCausalId() {
		return (java.lang.Integer) getValue(21);
	}

	/**
	 * Setter for <code>calendar.events.organizer</code>.
	 */
	public void setOrganizer(java.lang.String value) {
		setValue(22, value);
	}

	/**
	 * Getter for <code>calendar.events.organizer</code>.
	 */
	public java.lang.String getOrganizer() {
		return (java.lang.String) getValue(22);
	}

	/**
	 * Setter for <code>calendar.events.revision_sequence</code>.
	 */
	public void setRevisionSequence(java.lang.Integer value) {
		setValue(23, value);
	}

	/**
	 * Getter for <code>calendar.events.revision_sequence</code>.
	 */
	public java.lang.Integer getRevisionSequence() {
		return (java.lang.Integer) getValue(23);
	}

	/**
	 * Setter for <code>calendar.events.href</code>.
	 */
	public void setHref(java.lang.String value) {
		setValue(24, value);
	}

	/**
	 * Getter for <code>calendar.events.href</code>.
	 */
	public java.lang.String getHref() {
		return (java.lang.String) getValue(24);
	}

	/**
	 * Setter for <code>calendar.events.etag</code>.
	 */
	public void setEtag(java.lang.String value) {
		setValue(25, value);
	}

	/**
	 * Getter for <code>calendar.events.etag</code>.
	 */
	public java.lang.String getEtag() {
		return (java.lang.String) getValue(25);
	}

	/**
	 * Setter for <code>calendar.events.handle_invitation</code>.
	 */
	public void setHandleInvitation(java.lang.Boolean value) {
		setValue(26, value);
	}

	/**
	 * Getter for <code>calendar.events.handle_invitation</code>.
	 */
	public java.lang.Boolean getHandleInvitation() {
		return (java.lang.Boolean) getValue(26);
	}

	/**
	 * Setter for <code>calendar.events.creation_timestamp</code>.
	 */
	public void setCreationTimestamp(org.joda.time.DateTime value) {
		setValue(27, value);
	}

	/**
	 * Getter for <code>calendar.events.creation_timestamp</code>.
	 */
	public org.joda.time.DateTime getCreationTimestamp() {
		return (org.joda.time.DateTime) getValue(27);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached EventsRecord
	 */
	public EventsRecord() {
		super(com.sonicle.webtop.calendar.jooq.tables.Events.EVENTS);
	}

	/**
	 * Create a detached, initialised EventsRecord
	 */
	public EventsRecord(java.lang.Integer eventId, java.lang.Integer calendarId, java.lang.Integer recurrenceId, org.joda.time.DateTime startDate, org.joda.time.DateTime endDate, java.lang.String timezone, java.lang.Boolean allDay, java.lang.String title, java.lang.String description, java.lang.String location, java.lang.Boolean isPrivate, java.lang.Boolean busy, java.lang.Integer reminder, java.lang.Boolean readOnly, java.lang.String revisionStatus, org.joda.time.DateTime revisionTimestamp, java.lang.String publicUid, org.joda.time.DateTime remindedOn, java.lang.Integer activityId, java.lang.String masterDataId, java.lang.String statMasterDataId, java.lang.Integer causalId, java.lang.String organizer, java.lang.Integer revisionSequence, java.lang.String href, java.lang.String etag, java.lang.Boolean handleInvitation, org.joda.time.DateTime creationTimestamp) {
		super(com.sonicle.webtop.calendar.jooq.tables.Events.EVENTS);

		setValue(0, eventId);
		setValue(1, calendarId);
		setValue(2, recurrenceId);
		setValue(3, startDate);
		setValue(4, endDate);
		setValue(5, timezone);
		setValue(6, allDay);
		setValue(7, title);
		setValue(8, description);
		setValue(9, location);
		setValue(10, isPrivate);
		setValue(11, busy);
		setValue(12, reminder);
		setValue(13, readOnly);
		setValue(14, revisionStatus);
		setValue(15, revisionTimestamp);
		setValue(16, publicUid);
		setValue(17, remindedOn);
		setValue(18, activityId);
		setValue(19, masterDataId);
		setValue(20, statMasterDataId);
		setValue(21, causalId);
		setValue(22, organizer);
		setValue(23, revisionSequence);
		setValue(24, href);
		setValue(25, etag);
		setValue(26, handleInvitation);
		setValue(27, creationTimestamp);
	}
}
