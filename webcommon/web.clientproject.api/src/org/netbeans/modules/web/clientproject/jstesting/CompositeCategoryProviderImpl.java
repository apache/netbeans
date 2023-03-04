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

package org.netbeans.modules.web.clientproject.jstesting;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class CompositeCategoryProviderImpl implements ProjectCustomizer.CompositeCategoryProvider {

    @NbBundle.Messages("CompositeCategoryProviderImpl.testing.title=JavaScript Testing")
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        if (project == null) {
            throw new IllegalStateException("Project must be found in context: " + context);
        }
        return ProjectCustomizer.Category.create(
                JsTestingProviders.CUSTOMIZER_IDENT,
                Bundle.CompositeCategoryProviderImpl_testing_title(),
                null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        if (JsTestingProviders.CUSTOMIZER_IDENT.equals(category.getName())) {
            Project project = context.lookup(Project.class);
            assert project != null : "Cannot find project in lookup: " + context;
            return new CustomizerJsTesting(category, project);
        }
        return null;
    }

}
