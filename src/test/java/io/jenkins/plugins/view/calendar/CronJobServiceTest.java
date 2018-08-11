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
package io.jenkins.plugins.view.calendar;

import hudson.model.AbstractProject;
import hudson.model.TopLevelItem;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.view.calendar.time.Now;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.*;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static io.jenkins.plugins.view.calendar.test.CalendarUtil.str;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(Enclosed.class)
public class CronJobServiceTest {

    private static TimeZone defaultTimeZone;

    @BeforeClass
    public static void beforeClass() {
        CronJobServiceTest.defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    @AfterClass
    public static void afterClass() {
        TimeZone.setDefault(CronJobServiceTest.defaultTimeZone);
    }

    public static class GetCronTabsTests {

        @Test
        public void testThatHourlyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("10 * * * *");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronTab cronTab = cronTabs.get(0);

            assertThat(next(cronTab,"2018-01-01 00:00:00 UTC"), is("2018-01-01 01:10:00 CET"));
            assertThat(next(cronTab,"2018-01-01 00:10:00 UTC"), is("2018-01-01 01:10:00 CET"));
            assertThat(next(cronTab,"2018-01-01 00:20:00 UTC"), is("2018-01-01 02:10:00 CET"));
        }
   

        @Test
        public void testThatDailyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 12 * * *");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronTab cronTab = cronTabs.get(0);
            assertThat(next(cronTab, "2018-01-01 00:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 06:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 11:00:00 UTC"), is("2018-01-01 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 12:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
            assertThat(next(cronTab, "2018-01-01 13:00:00 UTC"), is("2018-01-02 12:00:00 CET"));
        }

        @Test
        public void testThatYearlyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 10 23 2 *");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(1));

            CronTab cronTab = cronTabs.get(0);
            assertThat(next(cronTab, "2018-02-23 00:00:00 UTC"), is("2018-02-23 10:00:00 CET"));
            assertThat(next(cronTab, "2018-02-23 09:00:00 UTC"), is("2018-02-23 10:00:00 CET"));
            assertThat(next(cronTab, "2018-02-23 10:00:00 UTC"), is("2019-02-23 10:00:00 CET"));
            assertThat(next(cronTab, "2018-02-23 11:00:00 UTC"), is("2019-02-23 10:00:00 CET"));
        }

        @Test
        public void testThatCommentsAreIgnored() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("# This is ignored");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(0));
        }

        @Test
        public void testThatEmptyLinesAreIgnored() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("  \n \n  ");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(0));
        }

        @Test
        public void testThatInvalidCronExpressionIsIgnored() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("This is not valid");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs, hasSize(0));
        }

        @Test
        public void testThatTimeZoneIsRespected() throws ParseException {
            Trigger triggerCET = mock(Trigger.class);
            when(triggerCET.getSpec()).thenReturn("0 6 * * *");

            Trigger triggerUTC = mock(Trigger.class);
            when(triggerUTC.getSpec()).thenReturn("TZ=UTC\n0 6 * * *");

            List<CronTab> cronTabsCET = new CronJobService().getCronTabs(triggerCET);
            assertThat(cronTabsCET, hasSize(1));

            List<CronTab> cronTabsUTC = new CronJobService().getCronTabs(triggerUTC);
            assertThat(cronTabsUTC, hasSize(1));

            CronTabList cronTabsListCET = new CronTabList(cronTabsCET);
            assertThat(cronTabsListCET.check(cal("2018-01-01 06:00:00 UTC")), is(false));
            assertThat(cronTabsListCET.check(cal("2018-01-01 06:00:00 CET")), is(true));

            CronTabList cronTabsListUTC = new CronTabList(cronTabsUTC);
            assertThat(cronTabsListUTC.check(cal("2018-01-01 06:00:00 UTC")), is(true));
            assertThat(cronTabsListUTC.check(cal("2018-01-01 06:00:00 CET")), is(false));
        }
    }

    public static class GetCronTriggersTests {
        @Test
        public void testItemIsNotAbstractProject() {
            TopLevelItem item = mock(TopLevelItem.class);
            assertThat(new CronJobService().getCronTriggers(item), hasSize(0));
        }

        @Test
        public void testEmptyTriggerMap() {
            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            assertThat(new CronJobService().getCronTriggers((TopLevelItem) item), hasSize(0));
        }

        @Test
        public void testTriggersWithoutSpecs() {
            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), mock(Trigger.class));
            triggers.put(mock(TriggerDescriptor.class), mock(Trigger.class));

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getTriggers()).thenReturn(triggers);
            assertThat(new CronJobService().getCronTriggers((TopLevelItem) item), hasSize(0));
        }

        @Test
        public void testTriggersWithSpecs() {
            Trigger trigger1 = mock(Trigger.class);
            Trigger trigger2 = mock(Trigger.class);
            when(trigger1.getSpec()).thenReturn("0 * * * *");
            when(trigger2.getSpec()).thenReturn("0 12 * * *");

            HashMap<TriggerDescriptor, Trigger> expectedTriggers = new HashMap<>();
            expectedTriggers.put(mock(TriggerDescriptor.class), trigger1);
            expectedTriggers.put(mock(TriggerDescriptor.class), trigger2);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getTriggers()).thenReturn(expectedTriggers);

            List<Trigger> triggers = new CronJobService().getCronTriggers((TopLevelItem) item);
            assertThat(triggers, hasSize(2));
            assertThat(triggers, hasItem(trigger1));
            assertThat(triggers, hasItem(trigger2));
        }
    }

    public static class GetNextStartTests {
        @Test
        public void testNoTriggers() {
            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));

            Calendar next = new CronJobService().getNextStart((TopLevelItem) item);
            assertThat(next, is(nullValue()));
        }

        @Test
        public void testNoCronTabs() {
            Trigger trigger1 = mock(Trigger.class);
            Trigger trigger2 = mock(Trigger.class);
            when(trigger1.getSpec()).thenReturn("# Test");
            when(trigger2.getSpec()).thenReturn("# Test");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger1);
            triggers.put(mock(TriggerDescriptor.class), trigger2);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService().getNextStart((TopLevelItem) item);
            assertThat(next, is(nullValue()));
        }

        @Test
        public void testWithCronTabs() throws ParseException {

            Trigger trigger1 = mock(Trigger.class);
            Trigger trigger2 = mock(Trigger.class);
            when(trigger1.getSpec()).thenReturn("10 * * * * \n 5 * * * *");
            when(trigger2.getSpec()).thenReturn("* 10 * * * \n * 5 * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger1);
            triggers.put(mock(TriggerDescriptor.class), trigger2);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService(new Now(cal("2018-01-01 06:00:00 UTC"))).getNextStart((TopLevelItem) item);
            assertThat(str(next), is("2018-01-01 07:05:00 CET"));
        }

        @Test
        public void testHash() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("H * * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("HashThisName");
            when(item.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService(new Now(cal("2018-01-01 00:00:00 CET"))).getNextStart((TopLevelItem) item);
            assertThat(str(next), is("2018-01-01 00:48:00 CET"));

            when(item.getFullName()).thenReturn("HashThisDifferentName");

            next = new CronJobService(new Now(cal("2018-01-01 00:00:00 CET"))).getNextStart((TopLevelItem) item);
            assertThat(str(next), is("2018-01-01 00:23:00 CET"));
        }

        @Test
        public void testSecondsAreZero() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("15 * * * *");

            HashMap<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject item = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(item.getFullName()).thenReturn("Project Name");
            when(item.getTriggers()).thenReturn(triggers);

            Calendar next = new CronJobService(new Now(cal("2018-01-01 00:00:23 CET"))).getNextStart((TopLevelItem) item);
            assertThat(next.get(Calendar.SECOND), is(0));
            assertThat(next.get(Calendar.MILLISECOND), is(0));
        }
    }


    private static String next(CronTab cronTab, String from) throws ParseException {
        return str(cronTab.ceil(cal(from)));
    }
}
