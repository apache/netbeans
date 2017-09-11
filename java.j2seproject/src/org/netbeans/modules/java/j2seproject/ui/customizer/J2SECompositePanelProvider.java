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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
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
                final J2SEProject j2sepe = project.getLookup().lookup(J2SEProject.class);
                fxOverride = J2SEProjectUtil.isTrue(j2sepe.evaluator().getProperty("javafx.enabled")); // NOI18N
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
        final J2SEProjectProperties uiProps = context.lookup(J2SEProjectProperties.class);
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
                    final J2SEProject project = context.lookup(J2SEProject.class);
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
        projectType="org-netbeans-modules-java-j2seproject",
        position=100
    )
    public static J2SECompositePanelProvider createSources() {
        return new J2SECompositePanelProvider(SOURCES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2seproject",
        position=200
    )
    public static J2SECompositePanelProvider createLibraries() {
        return new J2SECompositePanelProvider(LIBRARIES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2seproject",
        category="BuildCategory",
        position=100
    )
    public static J2SECompositePanelProvider createBuild() {
        return new J2SECompositePanelProvider(BUILD);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2seproject",
        category="BuildCategory",
        position=200
    )
    public static J2SECompositePanelProvider createJar() {
        return new J2SECompositePanelProvider(JAR);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2seproject",
        category="BuildCategory",
        position=300
    )
    public static J2SECompositePanelProvider createJavadoc() {
        return new J2SECompositePanelProvider(JAVADOC);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2seproject",
        position=400
    )
    public static J2SECompositePanelProvider createRun() {
        return new J2SECompositePanelProvider(RUN);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2seproject",
        category="Application",
        position=500,
        categoryLabel="#LBL_Config_Application"
    )
    public static J2SECompositePanelProvider createApplication() {
        return new J2SECompositePanelProvider(APPLICATION);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-java-j2seproject",
        position=605
    )
    public static J2SECompositePanelProvider createLicense() {
        return new J2SECompositePanelProvider(LICENSE);
    }
    
}
