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
Ext.define('Sonicle.webtop.calendar.view.CalendarRemoteWiz', {
	extend: 'WTA.sdk.WizardView',
	requires: [
		'Sonicle.FakeInput',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.form.field.Palette',
		'WTA.ux.panel.Form',
		'Sonicle.webtop.calendar.store.ProviderRemote',
		'Sonicle.webtop.calendar.store.RemoteSyncFreq'
	],
	
	dockableConfig: {
		title: '{calendarRemoteWiz.tit}',
		iconCls: 'wtcal-icon-calendar-remote-xs',
		width: 450,
		height: 300
	},
	useTrail: true,
	
	viewModel: {
		data: {
			profileId: null,
			provider: 'webcal',
			url: null,
			username: null,
			password: null,
			name: null,
			color: '#FFFFFF',
			syncFrequency: null
		}
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				data = ic['data'] || {};
		
		if (!Ext.isEmpty(data.profileId)) me.getVM().set('profileId', data.profileId);
		if (!Ext.isEmpty(data.provider)) me.getVM().set('provider', data.provider);
		if (!Ext.isEmpty(data.url)) me.getVM().set('url', data.url);
		if (!Ext.isEmpty(data.username)) me.getVM().set('username', data.username);
		if (!Ext.isEmpty(data.password)) me.getVM().set('password', data.password);
		if (!Ext.isEmpty(data.name)) me.getVM().set('name', data.name);
		if (!Ext.isEmpty(data.color)) me.getVM().set('color', data.color);
		if (!Ext.isEmpty(data.syncFrequency)) me.getVM().set('syncFrequency', data.syncFrequency);
		me.callParent(arguments);
		me.on('beforenavigate', me.onBeforeNavigate);
	},
	
	initPages: function() {
		return ['s1','s2','s3'];
	},
	
	createPages: function(path) {
		var me = this,
				SoString = Sonicle.String;
		return [{
			itemId: 's1',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: me.mys.res('calendarRemoteWiz.s1.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: SoString.htmlLineBreaks(me.mys.res('calendarRemoteWiz.s1.txt'))
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				defaults: {
					labelWidth: 80
				},
				items: [
				WTF.lookupCombo('id', 'desc', {
					bind: '{provider}',
					store: Ext.create('Sonicle.webtop.calendar.store.ProviderRemote', {
						autoLoad: true
					}),
					fieldLabel: me.mys.res('calendarRemoteWiz.fld-provider.lbl'),
					width: 80+170
				}),	
				{
					xtype: 'textfield',
					bind: '{url}',
					allowBlank: false,
					selectOnFocus: true,
					fieldLabel: me.mys.res('calendarRemoteWiz.fld-url.lbl'),
					anchor: '100%'
				}, {
					xtype: 'sofakeinput' // Disable Chrome autofill
				}, {
					xtype: 'sofakeinput', // Disable Chrome autofill
					type: 'password'
				}, {
					xtype: 'textfield',
					bind: '{username}',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('calendarRemoteWiz.fld-username.lbl'),
					width: 80+200
				}, {
					xtype: 'textfield',
					bind: '{password}',
					inputType: 'password',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('calendarRemoteWiz.fld-password.lbl'),
					width: 80+200
				}]
			}]
		}, {
			itemId: 's2',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: me.mys.res('calendarRemoteWiz.s2.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: SoString.htmlLineBreaks(me.mys.res('calendarRemoteWiz.s2.txt'))
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				defaults: {
					labelWidth: 80
				},
				items: [
					{
						xtype: 'fieldcontainer',
						layout: {
							type: 'hbox',
							padding: '0 0 1 0' // fixes classic-theme bottom border issue
						},
						items: [
							{
								xtype: 'textfield',
								bind: '{name}',
								allowBlank: false,
								margin: '0 5 0 0',
								flex: 1
							}, {
								xtype: 'sopalettefield',
								bind: '{color}',
								hideTrigger: true,
								colors: WT.getColorPalette('default'),
								tilesPerRow: 11,
								width: 24
							}
						],
						fieldLabel: me.mys.res('calendarRemoteWiz.fld-name.lbl'),
						anchor: '100%'
					}, {
						xtype: 'combo',
						bind: '{syncFrequency}',
						editable: false,
						store: Ext.create('Sonicle.webtop.calendar.store.RemoteSyncFreq', {
							autoLoad: true
						}),
						valueField: 'id',
						displayField: 'desc',
						triggers: {
							clear: WTF.clearTrigger()
						},
						hidden: !me.mys.getVar('calendarRemoteSyncEnabled', false),
						fieldLabel: me.mys.res('calendarRemoteWiz.fld-syncFrequency.lbl'),
						emptyText: me.mys.res('calendarRemoteWiz.fld-syncFrequency.emp'),
						width: 80+140
					}
				]
			}]
		}, {
			itemId: 's3',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: me.mys.res('calendarRemoteWiz.s3.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: SoString.htmlLineBreaks(me.mys.res('calendarRemoteWiz.s3.txt'))
			}]
		}];
	},
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if (dir === -1) return;
		var me = this,
				ret = true,
				ppcmp = me.getPageCmp(pp),
				vm = me.getVM();
		
		if (pp === 's1') {
			ret = ppcmp.down('wtform').isValid();
			if (!ret) return false;
			
			WT.ajaxReq(me.mys.ID, 'SetupCalendarRemote', {
				params: {
					crud: 's1',
					tag: vm.get('tag'),
					profileId: vm.get('profileId'),
					provider: vm.get('provider'),
					url: vm.get('url'),
					username: vm.get('username'),
					password: vm.get('password')
				},
				callback: function(success, json) {
					if (success) {
						vm.set('name', json.data.name);
						me.onNavigate(np);
					} else {
						WT.error(json.message);
					}
				}
			});
			return false;
			
		} else if (pp === 's2') {
			ret = ppcmp.down('wtform').isValid();
			if (!ret) return false;
			
			WT.ajaxReq(me.mys.ID, 'SetupCalendarRemote', {
				params: {
					crud: 's2',
					tag: vm.get('tag'),
					profileId: vm.get('profileId'),
					name: vm.get('name'),
					color: vm.get('color'),
					syncFrequency: vm.get('syncFrequency')
				},
				callback: function(success, json) {
					if (success) {
						me.onNavigate(np);
					} else {
						WT.error(json.message);
					}
				}
			});
			return false;
		}
	}
});

