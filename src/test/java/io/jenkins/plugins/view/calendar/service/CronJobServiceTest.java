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

import hudson.Plugin;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.view.calendar.CalendarView.CalendarViewEventsType;
import io.jenkins.plugins.view.calendar.time.Moment;
import io.jenkins.plugins.view.calendar.util.PluginUtil;
import java.util.Locale;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.parameterizedscheduler.ParameterizedCronTabList;
import org.jenkinsci.plugins.parameterizedscheduler.ParameterizedTimerTrigger;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.str;
import static io.jenkins.plugins.view.calendar.test.TestUtil.mockFreeStyleProject;
import static io.jenkins.plugins.view.calendar.test.TestUtil.mockParameterizedTriggers;
import static io.jenkins.plugins.view.calendar.test.TestUtil.mockTriggers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CronJobServiceTest {

    private static TimeZone defaultTimeZone;
    private static Locale defaultLocale;

    @BeforeAll
    static void beforeClass() {
        CronJobServiceTest.defaultTimeZone = TimeZone.getDefault();
        CronJobServiceTest.defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
        PluginUtil.setJenkins(mock(Jenkins.class));
    }

    @AfterAll
    static void afterClass() {
        TimeZone.setDefault(CronJobServiceTest.defaultTimeZone);
        Locale.setDefault(CronJobServiceTest.defaultLocale);
    }

    abstract static class MockPluginAvailabilityTests {
        @BeforeEach
        public void clearPlugins() {
            PluginUtil.setJenkins(mock(Jenkins.class));
        }
    }

    @Nested
    class GetCronTabsTests extends MockPluginAvailabilityTests {

        @Test
        void testThatHourlyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("10 * * * *");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronWrapper<?> cronTab = cronTabs.get(0);

            assertThat(next(cronTab, "2018-01-01 00:00:00 UTC"), is("2018-01-01 01:10:00 CET"));
            assertThat(next(cronTab, "2018-01-01 00:10:00 UTC"), is("2018-01-01 01:10:00 CET"));
            assertThat(next(cronTab, "2018-01-01 00:20:00 UTC"), is("2018-01-01 02:10:00 CET"));
        }

        @Test
        void testThatDailyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 12 * * *");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronWrapper<?> cronTab = cronTabs.get(0);
            assertThat(next(cronTab, "2018-01-01 00:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 06:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 11:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 12:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 13:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
        }

        @Test
        void testThatYearlyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 10 23 2 *");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronWrapper<?> cronTab = cronTabs.get(0);
            assertThat(next(cronTab, "2018-02-23 00:00:00 UTC"), is("2018-02-23 10:00:00 CET"));
            assertThat(next(cronTab, "2018-02-23 09:00:00 UTC"), is("2018-02-23 10:00:00 CET"));
            assertThat(next(cronTab, "2018-02-23 10:00:00 UTC"), is("2019-02-23 10:00:00 CET"));
            assertThat(next(cronTab, "2018-02-23 11:00:00 UTC"), is("2019-02-23 10:00:00 CET"));
        }

        @Test
        void testThatCommentsAreIgnored() {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("# This is ignored");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(0));
        }

        @Test
        void testThatEmptyLinesAreIgnored() {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("  \n \n  ");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(0));
        }

        @Test
        void testThatInvalidCronExpressionIsIgnored() {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("This is not valid");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(0));
        }

        @Test
        void testThatTimeZoneIsRespected() throws ParseException {
            Trigger triggerCET = mock(Trigger.class);
            when(triggerCET.getSpec()).thenReturn("0 6 * * *");

            Trigger triggerUTC = mock(Trigger.class);
            when(triggerUTC.getSpec()).thenReturn("TZ=UTC\n0 6 * * *");

            List<CronWrapper<?>> cronTabsCET = new CronJobService().getCronTabs(triggerCET);
            assertThat(cronTabsCET, hasSize(1));

            List<CronWrapper<?>> cronTabsUTC = new CronJobService().getCronTabs(triggerUTC);
            assertThat(cronTabsUTC, hasSize(1));

            CronTabList cronTabsListCET = new CronTabList(cronTabsCET.stream().filter(it ->
                    it instanceof CronWrapper.ClassicCronTab).map(it ->
                    ((CronWrapper.ClassicCronTab) it).getCronTab()).toList());
            assertThat(cronTabsListCET.check(cal("2018-01-01 06:00:00 UTC")), is(false));
            assertThat(cronTabsListCET.check(cal("2018-01-01 06:00:00 CET")), is(true));

            CronTabList cronTabsListUTC = new CronTabList(cronTabsUTC.stream().filter(it ->
                    it instanceof CronWrapper.ClassicCronTab).map(it ->
                    ((CronWrapper.ClassicCronTab) it).getCronTab()).toList());
            assertThat(cronTabsListUTC.check(cal("2018-01-01 06:00:00 UTC")), is(true));
            assertThat(cronTabsListUTC.check(cal("2018-01-01 06:00:00 CET")), is(false));
        }

        @Test
        void testThatParameterizedJobsWork() throws ParseException {
            Jenkins jenkins = mock(Jenkins.class);
            when(jenkins.getPlugin("parameterized-scheduler")).thenReturn(mock(Plugin.class));
            PluginUtil.setJenkins(jenkins);

            ParameterizedTimerTrigger trigger = mock(ParameterizedTimerTrigger.class);
            when(trigger.getParameterizedSpecification()).thenReturn("0 12 * * * % PARAM1=value;PARAM2=false");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronWrapper<?> cronTab = cronTabs.get(0);
            assertThat(next(cronTab, "2018-01-01 00:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 06:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 11:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 12:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 13:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
        }

        @Test
        void testThatParameterizedJobsWithoutParametersWork() throws ParseException {
            Jenkins jenkins = mock(Jenkins.class);
            when(jenkins.getPlugin("parameterized-scheduler")).thenReturn(mock(Plugin.class));
            PluginUtil.setJenkins(jenkins);

            ParameterizedTimerTrigger trigger = mock(ParameterizedTimerTrigger.class);
            when(trigger.getParameterizedSpecification()).thenReturn("0 12 * * *");

            List<CronWrapper<?>> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronWrapper<?> cronTab = cronTabs.get(0);
            assertThat(next(cronTab, "2018-01-01 00:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 06:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 11:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 12:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 13:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
        }
    }

    @Nested
    class GetCronTriggersTests extends MockPluginAvailabilityTests {

        @Test
        void testItemIsNotAbstractProject() {
            Job item = mock(Job.class);
            assertThat(new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL), hasSize(0));
        }

        @Test
        void testEmptyTriggerMap() {
            FreeStyleProject project = mockFreeStyleProject();
            assertThat(new CronJobService().getCronTriggers(project, CalendarViewEventsType.ALL), hasSize(0));
        }

        @Test
        void testTriggersWithoutSpecs() {

            Map<TriggerDescriptor, Trigger<?>> triggers = mockTriggers("", "");

            FreeStyleProject project = mockFreeStyleProject();
            when(project.getTriggers()).thenReturn(triggers);

            assertThat(new CronJobService().getCronTriggers(project, CalendarViewEventsType.ALL), hasSize(0));
        }

        @Test
        void testTriggersWithSpecs() {
            Map<TriggerDescriptor, Trigger<?>> mockedTriggers = mockTriggers("0 * * * *", "0 12 * * *");

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getTriggers()).thenReturn(mockedTriggers);

            Iterator<Trigger<?>> iterator = mockedTriggers.values().iterator();

            List<Trigger> triggers = new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL);
            assertThat(triggers, hasSize(2));
            assertThat(triggers, hasItem(iterator.next()));
            assertThat(triggers, hasItem(iterator.next()));
        }

        @Test
        void testWithWorkflowJobPluginInstalled() {
            Jenkins jenkins = mock(Jenkins.class);
            when(jenkins.getPlugin("workflow-job")).thenReturn(mock(Plugin.class));
            PluginUtil.setJenkins(jenkins);

            Map<TriggerDescriptor, Trigger<?>> mockedTriggers = mockTriggers("0 * * * *", "0 12 * * *");

            WorkflowJob item = mock(WorkflowJob.class);
            when(item.getTriggers()).thenReturn(mockedTriggers);

            Iterator<Trigger<?>> iterator = mockedTriggers.values().iterator();

            List<Trigger> triggers = new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL);
            assertThat(triggers, hasSize(2));
            assertThat(triggers, hasItem(iterator.next()));
            assertThat(triggers, hasItem(iterator.next()));
        }

        @Test
        void testWithParameterizedSchedulerPluginInstalled() {
            Jenkins jenkins = mock(Jenkins.class);
            when(jenkins.getPlugin("parameterized-scheduler")).thenReturn(mock(Plugin.class));
            PluginUtil.setJenkins(jenkins);

            Map<TriggerDescriptor, Trigger<?>> mockedTriggers =
                    mockParameterizedTriggers("0 * * * * % PARAM1=value; PARAM2=true", "0 12 * * * % PARAM1=\"another value\";PARAM2=false");

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getTriggers()).thenReturn(mockedTriggers);

            Iterator<Trigger<?>> iterator = mockedTriggers.values().iterator();

            List<Trigger> triggers = new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL);
            assertThat(triggers, hasSize(2));
            assertThat(triggers, hasItem(iterator.next()));
            assertThat(triggers, hasItem(iterator.next()));
        }

        @Test
        void testWithParameterizedSchedulerPluginNotInstalled() {
            Map<TriggerDescriptor, Trigger<?>> mockedTriggers =
                    mockParameterizedTriggers("0 * * * * % PARAM1=value; PARAM2=true", "0 12 * * * % PARAM1=\"another value\";PARAM2=false");

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getTriggers()).thenReturn(mockedTriggers);

            Iterator<Trigger<?>> iterator = mockedTriggers.values().iterator();

            List<Trigger> triggers = new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL);
            assertThat(triggers, hasSize(0));
        }

        @Test
        void testWithParameterizedSchedulerPluginInstalledAndWorkflowJobPluginInstalled() {
            Jenkins jenkins = mock(Jenkins.class);
            when(jenkins.getPlugin("parameterized-scheduler")).thenReturn(mock(Plugin.class));
            when(jenkins.getPlugin("workflow-job")).thenReturn(mock(Plugin.class));
            PluginUtil.setJenkins(jenkins);

            Map<TriggerDescriptor, Trigger<?>> mockedTriggers =
                    mockParameterizedTriggers("0 * * * * % PARAM1=value; PARAM2=true", "0 12 * * * % PARAM1=\"another value\";PARAM2=false");

            WorkflowJob item = mock(WorkflowJob.class);
            when(item.getTriggers()).thenReturn(mockedTriggers);

            Iterator<Trigger<?>> iterator = mockedTriggers.values().iterator();

            List<Trigger> triggers = new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL);
            assertThat(triggers, hasSize(2));
            assertThat(triggers, hasItem(iterator.next()));
            assertThat(triggers, hasItem(iterator.next()));
        }

        @Test
        void testWithParameterizedSchedulerPluginNotInstalledAndWorkflowJobPluginInstalled() {
            Jenkins jenkins = mock(Jenkins.class);
            when(jenkins.getPlugin("workflow-job")).thenReturn(mock(Plugin.class));
            PluginUtil.setJenkins(jenkins);

            Map<TriggerDescriptor, Trigger<?>> mockedTriggers =
                    mockParameterizedTriggers("0 * * * * % PARAM1=value; PARAM2=true", "0 12 * * * % PARAM1=\"another value\";PARAM2=false");

            WorkflowJob item = mock(WorkflowJob.class);
            when(item.getTriggers()).thenReturn(mockedTriggers);

            Iterator<Trigger<?>> iterator = mockedTriggers.values().iterator();

            List<Trigger> triggers = new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL);
            assertThat(triggers, hasSize(0));
        }

        @Test
        void testWithParameterizedSchedulerPluginNotInstalledAndWorkflowJobPluginNotInstalled() {
            Map<TriggerDescriptor, Trigger<?>> mockedTriggers =
                    mockParameterizedTriggers("0 * * * * % PARAM1=value; PARAM2=true", "0 12 * * * % PARAM1=\"another value\";PARAM2=false");

            WorkflowJob item = mock(WorkflowJob.class);
            when(item.getTriggers()).thenReturn(mockedTriggers);

            Iterator<Trigger<?>> iterator = mockedTriggers.values().iterator();

            List<Trigger> triggers = new CronJobService().getCronTriggers(item, CalendarViewEventsType.ALL);
            assertThat(triggers, hasSize(0));
        }
    }

    @Nested
    class GetNextStartTests extends MockPluginAvailabilityTests {
        @Test
        void testNoTriggers() {
            Calendar next = new CronJobService().getNextStart(mockFreeStyleProject(), CalendarViewEventsType.ALL);
            assertThat(next, is(nullValue()));
        }

        @Test
        void testNoCronTabs() {
            Map<TriggerDescriptor, Trigger<?>> triggers = mockTriggers("#Test", "#Test");

            FreeStyleProject project = mockFreeStyleProject();
            when(project.getFullName()).thenReturn("project");
            when(project.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService().getNextStart(project, CalendarViewEventsType.ALL);
            assertThat(next, is(nullValue()));
        }

        @Test
        void testWithCronTabs() throws ParseException {
            Map<TriggerDescriptor, Trigger<?>> triggers = mockTriggers("10 * * * * \n 5 * * * *", "* 10 * * * \n * 5 * * *");

            FreeStyleProject project = mockFreeStyleProject();
            when(project.getFullName()).thenReturn("project");
            when(project.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService(new Moment(cal("2018-01-01 06:00:00 UTC"))).getNextStart(project, CalendarViewEventsType.ALL);
            assertThat(str(next), is("2018-01-01 07:05:00 CET"));
        }

        @Test
        void testHash() throws ParseException {
            Map<TriggerDescriptor, Trigger<?>> triggers = mockTriggers("H * * * *");

            FreeStyleProject project = mockFreeStyleProject();
            when(project.getFullName()).thenReturn("HashThisName");
            when(project.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService(new Moment(cal("2018-01-01 00:00:00 CET"))).getNextStart(project, CalendarViewEventsType.ALL);
            assertThat(str(next), is("2018-01-01 00:48:00 CET"));

            when(project.getFullName()).thenReturn("HashThisDifferentName");

            next = new CronJobService(new Moment(cal("2018-01-01 00:00:00 CET"))).getNextStart(project, CalendarViewEventsType.ALL);
            assertThat(str(next), is("2018-01-01 00:23:00 CET"));
        }

        @Test
        void testSecondsAreZero() throws ParseException {
            Map<TriggerDescriptor, Trigger<?>> triggers = mockTriggers("15 * * * *");

            FreeStyleProject project = mockFreeStyleProject();
            when(project.getFullName()).thenReturn("Project Name");
            when(project.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService(new Moment(cal("2018-01-01 00:00:23 CET"))).getNextStart(project, CalendarViewEventsType.ALL);
            assertThat(next.get(Calendar.SECOND), is(0));
            assertThat(next.get(Calendar.MILLISECOND), is(0));
        }
    }

    private static String next(CronWrapper<?> cronTab, String from) throws ParseException {
        return str(cronTab.ceil(cal(from).getTimeInMillis()));
    }
}
