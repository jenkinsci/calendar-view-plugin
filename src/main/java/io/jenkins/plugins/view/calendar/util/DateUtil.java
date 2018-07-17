package io.jenkins.plugins.view.calendar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil {
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";

    private DateUtil() { }

    @SuppressWarnings("PMD.SimpleDateFormatNeedsLocale")
    public static String formatDateTime(final Calendar cal)  {
        return new SimpleDateFormat(FORMAT_DATETIME).format(cal.getTime());
    }

    @SuppressWarnings("PMD.SimpleDateFormatNeedsLocale")
    public static Date parseDate(final String dateString) throws ParseException {
        return new SimpleDateFormat(FORMAT_DATE).parse(dateString);
    }

}
