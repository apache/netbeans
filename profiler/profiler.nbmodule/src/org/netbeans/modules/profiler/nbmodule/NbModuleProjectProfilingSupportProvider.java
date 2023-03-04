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
            Set<? extends Project> subProjects;

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

            return getProjectJavaPlatform(subProjects.iterator().next());
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
