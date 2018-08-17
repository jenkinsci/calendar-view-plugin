package io.jenkins.plugins.view.calendar.time;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.TimeZone;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MomentRangeTest {

    private static TimeZone defaultTimeZone;

    @BeforeClass
    public static void beforeClass() {
        MomentRangeTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    @AfterClass
    public static void afterClass() {
        TimeZone.setDefault(MomentRangeTest.defaultTimeZone);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidRange() throws ParseException {
       new MomentRange(mom("2018-01-02 00:00:00 UTC"), mom("2018-01-01 23:59:59 UTC"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidRangeSameMoment() throws ParseException {
        new MomentRange(mom("2018-01-01 00:00:00 UTC"), mom("2018-01-01 00:00:00 UTC"));
    }

    @Test
    public void testConstructorValidRange() throws ParseException {
        new MomentRange(mom("2018-01-01 00:00:00 UTC"), mom("2018-01-02 00:00:00 UTC"));
    }

    @Test
    public void testIsValidRange() throws ParseException {
        assertThat(MomentRange.isValidRange(mom("2018-01-02 00:00:00 UTC"), mom("2018-01-01 23:59:59 UTC")), is(false));
        assertThat(MomentRange.isValidRange(mom("2018-01-01 00:00:00 UTC"), mom("2018-01-01 00:00:00 UTC")), is(false));
        assertThat(MomentRange.isValidRange(mom("2018-01-01 00:00:00 UTC"), mom("2018-01-02 00:00:00 UTC")), is(true));
    }

    @Test
    public void testToString() throws ParseException {
        String string = new MomentRange(mom("2018-01-01 00:00:00 UTC"), mom("2018-01-02 00:00:00 UTC")).toString();
        assertThat(string, is("2018-01-01T01:00:00 - 2018-01-02T01:00:00"));
    }

    @Test
    public void testDuration() throws ParseException {
        long duration = new MomentRange(mom("2018-01-01 00:00:00 UTC"), mom("2018-01-02 00:00:00 UTC")).duration();
        assertThat(duration, is(hours(24)));
    }
}
