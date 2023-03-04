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

package org.netbeans.modules.groovy.grails.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;


/**
 * Represents IDE configuration of the Grails project.
 *
 * @author schmidtm, Petr Hejl
 */
// FIXME move this to project support
public final class GrailsProjectConfig {

    public static final String GRAILS_PORT_PROPERTY = "grails.port"; // NOI18N

    public static final String GRAILS_ENVIRONMENT_PROPERTY = "grails.environment"; // NOI18N

    public static final String GRAILS_JAVA_PLATFORM_PROPERTY = "grails.java.platform"; // NOI18N

    public static final String GRAILS_PLATFORM_PROPERTY = "grails.platform"; // NOI18N

    public static final String GRAILS_DEBUG_BROWSER_PROPERTY = "grails.debug.browser"; // NOI18N

    public static final String GRAILS_DISPLAY_BROWSER_PROPERTY = "grails.display.browser"; // NOI18N

    public static final String GRAILS_PROJECT_PLUGINS_DIR_PROPERTY = "grails.project.plugins.dir"; // NOI18N

    public static final String GRAILS_GLOBAL_PLUGINS_DIR_PROPERTY = "grails.global.plugins.dir"; // NOI18N

    public static final String GRAILS_PROJECT_WORK_DIR_PROPERTY = "grails.project.work.dir"; // NOI18N

    public static final String GRAILS_WORK_DIR_PROPERTY = "grails.work.dir"; // NOI18N

    public static final String GRAILS_IVY_CACHE_DIR_PROPERTY = "grails.dependency.cache.dir"; // NOI18N

    public static final String GRAILS_LOCAL_PLUGINS_PROPERTY = "grails.local.plugins"; // NOI18N

    public static final String GRAILS_VM_OPTIONS_PROPERTY = "grails.vm.options"; // NOI18N

    private static final String DEFAULT_PORT = "8080"; // NOI18N

    private static final JavaPlatformManager PLATFORM_MANAGER  = JavaPlatformManager.getDefault();

    private final Project prj;

    private final GrailsSettings settings = GrailsSettings.getInstance();

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final PlatformChangeListener platformChangeListener = new PlatformChangeListener(propertyChangeSupport);

    public GrailsProjectConfig(Project prj) {
        this.prj = prj;
    }

    // FIXME this should be removed when real multiple platforms will be available
    public void initListeners() {
        GrailsPlatform platform = GrailsPlatform.getDefault();
        platform.addChangeListener(WeakListeners.change(platformChangeListener, platform));
    }

    /**
     * Returns the configuration of the given project.
     *
     * @param project project for which the returned configuration will serve
     * @return the configuration of the given project
     */
    // FIXME remove
    public static GrailsProjectConfig forProject(Project project) {
        GrailsProjectConfig config = project.getLookup().lookup(GrailsProjectConfig.class);

        return config;
    }

