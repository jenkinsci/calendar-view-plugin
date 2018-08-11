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
package io.jenkins.plugins.view.calendar.event;

import org.junit.Test;

import java.text.ParseException;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalendarEventComparatorTest {
    @Test
    public void testCalendarEarlierStart() throws ParseException {
        CalendarEvent c1 = mock(CalendarEvent.class);
        when(c1.getStart()).thenReturn(cal("2018-01-01 10:00:00 UTC"));
        when(c1.getEnd()).thenReturn(cal("2018-01-01 10:30:00 UTC"));
        CalendarEvent c2 = mock(CalendarEvent.class);
        when(c2.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c2.getEnd()).thenReturn(cal("2018-01-01 12:30:00 UTC"));

        assertThat(new CalendarEventComparator().compare(c1, c2), lessThan(0));
    }

    @Test
    public void testCalendarEarlierStartLaterEnd() throws ParseException {
        CalendarEvent c1 = mock(CalendarEvent.class);
        when(c1.getStart()).thenReturn(cal("2018-01-01 10:00:00 UTC"));
        when(c1.getEnd()).thenReturn(cal("2018-01-01 13:30:00 UTC"));
        CalendarEvent c2 = mock(CalendarEvent.class);
        when(c2.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c2.getEnd()).thenReturn(cal("2018-01-01 12:30:00 UTC"));

        assertThat(new CalendarEventComparator().compare(c1, c2), lessThan(0));
    }

    @Test
    public void testCalendarLaterStart() throws ParseException {
        CalendarEvent c1 = mock(CalendarEvent.class);
        when(c1.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c1.getEnd()).thenReturn(cal("2018-01-01 12:30:00 UTC"));
        CalendarEvent c2 = mock(CalendarEvent.class);
        when(c2.getStart()).thenReturn(cal("2018-01-01 10:00:00 UTC"));
        when(c2.getEnd()).thenReturn(cal("2018-01-01 10:30:00 UTC"));

        assertThat(new CalendarEventComparator().compare(c1, c2), greaterThan(0));
    }

    @Test
    public void testCalendarLaterStartEarlierEnd() throws ParseException {
        CalendarEvent c1 = mock(CalendarEvent.class);
        when(c1.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c1.getEnd()).thenReturn(cal("2018-01-01 12:30:00 UTC"));
        CalendarEvent c2 = mock(CalendarEvent.class);
        when(c2.getStart()).thenReturn(cal("2018-01-01 10:00:00 UTC"));
        when(c2.getEnd()).thenReturn(cal("2018-01-01 13:30:00 UTC"));

        assertThat(new CalendarEventComparator().compare(c1, c2), greaterThan(0));
    }

    @Test
    public void testCalendarSameStartEarlierEnd() throws ParseException {
        CalendarEvent c1 = mock(CalendarEvent.class);
        when(c1.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c1.getEnd()).thenReturn(cal("2018-01-01 12:30:00 UTC"));
        CalendarEvent c2 = mock(CalendarEvent.class);
        when(c2.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c2.getEnd()).thenReturn(cal("2018-01-01 13:00:00 UTC"));

        assertThat(new CalendarEventComparator().compare(c1, c2), lessThan(0));
    }

    @Test
    public void testCalendarSameStartLaterEnd() throws ParseException {
        CalendarEvent c1 = mock(CalendarEvent.class);
        when(c1.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c1.getEnd()).thenReturn(cal("2018-01-01 13:00:00 UTC"));
        CalendarEvent c2 = mock(CalendarEvent.class);
        when(c2.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c2.getEnd()).thenReturn(cal("2018-01-01 12:30:00 UTC"));

        assertThat(new CalendarEventComparator().compare(c1, c2), greaterThan(0));
    }

    @Test
    public void testCalendarSameStartSameEnd() throws ParseException {
        CalendarEvent c1 = mock(CalendarEvent.class);
        when(c1.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c1.getEnd()).thenReturn(cal("2018-01-01 13:00:00 UTC"));
        CalendarEvent c2 = mock(CalendarEvent.class);
        when(c2.getStart()).thenReturn(cal("2018-01-01 12:00:00 UTC"));
        when(c2.getEnd()).thenReturn(cal("2018-01-01 13:00:00 UTC"));

        assertThat(new CalendarEventComparator().compare(c1, c2), is(0));
    }
}
