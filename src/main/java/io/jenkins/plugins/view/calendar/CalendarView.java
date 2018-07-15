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
/*
 * Portions of this code copied from the Jenkins Project. See LICENSE.md for notice.
 */
package io.jenkins.plugins.view.calendar;

import antlr.ANTLRException;
import hudson.Util;
import hudson.model.*;
import hudson.scheduler.CronTab;
import hudson.triggers.Trigger;
import hudson.util.RunList;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.scheduler.CronTabList;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarView extends ListView {
    private static final String FORMAT_DATE = "yyyy-MM-dd";
    private static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss";

    private static final List<String> SLOT_DURATIONS = Collections.unmodifiableList(Arrays.asList(
        "00:05:00",
        "00:10:00",
        "00:15:00",
        "00:20:00",
        "00:30:00",
        "01:00:00"
    ));

    public static enum CalendarViewType {
       MONTH, WEEK, DAY;
    }

    public static class Event {
        private TopLevelItem item;
        private Run run;
        private Calendar start;
        private Calendar end;
        private EventType type;
        private String title;
        private String url;
        private long duration;

        public Event(TopLevelItem item, Calendar start, long durationInMillis)  {
            this.item = item;
            this.type = EventType.FUTURE;
            this.title = item.getFullDisplayName();
            this.url = item.getUrl();
            this.start = start;
            this.duration = durationInMillis;
            initEnd(durationInMillis);
        }

        public Event(TopLevelItem item, Run build) {
            this.item = item;
            this.run = build;
            this.type = EventType.fromResult(build.getResult());
            this.title = build.getFullDisplayName();
            this.url = build.getUrl();
            this.start = Calendar.getInstance();
            this.start.setTimeInMillis(build.getStartTimeInMillis());
            this.duration = build.getDuration();
            initEnd(build.getDuration());
        }

        private void initEnd(long durationInMillis) {
            // duration needs to be at least 1sec otherwise
            // fullcalendar will not properly display the event
            if (durationInMillis < 1000) {
                durationInMillis = 1000;
            }
            this.end = Calendar.getInstance();
            this.end.setTime(this.start.getTime());
            this.end.add(Calendar.SECOND, (int)(durationInMillis / 1000));
        }

        public TopLevelItem getItem() {
            return this.item;
        }

        public Calendar getStart() {
            return start;
        }

        public String getStartAsISO8601() {
            return new SimpleDateFormat(FORMAT_ISO8601).format(getStart().getTime());
        }

        public Calendar getEnd() {
            return this.end;
        }

        public String getEndAsISO8601() {
            return new SimpleDateFormat(FORMAT_ISO8601).format(getEnd().getTime());
        }

        public EventType getType() {
            return this.type;
        }

        public String getTypeAsClassName() {
            return "event-" + type.name().toLowerCase();
        }

        public String getUrl() {
            return this.url;
        }

        public String getTitle() {
            return this.title;
        }

        public long getDuration() {
            return this.duration;
        }

        public Run getRun() {
            return this.run;
        }

        public boolean isFuture() {
            return this.run == null;
        }

        public String getTimestampString() {
            long now = new GregorianCalendar().getTimeInMillis();
            long difference = Math.abs(now - start.getTimeInMillis());
            return Util.getPastTimeString(difference);
        }

        public String getDurationString() {
            return Util.getTimeSpanString(duration);
        }

        public String getIconClassName() {
            if (isFuture()) {
                return ((AbstractProject)this.item).getBuildHealth().getIconClassName();
            }
            switch (getType()) {
                case SUCCESS: return "icon-blue";
                case UNSTABLE: return "icon-yellow";
                case FAILURE: return "icon-red";
                default: return "icon-grey";
            }
        }
    }

    public static enum EventType {
        FAILURE, SUCCESS, UNSTABLE, ABORTED, NOT_BUILT, FUTURE;

        public static EventType fromResult(Result result) {
            if (result == null) {
                return null;
            }
            if (result.equals(Result.SUCCESS)) {
                return SUCCESS;
            }
            if (result.equals(Result.FAILURE)) {
                return FAILURE;
            }
            if (result.equals(Result.UNSTABLE)) {
                return UNSTABLE;
            }
            if (result.equals(Result.NOT_BUILT)) {
                return NOT_BUILT;
            }
            if (result.equals(Result.ABORTED)) {
                return ABORTED;
            }
            return null;
        }
    }

    private CalendarViewType calendarViewType = CalendarViewType.WEEK;

    private Boolean useCustomFormats = false;
    private Boolean useCustomWeekSettings = false;
    private Boolean useCustomSlotSettings = false;

    private Boolean weekSettingsShowWeekends;
    private Integer weekSettingsFirstDay = 1;

    private String monthTitleFormat;
    private String monthColumnHeaderFormat;
    private String monthTimeFormat;

    private String weekTitleFormat;
    private String weekColumnHeaderFormat;
    private String weekTimeFormat;
    private String weekSlotTimeFormat;

    private String dayTitleFormat;
    private String dayColumnHeaderFormat;
    private String dayTimeFormat;
    private String daySlotTimeFormat;

    private String weekSlotDuration;
    private String weekMinTime;
    private String weekMaxTime;
    private String daySlotDuration;
    private String dayMinTime;
    private String dayMaxTime;

    @DataBoundConstructor
    public CalendarView(String name) {
        super(name);
    }

    public CalendarViewType getCalendarViewType() {
        return calendarViewType;
    }

    public void setCalendarViewType(CalendarViewType calendarViewType) {
        this.calendarViewType = calendarViewType;
    }

    public boolean getUseCustomFormats() {
        if (useCustomFormats != null) {
            return useCustomFormats;
        }
        return false;
    }

    public void setUseCustomFormats(boolean useCustomFormats) {
        this.useCustomFormats = useCustomFormats;
    }

    public boolean getUseCustomWeekSettings() {
        if (useCustomWeekSettings != null) {
            return useCustomWeekSettings;
        }
        return false;
    }

    public void setUseCustomWeekSettings(boolean useCustomWeekSettings) {
        this.useCustomWeekSettings = useCustomWeekSettings;
    }

    public boolean getUseCustomSlotSettings() {
        if (useCustomSlotSettings != null) {
            return useCustomSlotSettings;
        }
        return false;
    }

    public void setUseCustomSlotSettings(boolean useCustomSlotSettings) {
        this.useCustomSlotSettings = useCustomSlotSettings;
    }

    public boolean getWeekSettingsShowWeekends() {
        if (weekSettingsShowWeekends != null) {
            return weekSettingsShowWeekends;
        }
        return true;
    }

    public void setWeekSettingsShowWeekends(boolean weekSettingsShowWeekends) {
        this.weekSettingsShowWeekends = weekSettingsShowWeekends;
    }

    public int getWeekSettingsFirstDay() {
        if (weekSettingsFirstDay != null) {
            return weekSettingsFirstDay;
        }
        return 1;
    }

    public void setWeekSettingsFirstDay(int weekSettingsFirstDay) {
        this.weekSettingsFirstDay = weekSettingsFirstDay;
    }

    public String getMonthTitleFormat() {
        return monthTitleFormat;
    }

    public void setMonthTitleFormat(String monthTitleFormat) {
        this.monthTitleFormat = monthTitleFormat;
    }

    public String getMonthColumnHeaderFormat() {
        return monthColumnHeaderFormat;
    }

    public void setMonthColumnHeaderFormat(String monthColumnHeaderFormat) {
        this.monthColumnHeaderFormat = monthColumnHeaderFormat;
    }

    public String getMonthTimeFormat() {
        return monthTimeFormat;
    }

    public void setMonthTimeFormat(String monthTimeFormat) {
        this.monthTimeFormat = monthTimeFormat;
    }

    public String getWeekTitleFormat() {
        return weekTitleFormat;
    }

    public void setWeekTitleFormat(String weekTitleFormat) {
        this.weekTitleFormat = weekTitleFormat;
    }

    public String getWeekColumnHeaderFormat() {
        return weekColumnHeaderFormat;
    }

    public void setWeekColumnHeaderFormat(String weekColumnHeaderFormat) {
        this.weekColumnHeaderFormat = weekColumnHeaderFormat;
    }

    public String getWeekTimeFormat() {
        return weekTimeFormat;
    }

    public void setWeekTimeFormat(String weekTimeFormat) {
        this.weekTimeFormat = weekTimeFormat;
    }

    public String getWeekSlotTimeFormat() {
        return weekSlotTimeFormat;
    }

    public void setWeekSlotTimeFormat(String weekSlotTimeFormat) {
        this.weekSlotTimeFormat = weekSlotTimeFormat;
    }

    public String getDayTitleFormat() {
        return dayTitleFormat;
    }

    public void setDayTitleFormat(String dayTitleFormat) {
        this.dayTitleFormat = dayTitleFormat;
    }

    public String getDayColumnHeaderFormat() {
        return dayColumnHeaderFormat;
    }

    public void setDayColumnHeaderFormat(String dayColumnHeaderFormat) {
        this.dayColumnHeaderFormat = dayColumnHeaderFormat;
    }

    public String getDayTimeFormat() {
        return dayTimeFormat;
    }

    public void setDayTimeFormat(String dayTimeFormat) {
        this.dayTimeFormat = dayTimeFormat;
    }

    public String getDaySlotTimeFormat() {
        return daySlotTimeFormat;
    }

    public void setDaySlotTimeFormat(String daySlotTimeFormat) {
        this.daySlotTimeFormat = daySlotTimeFormat;
    }

    public String getWeekSlotDuration() {
        if (weekSlotDuration != null) {
            return weekSlotDuration;
        }
        return "00:30:00";
    }

    public void setWeekSlotDuration(String weekSlotDuration) {
        this.weekSlotDuration = weekSlotDuration;
    }

    public String getDaySlotDuration() {
        if (daySlotDuration != null) {
            return daySlotDuration;
        }
        return "00:30:00";
    }

    public void setDaySlotDuration(String daySlotDuration) {
        this.daySlotDuration = daySlotDuration;
    }

    public String getWeekMinTime() {
        if (weekMinTime != null) {
            return weekMinTime;
        }
        return "00:00:00";
    }

    public void setWeekMinTime(String weekMinTime) {
        this.weekMinTime = weekMinTime;
    }

    public String getWeekMaxTime() {
        if (weekMaxTime != null) {
            return weekMaxTime;
        }
        return "24:00:00";
    }

    public void setWeekMaxTime(String weekMaxTime) {
        this.weekMaxTime = weekMaxTime;
    }

    public String getDayMinTime() {
        if (dayMinTime != null) {
            return dayMinTime;
        }
        return "00:00:00";
    }

    public void setDayMinTime(String dayMinTime) {
        this.dayMinTime = dayMinTime;
    }

    public String getDayMaxTime() {
        if (dayMaxTime != null) {
            return dayMaxTime;
        }
        return "24:00:00";
    }

    public void setDayMaxTime(String dayMaxTime) {
        this.dayMaxTime = dayMaxTime;
    }

    @Override
    public boolean isAutomaticRefreshEnabled() {
        return false;
    }

    @Override
    protected void submit(StaplerRequest req) throws ServletException, Descriptor.FormException, IOException {
        super.submit(req);

        validateRange(req, "weekSettingsFirstDay", 0, 7);

        validateInList(req, "weekSlotDuration", SLOT_DURATIONS);
        validatePattern(req, "weekMinTime", "(0[0-9]|1[0-9]|2[0-4]):00:00");
        validatePattern(req, "weekMaxTime", "(0[0-9]|1[0-9]|2[0-4]):00:00");

        validateInList(req, "daySlotDuration", SLOT_DURATIONS);
        validatePattern(req, "dayMinTime", "(0[0-9]|1[0-9]|2[0-4]):00:00");
        validatePattern(req, "dayMaxTime", "(0[0-9]|1[0-9]|2[0-4]):00:00");

        setCalendarViewType(CalendarViewType.valueOf(req.getParameter("calendarViewType")));

        setUseCustomFormats(req.getParameter("useCustomFormats") != null);
        setUseCustomWeekSettings(req.getParameter("useCustomWeekSettings") != null);
        setUseCustomSlotSettings(req.getParameter("useCustomSlotSettings") != null);

        setWeekSettingsShowWeekends(req.getParameter("weekSettingsShowWeekends") != null);
        setWeekSettingsFirstDay(Integer.parseInt(req.getParameter("weekSettingsFirstDay")));

        setMonthTitleFormat(req.getParameter("monthTitleFormat"));
        setMonthColumnHeaderFormat(req.getParameter("monthColumnHeaderFormat"));
        setMonthTimeFormat(req.getParameter("monthTimeFormat"));

        setWeekTitleFormat(req.getParameter("weekTitleFormat"));
        setWeekColumnHeaderFormat(req.getParameter("weekColumnHeaderFormat"));
        setWeekTimeFormat(req.getParameter("weekTimeFormat"));
        setWeekSlotTimeFormat(req.getParameter("weekSlotTimeFormat"));

        setDayTitleFormat(req.getParameter("dayTitleFormat"));
        setDayColumnHeaderFormat(req.getParameter("dayColumnHeaderFormat"));
        setDayTimeFormat(req.getParameter("dayTimeFormat"));
        setDaySlotTimeFormat(req.getParameter("daySlotTimeFormat"));

        setWeekSlotDuration(req.getParameter("weekSlotDuration"));
        setWeekMinTime(req.getParameter("weekMinTime"));
        setWeekMaxTime(req.getParameter("weekMaxTime"));

        setDaySlotDuration(req.getParameter("daySlotDuration"));
        setDayMinTime(req.getParameter("dayMinTime"));
        setDayMaxTime(req.getParameter("dayMaxTime"));
    }

    private void validateInList(StaplerRequest req, String formField, List<String> possibleValues) throws Descriptor.FormException {
        if (!possibleValues.contains(req.getParameter(formField))) {
            throw new Descriptor.FormException(formField + " must be one of " + possibleValues, formField);
        }
    }

    private void validatePattern(StaplerRequest req, String formField, String pattern) throws Descriptor.FormException {
        String value = req.getParameter(formField);
        if (value == null || !value.matches(pattern)) {
            throw new Descriptor.FormException(formField + " must match " + pattern, formField);
        }
    }

    private void validateRange(StaplerRequest req, String formField, int min, int max) throws Descriptor.FormException {
        int value = Integer.parseInt(req.getParameter("weekSettingsFirstDay"));
        if (value < min || value > max) {
            throw new Descriptor.FormException(formField + " must be: " + min + " <= " + formField + " <= " + max, formField);
        }
    }

    public List<Event> getEvents() throws ParseException {
        Calendar start = getCalendarFromRequestParameter("start");
        Calendar end = getCalendarFromRequestParameter("end");
        Calendar now = Calendar.getInstance();

        List<Event> events = new ArrayList<Event>();
        if (now.compareTo(start) < 0) {
            events.addAll(getFutureEvents(start, end));
        } else if (now.compareTo(end) > 0) {
            events.addAll(getPastEvents(start, end));
        } else {
            events.addAll(getPastEvents(start, now));
            events.addAll(getFutureEvents(now, end));
        }
        return events;
    }

    private List<Event> getFutureEvents(Calendar start, Calendar end) {
        List<Event> events = new ArrayList<Event>();
        for (TopLevelItem item: getItems()) {
            if (!(item instanceof AbstractProject)) {
               continue;
            }
            long durationInMillis = ((AbstractProject)item).getEstimatedDuration();
            List<Trigger> triggers = getCronTriggers(item);
            for (Trigger trigger: triggers) {
                List<CronTab> cronTabs = getCronTabs(trigger);
                for (CronTab cronTab: cronTabs) {
                    long timeInMillis = start.getTimeInMillis();
                    Calendar next;
                    while ((next = cronTab.ceil(timeInMillis)) != null && next.compareTo(start) >= 0 && next.compareTo(end) < 0) {
                        events.add(new Event(item, next, durationInMillis));
                        timeInMillis = next.getTimeInMillis() + 1000 * 60;
                    }
                }
            }
        }
        return events;
    }

    private List<Event> getPastEvents(Calendar start, Calendar end) {
        List<Event> events = new ArrayList<Event>();
        for (TopLevelItem item: getItems()) {
            if (!(item instanceof Job)) {
                continue;
            }
            RunList<Run> builds = ((Job) item).getBuilds();
            for (Run build : builds) {
                Event event = new Event(item, build);
                if (event.getStart().compareTo(start) >= 0 && event.getEnd().compareTo(end) < 0) {
                    events.add(event);
                }
            }
        }
        return events;
    }

    private Calendar getCalendarFromRequestParameter(String param) throws ParseException {
        StaplerRequest request = Stapler.getCurrentRequest();
        String date = request.getParameter(param);
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE);

        Calendar cal = Calendar.getInstance();
        cal.setTime(format.parse(date));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        return cal;
    }

    private List<CronTab> getCronTabs(Trigger trigger) {
        List<CronTab> cronTabs = new ArrayList<>();
        int lineNumber = 0;
        String timezone = null;

        for (String line : trigger.getSpec().split("\\r?\\n")) {
            lineNumber++;
            line = line.trim();

            if (lineNumber == 1 && line.startsWith("TZ=")) {
                timezone = CronTabList.getValidTimezone(line.replace("TZ=",""));
                continue;
            }

            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }

            try {
                cronTabs.add(new CronTab(line, lineNumber, null, timezone));
            } catch (ANTLRException e) {
                String msg = "Unable to parse cron trigger spec: '" + line + "'";
                Logger.getLogger(this.getClass()).error(msg, e);
            }
        }

        return cronTabs;
    }

    private List<Trigger> getCronTriggers(TopLevelItem item) {
        List<Trigger> triggers = new ArrayList<Trigger>();
        if (!(item instanceof AbstractProject)) {
            return triggers;
        }
        Collection<Trigger<?>> itemTriggers = ((AbstractProject) item).getTriggers().values();
        for (Trigger<?> trigger: itemTriggers) {
            if (StringUtils.isNotBlank(trigger.getSpec())) {
                triggers.add(trigger);
            }
        }
        return triggers;
    }


    public String jsonEscape(String text) {
        return StringEscapeUtils.escapeJavaScript(text);
    }

    @Extension
    public static final class DescriptorImpl extends ListView.DescriptorImpl {
        @Override
        public String getDisplayName() {
            return Messages.CalendarView_DisplayName();
        }
    }
}
