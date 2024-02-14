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

package org.netbeans.modules.java.api.common.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Support for {@link ProjectConfiguration}s in Ant based project.
 * @author Jesse Glick
 * @author Tomas Zezula
 * @since 1.64
 */
public class ProjectConfigurations {

    private static final Logger LOGGER = Logger.getLogger(ProjectConfigurations.class.getName());

    /**
     * Path to the file holding the active configuration.
     */
    public static final String CONFIG_PROPS_PATH = "nbproject/private/config.properties"; // NOI18N

    private ProjectConfigurations() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    /**
     * Creates a {@link ConfigurationProviderBuilder}.
     * @param project the {@link Project} to create builder for
     * @param eval the {@link Project}'s {@link PropertyEvaluator}
     * @param updateHelper the {@link Project}'s {@link UpdateHelper}
     * @return the {@link ConfigurationProviderBuilder}
     */
    @NonNull
    public static ConfigurationProviderBuilder createConfigurationProviderBuilder(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper updateHelper) {
        return new ConfigurationProviderBuilder(project, eval, updateHelper);
    }

    /**
     * Creates a {@link PropertyEvaluator} with {@link ProjectConfiguration} support.
     * @param project the project to create {@link PropertyEvaluator} for
     * @param helper the {@link Project}'s {@link AntProjectHelper}
     * @param additionalPropertyProviders the additional {@link PropertyProvider}s
     * @return a new {@link PropertyEvaluator}
     */
    @NonNull
    public static PropertyEvaluator createPropertyEvaluator(
        @NonNull final Project project,
        @NonNull final AntProjectHelper helper,
        @NonNull final PropertyProvider... additionalPropertyProviders) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("additionalPropertyProviders", additionalPropertyProviders); //NOI18N

        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(ProjectConfigurations.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        final Queue<PropertyProvider> providers = new ArrayDeque<>(additionalPropertyProviders.length + 7);
        providers.offer(helper.getPropertyProvider(ProjectConfigurations.CONFIG_PROPS_PATH));
        providers.offer(new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper));    //NOI18N
        providers.offer(helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        providers.offer(helper.getProjectLibrariesPropertyProvider());
        providers.offer(PropertyUtils.userPropertiesProvider(baseEval2,
            "user.properties.file", FileUtil.toFile(project.getProjectDirectory())));   //NOI18N
        providers.offer(new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper));    //NOI18N
        providers.offer(helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        Collections.addAll(providers, additionalPropertyProviders);
        return PropertyUtils.sequentialPropertyEvaluator(
            helper.getStockPropertyPreprovider(),
            providers.toArray(new PropertyProvider[0]));
    }

    /**
     * Builder for {@link ProjectConfigurationProvider}.
     */
    public static final class ConfigurationProviderBuilder {

        private final Project project;
        private final PropertyEvaluator eval;
        private final UpdateHelper updateHelper;
        private final Set<String> configurationsAffectActions;
        private Runnable customizerAction;

        private ConfigurationProviderBuilder(
                @NonNull final Project project,
                @NonNull final PropertyEvaluator eval,
                @NonNull final UpdateHelper updateHelper) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            this.project = project;
            this.eval = eval;
            this.updateHelper = updateHelper;
            this.configurationsAffectActions = new HashSet<>();
        }

        /**
         * Sets actions affected by the configurations.
         * @param commands the actions affected by configurations
         * @return the {@link ConfigurationProviderBuilder}
         */
        @NonNull
        public ConfigurationProviderBuilder addConfigurationsAffectActions(@NonNull final String... commands) {
            Parameters.notNull("commands", commands);   //NOI18N
            Collections.addAll(configurationsAffectActions, commands);
            return this;
        }

        /**
         * Sets an action showing the customizer for {@link ProjectConfiguration}s.
         * @param action the action
         * @return the {@link ConfigurationProviderBuilder}
         */
        @NonNull
        public ConfigurationProviderBuilder setCustomizerAction(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            this.customizerAction = action;
            return this;
        }

