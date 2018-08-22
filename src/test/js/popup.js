'use strict';

/* global describe:false, it:false, beforeEach:false */

var chai = require('chai');
var expect = chai.expect;

var requireUncached = require('require-uncached');
var mock = require('./mock');

var build15 = mock.build(15, 'finished');
var build16 = mock.build(16, 'finished');
var build17 = mock.build(17, 'running');

var view = mock.view([build15]);

var popup;

describe('popup.dom()', function() {
  beforeEach(function() {
    mock.jquery('<html></html>');
    mock.calendarViewOptions();
    popup = requireUncached('../../main/js/popup.js');
  });

  it('should create dom for finished event', function() {
    var event = mock.startedBuild('finished');
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

  it('should create dom for finished event with scheduled build', function() {
    var event = mock.startedBuild('finished');
    event.nextScheduledBuild = mock.scheduledBuild();
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

  it('should create dom for finished event with previous build', function() {
    var event = mock.startedBuild('finished');
    event.previousStartedBuild = mock.build(15, 'finished');
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

  it('should create dom for finished event with next build', function() {
    var event = mock.startedBuild('finished');
    event.nextStartedBuild = mock.build(17, 'finished');
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

  it('should create dom for running event', function() {
    var event = mock.startedBuild('running');
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

  it('should create dom for running event with build history', function() {
    var event = mock.startedBuild('running');
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

  it('should create dom for scheduled event', function() {
    var event = mock.startedBuild('scheduled');
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
    var event = mock.startedBuild('scheduled');
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
