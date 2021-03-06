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
public class Recurrences extends org.jooq.impl.TableImpl<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord> {

	private static final long serialVersionUID = -292617957;

	/**
	 * The reference instance of <code>calendar.recurrences</code>
	 */
	public static final com.sonicle.webtop.calendar.jooq.tables.Recurrences RECURRENCES = new com.sonicle.webtop.calendar.jooq.tables.Recurrences();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord> getRecordType() {
		return com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord.class;
	}

	/**
	 * The column <code>calendar.recurrences.recurrence_id</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> RECURRENCE_ID = createField("recurrence_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>calendar.recurrences.start_date</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, org.joda.time.DateTime> START_DATE = createField("start_date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "", new com.sonicle.webtop.core.jooq.DateTimeConverter());

	/**
	 * The column <code>calendar.recurrences.until_date</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, org.joda.time.DateTime> UNTIL_DATE = createField("until_date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "", new com.sonicle.webtop.core.jooq.DateTimeConverter());

	/**
	 * The column <code>calendar.recurrences.repeat</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> REPEAT = createField("repeat", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calendar.recurrences.permanent</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> PERMANENT = createField("permanent", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.type</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.String> TYPE = createField("type", org.jooq.impl.SQLDataType.VARCHAR.length(1), this, "");

	/**
	 * The column <code>calendar.recurrences.daily_freq</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> DAILY_FREQ = createField("daily_freq", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_freq</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> WEEKLY_FREQ = createField("weekly_freq", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_day_1</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> WEEKLY_DAY_1 = createField("weekly_day_1", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_day_2</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> WEEKLY_DAY_2 = createField("weekly_day_2", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_day_3</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> WEEKLY_DAY_3 = createField("weekly_day_3", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_day_4</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> WEEKLY_DAY_4 = createField("weekly_day_4", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_day_5</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> WEEKLY_DAY_5 = createField("weekly_day_5", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_day_6</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> WEEKLY_DAY_6 = createField("weekly_day_6", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.weekly_day_7</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Boolean> WEEKLY_DAY_7 = createField("weekly_day_7", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * The column <code>calendar.recurrences.monthly_freq</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> MONTHLY_FREQ = createField("monthly_freq", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calendar.recurrences.monthly_day</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> MONTHLY_DAY = createField("monthly_day", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calendar.recurrences.yearly_freq</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> YEARLY_FREQ = createField("yearly_freq", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calendar.recurrences.yearly_day</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> YEARLY_DAY = createField("yearly_day", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>calendar.recurrences.rule</code>.
	 */
	public final org.jooq.TableField<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.String> RULE = createField("rule", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * Create a <code>calendar.recurrences</code> table reference
	 */
	public Recurrences() {
		this("recurrences", null);
	}

	/**
	 * Create an aliased <code>calendar.recurrences</code> table reference
	 */
	public Recurrences(java.lang.String alias) {
		this(alias, com.sonicle.webtop.calendar.jooq.tables.Recurrences.RECURRENCES);
	}

	private Recurrences(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord> aliased) {
		this(alias, aliased, null);
	}

	private Recurrences(java.lang.String alias, org.jooq.Table<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, com.sonicle.webtop.calendar.jooq.Calendar.CALENDAR, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord, java.lang.Integer> getIdentity() {
		return com.sonicle.webtop.calendar.jooq.Keys.IDENTITY_RECURRENCES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord> getPrimaryKey() {
		return com.sonicle.webtop.calendar.jooq.Keys.RECURRENCES_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<com.sonicle.webtop.calendar.jooq.tables.records.RecurrencesRecord>>asList(com.sonicle.webtop.calendar.jooq.Keys.RECURRENCES_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public com.sonicle.webtop.calendar.jooq.tables.Recurrences as(java.lang.String alias) {
		return new com.sonicle.webtop.calendar.jooq.tables.Recurrences(alias, this);
	}

	/**
	 * Rename this table
	 */
	public com.sonicle.webtop.calendar.jooq.tables.Recurrences rename(java.lang.String name) {
		return new com.sonicle.webtop.calendar.jooq.tables.Recurrences(name, null);
	}
}
