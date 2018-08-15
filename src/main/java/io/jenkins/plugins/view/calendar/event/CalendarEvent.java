package io.jenkins.plugins.view.calendar.event;

import hudson.model.Job;
import io.jenkins.plugins.view.calendar.time.MomentRange;

import java.util.Calendar;

public interface CalendarEvent {
    String getId();

    Job getJob();

    Calendar getStart();

    Calendar getEnd();

    String getUrl();

    String getTitle();

    long getDuration();

    String getTimestampString();

    String getDurationString();

    String getIconClassName();

    boolean isInRange(MomentRange range);

    CalendarEventState getState();
}
