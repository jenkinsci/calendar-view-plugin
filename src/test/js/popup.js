'use strict';

/* global describe:false, it:false */

require('babel-register')({ presets: [ 'env' ] })

var JSDOM = require('jsdom').JSDOM;
var jquery = require('jquery')(new JSDOM('<html></html>').window);

var mockery = require('mockery');
mockery.enable({ useCleanCache: true });
mockery.warnOnUnregistered(false);
mockery.registerMock('jquery', jquery);

var moment = require('moment');
var chai = require('chai');
var expect = chai.expect;

var popup = require('../../main/js/popup.js');

global.CalendarViewOptions = {
  formats: {
    month: {
      popupBuildTimeFormat: 'YY-MM-DD'
    }
  },
  popupText: {
    project: 'ppp',
    build: 'bbb',
    nextBuild: 'nxb',
    buildHistory: 'bHist',
    buildHistoryEmpty: 'bEmpty'
  }
};

var pastBuild = {
  "id": "view-calendar-job-example-16",
  "title": "Example #16",
  "url": "\/jenkins\/view\/Calendar\/job\/Example\/16\/",
  "icon": "<img src=\"pastBuild.png\">",
  "start": "2018-07-26T20:00:12",
  "end": "2018-07-26T20:02:12",
  "duration": 120102,
  "future": false,
  "className": "event-failure event-id-view-calendar-job-example-16",
  "timestampString": "Vor 1 Tag 21 Stunden gestartet",
  "durationString": "Dauer: 2 Minuten 0 Sekunden",
  "job": {
    "title": "ExampleJob",
    "url": "\/jenkins\/view\/Calendar\/job\/Example\/",
    "icon": "<img src=\"job.png\">"
  },
  "allDay": false
};

var nextScheduledBuild = {
  "id": "view-calendar-job-example",
  "start": "2018-07-28T20:00:16",
  "end": "2018-07-28T20:29:22",
  "future": true
};

var build15 = {
  "id": "view-calendar-job-example-15",
  "title": "#15",
  "url": "\/jenkins\/view\/Calendar\/job\/Example\/15\/",
  "icon": "<img src=\"build15.png\">",
  "start": "2018-07-25T20:00:00",
  "end": "2018-07-25T20:02:00",
  "future": false
};

var build16 = {
  "id": "view-calendar-job-example-16",
  "title": "#16",
  "url": "\/jenkins\/view\/Calendar\/job\/Example\/16\/",
  "icon": "<img src=\"build16.png\">",
  "start": "2018-07-26T20:00:12",
  "end": "2018-07-26T20:02:12",
  "future": false
};

var build17 = {
  "id": "view-calendar-job-example-17",
  "title": "#17",
  "url": "\/jenkins\/view\/Calendar\/job\/Example\/17\/",
  "icon": "<img src=\"build17.png\">",
  "start": "2018-07-27T20:00:01",
  "end": "2018-07-27T20:02:01",
  "future": false
};

var futureBuild =  {
  "id": "view-calendar-job-backup",
  "title": "Example",
  "url": "\/jenkins\/view\/Calendar\/job\/Example\/",
  "icon": "<img src=\"futureBuild.png\">",
  "start": "2018-07-28T20:00:16",
  "end": "2018-07-28T20:29:22",
  "duration": 1746752,
  "future": true,
  "className": "event-future event-id-view-calendar-job-backup",
  "timestampString": "Startet in 2 Stunden 35 Minuten",
  "durationString": "Vorraussichtliche Dauer: 29 Minuten",
  "allDay": false
};

var view = {
  type: 'month',
  intervalStart: moment('2018-06-01'),
  intervalEnd: moment('2018-07-01'),
  calendar: {
    gotoDate: function() { },
    clientEvents: function() { return [ build15 ] }
  }
};

