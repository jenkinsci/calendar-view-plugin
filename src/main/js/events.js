/*
 * The MIT License
 *
 * Copyright (c) 2018 Sven Schoenung
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
