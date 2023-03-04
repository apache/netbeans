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
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.editor.indent.project.api.Customizers;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectType;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class CompositePanelProviderImpl implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String SOURCES = WebClientProjectConstants.CUSTOMIZER_SOURCES_IDENT;
    public static final String RUN = WebClientProjectConstants.CUSTOMIZER_RUN_IDENT;
    private static final String LICENSE = "License"; // NOI18N

    private final String name;

    public CompositePanelProviderImpl(String name) {
        this.name = name;
    }

    @NbBundle.Messages({
        "CompositePanelProviderImpl.sources.title=Sources",
        "CompositePanelProviderImpl.license.title=License Headers",
        "CompositePanelProviderImpl.run.title=Run"
    })
    @Override
    public Category createCategory(Lookup context) {
        ClientSideProject project = context.lookup(ClientSideProject.class);
        assert project != null;
        ProjectCustomizer.Category category = null;
        if (SOURCES.equals(name)) {
            category = ProjectCustomizer.Category.create(
                    SOURCES,
                    Bundle.CompositePanelProviderImpl_sources_title(),
                    null);
        } else if (RUN.equals(name)) {
            if (project.isJsLibrary()) {
                boolean runSupported = false;
                for (PlatformProvider platformProvider : project.getPlatformProviders()) {
                    ActionProvider actionProvider = platformProvider.getActionProvider(project);
                    if (actionProvider != null) {
                        List<String> actions = Arrays.asList(actionProvider.getSupportedActions());
                        if (actions.contains(ActionProvider.COMMAND_RUN)
                                || actions.contains(ActionProvider.COMMAND_DEBUG)) {
                            runSupported = true;
                            break;
                        }
                    }
                }
                if (!runSupported) {
                    return null;
                }
            }
            category = ProjectCustomizer.Category.create(
                    RUN,
                    Bundle.CompositePanelProviderImpl_run_title(),
                    null);
        } else if (LICENSE.equals(name)) {
            category = ProjectCustomizer.Category.create(
                    LICENSE,
                    Bundle.CompositePanelProviderImpl_license_title(),
                    null);
        }
        assert category != null : "No category for name: " + name; //NOI18N
        return category;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        String categoryName = category.getName();
        ClientSideProject project = context.lookup(ClientSideProject.class);
        assert project != null;
        final ClientSideProjectProperties uiProperties = context.lookup(ClientSideProjectProperties.class);
        if (SOURCES.equals(categoryName)) {
            return new SourcesPanel(category, uiProperties);
        } else if (RUN.equals(categoryName)) {
            return new RunPanel(category, uiProperties);
        } else if (LICENSE.equals(categoryName)) {
            return project.is.createLicenseHeaderCustomizerPanel(category, uiProperties.getLicenseSupport());
        }
        assert false : "No component found for " + category.getDisplayName(); //NOI18N
        return new JPanel();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = ClientSideProjectType.TYPE,
            position = 100)
    public static CompositePanelProviderImpl createSources() {
        return new CompositePanelProviderImpl(SOURCES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = ClientSideProjectType.TYPE,
            position = 150)
    public static ProjectCustomizer.CompositeCategoryProvider createCssPreprocessors() {
        return CssPreprocessorsUI.getDefault().createCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = ClientSideProjectType.TYPE,
            position = 300)
    public static CompositePanelProviderImpl createRunConfigs() {
        return new CompositePanelProviderImpl(RUN);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = ClientSideProjectType.TYPE,
            position = 400)
    public static ProjectCustomizer.CompositeCategoryProvider createJsTesting() {
        return JsTestingProviders.getDefault().createCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = ClientSideProjectType.TYPE,
        position=605
    )
    public static ProjectCustomizer.CompositeCategoryProvider createLicense() {
        return new CompositePanelProviderImpl(LICENSE);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = ClientSideProjectType.TYPE,
        position = 1000
    )
    public static ProjectCustomizer.CompositeCategoryProvider createFormatting() {
        return Customizers.createFormattingCategoryProvider(Collections.singletonMap(
                "allowedMimeTypes", "text/html,text/css,text/javascript,text/x-json")); // NOI18N
    }

}
