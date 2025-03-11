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
package io.jenkins.plugins.view.calendar.service;

import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.util.RunList;
import io.jenkins.plugins.view.calendar.CalendarView.CalendarViewEventsType;
import io.jenkins.plugins.view.calendar.event.CalendarEvent;
import io.jenkins.plugins.view.calendar.event.CalendarEventState;
import io.jenkins.plugins.view.calendar.event.ScheduledCalendarEvent;
import io.jenkins.plugins.view.calendar.event.StartedCalendarEvent;
import io.jenkins.plugins.view.calendar.time.Moment;
import io.jenkins.plugins.view.calendar.util.PluginUtil;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.hours;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.minutes;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.mom;
import static io.jenkins.plugins.view.calendar.test.TestUtil.*;
import static io.jenkins.plugins.view.calendar.time.MomentRange.range;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CalendarEventServiceTest {

    private static TimeZone defaultTimeZone;
    private static Locale defaultLocale;

    @BeforeAll
    static void beforeClass() {
        CalendarEventServiceTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));

        CalendarEventServiceTest.defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);

        PluginUtil.setJenkins(mock(Jenkins.class));
    }

    @AfterAll
    static void afterClass() {
        TimeZone.setDefault(CalendarEventServiceTest.defaultTimeZone);
        Locale.setDefault(CalendarEventServiceTest.defaultLocale);
    }

    private static CalendarEventService getCalendarEventService() {
        final Moment now = new Moment();
        return new CalendarEventService(now, new CronJobService(now));
    }

    private static CalendarEventService getCalendarEventService(Calendar calendar) {
        final Moment now = new Moment(calendar);
        return new CalendarEventService(now, new CronJobService(now));
    }

    @Nested
    class GetCalendarEventsTestCases {
        @Test
        void testCase1() throws ParseException {
            Calendar now = cal("2018-01-01 23:50:00 CET");
            Calendar start = cal("2018-01-02 00:00:00 CET");
            Calendar end = cal("2018-01-03 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    // Finished builds
                    mockFinishedFreeStyleProject("#1a", "2018-01-01 23:45:00 CET", minutes(2)),
                    mockFinishedFreeStyleProject("#1b", "2018-01-01 23:46:00 CET", minutes(4)),

                    // Running builds that started before the selection range
                    // but are not estimated to end within the selection range
                    mockRunningFreeStyleProject("#2", "2018-01-01 23:47:00 CET", minutes(5)),
                    mockRunningFreeStyleProject("#3", "2018-01-01 23:48:00 CET", minutes(12)),
                    mockRunningFreeStyleProject("#5", "2018-01-01 23:50:00 CET", minutes(6)),

                    // Running builds estimated to end in the selection range
                    mockRunningFreeStyleProject("#4", "2018-01-01 23:48:00 CET", minutes(15)),
                    mockRunningFreeStyleProject("#6", "2018-01-01 23:50:00 CET", minutes(11)),

                    // Scheduled builds that start before the selection range
                    // but are not estimated to end within the selection range
                    mockScheduledFreeStyleProject("#7", "51 23 1 1 *", minutes(5)),
                    mockScheduledFreeStyleProject("#8", "52 23 1 1 *", minutes(8)),

                    // Scheduled builds that start before the selection range
                    // but are estimated to end in the selection range
                    mockScheduledFreeStyleProject("#9a", "51 23 1 1 *", minutes(15)),
                    mockScheduledFreeStyleProject("#9b", "59 23 1 1 *", minutes(8)),

                    // Scheduled builds that start within the selection range
                    mockScheduledFreeStyleProject("#10", "0 0 2 1 *", minutes(8)),
                    mockScheduledFreeStyleProject("#11", "5 0 2 1 *", minutes(8)),
                    mockScheduledFreeStyleProject("#12", "50 23 2 1 *", minutes(20)),

                    // Scheduled builds that start after the selection range
                    mockScheduledFreeStyleProject("#13", "0 0 3 1 *", minutes(10)),
                    mockScheduledFreeStyleProject("#14", "5 0 3 1 *", minutes(12))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(7));
            assertThat(titlesOf(events), containsInAnyOrder("#4", "#6", "#9a", "#9b", "#10", "#11", "#12"));
        }

        @Test
        void testCase2() throws ParseException {
            Calendar now = cal("2018-01-02 00:00:00 CET");
            Calendar start = cal("2018-01-02 00:00:00 CET");
            Calendar end = cal("2018-01-03 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    // Finished builds
                    mockFinishedFreeStyleProject("#1", "2018-01-01 23:50:00 CET", minutes(2)),
                    mockFinishedFreeStyleProject("#2", "2018-01-01 23:52:00 CET", minutes(8)),

                    // Running builds that started before the selection range
                    // but are not estimated to end within the selection range
                    mockRunningFreeStyleProject("#3", "2018-01-01 23:54:00 CET", minutes(6)),

                    // Running builds estimated to end in the selection range
                    mockRunningFreeStyleProject("#4", "2018-01-01 23:56:00 CET", minutes(10)),
                    mockRunningFreeStyleProject("#5", "2018-01-02 00:00:00 CET", minutes(5)),

                    // Scheduled builds that start within the selection range
                    mockScheduledFreeStyleProject("#6", "0 12 2 1 *", minutes(5)),
                    mockScheduledFreeStyleProject("#7", "55 23 2 1 *", minutes(8)),

                    // Scheduled builds that start after the selection range
                    mockScheduledFreeStyleProject("#8", "0 0 3 1 *", minutes(10)),
                    mockScheduledFreeStyleProject("#9", "5 0 3 1 *", minutes(12))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(titlesOf(events), containsInAnyOrder("#4", "#5", "#6", "#7"));
        }

        @Test
        void testCase3() throws ParseException {
            Calendar now = cal("2018-01-02 12:00:00 CET");
            Calendar start = cal("2018-01-02 00:00:00 CET");
            Calendar end = cal("2018-01-03 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    // Finished builds that don't start or end in selection range
                    mockFinishedFreeStyleProject("#1", "2018-01-01 23:45:00 CET", minutes(2)),
                    mockFinishedFreeStyleProject("#2", "2018-01-01 23:46:00 CET", minutes(4)),

                    // Running builds started before the selection range
                    mockRunningFreeStyleProject("#6", "2018-01-01 23:48:00 CET", hours(8)),

                    // Finished builds that start or end in the selection range
                    mockFinishedFreeStyleProject("#3", "2018-01-01 23:50:00 CET", minutes(12)),
                    mockFinishedFreeStyleProject("#4", "2018-01-02 00:00:00 CET", minutes(8)),
                    mockFinishedFreeStyleProject("#5", "2018-01-02 06:00:00 CET", minutes(20)),

                    // Running builds started in the selection range
                    mockRunningFreeStyleProject("#7", "2018-01-02 11:50:00 CET", minutes(20)),
                    mockRunningFreeStyleProject("#8", "2018-01-02 12:00:00 CET", minutes(11)),

                    // Scheduled builds that start within the selection range
                    mockScheduledFreeStyleProject("#9", "1 12 2 1 *", minutes(8)),
                    mockScheduledFreeStyleProject("#10", "50 23 2 1 *", minutes(20)),

                    // Scheduled builds that start after the selection range
                    mockScheduledFreeStyleProject("#11", "0 0 3 1 *", minutes(10)),
                    mockScheduledFreeStyleProject("#12", "5 0 3 1 *", minutes(12))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(8));
            assertThat(titlesOf(events), containsInAnyOrder("#3", "#4", "#5", "#6", "#7", "#8", "#9", "#10"));
        }

        @Test
        void testCase4() throws ParseException {
            Calendar now = cal("2018-01-03 00:00:00 CET");
            Calendar start = cal("2018-01-02 00:00:00 CET");
            Calendar end = cal("2018-01-03 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    // Finished builds that don't start or end in selection range
                    mockFinishedFreeStyleProject("#1", "2018-01-01 23:45:00 CET", minutes(2)),
                    mockFinishedFreeStyleProject("#2", "2018-01-01 23:46:00 CET", minutes(4)),

                    // Running builds started before the selection range
                    mockRunningFreeStyleProject("#6", "2018-01-01 23:48:00 CET", hours(30)),

                    // Finished builds that start or end in the selection range
                    mockFinishedFreeStyleProject("#3", "2018-01-01 23:50:00 CET", minutes(12)),
                    mockFinishedFreeStyleProject("#4", "2018-01-02 00:00:00 CET", minutes(8)),
                    mockFinishedFreeStyleProject("#5", "2018-01-02 06:00:00 CET", minutes(20)),

                    // Running builds started in the selection range
                    mockRunningFreeStyleProject("#7", "2018-01-02 23:50:00 CET", minutes(20)),

                    // Running builds started after the selection range
                    mockRunningFreeStyleProject("#8", "2018-01-03 00:00:00 CET", minutes(11)),

                    // Scheduled builds that start after the selection range
                    mockScheduledFreeStyleProject("#9", "1 0 3 1 *", minutes(8))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(5));
            assertThat(titlesOf(events), containsInAnyOrder("#3", "#4", "#5", "#6", "#7"));
        }

        @Test
        void testCase5() throws ParseException {
            Calendar now = cal("2018-01-03 00:10:00 CET");
            Calendar start = cal("2018-01-02 00:00:00 CET");
            Calendar end = cal("2018-01-03 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    // Finished builds that don't start or end in selection range
                    mockFinishedFreeStyleProject("#1", "2018-01-01 23:45:00 CET", minutes(2)),
                    mockFinishedFreeStyleProject("#2", "2018-01-01 23:46:00 CET", minutes(4)),

                    // Running builds started before the selection range
                    mockRunningFreeStyleProject("#9", "2018-01-01 23:48:00 CET", hours(30)),

                    // Finished builds that start or end in the selection range
                    mockFinishedFreeStyleProject("#3", "2018-01-01 23:50:00 CET", minutes(12)),
                    mockFinishedFreeStyleProject("#4", "2018-01-02 00:00:00 CET", minutes(8)),
                    mockFinishedFreeStyleProject("#5", "2018-01-02 06:00:00 CET", minutes(2)),
                    mockFinishedFreeStyleProject("#6", "2018-01-02 23:50:00 CET", minutes(20)),

                    // Finished builds started after the selection range
                    mockFinishedFreeStyleProject("#7", "2018-01-03 00:00:00 CET", minutes(5)),
                    mockFinishedFreeStyleProject("#8", "2018-01-03 00:01:00 CET", minutes(2)),

                    // Running builds started in the selection range
                    mockRunningFreeStyleProject("#10", "2018-01-02 23:51:00 CET", minutes(25)),

                    // Running builds started after the selection range
                    mockRunningFreeStyleProject("#11a", "2018-01-03 00:00:00 CET", minutes(13)),
                    mockRunningFreeStyleProject("#11b", "2018-01-03 00:02:00 CET", minutes(13)),
                    mockRunningFreeStyleProject("#12", "2018-01-03 00:10:00 CET", minutes(7)),

                    // Scheduled builds that start after the selection range
                    mockScheduledFreeStyleProject("#13", "10 0 3 1 *", minutes(8))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(6));
            assertThat(titlesOf(events), containsInAnyOrder("#3", "#4", "#5", "#6", "#9", "#10"));
        }
    }

    @Nested
    class GetCalendarEventsTestScheduledStart {
        @Test
        void testOneMinuteBeforeScheduledStart() throws ParseException {
            Calendar now = cal("2018-01-04 23:43:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            FreeStyleProject project = mockScheduledFreeStyleProject("project", "44 23 * * *", minutes(10));

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(List.of(project), range(start, end), CalendarViewEventsType.ALL);

            assertThat(calendarEvents, hasSize(3));
            assertThat(calendarEvents.get(0).getStart(), is(mom("2018-01-04 23:44:00 CET")));
            assertThat(calendarEvents.get(0).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(calendarEvents.get(1).getStart(), is(mom("2018-01-05 23:44:00 CET")));
            assertThat(calendarEvents.get(1).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(calendarEvents.get(2).getStart(), is(mom("2018-01-06 23:44:00 CET")));
            assertThat(calendarEvents.get(2).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testThirtySecondsBeforeScheduledStart() throws ParseException {
            Calendar now = cal("2018-01-04 23:43:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            FreeStyleProject project = mockScheduledFreeStyleProject("project", "44 23 * * *", minutes(10));

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(List.of(project), range(start, end), CalendarViewEventsType.ALL);

            assertThat(calendarEvents, hasSize(3));
            assertThat(calendarEvents.get(0).getStart(), is(mom("2018-01-04 23:44:00 CET")));
            assertThat(calendarEvents.get(0).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(calendarEvents.get(1).getStart(), is(mom("2018-01-05 23:44:00 CET")));
            assertThat(calendarEvents.get(1).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(calendarEvents.get(2).getStart(), is(mom("2018-01-06 23:44:00 CET")));
            assertThat(calendarEvents.get(2).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testExactSecondOfScheduledStart() throws ParseException {
            Calendar now = cal("2018-01-04 23:44:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-04 23:44:00 CET", minutes(3)),
                    mockScheduledFreeStyleProject("project", "44 23 * * *", minutes(10))
            );

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(calendarEvents, hasSize(3));
            assertThat(calendarEvents.get(0).getStart(), is(mom("2018-01-04 23:44:00 CET")));
            assertThat(calendarEvents.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(calendarEvents.get(1).getStart(), is(mom("2018-01-05 23:44:00 CET")));
            assertThat(calendarEvents.get(1).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(calendarEvents.get(2).getStart(), is(mom("2018-01-06 23:44:00 CET")));
            assertThat(calendarEvents.get(1).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testThirtySecondsAfterScheduledStart() throws ParseException {
            Calendar now = cal("2018-01-04 23:44:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-04 23:44:00 CET", minutes(3)),
                    mockScheduledFreeStyleProject("project", "44 23 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(3));
            assertThat(events.get(0).getStart(), is(mom("2018-01-04 23:44:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(1).getStart(), is(mom("2018-01-05 23:44:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(2).getStart(), is(mom("2018-01-06 23:44:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testOneMinuteAfterScheduledStart() throws ParseException {
            Calendar now = cal("2018-01-04 23:45:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-04 23:44:00 CET", minutes(3)),
                    mockScheduledFreeStyleProject("project", "44 23 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events.get(0).getStart(), is(mom("2018-01-04 23:44:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(1).getStart(), is(mom("2018-01-05 23:44:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(2).getStart(), is(mom("2018-01-06 23:44:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.SCHEDULED));
        }
    }

    @Nested
    class GetCalendarEventsTestStartRange {
        @Test
        void testOneMinuteBeforeStartRange() throws ParseException {
            Calendar now = cal("2017-12-31 23:59:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2017-12-31 23:59:00 CET", minutes(10)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2017-12-31 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(1).getStart(), is(mom("2018-01-01 00:00:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(2).getStart(), is(mom("2018-01-01 00:01:00 CET")));
            assertThat(events.get(2).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(3).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(3).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testThirtySecondsBeforeStartRange() throws ParseException {
            Calendar now = cal("2017-12-31 23:59:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2017-12-31 23:59:00 CET", minutes(3)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2017-12-31 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(1).getStart(), is(mom("2018-01-01 00:00:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(2).getStart(), is(mom("2018-01-01 00:01:00 CET")));
            assertThat(events.get(2).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(3).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(3).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testExactSecondOfStartRange() throws ParseException {
            Calendar now = cal("2018-01-01 00:00:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2017-12-31 23:59:00 CET", minutes(3)),
                    mockRunningFreeStyleProject("build", "2018-01-01 00:00:00 CET", minutes(3)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2017-12-31 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(1).getStart(), is(mom("2018-01-01 00:00:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(2).getStart(), is(mom("2018-01-01 00:01:00 CET")));
            assertThat(events.get(2).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(3).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(3).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testThirtySecondsAfterStartRange() throws ParseException {
            Calendar now = cal("2018-01-01 00:00:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2017-12-31 23:59:00 CET", minutes(3)),
                    mockRunningFreeStyleProject("build", "2018-01-01 00:00:00 CET", minutes(3)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2017-12-31 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(1).getStart(), is(mom("2018-01-01 00:00:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(2).getStart(), is(mom("2018-01-01 00:01:00 CET")));
            assertThat(events.get(2).getState(), is(CalendarEventState.SCHEDULED));
            assertThat(events.get(3).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(3).getState(), is(CalendarEventState.SCHEDULED));
        }

        @Test
        void testOneMinuteAfterStartRange() throws ParseException {
            Calendar now = cal("2018-01-01 00:01:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2017-12-31 23:59:00 CET", minutes(3)),
                    mockRunningFreeStyleProject("build", "2018-01-01 00:00:00 CET", minutes(3)),
                    mockRunningFreeStyleProject("build", "2018-01-01 00:01:00 CET", minutes(3)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2017-12-31 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(1).getStart(), is(mom("2018-01-01 00:00:00 CET")));
            assertThat(events.get(1).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(2).getStart(), is(mom("2018-01-01 00:01:00 CET")));
            assertThat(events.get(2).getState(), is(CalendarEventState.RUNNING));
            assertThat(events.get(3).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(3).getState(), is(CalendarEventState.SCHEDULED));
        }
    }

    @Nested
    class GetCalendarEventsTestEndRange {
        @Test
        void testOneMinuteBeforeEndRange() throws ParseException {
            Calendar now = cal("2018-01-01 23:59:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-01 23:59:00 CET", minutes(10)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(1));
            assertThat(events.get(0).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
        }

        @Test
        void testThirtySecondsBeforeEndRange() throws ParseException {
            Calendar now = cal("2018-01-01 23:59:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-01 23:59:00 CET", minutes(10)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(1));
            assertThat(events.get(0).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
        }

        @Test
        void testExactSecondOfEndRange() throws ParseException {
            Calendar now = cal("2018-01-02 00:00:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-01 23:59:00 CET", minutes(10)),
                    mockRunningFreeStyleProject("build", "2018-01-02 00:00:00 CET", minutes(10)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(1));
            assertThat(events.get(0).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
        }

        @Test
        void testThirtySecondsAfterEndRange() throws ParseException {
            Calendar now = cal("2018-01-02 00:00:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-01 23:59:00 CET", minutes(10)),
                    mockRunningFreeStyleProject("build", "2018-01-02 00:00:00 CET", minutes(10)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(1));
            assertThat(events.get(0).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
        }

        @Test
        void testOneMinuteAfterEndRange() throws ParseException {
            Calendar now = cal("2018-01-02 00:01:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build", "2018-01-01 23:59:00 CET", minutes(10)),
                    mockRunningFreeStyleProject("build", "2018-01-02 00:00:00 CET", minutes(10)),
                    mockRunningFreeStyleProject("build", "2018-01-02 00:01:00 CET", minutes(10)),
                    mockScheduledFreeStyleProject("project", "59 23 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "0 0 * * *", minutes(10)),
                    mockScheduledFreeStyleProject("project", "1 0 * * *", minutes(10))
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(1));
            assertThat(events.get(0).getStart(), is(mom("2018-01-01 23:59:00 CET")));
            assertThat(events.get(0).getState(), is(CalendarEventState.RUNNING));
        }
    }

    @Nested
    class GetScheduledEventsForwardTests {
        @Test
        void testNoJobs() {
            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsForward(
                    new ArrayList<>(), null, null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(0));
        }

        @Test
        void testNoTriggers() {
            FreeStyleProject project = mockFreeStyleProject();

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsForward(
                    Collections.singletonList(project), null, null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(0));
        }

        @Test
        void testNoCronTabs() {
            FreeStyleProject project = mockScheduledFreeStyleProject("project", "", minutes(0));

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsForward(
                    List.of(project), null, null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(0));
        }

        @Test
        void testWithBuilds() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            FreeStyleProject project = mockScheduledFreeStyleProject("project", "0 21 * * *", minutes(10));

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsForward(
                    List.of(project), range(start, end), range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2018-01-01 21:00:00 CET")));
            assertThat(events.get(1).getStart(), is(mom("2018-01-02 21:00:00 CET")));
            assertThat(events.get(2).getStart(), is(mom("2018-01-03 21:00:00 CET")));
            assertThat(events.get(3).getStart(), is(mom("2018-01-04 21:00:00 CET")));
        }

        @Test
        void testHash() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-05 00:00:00 CET");

            FreeStyleProject project = mockScheduledFreeStyleProject("HashThisName", "H 21 * * *", minutes(10));

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsForward(
                    List.of(project), range(start, end), range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2018-01-01 21:48:00 CET")));
            assertThat(events.get(1).getStart(), is(mom("2018-01-02 21:48:00 CET")));
            assertThat(events.get(2).getStart(), is(mom("2018-01-03 21:48:00 CET")));
            assertThat(events.get(3).getStart(), is(mom("2018-01-04 21:48:00 CET")));
        }
    }

    @Nested
    class GetScheduledEventsBackwardTests {
        @Test
        void testNoJobs() {
            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsBackward(
                    new ArrayList<>(), null, null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(0));
        }

        @Test
        void testNoTriggers() {
            FreeStyleProject project = mockFreeStyleProject();

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsBackward(
                    Collections.singletonList(project), null, null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(0));
        }

        @Test
        void testNoCronTabs() {
            FreeStyleProject project = mockScheduledFreeStyleProject("project", "", minutes(0));

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsBackward(
                    List.of(project), null, null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(0));
        }

        @Test
        void testWithBuilds() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            FreeStyleProject project = mockScheduledFreeStyleProject("project", "0 21 * * *", minutes(10));

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsBackward(
                    List.of(project), range(start, end), range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2018-01-04 21:00:00 CET")));
            assertThat(events.get(1).getStart(), is(mom("2018-01-03 21:00:00 CET")));
            assertThat(events.get(2).getStart(), is(mom("2018-01-02 21:00:00 CET")));
            assertThat(events.get(3).getStart(), is(mom("2018-01-01 21:00:00 CET")));
        }

        @Test
        void testHash() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-05 00:00:00 CET");

            FreeStyleProject project = mockScheduledFreeStyleProject("HashThisName", "H 21 * * *", minutes(10));

            List<ScheduledCalendarEvent> events = getCalendarEventService().getScheduledEventsBackward(
                    List.of(project), range(start, end), range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(4));
            assertThat(events.get(0).getStart(), is(mom("2018-01-04 21:48:00 CET")));
            assertThat(events.get(1).getStart(), is(mom("2018-01-03 21:48:00 CET")));
            assertThat(events.get(2).getStart(), is(mom("2018-01-02 21:48:00 CET")));
            assertThat(events.get(3).getStart(), is(mom("2018-01-01 21:48:00 CET")));
        }
    }

    @Nested
    class GetStartedEventsTests {
        @Test
        void testNoJobs() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            List<StartedCalendarEvent> events = getCalendarEventService().getStartedEvents(new ArrayList<>(), range(start, end), null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(0));
        }

        @Test
        void testWithFinishedBuilds() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            List<FreeStyleProject> projects = asList(
                    mockFinishedFreeStyleProject("build1", "2017-12-31 23:49:59 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build2", "2017-12-31 23:50:00 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build3", "2017-12-31 23:59:59 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build4", "2018-01-01 00:00:00 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build5", "2018-01-01 00:01:00 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build6", "2018-01-04 23:59:59 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build7", "2018-01-05 00:00:00 UTC", minutes(10))
            );

            List<StartedCalendarEvent> events = getCalendarEventService().getStartedEvents(projects, range(start, end), null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(4));
            assertThat(titlesOf(events), containsInAnyOrder("build3", "build4", "build5", "build6"));
        }

        @Test
        void testWithRunningBuilds() throws ParseException {
            Calendar now = cal("2018-01-05 00:00:00 UTC");
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            List<FreeStyleProject> projects = asList(
                    mockRunningFreeStyleProject("build3", "2017-12-31 23:59:59 UTC", minutes(10)),
                    mockRunningFreeStyleProject("build4", "2018-01-01 00:00:00 UTC", minutes(10)),
                    mockRunningFreeStyleProject("build5", "2018-01-01 00:01:00 UTC", minutes(10)),
                    mockRunningFreeStyleProject("build6", "2018-01-04 23:59:59 UTC", minutes(10)),
                    mockRunningFreeStyleProject("build7", "2018-01-05 00:00:00 UTC", minutes(10))
            );

            List<StartedCalendarEvent> events = getCalendarEventService(now).getStartedEvents(projects, range(start, end), null, CalendarViewEventsType.ALL);
            assertThat(events, hasSize(4));
            assertThat(titlesOf(events), containsInAnyOrder("build3", "build4", "build5", "build6"));
        }

        @Test
        void testWithFinishedAndRunningBuilds() throws ParseException {
            Calendar now = cal("2018-01-05 00:00:00 UTC");
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            List<FreeStyleProject> projects = asList(
                    mockFinishedFreeStyleProject("build2", "2017-12-31 23:50:00 UTC", minutes(10)),
                    mockRunningFreeStyleProject("build3", "2017-12-31 23:59:59 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build4", "2018-01-01 00:00:00 UTC", minutes(10)),
                    mockRunningFreeStyleProject("build5", "2018-01-01 00:01:00 UTC", minutes(10)),
                    mockFinishedFreeStyleProject("build6", "2018-01-04 23:59:59 UTC", minutes(10)),
                    mockRunningFreeStyleProject("build7", "2018-01-05 00:00:00 UTC", minutes(10))
            );

            List<StartedCalendarEvent> events;

            events = getCalendarEventService(now).getFinishedEvents(projects, range(start, end), CalendarViewEventsType.ALL);
            assertThat(events, hasSize(2));
            assertThat(titlesOf(events), containsInAnyOrder("build4", "build6"));

            events = getCalendarEventService(now).getRunningEvents(projects, range(start, end), CalendarViewEventsType.ALL);
            assertThat(events, hasSize(2));
            assertThat(titlesOf(events), containsInAnyOrder("build3", "build5"));
        }

        @Test
        void testStateNotScheduled() {
            assertThrows(IllegalArgumentException.class, () ->
                    getCalendarEventService().getStartedEvents(new ArrayList<>(), null, CalendarEventState.SCHEDULED, CalendarViewEventsType.ALL));
        }

        @Test
        void testThatJobAndBuildMustBeRunning() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            RunList<FreeStyleBuild> finishedBuild = mockBuilds(
                    mockFinishedFreeStyleBuild("build", "2018-01-01 12:00:00 UTC", minutes(10), Result.SUCCESS)
            );
            RunList<FreeStyleBuild> runningBuild = mockBuilds(
                    mockRunningFreeStyleBuild("build", "2018-01-01 12:00:00 UTC", minutes(10))
            );

            FreeStyleProject runningProjectWithFinishedBuild = mockFreeStyleProject();
            when(runningProjectWithFinishedBuild.getBuilds()).thenReturn(finishedBuild);
            when(runningProjectWithFinishedBuild.isBuilding()).thenReturn(true);

            FreeStyleProject finishedProjectWithRunningBuild = mockFreeStyleProject();
            when(finishedProjectWithRunningBuild.getBuilds()).thenReturn(runningBuild);
            when(finishedProjectWithRunningBuild.isBuilding()).thenReturn(false);

            assertThat(getCalendarEventService().getRunningEvents(List.of(runningProjectWithFinishedBuild), range(start, end), CalendarViewEventsType.ALL), hasSize(0));
            assertThat(getCalendarEventService().getRunningEvents(List.of(finishedProjectWithRunningBuild), range(start, end), CalendarViewEventsType.ALL), hasSize(0));
        }
    }

    @Nested
    class GetLastEventsTests {
        @Test
        void testHasNoPastBuilds() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getJob()).thenReturn(mockFreeStyleProject());

            List<StartedCalendarEvent> lastEvents = getCalendarEventService().getLastEvents(event, 2);
            assertThat(lastEvents, hasSize(0));
        }

        @Test
        void testFreeStyleProjectWithLastBuilds() throws ParseException {
            final FreeStyleBuild build1 = mockFinishedFreeStyleBuild("build1", "2018-01-01 01:00:00 CET", minutes(10), Result.FAILURE);

            final FreeStyleBuild build2 = mockFinishedFreeStyleBuild("build2", "2018-01-01 02:00:00 CET", minutes(10), Result.SUCCESS);
            when(build2.getPreviousBuild()).thenReturn(build1);

            final FreeStyleBuild build3 = mockFinishedFreeStyleBuild("build3", "2018-01-01 03:00:00 CET", minutes(10), Result.ABORTED);
            when(build3.getPreviousBuild()).thenReturn(build2);

            final FreeStyleBuild build4 = mockFinishedFreeStyleBuild("build4", "2018-01-01 04:00:00 CET", minutes(10), Result.UNSTABLE);
            when(build4.getPreviousBuild()).thenReturn(build3);

            FreeStyleProject project = new FreeStyleProject(mock(ItemGroup.class), "project") {
                @Override
                public FreeStyleBuild getLastBuild() {
                    return build4;
                }

                @Override
                public List<FreeStyleBuild> getLastBuildsOverThreshold(int numberOfBuilds, Result threshold) {
                    List<FreeStyleBuild> builds = List.of(build4, build3, build2, build1);
                    return builds.subList(0, Math.min(numberOfBuilds, builds.size()));
                }

                @Override
                public String getShortUrl() {
                    return "";
                }
            };

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getJob()).thenReturn(project);

            List<StartedCalendarEvent> lastEvents;

            lastEvents = getCalendarEventService().getLastEvents(event, 5);
            assertThat(lastEvents, hasSize(4));
            assertThat(lastEvents.get(0).getBuild(), is(build4));
            assertThat(lastEvents.get(1).getBuild(), is(build3));
            assertThat(lastEvents.get(2).getBuild(), is(build2));
            assertThat(lastEvents.get(3).getBuild(), is(build1));

            lastEvents = getCalendarEventService().getLastEvents(event, 3);
            assertThat(lastEvents, hasSize(3));
            assertThat(lastEvents.get(0).getBuild(), is(build4));
            assertThat(lastEvents.get(1).getBuild(), is(build3));
            assertThat(lastEvents.get(2).getBuild(), is(build2));
        }

        @Issue("JENKINS-52797")
        @Test
        void testMatrixProjectWithLastBuilds() throws ParseException {
            final MatrixBuild build1 = mockFinishedBuild(MatrixBuild.class, "build1", "2018-01-01 01:00:00 CET", minutes(10), Result.FAILURE);

            final MatrixBuild build2 = mockFinishedBuild(MatrixBuild.class, "build2", "2018-01-01 02:00:00 CET", minutes(10), Result.SUCCESS);
            when(build2.getPreviousBuild()).thenReturn(build1);

            final MatrixBuild build3 = mockFinishedBuild(MatrixBuild.class, "build3", "2018-01-01 03:00:00 CET", minutes(10), Result.ABORTED);
            when(build3.getPreviousBuild()).thenReturn(build2);

            final MatrixBuild build4 = mockFinishedBuild(MatrixBuild.class, "build4", "2018-01-01 04:00:00 CET", minutes(10), Result.UNSTABLE);
            when(build4.getPreviousBuild()).thenReturn(build3);

            MatrixProject project = new MatrixProject(mock(ItemGroup.class), "project") {
                @Override
                public MatrixBuild getLastBuild() {
                    return build4;
                }

                @Override
                public List<MatrixBuild> getLastBuildsOverThreshold(int numberOfBuilds, Result threshold) {
                    List<MatrixBuild> builds = List.of(build4, build3, build2, build1);
                    return builds.subList(0, Math.min(numberOfBuilds, builds.size()));
                }

                @Override
                public String getShortUrl() {
                    return "";
                }
            };

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getJob()).thenReturn(project);

            List<StartedCalendarEvent> lastEvents;

            lastEvents = getCalendarEventService().getLastEvents(event, 5);
            assertThat(lastEvents, hasSize(4));
            assertThat(lastEvents.get(0).getBuild(), is(build4));
            assertThat(lastEvents.get(1).getBuild(), is(build3));
            assertThat(lastEvents.get(2).getBuild(), is(build2));
            assertThat(lastEvents.get(3).getBuild(), is(build1));

            lastEvents = getCalendarEventService().getLastEvents(event, 3);
            assertThat(lastEvents, hasSize(3));
            assertThat(lastEvents.get(0).getBuild(), is(build4));
            assertThat(lastEvents.get(1).getBuild(), is(build3));
            assertThat(lastEvents.get(2).getBuild(), is(build2));
        }
    }

    @Nested
    class GetPreviousEventTests {
        @Test
        void testHasNoBuilds() {
            StartedCalendarEvent event = mock(StartedCalendarEvent.class);
            assertThat(getCalendarEventService().getPreviousEvent(event), is(nullValue()));
        }

        @Test
        void testHasNoPreviousBuild() {
            StartedCalendarEvent event = mock(StartedCalendarEvent.class);
            when(event.getBuild()).thenReturn(mock(Run.class));

            assertThat(getCalendarEventService().getPreviousEvent(event), is(nullValue()));
        }

        @Test
        void testHasPreviousBuild() {
            Run previousBuild = mock(Run.class);

            Run build = mock(Run.class);
            when(build.getPreviousBuild()).thenReturn(previousBuild);

            StartedCalendarEvent event = mock(StartedCalendarEvent.class);
            when(event.getBuild()).thenReturn(build);
            when(event.getJob()).thenReturn(mock(Job.class));

            StartedCalendarEvent previousEvent = getCalendarEventService().getPreviousEvent(event);
            assertThat(previousEvent, is(notNullValue()));
            assertThat(previousEvent.getBuild(), is(previousBuild));
        }
    }

    @Nested
    class GetNextEventTests {
        @Test
        void testHasNoBuilds() {
            StartedCalendarEvent event = mock(StartedCalendarEvent.class);
            assertThat(getCalendarEventService().getNextEvent(event), is(nullValue()));
        }

        @Test
        void testHasNoPreviousBuild() {
            StartedCalendarEvent event = mock(StartedCalendarEvent.class);
            when(event.getBuild()).thenReturn(mock(Run.class));

            assertThat(getCalendarEventService().getNextEvent(event), is(nullValue()));
        }

        @Test
        void testHasPreviousBuild() throws ParseException {
            Run nextBuild = mock(Run.class);
            when(nextBuild.getStartTimeInMillis()).thenReturn(cal("2018-01-01 00:00:00 CET").getTimeInMillis());

            Run build = mock(Run.class);
            when(build.getNextBuild()).thenReturn(nextBuild);

            StartedCalendarEvent event = mock(StartedCalendarEvent.class);
            when(event.getBuild()).thenReturn(build);
            when(event.getJob()).thenReturn(mock(Job.class));

            StartedCalendarEvent nextEvent = getCalendarEventService().getNextEvent(event);
            assertThat(nextEvent, is(notNullValue()));
            assertThat(nextEvent.getBuild(), is(nextBuild));
        }
    }

    @Nested
    class GetNextScheduledEventTests {
        @Test
        void testHasNoNextScheduledEvent() {
            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getJob()).thenReturn(project);

            CalendarEvent nextScheduledEvent = getCalendarEventService().getNextScheduledEvent(event, CalendarViewEventsType.ALL);
            assertThat(nextScheduledEvent, is(nullValue()));
        }

        @Test
        void testHasNextScheduledEvent() {
            FreeStyleProject project = mockScheduledFreeStyleProject("project", "0 * * * *", minutes(10));

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getJob()).thenReturn(project);

            ScheduledCalendarEvent nextScheduledEvent = getCalendarEventService().getNextScheduledEvent(event, CalendarViewEventsType.ALL);
            assertThat(nextScheduledEvent, is(notNullValue()));
        }
    }

    @Nested
    class GetDifferentEventTypeEventsTest {

        @Test
        void testAllEvents() throws ParseException {
            Calendar now = cal("2018-01-01 05:00:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = List.of(
                    mockFinishedFreeStyleProjectWithBuildAndPollingTrigger("#1", "2018-01-01 01:00:00 CET", minutes(30), "0 1-19/6 * * *", "0 4-22/6 * * *")
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.ALL);

            assertThat(events, hasSize(7));
            assertThat(toStringOf(events), containsInAnyOrder("2018-01-01T01:00:00 - 2018-01-01T01:30:00: #1",
                    "2018-01-01T07:00:00 - 2018-01-01T07:30:00: #1", "2018-01-01T10:00:00 - 2018-01-01T10:30:00: #1",
                    "2018-01-01T13:00:00 - 2018-01-01T13:30:00: #1", "2018-01-01T16:00:00 - 2018-01-01T16:30:00: #1",
                    "2018-01-01T19:00:00 - 2018-01-01T19:30:00: #1", "2018-01-01T22:00:00 - 2018-01-01T22:30:00: #1"));
        }

        @Test
        void testBuildEvents() throws ParseException {
            Calendar now = cal("2018-01-01 05:00:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = List.of(
                    mockFinishedFreeStyleProjectWithBuildAndPollingTrigger("#1", "2018-01-01 01:00:00 CET", minutes(30), "0 1-19/6 * * *", "0 4-22/6 * * *")
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.BUILDS);

            assertThat(events, hasSize(4));
            assertThat(toStringOf(events), containsInAnyOrder("2018-01-01T01:00:00 - 2018-01-01T01:30:00: #1",
                    "2018-01-01T07:00:00 - 2018-01-01T07:30:00: #1", "2018-01-01T13:00:00 - 2018-01-01T13:30:00: #1",
                    "2018-01-01T19:00:00 - 2018-01-01T19:30:00: #1"));
        }

        @Test
        void testPollingEvents() throws ParseException {
            Calendar now = cal("2018-01-01 05:00:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-02 00:00:00 CET");

            List<FreeStyleProject> projects = List.of(
                    mockFinishedFreeStyleProjectWithBuildAndPollingTrigger("#1", "2018-01-01 01:00:00 CET", minutes(30), "0 1-19/6 * * *", "0 4-22/6 * * *")
            );

            List<CalendarEvent> events = getCalendarEventService(now).getCalendarEvents(projects, range(start, end), CalendarViewEventsType.POLLINGS);

            assertThat(events, hasSize(3));
            assertThat(toStringOf(events), containsInAnyOrder("2018-01-01T10:00:00 - 2018-01-01T10:30:00: #1",
                    "2018-01-01T16:00:00 - 2018-01-01T16:30:00: #1", "2018-01-01T22:00:00 - 2018-01-01T22:30:00: #1"));
        }
    }
}
