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

import hudson.model.Job;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;
import hudson.triggers.Trigger;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.extended_timer_trigger.ExtendedTimerTrigger;
import io.jenkins.plugins.view.calendar.CalendarView.CalendarViewEventsType;
import io.jenkins.plugins.view.calendar.time.Moment;
import io.jenkins.plugins.view.calendar.util.PluginUtil;

import jenkins.triggers.TriggeredItem;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jenkinsci.plugins.parameterizedscheduler.ParameterizedTimerTrigger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.*;

@Restricted(NoExternalUse.class)
public class CronJobService {

    private final transient Moment now;

    public CronJobService() {
        this(new Moment());
    }

    public CronJobService(final Moment now) {
        this.now = now;
    }

    public List<CronWrapper<?>> getCronTabs(final Trigger trigger) {
        return getCronTabs(trigger, null);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public List<CronWrapper<?>> getCronTabs(final Trigger trigger, final Hash hash) {
        final List<CronWrapper<?>> cronTabs = new ArrayList<>();
        int lineNumber = 0;
        String timezone = null;

        if (PluginUtil.hasParameterizedSchedulerPluginInstalled()
                && trigger instanceof ParameterizedTimerTrigger ptt) {
            ptt.getCronTabList().getCronTabs().forEach(ct -> cronTabs.add(new CronWrapper.ParameterizedCronWrapper(ct)));
            return cronTabs;
        }

        if (PluginUtil.hasExtendedTimerTriggerPluginInstalled()
                && trigger instanceof ExtendedTimerTrigger ett) {
            ett.getExtendedCronTabList().getCronTabWrapperList().forEach(ct -> cronTabs.add(new CronWrapper.ExtendedCronTab(ct)));
            return cronTabs;
        }

        final String specification = trigger.getSpec();

        for (String line : specification.split("\\r?\\n")) {
            lineNumber++;
            line = line.trim();

            if (lineNumber == 1 && line.startsWith("TZ=")) {
                timezone = CronTabList.getValidTimezone(line.replace("TZ=",""));
                continue;
            }

            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }

            try {
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                final CronTab cronTab = new CronTab(line, lineNumber, hash, timezone);
                cronTabs.add(new CronWrapper.ClassicCronTab(cronTab));
            } catch (IllegalArgumentException e) {
                final String msg = "Unable to parse cron trigger spec: '" + line + "'";
                Logger.getLogger(this.getClass()).error(msg, e);
            }
        }

        return cronTabs;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public List<Trigger> getCronTriggers(final Job job, final CalendarViewEventsType eventsType) {
        final Collection<Trigger<?>> jobTriggers;
        if (job instanceof TriggeredItem ti) {
            jobTriggers = ti.getTriggers().values();
        } else {
            return Collections.emptyList();
        }

        final List<Trigger> cronTriggers = new ArrayList<>();
        for (final Trigger<?> jobTrigger: jobTriggers) {
            if (eventsType == CalendarViewEventsType.ALL ||
                    (eventsType == CalendarViewEventsType.BUILDS ^ jobTrigger instanceof SCMTrigger)) {
                if (StringUtils.isNotBlank(jobTrigger.getSpec())) {
                    cronTriggers.add(jobTrigger);
                } else if (PluginUtil.hasParameterizedSchedulerPluginInstalled()
                        && jobTrigger instanceof ParameterizedTimerTrigger
                        && StringUtils.isNotBlank(((ParameterizedTimerTrigger) jobTrigger).getParameterizedSpecification())) {
                    cronTriggers.add(jobTrigger);
                } else if (PluginUtil.hasExtendedTimerTriggerPluginInstalled() &&
                        jobTrigger instanceof ExtendedTimerTrigger
                        && StringUtils.isNotBlank(((ExtendedTimerTrigger) jobTrigger).getCronSpec())) {
                    cronTriggers.add(jobTrigger);
                }
            }
        }
        return cronTriggers;
    }

    public List<CronWrapper<?>> getCronTabs(final Job job, final CalendarViewEventsType eventsType) {
        final List<CronWrapper<?>> cronTabs = new ArrayList<>();
        for (final Trigger trigger: getCronTriggers(job, eventsType)) {
            cronTabs.addAll(getCronTabs(trigger, Hash.from(job.getFullName())));
        }
        return cronTabs;
    }

    public Calendar getNextStart(final Job job, final CalendarViewEventsType eventsType) {
        Calendar next = null;
        final List<CronWrapper<?>> cronTabs = getCronTabs(job, eventsType);
        for (final CronWrapper<?> cronTab: cronTabs) {
            final Calendar ceil = cronTab.ceil(now.nextMinute().getTimeInMillis());
            if (next == null || ceil.before(next)) {
                next = ceil;
                next.set(Calendar.SECOND, 0);
                next.set(Calendar.MILLISECOND, 0);
            }
        }
        return next;
    }
}
