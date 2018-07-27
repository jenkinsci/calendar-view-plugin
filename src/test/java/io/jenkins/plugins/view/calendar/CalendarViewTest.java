package io.jenkins.plugins.view.calendar;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.apache.commons.io.FileUtils;
import hudson.model.*;
import hudson.tasks.Shell;
import org.junit.Test;
import org.junit.Rule;
import org.xml.sax.SAXException;

import java.io.IOException;

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

        return (CalendarView)j.getInstance().getView(name);
    }

    @Test
    public void testConfigRoundtripDefaults() throws Exception {
        CalendarView calendarView = createCalendarView("cal1");

        j.configRoundtrip(calendarView);

        assertThat(calendarView.getCalendarViewType(), equalTo(CalendarView.CalendarViewType.WEEK));

        assertThat(calendarView.isUseCustomWeekSettings(), equalTo(false));
        assertThat(calendarView.isUseCustomFormats(), equalTo(false));
        assertThat(calendarView.isUseCustomSlotSettings(), equalTo(false));

        assertThat(calendarView.isWeekSettingsShowWeekends(), equalTo(true));
        assertThat(calendarView.getWeekSettingsFirstDay(), equalTo(1));

        assertThat(calendarView.getMonthTimeFormat(), equalTo(""));
        assertThat(calendarView.getMonthColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getMonthTimeFormat(), equalTo(""));
        assertThat(calendarView.getMonthPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getWeekTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekColumnHeaderFormat(), equalTo(""));
        assertThat(calendarView.getWeekTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekSlotTimeFormat(), equalTo(""));
        assertThat(calendarView.getWeekPopupBuildTimeFormat(), equalTo(""));

        assertThat(calendarView.getDayTimeFormat(), equalTo(""));
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
