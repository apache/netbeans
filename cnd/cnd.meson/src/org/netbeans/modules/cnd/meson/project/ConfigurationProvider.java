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

package org.netbeans.modules.cnd.meson.project;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.GsonBuilder;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;

public class ConfigurationProvider implements ProjectConfigurationProvider<Configuration>
{
    private static final Logger LOGGER = Logger.getLogger(ConfigurationProvider.class.getName());
    private static final String CONFIGURATIONS_JSON = "configurations.json"; // NOI18N

    private final MesonProject project;
    private Configuration activeConfiguration = null;
    private List<Configuration> configurations = null;

    public ConfigurationProvider(MesonProject project) {
        this.project = project;

        File configurationsFile = new File(getConfigurationsFilePath());

        if (configurationsFile.exists()) {
            try {
                ConfigurationsFileContent content = new Gson().fromJson(new FileReader(configurationsFile), new TypeToken<ConfigurationsFileContent>(){}.getType());

                if ((content != null) && (content.configurations != null)) {
                    configurations = content.configurations;

                    if (content.activeConfigurationName != null) {
                        for (Configuration c: configurations) {
                            if (content.activeConfigurationName.equals(c.getDisplayName())) {
                                activeConfiguration = c;
                                break;
                            }
                        }
                    }

                    if (activeConfiguration == null) {
                        final String defaultName = Configuration.getDefault().getDisplayName();

                        for (Configuration c: configurations) {
                            if (defaultName.equals(c.getDisplayName())) {
                                activeConfiguration = c;
                                break;
                            }
                        }
                    }

                    if ((activeConfiguration == null) && !configurations.isEmpty()) {
                        activeConfiguration = configurations.get(0);
                    }

                    if (activeConfiguration == null) {
                        activeConfiguration = Configuration.getDefault();
                        configurations.add(activeConfiguration);
                    }
                }
            }
            catch (FileNotFoundException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        if (configurations == null) {
            configurations = new ArrayList<>();
            activeConfiguration = Configuration.getDefault();
            configurations.add(activeConfiguration);
        }
    }

    public void add(Configuration configuration) {
        configurations.add(configuration);
        if (activeConfiguration == null) {
            activeConfiguration = configuration;
        }
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setConfigurations(List<Configuration> configurations) {
        final String activeConfigurationName = getActiveConfiguration().getDisplayName();
        this.configurations = configurations;
        activeConfiguration = null;
        for (Configuration cfg: configurations) {
            if (activeConfigurationName.equals(cfg.getDisplayName())) {
                activeConfiguration = cfg;
                break;
            }
        }
        if (activeConfiguration == null && !configurations.isEmpty()) {
            activeConfiguration = configurations.get(0);
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(Paths.get(getConfigurationsFilePath()).getParent());

            try (FileWriter writer = new FileWriter(getConfigurationsFilePath())) {
                GsonBuilder builder = new GsonBuilder();
                builder.setPrettyPrinting();
                writer.write(builder.create().toJson(new ConfigurationsFileContent(activeConfiguration, configurations)));
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    @Override
    public Collection<Configuration> getConfigurations() {
        return Collections.unmodifiableCollection(configurations);
    }

    @Override
    public Configuration getActiveConfiguration() {
        return activeConfiguration;
    }

    @Override
    public void setActiveConfiguration(Configuration configuration) {
        activeConfiguration = configuration;
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        project.getLookup().lookup(CustomizerProvider2.class).showCustomizer();
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
    }

    private String getConfigurationsFilePath() {
        return Paths.get(project.getProjectDirectory().getPath(), MesonProject.NBPROJECT_DIRECTORY, CONFIGURATIONS_JSON).toString();
    }

    private static final class ConfigurationsFileContent {
        public String activeConfigurationName;
        public List<Configuration> configurations;

        public ConfigurationsFileContent() {}
        public ConfigurationsFileContent(Configuration activeConfiguration, List<Configuration> configurations) {
            this.activeConfigurationName = activeConfiguration.getDisplayName();
            this.configurations = configurations;
        }
    };
}