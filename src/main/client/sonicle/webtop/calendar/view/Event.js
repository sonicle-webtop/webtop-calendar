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
		'WT.model.Empty',
		'WT.model.Value',
		'WT.store.Timezone',
		'WT.store.RRDaylyFreq',
		'WT.store.RRWeeklyFreq',
		'WT.store.RRMonthlyDay',
		'WT.store.RRMonthlyFreq',
		'WT.store.RRYearlyDay',
		'WT.ux.SuggestCombo',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.Calendar',
		'Sonicle.webtop.calendar.store.Reminder',
		'Sonicle.webtop.calendar.store.AttendeeRcptType',
		'Sonicle.webtop.calendar.store.AttendeeRespStatus'
	],
	
	confirm: 'yn',
	autoToolbar: false,
	
	title: '@event.tit',
	iconCls: 'wtcal-icon-event-xs',
	model: 'Sonicle.webtop.calendar.model.Event',
	//session: true,
	viewModel: {
		formulas: {
			startDate: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'startDate', val);
				}
			},
			startTime: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'startDate', val);
				}
			},
			endDate: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setDate(this.get('record'), 'endDate', val);
				}
			},
			endTime: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					var EM = Sonicle.webtop.calendar.model.Event;
					EM.setTime(this.get('record'), 'endDate', val);
				}
			},
			allDay: WT.Util.checkboxBind('record', 'allDay'),
			isPrivate: WT.Util.checkboxBind('record', 'isPrivate'),
			busy: WT.Util.checkboxBind('record', 'busy'),
			rrType: WT.Util.checkboxGroupBind('record', 'rrType'),
			rrDaylyType: WT.Util.checkboxGroupBind('record', 'rrDaylyType'),
			rrWeeklyDay1: WT.Util.checkboxBind('record', 'rrWeeklyDay1'),
			rrWeeklyDay2: WT.Util.checkboxBind('record', 'rrWeeklyDay2'),
			rrWeeklyDay3: WT.Util.checkboxBind('record', 'rrWeeklyDay3'),
			rrWeeklyDay4: WT.Util.checkboxBind('record', 'rrWeeklyDay4'),
			rrWeeklyDay5: WT.Util.checkboxBind('record', 'rrWeeklyDay5'),
			rrWeeklyDay6: WT.Util.checkboxBind('record', 'rrWeeklyDay6'),
			rrWeeklyDay7: WT.Util.checkboxBind('record', 'rrWeeklyDay7'),
			rrEndsMode: WT.Util.checkboxGroupBind('record', 'rrEndsMode'),
			
			isRRNone: WT.Util.equalsFormula('record', 'rrType', '_'),
			isRRDayly: WT.Util.equalsFormula('record', 'rrType', 'D'),
			isRRWeekly: WT.Util.equalsFormula('record', 'rrType', 'W'),
			isRRMonthly: WT.Util.equalsFormula('record', 'rrType', 'M'),
			isRRYearly: WT.Util.equalsFormula('record', 'rrType', 'Y')
		}
	},
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				me.addAction('saveClose', {
					text: WT.res('act-saveClose.lbl'),
					iconCls: 'wt-icon-saveClose-xs',
					handler: function() {
						me.saveEvent();
					}
				}),
				'-',
				me.addAction('deleteEvent', {
					text: null,
					tooltip: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete-xs',
					handler: function() {
						me.deleteEvent();
					}
				}),
				me.addAction('restoreEvent', {
					text: null,
					tooltip: WT.res('act-restore.lbl'),
					iconCls: 'wt-icon-restore-xs',
					handler: function() {
						me.restoreEvent();
					},
					disabled: true
				}),
				'-',
				me.addAction('printEvent', {
					text: null,
					tooltip: WT.res('act-print.lbl'),
					iconCls: 'wt-icon-print-xs',
					handler: function() {
						//TODO: implementare stampa evento
						WT.warn('TODO');
					}
				})
			]
		});
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
					bind: {
						value: '{startDate}',
						disabled: '{record._isRecurring}'
					},
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
					iconCls: 'wtcal-icon-now-xs',
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
					bind: {
						value: '{endDate}',
						disabled: '{record._isRecurring}'
					},
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
					iconCls: 'wtcal-icon-now-xs',
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
		me.addRef('pAppointment', Ext.create({
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
					typeAhead: false,
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
					flex: 1,
					listeners: {
						select: function(s, rec) {
							me.onCalendarSelect(rec);
						}
					}
				}]	
			}, {
				xtype: 'soseparator'
			}]
		}));
		me.addRef('pInvitation', Ext.create({
			xtype: 'panel',
			itemId: 'invitation',
			title: me.mys.res('event.invitation.tit'),
			layout: 'card',
			items: [
				me.addRef('gpAttendees', Ext.create({
					xtype: 'gridpanel',
					itemId: 'attendees',
					bind: {
						store: '{record.attendees}'
					},
					columns: [{
						dataIndex: 'notify',
						xtype: 'checkcolumn',
						editor: {
							xtype: 'checkbox'
						},
						header: me.mys.res('event.gp-attendees.notify.lbl'),
						width: 70
					}, {
						dataIndex: 'recipient',
						editor: {
							xtype: 'textfield'
						},
						header: me.mys.res('event.gp-attendees.recipient.lbl'),
						flex: 1
					}, {
						dataIndex: 'recipientType',
						renderer: WT.Util.resValueRenderer(me.mys.ID, 'store.attendeeRcptType'),
						editor: Ext.create(WT.Util.localCombo({
							store: Ext.create('Sonicle.webtop.calendar.store.AttendeeRcptType', {
								autoLoad: true
							})
						})),
						header: me.mys.res('event.gp-attendees.recipientType.lbl'),
						width: 180
					}, {
						dataIndex: 'responseStatus',
						renderer: WT.Util.resValueRenderer(me.mys.ID, 'store.attendeeRespStatus'),
						editor: Ext.create(WT.Util.localCombo({
							store: Ext.create('Sonicle.webtop.calendar.store.AttendeeRespStatus', {
								autoLoad: true
							})
						})),
						header: me.mys.res('event.gp-attendees.responseStatus.lbl'),
						width: 100
					}],
					plugins: [
						Ext.create('Ext.grid.plugin.RowEditing', {
							pluginId: 'rowediting',
							clicksToMoveEditor: 2,
							saveBtnText: 'Conferma',
							cancelBtnText: 'Annulla'
						})
					],
					tbar: [
						me.addAction('addAttendee', {
							text: WT.res('act-add.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-add-xs',
							handler: function() {
								me.addAttendee();
							}
						}),
						me.addAction('deleteAttendee', {
							text: WT.res('act-delete.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-delete-xs',
							handler: function() {
								var sm = me.getRef('gpAttendees').getSelectionModel();
								me.deleteAttendee(sm.getSelection());
							},
							disabled: true
						}),
						'->',
						{
							xtype: 'button',
							text: me.mys.res('event.btn-planning.lbl'),
							handler: function() {
								me.getRef('pInvitation').getLayout().setActiveItem('planning');
							}
						}
					],
					listeners: {
						selectionchange: function(s,recs) {
							me.getAction('deleteAttendee').setDisabled(!recs.length);
						}
					}
				})),
				me.addRef('gpPlanning', Ext.create({
					xtype: 'gridpanel',
					itemId: 'planning',
					columns: [],
					store: {
						model: 'WT.model.Empty',
						proxy: WT.Util.proxy(me.mys.ID, 'GetPlanning', 'data')
					},
					tbar: [
						me.addAction('refreshPlanning', {
							text: WT.res('act-refresh.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-refresh-xs',
							handler: function() {
								var sto = me.getRef('gpPlanning').getStore(),
										model = me.getModel(),
										params;
								
								console.log('refreshPlanning');
								
								WT.Util.applyExtraParams(sto.getProxy(), {
									startDate: '',
									endDate: '',
									timezone: model.get('timezone')
								});
								
								WT.warn('TODO');
							}
						}),
						'->',
						{
							xtype: 'button',
							text: me.mys.res('event.btn-attendees.lbl'),
							handler: function() {
								me.getRef('pInvitation').getLayout().setActiveItem('attendees');
							}
						}
					]
				}))
			]
		}));
		me.addRef('pRecurrence', Ext.create({
			xtype: 'panel',
			itemId: 'recurrence',
			//layout: 'anchor',
			bodyPadding: 5,
			title: me.mys.res('event.recurrence.tit'),
			items: [{
				xtype: 'container',
				layout: 'column',
				items: [{
					xtype: 'form',
					layout: 'anchor',
					width: 120,
					items: [{
						xtype: 'radiogroup',
						bind: {
							value: '{rrType}'
						},
						layout: 'vbox',
						defaults: {
							name: 'rrType',
							margin: '0 0 15 0'
						},
						items: [{
							inputValue: '_',
							boxLabel: WT.res('rr.type.none')
						}, {
							inputValue: 'D',
							boxLabel: WT.res('rr.type.dayly')
						}, {
							inputValue: 'W',
							boxLabel: WT.res('rr.type.weekly')
						}, {
							inputValue: 'M',
							boxLabel: WT.res('rr.type.monthly')
						}, {
							inputValue: 'Y',
							boxLabel: WT.res('rr.type.yearly')
						}]
					}]
				}, {
					xtype: 'form',
					layout: 'anchor',
					columnWidth: 1,
					items: [{
						xtype: 'displayfield' // none row
					}, {
						xtype: 'soseparator'
					}, {
						xtype: 'fieldcontainer', // dayly row
						bind: {
							disabled: '{!isRRDayly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'radiogroup',
							bind: {
								value: '{rrDaylyType}'
							},
							columns: [80, 60, 80, 150],
							items: [{
								name: 'rrDaylyType',
								inputValue: '1',
								boxLabel: WT.res('rr.type.dayly.type.1')
							}, {
								xtype: 'combo',
								bind: '{record.rrDaylyFreq}',
								typeAhead: true,
								queryMode: 'local',
								forceSelection: true,
								store: Ext.create('WT.store.RRDaylyFreq'),
								valueField: 'id',
								displayField: 'id',
								width: 60,
								margin: '0 5 0 0'
							}, {
								xtype: 'label',
								text: WT.res('rr.type.dayly.freq')
							}, {
								name: 'rrDaylyType',
								inputValue: '2',
								boxLabel: WT.res('rr.type.dayly.type.2')
							}]
						}]
					}, {
						xtype: 'soseparator'
					}, {
						xtype: 'fieldcontainer', // weekly row
						bind: {
							disabled: '{!isRRWeekly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'label',
							text: WT.res('rr.type.weekly.msg1'),
							width: 75
						}, {
							xtype: 'combo',
							bind: '{record.rrWeeklyFreq}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRWeeklyFreq'),
							valueField: 'id',
							displayField: 'id',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.weekly.freq')
						}, {
							xtype: 'label',
							text: WT.res('rr.type.weekly.msg2')
						}, {
							xtype: 'checkboxgroup',
							items: [{
								bind: '{rrWeeklyDay1}',
								boxLabel: Sonicle.Date.getShortestDayName(1)
							}, {
								bind: '{rrWeeklyDay2}',
								boxLabel: Sonicle.Date.getShortestDayName(2)
							}, {
								bind: '{rrWeeklyDay3}',
								boxLabel: Sonicle.Date.getShortestDayName(3)
							}, {
								bind: '{rrWeeklyDay4}',
								boxLabel: Sonicle.Date.getShortestDayName(4)
							}, {
								bind: '{rrWeeklyDay5}',
								boxLabel: Sonicle.Date.getShortestDayName(5)
							}, {
								bind: '{rrWeeklyDay6}',
								boxLabel: Sonicle.Date.getShortestDayName(6)
							}, {
								bind: '{rrWeeklyDay7}',
								boxLabel: Sonicle.Date.getShortestDayName(0)
							}],
							width: 270
						}]
					}, {
						xtype: 'soseparator'
					}, {
						xtype: 'fieldcontainer', // monthly row
						bind: {
							disabled: '{!isRRMonthly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'label',
							text: WT.res('rr.type.monthly.msg1'),
							width: 75
						}, {
							xtype: 'combo',
							bind: '{record.rrMonthlyDay}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRMonthlyDay'),
							valueField: 'id',
							displayField: 'desc',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.monthly.msg2')
						}, {
							xtype: 'combo',
							bind: '{record.rrMonthlyFreq}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRMonthlyFreq'),
							valueField: 'id',
							displayField: 'id',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.monthly.freq')
						}]
					}, {
						xtype: 'soseparator'
					}, {
						xtype: 'fieldcontainer', // yearly row
						bind: {
							disabled: '{!isRRYearly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'label',
							text: WT.res('rr.type.yearly.msg1'),
							width: 75
						}, {
							xtype: 'combo',
							bind: '{record.rrYearlyDay}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRYearlyDay'),
							valueField: 'id',
							displayField: 'id',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.yearly.msg2')
						}, {
							xtype: 'combo',
							bind: '{record.rrYearlyFreq}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRYearlyFreq'),
							valueField: 'id',
							displayField: 'desc',
							width: 120
						}]
					}]
				}]
			}, {
				xtype: 'form',
				layout: 'anchor',
				items: [{
					xtype: 'radiogroup',
					bind: {
						value: '{rrEndsMode}',
						disabled: '{isRRNone}'
					},
					columns: [70, 70, 50, 90, 50, 105],
					items: [{
						name: 'rrEndsMode',
						inputValue: 'never',
						boxLabel: WT.res('rr.end.never')
					}, {
						name: 'rrEndsMode',
						inputValue: 'repeat',
						boxLabel: WT.res('rr.end.repeat')
					}, {
						xtype: 'numberfield',
						bind: '{record.rrRepeatTimes}',
						minValue: 1,
						maxValue: 10,
						width: 50,
						margin: '0 5 0 0'
					}, {
						xtype: 'label',
						text: WT.res('rr.end.repeat.times')
					}, {
						name: 'rrEndsMode',
						inputValue: 'until',
						boxLabel: WT.res('rr.end.until')
					}, {
						xtype: 'datefield',
						bind: '{record.rrUntilDate}',
						width: 105
					}],
					fieldLabel: WT.res('rr.end')
				}]
			}]
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
						me.getRef('pAppointment'), 
						me.getRef('pInvitation'), 
						me.getRef('pRecurrence')
					]
			}]
		}));
		
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				model = me.getModel(),
				main = me.getRef('main');
		
		
		
		me.getRef('pInvitation').setDisabled(!model.get('_isSingle') && !model.get('_isBroken'));
		me.getRef('pRecurrence').setDisabled(!model.get('_isSingle') && !model.get('_isRecurring'));
		
		// Overrides autogenerated string id by extjs...
		// It avoids type conversion problems server-side!
		//if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
		
		if(me.isMode(me.MODE_EDIT)) {
			me.getAction('restoreEvent').setDisabled(!(model.get('_isBroken') === true));
		} else {
			
		}
		
		main.getComponent('fldtitle').focus(true);
	},
	
	onCalendarSelect: function(cal) {
		var mo = this.getModel();
		mo.set({
			isPrivate: cal.get('isPrivate'),
			busy: cal.get('busy'),
			reminder: cal.get('reminder')
		});
	},
	
	saveEvent: function() {
		var me = this,
				rec = me.getModel();
		
		if(rec.get('_isRecurring') === true) {
			WT.confirmForRecurrence(me.mys.res('event.recurring.confirm.save'), function(bid) {
				if(bid === 'ok') {
					var target = WT.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']),
						proxy = rec.getProxy();
					
					// Inject target param into proxy...
					proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), {
						target: target
					}));
					me.doSave(true);
				}
			}, me);
		} else {
			me.doSave(true);
		}
	},
	
	deleteEvent: function() {
		var me = this,
				rec = me.getModel(),
				ajaxFn;
		
		ajaxFn = function(target, id) {
			me.wait();
			WT.ajaxReq(me.mys.ID, 'ManageEventsScheduler', {
				params: {
					crud: 'delete',
					target: target,
					id: id
				},
				callback: function(success, o) {
					me.unwait();
					if(success) {
						me.closeView(false);
						me.mys.refreshEvents();
					}
				}
			});
		};
		
		if(rec.get('_isRecurring') === true) {
			WT.confirmForRecurrence(me.mys.res('event.recurring.confirm.delete'), function(bid) {
				if(bid === 'ok') {
					var target = WT.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']);
					ajaxFn(target, rec.get('id'));
				}
			}, me);
		} else {
			WT.confirm(me.mys.res('event.confirm.delete', rec.get('title')), function(bid) {
				if(bid === 'yes') {
					ajaxFn('this', rec.get('id'));
				}
			}, me);
		}
	},
	
	restoreEvent: function() {
		var me = this,
				rec = me.getModel();
		WT.confirm(me.mys.res('event.recurring.confirm.restore'), function(bid) {
			if(bid === 'yes') {
				me.wait();
				WT.ajaxReq(me.mys.ID, 'ManageEventsScheduler', {
					params: {
						crud: 'restore',
						id: rec.get('id')
					},
					callback: function(success, o) {
						me.unwait();
						if(success) {
							me.closeView(false);
							me.mys.refreshEvents();
						}
					}
				});
			}
		}, me);
	},
	
	addAttendee: function() {
		var me = this,
				grid = me.getRef('gpAttendees'),
				sto = grid.getStore(),
				rowEditing = grid.getPlugin('rowediting'),
				rec;
		
		rowEditing.cancelEdit();
		rec = Ext.create('Sonicle.webtop.calendar.model.EventAttendee', {
			recipientType: 'N',
			responseStatus: 'needsAction',
			notify: true
		});
		sto.insert(0, rec);
		rowEditing.startEdit(0, 0);
	},
	
	deleteAttendee: function(rec) {
		var me = this,
				grid = me.getRef('gpAttendees'),
				sto = grid.getStore(),
				rowEditing = grid.getPlugin('rowediting');
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				rowEditing.cancelEdit();
				sto.remove(rec);
			}
		}, me);
	}
});
