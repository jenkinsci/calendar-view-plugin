package io.jenkins.plugins.view.calendar.util;

import hudson.Plugin;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public final class PluginUtil {
    private static Jenkins jenkins;

    private PluginUtil() { }

    public static void setJenkins(final Jenkins jenkins) {
       PluginUtil.jenkins = jenkins;
    }

    private static Jenkins getJenkins() {
        if (jenkins != null) {
            return jenkins;
        }
        return Jenkins.getInstance();
    }

    public static boolean hasPluginInstalled(final String pluginName) {
        final Plugin plugin = getJenkins().getPlugin(pluginName);
        return (plugin == null) ? false : true;
    }

    public static boolean hasGreenballsPluginInstalled() {
        return hasPluginInstalled("greenballs");
    }

    public static boolean hasWorkflowJobPluginInstalled() {
        return hasPluginInstalled("workflow-job");
    }
}
