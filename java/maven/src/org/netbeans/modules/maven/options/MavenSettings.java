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

package org.netbeans.modules.maven.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;

/**
 * a netbeans settings for global options that cannot be put into the settings file.
 * @author mkleint
 */
public final class MavenSettings  {
    //same prop constant in Embedderfactory.java    
    private static final String PROP_DEFAULT_OPTIONS = "defaultOptions"; // NOI18N
    private static final String PROP_SOURCE_DOWNLOAD = "sourceDownload"; //NOI18N
    private static final String PROP_JAVADOC_DOWNLOAD = "javadocDownload"; //NOI18N
    private static final String PROP_BINARY_DOWNLOAD = "binaryDownload"; //NOI18N
    private static final String PROP_LAST_ARCHETYPE_GROUPID = "lastArchetypeGroupId"; //NOI18N
    private static final String PROP_LAST_ARCHETYPE_VERSION = "lastArchetypeVersion"; //NOI18N
    private static final String PROP_SKIP_TESTS = "skipTests"; //NOI18N
    private static final String PROP_MAVEN_RUNTIMES = "mavenRuntimes"; //NOI18N
    public static final String PROP_PROJECTNODE_NAME_PATTERN = "project.displayName"; //NOI18N
    private static final String PROP_ALWAYS_OUTPUT = "alwaysShowOutput";
    private static final String PROP_SHOW_LOGGING_LEVEL = "showLoggingLevel"; //NOI18N
    private static final String PROP_REUSE_OUTPUT = "reuseOutputTabs";
    private static final String PROP_COLLAPSE_FOLDS = "collapseSuccessFolds";
    private static final String PROP_OUTPUT_TAB_CONFIG = "showConfigInOutputTab";
    private static final String PROP_OUTPUT_TAB_NAME = "showOutputTabAs";
    private static final String PROP_EXPERIMENTAL_USE_BEST_MAVEN = "useBestMaven";
    private static final String PROP_EXPERIMENTAL_USE_ALTERNATE_LOCATION = "useBestMavenAltLocation";
    private static final String PROP_EXPERIMENTAL_ALTERNATE_LOCATION = "bestMavenAltLocation";
    private static final String PROP_VM_OPTIONS_WRAP = "vmOptionsWrap";
    private static final String PROP_DEFAULT_JDK = "defaultJdk";

    //these are from former versions (6.5) and are here only for conversion
    private static final String PROP_DEBUG = "showDebug"; // NOI18N
    private static final String PROP_ERRORS = "showErrors"; //NOI18N
    private static final String PROP_CHECKSUM_POLICY = "checksumPolicy"; //NOI18N
    private static final String PROP_PLUGIN_POLICY = "pluginUpdatePolicy"; //NOI18N
    private static final String PROP_FAILURE_BEHAVIOUR = "failureBehaviour"; //NOI18N
    private static final String PROP_USE_REGISTRY = "usePluginRegistry"; //NOI18N
      
    private static final MavenSettings INSTANCE = new MavenSettings();
    
    private final Set<PropertyChangeListener> listeners = new WeakSet<>();

    public static MavenSettings getDefault() {
        return INSTANCE;
    }

    public boolean isInteractive() {
        return !hasOption("--batch", "-B"); //NOI18N
    }

    public Boolean isOffline() {
        if (hasOption("--offline", "-o")) { //NOI18N
            return Boolean.TRUE;
        }
        return null;
    }

    public boolean isShowDebug() {
        return hasOption("--debug", "-X"); //NOI18N
    }

    public boolean isShowErrors() {
        return hasOption("--errors", "-e"); //NOI18N
    }

    public boolean isUpdateSnapshots() {
        return hasOption("--update-snapshots", "-U"); //NOI18N
    }

