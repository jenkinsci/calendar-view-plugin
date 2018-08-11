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

import hudson.model.*;
import io.jenkins.plugins.view.calendar.event.CalendarEvent;
import io.jenkins.plugins.view.calendar.service.CalendarEventService;
import io.jenkins.plugins.view.calendar.service.CronJobService;
import io.jenkins.plugins.view.calendar.time.Now;
import io.jenkins.plugins.view.calendar.util.RequestUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

import static io.jenkins.plugins.view.calendar.util.FieldUtil.defaultIfNull;
import static io.jenkins.plugins.view.calendar.util.ValidationUtil.*;

@SuppressWarnings({
    "PMD.GodClass",
    "PMD.ExcessivePublicCount",
    "PMD.TooManyFields"
})
public class CalendarView extends ListView {

    public static enum CalendarViewType {
       MONTH, WEEK, DAY;
    }

    private CalendarViewType calendarViewType;

    private Boolean useCustomFormats;
    private Boolean useCustomWeekSettings;
    private Boolean useCustomSlotSettings;

    private Boolean weekSettingsShowWeekends;
    private Integer weekSettingsFirstDay;

    private String monthTitleFormat;
    private String monthColumnHeaderFormat;
    private String monthTimeFormat;
    private String monthPopupBuildTimeFormat;

    private String weekTitleFormat;
    private String weekColumnHeaderFormat;
    private String weekTimeFormat;
    private String weekSlotTimeFormat;
    private String weekPopupBuildTimeFormat;

    private String dayTitleFormat;
    private String dayColumnHeaderFormat;
    private String dayTimeFormat;
    private String daySlotTimeFormat;
    private String dayPopupBuildTimeFormat;

    private String weekSlotDuration;
    private String weekMinTime;
    private String weekMaxTime;

    private String daySlotDuration;
    private String dayMinTime;
    private String dayMaxTime;

    @DataBoundConstructor
    public CalendarView(final String name) {
        super(name);
    }

    public CalendarViewType getCalendarViewType() {
        return defaultIfNull(calendarViewType, CalendarViewType.WEEK);
    }

    public void setCalendarViewType(final CalendarViewType calendarViewType) {
        this.calendarViewType = calendarViewType;
    }

    public boolean isUseCustomFormats() {
        return defaultIfNull(useCustomFormats, false);
    }

    public void setUseCustomFormats(final boolean useCustomFormats) {
        this.useCustomFormats = useCustomFormats;
    }

    public boolean isUseCustomWeekSettings() {
        return defaultIfNull(useCustomWeekSettings, false);
    }

    public void setUseCustomWeekSettings(final boolean useCustomWeekSettings) {
        this.useCustomWeekSettings = useCustomWeekSettings;
    }

    public boolean isUseCustomSlotSettings() {
        return defaultIfNull(useCustomSlotSettings, false);
    }

    public void setUseCustomSlotSettings(final boolean useCustomSlotSettings) {
        this.useCustomSlotSettings = useCustomSlotSettings;
    }

    public boolean isWeekSettingsShowWeekends() {
        return defaultIfNull(weekSettingsShowWeekends, true);
    }

    public void setWeekSettingsShowWeekends(final boolean weekSettingsShowWeekends) {
        this.weekSettingsShowWeekends = weekSettingsShowWeekends;
    }

    public int getWeekSettingsFirstDay() {
        return defaultIfNull(weekSettingsFirstDay, 1);
    }

    public void setWeekSettingsFirstDay(final int weekSettingsFirstDay) {
        this.weekSettingsFirstDay = weekSettingsFirstDay;
    }

    public String getMonthTitleFormat() {
        return defaultIfNull(monthTitleFormat, "");
    }

    public void setMonthTitleFormat(final String monthTitleFormat) {
        this.monthTitleFormat = monthTitleFormat;
    }

    public String getMonthColumnHeaderFormat() {
        return defaultIfNull(monthColumnHeaderFormat, "");
    }

    public void setMonthColumnHeaderFormat(final String monthColumnHeaderFormat) {
        this.monthColumnHeaderFormat = monthColumnHeaderFormat;
    }

    public String getMonthTimeFormat() {
        return defaultIfNull(monthTimeFormat, "");
    }

