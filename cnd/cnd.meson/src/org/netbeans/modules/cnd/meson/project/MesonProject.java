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

import java.beans.PropertyChangeListener;
import java.nio.file.Paths;

import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.cnd.meson.project.ui.CustomizerProviderImpl;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class MesonProject implements Project {
    public static final String PROJECT_KEY = "org-netbeans-modules-cnd-meson-project"; // NOI18N
    public static final String NBPROJECT_DIRECTORY = ".netbeans"; // NOI18N
    public static final String BUILD_DIRECTORY = ".netbeans/build"; // NOI18N
    public static final String PROJECT_JSON = "project.json"; // NOI18N
    public static final String ICON = "org/netbeans/modules/cnd/meson/resources/project_icon.png"; // NOI18N
    public static final String OPEN_ICON = ICON;

    private final FileObject projectDirectory;
    private final ProjectState state;
    private final Lookup lookup;
    private final ConfigurationProvider configurationProvider;

    @Override
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public Configuration getActiveConfiguration() {
        return configurationProvider.getActiveConfiguration();
    }

    public String getCompileCommandsJsonPathForActiveConfiguration() {
        return Paths.get(getProjectDirectory().getPath(), configurationProvider.getActiveConfiguration().getCompileCommandsJsonPath()).toString();
    }

    public void setActiveConfiguration(Configuration configuration) {
        configurationProvider.setActiveConfiguration(configuration);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public MesonProject(FileObject projectDirectory, ProjectState state) {
        this.projectDirectory = projectDirectory;
        this.state = state;
        this.configurationProvider = new ConfigurationProvider(this);
        this.lookup = Lookups.fixed(new LogicalViewProviderImpl(this),
                                    new ActionProviderImpl(this),
                                    new CustomizerProviderImpl(this),
                                    new RecommendedTemplatesImpl(),
                                    new PrivilegedTemplatesImpl(),
                                    new ProjectInfo(this),
                                    configurationProvider,
                                    this);
    }

    public static Icon getIcon() {
        return ImageUtilities.image2Icon(ImageUtilities.loadImage(ICON));
    }

    private static class RecommendedTemplatesImpl implements RecommendedTemplates {
        private static final String[] TEMPLATES = new String[] {
            "cnd.meson"
        };

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public String[] getRecommendedTypes() {
            return TEMPLATES;
        }
    }

    private static class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        private static final String[] TEMPLATES = new String[] {
            "Templates/cnd.meson/cpp_template.cpp",
            "Templates/cnd.meson/cpp_template.hpp",
        };

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public String[] getPrivilegedTemplates() {
            return TEMPLATES;
        }
    }

    private static final class ProjectInfo implements ProjectInformation {
        private final Project project;

        public ProjectInfo(Project project) {
            this.project = project;
        }

        @Override
        public String getName() {
            return project.getProjectDirectory().getNameExt();
        }

        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getNameExt();
        }

        @Override
        public Icon getIcon() {
            return MesonProject.getIcon();
        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}
    }
}