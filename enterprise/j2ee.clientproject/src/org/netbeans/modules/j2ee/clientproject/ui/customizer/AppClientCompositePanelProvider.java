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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.wsclient.CustomizerWSClientHost;
import org.netbeans.modules.j2ee.clientproject.wsclient.NoWebServiceClientsPanel;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint, rnajman
 */
public class AppClientCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";

    private static final String BUILD = "Build";
    private static final String JAR = "Jar";
    private static final String JAVADOC = "Javadoc";
    public static final String RUN = "Run";
    
    private static final String WEBSERVICECLIENTS = "WebServiceClients";
    private static final String WEBSERVICESCATEGORY = "WebServicesCategory";
    private static final String LICENSE = "License";
    
    private String name;
    
    /** Creates a new instance of AppClientCompositePanelProvider */
    public AppClientCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"), //NOI18N
                    null,
                    (ProjectCustomizer.Category[]) null);
        } else if (LIBRARIES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LIBRARIES,
                    bundle.getString( "LBL_Config_Libraries" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[]) null );
        } else if (BUILD.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    BUILD,
                    bundle.getString( "LBL_Config_Build" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[]) null);
        } else if (JAR.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAR,
                    bundle.getString( "LBL_Config_Jar" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[]) null );
        } else if (JAVADOC.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAVADOC,
                    bundle.getString( "LBL_Config_Javadoc" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[]) null );
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    bundle.getString( "LBL_Config_Run" ), // NOI18N
                    null,
                    (ProjectCustomizer.Category[]) null );
        } else if (WEBSERVICESCATEGORY.equals(name)) {
            AppClientProject project = context.lookup(AppClientProject.class);
            List serviceClientsSettings = null;
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
            if (clientSupport != null) {
                serviceClientsSettings = clientSupport.getServiceClients();
            }
            if(Profile.J2EE_14.equals(
                    project.getCarModule().getJ2eeProfile()) &&
                    serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
                ProjectCustomizer.Category clients = ProjectCustomizer.Category.create(WEBSERVICECLIENTS,
                        bundle.getString("LBL_Config_WebServiceClients"), // NOI18N
                        null);
                toReturn = ProjectCustomizer.Category.create(WEBSERVICESCATEGORY,
                        bundle.getString("LBL_Config_WebServiceCategory"), // NOI18N
                        null, clients);
            }
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
        AppClientProjectProperties uiProps = context.lookup(AppClientProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(uiProps);
        } else if (LIBRARIES.equals(nm)) {
            CustomizerProviderImpl.SubCategoryProvider prov = context.lookup(CustomizerProviderImpl.SubCategoryProvider.class);
            assert prov != null : "Assuming CustomizerProviderImpl.SubCategoryProvider in customizer context";
            return new CustomizerLibraries(uiProps, prov);
        } else if (BUILD.equals(nm)) {
            return new CustomizerCompile(uiProps);
        } else if (JAR.equals(nm)) {
            return new CustomizerJar(uiProps);
        } else if (JAVADOC.equals(nm)) {
            return new CustomizerJavadoc(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps);
        } else if (WEBSERVICECLIENTS.equals(nm)) {
            List serviceClientsSettings = null;
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(uiProps.getProject().getProjectDirectory());
            if (clientSupport != null) {
                serviceClientsSettings = clientSupport.getServiceClients();
            }
            if(serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
                return new CustomizerWSClientHost( uiProps, serviceClientsSettings );
            } else {
                return new NoWebServiceClientsPanel();
            }
        } else if (LICENSE.equals(nm)) {
            return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, uiProps.LICENSE_SUPPORT);
        }
        
        return new JPanel();
    }

    public static AppClientCompositePanelProvider createSources() {
        return new AppClientCompositePanelProvider(SOURCES);
    }
    
    public static AppClientCompositePanelProvider createLibraries() {
        return new AppClientCompositePanelProvider(LIBRARIES);
    }

    public static AppClientCompositePanelProvider createBuild() {
        return new AppClientCompositePanelProvider(BUILD);
    }

    public static AppClientCompositePanelProvider createJar() {
        return new AppClientCompositePanelProvider(JAR);
    }

    public static AppClientCompositePanelProvider createJavadoc() {
        return new AppClientCompositePanelProvider(JAVADOC);
    }

    public static AppClientCompositePanelProvider createRun() {
        return new AppClientCompositePanelProvider(RUN);
    }

    public static AppClientCompositePanelProvider createWebServicesCategory() {
        return new AppClientCompositePanelProvider(WEBSERVICESCATEGORY);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-j2ee-clientproject",
        position=605
    )
    public static ProjectCustomizer.CompositeCategoryProvider createLicense() {
        return new AppClientCompositePanelProvider(LICENSE);
    }

}
