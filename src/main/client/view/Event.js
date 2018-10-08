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
		'Sonicle.form.field.rr.Recurrence',
		'Sonicle.plugin.FileDrop',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.ValueModel',
		'WTA.ux.field.RecipientSuggestCombo',
		'WTA.ux.field.SuggestCombo',
		'WTA.ux.grid.Attachments',
		'WTA.model.ActivityLkp',
		'WTA.model.CausalLkp',
		'WTA.store.Timezone',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.CalendarLkp',
		'Sonicle.webtop.calendar.store.Reminder',
		'Sonicle.webtop.calendar.store.AttendeeRcptType',
		'Sonicle.webtop.calendar.store.AttendeeRespStatus'
	],
	
	dockableConfig: {
		title: '{event.tit}',
		iconCls: 'wtcal-icon-event-xs',
		width: 650
		//height: see below...
	},
	confirm: 'yn',
	autoToolbar: false,
	fieldTitle: 'title',
	modelName: 'Sonicle.webtop.calendar.model.Event',
	
	suspendCheckboxChange: true,
	suspendPlanningRefresh: 0,
	suspendEventsInRR: false,
	
	constructor: function(cfg) {
		var me = this;
		Ext.merge(cfg || {}, {
            dockableConfig: {
               height: cfg.showStatisticFields ? 520 : 460
         }});
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			startDate: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return val ? Ext.Date.clone(val): null;
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
					return val ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setEndDate(val);
				}
			},
			endTime: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return val ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setEndTime(val);
				}
			},
			allDay: WTF.checkboxBind('record', 'allDay'),
			isPrivate: WTF.checkboxBind('record', 'isPrivate'),
			busy: WTF.checkboxBind('record', 'busy'),
			foHasRecurrence: WTF.foIsEmpty('record', 'rrule'),
			foIsRecurring: WTF.foIsEqual('record', '_recurringInfo', 'recurring')
		});
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		Ext.apply(me, {
			tbar: [
				me.addAct('saveClose', {
					text: WT.res('act-saveClose.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-saveClose-xs',
					handler: function() {
						me.saveEvent();
					}
				}),
				'-',
				me.addAct('delete', {
					text: null,
					tooltip: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete',
					handler: function() {
						me.deleteEvent();
					}
				}),
				me.addAct('restore', {
					text: null,
					tooltip: WT.res('act-restore.lbl'),
					iconCls: 'wt-icon-restore-xs',
					handler: function() {
						me.restoreEvent();
					},
					disabled: true
				}),
				'-',
				me.addAct('print', {
					text: null,
					tooltip: WT.res('act-print.lbl'),
					iconCls: 'wt-icon-print',
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
							me.refreshCausals();
						}
					}
				}),
				WTF.lookupCombo('calendarId', 'name', {
					xtype: 'socolorcombo',
					reference: 'fldcalendar',
					bind: '{record.calendarId}',
					autoLoadOnValue: true,
					store: {
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
		
		var main, appo, attends, recur, attachs;
		main = {
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
						me.getAct('saveClose').execute();
					}
				}
			}, {
				//FIXME: check suggestcombo on delete all field
				xtype: 'textfield',
				bind: '{record.location}',
				sid: me.mys.ID,
				suggestionContext: 'eventlocation',
				//suggestionContext: 'report_idcalendar', //TODO: verificare nome contesto
				fieldLabel: me.mys.res('event.fld-location.lbl'),
				anchor: '100%',
				listeners: {
					enterkey: function() {
						me.getAct('saveClose').execute();
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
					format: WT.getShortDateFmt(),
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
					handler: function(s, nv) {
						// This handler method and the change event will fire 
						// without any user interaction using binding. We have
						// to do a trick for disabling the code below just after
						// model loading.
						if (me.suspendCheckboxChange) return;
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
					format: WT.getShortDateFmt(),
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
		};
		
		appo = {
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
				xtype: 'formseparator',
				hidden: !me.showStatisticFields
			}, WTF.remoteCombo('id', 'desc', {
				reference: 'fldactivity',
				bind: '{record.activityId}',
				autoLoadOnValue: true,
				hidden: !me.showStatisticFields,
				store: {
					model: 'WTA.model.ActivityLkp',
					proxy: WTF.proxy(WT.ID, 'LookupActivities'),
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
				reference: 'fldmasterdata',
				bind: '{record.masterDataId}',
				autoLoadOnValue: true,
				hidden: !me.showStatisticFields,
				store: {
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupCustomersSuppliers')
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				listeners: {
					select: function() {
						var model = me.getModel();
						model.set('statMasterDataId', null);
						model.set('causalId', null);
					},
					clear: function() {
						var model = me.getModel();
						model.set('statMasterDataId', null);
						model.set('causalId', null);
					}
				},
				fieldLabel: me.mys.res('event.fld-masterData.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldstatmasterdata',
				bind: '{record.statMasterDataId}',
				autoLoadOnValue: true,
				hidden: !me.showStatisticFields,
				store: {
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupStatisticCustomersSuppliers'),
					listeners: {
						beforeload: {
							fn: function(s) {
								WTU.applyExtraParams(s, {
									parentMasterDataId: me.getModel().get('masterDataId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.mys.res('event.fld-statMasterData.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldcausal',
				bind: '{record.causalId}',
				autoLoadOnValue: true,
				hidden: !me.showStatisticFields,
				store: {
					model: 'WTA.model.CausalLkp',
					proxy: WTF.proxy(WT.ID, 'LookupCausals'),
					filters: [{
						filterFn: function(rec) {
							if(rec.get('readOnly')) {
								if(rec.getId() !== me.lref('fldcausal').getValue()) {
									return null;
								}
							}
							return rec;
						}
					}],
					listeners: {
						beforeload: {
							fn: function(s) {
								var mo = me.getModel();
								WTU.applyExtraParams(s, {
									profileId: mo.get('_profileId'),
									masterDataId: mo.get('masterDataId')
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
		};
		
		attends = {
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
						me.addAct('addAttendee', {
							text: WT.res('act-add.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-add-xs',
							handler: function() {
								me.addAttendee();
							}
						}),
						me.addAct('deleteAttendee', {
							text: WT.res('act-delete.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-delete',
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
							me.getAct('deleteAttendee').setDisabled(!recs.length);
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
						me.addAct('reloadPlanning', {
							text: WT.res('act-refresh.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-refresh',
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
		};
		
		recur = {
			xtype: 'wtform',
			reference: 'tabrecurrence',
			itemId: 'recurrence',
			title: me.mys.res('event.recurrence.tit'),
			modelValidation: true,
			defaults: {
				labelWidth: 110
			},
			items: [{
				xtype: 'sorrfield',
				bind: {
					value: '{record.rrule}',
					startDate: '{startDate}'
				},
				startDay: WT.getStartDay(),
				dateFormat: WT.getShortDateFmt(),
				repeatsText: WT.res('sorrfield.repeats'),
				endsText: WT.res('sorrfield.ends'),
				frequencyTexts: {
					'none': WT.res('sorrfield.freq.none'),
					'raw': WT.res('sorrfield.freq.raw'),
					'3': WT.res('sorrfield.freq.daily'),
					'2': WT.res('sorrfield.freq.weekly'),
					'1': WT.res('sorrfield.freq.monthly'),
					'0': WT.res('sorrfield.freq.yearly')
				},
				onEveryText: WT.res('sorrfield.onEvery'),
				onEveryWeekdayText: WT.res('sorrfield.onEveryWeekday'),
				onDayText: WT.res('sorrfield.onDay'),
				onTheText: WT.res('sorrfield.onThe'),
				thDayText: WT.res('sorrfield.thDay'),
				ofText: WT.res('sorrfield.of'),
				ofEveryText: WT.res('sorrfield.ofEvery'),
				dayText: WT.res('sorrfield.day'),
				weekText: WT.res('sorrfield.week'),
				monthText: WT.res('sorrfield.month'),
				yearText: WT.res('sorrfield.year'),
				ordinalsTexts: {
					'1': WT.res('sorrfield.nth.1st'),
					'2': WT.res('sorrfield.nth.2nd'),
					'3': WT.res('sorrfield.nth.3rd'),
					'4': WT.res('sorrfield.nth.4th'),
					'-2': WT.res('sorrfield.nth.las2nd'),
					'-1': WT.res('sorrfield.nth.last')
				},
				byDayText: WT.res('sorrfield.byDay'),
				byWeekdayText: WT.res('sorrfield.byWeekday'),
				byWeText: WT.res('sorrfield.byWe'),
				endsNeverText: WT.res('sorrfield.endsNever'),
				endsAfterText: WT.res('sorrfield.endsAfter'),
				endsByText: WT.res('sorrfield.endsBy'),
				occurrenceText: WT.res('sorrfield.occurrence'),
				rawFieldEmptyText: WT.res('sorrfield.raw.emp'),
				listeners: {
					rawpasteinvalid: function() {
						WT.warn(me.mys.res('event.error.rrpaste'));
					}
				}
			}]
		};
		
		attachs = {
			xtype: 'wtattachmentsgrid',
			title: me.mys.res('event.attachments.tit'),
			bind: {
				store: '{record.attachments}'
			},
			sid: me.mys.ID,
			uploadContext: 'EventAttachment',
			uploadTag: WT.uiid(me.getId()),
			dropElementId: null,
			highlightDrop: true,
			typeField: 'ext',
			listeners: {
				attachmentlinkclick: function(s, rec) {
					me.openAttachmentUI(rec, false);
				},
				attachmentdownloadclick: function(s, rec) {
					me.openAttachmentUI(rec, true);
				},
				attachmentdeleteclick: function(s, rec) {
					s.getStore().remove(rec);
				},
				attachmentuploaded: function(s, uploadId, file) {
					var sto = s.getStore();
					sto.add(sto.createModel({
						name: file.name,
						size: file.size,
						_uplId: uploadId
					}));
					me.lref('tpnlinner').getLayout().setActiveItem(s);
				}
			}
		};
		
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
					reference: 'tpnlinner',
					activeTab: 0,
					deferredRender: false,
					items: [
						appo,
						attends,
						recur,
						attachs
					],
					flex: 1
				}
			]
		});
		
		me.on('viewload', me.onViewLoad);
		me.on('viewclose', me.onViewClose);
		vm.bind('{record.startDate}', me.onDatesChanged, me);
		vm.bind('{record.endDate}', me.onDatesChanged, me);
		vm.bind('{record.timezone}', me.onDatesChanged, me);
		vm.bind('{record.masterDataId}', me.onMasterDataChanged, me);
	},
	
	onViewLoad: function(s, success) {
		var me = this,
				mo = me.getModel(),
				owner = me.lref('fldowner');
		
		// Overrides autogenerated string id by extjs...
		// It avoids type conversion problems server-side!
		//if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
		me.updateCalendarFilters();
		
		if(me.isMode(me.MODE_NEW)) {
			owner.setDisabled(false);
			me.getAct('delete').setDisabled(true);
			me.getAct('restore').setDisabled(true);			
		} else if(me.isMode(me.MODE_VIEW)) {
			me.getAct('saveClose').setDisabled(true);
			me.getAct('delete').setDisabled(true);
			me.getAct('restore').setDisabled(true);
			owner.setDisabled(true);
		} else if(me.isMode(me.MODE_EDIT)) {
			me.getAct('restore').setDisabled(!mo.isBroken());
			owner.setDisabled(true);
			//me.lref('tabinvitation').setDisabled(mo.isRecurring());
			me.lref('tabrecurrence').setDisabled(mo.isBroken());
		}
		
		me.lref('fldtitle').focus(true);
		
		// Disable checkbox change event suspension!!!
		Ext.defer(function() {
			me.suspendCheckboxChange = false;
		}, 500);
	},
	
	onViewClose: function(s) {
		s.mys.cleanupUploadedFiles(WT.uiid(s.getId()));
	},
	
	refreshActivities: function() {
		this.lref('fldactivity').getStore().load();
	},
	
	refreshStatMasterData: function() {
		this.lref('fldstatmasterdata').getStore().load();
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
		var me = this,
				mo = me.getModel(),
				sto = me.lref('fldcalendar').getStore();
		sto.clearFilter();
		sto.addFilter([{
				property: '_profileId',
				value: mo.get('_profileId')
			}, {
				filterFn: function(rec) {
					if (rec.get('_writable') === false) {
						if (me.isMode(me.MODE_NEW)) return false;
						return rec.getId() === mo.get('calendarId');
					} else {
						return true;
					}
				}
		}]);
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
	
	onMasterDataChanged: function() {
		this.refreshStatMasterData();
		this.refreshCausals();
	},
	
	openAttachmentUI: function(rec, download) {
		var me = this,
				name = rec.get('name'),
				uploadId = rec.get('_uplId'),
				url;
		
		if (!Ext.isEmpty(uploadId)) {
			url = WTF.processBinUrl(me.mys.ID, 'DownloadEventAttachment', {
				inline: !download,
				uploadId: uploadId
			});
		} else {
			url = WTF.processBinUrl(me.mys.ID, 'DownloadEventAttachment', {
				inline: !download,
				eventId: me.getModel().get('eventId'),
				attachmentId: rec.get('id')
			});
		}
		if (download) {
			Sonicle.URLMgr.downloadFile(url, {filename: name});
		} else {
			Sonicle.URLMgr.openFile(url, {filename: name});
		}
	},
	
	saveEvent: function() {
		var me = this,
				mo = me.getModel();
		
		if (mo.isRecurring()) {
			me.mys.confirmForRecurrence(me.mys.res('event.recurring.confirm.save'), function(bid, value) {
				if (bid === 'ok') {
					mo.setExtraParams({target: value});
					me.saveView(true);
				}
			}, me);
			
		} else {
			mo.setExtraParams({target: 'this'});
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
				callback: function(success) {
					me.unwait();
					if(success) {
						me.fireEvent('viewsave', me, true, rec);
						me.closeView(false);
					}
				}
			});
		};
		
		if(rec.isRecurring()) {
			me.mys.confirmForRecurrence(me.mys.res('event.recurring.confirm.delete'), function(bid, value) {
				if (bid === 'ok') {
					ajaxFn(value, rec.get('id'));
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
					callback: function(success) {
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
