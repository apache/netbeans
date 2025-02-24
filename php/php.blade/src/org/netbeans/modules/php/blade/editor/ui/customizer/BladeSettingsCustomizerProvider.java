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
package org.netbeans.modules.php.blade.editor.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Blade settings node
 *
 * @author bhaidu
 */
public class BladeSettingsCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String CUSTOMIZER_IDENT = "Laravel Blade"; // NOI18N

    @NbBundle.Messages("BladeSettingsCustomizerProvider.name=Blade")
    @Override
    public ProjectCustomizer.Category createCategory(Lookup lkp) {
        List<ProjectCustomizer.Category> subcategories = new ArrayList<>();
        BladeOptionsCustomizerProvider optionsCustomizer = new BladeOptionsCustomizerProvider();
        subcategories.add(optionsCustomizer.createCategory(lkp));
        BladeDirectivesCustomizerProvider directiveCustomizer = new BladeDirectivesCustomizerProvider();
        subcategories.add(directiveCustomizer.createCategory(lkp));
        BladeComponentsCustomizerProvider bladeComponentsCustomizer = new BladeComponentsCustomizerProvider();
        subcategories.add(bladeComponentsCustomizer.createCategory(lkp));
        return ProjectCustomizer.Category.create(CUSTOMIZER_IDENT,
                NbBundle.getMessage(BladeSettingsCustomizerProvider.class, "LBL_LaravelBlade"), null,
                subcategories.toArray(ProjectCustomizer.Category[]::new));
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        switch (category.getName()) {
            case BladeOptionsCustomizerProvider.VIEWS_FOLDERS -> {
                BladeOptionsCustomizerProvider provider = new BladeOptionsCustomizerProvider();
                return provider.createComponent(category, context);
            }
            case BladeDirectivesCustomizerProvider.BLADE_DIRECTIVES -> {
                BladeDirectivesCustomizerProvider directivesProvider = new BladeDirectivesCustomizerProvider();
                return directivesProvider.createComponent(category, context);
            }
            case BladeComponentsCustomizerProvider.COMPONENTS_CUSTOMIZER -> {
                BladeComponentsCustomizerProvider bladeComponentsCustomizer = new BladeComponentsCustomizerProvider();
                return bladeComponentsCustomizer.createComponent(category, context);
            }
        }

        return createGeneralSettingsComponent(category, context);
    }

    public JComponent createGeneralSettingsComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        assert project != null;

        BladeGeneralSettings panel = new BladeGeneralSettings(project);
        category.setOkButtonListener(new Listener(panel));
        return panel;
    }

    private class Listener implements ActionListener {

        private final BladeGeneralSettings panel;

        public Listener(BladeGeneralSettings panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.panel.storeData();
        }

    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org.netbeans.modules.web.clientproject", // NOI18N
            position = 367)
    public static BladeSettingsCustomizerProvider forHtml5Project() {
        return new BladeSettingsCustomizerProvider();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-php-project", // NOI18N
            position = 402)
    public static ProjectCustomizer.CompositeCategoryProvider forPhpProject() {
        return new BladeSettingsCustomizerProvider();
    }

}
