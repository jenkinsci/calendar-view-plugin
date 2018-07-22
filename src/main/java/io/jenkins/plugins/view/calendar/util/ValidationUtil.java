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
