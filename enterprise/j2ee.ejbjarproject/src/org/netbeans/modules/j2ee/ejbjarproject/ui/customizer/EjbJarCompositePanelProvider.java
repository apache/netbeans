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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProvider;
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
public class EjbJarCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";

    private static final String BUILD = "Build";
    private static final String JAR = "Jar";
    private static final String JAVADOC = "Javadoc";
    public static final String RUN = "Run";
    
    private static final String WEBSERVICES = "WebServices";
    private static final String WEBSERVICESCATEGORY = "WebServicesCategory";
    private static final String LICENSE = "License";
    
    private String name;
    
    /** Creates a new instance of EjbJarCompositePanelProvider */
    public EjbJarCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( EjbJarCompositePanelProvider.class );
        ProjectCustomizer.Category toReturn = null;
        
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"), //NOI18N
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
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    bundle.getString( "LBL_Config_Run" ), // NOI18N
                    null);
        } else if (WEBSERVICESCATEGORY.equals(name)
                && showWebServicesCategory(context.lookup(EjbJarProjectProperties.class))) {
            ProjectCustomizer.Category services = ProjectCustomizer.Category.create(WEBSERVICES,
                    bundle.getString("LBL_Config_WebServices"), // NOI18N
                    null);
            toReturn = ProjectCustomizer.Category.create(WEBSERVICESCATEGORY,
                    bundle.getString("LBL_Config_WebServiceCategory"), // NOI18N
                    null, services);
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
        EjbJarProjectProperties uiProps = context.lookup(EjbJarProjectProperties.class);
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
        } else if (WEBSERVICES.equals(nm)) {
            EjbJarProvider ejbJarProvider = uiProps.getProject().getLookup().lookup(EjbJarProvider.class);
            FileObject metaInf = ejbJarProvider.getMetaInf();
            List servicesSettings = null;
            if (metaInf != null) {
                WebServicesSupport servicesSupport = WebServicesSupport.getWebServicesSupport(metaInf);
                if (servicesSupport != null) {
                    servicesSettings = servicesSupport.getServices();
                }
            }
            if(servicesSettings != null && servicesSettings.size() > 0) {
                return new CustomizerWSServiceHost( uiProps, servicesSettings );
            } else {
                return new NoWebServicesPanel();
            }
        } else if (LICENSE.equals(nm)) {
            return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, uiProps.LICENSE_SUPPORT);
        }
        
        return new JPanel();
    }

    public static EjbJarCompositePanelProvider createSources() {
        return new EjbJarCompositePanelProvider(SOURCES);
    }
    
    public static EjbJarCompositePanelProvider createLibraries() {
        return new EjbJarCompositePanelProvider(LIBRARIES);
    }

    public static EjbJarCompositePanelProvider createBuild() {
        return new EjbJarCompositePanelProvider(BUILD);
    }

    public static EjbJarCompositePanelProvider createJar() {
        return new EjbJarCompositePanelProvider(JAR);
    }

    public static EjbJarCompositePanelProvider createJavadoc() {
        return new EjbJarCompositePanelProvider(JAVADOC);
    }

    public static EjbJarCompositePanelProvider createRun() {
        return new EjbJarCompositePanelProvider(RUN);
    }

    public static EjbJarCompositePanelProvider createWebServicesCategory() {
        return new EjbJarCompositePanelProvider(WEBSERVICESCATEGORY);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-j2ee-ejbjarproject",
        position=605
    )
    public static ProjectCustomizer.CompositeCategoryProvider createLicense() {
        return new EjbJarCompositePanelProvider(LICENSE);
    }

    private static boolean showWebServicesCategory(EjbJarProjectProperties uiProperties) {
        EjbJarProject project = (EjbJarProject) uiProperties.getProject();
        if(Profile.J2EE_14.equals(project.getEjbModule().getJ2eeProfile())) {
            return WebServicesSupport.getWebServicesSupport(project.getProjectDirectory())!=null;
        }
        return false;
    }
}
