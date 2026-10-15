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

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.EventQueue;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.editor.indent.project.api.Customizers;
import org.netbeans.modules.php.api.documentation.PhpDocumentations;
import org.netbeans.modules.php.api.framework.PhpFrameworks;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik, Radek Matous
 */
public class CompositePanelProviderImpl implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String SOURCES = "Sources"; // NOI18N
    public static final String RUN = "Run"; // NOI18N
    public static final String BROWSER = "Browser"; // NOI18N
    public static final String PHP_INCLUDE_PATH = "PhpIncludePath"; // NOI18N
    public static final String IGNORE_PATH = "IgnorePath"; // NOI18N
    public static final String FRAMEWORKS = "Frameworks"; // NOI18N
    public static final String TESTING = PhpTesting.CUSTOMIZER_IDENT;
    public static final String TESTING_SELENIUM = "SeleniumTesting"; // NOI18N
    public static final String ANNOTATIONS = "Annotations"; // NOI18N
    public static final String LICENSE = "License"; // NOI18N

    private final String name;
    private final Map<ProjectCustomizer.Category, PhpModuleCustomizerExtender> frameworkCategories;
    private final Map<ProjectCustomizer.Category, TestingProviderPanelProvider> testingProviderPanels;

    public CompositePanelProviderImpl(String name) {
        this.name = name;

        if (FRAMEWORKS.equals(name)) {
            frameworkCategories = new LinkedHashMap<>();
        } else {
            frameworkCategories = null;
        }
        if (TESTING.equals(name)) {
            testingProviderPanels = Collections.synchronizedMap(new LinkedHashMap<ProjectCustomizer.Category, TestingProviderPanelProvider>());
        } else {
            testingProviderPanels = null;
        }
    }

    @NbBundle.Messages({
        "CompositePanelProviderImpl.category.testing.title=Testing",
        "CompositePanelProviderImpl.category.selenium.testing.title=Selenium Testing",
        "CompositePanelProviderImpl.category.browser.title=Browser",
        "CompositePanelProviderImpl.category.annotations.title=Annotations",
        "CompositePanelProviderImpl.category.licenceHeaders.title=License Headers",
    })
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ProjectCustomizer.Category toReturn = null;
        final ProjectCustomizer.Category[] categories = null;
        PhpProject project = context.lookup(PhpProject.class);
        assert project != null;
        PhpProjectProperties uiProps = context.lookup(PhpProjectProperties.class);
        assert uiProps != null;
        assert project == uiProps.getProject() : project + " <> " + uiProps.getProject();
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_Sources"),
                    null,
                    categories);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_RunConfig"),
                    null,
                    categories);
        } else if (BROWSER.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    BROWSER,
                    Bundle.CompositePanelProviderImpl_category_browser_title(),
                    null,
                    categories);
        } else if (ANNOTATIONS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    ANNOTATIONS,
                    Bundle.CompositePanelProviderImpl_category_annotations_title(),
                    null,
                    categories);
        } else if (PHP_INCLUDE_PATH.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    PHP_INCLUDE_PATH,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_PhpIncludePath"),
                    null,
                    categories);
        } else if (IGNORE_PATH.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    IGNORE_PATH,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_IgnorePath"),
                    null,
                    categories);
        } else if (FRAMEWORKS.equals(name)) {
            fillFrameworkCategories(project);
            if (frameworkCategories.isEmpty()) {
                return null;
            }
            List<ProjectCustomizer.Category> subcategories = sortCategories(frameworkCategories.keySet());
            toReturn = ProjectCustomizer.Category.create(
                    FRAMEWORKS,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_Frameworks"),
                    null,
                    subcategories.toArray(new ProjectCustomizer.Category[0]));
        } else if (TESTING.equals(name)) {
            fillTestingProviderPanels(uiProps, context);
            List<ProjectCustomizer.Category> subcategories = sortCategories(testingProviderPanels.keySet());
            toReturn = ProjectCustomizer.Category.create(
                    TESTING,
                    Bundle.CompositePanelProviderImpl_category_testing_title(),
                    null,
                    subcategories.toArray(new ProjectCustomizer.Category[0]));
        } else if (TESTING_SELENIUM.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    TESTING_SELENIUM,
                    Bundle.CompositePanelProviderImpl_category_selenium_testing_title(),
                    null);
        } else if (LICENSE.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LICENSE,
                    Bundle.CompositePanelProviderImpl_category_licenceHeaders_title(),
                    null);
        }
        assert toReturn != null : "No category for name: " + name;
        return toReturn;
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        final PhpProjectProperties uiProps = context.lookup(PhpProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(category, uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps, category);
        } else if (BROWSER.equals(nm)) {
            return new CustomizerBrowser(category, uiProps);
        } else if (ANNOTATIONS.equals(nm)) {
            return new CustomizerAnnotations(category, context.lookup(PhpProject.class));
        } else if (PHP_INCLUDE_PATH.equals(nm)) {
            return new CustomizerIncludePath(category, uiProps);
        } else if (IGNORE_PATH.equals(nm)) {
            return new CustomizerIgnorePath(category, uiProps);
        } else if (FRAMEWORKS.equals(nm)) {
            return new JPanel();
        } else if (TESTING.equals(nm)) {
            return new CustomizerTesting(category, uiProps, creatingTestingPanels());
        } else if (TESTING_SELENIUM.equals(nm)) {
            return new CustomizerSeleniumTesting(category, uiProps);
        } else if (LICENSE.equals(nm)) {
            CustomizerUtilities.LicensePanelContentHandler handler = new CustomizerUtilities.LicensePanelContentHandler() {
                @Override
                public String getProjectLicenseLocation() {
                    return uiProps.getLicensePathValue();
                }
                @Override
                public String getGlobalLicenseName() {
                    return uiProps.getLicenseNameValue();
                }
                @Override
                public FileObject resolveProjectLocation(@NonNull String path) {
                    PhpProject project = uiProps.getProject();
                    String evaluated = ProjectPropertiesSupport.getPropertyEvaluator(project).evaluate(path);
                    assert evaluated != null : path;
                    return project.getHelper().resolveFileObject(evaluated);
                }

                @Override
                public void setProjectLicenseLocation(@NullAllowed String newLocation) {
                    uiProps.setLicensePathValue(newLocation);
                }

                @Override
                public void setGlobalLicenseName(@NullAllowed String newName) {
                    uiProps.setLicenseNameValue(newName);
                }

                @Override
                public String getDefaultProjectLicenseLocation() {
                    return "./nbproject/licenseheader.txt"; // NOI18N
                }

                @Override
                public void setProjectLicenseContent(@NullAllowed String text) {
                    uiProps.setChangedLicensePathContent(text);
                }

            };

            return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, handler);
        }
        // possibly framework?
        if (frameworkCategories != null) {
            PhpModuleCustomizerExtender extender = frameworkCategories.get(category);
            if (extender != null) {
                return new CustomizerFramework(category, extender, uiProps);
            }
        }
        // possibly testing provider?
        if (testingProviderPanels != null) {
            TestingProviderPanelProvider panelProvider = testingProviderPanels.get(category);
            if (panelProvider != null) {
                return panelProvider.getPanel();
            }
        }
        assert false : "No component found for " + category.getDisplayName();
        return new JPanel();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 100
    )
    public static CompositePanelProviderImpl createSources() {
        return new CompositePanelProviderImpl(SOURCES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 150
    )
    public static CompositePanelProviderImpl createRunConfig() {
        return new CompositePanelProviderImpl(RUN);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 180
    )
    public static CompositePanelProviderImpl createBrowser() {
        return new CompositePanelProviderImpl(BROWSER);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 195
    )
    public static ProjectCustomizer.CompositeCategoryProvider createCssPreprocessors() {
        return CssPreprocessorsUI.getDefault().createCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 200
    )
    public static CompositePanelProviderImpl createPhpIncludePath() {
        return new CompositePanelProviderImpl(PHP_INCLUDE_PATH);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 250
    )
    public static CompositePanelProviderImpl createIgnorePath() {
        return new CompositePanelProviderImpl(IGNORE_PATH);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 300
    )
    public static CompositePanelProviderImpl createFrameworks() {
        return new CompositePanelProviderImpl(FRAMEWORKS);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 350
    )
    public static CompositePanelProviderImpl createTesting() {
        return new CompositePanelProviderImpl(TESTING);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = UiUtils.CUSTOMIZER_PATH,
        position = 352
    )
    public static CompositePanelProviderImpl createSeleniumTesting() {
        return new CompositePanelProviderImpl(TESTING_SELENIUM);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = UiUtils.CUSTOMIZER_PATH,
            position = 351)
    public static ProjectCustomizer.CompositeCategoryProvider createJsTesting() {
        return JsTestingProviders.getDefault().createCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = UiUtils.CUSTOMIZER_PATH,
            position = 360)
    public static ProjectCustomizer.CompositeCategoryProvider createPhpDocumentation() {
        return PhpDocumentations.createCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = UiUtils.CUSTOMIZER_PATH,
            position = 370)
    public static ProjectCustomizer.CompositeCategoryProvider createAnnotations() {
        return new CompositePanelProviderImpl(ANNOTATIONS);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 900
    )
    public static CompositePanelProviderImpl createLicense() {
        return new CompositePanelProviderImpl(LICENSE);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = UiUtils.CUSTOMIZER_PATH,
        position = 1000
    )
    public static ProjectCustomizer.CompositeCategoryProvider createFormatting() {
        return Customizers.createFormattingCategoryProvider(Collections.singletonMap(
                "allowedMimeTypes", FileUtils.PHP_MIME_TYPE + ",text/html,text/css,text/javascript,text/x-json")); // NOI18N
    }

    private void fillFrameworkCategories(PhpProject project) {
        frameworkCategories.clear();

        final PhpModule phpModule = project.getPhpModule();
        int i = 0;
        for (PhpFrameworkProvider frameworkProvider : PhpFrameworks.getFrameworks()) {
            PhpModuleCustomizerExtender extender = frameworkProvider.createPhpModuleCustomizerExtender(phpModule);
            if (extender != null) {
                String categoryName = extender.getDisplayName(phpModule);
                if (categoryName == null) {
                    categoryName = frameworkProvider.getName();
                }
                ProjectCustomizer.Category category = ProjectCustomizer.Category.create(
                        FRAMEWORKS + i++,
                        categoryName,
                        null,
                        (ProjectCustomizer.Category[]) null);
                frameworkCategories.put(category, extender);
            }
        }
    }

    private void fillTestingProviderPanels(PhpProjectProperties uiProps, Lookup context) {
        testingProviderPanels.clear();
        PhpModule phpModule = uiProps.getProject().getPhpModule();
        for (PhpTestingProvider testingProvider : PhpTesting.getTestingProviders()) {
            ProjectCustomizer.CompositeCategoryProvider categoryProvider = testingProvider.createCustomizer(phpModule);
            if (categoryProvider == null) {
                continue;
            }
            ProjectCustomizer.Category category = categoryProvider.createCategory(context);
            if (category != null) {
                testingProviderPanels.put(category, new TestingProviderPanelProvider(category, uiProps, testingProvider, categoryProvider, context));
            }
        }
    }

    private Map<String, TestingProviderPanel> creatingTestingPanels() {
        Map<String, TestingProviderPanel> providers = new ConcurrentHashMap<>();
        for (TestingProviderPanelProvider panelProvider : testingProviderPanels.values()) {
            TestingProviderPanel panel = panelProvider.getPanel();
            providers.put(panel.getProviderIdentifier(), panel);
        }
        return providers;
    }

    private static List<ProjectCustomizer.Category> sortCategories(Collection<ProjectCustomizer.Category> categories) {
        final Collator collator = Collator.getInstance();
        List<ProjectCustomizer.Category> sortedCategories = new ArrayList<>(categories);
        sortedCategories.sort(new Comparator<ProjectCustomizer.Category>() {
            @Override
            public int compare(ProjectCustomizer.Category category1, ProjectCustomizer.Category category2) {
                return collator.compare(category1.getDisplayName(), category2.getDisplayName());
            }
        });
        return sortedCategories;
    }

    //~ Inner classes

    private static final class TestingProviderPanelProvider {

        private final ProjectCustomizer.Category category;
        private final PhpProjectProperties uiProps;
        private final PhpTestingProvider testingProvider;
        private final ProjectCustomizer.CompositeCategoryProvider categoryProvider;
        private final Lookup context;

        // @GuardedBy("EDT")
        private TestingProviderPanel panel;


        public TestingProviderPanelProvider(ProjectCustomizer.Category category, PhpProjectProperties uiProps,
                PhpTestingProvider testingProvider, ProjectCustomizer.CompositeCategoryProvider categoryProvider, Lookup context) {
            this.category = category;
            this.uiProps = uiProps;
            this.testingProvider = testingProvider;
            this.categoryProvider = categoryProvider;
            this.context = context;
        }

        public TestingProviderPanel getPanel() {
            assert EventQueue.isDispatchThread();
            if (panel == null) {
                JComponent component = categoryProvider.createComponent(category, context);
                assert component != null : "Component should be created by " + categoryProvider.getClass().getName();
                panel = new TestingProviderPanel(category, uiProps, testingProvider.getIdentifier(), component);
            }
            return panel;
        }

    }

}
