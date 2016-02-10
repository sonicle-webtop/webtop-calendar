/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
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
	extend: 'WT.sdk.Service',
	requires: [
		'Sonicle.calendar.Panel',
		'Sonicle.MultiCalendar',
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Color',
		'Sonicle.webtop.calendar.model.FolderNode',
		'Sonicle.webtop.calendar.model.MultiCalDate',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.GridEvent',
		'Sonicle.calendar.data.Events',
		'Sonicle.calendar.data.MemoryCalendarStore', //TODO: rimuovere dopo aver elminato la dipendenza inutile nel componente calendar
		'Sonicle.calendar.data.Calendars', //TODO: rimuovere dopo aver elminato la dipendenza inutile nel componente calendar
		'Sonicle.calendar.data.MemoryEventStore', //TODO: rimuovere dopo aver elminato la dipendenza inutile nel componente calendar
		'Sonicle.webtop.calendar.view.Calendar',
		'Sonicle.webtop.calendar.view.Sharing'
	],
	mixins: [
		'WT.mixin.FoldersTree'
	],
	
	needsRefresh: true,
	
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
							border: true,
							startDay: WT.getStartDay(),
							highlightMode: me.getOption('view'),
							width: 184, //TODO: valutare un sistema di misura affidabile
							height: 298,
							store: {
								model: 'Sonicle.webtop.calendar.model.MultiCalDate',
								proxy: WTF.proxy(me.ID, 'GetSchedulerDates', 'dates')
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
								me.refreshEvents();
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
					activeView: me.getOption('view'),
					startDay: WT.getStartDay(),
					use24HourTime: WT.getUse24HourTime(),
					timezone: WT.getTimezone(),
					viewCfg: {
						timezoneIconCls: 'fa fa-globe',
						privateIconCls: 'fa fa-lock',
						reminderIconCls: 'fa fa-bell-o',
						recurrenceIconCls: 'fa fa-refresh',
						recurrenceBrokenIconCls: 'fa fa-chain-broken',
						todayText: me.res('socal.today'),
						moreText: me.res('socal.more'),
						ddCreateEventText: me.res('socal.ddcreateevent'),
						ddCopyEventText: me.res('socal.ddcopyevent'),
						ddMoveEventText: me.res('socal.ddmoveevent'),
						ddResizeEventText: me.res('socal.ddresizeevent'),
						ddDateFormat: 'j/m',
						scrollStartHour: 7
					},
					monthViewCfg: {
						showHeader: true,
						showWeekLinks: true,
						showWeekNumbers: true
					},
					calendarStore: Ext.create('Sonicle.calendar.data.MemoryCalendarStore', {
						data: Sonicle.calendar.data.Calendars.getData()
					}),
					store: {
						autoSync: true,
						model: 'Sonicle.calendar.data.EventModel',
						proxy: WTF.apiProxy(me.ID, 'ManageEventsScheduler', 'events')
					},
					listeners: {
						rangeselect: function(s,dates,onComplete) {
							onComplete();
							var cal = me.getSelectedFolder(me.getRef('folderstree'));
							if(cal) me.addEvent(cal.get('_pid'), cal.get('_calId'), cal.get('_isPrivate'), cal.get('_busy'), cal.get('_reminder'), dates.startDate, dates.endDate, false);
						},
						eventdblclick: function(s, rec) {
							if(me.isEventEditable(rec)) me.editEvent(rec.get('id'));
						},
						daydblclick: function(s, dt, ad) {
							var cal = me.getSelectedFolder(me.getRef('folderstree')),
									soDate = Sonicle.Date,
									start, end;
							if(cal) {
								if(ad) {
									start = soDate.copyTime(me.getOption('workdayStart'), dt);
									end = soDate.copyTime(me.getOption('workdayEnd'), dt);
								} else {
									start = dt;
									end = soDate.add(dt, {minutes: 30});
								}
								me.addEvent(cal.get('_pid'), cal.get('_calId'), cal.get('_isPrivate'), cal.get('_busy'), cal.get('_reminder'), start, end, ad);
							}
						},
						eventcontextmenu: function(s, rec, el, evt) {
							WT.showContextMenu(evt, me.getRef('cxmEvent'), {event: rec});
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
						iconField: function(v,rec) {
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
							if(me.isEventEditable(rec)) me.editEvent(rec.get('id'));
						}
					}
				}))
			]
		}));
	},
	
	initActions: function() {
		var me = this,
				view = me.getOption('view');
		
		me.addAction('new', 'newEvent', {
			handler: function() {
				me.getAction('addEvent').execute();
			}
		});
		me.addAction('toolbox', 'erpExport', {
			iconCls: 'wtcal-icon-exportEvents-xs',
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
				if(node) me.addCalendar(node.get('_domainId'), node.get('_userId'));
			}
		});
		me.addAction('editCalendar', {
			handler: function() {
				var node = me.getSelectedFolder(me.getRef('folderstree'));
				if(node) me.editCalendar(node.get('_calId'));
			}
		});
		me.addAction('deleteCalendar', {
			handler: function() {
				var node = me.getSelectedFolder(me.getRef('folderstree'));
				if(node) me.deleteCalendar(node);
			}
		});
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
					fileuploaded: function(up, file) {
						var node = me.getSelectedFolder(me.getRef('folderstree'));
						if(node) me.importICal(node.get('_calId'), file.uploadId);
					}
				}
			})
		}));
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
				if(cal) me.addEventAtNow(cal.get('_pid'), cal.get('_calId'), cal.get('_isPrivate'), cal.get('_busy'), cal.get('_reminder'), day);
			}
		});
		me.addAction('openEvent', {
			text: WT.res('act-open.lbl'),
			handler: function() {
				var rec = WT.getContextMenuData().event;
				if(me.isEventEditable(rec)) me.editEvent(rec.get('id'));
			}
		});
		me.addAction('deleteEvent', {
			text: WT.res('act-delete.lbl'),
			iconCls: 'wt-icon-delete-xs',
			handler: function() {
				var rec = WT.getContextMenuData().event;
				me.deleteEvent(rec);
			}
		});
		me.addAction('restoreEvent', {
			text: WT.res('act-restore.lbl'),
			iconCls: 'wt-icon-restore-xs',
			handler: function() {
				var rec = WT.getContextMenuData().event;
				me.restoreEvent(rec);
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
				beforeshow: function() {
					var rec = WT.getContextMenuData().folder,
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
				me.getRef('uploaders', 'importEvents')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function() {
					var rec = WT.getContextMenuData().folder,
							rr = me.toRightsObj(rec.get('_rrights')),
							fr = me.toRightsObj(rec.get('_frights')),
							er = me.toRightsObj(rec.get('_erights'));
					me.getAction('editCalendar').setDisabled(!fr.UPDATE);
					me.getAction('deleteCalendar').setDisabled(!fr.DELETE || rec.get('_builtIn'));
					me.getAction('addCalendar').setDisabled(!rr.MANAGE);
					me.getAction('editSharing').setDisabled(!rr.MANAGE);
					me.getAction('addEvent').setDisabled(!er.CREATE);
					me.getRef('uploaders', 'importEvents').setDisabled(!er.CREATE);
				}
			}
		}));
		
		me.addRef('cxmEvent', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('openEvent'),
				'-',
				me.getAction('deleteEvent'),
				me.getAction('restoreEvent'),
				me.getAction('addEvent'),
				'-',
				me.getAction('printEvents')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function() {
					var rec = WT.getContextMenuData().event,
							er = me.toRightsObj(rec.get('rights')),
							readOnly = (rec.get('isReadOnly') === true),
							broken = (rec.get('isBroken') === true);
					
					me.getAction('openEvent').setDisabled(!er.UPDATE || readOnly);
					me.getAction('deleteEvent').setDisabled(!er.DELETE || readOnly);
					me.getAction('restoreEvent').setDisabled(!er.UPDATE || readOnly || !broken);
					//me.getAction('restoreEvent').setDisabled(rec.get('isBroken') === false);
				}
			}
		}));
	},
	
	onActivate: function() {
		var me = this,
				scheduler = me.getRef('scheduler');
		
		if(me.needsRefresh) {
			me.needsRefresh = false;
			if(scheduler.getStore().loadCount === 0) { // The first time...
				//...skip multical, it's already updated with now date
				scheduler.setStartDate(me.getRef('multical').getValue());
			} else {
				me.refreshEvents();
			}
		}
	},
	
	onCalendarViewSave: function(s, success, model) {
		if(!success) return;
		var me = this,
				store = me.getRef('folderstree').getStore(),
				node;
		
		// Look for root folder and reload it!
		node = store.findNode('_pid', model.get('_profileId'), false);
		if(node) {
			store.load({node: node});
			if(node.get('checked'))	me.refreshEvents();
		}
	},
	
	onEventViewSave: function(s, success, model) {
		if(!success) return;
		this.refreshEvents();
	},
	
	refreshEvents: function() {
		var me = this;
		if(me.isActive()) {
			me.getRef('multical').getStore().load();
			me.getRef('scheduler').getStore().load();
		} else {
			me.needsRefresh = true;
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
	
	editShare: function(id) {
		var me = this,
				vw = WT.createView(me.ID, 'view.Sharing');
		
		vw.show(false, function() {
			vw.getView().begin('edit', {
				data: {
					id: id
				}
			});
		});
	},
	
	addCalendar: function(domainId, userId) {
		var me = this,
				vw = WT.createView(me.ID, 'view.Calendar');
		
		vw.getView().on('viewsave', me.onCalendarViewSave, me);
		vw.show(false, function() {
			vw.getView().begin('new', {
				data: {
					domainId: domainId,
					userId: userId
				}
			});
		});
	},
	
	editCalendar: function(calendarId) {
		var me = this,
				vw = WT.createView(me.ID, 'view.Calendar');
		
		vw.getView().on('viewsave', me.onCalendarViewSave, me);
		vw.show(false, function() {
			vw.getView().begin('edit', {
				data: {
					calendarId: calendarId
				}
			});
		});
	},
	
	deleteCalendar: function(rec) {
		WT.confirm(this.res('calendar.confirm.delete', rec.get('text')), function(bid) {
			if(bid === 'yes') rec.drop();
		}, this);
	},
	
	addEventAtNow: function(ownerId, calendarId, isPrivate, busy, reminder, day) {
		if(day === undefined) day = new Date();
		var date = Sonicle.Date.copyTime(new Date(), day);
		this.addEvent(ownerId, calendarId, isPrivate, busy, reminder, date, date, false);
	},
	
	addEvent: function(ownerId, calendarId, isPrivate, busy, reminder, start, end, allDay) {
		var me = this,
				vw = WT.createView(me.ID, 'view.Event');
		
		vw.getComponent(0).on('viewsave', me.onEventViewSave, me);
		vw.show(false, function() {
			vw.getView().begin('new', {
				data: {
					calendarId: calendarId,
					isPrivate: isPrivate,
					busy: busy,
					reminder: reminder,
					startDate: start,
					endDate: end,
					timezone: WT.getOption('timezone'),
					allDay: allDay,
					_profileId: ownerId
				}
			});
		});
	},
	
	editEvent: function(id) {
		var me = this,
				vw = WT.createView(me.ID, 'view.Event');
		
		vw.getView().on('viewsave', me.onEventViewSave, me);
		vw.show(false, function() {
			vw.getView().begin('edit', {
				data: {
					id: id
				}
			});
		});
	},
	
	deleteEvent: function(rec, opts) {
		opts = opts || {};
		var me = this, ajaxFn;
		
		ajaxFn = function(target, id) {
			WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
				params: {
					crud: 'delete',
					target: target,
					id: id
				},
				callback: function(success, o) {
					Ext.callback(opts.callback, opts.scope, [success, o]);
					if(success) me.refreshEvents();
					
				}
			});
		};
		
		if(rec.get('isRecurring')) {
			me.confirmForRecurrence(me.res('event.recurring.confirm.delete'), function(bid) {
				if(bid === 'ok') {
					var target = WT.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']);
					ajaxFn(target, rec.get('id'));
				}
			}, me);
		} else {
			WT.confirm(me.res('event.confirm.delete', rec.get('title')), function(bid) {
				if(bid === 'yes') {
					ajaxFn('this', rec.get('id'));
				}
			}, me);
		}
	},
	
	restoreEvent: function(rec, opts) {
		opts = opts || {};
		var me = this;
		WT.confirm(me.res('event.recurring.confirm.restore'), function(bid) {
			if(bid === 'yes') {
				WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
					params: {
						crud: 'restore',
						id: rec.get('id')
					},
					callback: function(success, o) {
						Ext.callback(opts.callback, opts.scope, [success, o]);
						if(success) me.refreshEvents();
					}
				});
			}
		}, me);
	},
	
	importICal: function(calendarId, uploadId) {
		var me = this;
		WT.ajaxReq(me.ID, 'ImportICal', {
			params: {
				calendarId: calendarId,
				uploadId: uploadId
			},
			callback: function(success, json) {
				if(success) {
					me.refreshEvents();
				}
			}
		});
	},
	
	erpExport: function() {
		var me = this,
				vw = WT.createView(me.ID, 'view.ErpExport');
		vw.show(false);
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
	
	isEventEditable: function(rec) {
		return !(rec.get('isReadOnly') === true);
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
	
	print: function() {
		//TODO: completare stampa
		Ext.create('Ext.XTemplate',
			'<table border="0" cellspacing="0" width="100%">',
			
			
			
			
			'</table>'
		);
		
	},
	
	
	
	printEvents: function (owner, start, end, records) {
		var me = this,
				EM = Sonicle.calendar.data.EventMappings,
				days = Sonicle.Date.diffDays(start, end);
		
		var month = this.monthNames[this.value.getMonth()];
		var year = this.value.getFullYear();
		var max = this.value.getDaysInMonth();
		if (view === 'd') {
			max = 1;
		} else if (view === 'w5') {
			max = 5;
		} else if (view === 'w' || view === 'w7') {
			max = 7;
		}

		var htmlSrc = "";
		htmlSrc += "<TABLE BORDER=0 CELLSPACING=0 width='100%'>";

		// Title
		htmlSrc += "<TR BGCOLOR=GRAY>";
		htmlSrc += "<TD ALIGN=CENTER COLSPAN=2>";
		htmlSrc += "<H1>";
		htmlSrc += owner + " - " + Ext.Date.monthNames[start.getMonth()] + " " + start.getFullYear();
		htmlSrc += "</H1>";
		htmlSrc += "</TD>";
		htmlSrc += "</TR>";
		htmlSrc += "<TR BGCOLOR=GRAY>";
		htmlSrc += "<TD ALIGN=CENTER COLSPAN=2>";
		htmlSrc += "<H2>";

		htmlSrc += Ext.Date.dayNames[start.getDay()] + " " + start.getDate();
		if (days > 0) {
			htmlSrc += " - " + Ext.Date.dayNames[end.getDay()] + " " + end.getDate();
		}
		htmlSrc += "</H2>";
		htmlSrc += "</TD>";
		htmlSrc += "</TR>";
		
		
		Ext.iterate(records, function(rec) {
			
		});
		
		// Events
		var d = null;
		var cspan = "";
		if (days > 0) cspan = "COLSPAN=2";
		for (var i = 1; i <= max; i++) {
			d = this.sd[i].getDate();
			var len = this.sd[i].evts.length;
			if (len > 0) {
				for (var j = 0; j < len; j++) {
					var sevent = this.sd[i].evts[j];
					WT.debug("event=" + sevent.getCustomer() + " " + sevent.getReport() + " " + sevent.getActivity());
					htmlSrc += "<TR>";
					if (j == 0 && max > 1) {
						htmlSrc += "<TD width=100>" + this.dayNames[d.getDay()] + " " + d.getDate() + "</TD>";
					} else if (max > 1) {
						htmlSrc += "<TD width=100>&nbsp;</TD>";
					}
					htmlSrc += "<TD " + cspan + ">" + sevent.getStartHour() + ":" + sevent.getStartMinute() + " " + sevent.getEndHour() + ":" + sevent.getEndMinute() + " " + sevent.event + " " + WT.res("calendar", "textLocation") + sevent.getReport() + " - " + WT.res("calendar", "activity") + sevent.getActivity() + " - " + WT.res("calendar", "customer") + sevent.getCustomer() + "</TD>";
					htmlSrc += "</TR>";
				}
			} else {
				htmlSrc += "<TR>";
				if (max > 1)
					htmlSrc += "<TD width=100>" + this.dayNames[d.getDay()] + " " + d.getDate() + "</TD>";
				htmlSrc += "<TD " + cspan + ">&nbsp;</TD>";
				htmlSrc += "</TR>";
			}
		}

		htmlSrc += "</TABLE>";

		//WT.app.print(htmlSrc);
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
	}
});