describe('popup.dom()', function() {
  it('should create dom for past event', function() {
    var event = Object.assign({}, pastBuild);
    var html = popup.dom(event, view, {close: function() { }})[0].outerHTML;

    expect(html).to.have.string('>' + event.title + '<');
    expect(html).to.have.string('href="' + event.url + '"');
    expect(html).to.have.string(event.icon);
    expect(html).to.have.string(CalendarViewOptions.popupText.build);
    expect(html).to.have.string(event.timestampString);
    expect(html).to.have.string(event.durationString);
    expect(html).to.have.string(CalendarViewOptions.popupText.project);
    expect(html).to.have.string(event.job.title);
    expect(html).to.have.string('href="' + event.job.url + '"');
    expect(html).not.to.have.string(CalendarViewOptions.popupText.nextBuild);
  });

  it('should create dom for past event with scheduled build', function() {
    var event = Object.assign({}, pastBuild);
    event.nextScheduledBuild = nextScheduledBuild;
    var html = popup.dom(event, view, {close: function() { }})[0].outerHTML;

    expect(html).to.have.string('>' + event.title + '<');
    expect(html).to.have.string('href="' + event.url + '"');
    expect(html).to.have.string(event.icon);
    expect(html).to.have.string(CalendarViewOptions.popupText.build);
    expect(html).to.have.string(event.timestampString);
    expect(html).to.have.string(event.durationString);
    expect(html).to.have.string(CalendarViewOptions.popupText.project);
    expect(html).to.have.string(event.job.title);
    expect(html).to.have.string('href="' + event.job.url + '"');
    expect(html).to.have.string(CalendarViewOptions.popupText.nextBuild);
  });

  it('should create dom for past event with previous build', function() {
    var event = Object.assign({}, pastBuild);
    event.previousBuild = build15;
    var dom = popup.dom(event, view, {close: function() { }});
    var html = dom[0].outerHTML;

    expect(html).to.have.string('>' + event.title + '<');
    expect(html).to.have.string('href="' + event.url + '"');
    expect(html).to.have.string(event.icon);
    expect(html).to.have.string(CalendarViewOptions.popupText.build);
    expect(html).to.have.string(event.timestampString);
    expect(html).to.have.string(event.durationString);
    expect(html).to.have.string(CalendarViewOptions.popupText.project);
    expect(html).to.have.string(event.job.title);
    expect(html).to.have.string('href="' + event.job.url + '"');
    expect(html).not.to.have.string(CalendarViewOptions.popupText.nextBuild);
    expect(html).to.have.string('>' + event.previousBuild.title + '<');
    expect(html).to.have.string('href="' + event.previousBuild.url + '"');

    dom.find('.previous time').click();
  });

  it('should create dom for past event with next build', function() {
    var event = Object.assign({}, pastBuild);
    event.nextBuild = build17;
    var dom = popup.dom(event, view, {close: function() { }});
    var html = dom[0].outerHTML;

    expect(html).to.have.string('>' + event.title + '<');
    expect(html).to.have.string('href="' + event.url + '"');
    expect(html).to.have.string(event.icon);
    expect(html).to.have.string(CalendarViewOptions.popupText.build);
    expect(html).to.have.string(event.timestampString);
    expect(html).to.have.string(event.durationString);
    expect(html).to.have.string(CalendarViewOptions.popupText.project);
    expect(html).to.have.string(event.job.title);
    expect(html).to.have.string('href="' + event.job.url + '"');
    expect(html).not.to.have.string(CalendarViewOptions.popupText.nextBuild);
    expect(html).to.have.string('>' + event.nextBuild.title + '<');
    expect(html).to.have.string('href="' + event.nextBuild.url + '"');

    dom.find('.next time').click();
  });

  it('should create dom for future event', function() {
    var event = Object.assign({}, futureBuild);
    event.builds = [];
    var html = popup.dom(event, view, {close: function() { }})[0].outerHTML;

    expect(html).to.have.string('>' + event.title + '<');
    expect(html).to.have.string('href="' + event.url + '"');
    expect(html).to.have.string(event.icon);
    expect(html).to.have.string(CalendarViewOptions.popupText.build);
    expect(html).to.have.string(event.timestampString);
    expect(html).to.have.string(event.durationString);
    expect(html).to.have.string(CalendarViewOptions.popupText.buildHistory);
    expect(html).to.have.string(CalendarViewOptions.popupText.buildHistoryEmpty);
  });

  it('should create dom for future event with build history', function() {
    var event = Object.assign({}, futureBuild);
    event.builds = [build15, build16, build17];
    var html = popup.dom(event, view, {close: function() { }})[0].outerHTML;

    expect(html).to.have.string('>' + event.title + '<');
    expect(html).to.have.string('href="' + event.url + '"');
    expect(html).to.have.string(event.icon);
    expect(html).to.have.string(CalendarViewOptions.popupText.build);
    expect(html).to.have.string(event.timestampString);
    expect(html).to.have.string(event.durationString);
    expect(html).to.have.string(CalendarViewOptions.popupText.buildHistory);
    expect(html).to.have.string(build15.title);
    expect(html).to.have.string(build16.title);
    expect(html).to.have.string(build17.title);
    expect(html).to.have.string('href="' + build15.url + '"');
    expect(html).to.have.string('href="' + build16.url + '"');
    expect(html).to.have.string('href="' + build17.url + '"');
  });


});

