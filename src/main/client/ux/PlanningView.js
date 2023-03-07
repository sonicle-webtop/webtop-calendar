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
Ext.define('Sonicle.webtop.calendar.ux.PlanningView', {
	extend: 'Ext.view.Table',
	xtype: 'wtcalplanningview',
	
	onCellSelect: function(position) {
		var me = this;
		me.callParent(arguments);
		me.syncCollisionStatusOnSelection();
	},
	
	onCellDeselect: function(position) {
		var me = this,
			grid = me.ownerGrid,
			cell = me.getCellByPosition(position, true);
		me.callParent(arguments);
		if (cell) {
			//console.log('clearing: '+Sonicle.String.substrAfter(cell.getAttribute('data-columnid'), '-'));
			Ext.fly(cell).removeCls(grid.collisionSpanSelectionCls);
		}
		me.syncCollisionStatusOnSelection();
	},
	
	syncCollisionStatusOnSelection: function() {
		var me = this;
		if (me.rendered) {
			var grid = me.ownerGrid,
				colManager = grid.getColumnManager(),
				selModel = me.getSelectionModel(),
				sel = selModel.selected,
				rows = me.all,
				cells = [],
				collision = false,
				rowIdx, colIdx, pos, cell, i;
			
			for (rowIdx = rows.startIndex; rowIdx <= rows.endIndex; rowIdx++) {
				for (i = 0; i < sel.selectedColumns.length; i++) {
					colIdx = colManager.getHeaderIndex(sel.selectedColumns[i]) -1;
					//console.log('colIdx: '+colIdx);
					pos = {column: colIdx, row: rowIdx};
					cell = me.getCellByPosition(pos);
					if (cell) {
						if (!collision && cell.hasCls(grid.busySpanCls)) {
							collision = true;
						}
						cells.push(pos);
					}
				}
			}
			for (i = 0; i < cells.length; i++) {
				cell = me.getCellByPosition(cells[i], true);
				if (cell) {
					if (collision) {
						//console.log('adding: '+Sonicle.String.substrAfter(cell.getAttribute('data-columnid'), '-'));
						Ext.fly(cell).addCls(grid.collisionSpanSelectionCls);
					} else {
						//console.log('clearing: '+Sonicle.String.substrAfter(cell.getAttribute('data-columnid'), '-'));
						Ext.fly(cell).removeCls(grid.collisionSpanSelectionCls);
					}
				}
			}
		}
	}
});
