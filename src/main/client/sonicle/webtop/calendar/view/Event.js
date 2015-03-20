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
		'Sonicle.form.Separator',
		'Sonicle.form.RadioGroup',
		'Sonicle.form.field.IconComboBox',
		'WT.store.Timezone',
		'WT.ux.SuggestCombo',
		'WT.model.Value',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.Calendar',
		'Sonicle.webtop.calendar.store.Reminder'
	],
	
	title: '@event.tit',
	iconCls: 'wtcal-icon-event-xs',
	model: 'Sonicle.webtop.calendar.model.Event',
	viewModel: {
		formulas: {
			startDate: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return Ext.Date.clone(val);
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'startDate', val);
				}
			},
			startTime: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return Ext.Date.clone(val);
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'startDate', val);
				}
			},
			endDate: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return Ext.Date.clone(val);
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'endDate', val);
				}
			},
			endTime: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return Ext.Date.clone(val);
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'endDate', val);
				}
			},
			/*
			startDate: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('startDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'startDate', value);
				}
			},
			startTime: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('startDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'startDate', value);
				}
			},
			endDate: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('endDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'endDate', value);
				}
			},
			endTime: {
				bind: {bindTo: '{record}', deep: true},
				get: function(model) {
					return Ext.Date.clone(model.get('endDate'));
				},
				set: function(value) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'endDate', value);
				}
			},
			*/
			allDay: {
				bind: {bindTo: '{record.allDay}'},
				get: function(val) {
					return val;
				},
				set: function(val) {
					this.get('record').set('allDay', val);
				}
			},
			isPrivate: {
				bind: {bindTo: '{record.isPrivate}'},
				get: function(val) {
					return val;
				},
				set: function(val) {
					this.get('record').set('isPrivate', val);
				}
			},
			busy: {
				bind: {bindTo: '{record.busy}'},
				get: function(val) {
					return val;
				},
				set: function(val) {
					this.get('record').set('busy', val);
				}
			}
		}
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		
		
		me.addRef('main', Ext.create({
			xtype: 'form',
			layout: 'anchor',
			modelValidation: true,
			bodyPadding: 5,
			defaults: {
				labelWidth: 60
			},
			items: [{
				xtype: 'wtsuggestcombo',
				itemId: 'fldtitle',
				bind: '{record.title}',
				sid: me.mys.ID,
				suggestionContext: 'eventcalendar',
				fieldLabel: me.mys.res('event.fld-title.lbl'),
				anchor: '100%'
			}, {
				xtype: 'wtsuggestcombo',
				bind: '{record.location}',
				sid: me.mys.ID,
				suggestionContext: 'report_idcalendar', //TODO: verificare nome contesto
				fieldLabel: me.mys.res('event.fld-location.lbl'),
				anchor: '100%'
			}, {
				xtype: 'fieldset', // separator
				collapsed: true
			}, {
				xtype: 'fieldcontainer',
				fieldLabel: me.mys.res('event.fld-startDate.lbl'),
				layout: 'hbox',
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'datefield',
					bind: '{startDate}',
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					bind: {
						value: '{startTime}',
						disabled: '{fldallDay.checked}'
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
						EM.setTime(me.getModel(), 'startDate', new Date());
					},
					bind: {
						disabled: '{fldallDay.checked}'
					}
				}, {
					xtype: 'checkbox',
					reference: 'fldallDay', // Publishes field into viewmodel...
					bind: '{allDay}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-allDay.lbl')
				}]
			}, {
				xtype: 'fieldcontainer',
				fieldLabel: me.mys.res('event.fld-endDate.lbl'),
				layout: 'hbox',
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'datefield',
					bind: '{endDate}',
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					bind: {
						value: '{endTime}',
						disabled: '{fldallDay.checked}'
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
						EM.setTime(me.getModel(), 'endDate', new Date());
					},
					bind: {
						disabled: '{fldallDay.checked}'
					}
				}, {
					xtype: 'combo',
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
					margin: 0,
					flex: 1,
					labelWidth: 75
				}]
			}]
		}));
		me.addRef('appointment', Ext.create({
			itemId: 'appointment',
			xtype: 'form',
			layout: 'anchor',
			title: me.mys.res('event.appointment.tit'),
			modelValidation: true,
			bodyPadding: 5,
			defaults: {
				labelWidth: 80
			},
			items: [{
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: me.mys.res('event.fld-description.lbl'),
				height: 100,
				anchor: '100%'
			}, {
				xtype: 'fieldcontainer',
				layout: 'hbox',
				fieldLabel: me.mys.res('event.fld-reminder.lbl'),
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'combo',
					bind: '{record.reminder}',
					editable: false,
					store: Ext.create('Sonicle.webtop.calendar.store.Reminder'),
					valueField: 'id',
					displayField: 'desc',
					width: 110
				}, {
					xtype: 'checkbox',
					bind: '{isPrivate}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-private.lbl')
				}, {
					xtype: 'checkbox',
					bind: '{busy}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-busy.lbl')
				}, {
					xtype: 'soiconcombo',
					bind: '{record.calendarId}',
					typeAhead: true,
					queryMode: 'local',
					forceSelection: true,
					selectOnFocus: true,
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.calendar.model.Calendar',
						proxy: WT.Util.proxy(me.mys.ID, 'GetCalendars', 'calendars', {
							extraParams: {
								groupId: me.groupId
							}
						})
					},
					valueField: 'calendarId',
					displayField: 'name',
					iconClsField: 'colorCls',
					labelWidth: 70,
					fieldLabel: me.mys.res('event.fld-calendar.lbl'),
					margin: 0,
					flex: 1
				}]	
			}, {
				xtype: 'soseparator'
			} /*{
				xtype: 'fieldset', // separator
				collapsed: true,
				width: '100%'
			}*/]
		}));
		me.addRef('planning', Ext.create({
			xtype: 'panel',
			itemId: 'planning',
			layout: 'form',
			title: me.mys.res('event.planning.tit')
		}));
		me.addRef('recurrence', Ext.create({
			xtype: 'form',
			itemId: 'recurrence',
			layout: 'form',
			title: me.mys.res('event.recurrence.tit')
		}));
		me.add(Ext.create({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				me.getRef('main'),
				{
					xtype: 'tabpanel',
					flex: 1,
					activeTab: 0,
					items: [
						me.getRef('appointment'), 
						me.getRef('planning'), 
						me.getRef('recurrence')
					]
			}]
		}));
		
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				main = me.getRef('main');
		
		// Overrides autogenerated string id by extjs...
		// It avoids type conversion problems server-side!
		if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
		main.getComponent('fldtitle').focus(true);
	}
});
