/* 
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.calendar.ux.PlanningGrid', {
	extend: 'Ext.grid.Panel',
	alias: ['widget.wtcalplanninggrid'],
	requires: [
		'Sonicle.Date',
		'Sonicle.String',
		'Sonicle.webtop.calendar.ux.PlanningModel',
		'Sonicle.webtop.calendar.ux.PlanningView'
	],
	
	config: {
		hideWorkdayOff: true,
		resolution: 30
	},
	
	serviceId: null,
	serviceAction: null,
	eventStart: null,
	eventEnd: null,
	eventAllDay: false,
	eventTimezone: null,
	eventAttendees: null,
	eventOrganizerProfile: null,
	
	attendeeColumnWidth: 200,
	span15ColumnWidth: 15,
	
	componentCls: 'wtcal-planning-grid',
	workdayHoursOffIconCls: 'wtcal-icon-workdayHours-grayed',
	workdayHoursOnIconCls: 'wtcal-icon-workdayHours',
	legendItemCls: 'wtcal-planning-legend',
	freeSpanCls: 'wtcal-planning-free',
	freeInWorkdayHoursSpanCls: 'wtcal-planning-free',
	freeWorkdayOffHoursSpanCls: 'wtcal-planning-free-out',
	busySpanCls: 'wtcal-planning-busy',
	unknownSpanCls: 'wtcal-planning-unknown',
	collisionSpanSelectionCls: 'wtcal-planning-sel-collision',
	
	legendFreeText: 'Free',
	legendFreeWorkdayOffText: 'Workday Off',
	legendBusyText: 'Busy',
	legendUnknownText: 'Unknown',
	viewOptionsHideWorkdayOffText: 'Hide workday off hours',
	viewOptionsResolutionText: 'Span resolution',
	viewOptionsResolution60Text: '60 mins',
	viewOptionsResolution30Text: '30 mins',
	viewOptionsResolution15Text: '15 mins',
	hidePlanningButtonText: 'Close',
	attendeeHeaderText: 'Attendee',
	organizerText: '(organizer)',
	dayHeaderFmt: 'd M Y',
	
	refreshing: 0,
	
	constructor: function(cfg) {
		var me = this,
			SoU = Sonicle.Utils,
			icfg = SoU.getConstructorConfigs(me, cfg, [
				{viewConfig: true}
			]);
		cfg.viewConfig = Ext.apply(icfg.viewConfig, {
			xtype: 'wtcalplanningview',
			trackOver: false,
			overItemCls: '' // Disable over class on cells to NOT disturb bg coloring
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
			resolutionGroup = Ext.id(null, 'resolution-');
		
		Ext.apply(me, {
			enableLocking: true,
			columnLines: true,
			store: {
				model: 'Sonicle.webtop.calendar.ux.PlanningModel',
				proxy: WTF.proxy(me.serviceId, me.serviceAction, null, {
					resolution: me.resolution
				}),
				listeners: {
					beforeload: function(s, op) {
						var SoD = Sonicle.Date;
						me.getSelectionModel().deselectAll();
						op.setParams(Ext.apply(op.getParams() || {}, {
							resolution: me.getResolution(),
							boundToWorkingHours: me.getHideWorkdayOff(),
							startDate: SoD.format(me.eventStart, 'Y-m-d H:i'),
							endDate: SoD.format(me.eventEnd, 'Y-m-d H:i'),
							allDay: me.eventAllDay,
							timezone: me.eventTimezone,
							attendees: Sonicle.Utils.toJSONArray(Ext.Array.from(me.eventAttendees)),
							organizer: me.eventOrganizerProfile
						}));
						me.refreshing++;
					},
					metachange: function(s, meta) {
						if (meta.colsInfo) {
							var SoS = Sonicle.String,
								SoD = Sonicle.Date,
								XD = Ext.Date,
								colsInfo = [];

							// In order to draw a better view we need to nest grid columns (hours) 
							// belonging to same day date under the same master header.
							// So we need to create a nested structure identifying useful columns.
							
							var colMap = {};
							Ext.iterate(meta.colsInfo, function(col, i) {
								if (col.dataIndex === 'ATTENDEE') {
									col = Ext.apply(col, {
										dataIndex: 'displayName',
										header: me.attendeeHeaderText,
										menuDisabled: true,
										//resizable: false,
										lockable: false,
										locked: true,
										sortable: false,
										draggable: false,
										hideable: false,
										renderer: function(v, meta, rec) {
											if (rec.isOrganizer()) {
												v += '<span style="font-size:0.8em;opacity:0.4;">&nbsp;' + me.organizerText;
											}
											return v;
										},
										width: me.attendeeColumnWidth
									});
									colsInfo.push(col);
									
								} else {
									var date = XD.parse(col.date, 'Y-m-d H:i'),
										spanIndex = col.sidx,
										dayColKey = XD.format(date, 'Ymd'),
										hourColKey = XD.format(date, 'Ymd-H'),
										colWidth = me.span15ColumnWidth * (me.getResolution() / 15);
									
									col = Ext.apply(col, {
										dataIndex: 'spans',
										itemId: 'gridcolumn-' + XD.format(date, 'YmdHi'),
										day: SoS.substrBefore(col.date, ' '),
										hour: SoS.substrAfter(col.date, ' '),
										text: XD.format(date, 'H:i'),
										renderer: function(v, meta, rec) {
											var spanDate = date,
												avail = rec.getSpanAvailability(spanIndex),
												inWorkdayHours = rec.isSpanInWorkdayHours(spanDate),
												clss = [];

											if (avail === 1) {
												clss.push(me.freeSpanCls);
												clss.push(inWorkdayHours ? me.freeInWorkdayHoursSpanCls : me.freeWorkdayOffHoursSpanCls);
											} else if (avail === 2) {
												clss.push(me.busySpanCls);
											} else {
												clss.push(me.unknownSpanCls);
											}
											//if (col.ov === 1) clss.push('wtcal-planning-overlaps');
											meta.tdCls = SoS.join(' ', clss);
											return '';
										},
										menuDisabled: true,
										resizable: false,
										lockable: false,
										sortable: false,
										draggable: false,
										hideable: false,
										style: 'display:none;',
										width: colWidth
									});
									
									if (colMap[dayColKey] === undefined) {
										colMap[dayColKey] = (colsInfo.push({
											day: SoS.substrBefore(col.date, ' '),
											text: XD.format(date, me.dayHeaderFmt),
											menuDisabled: true,
											lockable: false,
											resizable: false,
											sortable: false,
											draggable: false,
											columns: []
										}) -1);
									}
									
									if (colMap[hourColKey] === undefined) {
										colMap[hourColKey] = (colsInfo[colMap[dayColKey]].columns.push({
											day: SoS.substrBefore(col.date, ' '),
											hour: SoS.substrAfter(col.date, ' '),
											text: XD.format(date, me.hourHeaderFmt || 'H') + ':00',
											menuDisabled: true,
											lockable: false,
											resizable: false,
											sortable: false,
											draggable: false,
											columns: []
										}) -1);
									}
									colsInfo[colMap[dayColKey]].columns[colMap[hourColKey]].columns.push(col);
								}
							});
							
							me.reconfigure(s, colsInfo);
							
							var selModel = me.getSelectionModel(),
								resolution = me.getResolution() || 30,
								startItemId = me.genColumnItemId(SoD.roundTime(me.eventStart, resolution)),
								endItemId = me.genColumnItemId(SoD.roundTime(me.eventEnd, resolution)),
								cols = me.getVisibleColumns(),
								i, itemId, selecting = false, temp = [], cols2Select;
							
							for (i = 0; i < cols.length; ++i) {
								itemId = cols[i].getItemId();
								if (itemId === startItemId) {
									selecting = true;
									temp.push(cols[i]);
								} else if (selecting === true && itemId === endItemId) {
									cols2Select = temp;
									break;
								} else if (selecting === true) {
									temp.push(cols[i]);
								}
							}
							if (cols2Select) {
								for (i = 0; i < cols2Select.length; ++i) {
									selModel.selectColumn(cols2Select[i], i > 0, false);
								}
							}
						}
						me.refreshing--;
					}
				}
			},
			viewConfig: {
				xtype: 'wtcalplanningview',
				trackOver: false,
				overItemCls: '' // Disable over class on cells to NOT disturb bg coloring
			},
			selModel: {
				type: 'spreadsheet',
				//type: 'planning',
				columnSelect: true,
				checkboxSelect: false,
				cellSelect: false,
				rowSelect: false
			},
			columns: [
				{
					header: me.attendeeHeaderText,
					lockable: false,
					locked: true,
					width: me.attendeeColumnWidth
				}
			],
			tbar: Sonicle.Utils.mergeToolbarItems(me.tbar, [
				'->',
				{
					xtype: 'button',
					iconCls: 'wt-icon-refresh',
					handler: function() {
						me.refresh();
					}
				}
			]),
			bbar: [
				{
					xtype: 'button',
					iconCls: 'wt-icon-options',
					menu: {
						items: [
							{
								xtype: 'menucheckitem',
								itemId: 'hideWorkdayOff',
								text: me.viewOptionsHideWorkdayOffText,
								hideOnClick: true,
								checkHandler: function(s, checked) {
									me.setHideWorkdayOff(checked);
								}
							}, {
								xtype: 'somenuheader',
								text: me.viewOptionsResolutionText
							}, {
								itemId: 'resolution-60',
								group: resolutionGroup,
								text: me.viewOptionsResolution60Text,
								checked: false,
								checkHandler: function(s, checked) {
									if (checked) me.setResolution(parseInt(Sonicle.String.substrAfter(s.getItemId(), '-')));
								}
							}, {
								itemId: 'resolution-30',
								group: resolutionGroup,
								text: me.viewOptionsResolution30Text,
								checked: false,
								checkHandler: function(s, checked) {
									if (checked) me.setResolution(parseInt(Sonicle.String.substrAfter(s.getItemId(), '-')));
								}
							}, {
								itemId: 'resolution-15',
								group: resolutionGroup,
								text: me.viewOptionsResolution15Text,
								checked: false,
								checkHandler: function(s, checked) {
									if (checked) me.setResolution(parseInt(Sonicle.String.substrAfter(s.getItemId(), '-')));
								}
							}
						],
						listeners: {
							beforeshow: function(s) {
								s.getComponent('hideWorkdayOff').setChecked(me.getHideWorkdayOff(), true);
								var itm = s.getComponent('resolution-' +  me.getResolution());
								if (itm) itm.setChecked(true, true);
							}
						}
					}
				},
				'->',
				{
					xtype: 'tbitem',
					width: 8,
					height: 15,
					cls: me.legendItemCls + ' ' + me.busySpanCls
				}, {
					xtype: 'tbtext',
					html: me.legendBusyText
				}, {
					xtype: 'tbitem',
					width: 8,
					height: 15,
					cls: me.legendItemCls + ' ' + me.unknownSpanCls
				}, {
					xtype: 'tbtext',
					html: me.legendUnknownText
				}, {
					xtype: 'tbitem',
					width: 8,
					height: 15,
					cls: me.legendItemCls + ' ' + me.freeWorkdayOffHoursSpanCls
				}, {
					xtype: 'tbtext',
					html: me.legendFreeWorkdayOffText
				}
			]
		});
		me.callParent(arguments);
		me.on('selectionchange', me.onSelectionChange, me);
	},
	
	updateHideWorkdayOff: function(v, ov) {
		this.refresh();
	},
	
	updateResolution: function(v, ov) {
		this.refresh();
	},
	
	setEventStart: function(value) {
		this.eventStart = value;
	},
	
	setEventEnd: function(value) {
		this.eventEnd = value;
	},
	
	setEventAllDay: function(value) {
		this.eventAllDay = value;
	},
	
	setEventTimezone: function(value) {
		this.eventTimezone = value;
	},
	
	setEventAttendees: function(value) {
		this.eventAttendees = value;
	},
	
	setEventOrganizerProfile: function(value) {
		this.eventOrganizerProfile = value;
	},
	
	refresh: function(eventAttendees) {
		var me = this,
			sto = me.getStore();
		if (me.rendered && sto && me.refreshing === 0) {
			if (eventAttendees !== undefined) me.setEventAttendees(eventAttendees);
			sto.load();
		}
	},
	
	privates: {
		onSelectionChange: function(s, sel) {
			if (sel.startColumn && sel.startColumn.dataIndex === 'displayName') return false;
			var me = this, dates;
			if (me.refreshing === 0) {
				dates = me.collectSelectionDates(sel);
				if (dates) {
					me.fireEvent('planningchange', me, dates[0], dates[1]);
				}
			}
		},
		
		genColumnItemId: function(date) {
			return 'gridcolumn-' + Ext.Date.format(date, 'YmdHi');
		},
		
		collectSelectionDates: function(selection) {
			var me = this,
				fmt = 'Y-m-d H:i',
				min, max;
			Ext.iterate(selection.selectedColumns, function(col) {
				var dataIndex = col.dataIndex;
				if (dataIndex === 'displayName') return false;
				if (min === undefined) min = col.date;
				if (max === undefined) max = col.date;
				if (col.date < min) min = col.date;
				if (col.date > max) max = col.date;
			});
			if (min && max) {
				return [Ext.Date.parseDate(min, fmt), Ext.Date.add(Ext.Date.parseDate(max, fmt), Ext.Date.MINUTE, me.resolution)];
			} else {
				return null;
			}
		}
	}
});
