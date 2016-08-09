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
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.form.field.Palette',
		'Sonicle.form.RadioGroup',
		'Sonicle.webtop.calendar.store.Reminder',
		'Sonicle.webtop.calendar.store.Sync'
	],
	
	dockableConfig: {
		title: '{calendar.tit}',
		iconCls: 'wtcal-icon-calendar-xs',
		width: 360,
		height: 400
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.calendar.model.Calendar',
	
	constructor: function(config) {
		var me = this;
		me.visibilityName = Ext.id(null, 'visibility-');
		me.showmeName = Ext.id(null, 'showme-');
		
		me.config.viewModel = {
			formulas: {
				visibility: WTF.radioGroupBind('record', 'isPrivate', me.visibilityName),
				showme: WTF.radioGroupBind('record', 'busy', me.showmeName),
				isDefault: WTF.checkboxBind('record', 'isDefault'),
				invitation: WTF.checkboxBind('record', 'invitation')
			}
		};
		me.callParent([config]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			modelValidation: true,
			defaults: {
				labelWidth: 110
			},
			items: [{
				xtype: 'textfield',
				reference: 'fldname',
				bind: '{record.name}',
				fieldLabel: me.mys.res('calendar.fld-name.lbl'),
				anchor: '100%'
			}, {
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: me.mys.res('calendar.fld-description.lbl'),
				anchor: '100%'
			}, {
				xtype: 'sopalettefield',
				bind: '{record.color}',
				colors: WT.getColorPalette(),
				fieldLabel: me.mys.res('calendar.fld-color.lbl'),
				width: 210
			}, {
				xtype: 'checkbox',
				bind: '{isDefault}',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('calendar.fld-default.lbl')
			}, {
				xtype: 'radiogroup',
				bind: {
					value: '{visibility}'
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
					value: '{showme}'
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
				bind: '{record.reminder}',
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
				width: 250
			}, {
				xtype: 'checkbox',
				bind: '{invitation}',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('calendar.fld-invitation.lbl')
			},
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.sync}',
				store: Ext.create('Sonicle.webtop.calendar.store.Sync', {
					autoLoad: true
				}),
				fieldLabel: me.mys.res('calendar.fld-sync.lbl'),
				width: 250
			})]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		me.lref('fldname').focus(true);
	}
});

