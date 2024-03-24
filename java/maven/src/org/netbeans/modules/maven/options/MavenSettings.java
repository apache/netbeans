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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
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
    private static final String PROP_VM_OPTIONS_WRAP = "vmOptionsWrap";
    private static final String PROP_DEFAULT_JDK = "defaultJdk";
    private static final String PROP_PREFER_WRAPPER = "preferWrapper"; //NOI18N

    //these are from former versions (6.5) and are here only for conversion
    private static final String PROP_DEBUG = "showDebug"; // NOI18N
    private static final String PROP_ERRORS = "showErrors"; //NOI18N
    private static final String PROP_CHECKSUM_POLICY = "checksumPolicy"; //NOI18N
    private static final String PROP_PLUGIN_POLICY = "pluginUpdatePolicy"; //NOI18N
    private static final String PROP_FAILURE_BEHAVIOUR = "failureBehaviour"; //NOI18N
    private static final String PROP_USE_REGISTRY = "usePluginRegistry"; //NOI18N
    public static final String PROP_NETWORK_PROXY = "networkProxy";
      
    private static final Pattern MAVEN_CORE_JAR_PATTERN = Pattern.compile("maven-core-(\\d+\\.\\d+\\.\\d+)\\.jar");  // NOI18N

    private static final MavenSettings INSTANCE = new MavenSettings();
    
    private final Set<PropertyChangeListener> listeners = new WeakSet<>();
    
    /**
     * Specifies how should be proxies handled by default, if no setting is given.
     */
    private static final String SYSPROP_DEFAULT_PROXY_BEHAVIOUR = "netbeans.networkProxy";
    
    private static final NetworkProxySettings DEFAULT_PROXY_BEHAVIOUR;
    
    static {
        NetworkProxySettings def;
        try {
            def = NetworkProxySettings.valueOf(System.getProperty(SYSPROP_DEFAULT_PROXY_BEHAVIOUR, NetworkProxySettings.ASK.name()).toUpperCase());
        } catch (IllegalArgumentException e) {
            def = NetworkProxySettings.ASK;
        }
        DEFAULT_PROXY_BEHAVIOUR = def;
    }

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
            defOpts = getDefaultOptions();
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
        return getPreferences().get(PROP_DEFAULT_OPTIONS, "--no-transfer-progress"); //NOI18N
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

    @NbBundle.Messages({
        "#reuse output: true, false, never",
        "#NOI18N",
        "DEFAULT_REUSE_OUTPUT=true"
    })
    public boolean isReuseOutputTabs() {
        String def = Bundle.DEFAULT_REUSE_OUTPUT();
        if ("never".equals(def)) { // NOI18N
            return false;
        }
        return getPreferences().getBoolean(PROP_REUSE_OUTPUT, "true".equals(def)); // NOI18N
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
    
    public boolean isPreferMavenWrapper() {
        return getPreferences().getBoolean(PROP_PREFER_WRAPPER, true);
    }
    
    public void setPreferMavenWrapper(boolean preferWrapper) {
        getPreferences().putBoolean(PROP_PREFER_WRAPPER, preferWrapper);
    }

    /**
     * Deprecated for removal - use mvnw instead.
     * Returns false.
     */
    @Deprecated/*(forRemoval = true)*/
    public boolean isUseBestMaven() {
        return false;
    }
    
    /**
     * Deprecated for removal - use mvnw instead.
     * No-op.
     */
    @Deprecated/*(forRemoval = true)*/
    public void setUseBestMaven(boolean bestMaven) {
    }
    
    /**
     * Deprecated for removal - use mvnw instead.
     * Returns false.
     */
    @Deprecated/*(forRemoval = true)*/
    public boolean isUseBestMavenAltLocation() {
        return false;
    }
    
    /**
     * Deprecated for removal - use mvnw instead.
     * No-op.
     */
    @Deprecated/*(forRemoval = true)*/
    public void setUseBestMavenAltLocation(boolean bestMavenAltLocation) {
    }
    
    /**
     * Deprecated for removal - use mvnw instead.
     * No-op.
     */
    @Deprecated/*(forRemoval = true)*/
    public void setBestMavenAltLocation(String location) {
    }

    /**
     * Deprecated for removal - use mvnw instead.
     * Returns null.
     */
    @Deprecated/*(forRemoval = true)*/
    public String getBestMavenAltLocation() {
        return null;
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
        Path lib = Paths.get(mavenHome.getPath(), "lib");        // mvn layout  // NOI18N
        if (!Files.exists(lib)) {
            lib = Paths.get(mavenHome.getPath(), "mvn", "lib");  // mvnd layout // NOI18N
            if (!Files.exists(lib)) {
                return null;
            }
        }

        // try to resolve maven version by checking maven-core jar name
        try (Stream<String> mavenCoreVersions = Files.list(lib)
                .map(file -> file.getFileName().toString())
                .filter(file -> file.startsWith("maven-core")) // NOI18N
                .map(file -> MAVEN_CORE_JAR_PATTERN.matcher(file))
                .filter(matcher -> matcher.matches())
                .map(matcher -> matcher.group(1))) {
            Optional<String> mavenCoreVersion = mavenCoreVersions.findFirst();
            if (mavenCoreVersion.isPresent()) {
                return mavenCoreVersion.get();
            }
        } catch (IOException ignored) {}

        // try to resolve maven version by parsing pom.properties
        try {
            try (Stream<Path> jars = Files.list(lib).filter(file -> file.toString().endsWith(".jar"))) {  // NOI18N
                for (Path jar : jars.collect(Collectors.toList())) {
                    // Prefer to use this rather than raw ZipFile since URLMapper since ArchiveURLMapper will cache JARs:
                    FileObject entry = URLMapper.findFileObject(
                            new URL(FileUtil.urlForArchiveOrDir(jar.toFile()), "META-INF/maven/org.apache.maven/maven-core/pom.properties")); // NOI18N
                    if (entry != null) {
                        try (InputStream is = entry.getInputStream()) {
                            Properties properties = new Properties();
                            properties.load(is);
                            return properties.getProperty("version"); // NOI18N
                        }
                    }
                }
            }
        } catch (IOException ignored) {}

        return null;
    }

    public static boolean isMavenDaemon(Path mavenHome) {
        String mvndExecutableName = Utilities.isWindows() ? "mvnd.exe" : "mvnd";

        return Files.exists(mavenHome.resolve("bin").resolve(mvndExecutableName));
    }

    private static List<String> searchMavenRuntimes(String[] paths, boolean stopOnFirstValid) {
        List<String> runtimes = new ArrayList<>();
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
	 * <li>PATH</li>
	 * </ul>
	 * <p>Only the first appereance will be appended.</p>
	 *
	 * @return the default external Maven runtime on the path.
	 */
    public static String getDefaultExternalMavenRuntime() {
        String paths = System.getenv("PATH"); // NOI18N
        String mavenHome = System.getenv("MAVEN_HOME"); // NOI18N
        String m2Home = System.getenv("M2_HOME"); // NOI18N

        List<String> mavenEnvDirs = new ArrayList<>();
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
        List<String> runtimes = new ArrayList<>();

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
    
    public NetworkProxySettings getNetworkProxy() {
        String s = getPreferences().get(PROP_NETWORK_PROXY, DEFAULT_PROXY_BEHAVIOUR.name());
        try {
            return NetworkProxySettings.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return DEFAULT_PROXY_BEHAVIOUR;
        }
    }
    
    public void setNetworkProxy(NetworkProxySettings s) {
        getPreferences().put(PROP_NETWORK_PROXY, s.name());
    }
}
