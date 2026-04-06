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
import * as scroll from './scroll.js';

const hashParamOptions = hashParams.parse(window.location.hash);

var timeout = null;

$(function() {
  const root = document.documentElement;
  root.style.setProperty('--result-success-color', CalendarViewOptions.colors.success);
  root.style.setProperty('--result-success-text-color', CalendarViewOptions.colors.successText);
  root.style.setProperty('--result-success-selected-color', CalendarViewOptions.colors.successSelected);
  root.style.setProperty('--result-success-selected-text-color', CalendarViewOptions.colors.successSelectedText);
  root.style.setProperty('--result-unstable-color', CalendarViewOptions.colors.unstable);
  root.style.setProperty('--result-unstable-text-color', CalendarViewOptions.colors.unstableText);
  root.style.setProperty('--result-unstable-selected-color', CalendarViewOptions.colors.unstableSelected);
  root.style.setProperty('--result-unstable-selected-text-color', CalendarViewOptions.colors.unstableSelectedText);
  root.style.setProperty('--result-failure-color', CalendarViewOptions.colors.failure);
  root.style.setProperty('--result-failure-text-color', CalendarViewOptions.colors.failureText);
  root.style.setProperty('--result-failure-selected-color', CalendarViewOptions.colors.failureSelected);
  root.style.setProperty('--result-failure-selected-text-color', CalendarViewOptions.colors.failureSelectedText);
  root.style.setProperty('--result-aborted-color', CalendarViewOptions.colors.aborted);
  root.style.setProperty('--result-aborted-text-color', CalendarViewOptions.colors.abortedText);
  root.style.setProperty('--result-aborted-selected-color', CalendarViewOptions.colors.abortedSelected);
  root.style.setProperty('--result-aborted-selected-text-color', CalendarViewOptions.colors.abortedSelectedText);
  root.style.setProperty('--result-scheduled-color', CalendarViewOptions.colors.scheduled);
  root.style.setProperty('--result-scheduled-text-color', CalendarViewOptions.colors.scheduledText);
  root.style.setProperty('--result-scheduled-selected-color', CalendarViewOptions.colors.scheduledSelected);
  root.style.setProperty('--result-scheduled-selected-text-color', CalendarViewOptions.colors.scheduledSelectedText);

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
    weekNumbers: CalendarViewOptions.weekSettings.weekNumbers,
    monthNames: CalendarViewOptions.names.monthNames,
    monthNamesShort: CalendarViewOptions.names.monthNamesShort,
    dayNames: CalendarViewOptions.names.dayNames,
    dayNamesShort: CalendarViewOptions.names.dayNamesShort,
    buttonText: CalendarViewOptions.buttonText,
    navLinks: true,
    viewRender: function(view, element) {
      window.location = hashParams.serialize({ date: view.calendar.currentDate.format('YYYY-MM-DD'), view: view.type });
    },
    eventAfterAllRender: function(view) {
      events.select();
      scroll.toSelected();
    },
    eventMouseover: function(event, jsEvent, view) {
      timeout = setTimeout(function() {
        events.select({ eventId: event.id, view: view });
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
