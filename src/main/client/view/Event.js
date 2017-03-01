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
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.form.field.Palette',
		'Sonicle.form.RadioGroup',
		'Sonicle.form.field.ColorComboBox',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.ValueModel',
		'WTA.ux.field.RecipientSuggestCombo',
		'WTA.ux.field.SuggestCombo',
		'WTA.model.ActivityLkp',
		'WTA.model.CausalLkp',
		'WTA.store.Timezone',
		'WTA.store.RRDailyFreq',
		'WTA.store.RRWeeklyFreq',
		'WTA.store.RRMonthlyDay',
		'WTA.store.RRMonthlyFreq',
		'WTA.store.RRYearlyDay',
		'WTA.store.RRYearlyFreq',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.CalendarLkp',
		'Sonicle.webtop.calendar.store.Reminder',
		'Sonicle.webtop.calendar.store.AttendeeRcptType',
		'Sonicle.webtop.calendar.store.AttendeeRespStatus'
	],
	
	dockableConfig: {
		title: '{event.tit}',
		iconCls: 'wtcal-icon-event-xs',
		width: 650,
		height: 510
	},
	confirm: 'yn',
	autoToolbar: false,
	fieldTitle: 'title',
	modelName: 'Sonicle.webtop.calendar.model.Event',
	
	suspendPlanningRefresh: 0,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			startDate: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setStartDate(val);
				}
			},
			startTime: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setStartTime(val);
				}
			},
			endDate: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setEndDate(val);
				}
			},
			endTime: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setEndTime(val);
				}
			},
			allDay: WTF.checkboxBind('record', 'allDay'),
			isPrivate: WTF.checkboxBind('record', 'isPrivate'),
			busy: WTF.checkboxBind('record', 'busy'),
			rrType: WTF.checkboxGroupBind('record', 'rrType'),
			rrDailyType: WTF.checkboxGroupBind('record', 'rrDailyType'),
			rrWeeklyDay1: WTF.checkboxBind('record', 'rrWeeklyDay1'),
			rrWeeklyDay2: WTF.checkboxBind('record', 'rrWeeklyDay2'),
			rrWeeklyDay3: WTF.checkboxBind('record', 'rrWeeklyDay3'),
			rrWeeklyDay4: WTF.checkboxBind('record', 'rrWeeklyDay4'),
			rrWeeklyDay5: WTF.checkboxBind('record', 'rrWeeklyDay5'),
			rrWeeklyDay6: WTF.checkboxBind('record', 'rrWeeklyDay6'),
			rrWeeklyDay7: WTF.checkboxBind('record', 'rrWeeklyDay7'),
			rrEndsMode: WTF.checkboxGroupBind('record', 'rrEndsMode'),
			
			isRRNone: WTF.equalsFormula('record', 'rrType', '_'),
			isRRDayly: WTF.equalsFormula('record', 'rrType', 'D'),
			isRRWeekly: WTF.equalsFormula('record', 'rrType', 'W'),
			isRRMonthly: WTF.equalsFormula('record', 'rrType', 'M'),
			isRRYearly: WTF.equalsFormula('record', 'rrType', 'Y'),
			foIsRecurring: WTF.equalsFormula('record', '_recurringInfo', 'recurring')
		});
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel(),
				main, appointment, invitation, recurrence;
		
		Ext.apply(me, {
			tbar: [
				me.addAction('saveClose', {
					text: WT.res('act-saveClose.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-saveClose-xs',
					handler: function() {
						me.saveEvent();
					}
				}),
				'-',
				me.addAction('delete', {
					text: null,
					tooltip: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete-xs',
					handler: function() {
						me.deleteEvent();
					}
				}),
				me.addAction('restore', {
					text: null,
					tooltip: WT.res('act-restore.lbl'),
					iconCls: 'wt-icon-restore-xs',
					handler: function() {
						me.restoreEvent();
					},
					disabled: true
				}),
				'-',
				me.addAction('print', {
					text: null,
					tooltip: WT.res('act-print.lbl'),
					iconCls: 'wt-icon-print-xs',
					handler: function() {
						//TODO: aggiungere l'azione 'salva' permettendo così la stampa senza chiudere la form
						me.printEvent(me.getModel().getId());
					}
				}),
				'->',
				WTF.localCombo('id', 'desc', {
					reference: 'fldowner',
					bind: '{record._profileId}',
					store: {
						autoLoad: true,
						model: 'WTA.model.Simple',
						proxy: WTF.proxy(me.mys.ID, 'LookupCalendarRoots', 'roots')
					},
					fieldLabel: me.mys.res('event.fld-owner.lbl'),
					labelWidth: 75,
					listeners: {
						select: function(s, rec) {
							me.updateCalendarFilters();
							//me.updateActivityParams(true);
							me.refreshActivities();
						}
					}
				}),
				WTF.lookupCombo('calendarId', 'name', {
					xtype: 'socolorcombo',
					reference: 'fldcalendar',
					bind: '{record.calendarId}',
					store: {
						autoLoad: true,
						model: me.mys.preNs('model.CalendarLkp'),
						proxy: WTF.proxy(me.mys.ID, 'LookupCalendarFolders', 'folders')
					},
					colorField: 'color',
					listeners: {
						select: function(s, rec) {
							me.onCalendarSelect(rec);
						}
					}
				})
			]
		});
		me.callParent(arguments);
		
		main = Ext.create({
			xtype: 'wtform',
			modelValidation: true,
			defaults: {
				labelWidth: 60
			},
			items: [{
				xtype: 'wtsuggestcombo',
				reference: 'fldtitle',
				bind: '{record.title}',
				sid: me.mys.ID,
				suggestionContext: 'eventtitle',
				//suggestionContext: 'eventcalendar',
				fieldLabel: me.mys.res('event.fld-title.lbl'),
				anchor: '100%',
				listeners: {
					enterkey: function() {
						me.getAction('saveClose').execute();
					}
				}
			}, {
				xtype: 'wtsuggestcombo',
				bind: '{record.location}',
				sid: me.mys.ID,
				suggestionContext: 'eventlocation',
				//suggestionContext: 'report_idcalendar', //TODO: verificare nome contesto
				fieldLabel: me.mys.res('event.fld-location.lbl'),
				anchor: '100%',
				plugins: [
					'soenterkeyplugin'
				],
				listeners: {
					enterkey: function() {
						me.getAction('saveClose').execute();
					}
				}
			}, {
				xtype: 'formseparator'
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
						disabled: '{foIsRecurring}'
					},
					startDay: WT.getStartDay(),
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					bind: {
						value: '{startTime}',
						disabled: '{fldallDay.checked}'
					},
					format: WT.getShortTimeFmt(),
					margin: '0 5 0 0',
					width: 80
				}, {
					xtype: 'button',
					iconCls: 'wtcal-icon-now-xs',
					tooltip: me.mys.res('event.btn-now.tip'),
					bind: {
						disabled: '{fldallDay.checked}'
					},
					handler: function() {
						me.getModel().setStartTime(new Date());
					}
				}, {
					xtype: 'checkbox',
					reference: 'fldallDay',
					bind: '{allDay}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-allDay.lbl'),
					handler: function(s,nv) {
						// It seems that the handler method and the change event
						// will fire without any user interaction (eg. binding);
						// so we need to deactivate the code below durin loading.
						if (me.modelLoading) return;
						// ---
						var mo = me.getModel(),
								soDate = Sonicle.Date,
								dt = null;
						if (nv === true) {
							dt = soDate.setTime(new Date(), 0, 0, 0);
							mo.setStartTime(dt);
							mo.setEndTime(dt);
						} else {
							dt = me.mys.getVar('workdayStart');
							mo.setStartTime(dt);
							mo.setEndTime(soDate.add(dt, {minutes: 30}));
						}
					}
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
						disabled: '{foIsRecurring}'
					},
					startDay: WT.getStartDay(),
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					bind: {
						value: '{endTime}',
						disabled: '{fldallDay.checked}'
					},
					format: WT.getShortTimeFmt(),
					margin: '0 5 0 0',
					width: 80
				}, {
					xtype: 'button',
					iconCls: 'wtcal-icon-now-xs',
					tooltip: me.mys.res('event.btn-now.tip'),
					bind: {
						disabled: '{fldallDay.checked}'
					},
					handler: function() {
						me.getModel().setEndTime(new Date());
					}
				}, {
					xtype: 'combo',
					bind: '{record.timezone}',
					typeAhead: true,
					queryMode: 'local',
					forceSelection: true,
					selectOnFocus: true,
					store: Ext.create('WTA.store.Timezone', {
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
		});
		appointment = Ext.create({
			xtype: 'wtform',
			title: me.mys.res('event.appointment.tit'),
			modelValidation: true,
			defaults: {
				labelWidth: 110
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
					store: Ext.create('Sonicle.webtop.calendar.store.Reminder', {
						autoLoad: true
					}),
					valueField: 'id',
					displayField: 'desc',
					triggers: {
						clear: WTF.clearTrigger()
					},
					emptyText: WT.res('word.none.male'),
					width: 150
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
				}]
			}, {
				xtype: 'formseparator'
			}, WTF.remoteCombo('id', 'desc', {
				reference: 'fldactivity',
				bind: '{record.activityId}',
				autoLoadOnValue: true,
				store: {
					model: 'WTA.model.ActivityLkp',
					proxy: WTF.proxy(WT.ID, 'LookupActivities', 'activities'),
					filters: [{
						filterFn: function(rec) {
							if(rec.get('readOnly')) {
								if(rec.getId() !== me.lref('fldactivity').getValue()) {
									return null;
								}
							}
							return rec;
						}
					}],
					listeners: {
						beforeload: {
							fn: function(s) {
								WTU.applyExtraParams(s, {
									profileId: me.getModel().get('_profileId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.mys.res('event.fld-activity.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldcustomer',
				bind: '{record.customerId}',
				autoLoadOnValue: true,
				store: {
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupCustomers', 'customers')
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				listeners: {
					select: function() {
						var model = me.getModel();
						model.set('statisticId', null);
						model.set('causalId', null);
					},
					clear: function() {
						var model = me.getModel();
						model.set('statisticId', null);
						model.set('causalId', null);
					}
				},
				fieldLabel: me.mys.res('event.fld-customer.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldstatistic',
				bind: '{record.statisticId}',
				autoLoadOnValue: true,
				store: {
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupStatisticCustomers', 'customers'),
					listeners: {
						beforeload: {
							fn: function(s) {
								WTU.applyExtraParams(s, {
									profileId: me.getModel().get('_profileId'),
									parentCustomerId: me.getModel().get('customerId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.mys.res('event.fld-statistic.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldcausal',
				bind: '{record.causalId}',
				autoLoadOnValue: true,
				store: {
					model: 'WTA.model.CausalLkp',
					proxy: WTF.proxy(WT.ID, 'LookupCausals', 'causals'),
					listeners: {
						beforeload: {
							fn: function(s) {
								WTU.applyExtraParams(s, {
									profileId: me.getModel().get('_profileId'),
									customerId: me.getModel().get('customerId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.mys.res('event.fld-causal.lbl'),
				anchor: '100%'
			})]
		});
		invitation = Ext.create({
			xtype: 'container',
			reference: 'tabinvitation',
			referenceHolder: true,
			title: me.mys.res('event.invitation.tit'),
			layout: 'card',
			items: [
				{
					xtype: 'gridpanel',
					reference: 'gpattendees',
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
							xtype: 'wtrcptsuggestcombo',
							matchFieldWidth: false,
							listConfig: {
								width: 350,
								minWidth: 350
							}
						},
						renderer: Ext.util.Format.htmlEncode,
						header: me.mys.res('event.gp-attendees.recipient.lbl'),
						flex: 1
					}, {
						dataIndex: 'recipientType',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRcptType',
							keepcase: true
						}),
						editor: Ext.create(WTF.localCombo('id', 'desc', {
							store: Ext.create('Sonicle.webtop.calendar.store.AttendeeRcptType', {
								autoLoad: true
							})
						})),
						header: me.mys.res('event.gp-attendees.recipientType.lbl'),
						width: 110
					}, {
						dataIndex: 'recipientRole',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRcptRole',
							keepcase: true
						}),
						editor: Ext.create(WTF.localCombo('id', 'desc', {
							store: Ext.create('Sonicle.webtop.calendar.store.AttendeeRcptRole', {
								autoLoad: true
							})
						})),
						header: me.mys.res('event.gp-attendees.recipientRole.lbl'),
						width: 110
					}, {
						dataIndex: 'responseStatus',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRespStatus',
							keepcase: true
						}),
						editor: Ext.create(WTF.localCombo('id', 'desc', {
							store: Ext.create('Sonicle.webtop.calendar.store.AttendeeRespStatus', {
								autoLoad: true
							})
						})),
						header: me.mys.res('event.gp-attendees.responseStatus.lbl'),
						width: 110
					}],
					plugins: [
						Ext.create('Ext.grid.plugin.RowEditing', {
							pluginId: 'rowediting',
							clicksToMoveEditor: 2,
							saveBtnText: WT.res('act-confirm.lbl'),
							cancelBtnText: WT.res('act-cancel.lbl')
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
								var sm = me.lref('tabinvitation.gpattendees').getSelectionModel();
								me.deleteAttendee(sm.getSelection());
							},
							disabled: true
						}),
						'->',
						{
							xtype: 'button',
							text: me.mys.res('event.btn-planning.lbl'),
							handler: function() {
								me.lref('tabinvitation').getLayout().setActiveItem('planning');
							}
						}
					],
					border: false,
					listeners: {
						selectionchange: function(s,recs) {
							me.getAction('deleteAttendee').setDisabled(!recs.length);
						}
					}
				}, {
					xtype: 'gridpanel',
					reference: 'gpplanning',
					itemId: 'planning',
					enableLocking: true,
					selModel: {
						type: 'spreadsheet',
						columnSelect: true,
						checkboxSelect: false,
						cellSelect: false,
						rowSelect: false
					},
					store: {
						model: 'WTA.ux.data.EmptyModel',
						proxy: WTF.proxy(me.mys.ID, 'GetPlanning', 'data'),
						listeners: {
							metachange: function(s, meta) {
								if (meta.colsInfo) {
									// In order to draw a better view we need to nest grid columns (hours) 
									// belonging to same day date under the same master header.
									// So we need to create a nested structure identifying useful columns.
									
									var colsInfo = [];
									Ext.iterate(meta.colsInfo, function(col,i) {
										if (col.dataIndex === 'recipient') {
											col.header = me.mys.res('event.gp-planning.recipient.lbl');
											col.lockable = false;
											col.locked = true;
											col.width = 200;
											
											// Add this column as is... skip nesting
											colsInfo.push(col);
											
										} else {
											col.renderer = WTF.clsColRenderer({
												clsPrefix: 'wtcal-planning-',
												moreCls: (col.overlaps) ? 'wtcal-planning-overlaps' : null
											});
											col.resizable = false;
											col.lockable = false;
											col.sortable = false;
											col.hideable = false;
											col.menuDisabled = true;
											col.draggable = false;
											col.flex = 1;
											
											// Nest this column under right day date
											if (colsInfo[colsInfo.length-1].date !== col.date) {
												colsInfo.push({
													date: col.date,
													text: col.date,
													lockable: false,
													columns: []
												});
											}
											colsInfo[colsInfo.length-1].columns.push(col);
										}
									});
									me.lref('tabinvitation.gpplanning').reconfigure(s, colsInfo);
								}
							}
						}
					},
					columns: [],
					tbar: [
						me.addAction('reloadPlanning', {
							text: WT.res('act-refresh.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-refresh-xs',
							handler: function() {
								me.reloadPlanning();
							}
						}),
						'-',
						{
							xtype: 'tbitem',
							width: 15,
							height: 15,
							cls: 'wtcal-planning-legend-free'
						}, {
							xtype: 'tbtext',
							html: me.mys.res('event.gp-planning.free')
						}, {
							xtype: 'tbitem',
							width: 15,
							height: 15,
							cls: 'wtcal-planning-legend-busy'
						}, {
							xtype: 'tbtext',
							html: me.mys.res('event.gp-planning.busy')
						}, {
							xtype: 'tbitem',
							width: 15,
							height: 15,
							cls: 'wtcal-planning-legend-unknown'
						}, {
							xtype: 'tbtext',
							html: me.mys.res('event.gp-planning.unknown')
						},
						'->',
						{
							xtype: 'button',
							text: me.mys.res('event.btn-attendees.lbl'),
							handler: function() {
								me.lref('tabinvitation').getLayout().setActiveItem('attendees');
							}
						}
					],
					border: false,
					listeners: {
						activate: function() {
							me.reloadPlanning();
						},
						selectionchange: function(s, sel) {
							var dates = me.getPlanningSelectionDates(sel);
							if (dates) {
								me.suspendPlanningRefresh++;
								me.getModel().setStart(dates[0]);
								me.getModel().setEnd(dates[1]);
								Ext.defer(function(){
									me.suspendPlanningRefresh--;
								}, 100);
							}
						}
					}
				}
			]
		});
		recurrence = Ext.create({
			xtype: 'wtpanel',
			reference: 'tabrecurrence',
			itemId: 'recurrence',
			title: me.mys.res('event.recurrence.tit'),
			layout: 'vbox',
			items: [{
				xtype: 'container',
				layout: 'hbox',
				items: [{
					xtype: 'wtform',
					modelValidation: true,
					layout: 'anchor',
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
							bind: {
								disabled: '{foIsRecurring}'
							},
							inputValue: '_',
							boxLabel: WT.res('rr.type.none')
						}, {
							inputValue: 'D',
							boxLabel: WT.res('rr.type.daily')
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
					}],
					width: 120
				}, {
					xtype: 'wtform',
					layout: 'anchor',
					modelValidation: true,
					items: [{
						xtype: 'displayfield' // none row
					}, {
						xtype: 'formseparator'
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
								value: '{rrDailyType}'
							},
							columns: [80, 60, 80, 150],
							items: [{
								name: 'rrDailyType',
								inputValue: '1',
								boxLabel: WT.res('rr.type.daily.type.1')
							}, {
								xtype: 'combo',
								bind: '{record.rrDailyFreq}',
								typeAhead: true,
								queryMode: 'local',
								forceSelection: true,
								store: Ext.create('WTA.store.RRDailyFreq'),
								valueField: 'id',
								displayField: 'id',
								width: 60,
								margin: '0 5 0 0',
								listeners: {
									change: function() {
										me.getModel().set('rrDailyType', '1');
									}
								}
							}, {
								xtype: 'label',
								text: WT.res('rr.type.daily.freq')
							}, {
								name: 'rrDailyType',
								inputValue: '2',
								boxLabel: WT.res('rr.type.daily.type.2')
							}]
						}]
					}, {
						xtype: 'formseparator'
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
							store: Ext.create('WTA.store.RRWeeklyFreq'),
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
						xtype: 'formseparator'
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
							store: Ext.create('WTA.store.RRMonthlyDay'),
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
							store: Ext.create('WTA.store.RRMonthlyFreq'),
							valueField: 'id',
							displayField: 'id',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.monthly.freq')
						}]
					}, {
						xtype: 'formseparator'
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
							store: Ext.create('WTA.store.RRYearlyDay'),
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
							store: Ext.create('WTA.store.RRYearlyFreq'),
							valueField: 'id',
							displayField: 'desc',
							width: 120
						}]
					}],
					flex: 1
				}],
				flex: 1
			}, {
				xtype: 'wtform',
				modelValidation: true,
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
						margin: '0 5 0 0',
						listeners: {
							change: function() {
								me.getModel().set('rrEndsMode', 'repeat');
							}
						}
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
						startDay: WT.getStartDay(),
						width: 105,
						listeners: {
							select: function() {
								me.getModel().set('rrEndsMode', 'until');
							}
						}
					}],
					fieldLabel: WT.res('rr.end')
				}],
				height: 40
			}]
		});
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				main,
				{
					xtype: 'wttabpanel',
					activeTab: 0,
					items: [
						appointment,
						invitation,
						recurrence
					],
					flex: 1
				}
			]
		});
		
		//me.updateCalendarFilters();
		//me.updateActivityParams(false);
		//me.updateStatisticParams(false);
		//me.updateCausalParams(false);
		
		me.on('viewload', me.onViewLoad);
		vm.bind('{record.startDate}', me.onDatesChanged, me);
		vm.bind('{record.endDate}', me.onDatesChanged, me);
		vm.bind('{record.timezone}', me.onDatesChanged, me);
		vm.bind('{record.customerId}', me.onCustomerChanged, me);
		vm.bind('{record.rrType}', me.onRrTypeChanged, me);
		vm.bind('{record.rrEndsMode}', me.onRrEndsModeChanged, me);
		vm.bind('{record.rrDailyType}', me.onRrDailyTypeChanged, me);
	},
	
	onRrTypeChanged: function(v) {
		var me = this,
				mo = me.getModel();
		if (!me.modelLoading) {
			if (mo.get('rrType') !== '_') {
				mo.setIfNull('rrRepeatTimes', 1);
				mo.setIfNull('rrUntilDate', Ext.Date.clone(mo.get('startDate')));
			}
			switch(mo.get('rrType')) {
				case 'D':
					mo.setIfNull('rrDailyFreq', 1);
					break;
				case 'W':
					mo.setIfNull('rrWeeklyFreq', 1);
					mo.setIfNull('rrWeeklyDay1', false);
					mo.setIfNull('rrWeeklyDay2', false);
					mo.setIfNull('rrWeeklyDay3', false);
					mo.setIfNull('rrWeeklyDay4', false);
					mo.setIfNull('rrWeeklyDay5', false);
					mo.setIfNull('rrWeeklyDay6', false);
					mo.setIfNull('rrWeeklyDay7', false);
					break;
				case 'M':
					mo.setIfNull('rrMonthlyFreq', 1);
					mo.setIfNull('rrMonthlyDay', 1);
					break;
				case 'Y':
					mo.setIfNull('rrYearlyFreq', 1);
					mo.setIfNull('rrYearlyDay', 1);
					break;
			}
		}
		mo.refreshValidatorsForRrType();
	},
	
	onRrEndsModeChanged: function(v) {
		var me = this,
				mo = me.getModel();
		if (!me.modelLoading) {
			mo.setIfNull('rrRepeatTimes', 1);
			mo.setIfNull('rrUntilDate', Ext.Date.clone(mo.get('startDate')));
		}
		me.getModel().refreshValidatorsForRrEndsMode();
	},
	
	onRrDailyTypeChanged: function(v) {
		this.getModel().refreshValidatorsForRrDailyType();
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				mo = me.getModel(),
				owner = me.lref('fldowner');
		
		// Overrides autogenerated string id by extjs...
		// It avoids type conversion problems server-side!
		//if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
		me.updateCalendarFilters();
		
		if(me.isMode(me.MODE_NEW)) {
			owner.setDisabled(false);
			me.getAction('delete').setDisabled(true);
			me.getAction('restore').setDisabled(true);
		} else if(me.isMode(me.MODE_VIEW)) {
			me.getAction('saveClose').setDisabled(true);
			me.getAction('delete').setDisabled(true);
			me.getAction('restore').setDisabled(true);
			owner.setDisabled(true);
		} else if(me.isMode(me.MODE_EDIT)) {
			me.getAction('restore').setDisabled(!mo.isBroken());
			owner.setDisabled(true);
			me.lref('tabinvitation').setDisabled(mo.isRecurring());
			me.lref('tabrecurrence').setDisabled(mo.hasAttendees() || !mo.isRecurring());
		}
		
		me.lref('fldtitle').focus(true);
	},
	
	refreshActivities: function() {
		this.lref('fldactivity').getStore().load();
	},
	
	refreshStatistics: function() {
		this.lref('fldstatistic').getStore().load();
	},
	
	refreshCausals: function() {
		this.lref('fldcausal').getStore().load();
	},
	
	getPlanningSelectionDates: function(sel) {
		var min, max, fmt = 'Y-m-d H:i';
		Ext.iterate(sel.selectedColumns, function(col) {
			if (!min) min = col.dataIndex;
			if (!max) max = col.dataIndex;
			if (col.dataIndex < min) min = col.dataIndex;
			if (col.dataIndex > max) max = col.dataIndex;
		});
		if (min && max) {
			return [Ext.Date.parseDate(min, fmt), Ext.Date.parseDate(max, fmt)];
		} else {
			return null;
		}
	},
	
	updateCalendarFilters: function() {
		this.lref('fldcalendar').getStore().addFilter({
			property: '_profileId',
			value: this.getModel().get('_profileId')
		});
	},
	
	onCalendarSelect: function(cal) {
		var mo = this.getModel();
		mo.set({
			isPrivate: cal.get('isPrivate'),
			busy: cal.get('busy'),
			reminder: cal.get('reminder')
		});
	},
	
	onDatesChanged: function() {
		var me = this;
		if ((me.suspendPlanningRefresh === 0) && me.isPlanningActive()) me.reloadPlanning();
	},
	
	onCustomerChanged: function() {
		this.refreshStatistics();
		this.refreshCausals();
	},
	
	saveEvent: function() {
		var me = this,
				rec = me.getModel();
		
		if(rec.isRecurring()) {
			me.mys.confirmForRecurrence(me.mys.res('event.recurring.confirm.save'), function(bid) {
				if(bid === 'ok') {
					var target = WTA.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']),
						proxy = rec.getProxy();
					
					// Inject target param into proxy...
					proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), {
						target: target
					}));
					me.saveView(true);
				}
			}, me);
		} else {
			me.saveView(true);
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
						me.fireEvent('viewsave', me, true, rec);
						me.closeView(false);
					}
				}
			});
		};
		
		if(rec.isRecurring()) {
			me.mys.confirmForRecurrence(me.mys.res('event.recurring.confirm.delete'), function(bid) {
				if(bid === 'ok') {
					var target = WTA.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']);
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
							me.fireEvent('viewsave', me, true, rec);
							me.closeView(false);
						}
					}
				});
			}
		}, me);
	},
	
	addAttendee: function() {
		var me = this,
				gp = me.lref('tabinvitation.gpattendees'),
				sto = gp.getStore(),
				re = gp.getPlugin('rowediting'),
				cal = me.lref('fldcalendar').getSelection(),
				rec;
		
		re.cancelEdit();
		rec = sto.add(sto.createModel({
			notify: (cal) ? cal.get('invitation') : false,
			recipientType: 'IND',
			recipientRole: 'REQ',
			responseStatus: 'NA'
		}))[0];
		re.startEdit(rec, 1);
	},
	
	deleteAttendee: function(rec) {
		var me = this,
				grid = me.lref('tabinvitation.gpattendees'),
				sto = grid.getStore(),
				rowEditing = grid.getPlugin('rowediting');
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				rowEditing.cancelEdit();
				sto.remove(rec);
			}
		}, me);
	},
	
	isPlanningActive: function() {
		return (this.lref('tabinvitation').getLayout().getActiveItem().getItemId() === 'planning');
	},
	
	reloadPlanning: function() {
		var me = this,
				sto = me.lref('tabinvitation.gpplanning').getStore(),
				model = me.getModel(),
				serData = model.getData({serialize: true, associated: true});

		WTA.Util.applyExtraParams(sto.getProxy(), {
			startDate: serData['startDate'],
			endDate: serData['endDate'],
			timezone: serData['timezone'],
			attendees: Ext.JSON.encode(serData['attendees'])
		});
		sto.load();
	},
	
	printEvent: function(eventKey) {
		var me = this;
		if(me.getModel().isDirty()) {
			WT.warn(WT.res('warn.print.notsaved'));
		} else {
			me.mys.printEventsDetail([eventKey]);
		}
	}
});
