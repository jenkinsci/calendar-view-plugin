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
import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.RunList;
import io.jenkins.plugins.view.calendar.CalendarEvent;
import io.jenkins.plugins.view.calendar.CalendarEventComparator;
import io.jenkins.plugins.view.calendar.CalendarEventType;
import io.jenkins.plugins.view.calendar.time.Now;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.str;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(Enclosed.class)
public class CalendarEventServiceTest {

    private static TimeZone defaultTimeZone;
    private CalendarEventService calendarEventService;

    @BeforeClass
    public static void beforeClass() {
        CalendarEventServiceTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    @AfterClass
    public static void afterClass() {
        TimeZone.setDefault(CalendarEventServiceTest.defaultTimeZone);
    }
    
    private static CalendarEventService getCalendarEventService() {
        final Now now = new Now();
        return new CalendarEventService(now, new CronJobService(now));
    }

    private static CalendarEventService getCalendarEventService(Calendar calendar) {
        final Now now = new Now(calendar);
        return new CalendarEventService(now, new CronJobService(now));
    }

    public static class GetCalendarEventsTests {
        @Test
        public void testRangeInFuture() {
            Calendar start = Calendar.getInstance();
            start.add(Calendar.DATE, 5);

            Calendar end = Calendar.getInstance();
            end.add(Calendar.DATE, 10);

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 12 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService().getCalendarEvents(items, start, end);
            assertThat(calendarEvents, hasSize(greaterThan(0)));
            for (CalendarEvent calendarEvent : calendarEvents) {
                assertThat(calendarEvent.getType(), Is.is(CalendarEventType.FUTURE));
            }
        }

        @Test
        public void testRangeInPast() {
            Calendar start = Calendar.getInstance();
            start.add(Calendar.DATE, -10);

            Calendar end = Calendar.getInstance();
            end.add(Calendar.DATE, -5);

            Calendar runDate = Calendar.getInstance();
            runDate.add(Calendar.DATE, -7);
            Run run = mock(Run.class);
            when(run.getStartTimeInMillis()).thenReturn(runDate.getTimeInMillis());
            when(run.getDuration()).thenReturn(10 * 60 * 1000L);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run).iterator());

            Job item = mock(Job.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getBuilds()).thenReturn(runs);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService().getCalendarEvents(items, start, end);
            assertThat(calendarEvents, hasSize(1));
            assertThat(calendarEvents.get(0).getBuild(), is(run));
        }

