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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.TimeZone;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.mom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

class MomentTest {
    private static TimeZone defaultTimeZone;

    @BeforeAll
    static void beforeClass() {
        MomentTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    @AfterAll
    static void afterClass() {
        TimeZone.setDefault(MomentTest.defaultTimeZone);
    }

    @Test
    void testHashCode() throws ParseException {
        int hashCode1 = new Moment(cal("2018-01-01 00:00:00 UTC")).hashCode();
        int hashCode2 = new Moment(cal("2018-01-01 00:00:00 UTC")).hashCode();
        assertThat(hashCode1, is(hashCode2));
    }

    @Test
    void testEquals() throws ParseException {
        Moment m1 = new Moment(cal("2018-01-01 00:00:00 UTC"));
        Moment m2 = new Moment(cal("2018-01-01 00:00:00 UTC"));
        Moment m3 = new Moment(cal("2018-01-01 00:00:01 UTC"));
        assertThat(m1.equals(m1), is(true));
        assertThat(m1.equals(m2), is(true));
        assertThat(m1.equals(m3), is(false));
        assertThat(m1.equals(new Object()), is(false));
    }

    @Test
    void testCompareTo() throws ParseException {
        Moment m1 = new Moment(cal("2018-01-01 00:00:00 UTC"));
        Moment m2 = new Moment(cal("2018-01-01 00:00:00 UTC"));
        Moment m3 = new Moment(cal("2018-01-01 00:00:01 UTC"));
        assertThat(m1.compareTo(m1), is(0));
        assertThat(m1.compareTo(m2), is(0));
        assertThat(m1.compareTo(m3), lessThan(0));
        assertThat(m3.compareTo(m1), greaterThan(0));
    }

    @Test
    void testToString() throws ParseException {
        Moment m = mom("2018-01-01 00:00:00 UTC");
        assertThat(m.toString(), is("2018-01-01T01:00:00"));
    }

    @Test
    void testNextMinute() throws ParseException {
        assertThat(mom("2018-01-01 00:03:30 UTC").nextMinute(), is(mom("2018-01-01 00:04:00 UTC")));
        assertThat(mom("2018-01-01 00:03:00 UTC").nextMinute(), is(mom("2018-01-01 00:04:00 UTC")));
    }

    @Test
    void testPreviousMinute() throws ParseException {
        assertThat(mom("2018-01-01 00:03:30 UTC").previousMinute(), is(mom("2018-01-01 00:02:00 UTC")));
        assertThat(mom("2018-01-01 00:03:00 UTC").previousMinute(), is(mom("2018-01-01 00:02:00 UTC")));
    }
}
