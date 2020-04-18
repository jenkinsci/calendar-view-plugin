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

import antlr.ANTLRException;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;
import hudson.triggers.Trigger;
import io.jenkins.plugins.view.calendar.time.Moment;
import io.jenkins.plugins.view.calendar.util.PluginUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jenkinsci.plugins.parameterizedscheduler.ParameterizedTimerTrigger;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
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

    public List<CronTab> getCronTabs(final Trigger trigger) {
        return getCronTabs(trigger, null);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public List<CronTab> getCronTabs(final Trigger trigger, final Hash hash) {
        final List<CronTab> cronTabs = new ArrayList<>();
        final boolean isParameterizedTrigger = PluginUtil.hasParameterizedSchedulerPluginInstalled()
                && trigger instanceof ParameterizedTimerTrigger;
        int lineNumber = 0;
        String timezone = null;

        final String specification = isParameterizedTrigger ?
                ((ParameterizedTimerTrigger) trigger).getParameterizedSpecification() : trigger.getSpec();

        for (String line : specification.split("\\r?\\n")) {
            lineNumber++;
            if (isParameterizedTrigger) {
                line = line.split("%")[0];
            }
            line = line.trim();

            if (lineNumber == 1 && line.startsWith("TZ=")) {
                timezone = CronTabList.getValidTimezone(line.replace("TZ=",""));
                continue;
            }

            if (line.length() == 0 || line.charAt(0) == '#') {
                continue;
            }

            try {
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                final CronTab cronTab = new CronTab(line, lineNumber, hash, timezone);
                cronTabs.add(cronTab);
            } catch (ANTLRException e) {
                final String msg = "Unable to parse cron trigger spec: '" + line + "'";
                Logger.getLogger(this.getClass()).error(msg, e);
            }
        }

        return cronTabs;
    }

    public List<Trigger> getCronTriggers(final Job job) {
        Collection<Trigger<?>> jobTriggers;
        if (job instanceof AbstractProject) {
            jobTriggers = ((AbstractProject)job).getTriggers().values();
        } else if (PluginUtil.hasWorkflowJobPluginInstalled() && job instanceof WorkflowJob) {
            jobTriggers = ((WorkflowJob)job).getTriggers().values();
        } else {
            return Collections.emptyList();
        }

        final List<Trigger> cronTriggers = new ArrayList<>();
        for (final Trigger<?> jobTrigger: jobTriggers) {
            if (StringUtils.isNotBlank(jobTrigger.getSpec())) {
                cronTriggers.add(jobTrigger);
            } else if (PluginUtil.hasParameterizedSchedulerPluginInstalled()
                    && jobTrigger instanceof ParameterizedTimerTrigger
                    && StringUtils.isNotBlank(((ParameterizedTimerTrigger) jobTrigger).getParameterizedSpecification())) {
                cronTriggers.add(jobTrigger);
            }
        }
        return cronTriggers;
    }

    public List<CronTab> getCronTabs(final Job job) {
        final List<CronTab> cronTabs = new ArrayList<CronTab>();
        for (final Trigger trigger: getCronTriggers(job)) {
            cronTabs.addAll(getCronTabs(trigger, Hash.from(job.getFullName())));
        }
        return cronTabs;
    }

    public Calendar getNextStart(final Job job) {
        Calendar next = null;
        final List<CronTab> cronTabs = getCronTabs(job);
        for (final CronTab cronTab: cronTabs) {
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
