package io.jenkins.plugins.view.calendar;

import antlr.ANTLRException;
import hudson.model.AbstractProject;
import hudson.model.TopLevelItem;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class CronJobService {

    public List<CronTab> getCronTabs(final Trigger trigger) {
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
                final CronTab cronTab = new CronTab(line, lineNumber, null, timezone);
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

    public Calendar getNextRun(final TopLevelItem item) {
        final Calendar now = GregorianCalendar.getInstance();
        Calendar next = null;
        final List<Trigger> triggers = getCronTriggers(item);
        for (final Trigger trigger: triggers) {
            final List<CronTab> cronTabs = getCronTabs(trigger);
            for (final CronTab cronTab: cronTabs) {
                final Calendar ceil = cronTab.ceil(now);
                if (next == null || ceil.compareTo(next) < 0) {
                    next = ceil;
                }
            }
        }
        return next;
    }
}
