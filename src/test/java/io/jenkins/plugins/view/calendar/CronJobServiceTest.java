package io.jenkins.plugins.view.calendar;

import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
            assertThat(cronTabs.size(), is(1));

            CronTab cronTab = cronTabs.get(0);

            assertThat(str(cronTab.ceil(cal("2018-01-01 00:00:00 UTC"))), is("2018-01-01 01:10:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-01-01 00:10:00 UTC"))), is("2018-01-01 01:10:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-01-01 00:20:00 UTC"))), is("2018-01-01 02:10:00 CET"));
        }

        @Test
        public void testThatDailyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 12 * * *");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs.size(), is(1));

            CronTab cronTab = cronTabs.get(0);
            assertThat(str(cronTab.ceil(cal("2018-01-01 00:00:00 UTC"))), is("2018-01-01 12:00:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-01-01 06:00:00 UTC"))), is("2018-01-01 12:00:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-01-01 11:00:00 UTC"))), is("2018-01-01 12:00:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-01-01 12:00:00 UTC"))), is("2018-01-02 12:00:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-01-01 13:00:00 UTC"))), is("2018-01-02 12:00:00 CET"));
        }

        @Test
        public void testThatYearlyJobsWork() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 10 23 2 *");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs.size(), is(1));

            CronTab cronTab = cronTabs.get(0);
            assertThat(str(cronTab.ceil(cal("2018-02-23 00:00:00 UTC"))), is("2018-02-23 10:00:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-02-23 09:00:00 UTC"))), is("2018-02-23 10:00:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-02-23 10:00:00 UTC"))), is("2019-02-23 10:00:00 CET"));
            assertThat(str(cronTab.ceil(cal("2018-02-23 11:00:00 UTC"))), is("2019-02-23 10:00:00 CET"));
        }

        @Test
        public void testThatCommentsAreIgnored() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("# This is ignored");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs.size(), is(0));
        }

        @Test
        public void testThatEmptyLinesAreIgnored() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("  \n \n  ");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs.size(), is(0));
        }

        @Test
        public void testThatInvalidCronExpressionIsIgnored() throws ParseException {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("This is not valid");

            List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
            assertThat(cronTabs.size(), is(0));
        }

        @Test
        public void testThatTimeZoneIsRespected() throws ParseException {
            Trigger triggerCET = mock(Trigger.class);
            when(triggerCET.getSpec()).thenReturn("0 6 * * *");

            Trigger triggerUTC = mock(Trigger.class);
            when(triggerUTC.getSpec()).thenReturn("TZ=UTC\n0 6 * * *");

            List<CronTab> cronTabsCET = new CronJobService().getCronTabs(triggerCET);
            assertThat(cronTabsCET.size(), is(1));

            List<CronTab> cronTabsUTC = new CronJobService().getCronTabs(triggerUTC);
            assertThat(cronTabsUTC.size(), is(1));

            CronTabList cronTabsListCET = new CronTabList(cronTabsCET);
            assertThat(cronTabsListCET.check(cal("2018-01-01 06:00:00 UTC")), is(false));
            assertThat(cronTabsListCET.check(cal("2018-01-01 06:00:00 CET")), is(true));

            CronTabList cronTabsListUTC = new CronTabList(cronTabsUTC);
            assertThat(cronTabsListUTC.check(cal("2018-01-01 06:00:00 UTC")), is(true));
            assertThat(cronTabsListUTC.check(cal("2018-01-01 06:00:00 CET")), is(false));
        }
    }

    private static Calendar cal(String date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(date));
        return cal;
    }

    private static String str(Calendar cal) throws ParseException {
        if (cal == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(cal.getTime());
    }
}