    private boolean hasOption(String longName, String shortName) {
        String defOpts = getDefaultOptions();
        if (defOpts != null) {
            try {
                String[] strs = CommandLineUtils.translateCommandline(defOpts);
                for (String s : strs) {
                    s = s.trim();
                    if (s.startsWith(shortName) || s.startsWith(longName)) {
                        return true;
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(MavenSettings.class.getName()).log(Level.FINE, "Error parsing global options:{0}", defOpts);
                //will check for contains of -X be enough?
                return defOpts.contains(longName) || defOpts.contains(shortName);
            }
        }
        return false;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(MavenSettings.class);
    }
    
    private String putProperty(String key, String value) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        return retval;
    }

    private String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    private MavenSettings() {
        //import from older versions
        String defOpts = getPreferences().get(PROP_DEFAULT_OPTIONS, null);
        if (defOpts == null) {
            defOpts = "";
            //only when not already set by user or by previous import
            String debug = getPreferences().get(PROP_DEBUG, null);
            if (debug != null) {
                boolean val = Boolean.parseBoolean(debug);
                if (val) {
                    defOpts = defOpts + " --debug";//NOI18N
                }
                getPreferences().remove(PROP_DEBUG);
            }
            String error = getPreferences().get(PROP_ERRORS, null);
            if (error != null) {
                boolean val = Boolean.parseBoolean(error);
                if (val) {
                    defOpts = defOpts + " --errors"; //NOI18N
                }
                getPreferences().remove(PROP_ERRORS);
            }
            String checksum = getPreferences().get(PROP_CHECKSUM_POLICY, null);
            if (checksum != null) {
                if (MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals(checksum)) {
                    defOpts = defOpts + " --strict-checksums";//NOI18N
                } else if (MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals(checksum)) {
                    defOpts = defOpts + " --lax-checksums";//NOI18N
                }
                getPreferences().remove(PROP_CHECKSUM_POLICY);
            }
            String fail = getPreferences().get(PROP_FAILURE_BEHAVIOUR, null);
            if (fail != null) {
                if (MavenExecutionRequest.REACTOR_FAIL_NEVER.equals(fail)) {
                    defOpts = defOpts + " --fail-never";//NOI18N
                } else if (MavenExecutionRequest.REACTOR_FAIL_FAST.equals(fail)) {
                    defOpts = defOpts + " --fail-fast";//NOI18N
                } else if (MavenExecutionRequest.REACTOR_FAIL_AT_END.equals(fail)) {
                    defOpts = defOpts + " --fail-at-end";//NOI18N
                }
                getPreferences().remove(PROP_FAILURE_BEHAVIOUR);
            }
            String pluginUpdate = getPreferences().get(PROP_PLUGIN_POLICY, null);
            if (pluginUpdate != null) {
                if (Boolean.parseBoolean(pluginUpdate)) {
                    defOpts = defOpts + " --check-plugin-updates";//NOI18N
                } else {
                    defOpts = defOpts + " --no-plugin-updates";//NOI18N
                }
                getPreferences().remove(PROP_PLUGIN_POLICY);
            }
            String registry = getPreferences().get(PROP_USE_REGISTRY, null);
            if (registry != null) {
                if (!Boolean.parseBoolean(registry)) {
                    defOpts = defOpts + " --no-plugin-registry";//NOI18N
                }
                getPreferences().remove(PROP_USE_REGISTRY);
            }
            setDefaultOptions(defOpts);
            try {
                getPreferences().flush();
            } catch (BackingStoreException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getDefaultOptions() {
        return getPreferences().get(PROP_DEFAULT_OPTIONS, ""); //NOI18N
    }

    public void setDefaultOptions(String options) {
        String old = getDefaultOptions();
        putProperty(PROP_DEFAULT_OPTIONS, options);
        if (!old.equals(options)) {
            //options could contain -Dkey=value, then the MavenEmbedder instances need to be reloaded
            EmbedderFactory.resetCachedEmbedders();
        }        
    }
    
    public boolean isVMOptionsWrap() {
        return getPreferences().getBoolean(PROP_VM_OPTIONS_WRAP, true);
    }
    
    public void setVMOptionsWrap(boolean b) {
        getPreferences().putBoolean(PROP_VM_OPTIONS_WRAP, b);
    }

    public String getDefaultJdk() {
        return getPreferences().get(PROP_DEFAULT_JDK, "");
    }

    public void setDefaultJdk(String jdk) {
        getPreferences().put(PROP_DEFAULT_JDK, jdk);
        PropertyChangeListener[] arr;
        synchronized (listeners) {
            arr = listeners.toArray(new PropertyChangeListener[0]);
        }
        for (PropertyChangeListener l : arr) {
            l.propertyChange(new PropertyChangeEvent(this, PROP_DEFAULT_JDK, null, jdk));
        }
    }

    public String getLastArchetypeGroupId() {
        return getPreferences().get(PROP_LAST_ARCHETYPE_GROUPID, Boolean.getBoolean("netbeans.full.hack") ? "test" : "com.mycompany"); //NOI18N
    }

    public void setLastArchetypeGroupId(String groupId) {
        putProperty(PROP_LAST_ARCHETYPE_GROUPID, groupId);
    }
    
    public boolean isSkipTests() {
        return getPreferences().getBoolean(PROP_SKIP_TESTS, false);
    }

    public void setSkipTests(boolean skipped) {
        getPreferences().putBoolean(PROP_SKIP_TESTS, skipped);
    }
    
    public boolean isAlwaysShowOutput() {
        return getPreferences().getBoolean(PROP_ALWAYS_OUTPUT, true);
    }
    
    public void setAlwaysShowOutput(boolean show) {
        getPreferences().putBoolean(PROP_ALWAYS_OUTPUT, show);
    }
    
    public boolean isShowLoggingLevel() {
        return getPreferences().getBoolean(PROP_SHOW_LOGGING_LEVEL, false);
    }
    
    public void setShowLoggingLevel(boolean show) {
        getPreferences().putBoolean(PROP_SHOW_LOGGING_LEVEL, show);
    }
    
    public boolean isReuseOutputTabs() {
        return getPreferences().getBoolean(PROP_REUSE_OUTPUT, true);
    }

    public void setReuseOutputTabs(boolean reuse) {
        getPreferences().putBoolean(PROP_REUSE_OUTPUT, reuse);
    }
    
    public String getLastArchetypeVersion() {
        return getPreferences().get(PROP_LAST_ARCHETYPE_VERSION, "1.0-SNAPSHOT"); //NOI18N
    }
    
    public void setLastArchetypeVersion(String version) {
        putProperty(PROP_LAST_ARCHETYPE_VERSION, version); //NOI18N
    }

    public void setProjectNodeNamePattern(String pattern) {
        if (null == pattern) {
            getPreferences().remove(PROP_PROJECTNODE_NAME_PATTERN);
        } else {
            putProperty(PROP_PROJECTNODE_NAME_PATTERN, pattern);
        }
    }

    public String getProjectNodeNamePattern() {
        return getPreferences().get(PROP_PROJECTNODE_NAME_PATTERN, null); //NOI18N
    }

    public boolean isUseBestMaven() {
        return getPreferences().getBoolean(PROP_EXPERIMENTAL_USE_BEST_MAVEN, false);
    }
    
    public void setUseBestMaven(boolean bestMaven) {
        getPreferences().putBoolean(PROP_EXPERIMENTAL_USE_BEST_MAVEN, bestMaven);
    }
    
    public boolean isUseBestMavenAltLocation() {
        return getPreferences().getBoolean(PROP_EXPERIMENTAL_USE_ALTERNATE_LOCATION, false);
    }
    
    public void setUseBestMavenAltLocation(boolean bestMavenAltLocation) {
        getPreferences().putBoolean(PROP_EXPERIMENTAL_USE_ALTERNATE_LOCATION, bestMavenAltLocation);
    }
    
    public void setBestMavenAltLocation(String location) {
        if (null == location) {
            getPreferences().remove(PROP_EXPERIMENTAL_ALTERNATE_LOCATION);
        } else {
            putProperty(PROP_EXPERIMENTAL_ALTERNATE_LOCATION, location);
        }
    }

    public String getBestMavenAltLocation() {
        return getPreferences().get(PROP_EXPERIMENTAL_ALTERNATE_LOCATION, null); //NOI18N
    }
    

    public boolean isCollapseSuccessFolds() {
        return getPreferences().getBoolean(PROP_COLLAPSE_FOLDS, false);
    }
    
    public void setCollapseSuccessFolds(boolean collapse) {
        getPreferences().putBoolean(PROP_COLLAPSE_FOLDS, collapse);
    }

    public void setOutputTabShowConfig(boolean selected) {
        getPreferences().putBoolean(PROP_OUTPUT_TAB_CONFIG, selected);
    }
    
    public boolean isOutputTabShowConfig() {
        return getPreferences().getBoolean(PROP_OUTPUT_TAB_CONFIG, false);
    }
    
    public OutputTabName getOutputTabName() {
        String val = getPreferences().get(PROP_OUTPUT_TAB_NAME, OutputTabName.PROJECT_NAME.name());
        try {
            return OutputTabName.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return OutputTabName.PROJECT_NAME;
        }
    }
    
    public void setOutputTabName(OutputTabName ds) {
        if (ds != null) {
            getPreferences().put(PROP_OUTPUT_TAB_NAME, ds.name());
        } else {
            getPreferences().remove(PROP_OUTPUT_TAB_NAME);
        }
    }

    public void addWeakPropertyChangeListener(PropertyChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public static enum OutputTabName {
        PROJECT_NAME,
        PROJECT_ID
    }
    
    public static enum DownloadStrategy {
        NEVER,
        FIRST_OPEN,
        EVERY_OPEN
    }

    public DownloadStrategy getSourceDownloadStrategy() {
        String val = getPreferences().get(PROP_SOURCE_DOWNLOAD, DownloadStrategy.NEVER.name());
        try {
            return DownloadStrategy.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return DownloadStrategy.NEVER;
        }
    }

    public void setSourceDownloadStrategy(DownloadStrategy ds) {
        if (ds != null) {
            getPreferences().put(PROP_SOURCE_DOWNLOAD, ds.name());
        } else {
            getPreferences().remove(PROP_SOURCE_DOWNLOAD);
        }
    }

    public DownloadStrategy getJavadocDownloadStrategy() {
        String val = getPreferences().get(PROP_JAVADOC_DOWNLOAD, DownloadStrategy.NEVER.name());
        try {
            return DownloadStrategy.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return DownloadStrategy.NEVER;
        }
    }

    public void setJavadocDownloadStrategy(DownloadStrategy ds) {
        if (ds != null) {
            getPreferences().put(PROP_JAVADOC_DOWNLOAD, ds.name());
        } else {
            getPreferences().remove(PROP_JAVADOC_DOWNLOAD);
        }
    }

    public DownloadStrategy getBinaryDownloadStrategy() {
        String val = getPreferences().get(PROP_BINARY_DOWNLOAD, DownloadStrategy.NEVER.name());
        try {
            return DownloadStrategy.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return DownloadStrategy.NEVER;
        }
    }
    
    public void setBinaryDownloadStrategy(DownloadStrategy ds) {
        if (ds != null) {
            getPreferences().put(PROP_BINARY_DOWNLOAD, ds.name());
        } else {
            getPreferences().remove(PROP_BINARY_DOWNLOAD);
        }
    }

    public static @CheckForNull String getCommandLineMavenVersion() {
        return getCommandLineMavenVersion(EmbedderFactory.getMavenHome());
    }
    
    public static @CheckForNull String getCommandLineMavenVersion(File mavenHome) {
        File[] jars = new File(mavenHome, "lib").listFiles(new FilenameFilter() { // NOI18N
            public @Override boolean accept(File dir, String name) {
                return name.endsWith(".jar"); // NOI18N
            }
        });
        if (jars == null) {
            return null;
        }
        for (File jar : jars) {
            try {
                // Prefer to use this rather than raw ZipFile since URLMapper since ArchiveURLMapper will cache JARs:
                FileObject entry = URLMapper.findFileObject(new URL(FileUtil.urlForArchiveOrDir(jar), "META-INF/maven/org.apache.maven/maven-core/pom.properties")); // NOI18N
                if (entry != null) {
                    InputStream is = entry.getInputStream();
                    try {
                        Properties properties = new Properties();
                        properties.load(is);
                        return properties.getProperty("version"); // NOI18N
                    } finally {
                        is.close();
                    }
                }
            } catch (IOException x) {
                // ignore for now
            }
        }
        return null;
    }

    private static List<String> searchMavenRuntimes(String[] paths, boolean stopOnFirstValid) {
        List<String> runtimes = new ArrayList<String>();
        for (String path : paths) {
            File file = new File(path);
            path = FileUtil.normalizeFile(file).getAbsolutePath();
            String version = getCommandLineMavenVersion(file);
            if (version != null) {
                runtimes.add(path);
                if (stopOnFirstValid) {
                    break;
                }
            }
        }

        return runtimes;
    }

	/**
	 * Searches for Maven Runtimes by the environment settings and returns the first valid one.
	 *
	 * <p>It searches in this order:
	 * <ul>
	 * <li>MAVEN_HOME</li>
	 * <li>M2_HOME</li>
	 * <li>PATH</li></ul>
	 * </p>
	 * <p>Only the first appereance will be appended.</p>
	 *
	 * @returns the default external Maven runtime on the path.
	 */
    public static String getDefaultExternalMavenRuntime() {
        String paths = System.getenv("PATH"); // NOI18N
        String mavenHome = System.getenv("MAVEN_HOME"); // NOI18N
        String m2Home = System.getenv("M2_HOME"); // NOI18N

        List<String> mavenEnvDirs = new ArrayList<String>();
        if (mavenHome != null) {
            mavenEnvDirs.add(mavenHome);
        }
        if (m2Home != null) {
            mavenEnvDirs.add(m2Home);
        }
        if (paths != null) {
            for (String path : paths.split(File.pathSeparator)) {
                if (!path.endsWith("bin")) { // NOI18N
                    continue;
                }

                if(path.equals("/bin") || path.equals("bin")) {
                    mavenEnvDirs.add(path);
                } else {
                    mavenEnvDirs.add(path.substring(0,
                        path.length() - "bin".length() - File.pathSeparator.length()));
                }
            }
        }

        List<String> runtimes = searchMavenRuntimes(mavenEnvDirs.toArray(new String[0]), true);
        return !runtimes.isEmpty() ? runtimes.get(0) : null;
    }
    
    public List<String> getUserDefinedMavenRuntimes() {
        List<String> runtimes = new ArrayList<String>();

        String defaultRuntimePath = getDefaultExternalMavenRuntime();
        String runtimesPref = getPreferences().get(PROP_MAVEN_RUNTIMES, null);
        if (runtimesPref != null) {
            for (String runtimePath : runtimesPref.split(File.pathSeparator)) {
                if (!"".equals(runtimePath) && !runtimePath.equals(defaultRuntimePath)) {
                    runtimes.add(runtimePath);
                }
            }
        }

        return Collections.unmodifiableList(runtimes);
    }

    public void setMavenRuntimes(List<String> runtimes) {
        if (runtimes == null) {
            getPreferences().remove(PROP_MAVEN_RUNTIMES);
        } else {
            String runtimesPref = "";
            for (String path : runtimes) {
                runtimesPref += path + File.pathSeparator;
            }
            if (runtimesPref.endsWith(File.pathSeparator)) {
                runtimesPref = runtimesPref.substring(0, runtimesPref.length() - 1);
            }
            putProperty(PROP_MAVEN_RUNTIMES, runtimesPref);
        }
    }
    
}
