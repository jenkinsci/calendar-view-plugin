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
import hudson.model.TopLevelItem;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;
import hudson.triggers.Trigger;
import io.jenkins.plugins.view.calendar.time.Now;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class CronJobService {

    private final transient Now now;

    public CronJobService() {
        this(new Now());
    }

    public CronJobService(final Now now) {
        this.now = now;
    }

    public List<CronTab> getCronTabs(final Trigger trigger) {
        return getCronTabs(trigger, null);
    }

    public List<CronTab> getCronTabs(final Trigger trigger, final Hash hash) {
        final List<CronTab> cronTabs = new ArrayList<>();
        int lineNumber = 0;
        String timezone = null;

        for (String line : trigger.getSpec().split("\\r?\\n")) {
            lineNumber++;
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

    public List<Trigger> getCronTriggers(final TopLevelItem item) {
        final List<Trigger> triggers = new ArrayList<Trigger>();
        if (!(item instanceof AbstractProject)) {
            return triggers;
        }
        final Collection<Trigger<?>> itemTriggers = ((AbstractProject) item).getTriggers().values();
        for (final Trigger<?> trigger: itemTriggers) {
            if (StringUtils.isNotBlank(trigger.getSpec())) {
                triggers.add(trigger);
            }
        }
        return triggers;
    }

    public List<CronTab> getCronTabs(final TopLevelItem item) {
        final List<CronTab> cronTabs = new ArrayList<CronTab>();
        for (final Trigger trigger: getCronTriggers(item)) {
            cronTabs.addAll(getCronTabs(trigger, Hash.from(item.getFullName())));
        }
        return cronTabs;
    }

    public Calendar getNextStart(final TopLevelItem item) {
        Calendar next = null;
        final List<CronTab> cronTabs = getCronTabs(item);
        for (final CronTab cronTab: cronTabs) {
            final Calendar ceil = cronTab.ceil(now.getNextMinute());
            if (next == null || ceil.before(next)) {
                next = ceil;
                next.set(Calendar.SECOND, 0);
                next.set(Calendar.MILLISECOND, 0);
            }
        }
        return next;
    }
}
