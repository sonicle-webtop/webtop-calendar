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
	extend: 'WT.sdk.WizardView',
	requires: [
		'Sonicle.webtop.calendar.model.ErpExport'
	],
	
	dockableConfig: {
		title: '@erpExport.tit',
		iconCls: 'wtcal-icon-exportEvents-xs',
		width: 450,
		height: 250
	},
	useTrail: true,
	doAction: 'ErpExportWizard',
	pages: ['start','end'],
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.on('beforenavigate', me.onBeforeNavigate);
		me.on('dosuccess', function() {
			//Sonicle.URLManager.download('http://iso.esd.microsoft.com/W10IP/4B33E0680A465FBA52C09A34454D5EB6/Windows10_InsiderPreview_x64_IT-IT_10162.iso');
			//Sonicle.URLManager.download("service-request?service=com.sonicle.webtop.calendar&action=ExportWizard&nowriter=true");
			//window.open("service-request?service=com.sonicle.webtop.calendar&action=ExportWizard&nowriter=true");
			//Sonicle.URLManager.download('service-request?service=com.sonicle.webtop.calendar&action=ExportWizard&nowriter=true');
			Sonicle.URLManager.download(WTF.processBinUrl(me.mys.ID, 'ErpExportWizard'));
		});
	},
	
	createPages: function() {
		var me = this;
		return [{
			itemId: 'start',
			xtype: 'form',
			viewModel: {
				links: {
					record: {
						type: 'Sonicle.webtop.calendar.model.ErpExportStart',
						create: true
					}
				}
			},
			modelValidation: true,
			layout: 'anchor',
			defaults: {
				labelWidth: 80
			},
			items: [{
				xtype: 'datefield',
				bind: '{record.fromDate}',
				width: 200,
				fieldLabel: me.mys.res('erpExport.start.fld-fromDate.lbl')
			}, {
				xtype: 'datefield',
				bind: '{record.toDate}',
				width: 200,
				fieldLabel: me.mys.res('erpExport.start.fld-toDate.lbl')
			}]
		}].concat(me.callParent(arguments));
	},
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if(dir === -1) return;
		var me = this,
				pcmp = me.getPageCmp(pp),
				model;
		if(pp === 'start') {
			model = pcmp.getViewModel().data['record'];
			if(model && model.isValid()) {
				me.wait();
				model.save({
					callback: function(rec, op, success) {
						me.unwait();
						if(success) {
							me.onNavigate(np);
						} else {
							WT.error(op.getError());
						}
					},
					scope: me
				});
			}
			return false;
		}
	}
});
