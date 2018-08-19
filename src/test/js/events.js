'use strict';

/* global describe:false, it:false, beforeEach:false */
/* eslint no-unused-expressions: 0 */

var requireUncached = require('require-uncached');
var mock = require('./mock');

var chai = require('chai');
var expect = chai.expect;

var events;

describe('events.select()', function() {
  beforeEach(function() {
    mock.jquery('<html><body><div class="event-id-view-calendar-job-example-16"></div></body></html>');
    mock.calendarViewOptions();
    events = requireUncached('../../main/js/events.js');
  });

  it('should do nothing when invoked without arg and nothing is preselected', function() {
    events.select();
    expect(events.hasSelected()).to.not.be.ok;
  });
  it('should select preselected event when invoked without arg', function() {
    events.preselect({eventId: 'view-calendar-job-example-16', view: mock.view([mock.pastBuild()]) });
    events.select();
    expect(events.hasSelected()).to.be.ok;
  });
  it('should select event when passed as arg', function() {
    events.select({eventId: 'view-calendar-job-example-16', view: mock.view([mock.pastBuild()]) });
    expect(events.hasSelected()).to.be.ok;
  });
  it('should unselect event when nonexistent event is passed as arg', function() {
    events.select({eventId: 'view-calendar-job-example-16', view: mock.view([mock.pastBuild()]) });
    events.select({eventId: 'view-calendar-job-example-17', view: mock.view([mock.pastBuild()]) });
    expect(events.hasSelected()).to.not.be.ok;
  });
});

