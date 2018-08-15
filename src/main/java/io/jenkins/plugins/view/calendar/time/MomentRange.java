package io.jenkins.plugins.view.calendar.time;

import java.util.Calendar;

public class MomentRange {
    private final Moment start;
    private final Moment end;

    public MomentRange(final Calendar start, final Calendar end) {
        this(new Moment(start), new Moment(end));
    }

    public MomentRange(final Moment start, final Moment end) {
        if (!isValidRange(start, end)) {
            throw new IllegalArgumentException("start has to be before end");
        }
        this.start = start;
        this.end = end;
    }

    public Moment getStart() {
        return start;
    }

    public Moment getEnd() {
        return end;
    }

    public static MomentRange range(final Calendar start, final Calendar end) {
        return new MomentRange(start, end);
    }

    public static MomentRange range(final Moment start, final Moment end) {
        return new MomentRange(start, end);
    }

    public static boolean isValidRange(final Moment m1, final Moment m2) {
        return m1.isBefore(m2);
    }

    public static boolean isValidRange(final Calendar c1, final Calendar c2) {
        return c1.before(c2);
    }

    @Override
    public String toString() {
        return start + " - " + end;
    }
}
