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

package org.netbeans.modules.web.project.ui.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint, rnajman
 */
public class WebCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";
    private static final String FRAMEWORKS = "Frameworks";

    private static final String BUILD = "Build";
    private static final String WAR = "War";
    private static final String JAVADOC = "Javadoc";
    public static final String RUN = "Run";
    
    private static final String WEBSERVICESCATEGORY = "WebServicesCategory";
    private static final String WEBSERVICES = "WebServices";
    private static final String WEBSERVICECLIENTS = "WebServiceClients";
    private static final String LICENSE = "License";

    private String name;
    
    private WebCompositePanelProvider(String name) {
        this.name = name;
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;

        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"), //NOI18N
                    null);
        } else if (FRAMEWORKS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    FRAMEWORKS,
                    bundle.getString( "LBL_Config_Frameworks" ), // NOI18N
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
        } else if (WAR.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    WAR,
                    bundle.getString( "LBL_Config_War" ), // NOI18N
                    null);
        } else if (JAVADOC.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAVADOC,
                    bundle.getString( "LBL_Config_Javadoc" ), // NOI18N
                    null);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    bundle.getString( "LBL_Config_Run" ), // NOI18N
                    null);
        } else if (WEBSERVICESCATEGORY.equals(name) && showWebServicesCategory(
                context.lookup(WebProjectProperties.class))) {
            ProjectCustomizer.Category services = ProjectCustomizer.Category.create(WEBSERVICES,
                    bundle.getString("LBL_Config_WebServices"), // NOI18N
                    null);
            ProjectCustomizer.Category clients = ProjectCustomizer.Category.create(WEBSERVICECLIENTS,
                    bundle.getString("LBL_Config_WebServiceClients"), // NOI18N
                    null);
            toReturn = ProjectCustomizer.Category.create(WEBSERVICESCATEGORY,
                    bundle.getString("LBL_Config_WebServiceCategory"), // NOI18N
                    null, services, clients);
        } else if (LICENSE.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LICENSE,
                    bundle.getString("LBL_Config_License"), // NOI18N
                    null);
        }
        
        return toReturn;
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        final WebProjectProperties uiProps = context.lookup(WebProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(uiProps);
        } else if (FRAMEWORKS.equals(nm)) {
            return new CustomizerFrameworks(category, uiProps);
        } else if (LIBRARIES.equals(nm)) {
            CustomizerProviderImpl.SubCategoryProvider prov = context.lookup(CustomizerProviderImpl.SubCategoryProvider.class);
            assert prov != null : "Assuming CustomizerProviderImpl.SubCategoryProvider in customizer context";
            return new CustomizerLibraries(uiProps, prov);
        } else if (BUILD.equals(nm)) {
            return new CustomizerCompile(uiProps);
        } else if (WAR.equals(nm)) {
            return new CustomizerWar(category, uiProps);
        } else if (JAVADOC.equals(nm)) {
            return new CustomizerJavadoc(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(category, uiProps);
        } else if (WEBSERVICES.equals(nm) || WEBSERVICECLIENTS.equals(nm)) {
            ProjectWebModule wm = uiProps.getProject().getLookup().lookup(ProjectWebModule.class);
            FileObject docBase = wm.getDocumentBase();
            if (WEBSERVICES.equals(nm)) {
                List servicesSettings = null;
                if (docBase != null) {
                    WebServicesSupport servicesSupport = WebServicesSupport.getWebServicesSupport(docBase);
                    if (servicesSupport != null) {
                        servicesSettings = servicesSupport.getServices();
                    }
                }
                if(servicesSettings != null && servicesSettings.size() > 0) {
                    return new CustomizerWSServiceHost( uiProps, servicesSettings );
                } else {
                    return new NoWebServicesPanel();
                }            
            } else if (WEBSERVICECLIENTS.equals(nm)) {
                List serviceClientsSettings = null;
                if (docBase != null) {
                    WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(docBase);
                    if (clientSupport != null) {
                        serviceClientsSettings = clientSupport.getServiceClients();
                    }
                }
                if(serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
                    return new CustomizerWSClientHost( uiProps, serviceClientsSettings );
                } else {
                    return new NoWebServiceClientsPanel();
                }
            }
        } else if (LICENSE.equals(nm)) {
            return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, uiProps.LICENSE_SUPPORT);
        }

        return new JPanel();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=100)
    public static WebCompositePanelProvider createSources() {
        return new WebCompositePanelProvider(SOURCES);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=200)
    public static WebCompositePanelProvider createFrameworks() {
        return new WebCompositePanelProvider(FRAMEWORKS);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=300)
    public static WebCompositePanelProvider createLibraries() {
        return new WebCompositePanelProvider(LIBRARIES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=100, category="BuildCategory")
    public static WebCompositePanelProvider createBuild() {
        return new WebCompositePanelProvider(BUILD);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=200, category="BuildCategory")
    public static WebCompositePanelProvider createWar() {
        return new WebCompositePanelProvider(WAR);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=300, category="BuildCategory")
    public static WebCompositePanelProvider createJavadoc() {
        return new WebCompositePanelProvider(JAVADOC);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=500)
    public static WebCompositePanelProvider createRun() {
        return new WebCompositePanelProvider(RUN);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", position=600)
    public static WebCompositePanelProvider createWebServicesCategory() {
        return new WebCompositePanelProvider(WEBSERVICESCATEGORY);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-web-project", position = 375)
    public static ProjectCustomizer.CompositeCategoryProvider createCssPreprocessors() {
        return CssPreprocessorsUI.getDefault().createCustomizer();
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-java-j2seproject", position = 500)
    public static ProjectCustomizer.CompositeCategoryProvider createCssPreprocessorsj2se() {
        return CssPreprocessorsUI.getDefault().createCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-web-project",
        position=605
    )
    public static ProjectCustomizer.CompositeCategoryProvider createLicense() {
        return new WebCompositePanelProvider(LICENSE);
    }

    private static boolean showWebServicesCategory(WebProjectProperties uiProperties) {
        WebProject project = uiProperties.getProject();
        if(!project.isJavaEE5(project)) {
            WebServicesSupport servicesSupport = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
            return servicesSupport!=null || clientSupport!=null;
        }
        return false;
    }
}
