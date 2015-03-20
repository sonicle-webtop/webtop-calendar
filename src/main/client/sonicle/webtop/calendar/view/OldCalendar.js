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
Ext.define('Sonicle.webtop.calendar.view.OldCalendar', {
	extend: 'WT.sdk.FormView',
	requires: [
		'Sonicle.form.field.Palette',
		'Sonicle.form.RadioGroup',
		'Sonicle.webtop.calendar.store.Reminder'
	],
	
	title: '@calendar.tit',
	iconCls: 'wtcal-icon-calendar',
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add(me.addRef('form', Ext.create({
			region: 'center',
			xtype: 'soform',
			bodyPadding: 10,
			model: 'Sonicle.webtop.calendar.model.Calendar',
			items: [{
				xtype: 'hiddenfield',
				name: 'calendarId'
			}, {
				xtype: 'hiddenfield',
				name: 'domainId'
			}, {
				xtype: 'hiddenfield',
				name: 'userId'
			}, {
				xtype: 'textfield',
				name: 'name',
				allowBlank: false,
				fieldLabel: me.mys.res('calendar.fld-name.lbl'),
				anchor: '100%'
			}, {
				xtype: 'textareafield',
				name: 'description',
				fieldLabel: me.mys.res('calendar.fld-description.lbl'),
				anchor: '100%'
			}, {
				xtype: 'sopalettefield',
				name: 'color',
				allowBlank: false,
				colors: WT.getColorPalette(),
				fieldLabel: me.mys.res('calendar.fld-color.lbl'),
				width: 200
			}, {
				xtype: 'checkbox',
				name: 'isDefault',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('calendar.fld-default.lbl')
			}, {
				xtype: 'soradiogroup',
				name: 'isPrivate',
				layout: 'hbox',
				defaults: {
					margin: '0 20 0 0'
				},
				fieldLabel: me.mys.res('calendar.fld-visibility.lbl'),
				items: [{
					name: 'visibility',
					submitValue: false,
					inputValue: false,
					boxLabel: me.mys.res('calendar.fld-visibility.default')
				}, {
					name: 'visibility',
					submitValue: false,
					inputValue: true,
					boxLabel: me.mys.res('calendar.fld-visibility.private')
				}]
			}, {
				xtype: 'soradiogroup',
				name: 'busy',
				layout: 'hbox',
				defaults: {
					margin: '0 20 0 0'
				},
				fieldLabel: me.mys.res('calendar.fld-showme.lbl'),
				items: [{
					name: 'showme',
					submitValue: false,
					inputValue: false,
					boxLabel: me.mys.res('calendar.fld-showme.available')
				}, {
					name: 'showme',
					submitValue: false,
					inputValue: true,
					boxLabel: me.mys.res('calendar.fld-showme.busy')
				}]
			}, {
				xtype: 'combo',
				name: 'reminder',
				allowBlank: false,
				editable: false,
				store: Ext.create('Sonicle.webtop.calendar.store.Reminder'),
				valueField: 'id',
				displayField: 'desc',
				fieldLabel: me.mys.res('calendar.fld-reminder.lbl')
			}, {
				xtype: 'checkbox',
				name: 'invitation',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('calendar.fld-invitation.lbl')
			}, {
				xtype: 'checkbox',
				name: 'sync',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('calendar.fld-sync.lbl')
			}]
		})));
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				form = me.getRef('form');
		
		form.getField('name').focus();
	}
});
