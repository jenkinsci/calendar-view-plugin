package io.jenkins.plugins.view.calendar;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class CalendarViewTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

    private void testValidation(HtmlPage configurePage) {
        HtmlButton submitButton = configurePage.querySelector(".submit-button button");
        try {
            submitButton.click();
        } catch (Exception e) {
            assertThat(e, instanceOf(FailingHttpStatusCodeException.class));
            assertThat(((FailingHttpStatusCodeException)e).getStatusCode(), equalTo(400));
            return;
        }
        fail("No Exception thrown");
    }

    private HtmlPage getConfigurePage(CalendarView calendarView) throws IOException, SAXException {
        return j.createWebClient().getPage(calendarView, "configure");
    }

    @Test
    public void testConfigValidation() throws Exception {
        CalendarView calendarView = createCalendarView("cal");

        HtmlPage configurePage;

        configurePage = getConfigurePage(calendarView);
        HtmlRadioButtonInput viewTypeRadioButton = configurePage.querySelector("input[name='calendarViewType'][value='MONTH']");
        viewTypeRadioButton.setValueAttribute("INVALID_VALUE");
        viewTypeRadioButton.setChecked(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(calendarView);
        HtmlOption firstDayOption = configurePage.querySelector("select[name='weekSettingsFirstDay'] option[value='0']");
        firstDayOption.setValueAttribute("8");
        firstDayOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(calendarView);
        HtmlOption weekSlotDurationOption = configurePage.querySelector("select[name='weekSlotDuration'] option[value='00:05:00']");
        weekSlotDurationOption.setValueAttribute("00:25:00");
        weekSlotDurationOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(calendarView);
        HtmlOption weekMinTimeOption = configurePage.querySelector("select[name='weekMinTime'] option[value='00:00:00']");
        weekMinTimeOption.setValueAttribute("10:30:00");
        weekMinTimeOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(calendarView);
        HtmlOption weekMaxTimeOption = configurePage.querySelector("select[name='weekMaxTime'] option[value='00:00:00']");
        weekMaxTimeOption.setValueAttribute("25:00:00");
        weekMaxTimeOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(calendarView);
        HtmlOption daySlotDurationOption = configurePage.querySelector("select[name='weekSlotDuration'] option[value='00:05:00']");
        daySlotDurationOption.setValueAttribute("00:45:00");
        daySlotDurationOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(calendarView);
        HtmlOption dayMinTimeOption = configurePage.querySelector("select[name='dayMinTime'] option[value='00:00:00']");
        dayMinTimeOption.setValueAttribute("00:00");
        dayMinTimeOption.setSelected(true);
        testValidation(configurePage);

        configurePage = getConfigurePage(calendarView);
        HtmlOption dayMaxTimeOption = configurePage.querySelector("select[name='dayMaxTime'] option[value='00:00:00']");
        dayMaxTimeOption.setValueAttribute("24:30:00");
        dayMaxTimeOption.setSelected(true);
        testValidation(configurePage);
    }
}