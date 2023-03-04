/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tools.ant.module;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;

public class AntSettings {

    private static final Logger LOG = Logger.getLogger(AntSettings.class.getName());

    private static final String PROP_VERBOSITY = "verbosity"; // NOI18N
    private static final String PROP_PROPERTIES = "properties"; // NOI18N
    private static final String PROP_SAVE_ALL = "saveAll"; // NOI18N
    private static final String PROP_CUSTOM_DEFS = "customDefs"; // NOI18N
    public static final String PROP_ANT_HOME = "antHome"; // NOI18N
    public static final String PROP_EXTRA_CLASSPATH = "extraClasspath"; // NOI18N
    public static final String PROP_AUTOMATIC_EXTRA_CLASSPATH = "automaticExtraClasspath"; // NOI18N
    private static final String PROP_AUTO_CLOSE_TABS = "autoCloseTabs"; // NOI18N
    private static final String PROP_ALWAYS_SHOW_OUTPUT = "alwaysShowOutput"; // NOI18N

    private AntSettings() {}

    private static Preferences prefs() {
        return NbPreferences.forModule(AntSettings.class);
    }

    public static int getVerbosity() {
        return prefs().getInt(PROP_VERBOSITY, AntEvent.LOG_INFO);
    }

    public static void setVerbosity(int v) {
        prefs().putInt(PROP_VERBOSITY, v);
    }

    public static Map<String,String> getProperties() {
        Map<String,String> p = new HashMap<String,String>();
        // Enable hyperlinking for Jikes by default:
        for (String pair : prefs().get(PROP_PROPERTIES, "build.compiler.emacs=true").split("\n")) { // NOI18N
            String[] nameval = pair.split("=", 2); // NOI18N
            if (nameval.length != 2) {
                LOG.log(Level.WARNING, "Unexpected name=value pair: ''{0}''", pair);
                continue;
            }
            p.put(nameval[0], nameval[1]);
        }
        return p;
    }

    public static void setProperties(Map<String,String> p) {
        if (!(p instanceof SortedMap)) {
            p = new TreeMap<String,String>(p);
        }
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String,String> pair : p.entrySet()) {
            if (b.length() > 0) {
                b.append('\n');
            }
            b.append(pair.getKey());
            b.append('=');
            b.append(pair.getValue());
        }
        prefs().put(PROP_PROPERTIES, b.toString());
    }

    public static boolean getSaveAll() {
        return prefs().getBoolean(PROP_SAVE_ALL, true);
    }

    public static void setSaveAll(boolean sa) {
        prefs().putBoolean(PROP_SAVE_ALL, sa);
    }

    private static IntrospectedInfo customDefs;
    static {
        new IntrospectedInfo(); // trigger IntrospectedInfo static block
    }
    public static synchronized IntrospectedInfo getCustomDefs() {
        if (customDefs == null) {
            customDefs = IntrospectedInfoSerializer.instance.load(prefs().node(PROP_CUSTOM_DEFS));
        }
        return customDefs;
    }

    public static synchronized void setCustomDefs(IntrospectedInfo ii) {
        IntrospectedInfoSerializer.instance.store(prefs().node(PROP_CUSTOM_DEFS), ii);
        customDefs = ii;
    }

    private static String antVersion;
    // #14993: read-only property for the version of Ant
    public static String getAntVersion() {
        if (antVersion == null) {
            antVersion = AntBridge.getInterface().getAntVersion();
        }
        return antVersion;
    }

    /**
     * Transient value of ${ant.home} unless otherwise set.
     * @see "#43522"
     */
    private static File defaultAntHome = null;

    private static synchronized File getDefaultAntHome() {
        if (defaultAntHome == null) {
            File antJar = InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", "org.apache.tools.ant.module", false); // NOI18N
            if (antJar == null) {
                return null;
            }
            defaultAntHome = antJar.getParentFile().getParentFile();
            if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                AntModule.err.log("getDefaultAntHome: " + defaultAntHome);
            }
        }
        assert defaultAntHome != null;
        return defaultAntHome;
    }

    /**
     * Get the Ant installation to use.
     * Might be null!
     */
    public static File getAntHome() {
        String h = prefs().get(PROP_ANT_HOME, null);
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("getAntHomeWithDefault: antHome=" + h);
        }
        if (h != null) {
            return new File(h);
        } else {
            // Not explicitly configured. Check default.
            return getDefaultAntHome();
        }
    }

    public static void setAntHome(File f) {
        if (f != null && f.equals(getDefaultAntHome())) {
            f = null;
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("setAntHome: " + f);
        }
        if (f != null) {
            prefs().put(PROP_ANT_HOME, f.getAbsolutePath());
        } else {
            prefs().remove(PROP_ANT_HOME);
        }
        antVersion = null;
        firePropertyChange(PROP_ANT_HOME);
    }

    public static List<File> getExtraClasspath() {
        List<File> files = new ArrayList<File>();
        for (String f : prefs().get(PROP_EXTRA_CLASSPATH, "").split(Pattern.quote(File.pathSeparator))) {
            if (f.length() == 0) {
                continue; // otherwise would add CWD to CP!
            }
            files.add(new File(f));
        }
        return files;
    }

    public static void setExtraClasspath(List<File> p) {
        StringBuilder b = new StringBuilder();
        for (File f : p) {
            if (b.length() > 0) {
                b.append(File.pathSeparatorChar);
            }
            b.append(f);
        }
        prefs().put(PROP_EXTRA_CLASSPATH, b.toString());
        firePropertyChange(PROP_EXTRA_CLASSPATH);
    }

    private static List<File> defAECP = null;
    private static Lookup.Result<AutomaticExtraClasspathProvider> aecpResult = null;

    public static synchronized List<File> getAutomaticExtraClasspath() {
        if (aecpResult == null) {
            aecpResult = Lookup.getDefault().lookupResult(AutomaticExtraClasspathProvider.class);
            aecpResult.addLookupListener(new LookupListener() {
                public @Override void resultChanged(LookupEvent ev) {
                    synchronized (AntSettings.class) {
                        defAECP = null;
                    }
                    firePropertyChange(PROP_AUTOMATIC_EXTRA_CLASSPATH);
                }
            });
        }
        if (defAECP == null) {
            defAECP = new ArrayList<File>();
            for (AutomaticExtraClasspathProvider provider : aecpResult.allInstances()) {
                assert provider != null;
                defAECP.addAll(Arrays.asList(provider.getClasspathItems()));
            }
            LOG.log(Level.FINE, "getAutomaticExtraClasspath: {0}", defAECP);
        }
        return defAECP;
    }

    public static boolean getAutoCloseTabs() {
        return prefs().getBoolean(PROP_AUTO_CLOSE_TABS, /*#47753*/ true);
    }

    public static void setAutoCloseTabs(boolean b) {
        prefs().putBoolean(PROP_AUTO_CLOSE_TABS, b);
    }

    public static boolean getAlwaysShowOutput() {
        return prefs().getBoolean(PROP_ALWAYS_SHOW_OUTPUT, /* #87801 */false);
    }

    public static void setAlwaysShowOutput(boolean b) {
        prefs().putBoolean(PROP_ALWAYS_SHOW_OUTPUT, b);
    }

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(AntSettings.class);

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private static void firePropertyChange(String prop) {
        pcs.firePropertyChange(prop, null, null);
    }

    public abstract static class IntrospectedInfoSerializer {
        public static IntrospectedInfoSerializer instance;
        public abstract IntrospectedInfo load(Preferences node);
        public abstract void store(Preferences node, IntrospectedInfo info);
    }

}
