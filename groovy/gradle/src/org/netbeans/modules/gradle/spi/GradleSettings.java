/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.spi;

import java.io.File;
import java.util.prefs.Preferences;
import org.gradle.util.GradleVersion;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine.LogLevel;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine.StackTrace;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 *
 * @author Laszlo Kishalmi
 */
@Messages({
    "DL_NEVER=Never",
    "DL_AS_NEEDED=Only if Required",
    "DL_ALWAYS=Always"
})
public final class GradleSettings {

    public enum DownloadLibsRule {
        NEVER,
        AS_NEEDED,
        ALWAYS;

        @Override
        public String toString() {
            switch (this) {
                case ALWAYS: return Bundle.DL_ALWAYS();
                case NEVER: return Bundle.DL_NEVER();
                case AS_NEEDED: return Bundle.DL_AS_NEEDED();
            }
            return name();
        }
    }

    public enum DownloadMiscRule {
        NEVER,
        ALWAYS;

        @Override
        public String toString() {
            switch (this) {
                case ALWAYS: return Bundle.DL_ALWAYS();
                case NEVER: return Bundle.DL_NEVER();
            }
            return name();
        }
    }

    public static final String PROP_GRADLE_DISTRIBUTION = "gradleHome";

    public static final String PROP_PREFER_WRAPPER = "preferWrapper";
    public static final String PROP_GRADLE_USER_HOME = "gradleUserHome";
    public static final String PROP_START_DAEMON_ON_START = "startDaemonOnStart";
    public static final String PROP_REUSE_OUTPUT_TABS = "reuseOutputTabs";
    public static final String PROP_USE_CUSTOM_GRADLE = "useCustomGradle";
    public static final String PROP_GRADLE_VERSION = "gradleVersion";
    public static final String PROP_SILENT_INSTALL = "silentInstall";

    public static final String PROP_OPT_OFFLINE = "offline";
    public static final String PROP_OPT_NO_REBUILD = "noRebuild";
    public static final String PROP_OPT_CONFIGURE_ON_DEMAND = "configureOnDemand";

    public static final String PROP_SKIP_TEST = "skipTest";
    public static final String PROP_SKIP_CHECK = "skipCheck";

    public static final String PROP_LOG_LEVEL = "logLevel";
    public static final String PROP_STACKTRACE = "stacktrace";

    public static final String PROP_HIDE_EMPTY_CONF = "hideEmptyConfiguration";

    public static final String PROP_ALWAYS_SHOW_OUTPUT = "alwaysShowOutput";
    public static final String PROP_DISPLAY_DESCRIPTION = "displayDescription";
    public static final String PROP_REUSE_EDITOR_ON_STACKTRACE = "reuseEditorOnStackTace";

    public static final String PROP_DISABLE_CACHE = "disableCache";
    public static final String PROP_LAZY_OPEN_GROUPS = "lazyOpen";
    public static final String PROP_PREFER_MAVEN = "preferMaven";

    public static final String PROP_DOWNLOAD_LIBS = "downloadLibs";
    public static final String PROP_DOWNLOAD_SOURCES = "downloadSources";
    public static final String PROP_DOWNLOAD_JAVADOC = "downloadJavaDoc";

    private static final GradleSettings INSTANCE = new GradleSettings();

    public Preferences getPreferences() {
        return NbPreferences.forModule(GradleSettings.class);
    }

    public static GradleSettings getDefault() {
        return INSTANCE;
    }

    public String getDistributionHome() {
        return getPreferences().get(PROP_GRADLE_DISTRIBUTION, "");
    }

    public void setDistributionHome(String gradleHome) {
        getPreferences().put(PROP_GRADLE_DISTRIBUTION, gradleHome);
    }

    public boolean isWrapperPreferred() {
        return getPreferences().getBoolean(PROP_PREFER_WRAPPER, true);
    }

    public void setWrapperPreferred(boolean b) {
        getPreferences().putBoolean(PROP_PREFER_WRAPPER, b);
    }

    public void setGradleUserHome(File dir) {
        if (dir != null) {
            getPreferences().put(PROP_GRADLE_USER_HOME, dir.getAbsolutePath());
        } else {
            getPreferences().remove(PROP_GRADLE_USER_HOME);
        }
    }

    public File getGradleUserHome() {
        String dir = getPreferences().get(PROP_GRADLE_USER_HOME, System.getenv("GRADLE_USER_HOME")); //NOI18N
        return dir != null ? new File(dir) : new File(System.getProperty("user.home"), ".gradle"); //NOI18N
    }

    public void setSilentInstall(boolean b) {
        getPreferences().putBoolean(PROP_SILENT_INSTALL, b);
    }

    public boolean isSilentInstall() {
        return getPreferences().getBoolean(PROP_SILENT_INSTALL, false);
    }

    public void setReuseOutputTabs(boolean b) {
        getPreferences().putBoolean(PROP_REUSE_OUTPUT_TABS, b);
    }

    public boolean isReuseOutputTabs() {
        return getPreferences().getBoolean(PROP_REUSE_OUTPUT_TABS, true);
    }

    public void setOffline(boolean b) {
        getPreferences().putBoolean(PROP_OPT_OFFLINE, b);
    }

    public boolean isOffline() {
        return getPreferences().getBoolean(PROP_OPT_OFFLINE, false);
    }

    public void setAlwaysShowOutput(boolean b) {
        getPreferences().putBoolean(PROP_ALWAYS_SHOW_OUTPUT, b);
    }

    public boolean isAlwaysShowOutput() {
        return getPreferences().getBoolean(PROP_ALWAYS_SHOW_OUTPUT, true);
    }

