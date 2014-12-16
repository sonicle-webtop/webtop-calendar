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

Ext.define('Sonicle.webtop.calendar.CustomDatePickerBase', {
	extend: 'Ext.picker.Date',
	parent_panel:null,
        first:true,
        startDay: 1,
	renderTpl: [
        '<div id="{id}-innerEl" data-ref="innerEl">',
            '<div style="height: 27px;" class="{baseCls}-header">',
                '<div id="{id}-prevEl" data-ref="prevEl" class="{baseCls}-prev {baseCls}-arrow" role="button" title="{prevText}"></div>',
                '<div id="{id}-middleBtnEl" data-ref="middleBtnEl" class="{baseCls}-month" role="heading">{%this.renderMonthBtn(values, out)%}</div>',
                '<div id="{id}-nextEl" data-ref="nextEl" class="{baseCls}-next {baseCls}-arrow" role="button" title="{nextText}"></div>',
            '</div>',
            '<table align="center" width=20px role="grid" id="{id}-eventEl" data-ref="eventEl"  {%',
                // If the DatePicker is focusable, make its eventEl tabbable.
                'if (values.$comp.focusable) {out.push("tabindex=\\\"0\\\"");}',
            '%} cellspacing="0">',
                '<thead><tr role="row">',
                    '<tpl for="dayNames">',
                        '<th role="columnheader" class="{parent.baseCls}-column-header" aria-label="{.}">',
                            '<div role="presentation" class="{parent.baseCls}-column-header-inner">{.:this.firstInitial}</div>',
                        '</th>',
                    '</tpl>',
                '</tr></thead>',
                '<tbody><tr role="row">',
                    '<tpl for="days">',
                        '{#:this.isEndOfWeek}',
                        '<td role="gridcell" id="{[Ext.id()]}">',
                            '<div hidefocus="on" class="{parent.baseCls}-date" style="padding: 0 3px 0 0;font: 300 13px helvetica, arial, verdana, sans-serif;cursor: pointer;line-height: 15px;"></div>',
                        '</td>',
                    '</tpl>',
                '</tr></tbody>',
            '</table>',
            '<tpl if="showToday">',
                '<div id="{id}-footerEl" data-ref="footerEl" role="presentation" class="{baseCls}-footer">{%this.renderTodayBtn(values, out)%}</div>',
            '</tpl>',
        '</div>',
        {
            firstInitial: function(value) {
                return Ext.picker.Date.prototype.getDayInitial(value);
            },
            isEndOfWeek: function(value) {
                // convert from 1 based index to 0 based
                // by decrementing value once.
                value--;
                var end = value % 7 === 0 && value !== 0;
                return end ? '</tr><tr role="row">' : '';
            },
            renderTodayBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.todayBtn.getRenderTree(), out);
            },
            renderMonthBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.monthBtn.getRenderTree(), out);
            }
        }
    ],
    
    fullUpdate: function(date) {
        var me = this,
            cells = me.cells.elements,
            textNodes = me.textNodes,
            disabledCls = me.disabledCellCls,
            eDate = Ext.Date,
            i = 0,
            extraDays = 0,
            newDate = +eDate.clearTime(date, true),
            today = +eDate.clearTime(new Date()),
            min = me.minDate ? eDate.clearTime(me.minDate, true) : Number.NEGATIVE_INFINITY,
            max = me.maxDate ? eDate.clearTime(me.maxDate, true) : Number.POSITIVE_INFINITY,
            ddMatch = me.disabledDatesRE,
            ddText = me.disabledDatesText,
            ddays = me.disabledDays ? me.disabledDays.join('') : false,
            ddaysText = me.disabledDaysText,
            format = me.format,
            days = eDate.getDaysInMonth(date),
            firstOfMonth = eDate.getFirstDateOfMonth(date),
            startingPos = firstOfMonth.getDay() - me.startDay,
            previousMonth = eDate.add(date, eDate.MONTH, -1),
            ariaTitleDateFormat = me.ariaTitleDateFormat,
            prevStart, current, disableToday, tempDate, setCellClass, html, cls,
            formatValue, value;

        if (startingPos < 0) {
            startingPos += 7;
        }

        days += startingPos;
        prevStart = eDate.getDaysInMonth(previousMonth) - startingPos;
        current = new Date(previousMonth.getFullYear(), previousMonth.getMonth(), prevStart, me.initHour);

        if (me.showToday) {
            tempDate = eDate.clearTime(new Date());
            disableToday = (tempDate < min || tempDate > max ||
                (ddMatch && format && ddMatch.test(eDate.dateFormat(tempDate, format))) ||
                (ddays && ddays.indexOf(tempDate.getDay()) != -1));

            if (!me.disabled) {
                me.todayBtn.setDisabled(disableToday);
            }
        }

        setCellClass = function(cellIndex, cls){
            var cell = cells[cellIndex];
            
            value = +eDate.clearTime(current, true);
            cell.setAttribute('aria-label', eDate.format(current, ariaTitleDateFormat));
            // store dateValue number as an expando
            cell.firstChild.dateValue = value;
            if (value == today) {
                cls += ' ' + me.todayCls;
                cell.firstChild.title = me.todayText;
                
                // Extra element for ARIA purposes
                me.todayElSpan = Ext.DomHelper.append(cell.firstChild, {
                    tag: 'span',
                    cls: Ext.baseCSSPrefix + 'hidden-clip',
                    html: me.todayText
                }, true);
            }
            if (value == newDate) {
                me.activeCell = cell;
                me.eventEl.dom.setAttribute('aria-activedescendant', cell.id);
                cell.setAttribute('aria-selected', true);
                cls += ' ' + me.selectedCls;
                me.fireEvent('highlightitem', me, cell);
            } else {
                cell.setAttribute('aria-selected', false);
            }
            if (value < min) {
                cls += ' ' + disabledCls;
                cell.setAttribute('aria-label', me.minText);
            }
            else if (value > max) {
                cls += ' ' + disabledCls;
                cell.setAttribute('aria-label', me.maxText);
            }
            else if (ddays && ddays.indexOf(current.getDay()) !== -1){
                cell.setAttribute('aria-label', ddaysText);
                cls += ' ' + disabledCls;
            }
            else if (ddMatch && format){
                formatValue = eDate.dateFormat(current, format);
                if(ddMatch.test(formatValue)){
                    cell.setAttribute('aria-label', ddText.replace('%0', formatValue));
                    cls += ' ' + disabledCls;
                }
            }
            cell.className = cls + ' ' + me.cellCls;
        };

        for(; i < me.numDays; ++i) {
            if (i < startingPos) {
                html = (++prevStart);
                cls = me.prevCls;
            } else if (i >= days) {
                html = (++extraDays);
                cls = me.nextCls;
            } else {
                html = i - startingPos + 1;
                cls = me.activeCls;
            }
            textNodes[i].innerHTML = html;
            current.setDate(current.getDate() + 1);
            setCellClass(i, cls);
        }

        me.monthBtn.setText(Ext.Date.format(date, me.monthYearFormat));
    },

    
	initEvents: function(){
        var me = this,
            pickerField = me.pickerField,
            eDate = Ext.Date,
            day = eDate.DAY;

        me.callParent(arguments);

        // If this is not focusable (eg being used as the picker of a DateField)
        // then prevent mousedown from blurring the input field.
        if (!me.focusable) {
            me.el.on({
                mousedown: me.onMouseDown
            });
        }

        me.prevRepeater = new Ext.util.ClickRepeater(me.prevEl, {
            handler: me.showPrevMonth,
            scope: me,
            preventDefault: true,
            stopDefault: true
        });

        me.nextRepeater = new Ext.util.ClickRepeater(me.nextEl, {
            handler: me.showNextMonth,
            scope: me,
            preventDefault: true,
            stopDefault: true
        });

        // Read key events through our pickerField if we are bound to one
        me.keyNav = new Ext.util.KeyNav(pickerField ? pickerField.inputEl : me.eventEl, Ext.apply({
            scope: me,

            // Must capture event so that the Picker sees it before the Field.
            capture: true,

            left: function(e) {
                if (e.ctrlKey) {
                    me.showPrevMonth();
                } else {
                    me.update(eDate.add(me.activeDate, day, -1));
                }
            },

            right: function(e){
                if (e.ctrlKey) {
                    me.showNextMonth();
                } else {
                    me.update(eDate.add(me.activeDate, day, 1));
                }
            },

            up: function(e) {
                if (e.ctrlKey) {
                    me.showNextYear();
                } else {
                    me.update(eDate.add(me.activeDate, day, -7));
                }
            },

            down: function(e) {
                if (e.ctrlKey) {
                    me.showPrevYear();
                } else {
                    me.update(eDate.add(me.activeDate, day, 7));
                }
            },

            pageUp: function(e) {
                if (e.ctrlKey) {
                    me.showPrevYear();
                } else {
                    me.showPrevMonth();
                }
            },

            pageDown: function(e) {
                if (e.ctrlKey) {
                    me.showNextYear();
                } else {
                    me.showNextMonth();
                }
            },

            tab: function (e) {
                me.handleTabClick(e);
                
                // Allow default behaviour of TAB - it MUST be allowed to navigate.
                return true;
            },

            enter: function(e) {
                me.handleDateClick(e, me.activeCell.firstChild);
            },

            space: function() {
                me.setValue(new Date(me.activeCell.firstChild.dateValue));
                var startValue = me.startValue,
                    value = me.value,
                    pickerValue;

                if (pickerField) {
                    pickerValue = pickerField.getValue();
                    if (pickerValue && startValue && pickerValue.getTime() === value.getTime()) {
                        pickerField.setValue(startValue);
                    } else {
                        pickerField.setValue(value);
                    }
                }
            },

            home: function(e) {
                me.update(eDate.getFirstDateOfMonth(me.activeDate));
            },

            end: function(e) {
                me.update(eDate.getLastDateOfMonth(me.activeDate));
            }
        }, me.keyNavConfig));

        if (me.disabled) {
            me.syncDisabled(true);
        }
        me.update(me.value);
    },
	
    selectedUpdate: function(date) {
        var me        = this,
            t         = date.getTime(),
            cells     = me.cells,
            cls       = me.selectedCls,
            c,
            cLen      = cells.getCount(),
            cell;
        
        if (!me.first){
            cell = me.activeCell;
            if (cell) {
                Ext.fly(cell).removeCls(cls);
                cell.setAttribute('aria-selected', false);
            }

                    var cell_item_picker=this.parent_panel.datepicker2.activeCell;
                    if (cell_item_picker){
                            Ext.fly(cell_item_picker).removeCls(cls);
                    }

                    for (c = 0; c < cLen; c++) {
                            cell = cells.item(c)
                            Ext.fly(cell).removeCls(cls);
                    }
                    if (this.parent_panel.datepicker2.cells){
                        for (c = 0; c < this.parent_panel.datepicker2.cells.getCount(); c++) {
                                cell = this.parent_panel.datepicker2.cells.item(c)
                                Ext.fly(cell).removeCls(cls);
                        }
                    }
                    var first = date.getDate() - date.getDay() +1; // First day is the day of the month - the day of the week
                    var last = first + 4; // last day is the first day + 6

                    var firstday = new Date(date.setDate(first));
                    var lastday = new Date(date.setDate(last));
                    //alert ("firstday="+firstday+" lastday="+lastday);

            for (c = 0; c < cLen; c++) {
                cell = cells.item(c);
                /*if (me.textNodes[c].dateValue >=firstday.getTime() && me.textNodes[c].dateValue<=lastday.getTime()) {
                    me.activeCell = cell.dom;
                    me.eventEl.dom.setAttribute('aria-activedescendant', cell.dom.id);
                    cell.dom.setAttribute('aria-selected', true);
                    cell.addCls(cls);
                    me.fireEvent('highlightitem', me, cell);
                    if (me.textNodes[c].dateValue==lastday.getTime()) break;

                }*/
                if (me.textNodes[c].dateValue ==t) {
                    me.activeCell = cell.dom;
                    me.eventEl.dom.setAttribute('aria-activedescendant', cell.dom.id);
                    cell.dom.setAttribute('aria-selected', true);
                    cell.addCls(cls);
                    me.fireEvent('highlightitem', me, cell);
                    //break;

                }
                if (me.textNodes[c].dateValue >=firstday.getTime() && me.textNodes[c].dateValue<=lastday.getTime()) {

                    cell.dom.setAttribute('aria-selected', true);
                    cell.addCls(cls);
                    if (me.textNodes[c].dateValue==lastday.getTime()) break;

                }

            }
        }
        me.first=false;
		
    },
	
    showPrevMonth: function(e) {
        this.parent_panel.datepicker2.setValue(Ext.Date.add(this.parent_panel.datepicker2.activeDate, Ext.Date.MONTH, -1));
        return this.setValue(Ext.Date.add(this.activeDate, Ext.Date.MONTH, -1));
    },
	
    showNextMonth: function(e) {
        this.parent_panel.datepicker2.setValue(Ext.Date.add(this.parent_panel.datepicker2.activeDate, Ext.Date.MONTH,1));
        return this.setValue(Ext.Date.add(this.activeDate, Ext.Date.MONTH, 1));
    },
    
    update: function(date, forceRefresh) {
        var me = this,
            active = me.activeDate;

        if (me.rendered) {
            me.activeDate = date;
            if(!forceRefresh && active && me.el && active.getMonth() == date.getMonth() && active.getFullYear() == date.getFullYear()){
                me.selectedUpdate(date, active);
            } else {
                me.fullUpdate(date, active);
                if (date && active && date.getMonth() < active.getMonth() && me.parent_panel.datepicker2 &&  me.parent_panel.datepicker2.activeDate) me.parent_panel.datepicker2.fullUpdate((Ext.Date.add(date, Ext.Date.MONTH, 1)),me.parent_panel.datepicker2.activeDate);
                else if (active && date && date.getMonth() > active.getMonth() && me.parent_panel.datepicker2 &&  me.parent_panel.datepicker2.activeDate) me.parent_panel.datepicker2.fullUpdate((Ext.Date.add(date, Ext.Date.MONTH, 1)),me.parent_panel.datepicker2.activeDate);
            }
        }
        return me;
    },
    
    createMonthPicker: function() {
        var me = this,
            picker = me.monthPicker;

        if (!picker) {
            me.monthPicker = picker = new Ext.picker.Month({
                renderTo: me.el,
                floating: true,
                padding: me.padding,
                shadow: false,
                small: true,
                renderTpl: [
                    '<div id="{id}-bodyEl" data-ref="bodyEl" class="{baseCls}-body">',
                      '<div id="{id}-monthEl" data-ref="monthEl" class="{baseCls}-months">',
                          '<tpl for="months">',
                              '<div class="{parent.baseCls}-item {parent.baseCls}-month">',
                                  '<a style="{parent.monthStyle} height:15px; font: 200 10px helvetica, arial, verdana, sans-serif;"  role="button" hidefocus="on" class="{parent.baseCls}-item-inner">{.}</a>',
                              '</div>',
                          '</tpl>',
                      '</div>',
                      '<div id="{id}-yearEl" data-ref="yearEl" class="{baseCls}-years">',
                          '<div class="{baseCls}-yearnav">',
                              '<div class="{baseCls}-yearnav-button-ct">',
                                  '<a id="{id}-prevEl" data-ref="prevEl" class="{baseCls}-yearnav-button {baseCls}-yearnav-prev" hidefocus="on" role="button"></a>',
                              '</div>',
                              '<div class="{baseCls}-yearnav-button-ct">',
                                  '<a id="{id}-nextEl" data-ref="nextEl" class="{baseCls}-yearnav-button {baseCls}-yearnav-next" hidefocus="on" role="button"></a>',
                              '</div>',
                          '</div>',
                          '<tpl for="years">',
                              '<div class="{parent.baseCls}-item {parent.baseCls}-year">',
                                  '<a style="height:14px; font: 200 10px helvetica, arial, verdana, sans-serif;" hidefocus="on" class="{parent.baseCls}-item-inner" role="button">{.}</a>',
                              '</div>',
                          '</tpl>',
                      '</div>',
                      '<div class="' + Ext.baseCSSPrefix + 'clear"></div>',
                      '<tpl if="showButtons">',
                          '<div class="{baseCls}-buttons">{%',
                              'var me=values.$comp, okBtn=me.okBtn, cancelBtn=me.cancelBtn;',
                              'okBtn.ownerLayout = cancelBtn.ownerLayout = me.componentLayout;',
                              'okBtn.ownerCt = cancelBtn.ownerCt = me;',
                              'Ext.DomHelper.generateMarkup(okBtn.getRenderTree(), out);',
                              'Ext.DomHelper.generateMarkup(cancelBtn.getRenderTree(), out);',
                          '%}</div>',
                      '</tpl>',
                    '</div>'
                ],
                listeners: {
                    scope: me,
                    cancelclick: me.onCancelClick,
                    okclick: me.onOkClick,
                    yeardblclick: me.onOkClick,
                    monthdblclick: me.onOkClick
                }
            });
            if (!me.disableAnim) {
                // hide the element if we're animating to prevent an initial flicker
                picker.el.setStyle('display', 'none');
            }
            picker.hide();
            me.on('beforehide', me.doHideMonthPicker, me);
        }
        return picker;
    },
    
    onOkClick: function(picker, value) {
        var me = this,
            month = value[0],
            year = value[1],
            date = new Date(year, month, me.getActive().getDate());

        if (date.getMonth() !== month) {
            // 'fix' the JS rolling date conversion if needed
            date = Ext.Date.getLastDateOfMonth(new Date(year, month, 1));
        }
        me.setValue(date);
        this.parent_panel.datepicker2.setValue(Ext.Date.add(date, Ext.Date.MONTH, 1));
        me.hideMonthPicker();
    },
	
});