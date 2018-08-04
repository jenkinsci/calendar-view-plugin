package io.jenkins.plugins.view.calendar;

import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(Enclosed.class)
public class CalendarEventServiceTest {

    public static class GetPreviousEventTests {
        @Test
        public void testHasNoBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            assertThat(new CalendarEventService().getPreviousEvent(event), is(nullValue()));
        }

        @Test
        public void testHasNoPreviousBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(mock(Run.class));

            assertThat(new CalendarEventService().getPreviousEvent(event), is(nullValue()));
        }

        @Test
        public void testHasPreviousBuild() {
            Run previousBuild = mock(Run.class);
            when(previousBuild.getUrl()).thenReturn("build/url");

            Run build = mock(Run.class);
            when(build.getPreviousBuild()).thenReturn(previousBuild);

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(build);

            CalendarEvent previousEvent = new CalendarEventService().getPreviousEvent(event);
            assertThat(previousEvent, is(notNullValue()));
            assertThat(previousEvent.getBuild(), is(previousBuild));
        }
    }

    public static class GetNextEventTests {
        @Test
        public void testHasNoBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            assertThat(new CalendarEventService().getNextEvent(event), is(nullValue()));
        }

        @Test
        public void testHasNoPreviousBuild() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(mock(Run.class));

            assertThat(new CalendarEventService().getNextEvent(event), is(nullValue()));
        }

        @Test
        public void testHasPreviousBuild() {
            Run nextBuild = mock(Run.class);
            when(nextBuild.getUrl()).thenReturn("build/url");

            Run build = mock(Run.class);
            when(build.getNextBuild()).thenReturn(nextBuild);

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getBuild()).thenReturn(build);

            CalendarEvent nextEvent = new CalendarEventService().getNextEvent(event);
            assertThat(nextEvent, is(notNullValue()));
            assertThat(nextEvent.getBuild(), is(nextBuild));
        }
    }

    public static class GetNextScheduledEventTests {
        @Test
        public void testIsNotAbstractProject() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn(mock(TopLevelItem.class));

            CalendarEvent nextScheduledEvent = new CalendarEventService().getNextScheduledEvent(event);
            assertThat(nextScheduledEvent, is(nullValue()));
        }

        @Test
        public void testHasNoNextScheduledEvent() {
            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            CalendarEvent nextScheduledEvent = new CalendarEventService().getNextScheduledEvent(event);
            assertThat(nextScheduledEvent, is(nullValue()));
        }

        @Test
        public void testHasNextScheduledEvent() {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn("0 * * * *");

            Map<TriggerDescriptor, Trigger> triggers = new HashMap<>();
            triggers.put(mock(TriggerDescriptor.class), trigger);

            AbstractProject project = mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
            when(project.getTriggers()).thenReturn(triggers);
            when(project.getUrl()).thenReturn("project/url");

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            CalendarEvent nextScheduledEvent = new CalendarEventService().getNextScheduledEvent(event);
            assertThat(nextScheduledEvent, is(notNullValue()));
        }
    }
}
