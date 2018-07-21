import $ from 'jquery';
import * as moment from 'moment';
import tippy from 'tippy.js';

function popup(event, view, options) {
  return $('<div></div>')
    .append(head(event, options))
    .append(body(event, view));
}

function head(event, options) {
  var $head = $('<div class="tooltip-head"></div>')
    .append(event.icon)
    .append($('<a class="tooltip-title"></a>').attr('href', event.url).text(event.title))
    .append($('<span class="tooltip-close">&#10006;</span>'));
  $head.find('.tooltip-close').click(options.close);
  return $head;
}

function body(event, view) {
  return $('<div class="tooltip-body"></div>')
    .append(left(event))
    .append(right(event, view))
    .append(bottom(event, view));
}

function left(event) {
  return $('<div class="tooltip-left"></div>')
    .append($('<span class="timestamp"></span>').text(event.timestampString))
    .append('<br/>')
    .append($('<span class="duration"></span>').text(event.durationString));
}

function right(event, view) {
  return (event.future) ? rightForFutureBuild(event, view) : rightForPastBuild(event, view);
}

function rightForPastBuild(event, view) {
  return $('<div class="tooltip-right"></div>');
}

function rightForFutureBuild(event, view) {
  return $('<div class="tooltip-right"></div>')
    .append($('<b></b>').text(CalendarViewOptions.popupText.buildHistory))
    .append($('<ul></ul>').append((event.builds.length > 0) ? buildHistory(event, view) : noBuildHistory()));
}

function noBuildHistory() {
  return $('<li></div>').text(CalendarViewOptions.popupText.buildHistoryEmpty);
}

function buildHistory(event, view) {
  return event.builds.map(function(build) {
    var link = $('<a></a>').attr('href', build.url).text(build.name);
    var date = $('<time></time>').text(moment(build.start).format(CalendarViewOptions.formats[view.type].popupBuildTimeFormat));
    return $('<li></li>').append(build.icon).append(' ').append(link).append(' ').append(date);
  });
}

function bottom(event) {
  return $('<div class="bottom"></div>');
}

export function show(event, view, target) {
  var $popup = popup(event, view, {
    close: function() { popupInstance.hide(); }
  });

  var popupInstance = tippy.one(target, {
    html: $popup[0],
    arrow: true,
    animation: 'fade',
    interactive: true,
    theme: 'jenkins',
    size: 'large',
    onHidden: function() {
      popupInstance.destroy();
    }
  });
}
