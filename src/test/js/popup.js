'use strict';

/* global describe:false, it:false */

var moment = require('moment');
var chai = require('chai');
var expect = chai.expect;

var requireUncached = require('require-uncached');
var mock = require('./mock');

var pastBuild = mock.pastBuild();
var scheduledBuild = mock.scheduledBuild();
var nextScheduledBuild = mock.nextScheduledBuild();
var build15 = mock.build(15);
var build16 = mock.build(16);
var build17 = mock.build(17);
var view = mock.view([build15]);

var popup;

describe('popup.dom()', function() {
  beforeEach(function() {
    mock.jquery('<html></html>');
    mock.calendarViewOptions();
    popup = requireUncached('../../main/js/popup.js');
  });

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
    expect(html).not.to.have.string(CalendarViewOptions.popupText.nextScheduledBuild);
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
    expect(html).to.have.string(CalendarViewOptions.popupText.nextScheduledBuild);
  });

  it('should create dom for past event with previous build', function() {
    var event = Object.assign({}, pastBuild);
    event.previousStartedBuild = build15;
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
    expect(html).not.to.have.string(CalendarViewOptions.popupText.nextScheduledBuild);
    expect(html).to.have.string('>' + event.previousStartedBuild.title + '<');
    expect(html).to.have.string('href="' + event.previousStartedBuild.url + '"');

    dom.find('.previous time').click();
  });

  it('should create dom for past event with next build', function() {
    var event = Object.assign({}, pastBuild);
    event.nextStartedBuild = build17;
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
    expect(html).not.to.have.string(CalendarViewOptions.popupText.nextScheduledBuild);
    expect(html).to.have.string('>' + event.nextStartedBuild.title + '<');
    expect(html).to.have.string('href="' + event.nextStartedBuild.url + '"');

    dom.find('.next time').click();
  });

  it('should create dom for scheduled event', function() {
    var event = Object.assign({}, scheduledBuild);
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

  it('should create dom for scheduled event with build history', function() {
    var event = Object.assign({}, scheduledBuild);
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

