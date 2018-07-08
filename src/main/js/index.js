import $ from 'jquery';
import * as moment from 'moment';
import 'fullcalendar';

import '../../../node_modules/fullcalendar/dist/fullcalendar.css';
import '../css/index.css';

import { parseHashParams, serializeHashParams } from './hashParams.js';

const hashParams = parseHashParams(window.location.hash);

$(function() {
  const calendar = $('#calendar-view').fullCalendar({
     events: 'events',
     defaultView: hashParams['view'] || CalendarViewOptions.defaultView,
     defaultDate: hashParams['date'] || moment(),
     header: {
        left: 'month-view,week-view,day-view',
        center: 'title',
        right: 'today prev,next',
     },
     views: {
        'month-view': {
            type: 'month',
            titleFormat: CalendarViewOptions.formats['month-view'].titleFormat,
            columnHeaderFormat: CalendarViewOptions.formats['month-view'].columnHeaderFormat,
            timeFormat: CalendarViewOptions.formats['month-view'].timeFormat,
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
            maxTime: CalendarViewOptions.slotSettings['week-view'].maxTime,
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
            maxTime: CalendarViewOptions.slotSettings['day-view'].maxTime,
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
        window.location = serializeHashParams({date: view.calendar.currentDate.format('YYYY-MM-DD'), view: view.type});
     }
  })
});
