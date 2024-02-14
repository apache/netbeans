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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Manages configurations for a PHP project (copy/pasted from Java SE project).
 * @author Jesse Glick, Radek Matous
 */
public final class PhpConfigurationProvider implements ProjectConfigurationProvider<PhpConfigurationProvider.Config> {

    private static final Logger LOGGER = Logger.getLogger(PhpConfigurationProvider.class.getName());
    /**
     * Ant property name for active config.
     */
    public static final String PROP_CONFIG = "config"; // NOI18N

    /**
     * Ant property file which specified active config.
     */
    public static final String CONFIG_PROPS_PATH = "nbproject/private/config.properties"; // NOI18N


    public static final class Config implements ProjectConfiguration {

        /** The file basename, or <code>null</code> for default config. */
        public final String name;
        private final String displayName;

        public Config(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Config) && Utilities.compareObjects(name, ((Config) o).name);
        }

        @Override
        public String toString() {
            return "PhpConfigurationProvider.Config[" + name + "," + displayName + "]"; // NOI18N

        }
    }
    private static final Config DEFAULT = new Config(null,
            NbBundle.getMessage(PhpConfigurationProvider.class, "LBL_DefaultConfiguration"));
    private final PhpProject project;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final FileChangeListener fcl = new FileChangeAdapter() {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            update(fe);
        }

        private void update(FileEvent ev) {
            LOGGER.log(Level.FINEST, "Received {0}", ev);
            Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
            configDir = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N

            if (configDir != null) {
                configDir.removeFileChangeListener(fclWeak);
                configDir.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "(Re-)added listener to {0}", configDir);
            } else {
                LOGGER.log(Level.FINEST, "No nbproject/configs exists");
            }
            calculateConfigs();
            Set<String> newConfigs = configs.keySet();
            if (!oldConfigs.equals(newConfigs)) {
                LOGGER.log(Level.FINER, "Firing " + ProjectConfigurationProvider.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[]{oldConfigs, newConfigs});
                pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
            // XXX also fire PROP_ACTIVE_CONFIGURATION?
            }
        }
    };
    private final FileChangeListener fclWeak;
    private FileObject configDir;
    private Map<String, Config> configs;
    private final FileObject nbp;

    public PhpConfigurationProvider(PhpProject project) {
        this.project = project;
        fclWeak = FileUtil.weakFileChangeListener(fcl, null);
        nbp = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N

        if (nbp != null) {
            nbp.addFileChangeListener(fclWeak);
            LOGGER.log(Level.FINEST, "Added listener to {0}", nbp);
            configDir = nbp.getFileObject("configs"); // NOI18N

            if (configDir != null) {
                configDir.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "Added listener to {0}", configDir);
            }
        }
        project.getEvaluator().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PROP_CONFIG.equals(evt.getPropertyName())) {
                    LOGGER.log(Level.FINER, "Refiring " + PROP_CONFIG + " -> " + ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE);
                    pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
                }
            }
        });
    }

    private void calculateConfigs() {
        configs = new HashMap<>();
        if (configDir != null) {
            for (FileObject kid : configDir.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                try {
                    try (InputStream is = kid.getInputStream()) {
                        Properties p = new Properties();
                        p.load(is);
                        String name = kid.getName();
                        String label = p.getProperty(ConfigManager.PROP_DISPLAY_NAME);

                        configs.put(name, new Config(name, label != null ? label : name));
                    }
                } catch (IOException x) {
                    LOGGER.log(Level.INFO, null, x);
                }
            }
        }
        LOGGER.log(Level.FINEST, "Calculated configurations: {0}", configs);
    }

    @Override
    public Collection<Config> getConfigurations() {
        calculateConfigs();
        List<Config> l = new ArrayList<>();
        l.addAll(configs.values());
        l.sort(new Comparator<Config>() {

            Collator c = Collator.getInstance();

            @Override
            public int compare(Config c1, Config c2) {
                return c.compare(c1.getDisplayName(), c2.getDisplayName());
            }
        });
        l.add(0, DEFAULT);
        return l;
    }

    @Override
    public Config getActiveConfiguration() {
        calculateConfigs();
        String config = project.getEvaluator().getProperty(PROP_CONFIG);
        if (config != null && configs.containsKey(config)) {
            return configs.get(config);
        } else {
            return DEFAULT;
        }
    }

    @Override
    public void setActiveConfiguration(Config c) throws IOException {
        if (c != DEFAULT && !configs.containsValue(c)) {
            throw new IllegalArgumentException();
        }
        final String n = c.name;
        EditableProperties ep = project.getHelper().getProperties(CONFIG_PROPS_PATH);
        if (Utilities.compareObjects(n, ep.getProperty(PROP_CONFIG))) {
            return;
        }
        if (n != null) {
            ep.setProperty(PROP_CONFIG, n);
        } else {
            ep.remove(PROP_CONFIG);
        }
        project.getHelper().putProperties(CONFIG_PROPS_PATH, ep);
        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        ProjectManager.getDefault().saveProject(project);
        assert project.getProjectDirectory().getFileObject(CONFIG_PROPS_PATH) != null;
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        PhpProjectUtils.openCustomizerRun(project);
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return command.equals(ActionProvider.COMMAND_RUN)
                || command.equals(ActionProvider.COMMAND_DEBUG);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }
}
