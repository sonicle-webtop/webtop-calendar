/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
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
Ext.define('Sonicle.webtop.calendar.Service', {
	extend: 'WT.sdk.Service',
	requires: [
		'Sonicle.webtop.calendar.CalendarTools',
		'Sonicle.webtop.calendar.view.PersonalCalendar'
	],
	
	init: function() {
		var me = this;
		console.log('Sonicle.webtop.calendar.CalendarService initialized!');
		this.on('activate', function () {
			console.log('activeeeeeeeeeeeeeeeeeeeee');
		});

		this.addAction('new', 'testaction', {
			tooltip: null,
			handler: function () {
				alert('Calendar testaction clicked');
			},
			scope: this
		});

		var tb = Ext.create({
			xtype: 'toolbar',
			items: [{
					xtype: 'button',
					text: 'Aggiungi cal.',
					handler: function () {
						calendarTools.addPersonalCalendar();

					}
				}, {
					xtype: 'button',
					text: 'Aggiorna cal.',
					handler: function () {
						calendarTools.editPersonalCalendar();
					}
				}, {
					xtype: 'button',
					text: 'Cancella cal.',
					handler: function () {
						calendarTools.deletePersonalCalendar();
					}
				}, {
					xtype: 'button',
					text: 'Visualizza',
					handler: function () {
						calendarTools.viewOnlyPersonalCalendar();
					}
				}
			]
		});
		this.setToolbar(tb);

		var calendarTools = Ext.create('Sonicle.webtop.calendar.CalendarTools', {
			mys: me,
			startDay: this.getOption("startDay")

		});
		me.calendarTools = calendarTools;
		var tool = Ext.create({
			xtype: 'panel',
			title: 'Calendar Toolbox',
			width: 150,
			items: [calendarTools]
		});
		this.setToolComponent(tool);

		var main = Ext.create({
			xtype: 'tabpanel',
			activeTab: 0,
			items: [
				/*Ext.create('Sonicle.webtop.calendar.Scheduler',{
				 startHour:this.startwork,
				 endHour:this.endwork,
				 cs: me,
				 proxy: null,
				 proxyEdit: null,
				 monthNames : [
				 this.res("monthNames1"),
				 this.res("monthNames2"),
				 this.res("monthNames3"),
				 this.res("monthNames4"),
				 this.res("monthNames5"),
				 this.res("monthNames6"),
				 this.res("monthNames7"),
				 this.res("monthNames8"),
				 this.res("monthNames9"),
				 this.res("monthNames10"),
				 this.res("monthNames11"),
				 this.res("monthNames12")
				 ],
				 dayNames : [
				 this.res("dayNames1"),
				 this.res("dayNames2"),
				 this.res("dayNames3"),
				 this.res("dayNames4"),
				 this.res("dayNames5"),
				 this.res("dayNames6"),
				 this.res("dayNames7")
				 ],
				 value: new Date()
				 
				 })*/

			]
		});
		this.setMainComponent(main);
	}
});
