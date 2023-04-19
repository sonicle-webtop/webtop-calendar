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
		'Sonicle.grid.column.Tag',
		'Sonicle.grid.plugin.StateResetMenu',
		'Sonicle.tree.Column',
		'WTA.ux.field.Search',
		'WTA.ux.menu.TagMenu',
		'Sonicle.webtop.calendar.model.FolderNode',
		'Sonicle.webtop.calendar.model.MultiCalDate',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.webtop.calendar.model.GridEvent'
	],
	uses: [
		'Sonicle.Data',
		'Sonicle.Utils',
		'Sonicle.picker.Color',
		'WTA.util.FoldersTree2',
		'WTA.ux.SelectTagsBox',
		'Sonicle.webtop.calendar.ux.RecurringConfirmBox',
		'Sonicle.webtop.calendar.view.FolderSharing',
		'Sonicle.webtop.calendar.view.Calendar',
		'Sonicle.webtop.calendar.view.CalendarLinks',
		'Sonicle.webtop.calendar.view.Event',
		'Sonicle.webtop.calendar.view.CalendarChooser',
		'Sonicle.webtop.calendar.view.HiddenCalendars',
		'Sonicle.webtop.calendar.ServiceApi',
		'Sonicle.webtop.calendar.portlet.Events'
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
		var me = this,
			SoS = Sonicle.String,
			tagsStore = WT.getTagsStore(),
			scfields = WTA.ux.field.Search.customFieldDefs2Fields(me.ID, me.getVar('cfieldsSearchable')),
			durRes = function(sym) { return WT.res('word.dur.'+sym); },
			durSym = [durRes('y'), durRes('d'), durRes('h'), durRes('m'), durRes('s')];
				
		//TODO: trovare una collocazione a questa chiamata
		Sonicle.upload.Uploader.registerMimeType('text/calendar', ['ical','ics','icalendar']);
		me.initActions();
		me.initCxm();
		
		me.on('activate', me.onActivate, me);
		me.onPushMessage('notifyReminder', function() {
			WT.info('reminder arrived');
		});
		
		me.onPushMessage('resourceAvailChange', function(msg) {
			var pl = msg.payload;
			WT.showNotification(me.ID, false, {
				tag: me.self.noTagResAvailChange(pl.id),
				title: pl.displayName,
				iconCls: SoS.join('-', 'wt-icon-resource', pl.type),
				body: pl.available ? me.res('not.calendar.resourceAvailChange.body.available') : me.res('not.calendar.resourceAvailChange.body.unavailable'),
				data: {
					resourceId: pl.id
				}
			});
		});
		me.onPushMessage('resourceReservationReply', function(msg) {
			var pl = msg.payload;
			WT.showNotification(me.ID, false, {
				tag: me.self.noTagResReservReply(pl.id, pl.eventUid),
				title: pl.displayName,
				iconCls: SoS.join('-', 'wt-icon-resource', pl.type),
				body: me.res('not.calendar.resourceReservationReply.body.' + pl.response, pl.eventTitle),
				bodyIconCls: pl.response === 'accepted' ? 'fas fa-check-circle' : 'fas fa-times-circle',
				data: {
					resourceId: pl.id
				}
			});
		});
		me.onPushMessage('resourceError', function(msg) {
			var pl = msg.payload,
				errc = pl.errorCode,
				body = 'Error';
			if ('notallowed' === errc) {
				body = me.res('not.calendar.resourceUsageUnauthorized.body');
			}
			WT.showNotification(me.ID, false, {
				tag: me.self.noResMessage(pl.id),
				title: pl.displayName,
				iconCls: SoS.join('-', 'wt-icon-resource', pl.type),
				body: body,
				data: {
					resourceId: pl.id
				}
			});
		});
		
		me.onPushMessage('remoteSyncResult', function(msg) {
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
					suggestionServiceId: me.ID,
					suggestionContext: 'mainsearch',
					enableQuerySaving: true,
					highlightKeywords: ['title', 'location'],
					fields: Ext.Array.push([
						{
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
						}/*, {
							name: 'any',
							type: 'string',
							textSink: true,
							label: me.res('fld-search.field.any.lbl')
						}*/, {
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
						}, {
							name: 'tag',
							type: 'tag',
							label: me.res('fld-search.field.tags.lbl'),
							customConfig: {
								store: WT.getTagsStore(), // This is filterable, let's do a separate copy!
								valueField: 'id',
								displayField: 'name',
								colorField: 'color',
								sourceField: 'source',
								sourceCls: 'wt-source'
							}
						}
					], scfields),
					tabs: Ext.isEmpty(scfields) ? undefined: [
						{
							title: WT.res('wtsearchfield.main.tit'),
							fields: ['title', 'location', 'description', 'after', 'before', 'busy', 'private', 'tag']
						}, {
							title: WT.res('wtsearchfield.customFields.tit'),
							fields: Ext.Array.pluck(scfields, 'name')
						}
					],
					tooltip: me.res('fld-search.tip'),
					searchTooltip: me.res('fld-search.tip'),
					emptyText: me.res('fld-search.emp'),
					listeners: {
						query: function(s, value, qObj) {
							if (Ext.isEmpty(value)) {
								me._activateMain(me.scheduler());
								//TODO: it would be nice clearing-up the grid but the following generates an error in extjs code
								//me.gpResults().removeAll(true);
							} else {
								me.queryEvents(qObj);
							}
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
					renderer: WTA.util.FoldersTree2.coloredCheckboxTreeRenderer({
						defaultText: me.res('trfolders.default'),
						getNodeText: function(node, val) {
							if ((node.isOrigin() && node.isPersonalNode()) || node.isGrouper()) {
								return me.resTpl(val);
							} else {
								return val;
							}
						},
						getNodeTdCls: function(node, val) {
							return (node.isResource() && node.get('_resourceAvail') === false) ? 'wt-text-striked' : '';
						}
					}),
					flex: 1
				}],
				listeners: {
					checkchange: function(n, ck) {
						n.refreshActive();
					},
					beforeselect: function(s, node) {
						if (me.treeSelEnabled === false) return false;
					},
					beforedeselect: function(s, node) {
						if (me.treeSelEnabled === false) return false;
					},
					itemclick: function(s, node, itm, i, e) {
						if (me.treeSelEnabled === false) {
							if (!e.getTarget(s.checkboxSelector, itm)) {
								s.setChecked(node, !node.get('checked'), e);
							}
						}
					},
					itemcontextmenu: function(s, node, itm, i, e) {
						if (node.isOrigin() || node.isGrouper()) {
							Sonicle.Utils.showContextMenu(e, me.getRef('cxmFolderRoot'), {node: node});
						} else {
							Sonicle.Utils.showContextMenu(e, me.getRef('cxmFolder'), {node: node});
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
				tagsStore: tagsStore,
				activeView: me.getVar('view'),
				startDay: WT.getStartDay(),
				use24HourTime: WT.getUse24HourTime(),
				viewCfg: {
					timezoneIconCls: 'fas fa-globe',
					privateIconCls: 'fas fa-lock',
					reminderIconCls: 'far fa-bell',
					attendeesIconCls: 'fas fa-users',
					recurrenceIconCls: 'fas fa-sync',
					recurrenceBrokenIconCls: 'fas fa-unlink',
					meetingIconCls: 'fas fa-video',
					commentsIconCls: 'far fa-comment-dots',
					todayText: me.res('scheduler.today'),
					moreText: me.res('scheduler.more'),
					ddCreateEventText: me.res('scheduler.ddcreateevent'),
					ddCopyEventText: me.res('scheduler.ddcopyevent'),
					ddMoveEventText: me.res('scheduler.ddmoveevent'),
					ddResizeEventText: me.res('scheduler.ddresizeevent'),
					ddDateFormat: 'j/m',
					scrollStartHour: Math.max(me.getVar('workdayStart').getHours()-1, 0),
					businessHoursStart: me.getVar('workdayStart').getHours(),
					businessHoursEnd: me.getVar('workdayEnd').getHours(),
					ddIncrement: me.getVar('timeResolution'),
					minEventDisplayMinutes: me.getVar('timeResolution'),
					hourHeight: me.getVar('timeResolution') === 15 ? 84 : 42
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
				tbar: {
					enableOverflow: true,
					items: [
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
					]
				},
				listeners: {
					rangeselect: function(s,dates,onComplete) {
						onComplete();
						var soDate = Sonicle.Date,
							node = WTA.util.FoldersTree2.getDefaultOrBuiltInFolder(me.trFolders()),
							ad;
						if (node) {
							ad = soDate.isMidnight(dates.startDate) && soDate.isMidnight(dates.endDate);
							me.addEventUI(node.getFolderId(), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), dates.startDate, dates.endDate, ad);
						}
					},
					eventdblclick: function(s, rec) {
						if (rec) {
							var er = WTA.util.FoldersTree2.toRightsObj(rec.get('_rights'));
							if (er.MANAGE) me.openEventUI(er.UPDATE, rec.get('id'));
						}
					},
					daydblclick: function(s, dt, allDay) {
						var soDate = Sonicle.Date,
							node = WTA.util.FoldersTree2.getDefaultOrBuiltInFolder(me.trFolders()),
							start = allDay ? soDate.copyTime(me.getVar('workdayStart'), dt) : dt,
							end = allDay ? soDate.copyTime(me.getVar('workdayEnd'), dt) : soDate.add(dt, {minutes: 30});
						if (node) {
							me.addEventUI(node.getFolderId(), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), start, end, allDay);
						}
					},
					eventcontextmenu: function(s, rec, e) {
						Sonicle.Utils.showContextMenu(e, me.getRef('cxmEvent'), {event: rec});
					},
					daycontextmenu: function(s, dt, allDay, e) {
						var soDate = Sonicle.Date,
								start = allDay ? soDate.copyTime(me.getVar('workdayStart'), dt) : dt,
								end = allDay ? soDate.copyTime(me.getVar('workdayEnd'), dt) : soDate.add(dt, {minutes: 30});
						Sonicle.Utils.showContextMenu(e, me.getRef('cxmScheduler'), {start: start, end: end, allDay: allDay});
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
					},
					beforeeventtitleupdate: function(s, rec, newTitle) {
						me.updateSchedEventUI(rec, null, null, newTitle);
						return false;
					}
				}
			}, {
				xtype: 'grid',
				reference: 'gpresults',
				stateful: true,
				stateId: me.buildStateId('gpeventsresults'),
				store: {
					model: 'Sonicle.webtop.calendar.model.GridEvent',
					proxy: WTF.apiProxy(me.ID, 'ManageGridEvents', 'events', {
						extraParams: {
							query: null
						}
					}),
					listeners: {
						load: function() {
							var el = me.gpResults().getEl(),
							searchComponent = me.getToolbar().lookupReference('fldsearch');
							searchComponent.highlight(el, '.x-grid-item-container');
						}
					}
				},
				columns: [
					{
						xtype: 'soiconcolumn',
						getIconCls: function(v,rec) {
							var ico = 'event-single';
							if (rec.get('isBroken')) ico = 'event-broken';
							if (rec.get('isRecurring')) ico = 'event-recurring';
							return WTF.cssIconCls(me.XID, ico);
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
						dataIndex: 'duration',
						header: me.res('gpevents.duration.lbl'),
						renderer : function(v, meta, rec) {
							return (v > 0) ? Sonicle.Date.humanReadableDuration(v, true, durSym) : '';
						},
						summaryType: 'sum',
						summaryRenderer: function(v) {
							return '<strong>Σ: ' + ((v > 0) ? Sonicle.Date.humanReadableDuration(v, true, durSym) : '') + '</strong>';
						},
						width: 80
					}, {
						xtype: 'sotagcolumn',
						dataIndex: 'tags',
						header: me.res('gpevents.tags.lbl'),
						tagsStore: tagsStore,
						width: 90
					}, {
						dataIndex: 'title',
						header: me.res('event.fld-title.lbl'),
						summaryType: 'count',
						summaryRenderer: function(v) {
							return '<strong>#: ' + v + '</strong>';
						},
						flex: 1
					}, {
						dataIndex: 'location',
						header: me.res('event.fld-location.lbl'),
						flex: 1
					}
				],
				features: [{
					ftype: 'summary'
				}],
				plugins: [
					{
						ptype: 'so-gridstateresetmenu',
						menuStateResetText: WT.res('act-clearColumnState.lbl'),
						menuStateResetTooltip: WT.res('act-clearColumnState.tip'),
						listeners: {
							stateresetclick: function(s, grid) {
								WT.clearState(grid);
							}
						}
					}
				],
				tools: [{
					type: 'close',
					callback: function() {
						me._activateMain(me.scheduler());
					}
				}],
				listeners: {
					rowdblclick: function(s, rec) {
						var er = WTA.util.FoldersTree2.toRightsObj(rec.get('_rights'));
						if (er.MANAGE) me.openEventUI(er.UPDATE, rec.get('id'));
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
		
		me.addAct('toolbox', 'erpExport', {
			tooltip: null,
			iconCls: 'wtcal-icon-export',
			handler: function() {
				me.erpExport();
			}
		});
		me.addAct('toolbox', 'manageTags', {
			text: WT.res('act-manageTags.lbl'),
			tooltip: WT.res('act-manageTags.tip'),
			iconCls: 'wt-icon-tag',
			handler: function() {
				me.showManageTagsUI();
			}
		});
		if (WT.isPermitted(WT.ID, 'CUSTOM_FIELDS', 'MANAGE')) {
			me.addAct('toolbox', 'manageCustomFields', {
				text: WT.res('act-manageCustomFields.lbl'),
				tooltip: WT.res('act-manageCustomFields.tip'),
				iconCls: 'wt-icon-customField',
				handler: function() {
					me.showCustomFieldsUI();
				}
			});
			me.addAct('toolbox', 'manageCustomPanels', {
				text: WT.res('act-manageCustomPanels.lbl'),
				tooltip: WT.res('act-manageCustomPanels.tip'),
				iconCls: 'wt-icon-customPanel',
				handler: function() {
					me.showCustomPanelsUI();
				}
			});
		}
		
		me.addAct('new', 'newEvent', {
			ignoreSize: true,
			handler: function() {
				me.getAct('addEvent').execute();
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
			iconCls: 'wt-icon-sharing',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.manageFolderSharingUI(node.getId());
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
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.addCalendarUI(node.getOwnerDomainId(), node.getOwnerUserId());
			}
		});
		me.addAct('addRemoteCalendar', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.addRemoteCalendarUI(node.getOwnerPid());
			}
		});
		me.addAct('viewCalendarLinks', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.viewCalendarLinks(node.getFolderId());
			}
		});
		me.addAct('editCalendar', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.editCalendarUI(node.getFolderId());
			}
		});
		me.addAct('deleteCalendar', {
			ignoreSize: true,
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
				if (node) me.syncRemoteCalendarUI(node.getFolderId());
			}
		});
		if (me.hasAuditUI()) {
			me.addAct('calendarAuditLog', {
				text: WT.res('act-auditLog.lbl'),
				tooltip: null,
				handler: function(s, e) {
					var node = e.menuData.node;
					if (node) me.openAuditUI(node.getFolderId(), 'CALENDAR');
				}
			});
		}
		me.addAct('importEvents', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.importEventsUI(node.getFolderId());
			}
		});
		me.addAct('applyTags', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.applyCalendarTagsUI(node);
			}
		});
		me.addAct('tags', {
			text: me.res('mni-tags.lbl'),
			tooltip: null,
			menu: {
				xtype: 'wttagmenu',
				bottomStaticItems: [
					'-',
					me.addAct('manageTags', {
						tooltip: null,
						handler: function(s, e) {
							var rec = WTU.itselfOrFirst(e.menuData.event);
							if (rec) me.manageEventItemsTagsUI([rec]);
						}
					})
				],
				restoreSelectedTags: function(menuData) {
					return me.toMutualTags([menuData.event]);
				},
				listeners: {
					tagclick: function(s, tagId, checked, itm, e) {
						var rec = WTU.itselfOrFirst(e.menuData.event),
							ids = Sonicle.Data.collectValues([rec]);
						me.updateEventItemsTags(ids, !checked ? 'unset' : 'set', [tagId], {
							callback: function(success) {
								if (success) me.reloadEvents();
							}
						});
					}
				}
			}
		});
		me.addAct('calendarColor', {
			text: me.res('mni-calendarColor.lbl'),
			tooltip: null,
			menu: {
				showSeparator: false,
				itemId: 'calendarColor',
				items: [
					{
						xtype: 'socolorpicker',
						colors: WT.getColorPalette('default'),
						tilesPerRow: 11,
						listeners: {
							select: function(s, color) {
								var node = s.menuData.node;
								me.getRef('cxmFolder').hide();
								if (node) me.updateCalendarColorUI(node, Sonicle.String.prepend(color, '#', true));
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
				items: [
					{
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
				if (node) WTA.util.FoldersTree2.activateSingleFolder(node.getFolderRootNode(), node.getId());
			}
		});
		me.addAct('viewAllFolders', {
			ignoreSize: true,
			tooltip: null,
			iconCls: 'wt-icon-select-all',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree2.setActiveAllFolders(node.getFolderRootNode(), true);
			}
		});
		me.addAct('viewNoneFolders', {
			ignoreSize: true,
			tooltip: null,
			iconCls: 'wt-icon-select-none',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree2.setActiveAllFolders(node.getFolderRootNode(), false);
			}
		});
		me.addAct('addSchedEvent', {
			text: me.res('act-addEvent.lbl'),
			tooltip: null,
			iconCls: 'wtcal-icon-addEvent',
			handler: function(s, e) {
				var node = WTA.util.FoldersTree2.getDefaultOrBuiltInFolder(me.trFolders()),
					mdata = e.menuData;
				if (node) {
					if (mdata.start && mdata.end) {
						me.addEventUI(node.getFolderId(), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), mdata.start, mdata.end, mdata.allDay);
					} else {
						me.addEventOnUI(node.getFolderId(), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), me.multical().getValue());
					}
				}
			}
		});
		me.addAct('addEvent', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var day = me.multical().getValue(),
					folderId = (e && e.menuData) ? e.menuData.node.getFolderId() : null,
					node = WTA.util.FoldersTree2.getFolderForAdd(me.trFolders(), folderId);
				if (node) me.addEventOnUI(node.getFolderId(), node.get('_isPrivate'), node.get('_defBusy'), node.get('_defReminder'), day);
			}
		});
		me.addAct('openEvent', {
			text: WT.res('act-open.lbl'),
			tooltip: null,
			handler: function(s, e) {
				var rec = WTU.itselfOrFirst(e.menuData.event), ir;
				if (rec) {
					ir = WTA.util.FoldersTree2.toRightsObj(rec.get('_rights'));
					me.openEventUI(ir.UPDATE, rec.get('id'));
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
			iconCls: 'wtcal-icon-rejoinSeries',
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
		if (me.hasAuditUI()) {
			me.addAct('eventAuditLog', {
				text: WT.res('act-auditLog.lbl'),
				tooltip: null,
				handler: function(s, e) {
					var rec = e.menuData.event;
					me.openAuditUI(rec.get('eventId'), 'EVENT');
				},
				scope: me
			});
		}
		
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
		
		me.addRef('cxmFolderRoot', Ext.create({
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
					var node = s.menuData.node,
						mine = node.isPersonalNode(),
						or = node.getOriginRights();
					me.getAct('addCalendar').setDisabled(!or.MANAGE);
					me.getAct('addRemoteCalendar').setDisabled(!or.MANAGE);
					me.getAct('editSharing').setDisabled(!or.MANAGE);
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
							me.getAct('calendarSync'),
							{
								itemId: 'defaultCalendar',
								text: me.res('mni-defaultCalendar.lbl'),
								group: 'defaultCalendar',
								checked: false,
								listeners: {
									click: function(s, e) {
										var node = e.menuData.node;
										if (node && s.checked) me.updateDefaultCalendarUI(node);
									}
								}
							}
						]
					}
				},
				me.getAct('syncRemoteCalendar'),
				'-',
				me.getAct('applyTags'),
				me.hasAuditUI() ? me.getAct('calendarAuditLog'): null,
				'-',
				me.getAct('addEvent'),
				me.getAct('importEvents')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var node = s.menuData.node,
						mine = node.isPersonalNode(),
						fr = node.getFolderRights(),
						ir = node.getItemsRights();
					
					me.getAct('editCalendar').setDisabled(!fr.UPDATE);
					me.getAct('deleteCalendar').setDisabled(!fr.DELETE || node.isBuiltInFolder());
					me.getAct('editSharing').setDisabled(!fr.MANAGE);
					me.getAct('addEvent').setDisabled(!ir.CREATE);
					me.getAct('importEvents').setDisabled(!ir.CREATE);
					me.getAct('hideCalendar').setDisabled(mine);
					me.getAct('restoreCalendarColor').setDisabled(mine);
					me.getAct('syncRemoteCalendar').setDisabled(!Sonicle.webtop.calendar.view.Calendar.isRemote(node.get('_provider')));
					me.getAct('applyTags').setDisabled(!ir.UPDATE);
					
					var picker = s.down('menu#calendarColor').down('colorpicker');
					picker.menuData = s.menuData; // Picker's handler doesn't carry the event, injects menuData inside the picket itself
					picker.select(node.getFolderColor(), true);
					s.down('menu#calendarSync').getComponent(node.get('_sync')).setChecked(true);
					
					var defltCmp = s.down('menuitem#defaultCalendar');
					defltCmp.setChecked(node.isDefaultFolder());
					defltCmp.setDisabled(!ir.CREATE);
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
				me.getAct('tags'),
				me.hasAuditUI() ? me.getAct('eventAuditLog') : null,
				'-',
				me.getAct('deleteEvent'),
				me.getAct('restoreEvent')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.event,
						ir = WTA.util.FoldersTree2.toRightsObj(rec.get('_rights')),
						brk = (rec.get('isBroken') === true);
					
					me.getAct('openEvent').setDisabled(!ir.MANAGE);
					me.getAct('moveEvent').setDisabled(!ir.DELETE);
					me.getAct('tags').setDisabled(!ir.UPDATE);
					me.getAct('deleteEvent').setDisabled(!ir.DELETE);
					me.getAct('restoreEvent').setDisabled(!brk || !ir.UPDATE);
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
	
	loadOriginNode: function(originPid, reloadItemsIf) {
		var me = this,
			WFT = WTA.util.FoldersTree2,
			tree = me.trFolders(),
			node = WFT.getOrigin(tree, originPid),
			fnode;
		
		// If node was not found, passed profileId may be the owner 
		// of a Resource: get the first match and check if it was found 
		// from a resource grouper parent.
		if (!node) {
			fnode = WFT.getFolderByOwnerProfile(tree, originPid);
			if (fnode && fnode.isResource() && fnode.parentNode.isGrouper()) node = fnode.parentNode;
		}
		if (node) {
			tree.getStore().load({node: node});
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
	
	showManageTagsUI: function() {
		var me = this,
				vw = WT.createView(WT.ID, 'view.Tags', {
					swapReturn: true,
					viewCfg: {
						enableSelection: false
					}
				});
		vw.on('viewclose', function(s) {
			if (s.syncCount > 0) me.reloadEvents();
		});
		vw.showView();
	},
	
	showCustomFieldsUI: function() {
		var me = this;
		WT.createView(WT.ID, 'view.CustomFields', {
			swapReturn: true,
			preventDuplicates: true,
			viewCfg: {
				serviceId: me.ID,
				serviceName: me.getName()
			}
		}).showView();
	},
	
	showCustomPanelsUI: function() {
		var me = this;
		WT.createView(WT.ID, 'view.CustomPanels', {
			swapReturn: true,
			preventDuplicates: true,
			viewCfg: {
				serviceId: me.ID,
				serviceName: me.getName()
			}
		}).showView();
	},
	
	addCalendarUI: function(domainId, userId) {
		var me = this;
		me.addCalendar(domainId, userId, {
			callback: function(success, model) {
				if (success) me.loadOriginNode(model.get('_profileId'));
			}
		});
	},
	
	addRemoteCalendarUI: function(profileId) {
		var me = this;
		me.setupRemoteCalendar(profileId, {
			callback: function(success, mo) {
				if (success) me.loadOriginNode(profileId);
			}
		});
	},
	
	editCalendarUI: function(calendarId) {
		var me = this;
		me.editCalendar(calendarId, {
			callback: function(success, model) {
				if (success) me.loadOriginNode(model.get('_profileId'), true);
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
					callback: function(success, data, json) {
						WT.handleError(success, json);
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
				Ext.iterate(json.data, function(originPid) {
					me.loadOriginNode(originPid);
				});
			}
		});
		vw.showView();
	},
	
	hideCalendarUI: function(node) {
		var me = this;
		WT.confirm(this.res('calendar.confirm.hide', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if(bid === 'yes') {
				me.updateCalendarVisibility(node.getFolderId(), true, {
					callback: function(success) {
						if (success) {
							me.loadOriginNode(node.getOwnerPid());
							node.setActive(false);
						}
					}
				});
			}
		}, this);
	},
	
	updateCalendarColorUI: function(node, color) {
		var me = this;
		me.updateCalendarColor(node.getFolderId(), color, {
			callback: function(success) {
				if (success) {
					me.loadOriginNode(node.getOwnerPid());
					if (node.isActive()) me.reloadEvents();
				}
			}
		});
	},
	
	updateCalendarSyncUI: function(node, sync) {
		var me = this;
		me.updateCalendarSync(node.getFolderId(), sync, {
			callback: function(success) {
				if (success) {
					me.loadOriginNode(node.getOwnerPid());
				}
			}
		});
	},
	
	updateDefaultCalendarUI: function(node) {
		var me = this;
		me.updateDefaultCalendar(node.getFolderId(), {
			callback: function(success, data) {
				if (success) {
					var FT = WTA.util.FoldersTree2,
						tree = me.trFolders(),
						node = FT.getFolderById(tree, data);
					if (node) FT.setFolderAsDefault(tree, node.getId());
				}
			}
		});
	},
	
	applyCalendarTagsUI: function(node) {
		var me = this, op;
		WT.confirmSelectTags(function(bid, value) {
			if (bid === 'yes' || bid === 'no') {
				op = (bid === 'yes') ? 'set' : ((bid === 'no') ? 'unset' : ''); 
				WT.confirm(me.res('calendar.confirm.tags.' + op, Ext.String.ellipsis(node.get('text'), 40)), function(bid2) {
					if (bid2 === 'yes') {
						me.updateCalendarTags(node.getFolderId(), op, value, {
							callback: function(success) {
								if (success) me.reloadEvents();
							}
						});
					}
				}, this);
			}
		}, me);
	},
	
	importEventsUI: function(calendarId) {
		var me = this;
		me.importEvents(calendarId, {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	manageEventItemsTagsUI: function(recs) {
		var me = this,
				ids = Sonicle.Data.collectValues(recs),
				tags = me.toMutualTags(recs),
				vw = WT.createView(WT.ID, 'view.Tags', {
					swapReturn: true,
					viewCfg: {
						data: {
							selection: tags
						}
					}
				});
		vw.on('viewok', function(s, data) {
			if (Sonicle.String.difference(tags, data.selection).length > 0) {
				me.updateEventItemsTags(ids, 'reset', data.selection, {
					callback: function(success) {
						if (success) me.reloadEvents();
					}
				});
			}	
		});
		vw.showView();
	},
	
	addEventUI: function(calendarId, isPrivate, busy, reminder, start, end, allDay) {
		var me = this;
		me.addEvent(calendarId, isPrivate, busy, reminder, start, end, allDay, {
			callback: function(success) {
				if(success) me.reloadEvents();
			}
		});
	},
	
	addEventOnUI: function(calendarId, isPrivate, busy, reminder, day) {
		var me = this;
		me.addEventOn(calendarId, isPrivate, busy, reminder, day, {
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
	
	manageFolderSharingUI: function(nodeId) {
		var me = this,
			vw = WT.createView(me.ID, 'view.FolderSharing', {swapReturn: true});
	
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: nodeId
				}
			});
		});
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
		WT.ajaxReq(me.ID, 'ManageCalendar', {
			params: {
				crud: 'sync',
				id: calendarId,
				full: full
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	updateDefaultCalendar: function(calendarId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetDefaultCalendar', {
			params: {
				id: calendarId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	updateCalendarSync: function(calendarId, sync, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetCalendarSync', {
			params: {
				id: calendarId,
				sync: sync
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	updateCalendarTags: function(calendarId, op, tagIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageCalendar', {
			params: {
				crud: 'updateTag',
				id: calendarId,
				op: op,
				tags: WTU.arrayAsParam(tagIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	addEventWithData: function(data, opts) {
		opts = opts || {};
		var me = this,
			ret = me.parseEventApiData(data) || {},
			vw = WT.createView(me.ID, 'view.Event', {
				swapReturn: true,
				viewCfg: {
					uploadTag: opts.uploadTag,
					showStatisticFields: me.getVar('eventStatFieldsVisible') === true
				}
			});	
		
		//TODO: delete _profileId when is not required anymore in Event view
		ret[0]['_profileId'] = WTA.util.FoldersTree2.getFolderById(me.trFolders(), ret[0].calendarId).getOwnerPid();
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: ret[0],
				cfData: ret[1],
				dirty: opts.dirty
			});
		});
		return vw;
	},
	
	addEventOn: function(calendarId, isPrivate, busy, reminder, day, opts) {
		var date = Sonicle.Date.copyTime(new Date(), day);
		this.addEvent(calendarId, isPrivate, busy, reminder, date, date, false, opts);
	},
	
	addEvent: function(calendarId, isPrivate, busy, reminder, start, end, allDay, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.ID, 'view.Event', {
				swapReturn: true,
				viewCfg: {
					uploadTag: opts.uploadTag,
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
					//TODO: delete _profileId when is not required anymore in Event view
					_profileId: WTA.util.FoldersTree2.getFolderById(me.trFolders(), calendarId).getOwnerPid()
				}
			});
		});
		return vw;
	},
	
	openEvent: function(edit, ekey, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.ID, 'view.Event', {
				swapReturn: true,
				viewCfg: {
					uploadTag: opts.uploadTag,
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
		return vw;
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
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
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
			queryText = isString ? query : query.value,
			obj = {
				allText: isString ? query : query.anyText,
				conditions: isString ? [] : query.conditionArray
			};
		
		gp.setTitle(Ext.String.format('{0}: {1}', WT.res('word.search'), queryText));
		me._activateMain(gp);
			Sonicle.Data.loadWithExtraParams(gp.getStore(), {
			query: Ext.JSON.encode(obj),
			queryText: queryText
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
	
	updateEventItemsTags: function(eventKeys, op, tagIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageGridEvents', {
			params: {
				crud: 'updateTag',
				keys: WTU.arrayAsParam(eventKeys),
				op: op,
				tags: WTU.arrayAsParam(tagIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	openAuditUI: function(referenceId, context) {
		var me = this,
				tagsStore = WT.getTagsStore();
		
		WT.getServiceApi(WT.ID).showAuditLog(me.ID, context, null, referenceId, function(data) {
			var str = '', logDate, actionString, eldata;
			
			Ext.each(data,function(el) {
				logDate = Ext.Date.parseDate(el.timestamp, 'Y-m-d H:i:s');
				actionString = Ext.String.format('auditLog.{0}.{1}', context, el.action);
				str += Ext.String.format('{0} - {1} - {2} ({3})\n', Ext.Date.format(logDate, WT.getShortDateTimeFmt()), me.res(actionString), el.userName, el.userId);
				eldata = Ext.JSON.decode(el.data);
				
				if (el.action === 'TAG' && eldata) {
					if (eldata.set) {
						Ext.each(eldata.set, function(tag) {
							var r = tagsStore.findRecord('id', tag);
							var desc = r ? r.get('name') : tag;
							str += Ext.String.format('\t+ {0}\n', desc);
						});
					}
					if (eldata.unset) {
						Ext.each(eldata.unset, function(tag) {
							var r = tagsStore.findRecord('id', tag);
							var desc = r ? r.get('name') : tag;
							str += Ext.String.format('\t- {0}\n', desc);
						});
					}
				}
			});
			return str;
		});
	},
	
	privates: {
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
		
		parseEventApiData: function(data) {
			data = data || {};
			var obj = {}, cfobj;
			
			obj.calendarId = WTA.util.FoldersTree2.getFolderForAdd(this.trFolders(), data.calendarId).getFolderId();
			obj.startDate = Ext.isDefined(data.startDate) ? data.startDate : new Date();
			obj.endDate = Ext.isDefined(data.endDate) ? data.endDate : new Date();
			obj.rstart = Ext.isDefined(data.recStartDate) ? data.recStartDate : data.startDate;
			obj.timezone = !Ext.isEmpty(data.timezone) ? data.timezone : WT.getTimezone();
			if (Ext.isDefined(data.allDay)) obj.allDay = data.allDay;
			if (Ext.isDefined(data.title)) obj.title = data.title;
			if (Ext.isDefined(data.description)) obj.description = data.description;
			if (Ext.isDefined(data.location)) obj.location = data.location;
			if (Ext.isDefined(data.visibility)) obj.isPrivate = (data.visibility === 'private');
			if (Ext.isDefined(data.busy)) obj.busy = data.busy;
			if (Ext.isDefined(data.reminder)) obj.reminder = data.reminder;
			if (Ext.isDefined(data.masterDataId)) obj.masterDataId = data.masterDataId;
			if (Ext.isDefined(data.statMasterDataId)) obj.statMasterDataId = data.statMasterDataId;
			if (Ext.isDefined(data.activityId)) obj.activityId = data.activityId;
			if (Ext.isDefined(data.causalId)) obj.causalId = data.causalId;
			if (Ext.isDefined(data.tags)) {
				if (Ext.isArray(data.tags)) {
					obj.tags = Sonicle.String.join('|', data.tags);
				} else if (Ext.isString(data.tags)) {
					obj.tags = data.tags;
				}
			}
			if (Ext.isDefined(data.customFields) && Ext.isObject(data.customFields)) {
				cfobj = data.customFields;
			}
			
			return [obj, cfobj];
		},
		
		createHiddenCalendars: function(originNodeId) {
			var me = this;
			return WT.createView(me.ID, 'view.HiddenCalendars', {
				swapReturn: true,
				viewCfg: {
					action: 'ManageHiddenCalendars',
					extraParams: {
						node: originNodeId
					}
				}
			});
		},
		
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
		
		toMutualTags: function(recs) {
			var arr, ids;
			Ext.iterate(recs, function(rec) {
				ids = Sonicle.String.split(rec.get('tags'), '|');
				if (!arr) {
					arr = ids;
				} else {
					arr = Ext.Array.intersect(arr, ids);
				}
				if (arr.length === 0) return false;
			});
			return arr;
		}
	},
	
	statics: {
		NOTAG_REMOTESYNC: 'remsync-',

		noTagRemoteSync: function(calendarId) {
			return this.NOTAG_REMOTESYNC + calendarId;
		},
		
		NOTAG_RESAVAILCHANGE: 'resavailchange-',
		
		noTagResAvailChange: function(resourceId) {
			return this.NOTAG_RESAVAILCHANGE + resourceId;
		},
		
		NOTAG_RESRESERVREPLY: 'resreservreply-',
		
		noTagResReservReply: function(resourceId, eventUid) {
			return this.NOTAG_RESRESERVREPLY + resourceId + '-' + eventUid;
		}
	}
});
