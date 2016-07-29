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
	extend: 'WT.ux.data.BaseModel',
	requires: [
		'Sonicle.webtop.calendar.model.EventAttendee',
		'Sonicle.data.writer.Json'
	],
	proxy: WTF.apiProxy('com.sonicle.webtop.calendar', 'ManageEvents', 'data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
			//writeChanges: true
		}
	}),
	
	identifier: 'negativestring',
	idProperty: 'id',
	fields: [
		WTF.field('id', 'string', false),
		WTF.field('eventId', 'int', true),
		WTF.field('calendarId', 'int', false),
		WTF.field('recurrenceId', 'int', true),
		WTF.field('startDate', 'date', false, {dateFormat: 'Y-m-d H:i:s'}),
		WTF.field('endDate', 'date', false, {dateFormat: 'Y-m-d H:i:s'}),
		WTF.field('timezone', 'string', false),
		WTF.field('allDay', 'boolean', false, {defaultValue: false}),
		WTF.field('title', 'string', false),
		WTF.field('description', 'string', true),
		WTF.field('location', 'string', true),
		WTF.field('isPrivate', 'boolean', false, {defaultValue: false}),
		WTF.field('busy', 'boolean', false, {defaultValue: false}),
		WTF.field('reminder', 'int', true),
		WTF.field('activityId', 'int', true),
		WTF.field('customerId', 'string', true),
		WTF.field('statisticId', 'string', true),
		WTF.field('causalId', 'int', true),
		WTF.field('rrEndsMode', 'string', false, {defaultValue: 'never'}),
		WTF.field('rrRepeatTimes', 'int', true),//false, {defaultValue: 1}),
		WTF.field('rrUntilDate', 'date', true, {dateFormat: 'Y-m-d H:i:s'}),//false, {dateFormat: 'Y-m-d H:i:s', defaultValue: new Date()}),
		WTF.field('rrType', 'string', false, {defaultValue: '_'}),
		WTF.field('rrDailyType', 'string', false, {defaultValue: '1'}),
		WTF.field('rrDailyFreq', 'int', true),//false, {defaultValue: 1}),
		WTF.field('rrWeeklyFreq', 'int', true),//false, {defaultValue: 1}),
		WTF.field('rrWeeklyDay1', 'boolean', true),//false, {defaultValue: false}),
		WTF.field('rrWeeklyDay2', 'boolean', true),//false, {defaultValue: false}),
		WTF.field('rrWeeklyDay3', 'boolean', true),//false, {defaultValue: false}),
		WTF.field('rrWeeklyDay4', 'boolean', true),//false, {defaultValue: false}),
		WTF.field('rrWeeklyDay5', 'boolean', true),//false, {defaultValue: false}),
		WTF.field('rrWeeklyDay6', 'boolean', true),//false, {defaultValue: false}),
		WTF.field('rrWeeklyDay7', 'boolean', true),//false, {defaultValue: false}),
		WTF.field('rrMonthlyFreq', 'int', true),//false, {defaultValue: 1}),
		WTF.field('rrMonthlyDay', 'int', true),//false, {defaultValue: 1}),
		WTF.field('rrYearlyFreq', 'int', true),//false, {defaultValue: 1}),
		WTF.field('rrYearlyDay', 'int', true),//false, {defaultValue: 1}),
		// Read-only fields
		WTF.roField('_profileId', 'string'),
		WTF.roField('_recurringInfo', 'string', {defaultValue: 'single'}),
		WTF.calcField('_isSingle', 'boolean', '_recurringInfo', function(v, rec) {
			return (rec.get('_recurringInfo') === 'single');
		}),
		WTF.calcField('_isBroken', 'boolean', '_recurringInfo', function(v, rec) {
			return (rec.get('_recurringInfo') === 'broken');
		}),
		WTF.calcField('_isRecurring', 'boolean', '_recurringInfo', function(v, rec) {
			return (rec.get('_recurringInfo') === 'recurring');
		})
	],
	hasMany: [
		WTF.hasMany('attendees', 'Sonicle.webtop.calendar.model.EventAttendee')
	],
	
	setStartDate: function(date) {
		var me = this,
				end = me.get('endDate'), dt;
		dt = me.setDatePart('startDate', date);
		if(!Ext.isDate(dt) || !Ext.isDate(end)) return;
		if(dt > end) me.set('endDate', dt);
	},
	
	setStartTime: function(date) {
		var me = this,
				end = me.get('endDate'), dt;
		dt = me.setTimePart('startDate', date);
		if(!Ext.isDate(dt) || !Ext.isDate(end)) return;
		if(dt > end) me.set('endDate', dt);
	},
	
	setEndDate: function(date) {
		var me = this,
				sta = me.get('startDate'), dt;
		dt = me.setDatePart('endDate', date);
		if(!Ext.isDate(dt) || !Ext.isDate(sta)) return;
		if(dt < sta) me.set('startDate', dt);
	},
	
	setEndTime: function(date) {
		var me = this,
				sta = me.get('startDate'), dt;
		dt = me.setTimePart('endDate', date);
		if(!Ext.isDate(dt) || !Ext.isDate(sta)) return;
		if(dt < sta) me.set('startDate', dt);
	},
	
	setDatePart: function(field, date) {
		var me = this,
				v = me.get(field), dt;
		if(!Ext.isDate(date) || !Ext.isDate(v)) return;
		dt = Sonicle.Date.copyDate(date, v);
		me.set(field, dt);
		return dt;
	},
	
	setTimePart: function(field, date) {
		var me = this,
				v = me.get(field), dt;
		if(!Ext.isDate(date) || !Ext.isDate(v)) return;
		dt = Sonicle.Date.copyTime(date, v);
		me.set(field, dt);
		return dt;
	}
});
