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
Ext.define('Sonicle.webtop.calendar.view.ErpExport', {
	extend: 'WTA.sdk.WizardView',
	
	dockableConfig: {
		title: '{erpExport.tit}',
		iconCls: 'wtcal-icon-export-xs',
		width: 450,
		height: 250
	},
	useTrail: true,
	
	showDoButton: true,
	disableNavAtEnd: false,
	disableNavOnSuccess: false,
	endPageTitleText: '{exportwiz.end.tit}',
	
	viewModel: {
		data: {
			fromDate: null,
			toDate: null
		}
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getVM();
		
		vm.set('fromDate', new Date());
		vm.set('toDate', new Date());
		me.callParent(arguments);
		me.on('beforenavigate', me.onBeforeNavigate);
		me.on('dosuccess', function() {
			Sonicle.URLMgr.download(WTF.processBinUrl(me.mys.ID, 'ExportForErp'));
		});
	},
	
	initPages: function() {
		return ['s1', 'end'];
	},
	
	initAction: function() {
		return 'ExportForErp';
	},
	
	createPages: function(path) {
		var me = this;
		return [{
			itemId: 's1',
			xtype: 'wtwizardpage',
			reference: 's1',
			items: [{
				xtype: 'wtform',
				items: [{
					xtype: 'datefield',
					bind: '{fromDate}',
					allowBlank: false,
					startDay: WT.getStartDay(),
					format: WT.getShortDateFmt(),
					fieldLabel: me.mys.res('erpExport.start.fld-fromDate.lbl'),
					width: 220
				}, {
					xtype: 'datefield',
					bind: '{toDate}',
					allowBlank: false,
					startDay: WT.getStartDay(),
					format: WT.getShortDateFmt(),
					fieldLabel: me.mys.res('erpExport.start.fld-toDate.lbl'),
					width: 220
				}]
			}]
		}, me.createEndPage()];
	},
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if(dir === -1) return;
		var me = this,
				ppcmp = me.getPageCmp(pp), ret;
		
		if(pp === 's1') {
			ret = ppcmp.down('wtform').isValid();
			if(!ret) return false;
		}
	},
	
	doOperationParams: function() {
		var vm = this.getVM();
		return {
			fromDate: Ext.Date.format(vm.get('fromDate'), 'Y-m-d'),
			toDate: Ext.Date.format(vm.get('toDate'), 'Y-m-d')
		};
	}
});
