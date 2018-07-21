import $ from 'jquery';
import * as moment from 'moment';
import 'fullcalendar';
import tippy from 'tippy.js';

import '../../../node_modules/fullcalendar/dist/fullcalendar.css';
import '../css/index.css';

import { parseHashParams, serializeHashParams } from './hashParams.js';

const hashParams = parseHashParams(window.location.hash);

$(function() {
  function $tooltip(event, body, closeFn) {
    var $head = $('<div class="tooltip-head"></div>')
      .append(event.icon)
      .append($('<a class="tooltip-title"></a>').attr('href', event.url).text(event.title))
      .append($('<span class="tooltip-close">&#10006;</span>'));
    $head.find('.tooltip-close').click(closeFn);
    var $body = $('<div class="tooltip-body"></div>')
      .append(
        '<div class="left">' +
          event.timestampString + '<br>' +
          event.durationString +
        '</div>' +
        '<div class="right">' +
          body +
        '</div>')
      .append('<div style="clear:both"></div>');
    return $('<div></div>').append($head).append($body);
  }

  $('#calendar-view').fullCalendar({
    events: 'events',
    defaultView: hashParams['view'] || CalendarViewOptions.defaultView,
    defaultDate: hashParams['date'] || moment(),
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
      window.location = serializeHashParams({date: view.calendar.currentDate.format('YYYY-MM-DD'), view: view.type});
    },
    eventMouseover: function(event, jsEvent, view) {
      var target = jsEvent.target || jsEvent.srcElement;

      var body;
      if (event.future) {
        body = '<b>' + CalendarViewOptions.popupText.buildHistory + '</b>';
        if (event.builds.length > 0) {
          body += '<ul>' + event.builds.map(function(build) {
            var link = $('<a></a>').attr('href', build.url).text(build.name);
            var date = $('<time></time>').text(moment(build.start).format(CalendarViewOptions.formats[view.type].popupBuildTimeFormat));
            return $('<li></li>').append(build.icon).append(' ').append(link).append(' ').append(date)[0].outerHTML;
          }).join('') + '</ul>';
        } else {
          body += $('<ul><li></li></ul>').text(CalendarViewOptions.popupText.buildHistoryEmpty)[0].outerHTML;
        }
      } else {
        body = '<div></div>';
      }

      var $tooltipHtml = $tooltip(event, body, function() { tooltip.hide(); });

      var tooltip = tippy.one($(target).closest('.fc-event')[0], {
        html: $tooltipHtml[0],
        arrow: true,
        animation: 'fade',
        interactive: true,
        theme: 'jenkins',
        size: 'large',
        onHidden: function() {
          tooltip.destroy();
        }
      });
    }
  });
});
