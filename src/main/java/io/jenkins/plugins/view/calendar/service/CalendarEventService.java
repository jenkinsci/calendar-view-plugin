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
package io.jenkins.plugins.view.calendar.service;

import static io.jenkins.plugins.view.calendar.time.MomentRange.isValidRange;
import static io.jenkins.plugins.view.calendar.time.MomentRange.range;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.view.calendar.CalendarView.CalendarViewEventsType;
import io.jenkins.plugins.view.calendar.event.CalendarEvent;
import io.jenkins.plugins.view.calendar.event.CalendarEventComparator;
import io.jenkins.plugins.view.calendar.event.CalendarEventFactory;
import io.jenkins.plugins.view.calendar.event.CalendarEventState;
import io.jenkins.plugins.view.calendar.event.ScheduledCalendarEvent;
import io.jenkins.plugins.view.calendar.event.StartedCalendarEvent;
import io.jenkins.plugins.view.calendar.time.Moment;
import io.jenkins.plugins.view.calendar.time.MomentRange;

@Restricted(NoExternalUse.class)
public class CalendarEventService {

    private final transient CronJobService cronJobService;
    private final transient Moment now;
    private final transient CalendarEventFactory calendarEventFactory;

    public CalendarEventService(final Moment now, final CronJobService cronJobService) {
        this.now = now;
        this.cronJobService = cronJobService;
        this.calendarEventFactory = new CalendarEventFactory(now, this);
    }

    /**
     * Collects all the events that overlap the given range. Includes events that start before the range, but
     * last into the range; also includes events that end after the range, but start within the range.
     *
     * @param jobs The jobs from which to collect all CalendarEvents
     * @param inclusionRange The range for which CalendarEvents should be returned
     * @param eventsType The type of event that should be filtered on
     * @return List of events that overlap the range
     */
    public List<CalendarEvent> getCalendarEvents(final List<? extends Job> jobs, final MomentRange inclusionRange, final CalendarViewEventsType eventsType) {
        final List<CalendarEvent> events = new ArrayList<>();

        if (now.isBefore(inclusionRange.getStart())) {
            events.addAll(getRunningEvents(jobs, inclusionRange, eventsType));
            if (isValidRange(now.nextMinute(), inclusionRange.getStart().previousMinute())) {
                events.addAll(getScheduledEventsBackward(jobs, range(now.nextMinute(), inclusionRange.getStart().previousMinute()), inclusionRange, eventsType));
            }
            events.addAll(getScheduledEventsForward(jobs, inclusionRange, inclusionRange, eventsType));
        } else if (now.isSame(inclusionRange.getStart())) {
            events.addAll(getRunningEvents(jobs, inclusionRange, eventsType));
            events.addAll(getScheduledEventsForward(jobs, range(now.nextMinute(), inclusionRange.getEnd()), inclusionRange, eventsType));
        } else if (now.isSame(inclusionRange.getEnd())) {
            events.addAll(getStartedEvents(jobs, inclusionRange, null, eventsType));
        } else if (now.isAfter(inclusionRange.getEnd())) {
            events.addAll(getStartedEvents(jobs, inclusionRange, null, eventsType));
        } else { // (now.isAfter(inclusionRange.getStart()) && now.isBefore(inclusionRange.getEnd())
            events.addAll(getStartedEvents(jobs, range(inclusionRange.getStart(), now.nextMinute()), null, eventsType));
            if (isValidRange(now.nextMinute(), inclusionRange.getEnd())) {
                events.addAll(getScheduledEventsForward(jobs, range(now.nextMinute(), inclusionRange.getEnd()), inclusionRange, eventsType));
            }
        }

        Collections.sort(events, new CalendarEventComparator());

        return events;
    }

    public List<ScheduledCalendarEvent> getScheduledEventsForward(final List<? extends Job> jobs, final MomentRange searchRange, final MomentRange inclusionRange, final CalendarViewEventsType eventsType) {
        return getScheduledEvents(jobs, new ForwardScheduledEventCollector(searchRange, inclusionRange), eventsType);
    }

    public List<ScheduledCalendarEvent> getScheduledEventsBackward(final List<? extends Job> jobs, final MomentRange searchRange, final MomentRange inclusionRange, final CalendarViewEventsType eventsType) {
        return getScheduledEvents(jobs, new BackwardScheduledEventCollector(searchRange, inclusionRange), eventsType);
    }

    public List<ScheduledCalendarEvent> getScheduledEvents(final List<? extends Job> jobs, final ScheduledEventCollector collector, final CalendarViewEventsType eventsType) {
        for (final Job job: jobs) {
            if (job.isBuildable()) {
                final long estimatedDuration = job.getEstimatedDuration();
                final List<CronWrapper<?>> cronTabs = cronJobService.getCronTabs(job, eventsType);
                for (final CronWrapper<?> cronTab: cronTabs) {
                    collector.collectEvents(job, cronTab, estimatedDuration);
                }
            }
        }
        return collector.getEvents();
    }

    private abstract class ScheduledEventCollector {
        private transient final List<ScheduledCalendarEvent> events = new ArrayList<>();
        protected transient final MomentRange searchRange;
        private transient final MomentRange inclusionRange;

        public ScheduledEventCollector( final MomentRange searchRange, final MomentRange inclusionRange) {
            this.searchRange = searchRange;
            this.inclusionRange = inclusionRange;
        }

