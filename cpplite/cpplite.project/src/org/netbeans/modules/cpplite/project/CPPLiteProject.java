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
package org.netbeans.modules.cpplite.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cpplite.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class CPPLiteProject implements Project {
    
    public static final String PROJECT_KEY = "org-netbeans-modules-cpplite-project-CPPLiteProject";
    public static final String KEY_COMPILE_COMMANDS = "compile-commands";
    public static final String KEY_COMPILE_COMMANDS_EXECUTABLE = "compile-commands-executable";
    public static final String KEY_NEXT_MARK = "next-mark";
    public static final String KEY_IS_PROJECT = "is-project";

    public static Preferences getRootPreferences(FileObject root) {
        return getRootPreferences(root, true);
    }

    private static Preferences getRootPreferences(FileObject root, boolean create) {
        String encoded = root.toURI().toString().replace("/", ".");
        Preferences projectsRoot = NbPreferences.forModule(CPPLiteProject.class).node("projects");
        try {
            for (String key : projectsRoot.keys()) {
                if (encoded.equals(projectsRoot.get(key, null))) {
                    return projectsRoot.node(key);
                }
            }
        } catch (BackingStoreException ex) {
        }
        if (!create) {
            return null;
        }
        synchronized (CPPLiteProject.class) {
            int mark = projectsRoot.getInt(KEY_NEXT_MARK, 0);
            String key = Integer.toHexString(mark);
            projectsRoot.putInt(KEY_NEXT_MARK, mark + 1);
            projectsRoot.put(key, encoded);
            return projectsRoot.node(key);
        }
    }

    public static Preferences getBuildPreferences(FileObject root) {
        return getRootPreferences(root).node("build");
    }

    private final FileObject projectDirectory;
    private final Lookup lookup;
    private final AtomicReference<BuildConfiguration> buildConfigurations = new AtomicReference<>();

    private CPPLiteProject(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
        this.lookup = Lookups.fixed(new LogicalViewProviderImpl(this),
                                    new ActionProviderImpl(this),
                                    new CustomizerProviderImpl(this),
                                    new CPPLiteCProjectConfigurationProvider(getRootPreferences(projectDirectory)),
                                    new RecommendedTemplatesImpl(),
                                    new PrivilegedTemplatesImpl(),
                                    new ProjectInfo(this),
                                    new ProjectOpenHookImpl(this),
                                    this);
        buildConfigurations.set(BuildConfiguration.read(getBuildPreferences(projectDirectory)));
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public BuildConfiguration getActiveBuildConfiguration() {
        return buildConfigurations.get();
    }

    public void setActiveBuildConfiguration(BuildConfiguration config) {
        buildConfigurations.set(config);
    }

    public String getCompileCommands() {
        return getRootPreferences(projectDirectory).get(KEY_COMPILE_COMMANDS, "");
    }

    public void setCompileCommands(String compileCommands) {
        getRootPreferences(projectDirectory).put(KEY_COMPILE_COMMANDS, compileCommands);
    }

    public String getCompileCommandsExecutable() {
        return getRootPreferences(projectDirectory).get(KEY_COMPILE_COMMANDS_EXECUTABLE, "");
    }

    public void setCompileCommandsExecutable(String compileCommandsExecutable) {
        getRootPreferences(projectDirectory).put(KEY_COMPILE_COMMANDS_EXECUTABLE, compileCommandsExecutable);
    }

    private static Icon loadProjectIcon() {
        return ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/cpplite/project/resources/project.gif"));
    }

    @ServiceProvider(service=ProjectFactory.class)
    public static final class FactoryImpl implements ProjectFactory2 {

        @Override
        public ProjectManager.Result isProject2(FileObject projectDirectory) {
            Preferences prefs = getRootPreferences(projectDirectory, false);
            if (prefs != null && prefs.getBoolean(KEY_IS_PROJECT, false)) {
                return new ProjectManager.Result(loadProjectIcon());
            }
            return null;
        }

        @Override
        public boolean isProject(FileObject projectDirectory) {
            return isProject2(projectDirectory) != null;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory)) {
                return new CPPLiteProject(projectDirectory);
            }
            return null;
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }
        
    }

    private static class RecommendedTemplatesImpl implements RecommendedTemplates {

        private static final String[] TEMPLATES = new String[] {
            "cpplite"
        };

        @Override
        public String[] getRecommendedTypes() {
            return TEMPLATES;
        }
    }

    private static class PrivilegedTemplatesImpl implements PrivilegedTemplates {

        private static final String[] TEMPLATES = new String[] {
            "Templates/cpplite/CTemplate.c",
            "Templates/cpplite/CPPTemplate.cpp",
            "Templates/cpplite/HTemplate.h",
            "Templates/cpplite/HPPTemplate.hpp",
        };

        @Override
        public String[] getPrivilegedTemplates() {
            return TEMPLATES;
        }
    }

    private static final class ProjectInfo implements ProjectInformation {

        private final Project prj;

        public ProjectInfo(Project prj) {
            this.prj = prj;
        }

        @Override
        public String getName() {
            return prj.getProjectDirectory().getNameExt();
        }

        @Override
        public String getDisplayName() {
            return prj.getProjectDirectory().getNameExt();
        }

        @Override
        public Icon getIcon() {
            return loadProjectIcon();
        }

        @Override
        public Project getProject() {
            return prj;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

    }

    private static final class ProjectOpenHookImpl extends ProjectOpenedHook {

        private final ClassPath source;

        public ProjectOpenHookImpl(Project prj) {
            this.source = ClassPathSupport.createClassPath(prj.getProjectDirectory());
        }

        @Override
        protected void projectOpened() {
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {source});
        }

        @Override
        protected void projectClosed() {
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {source});
        }

    }
}
