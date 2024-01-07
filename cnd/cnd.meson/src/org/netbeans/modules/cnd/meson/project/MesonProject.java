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
import java.util.Set;

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
    public static final String INFO_DIRECTORY = "meson-info";
    public static final String COMPILERS_JSON = Paths.get(INFO_DIRECTORY, "intro-compilers.json").toString();

    private static final String C_LANGUAGE = "c";
    private static final String CPP_LANGUAGE = "cpp";
    private static final String FORTRAN_LANGUAGE = "fortran";
    private static final String JAVA_LANGUAGE = "java";
    private static final String RUST_LANGUAGE = "rust";

    private final FileObject projectDirectory;
    private final ProjectState state;
    private final Lookup lookup;
    private final ConfigurationProvider configurationProvider;
    private Set<String> languages;

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

    public void setLanguages(Set<String> languages) {
        this.languages = languages;
    }

    public boolean isC() { return hasLanguage(C_LANGUAGE); }
    public boolean isCPP() { return hasLanguage(CPP_LANGUAGE); }
    public boolean isFortran() { return hasLanguage(FORTRAN_LANGUAGE); }
    public boolean isJava() { return hasLanguage(JAVA_LANGUAGE); }
    public boolean isRust() { return hasLanguage(RUST_LANGUAGE); }

    private boolean hasLanguage(String language) {
        return languages != null && languages.contains(language);
    }

    @SuppressWarnings({"LeakingThisInConstructor", "this-escape"})
    public MesonProject(FileObject projectDirectory, ProjectState state) {
        this.projectDirectory = projectDirectory;
        this.state = state;
        this.configurationProvider = new ConfigurationProvider(this);
        this.lookup = Lookups.fixed(new LogicalViewProviderImpl(this),
                                    new ActionProviderImpl(this),
                                    new CustomizerProviderImpl(this),
                                    new RecommendedTemplatesImpl(this),
                                    new PrivilegedTemplatesImpl(this),
                                    new ProjectInfo(this),
                                    configurationProvider,
                                    this);
    }

    public static Icon getIcon() {
        return ImageUtilities.image2Icon(ImageUtilities.loadImage(ICON));
    }

    private static class RecommendedTemplatesImpl implements RecommendedTemplates {
        private static final String[] MESON_TYPES = new String[] {
            "meson-types", // NOI18N
            "simple-files", // NOI18N
        };
        private static final String[] C_TYPES = new String[] {
            "c-types", // NOI18N
        };
        private static final String[] CPP_TYPES = new String[] {
            "cpp-types", // NOI18N
        };
        private static final String[] FORTRAN_TYPES = new String[] {
            "fortran-types", // NOI18N
        };
        private static final String[] JAVA_TYPES = new String[] {
            "java-classes", // NOI18N
            "java-main-class", // NOI18N
            "java-forms", // NOI18N
            "gui-java-application", // NOI18N
            "java-beans", // NOI18N
            "oasis-XML-catalogs", // NOI18N
            "XML", // NOI18N
            "junit", // NOI18N
        };
        private static final String[] RUST_TYPES = new String[] {
            "rust", // NOI18N
            "XML", // NOI18N
        };

        private final MesonProject project;

        public RecommendedTemplatesImpl(MesonProject project) {
            this.project = project;
        }

        @Override
        public String[] getRecommendedTypes() {
            String[] templates = MESON_TYPES;

            if (project.isC()) {
                templates = Utils.concatenate(templates, C_TYPES);
            }

            if (project.isCPP()) {
                templates = Utils.concatenate(templates, CPP_TYPES);
            }

            if (project.isFortran()) {
                templates = Utils.concatenate(templates, FORTRAN_TYPES);
            }

            if (project.isJava()) {
                templates = Utils.concatenate(templates, JAVA_TYPES);
            }

            if (project.isRust()) {
                templates = Utils.concatenate(templates, RUST_TYPES);
            }

            return templates;
        }
    }

    private static class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        private static final String[] MESON_TEMPLATES = new String[] {
            "Templates/meson/meson.build",   // NOI18N
            "Templates/meson/meson.options", // NOI18N
        };
        private static final String[] C_TEMPLATES = new String[] {
            "Templates/cFiles/main.c", // NOI18N
            "Templates/cFiles/file.c", // NOI18N
            "Templates/cFiles/file.h", // NOI18N
        };
        private static final String[] CPP_TEMPLATES = new String[] {
            "Templates/cppFiles/class.cc", // NOI18N
            "Templates/cppFiles/main.cc",  // NOI18N
            "Templates/cppFiles/file.cc",  // NOI18N
            "Templates/cppFiles/file.h",   // NOI18N
        };
        private static final String[] FORTRAN_TEMPLATES = new String[] {
            "Templates/fortranFiles/fortranFreeFormatFile.f90", // NOI18N
        };
        private static final String[] JAVA_TEMPLATES = new String[] {
            "Templates/Classes/Class.java",          // NOI18N
            "Templates/Classes/Interface.java",      // NOI18N
            "Templates/Other/properties.properties", // NOI18N
        };
        private static final String[] RUST_TEMPLATES = new String[] {
            "Templates/rust/rust-file.rs", // NOI18N
        };

        private final MesonProject project;

        public PrivilegedTemplatesImpl(MesonProject project) {
            this.project = project;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            String[] templates = MESON_TEMPLATES;

            if (project.isC()) {
                templates = Utils.concatenate(templates, C_TEMPLATES);
            }

            if (project.isCPP()) {
                templates = Utils.concatenate(templates, CPP_TEMPLATES);
            }

            if (project.isFortran()) {
                templates = Utils.concatenate(templates, FORTRAN_TEMPLATES);
            }

            if (project.isJava()) {
                templates = Utils.concatenate(templates, JAVA_TEMPLATES);
            }

            if (project.isRust()) {
                templates = Utils.concatenate(templates, RUST_TEMPLATES);
            }

            return templates;
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