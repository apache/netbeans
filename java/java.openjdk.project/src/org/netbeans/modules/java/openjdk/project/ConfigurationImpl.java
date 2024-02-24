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
package org.netbeans.modules.java.openjdk.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.netbeans.spi.java.platform.JavaPlatformFactory;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class ConfigurationImpl implements ProjectConfiguration {

    private final File location;
    private final boolean missing;

    public ConfigurationImpl(File location, boolean missing) {
        this.location = location;
        this.missing = missing;
    }

    @Override
    public String getDisplayName() {
        return (missing ? "<html><font color='#FF0000'>" : "") + location.getName();
    }

    public File getLocation() {
        return location;
    }

    private static final Map<FileObject, ProviderImpl> jdkRoot2ConfigurationProvider = new HashMap<>();

    public static ProviderImpl getProvider(FileObject jdkRoot) {
        ProviderImpl provider = jdkRoot2ConfigurationProvider.get(jdkRoot);

        if (provider == null) {
            jdkRoot2ConfigurationProvider.put(jdkRoot, provider = new ProviderImpl(jdkRoot, null));
        }

        return provider;
    }

    public static final class ProviderImpl implements ProjectConfigurationProvider<ConfigurationImpl>, FileChangeListener {

        private static final RequestProcessor WORKER = new RequestProcessor(ProviderImpl.class.getName(), 1, false, false);
        private final FileObject jdkRoot;
        private final File buildDir;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Set<File> buildDirsWithListeners = new HashSet<>();
        private List<ConfigurationImpl> configurations = Collections.emptyList();
        private ConfigurationImpl active;

        public ProviderImpl(FileObject jdkRoot, File buildDir) {
            this.jdkRoot = jdkRoot;
            this.buildDir = new File(FileUtil.toFile(jdkRoot), "build");
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                @Override
                public void run() {
                    FileUtil.addFileChangeListener(ProviderImpl.this, ProviderImpl.this.buildDir);
                    updateConfigurations();
                }
            });
        }

        private static final String PROP_ACTIVE_CONFIGURATION = "activeConfiguration";
        
        private synchronized void updateConfigurations() {
            File[] dirs = buildDir.listFiles(new FileFilter() {
                @Override public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            
            Map<File, ConfigurationImpl> configurations2Remove = new HashMap<>();

            for (ConfigurationImpl c : configurations) {
                configurations2Remove.put(c.getLocation(), c);
            }

            File dirToSelect;

            if (active != null) {
                dirToSelect = active.getLocation();
            } else {
                Preferences prefs = prefs();
                String activeConfig = prefs != null ? prefs.get(PROP_ACTIVE_CONFIGURATION, null) : null;

                if (activeConfig != null) {
                    dirToSelect = new File(activeConfig);
                } else {
                    dirToSelect = null;
                }
            }

            Set<File> missingBuildDirs = new HashSet<>(buildDirsWithListeners);
            List<ConfigurationImpl> newConfigurations = new ArrayList<>();
            ConfigurationImpl newActive = null;

            if (dirs != null) {
                for (File dir : dirs) {
                    if (!missingBuildDirs.remove(dir)) {
                        FileUtil.addFileChangeListener(this, dir);
                        buildDirsWithListeners.add(dir);
                    }
                    if (!new File(dir, "Makefile").canRead())
                        continue;
                    
                    ConfigurationImpl current = configurations2Remove.remove(dir);

                    if (current != null) newConfigurations.add(current);
                    else newConfigurations.add(current = new ConfigurationImpl(dir, false));

                    if (dir.equals(dirToSelect) || (dirToSelect == null && newActive == null)) {
                        newActive = current;
                    }
                }
            }

            for (File removedDir : missingBuildDirs) {
                FileUtil.removeFileChangeListener(this, removedDir);
                buildDirsWithListeners.remove(removedDir);
            }

            if (newActive == null && dirToSelect != null) {
                newActive = new ConfigurationImpl(dirToSelect, true);
                newConfigurations.add(0, newActive);
            }

            newConfigurations.sort(new Comparator<ConfigurationImpl>() {
                @Override public int compare(ConfigurationImpl o1, ConfigurationImpl o2) {
                    return o1.getLocation().getName().compareTo(o2.getLocation().getName());
                }
            });

            configurations = newConfigurations;
            
            pcs.firePropertyChange(PROP_CONFIGURATIONS, null, null);
            if (active != newActive) {
                setActiveConfiguration(newActive);
            }
        }

        @Override
        public synchronized Collection<ConfigurationImpl> getConfigurations() {
            return configurations;
        }

        @Override
        public synchronized ConfigurationImpl getActiveConfiguration() {
            return active;
        }

        @Override
        public synchronized void setActiveConfiguration(ConfigurationImpl configuration) {
            this.active = configuration;
            try {
                jdkRoot.setAttribute(BuildUtils.NB_JDK_PROJECT_BUILD, configuration.location);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            pcs.firePropertyChange(PROP_CONFIGURATION_ACTIVE, null, active);
            Preferences prefs = prefs();
            if (prefs != null)
                prefs.put(PROP_ACTIVE_CONFIGURATION, configuration.getLocation().getAbsolutePath());
            WORKER.post(() -> checkAndRegisterPlatform());
        }

        private Preferences prefs() {
            FileObject javaBase = BuildUtils.getFileObject(jdkRoot, "jdk/src/java.base");
            if (javaBase == null)
                javaBase = BuildUtils.getFileObject(jdkRoot, "jdk");
            Project javaBaseProject = javaBase != null ? FileOwnerQuery.getOwner(javaBase) : null;

            if (javaBaseProject != null) {
                return ProjectUtils.getPreferences(javaBaseProject, ConfigurationImpl.class, false);
            } else {
                return null;
            }
        }

        private void checkAndRegisterPlatform() {
            if (active.missing)
                return ;
            String name = jdkRoot.getNameExt() + " - " + active.getDisplayName();
            FileObject target = FileUtil.toFileObject(new File(active.getLocation(), "jdk"));
            if (target == null || BuildUtils.getFileObject(target, "bin/java") == null) {
                return ;
            }
            for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                if (platform.getInstallFolders().stream().anyMatch(folder -> folder.equals(target))) {
                    //found, skip:
                    return ;
                }
                if (name.equals(platform.getDisplayName())) {
                    //platform with the same name exists, skip:
                    return ;
                }
            }
            for (JavaPlatformFactory.Provider provider : Lookup.getDefault().lookupAll(JavaPlatformFactory.Provider.class)) {
                JavaPlatformFactory factory = provider.forType("j2se");
                if (factory != null) {
                    try {
                        factory.create(target, name, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        @Override
        public boolean hasCustomizer() {
            return false;
        }

        @Override
        public void customize() {
        }

        @Override
        public boolean configurationsAffectAction(String command) {
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener lst) {
            pcs.addPropertyChangeListener(lst);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener lst) {
            pcs.removePropertyChangeListener(lst);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            updateConfigurations();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            updateConfigurations();
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            updateConfigurations();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            updateConfigurations();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

    }

}
