package io.jenkins.plugins.view.calendar.event;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import io.jenkins.plugins.view.calendar.service.CalendarEventService;
import io.jenkins.plugins.view.calendar.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CalendarEventFactory {
    private final transient CalendarEventService calendarEventService;

    public CalendarEventFactory(final CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    public CalendarEvent createFutureEvent(final TopLevelItem item, final Calendar start, final long duration) {
        return new CalendarEventImpl(item, start, duration);
    }

    public CalendarEvent createPastEvent(final TopLevelItem item, final Run build) {
        return new CalendarEventImpl(item, build);
    }

    private class CalendarEventImpl implements CalendarEvent {
        private final String id;
        private final TopLevelItem item;
        private final Run build;
        private final Calendar start;
        private final Calendar end;
        private final CalendarEventType type;
        private final String title;
        private final String url;
        private final long duration;

        private transient List<CalendarEvent> lastEvents;
        private transient CalendarEvent previousEvent;
        private transient CalendarEvent nextEvent;
        private transient CalendarEvent nextScheduledEvent;

        @SuppressWarnings("PMD.NullAssignment")
        public CalendarEventImpl(final TopLevelItem item, final Calendar start, final long durationInMillis) {
            this.id = initId(item.getUrl());
            this.item = item;
            this.build = null;
            this.type = CalendarEventType.FUTURE;
            this.title = item.getFullDisplayName();
            this.url = item.getUrl();
            this.duration = durationInMillis;
            this.start = start;
            this.end = initEnd(start, durationInMillis);
        }

        @SuppressWarnings("PMD.NullAssignment")
        public CalendarEventImpl(final TopLevelItem item, final Run build) {
            this.id = initId(build.getUrl());
            this.item = item;
            this.build = build;
            this.type = CalendarEventType.fromResult(build.getResult());
            this.title = build.getFullDisplayName();
            this.url = build.getUrl();
            this.duration = build.getDuration();
            this.start = Calendar.getInstance();
            this.start.setTimeInMillis(build.getStartTimeInMillis());
            this.end = initEnd(this.start, build.getDuration());
        }

        private String initId(final String url) {
            return StringUtils.defaultString(url, "")
              .replace("/", "-")
              .toLowerCase(Locale.ENGLISH)
              .replaceAll("-$", "");
        }

        private Calendar initEnd(final Calendar start, final long duration) {
            // duration needs to be at least 1sec otherwise
            // fullcalendar will not properly display the event
            final long dur = (duration < 1000) ? 1000 : duration;
            final Calendar end = Calendar.getInstance();
            end.setTime(start.getTime());
            end.add(Calendar.SECOND, (int) (dur / 1000));
            return end;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public TopLevelItem getItem() {
            return this.item;
        }

        @Override
        public Calendar getStart() {
            return start;
        }

        @Override
        public String getStartAsDateTime() {
            return DateUtil.formatDateTime(getStart());
        }

        @Override
        public Calendar getEnd() {
            return this.end;
        }

        @Override
        public String getEndAsDateTime() {
            return DateUtil.formatDateTime(getEnd());
        }

        @Override
        public CalendarEventType getType() {
            return this.type;
        }

        @Override
        public String getTypeAsClassName() {
            return "event-" + type.name().toLowerCase(Locale.ENGLISH).replace("_", "-");
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
        public boolean isFuture() {
            return build == null;
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
        public String getIconClassName() {
            if (isFuture()) {
                return ((AbstractProject) this.item).getBuildHealth().getIconClassName();
            }
            return build.getIconColor().getIconClassName();
        }

        @Override
        public Run getBuild() {
            return build;
        }

        @Override
        public List<CalendarEvent> getLastEvents() {
            if (this.lastEvents == null) {
                this.lastEvents = calendarEventService.getLastEvents(this, 5);
            }
            return this.lastEvents;
        }

        @Override
        public CalendarEvent getPreviousEvent() {
            if (previousEvent == null && build != null) {
                previousEvent = calendarEventService.getPreviousEvent(this);
            }
            return previousEvent;
        }

        @Override
        public CalendarEvent getNextEvent() {
            if (nextEvent == null && build != null) {
                nextEvent = calendarEventService.getNextEvent(this);
            }
            return nextEvent;
        }

        @Override
        public CalendarEvent getNextScheduledEvent() {
            if (nextScheduledEvent == null && build != null) {
                nextScheduledEvent = calendarEventService.getNextScheduledEvent(this);
            }
            return nextScheduledEvent;
        }

        @Override
        public boolean isInRange(final Calendar start, final Calendar end) {
            return
              (getStart().compareTo(start) >= 0 && getStart().compareTo(end) < 0) ||
              (getEnd().compareTo(start) > 0 && getEnd().compareTo(end) < 0) ||
              (getStart().compareTo(start) <= 0 && getEnd().compareTo(end) >= 0);
        }

        @Override
        public String toString() {
            return getStartAsDateTime() + " - " + getEndAsDateTime() + ": " + getTitle();
        }
    }
}
