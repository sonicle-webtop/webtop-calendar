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

Ext.define('Sonicle.webtop.calendar.ColorField', {
  extend: 'Ext.form.field.Picker',
  alias: 'widget.colorcbo',
  triggerTip: 'Please select a color.',
  picker:null,
  onTriggerClick: function() {
    var me = this;
    if(!me.picker) {
        me.create();
        me.picker.show();
       
    }else {
            if(me.picker.hidden) {
                    me.picker.show();
            }
            else {
                    me.picker.hide();
            }
        }
    },
    create:function(){
        var me=this;
        if(!me.picker) {
            me.picker = Ext.create('Ext.picker.Color', {    
            pickerField: this,    
            ownerCt: this,   
            floating: true,   
            hidden: true,   
            focusOnShow: true, 
            scope:me,
            style: {
                      backgroundColor: "#fff"
                  } ,
            listeners: {

                        select: function(field, value, opts){
                            me.setValue('#' + value);
                            me.inputEl.setStyle('background','#' + value);
                            me.inputEl.setStyle('color','#' + value);
                            me.picker.hide();
                        },
                        show: function(field,opts){
                          field.getEl().monitorMouseLeave(500, field.hide, field);
                        },scope:this
                    }
            }); 
        }
    },
    setColor:function(value){
        var me=this;
        me.on("render",function(c){
            c.setValue(value);
            c.inputEl.setStyle('background',value);
            c.inputEl.setStyle('color',value);
        },me);
        
        
    }
});
