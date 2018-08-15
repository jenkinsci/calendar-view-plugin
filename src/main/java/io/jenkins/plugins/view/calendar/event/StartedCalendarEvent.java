package io.jenkins.plugins.view.calendar.event;

import hudson.model.Run;

public interface StartedCalendarEvent extends CalendarEvent {
    Run getBuild();

    StartedCalendarEvent getPreviousStartedEvent();

    StartedCalendarEvent getNextStartedEvent();

    ScheduledCalendarEvent getNextScheduledEvent();
}