    public void setStartDaemonOnStart(boolean b) {
        getPreferences().putBoolean(PROP_START_DAEMON_ON_START, b);
    }

    public boolean isStartDaemonOnStart() {
        return getPreferences().getBoolean(PROP_START_DAEMON_ON_START, false);
    }

    public void setUseCustomGradle(boolean b) {
        getPreferences().putBoolean(PROP_USE_CUSTOM_GRADLE, b);
    }

    public boolean useCustomGradle() {
        return getPreferences().getBoolean(PROP_USE_CUSTOM_GRADLE, false);
    }

    public void setSkipTest(boolean b) {
        getPreferences().putBoolean(PROP_SKIP_TEST, b);
    }

    public boolean skipTest() {
        return getPreferences().getBoolean(PROP_SKIP_TEST, false);
    }

    public void setSkipCheck(boolean b) {
        getPreferences().putBoolean(PROP_SKIP_CHECK, b);
    }

    public boolean skipCheck() {
        return getPreferences().getBoolean(PROP_SKIP_CHECK, true);
    }

    public void setGradleVersion(String version) {
        getPreferences().put(PROP_GRADLE_VERSION, version);
    }

    public String getGradleVersion() {
        return getPreferences().get(PROP_GRADLE_VERSION, GradleVersion.current().getVersion());
    }

    public void setNoRebuild(boolean b) {
        getPreferences().putBoolean(PROP_OPT_NO_REBUILD, b);
    }

    public boolean getNoRebuild() {
        return getPreferences().getBoolean(PROP_OPT_NO_REBUILD, false);
    }

    public void setConfigureOnDemand(boolean b) {
        getPreferences().putBoolean(PROP_OPT_CONFIGURE_ON_DEMAND, b);
    }

    public boolean isConfigureOnDemand() {
        return getPreferences().getBoolean(PROP_OPT_CONFIGURE_ON_DEMAND, true);
    }

    public void setDefaultLogLevel(LogLevel level) {
        getPreferences().put(PROP_LOG_LEVEL, level.name());
    }

    public LogLevel getDefaultLogLevel() {
        String lvl = getPreferences().get(PROP_LOG_LEVEL, LogLevel.LIFECYCLE.name());
        return LogLevel.valueOf(lvl);
    }

    public void setDefaultStackTrace(StackTrace st) {
        getPreferences().put(PROP_STACKTRACE, st.name());
    }

    public StackTrace getDefaultStackTrace() {
        String st = getPreferences().get(PROP_STACKTRACE, StackTrace.NONE.name());
        return StackTrace.valueOf(st);
    }

    public void setHideEmptyConfigurations(boolean b) {
        getPreferences().putBoolean(PROP_HIDE_EMPTY_CONF, b);
    }

    public boolean isHideEmptyConfigurations() {
        return getPreferences().getBoolean(PROP_HIDE_EMPTY_CONF, true);
    }

    public void setDisplayDescription(boolean b) {
        getPreferences().putBoolean(PROP_DISPLAY_DESCRIPTION, b);
    }

    public boolean isDisplayDesctiption() {
        return getPreferences().getBoolean(PROP_DISPLAY_DESCRIPTION, true);
    }

    public void setReuseEditorOnStackTrace(boolean b) {
        getPreferences().putBoolean(PROP_REUSE_EDITOR_ON_STACKTRACE, b);
    }

    public boolean isReuseEditorOnStackTace() {
        return getPreferences().getBoolean(PROP_REUSE_EDITOR_ON_STACKTRACE, false);
    }

    public void setOpenLazy(boolean b) {
        getPreferences().putBoolean(PROP_LAZY_OPEN_GROUPS, b);
    }

    public boolean isOpenLazy() {
        return getPreferences().getBoolean(PROP_LAZY_OPEN_GROUPS, false);
    }

    public void setCacheDisabled(boolean b) {
        getPreferences().putBoolean(PROP_DISABLE_CACHE, b);
    }

    public boolean isCacheDisabled() {
        return getPreferences().getBoolean(PROP_DISABLE_CACHE, false);
    }

    public void setPreferMaven(boolean b) {
        getPreferences().putBoolean(PROP_PREFER_MAVEN, b);
    }

    public boolean isPreferMaven() {
        return getPreferences().getBoolean(PROP_PREFER_MAVEN, false);
    }

    public void setDownloadLibs(DownloadLibsRule rule) {
        getPreferences().put(PROP_DOWNLOAD_LIBS, rule.name());
    }

    public DownloadLibsRule getDownloadLibs() {
        String ruleName = getPreferences().get(PROP_DOWNLOAD_LIBS, DownloadLibsRule.AS_NEEDED.name());
        return DownloadLibsRule.valueOf(ruleName);
    }

    public void setDownloadSources(DownloadMiscRule rule) {
        getPreferences().put(PROP_DOWNLOAD_SOURCES, rule.name());
    }

    public DownloadMiscRule getDownloadSources() {
        String ruleName = getPreferences().get(PROP_DOWNLOAD_SOURCES, DownloadMiscRule.ALWAYS.name());
        return DownloadMiscRule.valueOf(ruleName);
    }

    public void setDownloadJavadoc(DownloadMiscRule rule) {
        getPreferences().put(PROP_DOWNLOAD_JAVADOC, rule.name());
    }

    public DownloadMiscRule getDownloadJavadoc() {
        String ruleName = getPreferences().get(PROP_DOWNLOAD_JAVADOC, DownloadMiscRule.NEVER.name());
        return DownloadMiscRule.valueOf(ruleName);
    }
}
