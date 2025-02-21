/* 
 * Copyright (C) 2024 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2024 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.calendar.view.Event', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.Data',
		'Sonicle.String',
		'Sonicle.VMUtils',
		'Sonicle.form.FieldSection',
		'Sonicle.form.FieldHGroup',
		'Sonicle.form.field.ComboBox',
		'Sonicle.form.field.Display',
		'Sonicle.form.field.rr.Repeat',
		'Sonicle.form.field.TagDisplay',
		'Sonicle.plugin.DropMask',
		'WTA.util.CustomFields',
		'WTA.ux.UploadButton',
		'WTA.ux.field.Attachments',
		'WTA.ux.field.Meeting',
		'WTA.ux.field.MeetingUrl',
		'WTA.ux.field.RecipientSuggestCombo',
		'WTA.ux.panel.CustomFieldsEditor',
		'WTA.model.SubjectLkp',
		'WTA.store.Timezone',
		'Sonicle.webtop.calendar.ux.PlanningGrid',
		'Sonicle.webtop.calendar.model.CalendarLkp',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.store.Reminder',
		'Sonicle.webtop.calendar.store.AttendeeRcptRole',
		'Sonicle.webtop.calendar.store.AttendeeRcptType',
		'Sonicle.webtop.calendar.store.AttendeeRespStatus'
	],
	uses: [
		'Sonicle.ClipboardMgr',
		'Sonicle.URLMgr',
		'Sonicle.webtop.core.view.Tags',
		'Sonicle.webtop.core.view.Meeting',
		'Sonicle.webtop.calendar.ux.ChooseListConfirmBox',
		'Sonicle.webtop.calendar.view.PlanningEditor',
		'Sonicle.webtop.calendar.view.RecurrenceEditor'
	],
	
	dockableConfig: {
		title: '{event.tit}',
		iconCls: 'wtcal-icon-event',
		width: 700,
		height: 600
	},
	actionsResPrefix: 'event',
	confirm: 'yn',
	fieldTitle: 'title',
	modelName: 'Sonicle.webtop.calendar.model.Event',
	
	suspendCheckboxChange: true,
	
	viewModel: {
		data: {
			hidden: {
				flddescription: false
			}
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foIsView: WTF.foIsEqual('_mode', null, me.MODE_VIEW),
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
			foReminder: WTF.foFieldTwoWay('record', 'reminder', function(v) {
					return !Ext.isEmpty(v);
				}, function(v) {
					return (v === true) ? 5 : null;
			}), 
			foHasDescription: WTF.foIsEmpty('record', 'description', true),
			foHasReminder: WTF.foIsEmpty('record', 'reminder', true),
			foHasRecurrence: WTF.foIsEmpty('record', 'rrule', true),
			foRRuleString: WTF.foFieldTwoWay('record', 'rruleString', function(v, rec) {
					return v;
				}, function(v, rec) {
					var ret = Sonicle.form.field.rr.Recurrence.splitRRuleString(v);
					rec.set('rrule', ret.rrule, {convert: false});
					rec.setRecurStart(ret.start, {convert: false});
					return v;
			}),
			foHumanReadableRRule: WTF.foGetFn('record', 'rruleString', function(v) {
				return WT.toHumanReadableRRule(v);
			}),
			foWasRecurring: WTF.foIsEqual('record', '_recurringInfo', 'recurring'),
			foTags: WTF.foFieldTwoWay('record', 'tags', function(v) {
					return Sonicle.String.split(v, '|');
				}, function(v) {
					return Sonicle.String.join('|', v);
			}),
			foHasTags: WTF.foIsEmpty('record', 'tags', true),
			foLocationIsMeeting: WTF.foGetFn('record', 'location', function(val) {
				return WT.isMeetingUrl(val);
			}),
			foHasMeeting: WTF.foIsEmpty('record', 'meetingUrl', true),
			foHasAttendees: WTF.foAssociationIsEmpty('record', 'attendees', true),
			foHasAttendeesCount: WTF.foAssociationCount('record', 'attendees'),
			foHasAttachments: WTF.foAssociationIsEmpty('record', 'attachments', true)
		});
	},
	
	initComponent: function() {
		var me = this,
			vm = me.getViewModel();
		
		me.plugins = Sonicle.Utils.mergePlugins(me.plugins, [
			{
				ptype: 'sodropmask',
				text: WT.res('sofiledrop.text'),
				monitorExtDrag: false,
				shouldSkipMasking: function(dragOp) {
					return !Sonicle.plugin.DropMask.isBrowserFileDrag(dragOp);
				}
			}
		]);
		me.callParent(arguments);
		
		me.resourcesStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.SubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupResources', null)
		});
		
		me.addRef('cxmAttendee', Ext.create({
			xtype: 'menu',
			items: [
				{
					iconCls: 'wt-icon-trash',
					text: WT.res('act-remove.lbl'),
					handler: function(s, e) {
						var rec = e.menuData.rec;
						if (rec) me.deleteAttendeeUI(rec);
					}
				},
				'-',
				{
					xtype: 'somenuheader',
					text: WT.res('act-edit.lbl')
				}, {
					itemId: 'recipientRole',
					text: me.res('event.mni-recipientRole.lbl'),
					menu: {
						xtype: 'sostoremenu',
						store: {
							autoLoad: true,
							type: 'wtcalattendeercptrole'
						},
						textField: 'desc',
						listeners: {
							click: function(s, itm, e) {
								var rec = e.menuData.rec;
								if (rec && !rec.isResource()) rec.set('recipientRole', itm.getItemId());
							}
						}
					}
				}, {
					itemId: 'responseStatus',
					text: me.res('event.mni-responseStatus.lbl'),
					menu: {
						xtype: 'sostoremenu',
						store: {
							autoLoad: true,
							type: 'wtcalattendeerespstatus'
						},
						textField: 'desc',
						listeners: {
							click: function(s, itm, e) {
								var rec = e.menuData.rec;
								if (rec && !rec.isResource()) rec.set('responseStatus', itm.getItemId());
							}
						}
					}
				}
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.rec;
					s.getComponent('recipientRole').setDisabled(rec.isResource());
					s.getComponent('responseStatus').setDisabled(rec.isResource());
				}
			}
		}));
		
		me.addRef('cxmAttachment', Ext.create({
			xtype: 'menu',
			items: [
				{
					iconCls: 'wt-icon-open',
					text: WT.res('act-open.lbl'),
					handler: function(s, e) {
						var rec = e.menuData.rec;
						if (rec) me.openAttachmentUI(rec, false);
					}
				}, {
					iconCls: 'wt-icon-download',
					text: WT.res('act-download.lbl'),
					handler: function(s, e) {
						var rec = e.menuData.rec;
						if (rec) me.openAttachmentUI(rec, true);
					}
				}
			]
		}));
		
		me.add({
			region: 'center',
			xtype: 'wttabpanel',
			reference: 'tpnlmain',
			activeTab: 0,
			deferredRender: false,
			tabBar: {hidden: true},
			items: [
				{
					xtype: 'wtfieldspanel',
					title: me.res('event.main.tit'),
					paddingTop: true,
					paddingSides: true,
					scrollable: true,
					modelValidation: true,
					items: me.prepareMainFields()
				}, {
					xtype: 'wtcfieldseditorpanel',
					reference: 'tabcfields',
					title: me.res('event.cfields.tit'),
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
			]
		});
		
		me.on('viewload', me.onViewLoad);
		me.on('viewclose', me.onViewClose);
		me.on('beforemodelsave', me.onBeforeModelSave, me);
		vm.bind('{foTags}', me.onTagsChanged, me);
	},
	
	initTBar: function() {
		var me = this,
			SoU = Sonicle.Utils;
		
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'top', [
			me.createTopToolbar1Cfg(me.prepareTopToolbarItems())
		]);
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'bottom', [
			me.createStatusbarCfg()
		]);
	},
	
	restoreEventUI: function() {
		var me = this,
			mo = me.getModel();
		
		WT.confirm(me.res('event.recurring.confirm.restore'), function(bid) {
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
								if (mo.isFieldEmpty('title')) mo.set('title', fmt(data.embedTexts.subject, name));
								mo.set('location', data.link);
								mo.set('description', Sonicle.String.join('\n', mo.get('description'), fmt(data.embedTexts.unschedDescription, name, data.link)));
							}
						}
					});
				}
			}
		});
	},
	
	saveEventUI: function() {
		var me = this,
			mo = me.getModel();
		
		if (mo.wasRecurring()) {
			if (mo.isModified('rrule') || mo.isModified('rstart')) {
				me.mys.confirmOnSeries(function(bid) {
					if (bid === 'yes') me.doSaveEvent(mo, 'all');
				}, me);
			} else {
				me.mys.confirmOnRecurringFor('save', function(bid, value) {
					if (bid === 'ok') me.doSaveEvent(mo, value);
				}, me);
			}
		} else {
			me.doSaveEvent(mo, null);
		}
	},
	
	deleteEventUI: function() {
		var me = this,
				mo = me.getModel();
		
		if (mo.wasRecurring()) {
			me.mys.confirmOnRecurringFor('delete', function(bid, value) {
				if (bid === 'ok') me.doDeleteEvent(mo, value);
			}, me);
		} else {
			WT.confirm(me.mys.res('event.confirm.delete', mo.get('title')), function(bid) {
				if (bid === 'yes') me.doDeleteEvent(mo, null);
			}, me);
		}
	},
	
	printEventUI: function(eventId) {
		var me = this;
		if (me.getModel().isDirty()) {
			WT.warn(WT.res('warn.print.notsaved'));
		} else {
			me.mys.printEventsDetail([eventId]);
		}
	},
	
	importAttendeesUI: function(type) {
		var me = this;
		if (type === 'raw') {
			WT.prompt('', {
				title: me.res('event.btn-importAttendeesPaste.lbl'),
				fn: function(bid, value) {
					if (bid === 'ok') me.doImportTextAsAttendees(value);
				},
				scope: me,
				multiline: 200,
				value: '',
				width: 400
			});
			
		} else if (type === 'list') {
			WT.confirm(me.res('chooseListConfirmBox.msg'), function(bid, value) {
				if (bid === 'ok') {
					var conSvc = WT.getServiceApi('com.sonicle.webtop.contacts');
					conSvc.expandRecipientsList({
							address: value
						}, {
							callback: function(success, json) {
								if (success) {
									me.doImportListAsAttendees(json.data);
								}
							},
							scope: me
					});
				}
			}, me, {
				buttons: Ext.Msg.OKCANCEL,
				title: me.res('event.btn-importAttendeesList.lbl'),
				instClass: 'Sonicle.webtop.calendar.ux.ChooseListConfirmBox'
			});
		}
	},
	
	addResourceUI: function() {
		var me = this;
		me.showResourcePicker();
	},
	
	deleteAttendeeUI: function(rec) {
		this.getModel().attendees().remove(rec);
	},
	
	openContactUI: function(rec) {
		var me = this;
		if (!Ext.isEmpty(rec.get("recipient"))) {
			WT.ajaxReq(me.mys.ID, "OpenContact", {
				params: {
					recipient: rec.get("recipient")
				},
				callback: function(success,json) {
					if (success) {
						if (json.data.length>0) {
							me.openContactFromResult(json.data);
						}
						else WT.error(me.mys.res('openContact.notfound'));
					} else {
						WT.error(json.message);
					}
				}
			});
		} else {
			WT.error(me.mys.res('openContact.empty'))
		}
	},
	
	openContactFromResult: function(data) {
		var me = this,
			capi=WT.getServiceApi("com.sonicle.webtop.contacts");
		if (!capi) return;

		if (data.length == 1) {
			capi.openContact(data[0].id);
		} else {
			var picker = Ext.create({
				xtype: 'wtpickerwindow',
				title: me.mys.res('openContactFromResult.tit'),
				height: 350,
				width: 600,
				items: [
					{
						xtype: 'solistpicker',
						store: {
							type: 'json',
							fields: [
								{name: 'id', type: 'string'},
								{name: 'recipient', type: 'string'},
								{name: 'source', type: 'string'}
							],
							data: data
						},
						valueField: 'id',
						columns: [
							{ dataIndex: 'recipient', flex: 1 },
							{ dataIndex: 'source', flex: 1 }
						],
						selectedText: WT.res('grid.selected.lbl'),
						okText: WT.res('act-ok.lbl'),
						cancelText: WT.res('act-cancel.lbl'),
						allowMultiSelection: false,
						listeners: {
							cancelclick: function() {
								if (picker) picker.close();
							}
						},
						handler: function(s, values, recs, button) {
							capi.openContact(values[0]);
							if (picker) picker.close();
						},
						scope: me
					}
				]
			});
			picker.show();
		}
	},	
	
	privates: {
		onSaveCloseHandler: function() {
			this.saveEventUI();
		},
		
		prepareTopToolbarItems: function() {
			var me = this;
			return [
				WTF.lookupCombo('calendarId', '_label', {
					xtype: 'socombo',
					reference: 'fldcalendar',
					bind: {
						value: '{record.calendarId}',
						readOnly: '{foIsView}'
					},
					listConfig: {
						displayField: 'name',
						groupCls: 'wt-text-off wt-theme-text-color-off'
					},
					swatchGeometry: 'circle',
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
					width: 300,
					listeners: {
						select: function(s, rec) {
							me.setCalendarDefaults(rec);
							me.refreshActivities();
							me.refreshCausals();
						}
					}
				}),
				me.addAct('tags', {
					text: null,
					tooltip: me.res('act-manageTags.lbl'),
					iconCls: 'wt-icon-tags',
					handler: function() {
						me.manageTagsUI(Sonicle.String.split(me.getModel().get('tags'), '|'));
					}
				}),
				{
					xtype: 'wtuploadbutton',
					bind: {
						hidden: '{foIsView}',
						disabled: '{foIsView}'
					},
					tooltip: WT.res('act-attach.lbl'),
					iconCls: 'wt-icon-attach',
					sid: me.mys.ID,
					uploadContext: 'EventAttachment',
					uploadTag: WT.uiid(me.getId()),
					dropElement: me.getId(),
					listeners: {
						beforeupload: function(up, file) {
							me.wait(file.name, true);
						},
						uploaderror: function(up, file, cause, json) {
							me.unwait();
							WTA.mixin.HasUpload.handleUploadError(up, file, cause);
						},
						uploadprogress: function(up, file) {
							me.wait(Ext.String.format('{0}: {1}%', file.name, file.percent), true);
						},
						fileuploaded: function(up, file, resp) {
							me.unwait();
							var sto = me.getModel().attachments();
							sto.add(sto.createModel({
								name: file.name,
								size: file.size,
								_uplId: resp.data.uploadId
							}));
						}
					}
				},
				me.addAct('restore', {
					text: null,
					tooltip: WT.res('act-restore.lbl'),
					iconCls: 'wtcal-icon-rejoinSeries',
					hidden: true,
					handler: function() {
						me.restoreEventUI();
					}
				}),
				me.addAct('print', {
					text: null,
					tooltip: WT.res('act-print.lbl'),
					iconCls: 'wt-icon-print',
					handler: function() {
						//TODO: aggiungere l'azione 'salva' permettendo così la stampa senza chiudere la form
						me.printEventUI(me.getModel().getId());
					}
				}),
				me.addAct('delete', {
					text: null,
					tooltip: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete',
					hidden: true,
					handler: function() {
						me.deleteEventUI();
					}
				}),
				'->',
				{
					xtype: 'checkbox',
					bind: '{isPrivate}',
					hideEmptyLabel: true,
					boxLabel: me.res('event.fld-private.lbl')
				}, {
					xtype: 'checkbox',
					bind: '{busy}',
					hideEmptyLabel: true,
					boxLabel: me.res('event.fld-busy.lbl')
				}
			];
		},
		
		prepareMainFields: function() {
			var me = this;
			return [
				me.createTagsFieldCfg(),	
				{
					xtype: 'wtmeetingurlfield',
					bind: {
						value: '{record.meetingUrl}',
						hidden: '{!foHasMeeting}'
					},
					linkText: me.res('event.meeting.info'),
					hidden: true,
					listeners: {
						copy: function() {
							WT.toast(WT.res('meeting.toast.link.copied'));
						}
					},
					hideLabel: true,
					cls: 'wtcal-event-info',
					anchor: '100%'
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventTitle',
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								{
									xtype: 'wtsuggestcombo',
									reference: 'fldtitle',
									bind: '{record.title}',
									sid: me.mys.ID,
									suggestionContext: 'eventtitle',
									listeners: {
										enterkey: function() {
											me.getAct('saveClose').execute();
										}
									},
									emptyText: me.res('event.fld-title.emp'),
									flex: 1
								}, {
									xtype: 'sohspacer'
								}, {
									xtype: 'sotogglebutton',
									bind: {
										pressed: '{!hidden.flddescription}'
									},
									ui: 'default-toolbar',
									iconCls: 'wtcal-icon-showDescription',
									tooltip: me.res('event.btn-showDescription.tip'),
									toggleHandler: function(s, state) {
										me.showHideField('flddescription', !state);
									}
								}
							],
							fieldLabel: me.res('event.fld-title.lbl')
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventDescription',
					bind: {
						hidden: '{hidden.flddescription}'
					},
					hidden: true,
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								me.createDescriptionFieldCfg({
									minHeight: 100,
									flex: 1
								})
							]		
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventDateTime',
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								{
									xtype: 'datefield',
									bind: {
										value: '{startDate}',
										disabled: '{foWasRecurring}'
									},
									startDay: WT.getStartDay(),
									format: WT.getShortDateFmt(),
									fieldLabel: me.res('event.fld-start.lbl'),
									width: 130
								}, {
									xtype: 'sohspacer',
									ui: 'small'
								}, {
									xtype: 'timefield',
									bind: {
										value: '{startTime}',
										disabled: '{fldallDay.checked}'
									},
									format: WT.getShortTimeFmt(),
									width: 85
								}, {
									xtype: 'sohspacer'
								}, {
									xtype: 'button',
									ui: 'default-toolbar',
									iconCls: 'fas fa-stopwatch',
									tooltip: me.res('event.btn-now.tip'),
									bind: {
										disabled: '{fldallDay.checked}'
									},
									handler: function() {
										me.getModel().setStartTime(new Date());
									}
								}, {
									xtype: 'sohspacer',
									ui: 'small'
								}, {
									xtype: 'datefield',
									bind: {
										value: '{endDate}',
										disabled: '{foWasRecurring}'
									},
									startDay: WT.getStartDay(),
									format: WT.getShortDateFmt(),
									fieldLabel: me.res('event.fld-end.lbl'),
									width: 130
								}, {
									xtype: 'sohspacer',
									ui: 'small'
								}, {
									xtype: 'timefield',
									bind: {
										value: '{endTime}',
										disabled: '{fldallDay.checked}'
									},
									format: WT.getShortTimeFmt(),
									width: 85
								}, {
									xtype: 'sohspacer'
								}, {
									xtype: 'button',
									ui: 'default-toolbar',
									iconCls: 'fas fa-stopwatch',
									tooltip: me.res('event.btn-now.tip'),
									bind: {
										disabled: '{fldallDay.checked}'
									},
									handler: function() {
										me.getModel().setEndTime(new Date());
									}
								}, {
									xtype: 'sohspacer',
									ui: 'medium'
								}, {
									xtype: 'checkbox',
									reference: 'fldallDay',
									bind: '{allDay}',
									hideEmptyLabel: true,
									boxLabel: me.res('event.fld-allDay.lbl'),
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
								}
							]
						}, {
							xtype: 'sofieldhgroup',
							items: [
								WTF.localCombo('id', 'desc', {
									xtype: 'socombo',
									bind: '{record.timezone}',
									store: Ext.create('WTA.store.Timezone', {
										autoLoad: true
									}),
									staticIconCls: 'fas fa-earth-americas',
									width: 270
								}),
								{
									xtype: 'sohspacer',
									ui: 'medium',
									cls: 'wtcal-spacer-event-rrule'
								}, {
									xtype: 'sorrrepeatfield',
									bind: {
										value: '{foRRuleString}',
										defaultStartDate: '{startDate}'
									},
									noneText: WT.res('sorrrepeatfield.none'),
									dailyText: WT.res('sorrrepeatfield.daily'),
									weeklyText: WT.res('sorrrepeatfield.weekly'),
									monthlyText: WT.res('sorrrepeatfield.monthly'),
									yearlyText: WT.res('sorrrepeatfield.yearly'),
									advancedText: WT.res('sorrrepeatfield.advanced'),
									width: 240
								}, {
									xtype: 'sohspacer',
									ui: 'medium',
									cls: 'wtcal-spacer-event-reminder'
								}, {
									xtype: 'checkbox',
									bind: '{foHasReminder}',
									hideEmptyLabel: true,
									boxLabel: me.res('event.fld-reminder.lbl'),
									handler: function(s, nv) {
										var mo = me.getModel(),
											reminder = mo.get('reminder');
										if (nv === true && Ext.isEmpty(reminder)) {
											mo.set('reminder', 5);
										} else if (nv === false && !Ext.isEmpty(reminder)) {
											mo.set('reminder', null);
										}
									}
								}
							]
						}, {
							xtype: 'so-displayfield',
							bind: {
								value: '{foHumanReadableRRule}',
								hidden: '{!foHasRecurrence}'
							},
							hidden: true,
							enableClickEvents: true,
							tooltip: me.res('event.btn-editRecurrence.tip'),
							handler: function(s) {
								me.editRecurrence();
							}
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventReminder',
					bind: {
						hidden: '{!foHasReminder}'
					},
					hidden: true,
					items: [
						WTF.localCombo('id', 'desc', {
							bind: '{record.reminder}',
							store: Ext.create('Sonicle.webtop.calendar.store.Reminder', {
								autoLoad: true
							}),
							triggers: {
								clear: WTF.clearTrigger()
							},
							fieldLabel: me.res('event.fld-reminder.lbl')
						})
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventAttendees',
					items: [
						{
							xtype: 'sofieldhgroup',
							bind: {
								fieldLabel: me.res('event.fld-attendees.lbl', '{foHasAttendeesCount}')
							},
							items: [
								{
									xtype: 'wtrcptsuggestcombo',
									listConfig: {
										width: 350,
										minWidth: 350
									},
									emptyText: me.res('event.fld-attendees.emp'),
									triggers: {
										clear: WTF.clearTrigger()
									},
									listeners: {
										select: function(s, rec) {
											me.doImportRecipientsAsAttendees([rec.get('description')]);
											s.setValue(null);
										},
										enterkeypress: function(s, e, value) {
											me.doImportRecipientsAsAttendees([value]);
											s.setValue(null);
										}
									},
									flex: 1
								}, {
									xtype: 'sohspacer',
									ui: 'small',
									cls: 'wtcal-spacer-event-importattendee'
								}, {
									xtype: 'button',
									ui: '{secondary|toolbar}',
									tooltip: me.res('event.btn-importAttendees.tip'),
									iconCls: 'wtcal-icon-importAttendees',
									arrowVisible: false,
									menu: [
										{
											text: me.res('event.btn-importAttendeesPaste.lbl'),
											tooltip: me.res('event.btn-importAttendeesPaste.tip'),
											iconCls: 'wtcal-icon-importAttendeesPaste',
											handler: function() {
												me.importAttendeesUI('raw');
											}
										}, {
											text: me.res('event.btn-importAttendeesList.lbl'),
											iconCls: 'wtcal-icon-importAttendeesList',
											handler: function() {
												me.importAttendeesUI('list');
											}
										}
									]
								}, {
									xtype: 'sohspacer',
									cls: 'wtcal-spacer-event-addresource'
								}, {
									xtype: 'button',
									ui: '{secondary|toolbar}',
									text: me.res('event.btn-addResource.lbl'),
									iconCls: 'wtcal-icon-addResource',
									handler: function() {
										me.addResourceUI();
									}
								}
							]
						}
					]
				}, {
					xtype: 'sofieldsection',
					bind: {
						hidden: '{!foHasAttendees}'
					},
					hidden: true,
					items: [
						me.createAttendeesGridCfg({
							reference: 'gpattendees',
							maxHeight: 150
						})
					]
				}, {
					xtype: 'sofieldsection',
					stretchWidth: false,
					bind: {
						hidden: '{!foHasAttendees}'
					},
					hidden: true,
					items: [
						{
							xtype: 'button',
							ui: '{tertiary}',
							text: me.res('event.btn-showAvailability.lbl'),
							handler: function() {
								me.showAvailability();
							}
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventLocation',
					bind: {
						hidden: '{foLocationIsMeeting}'
					},
					hidden: true,
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								{
									//FIXME: check suggestcombo on delete all field
									xtype: 'textfield',
									bind: '{record.location}',
									sid: me.mys.ID,
									suggestionContext: 'eventlocation',
									flex: 1
								}, {
									xtype: 'sohspacer'
								}, {
									xtype: 'button',
									ui: 'default-toolbar',
									iconCls: 'wtcal-icon-addMeeting',
									tooltip: WT.res(WT.ID, 'act-addMeeting.lbl', WT.getMeetingConfig().name),
									disabled: Ext.isEmpty(WT.getMeetingProvider()) || !WT.isPermitted(WT.ID, 'MEETING', 'CREATE'),
									handler: function() {
										me.addMeetingUI();
									}
								}
							],
							fieldLabel: me.res('event.fld-location.lbl')
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventLocation',
					bind: {
						hidden: '{!foLocationIsMeeting}'
					},
					hidden: true,
					items: [
						{
							xtype: 'textfield',
							bind: '{record.location}',
							triggers: {
								clear: WTF.clearTrigger()
							},
							fieldLabel: me.res('event.fld-location.lbl')
						}, {
							xtype: 'sofieldhgroup',
							items: [
								{
									xtype: 'button',
									ui: '{secondary}',
									iconCls: 'fas fa-clone',
									tooltip: me.res('event.btn-copyMeeting.tip'),
									handler: function() {
										var location = me.getModel().get('location');
										if (!Ext.isEmpty(location)) {
											Sonicle.ClipboardMgr.copy(location);
											WT.toast(me.res('event.toast.meetinglink.copied'));
										}
									}
								}, {
									xtype: 'sohspacer',
									ui: 'small'
								}, {
									xtype: 'button',
									ui: '{secondary}',
									iconCls: 'fas fa-arrow-up-right-from-square',
									text: me.res('event.btn-goToMeeting.lbl'),
									handler: function() {
										var location = me.getModel().get('location');
										if (Sonicle.String.startsWith(location, 'http', true)) {
											Sonicle.URLMgr.open(location, true);
										}
									}
								}
							]
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventAttachments',
					bind: {
						hidden: '{!foHasAttachments}'
					},
					hidden: true,
					items: [
						{
							xtype: 'wtattachmentsfield',
							bind: {
								itemsStore: '{record.attachments}'
							},
							itemClickHandler: function(s, rec, e) {
								Sonicle.Utils.showContextMenu(e, me.getRef('cxmAttachment'), {rec: rec});
							},
							fieldLabel: me.res('event.fld-attachments.lbl')
						}
					]
				}
			];
		},
		
		createTagsFieldCfg: function(cfg) {
			return Ext.apply({
				xtype: 'sotagdisplayfield',
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
				hideLabel: true
			}, cfg);
		},
		
		createDescriptionFieldCfg: function(cfg) {
			return Ext.apply({
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: this.res('event.fld-description.lbl'),
				resizable: true,
				smartResize: true
			}, cfg);
		},
		
		createAttendeesGridCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'gridpanel',
				bind: {
					store: '{record.attendees}'
				},
				border: true,
				rowLines: false,
				hideHeaders: true,
				columns: [
					{
						xtype: 'soiconcolumn',
						dataIndex: 'responseStatus',
						getIconCls: function(v) {
							return 'wtcal-icon-attendeeResponse-' + v;
						},
						getTooltip: function(v, rec) {
							return me.res('store.attendeeRespStatus.' + v);
						},
						hideText: false,
						getText: function(v, rec) {
							return Sonicle.String.htmlEncode(rec.get('recipient'));
						},
						flex: 1
					}, {
						dataIndex: 'recipientRole',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRcptRole',
							keepcase: true
						}),
						width: 110
					},{
						dataIndex: 'recipientType',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRcptType',
							keepcase: true
						}),
						width: 110
					}, {
						dataIndex: 'notify',
						xtype: 'checkcolumn',
						cls: 'wtcal-attendees-notify-checkcolumn',
						tooltip: me.res('event.gp-attendees.notify.tip'),
						checkedTooltip: me.res('event.gp-attendees.notify.checked.tip'),
						listeners: {
							beforecheckchange: function(s, ridx, checked, rec) {
								if (rec.isResource()) return false;
							}
						},
						width: 30
					}, {
						xtype: 'soactioncolumn',
						items: [
							{
								iconCls: 'wt-glyph-contact',
								tooltip: me.mys.res('openContact.tip'),
								handler: function(g, ridx) {
									var rec = g.getStore().getAt(ridx);
									me.openContactUI(rec);
								}
							}, {
								iconCls: 'wt-glyph-menu-kebab',
								handler: function(g, ridx, cidx, itm, e, node, row) {
									var rec = g.getStore().getAt(ridx);
									Sonicle.Utils.showContextMenu(e, me.getRef('cxmAttendee'), {rec: rec});
								}
							}
						]
					}
				]
			}, cfg);
		},
		
		createStatusbarCfg: function() {
			var me = this;
			return me.mys.hasAuditUI() ? {
				xtype: 'statusbar',
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
			} : null;
		},
		
		setCalendarDefaults: function(cal) {
			var mo = this.getModel();
			if (mo) {
				mo.set({
					isPrivate: cal.get('evtPrivate'),
					busy: cal.get('evtBusy'),
					reminder: cal.get('evtReminder')
				});
			}
		},
		
		showHideField: function(vmField, hidden) {
			this.getVM().set('hidden.'+vmField, hidden);
		},
		
		showField: function(vmField, focusRef) {
			this.showHideField(vmField, false);
			if (focusRef) this.lref(vmField).focus(true, true);
		},
		
		isFieldHidden: function(vmField) {
			return !this.getVM().get('hidden.'+vmField);
		},
		
		initHiddenFields: function() {
			// Description field is now visible by default
			/*
			var mo = this.getModel();
			Sonicle.VMUtils.set(this.getVM(), 'hidden', {
				flddescription: mo.isFieldEmpty('description')
			});
			*/
		},
		
		onViewLoad: function(s, success) {
			var me = this,
				SoD = Sonicle.Data,
				vm = me.getVM(),
				mo = me.getModel();
			// Overrides autogenerated string id by extjs...
			// It avoids type conversion problems server-side!
			//if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
			
			
			if (me.isMode(me.MODE_NEW)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setHidden(true);
				me.getAct('restore').setHidden(true);
				me.getAct('tags').setHidden(false);
				if (me.mys.hasAuditUI()) me.getAct('eventAuditLog').setDisabled(true);
				me.reloadCustomFields((me.opts.data || {}).tags, me.opts.cfData, false);
			} else if (me.isMode(me.MODE_VIEW)) {
				me.getAct('saveClose').setDisabled(true);
				me.getAct('delete').setHidden(true);
				me.getAct('restore').setHidden(true);
				me.getAct('tags').setHidden(true);
				if (me.mys.hasAuditUI()) me.getAct('eventAuditLog').setDisabled(false);
				me.hideCustomFields(me.getModel().cvalues().getCount() < 1);
			} else if (me.isMode(me.MODE_EDIT)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setHidden(false);
				me.getAct('restore').setHidden(!mo.wasBroken());
				me.getAct('tags').setHidden(false);
				me.lref('fldcalendar').setReadOnly(false);
				if (me.mys.hasAuditUI()) me.getAct('eventAuditLog').setDisabled(false);
				me.hideCustomFields(me.getModel().cvalues().getCount() < 1);
			}
			
			me.initHiddenFields();
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
				me.reloadCustomFields(nv, false);
			}
		},
		
		reloadCustomFields: function(tags, cfInitialData) {
			var me = this;
			WTA.util.CustomFields.reloadCustomFields(tags, cfInitialData, {
				serviceId: me.mys.ID,
				model: me.getModel(),
				idField: 'eventId',
				cfPanel: me.lref('tabcfields'),
				callback: function(success, json) {
					if (success) me.hideCustomFields(json.total < 1);
				},
				scope: me
			});
		},
		
		hideCustomFields: function(hide) {
			this.lref('tpnlmain').getTabBar().setHidden(hide);
		},
		
		editRecurrence: function() {
			var me = this,
				mo = me.getModel(),
				vw = WT.createView(me.mys.ID, 'view.RecurrenceEditor', {
					swapReturn: true,
					viewCfg: {
						rruleString: mo.get('rruleString')
					}
				});
			
			vw.on('viewok', function(s, data) {
				var mo = me.getModel(),
					split = Sonicle.form.field.rr.Recurrence.splitRRuleString(data.rruleString);
				mo.set('rrule', split.rrule);
				mo.setRecurStart(split.start);
			});
			vw.showView();
		},
		
		showAvailability: function() {
			var me = this,
				SoS = Sonicle.String,
				mo = me.getModel(),
				vw = WT.createView(me.mys.ID, 'view.PlanningEditor', {
					swapReturn: true,
					viewCfg: {
						eventStart: Sonicle.Date.clone(mo.get('startDate')),
						eventEnd: Sonicle.Date.clone(mo.get('endDate')),
						eventTimezone: mo.get('timezone'),
						eventAllDay: mo.get('allDay'),
						eventOrganizerProfile: mo.get('_profileId'),
						eventAttendees: Sonicle.Data.collectValues(mo.attendees(), 'recipient', null, function(value) {
							return SoS.regexpExecAll(value, SoS.reMatchAnyRFC5322Addresses)[0];
						})
					}
				});
				
			vw.on('viewok', function(s, data) {
				var mo = me.getModel();
				if (mo.get('allDay') === true) {
					mo.setStartDate(data.start);
					mo.setEndDate(data.end);
				} else {
					mo.setStart(data.start);
					mo.setEnd(data.end);
				}
			});
			vw.showView();
		},
		
		showResourcePicker: function() {
			var me = this,
				SoS = Sonicle.String,
				usedResources = Sonicle.Data.collectValues(me.getModel().attendees(), 'recipient',
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
				title: WT.res(me.mys.ID, 'event.btn-addResource.lbl'),
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
				sto = me.getModel().attendees();
				
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
			me.resourcePicker.close();
			me.resourcePicker = null;
		},
		
		doSaveEvent: function(mo, target) {
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
	
		doDeleteEvent: function(mo, target) {
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
		
		doImportTextAsAttendees: function(text, recordDefaults) {
			this.doImportRecipientsAsAttendees(text.split(/\r\n|\r|\n|,|;/g), recordDefaults);
		},

		doImportRecipientsAsAttendees: function(items, recordDefaults, limit) {
			var me = this,
				mo = me.getModel(),
				arr = [],
				data, rcpt;
			
			if (limit === undefined) limit = 1000;
			if (limit === null || !Ext.isNumber(limit) || limit < 0) limit = Number.MAX_VALUE;
			
			if (Ext.isArray(items)) {
				if (items.length <= limit) {
					Ext.iterate(items, function(item) {
						rcpt = Ext.String.trim(Ext.isString(item) ? item : item.email);
						if (!Ext.isEmpty(rcpt)) {
							data = Ext.apply({}, recordDefaults, {
								notify: true,
								recipientType: 'IND',
								recipientRole: 'REQ',
								responseStatus: 'NA'
							});
							data['recipient'] = rcpt;
							arr.push(data);
						}
					});
					if (arr.length > 0) {
						if ((mo.attendees().getCount() + arr.length) <= limit) {
							mo.attendees().add(arr);
						} else {
							WT.error(me.res('event.importAttendees.error.limit', limit));
						}
					}
						
				} else {
					WT.error(me.res('event.importAttendees.error.limit', limit));
				}
			}
		}
	}
});
