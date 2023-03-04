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

package org.netbeans.modules.javascript2.requirejs.ui;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.editor.api.FrameworksUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsCustomizer implements ProjectCustomizer.CompositeCategoryProvider {
@ProjectCustomizer.CompositeCategoryProvider.Registrations({
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = FrameworksUtils.HTML5_CLIENT_PROJECT,
        category = FrameworksUtils.CATEGORY,
        position = 260
    ),
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = FrameworksUtils.PHP_PROJECT,
        category = FrameworksUtils.CATEGORY,
        position = 260
    ),
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = FrameworksUtils.MAVEN_PROJECT,
        category = FrameworksUtils.CATEGORY,
        position = 260
    ),
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = FrameworksUtils.GRADLE_PROJECT,
        category = FrameworksUtils.CATEGORY,
        position = 260
    ),
})
    
    public static RequireJsCustomizer createCustomizer() {
        return new RequireJsCustomizer();
    }

    @NbBundle.Messages("RequireJsCustomizer.displayName=RequireJs")
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                "requirejs", // NOI18N
                Bundle.RequireJsCustomizer_displayName(),
                null, (ProjectCustomizer.Category[]) null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        return new RequireJsPanel(category, project);
    }
    
}
