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
package io.jenkins.plugins.view.calendar;

import hudson.model.*;
import hudson.scheduler.CronTab;
import hudson.triggers.Trigger;
import hudson.util.RunList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarEventService {

    private CronJobService cronJobService;

    public CalendarEventService() {
        this.cronJobService = new CronJobService();
    }

    public CronJobService getCronJobService() {
        return cronJobService;
    }

    public void setCronJobService(final CronJobService cronJobService) {
        this.cronJobService = cronJobService;
    }

    public List<CalendarEvent> getCalendarEvents(final List<TopLevelItem> items, final Calendar start, final Calendar end) {
        final Calendar now = Calendar.getInstance();

        final List<CalendarEvent> events = new ArrayList<CalendarEvent>();
        if (now.compareTo(start) < 0) {
            events.addAll(getFutureEvents(items, start, end));
        } else if (now.compareTo(end) > 0) {
            events.addAll(getPastEvents(items, start, end));
        } else {
            events.addAll(getPastEvents(items, start, now));
            events.addAll(getFutureEvents(items, now, end));
        }
        return events;
    }

    public List<CalendarEvent> getFutureEvents(final List<TopLevelItem> items, final Calendar start, final Calendar end) {
        final List<CalendarEvent> events = new ArrayList<CalendarEvent>();
        for (final TopLevelItem item: items) {
            if (!(item instanceof AbstractProject)) {
                continue;
            }
            final long estimatedDuration = ((AbstractProject)item).getEstimatedDuration();
            final List<Trigger> triggers = cronJobService.getCronTriggers(item);
            for (final Trigger trigger: triggers) {
                final List<CronTab> cronTabs = cronJobService.getCronTabs(trigger);
                for (final CronTab cronTab: cronTabs) {
                    long timeInMillis = start.getTimeInMillis();
                    Calendar next = cronTab.ceil(timeInMillis);
                    while (next != null && next.compareTo(start) >= 0 && next.compareTo(end) < 0) {
                        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                        final CalendarEvent event = new CalendarEvent(item, next, estimatedDuration);
                        events.add(event);
                        timeInMillis = next.getTimeInMillis() + 1000 * 60;
                        next = cronTab.ceil(timeInMillis);
                    }
                }
            }
        }
        return events;
    }

    public List<CalendarEvent> getPastEvents(final List<TopLevelItem> items, final Calendar start, final Calendar end) {
        final List<CalendarEvent> events = new ArrayList<CalendarEvent>();
        for (final TopLevelItem item: items) {
            if (!(item instanceof Job)) {
                continue;
            }
            final RunList<Run> builds = ((Job) item).getBuilds();
            for (final Run build : builds) {
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                final CalendarEvent event = new CalendarEvent(item, build);
                if ((event.getStart().compareTo(start) >= 0 && event.getStart().compareTo(end) < 0) ||
                    (event.getEnd().compareTo(start) > 0 && event.getEnd().compareTo(end) < 0)) {
                    events.add(event);
                }
            }
        }
        return events;
    }

    public List<CalendarEvent> getLastEvents(final CalendarEvent event, final int numberOfEvents) {
        final List<CalendarEvent> lastEvents = new ArrayList<>();
        final TopLevelItem item = event.getItem();
        if (item instanceof Job) {
            final List<Run> lastBuilds = ((Job) item).getLastBuildsOverThreshold(numberOfEvents, Result.ABORTED);
            for (final Run lastBuild: lastBuilds) {
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                final CalendarEvent lastEvent = new CalendarEvent(item, lastBuild);
                lastEvents.add(lastEvent);
            }
        }
        return lastEvents;
    }

    public CalendarEvent getPreviousEvent(final CalendarEvent event) {
        if (event.getBuild() != null) {
            final Run previousBuild = event.getBuild().getPreviousBuild();
            if (previousBuild != null) {
                return new CalendarEvent(event.getItem(), previousBuild);
            }
        }
        return null;
    }

    public CalendarEvent getNextEvent(final CalendarEvent event) {
        if (event.getBuild() != null) {
            final Run nextBuild = event.getBuild().getNextBuild();
            if (nextBuild != null) {
                return new CalendarEvent(event.getItem(), nextBuild);
            }
        }
        return null;
    }

    public CalendarEvent getNextScheduledEvent(final CalendarEvent event) {
        final TopLevelItem item = event.getItem();
        if (!(item instanceof AbstractProject)) {
            return null;
        }
        final Calendar nextStart = cronJobService.getNextStart(item);
        if (nextStart != null) {
            final long estimatedDuration = ((AbstractProject)item).getEstimatedDuration();
            return new CalendarEvent(item, nextStart, estimatedDuration);
        }
        return null;
    }
}
