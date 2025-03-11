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
package io.jenkins.plugins.view.calendar;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlRadioButtonInput;
import org.htmlunit.html.HtmlTextInput;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WithJenkins
class CalendarViewTest {

    @Test
    void testConfigRoundtripDefaults(JenkinsRule j) throws Exception {
        CalendarView calendarView = createCalendarView(j, "cal_defaults");

        assertDefaults(calendarView);

        j.configRoundtrip(calendarView);

        assertDefaults(calendarView);
    }

    @Test
    void testConfigRoundtripForCustomWeekSettings(JenkinsRule j) throws Exception {
        CalendarView calendarView = createCalendarView(j, "cal_customWeekSettings");

        calendarView.setCalendarViewType(CalendarView.CalendarViewType.WEEK);

        calendarView.setUseCustomWeekSettings(true);
        calendarView.setWeekSettingsShowWeekends(false);
        calendarView.setWeekSettingsShowWeekNumbers(false);
        calendarView.setWeekSettingsFirstDay(3);

        j.configRoundtrip(calendarView);

        assertThat(calendarView.getCalendarViewEventsType(), equalTo(CalendarView.CalendarViewEventsType.ALL));
        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(true));
        assertThat(calendarView.isUseCustomFormats(), equalTo(false));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(false));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(false));
        assertThat(calendarView.isWeekSettingsShowWeekNumbers(), equalTo(false));
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
    void testConfigRoundtripForCustomFormats(JenkinsRule j) throws Exception {
        CalendarView calendarView = createCalendarView(j, "cal_customFormats");

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

        assertThat(calendarView.getCalendarViewEventsType(), equalTo(CalendarView.CalendarViewEventsType.ALL));
        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(false));
        assertThat(calendarView.isUseCustomFormats(), equalTo(true));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(false));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(true));
        assertThat(calendarView.isWeekSettingsShowWeekNumbers(), equalTo(true));
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
    void testConfigRoundtripForCustomSlotSettings(JenkinsRule j) throws Exception {
        CalendarView calendarView = createCalendarView(j, "cal_customSlotSettings");

        calendarView.setUseCustomSlotSettings(true);

        calendarView.setWeekSlotDuration("00:15:00");
        calendarView.setWeekMinTime("07:00:00");
        calendarView.setWeekMaxTime("22:00:00");

        calendarView.setDaySlotDuration("00:20:00");
        calendarView.setDayMinTime("09:00:00");
        calendarView.setDayMaxTime("18:00:00");

        j.configRoundtrip(calendarView);

        assertThat(calendarView.getCalendarViewEventsType(), equalTo(CalendarView.CalendarViewEventsType.ALL));
        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(false));
        assertThat(calendarView.isUseCustomFormats(), equalTo(false));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(true));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(true));
        assertThat(calendarView.isWeekSettingsShowWeekNumbers(), equalTo(true));
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

    @Test
    void testConfigValidation(JenkinsRule j) throws Exception {
        CalendarView calendarView = createCalendarView(j, "cal");

        HtmlPage configurePage;

        configurePage = getConfigurePage(j, calendarView);
        HtmlRadioButtonInput viewEventsTypeRadioButton = configurePage.querySelector("input[name='calendarViewEventsType'][value='BUILDS']");
        viewEventsTypeRadioButton.setValueAttribute("INVALID_VALUE");
        viewEventsTypeRadioButton.setChecked(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlRadioButtonInput viewTypeRadioButton = configurePage.querySelector("input[name='calendarViewType'][value='MONTH']");
        viewTypeRadioButton.setValueAttribute("INVALID_VALUE");
        viewTypeRadioButton.setChecked(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlOption firstDayOption = configurePage.querySelector("select[name='weekSettingsFirstDay'] option[value='0']");
        firstDayOption.setValueAttribute("8");
        firstDayOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlOption weekSlotDurationOption = configurePage.querySelector("select[name='weekSlotDuration'] option[value='00:05:00']");
        weekSlotDurationOption.setValueAttribute("00:25:00");
        weekSlotDurationOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlOption weekMinTimeOption = configurePage.querySelector("select[name='weekMinTime'] option[value='00:00:00']");
        weekMinTimeOption.setValueAttribute("10:30:00");
        weekMinTimeOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlOption weekMaxTimeOption = configurePage.querySelector("select[name='weekMaxTime'] option[value='00:00:00']");
        weekMaxTimeOption.setValueAttribute("25:00:00");
        weekMaxTimeOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlOption daySlotDurationOption = configurePage.querySelector("select[name='weekSlotDuration'] option[value='00:05:00']");
        daySlotDurationOption.setValueAttribute("00:45:00");
        daySlotDurationOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlOption dayMinTimeOption = configurePage.querySelector("select[name='dayMinTime'] option[value='00:00:00']");
        dayMinTimeOption.setValueAttribute("00:00");
        dayMinTimeOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(j, calendarView);
        HtmlOption dayMaxTimeOption = configurePage.querySelector("select[name='dayMaxTime'] option[value='00:00:00']");
        dayMaxTimeOption.setValueAttribute("24:30:00");
        dayMaxTimeOption.setSelected(true);
        testValidation(configurePage);
    }

    private static void testValidation(HtmlPage configurePage) {
        HtmlButton submitButton = configurePage.querySelector(".jenkins-button--primary");
        FailingHttpStatusCodeException ex = assertThrows(FailingHttpStatusCodeException.class, submitButton::click);
        assertThat(ex.getStatusCode(), equalTo(400));
    }

    private static HtmlPage getConfigurePage(JenkinsRule j, CalendarView calendarView) throws IOException, SAXException {
        return j.createWebClient().getPage(calendarView, "configure");
    }

    private static CalendarView createCalendarView(JenkinsRule j, String name) throws Exception {
        HtmlPage newView = j.createWebClient().goTo("newView");

        HtmlTextInput nameTextInput = newView.querySelector("input[name='name']");
        nameTextInput.setText(name);

        HtmlRadioButtonInput calendarViewRadioButton = newView.querySelector("input[value='io.jenkins.plugins.view.calendar.CalendarView']");
        calendarViewRadioButton.setChecked(true);

        HtmlButton okButton = newView.querySelector("#ok");
        okButton.click();

        return (CalendarView) j.getInstance().getView(name);
    }

    private static void assertDefaults(CalendarView calendarView) {
        assertThat(calendarView.getCalendarViewEventsType(), equalTo(CalendarView.CalendarViewEventsType.ALL));
        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(false));
        assertThat(calendarView.isUseCustomFormats(), equalTo(false));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(false));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(true));
        assertThat(calendarView.isWeekSettingsShowWeekNumbers(), equalTo(true));
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
}