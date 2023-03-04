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
package org.netbeans.modules.groovy.grails.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.RuntimeHelper;
import org.netbeans.modules.groovy.grails.api.GrailsEnvironment;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author schmidtm
 */
// FIXME this will fail for multiple projects with same name or on rename (?)
public final class GrailsSettings {

    public static final String GRAILS_BASE_PROPERTY = "grailsBase"; // NOI18N

    private static final String GRAILS_HOME_KEY = "grailsHome"; // NOI18N

    private static final String GRAILS_PORT_KEY = "grailsPrj-Port-"; // NOI18N

    private static final String GRAILS_ENV_KEY = "grailsPrj-Env-"; // NOI18N

    private static final String GRAILS_JAVA_PLATFORM_KEY = "grailsPrj-JavaPlatform-"; // NOI18N

    private static final String GRAILS_VM_OPTIONS_KEY = "grailsPrj-VmOptions-"; // NOI18N

    // Which browser to use for client side debugging Firfox or Internet Explorer ?
    // Possible values for this key are FIREFOX and INTERNET_EXPLORER
    private static final String GRAILS_DEBUG_BROWSER_KEY = "grailsPrj-DebugBrowser-"; // NOI18N

    private static final String GRAILS_DISPLAY_BROWSER_KEY = "grailsPrj-DisplayBrowser-"; // NOI18N

    private static final String GRAILS_PROJECT_PLUGINS_DIR_KEY = "grailsPrj-ProjectPluginsDir-"; // NOI18N

    private static final String GRAILS_GLOBAL_PLUGINS_DIR_KEY = "grailsPrj-GlobalPluginsDir-"; // NOI18N

    private static final String GRAILS_LOCAL_PLUGINS_KEY = "grailsPrj-LocalPlugins-"; // NOI18N

    private static GrailsSettings instance;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private GrailsSettings() {
        super();
    }

    public static synchronized GrailsSettings getInstance() {
        if (instance == null) {
            instance = new GrailsSettings();
        }
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public String getGrailsBase() {
        String base = null;
        synchronized (this) {
            base = getPreferences().get(GRAILS_HOME_KEY, null);
        }
        if (base == null || base.length() <= 0) {
            base = findGroovyPlatform();
        }
        return base;
    }

    public void setGrailsBase(String path) {
        String oldValue;
        synchronized (this) {
            oldValue = getGrailsBase();
            getPreferences().put(GRAILS_HOME_KEY, path);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_BASE_PROPERTY, oldValue, path);
    }

    // Which port should we run on
    public String getPortForProject(Project prj) {
        assert prj != null;
        return getPreferences().get(getPortKey(prj), null);
    }

    public void setPortForProject(Project prj, String port) {
        assert prj != null;
        assert port != null;

        getPreferences().put(getPortKey(prj), port);
    }

    // What VM options we should use
    public String getVmOptionsForProject(Project prj) {
        assert prj != null;
        return getPreferences().get(getVmOptionsKey(prj), null);
    }

    public void setVmOptionsForProject(Project prj, String options) {
        assert prj != null;
        assert options != null;

        getPreferences().put(getVmOptionsKey(prj), options);
    }

    // which Environment should we use (Test, Production, Development, etc.)
    public GrailsEnvironment getEnvForProject(Project prj) {
        assert prj != null;
        String value = getPreferences().get(getEnvKey(prj), null);
        if (value != null) {
            return GrailsEnvironment.valueOf(value);
        }
        return null;
    }

    public void setEnvForProject(Project prj, GrailsEnvironment env) {
        assert prj != null;
        assert env != null;

        getPreferences().put(getEnvKey(prj), env.toString());
    }

    // Which browser to use for client side debugging Firfox or Internet Explorer ?
    public String getDebugBrowserForProject(Project prj) {
        assert prj != null;
        return getPreferences().get(getDebugBrowserKey(prj), null);
    }

    public void setDebugBrowserProject(Project prj, String browser) {
        assert prj != null;
        assert browser != null;

        getPreferences().put(getDebugBrowserKey(prj), browser);
    }

    public String getJavaPlatformForProject(Project prj) {
        assert prj != null;

        return getPreferences().get(getJavaPlatformKey(prj), null);
    }

    public void setJavaPlatformForProject(Project prj, String platformId) {
        assert prj != null;
        assert platformId != null;

        getPreferences().put(getJavaPlatformKey(prj), platformId);
    }

    public boolean getDisplayBrowserForProject(Project prj) {
        assert prj != null;

        return getPreferences().getBoolean(getDisplayBrowserKey(prj), true);
    }

    public void setDisplayBrowserForProject(Project prj, boolean displayBrowser) {
        assert prj != null;

        getPreferences().putBoolean(getDisplayBrowserKey(prj), displayBrowser);
    }

    public String getProjectPluginsDirForProject(Project prj) {
        assert prj != null;

        return getPreferences().get(getProjectPluginsDirKey(prj), null);
    }

    public void setProjectPluginsDirForProject(Project prj, String dir) {
        assert prj != null;

        getPreferences().put(getProjectPluginsDirKey(prj), dir);
    }

    public String getGlobalPluginsDirForProject(Project prj) {
        assert prj != null;

        return getPreferences().get(getGlobalPluginsDirKey(prj), null);
    }

    public void setGlobalPluginsDirForProject(Project prj, String dir) {
        assert prj != null;

        getPreferences().put(getGlobalPluginsDirKey(prj), dir);
    }

    public Map<String, String> getLocalPluginsForProject(Project prj) {
        assert prj != null;

        Preferences prefs = getPreferences();
        Preferences subPrefs = prefs.node(getLocalPluginsKey(prj));
        Map<String, String> ret = new HashMap<String, String>();
        try {
            for (String name : subPrefs.keys()) {
                String value = subPrefs.get(name, null);
                if (value != null) {
                    ret.put(name, value);
                }
            }
        } catch (BackingStoreException ex) {
            return Collections.emptyMap();
        }
        return ret;
    }

    public void setLocalPluginsForProject(Project prj, Map<String, String> plugins) {
        assert prj != null;

        Preferences prefs = getPreferences();
        Preferences subPrefs = prefs.node(getLocalPluginsKey(prj));

        Set<String> keys = null;
        try {
            keys = new HashSet<String>(Arrays.asList(subPrefs.keys()));
        } catch (BackingStoreException ex) {
            keys = Collections.emptySet();
        }

        for (Map.Entry<String, String> entry : plugins.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                subPrefs.put(entry.getKey(), entry.getValue());
                keys.remove(entry.getKey());
            }
        }
        for (String key : keys) {
            subPrefs.remove(key);
        }
    }

