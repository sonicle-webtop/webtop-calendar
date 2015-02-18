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
Ext.define('Sonicle.webtop.calendar.store.Reminder', {
	extend: 'Ext.data.ArrayStore',
	
	autoLoad: true,
	model: 'WT.model.Simple',
	/*
	data: [
		[0, WT.res('com.sonicle.webtop.calendar', 'store.reminder.0')],
		[5, WT.res('com.sonicle.webtop.calendar', 'store.reminder.5')],
		[10, WT.res('com.sonicle.webtop.calendar', 'store.reminder.10')],
		[15, WT.res('com.sonicle.webtop.calendar', 'store.reminder.15')],
		[30, WT.res('com.sonicle.webtop.calendar', 'store.reminder.30')],
		[45, WT.res('com.sonicle.webtop.calendar', 'store.reminder.45')],
		[60, WT.res('com.sonicle.webtop.calendar', 'store.reminder.60')],
		[120, WT.res('com.sonicle.webtop.calendar', 'store.reminder.120')],
		[180, WT.res('com.sonicle.webtop.calendar', 'store.reminder.180')],
		[240, WT.res('com.sonicle.webtop.calendar', 'store.reminder.240')],
		[300, WT.res('com.sonicle.webtop.calendar', 'store.reminder.300')],
		[360, WT.res('com.sonicle.webtop.calendar', 'store.reminder.360')],
		[420, WT.res('com.sonicle.webtop.calendar', 'store.reminder.420')],
		[480, WT.res('com.sonicle.webtop.calendar', 'store.reminder.480')],
		[540, WT.res('com.sonicle.webtop.calendar', 'store.reminder.540')],
		[600, WT.res('com.sonicle.webtop.calendar', 'store.reminder.600')],
		[660, WT.res('com.sonicle.webtop.calendar', 'store.reminder.660')],
		[720, WT.res('com.sonicle.webtop.calendar', 'store.reminder.720')],
		[1080, WT.res('com.sonicle.webtop.calendar', 'store.reminder.1080')],
		[1440, WT.res('com.sonicle.webtop.calendar', 'store.reminder.1440')],
		[2880, WT.res('com.sonicle.webtop.calendar', 'store.reminder.2880')],
		[10080, WT.res('com.sonicle.webtop.calendar', 'store.reminder.10080')]
	],
	*/
	data: [
		[0, ''],
		[5, ''],
		[10, ''],
		[15, ''],
		[30, ''],
		[45, ''],
		[60, ''],
		[120, ''],
		[180, ''],
		[240, ''],
		[300, ''],
		[360, ''],
		[420, ''],
		[480, ''],
		[540, ''],
		[600, ''],
		[660, ''],
		[720, ''],
		[1080, ''],
		[1440, ''],
		[2880, ''],
		[10080, '']
	],
	
	
	constructor: function(cfg) {
		var me = this;
		Ext.each(me.config.data, function(row) {
			row[1] = WT.res('com.sonicle.webtop.calendar', 'store.reminder.'+row[0]);
		});
		me.callParent([cfg]);
	}
});
