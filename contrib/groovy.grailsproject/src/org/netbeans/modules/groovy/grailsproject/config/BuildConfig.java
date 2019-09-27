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

package org.netbeans.modules.groovy.grailsproject.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public final class BuildConfig {

    public static final String BUILD_CONFIG_PLUGINS = BuildConfig.class.getName() + ".plugins";
    private static final Logger LOGGER = Logger.getLogger(BuildConfig.class.getName());

    private final GrailsProject project;
    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    private Object buildSettingsInstance;
    private File projectRoot;
    private List<GrailsPlugin> localPlugins;
    private File projectPluginsDir;
    private File globalPluginsDir;
    

    public BuildConfig(GrailsProject project) {
        this.project = project;
        this.projectRoot = FileUtil.toFile(project.getProjectDirectory());

        loadLocalPluginsDefault();
        loadProjectPluginsDirDefault();
        loadGlobalPluginsDirDefault();
    }

    private boolean isBuildConfigPresent() {
        FileObject root = project.getProjectDirectory();
        return root.getFileObject("grails-app/conf/BuildConfig.groovy") != null; // NOI18N
    }

    private synchronized void loadProjectPluginsDirDefault() {
        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
            File cached = config.getProjectPluginsDir();
            if (cached != null && isBuildConfigPresent()) {
                projectPluginsDir = FileUtil.normalizeFile(cached);
            } else {
                projectPluginsDir = getProjectPluginsDirDefault11();
            }
        } else {
            projectPluginsDir = getProjectPluginsDir10();
        }
    }

    private File getProjectPluginsDirDefault11() {
        GrailsPlatform platform = GrailsProjectConfig.forProject(project).getGrailsPlatform();

        File pluginsDirFile;
        String strPluginsDir = System.getProperty(GrailsProjectConfig.GRAILS_PROJECT_PLUGINS_DIR_PROPERTY);
        if (strPluginsDir == null) {
            File projectWorkDirFile;
            String projectWorkDir = System.getProperty(GrailsProjectConfig.GRAILS_PROJECT_WORK_DIR_PROPERTY);
            if (projectWorkDir == null) {
                File workDirFile;
                String workDir = System.getProperty(GrailsProjectConfig.GRAILS_WORK_DIR_PROPERTY);
                if (workDir == null) {
                    workDir = System.getProperty("user.home"); // NOI18N
                    workDir = workDir + File.separator + ".grails" + File.separator + platform.getVersion(); // NOI18N
                    workDirFile = new File(workDir);
                } else {
                    workDirFile = new File(workDir);
                    if (!workDirFile.isAbsolute()) {
                        workDirFile = new File(projectRoot, workDir);
                    }
                }
                projectWorkDirFile = new File(workDirFile, "projects" + File.separator + projectRoot.getName()); // NOI18N
            } else {
                projectWorkDirFile = new File(projectWorkDir);
                if (!projectWorkDirFile.isAbsolute()) {
                    projectWorkDirFile = new File(projectRoot, projectWorkDir);
                }
            }
            pluginsDirFile = new File(projectWorkDirFile, "plugins"); // NOI18N
        } else {
            pluginsDirFile = new File(strPluginsDir);
            if (!pluginsDirFile.isAbsolute()) {
                pluginsDirFile = new File(projectRoot, strPluginsDir);
            }
        }

        return FileUtil.normalizeFile(pluginsDirFile);
    }

    /**
     * Returns {@link File} representing Ivy cache used for plugin jars.
     *
     * In case if "grails.dependency.cache.dir" property is set either in BuildConfig.groovy
     * or in settings.groovy we will use that one. If not, the default location is used.
     *
     * @return {@link File} representing Ivy cache used for plugin jars
     */
    public File getIvyCacheDir() {
        File ivyCacheDir;
        String ivyCache = System.getProperty(GrailsProjectConfig.GRAILS_IVY_CACHE_DIR_PROPERTY);
        if (ivyCache == null) {
            File workDirFile;
            String workDir = System.getProperty(GrailsProjectConfig.GRAILS_WORK_DIR_PROPERTY);
            if (workDir == null) {
                workDir = System.getProperty("user.home"); // NOI18N
                workDir = workDir + File.separator + ".grails"; // NOI18N
                workDirFile = new File(workDir);
            } else {
                workDirFile = new File(workDir);
                if (!workDirFile.isAbsolute()) {
                    workDirFile = new File(projectRoot, workDir);
                }
            }
            ivyCacheDir = new File(workDirFile, "ivy-cache"); // NOI18N
        } else {
            ivyCacheDir = new File(ivyCache);
            if (!ivyCacheDir.isAbsolute()) {
                ivyCacheDir = new File(projectRoot, ivyCache);
            }
        }
        return ivyCacheDir;
    }

    private synchronized void loadGlobalPluginsDirDefault() {
        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
            File cached = config.getGlobalPluginsDir();
            if (cached != null && isBuildConfigPresent()) {
                globalPluginsDir = FileUtil.normalizeFile(cached);
            } else {
                globalPluginsDir = getGlobalPluginsDirDefault11();
            }
        } else {
            globalPluginsDir = getGlobalPluginsDir10();
        }
    }

    private File getGlobalPluginsDirDefault11() {
        GrailsPlatform platform = GrailsProjectConfig.forProject(project).getGrailsPlatform();

        File pluginsDirFile;
        String strPluginsDir = System.getProperty(GrailsProjectConfig.GRAILS_GLOBAL_PLUGINS_DIR_PROPERTY);
        if (strPluginsDir == null) {
            File workDirFile;
            String workDir = System.getProperty(GrailsProjectConfig.GRAILS_WORK_DIR_PROPERTY);
            if (workDir == null) {
                workDir = System.getProperty("user.home"); // NOI18N
                workDir = workDir + File.separator + ".grails" + File.separator + platform.getVersion(); // NOI18N
                workDirFile = new File(workDir);
            } else {
                workDirFile = new File(workDir);
                if (!workDirFile.isAbsolute()) {
                    workDirFile = new File(projectRoot, workDir);
                }
            }
            pluginsDirFile = new File(workDirFile, "global-plugins"); // NOI18N
        } else {
            pluginsDirFile = new File(strPluginsDir);
            if (!pluginsDirFile.isAbsolute()) {
                pluginsDirFile = new File(projectRoot, strPluginsDir);
            }
        }

        return pluginsDirFile;
    }

    private synchronized void loadLocalPluginsDefault() {
        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
            Map<String, File> cached = config.getLocalPlugins();
            if (cached != null && isBuildConfigPresent()) {
                localPlugins = new ArrayList<>();
                for (Map.Entry<String, File> entry : cached.entrySet()) {
                    localPlugins.add(new GrailsPlugin(entry.getKey(), null, null, entry.getValue()));
                }
            } else {
                localPlugins = Collections.emptyList();
            }
        } else {
            localPlugins = Collections.emptyList();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public void reload() {
        long start = System.currentTimeMillis();

        File currentProjectPluginsDir;
        File currentGlobalPluginsDir;
        List<GrailsPlugin> currentLocalPlugins;

        synchronized (this) {
            File newProjectRoot = FileUtil.toFile(project.getProjectDirectory());
            assert newProjectRoot != null;

            if (!newProjectRoot.equals(projectRoot)) {
                projectRoot = newProjectRoot;
            }

            buildSettingsInstance = loadBuildSettings();
            LOGGER.log(Level.FINE, "Took {0} ms to load BuildSettings for {1}",
                    new Object[] {(System.currentTimeMillis() - start), project.getProjectDirectory().getNameExt()});

            currentLocalPlugins = loadLocalPlugins();
            currentProjectPluginsDir = loadProjectPluginsDir();
            currentGlobalPluginsDir = loadGlobalPluginsDir();
        }

        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            GrailsProjectConfig config = project.getLookup().lookup(GrailsProjectConfig.class);
            if (config != null) {
                ProjectConfigListener listener = new ProjectConfigListener();

                config.addPropertyChangeListener(listener);
                try {
                    config.setProjectPluginsDir(FileUtil.normalizeFile(currentProjectPluginsDir));
                    config.setGlobalPluginsDir(FileUtil.normalizeFile(currentGlobalPluginsDir));

                    Map<String, File> prepared = new HashMap<>();
                    for (GrailsPlugin plugin : currentLocalPlugins) {
                        prepared.put(plugin.getName(), plugin.getPath());
                    }
                    config.setLocalPlugins(prepared);
                } finally {
                    config.removePropertyChangeListener(listener);
                }
                if (listener.isChanged()) {
                    propertySupport.firePropertyChange(BUILD_CONFIG_PLUGINS, null, null);
                }
            }
        }
    }

    public synchronized File getProjectPluginsDir() {
        return projectPluginsDir;
    }

    private synchronized File loadProjectPluginsDir() {
        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            projectPluginsDir = getProjectPluginsDir11();
        } else {
            projectPluginsDir = getProjectPluginsDir10();
        }
        return projectPluginsDir;
    }

    public synchronized List<File> getCompileDependencies() {
        try {
            if (buildSettingsInstance != null) {
                Method getCompileDependenciesMethod = buildSettingsInstance.getClass().getMethod("getCompileDependencies", new Class[] {}); // NOI18N
                return (List<File>) getCompileDependenciesMethod.invoke(buildSettingsInstance, new Object[] {});
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return Collections.emptyList();
    }

    private File getProjectPluginsDir10() {
        assert Thread.holdsLock(this);
        return FileUtil.normalizeFile(new File(projectRoot, "plugins")); // NOI18N
    }

    private File getProjectPluginsDir11() {
        assert Thread.holdsLock(this);
        try {
            if (buildSettingsInstance != null) {
                Method getProjectPluginsDirMethod = buildSettingsInstance.getClass().getMethod("getProjectPluginsDir", // NOI18N
                        new Class[] {});
                Object value = getProjectPluginsDirMethod.invoke(buildSettingsInstance, new Object[] {});

                if (value instanceof File) {
                    File file = (File) value;
                    if (!file.isAbsolute()) {
                        file = new File(projectRoot, file.getPath());
                    }
                    return FileUtil.normalizeFile(file);
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        GrailsPlatform platform = config.getGrailsPlatform();
        if (platform.isConfigured()) {
            return FileUtil.normalizeFile(new File(System.getProperty("user.home"), ".grails" + File.separator // NOI18N
                    + config.getGrailsPlatform().getVersion().toString() + File.separator
                    + "projects" + File.separator + projectRoot.getName() + File.separator + "plugins")); // NOI18N
        }
        return null;
    }

    public synchronized File getGlobalPluginsDir() {
        return globalPluginsDir;
    }

    private synchronized File loadGlobalPluginsDir() {
        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            globalPluginsDir = getGlobalPluginsDir11();
        } else {
            globalPluginsDir = getGlobalPluginsDir10();
        }
        return globalPluginsDir;
    }

    private File getGlobalPluginsDir10() {
        assert Thread.holdsLock(this);
        return null;
    }

    private File getGlobalPluginsDir11() {
        assert Thread.holdsLock(this);
        try {
            if (buildSettingsInstance != null) {
                Method getGlobalPluginsDirMethod = buildSettingsInstance.getClass().getMethod("getGlobalPluginsDir", // NOI18N
                        new Class[] {});
                Object value = getGlobalPluginsDirMethod.invoke(buildSettingsInstance, new Object[] {});

                if (value instanceof File) {
                    File file = (File) value;
                    if (!file.isAbsolute()) {
                        file = new File(projectRoot, file.getPath());
                    }
                    return FileUtil.normalizeFile(file);
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        GrailsPlatform platform = config.getGrailsPlatform();
        if (platform.isConfigured()) {
            return FileUtil.normalizeFile(new File(System.getProperty("user.home"), ".grails" + File.separator // NOI18N
                    + config.getGrailsPlatform().getVersion().toString() + File.separator + "global-plugins")); // NOI18N
        }
        return null;
    }

    public synchronized List<GrailsPlugin> getLocalPlugins() {
        return Collections.unmodifiableList(localPlugins);
    }

    private synchronized List<GrailsPlugin> loadLocalPlugins() {
        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            localPlugins = getLocalPlugins11();
        } else {
            localPlugins = getLocalPlugins10();
        }
        return localPlugins;
    }

    private List<GrailsPlugin> getLocalPlugins10() {
        assert Thread.holdsLock(this);
        return Collections.emptyList();
    }

    private List<GrailsPlugin> getLocalPlugins11() {
        assert Thread.holdsLock(this);
        try {
            if (buildSettingsInstance != null) {
                Method getConfigMethod = buildSettingsInstance.getClass().getMethod("getConfig", // NOI18N
                        new Class[] {});
                Object configValue = getConfigMethod.invoke(buildSettingsInstance, new Object[] {});

                Method toPropertiesMethod = configValue.getClass().getMethod("toProperties", new Class[] {}); // NOI18N
                Object converted = toPropertiesMethod .invoke(configValue, new Object[] {});

                if (converted instanceof Properties) {
                    Properties properties = (Properties) converted;
                    List<GrailsPlugin> plugins = new ArrayList<>();
                    for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
                        String key = (String) e.nextElement();
                        if (key.startsWith("grails.plugin.location.")) { // NOI18N
                            String value = properties.getProperty(key);
                            key = key.substring("grails.plugin.location.".length()); // NOI18N
                            File file = new File(value);
                            if (!file.isAbsolute()) {
                                file = new File(projectRoot, value);
                            }
                            plugins.add(new GrailsPlugin(key, null, null, file));
                        }
                    }
                    return plugins;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        return Collections.emptyList();
    }

    private Object loadBuildSettings() {
        assert Thread.holdsLock(this);
        GrailsPlatform platform = GrailsProjectConfig.forProject(project).getGrailsPlatform();
        if (!platform.isConfigured()) {
            return null;
        }

        ClassLoader loader = platform.getClassPath().getClassLoader(true);
        URLClassLoader urlLoader;
        if (loader instanceof URLClassLoader) {
            urlLoader = (URLClassLoader) loader;
        } else {
            urlLoader = new URLClassLoader(new URL[] {}, loader);
        }

        try {
            Class<?> clazz = urlLoader.loadClass("grails.util.BuildSettings"); // NOI18N
            Constructor contructor = clazz.getConstructor(File.class, File.class);
            Object instance = contructor.newInstance(platform.getGrailsHome(), projectRoot);

            Method setRootLoaderMethod = clazz.getMethod("setRootLoader", new Class[] {URLClassLoader.class}); // NOI18N
            setRootLoaderMethod.invoke(instance, new Object[] {urlLoader});

            Method loadConfigMethod = clazz.getMethod("loadConfig", new Class[] {}); // NOI18N
            loadConfigMethod.invoke(instance, new Object[] {});

            return instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return null;
    }

    private static class ProjectConfigListener implements PropertyChangeListener {

        private boolean changed = false;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (GrailsProjectConfig.GRAILS_PROJECT_PLUGINS_DIR_PROPERTY.equals(evt.getPropertyName())
                    || GrailsProjectConfig.GRAILS_GLOBAL_PLUGINS_DIR_PROPERTY.equals(evt.getPropertyName())
                    || GrailsProjectConfig.GRAILS_LOCAL_PLUGINS_PROPERTY.equals(evt.getPropertyName())) {

                synchronized (this) {
                    changed = true;
                }
            }
        }

        public synchronized boolean isChanged() {
            return changed;
        }
    }
}
