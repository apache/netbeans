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
package org.netbeans.modules.php.phing.ui.customizer;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.phing.PhingBuildTool;
import org.netbeans.modules.php.phing.preferences.PhingPreferences;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.modules.web.clientproject.spi.build.CustomizerPanelImplementation;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class PhingCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String CUSTOMIZER_IDENT = "Phing"; // NOI18N


    @NbBundle.Messages("PhingCustomizerProvider.name=Phing")
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        if (!PhingBuildTool.forProject(context.lookup(Project.class)).getProjectBuildXml().exists()) {
            return null;
        }
        return ProjectCustomizer.Category.create(CUSTOMIZER_IDENT,
                Bundle.PhingCustomizerProvider_name(), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        assert project != null;
        return BuildTools.getDefault().createCustomizerComponent(
                new CustomizerSupportImpl(category, PhingBuildTool.forProject(project).getPhingPreferences()));
    }

    //~ Factories

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-php-project", // NOI18N
            position = 380)
    public static ProjectCustomizer.CompositeCategoryProvider forPhpProject() {
        return new PhingCustomizerProvider();
    }

    //~ Inner classes

    private static final class CustomizerSupportImpl implements BuildTools.CustomizerSupport {

        private final ProjectCustomizer.Category category;
        private final PhingPreferences preferences;


        public CustomizerSupportImpl(ProjectCustomizer.Category category, PhingPreferences preferences) {
            assert category != null;
            assert preferences != null;
            this.category = category;
            this.preferences = preferences;
        }

        @Override
        public ProjectCustomizer.Category getCategory() {
            return category;
        }

        @NbBundle.Messages("CustomizerSupportImpl.header=Assign IDE actions to Phing targets.")
        @Override
        public String getHeader() {
            return Bundle.CustomizerSupportImpl_header();
        }

        @Override
        public String getTask(String commandId) {
            assert commandId != null;
            return preferences.getTarget(commandId);
        }

        @Override
        public void setTask(String commandId, String task) {
            assert commandId != null;
            preferences.setTarget(commandId, task);
        }

        @Override
        public CustomizerPanelImplementation getCustomizerPanel() {
            return null;
        }

    }

}