        public void collectEvents(final Job job, final CronWrapper<?> cronTab, final long estimatedDuration) {
            long timeInMillis = searchStart();
            do {
                final Calendar next = nextRun(cronTab, timeInMillis);
                if (next == null || searchRange.getStart().isAfter(next) || searchRange.getEnd().isBefore(next)) {
                    break;
                }
                next.set(Calendar.SECOND, 0);
                next.set(Calendar.MILLISECOND, 0);
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                final ScheduledCalendarEvent event = calendarEventFactory.createScheduledEvent(job, cronTab.getParameters(), next, estimatedDuration);
                if (!event.isInRange(inclusionRange)) {
                    break;
                }
                events.add(event);
                timeInMillis = next.getTimeInMillis() + searchOffset();
            } while (true);
        }

        protected abstract Calendar nextRun(CronWrapper<?> cronTab, long timeInMillis);

        protected abstract long searchStart();

        protected abstract int searchOffset();

        public List<ScheduledCalendarEvent> getEvents() {
            return events;
        }
    }

    private class ForwardScheduledEventCollector extends ScheduledEventCollector {
        public ForwardScheduledEventCollector(final MomentRange searchRange, final MomentRange inclusionRange) {
            super(searchRange, inclusionRange);
        }

        @Override
        protected Calendar nextRun(final CronWrapper<?> cronTab, final long timeInMillis) {
            return cronTab.ceil(timeInMillis);
        }

        @Override
        protected long searchStart() {
            return searchRange.getStart().getTimeInMillis();
        }

        @Override
        protected int searchOffset() {
            return 1000 * 60;
        }
    }

    private class BackwardScheduledEventCollector extends ScheduledEventCollector {
        public BackwardScheduledEventCollector(final MomentRange searchRange, final MomentRange inclusionRange) {
            super(searchRange, inclusionRange);
        }

        @Override
        protected Calendar nextRun(final CronWrapper<?> cronTab, final long timeInMillis) {
            return cronTab.floor(timeInMillis);
        }

        @Override
        protected long searchStart() {
            return searchRange.getEnd().getTimeInMillis();
        }

        @Override
        protected int searchOffset() {
            return -1000 * 60;
        }
    }

    public List<StartedCalendarEvent> getFinishedEvents(final List<? extends Job> jobs, final MomentRange range, final CalendarViewEventsType eventsType) {
        return getStartedEvents(jobs, range, CalendarEventState.FINISHED, eventsType);
    }

    public List<StartedCalendarEvent> getRunningEvents(final List<? extends Job> jobs, final MomentRange range, final CalendarViewEventsType eventsType) {
        return getStartedEvents(jobs, range, CalendarEventState.RUNNING, eventsType);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public List<StartedCalendarEvent> getStartedEvents(final List<? extends Job> jobs, final MomentRange range, final CalendarEventState state, final CalendarViewEventsType eventsType) {
        if (state == CalendarEventState.SCHEDULED) {
            throw new IllegalArgumentException("State for started events cannot be " + CalendarEventState.SCHEDULED);
        }

        final List<StartedCalendarEvent> events = new ArrayList<>();
        if (eventsType != CalendarViewEventsType.POLLINGS) {
            for (final Job job: jobs) {
                if (state == CalendarEventState.RUNNING && !job.isBuilding()) {
                    continue;
                }
                final List<Run> builds = job.getBuilds();
                for (final Run build : builds) {
                    if (state == CalendarEventState.RUNNING && !build.isBuilding()) {
                        continue;
                    }
                    if (state == CalendarEventState.FINISHED && build.isBuilding()) {
                        continue;
                    }
                    final StartedCalendarEvent event = calendarEventFactory.createStartedEvent(job, build);
                    if (event.isInRange(range)) {
                        events.add(event);
                    }
                }
            }
        }
        return events;
    }

    public List<StartedCalendarEvent> getLastEvents(final CalendarEvent event, final int numberOfEvents) {
        final List<StartedCalendarEvent> lastEvents = new ArrayList<>();
        final Job job = event.getJob();
        final List<Run> lastBuilds = job.getLastBuildsOverThreshold(numberOfEvents, Result.ABORTED);
        for (final Run lastBuild: lastBuilds) {
            final StartedCalendarEvent lastEvent = calendarEventFactory.createStartedEvent(job, lastBuild);
            lastEvents.add(lastEvent);
        }
        return lastEvents;
    }

    public StartedCalendarEvent getPreviousEvent(final StartedCalendarEvent event) {
        if (event.getBuild() != null) {
            final Run previousBuild = event.getBuild().getPreviousBuild();
            if (previousBuild != null) {
                return calendarEventFactory.createStartedEvent(event.getJob(), previousBuild);
            }
        }
        return null;
    }

    public StartedCalendarEvent getNextEvent(final StartedCalendarEvent event) {
        if (event.getBuild() != null) {
            final Run nextBuild = event.getBuild().getNextBuild();
            if (nextBuild != null) {
                return calendarEventFactory.createStartedEvent(event.getJob(), nextBuild);
            }
        }
        return null;
    }

    public ScheduledCalendarEvent getNextScheduledEvent(final CalendarEvent event, final CalendarViewEventsType eventsType) {
        final Job job = event.getJob();
        final Calendar nextStart = cronJobService.getNextStart(job, eventsType);
        if (nextStart != null) {
            final long estimatedDuration = job.getEstimatedDuration();
            return calendarEventFactory.createScheduledEvent(job, Collections.emptyMap(), nextStart, estimatedDuration);
        }
        return null;
    }
}
