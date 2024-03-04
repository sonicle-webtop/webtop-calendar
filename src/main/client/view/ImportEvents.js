/*
 * webtop-contacts is a WebTop Service developed by Sonicle S.r.l.
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
Ext.define('Sonicle.webtop.calendar.view.ImportEvents', {
	extend: 'WTA.sdk.ImportWizardView',
	
	dockableConfig: {
		title: '{importEvents.tit}',
		iconCls: 'wtcal-icon-import',
		width: 480,
		height: 380
	},
	
	viewModel: {
		data: {
			url: null
		}
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig();
		
		if(!Ext.isEmpty(ic.calendarId)) me.getVM().set('calendarId', ic.calendarId);
		me.callParent(arguments);
	},
	
	initPages: function() {
		return {
			ics: ['upload','mode','end'],
			url: ['url','mode','end']
		};
	},
	
	initAction: function() {
		return {
			ics: 'ImportEventsFromICal',
			url: 'ImportEventsFromURL'
		};
	},
	
	initFiles: function() {
		return {
			ics: {label: this.mys.res('importEvents.path.fld-path.ics'), extensions: 'ical,ics,icalendar'},
			url: {label: this.mys.res('importEvents.path.fld-path.url'), extensions: 'ical,ics,icalendar'}
		};
	},
	
	addPathPage: function() {
		this.callParent();
		this.getVM().set('path', 'ics');
	},
	
	createPages: function(path) {
		var me = this;
		if(path === 'ics') {
			me.getVM().set('importmode', 'append');
			
			return [
				me.createUploadPage(path),
				me.createModePage(path, [
					{value: 'append', label: WT.res('importwiz.mode.fld-importmode.append')},
					{value: 'copy', label: WT.res('importwiz.mode.fld-importmode.copy')}
				]),
				me.createEndPage(path)
			];
		}
		else if(path === 'url') {
			me.getVM().set('importmode', 'append');
			
			return [
				me.createURLPage(path),
				me.createModePage(path, [
					{value: 'append', label: WT.res('importwiz.mode.fld-importmode.append')},
					{value: 'copy', label: WT.res('importwiz.mode.fld-importmode.copy')}
				]),
				me.createEndPage(path)
			];
		}
	},
	
	createURLPage: function(path) {
		var me = this;
		return {
			itemId: 'url',
			xtype: 'wtwizardpage',
			items: [
				{
					xtype: 'label',
					html: Sonicle.String.htmlLineBreaks(me.mys.res('importEvents.url.tit'))
				}, {
					xtype: 'sospacer'
				}, {
					xtype: 'wtform',
					defaults: {
						labelWidth: 120
					},
					items: [
						{
							xtype: 'textfield',
							reference: 'fldurl',
							bind: '{url}',
							responseValueProperty: 'url',
							width: 400,
							fieldLabel: me.mys.res('importEvents.url.fld-url.lbl'),
							allowBlank: false
						}
					]
				}
			]
		};
	},	
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if(dir === -1) return;
		var me = this,
				ret = true,
				vm = me.getVM(),
				path = vm.get('path'),
				ppcmp = me.getPageCmp(pp);
		
		if(me.callParent(arguments) === false) return false;
		
		if(path === 'ics') {
			if(pp === 'upload') {
				ret = ppcmp.down('wtform').isValid();
			}
			if(!ret) return false;
		}
		else if(path === 'url') {
			if(pp === 'url') {
				ret = ppcmp.down('wtform').isValid();
			}
			if(!ret) return false;
		}
		
		return;
	},
	
	buildDoParams: function(path) {
		var vm = this.getVM();
		if(path === 'ics') {
			return {
				path: path,
				uploadId: vm.get('file'),
				importMode: vm.get('importmode'),
				calendarId: vm.get('calendarId')
			};
		}
		else if(path === 'url') {
			return {
				path: path,
				url: vm.get('url'),
				importMode: vm.get('importmode'),
				calendarId: vm.get('calendarId')
			};
		}
	}
});
