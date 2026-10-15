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
package org.netbeans.modules.rust.project.ui.customizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A ProjectCustomizer.CompositeCategoryProvider that returns different project
 * customizers sorted by categories.
 */
public final class RustCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final Logger LOG = Logger.getLogger(RustCompositeCategoryProvider.class.getName());

    public static final String CATEGORY_LICENSE = "category-license";

    private final String categoryName;

    public RustCompositeCategoryProvider(String categoryName) {
        this.categoryName = categoryName;
        LOG.log(Level.INFO, "Instantiated RustCompositeCategoryProvider with category {0}", categoryName);
    }

    @NbBundle.Messages({
        "RustCompositeCategoryProvider.category.license.title=License headers",})
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CATEGORY_LICENSE, Bundle.RustCompositeCategoryProvider_category_license_title(), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String requestedCategoryName = category.getName();

        switch (requestedCategoryName) {
            case CATEGORY_LICENSE: {
                return createLicenseComponent(category);
            }
            default:
                throw new IllegalStateException(String.format("Don't know how to create a component for category with name '%s'", requestedCategoryName));
        }
    }

    private JComponent createLicenseComponent(ProjectCustomizer.Category category) {
        CustomizerUtilities.LicensePanelContentHandler handler = new CustomizerUtilities.LicensePanelContentHandler() {
            @Override
            public String getProjectLicenseLocation() {
                return "license.txt"; // NOI18N
            }

            @Override
            public String getGlobalLicenseName() {
                return "global-license-name.txt";
            }

            @Override
            public FileObject resolveProjectLocation(@NonNull String path) {
//                PhpProject project = uiProps.getProject();
//                String evaluated = ProjectPropertiesSupport.getPropertyEvaluator(project).evaluate(path);
//                assert evaluated != null : path;
//                return project.getHelper().resolveFileObject(evaluated);
                return null;
            }

            @Override
            public void setProjectLicenseLocation(@NullAllowed String newLocation) {
//                uiProps.setLicensePathValue(newLocation);
            }

            @Override
            public void setGlobalLicenseName(@NullAllowed String newName) {
//                uiProps.setLicenseNameValue(newName);
            }

            @Override
            public String getDefaultProjectLicenseLocation() {
                return "./nbproject/licenseheader.txt"; // NOI18N
            }

            @Override
            public void setProjectLicenseContent(@NullAllowed String text) {
//                uiProps.setChangedLicensePathContent(text);
            }

        };
        return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, handler);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = RustProjectAPI.RUST_PROJECT_KEY,
            position = 1000
    )
    public static ProjectCustomizer.CompositeCategoryProvider createLicenseCustomizer() {
        return new RustCompositeCategoryProvider(CATEGORY_LICENSE);
    }

}
