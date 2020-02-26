/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.calendar.jooq.tables.pojos;

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
public class EventsCustomValues implements java.io.Serializable {

	private static final long serialVersionUID = 786766344;

	private java.lang.Integer      eventId;
	private java.lang.String       customFieldId;
	private java.lang.String       stringValue;
	private java.lang.Double       numberValue;
	private java.lang.Boolean      booleanValue;
	private org.joda.time.DateTime dateValue;
	private java.lang.String       textValue;

	public EventsCustomValues() {}

	public EventsCustomValues(
		java.lang.Integer      eventId,
		java.lang.String       customFieldId,
		java.lang.String       stringValue,
		java.lang.Double       numberValue,
		java.lang.Boolean      booleanValue,
		org.joda.time.DateTime dateValue,
		java.lang.String       textValue
	) {
		this.eventId = eventId;
		this.customFieldId = customFieldId;
		this.stringValue = stringValue;
		this.numberValue = numberValue;
		this.booleanValue = booleanValue;
		this.dateValue = dateValue;
		this.textValue = textValue;
	}

	public java.lang.Integer getEventId() {
		return this.eventId;
	}

	public void setEventId(java.lang.Integer eventId) {
		this.eventId = eventId;
	}

	public java.lang.String getCustomFieldId() {
		return this.customFieldId;
	}

	public void setCustomFieldId(java.lang.String customFieldId) {
		this.customFieldId = customFieldId;
	}

	public java.lang.String getStringValue() {
		return this.stringValue;
	}

	public void setStringValue(java.lang.String stringValue) {
		this.stringValue = stringValue;
	}

	public java.lang.Double getNumberValue() {
		return this.numberValue;
	}

	public void setNumberValue(java.lang.Double numberValue) {
		this.numberValue = numberValue;
	}

	public java.lang.Boolean getBooleanValue() {
		return this.booleanValue;
	}

	public void setBooleanValue(java.lang.Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public org.joda.time.DateTime getDateValue() {
		return this.dateValue;
	}

	public void setDateValue(org.joda.time.DateTime dateValue) {
		this.dateValue = dateValue;
	}

	public java.lang.String getTextValue() {
		return this.textValue;
	}

	public void setTextValue(java.lang.String textValue) {
		this.textValue = textValue;
	}
}
