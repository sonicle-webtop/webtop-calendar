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
		'Sonicle.tree.Column',
		'WTA.ux.field.Search',
		'Sonicle.webtop.calendar.model.FolderNode',
		'Sonicle.webtop.calendar.model.MultiCalDate',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.GridEvent'
	],
	uses: [
		'WTA.util.FoldersTree',
		'Sonicle.webtop.calendar.ux.RecurringConfirmBox',
		'Sonicle.webtop.calendar.view.Sharing',
		'Sonicle.webtop.calendar.view.Calendar',
		'Sonicle.webtop.calendar.view.CalendarLinks',
		'Sonicle.webtop.calendar.view.Event',
		'Sonicle.webtop.calendar.view.CalendarChooser',
		'Sonicle.webtop.calendar.view.HiddenCalendars',
		'Sonicle.webtop.calendar.ServiceApi',
		'Sonicle.webtop.calendar.portlet.Events'
	],
	mixins: [
		'WTA.mixin.FoldersTree'
	],
	
	needsReload: true,
	api: null,
	
	treeSelEnabled: false,
	
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
		me.onMessage('remoteSyncResult', function(msg) {
			var pl = msg.payload,
					ok = (pl.success === true),
					tag = me.self.noTagRemoteSync(pl.calendarId),
					title = pl.calendarName;
			if (pl.start === true) {
				WT.showDesktopNotification(me.ID, {
					tag: tag,
					title: title,
					body: me.res('not.calendar.rsync.start.body')
				});
			} else {
				WT.showNotification(me.ID, false, {
					tag: tag,
					title: title,
					//iconCls: me.cssIconCls('im-chat', 'm'),
					body: ok ? me.res('not.calendar.rsync.end.body.ok') : me.res('not.calendar.rsync.end.body.err', pl.message),
					data: {
						calendarId: pl.calendarId
					}
				}/*, {callbackService: ok}*/);
				if (ok) me.reloadEvents();
			}
		});
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,			
			items: [
				'-',
				me.getAct('refresh'),
				me.getAct('printScheduler'),
				'->',
				{
					xtype: 'wtsearchfield',
					reference: 'fldsearch',
					fields: [{
						name: 'title',
						type: 'string',
						label: me.res('fld-search.field.title.lbl')
					}, {
						name: 'location',
						type: 'string',
						label: me.res('fld-search.field.location.lbl')
					}, {
						name: 'description',
						type: 'string',
						label: me.res('fld-search.field.description.lbl')
					}, {
						name: 'any',
						type: 'string',
						textSink: true,
						label: me.res('fld-search.field.any.lbl')
					}, {
						name: 'after',
						type: 'date',
						labelAlign: 'left',
						label: me.res('fld-search.field.after.lbl')
					}, {
						name: 'before',
						type: 'date',
						labelAlign: 'left',
						label: me.res('fld-search.field.before.lbl')
					}, {
						name: 'busy',
						type: 'boolean',
						boolKeyword: 'is',
						label: me.res('fld-search.field.busy.lbl')
					}, {
						name: 'private',
						type: 'boolean',
						boolKeyword: 'is',
						label: me.res('fld-search.field.private.lbl')
					}],
					tooltip: me.res('fld-search.tip'),
					emptyText: me.res('fld-search.emp'),
					listeners: {
						query: function(s, value, qObj) {
							me.queryEvents(qObj);
						}
					}
				},
				'->'
			]
		}));
		
		me.setToolComponent(Ext.create({
			xtype: 'panel',
			layout: 'border',
			referenceHolder: true,
			title: me.getName(),
			items: [{
				region: 'north',
				xtype: 'panel',
				layout: 'center',
				header: false,
				border: false,
				height: 305,
				items: [{
					xtype: 'somulticalendar',
					reference: 'multical',
					border: false,
					startDay: WT.getStartDay(),
					highlightMode: me.getVar('view'),
					dayText: me.res('multical.dayText'),
					weekText: me.res('multical.weekText'),
					width: 184, //TODO: valutare un sistema di misura affidabile
					height: 298,
					store: {
						model: 'Sonicle.webtop.calendar.model.MultiCalDate',
						proxy: WTF.proxy(me.ID, 'GetSchedulerDates', 'dates', {autoAbort: true})
					},
					listeners: {
						change: function(s, nv) {
							me.scheduler().setStartDate(nv);
						}
					}
				}]
			}, {
				region: 'center',
				xtype: 'treepanel',
				reference: 'trfolders',
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
				selModel: {
					mode: 'SIMPLE',
					toggleOnClick: true,
					ignoreRightMouseSelection: true
				},
				hideHeaders: true,
				columns: [{
					xtype: 'sotreecolumn',
					dataIndex: 'text',
					renderer: WTA.util.FoldersTree.coloredCheckboxTreeRenderer({
						defaultText: me.res('calendar.fld-default.lbl').toLowerCase()
					}),
					flex: 1
				}],
				listeners: {
					checkchange: function(n, ck) {
						n.refreshActive();
					},
					beforeselect: function(s, rec) {
						if (me.treeSelEnabled === false) return false;
					},
					beforedeselect: function(s, rec) {
						if (me.treeSelEnabled === false) return false;
					},
					itemclick: function(s, rec, itm, i, e) {
						if (me.treeSelEnabled === false) {
							if (!e.getTarget(s.checkboxSelector, itm)) {
								s.setChecked(rec, !rec.get('checked'), e);
							}
						}
					},
					itemcontextmenu: function(s, rec, itm, i, e) {
						if (rec.isFolderRoot()) {
							WT.showContextMenu(e, me.getRef('cxmRootFolder'), {node: rec});
						} else {
							WT.showContextMenu(e, me.getRef('cxmFolder'), {node: rec});
						}
					}
				}
			}]
		}));
		
		me.setMainComponent(Ext.create({
			xtype: 'container',
			layout: 'card',
			referenceHolder: true,
			activeItem: 0,
			items: [{
				xtype: 'calendarpanel',
				reference: 'scheduler',
				activeView: me.getVar('view'),
				startDay: WT.getStartDay(),
				use24HourTime: WT.getUse24HourTime(),
				viewCfg: {
					timezoneIconCls: 'fa fa-globe',
					privateIconCls: 'fa fa-lock',
					reminderIconCls: 'fa fa-bell-o',
					attendeesIconCls: 'fa fa-users',
					recurrenceIconCls: 'fa fa-refresh',
					recurrenceBrokenIconCls: 'fa fa-chain-broken',
					commentsIconCls: 'fa fa-commenting-o',
					todayText: me.res('scheduler.today'),
					moreText: me.res('scheduler.more'),
					ddCreateEventText: me.res('scheduler.ddcreateevent'),
					ddCopyEventText: me.res('scheduler.ddcopyevent'),
					ddMoveEventText: me.res('scheduler.ddmoveevent'),
					ddResizeEventText: me.res('scheduler.ddresizeevent'),
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
					model: 'Sonicle.calendar.data.EventModel',
					proxy: WTF.apiProxy(me.ID, 'ManageEventsScheduler', 'events', {autoAbort: true})
				},
				tbar: [
					me.getAct('today'),
					me.getAct('previousday'),
					me.getAct('nextday'),
					'->',
					me.getAct('dayview'),
					me.getAct('week5view'),
					me.getAct('weekview'),
					me.getAct('dweekview'),
					me.getAct('monthview'),
					'->'
				],
				listeners: {
					rangeselect: function(s,dates,onComplete) {
						onComplete();
						var soDate = Sonicle.Date,
								node = WTA.util.FoldersTree.getTargetFolder(me.trFolders()),
								ad;
						if (node) {
							ad = soDate.isMidnight(dates.startDate) && soDate.isMidnight(dates.endDate);
							me.addEventUI(node.get('_pid'), node.get('_calId'), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), dates.startDate, dates.endDate, ad);
						}
					},
					eventdblclick: function(s, rec) {
						if (rec && !me.self.schedEventHideData(rec)) {
							var er = WTA.util.FoldersTree.toRightsObj(rec.get('_rights'));
							me.openEventUI(er.UPDATE, rec.get('id'));
						}
					},
					daydblclick: function(s, dt, allDay) {
						var soDate = Sonicle.Date,
								node = WTA.util.FoldersTree.getTargetFolder(me.trFolders()),
								start = allDay ? soDate.copyTime(me.getVar('workdayStart'), dt) : dt,
								end = allDay ? soDate.copyTime(me.getVar('workdayEnd'), dt) : soDate.add(dt, {minutes: 30});
						if (node) {
							me.addEventUI(node.get('_pid'), node.get('_calId'), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), start, end, allDay);
						}
					},
					eventcontextmenu: function(s, rec, e) {
						WT.showContextMenu(e, me.getRef('cxmEvent'), {event: rec});
					},
					daycontextmenu: function(s, dt, allDay, e) {
						var soDate = Sonicle.Date,
								start = allDay ? soDate.copyTime(me.getVar('workdayStart'), dt) : dt,
								end = allDay ? soDate.copyTime(me.getVar('workdayEnd'), dt) : soDate.add(dt, {minutes: 30});
						WT.showContextMenu(e, me.getRef('cxmScheduler'), {start: start, end: end, allDay: allDay});
					},
					beforeeventmove: function(s, rec, ndt) {
						me.updateSchedEventUI(rec, ndt, null, null);
						return false;
					},
					beforeeventcopy: function(s, rec, ndt) {
						me.copySchedEventUI(rec, ndt);
						return false;
					},
					beforeeventresize: function(s, rec, data) {
						me.updateSchedEventUI(rec, data.StartDate, data.EndDate, null);
						return false;
					}
				}
			}, {
				xtype: 'grid',
				reference: 'gpresults',
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
						if (rec.get('isBroken')) ico = 'broken-event';
						if (rec.get('isRecurring')) ico = 'recurring-event';
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
					width: 150
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
						me.getMainComponent().setActiveItem(me.scheduler());
						me._activateMain(me.scheduler());
					}
				}],
				listeners: {
					rowdblclick: function(s, rec) {
						if (!me.self.schedEventHideData(rec)) {
							var er = WTA.util.FoldersTree.toRightsObj(rec.get('_rights'));
							me.openEventUI(er.UPDATE, rec.get('id'));
						}
					}
				}
			}]
		}));
	},
	
	notificationCallback: function(type, tag, data) {
		var me = this;
		if (Ext.String.startsWith(tag, me.self.NOTAG_REMOTESYNC)) {
			me.reloadEvents();
		}
	},
	
	multical: function() {
		return this.getToolComponent().lookupReference('multical');
	},
	
	trFolders: function() {
		return this.getToolComponent().lookupReference('trfolders');
	},
	
	scheduler: function() {
		return this.getMainComponent().lookupReference('scheduler');
	},
	
	gpResults: function() {
		return this.getMainComponent().lookupReference('gpresults');
	},
	
	initActions: function() {
		var me = this,
				hdscale = WT.getHeaderScale(),
				view = me.getVar('view');
		
		me.addAct('new', 'newEvent', {
			ignoreSize: true,
			handler: function() {
				me.getAct('addEvent').execute();
			}
		});
		me.addAct('toolbox', 'erpExport', {
			tooltip: null,
			iconCls: 'wtcal-icon-export-xs',
			handler: function() {
				me.erpExport();
			}
		});
		me.addAct('today', {
			ignoreSize: true,
			text: null,
			handler: function() {
				me.moveDate(0);
			}
		});
		me.addAct('previousday', {
			ignoreSize: true,
			text: null,
			handler: function() {
				me.moveDate(-1);
			}
		});
		me.addAct('nextday', {
			ignoreSize: true,
			text: null,
			handler: function() {
				me.moveDate(1);
			}
		});
		me.addAct('dayview', {
			ignoreSize: true,
			text: WT.plTags.desktop ? undefined : null,
			itemId: 'd',
			pressed: view === 'd',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('d');
			}
		});
		me.addAct('week5view', {
			ignoreSize: true,
			text: WT.plTags.desktop ? undefined : null,
			itemId: 'w5',
			pressed: view === 'w5',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('w5');
			}
		});
		me.addAct('weekview', {
			ignoreSize: true,
			text: WT.plTags.desktop ? undefined : null,
			itemId: 'w',
			pressed: view === 'w',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('w');
			}
		});
		me.addAct('dweekview', {
			ignoreSize: true,
			text: WT.plTags.desktop ? undefined : null,
			itemId: 'dw',
			pressed: view === 'dw',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('dw');
			}
		});
		me.addAct('monthview', {
			ignoreSize: true,
			text: WT.plTags.desktop ? undefined : null,
			itemId: 'm',
			pressed: view === 'm',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('m');
			}
		});
		me.addAct('editSharing', {
			text: WT.res('sharing.tit'),
			tooltip: null,
			iconCls: WTF.cssIconCls(WT.XID, 'sharing', 'xs'),
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.editShare(node.getId());
			}
		});
		me.addAct('manageHiddenCalendars', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.manageHiddenCalendarsUI(node);
			}
		});
		me.addAct('hideCalendar', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.hideCalendarUI(node);
			}
		});
		me.addAct('addCalendar', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.addCalendarUI(node.get('_domainId'), node.get('_userId'));
			}
		});
		me.addAct('addRemoteCalendar', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.addRemoteCalendarUI(node.get('_pid'));
			}
		});
		me.addAct('viewCalendarLinks', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.viewCalendarLinks(node.get('_calId'));
			}
		});
		me.addAct('editCalendar', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.editCalendarUI(node.get('_calId'));
			}
		});
		me.addAct('deleteCalendar', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.deleteCalendarUI(node);
			}
		});
		me.addAct('syncRemoteCalendar', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.syncRemoteCalendarUI(node.get('_calId'));
			}
		});
		me.addAct('importEvents', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.importEventsUI(node.get('_calId'));
			}
		});
		me.addAct('calendarColor', {
			text: me.res('mni-calendarColor.lbl'),
			tooltip: null,
			menu: {
				showSeparator: false,
				itemId: 'calendarColor',
				items: [{
						xtype: 'colorpicker',
						colors: WT.getColorPalette(),
						listeners: {
							select: function(s, color) {
								var node = s.menuData.node;
								me.getRef('cxmFolder').hide();
								if (node) me.updateCalendarColorUI(node, '#'+color);
							}
						}
					},
					'-',
					me.addAct('restoreCalendarColor', {
						tooltip: null,
						handler: function(s, e) {
							var node = e.menuData.node;
							if (node) me.updateCalendarColorUI(node, null);
						}
					})
				]
			}
		});
		var onItemClick = function(s, e) {
			var node = e.menuData.node;
			if (node && s.checked) me.updateCalendarSyncUI(node, s.getItemId());
		};
		me.addAct('calendarSync', {
			text: me.res('mni-calendarSync.lbl'),
			tooltip: null,
			menu: {
				itemId: 'calendarSync',
				items: [{
						itemId: 'O',
						text: me.res('store.sync.O'),
						group: 'calendarSync',
						checked: false,
						listeners: {
							click: onItemClick
						}
					}, {
						itemId: 'R',
						text: me.res('store.sync.R'),
						group: 'calendarSync',
						checked: false,
						listeners: {
							click: onItemClick
						}
					}, {
						itemId: 'W',
						text: me.res('store.sync.W'),
						group: 'calendarSync',
						checked: false,
						listeners: {
							click: onItemClick
						}
					}
				]
			}
		});
		me.addAct('viewThisFolderOnly', {
			ignoreSize: true,
			tooltip: null,
			iconCls: 'wt-icon-select-one',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree.activateSingleFolder(node.getFolderRootNode(), node.getId());
			}
		});
		me.addAct('viewAllFolders', {
			ignoreSize: true,
			tooltip: null,
			iconCls: 'wt-icon-select-all',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree.setActiveAllFolders(node.getFolderRootNode(), true);
			}
		});
		me.addAct('viewNoneFolders', {
			ignoreSize: true,
			tooltip: null,
			iconCls: 'wt-icon-select-none',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree.setActiveAllFolders(node.getFolderRootNode(), false);
			}
		});
		me.addAct('addSchedEvent', {
			text: me.res('act-addEvent.lbl'),
			tooltip: null,
			iconCls: 'wtcal-icon-addEvent-xs',
			handler: function(s, e) {
				var node = WTA.util.FoldersTree.getTargetFolder(me.trFolders()),
						mdata = e.menuData;
				if (node) {
					if (mdata.start && mdata.end) {
						me.addEventUI(node.get('_pid'), node.get('_calId'), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), mdata.start, mdata.end, mdata.allDay);
					} else {
						me.addEventOnUI(node.get('_pid'), node.get('_calId'), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), me.multical().getValue());
					}
				}
			}
		});
		me.addAct('addEvent', {
			tooltip: null,
			handler: function(s, e) {
				var node = (e && e.menuData) ? e.menuData.node : WTA.util.FoldersTree.getTargetFolder(me.trFolders()),
						day = me.multical().getValue();
				if (node) me.addEventOnUI(node.get('_pid'), node.get('_calId'), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), day);
			}
		});
		me.addAct('openEvent', {
			text: WT.res('act-open.lbl'),
			tooltip: null,
			handler: function(s, e) {
				var rec = WTU.itselfOrFirst(e.menuData.event), er;
				if (rec) {
					er = WTA.util.FoldersTree.toRightsObj(rec.get('_rights'));
					me.openEventUI(er.UPDATE, rec.get('id'));
				}
			}
		});
		me.addAct('deleteEvent', {
			text: WT.res('act-delete.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-delete',
			handler: function(s, e) {
				var rec = WTU.itselfOrFirst(e.menuData.event);
				if (rec) me.deleteSchedEventUI(rec);
			}
		});
		me.addAct('restoreEvent', {
			text: WT.res('act-restore.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-restore-xs',
			handler: function(s, e) {
				var rec = e.menuData.event;
				if (rec) me.restoreSchedEventUI(rec);
			}
		});
		me.addAct('copyEvent', {
			tooltip: null,
			handler: function(s, e) {
				var rec = WTU.itselfOrFirst(e.menuData.event);
				if (rec) me.moveSchedEventUI(true, rec);
			}
		});
		me.addAct('moveEvent', {
			tooltip: null,
			handler: function(s, e) {
				var rec = WTU.itselfOrFirst(e.menuData.event);
				if (rec) me.moveSchedEventUI(false, rec);
			}
		});
		me.addAct('printEvent', {
			text: WT.res('act-print.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-print',
			handler: function(s, e) {
				var rec = e.menuData.event;
				if (rec) me.printEventsDetail([rec.getId()]);
			}
		});
		
		me.addAct('refresh', {
			scale: hdscale,
			text: '',
			tooltip: WT.res('act-refresh.lbl'),
			iconCls: 'wt-icon-refresh',
			handler: function() {
				me.reloadEvents();
			}
		});
		me.addAct('printScheduler', {
			ignoreSize: true,
			scale: hdscale,
			text: null,
			tooltip: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print',
			handler: function() {
				var sched = me.scheduler(),
						bounds = sched.getActiveView().getViewBounds(),
						params = {
							startDate: Ext.Date.format(bounds.start, 'Y-m-d'),
							endDate: Ext.Date.format(bounds.end, 'Y-m-d'),
							view: me.multical().getHighlightMode()
						},
						url = WTF.processBinUrl(me.ID, 'PrintScheduler', params);
				Sonicle.URLMgr.openFile(url, {filename: 'agenda', newWindow: true});
			}
		});
	},
	
	initCxm: function() {
		var me = this;
		
		me.addRef('cxmRootFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('addCalendar'),
				me.getAct('addRemoteCalendar'),
				'-',
				{
					text: me.res('mni-viewFolders.lbl'),
					menu: {
						items: [
							me.getAct('viewAllFolders'),
							me.getAct('viewNoneFolders')
						]
					}
				},
				'-',
				me.getAct('editSharing'),
				me.getAct('manageHiddenCalendars')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.node,
							mine = rec.isPersonalNode(),
							rr = WTA.util.FoldersTree.toRightsObj(rec.get('_rrights'));
					me.getAct('addCalendar').setDisabled(!rr.MANAGE);
					me.getAct('addRemoteCalendar').setDisabled(!rr.MANAGE);
					me.getAct('editSharing').setDisabled(!rr.MANAGE);
					me.getAct('manageHiddenCalendars').setDisabled(mine);
				}
			}
		}));
		
		me.addRef('cxmFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('editCalendar'),
				me.getAct('deleteCalendar'),
				'-',
				{
					text: me.res('mni-viewFolder.lbl'),
					menu: {
						items: [
							me.getAct('viewThisFolderOnly'),
							me.getAct('viewAllFolders'),
							me.getAct('viewNoneFolders')
						]
					}
				},
				'-',
				me.getAct('editSharing'),
				me.getAct('viewCalendarLinks'),
				{
					text: me.res('mni-customizeFolder.lbl'),
					menu: {
						items: [
							me.getAct('hideCalendar'),
							me.getAct('calendarColor'),
							me.getAct('calendarSync')
						]
					}
				},
				me.getAct('syncRemoteCalendar'),
				'-',
				me.getAct('addEvent'),
				me.getAct('importEvents')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var FT = WTA.util.FoldersTree,
							rec = s.menuData.node,
							mine = rec.isPersonalNode(),
							rr = FT.toRightsObj(rec.get('_rrights')),
							fr = FT.toRightsObj(rec.get('_frights')),
							er = FT.toRightsObj(rec.get('_erights'));
					me.getAct('editCalendar').setDisabled(!fr.UPDATE);
					me.getAct('deleteCalendar').setDisabled(!fr.DELETE || rec.get('_builtIn'));
					me.getAct('editSharing').setDisabled(!rr.MANAGE);
					me.getAct('addEvent').setDisabled(!er.CREATE);
					me.getAct('importEvents').setDisabled(!er.CREATE);
					me.getAct('hideCalendar').setDisabled(mine);
					me.getAct('restoreCalendarColor').setDisabled(mine);
					me.getAct('syncRemoteCalendar').setDisabled(!Sonicle.webtop.calendar.view.Calendar.isRemote(rec.get('_provider')));
					
					var picker = s.down('menu#calendarColor').down('colorpicker');
					picker.menuData = s.menuData; // Picker's handler doesn't carry the event, injects menuData inside the picket itself
					picker.select(rec.get('_color'), true);
					s.down('menu#calendarSync').getComponent(rec.get('_sync')).setChecked(true);
				}
			}
		}));
		
		me.addRef('cxmScheduler', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('addSchedEvent'),
				me.getAct('printEvent')
			]
		}));
		
		me.addRef('cxmEvent', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('openEvent'),
				{
					text: me.res('mni-copyormove.lbl'),
					menu: {
						items: [
							me.getAct('moveEvent'),
							me.getAct('copyEvent')
						]
					}
				},
				me.getAct('printEvent'),
				'-',
				me.getAct('deleteEvent'),
				me.getAct('restoreEvent')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.event,
							hd = me.self.schedEventHideData(rec),
							er = WTA.util.FoldersTree.toRightsObj(rec.get('_rights')),
							brk = (rec.get('isBroken') === true);
					me.getAct('openEvent').setDisabled(hd);
					me.getAct('moveEvent').setDisabled(hd || !er.DELETE);
					me.getAct('deleteEvent').setDisabled(hd || !er.DELETE);
					me.getAct('restoreEvent').setDisabled(hd || !brk || !er.UPDATE);
				}
			}
		}));
	},
	
	onActivate: function() {
		var me = this,
				scheduler = me.scheduler();
		
		if(me.needsReload) {
			me.needsReload = false;
			if(scheduler.getStore().loadCount === 0) { // The first time...
				//...skip multical, it's already updated with now date
				scheduler.setStartDate(me.multical().getValue());
			} else {
				me.reloadEvents();
			}
		}
	},
	
	loadRootNode: function(pid, reloadItemsIf) {
		var me = this,
				sto = me.trFolders().getStore(),
				node;
		
		node = sto.findNode('_pid', pid, false);
		if (node) {
			sto.load({node: node});
			if (reloadItemsIf && node.get('checked')) me.reloadEvents();
		}
	},
	
	reloadEvents: function() {
		var me = this;
		if(me.isActive()) {
			me.multical().getStore().load();
			me.scheduler().getStore().load();
		} else {
			me.needsReload = true;
		}
	},
	
	changeView: function(view) {
		var me = this,
				sched = me.scheduler();
		me._activateMain(sched);
		me.multical().setHighlightMode(view);
		sched.setActiveView(view);
	},
	
	moveDate: function(direction) {
		var me = this,
				mc = me.multical();
		me._activateMain(me.scheduler());
		if(direction === 0) {
			mc.setToday();
		} else if(direction === -1) {
			mc.setPreviousDay();
		} else if(direction === 1) {
			mc.setNextDay();
		}
	},
	
	addCalendarUI: function(domainId, userId) {
		var me = this;
		me.addCalendar(domainId, userId, {
			callback: function(success, model) {
				if(success) me.loadRootNode(model.get('_profileId'));
			}
		});
	},
	
	addRemoteCalendarUI: function(profileId) {
		var me = this;
		me.setupRemoteCalendar(profileId, {
			callback: function(success, mo) {
				if(success) me.loadRootNode(profileId);
			}
		});
	},
	
	editCalendarUI: function(calendarId) {
		var me = this;
		me.editCalendar(calendarId, {
			callback: function(success, model) {
				if (success) me.loadRootNode(model.get('_profileId'), true);
			}
		});
	},
	
	deleteCalendarUI: function(node) {
		WT.confirm(this.res('calendar.confirm.delete', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if (bid === 'yes') node.drop();
		}, this);
	},
	
	syncRemoteCalendarUI: function(calendarId) {
		var me = this;
		WT.confirm(this.res('calendar.confirm.remotesync'), function(bid) {
			if (bid === 'yes') {
				me.syncRemoteCalendar(calendarId, false, {
					callback: function(success, json) {
						if (!success) WT.error(json.message);
					}
				});
			}
		}, this);
	},
	
	manageHiddenCalendarsUI: function(node) {
		var me = this,
				vw = me.createHiddenCalendars(node.getId());
		
		vw.on('viewcallback', function(s, success, json) {
			if (success) {
				Ext.iterate(json.data, function(pid) {
					me.loadRootNode(pid);
				});
			}
		});
		vw.showView();
	},
	
	hideCalendarUI: function(node) {
		var me = this;
		WT.confirm(this.res('calendar.confirm.hide', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if(bid === 'yes') {
				me.updateCalendarVisibility(node.get('_calId'), true, {
					callback: function(success) {
						if (success) {
							me.loadRootNode(node.get('_pid'));
							node.setActive(false);
						}
					}
				});
			}
		}, this);
	},
	
	updateCalendarColorUI: function(node, color) {
		var me = this;
		me.updateCalendarColor(node.get('_calId'), color, {
			callback: function(success) {
				if(success) {
					me.loadRootNode(node.get('_pid'));
					if (node.isActive()) me.reloadEvents();
				}
			}
		});
	},
	
	updateCalendarSyncUI: function(node, sync) {
		var me = this;
		me.updateCalendarSync(node.get('_calId'), sync, {
			callback: function(success) {
				if (success) {
					me.loadRootNode(node.get('_pid'));
				}
			}
		});
	},
	
	/*
	updateCheckedFoldersUI: function(node, checked) {
		var me = this;
		me.updateCheckedFolders(node.getId(), checked, {
			callback: function(success) {
				if(success) {
					if (node.get('_active')) {
						me.reloadEvents();
					} else {
						if (checked) node.setActive(checked);
					}
				}
			}
		});
	},
	*/
	
	importEventsUI: function(calendarId) {
		var me = this;
		me.importEvents(calendarId, {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	addEventUI: function(ownerId, calendarId, isPrivate, busy, reminder, start, end, allDay) {
		var me = this;
		me.addEvent(ownerId, calendarId, isPrivate, busy, reminder, start, end, allDay, {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	addEventOnUI: function(ownerId, calendarId, isPrivate, busy, reminder, day) {
		var me = this;
		me.addEventOn(ownerId, calendarId, isPrivate, busy, reminder, day, {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	openEventUI: function(edit, id) {
		var me = this;
		me.openEvent(edit, id, {
			callback: function(success) {
				if(success && edit) me.reloadEvents();
			}
		});
	},
	
	updateSchedEventUI: function(rec, newStartDate, newEndDate, newTitle) {
		var me = this,
				doFn = function(notify) {
					me.updateEvent(rec.getId(), newStartDate, newEndDate, newTitle, null, notify, {
						callback: function(success) {
							if (success) me.reloadEvents();
						}
					});
				};
		
		if (rec.get('isNtf')) { // TODO: convert 'isNtf' into a flag
			me.confirmOnInvitationFor('update', function(bid) {
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
	
	deleteSchedEventUI: function(rec) {
		var me = this;
		
		if (rec.get('isRecurring')) {
			me.confirmOnRecurringFor('delete', function(bid, value) {
				if (bid === 'ok') me._deleteSchedEventUI(rec, value);
			}, me);
		} else {
			WT.confirm(me.res('event.confirm.delete', Ext.String.ellipsis(rec.get('title'), 40)), function(bid) {
				if (bid === 'yes') me._deleteSchedEventUI(rec, null);
			}, me);
		}
	},
	
	_deleteSchedEventUI: function(rec, target) {
		var me = this,
				doFn = function(notify) {
					me.deleteEvent(rec.getId(), target, notify, {
						callback: function(success) {
							if (success) me.reloadEvents();
						}
					});
				};
		
		if (rec.get('isNtf')) { // TODO: convert 'isNtf' into a flag
			me.confirmOnInvitationFor('update', function(bid) {
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
	
	copySchedEventUI: function(rec, newStartDate) {
		var me = this,
				doFn = function(notify) {
					me.copyEvent(rec.getId(), newStartDate, null, notify, {
						callback: function(success) {
							if (success) me.reloadEvents();
						}
					});
				};
		
		if (rec.get('isNtf')) { // TODO: convert 'isNtf' into a flag
			me.confirmOnInvitationFor('save', function(bid) {
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
	
	moveSchedEventUI: function(copy, rec) {
		var me = this,
				doFn = function(notify, tgtCalendarId) {
					me.moveEvent(copy, rec.getId(), tgtCalendarId, notify, {
						callback: function(success) {
							if (success) me.reloadEvents();
						}
					});
				},
				vw = me.createCalendarChooser(copy);
		
		vw.on('viewok', function(s, calendarId) {
			if (copy && rec.get('isNtf')) { // TODO: convert 'isNtf' into a flag
				me.confirmOnInvitationFor('save', function(bid) {
					if (bid === 'yes') {
						doFn(true, calendarId);
					} else if (bid === 'no') {
						doFn(false, calendarId);
					}
				});
			} else {
				doFn(null, calendarId);
			}
		});
		vw.showView();
	},
	
	restoreSchedEventUI: function(rec) {
		var me = this;
		WT.confirm(me.res('event.recurring.confirm.restore'), function(bid) {
			if (bid === 'yes') {
				me.restoreEvent(rec.getId(), {
					callback: function(success) {
						if (success) me.reloadEvents();
					}
				});
			}
		}, me);
	},
	
	editShare: function(id) {
		var me = this,
				vw = WT.createView(me.ID, 'view.Sharing', {swapReturn: true});
		
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: id
				}
			});
		});
	},
	
	addCalendar: function(domainId, userId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Calendar', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {
					domainId: domainId,
					userId: userId,
					sync: me.getVar('defaultCalendarSync')
				}
			});
		});
	},
	
	setupRemoteCalendar: function(profileId, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.ID, 'view.CalendarRemoteWiz', {
				swapReturn: true,
				viewCfg: {
					data: {
						profileId: profileId
					}
				}
			});
		
		vw.on('viewclose', function(s) {
			Ext.callback(opts.callback, opts.scope || me, [true, s.getVMData()]);
		});
		vw.showView();
	},
	
	viewCalendarLinks: function(calendarId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.CalendarLinks', {swapReturn: true});
		
		vw.on('viewclose', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('view', {
				data: {
					calendarId: calendarId
				}
			});
		});
	},
	
	editCalendar: function(calendarId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Calendar', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					calendarId: calendarId
				}
			});
		});
	},
	
	syncRemoteCalendar: function(calendarId, full, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageCalendars', {
			params: {
				crud: 'sync',
				id: calendarId,
				full: full
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	updateCalendarVisibility: function(calendarId, hidden, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageHiddenCalendars', {
			params: {
				crud: 'update',
				calendarId: calendarId,
				hidden: hidden
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateCalendarColor: function(calendarId, color, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetCalendarColor', {
			params: {
				id: calendarId,
				color: color
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateCalendarSync: function(categoryId, sync, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetCalendarSync', {
			params: {
				id: categoryId,
				sync: sync
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateCheckedFolders: function(rootId, checked, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'UpdateCheckedFolders', {
			params: {
				rootId: rootId,
				checked: checked
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	prepareEventNewData: function(evt) {
		var me = this,
				n = WTA.util.FoldersTree.getTargetFolder(me.trFolders()),
				obj = {};
		
		obj._profileId = n.get('_pid');
		obj.calendarId = n.get('_calId');
		
		// TODO: abilitare supporto all'inserimento nel calendari condivisi
		
		/*
		if (!Ext.isDefined(evt.calendarId)) {
			var rn = me.getF3MyRoot(),
					n = me.getF3FolderByRoot(rn);
			if (!n) Ext.raise('errorrrrrrrr');
			obj._profileId = rn.get('_pid');
			obj.calendarId = n.get('_calId');
		} else {
			Ext.raise('Not yet supported');
			obj.calendarId = evt.calendarId;
		}
		*/
		
		obj.startDate = Ext.isDefined(evt.startDate) ? evt.startDate : new Date();
		obj.endDate = Ext.isDefined(evt.endDate) ? evt.endDate : new Date();
		obj.rstart = Ext.isDefined(evt.recStartDate) ? evt.recStartDate : evt.startDate;
		obj.timezone = Ext.isDefined(evt.timezone) ? evt.timezone : WT.getVar('timezone');
		if (Ext.isDefined(evt.allDay)) obj.allDay = evt.allDay;
		if (Ext.isDefined(evt.title)) obj.title = evt.title;
		if (Ext.isDefined(evt.description)) obj.description = evt.description;
		if (Ext.isDefined(evt.location)) obj.location = evt.location;
		if (Ext.isDefined(evt.isPrivate)) obj.isPrivate = evt.isPrivate;
		if (Ext.isDefined(evt.busy)) obj.busy = evt.busy;
		if (Ext.isDefined(evt.reminder)) obj.reminder = evt.reminder;
		return obj;
	},
	
	addEvent2: function(evt, opts) {
		evt = evt || {};
		opts = opts || {};
		var me = this,
				data = me.prepareEventNewData(evt),
				vw = WT.createView(me.ID, 'view.Event', {
					swapReturn: true,
					viewCfg: {
						showStatisticFields: me.getVar('eventStatFieldsVisible') === true
					}
				});	
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: data,
				dirty: opts.dirty
			});
		});
	},
	
	addEventOn: function(ownerId, calendarId, isPrivate, busy, reminder, day, opts) {
		var date = Sonicle.Date.copyTime(new Date(), day);
		this.addEvent(ownerId, calendarId, isPrivate, busy, reminder, date, date, false, opts);
	},
	
	addEvent: function(ownerId, calendarId, isPrivate, busy, reminder, start, end, allDay, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Event', {
					swapReturn: true,
					viewCfg: {
						showStatisticFields: me.getVar('eventStatFieldsVisible') === true
					}
				});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {
					calendarId: calendarId,
					isPrivate: isPrivate,
					busy: busy,
					reminder: reminder,
					startDate: start,
					endDate: end,
					timezone: WT.getVar('timezone'),
					allDay: allDay,
					rstart: start,
					_profileId: ownerId
				}
			});
		});
	},
	
	openEvent: function(edit, ekey, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Event', {
					swapReturn: true,
					viewCfg: {
						showStatisticFields: me.getVar('eventStatFieldsVisible') === true
					}
				}),
				mode = edit ? 'edit' : 'view';
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin(mode, {
				data: {
					id: ekey
				}
			});
		});
	},
	
	updateEvent: function(ekey, newStartDate, newEndDate, newTitle, target, notify, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
			params: {
				crud: 'update',
				target: target,
				notify: notify,
				id: ekey,
				newStart: Ext.isDate(newStartDate) ? Ext.Date.format(newStartDate, 'Y-m-d H:i:s') : null,
				newEnd: Ext.isDate(newEndDate) ? Ext.Date.format(newEndDate, 'Y-m-d H:i:s') : null,
				newTitle: newTitle
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	deleteEvent: function(ekey, target, notify, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
			params: {
				crud: 'delete',
				target: target,
				notify: notify,
				id: ekey
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	copyEvent: function(ekey, newStartDate, target, notify, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
			params: {
				crud: 'copy',
				target: target,
				notify: notify,
				id: ekey,
				newStart: Ext.isDate(newStartDate) ? Ext.Date.format(newStartDate, 'Y-m-d H:i:s') : null
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	moveEvent: function(copy, ekey, targetCalendarId, notify, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
			params: {
				crud: 'move',
				copy: copy,
				id: ekey,
				targetCalendarId: targetCalendarId,
				notify: notify
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	restoreEvent: function(ekey, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageEventsScheduler', {
			params: {
				crud: 'restore',
				id: ekey
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	importEvents: function(calendarId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.ImportEvents', {
					swapReturn: true,
					viewCfg: {
						calendarId: calendarId
					}
				});
		
		vw.on('dosuccess', function() {
			Ext.callback(opts.callback, opts.scope || me, [true]);
		});
		vw.showView();
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
				vw = WT.createView(me.ID, 'view.ErpExport', {swapReturn: true});
		vw.showView();
	},
	
	printEventsDetail: function(keys) {
		var me = this, url;
		url = WTF.processBinUrl(me.ID, 'PrintEventsDetail', {keys: WTU.arrayAsParam(keys)});
		Sonicle.URLMgr.openFile(url, {filename: 'events-detail', newWindow: true});
	},
	
	queryEvents: function(query) {
		var me = this,
			gp = me.gpResults(),
			isString = Ext.isString(query),
			value = isString ? query : query.value,
			obj = {
				allText: isString ? query : query.anyText,
				conditions: isString ? [] : query.conditionArray
			};
		
		gp.setTitle(Ext.String.format('{0}: {1}', WT.res('word.search'), value));
		me._activateMain(gp);
		WTU.loadWithExtraParams(gp.getStore(), {
			query: Ext.JSON.encode(obj)
		});
	},
	
	/*
	 * @private
	 */
	_activateMain: function(cmp) {
		this.getMainComponent().setActiveItem(cmp);
	},
	
	/*
	 * @private
	 */
	_isMainActive: function(id) {
		return (this.getMainComponent().getLayout().getActiveItem() === id);
	},
	
	confirmOnRecurringFor: function(type, cb, scope) {
		this.confirmOnRecurring(this.res('event.recurring.confirm.'+type), cb, scope);
	},
	
	confirmOnRecurring: function(msg, cb, scope) {
		var me = this;
		WT.confirm(msg, cb, scope, {
			buttons: Ext.Msg.OKCANCEL,
			instClass: 'Sonicle.webtop.calendar.ux.RecurringConfirmBox',
			instConfig: {
				thisText: me.res('confirm.recurrence.this'),
				sinceText: me.res('confirm.recurrence.since'),
				allText: me.res('confirm.recurrence.all')
			},
			config: {
				value: 'this'
			}
		});
	},
	
	confirmOnSeries: function(cb, scope) {
		var me = this;
		WT.confirm(me.res('event.recurring.confirm.series'), cb, scope, {
			buttons: Ext.Msg.YESNO
		});
	},
	
	confirmOnInvitationFor: function(type, cb, scope) {
		this.confirmOnInvitation(this.res('event.send.confirm.'+type), cb, scope);
	},
	
	confirmOnInvitation: function(msg, cb, scope) {
		var me = this;
		WT.confirm(msg, cb, scope, {
			buttons: Ext.Msg.YESNOCANCEL,
			config: {
				buttonText: {
					yes: me.res('event.send.confirm.yes'),
					no: me.res('event.send.confirm.no')
				}
			}
		});
	},
	
	/**
	 * @private
	 */
	createHiddenCalendars: function(rootNodeId) {
		var me = this;
		return WT.createView(me.ID, 'view.HiddenCalendars', {
			swapReturn: true,
			viewCfg: {
				action: 'ManageHiddenCalendars',
				extraParams: {
					crud: 'list',
					rootId: rootNodeId
				}
			}
		});
	},
	
	/**
	 * @private
	 */
	createCalendarChooser: function(copy) {
		var me = this;
		return WT.createView(me.ID, 'view.CalendarChooser', {
			swapReturn: true,
			viewCfg: {
				dockableConfig: {
					title: me.res(copy ? 'act-copyEvent.lbl' : 'act-moveEvent.lbl')
				},
				writableOnly: true
			}
		});
	},
	
	statics: {
		NOTAG_REMOTESYNC: 'remsync-',

		noTagRemoteSync: function(calendarId) {
			return this.NOTAG_REMOTESYNC + calendarId;
		},
		
		schedEventHideData: function(rec) {
			return rec.get('hideData') === true;
		}
	}
});
