package io.jenkins.plugins.view.calendar.util;

import hudson.model.Descriptor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private ValidationUtil() { }

    public static void validateInList(final StaplerRequest req, final String formfield, final List<String> possibleValues) throws Descriptor.FormException {
        if (!possibleValues.contains(req.getParameter(formfield))) {
            throw new Descriptor.FormException(formfield + " must be one of " + possibleValues, formfield);
        }
    }

    public static void validatePattern(final StaplerRequest req, final String formField, final Pattern pattern) throws Descriptor.FormException {
        final String value = req.getParameter(formField);
        if (value == null || !pattern.matcher(value).matches()) {
            throw new Descriptor.FormException(formField + " must match " + pattern, formField);
        }
    }

    public static void validateRange(final StaplerRequest req, final String formField, final int min, final int max) throws Descriptor.FormException {
        final int value = Integer.parseInt(req.getParameter("weekSettingsFirstDay"));
        if (value < min || value > max) {
            throw new Descriptor.FormException(formField + " must be: " + min + " <= " + formField + " <= " + max, formField);
        }
    }
}
