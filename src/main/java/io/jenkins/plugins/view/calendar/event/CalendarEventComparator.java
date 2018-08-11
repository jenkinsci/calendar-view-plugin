package io.jenkins.plugins.view.calendar.event;

import java.io.Serializable;
import java.util.Comparator;

public class CalendarEventComparator implements Comparator<CalendarEvent>, Serializable {

    private static final long serialVersionUID = -3188090417065119856L;

    @Override
    public int compare(final CalendarEvent e1, final CalendarEvent e2) {
        int c = e1.getStart().compareTo(e2.getStart());
        if (c == 0) {
            c = e1.getEnd().compareTo(e2.getEnd());
        }
        return c;
    }
}
