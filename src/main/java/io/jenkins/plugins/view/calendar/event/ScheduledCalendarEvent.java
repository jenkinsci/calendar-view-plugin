package io.jenkins.plugins.view.calendar.event;

import java.util.List;

public interface ScheduledCalendarEvent extends CalendarEvent {
    List<StartedCalendarEvent> getLastEvents();
}
