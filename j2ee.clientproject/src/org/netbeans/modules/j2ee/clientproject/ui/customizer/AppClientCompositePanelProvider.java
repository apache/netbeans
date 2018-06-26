/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            AppClientProject project = (AppClientProject) context.lookup(AppClientProject.class);
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
