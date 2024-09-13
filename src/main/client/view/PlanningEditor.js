/* 
 * Copyright (C) 2024 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2024 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.calendar.view.PlanningEditor', {
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.Date'
	],
	
	dockableConfig: {
		title: '{planningEditor.tit}',
		width: 650,
		height: 500,
		modal: true
	},
	promptConfirm: false,
	
	viewModel: {
		data: {
			data: {
				start: null,
				end: null
			}
		}
	},
	defaultButton: 'btnok',
	
	initComponent: function() {
		var me = this;
		
		me.setVMData({
			start: me.eventStart,
			end: me.eventEnd
		});
		
		Ext.apply(me, {
			buttons: [
				{
					ui: '{secondary}',
					text: WT.res('act-cancel.lbl'),
					handler: me.onCancelClick,
					scope: me
				}, {
					ui: '{primary}',
					text: WT.res('act-save.lbl'),
					handler: me.onOkClick,
					scope: me
				}
			]
		});
		
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtcalplanninggrid',
			reference: 'gpplanning',
			serviceId: me.mys.ID,
			serviceAction: 'GeneratePlanningView',
			eventStart: me.eventStart,
			eventEnd: me.eventEnd,
			eventAllDay: me.eventAllDay,
			eventTimezone: me.eventTimezone,
			eventOrganizerProfile: me.eventOrganizerProfile,
			eventAttendees: me.eventAttendees,
			dayHeaderFmt: WT.getLongDateFmt(),
			legendFreeText: me.res('wtcalplanninggrid.legend.free.txt'),
			legendFreeWorkdayOffText: me.res('wtcalplanninggrid.legend.freeWorkdayOff.txt'),
			legendBusyText: me.res('wtcalplanninggrid.legend.busy.txt'),
			legendUnknownText: me.res('wtcalplanninggrid.legend.unknown.txt'),
			viewOptionsHideWorkdayOffText: me.res('wtcalplanninggrid.viewOptions.hideWorkdayOff.lbl'),
			viewOptionsResolutionText: me.res('wtcalplanninggrid.viewOptions.resolution.lbl'),
			viewOptionsResolution60Text: me.res('wtcalplanninggrid.viewOptions.resolution.60.lbl'),
			viewOptionsResolution30Text: me.res('wtcalplanninggrid.viewOptions.resolution.30.lbl'),
			viewOptionsResolution15Text: me.res('wtcalplanninggrid.viewOptions.resolution.15.lbl'),
			hidePlanningButtonText: me.res('wtcalplanninggrid.btn-hidePlanning.lbl'),
			attendeeHeaderText: me.res('wtcalplanninggrid.attendee.lbl'),
			organizerText: me.res('wtcalplanninggrid.organizer'),
			tbar: [
				{
					xtype: 'displayfield',
					reference: 'fldstartend',
					fieldLabel: me.res('planningEditor.fld-selection.lbl')
				}
			],
			listeners: {
				planningchange: function(s, start, end) {
					me.setVMData({
						start: start,
						end: end
					});
					me.refreshRangeSelection(start, end);
				}
			},
			border: false
		});
		me.refreshRangeSelection(me.eventStart, me.eventEnd);
		me.lref('gpplanning').getStore().load();
	},
	
	privates: {
		onOkClick: function() {
			var me = this;
			me.fireEvent('viewok', me, me.getVMData());
			me.closeView(false);
		},

		onCancelClick: function() {
			var me = this;
			me.fireEvent('viewcancel', me);
			me.closeView(false);
		},
		
		refreshRangeSelection: function(start, end) {
			var me = this,
				SoD = Sonicle.Date,
				s = '';
			if (me.eventAllDay === true) {
				s += Ext.Date.format(start, WT.getShortDateFmt());
				if ((SoD.diffDays(start, end) -1) > 0) {
					s += ' - ';
					s += Ext.Date.format(end, WT.getShortDateFmt());
				}
			} else {
				s += Ext.Date.format(start, WT.getShortDateTimeFmt());
				s += ' - ';
				if (SoD.diffDays(start, end) === 0) {
					s += Ext.Date.format(end, WT.getShortTimeFmt());
				} else {
					s += Ext.Date.format(end, WT.getShortDateTimeFmt());
				}
			}
			me.lref('fldstartend').setValue(s);
		}
	}
});