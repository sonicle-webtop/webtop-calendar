/* 
 * Copyright (C) 2017 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2017 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.calendar.portlet.EventsBody', {
	extend: 'WTA.sdk.PortletBody',
	requires: [
		'Sonicle.webtop.calendar.model.PletEvents'
	],
	uses: [
		'Sonicle.Data'
	],
	
	isSearch: false,
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			bbar: {
				xtype: 'statusbar',
				reference: 'sbar',
				hidden: true
			}
		});
		me.callParent(arguments);
		me.add({
			region: 'center',
			xtype: 'gridpanel',
			reference: 'gp',
			cls: 'wtcal-portlet-grid',
			border: false,
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.calendar.model.PletEvents',
				proxy: WTF.apiProxy(me.mys.ID, 'PortletEvents', 'data', {
					extraParams: {
						query: null
					}
				}),
				listeners: {
					load: function(s) {
						if (me.isSearch) me.lref('sbar').setStatus(me.buildSearchStatus(s.getCount()));
					}
				}
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: me.mys.res('portlet.events.gp.emp')
			},
			columns: [
				{
					xtype: 'socolorcolumn',
					dataIndex: 'calendarColor',
					swatchGeometry: 'circle',
					tooltipField: 'title',
					labelField: 'title',
					labelCls: 'wt-text-sm-medium wtcal-portlet-event-title',
					flex: 1
				}, {
					xtype: 'soactioncolumn',
					itemsPadding: 5,
					items: [
						{
							tooltip: me.mys.res('portlet.events.gp.act-joinMeeting.tip'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								Sonicle.URLMgr.open(rec.get('meeting'), true);
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return !rec.hasMeeting();
							},
							getClass: function(v, meta, rec) {
								var cls = 'fas fa-video ';
								if (!rec.hasMeeting()) cls += (Ext.baseCSSPrefix + 'hidden-display');
								return cls;
							}
							//disabledCls: 'x-item-disabled x-hidden-display'
							//disabledCls: Ext.baseCSSPrefix + 'hidden-display'
						}
					],
					autoWidth: 1
				}
			],
			features: [
				{
					ftype: 'rowbody',
					getAdditionalData: function(data, idx, rec, orig) {
						var SoS = Sonicle.String,
							SoD = Sonicle.Date,
							atDate = me.formatAtDate(rec.get('startDate')),
							atTime = SoD.format(rec.get('startDate'), WT.getShortTimeFmt()),
							loc = Ext.String.ellipsis(rec.get('location'), 50),
							html = '';
						
						html += '<span class="wt-text-xs wt-text-regular wtcal-portlet-event-body-atdate">' + SoS.htmlEncode(atDate) + '</span>';
						html += '<span class="wt-text-xs wt-text-bold wtcal-portlet-event-body-attime">' + SoS.htmlEncode(atTime) + '</span>';
						if (!Ext.isEmpty(loc)) html += '<span class="wt-text-xs wt-text-regular wtcal-portlet-event-body-location">' + SoS.htmlEncode('@'+loc) + '</span>';
						return {
							rowBody: html,
							rowBodyCls: 'wtcal-portlet-event-body'
						};
					}
				}
			],
			listeners: {
				rowdblclick: function(s, rec) {
					me.mys.openEventUI(rec.getItemsRights().UPDATE, rec.get('id'));
				}
			}
		});
	},
	
	refresh: function() {
		if (!this.isSearch) {
			this.lref('gp').getStore().load();
		}
	},
	
	recents: function() {
		var me = this;
		me.isSearch = false;
		me.lref('sbar').hide();
		Sonicle.Data.loadWithExtraParams(me.lref('gp').getStore(), {query: null});
	},
	
	search: function(s) {
		var me = this,
				sbar = me.lref('sbar');
		me.isSearch = true;
		sbar.setStatus(me.buildSearchStatus(-1));
		sbar.show();
		Sonicle.Data.loadWithExtraParams(me.lref('gp').getStore(), {query: s});
	},
	
	buildSearchStatus: function(count) {
		return Ext.String.format(this.mys.res('portlet.events.sbar.count'), (count > -1) ? count : '?');
	},
	
	formatAtDate: function(start) {
		var me = this,
			SoD = Sonicle.Date,
			XD = Ext.Date,
			now = SoD.setTime(new Date(), 0, 0, 0);
		
		if (SoD.diffDays(now, start) === 0) {
			return me.mys.res('portlet.events.gp.dateat.today');
		} else if (SoD.diffDays(now, start) === 1) {
			return me.mys.res('portlet.events.gp.dateat.tomorrow');
		} else {
			return XD.format(start, WT.getShortDateFmt());
		}
	},
	
	buildDateTimeInfo: function(start) {
		var me = this,
			soDate = Sonicle.Date,
			eDate = Ext.Date,
			startd = eDate.format(start, WT.getShortDateFmt()),
			startt = eDate.format(start, WT.getShortTimeFmt()),
			now = soDate.setTime(new Date(), 0, 0, 0),
			str0;
		
		if (soDate.diffDays(now, start) === 0) {
			str0 = me.mys.res('portlet.events.gp.dateat.today');
		} else if (soDate.diffDays(now, start) === 1) {
			str0 = me.mys.res('portlet.events.gp.dateat.tomorrow');
		} else {
			str0 = startd;
		}
		return Ext.String.format(me.mys.res('portlet.events.gp.dateat'), str0, startt);
	}
});
