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

package org.netbeans.modules.java.j2semodule.ui.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
import org.netbeans.modules.java.j2semodule.J2SEModularProjectUtil;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class J2SECompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";
    
    private static final String BUILD = "Build";
    private static final String JAR = "Jar";
    private static final String JAVADOC = "Javadoc";
    public static final String RUN = "Run";
    private static final String APPLICATION = "Application";
    private static final String LICENSE = "License";

    private String name;
    
    private J2SECompositePanelProvider(String name) {
        this.name = name;
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"),
                    null);
        } else if (LIBRARIES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LIBRARIES,
                    bundle.getString( "LBL_Config_Libraries" ), // NOI18N
                    null);
        } else if (BUILD.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    BUILD,
                    bundle.getString( "LBL_Config_Build" ), // NOI18N
                    null);
        } else if (JAR.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAR,
                    bundle.getString( "LBL_Config_Jar" ), // NOI18N
                    null);
        } else if (JAVADOC.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAVADOC,
                    bundle.getString( "LBL_Config_Javadoc" ), // NOI18N
                    null);
        } else if (LICENSE.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LICENSE,
                    bundle.getString("LBL_Config_License"), // NOI18N
                    null);
        } else if (RUN.equals(name)) {
            boolean fxOverride = false;
            final Project project = context.lookup(Project.class);
            if (project != null) {
                final J2SEModularProject j2sepe = project.getLookup().lookup(J2SEModularProject.class);
                fxOverride = J2SEModularProjectUtil.isTrue(j2sepe.evaluator().getProperty("javafx.enabled")); // NOI18N
            }
            if(fxOverride) {
                toReturn = null;
            } else {
                toReturn = ProjectCustomizer.Category.create(
                        RUN,
                        bundle.getString( "LBL_Config_Run" ), // NOI18N
                        null);
            }
        }
        assert toReturn != null || RUN.equals(name) : "No category for name:" + name;
        return toReturn;
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, final Lookup context) {
        String nm = category.getName();
        final J2SEModularProjectProperties uiProps = context.lookup(J2SEModularProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(uiProps);
        } else if (LIBRARIES.equals(nm)) {
            CustomizerProviderImpl.SubCategoryProvider prov = context.lookup(CustomizerProviderImpl.SubCategoryProvider.class);
            assert prov != null : "Assuming CustomizerProviderImpl.SubCategoryProvider in customizer context";
            return new CustomizerLibraries(uiProps, prov, category);
        } else if (BUILD.equals(nm)) {
            return new CustomizerCompile(uiProps);
        } else if (JAR.equals(nm)) {
            return new CustomizerJar(uiProps);
        } else if (JAVADOC.equals(nm)) {
            return new CustomizerJavadoc(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps);
        } else if (APPLICATION.equals(nm)) {
            return new CustomizerApplication(uiProps);
        } else if (LICENSE.equals(nm)) {
            CustomizerUtilities.LicensePanelContentHandler handler =
            new CustomizerUtilities.LicensePanelContentHandler() {
                @Override
                public String getProjectLicenseLocation() {
                    return uiProps.LICENSE_PATH_VALUE;
                }

                @Override
                public String getGlobalLicenseName() {
                    return uiProps.LICENSE_NAME_VALUE;
                }

                @Override
                public FileObject resolveProjectLocation(@NonNull String path) {
                    final J2SEModularProject project = context.lookup(J2SEModularProject.class);
                    String evaluated = uiProps.getEvaluator().evaluate(path);
                    return project.getAntProjectHelper().resolveFileObject(evaluated);
                }

                @Override
                public void setProjectLicenseLocation(@NullAllowed String newLocation) {
                    uiProps.LICENSE_PATH_VALUE = newLocation;
                }

                @Override
                public void setGlobalLicenseName(@NullAllowed String newName) {
                    uiProps.LICENSE_NAME_VALUE = newName;
                }

                @Override
                public String getDefaultProjectLicenseLocation() {
                    return "./nbproject/licenseheader.txt";
                }

                @Override
                public void setProjectLicenseContent(@NullAllowed String text) {
                    uiProps.CHANGED_LICENSE_PATH_CONTENT = text;
                }
            };
            
            return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, handler);
        }
        return new JPanel();

    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        position=100
    )
    public static J2SECompositePanelProvider createSources() {
        return new J2SECompositePanelProvider(SOURCES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        position=200
    )
    public static J2SECompositePanelProvider createLibraries() {
        return new J2SECompositePanelProvider(LIBRARIES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        category="BuildCategory",
        position=100
    )
    public static J2SECompositePanelProvider createBuild() {
        return new J2SECompositePanelProvider(BUILD);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        category="BuildCategory",
        position=200
    )
    public static J2SECompositePanelProvider createJar() {
        return new J2SECompositePanelProvider(JAR);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        category="BuildCategory",
        position=300
    )
    public static J2SECompositePanelProvider createJavadoc() {
        return new J2SECompositePanelProvider(JAVADOC);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        position=400
    )
    public static J2SECompositePanelProvider createRun() {
        return new J2SECompositePanelProvider(RUN);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        category="Application",
        position=500,
        categoryLabel="#LBL_Config_Application"
    )
    public static J2SECompositePanelProvider createApplication() {
        return new J2SECompositePanelProvider(APPLICATION);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2semodule",
        position=605
    )
    public static J2SECompositePanelProvider createLicense() {
        return new J2SECompositePanelProvider(LICENSE);
    }
    
}
