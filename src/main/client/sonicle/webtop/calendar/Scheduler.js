/*
* WebTop Groupware is a bundle of WebTop Services developed by Sonicle S.r.l.
* Copyright (C) 2011 Sonicle S.r.l.
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

Ext.define('Sonicle.webtop.calendar.Scheduler', {
    extend: 'Ext.container.Container',
    /**
    * @cfg {Array} monthNames
    * An array of textual month names which can be overriden for localization support (defaults to Date.monthNames)
    */
    monthNames : Date.monthNames,
    /**
    * @cfg {Array} dayNames
    * An array of textual day names which can be overriden for localization support (defaults to Date.dayNames)
    */
    dayNames : Date.dayNames,
    /**
    * @cfg {String} Selection type
    * viewFlag "d" day "w" week "w5" work week "m" month
    */
    viewFlag : "d",
    /**
    * @cfg {Number} startDay
    * Day index at which the week should begin, 0-based (defaults to 0, which is Sunday)
    */
    startDay : 0,
    /**
    * @cfg {Number} startHour
    * Starting workig hour
    */
    startHour : 9,
    endHour : 18,
    /**
    * @cfg {Date} set date
    * Date
    */
    value : null,
    formContainer : null,
    hourstartworkday:null,
    hourendworkday:null,
    group : "",
    groupname : "",
    groupacl : "w",
    viewEvents : "1=1",
    waitMessage : null,
    defaultcalendar:"",
    defaultcalendarcolor:"",
    default_reminder:"",
    default_send_invite:"",
    isdrm:false,
    
    
    initComponent: function(){
        this.callParent(arguments);
        this.on("resize",this.resize,this);
		
        
        //this.value = this.value ? this.value.clearTime() : new Date().clearTime();    //TODO: non funziona clearTime
        this.value=new Date();
        this.sh = Ext.create('Sonicle.webtop.calendar.SchedulerHour',{sdparent : this});
        this.hdiv = this.sh.getHour();
        this.headerhdiv = this.sh.getHeaderHour();
        this.sd = new Array();
        this.sdiv = new Array();
        this.headersdiv = new Array();
        this.slistdiv = new Array();
        this.headerslistdiv = new Array();
        this.sdaydiv = new Array();
        this.hdrtdHour=new Array();
        for (var i=1;i<=31;i++) {
            this.sd[i] = Ext.create('Sonicle.webtop.calendar.SchedulerDay',{
                sdparent : this,
                monthNames : this.monthNames,
                dayNames : this.dayNames,
                startHour: this.startHour,
                endHour: this.endHour
            });
            this.sdiv[i] = this.sd[i].getSchedulerDay();
            this.headersdiv[i] = this.sd[i].getHeaderDay();
            this.hdrtdHour[i] = this.sd[i].getTitleDay();
            
            
            this.slistdiv[i] = this.sd[i].getSchedulerListDay();
            this.headerslistdiv[i] = this.sd[i].getHeaderListDay();
            
            this.sdaydiv[i] = this.sd[i].getSchedulerAllDay();
            //Ext.get(this.headerslistdiv[i]).on("click",this.newDateEvent,this);   //TODO
            //Ext.get(this.hdrtdHour[i]).on("click",this.newDateEvent,this);    //TODO
        }

        this.elHdr = document.createElement("DIV");
        this.elData = document.createElement("DIV");
        this.elData.className = "schedulerMainDiv";
        
    },
    
    // private
    render : function(container, position){
        var el = document.createElement("TABLE");
        el.cellPadding=0;
        el.cellSpacing=0;
        el.border=0;
        var elRowHdr = el.insertRow(-1);
        var elCellHdr = elRowHdr.insertCell(-1);
        elCellHdr.appendChild(this.elHdr);
        var elRowData = el.insertRow(-1);
        elRowData.height = "100%";
        var elCellData = elRowData.insertCell(-1);
        elCellData.appendChild(this.elData);

        // Obj to Ext
        container.dom.insertBefore(el, position);
        this.container = container;

        this.el = Ext.get(el);
        this.redoLayout(true);
    },
     
    redoLayout : function (firstTime) {
        if (firstTime==null) firstTime=false;
        var cs=this.cs;
        var setdate;
        var element = this.elHdr;
        while (element.firstChild)
            element.removeChild(element.firstChild);
        var elementData = this.elData;
        while (elementData.firstChild)
            elementData.removeChild(elementData.firstChild);

        if (this.viewFlag=="d") {
            this.elHdr.appendChild(this.headerhdiv);
            this.elHdr.appendChild(this.headersdiv[1]);
        } else if (this.viewFlag=="w5") {
            this.elHdr.appendChild(this.headerhdiv);
            for (var i=1;i<=5;i++) this.elHdr.appendChild(this.headersdiv[i]);
        }
        
        if (this.viewFlag=="d") {
            setdate = (new Date(this.value.getFullYear(), this.value.getMonth(), this.value.getDate())).clearTime();
            this.sd[1].setDate(setdate);
            this.elData.appendChild(this.hdiv);
            this.elData.appendChild(this.sdiv[1]);
            
        } else if (this.viewFlag=="w5") {
            var startingPos = this.value.getDay()-this.startDay;
            if(startingPos!=0 && startingPos < this.startDay) startingPos += 7;
            setdate = this.value.add("d",startingPos*-1).clearTime();
            while(setdate.getDay()<1) setdate = setdate.add("d",1).clearTime();
            this.elData.appendChild(this.hdiv);
            for (var i=1;i<=5;i++) {
                this.sd[i].setIndex(i);
                this.sd[i].setDate(setdate);
                this.elData.appendChild(this.sdiv[i]);
                setdate = setdate.add("d",1).clearTime();
            }
        }else if (this.viewFlag=="w") {
            var startingPos = this.value.getDay()-this.startDay;
            if(startingPos!=0 && startingPos < this.startDay) startingPos += 7;
            setdate = this.value.add("d",startingPos*-1).clearTime();
            while(setdate.getDay()<1) setdate = setdate.add("d",1).clearTime();
            for (var i=1;i<=7;i++) {
                this.sd[i].setDate(setdate);
                setdate = setdate.add("d",1).clearTime();
            }
            this.wtable = document.createElement("TABLE");
            this.wtable.cellPadding=0;
            this.wtable.cellSpacing=0;
            this.wtable.border=0;
            this.wtable.className="schedulerMainListView";
            var cell = new Array();
            var row1 = this.wtable.insertRow(-1);
            row1.style.height = "33%";
            cell[1] = row1.insertCell(-1);
            cell[1].style.width="50%";
            cell[4] = row1.insertCell(-1);
            cell[4].style.width="50%";
            var row2 = this.wtable.insertRow(-1);
            row2.style.height = "33%";
            cell[2] = row2.insertCell(-1);
            cell[2].style.width="50%";
            cell[5] = row2.insertCell(-1);
            cell[5].style.width="50%";
            var row3 = this.wtable.insertRow(-1);
            row3.style.height = "17%";
            var row3cs = this.wtable.insertRow(-1);
            row3cs.style.height = "17%";
            cell[3] = row3.insertCell(-1);
            cell[3].style.width="50%";
            cell[3].rowSpan = 2;
            cell[6] = row3.insertCell(-1);
            cell[6].style.width="50%";
            cell[7] = row3cs.insertCell(-1);
            cell[7].style.width="50%";
            var dtable = new Array();
            for (var i=1;i<=7;i++) {
                dtable[i] = document.createElement("TABLE");
                dtable[i].className="schedulerWeekView";
                dtable[i].style.width="100%";
                dtable[i].style.height="100%";
                var headerRow = dtable[i].insertRow(-1);
                var headerCell = headerRow.insertCell(-1);
                var dataRow = dtable[i].insertRow(-1);
                dataRow.style.height="100%";
                var dataCell = dataRow.insertCell(-1);
                headerCell.appendChild(this.headerslistdiv[i]);
                dataCell.appendChild(this.slistdiv[i]);
                cell[i].appendChild(dtable[i]);
            }
            this.elData.appendChild(this.wtable);
        } else if (this.viewFlag=="m") {
            var days = this.value.getDaysInMonth();
            var firstOfMonth = this.value.getFirstDateOfMonth();
            var startingPos = firstOfMonth.getDay();
            if (startingPos==0) startingPos=7;
            var dtable = new Array();
            for (var i=1;i<=31;i++) {
                dtable[i] = document.createElement("TABLE");
                dtable[i].className="schedulerMonthView";
                var headerRow = dtable[i].insertRow(-1);
                var headerCell = headerRow.insertCell(-1);
                var dataRow = dtable[i].insertRow(-1);
                var dataCell = dataRow.insertCell(-1);
                dataCell.style.height="100%";
                headerCell.appendChild(this.headerslistdiv[i]);
                dataCell.appendChild(this.slistdiv[i]);
            }
            var cell;
            var mday=0;
            this.mtable = document.createElement("TABLE");
            this.mtable.cellPadding=0;
            this.mtable.cellSpacing=0;
            this.mtable.border=0;
            this.mtable.className="schedulerMainListView";
            this.mtable.style.tableLayout="fixed";
            this.mtable.style.width="100%";
            this.mtable.style.height="120%";
            for (var w=1;w<=6;w++) {
                var wr1 = this.mtable.insertRow(-1);
                var wr2 = this.mtable.insertRow(-1);
                wr1.style.height="40";
                wr1.vAlign="top";
                wr2.style.height="40";
                wr2.vAlign="top";
                for (var i=1;i<=7;i++) {
                    if (i!=7)
                        cell = wr1.insertCell(-1);
                    else
                        cell = wr2.insertCell(-1);
                    if (i<=5) {
                        cell.style.width="17%";
                        cell.rowSpan = 2;
                    } else {
                        cell.style.width="15%";
                    }
                    var cellPos = ((w-1)*7)+i;
                    if (cellPos<startingPos || mday>=days) {
                        var nulltable = document.createElement("TABLE");
                        nulltable.className="schedulerNullMonthView";
                        var headerRow = nulltable.insertRow(-1);
                        var headerCell = headerRow.insertCell(-1);
                        headerCell.innerHTML="&nbsp;";
                        var dataRow = nulltable.insertRow(-1);
                        var dataCell = dataRow.insertCell(-1);
                        dataCell.innerHTML="&nbsp;";
                        dataCell.style.height="100%";
                        cell.appendChild(nulltable);
                    } else {
                        mday++;
                        setdate = (new Date(this.value.getFullYear(), this.value.getMonth(), mday)).clearTime();
                        this.sd[mday].setDate(setdate);
                        cell.appendChild(dtable[mday]);
                    }
                }
            }
            this.elData.appendChild(this.mtable);
        }

        if(Ext.isIE){
          this.el.repaint();
        }
        if (this.viewFlag=="d" || this.viewFlag=="w5") {
            if (firstTime || (this.previusView!="d" && this.previusView!="w5"))
                this.elData.scrollTop = 40*(this.startHour-1);
        } else if (this.viewFlag=="m" && this.previusView!="m") {
            this.elData.scrollTop = 0;
        }
        if (!firstTime) {
            this.resize(this.container,this.w);
        } else {
            this.schedulerMenu=new Ext.menu.Menu({
                items: [
                    new Ext.Action({
                        scope : this,
                        iconCls : 'iconPrint',
                        text: cs.res("print"),
                        handler: function(){
                            this.printEvents();
                        }
                    }),
                    new Ext.Action({
                        scope : this,
                        text: cs.res("today"),
                        handler: function(){
                            this.cs.setView("t");
                        }
                    })
                ]
            });
            //WT.app.setComponentContextMenu(this,this.schedulerMenu,this.getOffsetWidth,this);
        }
        this.previusView = this.viewFlag;
    },
    

    getOffsetWidth : function() {
        if (this.viewFlag=="w" || this.viewFlag=="m") return 0;
        return this.sh.getOffsetWidth();
    },

    getOffsetHeight : function() {
        if (this.viewFlag=="w" || this.viewFlag=="m") return 0;
        return this.sh.getOffsetHeight();
    },

    resize : function(container,w,h,rw,rh){
        this.w = w;
        this.width = w-18;
        this.hdrHeight = this.getOffsetHeight();
        if (this.viewFlag=="d") {
            var dWidth=this.width-this.getOffsetWidth();
            this.hdiv.style.top=this.hdrHeight;
            this.sdiv[1].style.top=this.hdrHeight;
            this.sdiv[1].style.width=dWidth;
            this.headersdiv[1].style.width=dWidth;
            this.headersdiv[1].style.left=this.getOffsetWidth();
            this.sd[1].doLayout(this.viewFlag,dWidth,h);
                this.sd[1].doListDayLayout(this.viewFlag,dWidth,h);
        } else if (this.viewFlag=="w5") {
            var w5Width=(this.width-this.getOffsetWidth())/5;
            this.hdiv.style.top=this.hdrHeight;
            for (var i=1;i<=5;i++) {
                this.sdiv[i].style.zIndex=0;
                this.sdiv[i].style.top=this.hdrHeight;
                this.sdiv[i].style.width=w5Width;
                this.headersdiv[i].style.width=w5Width;
                this.headersdiv[i].style.left=this.getOffsetWidth()+(w5Width*(i-1));
                this.sdiv[i].style.left=this.getOffsetWidth()+(w5Width*(i-1));
                this.sd[i].doLayout(this.viewFlag,w5Width,h);
                    this.sd[i].doListDayLayout(this.viewFlag,w5Width,h);
            }
        }else if (this.viewFlag=="w") {
            this.wtable.style.width="100%";
            this.wtable.style.height="100%";
            for (var i=1;i<=7;i++) {
                this.sd[i].doListLayout(this.viewFlag,w,h);
                
            }
        }else if (this.viewFlag=="m") {
            this.mtable.style.width="100%";
            this.mtable.style.height="120%";
            for (var i=1;i<=31;i++) {
                this.sd[i].doListLayout(this.viewFlag,w,h);
            }
        }
    },
    nrOfSelectedEvent : 0,
    selectEvent : function (select) {
        if (select)
            this.nrOfSelectedEvent++;
        else
            this.nrOfSelectedEvent--;
        if (this.nrOfSelectedEvent>0) {
          if (this.groupacl=='w')
            this.cs.bactiondelete.setDisabled(false);
        } else
          this.cs.bactiondelete.setDisabled(true);
    },
    
    deselectAllSchedulerEvents : function () {
        for (var i=1;i<=31;i++) {
            this.sd[i].deselectAllSchedulerEvents();
        }
        this.nrOfSelectedEvent=0;
        this.cs.bactiondelete.setDisabled(true);
    },
    
    findEvents: function(fe){
      /*var finderPanel = new WT.FinderPanel({
          dateFormat:this.format,
          findevents:fe,
          cs: this.cs
      });
      var w=WT.app.createWindow({
        title: this.cs.res("find")+" "+fe,
        width: 800,
        height: 540,
        layout: 'fit',
        iconCls: 'iconNewEvent',
        items: finderPanel
      });
      finderPanel.setWindow(w,this.cs);
      w.show();*/
    },
    
    
    
    printEvents: function(){ 
      var owner = this.groupname;
      var month = this.monthNames[this.value.getMonth()];
      var year = this.value.getFullYear();
      var max = this.value.getDaysInMonth();
      if (this.viewFlag=="d") {
          max=1;
      }else if (this.viewFlag=="w5") {
          max=5;
      } else if (this.viewFlag=="w") {
          max=7;
      }

      var htmlSrc = "";
      htmlSrc += "<TABLE BORDER=0 CELLSPACING=0 width='100%'>";

      // Title
      htmlSrc += "<TR BGCOLOR=GRAY>";
      htmlSrc += "<TD ALIGN=CENTER COLSPAN=2>";
      htmlSrc += "<H1>";
      htmlSrc += owner+" - "+month+" "+year;
      htmlSrc += "</H1>";
      htmlSrc += "</TD>";
      htmlSrc += "</TR>";
      htmlSrc += "<TR BGCOLOR=GRAY>";
      htmlSrc += "<TD ALIGN=CENTER COLSPAN=2>";
      htmlSrc += "<H2>";
      d = this.sd[1].getDate();
      htmlSrc += this.dayNames[d.getDay()]+" "+d.getDate()
      if (max>1) {
        d = this.sd[max].getDate();
        htmlSrc += " - "+this.dayNames[d.getDay()]+" "+d.getDate()
      }
      htmlSrc += "</H2>";
      htmlSrc += "</TD>";
      htmlSrc += "</TR>";
      
      // Events
      var d = null;
      var cspan = "";
      if (max==1) cspan="COLSPAN=2";
      for (var i=1;i<=max;i++) {
        d = this.sd[i].getDate();
        var len = this.sd[i].evts.length;
        if (len>0) {
          for (var j=0;j<len;j++) {
            var sevent=this.sd[i].evts[j];
            WT.debug("event="+sevent.getCustomer()+" "+sevent.getReport()+" "+sevent.getActivity());
            htmlSrc += "<TR>";
            if (j==0 && max>1) {
              htmlSrc += "<TD width=100>"+this.dayNames[d.getDay()]+" "+d.getDate()+"</TD>";
            } else if (max>1) {
              htmlSrc += "<TD width=100>&nbsp;</TD>";
            }
            htmlSrc += "<TD "+cspan+">"+sevent.getStartHour()+":"+sevent.getStartMinute()+" "+sevent.getEndHour()+":"+sevent.getEndMinute()+" "+sevent.event+" "+WT.res("calendar","textLocation")+sevent.getReport()+" - "+WT.res("calendar","activity")+sevent.getActivity()+" - "+WT.res("calendar","customer")+sevent.getCustomer()+"</TD>";
            htmlSrc += "</TR>";
          } 
        }else {
            htmlSrc += "<TR>";
            if (max>1)
              htmlSrc += "<TD width=100>"+this.dayNames[d.getDay()]+" "+d.getDate()+"</TD>";
            htmlSrc += "<TD "+cspan+">&nbsp;</TD>";
            htmlSrc += "</TR>";
        }
      }

      htmlSrc += "</TABLE>";

      WT.app.print(htmlSrc);
    },
    
    /*reader: new Ext.data.JsonReader({
        root: 'events',
        totalProperty: 'totalRecords',
        fields: [ 
            'event_id',
            'yyyy',
            'mm',
            'dd',
            'event',
            'hstart',
            'mstart',
            'hend',
            'mend',
            'hasDescription',
            'isRecurrence',
            'isReminder',
            'isReadonly',
            'isPlanning',
            'calendar',
            'colorcalendar',
            'allday',
            'startday',
            'startmonth',
            'startyear',
            'endday',
            'endmonth',
            'endyear',
            'timezone',
            'fromRecurrence',
            'visit',
            'activity',
            'report_id',
            'customer_description',
            'recurr_id'
        ]
    }),*/
    
    
    
    
    editEventByAttachment: function(event_id,ev,planninggrid){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var params={
            service:'calendar',action:'EditEvent',event_id:event_id,ev:ev,planninggrid:planninggrid
        };
        this.proxyEdit.doRequest(
            'read',null,
            params,
            this.eventReader,
            this.editEventResultByAttachment,
            this,
            params
        );
            
            
    },
   
   
   editEventResultByAttachment: function(o, params, success){
      this.waitMessage.hide();
      if (o && success) {
        var r=o.records;
        var item=r[0];          

        
        this.deselectAllSchedulerEvents();
        var eventForm = this.formContainer.getEventForm();
        if (this.groupacl!='w') {
            eventForm.saveEvent.disable();
            eventForm.deleteEvent.disable();
        }
        

        eventForm.event_id = params.ev.event_id;
        eventForm.event_by = item.get("by");
        eventForm.event.setValue(params.ev.summary);
        eventForm.report_id.setValue(params.ev.location);
        //eventForm.enddate.setVisible(false);
        if (item.get("recurr_id")!=""){
            eventForm.yyyymmdd.setDisabled(true);
            eventForm.enddate.setDisabled(true);
            eventForm.groupCombo.setDisabled(true);
        }else{
            eventForm.customer_id.getStore().reload();
            eventForm.planningGrid.getStore().baseParams.event_id=item.get('event_id');
            eventForm.planningGrid.getStore().on('load',function(s){
                if (s.getCount()>1){
                   eventForm.groupCombo.setDisabled(true); 
                }
            },
            this);
            eventForm.planningGrid.getStore().baseParams.calendar_id=item.get('calendar');
            eventForm.planningGrid.getStore().load();
            
            
            
        }
        
        eventForm.busy_value.setValue(item.get("busy"));
        eventForm.causal.getStore().on("load",function(){
            eventForm.causal.setValue(item.get("causal_id"));
        },this);
        eventForm.causal.getStore().on("beforeload",function(){
            eventForm.causal.getStore().baseParams.login_id=item.get("by");
            eventForm.causal.getStore().baseParams.customer_id=eventForm.customer_id.getValue();
        },this);
        eventForm.causal.getStore().load();
        
        WT.debug("isDrm="+this.isdrm);
        /*if (this.isdrm=="false") {
            eventForm.causal.setVisible(false);
            eventForm.field_causal.setVisible(false);
        }*/
        
        var eto=new Date(params.ev.endyyyy,params.ev.endmm,params.ev.enddd,params.ev.endhh,params.ev.endmmm,params.ev.endssss);
        var efrom=new Date(params.ev.startyyyy,params.ev.startmm,params.ev.startdd,params.ev.starthh,params.ev.startmmm,params.ev.startssss);
        var edate=new Date(params.ev.startyyyy,params.ev.startmm,params.ev.startdd,params.ev.starthh,params.ev.startmmm,params.ev.startssss);
        var sstart=null;
        //var allday=null;
        eventForm.yyyymmdd.setValue(edate);
        
        // eventForm.fthh.setValue(efrom);
	eventForm.fthh.setValue((!sstart||sstart=="")?efrom:sstart);
        eventForm.tthh.setValue(eto);
        eventForm.planningGrid.getStore().loadData(params.planninggrid);
        
        //eventForm.yyyymmdd.setValue(new Date(params.ev.startyyyy,params.ev.startmm,params.ev.startdd));
        //eventForm.fthh.setValue(params.ev.starthh+":"+params.ev.startmmm);
        //eventForm.tthh.setValue(params.ev.endhh+":"+params.ev.endmmm);
        eventForm.description.setValue(params.ev.description);
        if (item.get("private_value")!=null && item.get("private_value")=="Y")
          eventForm.private_value.setValue(true);
        else
          eventForm.private_value.setValue(false);
        if (item.get("reminder")!=null && item.get("reminder")!="")
          eventForm.reminder.setValue(item.get("reminder"));
        eventForm.share_with.setValue(item.get("share_with"));
        eventForm.activity_id.setValue(item.get("activity_id"));
        eventForm.activity_flag.setValue(item.get("activity_flag"));
        eventForm.uid=item.get('uid');
        
        
        if (item.get("customer_id")!=null && item.get("customer_id")!=""){
          eventForm.customer_id.setValue(item.get("customer_id"));
        }
        if (item.get("statistic_id")!=null && item.get("statistic_id")!="null" && item.get("statistic_id")!="")
          eventForm.statistic_id.setValue(item.get("statistic_id"));
        eventForm.recurr_id=item.get("recurr_id");
        eventForm.allday.setValue(item.get("allday"));
        var end=item.get("endday")+"/"+item.get("endmonth")+"/"+item.get("endyear");
        eventForm.enddate.setValue(end);
        if (item.get("allday")=="true"){
           eventForm.fthh.setDisabled(true);
           eventForm.tthh.setDisabled(true);
        }
        
        
        /*if (item.get("calendar")=="WebTop"){
             eventForm.calendar_id.setValue(this.groupname);
        }else{
             eventForm.calendar_id.setValue(item.get("calendar"));
        }*/
        //eventForm.calendar_id.getStore().on("load",function(){
            eventForm.calendar_id.setValue(item.get("calendar"));
        //},this);
        eventForm.calendar_id.getStore().load();
        
        eventForm.timezone_id.setValue(item.get("timezone"));
        
        //disabilito campi se evento da email
        eventForm.event.setDisabled(true);
        eventForm.report_id.setDisabled(true);
        eventForm.description.setDisabled(true);
        eventForm.yyyymmdd.setDisabled(true);
        eventForm.enddate.setDisabled(true);
        eventForm.fthh.setDisabled(true);
        eventForm.tthh.setDisabled(true);
        eventForm.allday.setDisabled(true);
        eventForm.startButton.setDisabled(true);
        eventForm.endButton.setDisabled(true);
        //
        
        
        if (params.ev.recurrence==true){
            eventForm.dayly_step.setValue("1");
            eventForm.weekly_step.setValue("1");
            eventForm.monthly_day.setValue("1");
            eventForm.monthly_month.setValue("1");
            eventForm.yearly_day.setValue("1");
            eventForm.yearly_month.setValue("1");
            eventForm.repeat_times_step.setValue("1");
            eventForm.eventtab.frommailrec=true;
            
        }
        //set ricorrenza
        if (params.ev.recurr_type=='D'){
           eventForm.none_recurrence.setValue(false);
           if (params.ev.dayly_freq!="null" && params.ev.dayly_freq!="" ){
               eventForm.dayly_recurrence.setValue(true);
               eventForm.dayly1.setValue(true);
               eventForm.dayly_step.setValue(params.ev.dayly_freq);
               eventForm.dayly1.setDisabled(false);
               eventForm.dayly_step.setDisabled(false);
               eventForm.dayly2.setDisabled(false);
           }
           eventForm.until_yyyymmdd.setValue(new Date(params.ev.until_date_yyyy,params.ev.until_date_mm-1,params.ev.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (params.ev.recurr_type=='F'){
           eventForm.none_recurrence.setValue(false);
           eventForm.dayly_recurrence.setValue(true);
           eventForm.dayly1.setDisabled(false);
           eventForm.dayly_step.setDisabled(false);
           eventForm.dayly2.setDisabled(false);
           eventForm.dayly2.setValue(true);
           eventForm.until_yyyymmdd.setValue(new Date(params.ev.until_date_yyyy,params.ev.until_date_mm-1,params.ev.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           

        }else if (params.ev.recurr_type=='W'){
           eventForm.none_recurrence.setValue(false);
           eventForm.weekly_recurrence.setValue(true);
           eventForm.weekly_step.setValue(params.ev.weekly_freq);
           eventForm.weekly_step.setDisabled(false);
           if (params.ev.weekly_day1=="true") eventForm.weekly1.setValue(true);
           if (params.ev.weekly_day2=="true") eventForm.weekly2.setValue(true);
           if (params.ev.weekly_day3=="true") eventForm.weekly3.setValue(true);
           if (params.ev.weekly_day4=="true") eventForm.weekly4.setValue(true);
           if (params.ev.weekly_day5=="true") eventForm.weekly5.setValue(true);
           if (params.ev.weekly_day6=="true") eventForm.weekly6.setValue(true);
           if (params.ev.weekly_day7=="true") eventForm.weekly7.setValue(true);
           eventForm.weekly1.setDisabled(false);
           eventForm.weekly2.setDisabled(false);
           eventForm.weekly3.setDisabled(false);
           eventForm.weekly4.setDisabled(false);
           eventForm.weekly5.setDisabled(false);
           eventForm.weekly6.setDisabled(false);
           eventForm.weekly7.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(params.ev.until_date_yyyy,params.ev.until_date_mm-1,params.ev.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (params.ev.recurr_type=='M'){
           eventForm.none_recurrence.setValue(false);
           eventForm.monthly_recurrence.setValue(true);
           eventForm.monthly_month.setValue(params.ev.monthly_month);
           eventForm.monthly_day.setValue(params.ev.monthly_day);
           eventForm.monthly_month.setDisabled(false);
           eventForm.monthly_day.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(params.ev.until_date_yyyy,params.ev.until_date_mm-1,params.ev.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (params.ev.recurr_type=='Y'){
           eventForm.none_recurrence.setValue(false);
           eventForm.yearly_recurrence.setValue(true);
           eventForm.yearly_day.setValue(params.ev.yearly_day);
           eventForm.yearly_month.setValue(params.ev.yearly_month);
           eventForm.yearly_month.setDisabled(false);
           eventForm.yearly_day.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(params.ev.until_date_yyyy,params.ev.until_date_mm-1,params.ev.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }
        if (params.ev.permanent=="true"){
               eventForm.permanent_recurrence.setValue(true);
               eventForm.radio_until_yyyymmdd.setValue(false);
               eventForm.until_yyyymmdd.setDisabled(false);
               eventForm.until_yyyymmdd.setValue(new Date(params.ev.startyyyy,params.ev.startmm,params.ev.startdd));
               eventForm.until_yyyymmdd.setDisabled(true);
            }
            if (params.ev.repeat!=null && params.ev.repeat!="0"){
                eventForm.repeat_times.setDisabled(false);
                eventForm.repeat_times.setValue(true);
                eventForm.repeat_times_step.setDisabled(false);
                eventForm.radio_until_yyyymmdd.setValue(false);
                eventForm.until_yyyymmdd.setDisabled(true);
                eventForm.repeat_times_step.setValue(params.ev.repeat);
            }
        
        //
        
        
        
        
        var w=WT.app.createWindow({
          title: item.get("event"),
          width: 640,
          height:500,
          layout: 'fit',
          iconCls: 'iconNewEvent',
          items: eventForm
        });
        
        
        if (eventForm.uid!=""){     //se � un evento generato da un invito via mail disabilito editing grid
                eventForm.planningGrid.on('beforeedit',function(e){
                    e.cancel=true;
                    eventForm.planningGrid.getTopToolbar().setDisabled(true);
                },this);

                eventForm.planningGrid.getSelectionModel().on('contextmenu',function(e){
                    eventForm.planningGrid.getTopToolbar().setDisabled(true);
                },this);

                eventForm.planningGrid.getSelectionModel().on('beforerowselect',function( sm, row, keepExisting,record ){
                    eventForm.menuPlanning.setDisabled(true);
                    return false;

                },this);
                eventForm.planningGrid.getColumnModel().setRenderer(0,function(v, p, record){
                    if (v=='true') 
                        return "<input checked=checked name='check[]' type='checkbox' value="+v+" disabled>";
                    else
                        return "<input name='check[]' type='checkbox' value="+v+" disabled>";
                 });
            
        }
        
        
      eventForm.groupCombo.getStore().on("load", function(store) {
                eventForm.groupCombo.setValue(this.group, false);
                var r=store.getById(this.group);
                var el=eventForm.calendar_id.getEl();            
                el.setStyle('background-image','none');
                el.setStyle('background-color',item.get('colorcalendar'));

         }, this);
        
        
        eventForm.groupCombo.on("select", function(c,rec,i) {
                if (rec.get('acl')!='w'){
                    eventForm.saveEvent.setDisabled(true);
                    eventForm.deleteEvent.setDisabled(true);
                }
                var el=eventForm.calendar_id.getEl();            
                el.setStyle('background-image','none');
                el.setStyle('background-color',rec.get('colorcalendar'));
                eventForm.private_value.setValue(rec.get('privatevalue'));
                eventForm.busy_value.setValue(rec.get('busy'));
                eventForm.calendar_id.reset();
                eventForm.calendar_id.getStore().baseParams.user=c.getValue();
                eventForm.calendar_id.getStore().load();
                eventForm.calendar_id.setValue(this.defaultcalendar);
                eventForm.causal.getStore().baseParams.login_id=c.getValue();
                eventForm.causal.getStore().load();
        
                
         }, this);
         
        eventForm.allday.on("check",function(c,checked){
                eventForm.fthh.setDisabled(checked);
                eventForm.fthh.setValue(this.hourstartworkday);
                eventForm.tthh.setDisabled(checked);
                eventForm.tthh.setValue(this.hourendworkday);
        
        },this);
        /*
        eventForm.enddate.on("select",function(c,n,o){
            eventForm.allday.setValue(true);
        },this);
        */
        eventForm.calendar_id.getStore().on('beforeload',function(s){
            s.baseParams.user=eventForm.groupCombo.getValue();
        },this);

        eventForm.calendar_id.on("select",function(s,r){
                var el = s.getEl();
                //el.setStyle('color', r.get('color'));
                el.setStyle('background-image','none');
                el.setStyle('background-color',r.get('color'));
                eventForm.private_value.setValue(r.get('privatevalue'));
                eventForm.busy_value.setValue(r.get('busy'));
                eventForm.planningGrid.getStore().baseParams.calendar_id=r.get("id");
                eventForm.calendar_id.default_send_invite=r.get("default_send_invite");
                eventForm.planningGrid.getStore().load();
         },this);
        var color=item.get('colorcalendar');
        eventForm.calendar_id.on("render",function(c){
            var el=c.getEl();            
            el.setStyle('background-image','none');
            el.setStyle('background-color',color);
        },this);
        
        eventForm.calendar_id.getStore().load();
        
        eventForm.planningGrid.getStore().on('update',function(s,record){
            if (record.get('name')==""){
                s.remove(record);
            }
            
            
        },this);
        
        
        eventForm.yyyymmdd.on("select",function(t,n,o){
            if (n>eventForm.enddate.getValue()){
                eventForm.enddate.setValue(n);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.enddate.on("select",function(t,n,o){
            if (n<eventForm.yyyymmdd.getValue()){
                eventForm.enddate.setValue(eventForm.yyyymmdd.getValue());
            }
            eventForm.doLayout();
        },this);
        
        eventForm.fthh.on("select",function(t,rec,index){
            if (eventForm.fthh.getValue()>=eventForm.tthh.getValue()){
               //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.fthh.getValue().substring(0,2);
                var min=eventForm.fthh.getValue().substring(3,5);
                if (parseInt(hour)=="0"){
                    hour=eventForm.fthh.getValue().substring(1,2);
                    hour=parseInt(hour)+1;
                }else{
                    hour=parseInt(hour)+1;
                }
                if (hour>23)
                    hour="00";
                eventForm.tthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        
        //eventForm.activity_id.getStore().on("beforeload",function(){
            eventForm.activity_id.getStore().baseParams.login=this.group;
        //},this);
        
        eventForm.tthh.on("select",function(t,n,o){
            if (eventForm.tthh.getValue()<=eventForm.fthh.getValue()){
                //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.tthh.getValue().substring(0,2);
                var min=eventForm.tthh.getValue().substring(3,5);
                if (hour=="00")
                    hour="23"
                else{ 
                    if (parseInt(hour)=="0"){
                        hour=eventForm.tthh.getValue().substring(1,2);
                        hour=parseInt(hour)-1;
                    }else{
                        hour=parseInt(hour)-1;
                    }
                }
                if (hour>23)
                    hour="00";
                eventForm.fthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.setWindow(w,this.cs);
        w.show();
      }
    },
    
    
    
    newDateEvent: function(e){
        var target = e.getTarget();
        this.newEvent(target.sdate,new Date(),new Date(),true);
    },
    
    newEventByAttachment: function(event,planning) {
        this.deselectAllSchedulerEvents();
        var eventForm = this.formContainer.getEventForm();
        eventForm.eventtab.frommail=true;
        eventForm.hourstartworkday=this.hourstartworkday;
        eventForm.hourendworkday=this.hourendworkday;
        eventForm.event_id = event.event_id;
        if (event.event_id=="")
            eventForm.event_id = null;
        eventForm.event_by = this.group;
        eventForm.uid=event.uid;
        var eto=new Date(event.endyyyy,event.endmm,event.enddd,event.endhh,event.endmmm,event.endssss);
        var efrom=new Date(event.startyyyy,event.startmm,event.startdd,event.starthh,event.startmmm,event.startssss);
        var edate=new Date(event.startyyyy,event.startmm,event.startdd,event.starthh,event.startmmm,event.startssss);
        var sstart=null;
        //var allday=null;
        eventForm.yyyymmdd.setValue(edate);
        
        // eventForm.fthh.setValue(efrom);
	eventForm.fthh.setValue((!sstart||sstart=="")?efrom:sstart);
        eventForm.tthh.setValue(eto);
        eventForm.calendar_id.setValue(this.groupname);
        eventForm.planningGrid.getStore().loadData(planning);
        eventForm.planningGrid.getTopToolbar().setDisabled(true);
        if (event.summary!=null) eventForm.event.setValue(event.summary);
        if (event.description!=null) eventForm.description.setValue(event.description);
        if (event.location!=null) eventForm.report_id.setValue(event.location);
        
        eventForm.enddate.setValue(edate);        
        
        eventForm.calendar_id.setValue(this.groupname);
        
        eventForm.timezone_id.setValue(this.defaulttimezone);
        
        //disabilito campi se evento da email
        eventForm.event.setDisabled(true);
        eventForm.report_id.setDisabled(true);
        eventForm.description.setDisabled(true);
        eventForm.yyyymmdd.setDisabled(true);
        eventForm.enddate.setDisabled(true);
        eventForm.fthh.setDisabled(true);
        eventForm.tthh.setDisabled(true);
        eventForm.allday.setDisabled(true);
        eventForm.startButton.setDisabled(true);
        eventForm.endButton.setDisabled(true);
        //
        eventForm.causal.getStore().on("beforeload",function(){
            eventForm.causal.getStore().baseParams.login_id=this.group;
            eventForm.causal.getStore().baseParams.customer_id=eventForm.customer_id.getValue();
            eventForm.causal.getStore().baseParams.all="false";
        },this);
        eventForm.causal.getStore().load();
        
        WT.debug("isDrm="+this.isdrm);
        /*if (this.isdrm=="false") {
            eventForm.causal.setVisible(false);
            eventForm.field_causal.setVisible(false);
        }*/
        if (event.recurrence==true){
            eventForm.dayly_step.setValue("1");
            eventForm.weekly_step.setValue("1");
            eventForm.monthly_day.setValue("1");
            eventForm.monthly_month.setValue("1");
            eventForm.yearly_day.setValue("1");
            eventForm.yearly_month.setValue("1");
            eventForm.repeat_times_step.setValue("1");
            eventForm.eventtab.frommailrec=true;
            
        }
        //set ricorrenza
        if (event.recurr_type=='D'){
           eventForm.none_recurrence.setValue(false);
           if (event.dayly_freq!="null" && event.dayly_freq!="" ){
               eventForm.dayly_recurrence.setValue(true);
               eventForm.dayly1.setValue(true);
               eventForm.dayly_step.setValue(event.dayly_freq);
               eventForm.dayly1.setDisabled(false);
               eventForm.dayly_step.setDisabled(false);
               eventForm.dayly2.setDisabled(false);
           }
           eventForm.until_yyyymmdd.setValue(new Date(event.until_date_yyyy,event.until_date_mm-1,event.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (event.recurr_type=='F'){
           eventForm.none_recurrence.setValue(false);
           eventForm.dayly_recurrence.setValue(true);
           eventForm.dayly1.setDisabled(false);
           eventForm.dayly_step.setDisabled(false);
           eventForm.dayly2.setDisabled(false);
           eventForm.dayly2.setValue(true);
           eventForm.until_yyyymmdd.setValue(new Date(event.until_date_yyyy,event.until_date_mm-1,event.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           

        }else if (event.recurr_type=='W'){
           eventForm.none_recurrence.setValue(false);
           eventForm.weekly_recurrence.setValue(true);
           eventForm.weekly_step.setValue(event.weekly_freq);
           eventForm.weekly_step.setDisabled(false);
           if (event.weekly_day1=="true") eventForm.weekly1.setValue(true);
           if (event.weekly_day2=="true") eventForm.weekly2.setValue(true);
           if (event.weekly_day3=="true") eventForm.weekly3.setValue(true);
           if (event.weekly_day4=="true") eventForm.weekly4.setValue(true);
           if (event.weekly_day5=="true") eventForm.weekly5.setValue(true);
           if (event.weekly_day6=="true") eventForm.weekly6.setValue(true);
           if (event.weekly_day7=="true") eventForm.weekly7.setValue(true);
           eventForm.weekly1.setDisabled(false);
           eventForm.weekly2.setDisabled(false);
           eventForm.weekly3.setDisabled(false);
           eventForm.weekly4.setDisabled(false);
           eventForm.weekly5.setDisabled(false);
           eventForm.weekly6.setDisabled(false);
           eventForm.weekly7.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(event.until_date_yyyy,event.until_date_mm-1,event.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (event.recurr_type=='M'){
           eventForm.none_recurrence.setValue(false);
           eventForm.monthly_recurrence.setValue(true);
           eventForm.monthly_month.setValue(event.monthly_month);
           eventForm.monthly_day.setValue(event.monthly_day);
           eventForm.monthly_month.setDisabled(false);
           eventForm.monthly_day.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(event.until_date_yyyy,event.until_date_mm-1,event.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (event.recurr_type=='Y'){
           eventForm.none_recurrence.setValue(false);
           eventForm.yearly_recurrence.setValue(true);
           eventForm.yearly_day.setValue(event.yearly_day);
           eventForm.yearly_month.setValue(event.yearly_month);
           eventForm.yearly_month.setDisabled(false);
           eventForm.yearly_day.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(event.until_date_yyyy,event.until_date_mm-1,event.until_date_dd));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }
        if (event.permanent=="true"){
               eventForm.permanent_recurrence.setValue(true);
               eventForm.radio_until_yyyymmdd.setValue(false);
               eventForm.until_yyyymmdd.setDisabled(false);
               eventForm.until_yyyymmdd.setValue(new Date(event.startyyyy,event.startmm,event.startdd));
               eventForm.until_yyyymmdd.setDisabled(true);
            }
            if (event.repeat!=null && event.repeat!="0"){
                eventForm.repeat_times.setDisabled(false);
                eventForm.repeat_times.setValue(true);
                eventForm.repeat_times_step.setDisabled(false);
                eventForm.radio_until_yyyymmdd.setValue(false);
                eventForm.until_yyyymmdd.setDisabled(true);
                eventForm.repeat_times_step.setValue(event.repeat);
            }
        
        //
        
        
        var w=WT.app.createWindow({
          title: eventForm.formtitle,
          width: 640,
          height:500,
          layout: 'fit',
          iconCls: 'iconNewEvent',
          items: eventForm
        });
        
        eventForm.planningGrid.getColumnModel().setRenderer(0,function(v, p, record){
                    if (v=='true') 
                        return "<input checked=checked name='check[]' type='checkbox' value="+v+" disabled>";
                    else
                        return "<input name='check[]' type='checkbox' value="+v+" disabled>";
         });
            
        
        
        
        eventForm.planningGrid.on('beforeedit',function(e){
            e.cancel=true;
            eventForm.planningGrid.getTopToolbar().setDisabled(true);
        },this);
        
        eventForm.planningGrid.getSelectionModel().on('contextmenu',function(e){
            eventForm.planningGrid.getTopToolbar().setDisabled(true);
        },this);
       
        eventForm.planningGrid.getSelectionModel().on('beforerowselect',function( sm, row, keepExisting,record ){
            eventForm.menuPlanning.setDisabled(true);
            return false;
        
        },this);
        
        eventForm.groupCombo.getStore().on("load", function(store) {
                eventForm.groupCombo.setValue(this.group, false);
                var r=store.getById(this.group);
                var el=eventForm.calendar_id.getEl();
                eventForm.private_value.setValue(r.get('privatevalue'));
                eventForm.busy_value.setValue(r.get('busy'));
                el.setStyle('background-image','none');
                el.setStyle('background-color',r.get('colorcalendar'));
                //eventForm.calendar_id.setValue(this.groupname);
         }, this);
        
        eventForm.groupCombo.on("select", function(c,rec,i) {
                if (rec.get('acl')!='w'){
                    eventForm.saveEvent.setDisabled(true);
                    eventForm.deleteEvent.setDisabled(true);
                }
                var el=eventForm.calendar_id.getEl();            
                el.setStyle('background-image','none');
                el.setStyle('background-color',rec.get('colorcalendar'));
                eventForm.private_value.setValue(rec.get('privatevalue'));
                eventForm.busy_value.setValue(rec.get('busy'));
                eventForm.calendar_id.reset();
                eventForm.calendar_id.getStore().baseParams.user=c.getValue();
                eventForm.calendar_id.getStore().load();
                eventForm.calendar_id.setValue(eventForm.groupCombo.getRawValue());
                eventForm.causal.getStore().baseParams.login_id=c.getValue();
                eventForm.causal.getStore().load();
        
         }, this);
        eventForm.allday.on("check",function(c,checked){
                eventForm.fthh.setDisabled(checked);
                eventForm.fthh.setValue(this.hourstartworkday);
                eventForm.tthh.setDisabled(checked);
                eventForm.tthh.setValue(this.hourendworkday);
        
        },this);
        
        eventForm.calendar_id.getStore().on('beforeload',function(s){
            s.baseParams.user=eventForm.groupCombo.getValue();
        },this);
        

        eventForm.calendar_id.on("select",function(s,r){
                var el = s.getEl();
                //el.setStyle('color', r.get('color'));
                el.setStyle('background-image','none');
                el.setStyle('background-color',r.get('color'));
                eventForm.private_value.setValue(r.get('privatevalue'));
                eventForm.busy_value.setValue(r.get('busy'));
         },this);
         
         eventForm.yyyymmdd.on("select",function(t,n,o){
            if (n>eventForm.enddate.getValue()){
                eventForm.enddate.setValue(n);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.enddate.on("select",function(t,n,o){
            if (n<eventForm.yyyymmdd.getValue()){
                eventForm.enddate.setValue(eventForm.yyyymmdd.getValue());
            }
            eventForm.doLayout();
        },this);
        
        eventForm.fthh.on("select",function(t,rec,index){
            if (eventForm.fthh.getValue()>=eventForm.tthh.getValue()){
               //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.fthh.getValue().substring(0,2);
                var min=eventForm.fthh.getValue().substring(3,5);
                if (parseInt(hour)=="0"){
                    hour=eventForm.fthh.getValue().substring(1,2);
                    hour=parseInt(hour)+1;
                }else{
                    hour=parseInt(hour)+1;
                }
                if (hour>23)
                    hour="00";
                eventForm.tthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.tthh.on("select",function(t,n,o){
            if (eventForm.tthh.getValue()<=eventForm.fthh.getValue()){
                //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.tthh.getValue().substring(0,2);
                var min=eventForm.tthh.getValue().substring(3,5);
                if (hour=="00")
                    hour="23"
                else{ 
                    if (parseInt(hour)=="0"){
                        hour=eventForm.tthh.getValue().substring(1,2);
                        hour=parseInt(hour)-1;
                    }else{
                        hour=parseInt(hour)-1;
                    }
                }
                if (hour>23)
                    hour="00";
                eventForm.fthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
         
         /*
        eventForm.none_recurrence.on('check',function(c,check){
            if (check==false){
                eventForm.eventtab.hideTabStripItem(eventForm.planningGrid);
                eventForm.doLayout();
            }else{
                eventForm.eventtab.unhideTabStripItem(eventForm.planningGrid);
                eventForm.doLayout();
            }
        },this);
        /*
        eventForm.planningGrid.getStore().on('update',function(s,record){
            var records = s.getRange();
            var existrecord=false;
            if (record.get('name')==""){
                s.remove(record);
            }
            for (var i = 0; i < records.length; i++) {
                var n=records[i].get('name');
                if (n!=""){
                    existrecord=true;
                    break;
                }
            }
            if (existrecord){
                eventForm.eventtab.hideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }else{
                eventForm.eventtab.unhideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }

        },this);
        
        eventForm.planningGrid.getStore().on('remove',function(s,record){
            var records = s.getRange();
            var existrecord=false;
            WT.debug('change');
            if (record.get('name')==""){
                s.remove(record);
            }
            for (var i = 0; i < records.length; i++) {
                var n=records[i].get('name');
                if (n!=""){
                    existrecord=true;
                    break;
                }
            }
            if (existrecord){
                eventForm.eventtab.hideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }else{
                eventForm.eventtab.unhideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }

        },this);
        */
        
        
        eventForm.setWindow(w,this.cs);
        
        w.show();
      
      
    },
    
    resetDirty:function(field){
        field.originalValue = field.getValue();
    },
    
    
    newEventDrmVisit:function(event_id){
        var serviceDrm=WT.app.getServiceByName('drm');
        var panel=serviceDrm.getPanelVisit(event_id,this.group);
        var w=WT.app.createWindow({
          title: WT.res("drm","visit"),
          width: 750,
          height:600,
          layout: 'fit',
          iconCls: 'icon-addvisit',
          panel:panel,
          items: panel
        });
        w.panel.window_external=w;
        w.on("close",function(){
           this.cs.scheduler.load(); 
        },this);
        w.show();
        
    },
    
    openEventDrmVisit:function(event_id){
        var serviceDrm=WT.app.getServiceByName('drm');
        var panel=serviceDrm.getEditPanelVisit(event_id);
        var w=WT.app.createWindow({
          title: WT.res("drm","visit"),
          width: 750,
          height:600,
          layout: 'fit',
          iconCls: 'icon-addvisit',
          panel:panel,
          items: panel
        });
        w.panel.window_external=w;
        w.on("close",function(){
           this.cs.scheduler.load(); 
        },this);
        w.show();
        
    },
    
    
    newEvent: function(edate,efrom,eto,allday,sevent,sdescription,sactivity_id,sactivity_flag,scalendar_id,slocation,scustomer_id,sstatistic_id,sstart){
        
        if (this.groupacl!='w') { 
            WT.alert(WT.res('error'),WT.res('alertreadonly'));
            return;
        }
        
        
        this.deselectAllSchedulerEvents();
        var eventForm = this.formContainer.getEventForm();
        eventForm.hourstartworkday=this.hourstartworkday;
        eventForm.hourendworkday=this.hourendworkday;
        eventForm.event_id = null;
        eventForm.event_by = this.group;
        eventForm.yyyymmdd.setValue(edate);
        eventForm.fthh.setValue((!sstart||sstart=="")?efrom:sstart);
        eventForm.tthh.setValue(eto);
        
        eventForm.causal.getStore().on("beforeload",function(){
            eventForm.causal.getStore().baseParams.login_id=this.group;
            eventForm.causal.getStore().baseParams.customer_id=eventForm.customer_id.getValue();
            eventForm.causal.getStore().baseParams.all="false";
        },this);
        eventForm.causal.getStore().load();
        
        
        
        WT.debug("isDrm="+this.isdrm);
        /*if (this.isdrm=="false") {
            eventForm.causal.setVisible(false);
            eventForm.field_causal.setVisible(false);
        }*/
        eventForm.planningGrid.getStore().baseParams.calendar_id=this.defaultcalendar;
        eventForm.planningGrid.getStore().load();
        eventForm.dayly1.setValue(true);
        eventForm.dayly_step.setValue("1");
        eventForm.weekly_step.setValue("1");
        eventForm.monthly_day.setValue("1");
        eventForm.monthly_month.setValue("1");
        eventForm.yearly_day.setValue("1");
        eventForm.yearly_month.setValue("1");
        eventForm.repeat_times_step.setValue("1");
        eventForm.until_yyyymmdd.setValue(edate);
        if (allday!=null && allday==true) {
            eventForm.allday.setValue(allday);
            eventForm.fthh.setDisabled(true);
            eventForm.fthh.setValue(this.hourstartworkday);
            eventForm.tthh.setDisabled(true);
            eventForm.tthh.setValue(this.hourendworkday);
        }
        if (sevent!=null) eventForm.event.setValue(sevent);
        if (sdescription!=null) eventForm.description.setValue(sdescription);
        if (sactivity_id!=null) eventForm.activity_id.setValue(sactivity_id);
        if (sactivity_flag!=null) eventForm.activity_flag.setValue(sactivity_flag);
        if (scalendar_id!=null) eventForm.calendar_id.setValue(scalendar_id);
        if (slocation!=null) eventForm.report_id.setValue(slocation);
        if (scustomer_id!=null) eventForm.customer_id.setValue(scustomer_id);
        if (sstatistic_id!=null) eventForm.statistic_id.setValue(sstatistic_id);
        
        eventForm.enddate.setValue(edate); 
        
        //eventForm.calendar_id.setValue(this.groupname);
        
        /*if (this.defaultcalendar=="WebTop"){
             eventForm.calendar_id.setValue(this.groupname);
        }else{
             eventForm.calendar_id.setValue(this.defaultcalendar);
        }*/
        //eventForm.calendar_id.getStore().on("load",function(){
            eventForm.calendar_id.setValue(this.defaultcalendar);
        //},this);
         var color=this.defaultcalendarcolor;
         eventForm.calendar_id.on("render",function(c){
            var el=c.getEl();            
            el.setStyle('background-image','none');
            el.setStyle('background-color',color);
         },this);
         
        eventForm.calendar_id.getStore().load();
        
        eventForm.old_recurr_id="false";

        eventForm.timezone_id.setValue(this.defaulttimezone);
        eventForm.reminder.setValue(this.default_reminder);
        
        var w=WT.app.createWindow({
          title: eventForm.formtitle,
          width: 640,
          height:500,
          layout: 'fit',
          iconCls: 'iconNewEvent',
          items: eventForm
        });
        
        
        eventForm.groupCombo.getStore().on("load", function(store,records,o) {
                eventForm.groupCombo.setValue(this.group, false);
                var r=store.getById(this.group);
                var el=eventForm.calendar_id.getEl();
                eventForm.private_value.setValue(r.get('privatevalue'));
                eventForm.busy_value.setValue(r.get('busy'));
                this.resetDirty(eventForm.busy_value);
                this.resetDirty(eventForm.private_value);
                el.setStyle('background-image','none');
                el.setStyle('background-color',r.get('colorcalendar'));
                
         }, this);
        
        eventForm.groupCombo.on("select", function(c,rec,i) {
                if (rec.get('acl')!='w'){
                    eventForm.saveEvent.setDisabled(true);
                    eventForm.deleteEvent.setDisabled(true);
                }
                var el=eventForm.calendar_id.getEl();            
                el.setStyle('background-image','none');
                el.setStyle('background-color',rec.get('colorcalendar'));
                eventForm.private_value.setValue(rec.get('privatevalue'));
                eventForm.busy_value.setValue(rec.get('busy'));
                eventForm.calendar_id.reset();
                eventForm.calendar_id.getStore().baseParams.user=c.getValue();
                eventForm.calendar_id.getStore().load();
                eventForm.calendar_id.setValue(this.defaultcalendar);
                eventForm.causal.getStore().baseParams.login_id=c.getValue();
                eventForm.causal.getStore().load();
         }, this);
        eventForm.allday.on("check",function(c,checked){
                eventForm.fthh.setDisabled(checked);
                eventForm.fthh.setValue(this.hourstartworkday);
                eventForm.tthh.setDisabled(checked);
                eventForm.tthh.setValue(this.hourendworkday);
        
        },this);
        
        eventForm.calendar_id.getStore().on('beforeload',function(s){
            s.baseParams.user=eventForm.groupCombo.getValue();
            
        },this);
        

        eventForm.calendar_id.on("select",function(s,r){
                var el = s.getEl();
                //el.setStyle('color', r.get('color'));
                el.setStyle('background-image','none');
                el.setStyle('background-color',r.get('color'));
                eventForm.private_value.setValue(r.get('privatevalue'));
                eventForm.busy_value.setValue(r.get('busy'));
                eventForm.reminder.setValue(r.get('default_reminder'));
                eventForm.planningGrid.getStore().baseParams.calendar_id=r.get("id");
                eventForm.calendar_id.default_send_invite=r.get("default_send_invite");
                eventForm.planningGrid.getStore().load();
         },this);
         
         
         
         /*
        eventForm.none_recurrence.on('check',function(c,check){
            if (check==false){
                eventForm.eventtab.hideTabStripItem(eventForm.planningGrid);
                eventForm.doLayout();
            }else{
                eventForm.eventtab.unhideTabStripItem(eventForm.planningGrid);
                eventForm.doLayout();
            }
        },this);
        */
       
       eventForm.planningGrid.getStore().on('update',function(s,record){
            var records = s.getRange();
            var existrecord=false;
            if (record.get('name')==""){
                s.remove(record);
            }
            /*
            for (var i = 0; i < records.length; i++) {
                var n=records[i].get('name');
                if (n!=""){
                    existrecord=true;
                    break;
                }
            }
            if (existrecord){
                eventForm.eventtab.hideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }else{
                eventForm.eventtab.unhideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }
            */
        },this);
        
        eventForm.planningGrid.getStore().on('remove',function(s,record){
            var records = s.getRange();
            var existrecord=false;
            if (record.get('name')==""){
                s.remove(record);
            }
            /*
            for (var i = 0; i < records.length; i++) {
                var n=records[i].get('name');
                if (n!=""){
                    existrecord=true;
                    break;
                }
            }
            if (existrecord){
                eventForm.eventtab.hideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }else{
                eventForm.eventtab.unhideTabStripItem(eventForm.recurrence);
                eventForm.doLayout();
            }
            */
        },this);
        eventForm.yyyymmdd.on("select",function(t,n,o){
            if (n>eventForm.enddate.getValue()){
                eventForm.enddate.setValue(n);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.enddate.on("select",function(t,n,o){
            if (n<eventForm.yyyymmdd.getValue()){
                eventForm.enddate.setValue(eventForm.yyyymmdd.getValue());
            }
            eventForm.doLayout();
        },this);
        
        eventForm.fthh.on("select",function(t,rec,index){
            if (eventForm.fthh.getValue()>=eventForm.tthh.getValue()){
               //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.fthh.getValue().substring(0,2);
                var min=eventForm.fthh.getValue().substring(3,5);
                if (parseInt(hour)=="0"){
                    hour=eventForm.fthh.getValue().substring(1,2);
                    hour=parseInt(hour)+1;
                }else{
                    hour=parseInt(hour)+1;
                }
                if (hour>23)
                    hour="00";
                eventForm.tthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.fthh.on("change",function(t,rec,index){
            if (eventForm.fthh.getValue()>=eventForm.tthh.getValue()){
               //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.fthh.getValue().substring(0,2);
                var min=eventForm.fthh.getValue().substring(3,5);
                if (parseInt(hour)=="0"){
                    hour=eventForm.fthh.getValue().substring(1,2);
                    hour=parseInt(hour)+1;
                }else{
                    hour=parseInt(hour)+1;
                }
                if (hour>23)
                    hour="00";
                eventForm.tthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        //eventForm.activity_id.getStore().on("beforeload",function(){
            eventForm.activity_id.getStore().baseParams.login=this.group;
        //},this);
        
        
        eventForm.tthh.on("select",function(t,n,o){
            if (eventForm.tthh.getValue()<=eventForm.fthh.getValue()){
                //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.tthh.getValue().substring(0,2);
                var min=eventForm.tthh.getValue().substring(3,5);
                if (hour=="00")
                    hour="23"
                else{ 
                    if (parseInt(hour)=="0"){
                        hour=eventForm.tthh.getValue().substring(1,2);
                        hour=parseInt(hour)-1;
                    }else{
                        hour=parseInt(hour)-1;
                    }
                }
                if (hour>23)
                    hour="00";
                eventForm.fthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.setWindow(w,this.cs);
        w.show();
    },
    
    /*eventReader: new Ext.data.JsonReader({
        root: 'eventReader',
        totalProperty: 'totalRecords',
        fields: [ 
            'event_id',
            'by',
            'event',
            'report_id',
            'yyyy',
            'mm',
            'dd',
            'hstart',
            'mstart',
            'hend',
            'mend',
            'description',
            'private_value',
            'recurr_id',
            'reminder',
            'share_with',
            'activity_id',
            'activity_flag',
            'calendar',
            'customer_id',
            'customer_description',
            'statistic_id',
            'isRecurrence',
            'isReminder',
            'isReadonly',
            'isPlanning',
            'allday',
            'endday',
            'endmonth',
            'endyear',
            'startday',
            'startmonth',
            'startyear',
            'colorcalendar',
            'uid',
            'recurr_type',
            'dayly_freq',
            'weekly_freq',
            'monthly_day',
            'monthly_month',
            'yearly_day',
            'yearly_month',
            'until_date_dd',
            'until_date_mm',
            'until_date_yyyy',
            'weekly_day1',
            'weekly_day2',
            'weekly_day3',
            'weekly_day4',
            'weekly_day5',
            'weekly_day6',
            'weekly_day7',
            'permanent',
            'repeat',
            'busy',
            'timezone',
            'fromRecurrence',
            'causal_id'
        ]
    }),*/
    
    
    
     editDrop:function(by,id){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        WT.JsonAjaxRequest({
            url: "ServiceRequest",
            params: {
                service:'calendar',
                action:'EditEvent',
                event_id:id
            },
            method: "POST",
            callback: function(o,options) {
                if (o){
                    this.editEventDrop(o,by);
  
                }else{
                    this.waitMessage.hide();
                    WT.alert(WT.res("error"),WT.res("error"));
                }
            },
            scope: this
        });
    },
    
    editEventDrop: function(o,by){
        this.waitMessage.hide();
        var item=o.eventReader[0];
        
        
        this.deselectAllSchedulerEvents();
        var eventForm = this.formContainer.getEventForm();
        if (this.groupacl!='w') {
            eventForm.saveEvent.disable();
            eventForm.deleteEvent.disable();
        }
        eventForm.planningGrid.getStore().baseParams.event_id=item.event_id;
        eventForm.planningGrid.getStore().load();
        //eventForm.event_id = item.event_id;
        eventForm.event_by = by;
        eventForm.event.setValue(item.event);
        eventForm.report_id.setValue(item.report_id);
        eventForm.yyyymmdd.setValue(new Date(item.yyyy,item.mm-1,item.dd));
        eventForm.fthh.setValue(item.hstart+":"+item.mstart);
        eventForm.tthh.setValue(item.hend+":"+item.mend);
        eventForm.description.setValue(item.description);
        if (item.private_value!=null && item.private_value=="Y")
          eventForm.private_value.setValue(true);
        else
          eventForm.private_value.setValue(false);
        if (item.reminder!=null && item.reminder!="")
          eventForm.reminder.setValue(item.reminder);
        eventForm.share_with.setValue(item.share_with);
        eventForm.activity_id.setValue(item.activity_id);
        eventForm.activity_flag.setValue(item.activity_flag);
        /*if (item.calendar=="WebTop"){
            eventForm.calendar_id.setValue(this.groupname);
        }else{
            eventForm.calendar_id.setValue(item.calendar);
        }*/
        //eventForm.calendar_id.getStore().on("load",function(){
            eventForm.calendar_id.setValue(this.defaultcalendar);
        //},this);
        
        eventForm.calendar_id.getStore().baseParams.user=this.groupname;
        eventForm.calendar_id.getStore().load();
        eventForm.causal.getStore().on("load",function(){
            eventForm.causal.setValue(item.get("causal_id"));
        },this);
        eventForm.causal.getStore().on("beforeload",function(){
            eventForm.causal.getStore().baseParams.login_id=by;
            eventForm.causal.getStore().baseParams.customer_id=item.customer_id;
            eventForm.causal.getStore().baseParams.all="true";
        },this);
        eventForm.causal.getStore().load();
        
        
        WT.debug("isDrm="+this.isdrm);
        /*if (this.isdrm=="false") {
            eventForm.causal.setVisible(false);
            eventForm.field_causal.setVisible(false);
        }*/
        eventForm.busy_value.setValue(item.busy);
        eventForm.timezone_id.setValue(item.timezone);
        
        //var el=eventForm.calendar_id.getEl();
        //el.setStyle('background-image','none');
        //el.setStyle('background-color',item.get('colorcalendar'));
        if (item.customer_id!=null && item.customer_id!="")
          eventForm.customer_id.setValue(item.customer_id);
        if (item.statistic_id!=null && item.statistic_id!="null" && item.statistic_id!="")
          eventForm.statistic_id.setValue(item.statistic_id);
        eventForm.recurr_id=item.recurr_id;
        eventForm.allday.setValue(item.allday);
        if (item.allday=="true"){
           eventForm.fthh.setDisabled(true);
           eventForm.tthh.setDisabled(true);
        }
        var end=item.endday+"/"+item.endmonth+"/"+item.endyear;
        eventForm.enddate.setValue(end);
        var w=WT.app.createWindow({
          title: item.event,
          width: 640,
          height:500,
          layout: 'fit',
          iconCls: 'iconNewEvent',
          items: eventForm
        });
        
        
        eventForm.groupCombo.getStore().on("load", function(store) {
                eventForm.groupCombo.setValue(by, false);
                eventForm.calendar_id.getStore().baseParams.user=by;
                eventForm.calendar_id.getStore().load();
                eventForm.calendar_id.setValue(this.defaultcalendar);
         }, this);
        
        eventForm.groupCombo.on("select", function(c,rec,i) {
                if (rec.get('acl')!='w'){
                    eventForm.saveEvent.setDisabled(true);
                    eventForm.deleteEvent.setDisabled(true);
                }
                var el=eventForm.calendar_id.getEl();            
                el.setStyle('background-image','none');
                el.setStyle('background-color','white');
                eventForm.private_value.setValue(rec.get('privatevalue'));
                eventForm.busy_value.setValue(rec.get('busy'));
                eventForm.calendar_id.reset();
                eventForm.calendar_id.getStore().baseParams.user=c.getValue();
                eventForm.calendar_id.getStore().load();
                eventForm.calendar_id.setValue(this.defaultcalendar);
                eventForm.causal.getStore().baseParams.login_id=c.getValue();
                eventForm.causal.getStore().load();
                
         }, this);
        eventForm.allday.on("check",function(c,checked){
                eventForm.fthh.setDisabled(checked);
                eventForm.fthh.setValue(this.hourstartworkday);
                eventForm.tthh.setDisabled(checked);
                eventForm.tthh.setValue(this.hourendworkday);
        
        },this);
        /*
        eventForm.enddate.on("select",function(c,n,o){
            eventForm.allday.setValue(true);
        },this);
        */
        eventForm.calendar_id.on("select",function(s,r){
                var el = s.getEl();
                //el.setStyle('color', r.get('color'));
                el.setStyle('background-image','none');
                el.setStyle('background-color',r.get('color'));
                eventForm.private_value.setValue(r.get('privatevalue'));
                eventForm.busy_value.setValue(r.get('busy'));
                eventForm.reminder.setValue(r.get('default_reminder'));
                eventForm.planningGrid.getStore().baseParams.calendar_id=r.get("id");
                eventForm.calendar_id.default_send_invite=r.get("default_send_invite");
                eventForm.planningGrid.getStore().load();
         },this);
         
        var color=this.defaultcalendarcolor;
        eventForm.calendar_id.on("render",function(c){
            var el=c.getEl();            
            el.setStyle('background-image','none');
            el.setStyle('background-color',color);
        },this);
        
        eventForm.planningGrid.getStore().on('update',function(s,record){
            if (record.get('name')==""){
                s.remove(record);
            }
        },this);
        
        eventForm.yyyymmdd.on("select",function(t,n,o){
            if (n>eventForm.enddate.getValue()){
                eventForm.enddate.setValue(n);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.enddate.on("select",function(t,n,o){
            if (n<eventForm.yyyymmdd.getValue()){
                eventForm.enddate.setValue(eventForm.yyyymmdd.getValue());
            }
            eventForm.doLayout();
        },this);
        
        eventForm.fthh.on("select",function(t,rec,index){
            if (eventForm.fthh.getValue()>=eventForm.tthh.getValue()){
               //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.fthh.getValue().substring(0,2);
                var min=eventForm.fthh.getValue().substring(3,5);
                if (parseInt(hour)=="0"){
                    hour=eventForm.fthh.getValue().substring(1,2);
                    hour=parseInt(hour)+1;
                }else{
                    hour=parseInt(hour)+1;
                }
                if (hour>23)
                    hour="00";
                eventForm.tthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        //eventForm.activity_id.getStore().on("beforeload",function(){
            eventForm.activity_id.getStore().baseParams.login=this.group;
        //},this);
        
        
        eventForm.tthh.on("select",function(t,n,o){
            if (eventForm.tthh.getValue()<=eventForm.fthh.getValue()){
                //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.tthh.getValue().substring(0,2);
                var min=eventForm.tthh.getValue().substring(3,5);
                if (hour=="00")
                    hour="23"
                else{ 
                    if (parseInt(hour)=="0"){
                        hour=eventForm.tthh.getValue().substring(1,2);
                        hour=parseInt(hour)-1;
                    }else{
                        hour=parseInt(hour)-1;
                    }
                }
                if (hour>23)
                    hour="00";
                eventForm.fthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        
        eventForm.setWindow(w,this.cs);
        w.show();
      
    },
    
    
    editEvent: function(event_id){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var params={
            service:'calendar',action:'EditEvent',event_id:event_id
        };
        this.proxyEdit.doRequest(
            'read',null,
            params,
            this.eventReader,
            this.editEventResult,
            this,
            params
         );
            
            
    },
        
    editEventResult: function(o, params, success){
      this.waitMessage.hide();
      if (o && success) {
        var r=o.records;
        var item=r[0];
        
        
        
        this.deselectAllSchedulerEvents();
        var eventForm = this.formContainer.getEventForm();
        if (this.groupacl!='w') {
            eventForm.saveEvent.disable();
            eventForm.deleteEvent.disable();
        }
        
        eventForm.isPlanning=item.get('isPlanning');
        if (item.get('uid')!=""){   //evento generato da un invito
            WT.alert(WT.res('calendar','dialogClosingTitle'),WT.res('calendar','msgisplanningevent'));
            eventForm.availablePlanning.setDisabled(true);
        }
        
        eventForm.busy_value.setValue(item.get("busy"));
        
        eventForm.mod_event=item.get("event");
        eventForm.mod_report_id=item.get("report_id");
        eventForm.mod_fromdate=new Date(item.get("yyyy"),item.get("mm")-1,item.get("dd"));
        eventForm.mod_todate=new Date(item.get("endyear"),item.get("endmonth")-1,item.get("endday"));
        eventForm.mod_description=item.get("description");
        eventForm.mod_fthh=item.get("hstart")+":"+item.get("mstart");
        eventForm.mod_tthh=item.get("hend")+":"+item.get("mend");
        
        eventForm.yyyymmdd.setValue(new Date(item.get("yyyy"),item.get("mm")-1,item.get("dd")));
        var end=item.get("endday")+"/"+item.get("endmonth")+"/"+item.get("endyear");
        eventForm.enddate.setValue(end);
        
        eventForm.event_id = item.get("event_id");
        eventForm.event_by = item.get("by");
        eventForm.causal.getStore().on("load",function(){
            eventForm.causal.setValue(item.get("causal_id"));
        },this);
        eventForm.causal.getStore().on("beforeload",function(){
            eventForm.causal.getStore().baseParams.login_id=item.get("by");
            eventForm.causal.getStore().baseParams.customer_id=item.get("customer_id");
            eventForm.causal.getStore().baseParams.all="true";
        },this);
        eventForm.causal.getStore().load();
        
        WT.debug("isDrm="+this.isdrm);
        /*if (this.isdrm=="false") {
            eventForm.causal.setVisible(false);
            eventForm.field_causal.setVisible(false);
        }*/
        eventForm.event.setValue(item.get("event"));
        eventForm.report_id.setValue(item.get("report_id"));
        //eventForm.enddate.setVisible(false);
        if (item.get("recurr_id")!=""){     //con ricorsione
            eventForm.yyyymmdd.setDisabled(true);
            eventForm.enddate.setDisabled(true);
            eventForm.groupCombo.setDisabled(true);
            
            //gestione date se evento ricorsivo
            eventForm.yyyymmdd.setValue(this.event_startdate_for_recurrences);
            eventForm.enddate.setValue(this.event_enddate_for_recurrences);
            
        }
        eventForm.planningGrid.getStore().baseParams.calendar_id=item.get("calendar");
        eventForm.planningGrid.getStore().load();
        if (item.get("isPlanning")=="Y"){   //con pianificazione
            eventForm.customer_id.getStore().reload();
            eventForm.planningGrid.getStore().baseParams.event_id=item.get('event_id');
            eventForm.planningGrid.getStore().on('load',function(s){
                if (s.getCount()>1){
                   eventForm.groupCombo.setDisabled(true); 
                }
            },
            this);
            eventForm.planningGrid.getStore().baseParams.calendar_id=item.get("calendar");
            eventForm.planningGrid.getStore().load();
            
            eventForm.yyyymmdd.setValue(new Date(item.get("yyyy"),item.get("mm")-1,item.get("dd")));
            var end=item.get("endday")+"/"+item.get("endmonth")+"/"+item.get("endyear");
            eventForm.enddate.setValue(end);
            if (item.get("recurr_id")!=""){
                //gestione date se evento ricorsivo
                eventForm.yyyymmdd.setValue(this.event_startdate_for_recurrences);
                eventForm.enddate.setValue(this.event_enddate_for_recurrences);
            }
        }
        
        
        
        eventForm.fthh.setValue(item.get("hstart")+":"+item.get("mstart"));
        eventForm.tthh.setValue(item.get("hend")+":"+item.get("mend"));
        eventForm.description.setValue(item.get("description"));
        if (item.get("private_value")!=null && item.get("private_value")=="Y")
          eventForm.private_value.setValue(true);
        else
          eventForm.private_value.setValue(false);
      
        if (item.get("reminder")!=null && item.get("reminder")!="")
          eventForm.reminder.setValue(item.get("reminder"));
      
        eventForm.share_with.setValue(item.get("share_with"));
        var first=true;
        eventForm.activity_id.getStore().on("load",function(){
            if(first) eventForm.activity_id.setValue(item.get("activity_id"));
            first=false;
        },this);
        eventForm.activity_id.getStore().load();
        eventForm.activity_flag.setValue(item.get("activity_flag"));
        eventForm.uid=item.get('uid');
        
        
        if (item.get("customer_id")!=null && item.get("customer_id")!=""){
          eventForm.customer_id.setValue(item.get("customer_id"));
        }
        if (item.get("statistic_id")!=null && item.get("statistic_id")!="null" && item.get("statistic_id")!="")
          eventForm.statistic_id.setValue(item.get("statistic_id"));
        eventForm.recurr_id=item.get("recurr_id");
        eventForm.allday.setValue(item.get("allday"));
        
        if (item.get("allday")=="true"){
           eventForm.fthh.setDisabled(true);
           eventForm.tthh.setDisabled(true);
        }
        
        /*if (item.get("calendar")=="WebTop"){
             eventForm.calendar_id.setValue(this.groupname);
        }else{
             eventForm.calendar_id.setValue(item.get("calendar"));
        }*/
        //eventForm.calendar_id.getStore().on("load",function(){
            eventForm.calendar_id.setValue(item.get("calendar"));
        //},this);
        eventForm.calendar_id.getStore().load();
        if (item.get("fromRecurrence")=="true") eventForm.restoreRecurrence.setDisabled(false);
        eventForm.timezone_id.setValue(item.get("timezone"));
        
        //set ricorrenze
        eventForm.dayly_step.setValue("1");
        eventForm.weekly_step.setValue("1");
        eventForm.monthly_day.setValue("1");
        eventForm.monthly_month.setValue("1");
        eventForm.yearly_day.setValue("1");
        eventForm.yearly_month.setValue("1");
        eventForm.until_yyyymmdd.setValue(new Date(item.get("endyear"),item.get("endmonth")-1,item.get("endday")));
        eventForm.repeat_times_step.setValue("1");
        
        var recurr_type=item.get("recurr_type");

        if (recurr_type=='D'){
           eventForm.none_recurrence.setValue(false);
           if (item.get("dayly_freq")!="null" && item.get("dayly_freq")!="" ){
               eventForm.dayly_recurrence.setValue(true);
               eventForm.dayly1.setValue(true);
               eventForm.dayly_step.setValue(item.get("dayly_freq"));
               eventForm.dayly1.setDisabled(false);
               eventForm.dayly_step.setDisabled(false);
               eventForm.dayly2.setDisabled(false);
           }
           eventForm.until_yyyymmdd.setValue(new Date(item.get("until_date_yyyy"),item.get("until_date_mm")-1,item.get("until_date_dd")));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
           
        }else if (recurr_type=='F'){
           eventForm.none_recurrence.setValue(false);
           eventForm.dayly_recurrence.setValue(true);
           eventForm.dayly1.setDisabled(false);
           eventForm.dayly_step.setDisabled(false);
           eventForm.dayly2.setDisabled(false);
           eventForm.dayly2.setValue(true);
           eventForm.until_yyyymmdd.setValue(new Date(item.get("until_date_yyyy"),item.get("until_date_mm")-1,item.get("until_date_dd")));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (recurr_type=='W'){
           eventForm.none_recurrence.setValue(false);
           eventForm.weekly_recurrence.setValue(true);
           eventForm.weekly_step.setValue(item.get("weekly_freq"));
           eventForm.weekly_step.setDisabled(false);
           if (item.get("weekly_day1")=="true") eventForm.weekly1.setValue(true);
           if (item.get("weekly_day2")=="true") eventForm.weekly2.setValue(true);
           if (item.get("weekly_day3")=="true") eventForm.weekly3.setValue(true);
           if (item.get("weekly_day4")=="true") eventForm.weekly4.setValue(true);
           if (item.get("weekly_day5")=="true") eventForm.weekly5.setValue(true);
           if (item.get("weekly_day6")=="true") eventForm.weekly6.setValue(true);
           if (item.get("weekly_day7")=="true") eventForm.weekly7.setValue(true);
           eventForm.weekly1.setDisabled(false);
           eventForm.weekly2.setDisabled(false);
           eventForm.weekly3.setDisabled(false);
           eventForm.weekly4.setDisabled(false);
           eventForm.weekly5.setDisabled(false);
           eventForm.weekly6.setDisabled(false);
           eventForm.weekly7.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(item.get("until_date_yyyy"),item.get("until_date_mm")-1,item.get("until_date_dd")));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (recurr_type=='M'){
           eventForm.none_recurrence.setValue(false);
           eventForm.monthly_recurrence.setValue(true);
           eventForm.monthly_month.setValue(item.get("monthly_month"));
           eventForm.monthly_day.setValue(item.get("monthly_day"));
           eventForm.monthly_month.setDisabled(false);
           eventForm.monthly_day.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(item.get("until_date_yyyy"),item.get("until_date_mm")-1,item.get("until_date_dd")));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }else if (recurr_type=='Y'){
           eventForm.none_recurrence.setValue(false);
           eventForm.yearly_recurrence.setValue(true);
           eventForm.yearly_day.setValue(item.get("yearly_day"));
           eventForm.yearly_month.setValue(item.get("yearly_month"));
           eventForm.yearly_month.setDisabled(false);
           eventForm.yearly_day.setDisabled(false);
           eventForm.until_yyyymmdd.setValue(new Date(item.get("until_date_yyyy"),item.get("until_date_mm")-1,item.get("until_date_dd")));
           eventForm.until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setDisabled(false);
           eventForm.radio_until_yyyymmdd.setValue(true);
           eventForm.permanent_recurrence.setValue(false);
           eventForm.permanent_recurrence.setDisabled(false);
           eventForm.repeat_times.setDisabled(false);
           eventForm.repeat_times_step.setDisabled(true);
           
        }
        
        if (item.get("permanent")=="true"){
               eventForm.permanent_recurrence.setValue(true);
               eventForm.radio_until_yyyymmdd.setValue(false);
               eventForm.until_yyyymmdd.setDisabled(false);
               eventForm.until_yyyymmdd.setValue(new Date(item.get("yyyy"),item.get("mm")-1,item.get("dd")));
            }
        if (item.get("repeat")!=null && item.get("repeat")!="0"){
                eventForm.repeat_times.setDisabled(false);
                eventForm.repeat_times.setValue(true);
                eventForm.repeat_times_step.setDisabled(false);
                eventForm.radio_until_yyyymmdd.setValue(false);
                eventForm.until_yyyymmdd.setDisabled(true);
                eventForm.repeat_times_step.setValue(item.get("repeat"));
            }
        //
        
        
        
        var w=WT.app.createWindow({
          title: item.get("event"),
          width: 640,
          height:500,
          layout: 'fit',
          iconCls: 'iconNewEvent',
          items: eventForm
        });
        
        
        if (eventForm.uid!=""){     //se � un evento generato da un invito via mail disabilito editing grid
                eventForm.planningGrid.on('beforeedit',function(e){
                    e.cancel=true;
                    eventForm.planningGrid.getTopToolbar().setDisabled(true);
                },this);

                eventForm.planningGrid.getSelectionModel().on('contextmenu',function(e){
                    eventForm.planningGrid.getTopToolbar().setDisabled(true);
                },this);

                eventForm.planningGrid.getSelectionModel().on('beforerowselect',function( sm, row, keepExisting,record ){
                    eventForm.menuPlanning.setDisabled(true);
                    return false;

                },this);
                eventForm.planningGrid.getColumnModel().setRenderer(0,function(v, p, record){
                    if (v=='true') 
                        return "<input checked=checked name='check[]' type='checkbox' value="+v+" disabled>";
                    else
                        return "<input name='check[]' type='checkbox' value="+v+" disabled>";
                 });
            
        }
        
        
        eventForm.groupCombo.getStore().on("load", function(store) {
                eventForm.groupCombo.setValue(this.group, false);
                var r=store.getById(this.group);
                var el=eventForm.calendar_id.getEl();            
                el.setStyle('background-image','none');
                el.setStyle('background-color',item.get('colorcalendar'));


         }, this);
        
        
        eventForm.groupCombo.on("select", function(c,rec,i) {
                if (rec.get('acl')!='w'){
                    eventForm.saveEvent.setDisabled(true);
                    eventForm.deleteEvent.setDisabled(true);
                }
                var el=eventForm.calendar_id.getEl();            
                el.setStyle('background-image','none');
                el.setStyle('background-color',rec.get('colorcalendar'));
                eventForm.private_value.setValue(rec.get('privatevalue'));
                eventForm.busy_value.setValue(rec.get('busy'));
                eventForm.calendar_id.reset();
                eventForm.calendar_id.getStore().baseParams.user=c.getValue();
                eventForm.calendar_id.getStore().load();
                eventForm.calendar_id.setValue(this.defaultcalendar);
                eventForm.causal.getStore().on("beforeload",function(){
                    eventForm.causal.getStore().baseParams.login_id=c.getValue();
                    eventForm.causal.getStore().baseParams.customer_id=eventForm.customer_id.getValue();
                },this);
                eventForm.causal.getStore().load();
                
                WT.debug("isDrm="+this.isdrm);
                /*if (this.isdrm=="false") {
                    eventForm.causal.setVisible(false);
                    eventForm.field_causal.setVisible(false);
                }*/
                
         }, this);
        eventForm.allday.on("check",function(c,checked){
                eventForm.fthh.setDisabled(checked); 
                eventForm.fthh.setValue(this.hourstartworkday);
                eventForm.tthh.setDisabled(checked);
                eventForm.tthh.setValue(this.hourendworkday);
        
        },this);
        /*
        eventForm.enddate.on("select",function(c,n,o){
            eventForm.allday.setValue(true);
        },this);
        */
        eventForm.calendar_id.getStore().on('beforeload',function(s){
            s.baseParams.user=eventForm.groupCombo.getValue();
        },this);

        eventForm.calendar_id.on("select",function(s,r){
                var el = s.getEl();
                //el.setStyle('color', r.get('color'));
                el.setStyle('background-image','none');
                el.setStyle('background-color',r.get('color'));
                eventForm.private_value.setValue(r.get('privatevalue'));
                eventForm.busy_value.setValue(r.get('busy'));
                eventForm.reminder.setValue(r.get('default_reminder'));
                eventForm.planningGrid.getStore().baseParams.calendar_id=r.get("id");
                eventForm.calendar_id.default_send_invite=r.get("default_send_invite");
                eventForm.planningGrid.getStore().load();
         },this);
        var color=item.get('colorcalendar');
        eventForm.calendar_id.on("render",function(c){
            var el=c.getEl();            
            el.setStyle('background-image','none');
            el.setStyle('background-color',color);
        },this);
        
        eventForm.calendar_id.getStore().load();
        
        eventForm.planningGrid.getStore().on('update',function(s,record){
            if (record.get('name')==""){
                s.remove(record);
            }
            
            
        },this);
        
        eventForm.yyyymmdd.on("select",function(t,n,o){
            if (n>eventForm.enddate.getValue()){
                eventForm.enddate.setValue(n);
            }
            eventForm.doLayout();
        },this);
        
        eventForm.enddate.on("select",function(t,n,o){
            if (n<eventForm.yyyymmdd.getValue()){
                eventForm.enddate.setValue(eventForm.yyyymmdd.getValue());
            }
            eventForm.doLayout();
        },this);
        
        eventForm.fthh.on("select",function(t,rec,index){
            if (eventForm.fthh.getValue()>=eventForm.tthh.getValue()){
               //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.fthh.getValue().substring(0,2);
                var min=eventForm.fthh.getValue().substring(3,5);
                if (parseInt(hour)=="0"){
                    hour=eventForm.fthh.getValue().substring(1,2);
                    hour=parseInt(hour)+1;
                }else{
                    hour=parseInt(hour)+1;
                }
                if (hour>23)
                    hour="00";
                eventForm.tthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        
        //eventForm.activity_id.getStore().on("beforeload",function(){
            eventForm.activity_id.getStore().baseParams.login=this.group;
        //},this);
        
        eventForm.tthh.on("select",function(t,n,o){
            if (eventForm.tthh.getValue()<=eventForm.fthh.getValue()){
                //eventForm.tthh.setValue(eventForm.fthh.getValue());
                var hour=eventForm.tthh.getValue().substring(0,2);
                var min=eventForm.tthh.getValue().substring(3,5);
                if (hour=="00")
                    hour="23"
                else{ 
                    if (parseInt(hour)=="0"){
                        hour=eventForm.tthh.getValue().substring(1,2);
                        hour=parseInt(hour)-1;
                    }else{
                        hour=parseInt(hour)-1;
                    }
                }
                if (hour>23)
                    hour="00";
                eventForm.fthh.setValue(hour+":"+min);
            }
            eventForm.doLayout();
        },this);
        
        
        eventForm.setWindow(w,this.cs);
        w.show();
      }
    },
    
    changeEvent : function(event_id, event){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var params={service:'calendar',action:'PostEvent',event_id:event_id,event:event};
        WT.JsonAjaxRequest({
            url: 'ServiceRequest',
            params: params,
            method: "POST",
            callback: this.writeEventResult,
            scope: this
        });
    },
    
    writeEventResult: function(o, params, success) {
        this.waitMessage.hide();
		this.load();
    },
    
    moveEvent : function(isCopy,event_id,ldate,hstart,mstart,hend,mend){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var yyyy = ldate.getFullYear();
        var mm = ""+(ldate.getMonth()+1);
        if ((ldate.getMonth()+1)<10) mm="0"+mm;
        var dd = ""+ldate.getDate();
        if (ldate.getDate()<10) dd="0"+dd;
        var params={
            service:'calendar',action:'MoveEvent',
            event_id:event_id,
            yyyy:yyyy,
            mm:mm,
            dd:dd,
            hstart:hstart,
            mstart:mstart,
            hend:hend,
            mend:mend,
            isCopy:isCopy
            
        };
        WT.JsonAjaxRequest({
            url: 'ServiceRequest',
            params: params,
            method: "POST",
            callback: this.saveEventResult,
            scope: this
        });
    },
    
    
    
    
    
    
    deleteEvents: function(){
      var event_ids=[];
      for (var i=1;i<=31;i++) {
        event_ids = event_ids.concat(this.sd[i].getSelectedSchedulerObjectEvents());
      }
      if (event_ids[0].recurrence==true){
          WT.show({
                width:500,
                title:this.cs.res("dialogDeletingTitle"),
                msg: this.cs.res("dialogRecurrMessage") +"<br/>"+"<br/>"+
                      "<table width='70%' style='font-size: 12px'>"+
                      "<tr><td width='60%'>"+WT.res("calendar","event_single")+"</td><td><input type='radio' name='option' id='single' checked=true /></td></tr>"+
                      "<tr><td width='60%'>"+WT.res("calendar","event_future")+"</td><td><input type='radio' name='option' id='future' / ></td></tr>"+
                      "<tr><td width='60%'>"+WT.res("calendar","event_all")+"</td><td><input type='radio' name='option' id='all' /></td></tr>"+
                      "</table>",
                buttons: Ext.Msg.OKCANCEL,
                scope: this,
                fn: function(btn, text){
                  if (btn == 'ok'){
                    if (Ext.get('all')!=null && Ext.get('all').dom.checked == true){
                      this.deleteEvent(event_ids[0].event_id,event_ids[0].recurr_id);
                      this.nrOfSelectedEvent=0;
                      this.cs.bactiondelete.setDisabled(true);
                    }
                    if (Ext.get('single')!=null && Ext.get('single').dom.checked == true){
                      
                      var yyyy = event_ids[0].yyyy_event;; //se si spezza la senza salvo date evento
                      var mm = event_ids[0].mm_event;
                      var dd = event_ids[0].dd_event;
                      WT.debug(event_ids[0].event_id+" "+event_ids[0].recurr_id+" "+dd+" "+mm+" "+yyyy);
                      this.deleteEvent(event_ids[0].event_id,null,event_ids[0].recurr_id,dd,mm,yyyy,"single");
                      this.nrOfSelectedEvent=0;
                      this.cs.bactiondelete.setDisabled(true);
                    }
                    if (Ext.get('future')!=null && Ext.get('future').dom.checked == true){
                      var yyyy = event_ids[0].yyyy_event; //se si spezza la senza salvo date evento
                      var mm = event_ids[0].mm_event;
                      var dd = event_ids[0].dd_event;
                      this.deleteEvent(event_ids[0].event_id,null,event_ids[0].recurr_id,dd,mm,yyyy,"future");
                      this.nrOfSelectedEvent=0;
                      this.cs.bactiondelete.setDisabled(true);
                    }
                    }else{
                        this.deselectAllSchedulerEvents();
                    }
                }
              });
      }else{
          WT.show({
            title:this.cs.res("dialogDeletingTitle"),
            msg: this.cs.res("dialogDeletingMessage"),
            buttons: Ext.Msg.YESNOCANCEL,
            scope: this,
            fn: function(btn, text){
              if (btn == 'yes'){
                Ext.Msg.hide();
                this.deleteEvent(event_ids[0].event_id);
                this.nrOfSelectedEvent=0;
                this.cs.bactiondelete.setDisabled(true);
              } else if (btn == 'no') {
                this.deselectAllSchedulerEvents();
              }
            }
          });
      }
      
    },
    
    deleteEvent: function(event_ids,recurr_id,old_recurr_id,dd,mm,yyyy,type){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var params={
            service:'calendar',action:'DeleteEvent',
            event_ids:event_ids,
            recurr_id:recurr_id,
            old_recurr_id:old_recurr_id,
            dd:dd,
            mm:mm,
            yyyy:yyyy,
            type:type
        };
        WT.JsonAjaxRequest({
            url: 'ServiceRequest',
            params: params,
            method: "POST",
            callback: this.saveEventResult,
            scope: this
        });
    },
    
    copyInCalendar:function(event_id,idcalendar){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var params={
            service:'calendar',action:'CopyInCalendar',
            event_id:event_id,
            idcalendar:idcalendar
        };
        WT.JsonAjaxRequest({
            url: 'ServiceRequest',
            params: params,
            method: "POST",
            callback: this.saveEventResult,
            scope: this
        });
    },
      
     deleteEventByAttachment: function(event_id){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var params={
            service:'calendar',action:'DeleteEventByAttachment',
            event_id:event_id
        };
        WT.JsonAjaxRequest({
            url: 'ServiceRequest',
            params: params,
            method: "POST",
            callback: this.saveEventResult,
            scope: this
        });
    },
    
    restoreRec:function(event_id){
        WT.show({
                title:this.cs.res("dialogRestoreRecurrence"),
                msg: this.cs.res("dialogRestoreRecurrenceMessage"),
                buttons: Ext.Msg.YESNOCANCEL,
                scope: this,
                fn: function(btn, text){
                  if (btn == 'yes'){
                   Ext.Msg.hide();
                   this.restoreRecurrence(event_id);
                  } else if (btn == 'no') {
                      this.deselectAllSchedulerEvents();
                  }
                }
        });
    },
    

    restoreRecurrence: function(event_id){
      if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
            this.waitMessage.show();
            var params={
                service:'calendar',action:'RestoreRecurrence',
                event_id:event_id
            };
            WT.JsonAjaxRequest({
                url: 'ServiceRequest',
                params: params,
                method: "POST",
                callback: this.saveEventResult,
                scope: this
            });
        
    },
    
    
    printEvent:function(eventForm){
        var event_id = eventForm.event_id;
        var recurr_id = eventForm.recurr_id;
        var event_by = eventForm.event_by;
        var event = eventForm.event.getValue();
        var report_id = eventForm.report_id.getValue();
        var ldate = eventForm.yyyymmdd.getValue();
        var yyyy = ldate.getFullYear();
        var mm = ""+(ldate.getMonth()+1);
        if ((ldate.getMonth()+1)<10) mm="0"+mm;
        var dd = ""+ldate.getDate();
        if (ldate.getDate()<10) dd="0"+dd;
        var fromtime = eventForm.fthh.getValue();
        var totime = eventForm.tthh.getValue();
        var description = eventForm.description.getValue();
        var private_value = "";
        if (eventForm.private_value.getValue()==true)
          private_value = this.cs.res("yes");
        else
          private_value = this.cs.res("no");  
        var reminder = eventForm.reminder.getRawValue();
        
        var share_with = eventForm.share_with.getValue();
        var activity_id = eventForm.activity_id.getRawValue();
        var activity_flag = eventForm.activity_flag.getRawValue();
        var calendar_id = eventForm.calendar_id.getRawValue();
        var customer_id = eventForm.customer_id.getRawValue();
        var statistic_id = eventForm.statistic_id.getRawValue();
        var none_recurrence = eventForm.none_recurrence.getValue();
        var dayly_recurrence = eventForm.dayly_recurrence.getValue();
        var weekly_recurrence = eventForm.weekly_recurrence.getValue();
        var monthly_recurrence = eventForm.monthly_recurrence.getValue();
        var yearly_recurrence = eventForm.yearly_recurrence.getValue();
        var recurrence="";
        if (none_recurrence)
            recurrence=this.cs.res("none_recurrence");  
        if (dayly_recurrence)
            recurrence=this.cs.res("dayly_recurrence");
        if (weekly_recurrence)
            recurrence=this.cs.res("weekly_recurrence");
        if (monthly_recurrence)
            recurrence=this.cs.res("monthly_recurrence");
        if (yearly_recurrence)
            recurrence=this.cs.res("yearly_recurrence");
        
        var dayly1 = eventForm.dayly1.getValue();
        var dayly_step = eventForm.dayly_step.getValue();
        var dayly2 = eventForm.dayly2.getValue();
        var weekly_step = eventForm.weekly_step.getValue();
        var weekly1 = eventForm.weekly1.getValue();
        var weekly2 = eventForm.weekly2.getValue();
        var weekly3 = eventForm.weekly3.getValue();
        var weekly4 = eventForm.weekly4.getValue();
        var weekly5 = eventForm.weekly5.getValue();
        var weekly6 = eventForm.weekly6.getValue();
        var weekly7 = eventForm.weekly7.getValue();
        var monthly_day = eventForm.monthly_day.getValue();
        var monthly_month = eventForm.monthly_month.getValue();
        var yearly_day = eventForm.yearly_day.getValue();
        var yearly_month = eventForm.yearly_month.getValue();
        var until_ldate = eventForm.until_yyyymmdd.getValue();
        var until_yyyy = "";
        var until_mm = "";
        var until_dd = "";
        if (until_ldate!=null && until_ldate!="") {
          until_yyyy = until_ldate.getFullYear();
          until_mm = ""+(until_ldate.getMonth()+1);
          if ((until_ldate.getMonth()+1)<10) until_mm="0"+until_mm;
          until_dd = ""+until_ldate.getDate();
          if (until_ldate.getDate()<10) until_dd="0"+until_dd;
        }
        var htmlSrc="<h3>"+event_by+"</h3>";
        htmlSrc+="<hr width=100% size='4' color='black' align='center'>"
        htmlSrc+="<br>";
        htmlSrc+="<table width='100%' border='0'>"
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("textEvent")+"</b></td>";
        htmlSrc+="<td>"+event+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("textLocation")+"</b></td>";
        htmlSrc+="<td>"+report_id+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr><td>&nbsp</td></tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("textDateEvent")+"</b></td>";
        htmlSrc+="<td>"+dd+"/"+mm+"/"+yyyy+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("textStartHour")+"</b></td>";
        htmlSrc+="<td>"+fromtime+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("textEndHour")+"</b></td>";
        htmlSrc+="<td>"+totime+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr><td>&nbsp</td></tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("recurrence")+":</b></td>";
        htmlSrc+="<td>"+recurrence+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr><td>&nbsp</td></tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td>"+description+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr><td>&nbsp</td></tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("reminder")+"</b></td>";
        htmlSrc+="<td>"+reminder+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("private")+"</b></td>";
        htmlSrc+="<td>"+private_value+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr><td>&nbsp</td></tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("activity")+"</b></td>";
        htmlSrc+="<td>"+activity_id+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td>&nbsp</td>";
        htmlSrc+="<td>"+activity_flag+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("category")+"</b></td>";
        htmlSrc+="<td>"+calendar_id+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("customer")+"</b></td>";
        htmlSrc+="<td>"+customer_id+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="<tr>";
        htmlSrc+="<td width='30%'><b>"+this.cs.res("statistic")+"</b></td>";
        htmlSrc+="<td>"+statistic_id+"</td>";
        htmlSrc+="</tr>";
        htmlSrc+="</table>";
        WT.app.print(htmlSrc);
        
    },
    
    
    getDateHeader:function(ldate){
         var startyyyy = ldate.getFullYear();
         var startmm = ""+(ldate.getMonth()+1);
         if ((ldate.getMonth()+1)<10) startmm="0"+startmm;
         var startdd = ""+ldate.getDate();
         if ((ldate.getDate())<10) startdd="0"+startdd;
         return startdd+"/"+startmm+"/"+startyyyy;
    },
    
    getDescriptionDate:function(ldate){
         var startmm = ""+(ldate.getMonth()+1);
         if ((ldate.getMonth()+1)<10) startmm="0"+startmm;
         var startdd = ""+ldate.getDate();
         var month="headermonthNames"+startmm;
         return startdd+" "+WT.res('calendar',month);
    },
    
    
    getNewStore:function(field){
       /*var store=new Ext.data.Store({                  
                proxy: new Ext.data.HttpProxy(new Ext.data.Connection({
                    url:'ServiceRequest'
                })),
                autoLoad: false,
                baseParams: {service: 'calendar', action: 'GetAvailable'},
                reader: new Ext.data.JsonReader ({                                        
                    root: 'available',
                    fields: field
                })//reader                
        });
                return store;
        */
       return null;
   },
    
    
    roundHour:function(hour,end){
         var date = new Date("01/01/2001 "+hour+":00");
         var endhourworkday = new Date("01/01/2001 "+end+":00");
         var hh = date.getHours();
         var mm = date.getMinutes();
         var endhh = endhourworkday.getHours();
         if (mm>00) hh=hh+1;
         if (hh>endhh) hh=endhh;
         return hh;
    },
    
    
    available:function(eventForm,store){
         var win=this.formContainer.getAvailableWindow();
         var cols=[];
         var start=this.hourstartworkday.substring(0,2);
         var end=this.hourendworkday.substring(0,2);
         var hourstartevent=eventForm.fthh.getValue();
         var hourendevent=this.roundHour(eventForm.tthh.getValue(),this.hourendworkday);
         hourstartevent=hourstartevent.substring(0,2);
         var ldate = eventForm.yyyymmdd.getValue();
         var startyyyy = ldate.getFullYear();
         var startmm = ""+(ldate.getMonth()+1);
         if ((ldate.getMonth()+1)<10) startmm="0"+startmm;
         var startdd = ""+ldate.getDate();
         if (ldate.getDate()<10) startdd="0"+startdd;
         var tdate = eventForm.enddate.getValue();
         var endyyyy = tdate.getFullYear();
         var endmm = ""+(tdate.getMonth()+1);
         if ((tdate.getMonth()+1)<10) endmm="0"+endmm;
         var enddd = ""+tdate.getDate();
         if (tdate.getDate()<10) enddd="0"+enddd;

         var startdayevent=this.getDateHeader(ldate)+"_"+hourstartevent;
         var enddayevent=this.getDateHeader(tdate)+"_"+hourendevent;
         var fieldstore=[];
         var this_column = {};
         this_column['id']="name";
         this_column['header']=WT.res("calendar","nameinvit");
         this_column['dataIndex']="name";
         this_column['width']=150;
         
         cols.push(this_column);
         fieldstore.push("name");
         var j=1;
         while(ldate<=tdate){
             for (var i=start;i<end;i++){
                    var this_column = {};
                    this_column['id']=this.getDateHeader(ldate)+"_"+i+":00";
                    this_column['header']=this.getDescriptionDate(ldate)+" "+i+":00";
                    this_column['dataIndex']=this.getDateHeader(ldate)+"_"+i;
                    this_column['width']=40;
                    
                    cols.push(this_column);
                    
                    fieldstore.push(this.getDateHeader(ldate)+"_"+i);
                    j++;
             }
             ldate=ldate.add("d",1);
        }
         //riconfigurazione griglia e store per date

         var newstore=this.getNewStore(fieldstore);
         
         
         win.getColumnModel().setConfig(cols);
         win.reconfigure(newstore,  win.getColumnModel());
         
         for (var x=0;x<j;x++){
         win.getColumnModel().setRenderer(x,function(value,meta,record,rowindex,collindex,store){
             var startindex=win.getColumnModel().getDataIndex(collindex);
             var endindex=win.getColumnModel().getDataIndex(collindex);
             if (value=='free'){
                 meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:#F3E4B1;border-left-color:#F3E4B1;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 if (startindex==startdayevent)
                    meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:#F3E4B1;border-left-color:green;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 if (endindex==enddayevent)
                    meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:#F3E4B1;border-left-color:red;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 if (endindex==enddayevent && startindex==startdayevent)
                     meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:red;border-left-color:green;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 
                    
             }else if (value=='busy'){
                 meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:#9FC6E7;border-left-color:#9FC6E7;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 if (startindex==startdayevent)
                    meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:#9FC6E7;border-left-color:green;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 if (endindex==enddayevent)
                    meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:#9FC6E7;border-left-color:red;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 if (endindex==enddayevent && startindex==startdayevent)
                     meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:red;border-left-color:green;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 
                    
             }else if (value=='noinfo'){
                 meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:lightgrey;border-left-color:lightgrey;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
                 if (startindex==startdayevent)
                    meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:lightgrey;border-left-color:green;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
                 if (endindex==enddayevent)
                    meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:lightgrey;border-left-color:red;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
                 if (endindex==enddayevent && startindex==startdayevent)
                     meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:red;border-left-color:green;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
                 
                    
            }else{ 
                 meta.attr ='style="line-height:8px;"';
                 return value;
             }
         });
        //
       }
         newstore.baseParams.event_id=eventForm.event_id;
         newstore.baseParams.hstart=start;
         newstore.baseParams.hend=end;
         newstore.baseParams.startdd=startdd;
         newstore.baseParams.startmm=startmm;
         newstore.baseParams.startyyyy=startyyyy;
         newstore.baseParams.enddd=enddd;
         newstore.baseParams.endmm=endmm;
         newstore.baseParams.endyyyy=endyyyy;

         var storePlanning=eventForm.planningGrid.getStore();
         var cm=eventForm.planningGrid.getColumnModel();
         var planning = Ext.encode(Ext.pluck(storePlanning.data.items, 'data'));
         newstore.baseParams.planning=planning;

         
         
         if (this.existMail(storePlanning)){
            eventForm.availablePlanning.setDisabled(true);
            eventForm.deleteRowPlanning.setDisabled(true);
            eventForm.planningGrid.getBottomToolbar().setVisible(true);
            newstore.load(); 
            eventForm.planningGrid.reconfigure(newstore,  win.getColumnModel());    //riconfiguro grid per store disponibilit�
            eventForm.fthh.on('select',function(){
                this.refreshGrid(eventForm,eventForm.planningGrid,newstore,planning);
            },this);
            eventForm.tthh.on('select',function(){
                this.refreshGrid(eventForm,eventForm.planningGrid,newstore,planning);
            },this);
            eventForm.yyyymmdd.on('select',function(){
                this.refreshGrid(eventForm,eventForm.planningGrid,newstore,planning);
            },this);
            eventForm.enddate.on('select',function(){
                this.refreshGrid(eventForm,eventForm.planningGrid,newstore,planning);
            },this);
            
            
            eventForm.closeavailablePlanning.setHandler(function(){ //chiusura grid disponibilit�
                eventForm.planningGrid.reconfigure(storePlanning,  cm);
                eventForm.availablePlanning.setDisabled(false);
                eventForm.closeavailablePlanning.setHidden(true);
                eventForm.planningGrid.getBottomToolbar().setVisible(false);
            });
            eventForm.closeavailablePlanning.setHidden(false);
            //win.show(); 
              
         }else{
            WT.alert(WT.res('calendar','dialogErrorTitle'),WT.res('calendar','msgnoplanning'));
         }
         
    },
    
    removeActivities:function(){
        var rec=this.gridActivities.getSelectionModel().getSelected();
        var id=rec.get("activity_id");
        WT.JsonAjaxRequest({
                url: "ServiceRequest",
                params: {
                    service: 'calendar',
                    action: 'RemoveActivities',
                    id:id
                },
                method: "POST",
                callback: function(o,options) {
                    if (o.success){
                        this.gridActivities.getStore().load();
                    }else{
                        WT.alert(WT.res('error'),WT.res('error'));
                    }
                },
                scope: this
            });    
    },
    
    getGridActivities:function(){
        /*var rowSelModel= new WT.GridSelectionModel({
            singleSelect:false
        });
        var store = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url:'ServiceRequest'
            }),
            baseParams: {
                service:'calendar',
                action:'GetManageActivities'
            },
            sortInfo:{
                field: 'description', 
                direction: "ASC"
            },
            autoLoad:true, 
            reader: new Ext.data.JsonReader({
                root: 'get_activities',
                fields: [
                'activity_id',
                'description',
                'login',
                'username',
                'language',
                'language_description',
                'read_only',
                'external_id'
                ]
            })
        });
        var buttonAdd=new Ext.Button({
           text:WT.res("calendar","add"),
           iconCls: 'icon-add',
           scope:this,
           handler: function(b,e){
                this.winAddActivities();
           }
        });
        var buttonRemove=new Ext.Button({
           text:WT.res("calendar","remove"),
           iconCls: 'icon-deleterow',
           scope:this,
           handler: function(b,e){
                WT.confirm(null, WT.res('calendar','msgdeletecalendar')+"?", function(b) {
                        if (b=="yes"){
                            this.removeActivities();   
                        }
                 },this);
           }
        });
        
        var gridActivities = new Ext.grid.GridPanel({
            height:250,
            region:'center',
            split:true,
            tbar:[buttonAdd,buttonRemove],
            loadMask: {
                msg: WT.res("calendar","loading")
                },
            viewConfig: {
                forceFit: true
            },
            selModel: rowSelModel,
            store : store,
            autoExpandColumn:'description',
            cm : new Ext.grid.ColumnModel([
            {id:'activity_id',hidden:true,menuDisabled:true,dataIndex:'activity_id'},
            {id:'external_id',hidden:true,menuDisabled:true,dataIndex:'external_id'},
            {id:'description',menuDisabled:true,dataIndex:'description',width: 150,header:WT.res('calendar','description')},
            {id:'login',hidden:true,menuDisabled:true,dataIndex:'login'},
            {id:'username',menuDisabled:true,dataIndex:'username',width: 150,header:WT.res('calendar','user')},
            {id:'language',menuDisabled:true,dataIndex:'language',hidden:true},
            {id:'language_description',menuDisabled:true,dataIndex:'language_description',width: 100,header:WT.res('calendar','language')},
            {id:'read_only',menuDisabled:true,dataIndex:'read_only',width: 100,header:WT.res('calendar','read_only'),
                renderer: function(value,metadata,record,rowIndex,colIndex,store){ 
                                    
                                    if (value=="true"){
                                        var a="webtop/themes/"+WT.theme+"/calendar/default.png";
                                        return "<img src='"+a+"' width=12 height=12 border=0 align=center>"
                                    }
                                    return "";
                                }
            }
            ])
            
        });
        gridActivities.on("dblclick",function(){
            var rec=gridActivities.getSelectionModel().getSelected();
            this.winEditActivities(rec);
        },this);
        return gridActivities;*/
        return null;
    
    },
    
    getFormEvents:function(){
      var date_start= new Ext.form.DateField({        
        width: 100,
        format: 'd/m/Y',
        fieldLabel:WT.res("calendar","date_start"),
        allowBlank:false
      });
      var date_end= new Ext.form.DateField({        
        width: 100,
        format: 'd/m/Y',
        fieldLabel:WT.res("calendar","date_end"),
        allowBlank:false
      });
      var form=new Ext.FormPanel({
               labelWidth:100,
               bodyCssClass: "x-panel-mc", 
               labelAlign: 'right',
               border:false,
               bodyStyle:'padding:5px 5px 0',
               defaultType: 'textfield',
               background:'trasparent',
               date_start:date_start,
               date_end:date_end,
               width:'100%',
               height:200,
               region:'center',
               items:[date_start,date_end],
               scope:this
            });
            return form;  
    },
    
    exportEvents:function(){
        var form=this.getFormEvents();
        var buttonExportEvents=new Ext.Button({
           text:WT.res("calendar","export_events"),
           iconCls: 'iconExport',
           scope:this,
           handler: function(b,e){
                var date_start=form.date_start.getRawValue();
                var date_end=form.date_end.getRawValue();
                this.href='ServiceRequest?service=calendar&action=ExportEvent&nowriter=true&date_start='+date_start+'&date_end='+date_end  ;
                window.open(this.href);
           }
        });
        var win=WT.app.createWindow({
            title:WT.res('calendar','export_events'),
            iconCls:'iconExport',
            width: 250,
            height:150,
            layout: 'fit',
            //autoScroll: true,
            modal:true,
            minimizable:false,
            maximizable:false,
            tbar:[buttonExportEvents],
            form:form,
            items: [form]
        });
        win.show();
    },
    
    
    manageActivities:function(){
        this.gridActivities=this.getGridActivities();
        var win=WT.app.createWindow({
            title:WT.res('calendar','manageactivities'),
            iconCls:'iconActivities',
            width: 550,
            height:400,
            layout: 'fit',
            //autoScroll: true,
            modal:true,
            minimizable:false,
            maximizable:false,
            gridActivities:this.gridActivities,
            items: [this.gridActivities]
        });
        win.show();
    },
    
    
    winAddActivities:function(){
       var buttonSave=new Ext.Button({
           text:WT.res("calendar","saveclose"),
           iconCls: 'icon-save',
           scope:this,
           handler: function(b,e){
                if (form.getForm().isValid()){
                    var description=form.description.getValue();
                    var login=form.login.getValue();
                    var language=form.language.getValue();
                    var activity_id=form.activity_id;
                    var read_only=form.read_only.getValue();
                    var external_id=form.external_id.getValue();
                    this.saveActivities(activity_id,description,login,language,read_only,external_id);
                    win.close();
                }
           }
        });
        var form=this.getFormActivities();
        form.language.getStore().on("load",function(){
            form.language.setValue("it");
        },this);
        var win=WT.app.createWindow({
            title:WT.res('calendar','manageactivities'),
            iconCls:'iconActivities',
            width: 500,
            height:200,
            layout: 'fit',
            //autoScroll: true,
            modal:true,
            minimizable:false,
            maximizable:false,
            form:form,
            tbar:[buttonSave],
            items: [form]
        });
        win.show();
    },
    
    
    winEditActivities:function(rec){
       
       var buttonSave=new Ext.Button({
           text:WT.res("calendar","saveclose"),
           iconCls: 'icon-save',
           scope:this,
           handler: function(b,e){
                if (form.getForm().isValid()){
                    var description=form.description.getValue();
                    var login=form.login.getValue();
                    var language=form.language.getValue();
                    var activity_id=form.activity_id;
                    var read_only=form.read_only.getValue();
                    var external_id=form.external_id.getValue();
                    this.saveActivities(activity_id,description,login,language,read_only,external_id);
                    win.close();
                    
                }
           }
        });
        var form=this.getFormActivities();
        form.description.setValue(rec.get("description"));
        
        form.language.getStore().on("load",function(){
            form.language.setValue(rec.get("language"));
        },this);
        form.language.getStore().load();
        form.read_only.setValue(rec.get("read_only"));
        var first=true;
        form.login.getStore().on("load",function(){
            if (first) form.login.setValue(rec.get("login"));
            first=false;
        },this);
        form.login.getStore().load();
        form.activity_id=rec.get("activity_id");
        form.external_id.setValue(rec.get("external_id"));
        var win=WT.app.createWindow({
            title:WT.res('calendar','manageactivities'),
            iconCls:'iconActivities',
            width: 500,
            height:200,
            layout: 'fit',
            //autoScroll: true,
            modal:true,
            minimizable:false,
            maximizable:false,
            form:form,
            tbar:[buttonSave],
            items: [form]
        });
        win.show();
    },
    
    saveActivities:function(activity_id,description,login,language,read_only,external_id){
        WT.JsonAjaxRequest({
                url: "ServiceRequest",
                params: {
                    service: 'calendar',
                    action: 'SaveActivities',
                    description: description,
                    activity_id:activity_id,
                    login:login,
                    language:language,
                    read_only:read_only,
                    external_id:external_id
                },
                method: "POST",
                callback: function(o,options) {
                    if (o.success){
                        this.gridActivities.getStore().load();
                    }else{
                        WT.alert(WT.res('error'),WT.res('error'));
                    }
                },
                scope: this
            });    
    },
    
    getStoreLogin:function(){
      /*var store=new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetLoginList'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewloginlist',
            fields: ['id','value']
        })
        });
        return store;*/
        return null;
    },
    getStoreLanguage:function(){
      /*var store=new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetLanguageList'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewlanguagelist',
            fields: ['id','value']
        })
        });
        return store;*/
        return null;
    },
    getStoreActivitiesDescription:function(){
      /*var store=new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetActivitiesDescription'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'description',
            fields: ['id','value']
        })
        });
        return store;*/
        return null;
    },
    
    getStoreCustomer:function(){
      /*var store=new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewCustomer_id'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewcustomer_id',
            fields: ['id','value']
        })
        
    });
    return store;*/
        return null;
    },
    
    
    getFormConfirmCausal:function(){
        var external_id= new Ext.form.TextField({
            fieldLabel: WT.res('drm',"external_id"),
            width:100
        });
        var description= new Ext.form.TextField({
            fieldLabel: WT.res('drm',"description"),
            allowBlank:false,
            width:300
        });
        
        var storeLanguage=this.getStoreLanguage();
        var language = new Ext.form.ComboBox({
            store: storeLanguage,
            mode: 'remote',
            //typeAhead : true,
            selectOnFocus : true,
            forceSelection: true,
            triggerAction: 'all',
            displayField: 'value',
            valueField: 'id',
            autoSelect: true,
            width:300,
            fieldLabel:WT.res("calendar","language"),
            allowBlank:false,
            editable:false
        });
        var storeLogin=this.getStoreLogin();
        var storeCustomer=this.getStoreCustomer();
        var login = new Ext.form.ComboBox({
            store: storeLogin,
            mode: 'remote',
            //typeAhead : true,
            selectOnFocus : true,
            forceSelection: true,
            triggerAction: 'all',
            displayField: 'value',
            valueField: 'id',
            editable: true,
            autoSelect: true,
            width:300,
            fieldLabel:WT.res("calendar","user"),
            minChars: 1,
            queryDelay: 500
        });
        var customer_id = new Ext.form.ComboBox({                      
            name: 'cmbCustomer',
            fieldLabel: WT.res('calendar','customers'),
            mode: 'remote',
            width: 300,
            triggerAction: 'all',
            editable: true,
            store: storeCustomer,
            displayField:'value',
            valueField: 'id',
            typeAhead: false,              
            forceSelection: false,                  
            selectOnFocus:false,
            minChars: 1,
            loadingText: WT.res("calendar","waiting"),
            queryDelay: 300
        });
         var status = new Ext.form.ComboBox({
                name: 'status',
                width: 100,
                fieldLabel: WT.res('calendar','status'),
                allowBlank: false,
                triggerAction: 'all',
                store: new Ext.data.ArrayStore({
                fields: ['id','value'],
                data : [
                            ['O',WT.res('calendar','status_open')],
                            ['C',WT.res('calendar','status_close')]
                    ]
                }),
                valueField: 'id',
                displayField: 'value',
                mode: 'local',
                forceSelection: true,
                value:'O'
        });
        
        var form=new Ext.FormPanel({
               labelWidth:150,
               bodyCssClass: "x-panel-mc", 
               labelAlign: 'right',
               border:false,
               bodyStyle:'padding:5px 5px 0',
               defaultType: 'textfield',
               background:'trasparent',
               description:description,
               login:login,
               language:language,
               external_id:external_id,
               customer_id:customer_id,
               status:status,
               width:'100%',
               height:200,
               region:'center',
               items:[external_id,description,login,customer_id,language,status],
               scope:this
            });
            return form;
    },
    
    
    
    getFormActivities:function(){
        var external_id= new Ext.form.TextField({
            fieldLabel: WT.res('drm',"external_id"),
            width:100
        });
        var storeDescription=this.getStoreActivitiesDescription();
        var description=new WT.SuggestTextField({ 
            store: storeDescription,
            mode: 'remote',
            //typeAhead : true,
            selectOnFocus : true,
            triggerAction: 'all',
            displayField: 'value',
            valueField: 'id',
            editable: true,
            width:300,
            fieldLabel: WT.res('drm',"description"),
            minChars: 1,
            queryDelay: 500,
            name: 'description',
            allowBlank:false
        });
        
        var storeLanguage=this.getStoreLanguage();
        var language = new Ext.form.ComboBox({
            store: storeLanguage,
            mode: 'remote',
            //typeAhead : true,
            selectOnFocus : true,
            forceSelection: true,
            triggerAction: 'all',
            displayField: 'value',
            valueField: 'id',
            autoSelect: true,
            width:220,
            fieldLabel:WT.res("calendar","language"),
            allowBlank:false,
            editable:false
        });
        var storeLogin=this.getStoreLogin();
        var login = new Ext.form.ComboBox({
            store: storeLogin,
            mode: 'remote',
            //typeAhead : true,
            selectOnFocus : true,
            forceSelection: true,
            triggerAction: 'all',
            displayField: 'value',
            valueField: 'id',
            editable: true,
            autoSelect: true,
            width:220,
            fieldLabel:WT.res("calendar","user"),
            minChars: 1,
            queryDelay: 500
        });
        var read_only=new Ext.form.Checkbox({                   
            fieldLabel: WT.res('calendar','read_only')
        });
        var form=new Ext.FormPanel({
               labelWidth:150,
               bodyCssClass: "x-panel-mc", 
               labelAlign: 'right',
               border:false,
               bodyStyle:'padding:5px 5px 0',
               defaultType: 'textfield',
               background:'trasparent',
               description:description,
               login:login,
               language:language,
               read_only:read_only,
               external_id:external_id,
               width:'100%',
               height:200,
               region:'center',
               items:[external_id,description,login,language,read_only],
               scope:this
            });
            return form;
    },
    
    winAddConfirmCausal:function(){
       var buttonSave=new Ext.Button({
           text:WT.res("calendar","saveclose"),
           iconCls: 'icon-save',
           scope:this,
           handler: function(b,e){
                if (form.getForm().isValid()){
                    var description=form.description.getValue();
                    var login=form.login.getValue();
                    var language=form.language.getValue();
                    var customer_id=form.customer_id.getValue();
                    var external_id=form.external_id.getValue();
                    var causalconfirm_id=form.causalconfirm_id;
                    var status=form.status.getValue();
                    this.saveConfirmCausal(causalconfirm_id,external_id,description,login,customer_id,language,status);
                    win.close();
                }
           }
        });
        var form=this.getFormConfirmCausal();
        form.language.getStore().on("load",function(){
            form.language.setValue("it");
        },this);
        var win=WT.app.createWindow({
            title:WT.res('calendar','manageconfirmcausal'),
            iconCls:'iconConfirmCausal',
            width: 600,
            height:250,
            layout: 'fit',
            //autoScroll: true,
            modal:true,
            minimizable:false,
            maximizable:false,
            form:form,
            tbar:[buttonSave],
            items: [form]
        });
        win.show();
    },
    
    
    winEditConfirmCausal:function(rec){
       
       var buttonSave=new Ext.Button({
           text:WT.res("calendar","saveclose"),
           iconCls: 'icon-save',
           scope:this,
           handler: function(b,e){
                if (form.getForm().isValid()){
                    var description=form.description.getValue();
                    var login=form.login.getValue();
                    var language=form.language.getValue();
                    var customer_id=form.customer_id.getValue();
                    var external_id=form.external_id.getValue();
                    var causalconfirm_id=form.causalconfirm_id;
                    var status=form.status.getValue();
                    this.saveConfirmCausal(causalconfirm_id,external_id,description,login,customer_id,language,status);
                    win.close();
                    
                }
           }
        });
        var form=this.getFormConfirmCausal();
        form.description.setValue(rec.get("description"));
        
        form.language.getStore().on("load",function(){
            form.language.setValue(rec.get("language"));
        },this);
        form.language.getStore().load();
        form.external_id.setValue(rec.get("external_id"));
        var first=true;
        form.login.getStore().on("load",function(){
            if (first) form.login.setValue(rec.get("login"));
            first=false;
        },this);
        form.login.getStore().load();
        var first_customer=true;
        form.customer_id.getStore().on("load",function(){
            if (first_customer) form.customer_id.setValue(rec.get("customer"));
            first_customer=false;
        },this);
        form.customer_id.getStore().load();
        form.status.setValue(rec.get("status"));
        
        form.causalconfirm_id=rec.get("confirm_causal_id");
        var win=WT.app.createWindow({
            title:WT.res('calendar','manageconfirmcausal'),
            iconCls:'iconConfirmCausal',
            width: 600,
            height:250,
            layout: 'fit',
            //autoScroll: true,
            modal:true,
            minimizable:false,
            maximizable:false,
            form:form,
            tbar:[buttonSave],
            items: [form]
        });
        win.show();
    },
    
    saveConfirmCausal:function(causalconfirm_id,external_id,description,login,customer_id,language,status){
        WT.JsonAjaxRequest({
                url: "ServiceRequest",
                params: {
                    service: 'calendar',
                    action: 'SaveConfirmCausal',
                    description: description,
                    causalconfirm_id:causalconfirm_id,
                    login:login,
                    language:language,
                    customer_id:customer_id,
                    status:status,
                    external_id:external_id
                },
                method: "POST",
                callback: function(o,options) {
                    if (o.success){
                        this.gridCausalConfirm.getStore().load();
                    }else{
                        WT.alert(WT.res('error'),WT.res('error'));
                    }
                },
                scope: this
            });    
    },
    
    
    getGridConfirmCausal:function(){
        /*var rowSelModel= new WT.GridSelectionModel({
            singleSelect:false
        });
        var store = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url:'ServiceRequest'
            }),
            baseParams: {
                service:'calendar',
                action:'GetManageConfirmCausal'
            },
            sortInfo:{
                field: 'description', 
                direction: "ASC"
            },
            autoLoad:true, 
            reader: new Ext.data.JsonReader({
                root: 'get_confirm_causal',
                fields: [
                'confirm_causal_id',
                'external_id',
                'description',
                'login',
                'username',
                'language',
                'language_description',
                'customer',
                'customer_description',
                'status'
                ]
            })
        });
        var buttonAdd=new Ext.Button({
           text:WT.res("calendar","add"),
           iconCls: 'icon-add',
           scope:this,
           handler: function(b,e){
                this.winAddConfirmCausal();
           }
        });
        var buttonRemove=new Ext.Button({
           text:WT.res("calendar","remove"),
           iconCls: 'icon-deleterow',
           scope:this,
           handler: function(b,e){
                WT.confirm(null, WT.res('calendar','msgdeletecalendar')+"?", function(b) {
                        if (b=="yes"){
                            this.removeConfirmCausal();   
                        }
                 },this);
           }
        });
        
        this.gridCausalConfirm = new Ext.grid.GridPanel({
            height:250,
            region:'center',
            split:true,
            tbar:[buttonAdd,buttonRemove],
            loadMask: {
                msg: WT.res("calendar","waiting"),
                },
            viewConfig: {
                forceFit: true
            },
            selModel: rowSelModel,
            store : store,
            autoExpandColumn:'description',
            cm : new Ext.grid.ColumnModel([
            {id:'confirm_causal_id',hidden:true,menuDisabled:true,dataIndex:'confirm_causal_id'},
            {id:'external_id',menuDisabled:true,dataIndex:'external_id',header:WT.res('calendar','external_id')},
            {id:'description',menuDisabled:true,dataIndex:'description',width: 150,header:WT.res('calendar','description')},
            {id:'login',hidden:true,menuDisabled:true,dataIndex:'login'},
            {id:'username',menuDisabled:true,dataIndex:'username',width: 150,header:WT.res('calendar','user')},
            {id:'customer',menuDisabled:true,dataIndex:'customer',hidden:true},
            {id:'customer_description',menuDisabled:true,dataIndex:'customer_description',width: 100,header:WT.res('calendar','customers')},
            {id:'language',menuDisabled:true,dataIndex:'language',hidden:true},
            {id:'language_description',menuDisabled:true,dataIndex:'language_description',width: 100,header:WT.res('calendar','language')},
            {id:'status',menuDisabled:true,dataIndex:'status',dataIndex:'status',header:WT.res('calendar','status'),
            renderer : function(v, p, record){
                    if (v=='O') 
                        return WT.res("calendar","status_open");
                    else
                        return WT.res("calendar","status_close");
                },}
            ])
            
        });
        this.gridCausalConfirm.on("dblclick",function(){
            var rec=this.gridCausalConfirm.getSelectionModel().getSelected();
            this.winEditConfirmCausal(rec);
        },this);
        return this.gridCausalConfirm;*/
        return null;
    
    },
    
    removeConfirmCausal:function(){
        var rec=this.gridConfirmCausal.getSelectionModel().getSelected();
        var id=rec.get("confirm_causal_id");
        WT.JsonAjaxRequest({
                url: "ServiceRequest",
                params: {
                    service: 'calendar',
                    action: 'RemoveConfirmCausal',
                    id:id
                },
                method: "POST",
                callback: function(o,options) {
                    if (o.success){
                        this.gridConfirmCausal.getStore().load();
                    }else{
                        WT.alert(WT.res('error'),WT.res('error'));
                    }
                },
                scope: this
            });    
    },
    
    manageConfirmCausal:function(){
        this.gridConfirmCausal=this.getGridConfirmCausal();
        var win=WT.app.createWindow({
            title:WT.res('calendar','manageconfirmcausal'),
            iconCls:'iconConfirmCausal',
            width: 600,
            height:400,
            layout: 'fit',
            //autoScroll: true,
            modal:true,
            minimizable:false,
            maximizable:false,
            gridActivities:this.gridConfirmCausal,
            items: [this.gridConfirmCausal]
        });
        win.show();
    },
    
    
    refreshGrid:function(eventForm,win,newstore,planning){
         newstore.removeAll(true);
         win.setVisible(false);
         var cols=[];
         var start=this.hourstartworkday.substring(0,2);
         var end=this.hourendworkday.substring(0,2);
         var hourstartevent=eventForm.fthh.getValue();
         var hourendevent=this.roundHour(eventForm.tthh.getValue());
         hourstartevent=hourstartevent.substring(0,2);
         var ldate = eventForm.yyyymmdd.getValue();
         var startyyyy = ldate.getFullYear();
         var startmm = ""+(ldate.getMonth()+1);
         if ((ldate.getMonth()+1)<10) startmm="0"+startmm;
         var startdd = ""+ldate.getDate();
         if (ldate.getDate()<10) startdd="0"+startdd;
         var tdate = eventForm.enddate.getValue();
         var endyyyy = tdate.getFullYear();
         var endmm = ""+(tdate.getMonth()+1);
         if ((tdate.getMonth()+1)<10) endmm="0"+endmm;
         var enddd = ""+tdate.getDate();
         if (tdate.getDate()<10) enddd="0"+enddd;
         var startdayevent=this.getDateHeader(ldate)+"_"+hourstartevent;
         var enddayevent=this.getDateHeader(tdate)+"_"+hourendevent;
         var fieldstore=[];
         var this_column = {};
         this_column['id']="name";
         this_column['header']=WT.res("calendar","nameinvit");
         this_column['dataIndex']="name";
         this_column['width']="150";
         cols.push(this_column);
         fieldstore.push("name");
         var j=1;
         while(ldate<=tdate){
             for (var i=start;i<end;i++){
                    var this_column = {};
                    this_column['id']=this.getDateHeader(ldate)+"_"+i+":00";
                    this_column['header']=this.getDescriptionDate(ldate)+" "+i+":00";
                    this_column['dataIndex']=this.getDateHeader(ldate)+"_"+i;
                    this_column['width']=40;
                    cols.push(this_column);
                    fieldstore.push(this.getDateHeader(ldate)+"_"+i);
                    j++;
             }
             ldate=ldate.add("d",1);
             
        }
         var storerefresh=this.getNewStore(fieldstore);
         
         win.getColumnModel().setConfig(cols);
         win.reconfigure(storerefresh,win.getColumnModel());
         for (var x=0;x<j;x++){
         win.getColumnModel().setRenderer(x,function(value,meta,record,rowindex,collindex,store){         
             var startindex=win.getColumnModel().getDataIndex(collindex);
             var endindex=win.getColumnModel().getDataIndex(collindex);
             if (value=='free'){
                 meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:#F3E4B1;border-left-color:#F3E4B1;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 if (startindex==startdayevent)
                    meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:#F3E4B1;border-left-color:green;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 if (endindex==enddayevent)
                    meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:F3E4B1;border-left-color:red;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 if (endindex==enddayevent && startindex==startdayevent)
                     meta.attr = 'style="background-color:#F3E4B1;border-style:solid;border-right-color:red;border-left-color:green;border-bottom-color:#F3E4B1;border-top-color:#F3E4B1;line-height:8px;"';
                 
                    
             }else if (value=='busy'){
                 meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:#9FC6E7;border-left-color:#9FC6E7;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 if (startindex==startdayevent)
                    meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:#9FC6E7;border-left-color:green;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 if (endindex==enddayevent)
                    meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:#9FC6E7;border-left-color:red;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 if (endindex==enddayevent && startindex==startdayevent)
                     meta.attr = 'style="background-color:#9FC6E7;border-style:solid;border-right-color:red;border-left-color:green;border-bottom-color:#9FC6E7;border-top-color:#9FC6E7;line-height:8px;"';
                 
                    
             }else if (value=='noinfo'){
                 meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:lightgrey;border-left-color:lightgrey;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
                 if (startindex==startdayevent)
                    meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:lightgrey;border-left-color:green;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
                 if (endindex==enddayevent)
                    meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:lightgrey;border-left-color:red;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
                 if (endindex==enddayevent && startindex==startdayevent)
                     meta.attr = 'style="background-color:lightgrey;border-style:solid;border-right-color:red;border-left-color:green;border-bottom-color:lightgrey;border-top-color:lightgrey;line-height:8px;"';
            }else{ 
                 meta.attr ='style="line-height:8px;"';
                 return value;
             }
         });
        //
         }
         win.getStore().baseParams.event_id=eventForm.event_id;
         win.getStore().baseParams.hstart=start;
         win.getStore().baseParams.hend=end;
         win.getStore().baseParams.startdd=startdd;
         win.getStore().baseParams.startmm=startmm;
         win.getStore().baseParams.startyyyy=startyyyy;
         win.getStore().baseParams.enddd=enddd;
         win.getStore().baseParams.endmm=endmm;
         win.getStore().baseParams.endyyyy=endyyyy;
         win.getStore().baseParams.planning=planning;
         win.getStore().load();
         win.setVisible(true);
    },
  
     

     
     
     existMail:function(store){
       var records = store.getRange();
       var exist=false;
       for (var i = 0; i < records.length; i++) {
           if (records[i].get("name")!=""){
               exist=true;
               break;
           }
       }
       return exist;
       
     },
     
    
     getJsonOfStore:function(store){
        var datar = new Array();
        var jsonDataEncode = "";
        var records = store.getRange();
        for (var i = 0; i < records.length; i++) {
            datar.push(records[i].data);
        }
        jsonDataEncode = Ext.util.JSON.encode(datar);

        return jsonDataEncode;
    },
    
    dirtyRecurrence:function(form){
        var dirty=false;
        form.items.each(function(f){
            window.fo=f;
            if (f.xtype=='fieldset' || f.xtype=='label'  ) return true;
            if (f.isDirty()) {
              dirty=true;
              return false;
            }
          });
        return dirty;
    },
    
     
    saveEvent : function(eventForm){
        if (this.waitMessage==null) this.waitMessage = new Ext.LoadMask(Ext.getBody(), {msg:this.cs.res("waiting")});
        this.waitMessage.show();
        var uid="";
        if (eventForm.uid!=undefined) uid=eventForm.uid;
        var event_id = eventForm.event_id;
        var recurr_id = eventForm.recurr_id;
        var event_by = eventForm.groupCombo.getValue();
        var event = eventForm.event.getValue();
        var report_id = eventForm.report_id.getValue();
        var ldate = eventForm.yyyymmdd.getValue();
        var yyyy = ldate.getFullYear();
        var mm = ""+(ldate.getMonth()+1);
        if ((ldate.getMonth()+1)<10) mm="0"+mm;
        var dd = ""+ldate.getDate();
        if (ldate.getDate()<10) dd="0"+dd;
        var fromtime = eventForm.fthh.getValue();
        var totime = eventForm.tthh.getValue();
        var allday=eventForm.allday.getValue();
        var enddate = eventForm.enddate.getRawValue();
        var description = eventForm.description.getValue();
        var private_value = "";
        if (eventForm.private_value.getValue()==true)
          private_value = "Y";
        var reminder = eventForm.reminder.getValue();
        var share_with = eventForm.share_with.getValue();
        var activity_id = eventForm.activity_id.getValue();
        var activity_flag = eventForm.activity_flag.getValue();
        var calendar_id = eventForm.calendar_id.getValue();
        if (calendar_id==eventForm.groupCombo.getRawValue()) 
            calendar_id="WebTop";
        var customer_id = eventForm.customer_id.getValue();
        var statistic_id = eventForm.statistic_id.getValue();
        var none_recurrence = eventForm.none_recurrence.getValue();
        var dayly_recurrence = eventForm.dayly_recurrence.getValue();
        var weekly_recurrence = eventForm.weekly_recurrence.getValue();
        var monthly_recurrence = eventForm.monthly_recurrence.getValue();
        var yearly_recurrence = eventForm.yearly_recurrence.getValue();
        var dayly1 = eventForm.dayly1.getValue();
        var dayly_step = eventForm.dayly_step.getValue();
        var dayly2 = eventForm.dayly2.getValue();
        var weekly_step = eventForm.weekly_step.getValue();
        var weekly1 = eventForm.weekly1.getValue();
        var weekly2 = eventForm.weekly2.getValue();
        var weekly3 = eventForm.weekly3.getValue();
        var weekly4 = eventForm.weekly4.getValue();
        var weekly5 = eventForm.weekly5.getValue();
        var weekly6 = eventForm.weekly6.getValue();
        var weekly7 = eventForm.weekly7.getValue();
        var monthly_day = eventForm.monthly_day.getValue();
        var monthly_month = eventForm.monthly_month.getValue();
        var yearly_day = eventForm.yearly_day.getValue();
        var yearly_month = eventForm.yearly_month.getValue();
        var until_ldate = eventForm.until_yyyymmdd.getValue();
        var until_yyyy = "";
        var until_mm = "";
        var until_dd = "";
        if (until_ldate!=null && until_ldate!="") {
          until_yyyy = until_ldate.getFullYear();
          until_mm = ""+(until_ldate.getMonth()+1);
          if ((until_ldate.getMonth()+1)<10) until_mm="0"+until_mm;
          until_dd = ""+until_ldate.getDate();
          if (until_ldate.getDate()<10) until_dd="0"+until_dd;
        }
        var permanent_recurrence = eventForm.permanent_recurrence.getValue();
        var repeat_times=eventForm.repeat_times.getValue();
        var repeat_times_step=eventForm.repeat_times_step.getValue();
        
        var storePlanning=eventForm.planningGrid.getStore();
        var planning = Ext.encode(Ext.pluck(storePlanning.data.items, 'data'));
        var editplanning="1";
        if (uid==undefined || uid=="")
            editplanning=storePlanning.getModifiedRecords().length;
        
        
        var busy_value=eventForm.busy_value.getValue();
        var timezone_id=eventForm.timezone_id.getValue();
        var causal=eventForm.causal.getValue();
        //check modifiche per invio mail
        var mod_event=false;
        var mod_report_id=false;
        var mod_description=false;
        var mod_fromdate=false;
        var mod_todate=false;
        var mod_fthh=false;
        var mod_tthh=false;

        
        if (eventForm.mod_event!=null && eventForm.mod_event!=event) 
            mod_event=true; 

        if (eventForm.mod_description!=null && eventForm.mod_description!=description) 
            mod_description=true; 
        
        if (eventForm.mod_report_id!=null && eventForm.mod_report_id!=report_id)
            mod_report_id=true;

        if (eventForm.mod_fromdate!=null && eventForm.mod_fromdate.getTime()!=ldate.getTime()) 
            mod_fromdate=true; 
  
        if (eventForm.mod_todate!=null && eventForm.mod_todate.getTime()!=eventForm.enddate.getValue().getTime()) 
            mod_todate=true; 
            
        
        if (eventForm.mod_fthh!=null && eventForm.mod_fthh!=fromtime) 
            mod_fthh=true; 
        
        if (eventForm.mod_tthh!=null && eventForm.mod_tthh!=totime) 
            mod_tthh=true; 
          
        
         if (this.dirtyRecurrence(eventForm.recurrence)){
             mod_fromdate=true; 
             mod_todate=true;
         }
        //
        
        //se viene spezzata la sequenza di una ricorsione
        var old_recurr_id=eventForm.old_recurr_id;
        var type=eventForm.type;
        var params={
            service:'calendar',action:'SaveEvent',
            event_id:event_id,
            recurr_id:recurr_id,
            event_by:event_by,
            event:event,
            report_id:report_id,
            day:dd,
            month:mm,
            year:yyyy,
            fromtime:fromtime,
            totime:totime,
            description:description,
            private_value:private_value,
            reminder:reminder,
            share_with:share_with,
            activity_id:activity_id,
            activity_flag:activity_flag,
            calendar_id:calendar_id,
            customer_id:customer_id,
            statistic_id:statistic_id,
            none_recurrence:none_recurrence,
            dayly_recurrence:dayly_recurrence,
            weekly_recurrence:weekly_recurrence,
            monthly_recurrence:monthly_recurrence,
            yearly_recurrence:yearly_recurrence,
            dayly1:dayly1,
            dayly_step:dayly_step,
            dayly2:dayly2,
            weekly_step:weekly_step,
            weekly1:weekly1,
            weekly2:weekly2,
            weekly3:weekly3,
            weekly4:weekly4,
            weekly5:weekly5,
            weekly6:weekly6,
            weekly7:weekly7,
            monthly_day:monthly_day,
            monthly_month:monthly_month,
            yearly_day:yearly_day,
            yearly_month:yearly_month,
            until_yyyy:until_yyyy,
            until_mm:until_mm,
            until_dd:until_dd,
            allday:allday,
            enddate:enddate,
            planning:planning,
            editplanning:editplanning,
            uid:uid,
            mod_event:mod_event,
            mod_report_id:mod_report_id,
            mod_fromdate:mod_fromdate,
            mod_todate:mod_todate,
            mod_description:mod_description,
            mod_fthh:mod_fthh,
            mod_tthh:mod_tthh,
            old_recurr_id:old_recurr_id,
            permanent_recurrence:permanent_recurrence,
            repeat_times:repeat_times,
            repeat_times_step:repeat_times_step,
            busy_value:busy_value,
            timezone_id:timezone_id,
            type:type,
            causal:causal
        };
        WT.JsonAjaxRequest({
            url: 'ServiceRequest',
            params: params,
            method: "POST",
            callback: this.saveEventResult,
            scope: this
        });
   },

    saveEventResult: function(o, params, success) {
        if (this.group!=null && this.group!="")
            this.cs.groupChanged(this.group,this.groupname,this.groupacl);
        (function(){this.waitMessage.hide();}).defer(400,this);
        if (o) {
            var r=o.event;
            if (r[0].result=="F") {
                WT.show({
                    title:this.cs.res("dialogErrorTitle"),
                    msg: this.cs.res("dialogErrorMessage"),
                    buttons: Ext.Msg.OK,
                    scope: this
                });
            }
        }
    },
    
    load: function() {
        this.nrOfSelectedEvent=0;
        var date=this.value;
        date=date.add("d",-1).clearTime();
        var dstartdd = date.getDate();
        var dstartmm = date.getMonth()+1;
        var dstartyyyy = date.getFullYear();
        date=date.add("d",2).clearTime();
        var denddd = date.getDate();
        var dendmm = date.getMonth()+1;
        var dendyyyy = date.getFullYear();
        if (this.viewFlag=="w5") {
          var startingPos = this.value.getDay()-this.startDay;
          if(startingPos!=0 && startingPos < this.startDay) startingPos += 7;
          var start = this.value.add("d",startingPos*-1).clearTime();
          while(start.getDay()<1) start = start.add("d",1).clearTime();
          var end = start.add("d",5).clearTime();
          dstartdd = start.getDate();
          dstartmm = start.getMonth()+1;
          dstartyyyy = start.getFullYear();
          //denddd = end.getDaysInMonth();
          denddd = end.getDate();
          dendmm = end.getMonth()+1;
          dendyyyy = end.getFullYear();
        } else if (this.viewFlag=="w") {
          var startingPos = this.value.getDay()-this.startDay;
          if(startingPos!=0 && startingPos < this.startDay) startingPos += 7;
          var start = this.value.add("d",startingPos*-1).clearTime();
          while(start.getDay()<1) start = start.add("d",1).clearTime();
          var end = start.add("d",7).clearTime();
          dstartdd = start.getDate();
          dstartmm = start.getMonth()+1;
          dstartyyyy = start.getFullYear();
          //denddd = end.getDaysInMonth();
          denddd = end.getDate();
          dendmm = end.getMonth()+1;
          dendyyyy = end.getFullYear();
        } else if (this.viewFlag=="m") {
          dstartdd = 1;
          denddd = date.getDaysInMonth();
        }
        
        var viewallgroup=this.cs.groups.viewallgroup.getValue();
        var group=this.cs.cList.currentCb;
        if (group==null){
           group=this.group 
        }else{
            group=group.inputValue;
        }
        //this.cs.getPersonalCalendarMenu(group); //reload copy menu calendar
        var params={service:'calendar',action:'GetEvents',viewEvents:this.viewEvents,group:group,dstartdd:dstartdd,dstartmm:dstartmm,dstartyyyy:dstartyyyy,denddd:denddd,dendmm:dendmm,dendyyyy:dendyyyy,viewallgroup:viewallgroup};
        /*this.proxy.doRequest(
            'read',null,
            params,
            this.reader,
            this.eventsRead,
            this,
            params
        );*/
    },
    
    eventsRead: function(o, params, success) {
        if (o && success) {
            var r=o.records;
            this.dropAllEvent();
            Ext.each(r,this.evalRecord,this);
            this.redoLayout();
        }
    },
    
    evalRecord: function(item,index,allItems) {
        var start=new Date(item.get("startyear"),(item.get("startmonth")-1),item.get("startday"));
        var end=new Date(item.get("endyear"),(item.get("endmonth")-1),item.get("endday"));
        if (start.getTime()==end.getTime()){
                this.addEvent(
                    new Date(item.get("yyyy"),(item.get("mm")-1),item.get("dd")),
                    Ext.create("Sonicle.webtop.calendar.SchedulerEvent",{
                        separent : this,
                        event_id : item.get("event_id"),
                        recurr_id : item.get("recurr_id"),
                        event_date : new Date(item.get("yyyy"),(item.get("mm")-1),item.get("dd")),
                        event_startdate : new Date(item.get("startyear"),(item.get("startmonth")-1),item.get("startday")),
                        event_enddate : new Date(item.get("endyear"),(item.get("endmonth")-1),item.get("endday")),
                        event : item.get("event"),
                        hstart : item.get("hstart"),
                        mstart : item.get("mstart"),
                        hend : item.get("hend"),
                        mend : item.get("mend"),
                        dd_event : item.get("dd"),
                        mm_event : item.get("mm"),
                        yyyy_event : item.get("yyyy"),
                        hasDescription : (item.get("hasDescription")=="Y")?true:false,
                        readonly : (item.get("isReadonly")=="Y")?true:false,
                        planning : (item.get("isPlanning")=="Y")?true:false,
                        recurrence : (item.get("isRecurrence")=="Y")?true:false,
                        reminder : (item.get("isReminder")=="Y")?true:false,
                        calendar : item.get("calendar"),
                        colorcalendar : item.get("colorcalendar"),
                        allday:item.get("allday"),
                        timezone:(item.get("timezone")!="null" && item.get("timezone")!=undefined  && item.get("timezone")!=this.cs.defaulttimezone /*&& item.get("timezone")!="Europe/Paris"*/)?true:false,
                        fromRecurrence:item.get("fromRecurrence")=="true"?true:false,
                        visit:item.get("visit")=="true"?true:false,
                        activity:item.get("activity"),
                        report_id:item.get("report_id"),
                        customer_description:item.get("customer_description"),
                    })

                );
        }else{
            var day=start;
            while(day.between(start,end)){
                this.addEvent(
                    day,
                    Ext.create("Sonicle.webtop.calendar.SchedulerEvent",{
                        separent : this,
                        event_id : item.get("event_id"),
                        event_date : new Date(item.get("yyyy"),(item.get("mm")-1),item.get("dd")),
                        event_startdate : new Date(item.get("startyear"),(item.get("startmonth")-1),item.get("startday")),
                        event_enddate : new Date(item.get("endyear"),(item.get("endmonth")-1),item.get("endday")),
                        event : item.get("event"),
                        hstart : item.get("hstart"),
                        mstart : item.get("mstart"),
                        hend : item.get("hend"),
                        mend : item.get("mend"),
                        hasDescription : (item.get("hasDescription")=="Y")?true:false,
                        readonly : (item.get("isReadonly")=="Y")?true:false,
                        planning : (item.get("isPlanning")=="Y")?true:false,
                        recurrence : (item.get("isRecurrence")=="Y")?true:false,
                        reminder : (item.get("isReminder")=="Y")?true:false,
                        calendar : item.get("calendar"),
                        colorcalendar : item.get("colorcalendar"),
                        allday:item.get("allday"),
                        timezone:(item.get("timezone")!="null" && item.get("timezone")!=undefined  && item.get("timezone")!=this.cs.defaulttimezone /*&& item.get("timezone")!="Europe/Paris"*/)?true:false,
                        activity:item.get("activity"),
                        report_id:item.get("report_id"),
                        customer_description:item.get("customer_description")
                    })

                );
                day=day.add(Date.DAY,1);
            }
            
        }
    },
    
    dropAllEvent : function(){
        for (var i=1;i<=31;i++) {
            this.sd[i].removeAllSchedulerEvent();
        }
        this.redoLayout(true);
    },
        
    addEvent : function(date,event){
        var max = this.value.getDaysInMonth();
        if (this.viewFlag=="d") {
            max=1;
        } else if (this.viewFlag=="w5") {
            max=5;
        }else if (this.viewFlag=="w") {
            max=7;
        }
        for (var i=1;i<=max;i++) {
            
            if (this.sd[i].getDate().getTime() == date.getTime()) {
                if (event.event_startdate.getTime() == event.event_enddate.getTime()){
                    //event.allday="true";
                    this.sd[i].addSchedulerEvent(event);
                }else{
                    var start=event.event_startdate;
                    var end=event.event_enddate; 
                    if (this.sd[i].getDate().between(start,end)){
                        event.allday="true";
                        this.sd[i].addSchedulerEvent(event);
                    }
                    
                    
                }
                
                
            }
        }
        

        /*
        for (var i=1;i<=max;i++) {
            
            while (start.getTime()<end.getTime()){
                if (this.sd[i].getDate().getTime() == start.getTime()) {
                    //if (event.event_startdate.getTime() == event.event_enddate.getTime()){
                        event.allday="true";
                        this.sd[i].addSchedulerEvent(event);
                }
                start=event.event_startdate.add(Date.DAY,1);
            }
        }
        */
        
        
    }
       
    
});

Ext.define('Sonicle.webtop.calendar.SchedulerHour', {
  extend: 'Ext.container.Container',

    offsetHeight: 65,
    sdparent : null,

    initComponent: function(){
        this.callParent(arguments);
        // Hours col
        var cs=this.cs=this.sdparent.cs;
        this.htable = document.createElement("TABLE");
        this.htable.cellPadding=0;
        this.htable.cellSpacing=0;
        this.htable.border=0;
        this.htable.className="schedulerHTable";
        for (var i=0; i<=23; i++) {
            this.trHour = this.htable.insertRow(-1);
            this.tdHourLeft = this.trHour.insertCell(-1); 
            this.tdHourLeft.rowSpan = 2;
            this.tdHourLeft.className="schedulerHTable_TD_Border";
            this.tdHourLeft.innerHTML="";
            this.tdHour00 = this.trHour.insertCell(-1); 
            this.tdHour00.rowSpan = 2;
            this.tdHour00.className="schedulerHTable_TD_Left";
            this.tdHour00.innerHTML=i;
            this.tdMinute00 = this.trHour.insertCell(-1);
            this.tdMinute00.className="schedulerHTable_TD_Right";
            this.tdMinute00.innerHTML="00";
            this.tdHourRight = this.trHour.insertCell(-1); 
            this.tdHourRight.rowSpan = 2;
            this.tdHourRight.className="schedulerHTable_TD_BorderRight";
            this.tdHourRight.innerHTML="&nbsp;";
            this.trHour = this.htable.insertRow(-1);
            this.tdMinute30 = this.trHour.insertCell(-1);
            this.tdMinute30.innerHTML="&nbsp;";
        }
        
        // Hours div
        this.hdiv = document.createElement("DIV");
        this.hdiv.className="schedulerHourDiv";
        this.hdiv.appendChild(this.htable)
        this.hdiv.style.zIndex=0;

        // Header
        this.hdrtable = document.createElement("TABLE");
        this.hdrtable.cellPadding=0;
        this.hdrtable.cellSpacing=0;
        this.hdrtable.border=0;
        this.hdrtable.style.height=this.offsetHeight;
        this.hdrtable.className="schedulerHourHeader";
        this.hdrtrHour = this.hdrtable.insertRow(-1);
        this.hdrtdHour = this.hdrtrHour.insertCell(-1); 
        this.hdrtdHour.innerHTML="&nbsp;";
        // Header Hours div
        this.hdrdiv = document.createElement("DIV");
        this.hdrdiv.className="schedulerHourHeader";
        this.hdrdiv.style.zIndex=15;
        this.hdrdiv.appendChild(this.hdrtable)

        // Disabilita selezione evento
        var ehdiv = Ext.get(this.hdiv);
        ehdiv.on("click",this.mouseClick,this);
        var ehdrdiv = Ext.get(this.hdrdiv);
        ehdrdiv.on("click",this.mouseClick,this);
    },
	
    mouseClick : function(e) {
        //target = e.getTarget();
        this.sdparent.deselectAllSchedulerEvents();
    },
    
    getHour : function() {
        return this.hdiv;
    },
    
    getHeaderHour : function() {
        return this.hdrdiv;
    },
    
    setTitle : function(t) {
        this.hdrtdHour.innerHTML=t;
    },
    
    getOffsetWidth : function() {
        return this.htable.offsetWidth;
    },

    getOffsetHeight : function() {
        return this.offsetHeight;
    }

});

Ext.define('Sonicle.webtop.calendar.SchedulerDay', {
    extend: 'Ext.container.Container',
    sdparent : null,
    monthNames : Date.monthNames,
    dayNames : Date.dayNames,
    offsetHeight: 65,
    startHour : 8,
    endHour : 19,
    viewFlag : "d",
    seIndex:0,

    newEvent : function(e) {
        target = e.getTarget();
        var fh = (target.index)/2;
        var ftime = new Date();
        var ttime = new Date();
        if ((target.index%2)!=0) {
          fh=fh-0.5;
          th=fh;
          ftime.setHours(fh, 0, 0, 0)
          ttime.setHours(th, 30, 0, 0)
        } else {
          fh=fh-1;
          th=fh+1;          
          ftime.setHours(fh, 30, 0, 0)
          ttime.setHours(th, 0, 0, 0)
        }
        target.schedulerDay.sdparent.newEvent(target.schedulerDay.sdate,ftime, ttime);
    },
    
    initComponent: function(){
        this.callParent(arguments);
        // Scheduler col
        this.stable = document.createElement("TABLE");
        this.stable.cellPadding=0;
        this.stable.cellSpacing=0;
        this.stable.border=0;
        this.stable.className="schedulerSTable";
        var working="";
        var cs=this.cs=this.sdparent.cs;

        for (var i=1; i<=48; i++) {
            this.trHourSchedule = this.stable.insertRow(-1);
            this.tdHourScheduleLeft = this.trHourSchedule.insertCell(-1);
            this.tdHourScheduleLeft.i = i;
            this.tdHourScheduleLeft.className="schedulerSTable_TD_Left";
            this.tdHourScheduleLeft.innerHTML="&nbsp;";
            Ext.get(this.tdHourScheduleLeft).on("click",this.mouseDayClick,this);
            this.tdHourSchedule = this.trHourSchedule.insertCell(-1);
            this.tdHourSchedule.index = i;
            this.tdHourSchedule.schedulerDay = this;
            Ext.get(this.tdHourSchedule).on("click",this.mouseDayClick,this);
            Ext.get(this.tdHourSchedule).on("dblclick",this.newEvent,this);
            working="";
            if (i>(this.startHour*2) && i<=(this.endHour*2))
                working="_Working";
            if ((i%2)!=0)
                this.tdHourSchedule.className="schedulerSTable_TD_Up"+working;
            else
                this.tdHourSchedule.className="schedulerSTable_TD_Dn"+working;
            this.tdHourSchedule.innerHTML="&nbsp;";
        }
        // Scheduler div
        this.sdiv = document.createElement("DIV");
        this.sdiv.className="schedulerDiv";
        this.sdiv.appendChild(this.stable);
        Ext.get(this.sdiv).on("click",this.mouseClick,this);
        
        // Header
        this.hdrtable = document.createElement("TABLE");
        this.hdrtable.cellPadding=0;
        this.hdrtable.cellSpacing=0;
        this.hdrtable.border=0;
        this.hdrtable.style.width="100%";
        this.hdrtable.style.height="100%";
        this.hdrtable.className="schedulerDataHeader";
        this.hdrtrHour = this.hdrtable.insertRow(-1);
        this.hdrtdHour = this.hdrtrHour.insertCell(-1);
        this.hdrtdHour.innerHTML="&nbsp;";
        this.hdrtdHour.schedulerDay = this;
        this.row = this.hdrtable.insertRow(-1);
        this.cell = this.row.insertCell(-1);
        this.sdaydiv = document.createElement("DIV");
        this.sdaydiv.className="schedulerListDiv";
        this.sdaydiv.style.borderSpacing=0;
        this.sdaydiv.style.height=41;
        this.sdaydiv.style.cursor="default";
        this.cell.appendChild(this.sdaydiv);
        this.sdaydiv.schedulerDay = this;
        
        //Ext.get(this.sdaydiv).on("click",this.mouseClick,this);
        // Header Hours div        
        this.hdrdiv = document.createElement("DIV");
        this.hdrdiv.className="schedulerDataHeader";
        this.hdrdiv.style.zIndex=15;
        this.hdrdiv.appendChild(this.hdrtable);
        
        
        // Scheduler List div
        this.slistdiv = document.createElement("DIV");
        this.slistdiv.className="schedulerListDiv";
        this.slistdiv.style.borderSpacing=0;
        Ext.get(this.slistdiv).on("click",this.mouseClick,this);

        // Header List
        this.hdrlisttable = document.createElement("TABLE");
        this.hdrlisttable.cellPadding=0;
        this.hdrlisttable.cellSpacing=0;
        this.hdrlisttable.border=0;
        this.hdrlisttable.style.width="100%";
        this.hdrlisttable.className="schedulerListDataHeader";
        this.hdrlisttrHour = this.hdrlisttable.insertRow(-1);
        this.hdrlisttdHour = this.hdrlisttrHour.insertCell(-1); 
        this.hdrlisttdHour.innerHTML="&nbsp;";
        this.hdrlisttdHour.schedulerDay = this;
        // Header Hours List div
        this.hdrlistdiv = document.createElement("DIV");
        this.hdrlistdiv.className="schedulerListDataHeader";
        this.hdrlistdiv.appendChild(this.hdrlisttable);
        
        
        
        this.schedulerMenu=new Ext.menu.Menu({
            items: [
                new Ext.Action({
                    scope : this,
                    iconCls : 'icon-calendar-action-new',
                    text: cs.res("action-new"),
                    handler: function(){
                        this.actionNew();
                    }
                }),
                new Ext.Action({
                    scope : this,
                    iconCls : 'iconPrint',
                    text: cs.res("print"),
                    handler: function(){
                        this.actionPrint();
                    }
                }),
                new Ext.Action({
                    scope : this,
                    text: cs.res("today"),
                    handler: function(){
                        this.actionToday();
                    }
                })
            ]
        });
        //WT.app.setElementContextMenu(Ext.get(this.sdiv),this.schedulerMenu);
        //WT.app.setElementContextMenu(Ext.get(this.slistdiv),this.schedulerMenu);
        //WT.app.setElementContextMenu(Ext.get(this.sdaydiv),this.schedulerMenu);
        // Init common vars
        this.evts=new Array();
        this.maxWidthEvent=new Array();
        
        this.listdd = new Ext.dd.DropTarget(this.hdrlisttdHour,{
          ddGroup : 'sev',
          notifyEnter : function(dd, e, data){
              if (e.ctrlKey)
                    return "x-tree-drop-ok-append";
              return "x-dd-drop-ok";
          },
          notifyOver : function(dd, e, data){
              if (e.ctrlKey)
                  return "x-tree-drop-ok-append";
              return "x-dd-drop-ok";
          },
          notifyDrop : function (source, e, data) {
            target = e.getTarget();
            target = e.getTarget();
            target.schedulerDay.sdparent.moveEvent(e.ctrlKey,data,target.schedulerDay.sdate);
            return true;
          }
        });
        
        this.listalldd = new Ext.dd.DropTarget(this.hdrtrHour,{
          ddGroup : 'sev',
          notifyEnter : function(dd, e, data){
              if (e.ctrlKey)
                return "x-tree-drop-ok-append";
              return "x-dd-drop-ok";
          },
          notifyOver : function(dd, e, data){
              if (e.ctrlKey)
                  return "x-tree-drop-ok-append";
              return "x-dd-drop-ok";
          },
          notifyDrop : function (source, e, data) {      
            target = e.getTarget();
            target.schedulerDay.sdparent.moveEvent(e.ctrlKey,data,target.schedulerDay.sdate);
            return true;
          }
        });
        
    },
	
    actionNew : function () {
      this.sdparent.newEvent(this.getDate(),new Date(),new Date());
    },

    actionPrint : function () {
      this.sdparent.printEvents();
    },

    actionToday : function () {
      this.sdparent.cs.setView("t");
    },

    mouseClick : function(e) {
        if (e.getTarget()!=this.slistdiv) return;
        this.sdparent.deselectAllSchedulerEvents();
    },
    
    mouseDayClick : function(e) {
        this.sdparent.deselectAllSchedulerEvents();
    },
    
    deselectAllSchedulerEvents : function() {
        var len = this.evts.length;
        for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            if (this.viewFlag=="d" || this.viewFlag=="w5")
              sevent.selectEvent(sevent.sevText,false);
            else
              sevent.selectEvent(sevent.listsevText,false);
        }
    },

    getSelectedSchedulerEvents : function() {
        var note_ids=[];
        var len = this.evts.length;
        for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            if (sevent.isSelected) {
                note_ids[note_ids.length] = sevent.event_id;
                WT.debugObject(sevent);
            }
        }
        return note_ids;
    },
    
    getSelectedSchedulerObjectEvents : function() {
        var note_ids=[];
        var len = this.evts.length;
        for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            if (sevent.isSelected) {
                note_ids[note_ids.length] = sevent;
                WT.debugObject(sevent);
            }
        }
        return note_ids;
    },
    
    removeAllSchedulerEvent : function() {
        var len = this.evts.length;
        for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            try {
                this.sdiv.removeChild(sevent.getRootElement());
                
            } catch (err) {}
            try {
                this.slistdiv.removeChild(sevent.getRootListElement());
            } catch (err) {}
            
            try {
                this.sdaydiv.removeChild(sevent.getRootDayElement());
            } catch (err) {}
        }
        this.evts=new Array();
    },

    addSchedulerEvent : function(sevent) {
        this.evts[this.evts.length]=sevent;
        try {
            this.sdiv.appendChild(sevent.getRootElement());
            
        } catch (err) {}
        try {
            this.slistdiv.appendChild(sevent.getRootListElement());
            
        } catch (err) {}
        
        try {
            this.sdaydiv.appendChild(sevent.getRootDayElement());
            
        } catch (err) {}
        this.evts.sort(this.sortEvents);
    },


    sortEvents : function(ev1,ev2) {
        if (ev1.getStartHour()<ev2.getStartHour() || 
            (ev1.getStartHour()==ev2.getStartHour() && ev1.getStartMinute()<ev2.getStartMinute()))
            return -1;
        else if (ev2.getStartHour()<ev1.getStartHour() || 
            (ev2.getStartHour()==ev1.getStartHour() && ev2.getStartMinute()<ev1.getStartMinute()))
            return 1;
        else if (ev1.getEndHour()>ev2.getEndHour() || 
            (ev1.getEndHour()==ev2.getEndHour() && ev1.getEndMinute()>=ev2.getEndMinute()))
            return -1;
        else 
            return 1;
        return 0;
    },

    doListLayout : function(viewFlag,w,h) {
        this.viewFlag = viewFlag;
        var len = this.evts.length;
        for (var i=0;i<len;i++) {
            try {
                this.slistdiv.removeChild(this.evts[i].getRootListElement());
            } catch (err) {}
        }
        for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            sevent.viewFlag = viewFlag;
            try {
                this.slistdiv.appendChild(sevent.getRootListElement());
                
            }catch (err) {}
            
            // Top e Heigth
            var height=sevent.getDefaultListHeight();
            var top=i;

            // Move events
            sevent.moveListEvent(top,0,height,"99.6%");
        }
    },
        
    doLayout : function(viewFlag,w,h) {
        this.viewFlag = viewFlag;
        this.sdiv.style.width=w;
        var swidth=w-8;

        // Layout Events
        for (var i=0; i<48; i++) {
            this.maxWidthEvent[i]=0;
        }
        var len = this.evts.length;
        for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            if (sevent.allday=="false"){
                sevent.viewFlag = viewFlag;
                sevent.seIndex = this.seIndex;
                sevent.moveEvent(0,0,0,swidth);
                for (var j=sevent.ft;j<(sevent.ft+sevent.rowspan);j++) {
                  this.maxWidthEvent[j]++;
                }
            }
        }
        for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            if (sevent.allday=="false"){
                // Top e Heigth
                var top=sevent.ft*sevent.getDefaultHeight();
                var height=(sevent.rowspan*sevent.getDefaultHeight())-2;

                // Left e Width
                var width = Math.floor(swidth/this.getMaxConcurrentEvent(sevent))-1;
                var left=this.getFirstFreeLeftEvent(sevent,top,top+height,width,swidth);

                // Move events
                sevent.moveEvent(top,left,height,width);
            }
        }
    },
    
    
    doListDayLayout : function(viewFlag) {
       var len = this.evts.length;
       
       for (var i=0;i<len;i++) {
            try {
                this.sdaydiv.removeChild(this.evts[i].getRootDayElement());
            } catch (err) {}
        }
       
       for (var i=0;i<len;i++) {
            var sevent=this.evts[i];
            sevent.viewFlag = viewFlag;

            try {
                if (sevent.allday == "true")    
                    this.sdaydiv.appendChild(sevent.getRootDayElement());
            
            }catch (err) {}
            
            var height=sevent.getDefaultListHeight();
            var top=i;

            // Move events
            sevent.moveListDayEvent(top,0,height,"98%");
            
            //sevent.moveEvent(20,20,30,40);
       }
    },
    
    
    getMaxConcurrentEvent : function(sevent) {
        var mce=1;
        var fmce = this.getMaxForwardConcurrentEvent(sevent,0);
        var bmce = this.getMaxBackwardConcurrentEvent(sevent,this.evts.length-1);
        var mce = fmce;
        if (mce<bmce) mce=bmce;
        return mce;
    },

    getFirstFreeLeftEvent : function(sevent,se_top,se_bottom,se_width,width) {
        var top=0;
        var width=0;
        var bottom=0;
        var left=0;
        var se_left = 0;
        var len = this.evts.length;
        for (var i=0;i<len;i++) {
            if (this.evts[i]==sevent) continue;
            top=this.evts[i].localTop;
            width=this.evts[i].localWidth;
            bottom=top+this.evts[i].localHeight;
            left=this.evts[i].localLeft;
            if ((se_top>=top && se_top<=bottom && left==se_left) || (se_bottom>=top && se_bottom<=bottom && left==se_left)) {
                se_left = left+se_width+2;
                i = 0;
            }
        }
        return se_left;
    },

    getMaxForwardConcurrentEvent : function(sevent,i) {
        var ft=0;
        var fm=0;
        var tt=0;
        var tm=0;
        var mce = this.getConcurrentEvent(sevent);
        var tmpMce = mce;
        var se_ft=sevent.getStartHour();
        var se_fm=sevent.getStartMinute();
        var se_tt=sevent.getEndHour();
        var se_tm=sevent.getEndMinute();
        var len = this.evts.length;
        for (;i<len;i++) {
            if (this.evts[i]==sevent || this.evts[i].allday=="true") continue;
            ft=this.evts[i].getStartHour();
            fm=this.evts[i].getStartMinute();
            tt=this.evts[i].getEndHour();
            tm=this.evts[i].getEndMinute();
            if ( (se_ft<tt || (se_ft==tt && se_fm<tm)) && (se_tt>tt || (se_tt==tt && se_tm>=tm)) )
                tmpMce = this.getMaxForwardConcurrentEvent(this.evts[i],i++);
            else if ( (se_tt>ft || (se_tt==ft && se_tm>fm)) && (se_ft<ft || (se_ft==ft && se_fm<=fm)) )
                tmpMce = this.getMaxForwardConcurrentEvent(this.evts[i],i++);
            else if ( (se_ft>ft || (se_ft==ft && se_fm>=fm)) && (se_tt<tt || (se_tt==tt && se_tm<=tm)) )
                tmpMce = this.getMaxForwardConcurrentEvent(this.evts[i],i++);
            if (tmpMce>mce) mce=tmpMce;
        }
        return mce;
    },

    getMaxBackwardConcurrentEvent : function(sevent,i) {
        var ft=0;
        var fm=0;
        var tt=0;
        var tm=0;
        var mce = this.getConcurrentEvent(sevent);
        var tmpMce = mce;
        var se_ft=sevent.getStartHour();
        var se_fm=sevent.getStartMinute();
        var se_tt=sevent.getEndHour();
        var se_tm=sevent.getEndMinute();
        var len = this.evts.length;
        for (;i>=0;i--) {
            if (this.evts[i]==sevent || this.evts[i].allday=="true") continue;
            ft=this.evts[i].getStartHour();
            fm=this.evts[i].getStartMinute();
            tt=this.evts[i].getEndHour();
            tm=this.evts[i].getEndMinute();
            if ( (se_ft<tt || (se_ft==tt && se_fm<tm)) && (se_tt>tt || (se_tt==tt && se_tm>=tm)) )
                tmpMce = this.getMaxBackwardConcurrentEvent(this.evts[i],i--);
            else if ( (se_tt>ft || (se_tt==ft && se_tm>fm)) && (se_ft<ft || (se_ft==ft && se_fm<=fm)) )
                tmpMce = this.getMaxBackwardConcurrentEvent(this.evts[i],i--);
            else if ( (se_ft>ft || (se_ft==ft && se_fm>=fm)) && (se_tt<tt || (se_tt==tt && se_tm<=tm)) )
                tmpMce = this.getMaxBackwardConcurrentEvent(this.evts[i],i--);
            if (tmpMce>mce) mce=tmpMce;
        }
        return mce;
    },

    getConcurrentEvent : function(sevent) {
        var ce=1;
        for (var j=sevent.ft;j<(sevent.ft+sevent.rowspan);j++) {
          if (ce<this.maxWidthEvent[j]) ce=this.maxWidthEvent[j];
        }
        return ce;
    },
    
    setIndex : function(i) {
      this.seIndex=i;
    },
    
    setDate : function(d) {
        this.sdate = d;
        this.hdrtdHour.sdate = d;
        this.hdrlisttdHour.sdate = d;
        this.setTitle(this.dayNames[d.getDay()]+" "+d.getDate()+" "+this.monthNames[d.getMonth()]);
        this.setListTitle(this.dayNames[d.getDay()].substr(0,3)+" "+d.getDate()+" "+this.monthNames[d.getMonth()]);
    },
    
    getDate : function() {
        if (this.sdate==null)
            return new Date();
        return this.sdate;
    },
    
    setTitle : function(t) {
        this.hdrtdHour.innerHTML=t;
    },
    
    setListTitle : function(t) {
        this.hdrlisttdHour.innerHTML=t;
    },
    
    getHeaderListDay : function() {
        return this.hdrlistdiv;
    },
    
    getSchedulerListDay : function() {
        return this.slistdiv;
    },
    
    getHeaderDay : function() {
        return this.hdrdiv;
    },
    
    getTitleDay : function() {
        return this.hdrtdHour;
    },
    
    getSchedulerDay : function() {
        return this.sdiv;
    },
    
    getSchedulerAllDay : function() {
        return this.sdaydiv;
    }
    
});

Ext.define('Sonicle.webtop.calendar.SchedulerEvent', {
    extend: 'Ext.container.Container',
    separent : null,
    seIndex : null,
    event_id : -1,
    event_date : null,
    event_startdate : null,
    event_enddate : null,
    event : "",
    hstart : 9,
    mstart : 0,
    hend : 10,
    mend : 0,
    hasDescription : false,
    readonly : false,
    planning : false,
    recurrence : false,
    reminder : false,
    viewFlag : "d",
    editEvent: false,
    isSelected : false,
    calendar: "WebTop",
    colorcalendar: "#FFFFFF",
    allday: 'false',
    timezone:null,
    fromRecurrence:false,
    visit:null,
    activity:null,
    report_id:null,
    customer_description:null,
    recurr_id:null,
    
    initComponent : function(){
        this.callParent(arguments);
        var cs=this.cs=this.separent.cs;
        this.sevTop = document.createElement("DIV");
        this.sevTop.className="schedulerEventLine";
        this.sevLeft = document.createElement("DIV");
        this.sevLeft.className="schedulerEventLineLeft";
        this.sevBottom = document.createElement("DIV");
        this.sevBottom.className="schedulerEventLine";
        this.sevText = document.createElement("DIV");
        this.sevText.className="schedulerEventText";
        //this.sevText.style.background=this.colorcalendar;
        this.sevTextArea=document.createElement("TEXTAREA");
        this.sevTextArea.className="schedulerEventText";
        this.sevTextArea.style.visibility="hidden";
        this.rel = document.createElement("DIV");
        this.rel.className="schedulerEvent";
        this.rel.style.background=this.colorcalendar;
        this.sevTop.style.zIndex=12;
        this.sevLeft.style.zIndex=13;
        this.sevBottom.style.zIndex=12;
        this.sevText.style.zIndex=11;
        this.sevTextArea.style.zIndex=10;
        this.rel.style.zIndex=10;
        this.rel.style.top=0;
        this.rel.style.height=0;
        this.rel.style.left=0;
        this.rel.style.width=0;
        this.rel.appendChild(this.sevTop);
        this.rel.appendChild(this.sevLeft);
        this.rel.appendChild(this.sevText);
        this.rel.appendChild(this.sevTextArea);
        this.rel.appendChild(this.sevBottom);

        this.listsevText = document.createElement("DIV");
        this.listsevText.className="schedulerListEventText";
        this.listsevTextArea=document.createElement("TEXTAREA");
        this.listsevTextArea.className="schedulerListEventTextArea";
        this.listsevTextArea.style.visibility="hidden";
        this.listrel = document.createElement("DIV");
        this.listrel.className="schedulerListEvent";
        this.listrel.style.background=this.colorcalendar;
        this.listsevText.style.zIndex=11;
        this.listsevTextArea.style.zIndex=10;
        this.listrel.style.zIndex=10;
        this.listrel.style.top=0;
        this.listrel.style.height=0;
        this.listrel.style.left=0;
        this.listrel.style.width=0;
        this.listrel.appendChild(this.listsevText);
        this.listrel.appendChild(this.listsevTextArea);
       
       
       
       
        this.listdaysevText = document.createElement("DIV");
        this.listdaysevText.className="schedulerListDayEventText";
        this.listdaysevTextArea=document.createElement("TEXTAREA");
        this.listdaysevTextArea.className="schedulerListDayEventTextArea";
        this.listdaysevTextArea.style.visibility="hidden";
        this.listdayrel = document.createElement("DIV");
        this.listdayrel.className="schedulerListDayEvent";
        this.listdayrel.style.background=this.colorcalendar;
        this.listdaysevText.style.zIndex=11;
        this.listdaysevTextArea.style.zIndex=10;
        this.listdayrel.style.zIndex=10;
        this.listdayrel.style.top=0;
        this.listdayrel.style.height=0;
        this.listdayrel.style.left=0;
        this.listdayrel.style.width=0;
        
        
        this.listdayrel.appendChild(this.listdaysevText);
        this.listdayrel.appendChild(this.listdaysevTextArea);       
        
        
        
        
        this.ttip = new Ext.ToolTip({
            target: this.rel,
            trackMouse:true,
            dismissDelay: 3000
        });
        this.listttip = new Ext.ToolTip({
            target:this.listrel,
            trackMouse:true,
            dismissDelay: 3000
        });
        
        /*Menu copy in calendar*/
        this.menuPersonalCalendar=new Ext.menu.Menu({
            items: []
        });
        
        this.menuCalendarScheduler=new Ext.menu.Item({
                text: WT.res("calendar","personal"),
                menu: this.menuPersonalCalendar
        }); 
        
        for(var i=0;i<this.cs.personalCalendarMenu.length;i++){
             var disabled=false;
             if (this.calendar==this.cs.personalCalendarMenu[i]) disabled=true;
             var actionCalendar=new Ext.Action({            
                       text:this.cs.personalCalendarMenu[i],
                       calendar:this.cs.personalCalendarMenu[i],
                       scope:this,
                       disabled:disabled,
                       iconCls:"icon-color"+this.cs.personalCalendarMenuColor[i].replace('#',''),
                       handler:function(b,t){
                           this.actionCopyInCalendar(b.calendar);
                       }
             });
             this.menuPersonalCalendar.addItem(actionCalendar);
        }
        /*end Menu copy in calendar*/
        
        this.schedulerMenu=new Ext.menu.Menu({
            items: [
                new Ext.Action({
                    scope : this,
                    text: cs.res("open"),
                    disabled: this.separent.groupacl!='w',
                    handler: function(){
                        this.actionOpen();
                    }
                }),
                new Ext.Action({
                    scope : this,
                    iconCls : 'iconDeleteEvent',
                    text: cs.res("delete"),
                    disabled: this.separent.groupacl!='w',
                    handler: function(){
                        this.actionDelete();
                    }
                }),
                new Ext.Action ({
                    scope:this,
                    iconCls: 'iconRestoreRecurrence',
                    text: cs.res("restorerecurrence"),
                    disabled:!this.fromRecurrence,
                    handler: function(){
                        this.actionRestoreRecurrence();
                    }
                        
                }),
                this.menuCalendarScheduler,
                
                new Ext.Action({
                    scope : this,
                    iconCls : 'icon-addvisit',
                    text: cs.res("action-new_drm_visit"),
                    disabled:this.visit,
                    hidden:this.isdrm,
                    handler: function(){
                        this.actionNewDrmVisit();
                    }
                }),
                new Ext.Action({
                    scope : this,
                    iconCls : 'icon-openvisit',
                    text: cs.res("action-open_drm_visit"),
                    disabled:!this.visit,
                    hidden:!this.separent.isdrm,
                    handler: function(){
                        this.actionOpenDrmVisit();
                    }
                }),
                '-',
                new Ext.Action({
                    scope : this,
                    iconCls : 'icon-calendar-action-new',
                    text: cs.res("action-new"),
                    handler: function(){
                        this.actionNew();
                    }
                }),
                
                new Ext.Action({
                    scope : this,
                    iconCls : 'iconPrint',
                    text: cs.res("print"),
                    handler: function(){
                        this.actionPrint();
                    }
                }),
                new Ext.Action({
                    scope : this,
                    text: cs.res("today"),
                    handler: function(){
                        this.actionToday();
                    }
                })

            ]
        });
        //WT.app.setElementContextMenu(Ext.get(this.rel),this.schedulerMenu);
        //WT.app.setElementContextMenu(Ext.get(this.listrel),this.schedulerMenu);
        //WT.app.setElementContextMenu(Ext.get(this.listdayrel),this.schedulerMenu);
        Ext.get(this.rel).on('click',this.mouseUp,this);
        Ext.get(this.rel).on('dblclick',this.editEventByDblClick,this);
        Ext.get(this.listrel).on('click',this.mouseUpList,this);
        Ext.get(this.listrel).on('dblclick',this.editEventByDblClick,this);
        Ext.get(this.sevTextArea).on('keydown',this.keyDown,this);
        Ext.get(this.listsevTextArea).on('keydown',this.keyDownList,this);
        Ext.get(this.sevTextArea).on('blur',this.focusOut,this);
        Ext.get(this.listsevTextArea).on('blur',this.focusOutList,this);
        
        Ext.get(this.listdayrel).on('click',this.mouseUpDayList,this);
        Ext.get(this.listdayrel).on('dblclick',this.editEventByDblClick,this);
        Ext.get(this.listdaysevTextArea).on('keydown',this.keyDownDayList,this);
        Ext.get(this.listdaysevTextArea).on('blur',this.focusOutDayList,this);
        
        
        this.localTop=0;
        this.localLeft=0;
        this.localHeight=0;
        this.localWidth=0;

        this.imgReminder=document.createElement("img");
        this.imgReminder.src="webtop/themes/"+WT.theme+"/calendar/reminder.gif";
        this.imgPlanning=document.createElement("img");
        this.imgPlanning.src="webtop/themes/"+WT.theme+"/calendar/planning.gif";
        this.imgRecurrence=document.createElement("img");
        this.imgRecurrence.src="webtop/themes/"+WT.theme+"/calendar/recurrence.gif";
        this.imgRecurrenceBroken=document.createElement("img");
        this.imgRecurrenceBroken.src="webtop/themes/"+WT.theme+"/calendar/recurrence_broken.png";
        this.imgMultidate=document.createElement("img");
        this.imgMultidate.src="webtop/themes/"+WT.theme+"/calendar/multidate.png";
        this.imgTimezone=document.createElement("img");
        this.imgTimezone.src="webtop/themes/"+WT.theme+"/calendar/world.png";
        this.imgVisit=document.createElement("img");
        this.imgVisit.src="webtop/themes/"+WT.theme+"/calendar/visit.png";
        this.imgListReminder=document.createElement("img");
        this.imgListReminder.src="webtop/themes/"+WT.theme+"/calendar/reminder.gif";
        this.imgListReminderDay=document.createElement("img");
        this.imgListReminderDay.src="webtop/themes/"+WT.theme+"/calendar/reminder.gif";
        this.imgListPlanning=document.createElement("img");
        this.imgListPlanning.src="webtop/themes/"+WT.theme+"/calendar/planning.gif";
        this.imgListPlanningDay=document.createElement("img");
        this.imgListPlanningDay.src="webtop/themes/"+WT.theme+"/calendar/planning.gif";
        this.imgListRecurrence=document.createElement("img");
        this.imgListRecurrence.src="webtop/themes/"+WT.theme+"/calendar/recurrence.gif";
        this.imgListRecurrenceBroken=document.createElement("img");
        this.imgListRecurrenceBroken.src="webtop/themes/"+WT.theme+"/calendar/recurrence_broken.png";
        this.imgListRecurrenceBrokenDay=document.createElement("img");
        this.imgListRecurrenceBrokenDay.src="webtop/themes/"+WT.theme+"/calendar/recurrence_broken.png";
        this.imgListRecurrenceDay=document.createElement("img");
        this.imgListRecurrenceDay.src="webtop/themes/"+WT.theme+"/calendar/recurrence.gif";
        this.imgListMultidate=document.createElement("img");
        this.imgListMultidate.src="webtop/themes/"+WT.theme+"/calendar/multidate.png";
        this.imgListMultidateDay=document.createElement("img");
        this.imgListMultidateDay.src="webtop/themes/"+WT.theme+"/calendar/multidate.png";
        this.imgListTimezone=document.createElement("img");
        this.imgListTimezone.src="webtop/themes/"+WT.theme+"/calendar/world.png";
        this.imgListTimezoneDay=document.createElement("img");
        this.imgListTimezoneDay.src="webtop/themes/"+WT.theme+"/calendar/world.png";
        this.imgListVisit=document.createElement("img");
        this.imgListVisit.src="webtop/themes/"+WT.theme+"/calendar/visit.png";
        
        
        this.setSpan();
        this.resetEvent();

        
    },

    
    manageDragZone : function (isList) {
      if (this.dd==null) {
        this.ddel = document.createElement('div');
        if (isList) {
          this.dd = new Ext.dd.DragSource(Ext.get(this.listsevText),{
            ddGroup : 'sev', 
            editEvent : false,
            dragged:false,
            se : this,
            onInitDrag : function(e){
              this.se.ddel.innerHTML = this.se.event;
              this.proxy.update(this.se.ddel);
            },
            onBeforeDrag : function (data,e) {
              if (this.se.separent.groupacl!='w') return false;

              this.editEvent=false;
              if (this.se.readonly || this.se.planning || this.se.recurrence) return false;
              this.ourInterval=new Ext.util.DelayedTask(this.setEditEvent,this);
              this.ourInterval.delay(250);
              this.dragged=false;
              return true;
            },
            setEditEvent : function() {
              this.editEvent=true;
              this.ourInterval.cancel();
            },
            onDrag : function() {
              this.editEvent=false;
              if (!this.dragged) {
                this.dragged=true;
                this.se.separent.deselectAllSchedulerEvents();
              }
            },
            endDrag : function (e) {
                this.se.separent.deselectAllSchedulerEvents();
            },
            getDragData : function (e) {
              return this.se.event_id;
            }
          });
        } else {
          new Ext.dd.DragSource(Ext.get(this.sevTop), {
              se : this,
              getRepairXY : function(e, data){
                return false;
              },
              onMouseDown : function (ev) {
                this.se.mouseStartMove(ev);
              },
              onBeforeDrag : function (data,e) {
                if (this.se.separent.groupacl!='w') return false;
                if (this.se.recurrence) return false; // no drag recurrence
                return true;
              },
              onDrag : function(ev) {
                this.proxy.hide();
                this.se.mouseMove(ev);
              },
              onMouseUp : function(ev) {
                  this.se.mouseEndMove(ev);
              }
          });
          new Ext.dd.DragSource(Ext.get(this.sevBottom), {
              se : this,
              getRepairXY : function(e, data){
                return false;
              },
              onMouseDown : function (ev) {
                this.se.mouseStartMove(ev);
              },
              onBeforeDrag : function (data,e) {
                                    WT.debug("group="+this.se.separent.groupacl);
                if (this.se.separent.groupacl!='w') return false;
                if (this.se.recurrence) return false; // no drag recurrence
                return true;
              },
              onDrag : function(ev) {
                this.proxy.hide();
                this.se.mouseMove(ev);
              },
              onMouseUp : function(ev) {
                  this.se.mouseEndMove(ev);
              }
          });
          new Ext.dd.DragSource(Ext.get(this.sevLeft), {
              ddGroup : 'sev',
              se : this,
              getDragData : function (e) {
                return this.se.event_id;
              },
              getRepairXY : function(e, data){
                return false;
              },
              onInitDrag : function(e){
                this.se.ddel.innerHTML = this.se.event;
                this.proxy.update(this.se.ddel);
              },
              onMouseDown : function (ev) {
                this.localTop = this.se.localTop;
                this.localLeft = this.se.localLeft;
                this.se.mouseStartMove(ev);
              },
              onBeforeDrag : function (data,e) {
                if (this.se.separent.groupacl!='w') return false;
                if (this.se.readonly ) return false;
                if (this.se.recurrence) return false;   // no drag recurrence
                this.dragged=false;
                return true;
              },
              onDrag : function(ev) {
                if (!this.dragged) {
                  this.dragged=true;
                  this.se.separent.deselectAllSchedulerEvents();
                }
                var clientX = this.se.documentXToElementX(document,ev.getPageX(),this.se.rel.offsetParent)-this.se.layerX;
                if (clientX<this.se.getMinX()) {
                  if (!this.dragging) {
                    this.proxy.show();
                    this.dragging = true;
                    this.se.moveEvent(this.localTop,this.localLeft,this.se.localHeight,this.se.localWidth);
                    this.se.eventMoveReset(true);
                  }
                } else {
                  if (this.dragging) {
                    this.proxy.hide();  
                    this.dragging = false;
                    this.se.eventMove();
                  }
                  this.se.mouseMove(ev);
                }
              },
              onMouseUp : function(ev) {
                var clientX = this.se.documentXToElementX(document,ev.getPageX(),this.se.rel.offsetParent)-this.se.layerX;
                if (clientX>=this.se.getMinX()) this.se.mouseEndMove(ev);
              },
              endDrag : function (e) {
                this.se.separent.deselectAllSchedulerEvents();
              }
          });
          if (!Ext.isGecko) {
            this.dd = new Ext.dd.DragSource(Ext.get(this.sevText),{
              ddGroup : 'sev', 
              se : this,
              editEvent : false,
              dragged:false,
              onBeforeDrag : function (data,e) {
                if (this.se.separent.groupacl!='w') return false;
                this.editEvent=false;
                this.ourInterval=new Ext.util.DelayedTask(this.setEditEvent,this);
                this.ourInterval.delay(250);
                return false;
              },
              setEditEvent : function() {
                this.editEvent=true;
                this.ourInterval.cancel();
              }
            });
          } else {
            this.dd = new Ext.dd.DragSource(Ext.get(this.sevText),{
              ddGroup : 'sev',
              se : this,
              editEvent : false,
              dragged:false,
              getDragData : function (e) {
                return this.se.event_id;
              },
              getRepairXY : function(e, data){
                return null;
              },
              onInitDrag : function(e){
                this.se.ddel.innerHTML = this.se.event;
                this.proxy.update(this.se.ddel);
              },
              onMouseDown : function (ev) {
                this.localTop = this.se.localTop;
                this.localLeft = this.se.localLeft;
                this.se.mouseStartMove(ev);
              },
              onBeforeDrag : function (data,e) {
                if (this.se.separent.groupacl!='w') return false;
                this.editEvent=false;
                if (this.se.readonly || this.se.planning || this.se.recurrence) return false;
                this.ourInterval=new Ext.util.DelayedTask(this.setEditEvent,this);
                this.ourInterval.delay(250);
                this.dragged=false;
                return true;
              },
              setEditEvent : function() {
                this.editEvent=true;
                this.ourInterval.cancel();
              },
              onDrag : function(ev) {
                this.editEvent=false;
                if (!this.dragged) {
                  this.dragged=true;
                  this.se.separent.deselectAllSchedulerEvents();
                }
                var clientX = this.se.documentXToElementX(document,ev.getPageX(),this.se.rel.offsetParent)-this.se.layerX;
                if (clientX<this.se.getMinX()) {
                  if (!this.dragging) {
                    this.proxy.show();
                    this.dragging = true;
                    this.se.moveEvent(this.localTop,this.localLeft,this.se.localHeight,this.se.localWidth);
                    this.se.eventMoveReset(true);
                  }
                } else {
                  if (this.dragging) {
                    this.proxy.hide();  
                    this.dragging = false;
                    this.se.eventMove();
                  }
                  this.se.mouseMove(ev);
                }
              },
              onMouseUp : function(ev) {
                var clientX = this.se.documentXToElementX(document,ev.getPageX(),this.se.rel.offsetParent)-this.se.layerX;
                if (clientX>=this.se.getMinX()) this.se.mouseEndMove(ev);
              },
              endDrag : function (e) {
                this.se.separent.deselectAllSchedulerEvents();
              }
            });
          }
        }
      }
    },
    
    
    manageDragDayZone : function () {
      if (this.dd==null) {
        this.ddel = document.createElement('div');
          this.dd = new Ext.dd.DragSource(Ext.get(this.listdaysevText),{
            ddGroup : 'sev', 
            editEvent : false,
            dragged:false,
            se : this,
            onInitDrag : function(e){
              this.se.ddel.innerHTML = this.se.event;
              this.proxy.update(this.se.ddel);
            },
            onBeforeDrag : function (data,e) {
              if (this.se.separent.groupacl!='w') return false;

              this.editEvent=false;
              if (this.se.readonly || this.se.planning || this.se.recurrence ) return false;
              this.ourInterval=new Ext.util.DelayedTask(this.setEditEvent,this);
              this.ourInterval.delay(250);
              this.dragged=false;
              return true;
            },
            setEditEvent : function() {
              this.editEvent=true;
              this.ourInterval.cancel();
            },
            onDrag : function() {
              this.editEvent=false;
              if (!this.dragged) {
                this.dragged=true;
                this.se.separent.deselectAllSchedulerEvents();
              }
            },
            endDrag : function (e) {
                this.se.separent.deselectAllSchedulerEvents();
            },
            getDragData : function (e) {
              return this.se.event_id;
            }
          });
        }
       
    },
        
    
    
    
    documentYToElementY : function(document,y,obj) {
      while(obj.offsetParent && obj.offsetParent!=document) {
        y-=obj.offsetTop-obj.scrollTop;
        obj=obj.offsetParent;
      }
      return y;
    },
    
    documentXToElementX : function(document,x,obj) {
      while(obj.offsetParent && obj.offsetParent!=document) {
        x-=obj.offsetTop-obj.scrollTop;
        obj=obj.offsetParent;
      }
      return x;
    },
    
    eventMove : function() {
      this.sevTop.className="schedulerEventLineDrag";
      this.sevBottom.className="schedulerEventLineDrag";
      this.rel.style.zIndex=100;
      this.sevTextArea.style.zIndex=100;
      this.sevText.style.zIndex=101;
      this.sevTop.style.zIndex=102;
      this.sevBottom.style.zIndex=102;
      this.sevLeft.style.zIndex=103;
    },

    eventMoveReset : function(isDragDrop) {
      this.sevTop.className="schedulerEventLine";
      this.sevBottom.className="schedulerEventLine";
      this.rel.style.zIndex=10;
      this.sevTextArea.style.zIndex=10;
      this.sevText.style.zIndex=11;
      this.sevTop.style.zIndex=12;
      this.sevBottom.style.zIndex=12;
      this.sevLeft.style.zIndex=13;
      if (!isDragDrop) {
        this.dragevent=null;
        this.setSpan();
      }
    },

    mouseStartMove : function(ev){
        if (this.readonly) return false;
        var src=ev.getTarget();
        if (src==this.sevTop) {
            this.dragevent=this.sevTop;
            this.layerY=0;
            this.layerX=0;
            this.eventMove();
        } else if (src==this.sevBottom) {
            this.dragevent=this.sevBottom;
            this.layerY=0;
            this.layerX=0;
            this.eventMove();
        } else if (src==this.sevLeft || src==this.sevText) {
            this.dragevent=this.sevLeft;
            this.layerY=this.documentYToElementY(document,ev.getPageY(),this.rel.offsetParent)-this.localTop-1;
            if (src==this.sevLeft) 
              this.layerX=this.documentXToElementX(document,ev.getPageX(),this.rel.offsetParent)-this.localLeft-1;
            else
              this.layerX=this.documentXToElementX(document,this.getX(this.sevText),this.rel.offsetParent)-this.localLeft-1;
            if (this.separent.sdiv[this.seIndex]) this.separent.sdiv[this.seIndex].style.zIndex=1;
            this.eventMove();
        }
        return true;
    },
    
    getX : function ( oElement ) {
      var iReturnValue = 0;
      while( oElement != null ) {
        iReturnValue += oElement.offsetLeft;
        oElement = oElement.offsetParent;
      }
      return iReturnValue;
    },
    

    mouseMove : function(ev) {
        if (ev==null) retunr;
        var clientY = this.documentYToElementY(document,ev.getPageY(),this.rel.offsetParent)-this.layerY;
        var clientX = this.documentXToElementX(document,ev.getPageX(),this.rel.offsetParent)-this.layerX;
        
        if (this.dragevent==this.sevTop) {
            var nHeight = (this.localTop+this.localHeight)-clientY;
            var mod=nHeight%this.getDefaultHeight();
            if (clientY>=5 && nHeight>=this.getDefaultHeight()) 
                this.moveEvent(clientY+mod+2,this.localLeft,nHeight-mod-2                       ,this.localWidth);
            else if (this.localTop!=0 && clientY<5 && nHeight>=this.getDefaultHeight()) 
                this.moveEvent(0        ,this.localLeft,this.localHeight+this.getDefaultHeight(),this.localWidth);
        } else if (this.dragevent==this.sevBottom) {
            var nHeight = clientY-this.localTop;
            var mod=nHeight%this.getDefaultHeight();
            if (clientY<=((this.getDefaultSchedulerHeight())-5) && nHeight>=this.getDefaultHeight()) 
                this.moveEvent(this.localTop,this.localLeft,nHeight-mod-2                                     ,this.localWidth);
            else if (clientY>(this.getDefaultSchedulerHeight()-5) && nHeight>=this.getDefaultHeight()) 
                this.moveEvent(this.localTop,this.localLeft,(this.getDefaultSchedulerHeight()-this.localTop-2),this.localWidth);
        } else if (this.dragevent==this.sevLeft) {
            if (ev.ctrlKey) 
                this.rel.style.cursor="crosshair";
            else
                this.rel.style.cursor="move";
            var clientBottom = clientY+this.localHeight;
            var clientRight = clientX+this.localWidth;
            var mod=clientY%this.getDefaultHeight();
            var modX=clientX%this.getDefaultWidth();
            var localY=clientY-mod;
            var localX=this.localLeft;
            if (this.separent.viewFlag=="d" || this.separent.viewFlag=="w5") {
              if (clientX>=0)
                localX=clientX-modX;
              else
                localX=clientX-modX-this.getDefaultWidth();
              clientRight = localX+this.localWidth;
            }
            if (clientY>=0 && clientBottom<this.getDefaultSchedulerHeight()) {
              if (clientX>this.getMinX() && clientRight<this.getMaxRight()) {
                this.moveEvent(localY,localX,this.localHeight,this.localWidth);
              }
            }
        }
    },

    mouseEndMove : function(ev){
      this.rel.style.cursor="default";
      this.cloneRel=null;
      var eupdate = false;
      if (this.dragevent==this.sevTop) {
          var hstart=Math.floor(this.localTop/(this.getDefaultHeight()*2));
          var mstart=this.localTop%(this.getDefaultHeight()*2);
          if (mstart!=0) mstart=30;
          if (this.hstart != hstart || this.mstart != mstart)
            eupdate = true;
          this.hstart = hstart;
          this.mstart = mstart;
          this.eventMoveReset(false);
          this.separent.redoLayout();
      } else if (this.dragevent==this.sevBottom) {
          var hend=Math.floor((this.localTop+this.localHeight+2)/(this.getDefaultHeight()*2));
          var mend=(this.localTop+this.localHeight+2)%(this.getDefaultHeight()*2);
          if (mend!=0) mend=30;
          if (this.hend != hend || this.mend != mend)
            eupdate = true;
          this.hend = hend;
          this.mend = mend;
          this.eventMoveReset(false);
          this.separent.redoLayout();
      } else if (this.dragevent==this.sevLeft) {
          var hstart=Math.floor(this.localTop/(this.getDefaultHeight()*2));
          var mstart=this.localTop%(this.getDefaultHeight()*2);
          var hend=Math.floor((this.localTop+this.localHeight+2)/(this.getDefaultHeight()*2));
          var mend=(this.localTop+this.localHeight+2)%(this.getDefaultHeight()*2);
          var dayStep = this.localLeft/this.getDefaultWidth();
          var edate = this.event_date.add("d",dayStep).clearTime();
          if (mstart!=0) mstart=30;
          if (mend!=0) mend=30;
          var layerY=this.documentYToElementY(document,ev.getPageY(),this.rel.offsetParent)-this.localTop-1;
          //if (this.hstart!=hstart || this.mstart!=mstart || this.hend!=hend || this.mend!=mend || this.event_date.getTime()!=edate.getTime())
          if (this.layerY!=layerY || this.event_date.getTime()!=edate.getTime())
            eupdate = true;
          this.layerY=0;
          this.hstart = hstart;
          this.mstart = mstart;
          this.hend = hend;
          this.mend = mend;
          this.event_date = edate;
          this.seIndex += dayStep;
          this.eventMoveReset(false);
          if (!eupdate) {
            this.separent.sdiv[this.seIndex].style.zIndex=0;
            this.separent.redoLayout();
          }
      }
      if (eupdate)
        this.separent.moveEvent(ev.ctrlKey,this.event_id,this.event_date,this.hstart,this.mstart,this.hend,this.mend);
    },

    actionOpen : function () {
      this.editEventByDblClick(null);
    },

    actionDelete : function () {
      if (this.readonly)
          return;
      this.separent.deselectAllSchedulerEvents();
      this.selectEvent(this.sevText,!this.isSelected);
      this.separent.deleteEvents();
      
    },

    actionNew : function () {
      this.separent.newEvent(this.event_date,new Date(),new Date());
    },
    actionNewDrmVisit : function () {
      this.separent.newEventDrmVisit(this.event_id);
    },
    actionOpenDrmVisit : function () {
      this.separent.openEventDrmVisit(this.event_id);
    },
    actionPrint : function () {
      this.separent.printEvents();
    },

    actionToday : function () {
      this.separent.cs.setView("t");
    },
    
    actionRestoreRecurrence : function () {
      this.separent.restoreRec(this.event_id);
    },
    
    actionCopyInCalendar:function(idcalendar){
      this.separent.copyInCalendar(this.event_id,idcalendar);
    },
    
    
    
    
    editEventByDblClick : function(e) {
      if (e!=null) {
          var src=e.getTarget();
          if (src==this.sevTop||src==this.sevLeft||src==this.sevBottom) return false;
      }
      if (this.readonly) return false;
      if (this.event_enddate.getTime()==this.event_startdate.getTime()){       //salvo data giorno per le ricorrenze
          var eventdate_yyyy = this.event_date.getFullYear();
          var eventdate_mm = ""+(this.event_date.getMonth()+1);
          if ((this.event_date.getMonth()+1)<10) eventdate_mm="0"+eventdate_mm;
          var eventdate_dd = ""+this.event_date.getDate();
          if (this.event_date.getDate()<10) eventdate_dd="0"+eventdate_dd;
          this.separent.event_startdate_for_recurrences=new Date(eventdate_yyyy,eventdate_mm-1,eventdate_dd);
          this.separent.event_enddate_for_recurrences=new Date(eventdate_yyyy,eventdate_mm-1,eventdate_dd);
      }else{
          var eventenddate_yyyy = this.event_enddate.getFullYear();
          var eventenddate_mm = ""+(this.event_enddate.getMonth()+1);
          if ((this.event_enddate.getMonth()+1)<10) eventenddate_mm="0"+eventenddate_mm;
          var eventenddate_dd = ""+this.event_enddate.getDate();
          if (this.event_enddate.getDate()<10) eventenddate_dd="0"+eventenddate_dd;
          var eventstartdate_yyyy = this.event_startdate.getFullYear();
          var eventstartdate_mm = ""+(this.event_startdate.getMonth()+1);
          if ((this.event_enddate.getMonth()+1)<10) eventstartdate_mm="0"+eventstartdate_mm;
          var eventstartdate_dd = ""+this.event_startdate.getDate();
          if (this.event_startdate.getDate()<10) eventstartdate_dd="0"+eventstartdate_dd;
          this.separent.event_startdate_for_recurrences=new Date(eventstartdate_yyyy,eventstartdate_mm-1,eventstartdate_dd);
          this.separent.event_enddate_for_recurrences=new Date(eventenddate_yyyy,eventenddate_mm-1,eventenddate_dd);
      }
      
      //this.separent.event_date_for_recurrences=new Date(eventdate_yyyy,eventdate_mm-1,eventdate_dd);
      this.separent.editEvent(this.event_id);
    },
    
    doMouseUp : function(obj){
      this.separent.deselectAllSchedulerEvents();
      this.dd.editEvent=false;
      obj.disabled=false;
      obj.value=this.event;
      obj.style.visibility="visible";
      obj.style.zIndex=100;
      obj.focus();
    },
    
    selectEvent : function(src,select) {
      if (this.dd.dragged) return;
      if (this.readonly) return;
      if (select) {
        this.isSelected=true;
        src.style.background="#DFE8F6";
        //src.style.background=this.colorcalendar;
      } else {
        this.isSelected=false;
        if (this.viewFlag=="d" || this.viewFlag=="w5") {
          //src.style.background="WHITE";
          src.style.background=this.colorcalendar;
        } else {
          //src.style.background="#F3E4B1";
          src.style.background=this.colorcalendar;
        }
      }
      this.separent.selectEvent(select);
    },
    
    mouseUp : function(e){
        var src=e.getTarget();
        if (this.dd.dragged||src==this.sevTop||src==this.sevLeft||src==this.sevBottom) return false;
        if (this.dd.editEvent) {
          this.doMouseUp(this.sevTextArea);
        } else if (e.ctrlKey)
          this.selectEvent(this.sevText,!this.isSelected);
        else {
          this.separent.deselectAllSchedulerEvents();
          this.selectEvent(this.sevText,!this.isSelected);
        }
        return true;
    },
    
    mouseUpList : function(e){
        if (this.dd.editEvent) {
          this.doMouseUp(this.listsevTextArea);
        } else if (e.ctrlKey)
          this.selectEvent(this.listsevText,!this.isSelected);
        else {
          this.separent.deselectAllSchedulerEvents();
          this.selectEvent(this.listsevText,!this.isSelected);
        }
        return true;
    },
    
    mouseUpDayList : function(e){
        var src=e.getTarget();
        if (this.dd.dragged) return false;
        if (this.dd.editEvent) {
          this.doMouseUp(this.listdaysevTextArea);
        }else if (e.ctrlKey)
          this.selectEvent(this.listdaysevText,!this.isSelected);
        else {
          this.separent.deselectAllSchedulerEvents();
          this.selectEvent(this.listdaysevText,!this.isSelected);
        }
        return true;
    },
    
    
    doKeyDown : function(e,obj){
        var unicode = e.getKey();
        if (unicode==13) {
            obj.blur();
            this.separent.changeEvent(this.event_id,obj.value);
            return true;
        } else if (unicode==27) {
            obj.value=this.event;
            obj.blur();
            return true;
        }
    },

    keyDown : function(e){
        this.doKeyDown(e,this.sevTextArea);
    },
         
    keyDownList : function(e){
        this.doKeyDown(e,this.listsevTextArea);
    },
    
    keyDownDayList : function(e){
        this.doKeyDown(e,this.listdaysevTextArea);
    },
    
    
    doFocusOut : function(e,obj){
        this.event=obj.value;
        obj.disabled=true;
        obj.style.zIndex=10;
        obj.style.visibility="hidden";
        this.resetEvent();
    },
         
    focusOut : function(e){
        this.doFocusOut(e,this.sevTextArea);
    },
    
    focusOutList : function(e){
        this.doFocusOut(e,this.listsevTextArea);
        this.separent.redoLayout();
    },
    
    focusOutDayList : function(e){
        this.doFocusOut(e,this.listdaysevTextArea);
        this.separent.redoLayout();
    },
    
    setSpan : function() {
        this.ft=this.hstart;
        this.fm=this.mstart;
        this.tt=this.hend;
        this.tm=this.mend;
        if (this.ft==this.tt && this.fm==this.tm) 
            if (this.tm==59)
                this.fm--;
            else
                this.tm++;
        if (this.fm<30) this.fm=0;
        else this.fm=1;
        this.ft=this.ft*2+this.fm;
        if (this.tm==0) {
          this.tm=1;
          this.tt=(this.tt-1)*2+this.tm;
        } else {
          if (this.tm<=30) this.tm=0;
          else this.tm=1;
          this.tt=this.tt*2+this.tm;
        }
        this.rowspan=this.tt-this.ft+1;
    },

    resetEvent : function() {
        this.sevText.innerHTML="";
        this.listsevText.innerHTML="";
        if (this.readonly) {
            this.sevText.style.color="red";
            this.listsevText.style.color="red";
            this.listdaysevText.style.color="red";
        } else {
            this.sevText.style.color="black";
            this.listsevText.style.color="black";
            this.listdaysevText.style.color="black";
        }
        if (this.reminder) {
            this.sevText.appendChild(this.imgReminder);
            this.sevText.appendChild(document.createTextNode(" "));
            this.listdaysevText.appendChild(this.imgListReminderDay);
            this.listdaysevText.appendChild(document.createTextNode(" "));
            this.listsevText.appendChild(this.imgListReminder);
            this.listsevText.appendChild(document.createTextNode(" "));
        }
        if (this.planning) {
            this.sevText.appendChild(this.imgPlanning);
            this.sevText.appendChild(document.createTextNode(" "));
            this.listdaysevText.appendChild(this.imgListPlanningDay);
            this.listdaysevText.appendChild(document.createTextNode(" "));
            this.listsevText.appendChild(this.imgListPlanning);
            this.listsevText.appendChild(document.createTextNode(" "));
        }
        if (this.recurrence) {
            this.sevText.appendChild(this.imgRecurrence);
            this.sevText.appendChild(document.createTextNode(" "));
            this.listdaysevText.appendChild(this.imgListRecurrenceDay);
            this.listdaysevText.appendChild(document.createTextNode(" "));
            this.listsevText.appendChild(this.imgListRecurrence);
            this.listsevText.appendChild(document.createTextNode(" "));
        }
        
        if (this.timezone){
            this.sevText.appendChild(this.imgTimezone);
            this.sevText.appendChild(document.createTextNode(" "));
            this.listdaysevText.appendChild(this.imgListTimezoneDay);
            this.listdaysevText.appendChild(document.createTextNode(" "));
            this.listsevText.appendChild(this.imgListTimezone);
            this.listsevText.appendChild(document.createTextNode(" "));
        }
        
        if (this.event_startdate.getTime()!=this.event_enddate.getTime()){
            this.sevText.appendChild(this.imgMultidate);
            this.sevText.appendChild(document.createTextNode(" "));
            this.listdaysevText.appendChild(this.imgListMultidateDay);
            this.listdaysevText.appendChild(document.createTextNode(" "));
            this.listsevText.appendChild(this.imgListMultidate);
            this.listsevText.appendChild(document.createTextNode(" "));
        }
        
        if (this.visit){
            this.sevText.appendChild(this.imgVisit);
            this.sevText.appendChild(document.createTextNode(" "));
            this.listdaysevText.appendChild(this.imgListVisit);
            this.listdaysevText.appendChild(document.createTextNode(" "));
            this.listsevText.appendChild(this.imgListVisit);
            this.listsevText.appendChild(document.createTextNode(" "));
        }
        
        if (this.fromRecurrence && !this.recurrence) {
            WT.debug("fromRecurrence="+this.fromRecurrence);
            this.sevText.appendChild(this.imgRecurrenceBroken);
            this.sevText.appendChild(document.createTextNode(" "));
            this.listdaysevText.appendChild(this.imgListRecurrenceBrokenDay);
            this.listdaysevText.appendChild(document.createTextNode(" "));
            this.listsevText.appendChild(this.imgListRecurrenceBroken);
            this.listsevText.appendChild(document.createTextNode(" "));
        }
        
        this.sevText.appendChild(document.createTextNode(this.event));
        this.listsevText.appendChild(document.createTextNode(this.getStartHour()+":"+this.getStartMinute()+" "+this.getEndHour()+":"+this.getEndMinute()+" "+this.event));
        this.listdaysevText.appendChild(document.createTextNode(this.event));

        if (this.hasDescription) {
            this.sevText.appendChild(document.createTextNode(" (...)"));
            this.listsevText.appendChild(document.createTextNode(" (...)"));
            this.listdaysevText.appendChild(document.createTextNode(" (...)"));
        }
        if (this.activity!=null && this.activity!="") {
            this.sevText.appendChild(document.createTextNode(" ("+this.activity+")"));
            this.listsevText.appendChild(document.createTextNode(" ("+this.activity+")"));
            this.listdaysevText.appendChild(document.createTextNode(" ("+this.activity+")"));
        }
        if (this.report_id!=null && this.report_id!="") {
            this.sevText.appendChild(document.createTextNode(" @"+this.report_id+""));
            this.listsevText.appendChild(document.createTextNode(" @"+this.report_id+""));
            this.listdaysevText.appendChild(document.createTextNode(" @"+this.report_id+""));
            
        }
        
        this.ttip.html=this.getStartHour()+":"+this.getStartMinute()+" "+this.getEndHour()+":"+this.getEndMinute()+" "+this.event;
        this.listttip.html=this.getStartHour()+":"+this.getStartMinute()+" "+this.getEndHour()+":"+this.getEndMinute()+" "+this.event;
    },
    
    getActivity:function(){
        return this.activity;
    },
    
    getCustomer:function(){
        return this.customer_description;
    },
    
    getReport:function(){
        return this.report_id;
    },
    
    getStartHour : function(){
        return this.hstart;
    },

    getStartMinute : function(){
        return this.mstart;
    },

    getEndHour : function(){
        return this.hend;
    },

    getEndMinute : function(){
        return this.mend;
    },
    
    moveEvent : function(top,left,height,width){
        this.localTop=top;
        this.localLeft=left;
        this.localHeight=height;
        this.localWidth=width;
        if(Ext.isIE) height+=2;

        this.rel.style.top=top;
        this.rel.style.left=left;
        this.rel.style.height=height;
        this.rel.style.width=width;

        this.sevText.style.top=1;
        if(Ext.isIE)
          this.sevText.style.height=height;
        else
          this.sevText.style.height=height-2;
        this.sevText.style.left=7;
        this.sevText.style.width=width-8;
        this.sevTextArea.style.top=1;
        if(Ext.isIE)
          this.sevTextArea.style.height=height;
        else
          this.sevTextArea.style.height=height-2;
        this.sevTextArea.style.left=7;
        this.sevTextArea.style.width=width-8;

        if(Ext.isIE) {
          this.sevTop.style.top=-1;
          this.sevTop.style.height=8;
        } else {
          this.sevTop.style.top=-2;
          this.sevTop.style.height=7;
        }
        this.sevTop.style.left=0;
        this.sevTop.style.width=width;

        if(Ext.isIE) {
          this.sevBottom.style.top=height-7;
          this.sevBottom.style.height=7;
        } else {
          this.sevBottom.style.top=height-6;
          this.sevBottom.style.height=6;
        }
        this.sevBottom.style.left=0;
        this.sevBottom.style.width=width;

        if(Ext.isIE) {
          this.sevLeft.style.left=-1;
          this.sevLeft.style.width=9;
        } else {
          this.sevLeft.style.left=-1;
          this.sevLeft.style.width=7;
        }
        this.sevLeft.style.top=-1;
        this.sevLeft.style.height=height+1;
        this.manageDragZone(false);
    },
    
    moveListEvent : function(top,left,height,width){
        this.t = top;
        this.l = left;
        this.h = height;
        this.w = width;
        this.listrel.style.top=top;
        this.listrel.style.height=height;
        this.listrel.style.left=left;
        this.listrel.style.width=width;

        this.listsevText.style.top=0;
        this.listsevText.style.height=height;
        this.listsevText.style.left=0;
        this.listsevText.style.width="100%";
        this.listsevTextArea.style.top=0;
        this.listsevTextArea.style.height=height;
        this.listsevTextArea.style.left=0;
        this.listsevTextArea.style.width="100%";
        this.manageDragZone(true);
    },
    
    
    moveListDayEvent : function(top,left,height,width){
        this.t = 0;
        this.l = left;
        this.h = height;
        this.w = width;
        this.listdayrel.style.top=0;
        this.listdayrel.style.height=height;
        this.listdayrel.style.left=left;
        this.listdayrel.style.width=width;
        
        
        this.listdaysevText.style.top=0;
        this.listdaysevText.style.height=height;
        this.listdaysevText.style.left=0;
        this.listdaysevText.style.width="100%";
        this.listdaysevTextArea.style.top=0;
        this.listdaysevTextArea.style.height=height;
        this.listdaysevTextArea.style.left=0;
        this.listdaysevTextArea.style.width="100%";
        this.manageDragDayZone();
    },
    
    
    getDefaultSchedulerWidth : function(){
        return this.separent.width-this.separent.getOffsetWidth();
    },

    getDefaultWidth : function(){
        if (this.viewFlag=="w5")
          return this.getDefaultSchedulerWidth()/5;
        return this.getDefaultSchedulerWidth();
    },

    getMinX : function(){
        if (this.separent.viewFlag=="w5")
          return this.getDefaultWidth()*(this.seIndex-1)*-1;
        return 0;
    },

    getMaxRight : function(){
        if (this.separent.viewFlag=="w5")
          return this.getDefaultWidth()*(6-this.seIndex);
        return this.getDefaultSchedulerWidth();
    },

    getDefaultSchedulerHeight : function(){
      return this.getDefaultHeight()*48;
    },

    getDefaultHeight : function(){
        return 20;
    },

    getDefaultListHeight : function(){
        return 14;
    },

    getRootDayElement : function(){
        return this.listdayrel;
    },

    getRootListElement : function(){
        return this.listrel;
    },

    getRootElement : function(){
        return this.rel;
    }

});

Ext.define('Sonicle.webtop.calendar.FormEvent', {
    extend: 'Ext.container.Container',
    cs: null,
    initx:8,
    inity:5,
    planningGrid:null,
    /*storeRemainder:new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewRemainder'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewremainder',
            fields: ['id','value']
        })
    }),*/
     storeRemainder:null,           
    storeShare_with:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewShare_with'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewshare_with',
            fields: ['id','value']
        })
    }),*/
    storeActivity_id:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewActivity_id'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewactivity_id',
            fields: ['id','value']
        })
    }),*/
    storeActivity_flag:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewActivity_flag'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewactivity_flag',
            fields: ['id','value']
        })
    }),*/
    storeCategory_id:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewCategory_id'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewcategory_id',
            fields: ['id','value']
        })
    }),*/
    
    storeCalendar:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewCalendar'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewcalendar',
            fields: ['id','color','value','privatevalue','busy','default_reminder','default_send_invite']
        })
    }),*/
    storeCustomer_id:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewCustomer_id'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewcustomer_id',
            fields: ['id','value']
        })
    }),*/
    storeStatistic_id:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewStatistic_id'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewstatistic_id',
            fields: ['id','value','pid']
        })
    }),*/
    storeViewDayly:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewDayly'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewdayly',
            fields: ['id','value']
        })
    }),*/
    storeViewWeekly:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewWeekly'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewweekly',
            fields: ['id','value']
        })
    }),*/
    storeViewMonthlyDay:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewMonthlyDay'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewmonthlyday',
            fields: ['id','value']
        })
    }),*/
    storeViewMonthlyMonth:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewMonthlyMonth'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewmonthlymonth',
            fields: ['id','value']
        })
    }),*/
    storeViewYearlyDay:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewYearlyDay'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewyearlyday',
            fields: ['id','value']
        })
    }),*/
    storeViewYearlyMonth:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewYearlyMonth'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewyearlymonth',
            fields: ['id','value']
        })
    }),*/
    
    storeViewRepeatTimes:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewRepeatTimes'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewrepeattimes',
            fields: ['id','value']
        })
    }),*/
    
   storeTimezone:null,/*new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetViewTimeZone'},
        autoLoad: true,
        reader: new Ext.data.JsonReader({
            root: 'viewtimezone',
            fields: ['id','value']
        })
    }),*/
    
    
    
     rclickrowselect: function(e, t) {
        var row = this.planningGrid.view.findRowIndex(t);
        var sm = this.planningGrid.getSelectionModel();
        if (row>=0 && !sm.isSelected(row)) sm.selectRow(row, false);

    },
    
    trim:function(s){
            var l=0;var r=s.length -1;
            while(l < s.length && s[l] == ' ')
            {l++;}
            while(r > l && s[r] == ' ')
            {r-=1;}
            return s.substring(l, r+1);
    },
    
    
    
    
    getAvailableWindow:function(){
        /*
       this.storeavailable = new Ext.data.Store({                  
                proxy: new Ext.data.HttpProxy(new Ext.data.Connection({
                    url:'ServiceRequest'
                })),
                autoLoad: false,
                baseParams: {service: 'calendar', action: 'GetAvailable'},
                reader: new Ext.data.JsonReader ({                                        
                    root: 'available',
                    fields: []
                })//reader                
        });
    */
   
      
      
      
      var availableGrid=new Ext.grid.GridPanel ({                                                                          
            columns:[],
            autoScroll:true,
            columnLines: true,   
            selModel:new Ext.grid.RowSelectionModel({singleSelect:true}),
            //store:this.storeavailable,
            loadMask: {msg: WT.res('calendar','waiting')},
            enableRowBody : true,
            autoExpandMin:150,
            autoExpandColumn:'name'
            
                
            
        }); 
    /*
    var win=WT.app.createWindow({
                    availableGrid:availableGrid,
                    width: 600,
                    height:200,
                    layout: 'fit',
                    autoScroll: true,
                    modal:true,
                    minimizable:false,
                    maximizable:false,
                    iconCls: 'icon-menucalendar',
                    title:WT.res('calendar',"available"),
                    items: [
                       availableGrid
                    ]
                });
            
    */
    return availableGrid;
      
    },
    
    getStoreCausal:function(){
      /*var sc=new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
        baseParams: {service:'calendar',action:'GetConfirmCausal'},
        autoLoad: false,
        reader: new Ext.data.JsonReader({
            root: 'getcausal',
            fields: ['id','value',]
        })
      });
      return sc;*/
        return null;
    },

    getEventForm : function() {
      var cs=this.cs;
      var saveEvent = new Ext.Action ({
        text: cs.res("saveclose"),
        iconCls: 'iconSaveEvent'
      });
      var deleteEvent = new Ext.Action ({
        iconCls: 'iconDeleteEvent'
      });
      var printEvent = new Ext.Action ({
        iconCls: 'iconPrint'
      });
      var restoreRecurrence = new Ext.Action ({
        iconCls: 'iconRestoreRecurrence',
        disabled:true
      });
      var event=new WT.SuggestTextField({ 
        lookupService: 'calendar',
        lookupContext: 'eventcalendar',
        name: 'event',
        maxLength : 100,
        allowBlank : false,
        x: 80+this.initx,
        y: 0+this.inity,
        anchor:'99%'
      });
      var report_id= new WT.SuggestTextField({  
        lookupService: 'calendar',
        lookupContext: 'report_idcalendar',
        name: 'report_id',
        maxLength : 100,
        x: 80+this.initx,
        y: 25+this.inity,
        anchor:'99%'
      });
      var yyyymmdd = null;
      var fthh = new Ext.form.TimeField({
        x: 250+this.initx,
        y: 55+this.inity,
        width: 60,
        format : 'H:i',
        increment : 30,
        allowBlank : false,
        name: 'fthh'
      });
      var tthh = new Ext.form.TimeField({
        x: 250+this.initx,
        y: 80+this.inity,
        width: 60,
        format : 'H:i',
        increment : 30,
        allowBlank : false,
        name: 'tthh'
      });
      
      
      var allday= new Ext.form.Checkbox({
        x: 490+this.initx,
        y: 55+this.inity,
        hideLabel: true,
        name: 'allday'
      });
      
      var timezone_id = new Ext.form.ComboBox({
        x: 435+this.initx,
        y: 80+this.inity,
        hideLabel: true,
        name: 'timezone_id',
        anchor: '99%',
        store: this.storeTimezone,
        mode: 'remote',
        //typeAhead : true,
        selectOnFocus : true,
        forceSelection: true,
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false,
        autoSelect: true,
        width:220,
        tpl:'<tpl for="."><div ext:qtip="{value}" class="x-combo-list-item">{value}</div></tpl>'
			
      });
      
      
      var enddate = new Ext.form.DateField({
        x: 80+this.initx,
        y: 80+this.inity,
        width: 100,
        format: 'd/m/Y',
        name: 'enddate'
        /*
        format : this.format,
        cancelText: this.cancelText,
        okText : this.okText,
        monthNames:yyyymmdd.monthNames,
        dayNames:yyyymmdd.dayNames,
        startDay:yyyymmdd.startDay,
        todayText:yyyymmdd.todayText
        */
      });
      var description = new Ext.form.TextArea({
        name: 'description',
        maxLength : 1024,
        x: 0+this.initx,
        y: 25+this.inity,
        hideLabel: true,
        height: '95',
        anchor:'99%'
      });
      var reminder = new Ext.form.ComboBox({
        x: 100+this.initx,
        y: 125+this.inity,
        width: 80,
        name: 'reminder',
        store: this.storeRemainder,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      
      var calendar_id = new Ext.form.ComboBox({
        x: 410+this.initx,
        y: 125+this.inity,
        hideLabel: true,
        name: 'calendar_id',
        anchor: '99%',
        store: this.storeCalendar,
        mode: 'remote',
        //typeAhead : true,
        selectOnFocus : true,
        forceSelection: true,
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: true,
        autoSelect: false,
        minChars: 1,
        loadingText: "",
        queryDelay: 500,
        tpl: '<tpl for="."><div class="x-combo-list-item" style="background-color: {color};">{value}</div></tpl>',
        value:'',
        default_send_invite:cs.default_send_invite
        
      });
      
      var private_value= new Ext.form.Checkbox({
        x: 235+this.initx,
        y: 125+this.inity,
        hideLabel: true,
        name: 'private_value'
      });
      var busy_value= new Ext.form.Checkbox({
        x: 320+this.initx,
        y: 125+this.inity,
        hideLabel: true,
        name: 'busy_value'
      });
      var share_with = new Ext.form.ComboBox({
        x: 360+this.initx,
        y: 125+this.inity,
        xtype: 'combo',
        hideLabel: true,
        name: 'share_with',
        anchor: '99%',
        store: this.storeShare_with,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      var causalStore=this.getStoreCausal();
      var causal = new Ext.form.ComboBox({                                                                                                                                                                               
            mode: 'remote',
            triggerAction: 'all',
            editable: true,
            store: causalStore,
            displayField:'value',
            valueField: 'id',
            typeAhead: false,              
            forceSelection: false,                  
            selectOnFocus:false,
            minChars: 1,
            loadingText: WT.res("calendar","waiting"),
            queryDelay: 300,
            anchor: '99%',
            x: 100+this.initx,
            y: 250+this.inity
      });
      var activity_id = new Ext.form.ComboBox({
        x: 100+this.initx,
        y: 175+this.inity,
        width: 395,
        name: 'activity_id',
        store: this.storeActivity_id,
        mode: 'remote',
        //typeAhead : true,
        selectOnFocus : true,
        forceSelection: true,
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: true,
        autoSelect: false,
        minChars: 1,
        loadingText: "",
        queryDelay: 500,
        value:''
      });
      var activity_flag = new Ext.form.ComboBox({
        x: 500+this.initx,
        y: 175+this.inity,
        hideLabel: true,
        name: 'activity_flag',
        anchor: '99%',
        store: this.storeActivity_flag,
        mode: 'remote',
        //typeAhead : true,
        selectOnFocus : true,
        forceSelection: true,
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: true,
        value:''
      });
      
      var customer_id = new Ext.form.ComboBox({
        x: 100+this.initx,
        y: 200+this.inity,
        hideLabel: true,
        name: 'customer_id',
        anchor: '99%',
        store: this.storeCustomer_id,
        mode: 'remote',
        //typeAhead : true,
        selectOnFocus : true,
        forceSelection: true,
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: true,
        autoSelect: false,
        minChars: 1,
        loadingText: "",
        queryDelay: 500,
        value:''
			
      });
      var statistic_id = new Ext.form.ComboBox({
        x: 100+this.initx,
        y: 225+this.inity,
        hideLabel: true,
        name: 'statistic_id',
        anchor: '99%',
        store: this.storeStatistic_id,
        mode: 'remote',
        //typeAhead : true,
        selectOnFocus : true,
        forceSelection: true,
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: true,
        autoSelect: false,
        minChars: 1,
        loadingText: "",
        queryDelay: 500,
        value:''
      });
      var field_causal=new Ext.form.Label({ // Causal
              x: 0+this.initx,
              y: 250+this.inity,
              xtype: 'label',
              text: WT.res("calendar","causal")+":"
      });
          
      var appointment = new Ext.Panel({
        name:'appointment',
        layout:'absolute',
        title: cs.res("appointment"),
        iconCls:"icon-reminder",
        items:[
          { // Descrizione
              x: 0+this.initx,
              y: 4+this.inity,
              xtype: 'label',
              text: cs.res("description")
          },
          description,
          { // Promemoria
              x: 0+this.initx,
              y: 129+this.inity,
              xtype: 'label',
              text: cs.res("reminder")
          },
          reminder,
          { // Privato
              x: 190+this.initx,
              y: 129+this.inity,
              xtype: 'label',
              text: cs.res("private")
          },
          private_value,
          { // Occupato
              x: 260+this.initx,
              y: 129+this.inity,
              xtype: 'label',
              text: cs.res("busy")+":"
          },
          busy_value,
          /*{ // Condividi con
              x: 290+this.initx,
              y: 129+this.inity,
              xtype: 'label',
              text: cs.res("share")
          },
          share_with,*/
          { // Linea separazione
              x: 0+this.initx,
              y: 165+this.inity,
              anchor:'99%',
              xtype:'fieldset',
              autoHeight:true,
              collapsed: true
          },
          { // Attività
              x: 350+this.initx,
              y: 129+this.inity,
              xtype: 'label',
              text: cs.res("calendarl")
          },
          
          activity_id,
          // Flag Attività
          activity_flag, 
          { // Categoria
              x: 0+this.initx,
              y: 175+this.inity,
              xtype: 'label',
              text: cs.res("activity")
          },
          calendar_id, 
          { // Clienti
              x: 0+this.initx,
              y: 200+this.inity,
              xtype: 'label',
              text: cs.res("customer")
          },
          customer_id, 
          { // Statistico
              x: 0+this.initx,
              y: 225+this.inity,
              xtype: 'label',
              text: cs.res("statistic")
          },
          statistic_id,
          causal,
          field_causal
          
        ]
      });
      var none_recurrence = new Ext.form.Radio({
        x: 0+this.initx,
        y: 20+this.inity,
        checked:true,
        name: 'fav-recurrence',
        boxLabel: cs.res("none_recurrence")
      });
      var dayly_recurrence = new Ext.form.Radio({
        x: 0+this.initx,
        y: 60+this.inity,
        name: 'fav-recurrence',
        boxLabel: cs.res("dayly_recurrence")
      });
      var dayly1 = new Ext.form.Radio({
        disabled: true,
        x: 140+this.initx,
        y: 60+this.inity,
        name: 'fav-dayly',
        boxLabel: cs.res("dayly1")
      });
      var dayly_step = new Ext.form.ComboBox({
        disabled: true,
        x: 205+this.initx,
        y: 60+this.inity,
        width: 40,
        name: 'dayly_step',
        store: this.storeViewDayly,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      var dayly2 = new Ext.form.Radio({
        disabled: true,
        x: 350+this.initx,
        y: 60+this.inity,
        name: 'fav-dayly',
        boxLabel: cs.res("dayly2")
      });
      var weekly_recurrence = new Ext.form.Radio({
        x: 0+this.initx,
        y: 100+this.inity,
        name: 'fav-recurrence',
        boxLabel: cs.res("weekly_recurrence")
      });
      var weekly_step = new Ext.form.ComboBox({
        disabled: true,
        x: 205+this.initx,
        y: 100+this.inity,
        width: 40,
        name: 'weekly_step',
        store: this.storeViewWeekly,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      var weekly1 = new Ext.form.Checkbox({
        disabled: true,
        x: 350+this.initx,
        y: 100+this.inity,
        boxLabel : cs.res("weekly1"),
        name: 'weekly1'
      });
      var weekly2 = new Ext.form.Checkbox({
        disabled: true,
        x: 385+this.initx,
        y: 100+this.inity,
        boxLabel : cs.res("weekly2"),
        name: 'weekly2'
      });
      var weekly3 = new Ext.form.Checkbox({
        disabled: true,
        x: 420+this.initx,
        y: 100+this.inity,
        boxLabel : cs.res("weekly3"),
        name: 'weekly3'
      });
      var weekly4 = new Ext.form.Checkbox({
        disabled: true,
        x: 455+this.initx,
        y: 100+this.inity,
        boxLabel : cs.res("weekly4"),
        name: 'weekly4'
      });
      var weekly5 = new Ext.form.Checkbox({
        disabled: true,
        x: 490+this.initx,
        y: 100+this.inity,
        boxLabel : cs.res("weekly5"),
        name: 'weekly5'
      });
      var weekly6 = new Ext.form.Checkbox({
        disabled: true,
        x: 525+this.initx,
        y: 100+this.inity,
        boxLabel : cs.res("weekly6"),
        name: 'weekly6'
      });
      var weekly7 = new Ext.form.Checkbox({
        disabled: true,
        x: 560+this.initx,
        y: 100+this.inity,
        boxLabel : cs.res("weekly7"),
        name: 'weekly7'
      });
      var monthly_recurrence = new Ext.form.Radio({
        x: 0+this.initx,
        y: 140+this.inity,
        name: 'fav-recurrence',
        boxLabel: cs.res("monthly_recurrence")
      });
      var monthly_day = new Ext.form.ComboBox({
        disabled: true,
        x: 205+this.initx,
        y: 140+this.inity,
        width: 40,
        name: 'monthly_day',
        store: this.storeViewMonthlyDay,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      var monthly_month = new Ext.form.ComboBox({
        disabled: true,
        x: 300+this.initx,
        y: 140+this.inity,
        width: 40,
        name: 'monthly_month',
        store: this.storeViewMonthlyMonth,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      var yearly_recurrence = new Ext.form.Radio({
        x: 0+this.initx,
        y: 180+this.inity,
        name: 'fav-recurrence',
        boxLabel: cs.res("yearly_recurrence")
      });
      var yearly_day = new Ext.form.ComboBox({
        disabled: true,
        x: 205+this.initx,
        y: 180+this.inity,
        width: 40,
        name: 'yearly_day',
        store: this.storeViewYearlyDay,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      var yearly_month = new Ext.form.ComboBox({
        disabled: true,
        x: 300+this.initx,
        y: 180+this.inity,
        width: 80,
        name: 'yearly_month',
        store: this.storeViewYearlyMonth,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      
      var radio_until_yyyymmdd = new Ext.form.Radio({
        name: 'radio_until_yyyymmdd',
        boxLabel: cs.res("until"),
        x: 0+this.initx,
        y: 220+this.inity,
        disabled: true,
        checked:false
        
      });
      
      var until_yyyymmdd = null;
      
      var repeat_times = new Ext.form.Radio({
        name: 'repeat_times',
        boxLabel: cs.res("repeat_times"),
        x: 260+this.initx,
        y: 220+this.inity,
        disabled: true
        
      });
      
      
      var repeat_times_step = new Ext.form.ComboBox({
        disabled: true,
        x: 320+this.initx,
        y: 220+this.inity,
        width: 40,
        name: 'repeat_times_step',
        store: this.storeViewRepeatTimes,
        mode: 'local',
        triggerAction: 'all',
        displayField: 'value',
        valueField: 'id',
        editable: false
      });
      var permanent_recurrence=new Ext.form.Radio({
        name: 'permanent',
        boxLabel: cs.res("permanent"),
        x: 420+this.initx,
        y: 220+this.inity,
        disabled: true,
        checked:true
      });
          
      var recurrence = new Ext.Panel({
        name:'recurrence',
        layout:'absolute',
        title:cs.res("recurrence"),
        iconCls:"icon-recurrence",
        items: [
          none_recurrence,
          { // Linea separazione
              x: 140+this.initx,
              y: 50+this.inity,
              anchor:'-5',
              xtype:'fieldset',
              collapsed: true
          },
          dayly_recurrence,
            dayly1,
            dayly_step,
            {// Ogni n Giorni combo fine
                x: 250+this.initx,
                y: 60+this.inity,
                xtype: 'label',
                text: cs.res("dayly_msg1")
            },
            dayly2,
          { // Linea separazione
              x: 140+this.initx,
              y: 90+this.inity,
              anchor:'-5',
              xtype:'fieldset',
              collapsed: true
          },
          weekly_recurrence,
            { // Settimana
                x: 140+this.initx,
                y: 100+this.inity,
                xtype: 'label',
                text: cs.res("weekly_msg1")
            },
            weekly_step,
            { // Settimana
                x: 250+this.initx,
                y: 100+this.inity,
                xtype: 'label',
                text: cs.res("weekly_msg2")
            },
            weekly1,weekly2,weekly3,weekly4,weekly5,weekly6,weekly7,
          { // Linea separazione
              x: 140+this.initx,
              y: 130+this.inity,
              anchor:'-5',
              xtype:'fieldset',
              collapsed: true
          },
          monthly_recurrence,
            { // Mese
                x: 140+this.initx,
                y: 140+this.inity,
                xtype: 'label',
                text: cs.res("monthly_msg1")
            },
            monthly_day,
            { // Mese
                x: 250+this.initx,
                y: 140+this.inity,
                xtype: 'label',
                text: cs.res("monthly_msg2")
            },
            monthly_month,
            { // Mese
                x: 350+this.initx,
                y: 140+this.inity,
                xtype: 'label',
                text: cs.res("monthly_msg3")
            },
          { // Linea separazione
              x: 140+this.initx,
              y: 170+this.inity,
              anchor:'-5',
              xtype:'fieldset',
              collapsed: true
          },
          yearly_recurrence,
            { // Anno
                x: 140+this.initx,
                y: 180+this.inity,
                xtype: 'label',
                text: cs.res("yearly_msg1")
            },
            yearly_day,
            { // Anno
                x: 250+this.initx,
                y: 180+this.inity,
                xtype: 'label',
                text: cs.res("yearly_msg2")
            },
            yearly_month,
          { // Linea separazione
              x: 140+this.initx,
              y: 210+this.inity,
              anchor:'-5',
              xtype:'fieldset',
              collapsed: true
          },
          /*
          {// Fino a
              x: 0+this.initx,
              y: 220+this.inity,
              xtype: 'label',
              text: cs.res("until")
          },
          */
          radio_until_yyyymmdd,
          until_yyyymmdd,
          repeat_times,
          repeat_times_step,
          { // volte
              x: 370+this.initx,
              y: 225+this.inity,
              xtype: 'label',
              text: cs.res("times")
          },
          permanent_recurrence
        ]
      });
      
      /*
      this.invitationStore=new Ext.data.ArrayStore({
                       fields: ['invitation', 'action'],
                       autoLoad:true,
                       data : [                                         
                               ['S',cs.res('sendinvit')],
                               ['N', cs.res('notsendinvit')]
                              ]
                        });
      
      /*
      this.invitation=new Ext.form.ComboBox({
                      store:this.invitationStore,
                      displayField: 'action',
                      valueField: 'invitation',
                      editable: false,
                      forceSelection:true,
                      mode: 'local',
                      triggerAction: 'all',
                      width:100,
                      hiddeLabel:true,
                      typeAhead: true,
                      autoSelect : true,
                      autoShow : true
      
      });
      */
      this.nameinvit=new WT.SuggestTextField({ 
               displayField: 'email',
               lookupService: 'mail',
               lookupAction: 'LookupContacts',
               lookupContext: 'recipients',
               lookupRoot: 'contacts',
               allowBlank: true
       });
      
      
            
     this.partecipantStore=new Ext.data.ArrayStore({
               fields: ['id', 'action'],
               autoLoad:true,
               data : [                                         
                       ['N',cs.res('necessary')],
                       ['O',cs.res('optional')],
                       ['R', cs.res('resource')]
                      ]
                });
      
      
      this.partecipant=new Ext.form.ComboBox({
                      store:this.partecipantStore,
                      displayField: 'action',
                      valueField: 'id',
                      editable: false,
                      forceSelection:true,
                      mode: 'local',
                      triggerAction: 'all',
                      width:100,
                      hiddeLabel:true
      });      
     var checkBoxSelMod = new Ext.grid.CheckboxSelectionModel();
     this.responseStore=new Ext.data.ArrayStore({
               fields: ['id', 'action'],
               autoLoad:true,
               data : [                                         
                       ['none',cs.res('none')],
                       ['accepted',cs.res('accepted')],
                       ['refused', cs.res('refused')],
                       ['attempt', cs.res('attempt')]
                      ]
                });
     
     this.response=new Ext.form.ComboBox({
                      store:this.responseStore,
                      displayField: 'action',
                      valueField: 'id',
                      editable: false,
                      forceSelection:true,
                      mode: 'local',
                      triggerAction: 'all',
                      width:100,
                      hiddeLabel:true
      });    
      
      
      this.planningStore = null,/*new Ext.data.Store({                  
                proxy: new Ext.data.HttpProxy(new Ext.data.Connection({
                    url:'ServiceRequest'
                })),
                autoLoad: false,
                baseParams: {service: 'calendar', action: 'GetPlanning'},
                reader: new Ext.data.JsonReader ({                                        
                    totalProperty: 'totalCount',
                    root: 'planning',
                    fields: [                                                    
                        {name:'invitation'},
                        {name:'name'},
                        {name:'partecipant'},
                        {name:'response'},
                        {name:'newormodify'}
                        
                     ]
                })//reader                
        });*/
      
      
     this.sendall=new Ext.Action({ 
                        text:cs.res("sendall"),
                        iconCls:"icon-sendall",
                        handler:function(){
                            var store=this.planningGrid.getStore();
                            store.each(function(rec){
                                if (rec.get('response')=='none'){
                                    rec.set('invitation','true');
                                }
                             });
                            
                            
                        },
                        scope: this
     });
     
     this.notsendall=new Ext.Action({ 
                        text:cs.res("notsendall"),
                        iconCls:"icon-notsendall",
                        handler:function(){
                            var store=this.planningGrid.getStore();
                            store.each(function(rec){
                                    rec.set('invitation','false');
                             });
                        },
                        scope: this
     });
     
     
      
      this.newRowPlanning=new Ext.Action({
                    text: cs.res("newrowplanning"),
                    iconCls: 'icon-newrow',
                    scope:this,
                    handler:function(){
                            var records=this.planningGrid.getStore().getCount();
                            var lastrec=this.planningGrid.getStore().getAt(records-1);
                            if (lastrec.get('name')!=""){
                                var rec=Ext.data.Record.create([{name: 'invitation'},{name: 'name'},{name: 'partecipant'},{name: 'response'},{name: 'newormodify'}]);
                                this.planningGrid.getStore().add(new rec({invitation: calendar_id.default_send_invite, name:'' ,partecipant:'O', response:'none', newormodify:'false'}));
                                //var records=this.planningGrid.getStore().getCount();
                                this.planningGrid.getSelectionModel().selectRow(records);
                                this.planningGrid.getView().focusRow(records);
                                this.planningGrid.doLayout();
                                this.planningGrid.startEditing(records,1);
                            }else{
                                this.planningGrid.startEditing(records-1,1);
                            }
                        this.planningGrid.getSelectionModel().selectLastRow();
                    } 
      });
      
      
      this.deleteRowPlanning=new Ext.Action({
                    text: cs.res("deleterowplanning"),
                    iconCls: 'icon-deleterow',
                    scope:this,
                    disabled:true,
                    handler:function(){
                        var rec=this.planningGrid.getSelectionModel().getSelections();
                        for (var i=0;i<rec.length;i++){
                        if (rec[i].get('name')!="")
                            this.planningGrid.getStore().remove(rec[i]);
                        }
                        
                        this.planningGrid.getSelectionModel().selectLastRow();
                    } 
      });
      
      
      var availablePlanning=new Ext.Action({
                    text: WT.res('calendar','available'),
                    iconCls: 'icon-menucalendar'
                    
      });
      
      var closeavailablePlanning=new Ext.Action({
                    text: WT.res('calendar','closeavailable'),
                    iconCls: 'icon-menucalendar',
                    hidden:true
                    
      });
      
      
      this.menuPlanning=new Ext.menu.Menu({
            items: [
                this.deleteRowPlanning,
                '-',
                this.sendall,
                this.notsendall
                
            ]
        });
      
      var textbusy=new Ext.form.DisplayField({
            value:WT.res('calendar','busy')
      });
      
      var iconbusy = new Ext.BoxComponent({
                              autoEl: {tag: "div", style:"padding:0;margin:0;border: 1px solid;margin-right:3px;background:#9FC6E7;"},
                              margins:{top:5, right:3, bottom:0, left:0},
                              width: 15,
                              height: 15
                      });
      var textfree=new Ext.form.DisplayField({
            value:WT.res('calendar','free')
      });
      
      var iconfree = new Ext.BoxComponent({
                              autoEl: {tag: "div", style:"padding:0;margin:0;border: 1px solid;margin-right:3px;background:#F3E4B1;"},
                              margins:{top:5, right:3, bottom:0, left:0},
                              width: 15,
                              height: 15
      });
      
      var textnoinfo=new Ext.form.DisplayField({
            value:WT.res('calendar','noinfo')
      });
      
      var iconnoinfo= new Ext.BoxComponent({
                              autoEl: {tag: "div", style:"padding:0;margin:0;border: 1px solid;margin-right:3px;background:lightgrey;"},
                              margins:{top:5, right:3, bottom:0, left:0},
                              width: 15,
                              height: 15
      });
      
      this.planningGrid=new Ext.grid.EditorGridPanel ({                                                                          
            iconCls:"icon-planning",
            tbar: [this.newRowPlanning,this.deleteRowPlanning,availablePlanning,closeavailablePlanning],
            bbar:[' ',iconbusy,' ',textbusy,' ',' ',iconfree,' ',textfree,' ',' ',iconnoinfo,' ',textnoinfo],
            columns: [                  
               /*
               {
                id:'invitation',
                header: cs.res('invitation'),                
                dataIndex:'invitation',
                editor:new Ext.form.Checkbox({}),
                renderer:function(value){
                    if (value=='S') return cs.res('sendinvit');
                    if (value=='N') return cs.res('notsendinvit');
                }
                
               },
               */
              {
                id:'invitation',
                header: cs.res('invitation'),                
                dataIndex:'invitation',
                width:40,
                renderer : function(v, p, record){

                    if (v=='true') 
                        return "<input checked=checked name='check[]' type='checkbox' value="+v+">";
                    else
                        return "<input name='check[]' type='checkbox' value="+v+">";
                },
                
		listeners : {
			click : function(col, grid,rowIndex,e){
				var check = document.getElementsByName('check[]');
                                var c=check[rowIndex].checked;
                                if (grid.getStore().getAt(rowIndex).get('name')!=""){
                                    grid.getStore().getAt(rowIndex).set('invitation',c);
                                }

			}
		}
	


                
               },
               
               {
                id:'name',
                header: cs.res('nameinvit'),                
                dataIndex:'name',
                editor:this.nameinvit
                
                
               },
               {
                id:'partecipant',
                header: cs.res('partecipant'),                
                dataIndex:'partecipant',
                editor:this.partecipant,
                renderer:function(value){
                    if (value=='N') return cs.res('necessary');
                    if (value=='O') return cs.res('optional');
                    if (value=='R') return cs.res('resource');
                }
               },
              
               {
                id:'response',
                header: cs.res('response'),                
                dataIndex:'response',
                editor:this.response,
                renderer:function(value){
                    return cs.res(value);
                }
               },
               {
                id:'newormodify',
                dataIndex:'newormodify',
                hidden:true
                
               }
               
               
               
            ],
            clicksToEdit: 1,
            autoScroll:true,
            columnLines: false,  
            //autoHeight: true,
            selModel:new Ext.grid.RowSelectionModel({singleSelect:false}),
            store:  this.planningStore, 
            loadMask: {msg: cs.res('waiting')},
            autoExpandColumn:'name',
            autoExpandMin:150,
            title: cs.res("planning"),
            listeners:{
                    keydown:function(e){
                       if (e.getKey() == e.ENTER) {
                            var records=this.planningGrid.getStore().getCount();
                            var lastrec=this.planningGrid.getStore().getAt(records-1);
                            if (lastrec.get('name')!=""){
                                var rec=Ext.data.Record.create([{name: 'invitation'},{name: 'name'},{name: 'partecipant'},{name: 'response'},{name: 'newormodify'}]);
                                this.planningGrid.getStore().add(new rec({invitation: calendar_id.default_send_invite, name:'' ,partecipant:'O', response:'none', newormodify:'false'}));
                                //var records=this.planningGrid.getStore().getCount();
                                this.planningGrid.getSelectionModel().selectRow(records);
                                this.planningGrid.getView().focusRow(records);
                                this.planningGrid.doLayout();
                                this.planningGrid.startEditing(records,1);
                            }else{
                                this.planningGrid.startEditing(records-1,1);
                            }
                        }
                        
                    },
                    keypress:function(e){
                        if (e.getKey() == e.ENTER) {
                            WT.debug("invite="+cs.default_send_invite);
                            var records=this.planningGrid.getStore().getCount();
                            var lastrec=this.planningGrid.getStore().getAt(records-1);
                            if (lastrec.get('name')!=""){
                                var rec=Ext.data.Record.create([{name: 'invitation'},{name: 'name'},{name: 'partecipant'},{name: 'response'},{name: 'newormodify'}]);
                                this.planningGrid.getStore().add(new rec({invitation: calendar_id.default_send_invite, name:'' ,partecipant:'O', response:'none', newormodify:'false'}));
                                //var records=this.planningGrid.getStore().getCount();
                                this.planningGrid.getSelectionModel().selectRow(records);
                                this.planningGrid.getView().focusRow(records);
                                this.planningGrid.doLayout();
                                this.planningGrid.startEditing(records,1);
                            }else{
                                this.planningGrid.startEditing(records-1,1);
                            }
                        }
                        
                    },
                    scope:this,
                    afteredit:function(e){
                        var oldvalue=this.trim(e.originalValue);
                        var value=this.trim(e.value);
                        if (oldvalue!=value && e.field=='name'){
                            e.record.set('newormodify',"true");
                        }
                    }
                    
                    
            }
        });
        

        
        this.planningGrid.getBottomToolbar().setVisible(false);
        
        
      /*
      var planningPanel=new Ext.Panel({
            name:'planning',
            title: cs.res("planning"),
            items:[
                this.planningGrid
        ]
      });
      */
      //WT.app.setComponentContextMenu(this.planningGrid, this.menuPlanning, this.rclickrowselect, this);
      this.planningGrid.getSelectionModel().on('selectionchange', function(){
          this.deleteRowPlanning.disable();
          var rec = this.planningGrid.getSelectionModel().getSelected();
          
          if (rec != null){
              this.deleteRowPlanning.enable();
           } 
      }, this);
      
      var eventtab = new Ext.TabPanel({
        x: 5,
        y: 120+this.inity,
        anchor:'-4 -4',
        xtype:'tabpanel',
        plain:true,
        activeTab: 0,
        height:'250',
        name:'eventtab',
        items:[
            appointment,
            this.planningGrid,
            recurrence
        ]
      });

      
      
      
      var storeGroup = null;/*new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({url:'ServiceRequest'}),
            baseParams: {service:'calendar',action:'GetCalendars'},
            autoLoad: true,
            reader: new Ext.data.JsonReader({
                root: 'resources',
                fields: ['id','name','acl','checked','colorcalendar','privatevalue','busy']
            })
        });*/
       var groupCombo = new Ext.form.ComboBox({
            store: storeGroup,
            displayField: 'name',
            valueField: 'id',
            editable: false,
            forceSelection:true,
            mode: 'remote',
            triggerAction: 'all',
            width:150,
            hiddeLabel:true
        });
      
      var startButton=new Ext.Button({
              minWidth :50,
              xtype: 'button',
              handler: function() {
                form = this.findParentByType("form");
                form.fthh.setValue(new Date());
              },
              text: cs.res("textStart")
      });
      
      var endButton=new Ext.Button({
              minWidth :50,
              xtype: 'button',
              handler: function() {
                form = this.findParentByType("form");
                form.tthh.setValue(new Date());
              },
              text: cs.res("textEnd")
      });
      
      
      
      var form = new Ext.form.FormPanel({
        event_id:null,
        event_by:null,
        recurr_id:null,
        event:event,
        report_id:report_id,
        yyyymmdd:yyyymmdd,
        fthh:fthh,
        tthh:tthh,
        allday:allday,
        enddate:enddate,
        hourstartworkday:this.hourstartworkday,
        hourendworkday:this.hourendworkday,
        description:description,
        private_value:private_value,
        reminder:reminder,
        share_with:share_with,
        activity_id:activity_id,
        activity_flag:activity_flag,
        calendar_id:calendar_id,
        customer_id:customer_id,
        statistic_id:statistic_id,
        none_recurrence:none_recurrence,
        dayly_recurrence:dayly_recurrence,
        dayly1:dayly1,
        dayly_step:dayly_step,
        dayly2:dayly2,
        weekly_recurrence:weekly_recurrence,
        weekly_step:weekly_step,
        weekly1:weekly1,
        weekly2:weekly2,
        weekly3:weekly3,
        weekly4:weekly4,
        weekly5:weekly5,
        weekly6:weekly6,
        weekly7:weekly7,
        monthly_recurrence:monthly_recurrence,
        monthly_day:monthly_day,
        monthly_month:monthly_month,
        yearly_recurrence:yearly_recurrence,
        yearly_day:yearly_day,
        yearly_month:yearly_month,
        radio_until_yyyymmdd:radio_until_yyyymmdd,
        until_yyyymmdd:until_yyyymmdd,
        groupCombo:groupCombo,
        isPlanning:null,
        planningGrid:this.planningGrid,
        invitation:this.invitation,
        saveEvent: saveEvent,
        deleteEvent: deleteEvent,
        printEvent:printEvent,
        restoreRecurrence:restoreRecurrence,
        eventtab:eventtab,
        recurrence:recurrence,
        deleteRowPlanning:this.deleteRowPlanning,
        menuPlanning:this.menuPlanning,
        mod_event:null,
        mod_report_i:null,
        mod_fromdate:null,
        mod_todate:null,
        mod_description:null,
        mod_fthh:null,
        mod_tthh:null,
        startButton:startButton,
        endButton:endButton,
        availablePlanning:availablePlanning,
        closeavailablePlanning:closeavailablePlanning,
        old_recurr_id:null,
        repeat_times:repeat_times,
        repeat_times_step:repeat_times_step,
        permanent_recurrence:permanent_recurrence,
        busy_value:busy_value,
        timezone_id:timezone_id,
        causal:causal,
        field_causal:field_causal,
        win:null,
        cs:null,
        formtitle:cs.res("eventtitle"),
        tbar: [saveEvent,deleteEvent,printEvent,restoreRecurrence,'-',groupCombo],
        items: [
        { // Evento
            x: 0+this.initx,
            y: 4+this.inity,
            xtype: 'label',
            cls: 'schedulerBold',
            text: cs.res("textEvent")
        },
        event,
        {// Luogo
            x: 0+this.initx,
            y: 29+this.inity,
            xtype: 'label',
            text: cs.res("textLocation")
        },
        report_id,
        { // Linea separazione
            x: 0+this.initx,
            y: 50+this.inity,
            anchor:'99%',
            xtype:'fieldset',
            autoHeight:true,
            collapsed: true
        },{ // Data
            x: 0+this.initx,
            y: 59+this.inity,
            xtype: 'label',
            cls: 'schedulerBold',
            text: cs.res("textDateEvent")
        },
        yyyymmdd,
        { // Ora inizio
            x: 190+this.initx,
            y: 59+this.inity,
            xtype: 'label',
            cls: 'schedulerBold',
            text: cs.res("textStartHour")
        },
        fthh,
        {
            x: 312+this.initx,
            y: 55+this.inity,
            width:50,
            border : false,
            xtype: 'panel',
            items: startButton
            
            
        },{ // Ora fine
            x: 190+this.initx,
            y: 84+this.inity,
            xtype: 'label',
            cls: 'schedulerBold',
            text: cs.res("textEndHour")
        },
        tthh,
        enddate,
        {
            x: 312+this.initx,
            y: 80+this.inity,
            width:50,
            border : false,
            xtype: 'panel',
            items: endButton
        },
        allday,
        
        {
          x: 370+this.initx,
          y: 59+this.inity,
          xtype: 'label',
          text: cs.res("allday")
        },
        timezone_id,
        { // TimeZone
            x: 370+this.initx,
            y: 84+this.inity,
            xtype: 'label',
            text: cs.res("timezone")+":"
        },
        eventtab
        ],
        baseCls: 'x-plain',
        layout:'absolute',
        buttonAlign:'center',
        beginDone: false,
        setWindow:function(w,cs) {
            this.win=w;
            this.cs=cs;
            this.promptclose=true;
            w.on('show',this.beginNew,this);
            w.on('beforeclose',this.closing,this);
        },
        beginNew:function() {
            
            if (!this.beginDone) {
              (function(){this.event.focus();}).defer(200,this);
              this.beginDone=true;
              if (this.event_id!=null) {
                  
                  eventtab.hideTabStripItem(recurrence);
                  if (this.recurr_id!=""){
                        if (this.isPlanning!='Y')   //se non � in planning  
                            eventtab.hideTabStripItem(this.planningGrid);
                        eventtab.unhideTabStripItem(recurrence);
                  }
              }else{
                  if (eventtab.frommail){
                        if (eventtab.frommailrec)   //se arriva da mail ed � con riccorenza
                            eventtab.unhideTabStripItem(recurrence);
                        else
                            eventtab.hideTabStripItem(recurrence);
                  }
                  
              }
              
            }
        },
        closing:function (p) {
          if (!this.promptclose) return true;
          var dirty = false;
          this.form.items.each(function(f){
            if (f.getName()!="" && f.isDirty()) {

              dirty = true;
              return false;
            }
          });
          if (dirty) {
            WT.confirm(
                this.cs.res("dialogClosingTitle"),
                this.cs.res("dialogClosingMessage"),
                this.closeConfirm,this
            );
            return false;
          }
          return true;
        },
        closeConfirm:function(bid,text,opt) {
          if (bid=="yes") {
            this.promptclose=false;
            this.win.close();
          }
        }
      }); 
      event.on('blur',function() {
          var vevent = this.event.getValue();
          if (vevent!=null && vevent.trim()!="")
            this.win.setTitle(vevent);
          else
            this.win.setTitle(this.formtitle);
        },form
      );
      event.on('specialkey',function(event,eo) {
          if (eo.getKey()==eo.RETURN)
              saveEvent.execute();
        },form
      );
      report_id.on('specialkey',function(event,eo) {
          if (eo.getKey()==eo.RETURN)
              saveEvent.execute();
        },form
      );
      customer_id.on('select',function() {
          this.statistic_id.clearValue();
          this.statistic_id.getStore().baseParams.customer_id=customer_id.getValue();
          this.statistic_id.getStore().load();
          this.causal.clearValue();
          this.causal.getStore().load();
        },form
      );
      customer_id.on('change',function() {
          this.statistic_id.clearValue();
          this.causal.clearValue();
          this.causal.getStore().load();
        },form
      );
      statistic_id.doQuery();
      statistic_id.on('beforequery',function(qe) {
          if (this.customer_id.getValue()==null || this.customer_id.getValue().trim()=="")
            return false;        
          this.statistic_id.store.filter('pid',this.customer_id.getValue(),true,true);
        },form
      );
      
      
      
      none_recurrence.on('focus',function() {
          permanent_recurrence.setDisabled(false);
          repeat_times.setDisabled(true);
          repeat_times_step.setDisabled(true);
          radio_until_yyyymmdd.setDisabled(true);
          until_yyyymmdd.setDisabled(true);
          dayly1.setDisabled(true);
          dayly_step.setDisabled(true);
          dayly2.setDisabled(true);
          weekly_step.setDisabled(true);
          weekly1.setDisabled(true);
          weekly2.setDisabled(true);
          weekly3.setDisabled(true);
          weekly4.setDisabled(true);
          weekly5.setDisabled(true);
          weekly6.setDisabled(true);
          weekly7.setDisabled(true);
          monthly_day.setDisabled(true);
          monthly_month.setDisabled(true);
          yearly_day.setDisabled(true);
          yearly_month.setDisabled(true);
        },form
      );
      dayly_recurrence.on('focus',function() {
          permanent_recurrence.setDisabled(false);
          repeat_times.setDisabled(false);
          repeat_times_step.setDisabled(true);
          radio_until_yyyymmdd.setDisabled(false);
          until_yyyymmdd.setDisabled(true);
          dayly1.setDisabled(false);
          dayly_step.setDisabled(false);
          dayly2.setDisabled(false);
          weekly_step.setDisabled(true);
          weekly1.setDisabled(true);
          weekly2.setDisabled(true);
          weekly3.setDisabled(true);
          weekly4.setDisabled(true);
          weekly5.setDisabled(true);
          weekly6.setDisabled(true);
          weekly7.setDisabled(true);
          monthly_day.setDisabled(true);
          monthly_month.setDisabled(true);
          yearly_day.setDisabled(true);
          yearly_month.setDisabled(true);
        },form
      );
      weekly_recurrence.on('focus',function() {
          permanent_recurrence.setDisabled(false);
          repeat_times.setDisabled(false);
          repeat_times_step.setDisabled(true);
          radio_until_yyyymmdd.setDisabled(false);
          until_yyyymmdd.setDisabled(true);
          dayly1.setDisabled(true);
          dayly_step.setDisabled(true);
          dayly2.setDisabled(true);
          weekly_step.setDisabled(false);
          weekly1.setDisabled(false);
          weekly2.setDisabled(false);
          weekly3.setDisabled(false);
          weekly4.setDisabled(false);
          weekly5.setDisabled(false);
          weekly6.setDisabled(false);
          weekly7.setDisabled(false);
          monthly_day.setDisabled(true);
          monthly_month.setDisabled(true);
          yearly_day.setDisabled(true);
          yearly_month.setDisabled(true);
        },form
      );
      monthly_recurrence.on('focus',function() {
          permanent_recurrence.setDisabled(false);
          repeat_times.setDisabled(false);
          repeat_times_step.setDisabled(true);
          radio_until_yyyymmdd.setDisabled(false);
          until_yyyymmdd.setDisabled(true);
          dayly1.setDisabled(true);
          dayly_step.setDisabled(true);
          dayly2.setDisabled(true);
          weekly_step.setDisabled(true);
          weekly1.setDisabled(true);
          weekly2.setDisabled(true);
          weekly3.setDisabled(true);
          weekly4.setDisabled(true);
          weekly5.setDisabled(true);
          weekly6.setDisabled(true);
          weekly7.setDisabled(true);
          monthly_day.setDisabled(false);
          monthly_month.setDisabled(false);
          yearly_day.setDisabled(true); 
          yearly_month.setDisabled(true);
          var dd = yyyymmdd.getValue().getDate();
          if (yyyymmdd.getValue().getDate()<10) dd="0"+dd;
          monthly_day.setValue(dd);
        },form
      );
      yearly_recurrence.on('focus',function() {
          permanent_recurrence.setDisabled(false);
          repeat_times.setDisabled(false);
          repeat_times_step.setDisabled(true);
          radio_until_yyyymmdd.setDisabled(false);
          until_yyyymmdd.setDisabled(true);
          dayly1.setDisabled(true);
          dayly_step.setDisabled(true);
          dayly2.setDisabled(true);
          weekly_step.setDisabled(true);
          weekly1.setDisabled(true);
          weekly2.setDisabled(true);
          weekly3.setDisabled(true);
          weekly4.setDisabled(true);
          weekly5.setDisabled(true);
          weekly6.setDisabled(true);
          weekly7.setDisabled(true);
          monthly_day.setDisabled(true);
          monthly_month.setDisabled(true);
          yearly_day.setDisabled(false);
          yearly_month.setDisabled(false);
          var dd = yyyymmdd.getValue().getDate();
          if (yyyymmdd.getValue().getDate()<10) dd="0"+dd;
          yearly_day.setValue(dd);
          var mm = yyyymmdd.getValue().getMonth()+1;
          //if (yyyymmdd.getValue().getMonth()<10) mm="0"+mm;
          yearly_month.setValue(mm);
        },form
      );
      
      radio_until_yyyymmdd.on('focus',function() {
          repeat_times.setValue(false);
          repeat_times_step.setDisabled(true);
          permanent_recurrence.setValue(false);
          until_yyyymmdd.setDisabled(false);
      },this);
      
      repeat_times.on('focus',function() {
          radio_until_yyyymmdd.setValue(false);
          permanent_recurrence.setValue(false);
          until_yyyymmdd.setDisabled(true);
          repeat_times_step.setDisabled(false);
      },this);
      
      permanent_recurrence.on('focus',function() {
          radio_until_yyyymmdd.setValue(false);
          repeat_times_step.setDisabled(true);
          repeat_times.setValue(false);
          until_yyyymmdd.setDisabled(true);
      },this);
      
      saveEvent.setHandler(function() {
          if (!this.form.isValid())
            return;
          if (this.recurr_id!=null && this.recurr_id!="") {
            WT.show({
              width:500,
              title:this.cs.res("dialogSavingTitle"),
              msg: this.cs.res("dialogRecurrMessage") +"<br/>"+"<br/>"+
                      "<table width='80%' style='font-size: 12px'>"+
                      "<tr><td width='70%'>"+WT.res("calendar","apply_event_single")+"</td><td><input type='radio' name='option' id='single' checked=true/></td></tr>"+
                      "<tr><td width='70%'>"+WT.res("calendar","apply_event_future")+"</td><td><input type='radio' name='option' id='future' /></td></tr>"+
                      "<tr><td width='70%'>"+WT.res("calendar","apply_event_all")+"</td><td><input type='radio' name='option' id='all'  /></td></tr>"+
                      "</table>",
              buttons: Ext.Msg.OKCANCEL,
              scope: form,
              fn: function(btn, text){
                if (btn == 'ok'){ 
                    if (Ext.get('all')!=null && Ext.get('all').dom.checked == true){  
                      this.cs.scheduler.saveEvent(this);
                      this.promptclose=false;
                      this.win.close();
                    } 
                    if (Ext.get('single')!=null && Ext.get('single').dom.checked == true){
                      this.old_recurr_id=this.recurr_id;    //se si spezza la senza salvo id ricorsione 
                      this.recurr_id=null;
                      this.cs.scheduler.saveEvent(this);
                      this.promptclose=false;
                      this.win.close();
                    }
                    if (Ext.get('future')!=null && Ext.get('future').dom.checked == true){
                        this.type="future";
                        this.cs.scheduler.saveEvent(this);
                        this.promptclose=false;
                        this.win.close();
                    }
                }
              }
            });
            return;
          }
          this.cs.scheduler.saveEvent(this);
          this.promptclose=false;
          this.win.close();
        },form
      );
      deleteEvent.setHandler(function() {
          if (this.form.isDirty() || this.event_id!=null) {
            if (this.recurr_id!=null && this.recurr_id!="") {
              WT.show({
                width:500,
                title:this.cs.res("dialogDeletingTitle"),
                msg: this.cs.res("dialogRecurrMessage") +"<br/>"+"<br/>"+
                      "<table width='70%' style='font-size: 12px'>"+
                      "<tr><td width='60%'>"+WT.res("calendar","event_single")+"</td><td><input type='radio' name='option' id='single' checked=true /></td></tr>"+
                      "<tr><td width='60%'>"+WT.res("calendar","event_future")+"</td><td><input type='radio' name='option' id='future' / ></td></tr>"+
                      "<tr><td width='60%'>"+WT.res("calendar","event_all")+"</td><td><input type='radio' name='option' id='all' checked=true /></td></tr>"+
                      "</table>",
                buttons: Ext.Msg.OKCANCEL,
                scope: form,
                fn: function(btn, text){
                  if (btn == 'ok'){
                    if (Ext.get('all')!=null && Ext.get('all').dom.checked == true){
                      this.cs.scheduler.deleteEvent(this.event_id,this.recurr_id);
                      this.promptclose=false;
                      this.win.close();
                    }
                    if (Ext.get('single')!=null && Ext.get('single').dom.checked == true){
                      this.old_recurr_id=this.recurr_id;    //se si spezza la senza salvo id ricorsione
                      var yyyy = this.yyyymmdd.getValue().getFullYear(); //se si spezza la senza salvo date evento
                      var mm = ""+(this.yyyymmdd.getValue().getMonth()+1);
                      if ((this.yyyymmdd.getValue().getMonth()+1)<10) mm="0"+mm;
                      var dd = ""+this.yyyymmdd.getValue().getDate();
                      if (this.yyyymmdd.getValue().getDate()<10) dd="0"+dd;
                      this.recurr_id=null;
                      this.cs.scheduler.deleteEvent(this.event_id,null,this.old_recurr_id,dd,mm,yyyy,"single");
                      this.promptclose=false;
                      this.win.close();
                    }
                    if (Ext.get('future')!=null && Ext.get('future').dom.checked == true){
                        this.old_recurr_id=this.recurr_id;    //se si spezza la senza salvo id ricorsione
                        var yyyy = this.yyyymmdd.getValue().getFullYear(); //se si spezza la senza salvo date evento
                        var mm = ""+(this.yyyymmdd.getValue().getMonth()+1);
                        if ((this.yyyymmdd.getValue().getMonth()+1)<10) mm="0"+mm;
                        var dd = ""+this.yyyymmdd.getValue().getDate();
                        if (this.yyyymmdd.getValue().getDate()<10) dd="0"+dd;
                        this.recurr_id=null;
                        this.cs.scheduler.deleteEvent(this.event_id,null,this.old_recurr_id,dd,mm,yyyy,"future");
                        this.promptclose=false;
                        this.win.close();
                    }
                    }
                }
              });
              return;
            }
            WT.show({
              title:this.cs.res("dialogDeletingTitle"),
              msg: this.cs.res("dialogDeletingMessage"),
              buttons: Ext.Msg.YESNOCANCEL,
              scope: form,
              fn: function(btn, text){

                if (btn == 'yes'){
                  if (this.event_id!=null && this.event_id!="") {
                    this.cs.scheduler.deleteEvent(this.event_id,this.recurr_id);
                  }
                  this.promptclose=false;
                  this.win.close();
                } else if (btn == 'no' && this.event_id!=null){
                  this.promptclose=false;
                  this.win.close();
                }
              }
            });
            return;
          }
          this.promptclose=false;
          this.win.close();
        },form
      );
        
      printEvent.setHandler(function() {
          this.cs.scheduler.printEvent(this);
          this.promptclose=false;
      },form);  
      
      restoreRecurrence.setHandler(function() {
          WT.show({
            title:this.cs.res("dialogRestoreRecurrence"),
            msg: this.cs.res("dialogRestoreRecurrenceMessage"),
            buttons: Ext.Msg.YESNOCANCEL,
            scope: this,
            fn: function(btn, text){
              if (btn == 'yes'){
               this.cs.scheduler.restoreRecurrence(this.event_id);
               this.promptclose=false;
               this.win.close();
              } else if (btn == 'no') {
                this.promptclose=false;
                this.win.close();
              }
            }
          });
          },form);
      
      availablePlanning.setHandler(function(){
          this.cs.scheduler.available(this);
      },form);
      
      return form;
    }

    
});


