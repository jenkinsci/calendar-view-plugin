import $ from 'jquery';
import * as popup from './popup.js';

function noSelectedEvent() {
  return {
    eventId: null,
    view: null
  };
}

var timeout = 200;
var selectedEvent = noSelectedEvent();

function getSelectedEvent() {
  return selectedEvent.view.calendar.clientEvents(function(e) {
    return e.id === selectedEvent.eventId;
  })[0];
}

function getSelectedElement() {
  return $('.event-id-' + selectedEvent.eventId)[0];
}

export function hasSelected() {
  return selectedEvent.eventId;
}

export function selectTimeout(t) {
  if (t) {
    timeout = t;
  }
  return timeout;
}

export function unselect() {
  selectedEvent = noSelectedEvent();
  timeout = 200;
  popup.close();
  $('.event-selected').removeClass('event-selected');
}

export function preselect(newSelectedEvent) {
  if (newSelectedEvent && selectedEvent.eventId !== newSelectedEvent.eventId) {
    unselect();
    selectedEvent = newSelectedEvent;
  }
}

export function select(newSelectedEvent) {
  preselect(newSelectedEvent);
  if (hasSelected()) {
    var event = getSelectedEvent();
    var element = getSelectedElement();
    if (event && element) {
      $(element).addClass('event-selected');
      popup.open({
        event: event,
        view: selectedEvent.view,
        target: element
      });
    } else {
      unselect();
    }
  }
}
