/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.calendar.jooq.tables;

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
public class RecurrencesBroken extends org.jooq.impl.TableImpl<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord> {

	private static final long serialVersionUID = -1055231334;

	/**
	 * The reference instance of <code>calendar.recurrences_broken</code>
	 */
	public static final com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken RECURRENCES_BROKEN = new com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord> getRecordType() {
		return com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord.class;
	}

	/**
	 * The column <code>calendar.recurrences_broken.event_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord, java.lang.Integer> EVENT_ID = createField("event_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>calendar.recurrences_broken.recurrence_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord, java.lang.Integer> RECURRENCE_ID = createField("recurrence_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>calendar.recurrences_broken.event_date</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord, org.joda.time.LocalDate> EVENT_DATE = createField("event_date", org.jooq.impl.SQLDataType.DATE.nullable(false), this, "", new com.sonicle.webtop.core.jooq.LocalDateConverter());

	/**
	 * The column <code>calendar.recurrences_broken.new_event_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord, java.lang.Integer> NEW_EVENT_ID = createField("new_event_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * Create a <code>calendar.recurrences_broken</code> table reference
	 */
	public RecurrencesBroken() {
		this("recurrences_broken", null);
	}

	/**
	 * Create an aliased <code>calendar.recurrences_broken</code> table reference
	 */
	public RecurrencesBroken(java.lang.String alias) {
		this(alias, com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken.RECURRENCES_BROKEN);
	}

	private RecurrencesBroken(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord> aliased) {
		this(alias, aliased, null);
	}

	private RecurrencesBroken(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.sonicle.webtop.calendar.jooq.Calendar.CALENDAR, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord> getPrimaryKey() {
		return com.sonicle.webtop.calendar.jooq.Keys.RECURRENCES_BROKEN_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesBrokenRecord>>asList(com.sonicle.webtop.calendar.jooq.Keys.RECURRENCES_BROKEN_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken as(java.lang.String alias) {
		return new com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken rename(java.lang.String name) {
		return new com.sonicle.webtop.calendar.jooq.tables.RecurrencesBroken(name, null);
	}
}