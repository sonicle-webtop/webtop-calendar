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
		'Sonicle.calendar.data.Events',
		'Sonicle.calendar.data.Calendars',
		'Sonicle.webtop.calendar.view.Calendar'
	],
	
	init: function() {
		var me = this;
		
		me.initActions();
		me.initCxm();

		this.addAction('new', 'testaction', {
			tooltip: null,
			handler: function () {
				alert('Calendar testaction clicked');
			},
			scope: this
		});
		
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
								proxy: WT.proxy(me.ID, 'GetEventDates', 'dates')
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
					useArrows: true,
					rootVisible: false,
					store: {
						autoLoad: true,
						autoSync: true,
						model: 'Sonicle.webtop.calendar.model.TreeCal',
						proxy: WT.apiProxy(me.ID, 'ManageCalendarsTree', 'children', {
							writer: {
								allowSingle: false // Make update/delete using array payload
							}
						}),
						root: {
							id: 'root',
							expanded: true
						},
						listeners: {
							datachanged: function() {
								console.log('datachanged');
							},
							update: function() {
								console.log('update');
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
							if(n.get('nodeType') === 'group') {
								
							} else {
								me._showHideCal(n, ck);
							}
						},
						itemcontextmenu: function(vw, rec, itm, i, e) {
							// TODO: disabilitare azioni se readonly
							if(rec.get('nodeType') === 'group') {
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
			use24HourTime: true,
			viewCfg: {
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
			eventStore: Ext.create('Sonicle.calendar.data.MemoryEventStore', {
				data: Sonicle.calendar.data.Events.getData()
			})
		}));
	},
	
	_showCxm: function(menu, evt) {
		evt.preventDefault();
		menu.showAt(evt.getXY());
	},
	
	initActions: function() {
		var me = this,
				view = me.getOption('view');
		
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
			pressed: view === 'd'
		});
		me.addAction('week5view', {
			itemId: 'w5',
			pressed: view === 'w5'
		});
		me.addAction('weekview', {
			itemId: 'w',
			pressed: view === 'w'
		});
		me.addAction('weekagview', {
			itemId: 'wa',
			pressed: view === 'wa'
		});
		me.addAction('monthview', {
			itemId: 'm',
			pressed: view === 'm'
		});
		me.addAction('addCalendar', {
			handler: function() {
				var sel = me.getRef('treecal').getSelection()[0],
						type = sel.get('nodeType'),
						node = (type === 'group') ? sel : sel.parentNode;
				me.addCalendar(node);
			}
		});
		me.addAction('editCalendar', {
			handler: function() {
				var sel = me.getRef('treecal').getSelection()[0];
				me.editCalendar(sel);
			}
		});
		me.addAction('deleteCalendar', {
			handler: function() {
				var sel = me.getRef('treecal').getSelection()[0];
				me.deleteCalendar(sel);
			}
		});
		me.addAction('importEvents', {
			handler: function() {
				// TODO: implement this function
				WT.warn('To be implemented!');
			}
		});
		me.addAction('viewAllCalendars', {
			iconCls: 'wt-icon-select-all-xs',
			handler: function() {
				var sel = me.getRef('treecal').getSelection()[0];
				me._showHideAllCals(sel.parentNode, true);
			}
		});
		me.addAction('viewNoneCalendars', {
			iconCls: 'wt-icon-select-none-xs',
			handler: function() {
				var sel = me.getRef('treecal').getSelection()[0];
				me._showHideAllCals(sel.parentNode, false);
			}
		});
		me.addAction('viewAllCalGroups', {
			iconCls: 'wt-icon-select-all-xs',
			handler: function() {
				var sel = me.getRef('treecal').getSelection()[0];
				me._showHideAllCalGroups(sel, true);
			}
		});
		me.addAction('viewNoneCalGroups', {
			iconCls: 'wt-icon-select-none-xs',
			handler: function() {
				var sel = me.getRef('treecal').getSelection()[0];
				me._showHideAllCalGroups(sel, false);
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
				me.getAction('viewAllCalGroups'),
				me.getAction('viewNoneCalGroups')
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
				me.getAction('viewNoneCalendars')
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
		node.set('showEvents', show);
		node.endEdit();
	},
	
	_showHideAllCalGroups: function(parent, show) {
		
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
	
	addCalendar: function(rec) {
		var me = this,
				wnd = this.buildCalendarWnd();
		
		wnd.getComponent(0).on('viewsave', me.onCalendarViewSave, me);
		wnd.show(false, function() {
			wnd.getComponent(0).beginNew({
				domainId: rec.get('domainId'),
				userId: rec.get('userId')
			});
		});
	},
	
	editCalendar: function(rec) {
		var me = this,
				wnd = this.buildCalendarWnd();
		
		wnd.getComponent(0).on('viewsave', me.onCalendarViewSave, me);
		wnd.show(false, function() {
			wnd.getComponent(0).beginEdit({
				calendarId: rec.getId()
			});
		});
	},
	
	deleteCalendar: function(rec) {
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
	
	buildCalendarWnd: function() {
		var wnd = Ext.create({
			xtype: 'window',
			layout: 'fit',
			width: 360,
			height: 400,
			items: [
				Ext.create('Sonicle.webtop.calendar.view.Calendar', {
					mys: this
				})
			]
		});
		return wnd;
	},
	
	_buildGroupNodeId: function(rec) {
		return rec.get('userId')+'@'+rec.get('domainId');
	}
});
