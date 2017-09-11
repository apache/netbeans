/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
////            if (mainClassSetManually != null) {
////                props.put("main.class", mainClassSetManually); // NOI18N
////                mainClassSetManually = null;
////            }
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