        @Test
        public void testRangeInPastAndFuture() {
            Calendar start = Calendar.getInstance();
            start.add(Calendar.DATE, -10);

            Calendar end = Calendar.getInstance();
            end.add(Calendar.DATE, 10);

            Calendar runDate = Calendar.getInstance();
            runDate.add(Calendar.DATE, -7);

            Run run = mock(Run.class);
            when(run.getStartTimeInMillis()).thenReturn(runDate.getTimeInMillis());
            when(run.getDuration()).thenReturn(10 * 60 * 1000L);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run).iterator());

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 12 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);
            when(item.getBuilds()).thenReturn(runs);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService().getCalendarEvents(items, start, end);
            Collections.sort(calendarEvents, new CalendarEventComparator());
            assertThat(calendarEvents, hasSize(greaterThan(1)));
            assertThat(calendarEvents.get(0).getBuild(), is(run));
            for (int i = 1; i < calendarEvents.size(); i++) {
                assertThat(calendarEvents.get(i).getType(), is(CalendarEventType.FUTURE));
            }
        }

        @Test
        public void testOneMinuteBeforeScheduledStart() throws ParseException {
            Calendar now = cal("2018-01-04 23:43:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(new ArrayList().iterator());

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("44 23 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);
            when(item.getBuilds()).thenReturn(runs);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(items, start, end);
            assertThat(calendarEvents, hasSize(3));
            assertThat(str(calendarEvents.get(0).getStart()), is("2018-01-04 23:44:00 CET"));
            assertThat(calendarEvents.get(0).isFuture(), is(true));
            assertThat(str(calendarEvents.get(1).getStart()), is("2018-01-05 23:44:00 CET"));
            assertThat(calendarEvents.get(1).isFuture(), is(true));
            assertThat(str(calendarEvents.get(2).getStart()), is("2018-01-06 23:44:00 CET"));
            assertThat(calendarEvents.get(2).isFuture(), is(true));
        }

        @Test
        public void testThirtySecondsBeforeScheduledStart() throws ParseException {
            Calendar now = cal("2018-01-04 23:43:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(new ArrayList().iterator());

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("44 23 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);
            when(item.getBuilds()).thenReturn(runs);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(items, start, end);
            assertThat(calendarEvents, hasSize(3));
            assertThat(str(calendarEvents.get(0).getStart()), is("2018-01-04 23:44:00 CET"));
            assertThat(calendarEvents.get(0).isFuture(), is(true));
            assertThat(str(calendarEvents.get(1).getStart()), is("2018-01-05 23:44:00 CET"));
            assertThat(calendarEvents.get(1).isFuture(), is(true));
            assertThat(str(calendarEvents.get(2).getStart()), is("2018-01-06 23:44:00 CET"));
            assertThat(calendarEvents.get(2).isFuture(), is(true));
        }

        @Test
        public void testExactSecondOfScheduledStart() throws ParseException {
            Calendar runDate = cal("2018-01-04 23:44:00 CET");
            Calendar now = cal("2018-01-04 23:44:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            Run run = mock(Run.class);
            when(run.getStartTimeInMillis()).thenReturn(runDate.getTimeInMillis());
            when(run.getDuration()).thenReturn(10 * 60 * 1000L);
            when(run.isBuilding()).thenReturn(true);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run).iterator());

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("44 23 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);
            when(item.getBuilds()).thenReturn(runs);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(items, start, end);

            assertThat(calendarEvents, hasSize(3));
            assertThat(str(calendarEvents.get(0).getStart()), is("2018-01-04 23:44:00 CET"));
            assertThat(calendarEvents.get(0).isFuture(), is(false));
            assertThat(calendarEvents.get(0).getBuild().isBuilding(), is(true));
            assertThat(str(calendarEvents.get(1).getStart()), is("2018-01-05 23:44:00 CET"));
            assertThat(calendarEvents.get(1).isFuture(), is(true));
            assertThat(str(calendarEvents.get(2).getStart()), is("2018-01-06 23:44:00 CET"));
            assertThat(calendarEvents.get(2).isFuture(), is(true));
        }


        @Test
        public void testThirtySecondsAfterScheduledStart() throws ParseException {
            Calendar runDate = cal("2018-01-04 23:44:00 CET");
            Calendar now = cal("2018-01-04 23:44:30 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            Run run = mock(Run.class);
            when(run.getStartTimeInMillis()).thenReturn(runDate.getTimeInMillis());
            when(run.getDuration()).thenReturn(10 * 60 * 1000L);
            when(run.isBuilding()).thenReturn(true);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run).iterator());

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("44 23 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);
            when(item.getBuilds()).thenReturn(runs);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(items, start, end);

            assertThat(calendarEvents, hasSize(3));
            assertThat(str(calendarEvents.get(0).getStart()), is("2018-01-04 23:44:00 CET"));
            assertThat(calendarEvents.get(0).isFuture(), is(false));
            assertThat(calendarEvents.get(0).getBuild().isBuilding(), is(true));
            assertThat(str(calendarEvents.get(1).getStart()), is("2018-01-05 23:44:00 CET"));
            assertThat(calendarEvents.get(1).isFuture(), is(true));
            assertThat(str(calendarEvents.get(2).getStart()), is("2018-01-06 23:44:00 CET"));
            assertThat(calendarEvents.get(2).isFuture(), is(true));
        }

        @Test
        public void testOneMinuteAfterScheduledStart() throws ParseException {
            Calendar runDate = cal("2018-01-04 23:44:00 CET");
            Calendar now = cal("2018-01-04 23:45:00 CET");
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-07 00:00:00 CET");

            Run run = mock(Run.class);
            when(run.getStartTimeInMillis()).thenReturn(runDate.getTimeInMillis());
            when(run.getDuration()).thenReturn(10 * 60 * 1000L);
            when(run.isBuilding()).thenReturn(true);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run).iterator());

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("44 23 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);
            when(item.getBuilds()).thenReturn(runs);

            List<TopLevelItem> items = new ArrayList<>();
            items.add((TopLevelItem) item);

            List<CalendarEvent> calendarEvents = getCalendarEventService(now).getCalendarEvents(items, start, end);

            assertThat(calendarEvents, hasSize(3));
            assertThat(str(calendarEvents.get(0).getStart()), is("2018-01-04 23:44:00 CET"));
            assertThat(calendarEvents.get(0).isFuture(), is(false));
            assertThat(calendarEvents.get(0).getBuild().isBuilding(), is(true));
            assertThat(str(calendarEvents.get(1).getStart()), is("2018-01-05 23:44:00 CET"));
            assertThat(calendarEvents.get(1).isFuture(), is(true));
            assertThat(str(calendarEvents.get(2).getStart()), is("2018-01-06 23:44:00 CET"));
            assertThat(calendarEvents.get(2).isFuture(), is(true));
        }
    }

    public static class GetFutureEventsTests {
        @Test
        public void testNoItems() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            List<CalendarEvent> events = getCalendarEventService().getFutureEvents(new ArrayList<TopLevelItem>(), start, end);
            assertThat(events, hasSize(0));
        }

        @Test
        public void testItemNotAnAbstractProject() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            TopLevelItem item = mock(TopLevelItem.class);

            List<CalendarEvent> events = getCalendarEventService().getFutureEvents(Arrays.asList(item), start, end);
            assertThat(events, hasSize(0));
        }

        @Test
        public void testNoTriggers() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));

            List<CalendarEvent> events = getCalendarEventService().getFutureEvents(Arrays.asList((TopLevelItem)project), start, end);
            assertThat(events, hasSize(0));
        }

        @Test
        public void testNoCronTabs() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("# Test");

            Map<TriggerDescriptor, Trigger> triggers = new HashMap<TriggerDescriptor, Trigger>();
            triggers.put(mock(TriggerDescriptor.class), trigger) ;

            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(project.getFullName()).thenReturn("Project Name");
            when(project.getTriggers()).thenReturn(triggers);

            List<CalendarEvent> events = getCalendarEventService().getFutureEvents(Arrays.asList((TopLevelItem)project), start, end);
            assertThat(events, hasSize(0));
        }

        @Test
        public void testHasBuilds() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 21 * * *");

            Map<TriggerDescriptor, Trigger> triggers = new HashMap<TriggerDescriptor, Trigger>();
            triggers.put(mock(TriggerDescriptor.class), trigger) ;

            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(project.getFullName()).thenReturn("Project Name");
            when(project.getTriggers()).thenReturn(triggers);
            when(project.getEstimatedDuration()).thenReturn(6 * 60 * 60 * 1000L);

            List<CalendarEvent> events = getCalendarEventService().getFutureEvents(Arrays.asList((TopLevelItem)project), start, end);
            assertThat(events, hasSize(5));
            assertThat(str(events.get(0).getStart()), is("2018-01-01 21:00:00 CET"));
            assertThat(str(events.get(1).getStart()), is("2018-01-02 21:00:00 CET"));
            assertThat(str(events.get(2).getStart()), is("2018-01-03 21:00:00 CET"));
            assertThat(str(events.get(3).getStart()), is("2018-01-04 21:00:00 CET"));
            assertThat(str(events.get(4).getStart()), is("2017-12-31 21:00:00 CET"));
        }

        @Test
        public void testHash() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 CET");
            Calendar end = cal("2018-01-05 00:00:00 CET");

            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("H 21 * * *");

            Map<TriggerDescriptor, Trigger> triggers = new HashMap<TriggerDescriptor, Trigger>();
            triggers.put(mock(TriggerDescriptor.class), trigger) ;

            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(project.getFullName()).thenReturn("HashThisName");
            when(project.getTriggers()).thenReturn(triggers);
            when(project.getEstimatedDuration()).thenReturn(6 * 60 * 60 * 1000L);

            List<CalendarEvent> events = getCalendarEventService().getFutureEvents(Arrays.asList((TopLevelItem)project), start, end);
            assertThat(events, hasSize(5));
            assertThat(str(events.get(0).getStart()), is("2018-01-01 21:48:00 CET"));
            assertThat(str(events.get(1).getStart()), is("2018-01-02 21:48:00 CET"));
            assertThat(str(events.get(2).getStart()), is("2018-01-03 21:48:00 CET"));
            assertThat(str(events.get(3).getStart()), is("2018-01-04 21:48:00 CET"));
            assertThat(str(events.get(4).getStart()), is("2017-12-31 21:48:00 CET"));
        }
    }

    public static class GetPastEventsTests {
        @Test
        public void testNoItems() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            List<CalendarEvent> events = getCalendarEventService().getPastEvents(new ArrayList<TopLevelItem>(), start, end);
            assertThat(events, hasSize(0));
        }

        @Test
        public void testItemNotAJob() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            TopLevelItem item = mock(TopLevelItem.class);

            List<CalendarEvent> events = getCalendarEventService().getPastEvents(Arrays.asList(item), start, end);
            assertThat(events, hasSize(0));
        }

        @Test
        public void testItemHasBuilds() throws ParseException {
            Calendar start = cal("2018-01-01 00:00:00 UTC");
            Calendar end = cal("2018-01-05 00:00:00 UTC");

            Run run1 = mock(Run.class);
            when(run1.getStartTimeInMillis()).thenReturn(cal("2017-12-01 06:00:00 UTC").getTimeInMillis());
            when(run1.getDuration()).thenReturn(10 * 60 * 1000L);

            Run run2 = mock(Run.class);
            when(run2.getStartTimeInMillis()).thenReturn(cal("2018-01-01 06:00:00 UTC").getTimeInMillis());
            when(run2.getDuration()).thenReturn(10 * 60 * 1000L);

            Run run3 = mock(Run.class);
            when(run3.getStartTimeInMillis()).thenReturn(cal("2018-01-05 06:00:00 UTC").getTimeInMillis());
            when(run3.getDuration()).thenReturn(10 * 60 * 1000L);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run1, run2, run3).iterator());

            Job item = mock(Job.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getBuilds()).thenReturn(runs);

            List<CalendarEvent> events = getCalendarEventService().getPastEvents(Arrays.asList((TopLevelItem)item), start, end);
            assertThat(events, hasSize(1));
            assertThat(events.get(0).getBuild(), is(run2));
        }

        @Test
        public void testBuildOnStartDate() throws ParseException {
            Calendar start = cal("2018-01-01 12:00:00 UTC");
            Calendar end = cal("2018-01-05 12:00:00 UTC");

            Run run1 = mock(Run.class);
            when(run1.getStartTimeInMillis()).thenReturn(cal("2018-01-01 10:00:00 UTC").getTimeInMillis());
            when(run1.getDuration()).thenReturn(5 * 60 * 60 * 1000L);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run1).iterator());

            Job item = mock(Job.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getBuilds()).thenReturn(runs);

            List<CalendarEvent> events = getCalendarEventService().getPastEvents(Arrays.asList((TopLevelItem) item), start, end);
            assertThat(events, hasSize(1));
            assertThat(events.get(0).getBuild(), is(run1));
        }

        @Test
        public void testBuildOnEndDate() throws ParseException {
            Calendar start = cal("2018-01-01 12:00:00 UTC");
            Calendar end = cal("2018-01-05 12:00:00 UTC");

            Run run1 = mock(Run.class);
            when(run1.getStartTimeInMillis()).thenReturn(cal("2018-01-05 10:00:00 UTC").getTimeInMillis());
            when(run1.getDuration()).thenReturn(5 * 60 * 60 * 1000L);

            RunList runs = mock(RunList.class);
            when(runs.iterator()).thenReturn(Arrays.asList(run1).iterator());

            Job item = mock(Job.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getBuilds()).thenReturn(runs);

            List<CalendarEvent> events = getCalendarEventService().getPastEvents(Arrays.asList((TopLevelItem) item), start, end);
            assertThat(events, hasSize(1));
            assertThat(events.get(0).getBuild(), is(run1));
        }
    }

    public static class GetLastEventsTests {
        @Test
        public void testIsNotAJob() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn(mock(TopLevelItem.class));

            List<CalendarEvent> lastEvents = getCalendarEventService().getLastEvents(event, 2);
            assertThat(lastEvents, hasSize(0));
        }

        @Test
        public void testHasNoPastBuilds() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)mock(Job.class, withSettings().extraInterfaces(TopLevelItem.class)));

            List<CalendarEvent> lastEvents = getCalendarEventService().getLastEvents(event, 2);
            assertThat(lastEvents, hasSize(0));
        }

        @Test
        public void testFreeStyleProjectHasPastBuilds() throws IOException {
            Run build1 = mock(FreeStyleBuild.class);
            when(build1.getResult()).thenReturn(Result.FAILURE);

            Run build2 = mock(FreeStyleBuild.class);
            when(build2.getResult()).thenReturn(Result.SUCCESS);
            when(build2.getPreviousBuild()).thenReturn(build1);

            Run build3 = mock(FreeStyleBuild.class);
            when(build3.getResult()).thenReturn(Result.ABORTED);
            when(build3.getPreviousBuild()).thenReturn(build2);

            final Run build4 = mock(FreeStyleBuild.class);
            when(build4.getResult()).thenReturn(Result.UNSTABLE);
            when(build4.getPreviousBuild()).thenReturn(build3);

            FreeStyleProject project = new FreeStyleProject(mock(ItemGroup.class), "project") {
                @Override
                public FreeStyleBuild getLastBuild() {
                    return (FreeStyleBuild) build4;
                }
            };

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            List<CalendarEvent> lastEvents = getCalendarEventService().getLastEvents(event, 5);
            assertThat(lastEvents, hasSize(4));
            assertThat(lastEvents.get(0).getBuild(), is(build4));
            assertThat(lastEvents.get(1).getBuild(), is(build3));
            assertThat(lastEvents.get(2).getBuild(), is(build2));
            assertThat(lastEvents.get(3).getBuild(), is(build1));
        }

        @Issue("JENKINS-52797")
        @Test
        public void testMatrixProjectHasPastBuilds() throws IOException {
            Run build1 = mock(MatrixBuild.class);
            when(build1.getResult()).thenReturn(Result.FAILURE);

            Run build2 = mock(MatrixBuild.class);
            when(build2.getResult()).thenReturn(Result.SUCCESS);
            when(build2.getPreviousBuild()).thenReturn(build1);

            Run build3 = mock(MatrixBuild.class);
            when(build3.getResult()).thenReturn(Result.ABORTED);
            when(build3.getPreviousBuild()).thenReturn(build2);

            final Run build4 = mock(MatrixBuild.class);
            when(build4.getResult()).thenReturn(Result.UNSTABLE);
            when(build4.getPreviousBuild()).thenReturn(build3);

            MatrixProject project = new MatrixProject(mock(ItemGroup.class), "project") {
                @Override
                public MatrixBuild getLastBuild() {
                    return (MatrixBuild) build4;
                }
            };

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            List<CalendarEvent> lastEvents = getCalendarEventService().getLastEvents(event, 5);
            assertThat(lastEvents, hasSize(4));
            assertThat(lastEvents.get(0).getBuild(), is(build4));
            assertThat(lastEvents.get(1).getBuild(), is(build3));
            assertThat(lastEvents.get(2).getBuild(), is(build2));
            assertThat(lastEvents.get(3).getBuild(), is(build1));
        }
    }

    public static class GetPreviousEventTests {
        @Test
        public void testHasNoBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            assertThat(getCalendarEventService().getPreviousEvent(event), is(nullValue()));
        }

        @Test
        public void testHasNoPreviousBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(mock(Run.class));

            assertThat(getCalendarEventService().getPreviousEvent(event), is(nullValue()));
        }

        @Test
        public void testHasPreviousBuild() {
            Run previousBuild = mock(Run.class);

            Run build = mock(Run.class);
            when(build.getPreviousBuild()).thenReturn(previousBuild);

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(build);

            CalendarEvent previousEvent = getCalendarEventService().getPreviousEvent(event);
            assertThat(previousEvent, is(notNullValue()));
            assertThat(previousEvent.getBuild(), is(previousBuild));
        }
    }

    public static class GetNextEventTests {
        @Test
        public void testHasNoBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            assertThat(getCalendarEventService().getNextEvent(event), is(nullValue()));
        }

        @Test
        public void testHasNoPreviousBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(mock(Run.class));

            assertThat(getCalendarEventService().getNextEvent(event), is(nullValue()));
        }

        @Test
        public void testHasPreviousBuild() {
            Run nextBuild = mock(Run.class);

            Run build = mock(Run.class);
            when(build.getNextBuild()).thenReturn(nextBuild);

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(build);

            CalendarEvent nextEvent = getCalendarEventService().getNextEvent(event);
            assertThat(nextEvent, is(notNullValue()));
            assertThat(nextEvent.getBuild(), is(nextBuild));
        }
    }

    public static class GetNextScheduledEventTests {
        @Test
        public void testIsNotAbstractProject() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn(mock(TopLevelItem.class));

            CalendarEvent nextScheduledEvent = getCalendarEventService().getNextScheduledEvent(event);
            assertThat(nextScheduledEvent, is(nullValue()));
        }

        @Test
        public void testHasNoNextScheduledEvent() {
            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            CalendarEvent nextScheduledEvent = getCalendarEventService().getNextScheduledEvent(event);
            assertThat(nextScheduledEvent, is(nullValue()));
        }

        @Test
        public void testHasNextScheduledEvent() {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 * * * *");

            Map<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(project.getFullName()).thenReturn("Project Name");
            when(project.getTriggers()).thenReturn(triggers);

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            CalendarEvent nextScheduledEvent = getCalendarEventService().getNextScheduledEvent(event);
            assertThat(nextScheduledEvent, is(notNullValue()));
        }
    }
}
