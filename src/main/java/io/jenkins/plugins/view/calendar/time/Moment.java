package io.jenkins.plugins.view.calendar.time;

import io.jenkins.plugins.view.calendar.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("PMD.ShortClassName")
public class Moment implements Comparable<Object> {
    private final Calendar calendar;

    public Moment() {
       this(Calendar.getInstance());
    }

    public Moment(long timeInMillis) {
        this.calendar = Calendar.getInstance();
        this.calendar.setTimeInMillis(timeInMillis);
    }

    public Moment(final Calendar cal) {
        this.calendar = (Calendar) cal.clone();
    }

    public Moment nextMinute() {
        final Calendar nextMinute = (Calendar) calendar.clone();
        nextMinute.set(Calendar.SECOND, 0);
        nextMinute.set(Calendar.MILLISECOND, 0);
        nextMinute.add(Calendar.MINUTE, +1);
        return new Moment(nextMinute);
    }

    public Moment previousMinute() {
        final Calendar previousMinute = (Calendar) calendar.clone();
        previousMinute.set(Calendar.SECOND, 0);
        previousMinute.set(Calendar.MILLISECOND, 0);
        previousMinute.add(Calendar.MINUTE, -1);
        return new Moment(previousMinute);
    }

    public Date getTime() {
        return calendar.getTime();
    }

    @Override
    public int hashCode() {
        return calendar.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Moment)) {
            return false;
        }
        return calendar.equals(((Moment)o).calendar);
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof Moment) {
            return calendar.compareTo(((Moment)o).calendar);
        }
        throw new IllegalArgumentException("Can't compare object of type " + o.getClass().getCanonicalName());
    }

    public boolean isBefore(final Moment m) {
        return this.isBefore(m.calendar);
    }

    public boolean isBefore(final Calendar c) {
        return this.calendar.compareTo(c) < 0;
    }

    public boolean isSame(final Moment m) {
        return this.isSame(m.calendar);
    }

    public boolean isSame(final Calendar c) {
        return this.calendar.compareTo(c) == 0;
    }

    public boolean isAfter(final Moment m) {
        return this.isAfter(m.calendar);
    }

    public boolean isAfter(final Calendar c) {
        return this.calendar.compareTo(c) > 0;
    }


    public long getTimeInMillis() {
        return calendar.getTimeInMillis();
    }

    @Override
    public String toString() {
        return DateUtil.formatDateTime(calendar.getTime());
    }
}
