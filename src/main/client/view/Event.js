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
		'Sonicle.Data',
		'Sonicle.String',
		'Sonicle.form.RadioGroup',
		'Sonicle.form.field.Palette',
		'Sonicle.form.field.ComboBox',
		'Sonicle.form.field.TagDisplay',
		'Sonicle.form.field.rr.Recurrence',
		'Sonicle.plugin.FileDrop',
		'WTA.util.CustomFields',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.ValueModel',
		'WTA.ux.field.Meeting',
		'WTA.ux.field.MeetingUrl',
		'WTA.ux.field.RecipientSuggestCombo',
		'WTA.ux.field.SuggestCombo',
		'WTA.ux.grid.Attachments',
		'WTA.ux.panel.CustomFieldsEditor',
		'WTA.model.SubjectLkp',
		'WTA.model.ActivityLkp',
		'WTA.model.CausalLkp',
		'WTA.store.Timezone',
		'Sonicle.webtop.calendar.ux.PlanningGrid',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.CalendarLkp',
		'Sonicle.webtop.calendar.store.Reminder',
		'Sonicle.webtop.calendar.store.AttendeeRcptRole',
		'Sonicle.webtop.calendar.store.AttendeeRcptType',
		'Sonicle.webtop.calendar.store.AttendeeRespStatus'
	],
	uses: [
		'Sonicle.Data',
		'Sonicle.webtop.core.view.Tags',
		'Sonicle.webtop.core.view.Meeting'
	],
	
	dockableConfig: {
		title: '{event.tit}',
		iconCls: 'wtcal-icon-event',
		width: 700
		//height: see below...
	},
	actionsResPrefix: 'event',
	confirm: 'yn',
	autoToolbar: false,
	fieldTitle: 'title',
	modelName: 'Sonicle.webtop.calendar.model.Event',
	
	planningResolution: 30,
	suspendCheckboxChange: true,
	suspendPlanningRefresh: 0,
	suspendEventsInRR: false,
	
	constructor: function(cfg) {
		var me = this;
		Ext.merge(cfg || {}, {
            dockableConfig: {
               height: cfg.showStatisticFields ? 550 : 490
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
			recStartDate: {
				bind: {bindTo: '{record.rstart}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setRecurStart(val);
				}
			},
			allDay: WTF.checkboxBind('record', 'allDay'),
			isPrivate: WTF.checkboxBind('record', 'isPrivate'),
			busy: WTF.checkboxBind('record', 'busy'),
			foHasRecurrence: WTF.foIsEmpty('record', 'rrule'),
			foWasRecurring: WTF.foIsEqual('record', '_recurringInfo', 'recurring'),
			foTags: WTF.foTwoWay('record', 'tags', function(v) {
					return Sonicle.String.split(v, '|');
				}, function(v) {
					return Sonicle.String.join('|', v);
			}),
			foHasTags: WTF.foIsEmpty('record', 'tags', true),
			foLocationIsMeeting: WTF.foGetFn('record', 'location', function(val) {
				return WT.isMeetingUrl(val);
			}),
			foHasMeeting: WTF.foGetFn('record', 'extractedUrl', function(val) {
				return WT.isMeetingUrl(val);
			})
		});
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		Ext.apply(me, {
			dockedItems: [
				{
					xtype: 'toolbar',
					dock: 'top',
					items: [
						me.addAct('saveClose', {
							text: WT.res('act-saveClose.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-saveClose',
							handler: function() {
								me.saveEventUI();
							}
						}),
						'-',
						me.addAct('delete', {
							text: null,
							tooltip: WT.res('act-delete.lbl'),
							iconCls: 'wt-icon-delete',
							handler: function() {
								me.deleteEventUI();
							}
						}),
						me.addAct('restore', {
							text: null,
							tooltip: WT.res('act-restore.lbl'),
							iconCls: 'wtcal-icon-rejoinSeries',
							handler: function() {
								me.restoreEventUI();
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
						me.addAct('tags', {
							text: null,
							tooltip: me.mys.res('act-manageTags.lbl'),
							iconCls: 'wt-icon-tag',
							handler: function() {
								me.manageTagsUI(Sonicle.String.split(me.getModel().get('tags'), '|'));
							}
						}),
						me.addAct('addMeeting', {
							text: null,
							tooltip: WT.res(WT.ID, 'act-addMeeting.lbl', WT.getMeetingConfig().name),
							iconCls: 'wt-icon-newMeeting',
							disabled: Ext.isEmpty(WT.getMeetingProvider()) || !WT.isPermitted(WT.ID, 'MEETING', 'CREATE'),
							handler: function() {
								me.addMeetingUI();
							}
						}),
						'->',
						WTF.lookupCombo('calendarId', '_label', {
							xtype: 'socombo',
							reference: 'fldcalendar',
							bind: '{record.calendarId}',
							listConfig: {
								displayField: 'name',
								groupCls: 'wt-theme-text-lighter2'
							},
							autoLoadOnValue: true,
							store: {
								model: me.mys.preNs('model.CalendarLkp'),
								proxy: WTF.proxy(me.mys.ID, 'LookupCalendarFolders', 'folders'),
								grouper: {
									property: '_profileId',
									sortProperty: '_order'
								},
								filters: [{
									filterFn: function(rec) {
										var mo = me.getModel();
										if (mo && me.isMode(me.MODE_NEW)) {
											return rec.get('_writable');
										} else if (mo && me.isMode(me.MODE_VIEW)) {
											if (rec.getId() === mo.get('calendarId')) return true;
										} else if (mo && me.isMode(me.MODE_EDIT)) {
											if (rec.getId() === mo.get('calendarId')) return true;
											if (rec.get('_profileId') === mo.get('_profileId') && rec.get('_writable')) return true;
										}
										return false;
									}
								}],
								listeners: {
									load: function(s, recs, succ) {
										if (succ && (s.loadCount === 1) && me.isMode(me.MODE_NEW)) {
											var rec = s.getById(me.lref('fldcalendar').getValue());
											if (rec) me.setCalendarDefaults(rec);
										}
									}
								}
							},
							groupField: '_profileDescription',
							colorField: 'color',
							fieldLabel: me.mys.res('event.fld-calendar.lbl'),
							labelAlign: 'right',
							width: 400,
							listeners: {
								select: function(s, rec) {
									me.setCalendarDefaults(rec);
									me.refreshActivities();
									me.refreshCausals();
								}
							}
						})
					]
				}, {
					xtype: 'sotagdisplayfield',
					dock : 'top',
					bind: {
						value: '{foTags}',
						hidden: '{!foHasTags}'
					},
					valueField: 'id',
					displayField: 'name',
					colorField: 'color',
					store: WT.getTagsStore(),
					dummyIcon: 'loading',
					hidden: true,
					hideLabel: true,
					margin: '0 0 5 0'
				},
				me.mys.hasAuditUI() ? {
					xtype: 'statusbar',
					dock: 'bottom',
					items: [
						me.addAct('eventAuditLog', {
							text: null,
							tooltip: WT.res('act-auditLog.lbl'),
							iconCls: 'fas fa-history',
							handler: function() {
								me.mys.openAuditUI(me.getModel().get('eventId'), 'EVENT');
							},
							scope: me
						})
					]
				} : null
			]
		});
		me.callParent(arguments);
		
		me.resourcesStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.SubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupResources', null)
		});
		
		var main, appo, attends, recur, attachs;
		main = {
			xtype: 'wtform',
			modelValidation: true,
			defaults: {
				labelWidth: 60
			},
			items: [{
				xtype: 'wtmeetingurlfield',
				bind: {
					value: '{record.extractedUrl}',
					hidden: '{!foHasMeeting}'
				},
				linkText: me.res('event.meeting.info'),
				hidden: true,
				listeners: {
					copy: function() {
						WT.toast(WT.res('meeting.toast.link.copied'));
					}
				},
				anchor: '100%'
			}, {
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
				sid: me.mys.ID,
				bind: {
					value: '{record.location}',
					hidden: '{foLocationIsMeeting}'
				},
				suggestionContext: 'eventlocation',
				//suggestionContext: 'report_idcalendar', //TODO: verificare nome contesto
				listeners: {
					enterkey: function() {
						me.getAct('saveClose').execute();
					}
				},
				fieldLabel: me.mys.res('event.fld-location.lbl'),
				hidden: true,
				anchor: '100%'
			}, {
				xtype: 'wtmeetingfield',
				bind: {
					value: '{record.location}',
					hidden: '{!foLocationIsMeeting}'
				},
				listeners: {
					copy: function() {
						WT.toast(WT.res('meeting.toast.link.copied'));
					}
				},
				hidden: true,
				fieldLabel: me.mys.res('event.fld-location.lbl'),
				anchor: '100%'
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
						disabled: '{foWasRecurring}'
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
					ui: 'default-toolbar',
					iconCls: 'far fa-clock',
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
						disabled: '{foWasRecurring}'
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
					ui: 'default-toolbar',
					iconCls: 'far fa-clock',
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
					boxLabel: me.res('event.fld-private.lbl')
				}, {
					xtype: 'checkbox',
					bind: '{busy}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.res('event.fld-busy.lbl')
				}]
			}, {
				xtype: 'formseparator',
				hidden: !me.showStatisticFields
			}, WTF.remoteCombo('id', 'desc', {
				reference: 'fldactivity',
				bind: '{record.activityId}',
				autoLoadOnValue: me.showStatisticFields ? true : false,
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
								Sonicle.Data.applyExtraParams(s, {
									profileId: me.getModel().get('_profileId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.res('event.fld-activity.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldmasterdata',
				bind: '{record.masterDataId}',
				autoLoadOnValue: me.showStatisticFields ? true : false,
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
				fieldLabel: me.res('event.fld-masterData.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldstatmasterdata',
				bind: '{record.statMasterDataId}',
				autoLoadOnValue: me.showStatisticFields ? true : false,
				hidden: !me.showStatisticFields,
				store: {
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupStatisticCustomersSuppliers'),
					listeners: {
						beforeload: {
							fn: function(s) {
								Sonicle.Data.applyExtraParams(s, {
									parentMasterDataId: me.getModel().get('masterDataId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.res('event.fld-statMasterData.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldcausal',
				bind: '{record.causalId}',
				autoLoadOnValue: me.showStatisticFields ? true : false,
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
								Sonicle.Data.applyExtraParams(s, {
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
				fieldLabel: me.res('event.fld-causal.lbl'),
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
						header: me.res('event.gp-attendees.notify.lbl'),
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
						header: me.res('event.gp-attendees.recipient.lbl'),
						flex: 1
					}, {
						dataIndex: 'recipientType',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRcptType',
							keepcase: true
						}),
						editor: WTF.localCombo('id', 'desc', {
							store: {
								autoLoad: true,
								type: 'wtcalattendeercpttype'
							}
						}),
						header: me.res('event.gp-attendees.recipientType.lbl'),
						width: 110
					}, {
						dataIndex: 'recipientRole',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRcptRole',
							keepcase: true
						}),
						editor: WTF.localCombo('id', 'desc', {
							store: {
								autoLoad: true,
								type: 'wtcalattendeercptrole'
							}
						}),
						header: me.res('event.gp-attendees.recipientRole.lbl'),
						width: 110
					}, {
						dataIndex: 'responseStatus',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRespStatus',
							keepcase: true
						}),
						editor: WTF.localCombo('id', 'desc', {
							store: {
								autoLoad: true,
								type: 'wtcalattendeerespstatus'
							}
						}),
						header: me.res('event.gp-attendees.responseStatus.lbl'),
						width: 110
					}, {
						xtype: 'actioncolumn',
						draggable: false,
						hideable: false,
						groupable: false,
						align: 'center',
						items: [{
							iconCls: 'far fa-trash-alt',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteAttendeeUI(rec);
							}
						}],
						width: 50
					}],
					plugins: [{
						id: 'cellediting',
						ptype: 'cellediting',
						clicksToEdit: 1
					}],
					listeners: {
						beforeedit: function(s, loc) {
							// Prevent changing field if attendee is a resource!
							if (loc.record.get('recipientType') === 'RES') return false;
						}
					},
					tbar: [
						me.addAct('addAttendee', {
							tooltip: null,
							iconCls: null,
							handler: function() {
								me.addAttendeeUI();
							}
						}),
						me.addAct('addResource', {
							tooltip: null,
							iconCls: null,
							handler: function() {
								me.addResourceUI();
							}
						}),
						{
							xtype: 'splitbutton',
							tooltip: me.res('act-pasteList.lbl'),
							iconCls: 'wt-icon-clipboard-paste',
							handler: function() {
								me.pasteList();
							},
							menu: [
								{
									text: me.res('act-pasteList.lbl'),
									tooltip: me.res('act-pasteList.tip'),
									iconCls: 'wt-icon-clipboard-paste',
									handler: function() {
										me.pasteList();
									}
								}, {
									text: me.res('act-pasteContactsList.lbl'),
									iconCls: 'wt-icon-clipboard-paste',
									handler: function() {
										me.pasteContactsList();
									}
								}
							]
						},
						'->',
						{
							xtype: 'button',
							text: me.mys.res('event.btn-planning.lbl'),
							handler: function() {
								me.lref('tabinvitation').getLayout().setActiveItem('planning');
							}
						}
					],
					border: false
				}, {
					xtype: 'wtcalplanninggrid',
					reference: 'gpplanning',
					itemId: 'planning',
					bind: {
						eventStart: '{record.startDate}',
						eventEnd: '{record.endDate}',
						eventAllDay: '{record.allDay}',
						eventTimezone: '{record.timezone}',
						eventOrganizerProfile: '{record._profileId}'
					},
					serviceId: me.mys.ID,
					serviceAction: 'GeneratePlanningView',
					dayHeaderFmt: WT.getLongDateFmt(),
					legendFreeText: me.res('wtcalplanninggrid.legend.free.txt'),
					legendFreeWorkdayOffText: me.res('wtcalplanninggrid.legend.freeWorkdayOff.txt'),
					legendBusyText: me.res('wtcalplanninggrid.legend.busy.txt'),
					legendUnknownText: me.res('wtcalplanninggrid.legend.unknown.txt'),
					viewOptionsHideWorkdayOffText: me.res('wtcalplanninggrid.viewOptions.hideWorkdayOff.lbl'),
					viewOptionsResolutionText: me.res('wtcalplanninggrid.viewOptions.resolution.lbl'),
					viewOptionsResolution60Text: me.res('wtcalplanninggrid.viewOptions.resolution.60.lbl'),
					viewOptionsResolution30Text: me.res('wtcalplanninggrid.viewOptions.resolution.30.lbl'),
					viewOptionsResolution15Text: me.res('wtcalplanninggrid.viewOptions.resolution.15.lbl'),
					hidePlanningButtonText: me.res('wtcalplanninggrid.btn-hidePlanning.lbl'),
					attendeeHeaderText: me.res('wtcalplanninggrid.attendee.lbl'),
					organizerText: me.res('wtcalplanninggrid.organizer'),
					listeners: {
						activate: function() {
							me.reloadPlanning();
						},
						hideplanning: function() {
							me.lref('tabinvitation').getLayout().setActiveItem('attendees');
						},
						planningchange: function(s, start, end) {
							var mo = me.getModel();
							me.suspendPlanningRefresh++;
							mo.setStart(start);
							mo.setEnd(end);
							Ext.defer(function(){
								me.suspendPlanningRefresh--;
							}, 100);
						}
					},
					border: false
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
				labelWidth: 80
			},
			items: [{
				xtype: 'datefield',
				bind: {
					value: '{recStartDate}'
				},
				startDay: WT.getStartDay(),
				format: WT.getShortDateFmt(),
				width: 110 + 80,
				fieldLabel: WT.res('sorrfield.starts')
			}, {
				xtype: 'sorrfield',
				bind: {
					value: '{record.rrule}',
					startDate: '{recStartDate}'
				},
				startDay: WT.getStartDay(),
				dateFormat: WT.getShortDateFmt(),
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
				},
				fieldLabel: WT.res('sorrfield.repeats')
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
					me.lref('tpnlmain').setActiveItem(s);
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
					reference: 'tpnlmain',
					activeTab: 0,
					deferredRender: false,
					items: [
						appo,
						attends,
						recur,
						attachs,
						{
							xtype: 'wtcfieldseditorpanel',
							reference: 'tabcfields',
							title: me.mys.res('event.cfields.tit'),
							bind: {
								store: '{record.cvalues}',
								fieldsDefs: '{record._cfdefs}'
							},
							serviceId: me.mys.ID,
							mainView: me,
							defaultLabelWidth: 120,
							listeners: {
								prioritize: function(s) {
									me.lref('tpnlmain').setActiveItem(s);
								}
							}
						}
					],
					flex: 1
				}
			]
		});
		
		me.on('viewload', me.onViewLoad);
		me.on('viewclose', me.onViewClose);
		me.on('beforemodelsave', me.onBeforeModelSave, me);
		vm.bind('{record.startDate}', me.onDatesChanged, me);
		vm.bind('{record.endDate}', me.onDatesChanged, me);
		vm.bind('{record.timezone}', me.onDatesChanged, me);
		vm.bind('{record._profileId}', me.onDatesChanged, me);
		vm.bind('{record.masterDataId}', me.onMasterDataChanged, me);
		vm.bind('{foTags}', me.onTagsChanged, me);
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
	
	onDatesChanged: function() {
		var me = this;
		if ((me.suspendPlanningRefresh === 0) && me.isPlanningActive()) me.reloadPlanning();
	},
	
	onMasterDataChanged: function() {
		this.refreshStatMasterData();
		this.refreshCausals();
	},
	
	manageTagsUI: function(selTagIds) {
		var me = this,
				vw = WT.createView(WT.ID, 'view.Tags', {
					swapReturn: true,
					viewCfg: {
						data: {
							selection: selTagIds
						}
					}
				});
		vw.on('viewok', function(s, data) {
			me.getModel().set('tags', Sonicle.String.join('|', data.selection));
		});
		vw.showView();
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
	
	saveEventUI: function() {
		var me = this,
				mo = me.getModel();
		
		if (mo.wasRecurring()) {
			if (mo.isModified('rrule') || mo.isModified('rstart')) {
				me.mys.confirmOnSeries(function(bid) {
					if (bid === 'yes') me._saveEventUI(mo, 'all');
				}, me);
			} else {
				me.mys.confirmOnRecurringFor('save', function(bid, value) {
					if (bid === 'ok') me._saveEventUI(mo, value);
				}, me);
			}
		} else {
			me._saveEventUI(mo, null);
		}
	},
	
	_saveEventUI: function(mo, target) {
		var me = this,
				doFn = function(notify) {
					mo.setExtraParams({
						target: target,
						notify: notify === true
					});
					me.saveView(true);
				};
		
		if (mo.isNotifyable()) {
			me.mys.confirmOnInvitationFor(me.isMode('new') ? 'save' : 'update', function(bid) {
				if (bid === 'yes') {
					doFn(true);
				} else if (bid === 'no') {
					doFn(false);
				}
			});
		} else {
			doFn(null);
		}
	},
	
	deleteEventUI: function() {
		var me = this,
				mo = me.getModel();
		
		if (mo.wasRecurring()) {
			me.mys.confirmOnRecurringFor('delete', function(bid, value) {
				if (bid === 'ok') me._deleteEventUI(mo, value);
			}, me);
		} else {
			WT.confirm(me.mys.res('event.confirm.delete', Ext.String.htmlEncode(mo.get('title'))), function(bid) {
				if (bid === 'yes') me._deleteEventUI(mo, null);
			}, me);
		}
	},
	
	_deleteEventUI: function(mo, target) {
		var me = this,
				doFn = function(notify) {
					me.wait();
					me.mys.deleteEvent(mo.getId(), target, notify, {
						callback: function(success) {
							me.unwait();
							if (success) {
								me.fireEvent('viewsave', me, true, mo);
								me.closeView(false);
							}
						}
					});
				};
		
		if (mo.isNotifyable()) {
			me.mys.confirmOnInvitationFor('update', function(bid) {
				if (bid === 'yes') {
					doFn(true);
				} else if (bid === 'no') {
					doFn(false);
				}
			});
		} else {
			doFn(null);
		}
	},
	
	restoreEventUI: function() {
		var me = this,
				mo = me.getModel();
		
		WT.confirm(me.mys.res('event.recurring.confirm.restore'), function(bid) {
			if (bid === 'yes') {
				me.wait();
				me.mys.restoreEvent(mo.getId(), {
					callback: function(success) {
						me.unwait();
						if (success) {
							me.fireEvent('viewsave', me, true, mo);
							me.closeView(false);
						}
					}
				});
			}
		}, me);
	},
	
	addAttendeeUI: function() {
		var me = this,
			gp = me.lref('tabinvitation.gpattendees'),
			sto = gp.getStore(),
			ed = gp.getPlugin('cellediting');
		
		ed.cancelEdit();
		sto.add(sto.createModel({
			notify: true,
			recipientType: 'IND',
			recipientRole: 'REQ',
			responseStatus: 'NA'
		}));
		ed.startEditByPosition({row: sto.getCount()-1, column: 1});
	},
	
	addResourceUI: function() {
		var me = this;
		me.showResourcePicker();
	},
	
	deleteAttendeeUI: function(rec) {
		this.lref('tabinvitation.gpattendees').getStore().remove(rec);
	},
	
	addMeetingUI: function() {
		var me = this,
				Meeting = Sonicle.webtop.core.view.Meeting;
		
		Meeting.promptForInfo({
			whatAsRoomName: true,
			hideDateTime: true,
			callback: function(ok, values) {
				if (ok) {
					me.wait();
					Meeting.getMeetingLink(values[0], {
						callback: function(success, data) {
							me.unwait();
							if (success) {
								var fmt = Ext.String.format,
										name =  WT.getVar('userDisplayName'),
										mo = me.getModel();
								if (Ext.isEmpty(mo.get('title'))) mo.set('title', fmt(data.embedTexts.subject, name));
								mo.set('location', data.link);
								mo.set('description', Sonicle.String.join('\n', mo.get('description'), fmt(data.embedTexts.unschedDescription, name, data.link)));
							}
						}
					});
				}
			}
		});
	},
	
	isPlanningActive: function() {
		return (this.lref('tabinvitation').getLayout().getActiveItem().getItemId() === 'planning');
	},
	
	reloadPlanning: function() {
		var me = this,
			SoS = Sonicle.String,
			gp = me.lref('tabinvitation.gpplanning'),
			mo = me.getModel(),
			recipients = Sonicle.Data.collectValues(mo.attendees(), 'recipient', null, function(value) {
				return SoS.regexpExecAll(value, SoS.reMatchAnyRFC5322Addresses)[0];
			});
		
		gp.refresh(recipients);
	},
	
	printEvent: function(eventKey) {
		var me = this;
		if(me.getModel().isDirty()) {
			WT.warn(WT.res('warn.print.notsaved'));
		} else {
			me.mys.printEventsDetail([eventKey]);
		}
	},
	
	pasteList: function() {
		var me=this;
		
		WT.prompt('',{
			title: me.mys.res("act-pasteList.tit"),
			fn: function(btn,text) {
				if (btn==='ok') {
					me.loadValues(text);
				}
			},
			scope: me,
			width: 400,
			multiline: 200,
			value: ''
		});
	},
	
	pasteContactsList: function() {
		var me = this;
		WT.confirm(me.mys.res('confirmBox.listChoose.lbl'), function(bid, value) {
			if (bid === 'ok') {
				var conSvc = WT.getServiceApi('com.sonicle.webtop.contacts');
				conSvc.expandRecipientsList({
						address: value
					}, {
						callback: function(success, json) {
							if (success) {
								var data = json.data,
										emails = '', i;
								for (i=0; i<data.length; i++) {
									emails += data[i] + '\n';
								}
								this.loadValues(emails);
							}
						},
						scope: me
				});
			}
		}, me, {
			buttons: Ext.Msg.OKCANCEL,
			title: me.mys.res('act-pasteContactsList.confirm.tit'),
			instClass: 'Sonicle.webtop.mail.ux.ChooseListConfirmBox'
		});
	},
	
    loadValues: function(v) {
        var me=this,
			g=me.lref('tabinvitation.gpattendees'),
			s=g.store,
			lines = v.split(/\r\n|\r|\n/g);
			
        for(var i=0;i<lines.length;++i) {
            var email=lines[i].trim();
            if (email.length>0) {
				s.add(s.createModel({
					notify: true,
					recipient: email,
					recipientType: 'IND',
					recipientRole: 'REQ',
					responseStatus: 'NA'
				}));					
            }
        }
    },
	
	privates: {
		setCalendarDefaults: function(cal) {
			var me = this, mo = this.getModel(), od = me.opts.data;
			if (mo) {
				//set defaults only on non prepared fields
				if (!Ext.isDefined(od.isPrivate)) mo.set('isPrivate', cal.get('evtPrivate'));
				if (!Ext.isDefined(od.busy)) mo.set('busy', cal.get('evtBusy'));
				if (!Ext.isDefined(od.reminder)) mo.set('reminder', cal.get('evtReminder'));
			}
		},
		
		onViewLoad: function(s, success) {
			var me = this,
				SoD = Sonicle.Data,
				mo = me.getModel();
			// Overrides autogenerated string id by extjs...
			// It avoids type conversion problems server-side!
			//if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
			
			if (me.showStatisticFields) {
				SoD.loadWithExtraParams(me.lref('fldactivity').getStore());
				SoD.loadWithExtraParams(me.lref('fldmasterdata').getStore());
				SoD.loadWithExtraParams(me.lref('fldstatmasterdata').getStore());
				SoD.loadWithExtraParams(me.lref('fldcausal').getStore());
			}

			if (me.isMode(me.MODE_NEW)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setDisabled(true);
				me.getAct('restore').setDisabled(true);
				me.getAct('tags').setDisabled(false);
				me.lref('fldcalendar').setReadOnly(false);
				me.lref('tabrecurrence').setDisabled(false);
				if (me.mys.hasAuditUI()) me.getAct('eventAuditLog').setDisabled(true);
				WTA.util.CustomFields.reloadCustomFields((me.opts.data || {}).tags, me.opts.cfData, {
					serviceId: me.mys.ID,
					model: me.getModel(),
					idField: 'eventId',
					cfPanel: me.lref('tabcfields')
				});
			} else if (me.isMode(me.MODE_VIEW)) {
				me.getAct('saveClose').setDisabled(true);
				me.getAct('delete').setDisabled(true);
				me.getAct('restore').setDisabled(true);
				me.getAct('tags').setDisabled(true);
				me.lref('fldcalendar').setReadOnly(true);
				me.lref('tabrecurrence').setDisabled(false);
				if (me.mys.hasAuditUI()) me.getAct('eventAuditLog').setDisabled(false);
			} else if (me.isMode(me.MODE_EDIT)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setDisabled(false);
				me.getAct('restore').setDisabled(!mo.wasBroken());
				me.getAct('tags').setDisabled(false);
				me.lref('fldcalendar').setReadOnly(false);
				me.lref('tabrecurrence').setDisabled(mo.wasBroken());
				if (me.mys.hasAuditUI()) me.getAct('eventAuditLog').setDisabled(false);
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
		
		onBeforeModelSave: function(s) {
			var me = this,
				cp = me.lref('tabcfields');
			if (!cp.isValid()) {
				me.lref('tpnlmain').getLayout().setActiveItem(cp);
				return false;
			}
		},
		
		onTagsChanged: function(nv, ov) {
			var me = this;
			if (ov && Sonicle.String.difference(nv, ov).length > 0) { // Make sure that there are really differences!
				WTA.util.CustomFields.reloadCustomFields(nv, false, {
					serviceId: me.mys.ID,
					model: me.getModel(),
					idField: 'eventId',
					cfPanel: me.lref('tabcfields')
				});
			}
		},
		
		showResourcePicker: function() {
			var me = this,
				SoS = Sonicle.String,
				gp = me.lref('tabinvitation.gpattendees'),
				usedResources = Sonicle.Data.collectValues(gp.getStore(), 'recipient',
					function(rec) {
						return rec.get('recipientType') === 'RES';
					},
					function(value) {
						return SoS.regexpExecAll(value, SoS.reMatchAnyRFC5322Addresses)[0];
					}
				);
			me.resourcePicker = me.createResourcePicker();
			me.resourcePicker.getComponent(0).setSkipValues(usedResources);
			me.resourcePicker.show();
		},
		
		createResourcePicker: function() {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: WT.res(me.mys.ID, 'event.act-addResource.lbl'),
				height: 350,
				items: [
					{
						xtype: 'solistpicker',
						store: {
							xclass: 'Ext.data.ChainedStore',
							source: me.resourcesStore
						},
						valueField: 'address',
						displayField:'displayName',
						searchField: 'search',
						emptyText: WT.res('grid.emp'),
						searchText: WT.res('textfield.search.emp'),
						selectedText: WT.res('grid.selected.lbl'),
						okText: WT.res('act-ok.lbl'),
						cancelText: WT.res('act-cancel.lbl'),
						allowMultiSelection: true,
						listeners: {
							cancelclick: function() {
								if (me.resourcePicker) me.resourcePicker.close();
							}
						},
						handler: me.onResourcePickerPick,
						scope: me
					}
				]
			});
		},
		
		onResourcePickerPick: function(s, values, recs, button) {
			var me = this,
				gp = me.lref('tabinvitation.gpattendees'),
				sto = gp.getStore(),
				ed = gp.getPlugin('cellediting');

			ed.cancelEdit();
			Ext.iterate(recs, function(rec) {
				var address = rec.get('address'),
					dn = rec.get('displayName');
				sto.add(sto.createModel({
					notify: true,
					recipient: Ext.isEmpty(address) ? dn : (dn + ' <' + address + '>'),
					recipientType: 'RES',
					recipientRole: 'REQ',
					responseStatus: 'NA'
				}));
			});
			//ed.startEditByPosition({row: sto.getCount()-1, column: 1});
			
			me.resourcePicker.close();
			me.resourcePicker = null;
		}
	}
});
