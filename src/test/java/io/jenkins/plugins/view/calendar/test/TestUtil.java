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
package io.jenkins.plugins.view.calendar.test;

import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.RunList;
import io.jenkins.plugins.view.calendar.event.CalendarEvent;

import org.jenkinsci.plugins.parameterizedscheduler.ParameterizedTimerTrigger;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.util.*;

import static io.jenkins.plugins.view.calendar.test.CalendarUtil.cal;
import static org.mockito.Mockito.*;

public class TestUtil {
    public static AbstractProject mockProject() {
        return mock(AbstractProject.class, withSettings().extraInterfaces(TopLevelItem.class));
    }

    public static FreeStyleProject mockFreeStyleProject() {
        return mock(FreeStyleProject.class);
    }

    public static FreeStyleProject mockScheduledFreeStyleProject(String name, String spec, long estimatedDuration) {
        Map<TriggerDescriptor, Trigger<?>> triggers = mockTriggers(spec);

        FreeStyleProject project = mock(FreeStyleProject.class);
        when(project.getFullName()).thenReturn(name);
        when(project.getFullDisplayName()).thenReturn(name);
        when(project.getTriggers()).thenReturn(triggers);
        when(project.getEstimatedDuration()).thenReturn(estimatedDuration);
        when(project.getBuilds()).thenReturn(new RunList());
        when(project.isBuilding()).thenReturn(false);
        when(project.isBuildable()).thenReturn(true);
        return project;
    }

    public static FreeStyleProject mockRunningFreeStyleProject(String name, String start, long estimatedDuration) throws ParseException {
        RunList<FreeStyleBuild> builds = mockBuilds(
          mockRunningFreeStyleBuild(name, start, estimatedDuration)
        );

        FreeStyleProject project = mock(FreeStyleProject.class);
        when(project.getFullName()).thenReturn(name);
        when(project.getFullDisplayName()).thenReturn(name);
        when(project.getEstimatedDuration()).thenReturn(estimatedDuration);
        when(project.getBuilds()).thenReturn(builds);
        when(project.isBuilding()).thenReturn(true);
        return project;
    }

    public static FreeStyleProject mockFinishedFreeStyleProject(String name, String start, long duration) throws ParseException {
        RunList<FreeStyleBuild> builds = mockBuilds(
          mockFinishedFreeStyleBuild(name, start, duration, Result.SUCCESS)
        );

        FreeStyleProject project = mock(FreeStyleProject.class);
        when(project.getFullName()).thenReturn(name);
        when(project.getFullDisplayName()).thenReturn(name);
        when(project.getEstimatedDuration()).thenReturn(duration);
        when(project.getBuilds()).thenReturn(builds);
        when(project.isBuilding()).thenReturn(false);
        return project;
    }

    public static Map<TriggerDescriptor, Trigger<?>> mockTriggers(String... specs)  {
        Map<TriggerDescriptor, Trigger<?>> triggers = new HashMap<>();
        for (String spec : specs) {
            Trigger trigger = mock(Trigger.class);
            when(trigger.getSpec()).thenReturn(spec);
            triggers.put(mock(TriggerDescriptor.class), trigger);
        }
        return triggers;
    }

    public static Map<TriggerDescriptor, Trigger<?>> mockParameterizedTriggers(String... specs)  {
        Map<TriggerDescriptor, Trigger<?>> triggers = new HashMap<>();
        for (String spec : specs) {
            ParameterizedTimerTrigger trigger = mock(ParameterizedTimerTrigger.class);
            when(trigger.getParameterizedSpecification()).thenReturn(spec);
            triggers.put(mock(TriggerDescriptor.class), trigger);
        }
        return triggers;
    }

    public static <T extends Run> RunList<T> mockBuilds(final T... builds) {
        RunList runList = mock(RunList.class);
        when(runList.iterator()).thenAnswer(new Answer<Iterator<T>>() {
            @Override
            public Iterator<T> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Arrays.asList(builds).iterator();
            }
        });
        return runList;
    }

    public static FreeStyleBuild mockFinishedFreeStyleBuild(String name, String start, long duration, Result result) throws ParseException {
        return mockFinishedBuild(FreeStyleBuild.class, name, start, duration, result);
    }

    public static FreeStyleBuild mockRunningFreeStyleBuild(String name, String start, long duration) throws ParseException {
        return mockRunningBuild(FreeStyleBuild.class, name, start, duration);
    }

    public static <T extends Run> T mockFinishedBuild(Class<T> clazz, String name, String start, long duration, Result result) throws ParseException {
        Calendar startCal = cal(start);
        T build = mock(clazz);
        when(build.getFullDisplayName()).thenReturn(name);
        when(build.getStartTimeInMillis()).thenReturn(startCal.getTimeInMillis());
        when(build.getDuration()).thenReturn(duration);
        when(build.getResult()).thenReturn(result);
        when(build.isBuilding()).thenReturn(false);
        return build;
    }

    public static <T extends Run> T mockRunningBuild(Class<T> clazz, String name, String start, long estimatedDuration) throws ParseException {
        Calendar startCal = cal(start);
        T build = mock(clazz);
        when(build.getFullDisplayName()).thenReturn(name);
        when(build.getStartTimeInMillis()).thenReturn(startCal.getTimeInMillis());
        when(build.getEstimatedDuration()).thenReturn(estimatedDuration);
        when(build.isBuilding()).thenReturn(true);
        return build;
    }

    public static Set<String> titlesOf(List<? extends CalendarEvent> events) {
        Set<String> titles = new HashSet<>();
        for (CalendarEvent event: events) {
            titles.add(event.getTitle());
        }
        return titles;
    }
}
