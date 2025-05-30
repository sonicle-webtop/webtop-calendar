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
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.String',
		'Sonicle.webtop.core.ux.data.CustomFieldValueModel',
		'Sonicle.webtop.calendar.model.EventAttendee',
		'Sonicle.webtop.calendar.model.EventAttachment',
		'Sonicle.data.writer.Json'
	],
	uses: [
		'Sonicle.form.field.rr.Recurrence'
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
		WTF.field('eventId', 'string', true),
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
		WTF.field('masterDataId', 'string', true),
		WTF.field('statMasterDataId', 'string', true),
		WTF.field('causalId', 'int', true),
		WTF.field('rrule', 'string', true, {
			isEqual: function(value1, value2) {
				return Sonicle.form.field.rr.Recurrence.isRRuleEqual(value1, value2);
			}
		}),
		WTF.field('rstart', 'date', true, {dateFormat: 'Y-m-d'}),
		WTF.calcField('rruleString', 'string', ['rrule', 'rstart'], function(v, rec, rrule, rstart) {
			var date = !Ext.isEmpty(rrule) ? Sonicle.Date.idate(rstart, true) : null;
			return Sonicle.form.field.rr.Recurrence.joinRRuleString(rrule, date);
		}),
		WTF.field('tags', 'string', true),
		WTF.calcField('meetingUrl', 'string', ['location', 'description'], function(v, rec, loc, desc) {
			var reURL = Sonicle.String.reSimpleURLs,
				urls = [], groups, i;
			Ext.iterate([loc, desc], function(s) {
				if (!Ext.isEmpty(s)) {
					groups = s.match(reURL);
					if (groups) Ext.Array.push(urls, groups);
				}
			});
			for (i=0; i<urls.length; i++) {
				if (WT.isMeetingUrl(urls[i])) return urls[i];
			}
			return null;
		}),
		// Read-only fields
		WTF.roField('_profileId', 'string'),
		WTF.roField('_recurringInfo', 'string', {defaultValue: 'none'}),
		WTF.roField('_cfdefs', 'string')
	],
	hasMany: [
		WTF.hasMany('attendees', 'Sonicle.webtop.calendar.model.EventAttendee'),
		WTF.hasMany('attachments', 'Sonicle.webtop.calendar.model.EventAttachment'),
		WTF.hasMany('cvalues', 'Sonicle.webtop.core.ux.data.CustomFieldValueModel')
	],
	
	wasRecurring: function() {
		return !this.phantom && this.get('_recurringInfo') === 'recurring';
	},
	
	wasBroken: function() {
		return !this.phantom && this.get('_recurringInfo') === 'broken';
	},
	
	isRecurring: function() {
		return !this.isFieldEmpty('rrule');
	},
	
	isNotifyable: function() {
		var ret = false;
		this.attendees().each(function(rec) {
			if (rec.get('notify') === true) ret = true;
			return;
		});
		return ret;
	},
	
	hasAttendees: function() {
		var sto = this.attendees();
		return !sto ? false : (sto.getCount() > 0);
	},
	
	setStart: function(date) {
		var me = this,
			dt = Ext.isDate(date) ? date : new Date(),
			dur = Sonicle.Date.diff(me.get('startDate'), me.get('endDate'), Ext.Date.MINUTE, true);
		
		me.set('startDate', dt);
		me.set('endDate', Ext.Date.add(dt, Ext.Date.MINUTE, dur, true));
		if (me.phantom) me.set('rstart', Ext.Date.clone(dt));
	},
	
	setEnd: function(date) {
		var me = this,
			dt = Ext.isDate(date) ? date : new Date(),
			dur = Sonicle.Date.diff(me.get('startDate'), me.get('endDate'), Ext.Date.MINUTE, true),
			sta = me.get('startDate');
		
		me.set('endDate', dt);
		if (!Ext.isDate(sta)) return;
		if (dt < sta) me.set('startDate', Ext.Date.add(dt, Ext.Date.MINUTE, -dur, true));
	},
	
	setStartDate: function(date) {
		var me = this,
			dt = Ext.isDate(date) ? date : new Date(),
			dur = Sonicle.Date.diff(me.get('startDate'), me.get('endDate'), Ext.Date.MINUTE, true),
			v;
		
		v = me.setDatePart('startDate', dt);
		me.set('endDate', Ext.Date.add(v, Ext.Date.MINUTE, dur, true));
		if (me.phantom) me.set('rstart', Ext.Date.clone(v));
	},
	
	setRecurStart: function(date, options) {
		var me = this, sta;
		me.setDatePart('rstart', date, null, null, options);
		if (me.phantom && Ext.isDate(date)) {
			sta = me.get('startDate');
			if (Ext.isDate(sta)) me.setStartDate(Ext.Date.clone(Sonicle.Date.min(date, sta)));
		}
	},
	
	setStartTime: function(date) {
		var me = this,
			dt = Ext.isDate(date) ? date : new Date(),
			dur = Sonicle.Date.diff(me.get('startDate'), me.get('endDate'), Ext.Date.MINUTE, true),
			v;
		
		v = me.setTimePart('startDate', dt);
		me.set('endDate', Ext.Date.add(v, Ext.Date.MINUTE, dur, true));
	},
	
	setEndDate: function(date) {
		var me = this,
			dt = Ext.isDate(date) ? date : new Date(),
			sta = me.get('startDate'),
			dur = Sonicle.Date.diff(sta, me.get('endDate'), Ext.Date.MINUTE, true),
			v;
		
		v = me.setDatePart('endDate', dt);
		if (!Ext.isDate(sta)) return;
		if (v < sta) me.set('startDate', Ext.Date.add(v, Ext.Date.MINUTE, -dur, true));
	},
	
	setEndTime: function(date) {
		var me = this,
			dt = Ext.isDate(date) ? date : new Date(),
			sta = me.get('startDate'),
			v;
		
		v = me.setTimePart('endDate', dt);
		if (!Ext.isDate(sta)) return;
		if (v < sta) me.set('startDate', v);
	}
});