    /**
     * Returns the project for wich the configuration is used.
     *
     * @return the project for wich the configuration is used
     */
    public Project getProject() {
        return prj;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Returns the port configured for the project.
     *
     * @return the port configured for the project
     */
    public String getPort() {
        synchronized (settings) {
            String port = settings.getPortForProject(prj);
            if (port == null) {
                port = DEFAULT_PORT;
            }
            return port;
        }
    }

    /**
     * Sets the port for the project.
     *
     * @param port the port to set
     */
    public void setPort(String port) {
        assert port != null;
        String oldValue;
        synchronized (settings) {
            oldValue = getPort();
            settings.setPortForProject(prj, port);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_PORT_PROPERTY, oldValue, port);
    }

    /**
     * Returns the VM options configured for the project.
     *
     * @return the port configured for the project
     */
    public String getVmOptions() {
        synchronized (settings) {
            String port = settings.getVmOptionsForProject(prj);
            if (port == null) {
                port = ""; // NOI18N
            }
            return port;
        }
    }

    /**
     * Sets the VM options for the project.
     *
     * @param options VM options to set
     */
    public void setVmOptions(String options) {
        assert options != null;
        String oldValue;
        synchronized (settings) {
            oldValue = getPort();
            settings.setVmOptionsForProject(prj, options);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_VM_OPTIONS_PROPERTY, oldValue, options);
    }

    /**
     * Returns the environment configured for the project.
     *
     * @return the environment configured for the project or <code>null</code>
     *             if no environment has been configured yet
     */
    public GrailsEnvironment getEnvironment() {
        synchronized (settings) {
            return settings.getEnvForProject(prj);
        }
    }

    /**
     * Sets the environment for the project.
     *
     * @param env the environment to set
     */
    public void setEnvironment(GrailsEnvironment env) {
        assert env != null;
        GrailsEnvironment oldValue;
        synchronized (settings) {
            oldValue = getEnvironment();
            settings.setEnvForProject(prj, env);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_ENVIRONMENT_PROPERTY, oldValue, env);
    }

    /**
     * Returns the browser configured for the project.
     *
     * @return the browser configured for the project or <code>null</code>
     *             if no browser has been configured yet
     */
    public String getDebugBrowser() {
        synchronized (settings) {
            return settings.getDebugBrowserForProject(prj);
        }
    }

    /**
     * Sets the browser for the project.
     *
     * @param browser browser to set
     */
    public void setDebugBrowser(String browser) {
        assert browser != null;
        String oldValue;
        synchronized (settings) {
            oldValue = getDebugBrowser();
            settings.setDebugBrowserProject(prj, browser);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_DEBUG_BROWSER_PROPERTY, oldValue, browser);
    }

    public JavaPlatform getJavaPlatform() {
        String platformId;
        synchronized (settings) {
            platformId = settings.getJavaPlatformForProject(prj);
        }

        if (platformId == null) {
            return JavaPlatform.getDefault();
        }

        JavaPlatform[] platforms = PLATFORM_MANAGER.getPlatforms(null, new Specification("j2se", null)); //NOI18N
        for (JavaPlatform platform : platforms) {
            if (platform.getInstallFolders().size() > 0) {
                String antName = platform.getProperties().get("platform.ant.name"); //NOI18N
                if (platformId.equals(antName)) {
                    return platform;
                }
            }
        }
        return JavaPlatform.getDefault();
    }

    public void setJavaPlatform(JavaPlatform platform) {
        assert platform != null;
        JavaPlatform oldValue;
        synchronized (settings) {
            oldValue = getJavaPlatform();
            settings.setJavaPlatformForProject(prj, platform.getProperties().get("platform.ant.name"));
        }
        propertyChangeSupport.firePropertyChange(GRAILS_JAVA_PLATFORM_PROPERTY, oldValue, platform);
    }

    public GrailsPlatform getGrailsPlatform() {
        GrailsPlatform runtime = GrailsPlatform.getDefault();
        return runtime;
//        if (runtime.isConfigured()) {
//            return runtime;
//        }
//        return null;
    }

    /**
     * Returns the display browser flag of the project.
     *
     * @return the display browser flag of the project
     */
    public boolean getDisplayBrowser() {
        synchronized (settings) {
            return settings.getDisplayBrowserForProject(prj);
        }
    }

    /**
     * Sets the display browser flag of the project.
     *
     * @param displayBrowser display browser flag to set
     */
    public void setDisplayBrowser(boolean displayBrowser) {
        boolean oldValue;
        synchronized (this) {
            oldValue = getDisplayBrowser();
            settings.setDisplayBrowserForProject(prj, displayBrowser);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_DISPLAY_BROWSER_PROPERTY, oldValue, displayBrowser);
    }

    public File getProjectPluginsDir() {
        synchronized (settings) {
            String value = settings.getProjectPluginsDirForProject(prj);
            if (value != null) {
                return new File(value);
            }
            return null;
        }
    }

    public void setProjectPluginsDir(File dir) {
        assert FileUtil.normalizeFile(dir).equals(dir);
        
        File oldValue;
        synchronized (this) {
            oldValue = getProjectPluginsDir();
            settings.setProjectPluginsDirForProject(prj, dir.getAbsolutePath());
        }
        propertyChangeSupport.firePropertyChange(GRAILS_PROJECT_PLUGINS_DIR_PROPERTY, oldValue, dir);
    }

    public File getGlobalPluginsDir() {
        synchronized (settings) {
            String value = settings.getGlobalPluginsDirForProject(prj);
            if (value != null) {
                return new File(value);
            }
            return null;
        }
    }

    public void setGlobalPluginsDir(File dir) {
        assert FileUtil.normalizeFile(dir).equals(dir);

        File oldValue;
        synchronized (this) {
            oldValue = getGlobalPluginsDir();
            settings.setGlobalPluginsDirForProject(prj, dir.getAbsolutePath());
        }
        propertyChangeSupport.firePropertyChange(GRAILS_GLOBAL_PLUGINS_DIR_PROPERTY, oldValue, dir);
    }

    public Map<String, File> getLocalPlugins() {
        synchronized (settings) {
            Map<String, String> value = settings.getLocalPluginsForProject(prj);
            if (value != null) {
                File base = FileUtil.toFile(prj.getProjectDirectory());
                Map<String, File> ret = new HashMap<String, File>();
                for (Map.Entry<String, String> entry : value.entrySet()) {
                    File file = new File(entry.getValue());
                    if (!file.isAbsolute()) {
                        file = new File(base, entry.getValue());
                    }
                    ret.put(entry.getKey(), file);
                }
                return ret;
            }
            return null;
        }
    }

    public void setLocalPlugins(Map<String, File> plugins) {
        Map<String, File> oldValue;
        boolean changed = false;
        synchronized (this) {
            oldValue = getLocalPlugins();
            Map<String, String> prepared = new HashMap<String, String>();
            for (Map.Entry<String, File> entry : plugins.entrySet()) {
                prepared.put(entry.getKey(), entry.getValue().getAbsolutePath());

                File oldFile = oldValue.remove(entry.getKey());
                if (oldFile == null || !oldFile.equals(entry.getValue())) {
                    changed = true;
                }
            }
            settings.setLocalPluginsForProject(prj, prepared);
        }

        if (changed || !oldValue.isEmpty()) {
            propertyChangeSupport.firePropertyChange(GRAILS_LOCAL_PLUGINS_PROPERTY, oldValue, plugins);
        }
    }

    private static class PlatformChangeListener implements ChangeListener {

        private final PropertyChangeSupport propertyChangeSupport;

        public PlatformChangeListener(PropertyChangeSupport propertyChangeSupport) {
            this.propertyChangeSupport = propertyChangeSupport;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            propertyChangeSupport.firePropertyChange(GRAILS_PLATFORM_PROPERTY, null, null);
        }
    }
}