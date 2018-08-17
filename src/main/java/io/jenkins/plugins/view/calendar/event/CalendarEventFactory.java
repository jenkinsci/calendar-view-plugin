package io.jenkins.plugins.view.calendar.event;

import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.view.calendar.service.CalendarEventService;
import io.jenkins.plugins.view.calendar.time.Moment;
import io.jenkins.plugins.view.calendar.time.MomentRange;
import io.jenkins.plugins.view.calendar.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static io.jenkins.plugins.view.calendar.time.MomentRange.range;

public class CalendarEventFactory {
    private final transient Moment now;
    private final transient CalendarEventService calendarEventService;

    public CalendarEventFactory(final Moment now, final CalendarEventService calendarEventService) {
        this.now = now;
        this.calendarEventService = calendarEventService;
    }

    public ScheduledCalendarEvent createScheduledEvent(final Job job, final Calendar start, final long duration) {
        return new ScheduledCalendarEventImpl(job, start, duration);
    }

    public StartedCalendarEvent createStartedEvent(final Job job, final Run build) {
        return new StartedCalendarEventImpl(job, build);
    }

    private abstract class CalendarEventImpl implements CalendarEvent {
        protected String id;
        protected Job job;
        protected Moment start;
        protected Moment end;
        protected String title;
        protected String url;
        protected long duration;

        protected String initId(final String url, final long startTimeInMillis) {
            return StringUtils.defaultString(url, "")
              .replace("/", "-")
              .toLowerCase(Locale.ENGLISH) + startTimeInMillis;
        }

        protected Moment initEnd(final long timeInMillis, final long duration) {
            // duration needs to be at least 1sec otherwise
            // fullcalendar will not properly display the event
            final long dur = (duration < 1000) ? 1000 : duration;
            final Calendar end = Calendar.getInstance();
            end.setTimeInMillis(timeInMillis);
            end.add(Calendar.SECOND, (int) (dur / 1000));
            return new Moment(end);
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public Job getJob() {
            return this.job;
        }

        @Override
        public Moment getStart() {
            return start;
        }

        @Override
        public Moment getEnd() {
            return this.end;
        }

        @Override
        public String getUrl() {
            return this.url;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public long getDuration() {
            return this.duration;
        }

        @Override
        public String getTimestampString() {
            final long now = new GregorianCalendar().getTimeInMillis();
            final long difference = Math.abs(now - start.getTimeInMillis());
            return Util.getPastTimeString(difference);
        }

        @Override
        public String getDurationString() {
            return Util.getTimeSpanString(duration);
        }

        @Override
        public boolean isInRange(final MomentRange range) {
            return
              (start.compareTo(range.getStart()) >= 0 && start.compareTo(range.getEnd()) < 0) ||
              (end.compareTo(range.getStart()) > 0 && end.compareTo(range.getEnd()) < 0) ||
              (start.compareTo(range.getStart()) <= 0 && end.compareTo(range.getEnd()) >= 0);
        }

        @Override
        public String toString() {
            return DateUtil.formatDateTime(start.getTime()) + " - " + DateUtil.formatDateTime(end.getTime()) + ": " + getTitle();
        }
    }

    private class ScheduledCalendarEventImpl extends CalendarEventImpl implements ScheduledCalendarEvent {
        private transient List<StartedCalendarEvent> lastEvents;

        public ScheduledCalendarEventImpl(final Job job, final Calendar start, final long durationInMillis) {
            super();
            this.job = job;
            this.id = initId(job.getUrl(), start.getTimeInMillis());
            this.title = job.getFullDisplayName();
            this.url = job.getUrl();
            this.duration = durationInMillis;
            this.start = new Moment(start);
            this.end = initEnd(start.getTimeInMillis(), durationInMillis);
        }

        @Override
        public CalendarEventState getState() {
            return CalendarEventState.SCHEDULED;
        }

        @Override
        public String getIconClassName() {
            return job.getBuildHealth().getIconClassName();
        }

        @Override
        public List<StartedCalendarEvent> getLastEvents() {
            if (this.lastEvents == null) {
                this.lastEvents = calendarEventService.getLastEvents(this, 5);
            }
            return this.lastEvents;
        }
    }

    private class StartedCalendarEventImpl extends CalendarEventImpl implements StartedCalendarEvent {
        private final Run build;
        private final CalendarEventState state;

        private transient StartedCalendarEvent previousEvent;
        private transient StartedCalendarEvent nextEvent;
        private transient ScheduledCalendarEvent nextScheduledEvent;

        public StartedCalendarEventImpl(final Job job, final Run build) {
            super();
            this.id = initId(job.getUrl(), build.getStartTimeInMillis());
            this.job = job;
            this.build = build;
            this.title = build.getFullDisplayName();
            this.url = build.getUrl();
            this.start = new Moment(build.getStartTimeInMillis());
            if (build.isBuilding()) {
                this.duration = Math.max(MomentRange.duration(start, now), build.getEstimatedDuration());
                this.state = CalendarEventState.RUNNING;
            } else {
                this.duration = build.getDuration();
                this.state = CalendarEventState.FINISHED;
            }
            this.end = initEnd(start.getTimeInMillis(), this.duration);
        }

        @Override
        public Run getBuild() {
            return build;
        }

        @Override
        public CalendarEventState getState() {
            return state;
        }

        @Override
        public String getIconClassName() {
            return build.getIconColor().getIconClassName();
        }

        @Override
        public StartedCalendarEvent getPreviousStartedEvent() {
            if (previousEvent == null && build != null) {
                previousEvent = calendarEventService.getPreviousEvent(this);
            }
            return previousEvent;
        }

        @Override
        public StartedCalendarEvent getNextStartedEvent() {
            if (nextEvent == null && build != null) {
                nextEvent = calendarEventService.getNextEvent(this);
            }
            return nextEvent;
        }

        @Override
        public ScheduledCalendarEvent getNextScheduledEvent() {
            if (nextScheduledEvent == null && build != null) {
                nextScheduledEvent = calendarEventService.getNextScheduledEvent(this);
            }
            return nextScheduledEvent;
        }

    }
}
