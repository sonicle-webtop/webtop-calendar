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
Ext.define('Sonicle.webtop.calendar.ServiceApi', {
	extend: 'WTA.sdk.ServiceApi',
	
	/**
	 * @deprecated Use openEvent() instead
	 */
	editEvent: function(data, opts) {
		data = data || {};
		Ext.log.warn('ServiceApi.editEvent() is deprecated. Use ServiceApi.openEvent() instead.');
		this.openEvent(data.ekey, opts);
	},
	
	openReminder: function(type, id) {
		if (Sonicle.String.isIn(type, ['event', 'event-recurring'])) {
			this.service.openEvent(true, id);
		} else {
			Ext.raise('Reminder type not supported [' + type + ']');
		}
	},
	
	/**
	 * Force a reload for the current view.
	 */
	reloadEvents: function() {
		this.service.reloadEvents();
	},
	
	/**
	 * Opens an event using the choosen editing mode, defaults to edit.
	 * @param {String} ekey The event KEY.
	 * @param {Object} opts An object containing configuration.
	 * @param {edit|view} [opts.mode="edit"] Opening mode.
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 * @param {Boolean} [opts.dirty] The dirty state of the model.
	 * @param {Boolean} [opts.uploadTag] A custom upload tag.
	 * @returns {WTA.sdk.ModelView}
	 */
	openEvent: function(ekey, opts) {
		opts = opts || {};
		return this.service.openEvent(opts.mode === 'view' ? false : true, ekey, {
			callback: opts.callback,
			scope: opts.scope,
			dirty: opts.dirty,
			uploadTag: opts.uploadTag
		});
	},
	
	/**
	 * Adds a new event.
	 * @param {Object} data An object containing event data.
	 * @paran {String} [data.calendarId] The calendar ID in which to add the item.
	 * @param {Date} [data.startDate] The start date-time.
	 * @param {Date} [data.endDate] The end date-time.
	 * @param {String} [data.timezone] The timezone identifier.
	 * @param {Boolean} [data.allDay] Set to `true` to mark the appointment as 'all-day'.
	 * @param {String} [data.title] The title.
	 * @param {String} [data.description] The extended description.
	 * @param {String} [data.location] The location.
	 * @param {default|private} [data.visibility] Visibility value.
	 * @param {Boolean} [data.busy] Set to `true` to mark as 'busy' the underlyning time-range.
	 * @param {0|5|10|15|30|45|60|120|180|240|300|360|420|480|540|600|660|720|1080|1440|2880|10080|20160|43200} [data.reminder] Minutes before start at which set reminder.
	 * @param {String} [data.masterDataId] The ID of the linked MasterData.
	 * @param {String} [data.statMasterDataId] The ID of the linked statistic MasterData.
	 * @param {String} [data.activityId] The ID of the Activity.
	 * @param {String} [data.causalId] The ID of the Causal.
	 * @param {String[]|String} [data.tags] Array or pipe-separated list of WebTop's tag IDs.
	 * @param {Object} [data.customFields] Map of values, with CustomField name as key.
	 * @param {Object} opts An object containing configuration.
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 * @param {Boolean} [opts.dirty] The dirty state of the model.
	 * @param {Boolean} [opts.uploadTag] A custom upload tag.
	 * @returns {WTA.sdk.ModelView}
	 */
	addEvent: function(data, opts) {
		opts = opts || {};
		return this.service.addEventWithData(data, {
			callback: opts.callback,
			scope: opts.scope,
			dirty: opts.dirty
		});
	},
	
	/**
	 * Create an instance of the events portlet body.
	 */
	createEventsPortletBody: function(cfg) {		
		return Ext.create('Sonicle.webtop.calendar.portlet.EventsBody', Ext.apply(cfg||{},{ mys: this.service }));
	}
});
