package io.jenkins.plugins.view.calendar.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarUtil {

    public static Calendar cal(String date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(date));
        return cal;
    }

    public static String str(Calendar cal) throws ParseException {
        if (cal == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(cal.getTime());
    }
}
