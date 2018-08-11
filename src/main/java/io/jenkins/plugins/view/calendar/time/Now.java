package io.jenkins.plugins.view.calendar.time;

import java.util.Calendar;

@SuppressWarnings("PMD.ShortClassName")
public class Now {
    private final Calendar second;

    public Now() {
       this(Calendar.getInstance());
    }

    public Now(final Calendar cal) {
        this.second = (Calendar) cal.clone();
    }

    public Calendar getSecond() {
        return (Calendar) second.clone();
    }

    public Calendar getMinute() {
        final Calendar minute = (Calendar) second.clone();
        minute.set(Calendar.SECOND, 0);
        minute.set(Calendar.MILLISECOND, 0);
        return minute;
    }


    public Calendar getNextMinute() {
        final Calendar nextMinute = (Calendar) second.clone();
        nextMinute.set(Calendar.SECOND, 0);
        nextMinute.set(Calendar.MILLISECOND, 0);
        nextMinute.add(Calendar.MINUTE, +1);
        return nextMinute;
    }

    public Calendar getPreviousMinute() {
        final Calendar previousMinute = (Calendar) second.clone();
        previousMinute.set(Calendar.SECOND, 0);
        previousMinute.set(Calendar.MILLISECOND, 0);
        previousMinute.add(Calendar.MINUTE, -1);
        return previousMinute;
    }
}
