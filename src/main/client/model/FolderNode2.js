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
Ext.define('Sonicle.webtop.calendar.model.FolderNode2', {
	extend: 'Ext.data.Model',
		mixins: [
		'WTA.mixin.FolderNodeInterface2'
	],
	
	colorField: '_color',
	defaultField: '_default',
	builtInField: '_builtIn',
	activeField: '_active',
	originRightsField: '_orights',
	folderRightsField: '_frights',
	itemsRightsField: '_irights',
	
	fields: [
		WTF.roField('_orights', 'string'),
		WTF.roField('_frights', 'string'),
		WTF.roField('_irights', 'string'),
		WTF.roField('_builtIn', 'boolean'),
		WTF.roField('_provider', 'string'),
		WTF.roField('_color', 'string'),
		WTF.roField('_sync', 'string'),
		WTF.roField('_default', 'boolean'),
		WTF.field('_active', 'boolean', true), // Same as checked
		WTF.roField('_isPrivate', 'boolean'),
		WTF.roField('_defBusy', 'boolean'),
		WTF.roField('_defReminder', 'int'),
		WTF.roField('_resourceAvail', 'boolean')
		//WTF.calcField('_domainId', 'string', '_pid', function(v, rec) {
		//	return (rec.get('_pid')) ? rec.get('_pid').split('@')[1] : null;
		//}),
		//WTF.calcField('_userId', 'string', '_pid', function(v, rec) {
		//	return (rec.get('_pid')) ? rec.get('_pid').split('@')[0] : null;
		//})
	],
	
	isFolder: function() {
		var type = this.parseId().type;
		return 'F' === type || 'R' === type;
	},
	
	isResource: function() {
		var type = this.parseId().type;
		return 'R' === type;
	}
});