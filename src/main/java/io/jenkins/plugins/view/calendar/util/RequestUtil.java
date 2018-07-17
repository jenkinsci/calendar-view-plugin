package io.jenkins.plugins.view.calendar.util;

import org.kohsuke.stapler.StaplerRequest;

import java.text.ParseException;
import java.util.Calendar;

public final class RequestUtil {

    private RequestUtil() {}

    public static Calendar getParamAsCalendar(final StaplerRequest req, final String param) throws ParseException {
        final String dateString = req.getParameter(param);

        final Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtil.parseDate(dateString));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        return cal;
    }
}
