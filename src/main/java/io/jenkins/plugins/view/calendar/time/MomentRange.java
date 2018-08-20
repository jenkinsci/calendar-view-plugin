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

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Calendar;

@Restricted(NoExternalUse.class)
public class MomentRange {
    private final Moment start;
    private final Moment end;

    public MomentRange(final Calendar start, final Calendar end) {
        this(new Moment(start), new Moment(end));
    }

    public MomentRange(final Moment start, final Moment end) {
        if (!isValidRange(start, end)) {
            throw new IllegalArgumentException("start has to be before end: " + start + " < " + end);
        }
        this.start = start;
        this.end = end;
    }

    public long duration() {
        return duration(start, end);
    }

    public static long duration(final Moment m1, final Moment m2) {
        return m2.getTimeInMillis() - m1.getTimeInMillis();
    }

    public Moment getStart() {
        return start;
    }

    public Moment getEnd() {
        return end;
    }

    public static MomentRange range(final Calendar start, final Calendar end) {
        return new MomentRange(start, end);
    }

    public static MomentRange range(final Moment start, final Moment end) {
        return new MomentRange(start, end);
    }

    public static boolean isValidRange(final Moment m1, final Moment m2) {
        return m1.isBefore(m2);
    }

    public static boolean isValidRange(final Calendar c1, final Calendar c2) {
        return c1.before(c2);
    }

    @Override
    public String toString() {
        return start + " - " + end;
    }
}
