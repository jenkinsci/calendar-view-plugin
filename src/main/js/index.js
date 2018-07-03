import $ from 'jquery';
import 'fullcalendar';

import '../../../node_modules/fullcalendar/dist/fullcalendar.css';
import '../css/index.css';

$(function() {
  $('#calendar-view').fullCalendar({
     events: 'events'
  })
});