        /**
         * Creates a configured {@link ProjectConfigurationProvider}.
         * @return a new configured instance of {@link ProjectConfigurationProvider}
         */
        @NonNull
        public ProjectConfigurationProvider<? extends ProjectConfiguration> build() {
            return new ConfigurationProviderImpl(
                project,
                eval,
                updateHelper,
                configurationsAffectActions,
                customizerAction);
        }
    }

    /**
     * The {@link ProjectConfiguration} implementation.
     */
    public static final class Configuration implements ProjectConfiguration {
        private final String name;
        private final String displayName;

        private Configuration(
                @NullAllowed final String name,
                @NonNull final String displayName) {
            Parameters.notNull("displayName", displayName); //NOI18N
            this.name = name;
            this.displayName = displayName;
        }

        /**
         * Returns the system name of the configuration.
         * @return the system name of configuration
         */
        @CheckForNull
        public String getName() {
            return name;
        }

        /**
         * Checks if the {@link Configuration} is a default one.
         * @return true when the {@link Configuration} is default
         */
        public boolean isDefault() {
            return name == null;
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Configuration) && Objects.equals(name, ((Configuration) o).name);
        }

        @Override
        public String toString() {
            return "Config[" + name + "," + displayName + "]"; // NOI18N
        }
    }

    private static final class ConfigurationProviderImpl implements ProjectConfigurationProvider<Configuration> {

        private static final Configuration DEFAULT = new Configuration(
            null,
            NbBundle.getMessage(ProjectConfigurations.class, "TXT_DefaultConfig"));

        private final Project p;
        private final PropertyEvaluator eval;
        private final UpdateHelper updateHelper;
        private final Set<String> configurationsAffectActions;
        private final Runnable customizerAction;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final FileChangeListener fcl = new FileChangeAdapter() {
            @Override
            public void fileFolderCreated(@NonNull final FileEvent fe) {
                update(fe);
            }

            @Override
            public void fileDataCreated(@NonNull final FileEvent fe) {
                update(fe);
            }

            @Override
            public void fileDeleted(@NonNull final FileEvent fe) {
                update(fe);
            }

            @Override
            public void fileRenamed(@NonNull final FileRenameEvent fe) {
                update(fe);
            }

            private void update(@NonNull final FileEvent ev) {
                Parameters.notNull("ev", ev);   //NOI18N
                LOGGER.log(Level.FINEST, "Received {0}", ev);
                Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
                configDir = p.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N
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
                    LOGGER.log(Level.FINER, "Firing " + ProjectConfigurationProvider.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[] {oldConfigs, newConfigs});
                    pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
                    // XXX also fire PROP_ACTIVE_CONFIGURATION?
                }
            }
        };
        private final FileChangeListener fclWeak;
        private FileObject configDir;
        private volatile Map<String,Configuration> configs;
        private FileObject nbp;

        public ConfigurationProviderImpl(
                @NonNull final Project p,
                @NonNull final PropertyEvaluator eval,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final Set<String> configurationsAffectActions,
                @NullAllowed final Runnable customizerAction) {
            Parameters.notNull("p", p); //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            Parameters.notNull("configurationsAffectActions", configurationsAffectActions); //NOI18N
            this.p = p;
            this.eval = eval;
            this.updateHelper = updateHelper;
            this.configurationsAffectActions = configurationsAffectActions;
            this.customizerAction = customizerAction;
            fclWeak = FileUtil.weakFileChangeListener(fcl, null);
            nbp = p.getProjectDirectory().getFileObject("nbproject"); // NOI18N
            if (nbp != null) {
                nbp.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "Added listener to {0}", nbp);
                configDir = nbp.getFileObject("configs"); // NOI18N
                if (configDir != null) {
                    configDir.addFileChangeListener(fclWeak);
                    LOGGER.log(Level.FINEST, "Added listener to {0}", configDir);
                }
            }
            eval.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(@NonNull final PropertyChangeEvent evt) {
                    if (ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG.equals(evt.getPropertyName())) {
                        LOGGER.log(Level.FINER, "Refiring " + ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG + " -> " + ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE);
                        Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
                        calculateConfigs();
                        Set<String> newConfigs = configs.keySet();
                        if (!oldConfigs.equals(newConfigs)) {
                            LOGGER.log(Level.FINER, "Firing " + ProjectConfigurationProvider.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[] {oldConfigs, newConfigs});
                            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
                        }
                        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
                    }
                }
            });
        }

        @NonNull
        private Map<String,Configuration> calculateConfigs() {
            final Map<String,Configuration> cfgs = new HashMap<>();
            if (configDir != null) {
                for (FileObject kid : configDir.getChildren()) {
                    if (!kid.hasExt("properties")) {    //NOI18N
                        continue;
                    }
                    try {
                        try (InputStream is = kid.getInputStream()) {
                            final Properties props = new Properties();
                            props.load(is);
                            final String name = kid.getName();
                            final String label = props.getProperty("$label"); // NOI18N
                            cfgs.put(name, new Configuration(name, label != null ? label : name));
                        }
                    } catch (IOException x) {
                        LOGGER.log(Level.INFO, null, x);
                    }
                }
            }
            configs = cfgs;
            LOGGER.log(Level.FINEST, "Calculated configurations: {0}", cfgs);
            return cfgs;
        }

        @NonNull
        private Map<String,Configuration> getConfigs() {
            Map<String,Configuration> cfgs = configs;
            if (cfgs == null) {
                cfgs = calculateConfigs();
            }
            return cfgs;
        }

        @NonNull
        @Override
        public Collection<Configuration> getConfigurations() {
            final Map<String,Configuration> cfgs = getConfigs();
            final List<Configuration> l = new ArrayList<>();
            l.addAll(cfgs.values());
            l.sort(new Comparator<Configuration>() {
                Collator c = Collator.getInstance();
                @Override
                public int compare(Configuration c1, Configuration c2) {
                    return c.compare(c1.getDisplayName(), c2.getDisplayName());
                }
            });
            l.add(0, DEFAULT);
            return l;
        }

        @NonNull
        @Override
        public Configuration getActiveConfiguration() {
            final Map<String,Configuration> cfgs = getConfigs();
            String config = eval.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
            if (config != null && cfgs.containsKey(config)) {
                return cfgs.get(config);
            } else {
                return DEFAULT;
            }
        }

        @Override
        public void setActiveConfiguration(@NonNull final Configuration c) throws IllegalArgumentException, IOException {
            final Map<String,Configuration> cfgs = getConfigs();
            if (c != DEFAULT && !cfgs.containsValue(c)) {
                throw new IllegalArgumentException(String.format("Configuration: %s, Known Configurations: %s",
                        c,
                        cfgs.values()));
            }
            assert ProjectManager.mutex().isWriteAccess();
            final String n = c.name;
            EditableProperties ep = updateHelper.getProperties(CONFIG_PROPS_PATH);
            if (Utilities.compareObjects(n, ep.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG))) {
                return;
            }
            if (n != null) {
                ep.setProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG, n);
            } else {
                ep.remove(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
            }
            updateHelper.putProperties(CONFIG_PROPS_PATH, ep);
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
            ProjectManager.getDefault().saveProject(p);
            assert ep.isEmpty() || p.getProjectDirectory().getFileObject(CONFIG_PROPS_PATH) != null :
                String.format("Setting config to: %s, properties are empty: %b",    //NOI18N
                n,
                ep.isEmpty());
        }

        @Override
        public boolean hasCustomizer() {
            return customizerAction != null;
        }

        @Override
        public void customize() {
            if (customizerAction != null) {
                customizerAction.run();
            }
        }


        @Override
        public boolean configurationsAffectAction(@NonNull final String command) {
            return configurationsAffectActions.contains(command);
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener lst) {
            Parameters.notNull("lst", lst);     //NOI18N
            pcs.addPropertyChangeListener(lst);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener lst) {
            Parameters.notNull("lst", lst);     //NOI18N
            pcs.removePropertyChangeListener(lst);
        }
    }

    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;

        @SuppressWarnings("LeakingThisInConstructor")
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }

        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            } else {
                return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
            }
        }
    }
}