    public void setMonthTimeFormat(final String monthTimeFormat) {
        this.monthTimeFormat = monthTimeFormat;
    }

    public String getMonthPopupBuildTimeFormat() {
        return defaultIfNull(monthPopupBuildTimeFormat, "");
    }

    public void setMonthPopupBuildTimeFormat(final String monthPopupBuildTimeFormat) {
        this.monthPopupBuildTimeFormat = monthPopupBuildTimeFormat;
    }

    public String getWeekTitleFormat() {
        return defaultIfNull(weekTitleFormat, "");
    }

    public void setWeekTitleFormat(final String weekTitleFormat) {
        this.weekTitleFormat = weekTitleFormat;
    }

    public String getWeekColumnHeaderFormat() {
        return defaultIfNull(weekColumnHeaderFormat, "");
    }

    public void setWeekColumnHeaderFormat(final String weekColumnHeaderFormat) {
        this.weekColumnHeaderFormat = weekColumnHeaderFormat;
    }

    public String getWeekTimeFormat() {
        return defaultIfNull(weekTimeFormat, "");
    }

    public void setWeekTimeFormat(final String weekTimeFormat) {
        this.weekTimeFormat = weekTimeFormat;
    }

    public String getWeekSlotTimeFormat() {
        return defaultIfNull(weekSlotTimeFormat, "");
    }

    public void setWeekSlotTimeFormat(final String weekSlotTimeFormat) {
        this.weekSlotTimeFormat = weekSlotTimeFormat;
    }

    public String getWeekPopupBuildTimeFormat() {
        return defaultIfNull(weekPopupBuildTimeFormat, "");
    }

    public void setWeekPopupBuildTimeFormat(final String weekPopupBuildTimeFormat) {
        this.weekPopupBuildTimeFormat = weekPopupBuildTimeFormat;
    }

    public String getDayTitleFormat() {
        return defaultIfNull(dayTitleFormat, "");
    }

    public void setDayTitleFormat(final String dayTitleFormat) {
        this.dayTitleFormat = dayTitleFormat;
    }

    public String getDayColumnHeaderFormat() {
        return defaultIfNull(dayColumnHeaderFormat, "");
    }

    public void setDayColumnHeaderFormat(final String dayColumnHeaderFormat) {
        this.dayColumnHeaderFormat = dayColumnHeaderFormat;
    }

    public String getDayTimeFormat() {
        return defaultIfNull(dayTimeFormat, "");
    }

    public void setDayTimeFormat(final String dayTimeFormat) {
        this.dayTimeFormat = dayTimeFormat;
    }

    public String getDaySlotTimeFormat() {
        return defaultIfNull(daySlotTimeFormat, "");
    }

    public void setDaySlotTimeFormat(final String daySlotTimeFormat) {
        this.daySlotTimeFormat = daySlotTimeFormat;
    }

    public String getDayPopupBuildTimeFormat() {
        return defaultIfNull(dayPopupBuildTimeFormat, "");
    }

    public void setDayPopupBuildTimeFormat(final String dayPopupBuildTimeFormat) {
        this.dayPopupBuildTimeFormat = dayPopupBuildTimeFormat;
    }

    public String getWeekSlotDuration() {
        return defaultIfNull(weekSlotDuration, "00:30:00");
    }

    public void setWeekSlotDuration(final String weekSlotDuration) {
        this.weekSlotDuration = weekSlotDuration;
    }

    public String getDaySlotDuration() {
        return defaultIfNull(daySlotDuration, "00:30:00");
    }

    public void setDaySlotDuration(final String daySlotDuration) {
        this.daySlotDuration = daySlotDuration;
    }

    public String getWeekMinTime() {
        return defaultIfNull(weekMinTime, "00:00:00");
    }

    public void setWeekMinTime(final String weekMinTime) {
        this.weekMinTime = weekMinTime;
    }

    public String getWeekMaxTime() {
        return defaultIfNull(weekMaxTime, "24:00:00");
    }

    public void setWeekMaxTime(final String weekMaxTime) {
        this.weekMaxTime = weekMaxTime;
    }

    public String getDayMinTime() {
        return defaultIfNull(dayMinTime, "00:00:00");
    }

    public void setDayMinTime(final String dayMinTime) {
        this.dayMinTime = dayMinTime;
    }

