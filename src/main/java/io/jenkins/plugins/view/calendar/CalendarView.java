package io.jenkins.plugins.view.calendar;

import antlr.ANTLRException;
import hudson.Functions;
import hudson.model.*;
import hudson.scheduler.CronTab;
import hudson.triggers.Trigger;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
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

        public Event(TopLevelItem item, Calendar start)  {
            this.item = item;
            this.start = start;
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
            Calendar end = Calendar.getInstance();
            end.setTime(start.getTime());
            end.add(Calendar.HOUR, 1);
            return end;
        }

        public String getEndAsISO8601() {
            return new SimpleDateFormat(FORMAT_ISO8601).format(getEnd().getTime());
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

        List<Event> events = new ArrayList<Event>();
        for (TopLevelItem item: getItems()) {
            List<Trigger> triggers = getCronTriggers(item);
            for (Trigger trigger: triggers) {
                List<CronTab> cronTabs = getCronTabs(trigger);
                for (CronTab cronTab: cronTabs) {
                    long time = start.getTimeInMillis();
                    Calendar next;
                    while ((next = cronTab.ceil(time)) != null && next.compareTo(start) >= 0 && next.compareTo(end) < 0) {
                        events.add(new Event(item, next));
                        time = next.getTimeInMillis() + 1000 * 60;
                    }
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
        if (item instanceof FreeStyleProject) {
            for (Trigger<?> trigger : ((FreeStyleProject) item).getTriggers().values()) {
                if (StringUtils.isNotBlank(trigger.getSpec())) {
                    triggers.add(trigger);
                }
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
