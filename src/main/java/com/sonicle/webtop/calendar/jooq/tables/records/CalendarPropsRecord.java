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
public class CalendarPropsRecord extends org.jooq.impl.UpdatableRecordImpl<com.sonicle.webtop.calendar.jooq.tables.records.CalendarPropsRecord> implements org.jooq.Record6<java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = 1957139675;

	/**
	 * Setter for <code>calendar.calendar_props.domain_id</code>.
	 */
	public void setDomainId(java.lang.String value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>calendar.calendar_props.domain_id</code>.
	 */
	public java.lang.String getDomainId() {
		return (java.lang.String) getValue(0);
	}

	/**
	 * Setter for <code>calendar.calendar_props.user_id</code>.
	 */
	public void setUserId(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>calendar.calendar_props.user_id</code>.
	 */
	public java.lang.String getUserId() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>calendar.calendar_props.calendar_id</code>.
	 */
	public void setCalendarId(java.lang.Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>calendar.calendar_props.calendar_id</code>.
	 */
	public java.lang.Integer getCalendarId() {
		return (java.lang.Integer) getValue(2);
	}

	/**
	 * Setter for <code>calendar.calendar_props.hidden</code>.
	 */
	public void setHidden(java.lang.Boolean value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>calendar.calendar_props.hidden</code>.
	 */
	public java.lang.Boolean getHidden() {
		return (java.lang.Boolean) getValue(3);
	}

	/**
	 * Setter for <code>calendar.calendar_props.color</code>.
	 */
	public void setColor(java.lang.String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>calendar.calendar_props.color</code>.
	 */
	public java.lang.String getColor() {
		return (java.lang.String) getValue(4);
	}

	/**
	 * Setter for <code>calendar.calendar_props.sync</code>.
	 */
	public void setSync(java.lang.String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>calendar.calendar_props.sync</code>.
	 */
	public java.lang.String getSync() {
		return (java.lang.String) getValue(5);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record3<java.lang.String, java.lang.String, java.lang.Integer> key() {
		return (org.jooq.Record3) super.key();
	}

	// -------------------------------------------------------------------------
	// Record6 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row6) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row6) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field1() {
		return com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.DOMAIN_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.USER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field3() {
		return com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.CALENDAR_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field4() {
		return com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.HIDDEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field5() {
		return com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.COLOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field6() {
		return com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS.SYNC;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value1() {
		return getDomainId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value3() {
		return getCalendarId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value4() {
		return getHidden();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value5() {
		return getColor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value6() {
		return getSync();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarPropsRecord value1(java.lang.String value) {
		setDomainId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarPropsRecord value2(java.lang.String value) {
		setUserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarPropsRecord value3(java.lang.Integer value) {
		setCalendarId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarPropsRecord value4(java.lang.Boolean value) {
		setHidden(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarPropsRecord value5(java.lang.String value) {
		setColor(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarPropsRecord value6(java.lang.String value) {
		setSync(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarPropsRecord values(java.lang.String value1, java.lang.String value2, java.lang.Integer value3, java.lang.Boolean value4, java.lang.String value5, java.lang.String value6) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached CalendarPropsRecord
	 */
	public CalendarPropsRecord() {
		super(com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS);
	}

	/**
	 * Create a detached, initialised CalendarPropsRecord
	 */
	public CalendarPropsRecord(java.lang.String domainId, java.lang.String userId, java.lang.Integer calendarId, java.lang.Boolean hidden, java.lang.String color, java.lang.String sync) {
		super(com.sonicle.webtop.calendar.jooq.tables.CalendarProps.CALENDAR_PROPS);

		setValue(0, domainId);
		setValue(1, userId);
		setValue(2, calendarId);
		setValue(3, hidden);
		setValue(4, color);
		setValue(5, sync);
	}
}