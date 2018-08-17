/*
 * The MIT License
 *
 * Copyright (c) 2018 Sven Schoenung
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import $ from 'jquery';
import moment from 'moment';
import 'fullcalendar';

import '../../../node_modules/fullcalendar/dist/fullcalendar.css';
import '../css/index.css';

import * as hashParams from './hash-params.js';
import * as events from './events.js';

const hashParamOptions = hashParams.parse(window.location.hash);

var timeout = null;

$(function() {
  $('#calendar-view').fullCalendar({
    events: 'events',
    defaultView: hashParamOptions['view'] || CalendarViewOptions.defaultView,
    defaultDate: hashParamOptions['date'] || moment(),
    header: {
      left: 'month-view,week-view,day-view',
      center: 'title',
      right: 'today prev,next'
    },
    views: {
      'month-view': {
        type: 'month',
        titleFormat: CalendarViewOptions.formats['month-view'].titleFormat,
        columnHeaderFormat: CalendarViewOptions.formats['month-view'].columnHeaderFormat,
        timeFormat: CalendarViewOptions.formats['month-view'].timeFormat
      },
      'week-view': {
        type: 'agendaWeek',
        allDaySlot: false,
        agendaEventMinHeight: 16,
        nowIndicator: true,
        titleFormat: CalendarViewOptions.formats['week-view'].titleFormat,
        columnHeaderFormat: CalendarViewOptions.formats['week-view'].columnHeaderFormat,
        timeFormat: CalendarViewOptions.formats['week-view'].timeFormat,
        slotLabelFormat: CalendarViewOptions.formats['week-view'].slotLabelFormat,
        slotDuration: CalendarViewOptions.slotSettings['week-view'].slotDuration,
        minTime: CalendarViewOptions.slotSettings['week-view'].minTime,
        maxTime: CalendarViewOptions.slotSettings['week-view'].maxTime
      },
      'day-view': {
        type: 'agenda',
        allDaySlot: false,
        agendaEventMinHeight: 16,
        duration: { days: 1 },
        nowIndicator: true,
        titleFormat: CalendarViewOptions.formats['day-view'].titleFormat,
        columnHeaderFormat: CalendarViewOptions.formats['day-view'].columnHeaderFormat,
        timeFormat: CalendarViewOptions.formats['day-view'].timeFormat,
        slotLabelFormat: CalendarViewOptions.formats['day-view'].slotLabelFormat,
        slotDuration: CalendarViewOptions.slotSettings['day-view'].slotDuration,
        minTime: CalendarViewOptions.slotSettings['day-view'].minTime,
        maxTime: CalendarViewOptions.slotSettings['day-view'].maxTime
      }
    },
    weekends: CalendarViewOptions.weekSettings.weekends,
    firstDay: CalendarViewOptions.weekSettings.firstDay,
    monthNames: CalendarViewOptions.names.monthNames,
    monthNamesShort: CalendarViewOptions.names.monthNamesShort,
    dayNames: CalendarViewOptions.names.dayNames,
    dayNamesShort: CalendarViewOptions.names.dayNamesShort,
    buttonText: CalendarViewOptions.buttonText,
    viewRender: function(view, element) {
      window.location = hashParams.serialize({date: view.calendar.currentDate.format('YYYY-MM-DD'), view: view.type});
    },
    eventAfterAllRender: function(view) {
      events.select();
    },
    eventMouseover: function(event, jsEvent, view) {
      timeout = setTimeout(function() {
        events.select({eventId: event.id, view: view});
        timeout = null;
      }, events.selectTimeout());
    },
    eventMouseout: function(event, jsEvent, view) {
      if (timeout) {
        clearTimeout(timeout);
        timeout = null;
      }
    }
  });
});
