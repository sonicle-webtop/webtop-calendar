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
Ext.define('Sonicle.webtop.calendar.view.UserOptions', {
	extend: 'WT.sdk.UserOptionsView',
	requires: [
		'Sonicle.webtop.calendar.store.View',
		'Sonicle.webtop.calendar.store.StartDay'
	],
	controller: Ext.create('Sonicle.webtop.calendar.view.UserOptionsC'),
	//idField: 'id',
	listeners: {
		save: 'onFormSave'
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'hiddenfield',
			name: 'id'
		}, {
			xtype: 'wtopttabsection',
			title: WT.res(me.ID, 'opts.main.tit'),
			items: [{
				xtype: 'combo',
				name: 'view',
				allowBlank: false,
				editable: false,
				store: Ext.create('Sonicle.webtop.calendar.store.View', {
					autoLoad: true
				}),
				valueField: 'id',
				displayField: 'desc',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-view.lbl'),
				listeners: {
					blur: 'onBlurAutoSave'
				}
			}, {
				xtype: 'combo',
				name: 'startDay',
				allowBlank: false,
				editable: false,
				store: Ext.create('Sonicle.webtop.calendar.store.StartDay', {
					autoLoad: true
				}),
				valueField: 'id',
				displayField: 'desc',
				fieldLabel: WT.res(me.ID, 'opts.main.fld-startDay.lbl'),
				listeners: {
					blur: 'onBlurAutoSave'
				},
				reload: true
			}, {
				xtype: 'timefield',
				name: 'workdayStart',
				allowBlank: false,
				format: 'H:i',
				increment : 60,
				fieldLabel: WT.res(me.ID, 'opts.main.fld-workdayStart.lbl'),
				listeners: {
					blur: 'onBlurAutoSave'
				},
				validator: function(value) {
					var end = me.getFieldValue('workdayEnd');
					//if(!end) return true;
					//if(value <= end) return true; 
					//return Ext.String.format(WT.res('error.fieldlweqthan'), WT.res(me.ID, 'opts.main.fld-workdayEnd.lbl'));
					return true;
				}
			}, {
				xtype: 'timefield',
				name: 'workdayEnd',
				allowBlank: false,
				format: 'H:i',
				increment : 60,
				fieldLabel: WT.res(me.ID, 'opts.main.fld-workdayEnd.lbl'),
				listeners: {
					blur: 'onBlurAutoSave'
				},
				validator: function(value) {
					var start = me.getFieldValue('workdayStart');
					//if(!start) return true;
					//if(value >= start) return true; 
					//return Ext.String.format(WT.res('error.fieldgteqthan'), WT.res(me.ID, 'opts.main.fld-workdayStart.lbl'));
					return true;
				}
			}]
		});
	}
});
