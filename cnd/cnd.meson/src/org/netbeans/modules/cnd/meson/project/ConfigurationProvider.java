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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

public class ConfigurationProvider implements ProjectConfigurationProvider<Configuration>
{
    private static final Logger LOGGER = Logger.getLogger(ConfigurationProvider.class.getName());
    private static final String CONFIGURATIONS_JSON = "configurations.json"; // NOI18N

    private final MesonProject project;
    private Configuration activeConfiguration = null;
    private List<Configuration> configurations = null;
    private IntroCompilersJsonListener compilersListener = null;

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

        File compilersFile = getActiveIntroCompilersJsonPath(project, activeConfiguration).toFile();

        if (compilersFile.exists()) {
            updateLanguages(project, compilersFile);
        }

        FileUtil.addFileChangeListener(compilersListener = new IntroCompilersJsonListener(project), compilersFile);
    }

    public void add(Configuration configuration) {
        configurations.add(configuration);
        if (activeConfiguration == null) {
            setActiveConfiguration(configuration);
        }
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setConfigurations(List<Configuration> configurations) {
        final String activeConfigurationName = getActiveConfiguration().getDisplayName();
        this.configurations = configurations;
        activeConfiguration = null;
        for (Configuration cfg: configurations) {
            if (activeConfigurationName.equals(cfg.getDisplayName())) {
                setActiveConfiguration(cfg);
                break;
            }
        }
        if (activeConfiguration == null && !configurations.isEmpty()) {
            setActiveConfiguration(configurations.get(0));
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
        if (activeConfiguration != configuration) {
            if ((activeConfiguration != null) && (compilersListener != null)) {
                FileUtil.removeFileChangeListener(compilersListener, getActiveIntroCompilersJsonPath(project).toFile());
            }

            activeConfiguration = configuration;

            File compilersFile = getActiveIntroCompilersJsonPath(project).toFile();

            if (compilersFile.exists()) {
                updateLanguages(project, compilersFile);
            }

            FileUtil.addFileChangeListener(compilersListener = new IntroCompilersJsonListener(project), compilersFile);
        }
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

    private static Path getActiveIntroCompilersJsonPath(MesonProject project) {
        return getActiveIntroCompilersJsonPath(project, project.getActiveConfiguration());
    }

    private static Path getActiveIntroCompilersJsonPath(MesonProject project, Configuration configuration) {
        return Paths.get(project.getProjectDirectory().getPath(), configuration.getBuildDirectory(), MesonProject.COMPILERS_JSON);
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

    private static final class IntroCompilersJsonListener implements FileChangeListener {
        private final MesonProject project;

        public IntroCompilersJsonListener(MesonProject project) {
            this.project = project;
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent e) {}

        @Override
        public void fileChanged(FileEvent e) {
            updateLanguages(project);
        }

        @Override
        public void fileDataCreated(FileEvent e) {
            updateLanguages(project);
        }

        @Override
        public void fileDeleted(FileEvent e) {}

        @Override
        public void fileFolderCreated(FileEvent e) {}

        @Override
        public void fileRenamed(FileRenameEvent e) {}
    };

    private static final class Compiler {
        String id;
        String[] exelist;
        String[] linker_exelist;
        String[] file_suffixes;
        String default_suffix;
        String version;
        String full_version;
        String linker_id;
    };

    private static void updateLanguages(MesonProject project) {
        updateLanguages(project, getActiveIntroCompilersJsonPath(project).toFile());
    }

    private static void updateLanguages(MesonProject project, File compilersFile) {
        try {
            Map<String, Map<String, Compiler>> machines = new Gson().fromJson(new FileReader(compilersFile), new TypeToken<Map<String, Map<String, Compiler>>>(){}.getType());

            if (machines != null) {
                Map<String, Compiler> host = machines.get("host");
                if (host != null) {
                    Set<String> languages = host.keySet();
                    if (!languages.isEmpty()) {
                        project.setLanguages(languages);
                    }
                }
            }
        }
        catch (FileNotFoundException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }
}