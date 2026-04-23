/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.dal;

import com.sonicle.commons.rsql.parser.Operator;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_CUSTOM_VALUES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_TAGS;
import com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues;
import com.sonicle.webtop.calendar.jooq.tables.EventsTags;
import com.sonicle.webtop.calendar.model.EventQuery;
import com.sonicle.webtop.core.app.sdk.JOOQConditionBuildingVisitorWithCFields;
import com.sonicle.webtop.core.model.CustomFieldBase;
import java.util.Collection;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.TableLike;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.selectOne;

/**
 *
 * @author malbinola
 */
public class EventConditionBuildingVisitor extends JOOQConditionBuildingVisitorWithCFields {
	private final EventsCustomValues PV_EVENTS_CUSTOM_VALUES = EVENTS_CUSTOM_VALUES.as("pvis_cv");
	private final EventsTags PV_EVENTS_TAGS = EVENTS_TAGS.as("pvis_ct");
	
	public EventConditionBuildingVisitor() {
		super(false);
	}
	
	@Override
	protected Condition buildCondition(String fieldName, Operator operator, Collection<?> values) {
		if (EventQuery.ID.equals(fieldName)) {
			return defaultCondition(EVENTS.EVENT_ID, operator, values);
			
		} else if (EventQuery.CREATED_AT.equals(fieldName)) {
			return defaultCondition(EVENTS.CREATION_TIMESTAMP, operator, values);
			
		} else if (EventQuery.UPDATED_AT.equals(fieldName)) {
			return defaultCondition(EVENTS.REVISION_TIMESTAMP, operator, values);
			
		} else if (EventQuery.ROW_STATUS.equals(fieldName)) {
			return defaultCondition(EVENTS.ROW_STATUS, operator, values);
			
		} else if (EventQuery.STATUS.equals(fieldName)) {
			return defaultCondition(EVENTS.STATUS, operator, values);
			
		} else if (EventQuery.ORGANIZER.equals(fieldName)) {
			return defaultCondition(EVENTS.ORGANIZER, operator, values);
			
		} else if (EventQuery.ORGANIZER_ID.equals(fieldName)) {
			return defaultCondition(EVENTS.ORGANIZER_ID, operator, values);
			
		} else if (EventQuery.TITLE.equals(fieldName)) {
			return defaultCondition(EVENTS.TITLE, operator, values);
			
		} else if (EventQuery.LOCATION.equals(fieldName)) {
			return defaultCondition(EVENTS.LOCATION, operator, values);
			
		} else if (EventQuery.DESCRIPTION.equals(fieldName)) {
			return defaultCondition(EVENTS.DESCRIPTION, operator, values);
			
		} else if (EventQuery.TIMEZONE.equals(fieldName)) {
			return defaultCondition(EVENTS.TIMEZONE, operator, values);
			
		} else if (EventQuery.ALL_DAY.equals(fieldName)) {
			return defaultCondition(EVENTS.ALL_DAY, operator, values);
			
		} else if (EventQuery.START.equals(fieldName)) {
			return defaultCondition(EVENTS.START, operator, values);
			
		} else if (EventQuery.END.equals(fieldName)) {
			return defaultCondition(EVENTS.END, operator, values);
			
		} else if (EventQuery.VISIBILITY.equals(fieldName)) {
			return defaultCondition(EVENTS.VISIBILITY, operator, values);
			
		} else if (EventQuery.TRANSPARENCY.equals(fieldName)) {
			return defaultCondition(EVENTS.TRANSPARENCY, operator, values);
			
		} else if (EventQuery.REMINDER.equals(fieldName)) {
			return defaultCondition(EVENTS.REMINDER, operator, values);
			
		}/* else if (EventQueryNew.COMPANY.equals(fieldName)) {
			return defaultCondition(EVENTS.COMPANY, operator, values);
			
		} else if (EventQueryNew.COMPANY_ID.equals(fieldName)) {
			return defaultCondition(EVENTS.COMPANY_ID, operator, values);
			
		}*/ else if (EventQuery.TAG_ID.equals(fieldName)) {
			return exists(
				selectOne()
				.from(EVENTS_TAGS)
				.where(
					EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
					.and(EVENTS_TAGS.TAG_ID.equal(singleValueAsString(values)))
				)
			);
			
		} else if (isCFieldPlainNotation(fieldName) || isCFieldWithRawValueTypeNotation(fieldName)) {
			return evalFieldNameAndGenerateCFieldCondition(fieldName, operator, values);
		}
		
		return null;
	}
	
	@Override
	protected Field<?> getFieldEntityIdOfEntityTable() {
		return EVENTS.EVENT_ID;
	}

	@Override
	protected TableLike<?> getTableTags() {
		return PV_EVENTS_TAGS;
	}

	@Override
	protected Field<String> getFieldTagIdOfTableTags() {
		return PV_EVENTS_TAGS.TAG_ID;
	}

	@Override
	protected Condition getConditionTagsForCurrentEntity() {
		return PV_EVENTS_TAGS.EVENT_ID.eq(EVENTS.EVENT_ID);
	}

	@Override
	protected TableLike<?> getTableCustomValues() {
		return PV_EVENTS_CUSTOM_VALUES;
	}
	
	@Override
	protected Condition getConditionCustomValuesForCurrentEntityAndField(String fieldId) {
		return PV_EVENTS_CUSTOM_VALUES.EVENT_ID.eq(EVENTS.EVENT_ID)
			.and(PV_EVENTS_CUSTOM_VALUES.CUSTOM_FIELD_ID.eq(fieldId));
	}

	@Override
	protected Field<?> getTableCustomValuesTypeTableField(CustomFieldBase.RawValueType cvalueType) {
		if (CustomFieldBase.RawValueType.CVSTRING.equals(cvalueType)) {
			return PV_EVENTS_CUSTOM_VALUES.STRING_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVSTRINGARRAY.equals(cvalueType)) {
			return PV_EVENTS_CUSTOM_VALUES.STRING_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVNUMBER.equals(cvalueType)) {
			return PV_EVENTS_CUSTOM_VALUES.NUMBER_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVBOOL.equals(cvalueType)) {
			return PV_EVENTS_CUSTOM_VALUES.BOOLEAN_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVDATE.equals(cvalueType)) {
			return PV_EVENTS_CUSTOM_VALUES.DATE_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVTEXT.equals(cvalueType)) {
			return PV_EVENTS_CUSTOM_VALUES.TEXT_VALUE;
			
		} else {
			return null;
		}
	}
}
