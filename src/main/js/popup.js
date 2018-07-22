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
    .append($('<b></b>').text(CalendarViewOptions.popupText.build))
    .append($('<div class="timestamp"></div>').text(event.timestampString))
    .append($('<div class="duration"></div>').text(event.durationString));
}

function right(event, view) {
  return (event.future) ? rightForFutureBuild(event, view) : rightForPastBuild(event, view);
}

function rightForPastBuild(event, view) {
  return $('<div class="tooltip-right"></div>')
    .append($('<b></b>').text(CalendarViewOptions.popupText.project))
    .append($('<div></div>')
      .append(event.job.icon)
      .append(' ')
      .append($('<a></a>').attr('href', event.job.url).text(event.job.name)))
    .append($('<div class="nextBuild"></div>')
      .append(event.job.nextRun ? [CalendarViewOptions.popupText.nextBuild, dateLink(event.job.nextRun, view)] : ''));
}

function rightForFutureBuild(event, view) {
  return $('<div class="tooltip-right"></div>')
    .append($('<b></b>').text(CalendarViewOptions.popupText.buildHistory))
    .append($('<ul></ul>').append((event.builds.length > 0) ? buildHistory(event, view) : noBuildHistory()));
}

function noBuildHistory() {
  return $('<li></li>').text(CalendarViewOptions.popupText.buildHistoryEmpty);
}

function buildHistory(event, view) {
  return event.builds.map(function(b) { return $('<li></li>').append(build(b, view)); });
}

function bottom(event, view) {
  if (event.future || (!event.previousBuild && !event.nextBuild)) {
    return '';
  }
  var $bottom = $('<div class="tooltip-bottom"></div>');
  if (event.previousBuild) {
    var bottomLeft = $('<span class="previous"></span>')
      .append('<i></i>')
      .append(build(event.previousBuild, view));
    $bottom.append(bottomLeft);
  }
  if (event.nextBuild) {
    var bottomRight = $('<span class="next"></span>')
      .append(build(event.nextBuild, view).reverse())
      .append('<i></i>');
    $bottom.append(bottomRight);
  }
  return $bottom.append('<div style="clear:both"></div>');
}

function build(build, view) {
  var $link = $('<a></a>').attr('href', build.url).text(build.name);
  var $dateLink = dateLink(build.start, view);
  return [build.icon, ' ', $link, ' ', $dateLink];
}

function dateLink(date, view) {
  var mom = moment(date);
  var dateFormat = CalendarViewOptions.formats[view.type].popupBuildTimeFormat;
  var $dateLink = $('<a class="time" href="#"></a>');
  $dateLink.click(function() {
    view.calendar.gotoDate(mom);
    return false;
  });
  return $dateLink.append($('<time></time>').text(mom.format(dateFormat)));
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
