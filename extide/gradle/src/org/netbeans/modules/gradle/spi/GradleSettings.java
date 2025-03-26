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
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.gradle.util.GradleVersion;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine.LogLevel;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine.StackTrace;
import org.openide.util.NbBundle;
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

    private static final Logger LOG = Logger.getLogger(GradleSettings.class.getName());
    
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

    /**
     * Rule, that determines when to allow automatic Gradle execution.
     * @since 2.2
     */
    @Messages({
        "GE_TRUSTED_PROJECTS=Trusted Projects Only",
        "GE_ALWAYS=Always"
    })
    public enum GradleExecutionRule {
        TRUSTED_PROJECTS,
        ALWAYS;

        @Override
        public String toString() {
            switch (this) {
                case TRUSTED_PROJECTS: return Bundle.GE_TRUSTED_PROJECTS();
                case ALWAYS: return Bundle.GE_ALWAYS();
            }
            return name();
        }

    }

    public static final String PROP_GRADLE_DISTRIBUTION = "gradleHome";

    public static final String PROP_PREFER_WRAPPER = "preferWrapper";
    public static final String PROP_GRADLE_USER_HOME = "gradleUserHome";
    @Deprecated
    public static final String PROP_START_DAEMON_ON_START = "startDaemonOnStart";
    public static final String PROP_REUSE_OUTPUT_TABS = "reuseOutputTabs";
    public static final String PROP_USE_CUSTOM_GRADLE = "useCustomGradle";
    @Deprecated
    public static final String PROP_GRADLE_VERSION = "gradleVersion";
    @Deprecated
    public static final String PROP_SILENT_INSTALL = "silentInstall";

    public static final String PROP_OPT_OFFLINE = "offline";
    @Deprecated
    public static final String PROP_OPT_NO_REBUILD = "noRebuild";
    public static final String PROP_OPT_USE_CONFIG_CACHE = "useConfigCache";
    public static final String PROP_OPT_CONFIGURE_ON_DEMAND = "configureOnDemand";

    public static final String PROP_SKIP_TEST = "skipTest";
    public static final String PROP_SKIP_CHECK = "skipCheck";

    public static final String PROP_LOG_LEVEL = "logLevel";
    public static final String PROP_STACKTRACE = "stacktrace";

    public static final String PROP_HIDE_EMPTY_CONF = "hideEmptyConfiguration";

    public static final String PROP_ALWAYS_SHOW_OUTPUT = "alwaysShowOutput";
    public static final String PROP_DISPLAY_DESCRIPTION = "displayDescription";
    public static final String PROP_REUSE_EDITOR_ON_STACKTRACE = "reuseEditorOnStackTace";

    @Deprecated
    public static final String PROP_DISABLE_CACHE = "disableCache";
    @Deprecated
    public static final String PROP_LAZY_OPEN_GROUPS = "lazyOpen";
    public static final String PROP_PREFER_MAVEN = "preferMaven";

    public static final String PROP_DOWNLOAD_LIBS = "downloadLibs";
    public static final String PROP_DOWNLOAD_SOURCES = "downloadSources";
    public static final String PROP_DOWNLOAD_JAVADOC = "downloadJavaDoc";

    public static final String PROP_GRADLE_EXEC_RULE = "gradleExecutionRule";

    private static final GradleSettings INSTANCE = new GradleSettings(NbPreferences.forModule(GradleSettings.class));

    private final Preferences preferences;

    @Deprecated
    public GradleSettings() {
        this(NbPreferences.forModule(GradleSettings.class));
    }

    GradleSettings(Preferences preferences) {
        this.preferences = preferences;
    }


    public Preferences getPreferences() {
        return preferences;
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

    /**
     * Not in use.
     * @param b
     * @deprecated
     */
    @Deprecated
    public void setSilentInstall(boolean b) {
        LOG.warning("silentInstall setting is deprecated. This setter has no effect.");
    }

    /**
     * Not in use, returns {@code false}.
     * 
     * @return
     * @deprecated
     */
    @Deprecated
    public boolean isSilentInstall() {
        return false;
    }

    public void setReuseOutputTabs(boolean b) {
        getPreferences().putBoolean(PROP_REUSE_OUTPUT_TABS, b);
    }

    @NbBundle.Messages({
        "#reuse output tabs: true, false, never",
        "#NOI18N",
        "DEFAULT_REUSE_OUTPUT=true"
    })
    public boolean isReuseOutputTabs() {
        String def = Bundle.DEFAULT_REUSE_OUTPUT();
        if ("never".equals(def)) { // NOI18N
            return false;
        }
        return getPreferences().getBoolean(PROP_REUSE_OUTPUT_TABS, "true".equals(def)); // NOI18N
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

    /**
     * Not in used.
     * @param b
     * @deprecated
     */
    @Deprecated
    public void setStartDaemonOnStart(boolean b) {
        LOG.warning("startDaemonOnStart setting is deprecated. This setter has no effect.");
    }

    /**
     * Not used, returns {@code false}
     * @return
     * @deprecated
     */
    @Deprecated
    public boolean isStartDaemonOnStart() {
        return false;
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

    @Deprecated
    public void setGradleVersion(String version) {
        LOG.warning("gradleVersion setting is deprecated. This setter has no effect.");
    }

    @Deprecated
    /**
     * Returns the Gradle Version of the Tooling API bundled with the IDE.
     * @returns the Gradle Version of the Tooling API bundled with the IDE.
     */
    public String getGradleVersion() {
        return GradleVersion.current().getVersion();
    }

    /**
     * Gradle removed this option in version 8.0. NetBeans is going to remove
     * the UI option in NetBeans 20, beyond that point this option would be
     * available to keep binary compatibility, but would be un-effective.
     * 
     * @param b
     * @deprecated Not in use, since version 2.36 (NB20)
     */
    @Deprecated
    public void setNoRebuild(boolean b) {
        LOG.warning("noRebuild setting is deprecated. This setter has no effect.");
    }

    /**
     * Gradle removed this option in version 8.0. NetBeans is going to remove
     * the UI option in NetBeans 20, beyond that point this option would be
     * available to keep binary compatibility, but would return {@code false}.
     * 
     * @return whether the {@code --no-rebuild} command line option should be set by default.
     * @deprecated Always returns {@code false}, since version 2.36 (NB20)
     */
    @Deprecated
    public boolean getNoRebuild() {
        return false;
    }

    public void setUseConfigCache(boolean b) {
        getPreferences().putBoolean(PROP_OPT_USE_CONFIG_CACHE, b);        
    }
    
    public boolean getUseConfigCache() {
        return getPreferences().getBoolean(PROP_OPT_USE_CONFIG_CACHE, false);        
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

    /**
     * This experimental setting shouldn't have been exposed to the SPI. The
     * method remains here for binary compatibility, but it won't have any
     * effect.
     *
     * @param b
     * @deprecated since version 2.7
     */
    @Deprecated
    public void setOpenLazy(boolean b) {
    }

    /**
     * This experimental setting shouldn't have been exposed to the SPI. The
     * method remains here for binary compatibility, returning its former
     * default value.
     *
     * @return <code>true</code>
     * @deprecated since version 2.7
     */
    @Deprecated
    public boolean isOpenLazy() {
        return true;
    }

    /**
     * This experimental setting shouldn't have been exposed to the SPI. The
     * method remains here for binary compatibility, but it won't have any
     * effect.
     *
     * @param b
     * @deprecated since version 2.7
     */
    @Deprecated
    public void setCacheDisabled(boolean b) {
    }

    /**
     * This experimental setting shouldn't have been exposed to the SPI. The
     * method remains here for binary compatibility, returning its former
     * default value.
     *
     * @return <code>false</code>
     * @deprecated since version 2.7
     */
    @Deprecated
    public boolean isCacheDisabled() {
        return false;
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

    /**
     * @since 2.2
     */
    public void setGradleExecutionRule(GradleExecutionRule rule) {
        getPreferences().put(PROP_GRADLE_EXEC_RULE, rule.name());
    }

    /**
     * @since 2.2
     */
    public GradleExecutionRule getGradleExecutionRule() {
        String ruleName = getPreferences().get(PROP_GRADLE_EXEC_RULE, GradleExecutionRule.TRUSTED_PROJECTS.name());
        return GradleExecutionRule.valueOf(ruleName);
    }
}
