package io.jenkins.plugins.view.calendar.util;

public final class FieldUtil {
    private FieldUtil() {}

    public static <T> T defaultIfNull(final T value, final T defaultValue) {
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
}
