/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.calendar.dal;

import com.sonicle.commons.qbuilders.nodes.ComparisonNode;
import com.sonicle.commons.qbuilders.operators.ComparisonOperator;
import com.sonicle.commons.web.json.CId;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_CUSTOM_VALUES;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS_TAGS;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES;
import com.sonicle.webtop.calendar.jooq.tables.EventsCustomValues;
import com.sonicle.webtop.calendar.jooq.tables.EventsTags;
import com.sonicle.webtop.core.app.sdk.JOOQPredicateVisitorWithCValues;
import com.sonicle.webtop.core.app.sdk.QueryBuilderWithCValues;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.TableLike;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author malbinola
 */
public class EventPredicateVisitor extends JOOQPredicateVisitorWithCValues {
	private final EventsCustomValues PV_EVENTS_CUSTOM_VALUES = EVENTS_CUSTOM_VALUES.as("pvis_cv");
	private final EventsTags PV_EVENTS_TAGS = EVENTS_TAGS.as("pvis_ct");
	protected final Target target;
	protected DateTime fromRange = null;
	protected DateTime toRange = null;
	
	public EventPredicateVisitor(Target target) {
		super(false);
		this.target = target;
	}

	public boolean hasFromRange() {
		return fromRange != null;
	}
	
	public DateTime getFromRange() {
		return fromRange;
	}

	public boolean hasToRange() {
		return toRange != null;
	}
	
	public DateTime getToRange() {
		return toRange;
	}

	@Override
	protected Condition toCondition(String fieldName, ComparisonOperator operator, Collection<?> values, ComparisonNode node) {
		if ("title".equals(fieldName)) {
			return defaultCondition(EVENTS.TITLE, operator, values);
			
		} else if ("location".equals(fieldName)) {
			return defaultCondition(EVENTS.LOCATION, operator, values);
			
		} else if ("description".equals(fieldName)) {
			return defaultCondition(EVENTS.DESCRIPTION, operator, values);
			
		} else if ("after".equals(fieldName)) {
			fromRange = (DateTime)single(values);
			if (Target.RECURRING.equals(target)) {
				return RECURRENCES.START_DATE.greaterOrEqual(fromRange)
						.or(RECURRENCES.UNTIL_DATE.greaterOrEqual(fromRange));
			} else {
				return EVENTS.START_DATE.greaterOrEqual(fromRange)
						.or(EVENTS.END_DATE.greaterOrEqual(fromRange));
			}
			
		} else if ("before".equals(fieldName)) {
			toRange = (DateTime)single(values);
			if (toRange != null) toRange = toRange.plusDays(1);
			if (Target.RECURRING.equals(target)) {
				return RECURRENCES.START_DATE.lessThan(toRange)
						.or(RECURRENCES.UNTIL_DATE.lessThan(toRange));
			} else {
				return EVENTS.START_DATE.lessOrEqual(toRange)
						.or(EVENTS.END_DATE.lessOrEqual(toRange));
			}
			
		} else if ("busy".equals(fieldName)) {
			return defaultCondition(EVENTS.BUSY, operator, values);
			
		} else if ("private".equals(fieldName)) {
			return defaultCondition(EVENTS.IS_PRIVATE, operator, values);
			
		} else if ("tag".equals(fieldName)) {
			return exists(
				selectOne()
				.from(EVENTS_TAGS)
				.where(
					EVENTS_TAGS.EVENT_ID.equal(EVENTS.EVENT_ID)
					.and(EVENTS_TAGS.TAG_ID.equal(singleAsString(values)))
				)
			);
			
		} else if ("any".equals(fieldName)) {
			String singleAsString = valueToLikePattern(singleAsString(values));
			return EVENTS.TITLE.likeIgnoreCase(singleAsString)
				.or(EVENTS.LOCATION.likeIgnoreCase(singleAsString))
				.or(EVENTS.DESCRIPTION.likeIgnoreCase(singleAsString))
				.or(EVENTS.ORGANIZER.likeIgnoreCase(singleAsString));
			
		} else if (StringUtils.startsWith(fieldName, "CV")) {
			CId fn = new CId(fieldName, 2);
			if (fn.isTokenEmpty(1)) throw new UnsupportedOperationException("Field name invalid: " + fieldName);
			return generateCValueCondition(fn, operator, values);
			
		} else {
			throw new UnsupportedOperationException("Field not supported: " + fieldName);
		}
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
	protected Condition getConditionCustomValuesForFieldValue(QueryBuilderWithCValues.Type cvalueType, ComparisonOperator operator, Collection<?> values) {
		if (QueryBuilderWithCValues.Type.CVSTRING.equals(cvalueType)) {
			return defaultCondition(PV_EVENTS_CUSTOM_VALUES.STRING_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVNUMBER.equals(cvalueType)) {
			return defaultCondition(PV_EVENTS_CUSTOM_VALUES.NUMBER_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVBOOL.equals(cvalueType)) {
			return defaultCondition(PV_EVENTS_CUSTOM_VALUES.BOOLEAN_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVDATE.equals(cvalueType)) {
			return defaultCondition(PV_EVENTS_CUSTOM_VALUES.DATE_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVTEXT.equals(cvalueType)) {
			return defaultCondition(PV_EVENTS_CUSTOM_VALUES.TEXT_VALUE, operator, values);
			
		} else {
			return null;
		}
	}
	
	public static enum Target {
		NORMAL, RECURRING;
	}
}
