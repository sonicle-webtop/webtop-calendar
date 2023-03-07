/* 
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.calendar.ux.PlanningModel', {
	extend: 'WTA.ux.data.BaseModel',
	
	idProperty: 'recipient',
	fields: [
		WTF.roField('attendee', 'string'),
		WTF.roField('attendeeAddress', 'string'),
		WTF.roField('organizer', 'boolean', {defaultValue: false}),
		WTF.calcField('displayName', 'string', ['attendee', 'attendeeAddress'], function(v, rec, att, addr) {
			return Sonicle.String.coalesce(att, addr);
		}),
		WTF.roField('spanAvailability', 'string'),
		WTF.roField('workdayStart', 'string'),
		WTF.roField('workdayEnd', 'string')
	],
	
	isOrganizer: function() {
		return this.get('organizer') === true;
	},
	
	getSpanAvailability: function(spanIndex) {
		return parseInt(Sonicle.String.split(this.get('spanAvailability'), '|')[spanIndex]);
	},
	
	isSpanInWorkdayHours: function(spanDate) {
		var start = this.get('workdayStart'),
			end = this.get('workdayEnd');
		
		if (!Ext.isEmpty(start) && !Ext.isEmpty(end)) {
			var day = Ext.Date.format(spanDate, 'Y-m-d');
			if ((spanDate >= Ext.Date.parse(day + ' ' + start, 'Y-m-d H:i')) && (spanDate < Ext.Date.parse(day + ' ' + end, 'Y-m-d H:i'))) {
				return true;
			}
		}
		return false;
	}
});
