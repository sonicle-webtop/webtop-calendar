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
public class EventsCustomValues extends org.jooq.impl.TableImpl<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord> {

	private static final long serialVersionUID = -435041591;

	/**
	 * The reference instance of <code>calendar.events_custom_values</code>
	 */
	public static final com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues EVENTS_CUSTOM_VALUES = new com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord> getRecordType() {
		return com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord.class;
	}

	/**
	 * The column <code>calendar.events_custom_values.event_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, java.lang.Integer> EVENT_ID = createField("event_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>calendar.events_custom_values.custom_field_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, java.lang.String> CUSTOM_FIELD_ID = createField("custom_field_id", org.jooq.impl.SQLDataType.VARCHAR.length(22).nullable(false), this, "");

	/**
	 * The column <code>calendar.events_custom_values.string_value</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, java.lang.String> STRING_VALUE = createField("string_value", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>calendar.events_custom_values.number_value</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, java.lang.Double> NUMBER_VALUE = createField("number_value", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>calendar.events_custom_values.boolean_value</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, java.lang.Boolean> BOOLEAN_VALUE = createField("boolean_value", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.events_custom_values.date_value</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, org.joda.time.DateTime> DATE_VALUE = createField("date_value", org.jooq.impl.SQLDataType.TIMESTAMP, this, "", new com.sonicle.webtop.core.jooq.DateTimeConverter());

	/**
	 * The column <code>calendar.events_custom_values.text_value</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, java.lang.String> TEXT_VALUE = createField("text_value", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * Create a <code>calendar.events_custom_values</code> table reference
	 */
	public EventsCustomValues() {
		this("events_custom_values", null);
	}

	/**
	 * Create an aliased <code>calendar.events_custom_values</code> table reference
	 */
	public EventsCustomValues(java.lang.String alias) {
		this(alias, com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues.EVENTS_CUSTOM_VALUES);
	}

	private EventsCustomValues(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord> aliased) {
		this(alias, aliased, null);
	}

	private EventsCustomValues(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.sonicle.webtop.calendar.jooq.Calendar.CALENDAR, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord> getPrimaryKey() {
		return com.sonicle.webtop.calendar.jooq.Keys.EVENTS_CUSTOM_VALUES_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord>>asList(com.sonicle.webtop.calendar.jooq.Keys.EVENTS_CUSTOM_VALUES_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<com.sonicle.webtop.calendar.jooq.tables.records.EventsCustomValuesRecord, ?>>asList(com.sonicle.webtop.calendar.jooq.Keys.EVENTS_CUSTOM_VALUES__EVENTS_CUSTOM_VALUES_EVENT_ID_FKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues as(java.lang.String alias) {
		return new com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues rename(java.lang.String name) {
		return new com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues(name, null);
	}
}