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
package org.netbeans.modules.profiler.j2se;

import java.io.File;
import java.net.URL;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.netbeans.modules.profiler.projectsupport.utilities.AppletSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider.class, 
                        projectType="org-netbeans-modules-java-j2seproject" // NOI18N
)
public class J2SEAntProjectSupportProvider extends AntProjectSupportProvider.Abstract {    
    
    public J2SEAntProjectSupportProvider(Project project) {
        super(project);
    }
    
    @Override
    public void configurePropertiesForProfiling(final Map<String, String> props, final FileObject profiledClassFile) {
        if (profiledClassFile == null) {
//            if (mainClassSetManually != null) {
//                props.put("main.class", mainClassSetManually); // NOI18N
//                mainClassSetManually = null;
//            }
        } else {
            // In case the class to profile is explicitely selected (profile-single)
            // 1. specify profiled class name
            
            // FIXME
            JavaProfilerSource src = JavaProfilerSource.createFrom(profiledClassFile);
            if (src != null) {
                Project project = getProject();
                if (src.isApplet()) {
                    
                    String jvmargs = props.get("run.jvmargs"); // NOI18N
                    PropertyEvaluator projectProps = J2SEProjectProfilingSupportProvider.getProjectProperties(project);

                    URL url = null;

                    // do this only when security policy is not set manually
                    if ((jvmargs == null) || !(jvmargs.indexOf("java.security.policy") > 0)) { //NOI18N
                        String buildDirProp = projectProps.getProperty("build.dir"); //NOI18N
                                                                                     // TODO [M9] what if buildDirProp is null?

                        FileObject buildFolder = ProjectUtilities.getOrCreateBuildFolder(project, buildDirProp);

                        AppletSupport.generateSecurityPolicy(project.getProjectDirectory(), buildFolder);

                        if ((jvmargs == null) || (jvmargs.length() == 0)) {
                            props.put("run.jvmargs",
                                              "-Djava.security.policy=" + FileUtil.toFile(buildFolder).getPath() + File.separator
                                              + "applet.policy"); //NOI18N
                        } else {
                            props.put("run.jvmargs",
                                              jvmargs + " -Djava.security.policy=" + FileUtil.toFile(buildFolder).getPath()
                                              + File.separator + "applet.policy"); //NOI18N
                        }
                    }

                    if (profiledClassFile.existsExt("html") || profiledClassFile.existsExt("HTML")) { //NOI18N
                        url = ProjectUtilities.copyAppletHTML(project, projectProps, profiledClassFile, "html"); //NOI18N
                    } else {
                        url = ProjectUtilities.generateAppletHTML(project, projectProps, profiledClassFile);
                    }

                    if (url == null) {
                        return; // TODO: fail?
                    }

                    props.put("applet.url", url.toString()); // NOI18N
                } else {
                    final String profiledClass = src.getTopLevelClass().getQualifiedName();
                    props.put("profile.class", profiledClass); //NOI18N
                }

                // 2. include it in javac.includes so that the compile-single picks it up
                final String clazz = FileUtil.getRelativePath(ProjectUtilities.getRootOf(ProjectUtilities.getSourceRoots(project),
                                                                                         profiledClassFile), profiledClassFile);
                props.put("javac.includes", clazz); //NOI18N
            }
        }
    }
    
}
