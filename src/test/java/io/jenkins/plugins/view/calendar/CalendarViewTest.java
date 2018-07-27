package io.jenkins.plugins.view.calendar;

import com.gargoylesoftware.htmlunit.html.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Test;
import org.junit.Rule;


import static org.junit.Assert.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class CalendarViewTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    private CalendarView createCalendarView(String name) throws Exception {
        HtmlPage newView = j.createWebClient().goTo("newView");

        HtmlTextInput nameTextInput = newView.querySelector("input[name='name']");
        nameTextInput.setText(name);

        HtmlRadioButtonInput calendarViewRadioButton = newView.querySelector("input[value='io.jenkins.plugins.view.calendar.CalendarView']");
        calendarViewRadioButton.setChecked(true);

        HtmlButton okButton = newView.querySelector("#ok-button");
        okButton.click();

        return (CalendarView) j.getInstance().getView(name);
    }

    @Test
    public void testConfigRoundtripDefaults() throws Exception {
        CalendarView calendarView = createCalendarView("cal_defaults");

        assertDefaults(calendarView);

        j.configRoundtrip(calendarView);

        assertDefaults(calendarView);
    }

    private void assertDefaults(CalendarView calendarView) {
        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(false));
        assertThat(calendarView.isUseCustomFormats(), equalTo(false));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(false));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(true));
        assertThat(calendarView.getWeekSettingsFirstDay(), equalTo(1));

        assertThat(calendarView.getMonthTitleFormat(), equalTo(""));
        assertThat(calendarView.getMonthColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getMonthTimeFormat(), equalTo(""));
        assertThat(calendarView.getMonthPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getWeekTitleFormat(), equalTo(""));
        assertThat(calendarView.getWeekColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getWeekTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekSlotTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getDayTitleFormat(), equalTo(""));
        assertThat(calendarView.getDayColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getDayTimeFormat(), equalTo(""));
        assertThat(calendarView.getDaySlotTimeFormat(), equalTo(""));
        assertThat(calendarView.getDayPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getWeekSlotDuration(), equalTo("00:30:00"));
        assertThat(calendarView.getWeekMinTime(), equalTo("00:00:00"));
        assertThat(calendarView.getWeekMaxTime(), equalTo("24:00:00"));

        assertThat(calendarView.getDaySlotDuration(), equalTo("00:30:00"));
        assertThat(calendarView.getDayMinTime(), equalTo("00:00:00"));
        assertThat(calendarView.getDayMaxTime(), equalTo("24:00:00"));
    }

    @Test
    public void testConfigRoundtripForCustomWeekSettings() throws Exception {
        CalendarView calendarView = createCalendarView("cal_customWeekSettings");

        calendarView.setCalendarViewType(CalendarView.CalendarViewType.WEEK);

        calendarView.setUseCustomWeekSettings(true);
        calendarView.setWeekSettingsShowWeekends(false);
        calendarView.setWeekSettingsFirstDay(3);

        j.configRoundtrip(calendarView);

        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(true));
        assertThat(calendarView.isUseCustomFormats(), equalTo(false));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(false));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(false));
        assertThat(calendarView.getWeekSettingsFirstDay(), equalTo(3));

        assertThat(calendarView.getMonthTitleFormat(), equalTo(""));
        assertThat(calendarView.getMonthColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getMonthTimeFormat(), equalTo(""));
        assertThat(calendarView.getMonthPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getWeekTitleFormat(), equalTo(""));
        assertThat(calendarView.getWeekColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getWeekTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekSlotTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getDayTitleFormat(), equalTo(""));
        assertThat(calendarView.getDayColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getDayTimeFormat(), equalTo(""));
        assertThat(calendarView.getDaySlotTimeFormat(), equalTo(""));
        assertThat(calendarView.getDayPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getWeekSlotDuration(), equalTo("00:30:00"));
        assertThat(calendarView.getWeekMinTime(), equalTo("00:00:00"));
        assertThat(calendarView.getWeekMaxTime(), equalTo("24:00:00"));

        assertThat(calendarView.getDaySlotDuration(), equalTo("00:30:00"));
        assertThat(calendarView.getDayMinTime(), equalTo("00:00:00"));
        assertThat(calendarView.getDayMaxTime(), equalTo("24:00:00"));
    }


    @Test
    public void testConfigRoundtripForCustomFormats() throws Exception {
        CalendarView calendarView = createCalendarView("cal_customFormats");

        calendarView.setUseCustomFormats(true);

        calendarView.setMonthTitleFormat("abc");
        calendarView.setMonthColumnHeaderFormat("def");
        calendarView.setMonthTimeFormat("ghi");
        calendarView.setMonthPopupBuildTimeFormat("jkl");

        calendarView.setWeekTitleFormat("mno");
        calendarView.setWeekColumnHeaderFormat("pqr");
        calendarView.setWeekTimeFormat("stu");
        calendarView.setWeekSlotTimeFormat("vwx");
        calendarView.setWeekPopupBuildTimeFormat("yza");

        calendarView.setDayTitleFormat("bcd");
        calendarView.setDayColumnHeaderFormat("efg");
        calendarView.setDayTimeFormat("hij");
        calendarView.setDaySlotTimeFormat("klm");
        calendarView.setDayPopupBuildTimeFormat("nop");

        j.configRoundtrip(calendarView);

        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(false));
        assertThat(calendarView.isUseCustomFormats(), equalTo(true));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(false));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(true));
        assertThat(calendarView.getWeekSettingsFirstDay(), equalTo(1));

        assertThat(calendarView.getMonthTitleFormat(), equalTo("abc"));
        assertThat(calendarView.getMonthColumnHeaderFormat(), equalTo("def"));
        assertThat(calendarView.getMonthTimeFormat(), equalTo("ghi"));
        assertThat(calendarView.getMonthPopupBuildTimeFormat(), equalTo("jkl"));

        assertThat(calendarView.getWeekTitleFormat(), equalTo("mno"));
        assertThat(calendarView.getWeekColumnHeaderFormat(), equalTo("pqr"));
        assertThat(calendarView.getWeekTimeFormat(), equalTo("stu"));
        assertThat(calendarView.getWeekSlotTimeFormat(), equalTo("vwx"));
        assertThat(calendarView.getWeekPopupBuildTimeFormat(), equalTo("yza"));

        assertThat(calendarView.getDayTitleFormat(), equalTo("bcd"));
        assertThat(calendarView.getDayColumnHeaderFormat(), equalTo("efg"));
        assertThat(calendarView.getDayTimeFormat(), equalTo("hij"));
        assertThat(calendarView.getDaySlotTimeFormat(), equalTo("klm"));
        assertThat(calendarView.getDayPopupBuildTimeFormat(), equalTo("nop"));

        assertThat(calendarView.getWeekSlotDuration(), equalTo("00:30:00"));
        assertThat(calendarView.getWeekMinTime(), equalTo("00:00:00"));
        assertThat(calendarView.getWeekMaxTime(), equalTo("24:00:00"));

        assertThat(calendarView.getDaySlotDuration(), equalTo("00:30:00"));
        assertThat(calendarView.getDayMinTime(), equalTo("00:00:00"));
        assertThat(calendarView.getDayMaxTime(), equalTo("24:00:00"));
    }

    @Test
    public void testConfigRoundtripForCustomSlotSettings() throws Exception {
        CalendarView calendarView = createCalendarView("cal_customSlotSettings");

        calendarView.setUseCustomSlotSettings(true);

        calendarView.setWeekSlotDuration("00:15:00");
        calendarView.setWeekMinTime("07:00:00");
        calendarView.setWeekMaxTime("22:00:00");

        calendarView.setDaySlotDuration("00:20:00");
        calendarView.setDayMinTime("09:00:00");
        calendarView.setDayMaxTime("18:00:00");

        j.configRoundtrip(calendarView);

        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(false));
        assertThat(calendarView.isUseCustomFormats(), equalTo(false));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(true));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(true));
        assertThat(calendarView.getWeekSettingsFirstDay(), equalTo(1));

        assertThat(calendarView.getMonthTitleFormat(), equalTo(""));
        assertThat(calendarView.getMonthColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getMonthTimeFormat(), equalTo(""));
        assertThat(calendarView.getMonthPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getWeekTitleFormat(), equalTo(""));
        assertThat(calendarView.getWeekColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getWeekTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekSlotTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getDayTitleFormat(), equalTo(""));
        assertThat(calendarView.getDayColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getDayTimeFormat(), equalTo(""));
        assertThat(calendarView.getDaySlotTimeFormat(), equalTo(""));
        assertThat(calendarView.getDayPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getWeekSlotDuration(), equalTo("00:15:00"));
        assertThat(calendarView.getWeekMinTime(), equalTo("07:00:00"));
        assertThat(calendarView.getWeekMaxTime(), equalTo("22:00:00"));

        assertThat(calendarView.getDaySlotDuration(), equalTo("00:20:00"));
        assertThat(calendarView.getDayMinTime(), equalTo("09:00:00"));
        assertThat(calendarView.getDayMaxTime(), equalTo("18:00:00"));
    }
}