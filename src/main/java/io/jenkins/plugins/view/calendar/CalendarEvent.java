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

import hudson.Util;
import hudson.model.*;
import io.jenkins.plugins.view.calendar.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class CalendarEvent implements Comparable<CalendarEvent> {
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
    public CalendarEvent(final TopLevelItem item, final Calendar start, final long durationInMillis) {
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
    public CalendarEvent(final TopLevelItem item, final Run build) {
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

    private static String initId(final String url) {
        return StringUtils.defaultString(url, "")
          .replace("/", "-")
          .toLowerCase(Locale.ENGLISH)
          .replaceAll("-$", "");
    }

    private static Calendar initEnd(final Calendar start, final long duration) {
        // duration needs to be at least 1sec otherwise
        // fullcalendar will not properly display the event
        final long dur = (duration < 1000) ? 1000 : duration;
        final Calendar end = Calendar.getInstance();
        end.setTime(start.getTime());
        end.add(Calendar.SECOND, (int) (dur / 1000));
        return end;
    }

    public String getId() {
        return this.id;
    }

    public TopLevelItem getItem() {
        return this.item;
    }

    public Calendar getStart() {
        return start;
    }

    public String getStartAsDateTime() {
        return DateUtil.formatDateTime(getStart());
    }

    public Calendar getEnd() {
        return this.end;
    }

    public String getEndAsDateTime() {
        return DateUtil.formatDateTime(getEnd());
    }

    public CalendarEventType getType() {
        return this.type;
    }

    public String getTypeAsClassName() {
        return "event-" + type.name().toLowerCase(Locale.ENGLISH);
    }

    public String getUrl() {
        return this.url;
    }

    public String getTitle() {
        return this.title;
    }

    public long getDuration() {
        return this.duration;
    }

    public boolean isFuture() {
        return build == null;
    }

    public String getTimestampString() {
        final long now = new GregorianCalendar().getTimeInMillis();
        final long difference = Math.abs(now - start.getTimeInMillis());
        return Util.getPastTimeString(difference);
    }

    public String getDurationString() {
        return Util.getTimeSpanString(duration);
    }

    public String getIconClassName() {
        if (isFuture()) {
            return ((AbstractProject) this.item).getBuildHealth().getIconClassName();
        }
        switch (getType()) {
            case SUCCESS:
                return "icon-blue";
            case UNSTABLE:
                return "icon-yellow";
            case FAILURE:
                return "icon-red";
            default:
                return "icon-grey";
        }
    }

    public Run getBuild() {
        return build;
    }

    public List<CalendarEvent> getLastEvents() {
        if (this.lastEvents == null) {
            this.lastEvents = new CalendarEventService().getLastEvents(this, 5);
        }
        return this.lastEvents;
    }


    public CalendarEvent getPreviousEvent() {
        if (previousEvent == null && build != null) {
            previousEvent = new CalendarEventService().getPreviousEvent(this);
        }
        return previousEvent;
    }

    public CalendarEvent getNextEvent() {
        if (nextEvent == null && build != null) {
            nextEvent = new CalendarEventService().getNextEvent(this);
        }
        return nextEvent;
    }

    public CalendarEvent getNextScheduledEvent() {
        if (nextScheduledEvent == null && build != null) {
            nextScheduledEvent = new CalendarEventService().getNextScheduledEvent(this);
        }
        return nextScheduledEvent;
    }

    @Override
    public int compareTo(final CalendarEvent other) {
        final int c = this.getStart().compareTo(other.getStart());
        if (c == 0) {
            return this.getEnd().compareTo(other.getEnd());
        }
        return c;
    }

    public boolean isInRange(final Calendar start, final Calendar end) {
        return
          (getStart().compareTo(start) >= 0 && getStart().compareTo(end) < 0) ||
          (getEnd().compareTo(start) > 0 && getEnd().compareTo(end) < 0);
    }

    @Override
    public String toString() {
        return getStartAsDateTime() + "-" + getEndAsDateTime() + ": " + getTitle();
    }
}
