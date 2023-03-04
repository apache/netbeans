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
package org.netbeans.modules.maven.j2ee.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.j2ee.ui.customizer.impl.CustomizerFrameworks;
import org.netbeans.modules.maven.j2ee.ui.customizer.impl.CustomizerRunEar;
import org.netbeans.modules.maven.j2ee.ui.customizer.impl.CustomizerRunEjb;
import org.netbeans.modules.maven.j2ee.ui.customizer.impl.CustomizerRunWeb;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
public final class MavenCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String FRAMEWORKS = "Frameworks"; // NOI18N
    private static final String RUN = "Run"; // NOI18N

    private CustomizerFrameworks frameworkCustomizer;
    private BaseRunCustomizer runCustomizer;


    private String type;

    private MavenCompositePanelProvider(String type) {
        this.type = type;
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-maven", position = 257)
    public static MavenCompositePanelProvider createFrameworks() {
        return new MavenCompositePanelProvider(FRAMEWORKS);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-maven", position = 259)
    public static ProjectCustomizer.CompositeCategoryProvider createCssPreprocessors() {
        return new FilterProvider(CssPreprocessorsUI.getDefault().createCustomizer());
    }

    // We want to create JavaScript libraries customizer/CSS Processors only for Maven Web Project:
    private static class FilterProvider implements ProjectCustomizer.CompositeCategoryProvider {

        private ProjectCustomizer.CompositeCategoryProvider original;

        public FilterProvider(ProjectCustomizer.CompositeCategoryProvider original) {
            this.original = original;
        }

        @Override
        public Category createCategory(Lookup context) {
            Project project = context.lookup(Project.class);
            assert project != null;
            String projectType = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
            if (NbMavenProject.TYPE_WAR.equalsIgnoreCase(projectType) == false) {
                return null;
            }
            return original.createCategory(context);
        }

        @Override
        public JComponent createComponent(Category category, Lookup context) {
            return original.createComponent(category, context);
        }

    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-maven", position = 301)
    public static MavenCompositePanelProvider createRun() {
        return new MavenCompositePanelProvider(RUN);
    }


    @Override
    public Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        String projectType = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
        
        if (FRAMEWORKS.equals(type)) {
            if (NbMavenProject.TYPE_WAR.equalsIgnoreCase(projectType) == false) {
                return null; // We want to create Framework customizer only for Maven Web Project
            }
        }
        if (RUN.equals(type)) {
            if ((NbMavenProject.TYPE_WAR.equalsIgnoreCase(projectType) == false) &&
                (NbMavenProject.TYPE_EJB.equalsIgnoreCase(projectType) == false) &&
                (NbMavenProject.TYPE_EAR.equalsIgnoreCase(projectType) == false)) {
                return null; // We want to create Run customizer only for Web/Ejb/Ear projects
            }
        }

        return ProjectCustomizer.Category.create(type, NbBundle.getMessage(MavenCompositePanelProvider.class, "PNL_" + type), null); // NOI18N
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        String name = category.getName();
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        final Project project = context.lookup(Project.class);

        category.setOkButtonListener(listenerAWT);
        category.setStoreListener(listenerNonAWT);

        if (FRAMEWORKS.equals(name)) {
            frameworkCustomizer = new CustomizerFrameworks(category, project);
            return frameworkCustomizer;
        }
        if (RUN.equals(name)) {
            String projectType = project.getLookup().lookup(NbMavenProject.class).getPackagingType();

            if (NbMavenProject.TYPE_WAR.equalsIgnoreCase(projectType)) {
                runCustomizer = new CustomizerRunWeb(handle, project);
            }
            if (NbMavenProject.TYPE_EJB.equalsIgnoreCase(projectType)) {
                runCustomizer = new CustomizerRunEjb(handle, project);
            }
            if (NbMavenProject.TYPE_EAR.equalsIgnoreCase(projectType)) {
                runCustomizer = new CustomizerRunEar(handle, project);
            }
            return runCustomizer;
        }

        return null;
    }

    private ActionListener listenerAWT = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (runCustomizer != null) {
                runCustomizer.applyChangesInAWT();
            }
            if (frameworkCustomizer != null) {
                frameworkCustomizer.applyChangesInAWT();
            }
        }
    };

    private ActionListener listenerNonAWT = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (runCustomizer != null) {
                runCustomizer.applyChanges();
            }
            if (frameworkCustomizer != null) {
                frameworkCustomizer.applyChanges();
            }
        }
    };
}
