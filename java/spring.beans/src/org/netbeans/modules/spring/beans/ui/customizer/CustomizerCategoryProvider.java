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

package org.netbeans.modules.spring.beans.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.spring.api.SpringUtilities;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.ConfigFileManager;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registrations;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
@Registrations({
    @Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject", position=250),
    @Registration(projectType="org-netbeans-modules-java-j2seproject", position=250),
    @Registration(projectType="org-netbeans-modules-web-project", position=250),
    @Registration(projectType="org-netbeans-modules-maven", position=278)
})
public class CustomizerCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    public Category createCategory(Lookup context) {
        Project project = getProject(context);
        ConfigFileManager manager = getConfigFileManager(project);
        // Do not display the customizer if there are no Spring config files
        // and the Spring library is not on the classpath.
        if (manager.getConfigFiles().size() <= 0) {
            if (!hasSpringOnClassPath(project)) {
                return null;
            }
        }
        String categoryName = NbBundle.getMessage(CustomizerCategoryProvider.class, "LBL_SpringFramework");
        return Category.create("SpringFramework", categoryName, null); // NOI18N
    }

    public JComponent createComponent(Category category, Lookup context) {
        Project project = getProject(context);
        ConfigFileManager manager = getConfigFileManager(project);
        SpringCustomizerPanel panel = new SpringCustomizerPanel(project, manager.getConfigFiles(), manager.getConfigFileGroups());
        CategoryListener listener = new CategoryListener(manager, panel);
        category.setOkButtonListener(listener);
        category.setStoreListener(listener);
        return panel;
    }

    private static Project getProject(Lookup context) {
        Project project = context.lookup(Project.class);
        if (project == null) {
            throw new IllegalStateException("The lookup " + context + " does not contain a Project");
        }
        return project;
    }

    private static ConfigFileManager getConfigFileManager(Project project) {
        ProjectSpringScopeProvider scopeProvider = project.getLookup().lookup(ProjectSpringScopeProvider.class);
        // The following should pass, since we only register the customizer for
        // projects for which we also extend the lookup with a ProjectSpringScopeProvider.
        assert scopeProvider != null;
        return scopeProvider.getSpringScope().getConfigFileManager();
    }

    private static boolean hasSpringOnClassPath(Project project) {
        SourceGroup[] javaSources = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup javaSource : javaSources) {
            ClassPath compileCp = ClassPath.getClassPath(javaSource.getRootFolder(), ClassPath.COMPILE);
            if (compileCp != null) {
                if (SpringUtilities.containsSpring(compileCp)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class CategoryListener implements ActionListener {

        private final ConfigFileManager manager;
        private final SpringCustomizerPanel panel;
        private volatile List<File> files;
        private volatile List<ConfigFileGroup> groups;

        public CategoryListener(ConfigFileManager manager, SpringCustomizerPanel panel) {
            this.manager = manager;
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent e) {
            if (files == null || groups == null) {
                // OK button listener called.
                assert SwingUtilities.isEventDispatchThread();
                files = panel.getConfigFiles();
                groups = panel.getConfigFileGroups();
            } else {
                // Store listener called.
                manager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        manager.putConfigFilesAndGroups(files, groups);
                        // No need to save the project explicitly, the
                        // customizer dialog will.
                    }
                });
            }
        }
    }
}
