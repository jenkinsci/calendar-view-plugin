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
