/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
Ext.define('Sonicle.webtop.calendar.model.GridEvent', {
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.Date'
	],
	
	fields: [
		WTF.roField('id', 'string'),
		WTF.roField('eventId', 'int'),
		WTF.roField('startDate', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.roField('endDate', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.roField('timezone', 'string'),
		WTF.roField('title', 'string'),
		WTF.roField('location', 'string'),
		WTF.roField('meeting', 'string'),
		WTF.roField('color', 'string'),
		WTF.roField('tags', 'string'),
		WTF.roField('folderName', 'string'),
		WTF.roField('isAllDay', 'boolean'),
		WTF.roField('isRecurring', 'boolean'),
		WTF.roField('isBroken', 'boolean'),
		WTF.calcField('duration', 'int', ['startDate', 'endDate', 'isAllDay'], function(v, rec, start, end, ad) {
			var SoD = Sonicle.Date, diff;
			if (ad === true) {
				if (end.getHours() === 23 && end.getMinutes() === 59) {
					end = SoD.setTime(SoD.add(end, {days: 1}, true), 0, 0, 0);
				}
				diff = SoD.diffDays(end, start) * 86400;
			} else {
				diff = SoD.diff(start, end, Ext.Date.SECOND, true);
			}
			return diff ? Math.abs(diff) : 0;
		})
		//TODO: unire i campi isRecurring e isBroken nel campo recurringInfo... vedi JsEvent
	]
});
