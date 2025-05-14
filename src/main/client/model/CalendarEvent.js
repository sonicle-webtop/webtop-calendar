/* 
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.calendar.model.CalendarEvent', {
	extend: 'Ext.data.Model',
	mixins: [
		'Sonicle.fullcalendar.api.EventDataMixin'
	],
	
	allDayField: 'isAllDay',
	startField: 'startDate',
	endField: 'endDate',
	
	identifier: 'sequential',
	idProperty: 'id',
	fields: [
		WTF.field('id', 'string', false),
		WTF.field('eventId', 'string', false),
		WTF.field('calendarId', 'int', false),
		WTF.field('calendarName', 'string', false),
		WTF.field('description', 'string', false),
		WTF.field('startDate', 'date', false, {dateFormat: 'Y-m-d H:i:s'}),
		WTF.field('endDate', 'date', false, {dateFormat: 'Y-m-d H:i:s'}),
		WTF.field('timezone', 'string', false),
		WTF.field('isAllDay', 'boolean', false, {defaultValue: false}),
		WTF.field('title', 'string', false),
		WTF.field('color', 'string', false),
		WTF.field('location', 'string', true),
		WTF.field('meeting', 'string', true),
		WTF.field('isPrivate', 'boolean', false, {defaultValue: false}),
		WTF.field('reminder', 'int', true),
		WTF.field('isReadOnly', 'boolean', false, {defaultValue: false}),
		WTF.field('hideData', 'boolean', false, {defaultValue: false}),
		WTF.field('hasTz', 'boolean', false, {defaultValue: false}),
		WTF.field('hasAtts', 'boolean', false, {defaultValue: false}),
		WTF.field('isNtf', 'boolean', false, {defaultValue: false}),
		WTF.field('isRecurring', 'boolean', false, {defaultValue: false}),
		WTF.field('isBroken', 'boolean', false, {defaultValue: false}),
		WTF.field('hasCmts', 'boolean', false, {defaultValue: false}),
		WTF.field('tags', 'string', true),
		WTF.field('folderName', 'string', false),
		WTF.field('org', 'string', false),
		WTF.field('_owner', 'string', false),
		WTF.field('_rights', 'string', false),
		WTF.field('_profileId', 'string', false)
	],
	
	privates: {
		getColor: function() {
			var parent = this.mixins.sofullcalendareventdata.getColor.call(this);
			return parent;
			//return WTA.sdk.mixin.FolderNodeInterface.tailwindColor(parent);
		},
		
		fcIsEditable: function() {
			return !this.get('isReadOnly') && !this.get('isRecurring');
		},

		fcPrepareEventExtendedProps: function() {
			var me = this,
				SoO = Sonicle.Object,
				parent = me.mixins.sofullcalendareventdata.fcPrepareEventExtendedProps.call(me);
		
			return Ext.apply(parent || {}, {
				calendarId: me.get('calendarId'),
				calendarName: me.get('calendarName'),
				calendarOwner: me.get('_owner'),
				timezone: me.get('timezone'),
				location: me.get('location'),
				description: me.get('description'),
				meeting: me.get('meeting'),
				tags: me.get('tags'),
				organizer: me.get('org'),
				isRecurring: me.get('isRecurring'),
				isBroken: me.get('isBroken'),
				isPrivate: me.get('isPrivate'),
				hasOtherTz: me.get('hasTz'),
				hasBody: me.get('hasCmts'),
				hasAttendees: me.get('hasAtts'),
				hasMeeting: !Ext.isEmpty(me.get('meeting')),
				hasReminder: SoO.numberValue(me.get('reminder'), -1) >= 0
			});
		}
	}
});
