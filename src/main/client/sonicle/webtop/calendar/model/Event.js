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
Ext.define('Sonicle.webtop.calendar.model.Event', {
	extend: 'WT.model.Base',
	proxy: WT.Util.apiProxy('com.sonicle.webtop.calendar', 'ManageEvents'),
	
	idProperty: 'eventId',
	fields: [
		WT.Util.field('eventId', 'int', false),
		WT.Util.field('calendarId', 'int', false),
		WT.Util.field('startDate', 'date', false, {dateFormat: 'Y-m-d H:i:s'}),
		WT.Util.field('endDate', 'date', false, {dateFormat: 'Y-m-d H:i:s'}),
		WT.Util.field('timezone', 'string', false),
		WT.Util.field('title', 'string', false),
		WT.Util.field('location', 'int', true),
		WT.Util.field('allDay', 'boolean', false, {defaultValue: false}),
		WT.Util.field('description', 'string', true),
		WT.Util.field('reminder', 'int', false, {
			defaultValue: -1,
			convert: function(v) {
				return (v) ? v : -1;
			},
			serialize: function(v) {
				return (v === -1) ? null : v;
			}
		}),
		WT.Util.field('isPrivate', 'boolean', false, {defaultValue: false}),
		WT.Util.field('busy', 'boolean', false, {defaultValue: false})
	],
	
	statics: {
		setDate: function(model, field, date) {
			var val = model.get(field);
			if(!Ext.isDate(date) || !Ext.isDate(val)) return;
			model.set(field, Sonicle.Date.copyDate(date, val));
		},

		setTime: function(model, field, date) {
			var val = model.get(field);
			if(!Ext.isDate(date) || !Ext.isDate(val)) return;
			model.set(field, Sonicle.Date.copyTime(date, val));
		}
	}
});
