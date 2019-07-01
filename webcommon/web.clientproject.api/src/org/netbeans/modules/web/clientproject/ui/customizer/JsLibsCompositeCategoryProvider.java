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
package org.netbeans.modules.web.clientproject.ui.customizer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class JsLibsCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CATEGORY_IDENT = "JsLibs"; // NOI18N
    private static final JComponent COMPONENT = new JLabel();

    private final boolean checkWebRoot;


    public JsLibsCompositeCategoryProvider() {
        this(false);
    }

    public JsLibsCompositeCategoryProvider(boolean checkWebRoot) {
        this.checkWebRoot = checkWebRoot;
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        // XXX
        assert false : "Unfortunately, this method gets never called...";
        if (checkWebRoot
                && !WebUtils.hasWebRoot(context.lookup(Project.class))) {
            return null;
        }
        return ProjectCustomizer.Category.create(
                CATEGORY_IDENT, NbBundle.getMessage(JsLibsCompositeCategoryProvider.class, "JsLibs.name"), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        return COMPONENT;
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org.netbeans.modules.web.clientproject", // NOI18N
            category = CATEGORY_IDENT,
            categoryLabel = "#JsLibs.name", // NOI18N
            position = 200)
    public static ProjectCustomizer.CompositeCategoryProvider forHtml5Project() {
        return new JsLibsCompositeCategoryProvider();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-php-project", // NOI18N
            category = CATEGORY_IDENT,
            categoryLabel = "#JsLibs.name", // NOI18N
            position = 190)
    public static ProjectCustomizer.CompositeCategoryProvider forPhpProject() {
        return new JsLibsCompositeCategoryProvider();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-web-project", // NOI18N
            category = CATEGORY_IDENT,
            categoryLabel = "#JsLibs.name", // NOI18N
            position = 350)
    public static ProjectCustomizer.CompositeCategoryProvider forWebProject() {
        return new JsLibsCompositeCategoryProvider();
    }

    // XXX does not work, createCategory() is not called
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-maven", // NOI18N
            category = CATEGORY_IDENT,
            categoryLabel = "#JsLibs.name", // NOI18N
            position = 258)
    public static ProjectCustomizer.CompositeCategoryProvider forMavenProject() {
        return new JsLibsCompositeCategoryProvider(true);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-gradle", // NOI18N
            category = CATEGORY_IDENT,
            categoryLabel = "#JsLibs.name", // NOI18N
            position = 258)
    public static ProjectCustomizer.CompositeCategoryProvider forGradleProject() {
        return new JsLibsCompositeCategoryProvider(true);
    }
}
