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
package org.netbeans.modules.javascript.nodejs.ui.libraries;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * npm libraries customizer.
 * 
 * @author Jan Stola
 */
public class LibraryCustomizer implements ProjectCustomizer.CompositeCategoryProvider {
    public static final String CATEGORY_NAME = "NpmLibraries"; // NOI18N

    private final boolean checkWebRoot;

    public LibraryCustomizer() {
        this(false);
    }

    public LibraryCustomizer(boolean checkWebRoot) {
        this.checkWebRoot = checkWebRoot;
    }

    @Override
    @NbBundle.Messages("LibraryCustomizer.displayName=npm")
    public ProjectCustomizer.Category createCategory(Lookup context) {
        if (checkWebRoot
                && !WebUtils.hasWebRoot(context.lookup(Project.class))) {
            return null;
        }
        return ProjectCustomizer.Category.create(
                CATEGORY_NAME, Bundle.LibraryCustomizer_displayName(), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        LibrariesPanel librariesPanel = new LibrariesPanel(project);
        category.setStoreListener(librariesPanel.createStoreListener());
        return librariesPanel;
    }

    //~ Factories

    @ProjectCustomizer.CompositeCategoryProvider.Registrations({
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org.netbeans.modules.web.clientproject", // NOI18N
                category = "JsLibs", // NOI18N
                position = 100),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-php-project", // NOI18N
                category = "JsLibs", // NOI18N
                position = 100),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-web-project", // NOI18N
                category = "JsLibs", // NOI18N
                position = 100),
    })
    public static ProjectCustomizer.CompositeCategoryProvider forWebProjects() {
        return new LibraryCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registrations({
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-maven", // NOI18N
                category = "JsLibs", // NOI18N
                position = 100),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-gradle", // NOI18N
                category = "JsLibs", // NOI18N
                position = 100),
    })
    public static ProjectCustomizer.CompositeCategoryProvider forOtherProjects() {
        return new LibraryCustomizer(true);
    }

}