    public String getDayMaxTime() {
        return defaultIfNull(dayMaxTime, "24:00:00");
    }

    public void setDayMaxTime(final String dayMaxTime) {
        this.dayMaxTime = dayMaxTime;
    }

    @Override
    public boolean isAutomaticRefreshEnabled() {
        return false;
    }

    @Override
    protected void submit(final StaplerRequest req) throws ServletException, Descriptor.FormException, IOException {
        this.validate(req);
        super.submit(req);
        this.updateFields(req);
    }

    private void validate(final StaplerRequest req) throws Descriptor.FormException {
        final List<String> validSlotDurations = Collections.unmodifiableList(Arrays.asList(
            "00:05:00", "00:10:00", "00:15:00", "00:20:00", "00:30:00", "01:00:00"
        ));
        final Pattern validDateTimePattern = Pattern.compile("(0[0-9]|1[0-9]|2[0-4]):00:00");

        validateEnum(req, "calendarViewType", CalendarViewType.class);
        validateRange(req, "weekSettingsFirstDay", 0, 7);

        validateInList(req, "weekSlotDuration", validSlotDurations);
        validatePattern(req, "weekMinTime", validDateTimePattern);
        validatePattern(req, "weekMaxTime", validDateTimePattern);

        validateInList(req, "daySlotDuration", validSlotDurations);
        validatePattern(req, "dayMinTime", validDateTimePattern);
        validatePattern(req, "dayMaxTime", validDateTimePattern);
    }

    private void updateFields(final StaplerRequest req) {
        setCalendarViewType(CalendarViewType.valueOf(req.getParameter("calendarViewType")));

        setUseCustomFormats(req.getParameter("useCustomFormats") != null);
        setUseCustomWeekSettings(req.getParameter("useCustomWeekSettings") != null);
        setUseCustomSlotSettings(req.getParameter("useCustomSlotSettings") != null);

        setWeekSettingsShowWeekends(req.getParameter("weekSettingsShowWeekends") != null);
        setWeekSettingsFirstDay(Integer.parseInt(req.getParameter("weekSettingsFirstDay")));

        setMonthTitleFormat(req.getParameter("monthTitleFormat"));
        setMonthColumnHeaderFormat(req.getParameter("monthColumnHeaderFormat"));
        setMonthTimeFormat(req.getParameter("monthTimeFormat"));
        setMonthPopupBuildTimeFormat(req.getParameter("monthPopupBuildTimeFormat"));

        setWeekTitleFormat(req.getParameter("weekTitleFormat"));
        setWeekColumnHeaderFormat(req.getParameter("weekColumnHeaderFormat"));
        setWeekTimeFormat(req.getParameter("weekTimeFormat"));
        setWeekSlotTimeFormat(req.getParameter("weekSlotTimeFormat"));
        setWeekPopupBuildTimeFormat(req.getParameter("weekPopupBuildTimeFormat"));

        setDayTitleFormat(req.getParameter("dayTitleFormat"));
        setDayColumnHeaderFormat(req.getParameter("dayColumnHeaderFormat"));
        setDayTimeFormat(req.getParameter("dayTimeFormat"));
        setDaySlotTimeFormat(req.getParameter("daySlotTimeFormat"));
        setDayPopupBuildTimeFormat(req.getParameter("dayPopupBuildTimeFormat"));

        setWeekSlotDuration(req.getParameter("weekSlotDuration"));
        setWeekMinTime(req.getParameter("weekMinTime"));
        setWeekMaxTime(req.getParameter("weekMaxTime"));

        setDaySlotDuration(req.getParameter("daySlotDuration"));
        setDayMinTime(req.getParameter("dayMinTime"));
        setDayMaxTime(req.getParameter("dayMaxTime"));
    }

    public List<CalendarEvent> getEvents() throws ParseException {
        final StaplerRequest req = Stapler.getCurrentRequest();

        final Calendar start = RequestUtil.getParamAsCalendar(req, "start");
        final Calendar end = RequestUtil.getParamAsCalendar(req, "end");

        final List<TopLevelItem> items = getItems();

        final Now now = new Now();
        return new CalendarEventService(now, new CronJobService(now)).getCalendarEvents(items, start, end);
    }

    public String jsonEscape(final String text) {
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
