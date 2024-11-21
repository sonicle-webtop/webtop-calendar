/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.dal;

import com.sonicle.webtop.calendar.bol.OEventAttachment;
import com.sonicle.webtop.calendar.bol.OEventAttachmentData;
import com.sonicle.webtop.calendar.bol.VEventAttachmentWithBytes;
import static com.sonicle.webtop.calendar.jooq.tables.EventsAttachments.EVENTS_ATTACHMENTS;
import static com.sonicle.webtop.calendar.jooq.tables.EventsAttachmentsData.EVENTS_ATTACHMENTS_DATA;
import com.sonicle.webtop.calendar.jooq.tables.records.EventsAttachmentsRecord;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import java.sql.Connection;
import java.util.List;
import org.joda.time.DateTime;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class EventAttachmentDAO extends BaseDAO {
	private final static EventAttachmentDAO INSTANCE = new EventAttachmentDAO();
	public static EventAttachmentDAO getInstance() {
		return INSTANCE;
	}
	
	public List<OEventAttachment> selectByEvent(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS_ATTACHMENTS)
			.where(
				EVENTS_ATTACHMENTS.EVENT_ID.equal(eventId)
			)
			.orderBy(
				EVENTS_ATTACHMENTS.FILENAME.asc()
			)
			.fetchInto(OEventAttachment.class);
	}
	
	public List<VEventAttachmentWithBytes> selectByEventWithBytes(Connection con, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS_ATTACHMENTS.EVENT_ATTACHMENT_ID,
				EVENTS_ATTACHMENTS.EVENT_ID,
				EVENTS_ATTACHMENTS.FILENAME,
				EVENTS_ATTACHMENTS.MEDIA_TYPE,
				EVENTS_ATTACHMENTS.REVISION_SEQUENCE,
				EVENTS_ATTACHMENTS.REVISION_TIMESTAMP,
				EVENTS_ATTACHMENTS.SIZE,
				EVENTS_ATTACHMENTS_DATA.BYTES

			)
			.from(EVENTS_ATTACHMENTS)
			.join(EVENTS_ATTACHMENTS_DATA).on(EVENTS_ATTACHMENTS.EVENT_ATTACHMENT_ID.equal(EVENTS_ATTACHMENTS_DATA.EVENT_ATTACHMENT_ID))
			.where(
				EVENTS_ATTACHMENTS.EVENT_ID.equal(eventId)
			)
			.orderBy(
				EVENTS_ATTACHMENTS.FILENAME.asc()
			)
			.fetchInto(VEventAttachmentWithBytes.class);
	}
	
	public OEventAttachment selectByIdEvent(Connection con, String attachmentId, String eventId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(EVENTS_ATTACHMENTS)
			.where(
				EVENTS_ATTACHMENTS.EVENT_ATTACHMENT_ID.equal(attachmentId)
				.and(EVENTS_ATTACHMENTS.EVENT_ID.equal(eventId))
			)
			.fetchOneInto(OEventAttachment.class);
	}
	
	public int insert(Connection con, OEventAttachment item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionTimestamp(revisionTimestamp);
		item.setRevisionSequence((short)0);
		EventsAttachmentsRecord record = dsl.newRecord(EVENTS_ATTACHMENTS, item);
		return dsl
			.insertInto(EVENTS_ATTACHMENTS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OEventAttachment item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionTimestamp(revisionTimestamp);
		return dsl
			.update(EVENTS_ATTACHMENTS)
			.set(EVENTS_ATTACHMENTS.FILENAME, item.getFilename())
			.set(EVENTS_ATTACHMENTS.SIZE, item.getSize())
			.set(EVENTS_ATTACHMENTS.MEDIA_TYPE, item.getMediaType())
			.where(
				EVENTS_ATTACHMENTS.EVENT_ATTACHMENT_ID.equal(item.getEventAttachmentId())
			)
			.execute();
	}
	
	public int delete(Connection con, String attachmentId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_ATTACHMENTS)
			.where(EVENTS_ATTACHMENTS.EVENT_ATTACHMENT_ID.equal(attachmentId))
			.execute();
	}
	
	public OEventAttachmentData selectBytes(Connection con, String attachmentId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				EVENTS_ATTACHMENTS_DATA.BYTES
			)
			.from(EVENTS_ATTACHMENTS_DATA)
			.where(EVENTS_ATTACHMENTS_DATA.EVENT_ATTACHMENT_ID.equal(attachmentId))
			.fetchOneInto(OEventAttachmentData.class);
	}
	
	public int insertBytes(Connection con, String attachmentId, byte[] bytes) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(EVENTS_ATTACHMENTS_DATA)
			.set(EVENTS_ATTACHMENTS_DATA.EVENT_ATTACHMENT_ID, attachmentId)
			.set(EVENTS_ATTACHMENTS_DATA.BYTES, bytes)
			.execute();
	}
	
	public int deleteBytes(Connection con, String attachmentId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(EVENTS_ATTACHMENTS_DATA)
			.where(EVENTS_ATTACHMENTS_DATA.EVENT_ATTACHMENT_ID.equal(attachmentId))
			.execute();
	}
}