    private String getProjectName(Project prj) {
        assert prj != null;

        ProjectInformation info = prj.getLookup().lookup(ProjectInformation.class);
        assert info != null;
        return info.getName();
    }

    private String getPortKey(Project prj) {
        assert prj != null;
        return GRAILS_PORT_KEY + getProjectName(prj);
    }

    private String getVmOptionsKey(Project prj) {
        assert prj != null;
        return GRAILS_VM_OPTIONS_KEY + getProjectName(prj);
    }

    private String getEnvKey(Project prj) {
        assert prj != null;
        return GRAILS_ENV_KEY + getProjectName(prj);
    }

    private String getDebugBrowserKey(Project prj) {
        assert prj != null;
        return GRAILS_DEBUG_BROWSER_KEY + getProjectName(prj);
    }

    private String getJavaPlatformKey(Project prj) {
        assert prj != null;
        return GRAILS_JAVA_PLATFORM_KEY + getProjectName(prj);
    }

    private String getDisplayBrowserKey(Project prj) {
        assert prj != null;
        return GRAILS_DISPLAY_BROWSER_KEY + getProjectName(prj);
    }

    private String getProjectPluginsDirKey(Project prj) {
        assert prj != null;
        return GRAILS_PROJECT_PLUGINS_DIR_KEY + getProjectName(prj);
    }

    private String getGlobalPluginsDirKey(Project prj) {
        assert prj != null;
        return GRAILS_GLOBAL_PLUGINS_DIR_KEY + getProjectName(prj);
    }

    private String getLocalPluginsKey(Project prj) {
        assert prj != null;
        return GRAILS_LOCAL_PLUGINS_KEY + getProjectName(prj);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(GrailsSettings.class);
    }

    private String findGroovyPlatform() {
        String groovyPath = System.getenv(RuntimeHelper.GRAILS_HOME_PROPERTY);
        if (groovyPath == null) {
            for (String dir : dirsOnPath()) {
                File f = null;
                if (Utilities.isWindows()) {
                    f = new File(dir, RuntimeHelper.WIN_EXECUTABLE_FILE);
                } else {
                    f = new File(dir, RuntimeHelper.NIX_EXECUTABLE_FILE);
                }
                if (f.isFile()) {
                    try {
                        groovyPath = f.getCanonicalFile().getParentFile().getParent();
                        break;
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }
        return groovyPath;
    }

    /**
     * Returns an {@link Iterable} which will uniquely traverse all valid
     * elements on the <em>PATH</em> environment variables. That means,
     * duplicates and elements which are not valid, existing directories are
     * skipped.
     *
     * @return an {@link Iterable} which will traverse all valid elements on the
     * <em>PATH</em> environment variables.
     */

    /*FIXME: This method has been copied from the ruby.platform module.
     *  ruby.platform/src/org/netbeans/modules/ruby/platform/Util.java
     *
     * I don't know if it could be included into a shared module.
    */
    private static Iterable<String> dirsOnPath() {
        String rawPath = System.getenv("PATH"); // NOI18N
        if (rawPath == null) {
            rawPath = System.getenv("Path"); // NOI18N
        }
        if (rawPath == null) {
            return Collections.emptyList();
        }
        Set<String> candidates = new LinkedHashSet<String>(Arrays.asList(rawPath.split(File.pathSeparator)));
        for (Iterator<String> it = candidates.iterator(); it.hasNext();) {
            String dir = it.next();
            if (!new File(dir).isDirectory()) { // remove non-existing directories (#124562)
                it.remove();
            }
        }
        return NbCollections.iterable(candidates.iterator());
    }
}
