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
Ext.define('Sonicle.webtop.calendar.view.Event', {
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.form.field.Palette',
		'Sonicle.form.RadioGroup',
		'Sonicle.form.field.IconComboBox',
		'WT.store.Timezone',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.Calendar',
		'Sonicle.webtop.calendar.store.Reminder'
	],
	
	title: '@event.tit',
	iconCls: 'wtcal-icon-event',
	model: 'Sonicle.webtop.calendar.model.Event',
	viewModel: {
		formulas: {
			fromDate: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('fromDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'fromDate', value);
				}
			},
			fromTime: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('fromDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'fromDate', value);
				}
			},
			toDate: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('toDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'toDate', value);
				}
			},
			toTime: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('toDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'toDate', value);
				}
			}
		}
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add(me.addRef('form', Ext.create({
			region: 'center',
			xtype: 'form',
			modelValidation: true,
			bodyPadding: 10,
			defaults: {
				labelWidth: 60
			},
			items: [{
				xtype: 'textfield',
				name: 'title',
				bind: '{record.title}',
				fieldLabel: me.mys.res('event.fld-title.lbl'),
				anchor: '100%'
			}, {
				xtype: 'textfield',
				name: 'location',
				bind: '{record.location}',
				fieldLabel: me.mys.res('event.fld-location.lbl'),
				anchor: '100%'
			}, {
				xtype: 'fieldset',
				collapsed: true
			}, {
				xtype: 'fieldcontainer',
				fieldLabel: me.mys.res('event.fld-fromDate.lbl'),
				layout: 'hbox',
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'datefield',
					name: 'fromDate',
					bind: '{fromDate}',
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					name: 'fromTime',
					bind: {
						value: '{fromTime}',
						disabled: '{allDay.checked}'
					},
					format: WT.getTimeFmt(),
					margin: '0 5 0 0',
					width: 80
				}, {
					xtype: 'button',
					iconCls: 'wtcal-icon-now',
					tooltip: me.mys.res('event.btn-now.tip'),
					handler: function() {
						var EM = Sonicle.webtop.calendar.model.Event;
						EM.setTime(me.getModel(), 'fromDate', new Date());
					},
					bind: {
						disabled: '{allDay.checked}'
					}
				}, {
					xtype: 'checkbox',
					name: 'allDay',
					reference: 'allDay',
					bind: '{record.allDay}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-allDay.lbl')
				}]
			}, {
				xtype: 'fieldcontainer',
				fieldLabel: me.mys.res('event.fld-toDate.lbl'),
				layout: 'hbox',
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'datefield',
					name: 'toDate',
					bind: '{toDate}',
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					name: 'toTime',
					bind: {
						value: '{toTime}',
						disabled: '{allDay.checked}'
					},
					format: WT.getTimeFmt(),
					margin: '0 5 0 0',
					width: 80
				}, {
					xtype: 'button',
					iconCls: 'wtcal-icon-now',
					tooltip: me.mys.res('event.btn-now.tip'),
					handler: function() {
						var EM = Sonicle.webtop.calendar.model.Event;
						EM.setTime(me.getModel(), 'toDate', new Date());
					},
					bind: {
						disabled: '{allDay.checked}'
					}
				}, {
					xtype: 'combo',
					name: 'timezone',
					bind: '{record.timezone}',
					typeAhead: true,
					queryMode: 'local',
					forceSelection: true,
					selectOnFocus: true,
					store: Ext.create('WT.store.Timezone', {
						autoLoad: true
					}),
					valueField: 'id',
					displayField: 'desc',
					fieldLabel: me.mys.res('event.fld-timezone.lbl'),
					margin: '0 20 0 0',
					labelWidth: 75,
					width: 350
				}]
			}, {
				xtype: 'soiconcombo',
				name: 'calendarId',
				bind: '{record.calendarId}',
				typeAhead: true,
				queryMode: 'local',
				forceSelection: true,
				selectOnFocus: true,
				store: {
					autoLoad: true,
					model: 'Sonicle.webtop.calendar.model.Calendar',
					proxy: WT.Util.proxy(me.mys.ID, 'GetGroupCalendars', 'calendars', {
						extraParams: {
							groupId: me.groupId
						}
					})
				},
				valueField: 'calendarId',
				displayField: 'name',
				iconClsField: 'colorCls',
				width: 200
			}]
		})));
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				form = me.getRef('form');
		
		// Overrides autogenerated string id by extjs...
		// It avoids type conversion problems server-side!
		if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
		WT.Util.focusField(form, 'title');
	}
});
