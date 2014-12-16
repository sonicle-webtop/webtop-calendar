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


Ext.define('Sonicle.webtop.calendar.CalendarTools', {
	extend: 'Ext.panel.Panel',
	requires: [
		'Sonicle.webtop.calendar.MultiMonthCalendar'
	],

        layout: {
		type: 'vbox',
		align: 'stretch'
	},
        border: false,
        mys:null,
	startDay:null,
        
	initComponent: function() {
		var me=this;
		
		me.callParent(arguments);
		var ca=Ext.create('Sonicle.webtop.calendar.MultiMonthCalendar',{
                    startDay:me.startDay
                });
		me.add(ca);
                me.add(Ext.create('Ext.tab.Panel', {
				items: [
					{
                                            xtype:'panel',
                                            title:me.mys.res('title.calendars'),
                                            height:200
					},
                                        
					{
                                            xtype:'panel',
                                            title:me.mys.res('title.personals'),
                                            id:'panel-personalcalendarwt',
                                            bodyStyle:'border:0px;padding:5px',
                                            border:false,
                                            name:'personal',
                                            height:200,
                                            autoScroll:true
					}
				]
			})
		);
        
                
                me.actionRemoveCalendar=Ext.create("Ext.Action",{ 
                        tooltip:WT.res('calendar',"deletecalendar"),
                        text:WT.res('calendar',"deletecalendar"),
                        iconCls:"icon-deletecalendar",
                        handler:function(){
                            var b=this.menucalendar.b;
                            this.msgDeleteCalendar(b.name, b.description, b.color)
                        },
                        scope:this
                });
          
                me.actionEditCalendar=Ext.create("Ext.Action",{ 
                        tooltip:WT.res('calendar',"editcalendar"),
                        text:WT.res('calendar',"editcalendar"),
                        iconCls:"icon-editcalendar",
                        handler:function(){
                             var b=this.menucalendar.b;
                             this.editCalendar(b.idc,b.name,b.description,b.color,b.privatevalue,b.busy,b.sync,b.defaultvalue,b.default_reminder,b.default_send_invite);
                        },
                        scope: this
                });
        
            
                me.actionAddCalendar=Ext.create("Ext.Action",{ 
                        text:WT.res('calendar',"addcalendar"),
                        tooltip:WT.res('calendar',"addcalendar"),
                        iconCls:"icon-addcalendar",
                        handler:function(){
                            this.addCalendar();
                        },
                        scope: this
                });
           
                me.actionOnlyCalendar=Ext.create("Ext.Action",{ 
                        text:WT.res('calendar',"onlycalendar"),
                        tooltip:WT.res('calendar',"onlycalendar"),
                        iconCls:"icon-onlycalendar",
                        handler:function(){
                            var b=this.menucalendar.b;
                            this.onlyCalendar(b.idc);
                        },
                        scope: this
                });
                
                me.contextMenuPersonalCalendar=Ext.create("Ext.menu.Menu",{
                    items: [
                        me.actionEditCalendar,
                        me.actionRemoveCalendar,
                        me.actionOnlyCalendar,
                        //me.importEvent,
                        '-',
                        me.actionAddCalendar
                    ]
                });
        
                me.getPersonalCalendars();
		
	},
        
        getPersonalCalendars:function(){
            var me=this;
            WT.ajaxReq('com.sonicle.webtop.calendar', 'GetPersonalCalendars', {
                    callback: function(success, o) {
                            if(success) {
                                var j=o.data;
                                var items=[];
                                Ext.each(j, function (calendar) {
                                    items.push(me.getItemsPersonalCalendars(calendar));
                                    
                                });  
                                me.setPersonalCalendars(items);
                            }
                    },scope:me
            });
        },
        
        getItemsPersonalCalendars:function(calendar){
            var me=this;
            
            var btmenu = Ext.create('Ext.Component',{
                        calendarId:calendar.calendarId,
                        name:calendar.calendar,
                        color:calendar.color,
                        description:calendar.description,
                        autoEl: {tag: "div", style:"width:13px;height:13px;padding:0;margin:0;border: 1px solid;margin-right:3px;background:"+calendar.color},
                        margin:{top:6, right:3, bottom:0, left:0},
                        width: 13,
                        height: 13,
                        listeners: {
                          render: function(c) {
                            c.getEl().on('click', function(e) {
                              //this.menucalendar.b=c;
                              //this.contextMenu(c,e);
                            }, me);
                          
                          },
                          scope:me
                        }
                    });
            var boxlabel=calendar.name;
            if (calendar.default_==true){
                boxlabel="<b>"+boxlabel+"</b>";
            }
            var checkbox = Ext.create("Ext.form.Checkbox",{
                    boxLabel:boxlabel,
                    tooltip:calendar.name,
                    userId:calendar.userId,
                    calendarId:calendar.calendarId,
                    userId:calendar.userId,
                    name:calendar.name,
                    color:calendar.color,
                    description:calendar.description,
                    checked:calendar.showEvents,
                    showEvents:calendar.showEvents,
                    private_:calendar.private_,
                    sync:calendar.sync,
                    busy:calendar.busy,
                    default_:calendar.default_,
                    defaultReminder:calendar.defaultReminder,
                    defaultSendInvite:calendar.defaultSendInvite,
                    ctCls: "gs-form-field",
                    labelStyle:'font-weight:bold;',
                    listeners:{
                        change:function(c,newValue,oldValue){
                            me.checkPersonalCalendar(c.calendarId,newValue);
                        },
                    
                        scope:me
                        
                    }
                });
                
                me.mys.test=checkbox; ///DA CANCELLARE!!!!!
                
                var field_container=Ext.create("Ext.form.FieldContainer",{
                    layout:'hbox',
                    items:[btmenu,checkbox],
                    listeners: {
                        render: function(c) {
                            c.getEl().on('longpress', function(e) {
                              //me.menucalendar.b=c;
                              alert("ok");
                              me.contextMenu(c,e);
                            }, me);
                        }
                    }
                });
                
                                        
                return field_container;
        },        
        
	
        contextMenu: function(n,e) {
            var me=this;
            e.stopEvent();
            me.menucalendar.showAt(e.getXY());
        },
        
        setPersonalCalendars:function(items){
            var p=Ext.getCmp('personalcalendars');
            if (p) p.destroy(); 
            var cp=Ext.getCmp("panel-personalcalendarwt");
            var personalCalendars=Ext.create("Ext.form.CheckboxGroup",{
                    id:'personalcalendars',
                    xtype:'checkboxgroup',
                    itemCls: 'x-check-group-alt',    
                    columns: 1,
                    region:'center',
                    items: items,
                    height:'200'
            });
            cp.add(personalCalendars);
            cp.doLayout();
        },
        
        checkPersonalCalendar:function(calendarId,showEvents){
            var me=this;
            WT.ajaxReq('com.sonicle.webtop.calendar', 'CheckPersonalCalendar', {
                    params: {calendarId:calendarId,showEvents:showEvents},
                    callback: function(success, o) {
                            if(success) {
                                me.getPersonalCalendars();
                                //refresh calendario
                            }
                    },scope:me
            });
        },
        
        
        addPersonalCalendar:function(){
            var me=this;
            var view=Ext.create('Sonicle.webtop.calendar.view.PersonalCalendar',{mys:me.mys});
            var wnd = Ext.create({
                    xtype: 'window',
                    layout: 'fit',
                    height: 400,
                    width: 350,
                    title:me.mys.res('title.addPersonalCalendar'),
                    items: [view]
            });
            if(wnd) wnd.show();
        },
        
        editPersonalCalendar:function(){
            var me=this;
            var view=Ext.create('Sonicle.webtop.calendar.view.PersonalCalendar',{mys:me.mys});
            var wnd = Ext.create({
                    xtype: 'window',
                    layout: 'fit',
                    height: 400,
                    width: 350,
                    title:me.mys.res('title.editPersonalCalendar'),
                    items: [view]
            });
            if(wnd) {
                view.getForm().findField('calendarId').setValue(me.mys.test.calendarId);
                view.getForm().findField('userId').setValue(me.mys.test.userId);
                view.getForm().findField('name').setValue(me.mys.test.name);
                view.getForm().findField('description').setValue(me.mys.test.description);
                view.getForm().findField('color').setColor(me.mys.test.color);
                view.getForm().findField('private_').setValue(me.mys.test.private_);
                view.getForm().findField('busy').setValue(me.mys.test.busy);
                view.getForm().findField('sync').setValue(me.mys.test.sync);
                view.getForm().findField('default_').setValue(me.mys.test.default_);
                view.getForm().findField('defaultReminder').setValue(me.mys.test.defaultReminder);
                view.getForm().findField('defaultSendInvite').setValue(me.mys.test.defaultSendInvite);
                view.getForm().findField('defaultSendInvite').setValue(me.mys.test.defaultSendInvite);
                view.getForm().findField('showEvents').setValue(me.mys.test.showEvents);
                wnd.show();
            }        
        },
        
        deletePersonalCalendar:function(calendarId,default_,userId){
            var me=this;
            calendarId=me.mys.test.calendarId; //DA CANCELLARE!!!!
            default_=me.mys.test.default_;//DA CANCELLARE!!!!
            userId=me.mys.test.userId;//DA CANCELLARE!!!!
            WT.confirm(me.mys.res("dlg-deletingMessage"),function(r){
                if (r == 'yes'){
                   WT.ajaxReq('com.sonicle.webtop.calendar', 'DeletePersonalCalendar', {
                        params: {calendarId:calendarId,default_:default_,userId:userId},
                        callback: function(success, o) {
                                if(success) {
                                    me.getPersonalCalendars();
                                    //refresh calendario
                                }
                        },scope:me
                    });
                }
            },me);
            
        },
        
        viewOnlyPersonalCalendar:function(calendarId,userId){
            var me=this;
            calendarId=me.mys.test.calendarId; //DA CANCELLARE!!!!
            userId=me.mys.test.userId; //DA CANCELLARE!!!!
            WT.ajaxReq('com.sonicle.webtop.calendar', 'ViewOnlyPersonalCalendar', {
                 params: {calendarId:calendarId,userId:userId},
                 callback: function(success, o) {
                         if(success) {
                             me.getPersonalCalendars();
                             //refresh calendario
                         }
                 },scope:me
             });
            }
            
        
        
});
