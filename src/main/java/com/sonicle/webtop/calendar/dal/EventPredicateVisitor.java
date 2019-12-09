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

import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode;
import com.github.rutledgepaulv.qbuilders.operators.ComparisonOperator;
import static com.sonicle.webtop.calendar.jooq.Tables.EVENTS;
import static com.sonicle.webtop.calendar.jooq.Tables.RECURRENCES;
import com.sonicle.webtop.core.app.sdk.BaseJOOQVisitor;
import java.util.Collection;
import org.joda.time.DateTime;
import org.jooq.Condition;

/**
 *
 * @author malbinola
 */
public class EventPredicateVisitor extends BaseJOOQVisitor {
	protected final Target target;
	protected DateTime fromRange = null;
	protected DateTime toRange = null;
	
	public EventPredicateVisitor(Target target) {
		super();
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
		switch(fieldName) {
			case "title":
				return defaultCondition(EVENTS.TITLE, operator, values);
				
			case "location":
				return defaultCondition(EVENTS.LOCATION, operator, values);
				
			case "description":
				return defaultCondition(EVENTS.DESCRIPTION, operator, values);
				
			case "after":
				fromRange = (DateTime)single(values);
				if (Target.RECURRING.equals(target)) {
					return RECURRENCES.START_DATE.greaterOrEqual(fromRange)
							.or(RECURRENCES.UNTIL_DATE.greaterOrEqual(fromRange));
				} else {
					return EVENTS.START_DATE.greaterOrEqual(fromRange)
							.or(EVENTS.END_DATE.greaterOrEqual(fromRange));
				}
			
			case "before":
				toRange = (DateTime)single(values);
				if (toRange != null) toRange = toRange.plusDays(1);
				if (Target.RECURRING.equals(target)) {
					return RECURRENCES.START_DATE.lessThan(toRange)
							.or(RECURRENCES.UNTIL_DATE.lessThan(toRange));
				} else {
					return EVENTS.START_DATE.lessOrEqual(toRange)
							.or(EVENTS.END_DATE.lessOrEqual(toRange));
				}
				
			case "busy":
				return defaultCondition(EVENTS.BUSY, operator, values);
				
			case "private":
				return defaultCondition(EVENTS.IS_PRIVATE, operator, values);
				
			case "any":
				return EVENTS.TITLE.likeIgnoreCase(valueToSmartLikePattern(singleAsString(values)))
						.or(EVENTS.LOCATION.likeIgnoreCase(valueToSmartLikePattern(singleAsString(values))))
						.or(EVENTS.DESCRIPTION.likeIgnoreCase(valueToSmartLikePattern(singleAsString(values))))
						.or(EVENTS.ORGANIZER.likeIgnoreCase(valueToSmartLikePattern(singleAsString(values))));
				
			default:
				throw new UnsupportedOperationException("Field not supported: " + fieldName);
		}
	}
	
	public static enum Target {
		NORMAL, RECURRING;
	}
}
