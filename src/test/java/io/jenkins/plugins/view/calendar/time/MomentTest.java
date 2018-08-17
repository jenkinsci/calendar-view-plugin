package io.jenkins.plugins.view.calendar.time;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.TimeZone;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MomentTest {
    private static TimeZone defaultTimeZone;

    @BeforeClass
    public static void beforeClass() {
        MomentTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    @AfterClass
    public static void afterClass() {
        TimeZone.setDefault(MomentTest.defaultTimeZone);
    }

    @Test
    public void testHashCode() throws ParseException {
       int hashCode1 = new Moment(cal("2018-01-01 00:00:00 UTC")).hashCode();
       int hashCode2 = new Moment(cal("2018-01-01 00:00:00 UTC")).hashCode();
       assertThat(hashCode1, is(hashCode2));
    }

    @Test
    public void testEquals() throws ParseException {
       Moment m1 = new Moment(cal("2018-01-01 00:00:00 UTC"));
       Moment m2 = new Moment(cal("2018-01-01 00:00:00 UTC"));
       Moment m3 = new Moment(cal("2018-01-01 00:00:01 UTC"));
       assertThat(m1.equals(m1), is(true));
       assertThat(m1.equals(m2), is(true));
       assertThat(m1.equals(m3), is(false));
       assertThat(m1.equals(new Object()), is(false));
    }

    @Test
    public void testCompareTo() throws ParseException {
        Moment m1 = new Moment(cal("2018-01-01 00:00:00 UTC"));
        Moment m2 = new Moment(cal("2018-01-01 00:00:00 UTC"));
        Moment m3 = new Moment(cal("2018-01-01 00:00:01 UTC"));
        assertThat(m1.compareTo(m1), is(0));
        assertThat(m1.compareTo(m2), is(0));
        assertThat(m1.compareTo(m3), lessThan(0));
        assertThat(m3.compareTo(m1), greaterThan(0));
    }

    @Test
    public void testToString() throws ParseException {
        Moment m = mom("2018-01-01 00:00:00 UTC");
        assertThat(m.toString(), is("2018-01-01T01:00:00"));
    }

    @Test
    public void testNextMinute() throws ParseException {
        assertThat(mom("2018-01-01 00:03:30 UTC").nextMinute(), is(mom("2018-01-01 00:04:00 UTC")));
        assertThat(mom("2018-01-01 00:03:00 UTC").nextMinute(), is(mom("2018-01-01 00:04:00 UTC")));
    }

    @Test
    public void testPreviousMinute() throws ParseException {
        assertThat(mom("2018-01-01 00:03:30 UTC").previousMinute(), is(mom("2018-01-01 00:02:00 UTC")));
        assertThat(mom("2018-01-01 00:03:00 UTC").previousMinute(), is(mom("2018-01-01 00:02:00 UTC")));
    }
}
