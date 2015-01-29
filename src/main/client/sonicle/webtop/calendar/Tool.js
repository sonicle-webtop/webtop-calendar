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
Ext.define('Sonicle.webtop.calendar.Tool', {
	extend: 'Ext.panel.Panel',
	requires: [
		'Sonicle.MultiCalendar',
		'Sonicle.webtop.calendar.model.TreeCal'
	],
	mixins: [
		'WT.mixin.RefStorer'
	],
	
	layout: 'border',
	
	constructor: function(cfg) {
		var me = this;
		me.mixins.refstorer.constructor.call(me, cfg);
		me.callParent(arguments);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add(Ext.create({
			region: 'north',
			xtype: 'panel',
			layout: 'center',
			header: false,
			height: 305,
			items: [
				me.addRef('multical', Ext.create({
					xtype: 'somulticalendar',
					border: true,
					startDay: me.mys.getOption('startDay'),
					width: 182,
					height: 298
				}))
			]
		}));
		
		me.add(me.addRef('treecal', Ext.create({
			region: 'center',
			xtype: 'treepanel',
			useArrows: true,
			rootVisible: false,
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.calendar.model.TreeCal',
				proxy: WT.apiProxy(me.mys.ID, 'ManageCalendarsTree', 'children'),
				root: {
					id: 'root',
					expanded: true
				}
			},
			hideHeaders: true,
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
			listeners: {
				checkchange: function(n, ck) {
					if(!n.get('leaf')) return;
					n.beginEdit();
					n.set('calShowEvents', ck);
					n.endEdit();
				}
			}
		})));
		
		var mc = me.getRef('multical');
		mc.on('change', function(s,nv,ov) {
			me.fireDatesChanged(nv, s.getHighlightMode(), s.getStartDay());
		});
	},
	
	cazzo: function(rec, view) {
		var node = view.getNode(rec);
		var el = Ext.fly(node);
	},
	
	changeView: function(view) {
		var me = this, mc = me.getRef('multical');
		mc.setHighlightMode(view);
		me.fireEvent('viewchanged', me, view);
		me.fireDatesChanged(mc.getValue(), view, mc.getStartDay());
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
	
	fireDatesChanged: function(date, view, startDay) {
		var me = this, eDate = Ext.Date, soDate = Sonicle.Date, from, to;
		
		if(view === 'd') {
			from = date;
			to = date;
		} else if (view === 'w')  {
			from = soDate.getFirstDateOfWeek(date, startDay);
			to = soDate.getLastDateOfWeek(date, startDay);
		} else if (view === 'w5')  {
			from = soDate.getFirstDateOfWeek(date, startDay);
			to = eDate.add(soDate.getLastDateOfWeek(date, startDay), eDate.DAY, -2);
		} else if (view === 'm') {
			from = eDate.getFirstDateOfMonth(date);
			to = eDate.getLastDateOfMonth(date);
		}
		me.fireEvent('dateschanged', me, date, from, to, view, startDay);
	}
});
