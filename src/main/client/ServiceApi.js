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
	 * Force a reload for the current view.
	 */
	reloadEvents: function() {
		this.service.reloadEvents();
	},
	
	/**
	 * Adds a new event.
	 * @param {Object} evt An object containing event data.
	 * @param {Date} [evt.startDate] The start date-time.
	 * @param {Date} [evt.endDate] The end date-time.
	 * @param {Boolean} [evt.timezone] The timezone identifier.
	 * @param {Boolean} [evt.allDay]
	 * @param {String} [evt.title]
	 * @param {String} [evt.description]
	 * @param {String} [evt.location]
	 * @param {Boolean} [evt.isPrivate]
	 * @param {Boolean} [evt.busy]
	 * @param {Integer} [evt.reminder]
	 * @param {Object} opts An object containing configuration.
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 * @param {Boolean} [opts.dirty] The dirty state of the model.
	 */
	addEvent: function(evt, opts) {
		opts = opts || {};
		this.service.addEvent2(evt, {
			callback: opts.callback,
			scope: opts.scope,
			dirty: opts.dirty
		});
	},
	
	/**
	 * Opens an event for viewing it.
	 * @param {Object} evt An object containing event data.
	 * @param {String} evt.ekey The event key identifier.
	 * @param {Object} opts An object containing configuration.
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 */
	openEvent: function(evt, opts) {
		opts = opts || {};
		this.service.openEvent(false, evt.ekey, {
			callback: opts.callback,
			scope: opts.scope
		});
	},
	
	/**
	 * Opens an event for editing it.
	 * @param {Object} evt An object containing event data.
	 * @param {String} evt.ekey The event key identifier.
	 * @param {Object} opts An object containing configuration.
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 */
	editEvent: function(evt, opts) {
		opts = opts || {};
		this.service.openEvent(true, evt.ekey, {
			callback: opts.callback,
			scope: opts.scope
		});
	}
});
