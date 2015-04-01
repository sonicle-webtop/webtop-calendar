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
		//'Sonicle.webtop.calendar.Tool',
		'Sonicle.calendar.Panel',
		'Sonicle.MultiCalendar',
		'Sonicle.webtop.calendar.model.TreeCal',
		'Sonicle.webtop.calendar.model.MultiCalDate',
		'Sonicle.calendar.data.MemoryCalendarStore',
		'Sonicle.calendar.data.MemoryEventStore',
		'Sonicle.webtop.calendar.model.Event',
		'Sonicle.calendar.data.Events',
		'Sonicle.calendar.data.Calendars',
		'Sonicle.webtop.calendar.view.Calendar'
	],
	
	selectedGroup: null,
	
	init: function() {
		var me = this;
		
		me._selectMyGroup();
		me.initActions();
		me.initCxm();

/*
		this.addAction('new', 'testaction', {
			tooltip: null,
			handler: function () {
				alert('Calendar testaction clicked');
			},
			scope: this
		});
		*/
		
		/*
		var tb = Ext.create({
			xtype: 'toolbar',
			items: [{
					xtype: 'button',
					text: 'Aggiungi cal.',
					handler: function () {
						calendarTools.addPersonalCalendar();

					}
				}, {
					xtype: 'button',
					text: 'Aggiorna cal.',
					handler: function () {
						calendarTools.editPersonalCalendar();
					}
				}, {
					xtype: 'button',
					text: 'Cancella cal.',
					handler: function () {
						calendarTools.deletePersonalCalendar();
					}
				}, {
					xtype: 'button',
					text: 'Visualizza',
					handler: function () {
						calendarTools.viewOnlyPersonalCalendar();
					}
				}
			]
		});
		*/
		
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
				me.getAction('weekagview'),
				me.getAction('monthview')
			]
			
			/*
			items: [{
				xtype: 'segmentedbutton',
				allowToggle: false,
				items: [
					me.getAction('today'),
					me.getAction('previousday'),
					me.getAction('nextday')
				]
			},{
				xtype: 'segmentedbutton',
				allowToggle: true,
				items: [
					me.getAction('dayview'),
					me.getAction('week5view'),
					me.getAction('weekview'),
					me.getAction('weekagview'),
					me.getAction('monthview')
				],
				listeners: {
					toggle: function(s,btn) {
						me.changeView(btn.getItemId());
						//me.getToolComponent().changeView(btn.getItemId());
						//me.getMainComponent().setActiveView(btn.getItemId());
					}
				}
			}]
		*/
		}));
		/*
		me.setToolComponent(Ext.create('Sonicle.webtop.calendar.Tool', {
			mys: me,
			title: me.getName(),
			listeners: {
				datechanged: function(s,date) {
					me.getMainComponent().setStartDate(date);
				}
			}
		}));
		*/
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
							startDay: me.getOption('startDay'),
							highlightMode: me.getOption('view'),
							width: 184,
							height: 298,
							store: {
								model: 'Sonicle.webtop.calendar.model.MultiCalDate',
								proxy: WT.Util.proxy(me.ID, 'GetEventDates', 'dates')
							},
							listeners: {
								change: function(s, nv) {
									me.getMainComponent().setStartDate(nv);
								}
							}
						}))
					]
				},
				me.addRef('treecal', Ext.create({
					region: 'center',
					xtype: 'treepanel',
					border: false,
					useArrows: true,
					rootVisible: false,
					store: {
						autoLoad: true,
						autoSync: true,
						model: 'Sonicle.webtop.calendar.model.TreeCal',
						proxy: WT.Util.apiProxy(me.ID, 'ManageCalendarsTree', 'children', {
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
					/*
					columns: [{
						xtype: 'treecolumn',
						flex: 1,
						dataIndex: 'text',
						renderer: function(val, meta, rec, ri, ci, sto, view) {
							Ext.defer(function() {
								if(rec.get('leaf')) {
									var node = view.getNode(rec);
									if(node) Ext.get(node).down('.x-tree-icon').setStyle('background-color', rec.get('calColor'));
								}
							}, 100);
							return val;
						}
					}],
					*/
					listeners: {
						checkchange: function(n, ck) {
							me._showHideCal(n, ck);
						},
						select: function(s, rec, i) {
							console.log('selectedGroup: '+rec.get('_groupId'));
							me.selectedGroup = rec.get('_groupId');
						},
						itemcontextmenu: function(vw, rec, itm, i, e) {
							// TODO: disabilitare azioni se readonly
							if(rec.get('_nodeType') === 'group') {
								me._showCxm(me.getRef('cxmCalGroup'), e);
							} else {
								me.getAction('deleteCalendar').setDisabled(rec.get('builtIn'));
								me._showCxm(me.getRef('cxmCal'), e);
							}
						}
					}
				}))
			]
		}));
		
		me.setMainComponent(Ext.create({
			xtype: 'calendarpanel',
			activeView: me.getOption('view'),
			startDay: me.getOption('startDay'),
			use24HourTime: WT.getOption('use24HourTime'),
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
				proxy: WT.Util.apiProxy(me.ID, 'ManageEventsScheduler', 'events')
			},
			listeners: {
				rangeselect: function(s,dates,onComplete) {
					onComplete();
					var cal = me.getSelectedCalendar();
					if(cal) me.addEvent(cal.get('_groupId'), cal.getId(), dates.startDate, dates.endDate);
				}
			}
		}));
	},
	
	/**
	 * Resets selected group to my
	 */
	_selectMyGroup: function() {
		this.selectedGroup = WT.getOption('principal');
	},
	
	
	
	
	
	refreshEvents: function() {
		var me = this;
		me.getRef('multical').getStore().load();
		me.getMainComponent().getStore().load();
	},
	
	_showCxm: function(menu, evt) {
		evt.preventDefault();
		menu.showAt(evt.getXY());
	},
	
	initActions: function() {
		var me = this,
				view = me.getOption('view');
		
		me.addAction('new', 'addEvent', {
			handler: function() {
				var cal = me.getSelectedCalendar();
				if(cal) me.addEventAtNow(cal.get('_groupId'), cal.getId());
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
		me.addAction('weekagview', {
			itemId: 'wa',
			pressed: view === 'wa',
			toggleGroup: 'view',
			handler: function() {
				me.changeView('wa');
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
		me.addAction('addCalendar', {
			handler: function() {
				var node = me._getSelectedNode();
				if(node) me.addCalendar(node.get('_domainId'), node.get('_userId'));
			}
		});
		me.addAction('editCalendar', {
			handler: function() {
				var node = me._getSelectedNode();
				if(node) me.editCalendar(node.getId());
			}
		});
		me.addAction('deleteCalendar', {
			handler: function() {
				var node = me._getSelectedNode();
				if(node) me.deleteCalendar(node);
			}
		});
		me.addAction('importEvents', {
			handler: function() {
				// TODO: implementare importazione
				WT.warn('To be implemented!');
			}
		});
		me.addAction('viewAllCalendars', {
			iconCls: 'wt-icon-select-all-xs',
			handler: function() {
				me._showHideAllCals(me._getSelectedCalGroupNode(), true);
			}
		});
		me.addAction('viewNoneCalendars', {
			iconCls: 'wt-icon-select-none-xs',
			handler: function() {
				me._showHideAllCals(me._getSelectedCalGroupNode(), false);
			}
		});
		me.addAction('addEvent', {
			handler: function() {
				var cal = me.getSelectedCalendar();
				if(cal) me.addEventAtNow(cal.get('_groupId'), cal.getId());
			}
		});
	},
	
	initCxm: function() {
		var me = this;
		
		me.addRef('cxmCalGroup', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('addCalendar'),
				'-',
				me.getAction('addEvent')
				//TODO: azioni altri servizi?
			]
		}));
		
		me.addRef('cxmCal', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('editCalendar'),
				me.getAction('deleteCalendar'),
				'-',
				me.getAction('addCalendar'),
				me.getAction('importEvents'),
				'-',
				me.getAction('viewAllCalendars'),
				me.getAction('viewNoneCalendars'),
				'-',
				me.getAction('addEvent')
				//TODO: azioni altri servizi?
			]
		}));
	},
	
	changeView: function(view) {
		var me = this;
		me.getRef('multical').setHighlightMode(view);
		me.getMainComponent().setActiveView(view);
	},
	
	moveDate: function(direction) {
		var mc = this.getRef('multical');
		if(direction === 0) {
			mc.setToday();
		} else if(direction === -1) {
			mc.setPreviousDay();
		} else if(direction === 1) {
			mc.setNextDay();
		}
	},
	
	_showHideCal: function(node, show) {
		node.beginEdit();
		node.set('_visible', show);
		node.endEdit();
	},
	
	_showHideAllCals: function(parent, show) {
		var me = this,
				store = parent.getTreeStore();
		
		store.suspendAutoSync();
		parent.cascadeBy(function(n) {
			if(n !== parent) {
				n.set('checked', show);
				me._showHideCal(n, show);
			}
		});
		store.resumeAutoSync();
		store.sync();
	},
	
	addCalendar: function(domainId, userId) {
		var me = this,
				wnd = this._buildCalendarWnd();
		
		wnd.getComponent(0).on('viewsave', me.onCalendarViewSave, me);
		wnd.show(false, function() {
			wnd.getComponent(0).beginNew({
				data: {
					domainId: domainId,
					userId: userId
				}
			});
		});
	},
	
	editCalendar: function(calendarId) {
		var me = this,
				wnd = this._buildCalendarWnd();
		
		wnd.getComponent(0).on('viewsave', me.onCalendarViewSave, me);
		wnd.show(false, function() {
			wnd.getComponent(0).beginEdit({
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
	
	addEventAtNow: function(groupId, calendarId) {
		var now = new Date();
		this.addEvent(groupId, calendarId, now, now);
	},
	
	addEvent: function(groupId, calendarId, start, end) {
		var me = this,
				EM = Sonicle.webtop.calendar.model.Event,
				wnd = this._buildEventWnd({
					groupId: groupId
				});
		
		wnd.getComponent(0).on('viewsave', me.onEventViewSave, me);
		wnd.show(false, function() {
			wnd.getComponent(0).beginNew({
				data: {
					calendarId: calendarId,
					startDate: start,
					endDate: end,
					timezone: WT.getOption('timezone')
				}
			});
		});
	},
	
	editEvent: function(eventId) {
		var me = this,
				wnd = this._buildEventWnd();
		
		wnd.getComponent(0).on('viewsave', me.onEventViewSave, me);
		wnd.show(false, function() {
			wnd.getComponent(0).beginEdit({
				data: {
					eventId: eventId
				}
			});
		});
	},
	
	deleteEvent: function(rec) {
		WT.confirm(this.res('calendar.confirm.delete', rec.get('text')), function(bid) {
			if(bid === 'yes') rec.drop();
		}, this);
	},
	
	
	
	onCalendarViewSave: function(s, success, model) {
		if(!success) return;
		var me = this,
				nodeId = me._buildGroupNodeId(model),
				store = me.getRef('treecal').getStore(),
				node;
		
		// Look for group node and reload it!
		node = store.getNodeById(nodeId);
		if(node) store.load({node: node});
	},
	
	onEventViewSave: function(s, success, model) {
		if(!success) return;
		//TODO: completare
	},
	
	_buildCalendarWnd: function(cfg) {
		return WT.createView(this.ID, 'Sonicle.webtop.calendar.view.Calendar', {
			containerCfg: {
				width: 360,
				height: 400
			},
			viewCfg: cfg
		});
	},
	
	_buildEventWnd: function(cfg) {
		return WT.createView(this.ID, 'Sonicle.webtop.calendar.view.Event', {
			containerCfg: {
				width: 650,
				height: 480
			},
			viewCfg: cfg
		});
	},
	
	_buildGroupNodeId: function(calendar) {
		return calendar.get('userId')+'@'+calendar.get('domainId');
	},
	
	_getSelectedNode: function() {
		var sel = this.getRef('treecal').getSelection();
		return (sel.length > 0) ? sel[0] : null;
	},
	
	/**
	 * Returns selected calendar node.
	 * @returns {Ext.data.NodeInterface}
	 */
	_getSelectedCalNode: function() {
		var sel = this.getRef('treecal').getSelection();
		
		if(sel.length === 0) return null;
		return (sel[0].get('_nodeType') === 'group') ? null : sel[0];
	},
	
	/**
	 * Returns selected calendar group node.
	 * @param {Boolean} force True to return a default (my group) if selection is not available.
	 * @returns {Ext.data.NodeInterface}
	 */
	_getSelectedCalGroupNode: function(force) {
		var tree = this.getRef('treecal'),
				sel = tree.getSelection();
		
		if(sel.length === 0) {
			if(!force) return null;
			// As default returns my group, which have id equals to principal option
			return tree.getStore().getNodeById(WT.getOption('principal'));
		}
		return (sel[0].get('_nodeType') === 'group') ? sel[0] : sel[0].parentNode;
	},
	
	
	
	
	
	
	
	getSelectedCalendar: function() {
		var me = this,
				tree = me.getRef('treecal'),
				sel = tree.getSelection(),
				group;
		
		if(sel.length > 0) {
			if(sel[0].get('_nodeType') === 'group') {
				return me.getCalendarByGroup(sel[0]);
			} else {
				return sel[0];
			}
		} else {
			group = tree.getStore().getNodeById(WT.getOption('principal'));
			if(group) return me.getCalendarByGroup(group);
		}
		return null;
	},
	
	getCalendarByGroup: function(groupNode) {
		var cal = this.getDefaultCalendar(groupNode);
		return (cal) ? cal : this.getBuiltInCalendar(groupNode);
	},
	
	getDefaultCalendar: function(groupNode) {
		return groupNode.findChildBy(function(n) {
			return (n.get('_default') === true);
		});
	},
	
	getBuiltInCalendar: function(groupNode) {
		return groupNode.findChildBy(function(n) {
			return (n.get('_builtIn') === true);
		});
	}
});
