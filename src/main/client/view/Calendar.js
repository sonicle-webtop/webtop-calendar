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
Ext.define('Sonicle.webtop.calendar.view.Calendar', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.FakeInput',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.form.RadioGroup',
		'Sonicle.form.Separator',
		'Sonicle.form.Spacer',
		'Sonicle.form.field.Palette',
		'Sonicle.webtop.calendar.model.Calendar',
		'Sonicle.webtop.calendar.store.Provider',
		'Sonicle.webtop.calendar.store.Reminder',
		'Sonicle.webtop.calendar.store.Sync',
		'Sonicle.webtop.calendar.store.RemoteSyncFreq'
	],
	
	dockableConfig: {
		title: '{calendar.tit}',
		iconCls: 'wtcal-icon-calendar',
		width: 440,
		height: 530
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.calendar.model.Calendar',
	
	constructor: function(cfg) {
		var me = this;
		me.visibilityName = Ext.id(null, 'visibility-');
		me.showmeName = Ext.id(null, 'showme-');
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			visibility: WTF.radioGroupBind('record', 'isPrivate', me.visibilityName),
			showme: WTF.radioGroupBind('record', 'busy', me.showmeName),
			//invitation: WTF.checkboxBind('record', 'invitation'),
			notifyOnExtUpdate: WTF.checkboxBind('record', 'notifyOnExtUpdate'),
			foIsRemote: WTF.foGetFn('record', 'provider', function(v) {
				return Sonicle.webtop.calendar.view.Calendar.isRemote(v);
			}),
			foRemoteLastSync: WTF.foGetFn('record', 'remoteLastSync', function(v) {
				return Ext.isEmpty(v) ? WT.res('word.na') : Ext.Date.format(v, WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt());
			})
		});
	},
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
        	bbar: {
        		xtype: 'statusbar',
        		items: [
					WTF.recordInfoButton({
						getTooltip: function() {
							return Ext.String.format('ID: {0}', Sonicle.String.coalesce(me.getModel().get('calendarId'), '?'));
						}
					})
        		]
        	}
        });
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wttabpanel',
			items: [{
				xtype: 'wtform',
				title: me.mys.res('calendar.main.tit'),
				modelValidation: true,
				defaults: {
					labelWidth: 110
				},
				items: [
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.provider}',
						disabled: true,
						store: Ext.create('Sonicle.webtop.calendar.store.Provider', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('calendar.fld-provider.lbl'),
						width: 110+140
					}), 
					{
						xtype: 'fieldcontainer',
						layout: {
							type: 'hbox',
							padding: '0 0 1 0' // fixes classic-theme bottom border issue
						},
						items: [
							{
								xtype: 'textfield',
								reference: 'fldname',
								bind: '{record.name}',
								margin: '0 5 0 0',
								flex: 1
							}, {
								xtype: 'sopalettefield',
								bind: '{record.color}',
								hideTrigger: true,
								colors: WT.getColorPalette('default'),
								tilesPerRow: 11,
								width: 24
							}
						],
						fieldLabel: me.mys.res('calendar.fld-name.lbl'),
						anchor: '100%'
					}, {
						xtype: 'textareafield',
						bind: '{record.description}',
						fieldLabel: me.mys.res('calendar.fld-description.lbl'),
						anchor: '100%'
					},
					WTF.lookupCombo('id', 'desc', {
						reference: 'fldsync',
						bind: '{record.sync}',
						store: Ext.create('Sonicle.webtop.calendar.store.Sync', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('calendar.fld-sync.lbl'),
						width: 110+140
					}), 
					{
						xtype: 'soformseparator',
						bind: {
							disabled: '{foIsRemote}'
						},
						title: me.mys.res('calendar.main.defaults.tit')
					}, {
						xtype: 'radiogroup',
						bind: {
							value: '{visibility}',
							disabled: '{foIsRemote}'
						},
						layout: 'hbox',
						defaults: {
							name: me.visibilityName,
							margin: '0 20 0 0'
						},
						items: [{
							inputValue: false,
							boxLabel: me.mys.res('calendar.fld-visibility.default')
						}, {
							inputValue: true,
							boxLabel: me.mys.res('calendar.fld-visibility.private')
						}],
						fieldLabel: me.mys.res('calendar.fld-visibility.lbl')
					}, {
						xtype: 'radiogroup',
						bind: {
							value: '{showme}',
							disabled: '{foIsRemote}'
						},
						layout: 'hbox',
						defaults: {
							name: me.showmeName,
							margin: '0 20 0 0'
						},
						items: [{
							inputValue: false,
							boxLabel: me.mys.res('calendar.fld-showme.available')
						}, {
							inputValue: true,
							boxLabel: me.mys.res('calendar.fld-showme.busy')
						}],
						fieldLabel: me.mys.res('calendar.fld-showme.lbl')
					}, {
						xtype: 'combo',
						bind: {
							value: '{record.reminder}',
							disabled: '{foIsRemote}'
						},
						editable: false,
						store: Ext.create('Sonicle.webtop.calendar.store.Reminder', {
							autoLoad: true
						}),
						valueField: 'id',
						displayField: 'desc',
						triggers: {
							clear: WTF.clearTrigger()
						},
						emptyText: WT.res('word.none.male'),
						fieldLabel: me.mys.res('calendar.fld-reminder.lbl'),
						width: 110+140
					}/*, {
						xtype: 'checkbox',
						bind: {
							value: '{invitation}',
							disabled: '{foIsRemote}'
						},
						hideEmptyLabel: false,
						boxLabel: me.mys.res('calendar.fld-invitation.lbl')
					}*/, {
						xtype: 'soformseparator',
						bind: {
							disabled: '{foIsRemote}'
						}
					}, {
						xtype: 'checkbox',
						bind: {
							value: '{notifyOnExtUpdate}',
							disabled: '{foIsRemote}'
						},
						hideEmptyLabel: false,
						boxLabel: me.mys.res('calendar.fld-notifyOnExtUpdate.lbl'),
						labelWidth: 0
				}]
			}, {
				xtype: 'wtform',
				title: me.mys.res('calendar.remote.tit'),
				bind: {
					disabled: '{!foIsRemote}'
				},
				modelValidation: true,
				defaults: {
					labelWidth: 90
				},
				items: [{
					xtype: 'textfield',
					bind: '{record.remoteUrl}',
					selectOnFocus: true,
					fieldLabel: me.mys.res('calendar.fld-remoteUrl.lbl'),
					anchor: '100%'
				}, {
					xtype: 'sofakeinput' // Disable Chrome autofill
				}, {
					xtype: 'sofakeinput', // Disable Chrome autofill
					type: 'password'
				}, {
					xtype: 'textfield',
					bind: '{record.remoteUsername}',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('calendar.fld-remoteUsername.lbl'),
					width: 90+190
				}, {
					xtype: 'textfield',
					bind: '{record.remotePassword}',
					inputType: 'password',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('calendar.fld-remotePassword.lbl'),
					width: 90+190
				}, {
					xtype: 'combo',
					bind: '{record.remoteSyncFrequency}',
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
					fieldLabel: me.mys.res('calendar.fld-remoteSyncFrequency.lbl'),
					emptyText: me.mys.res('calendar.fld-remoteSyncFrequency.emp'),
					width: 90+140
				}],
				tbar: [{
						xtype: 'tbtext',
						cls: 'x-unselectable',
						html: me.mys.res('calendar.tbi-lastSync.lbl')
					}, {
						xtype: 'tbtext',
						bind: {
							html: '{foRemoteLastSync}'
						}
					},
					'->',
					{
						xtype: 'splitbutton',
						tooltip: me.mys.res('calendar.btn-syncnow.tip'),
						iconCls: 'wt-icon-refresh',
						handler: function() {
							me.syncRemoteCalendarUI(me.getModel().get('calendarId'), false);
						},
						menu: {
							items: [{
								itemId: 'partial',
								text: me.mys.res('calendar.btn-syncnow.partial.lbl')
							}, {
								itemId: 'full',
								text: me.mys.res('calendar.btn-syncnow.full.lbl')
							}],
							listeners: {
								click: function(s, itm) {
									me.syncRemoteCalendarUI(me.getModel().get('calendarId'), itm.getItemId() === 'full');
								}
							}
						}
				}]
			}]
		});
		
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		me.updateSyncFilters();
		me.lref('fldname').focus(true);
	},
	
	updateSyncFilters: function() {
		var me = this,
				isRem = me.self.isRemote(me.getModel().get('provider')),
				sto = me.lref('fldsync').getStore();
		sto.clearFilter();
		sto.addFilter([{
			filterFn: function(rec) {
				return (isRem && (rec.getId() === 'W')) ? false : true;
			}
		}]);
	},
	
	syncRemoteCalendarUI: function(calendarId, full) {
		var me = this;
		WT.confirm(me.mys.res('calendar.confirm.remotesync'), function(bid) {
			if (bid === 'yes') {
				me.mys.syncRemoteCalendar(calendarId, full, {
					callback: function(success, json) {
						if (success) {
							me.closeView(true);
						} else {
							WT.error(json.message);
						}
					}
				});
			}
		}, this);
	},
	
	statics: {
		isRemote: function(provider) {
			return (provider === 'webcal') || (provider === 'caldav');
		}
	}
});

