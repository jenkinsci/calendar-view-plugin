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

import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.view.calendar.service.CalendarEventService;
import io.jenkins.plugins.view.calendar.service.CronJobService;
import io.jenkins.plugins.view.calendar.time.Moment;
import io.jenkins.plugins.view.calendar.time.MomentRange;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.hours;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.minutes;
import static io.jenkins.plugins.view.calendar.test.TestUtil.mockScheduledFreeStyleProject;
import static io.jenkins.plugins.view.calendar.test.TestUtil.mockTriggers;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class CalendarEventFactoryTest {

    private static TimeZone defaultTimeZone;
    private static Locale defaultLocale;
    private CalendarEventFactory calendarEventFactory;

    @BeforeClass
    public static void beforeClass() {
        CalendarEventFactoryTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));

        CalendarEventFactoryTest.defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
    }

    @AfterClass
    public static void afterClass() {
        TimeZone.setDefault(CalendarEventFactoryTest.defaultTimeZone);
        Locale.setDefault(CalendarEventFactoryTest.defaultLocale);
    }

    @Before
    public void initCalendarEventFactory() {
        final Moment now = new Moment();
        final CronJobService cronJobService = new CronJobService(now);
        final CalendarEventService calendarEventService = new CalendarEventService(now, cronJobService);
        this.calendarEventFactory = new CalendarEventFactory(calendarEventService);
    }

    @Test
    public void testScheduledEvent() throws ParseException {
        Calendar start = cal("2018-01-01 00:00:00 UTC");
        Calendar end = cal("2018-01-01 00:01:00 UTC");
        long duration =  60 * 1000;

        HealthReport health = mock(HealthReport.class);
        when(health.getIconClassName()).thenReturn("health-icon-class-name");

        FreeStyleProject project = mockScheduledFreeStyleProject("Example Project", "2018-01-01 00:00:00 UTC", minutes(1));
        when(project.getUrl()).thenReturn("example/item/url/");
        when(project.getBuildHealth()).thenReturn(health);

        ScheduledCalendarEvent event = calendarEventFactory.createScheduledEvent(project, start, duration);
        assertThat(event.getJob(), is((Job)project));
        assertThat(event.getTitle(), is("Example Project"));
        assertThat(event.getStart(), is(start));
        assertThat(event.getEnd(), is(end));
        assertThat(event.getState(), is(CalendarEventState.SCHEDULED));
        assertThat(event.getStartAsDateTime(), is("2018-01-01T01:00:00"));
        assertThat(event.getEndAsDateTime(), is("2018-01-01T01:01:00"));
        assertThat(event.getDuration(), is(duration));
        assertThat(event.getDurationString(), containsString("1 Minute"));
        assertThat(event.getUrl(), is("example/item/url/"));
        assertThat(event.getId(), is("example-item-url"));
        assertThat(event.getIconClassName(), is("health-icon-class-name"));
        assertThat(event.getLastEvents(), is(notNullValue()));
        assertThat(event.getLastEvents(), hasSize(0));
        assertThat(event.toString(), is("2018-01-01T01:00:00 - 2018-01-01T01:01:00: Example Project"));
    }

    @Test
    public void testFinishedEvent() throws ParseException {
        testStartedEvent(Result.SUCCESS, BallColor.BLUE, "icon-blue",false, CalendarEventState.FINISHED);
        testStartedEvent(Result.FAILURE, BallColor.RED, "icon-red", false, CalendarEventState.FINISHED);
        testStartedEvent(Result.UNSTABLE, BallColor.YELLOW, "icon-yellow", false, CalendarEventState.FINISHED);
        testStartedEvent(Result.NOT_BUILT, BallColor.GREY, "icon-grey", false, CalendarEventState.FINISHED);
        testStartedEvent(Result.ABORTED, BallColor.GREY, "icon-grey", false, CalendarEventState.FINISHED);
    }

        @Test
    public void testRunningEvent() throws ParseException {
        testStartedEvent(Result.SUCCESS, BallColor.BLUE, "icon-blue", true, CalendarEventState.RUNNING);
        testStartedEvent(Result.FAILURE, BallColor.RED, "icon-red", true, CalendarEventState.RUNNING);
        testStartedEvent(Result.UNSTABLE, BallColor.YELLOW, "icon-yellow", true, CalendarEventState.RUNNING);
        testStartedEvent(Result.NOT_BUILT, BallColor.GREY, "icon-grey", true, CalendarEventState.RUNNING);
        testStartedEvent(Result.ABORTED, BallColor.GREY, "icon-grey", true, CalendarEventState.RUNNING);
    }

    public void testStartedEvent(Result result, BallColor ballColor, String iconClass, boolean running, CalendarEventState state) throws ParseException {
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
        when(build.isBuilding()).thenReturn(running);

        Map<TriggerDescriptor, Trigger<?>> triggers = mockTriggers("0 20 * * *");

        AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
        when(project.getFullName()).thenReturn("Project Name");
        when(project.getFullDisplayName()).thenReturn("Example Project");
        when(project.getUrl()).thenReturn("example/item/url/");
        when(project.getTriggers()).thenReturn(triggers);
        when(project.isBuilding()).thenReturn(running);

        StartedCalendarEvent event = calendarEventFactory.createStartedEvent(project, build);
        assertThat(event.getJob(), is((Job)project));
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
        assertThat(event.getState(), is(state));
        assertThat(event.getIconClassName(), is(iconClass));
        assertThat(event.getNextScheduledEvent(), is(notNullValue()));
        assertThat(event.getNextScheduledEvent().getState(), is(CalendarEventState.SCHEDULED));
        assertThat(event.getPreviousStartedEvent(), is(notNullValue()));
        assertThat(event.getNextStartedEvent().getState(), is(not(CalendarEventState.SCHEDULED)));
        assertThat(event.getNextStartedEvent(), is(notNullValue()));
        assertThat(event.getNextStartedEvent().getState(), is(not(CalendarEventState.SCHEDULED)));
        assertThat(event.toString(), is("2018-01-01T01:00:00 - 2018-01-01T01:01:00: Example Build #1"));
    }

    @Test
    public void testInRange() throws ParseException {

        MomentRange range = MomentRange.range(cal("2018-01-01 00:00:00 UTC"), cal("2018-01-02 00:00:00 UTC"));

        Job item = mock(Job.class);

        // MomentRange:       |           |
        // Event:       #####
        Calendar start1 = cal("2018-01-01 00:00:00 UTC");
        CalendarEvent event1 = calendarEventFactory.createScheduledEvent(item, start1, hours(6));
        assertThat(event1.getEnd(), is(cal("2018-01-01 06:00:00 UTC")));
        assertThat(event1.isInRange(range), is(true));

        // MomentRange:       |           |
        // Event:                   #####
        Calendar start2 = cal("2018-01-02 00:00:00 UTC");
        CalendarEvent event2 = calendarEventFactory.createScheduledEvent(item, start2, hours(6));
        assertThat(event2.getEnd(), is(cal("2018-01-02 06:00:00 UTC")));
        assertThat(event2.isInRange(range), is(not(true)));

        // MomentRange:       |           |
        // Event:   #####
        Calendar start3 = cal("2017-12-31 18:00:00 UTC");
        CalendarEvent event3 = calendarEventFactory.createScheduledEvent(item, start3, hours(6));
        assertThat(event3.getEnd(), is(cal("2018-01-01 00:00:00 UTC")));
        assertThat(event3.isInRange(range), is(not(true)));

        // MomentRange:       |           |
        // Event:               #####
        Calendar start4 = cal("2018-01-01 18:00:00 UTC");
        CalendarEvent event4 = calendarEventFactory.createScheduledEvent(item, start4, hours(6));
        assertThat(event4.getEnd(), is(cal("2018-01-02 00:00:00 UTC")));
        assertThat(event4.isInRange(range), is(true));

        // MomentRange:       |           |
        // Event:     #####
        Calendar start5 = cal("2017-12-31 21:00:00 UTC");
        CalendarEvent event5 = calendarEventFactory.createScheduledEvent(item, start5, hours(6));
        assertThat(event5.getEnd(), is(cal("2018-01-01 03:00:00 UTC")));
        assertThat(event5.isInRange(range), is(true));

        // MomentRange:       |           |
        // Event:                 #####
        Calendar start6 = cal("2018-01-01 21:00:00 UTC");
        CalendarEvent event6 = calendarEventFactory.createScheduledEvent(item, start6, hours(6));
        assertThat(event6.getEnd(), is(cal("2018-01-02 03:00:00 UTC")));
        assertThat(event6.isInRange(range), is(true));

        // MomentRange:       |           |
        // Event:                     #####
        Calendar start7 = cal("2018-01-02 03:00:00 UTC");
        CalendarEvent event7 = calendarEventFactory.createScheduledEvent(item, start7, hours(6));
        assertThat(event7.getEnd(), is(cal("2018-01-02 09:00:00 UTC")));
        assertThat(event7.isInRange(range), is(not(true)));

        // MomentRange:       |           |
        // Event: #####
        Calendar start8 = cal("2017-12-31 03:00:00 UTC");
        CalendarEvent event8 = calendarEventFactory.createScheduledEvent(item, start8, hours(6));
        assertThat(event8.getEnd(), is(cal("2017-12-31 09:00:00 UTC")));
        assertThat(event8.isInRange(range), is(not(true)));

        // MomentRange:       |           |
        // Event:      ###############
        Calendar start9 = cal("2017-12-31 21:00:00 UTC");
        CalendarEvent event9 = calendarEventFactory.createScheduledEvent(item, start9, hours(30));
        assertThat(event9.getEnd(), is(cal("2018-01-02 03:00:00 UTC")));
        assertThat(event9.isInRange(range), is(true));
    }

    @Test
    public void testEventIsAtLeastOneSecondLong() throws ParseException {
        Calendar start = cal("2018-01-01 00:00:00 UTC");
        Calendar end = cal("2018-01-01 00:00:01 UTC");

        Run build = mock(Run.class);
        when(build.getStartTimeInMillis()).thenReturn(start.getTimeInMillis());
        when(build.getDuration()).thenReturn(minutes(0));

        AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));

        StartedCalendarEvent event = calendarEventFactory.createStartedEvent(project, build);
        assertThat(event.getStartAsDateTime(), is("2018-01-01T01:00:00"));
        assertThat(event.getEndAsDateTime(), is("2018-01-01T01:00:01"));
    }
}
