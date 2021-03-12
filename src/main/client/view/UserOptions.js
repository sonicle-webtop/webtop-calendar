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
	extend: 'WTA.sdk.UserOptionsView',
	requires: [
		'Sonicle.webtop.calendar.store.View',
		'Sonicle.webtop.calendar.store.ReminderDelivery',
		'Sonicle.webtop.calendar.store.TimeResolution'
	],
	
	viewModel: {
		formulas: {
			foWorkdayStart: {
				bind: {bindTo: '{record.workdayStart}'},
				get: function(val) {
					return val;
				},
				set: function(val) {
					this.get('record').setWorkdayStart(val);
				}
			},
			foWorkdayEnd: {
				bind: {bindTo: '{record.workdayEnd}'},
				get: function(val) {
					return val;
				},
				set: function(val) {
					this.get('record').setWorkdayEnd(val);
				}
			}
		}
	},
		
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'wtopttabsection',
			title: WT.res(me.ID, 'opts.main.tit'),
			items: [
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.view}',
				store: Ext.create('Sonicle.webtop.calendar.store.View', {
					autoLoad: true
				}),
				fieldLabel: WT.res(me.ID, 'opts.main.fld-view.lbl'),
				width: 280,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}), WTF.lookupCombo('id', 'desc', {
				bind: '{record.timeResolution}',
				store: {
					type: 'wtcaltimeresolution',
					autoLoad: true
				},
				fieldLabel: WT.res(me.ID, 'opts.main.fld-timeResolution.lbl'),
				width: 280,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			}), {
				xtype: 'timefield',
				bind: '{foWorkdayStart}',
				format: 'H:i',
				increment : 60,
				snapToIncrement: true,
				fieldLabel: WT.res(me.ID, 'opts.main.fld-workdayStart.lbl'),
				width: 220,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
				/*,
				validator: function(value) {
					var end = me.getFieldValue('workdayEnd');
					//if(!end) return true;
					//if(value <= end) return true; 
					//return Ext.String.format(WT.res('error.fieldlweqthan'), WT.res(me.ID, 'opts.main.fld-workdayEnd.lbl'));
					return true;
				}*/
			}, {
				xtype: 'timefield',
				bind: '{foWorkdayEnd}',
				format: 'H:i',
				increment : 60,
				snapToIncrement: true,
				fieldLabel: WT.res(me.ID, 'opts.main.fld-workdayEnd.lbl'),
				width: 220,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
				/*,
				validator: function(value) {
					var start = me.getFieldValue('workdayStart');
					//if(!start) return true;
					//if(value >= start) return true; 
					//return Ext.String.format(WT.res('error.fieldgteqthan'), WT.res(me.ID, 'opts.main.fld-workdayStart.lbl'));
					return true;
				}*/
			}, WTF.lookupCombo('id', 'desc', {
				bind: '{record.eventReminderDelivery}',
				store: Ext.create('Sonicle.webtop.calendar.store.ReminderDelivery', {
					autoLoad: true
				}),
				fieldLabel: WT.res(me.ID, 'opts.main.fld-eventReminderDelivery.lbl'),
				width: 280,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			})]
		});
	}
});
