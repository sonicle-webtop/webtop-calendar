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
public class EventsTags implements java.io.Serializable {

	private static final long serialVersionUID = 1931970304;

	private java.lang.Integer eventId;
	private java.lang.String  tagId;

	public EventsTags() {}

	public EventsTags(
		java.lang.Integer eventId,
		java.lang.String  tagId
	) {
		this.eventId = eventId;
		this.tagId = tagId;
	}

	public java.lang.Integer getEventId() {
		return this.eventId;
	}

	public void setEventId(java.lang.Integer eventId) {
		this.eventId = eventId;
	}

	public java.lang.String getTagId() {
		return this.tagId;
	}

	public void setTagId(java.lang.String tagId) {
		this.tagId = tagId;
	}
}
