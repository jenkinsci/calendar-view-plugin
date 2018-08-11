package io.jenkins.plugins.view.calendar.event;

import hudson.model.Run;
import hudson.model.TopLevelItem;

import java.util.Calendar;
import java.util.List;

public interface CalendarEvent {
    String getId();

    TopLevelItem getItem();

    Calendar getStart();

    String getStartAsDateTime();

    Calendar getEnd();

    String getEndAsDateTime();

    CalendarEventType getType();

    String getTypeAsClassName();

    String getUrl();

    String getTitle();

    long getDuration();

    boolean isFuture();

    String getTimestampString();

    String getDurationString();

    String getIconClassName();

    Run getBuild();

    List<CalendarEvent> getLastEvents();

    CalendarEvent getPreviousEvent();

    CalendarEvent getNextEvent();

    CalendarEvent getNextScheduledEvent();

    boolean isInRange(final Calendar start, final Calendar end);
}
