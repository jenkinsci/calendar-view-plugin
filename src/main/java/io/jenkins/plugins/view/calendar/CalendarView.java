package io.jenkins.plugins.view.calendar;

import antlr.ANTLRException;
import hudson.Functions;
import hudson.model.*;
import hudson.scheduler.CronTab;
import hudson.triggers.Trigger;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.scheduler.CronTabList;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarView extends ListView {
    private static final Logger LOGGER = Logger.getLogger(CalendarView.class.getName());

    private static final String FORMAT_DATE = "yyyy-MM-dd";
    private static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss";


    public static enum CalendarViewType {
       MONTH, WEEK, DAY;
    }

    public static class Event {
        private TopLevelItem item;
        private Calendar start;
        private Calendar end;
        private EventType type;
        private String title;
        private String url;

        public Event(TopLevelItem item, Calendar start, long durationInMillis)  {
            this.item = item;
            this.type = EventType.FUTURE;
            this.title = item.getFullDisplayName();
            this.url = item.getUrl();
            this.start = start;
            initEnd(durationInMillis);
        }

        public Event(TopLevelItem item, Run build) {
            this.item = item;
            this.type = EventType.fromResult(build.getResult());
            this.title = build.getFullDisplayName();
            this.url = build.getUrl();
            this.start = Calendar.getInstance();
            this.start.setTimeInMillis(build.getStartTimeInMillis());
            initEnd(build.getDuration());
        }

        private void initEnd(long durationInMillis) {
            // duration needs to be at least 1sec otherwise
            // fullcalendar will not properly display the event
            if (durationInMillis < 1000) {
                durationInMillis = 1000;
            }
            this.end = Calendar.getInstance();
            this.end.setTime(this.start.getTime());
            this.end.add(Calendar.SECOND, (int)(durationInMillis / 1000));
        }

        public TopLevelItem getItem() {
            return this.item;
        }

        public Calendar getStart() {
            return start;
        }

        public String getStartAsISO8601() {
            return new SimpleDateFormat(FORMAT_ISO8601).format(getStart().getTime());
        }

        public Calendar getEnd() {
            return this.end;
        }

        public String getEndAsISO8601() {
            return new SimpleDateFormat(FORMAT_ISO8601).format(getEnd().getTime());
        }

        public EventType getType() {
            return this.type;
        }

        public String getTypeAsClassName() {
            return "event-" + type.name().toLowerCase();
        }

        public String getUrl() {
            return this.url;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum EventType {
        FAILURE, SUCCESS, UNSTABLE, ABORTED, NOT_BUILT, FUTURE;

        public static EventType fromResult(Result result) {
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
                return ABORTED;
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


    private CalendarViewType calendarViewType = CalendarViewType.WEEK;

    @DataBoundConstructor
    public CalendarView(String name) {
        super(name);
    }

    public CalendarViewType getCalendarViewType() {
        return calendarViewType;
    }

    public void setCalendarViewType(CalendarViewType calendarViewType) {
        this.calendarViewType = calendarViewType;
    }

    @Override
    public boolean isAutomaticRefreshEnabled() {
        return false;
    }

    @Override
    protected void submit(StaplerRequest req) throws ServletException, Descriptor.FormException, IOException {
        super.submit(req);
        setCalendarViewType(CalendarViewType.valueOf(req.getParameter("calendarViewType")));
    }

    public List<Event> getEvents() throws ParseException {
        StaplerRequest request = Stapler.getCurrentRequest();

        Calendar start = getCalendarFromRequestParameter("start");
        Calendar end = getCalendarFromRequestParameter("end");
        Calendar now = Calendar.getInstance();

        List<Event> events = new ArrayList<Event>();
        if (now.compareTo(start) < 0) {
            events.addAll(getFutureEvents(start, end));
        } else if (now.compareTo(end) > 0) {
            events.addAll(getPastEvents(start, end));
        } else {
            events.addAll(getPastEvents(start, now));
            events.addAll(getFutureEvents(now, end));
        }
        return events;
    }

    private List<Event> getFutureEvents(Calendar start, Calendar end) {
        List<Event> events = new ArrayList<Event>();
        for (TopLevelItem item: getItems()) {
            if (!(item instanceof AbstractProject)) {
               continue;
            }
            long durationInMillis = ((AbstractProject)item).getEstimatedDuration();
            List<Trigger> triggers = getCronTriggers(item);
            for (Trigger trigger: triggers) {
                List<CronTab> cronTabs = getCronTabs(trigger);
                for (CronTab cronTab: cronTabs) {
                    long timeInMillis = start.getTimeInMillis();
                    Calendar next;
                    while ((next = cronTab.ceil(timeInMillis)) != null && next.compareTo(start) >= 0 && next.compareTo(end) < 0) {
                        events.add(new Event(item, next, durationInMillis));
                        timeInMillis = next.getTimeInMillis() + 1000 * 60;
                    }
                }
            }
        }
        return events;
    }

    private List<Event> getPastEvents(Calendar start, Calendar end) {
        List<Event> events = new ArrayList<Event>();
        for (TopLevelItem item: getItems()) {
            if (!(item instanceof Job)) {
                continue;
            }
            RunList<Run> builds = ((Job) item).getBuilds();
            for (Run build : builds) {
                Event event = new Event(item, build);
                if (event.getStart().compareTo(start) >= 0 && event.getEnd().compareTo(end) < 0) {
                    events.add(event);
                }
            }
        }
        return events;
    }

    private Calendar getCalendarFromRequestParameter(String param) throws ParseException {
        StaplerRequest request = Stapler.getCurrentRequest();
        String date = request.getParameter(param);
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE);

        Calendar cal = Calendar.getInstance();
        cal.setTime(format.parse(date));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        return cal;
    }

    private List<CronTab> getCronTabs(Trigger trigger) {
        List<CronTab> cronTabs = new ArrayList<>();
        int lineNumber = 0;
        String timezone = null;

        for (String line : trigger.getSpec().split("\\r?\\n")) {
            lineNumber++;
            line = line.trim();

            if (lineNumber == 1 && line.startsWith("TZ=")) {
                timezone = CronTabList.getValidTimezone(line.replace("TZ=",""));
                continue;
            }

            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }

            try {
                cronTabs.add(new CronTab(line, lineNumber, null, timezone));
            } catch (ANTLRException e) {
                String msg = "Unable to parse cron trigger spec: '" + line + "'";
                Logger.getLogger(this.getClass()).error(msg, e);
            }
        }

        return cronTabs;
    }



    public List<TopLevelItem> getCronJobs(ItemGroup root) {
        List<TopLevelItem> scheduledJobs = new ArrayList<TopLevelItem>();
        for (TopLevelItem item: Functions.getAllTopLevelItems(root)) {
           if (!getCronTriggers(item).isEmpty()) {
               scheduledJobs.add(item);
           }
        }
        return scheduledJobs;
    }

    private List<Trigger> getCronTriggers(TopLevelItem item) {
        List<Trigger> triggers = new ArrayList<Trigger>();
        if (!(item instanceof AbstractProject)) {
            return triggers;
        }
        Collection<Trigger<?>> itemTriggers = ((AbstractProject) item).getTriggers().values();
        for (Trigger<?> trigger: itemTriggers) {
            if (StringUtils.isNotBlank(trigger.getSpec())) {
                triggers.add(trigger);
            }
        }
        return triggers;
    }


    public String jsonEscape(String text) {
        return StringEscapeUtils.escapeJavaScript(text);
    }

    public String getRootUrl() {
       return Jenkins.getInstance().getRootUrl();
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.CalendarView_DisplayName();
        }
    }
}
