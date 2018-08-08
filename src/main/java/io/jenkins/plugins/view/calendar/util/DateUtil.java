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

    public static Calendar roundToNextMinute(final Calendar cal) {
        final Calendar clone = (Calendar)cal.clone();
        clone.set(Calendar.SECOND, 0);
        clone.set(Calendar.MILLISECOND, 0);
        clone.add(Calendar.MINUTE, 1);
        return clone;
    }

    public static Calendar roundToPreviousMinute(final Calendar cal) {
        final Calendar clone = (Calendar)cal.clone();
        clone.set(Calendar.SECOND, 0);
        clone.set(Calendar.MILLISECOND, 0);
        clone.add(Calendar.MINUTE, -1);
        return clone;
    }
}
