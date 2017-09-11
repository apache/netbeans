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

package org.netbeans.modules.profiler.nbmodule;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;


/**
 * A class providing basic support for profiling free-form projects.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider.class, 
                        projectTypes={
                            @ProjectType(id="org-netbeans-modules-apisupport-project"),
                            @ProjectType(id="org-netbeans-modules-apisupport-project-suite")
                        }
)
public final class NbModuleProjectProfilingSupportProvider extends JavaProjectProfilingSupportProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String NBMODULE_PROJECT_NAMESPACE_2 = "http://www.netbeans.org/ns/nb-module-project/2"; // NOI18N
    private static final String NBMODULE_PROJECT_NAMESPACE_3 = "http://www.netbeans.org/ns/nb-module-project/3"; // NOI18N
    private static final String NBMODULE_SUITE_PROJECT_NAMESPACE = "http://www.netbeans.org/ns/nb-module-suite-project/1"; // NOI18N
    // -----

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean isFileObjectSupported(final FileObject fo) {
        // FIXME
        JavaProfilerSource src = JavaProfilerSource.createFrom(fo);
        return src != null ? src.isTest() : false; // profile single only for tests
    }

    @Override
    public JavaPlatform resolveProjectJavaPlatform() {
        return getProjectJavaPlatform(getProject());
    }
    
    private JavaPlatform getProjectJavaPlatform(Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        FileObject projectDir = project.getProjectDirectory();

        if (aux.getConfigurationFragment("data", NBMODULE_SUITE_PROJECT_NAMESPACE, true) != null) { // NOI18N
                                                                                                    // NetBeans suite
                                                                                                    // ask first subproject for its JavaPlatform

            SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
            Set subProjects;

            if (ProfilerLogger.isDebug()) {
                ProfilerLogger.debug("NB Suite " + projectDir.getPath()); //NOI18N
            }

            if (spp == null) {
                return null;
            }

            subProjects = spp.getSubprojects();

            if (subProjects.isEmpty()) {
                return null;
            }

            return getProjectJavaPlatform((Project) subProjects.iterator().next());
        }

        ClassPath bootCp = ClassPath.getClassPath(projectDir, ClassPath.BOOT);
        List bootCpEntries = bootCp.entries();

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("Boot CP " + bootCp); //NOI18N
        }

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("File " + projectDir.getPath()); //NOI18N
        }

        org.netbeans.api.java.platform.JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification("j2se", null)); // NOI18N

        for (int i = 0; i < platforms.length; i++) {
            org.netbeans.api.java.platform.JavaPlatform platform = platforms[i];

            if (bootCpEntries.equals(platform.getBootstrapLibraries().entries())) {
                if (ProfilerLogger.isDebug()) {
                    ProfilerLogger.debug("Platform " + platform.getDisplayName()); //NOI18N
                }

                return JavaPlatform.getJavaPlatformById(platform.getProperties().get("platform.ant.name"));
            }
        }

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("Platform null"); //NOI18N
        }

        return null;
    }

    @Override
    public void setupProjectSessionSettings(SessionSettings ss) {
        JavaPlatform platform = getProjectJavaPlatform();
        
        ss.setSystemArchitecture(platform.getPlatformArchitecture());
        ss.setJavaVersionString(platform.getPlatformJDKVersion());
        ss.setJavaExecutable(platform.getPlatformJavaFile());
    }
    
    
    
////    /**
////     * Returns true if the provided Project is a NB source module, false otherwise.
////     * 
////     * @param project Project to be checked.
////     * @return true if the provided Project is a NB source module, false otherwise.
////     */
////    private boolean isNbSourceModule(Project project) {
////        // Resolve project.xml
////        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
////        
////        // Guess the namespace
////        String namespace = NBMODULE_PROJECT_NAMESPACE_3;
////        // Try to resolve nb-module-project/3 (current version in NB sources)
////        Element e = aux.getConfigurationFragment("data", namespace, true); // NOI18N
////        // Not a nb-module-project/3, can still be nb-module-project/2 or a suite
////        if (e == null) {
////            // Try to resolve nb-module-project/2 (just for compatibility)
////            namespace = NBMODULE_PROJECT_NAMESPACE_2;
////            e = aux.getConfigurationFragment("data", namespace, true); // NOI18N
////            // Project is a NB module suite - not a NB source module
////            if (e == null) return false;
////        }
////        
////        // Module is a NB module suite component, not a NB source module
////        if (XMLUtil.findElement(e, "suite-component", namespace) != null) return false; // NOI18N
////        
////        // Module is a NB module suite component, not a NB source module
////        if (XMLUtil.findElement(e, "standalone", namespace) != null) return false; // NOI18N
////        
////        // Module is a NB source module (neither suite component nor standalone)
////        return true;
////    }
    
    
    public NbModuleProjectProfilingSupportProvider(Project project) {
        super(project);
    }
    
}
