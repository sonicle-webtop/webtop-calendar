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
Ext.define('Sonicle.webtop.calendar.Service', {
	extend: 'WTA.sdk.Service',
	requires: [
		'Sonicle.calendar.Panel',
		'Sonicle.calendar.MultiCalendar',
		'Sonicle.calendar.data.EventModel',
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Color',
		'Sonicle.webtop.calendar.model.FolderNode',
		'Sonicle.webtop.calendar.model.MultiCalDate',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.GridEvent',
		'Sonicle.webtop.calendar.view.Sharing',
		'Sonicle.webtop.calendar.view.Calendar',
		'Sonicle.webtop.calendar.view.Event',
		'Sonicle.webtop.calendar.view.CalendarChooser',
		'Sonicle.webtop.calendar.ServiceApi'
	],
	mixins: [
		'WTA.mixin.FoldersTree'
	],
	
	needsReload: true,
	api: null,
	
	getApiInstance: function() {
		var me = this;
		if (!me.api) me.api = Ext.create('Sonicle.webtop.calendar.ServiceApi', {service: me});
		return me.api;
	},
	
	init: function() {
		var me = this;
		//TODO: trovare una collocazione a questa chiamata
		Sonicle.upload.Uploader.registerMimeType('text/calendar', ['ical','ics','icalendar']);
		
		me.initActions();
		me.initCxm();
		
		me.on('activate', me.onActivate, me);
		me.onMessage('notifyReminder', function() {
			WT.info('reminder arrived');
		});
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			items: [
				'-',
				me.getAction('printScheduler'),
				'-',
				me.getAction('today'),
				me.getAction('previousday'),
				me.getAction('nextday'),
				'-',
				me.getAction('dayview'),
				me.getAction('week5view'),
				me.getAction('weekview'),
				me.getAction('dweekview'),
				me.getAction('monthview'),
				'->',
				Ext.create({
					xtype: 'textfield',
					width: 200,
					triggers: {
						search: {
							cls: Ext.baseCSSPrefix + 'form-search-trigger',
							handler: function(s) {
								me.searchEvents(s.getValue());
							}
						}
					},
					listeners: {
						specialkey: function(s, e) {
							if(e.getKey() === e.ENTER) {
								me.searchEvents(s.getValue());
							}
						}
					}
				})
			]
		}));
		
		me.setToolComponent(Ext.create({
			xtype: 'panel',
			layout: 'border',
			title: me.getName(),
			items: [{
					region: 'north',
					xtype: 'panel',
					layout: 'center',
					header: false,
					border: false,
					height: 305,
					items: [
						me.addRef('multical', Ext.create({
							xtype: 'somulticalendar',
							border: false,
							startDay: WT.getStartDay(),
							highlightMode: me.getVar('view'),
							width: 184, //TODO: valutare un sistema di misura affidabile
							height: 298,
							store: {
								model: 'Sonicle.webtop.calendar.model.MultiCalDate',
								proxy: WTF.proxy(me.ID, 'GetSchedulerDates', 'dates', {autoAbort: true})
							},
							listeners: {
								change: function(s, nv) {
									me.getRef('scheduler').setStartDate(nv);
								}
							}
						}))
					]
				},
				me.addRef('folderstree', Ext.create({
					region: 'center',
					xtype: 'treepanel',
					border: false,
					useArrows: true,
					rootVisible: false,
					store: {
						autoLoad: true,
						autoSync: true,
						model: 'Sonicle.webtop.calendar.model.FolderNode',
						proxy: WTF.apiProxy(me.ID, 'ManageFoldersTree', 'children', {
							writer: {
								allowSingle: false // Make update/delete using array payload
							}
						}),
						root: {
							id: 'root',
							expanded: true
						},
						listeners: {
							write: function(s,op) {
								me.reloadEvents();
							}
						}
					},
					hideHeaders: true,
					listeners: {
						checkchange: function(n, ck) {
							me.showHideFolder(n, ck);
						},
						itemcontextmenu: function(s, rec, itm, i, e) {
							if(rec.get('_type') === 'root') {
								WT.showContextMenu(e, me.getRef('cxmRootFolder'), {folder: rec});
							} else {
								WT.showContextMenu(e, me.getRef('cxmFolder'), {folder: rec});
							}
						}
					}
				}))
			]
		}));
		
		me.setMainComponent(Ext.create({
			xtype: 'container',
			layout: 'card',
			activeItem: 0,
			items: [
				me.addRef('scheduler', Ext.create({
					xtype: 'calendarpanel',
					itemId: 'scheduler',
					activeView: me.getVar('view'),
					startDay: WT.getStartDay(),
					use24HourTime: WT.getUse24HourTime(),
					timezone: WT.getTimezone(),
					viewCfg: {
						timezoneIconCls: 'fa fa-globe',
						privateIconCls: 'fa fa-lock',
						reminderIconCls: 'fa fa-bell-o',
						recurrenceIconCls: 'fa fa-refresh',
						recurrenceBrokenIconCls: 'fa fa-chain-broken',
						commentsIconCls: 'fa fa-commenting-o',
						todayText: me.res('socal.today'),
						moreText: me.res('socal.more'),
						ddCreateEventText: me.res('socal.ddcreateevent'),
						ddCopyEventText: me.res('socal.ddcopyevent'),
						ddMoveEventText: me.res('socal.ddmoveevent'),
						ddResizeEventText: me.res('socal.ddresizeevent'),
						ddDateFormat: 'j/m',
						scrollStartHour: Math.max(me.getVar('workdayStart').getHours()-1, 0),
						businessHoursStart: me.getVar('workdayStart').getHours(),
						businessHoursEnd: me.getVar('workdayEnd').getHours()
					},
					monthViewCfg: {
						showHeader: true,
						showWeekLinks: true,
						showWeekNumbers: true
					},
					store: {
						autoSync: true,
						model: 'Sonicle.calendar.data.EventModel',
						proxy: WTF.apiProxy(me.ID, 'ManageEventsScheduler', 'events', {autoAbort: true}),
						listeners: {
							write: function() {
								me.getRef('multical').getStore().load();
							}
						}
					},
					listeners: {
						contextmenu: function(s,e) {
							WT.showContextMenu(e, me.getRef('cxmScheduler'));
						},
						rangeselect: function(s,dates,onComplete) {
							onComplete();
							var cal = me.getSelectedFolder(me.getRef('folderstree'));
							if(cal) me.addEventUI(cal, dates.startDate, dates.endDate, false);
						},
						eventdblclick: function(s, rec) {
							if(rec && !me.isEventRO(rec)) {
								var er = me.toRightsObj(rec.get('_rights'));
								me.openEventUI(er.UPDATE, rec);
							}
						},
						daydblclick: function(s, dt, ad) {
							var cal = me.getSelectedFolder(me.getRef('folderstree')),
									soDate = Sonicle.Date,
									start, end;
							if(cal) {
								if(ad) {
									start = soDate.copyTime(me.getVar('workdayStart'), dt);
									end = soDate.copyTime(me.getVar('workdayEnd'), dt);
								} else {
									start = dt;
									end = soDate.add(dt, {minutes: 30});
								}
								me.addEventUI(cal, start, end, ad);
							}
						},
						eventcontextmenu: function(s, rec, el, e) {
							WT.showContextMenu(e, me.getRef('cxmEvent'), {event: rec});
						}
					}
				})),
				me.addRef('results', Ext.create({
					xtype: 'grid',
					itemId: 'results',
					store: {
						model: 'Sonicle.webtop.calendar.model.GridEvent',
						proxy: WTF.apiProxy(me.ID, 'ManageGridEvents', 'events', {
							extraParams: {
								query: null
							}
						})
					},
					columns: [{
						xtype: 'soiconcolumn',
						getIconCls: function(v,rec) {
							var ico = 'single-event';
							if(rec.get('isBroken')) ico = 'broken-event';
							if(rec.get('isRecurring')) ico = 'recurring-event';
							return WTF.cssIconCls(me.XID, ico, 'xs');
						},
						iconSize: WTU.imgSizeToPx('xs'),
						width: 40
					}, {
						xtype: 'socolorcolumn',
						dataIndex: 'folderName',
						colorField: 'color',
						displayField: 'folderName',
						header: me.res('event.fld-calendar.lbl'),
						width: 100
					}, {
						dataIndex: 'startDate',
						xtype: 'datecolumn',
						format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
						header: me.res('event.fld-startDate.lbl'),
						width: 150
					}, {
						dataIndex: 'endDate',
						xtype: 'datecolumn',
						format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
						header: me.res('event.fld-endDate.lbl'),
						width: 150
					}, {
						dataIndex: 'title',
						header: me.res('event.fld-title.lbl'),
						flex: 1
					}, {
						dataIndex: 'location',
						header: me.res('event.fld-location.lbl'),
						flex: 1
					}],
					tools: [{
						type: 'close',
						callback: function() {
							me._activateMain('scheduler');
						}
					}],
					listeners: {
						rowdblclick: function(s, rec) {
							//TODO: handle edit permission
							if(!me.isEventRO(rec)) me.editEvent(rec.get('id'));
						}
					}
				}))
			]
		}));
	},
	
	trFolders: function() {
		return this.getRef('folderstree');
	},
	
	initActions: function() {
		var me = this,
				view = me.getVar('view');
		
		me.addAction('new', 'newEvent', {
			handler: function() {
				me.getAction('addEvent').execute();
			}
		});
		me.addAction('toolbox', 'erpExport', {
			iconCls: 'wtcal-icon-export-xs',
			handler: function() {
				me.erpExport();
			}
		});
		
		me.addAction('today', {
			handler: function() {
				me.moveDate(0);
			}
		});
		me.addAction('previousday', {
			text: null,
			handler: function() {
				me.moveDate(-1);
			}
		});
		me.addAction('nextday', {
			text: null,
			handler: function() {
				me.moveDate(1);
			}
		});
		me.addAction('dayview', {
			itemId: 'd',
			pressed: view === 'd',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('d');
			}
		});
		me.addAction('week5view', {
			itemId: 'w5',
			pressed: view === 'w5',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('w5');
			}
		});
		me.addAction('weekview', {
			itemId: 'w',
			pressed: view === 'w',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('w');
			}
		});
		me.addAction('dweekview', {
			itemId: 'dw',
			pressed: view === 'dw',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('dw');
			}
		});
		me.addAction('monthview', {
			itemId: 'm',
			pressed: view === 'm',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('m');
			}
		});
		me.addAction('editSharing', {
			text: WT.res('sharing.tit'),
			iconCls: WTF.cssIconCls(WT.XID, 'sharing', 'xs'),
			handler: function() {
				var node = me.getSelectedNode(me.getRef('folderstree'));
				if(node) me.editShare(node.getId());
			}
		});
		me.addAction('addCalendar', {
			handler: function() {
				var node = me.getSelectedFolder(me.getRef('folderstree'));
				if(node) me.addCalendarUI(node);
			}
		});
		me.addAction('editCalendar', {
			handler: function() {
				var node = me.getSelectedFolder(me.getRef('folderstree'));
				if(node) me.editCalendarUI(node);
			}
		});
		me.addAction('deleteCalendar', {
			handler: function() {
				var node = me.getSelectedFolder(me.getRef('folderstree'));
				if(node) me.deleteCalendarUI(node);
			}
		});
		me.addAction('importEvents', {
			handler: function() {
				var node = me.getSelectedFolder(me.getRef('folderstree'));
				if(node) me.importEventsUI(node);
			}
		});
		/*
		me.addRef('uploaders', 'importEvents', Ext.create('Sonicle.upload.Item', {
			text: WT.res(me.ID, 'act-importEvents.lbl'),
			iconCls: WTF.cssIconCls(me.XID, 'importEvents', 'xs'),
			uploaderConfig: WTF.uploader(me.ID, 'ICalUpload', {
				mimeTypes: [
					{title: 'iCalendar files', extensions: 'ical,ics,icalendar'}
				],
				listeners: {
					uploadstarted: function(up) {
						//TODO: caricamento
						//me.wait();
					},
					uploadcomplete: function(up) {
						//TODO: caricamento
						//me.unwait();
					},
					uploaderror: function(up) {
						//TODO: caricamento
						//me.unwait();
					},
					fileuploaded: function(up, file, json) {
						var node = me.getSelectedFolder(me.getRef('folderstree'));
						if(node) me.importICal(node.get('_calId'), json.data.uploadId);
					}
				}
			})
		}));
		*/
		me.addAction('viewAllFolders', {
			iconCls: 'wt-icon-select-all-xs',
			handler: function() {
				me.showHideAllFolders(me.getSelectedRootFolder(me.getRef('folderstree')), true);
			}
		});
		me.addAction('viewNoneFolders', {
			iconCls: 'wt-icon-select-none-xs',
			handler: function() {
				me.showHideAllFolders(me.getSelectedRootFolder(me.getRef('folderstree')), false);
			}
		});
		me.addAction('addEvent', {
			handler: function() {
				var cal = me.getSelectedFolder(me.getRef('folderstree')),
						day = me.getRef('multical').getValue();
				if(cal) me.addEventAtUI(cal, day);
			}
		});
		me.addAction('openEvent', {
			text: WT.res('act-open.lbl'),
			handler: function(s,e) {
				var rec = WTU.itselfOrFirst(e.menuData.event), er;
				if(rec) {
					er = me.toRightsObj(rec.get('_rights'));
					me.openEventUI(er.UPDATE, rec);
				}
			}
		});
		me.addAction('deleteEvent', {
			text: WT.res('act-delete.lbl'),
			iconCls: 'wt-icon-delete-xs',
			handler: function(s,e) {
				var rec = WTU.itselfOrFirst(e.menuData.event);
				if(rec) me.deleteEventUI(rec);
			}
		});
		me.addAction('restoreEvent', {
			text: WT.res('act-restore.lbl'),
			iconCls: 'wt-icon-restore-xs',
			handler: function(s,e) {
				var rec = e.menuData.event;
				if(rec) me.confirmRestoreEvent(rec);
			}
		});
		me.addAction('copyEvent', {
			handler: function(s,e) {
				var rec = WTU.itselfOrFirst(e.menuData.event);
				if(rec) me.moveEventUI(true, rec);
			}
		});
		me.addAction('moveEvent', {
			handler: function(s,e) {
				var rec = WTU.itselfOrFirst(e.menuData.event);
				if(rec) me.moveEventUI(false, rec);
			}
		});
		me.addAction('printScheduler', {
			text: null,
			tooltip: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print-xs',
			handler: function() {
				var sched = me.getRef('scheduler'),
						bounds = sched.getActiveView().getViewBounds(),
						params = {
							startDate: Ext.Date.format(bounds.start, 'Y-m-d'),
							endDate: Ext.Date.format(bounds.end, 'Y-m-d'),
							view: me.getRef('multical').getHighlightMode()
						},
						url = WTF.processBinUrl(me.ID, 'PrintScheduler', params);
				Sonicle.URLMgr.openFile(url, {filename: 'agenda', newWindow: true});
			}
		});
		me.addAction('printEvent', {
			text: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print-xs',
			handler: function(s,e) {
				var rec = e.menuData.event;
				if(rec) me.printEventsDetail([rec.getId()]);
			}
		});
		
		
		
		me.addAction('printEvents', {
			text: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print-xs',
			handler: function() {
				//TODO: implementare stampa
				WT.warn('TODO');
			}
		});
	},
	
	initCxm: function() {
		var me = this;
		
		me.addRef('cxmRootFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('addCalendar'),
				'-',
				me.getAction('editSharing')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.folder,
							rr = me.toRightsObj(rec.get('_rrights'));
					me.getAction('addCalendar').setDisabled(!rr.MANAGE);
					me.getAction('editSharing').setDisabled(!rr.MANAGE);
				}
			}
		}));
		
		me.addRef('cxmFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('editCalendar'),
				me.getAction('deleteCalendar'),
				me.getAction('addCalendar'),
				'-',
				me.getAction('editSharing'),
				'-',
				me.getAction('viewAllFolders'),
				me.getAction('viewNoneFolders'),
				'-',
				me.getAction('addEvent'),
				me.getAction('importEvents')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.folder,
							rr = me.toRightsObj(rec.get('_rrights')),
							fr = me.toRightsObj(rec.get('_frights')),
							er = me.toRightsObj(rec.get('_erights'));
					me.getAction('editCalendar').setDisabled(!fr.UPDATE);
					me.getAction('deleteCalendar').setDisabled(!fr.DELETE || rec.get('_builtIn'));
					me.getAction('addCalendar').setDisabled(!rr.MANAGE);
					me.getAction('editSharing').setDisabled(!rr.MANAGE);
					me.getAction('addEvent').setDisabled(!er.CREATE);
					me.getAction('importEvents').setDisabled(!er.CREATE);
				}
			}
		}));
		
		me.addRef('cxmScheduler', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('addEvent'),
				me.getAction('printEvent')
			]
		}));
		
		me.addRef('cxmEvent', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('openEvent'),
				{
					text: me.res('copyormove.lbl'),
					menu: {
						items: [
							me.getAction('copyEvent'),
							me.getAction('moveEvent')
						]
					}
				},
				me.getAction('printEvent'),
				'-',
				me.getAction('deleteEvent'),
				me.getAction('restoreEvent')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.event,
							ro = me.isEventRO(rec);
							er = me.toRightsObj(rec.get('_rights')),
							brk = (rec.get('isBroken') === true);
					me.getAction('openEvent').setDisabled(ro);
					me.getAction('deleteEvent').setDisabled(ro || !er.DELETE);
					me.getAction('restoreEvent').setDisabled(ro || !brk || !er.UPDATE);
				}
			}
		}));
	},
	
	onActivate: function() {
		var me = this,
				scheduler = me.getRef('scheduler');
		
		if(me.needsReload) {
			me.needsReload = false;
			if(scheduler.getStore().loadCount === 0) { // The first time...
				//...skip multical, it's already updated with now date
				scheduler.setStartDate(me.getRef('multical').getValue());
			} else {
				me.reloadEvents();
			}
		}
	},
	
	loadFolderNode: function(pid) {
		var me = this,
				sto = me.trFolders().getStore(),
				node;
		
		node = sto.findNode('_pid', pid, false);
		if (node) {
			sto.load({node: node});
			if(node.get('checked'))	me.reloadEvents();
		}
	},
	
	reloadEvents: function() {
		var me = this;
		if(me.isActive()) {
			me.getRef('multical').getStore().load();
			me.getRef('scheduler').getStore().load();
		} else {
			me.needsReload = true;
		}
	},
	
	changeView: function(view) {
		var me = this;
		me._activateMain('scheduler');
		me.getRef('multical').setHighlightMode(view);
		me.getRef('scheduler').setActiveView(view);
	},
	
	moveDate: function(direction) {
		this._activateMain('scheduler');
		var mc = this.getRef('multical');
		if(direction === 0) {
			mc.setToday();
		} else if(direction === -1) {
			mc.setPreviousDay();
		} else if(direction === 1) {
			mc.setNextDay();
		}
	},
	
	addCalendarUI: function(node) {
		var me = this;
		me.addCalendar(node.get('_domainId'), node.get('_userId'), {
			callback: function(success, model) {
				if(success) me.loadFolderNode(model.get('_profileId'));
			}
		});
	},
	
	editCalendarUI: function(node) {
		var me = this;
		me.editCalendar(node.get('_calId'), {
			callback: function(success, model) {
				if(success) me.loadFolderNode(model.get('_profileId'));
			}
		});
	},
	
	deleteCalendarUI: function(node) {
		WT.confirm(this.res('calendar.confirm.delete', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if(bid === 'yes') node.drop();
		}, this);
	},
	
	importEventsUI: function(node) {
		var me = this;
		me.importEvents(node.get('_calId'), {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	addEventUI: function(cal, start, end, allDay) {
		var me = this;
		me.addEvent(cal.get('_pid'), cal.get('_calId'), cal.get('_isPrivate'), cal.get('_busy'), cal.get('_reminder'), start, end, allDay, {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	addEventAtUI: function(cal, day) {
		var me = this;
		me.addEventAt(cal.get('_pid'), cal.get('_calId'), cal.get('_isPrivate'), cal.get('_busy'), cal.get('_reminder'), day, {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	openEventUI: function(edit, evt) {
		var me = this;
		me.openEvent(edit, evt.get('id'), {
			callback: function(success) {
				if(success && edit) me.reloadEvents();
			}
		});
	},
	
	deleteEventUI: function(rec) {
		var me = this,
				id = rec.get('id');
		
		if(rec.get('isRecurring')) {
			me.confirmForRecurrence(me.res('event.recurring.confirm.delete'), function(bid) {
				if(bid === 'ok') {
					var target = WTA.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']);
					me.deleteEvent(id, target, {
						callback: function(succ) {
							if(succ) me.reloadEvents();
						}
					});
				}
			}, me);
		} else {
			WT.confirm(me.res('event.confirm.delete', Ext.String.ellipsis(rec.get('title'), 40)), function(bid) {
				if(bid === 'yes') {
					me.deleteEvent(id, 'this', {
						callback: function(succ) {
							if(succ) me.reloadEvents();
						}
					});
				}
			}, me);
		}
	},
	
	eventRestoreUI: function(evt) {
		var me = this;
		WT.confirm(me.res('event.recurring.confirm.restore'), function(bid) {
			if(bid === 'yes') {
				me.restoreEvent(evt.get('id'), {
					callback: function(success) {
						if(success) me.reloadEvents();
					}
				});
			}
		}, me);
	},
	
	moveEventUI: function(copy, evt) {
		var me = this,
				vct = me.createCalendarChooser(copy, evt.get('_profileId'), evt.get('calendarId'));
		
		vct.getView().on('viewok', function(s) {
			me.moveEvent(copy, evt.get('id'), s.getVMData().calendarId, {
				callback: function(success) {
					if(success) me.reloadEvents();
				}
			});
		});
		vct.show();
	},
	
	editShare: function(id) {
		var me = this,
				vct = WT.createView(me.ID, 'view.Sharing');
		
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					id: id
				}
			});
		});
	},
	
	addCalendar: function(domainId, userId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Calendar');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					domainId: domainId,
					userId: userId
				}
			});
		});
	},
	
	editCalendar: function(calendarId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Calendar');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					calendarId: calendarId
				}
			});
		});
	},
	
	addEventAt: function(ownerId, calendarId, isPrivate, busy, reminder, day, opts) {
		var date = Sonicle.Date.copyTime(new Date(), day);
		this.addEvent(ownerId, calendarId, isPrivate, busy, reminder, date, date, false, opts);
	},
	
	addEvent: function(ownerId, calendarId, isPrivate, busy, reminder, start, end, allDay, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Event');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					calendarId: calendarId,
					isPrivate: isPrivate,
					busy: busy,
					reminder: reminder,
					startDate: start,
					endDate: end,
					timezone: WT.getVar('timezone'),
					allDay: allDay,
					_profileId: ownerId
				}
			});
		});
	},
	
	openEvent: function(edit, eventId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Event'),
				mode = edit ? 'edit' : 'view';
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin(mode, {
				data: {
					id: eventId
				}
			});
		});
	},
	
	deleteEvent: function(eventId, target, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
			params: {
				crud: 'delete',
				target: target,
				id: eventId
			},
			callback: function(success, o) {
				Ext.callback(opts.callback, opts.scope, [success, o]);
			}
		});
	},
	
	restoreEvent: function(eventId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
			params: {
				crud: 'restore',
				id: eventId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	moveEvent: function(copy, eventId, targetCalendarId, opts) {
		opts = opts || {};
		var me = this;
		
		WT.ajaxReq(me.ID, 'ManageEvents', {
			params: {
				crud: 'move',
				copy: copy,
				id: eventId,
				targetCalendarId: targetCalendarId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	importEvents: function(calendarId, opts) {
		opts = opts || {};
		var me = this,
				vwc = WT.createView(me.ID, 'view.ImportEvents', {
					viewCfg: {
						calendarId: calendarId
					}
				});
		
		vwc.getView().on('dosuccess', function() {
			Ext.callback(opts.callback, opts.scope || me, true);
		});
		vwc.show();
	},
	
	/*
	importICal: function(calendarId, uploadId) {
		var me = this;
		WT.ajaxReq(me.ID, 'ImportICal', {
			params: {
				calendarId: calendarId,
				uploadId: uploadId
			},
			callback: function(success, json) {
				if(success) {
					me.reloadEvents();
				}
			}
		});
	},
	*/
	
	erpExport: function() {
		var me = this,
				vw = WT.createView(me.ID, 'view.ErpExport');
		vw.show(false);
	},
	
	printEventsDetail: function(keys) {
		var me = this, url;
		url = WTF.processBinUrl(me.ID, 'PrintEventsDetail', {keys: WTU.arrayAsParam(keys)});
		Sonicle.URLMgr.openFile(url, {filename: 'events-detail', newWindow: true});
	},
	
	searchEvents: function(query) {
		var me = this,
				gp = me.getRef('results');
		
		gp.setTitle(Ext.String.format('{0}: {1}', WT.res('word.search'), query));
		me._activateMain('results');
		WTU.loadWithExtraParams(gp.getStore(), {
			query: query
		});
	},
	
	isEventRO: function(rec) {
		return rec.get('isReadOnly') === true;
	},
	
	/*
	 * @private
	 */
	_activateMain: function(id) {
		this.getMainComponent().getLayout().setActiveItem(id);
	},
	
	/*
	 * @private
	 */
	_isMainActive: function(id) {
		return (this.getMainComponent().getLayout().getActiveItem() === id);
	},
	
	confirmForRecurrence: function(msg, cb, scope, opts) {
		var me = this, html;
		html = "</br></br>"
				+ "<table width='70%' style='font-size: 12px'>"
				+ "<tr><td><input type='radio' name='recurrence' id='this' checked='true' /></td><td width='95%'>"+me.res("confirm.recurrence.this")+"</td></tr>"
				+ "<tr><td><input type='radio' name='recurrence' id='since' /></td><td width='95%'>"+me.res("confirm.recurrence.since")+"</td></tr>"
				+ "<tr><td><input type='radio' name='recurrence' id='all' /></td><td width='95%'>"+me.res("confirm.recurrence.all")+"</td></tr>"
				+ "</table>";
		WT.confirm(msg + html, cb, scope, Ext.apply({
			buttons: Ext.Msg.OKCANCEL
		}, opts));
	},
	
	/**
	 * @private
	 */
	createCalendarChooser: function(copy, ownerId, calendarId) {
		var me = this;
		return WT.createView(me.ID, 'view.CalendarChooser', {
			viewCfg: {
				dockableConfig: {
					title: me.res(copy ? 'act-copyEvent.lbl' : 'act-moveEvent.lbl')
				},
				ownerId: ownerId,
				calendarId: calendarId
			}
		});
	}
});
