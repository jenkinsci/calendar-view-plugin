package io.jenkins.plugins.view.calendar;

import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class CalendarEventTest {

    private static TimeZone defaultTimeZone;
    private static Locale defaultLocale;

    @BeforeClass
    public static void beforeClass() {
        CalendarEventTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));

        CalendarEventTest.defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
    }

    @AfterClass
    public static void afterClass() {
        TimeZone.setDefault(CalendarEventTest.defaultTimeZone);
        Locale.setDefault(CalendarEventTest.defaultLocale);
    }

    @Test
    public void testFutureEvent() throws ParseException {
        Calendar start = cal("2018-01-01 00:00:00 UTC");
        Calendar end = cal("2018-01-01 00:01:00 UTC");
        long duration =  60 * 1000;

        HealthReport health = mock(HealthReport.class);
        when(health.getIconClassName()).thenReturn("health-icon-class-name");

        AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
        when(project.getFullDisplayName()).thenReturn("Example Project");
        when(project.getUrl()).thenReturn("example/item/url/");
        when(project.getBuildHealth()).thenReturn(health);

        CalendarEvent event = new CalendarEvent((TopLevelItem) project, start, duration);
        assertThat(event.getItem(), is((TopLevelItem)project));
        assertThat(event.getTitle(), is("Example Project"));
        assertThat(event.getStart(), is(start));
        assertThat(event.getEnd(), is(end));
        assertThat(event.getStartAsDateTime(), is("2018-01-01T01:00:00"));
        assertThat(event.getEndAsDateTime(), is("2018-01-01T01:01:00"));
        assertThat(event.getDuration(), is(duration));
        assertThat(event.getDurationString(), containsString("1 Minute"));
        assertThat(event.getUrl(), is("example/item/url/"));
        assertThat(event.getId(), is("example-item-url"));
        assertThat(event.getBuild(), is(nullValue()));
        assertThat(event.isFuture(), is(true));
        assertThat(event.getType(), is(CalendarEventType.FUTURE));
        assertThat(event.getTypeAsClassName(), is("event-future"));
        assertThat(event.getIconClassName(), is("health-icon-class-name"));
        assertThat(event.getLastEvents(), is(notNullValue()));
        assertThat(event.getLastEvents(), hasSize(0));
        assertThat(event.toString(), is("2018-01-01T01:00:00 - 2018-01-01T01:01:00: Example Project"));
    }

    @Test
    public void testPastEvent() throws ParseException {
        testPastEvent(Result.SUCCESS, BallColor.BLUE, CalendarEventType.SUCCESS, "event-success", "icon-blue") ;
        testPastEvent(Result.FAILURE, BallColor.RED, CalendarEventType.FAILURE, "event-failure", "icon-red") ;
        testPastEvent(Result.UNSTABLE, BallColor.YELLOW, CalendarEventType.UNSTABLE, "event-unstable", "icon-yellow") ;
        testPastEvent(Result.NOT_BUILT, BallColor.GREY, CalendarEventType.NOT_BUILT, "event-not-built", "icon-grey") ;
        testPastEvent(Result.ABORTED, BallColor.GREY, CalendarEventType.ABORTED, "event-aborted", "icon-grey") ;
    }

    public void testPastEvent(Result result, BallColor ballColor, CalendarEventType type, String typeClass, String iconClass) throws ParseException {
        Calendar start = cal("2018-01-01 00:00:00 UTC");
        Calendar end = cal("2018-01-01 00:01:00 UTC");
        long duration =  60 * 1000;

        Run build = mock(Run.class);
        when(build.getStartTimeInMillis()).thenReturn(start.getTimeInMillis());
        when(build.getDuration()).thenReturn(duration);
        when(build.getUrl()).thenReturn("example/build/url/");
        when(build.getResult()).thenReturn(result);
        when(build.getFullDisplayName()).thenReturn("Example Build #1");
        when(build.getIconColor()).thenReturn(ballColor);
        when(build.getPreviousBuild()).thenReturn(mock(Run.class));
        when(build.getNextBuild()).thenReturn(mock(Run.class));

        Trigger trigger = mock(Trigger.class);
        when(trigger.getSpec()).thenReturn("0 20 * * *");

        Map<TriggerDescriptor, Trigger> triggers = new HashMap<>();
        triggers.put(mock(TriggerDescriptor.class), trigger);

        AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
        when(project.getFullDisplayName()).thenReturn("Example Project");
        when(project.getUrl()).thenReturn("example/item/url/");
        when(project.getTriggers()).thenReturn(triggers);

        CalendarEvent event = new CalendarEvent((TopLevelItem) project, build);
        assertThat(event.getItem(), is((TopLevelItem)project));
        assertThat(event.getTitle(), is("Example Build #1"));
        assertThat(event.getStart(), is(start));
        assertThat(event.getEnd(), is(end));
        assertThat(event.getStartAsDateTime(), is("2018-01-01T01:00:00"));
        assertThat(event.getEndAsDateTime(), is("2018-01-01T01:01:00"));
        assertThat(event.getDuration(), is(duration));
        assertThat(event.getDurationString(), containsString("1 Minute"));
        assertThat(event.getUrl(), is("example/build/url/"));
        assertThat(event.getId(), is("example-build-url"));
        assertThat(event.getBuild(), is(build));
        assertThat(event.isFuture(), is(false));
        assertThat(event.getType(), is(type));
        assertThat(event.getTypeAsClassName(), is(typeClass));
        assertThat(event.getIconClassName(), is(iconClass));
        assertThat(event.getNextScheduledEvent(), is(notNullValue()));
        assertThat(event.getNextScheduledEvent().isFuture(), is(true));
        assertThat(event.getPreviousEvent(), is(notNullValue()));
        assertThat(event.getPreviousEvent().isFuture(), is(false));
        assertThat(event.getNextEvent(), is(notNullValue()));
        assertThat(event.getNextEvent().isFuture(), is(false));
        assertThat(event.toString(), is("2018-01-01T01:00:00 - 2018-01-01T01:01:00: Example Build #1"));
    }

    @Test
    public void testInRange() throws ParseException {

        Calendar rangeStart = cal("2018-01-01 00:00:00 UTC");
        Calendar rangeEnd = cal("2018-01-02 00:00:00 UTC");

        long duration = 6 * 60 * 60 * 1000;
        TopLevelItem item = mock(TopLevelItem.class);

        // Range:       |           |
        // Event:       #####
        Calendar start1 = cal("2018-01-01 00:00:00 UTC");
        CalendarEvent event1 = new CalendarEvent(item, start1, duration);
        assertThat(event1.getEnd(), is(cal("2018-01-01 06:00:00 UTC")));
        assertThat(event1.isInRange(rangeStart, rangeEnd), is(true));

        // Range:       |           |
        // Event:                   #####
        Calendar start2 = cal("2018-01-02 00:00:00 UTC");
        CalendarEvent event2 = new CalendarEvent(item, start2, duration);
        assertThat(event2.getEnd(), is(cal("2018-01-02 06:00:00 UTC")));
        assertThat(event2.isInRange(rangeStart, rangeEnd), is(not(true)));

        // Range:       |           |
        // Event:   #####
        Calendar start3 = cal("2017-12-31 18:00:00 UTC");
        CalendarEvent event3 = new CalendarEvent(item, start3, duration);
        assertThat(event3.getEnd(), is(cal("2018-01-01 00:00:00 UTC")));
        assertThat(event3.isInRange(rangeStart, rangeEnd), is(not(true)));

        // Range:       |           |
        // Event:               #####
        Calendar start4 = cal("2018-01-01 18:00:00 UTC");
        CalendarEvent event4 = new CalendarEvent(item, start4, duration);
        assertThat(event4.getEnd(), is(cal("2018-01-02 00:00:00 UTC")));
        assertThat(event4.isInRange(rangeStart, rangeEnd), is(true));

        // Range:       |           |
        // Event:     #####
        Calendar start5 = cal("2017-12-31 21:00:00 UTC");
        CalendarEvent event5 = new CalendarEvent(item, start5, duration);
        assertThat(event5.getEnd(), is(cal("2018-01-01 03:00:00 UTC")));
        assertThat(event5.isInRange(rangeStart, rangeEnd), is(true));

        // Range:       |           |
        // Event:                 #####
        Calendar start6 = cal("2018-01-01 21:00:00 UTC");
        CalendarEvent event6 = new CalendarEvent(item, start6, duration);
        assertThat(event6.getEnd(), is(cal("2018-01-02 03:00:00 UTC")));
        assertThat(event6.isInRange(rangeStart, rangeEnd), is(true));

        // Range:       |           |
        // Event:                     #####
        Calendar start7 = cal("2018-01-02 03:00:00 UTC");
        CalendarEvent event7 = new CalendarEvent(item, start7, duration);
        assertThat(event7.getEnd(), is(cal("2018-01-02 09:00:00 UTC")));
        assertThat(event7.isInRange(rangeStart, rangeEnd), is(not(true)));

        // Range:       |           |
        // Event: #####
        Calendar start8 = cal("2017-12-31 03:00:00 UTC");
        CalendarEvent event8 = new CalendarEvent(item, start8, duration);
        assertThat(event8.getEnd(), is(cal("2017-12-31 09:00:00 UTC")));
        assertThat(event8.isInRange(rangeStart, rangeEnd), is(not(true)));
    }

}
