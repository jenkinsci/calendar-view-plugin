package io.jenkins.plugins.view.calendar;

import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(Enclosed.class)
public class CalendarEventServiceTest {

    public static class GetPastEventsTests {
        @Test
        public void testIsNotAJob() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn(mock(TopLevelItem.class));

            List<CalendarEvent> lastEvents = new CalendarEventService().getLastEvents(event, 2);
            assertThat(lastEvents, hasSize(0));
        }

        @Test
        public void testHasNoPastBuilds() {
            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)mock(Job.class, withSettings().extraInterfaces(TopLevelItem.class)));

            List<CalendarEvent> lastEvents = new CalendarEventService().getLastEvents(event, 2);
            assertThat(lastEvents, hasSize(0));
        }

        @Test
        public void testFreeStyleProjectHasPastBuilds() throws IOException {
            Run build1 = mock(FreeStyleBuild.class);
            when(build1.getResult()).thenReturn(Result.FAILURE);
            when(build1.getUrl()).thenReturn("build1/url");

            Run build2 = mock(FreeStyleBuild.class);
            when(build2.getResult()).thenReturn(Result.SUCCESS);
            when(build2.getUrl()).thenReturn("build2/url");
            when(build2.getPreviousBuild()).thenReturn(build1);

            Run build3 = mock(FreeStyleBuild.class);
            when(build3.getResult()).thenReturn(Result.ABORTED);
            when(build3.getUrl()).thenReturn("build3/url");
            when(build3.getPreviousBuild()).thenReturn(build2);

            final Run build4 = mock(FreeStyleBuild.class);
            when(build4.getResult()).thenReturn(Result.UNSTABLE);
            when(build4.getUrl()).thenReturn("build4/url");
            when(build4.getPreviousBuild()).thenReturn(build3);

            FreeStyleProject project = new FreeStyleProject(mock(ItemGroup.class), "project") {
                @Override
                public FreeStyleBuild getLastBuild() {
                    return (FreeStyleBuild) build4;
                }
            };

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            List<CalendarEvent> lastEvents = new CalendarEventService().getLastEvents(event, 5);
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
            when(build1.getUrl()).thenReturn("build1/url");

            Run build2 = mock(MatrixBuild.class);
            when(build2.getResult()).thenReturn(Result.SUCCESS);
            when(build2.getUrl()).thenReturn("build2/url");
            when(build2.getPreviousBuild()).thenReturn(build1);

            Run build3 = mock(MatrixBuild.class);
            when(build3.getResult()).thenReturn(Result.ABORTED);
            when(build3.getUrl()).thenReturn("build3/url");
            when(build3.getPreviousBuild()).thenReturn(build2);

            final Run build4 = mock(MatrixBuild.class);
            when(build4.getResult()).thenReturn(Result.UNSTABLE);
            when(build4.getUrl()).thenReturn("build4/url");
            when(build4.getPreviousBuild()).thenReturn(build3);

            MatrixProject project = new MatrixProject(mock(ItemGroup.class), "project") {
                @Override
                public MatrixBuild getLastBuild() {
                    return (MatrixBuild) build4;
                }
            };

            CalendarEvent event = mock(CalendarEvent.class);
            when(event.getItem()).thenReturn((TopLevelItem)project);

            List<CalendarEvent> lastEvents = new CalendarEventService().getLastEvents(event, 5);
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
