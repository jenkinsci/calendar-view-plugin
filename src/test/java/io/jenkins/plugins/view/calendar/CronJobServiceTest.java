package io.jenkins.plugins.view.calendar;

import hudson.model.AbstractProject;
import hudson.model.TopLevelItem;
import hudson.scheduler.CronTab;
import hudson.triggers.Trigger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class CronJobServiceTest {

    @Test
    public void testThatPeriodicJobsWork() throws ParseException {
        Trigger trigger = mock(Trigger.class);
        when(trigger.getSpec()).thenReturn("10 * * * *");

        List<CronTab> cronTabs = new CronJobService().getCronTabs(trigger);
        assertThat(cronTabs.size(), is(1)) ;

        CronTab cronTab = cronTabs.get(0);
        assertThat(str(cronTab.ceil(cal("2018-01-01 00:00:00"))), is("2018-01-01 00:10:00"));
        assertThat(str(cronTab.ceil(cal("2018-01-01 00:10:00"))), is("2018-01-01 00:10:00"));
        assertThat(str(cronTab.ceil(cal("2018-01-01 00:20:00"))), is("2018-01-01 01:10:00"));
    }

    @Test
    public void testThatCommentsAreIgnored() throws ParseException {
        Trigger trigger = mock(Trigger.class);
        when(trigger.getSpec()).thenReturn("# This is ignored");

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

    private Calendar cal(String date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
        return cal;
    }

    private String str(Calendar cal) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
    }
}
