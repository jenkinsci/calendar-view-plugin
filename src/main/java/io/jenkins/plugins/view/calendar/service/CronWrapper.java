package io.jenkins.plugins.view.calendar.service;

import hudson.scheduler.CronTab;
import io.jenkins.plugins.extended_timer_trigger.CronTabWrapper;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;

import org.jenkinsci.plugins.parameterizedscheduler.ParameterizedCronTab;

public abstract class CronWrapper<T> {
    public abstract Calendar ceil(long timeInMillis);
    public abstract Calendar floor(long timeInMillis);
    public abstract T getCronTab();
    public abstract Map<String, String> getParameters();

    public static class ClassicCronTab extends CronWrapper<CronTab> {
        private final CronTab cronTab;

        public ClassicCronTab(CronTab cronTab) {
            this.cronTab = cronTab;
        }

        @Override
        public Calendar ceil(long timeInMillis) {
            return cronTab.ceil(timeInMillis);
        }

        @Override
        public Calendar floor(long timeInMillis) {
            return cronTab.floor(timeInMillis);
        }

        @Override
        public CronTab getCronTab() {
            return cronTab;
        }

        @Override
        public Map<String, String> getParameters() {
            return Collections.emptyMap();
        }
    }

    public static class ParameterizedCronWrapper extends CronWrapper<ParameterizedCronTab> {

        private final ParameterizedCronTab cronTab;
        public ParameterizedCronWrapper(ParameterizedCronTab cronTab) {
            this.cronTab = cronTab;
        }

        @Override
        public Calendar ceil(long timeInMillis) {
            return cronTab.ceil(timeInMillis);
        }

        @Override
        public Calendar floor(long timeInMillis) {
            return cronTab.floor(timeInMillis);
        }

        @Override
        public ParameterizedCronTab getCronTab() {
            return cronTab;
        }

        @Override
        public Map<String, String> getParameters() {
            return this.cronTab.getParameterValues();
        }
    }

    public static class ExtendedCronTab extends CronWrapper<CronTabWrapper> {
        private final CronTabWrapper cronTab;

        public ExtendedCronTab(CronTabWrapper cronTab) {
            this.cronTab = cronTab;
        }

        public Calendar ceil(long timeInMillis) {
            ZonedDateTime ceil = cronTab.ceil(timeInMillis);
            if (ceil != null) {
                return GregorianCalendar.from(ceil);
            }
            return null;
        }

        public Calendar floor(long timeInMillis) {
            ZonedDateTime ceil = cronTab.floor(timeInMillis);
            if (ceil != null) {
                return GregorianCalendar.from(ceil);
            }
            return null;
        }

        @Override
        public CronTabWrapper getCronTab() {
            return cronTab;
        }

        @Override
        public Map<String, String> getParameters() {
            return this.cronTab.getParameters();
        }
    }
}
