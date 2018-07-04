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
        left: 'MONTH WEEK DAY',
        center: 'title',
        right: 'today prev next',
     },
     views: {
        MONTH: {
            type: 'month',
            buttonText: 'month',
        },
        WEEK: {
            type: 'agendaWeek',
            buttonText: 'week',
            allDaySlot: false,
            agendaEventMinHeight: 20,
        },
        DAY: {
            type: 'agenda',
            buttonText: 'day',
            allDaySlot: false,
            agendaEventMinHeight: 20,
            duration: { days: 1 },
        }
     },
     viewRender: function(view, element) {
        window.location = serializeHashParams({date: view.calendar.currentDate.format('YYYY-MM-DD'), type: view.type});
     }
  })
});
