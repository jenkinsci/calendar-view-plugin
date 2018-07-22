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

import hudson.model.Result;

public enum CalendarEventType {
    FAILURE, SUCCESS, UNSTABLE, ABORTED, NOT_BUILT, FUTURE;

    public static CalendarEventType fromResult(final Result result) {
        if (result == null) {
            return null;
        }
        if (result.equals(Result.SUCCESS)) {
            return SUCCESS;
        }
        if (result.equals(Result.FAILURE)) {
            return FAILURE;
        }
        if (result.equals(Result.UNSTABLE)) {
            return UNSTABLE;
        }
        if (result.equals(Result.NOT_BUILT)) {
            return NOT_BUILT;
        }
        if (result.equals(Result.ABORTED)) {
            return ABORTED;
        }
        return null;
    }
}
