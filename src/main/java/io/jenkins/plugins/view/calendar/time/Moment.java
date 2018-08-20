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
package io.jenkins.plugins.view.calendar.time;

import io.jenkins.plugins.view.calendar.util.DateUtil;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("PMD.ShortClassName")
@Restricted(NoExternalUse.class)
public class Moment implements Comparable<Object> {
    private final Calendar calendar;

    public Moment() {
       this(Calendar.getInstance());
    }

    public Moment(final long timeInMillis) {
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
