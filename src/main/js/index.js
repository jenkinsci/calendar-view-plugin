import $ from 'jquery';
import * as moment from 'moment';
import 'fullcalendar';

import '../../../node_modules/fullcalendar/dist/fullcalendar.css';
import '../css/index.css';

import { parseHashParams, serializeHashParams } from './hashParams.js';

const hashParams = parseHashParams(window.location.hash);

console.log(hashParams);

$(function() {
  const calendar = $('#calendar-view').fullCalendar({
     events: 'events',
     defaultView: hashParams['view'] || 'month',
     defaultDate: hashParams['date'] || moment(),
     viewRender: function(view, element) {
        window.location = serializeHashParams({date: view.calendar.currentDate.format('YYYY-MM-DD'), type: view.type});
     }
  })
});
