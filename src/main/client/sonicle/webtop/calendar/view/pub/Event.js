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
Ext.define('Sonicle.webtop.calendar.view.pub.Event', {
	extend: 'WT.ux.panel.Panel',
	requires: [
		'Sonicle.form.Spacer',
		'Sonicle.form.field.Link',
		'Sonicle.grid.column.Icon',
		'WT.ux.panel.Panel',
		'Sonicle.webtop.calendar.model.pub.Event'
	],
	
	layout: 'center',
	referenceHolder: true,
	viewModel: {},
	
	mys: null,
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		WTU.applyFormulas(vm, {
			foIsWhereEmpty: WTF.isEmptyFormula('record', 'where')
		});
		
		me.callParent(arguments);
		
		me.add({
			xtype: 'panel',
			layout: 'border',
			border: false,
			width: '100%',
			maxWidth: 500,
			height: '100%',
			maxHeight: 400,
			bind: {
				title: '{record.title}'
			},
			iconCls: me.mys.cssIconCls('event', 'xs'),
			items: [{
				region: 'center',
				xtype: 'wtfieldspanel',
				bodyPadding: 10,
				items: [{
					xtype: 'textarea',
					bind: '{record.description}',
					editable: false,
					grow: true,
					growMin: 0,
					growMax: 150,
					hideEmptyLabel: true,
					emptyText: me.mys.res('pub.event.fld-description.et'),
					anchor: '100%',
					listeners: {
						afterrender: function(s) {
							s.triggerWrap.setStyle({border: 'none'});
						}
					}
				}, {
					xtype: 'sospacer'
				}, {
					xtype: 'fieldcontainer',
					layout: 'hbox',
					fieldLabel: me.mys.res('pub.event.fld-when.lbl'),
					items: [{
						xtype: 'displayfield',
						bind: '{record.when}'
					}, {
						xtype: 'displayfield',
						value: '-',
						margin: '0 5 0 5'
					}, {
						xtype: 'displayfield',
						bind: '{record.timezone}'
					}]
				}, {
					xtype: 'fieldcontainer',
					layout: 'hbox',
					fieldLabel: me.mys.res('pub.event.fld-where.lbl'),
					items: [{
						xtype: 'displayfield',
						bind: '{record.where}',
						margin: '0 5 0 0'
					}, {
						xtype: 'fieldcontainer',
						layout: 'hbox',
						bind: {
							hidden: '{foIsWhereEmpty}'
						},
						items: [{
							xtype: 'displayfield',
							value: '('
						}, {
							xtype: 'solinkfield',
							displayText: me.mys.res('pub.event.fld-whereUrl.lbl'),
							bind: '{record.whereUrl}'
						}, {
							xtype: 'displayfield',
							value: ')'
						}]
					}]
				}, {
					xtype: 'displayfield',
					bind: '{record.calendar}',
					fieldLabel: me.mys.res('pub.event.fld-calendar.lbl')
				}, {
					xtype: 'displayfield',
					bind: '{record.organizer}',
					fieldLabel:  me.mys.res('pub.event.fld-organizer.lbl')
				}, {
					xtype: 'fieldcontainer',
					layout: 'fit',
					fieldLabel:  me.mys.res('pub.event.fld-attendees.lbl'),
					items: [{
						xtype: 'gridpanel',
						bind: {
							store: '{record.attendees}'
						},
						viewConfig: {
							deferEmptyText: false,
							emptyText: WT.res('word.none.male')
						},
						hideHeaders: true,
						disableSelection: true,
						columns: [{
							xtype: 'soiconcolumn',
							dataIndex: 'response',
							getIconCls: function(v,rec) {
								return me.mys.cssIconCls('response-'+v, 'xs');
							},
							iconSize: WTU.imgSizeToPx('xs'),
							width: 40
						}, {
							dataIndex: 'name',
							flex: 1
						}],
						maxHeight: 150,
						border: false
					}],
					anchor: '100%'
				}]
			}]
		});
		
		me.linkModel();
	},
	
	linkModel: function() {
		var me = this,
				linkName = 'record',
				vm = me.getViewModel(),
				data = Ext.JSON.decode(me.mys.getVar('eventData'), true),
				mo;
		
		// Defines a viewmodel link, creating an empty (phantom) model
		vm.linkTo(linkName, {
			type: 'Sonicle.webtop.calendar.model.pub.Event',
			create: true
		});
		if(data) {
			mo = vm.get('record');
			// Apply initial data resetting dirty flag
			mo = vm.get(linkName);
			mo.set(data, {dirty: false});
			mo.setAssociated(data); // Using our custom Sonicle.data.Model!
		}
	}
});
