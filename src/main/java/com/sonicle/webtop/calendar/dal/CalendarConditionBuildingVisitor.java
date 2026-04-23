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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.rsql.parser.Operator;
import static com.sonicle.webtop.calendar.jooq.Tables.CALENDARS;
import com.sonicle.webtop.calendar.model.CalendarBase;
import com.sonicle.webtop.calendar.model.CalendarQuery;
import com.sonicle.webtop.core.app.sdk.JOOQConditionBuildingVisitor;
import java.util.ArrayList;
import java.util.Collection;
import org.jooq.Condition;

/**
 *
 * @author malbinola
 */
public class CalendarConditionBuildingVisitor extends JOOQConditionBuildingVisitor {
	
	@Override
	protected Condition buildCondition(String fieldName, Operator operator, Collection<?> values) {
		if (CalendarQuery.ID.equals(fieldName)) {
			return defaultCondition(CALENDARS.CALENDAR_ID, operator, values);
			
		} else if (CalendarQuery.CREATED_AT.equals(fieldName)) {
			return defaultCondition(CALENDARS.CREATION_TIMESTAMP, operator, values);
			
		} else if (CalendarQuery.UPDATED_AT.equals(fieldName)) {
			return defaultCondition(CALENDARS.REVISION_TIMESTAMP, operator, values);
			
		} else if (CalendarQuery.USER_ID.equals(fieldName)) {
			return defaultCondition(CALENDARS.USER_ID, operator, values);
			
		} else if (CalendarQuery.BUILT_IN.equals(fieldName)) {
			return defaultCondition(CALENDARS.BUILT_IN, operator, values);
			
		} else if (CalendarQuery.PROVIDER.equals(fieldName)) {
			return defaultCondition(CALENDARS.PROVIDER, operator, parseProviderValues(values), DefaultConditionOption.stringICaseComparison());
		
		} else if (CalendarQuery.NAME.equals(fieldName)) {
			return defaultCondition(CALENDARS.NAME, operator, values);
		
		} else if (CalendarQuery.DESCRIPTION.equals(fieldName)) {
			return defaultCondition(CALENDARS.DESCRIPTION, operator, values, DefaultConditionOption.stringICaseComparison());
		
		} else if (CalendarQuery.COLOR.equals(fieldName)) {
			return defaultCondition(CALENDARS.COLOR, operator, values, DefaultConditionOption.stringICaseComparison());
		
		} else if (CalendarQuery.EAS_SYNC.equals(fieldName)) {
			return defaultCondition(CALENDARS.SYNC, operator, parseSyncValues(values), DefaultConditionOption.stringICaseComparison());
		}
		
		throw new UnsupportedOperationException("Field not supported: " + fieldName);
	}
	
	private Collection<String> parseProviderValues(Collection<?> values) {
		ArrayList<String> newValues = new ArrayList<>(values.size());
		for (Object value : values) {
			final String svalue = String.valueOf(value);
			final CalendarBase.Provider sync = EnumUtils.forString(svalue, CalendarBase.Provider.class, true);
			if (sync != null) newValues.add(svalue);
		}
		return newValues;
	}
	
	private Collection<String> parseSyncValues(Collection<?> values) {
		ArrayList<String> newValues = new ArrayList<>(values.size());
		for (Object value : values) {
			final String svalue = String.valueOf(value);
			final CalendarBase.Sync sync = EnumUtils.forString(svalue, CalendarBase.Sync.class, true);
			if (sync != null) newValues.add(svalue);
		}
		return newValues;
	}
}
