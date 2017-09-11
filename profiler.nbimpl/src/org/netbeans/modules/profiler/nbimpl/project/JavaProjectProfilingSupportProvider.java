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
package org.netbeans.modules.profiler.nbimpl.project;

import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.utils.MiscUtils;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class JavaProjectProfilingSupportProvider extends ProjectProfilingSupportProvider.Basic {
    
    private final Project project;
//    private JavaPlatform customProjectJavaPlatform = null;
    
    @Override
    public boolean isProfilingSupported() {
        return true;
    }
    
    @Override
    public boolean isAttachSupported() {
        return true;
    }
    
    @Override
    public boolean isFileObjectSupported(FileObject file) {
        return file.hasExt("java"); // NOI18N
    }
    
    @Override
    public boolean areProfilingPointsSupported() {
        return true;
    }

    @Override
    public void setupProjectSessionSettings(SessionSettings ss) {
        JavaPlatform platform = getProjectJavaPlatform();
        if (platform != null) {
            ss.setSystemArchitecture(platform.getPlatformArchitecture());
            ss.setJavaVersionString(platform.getPlatformJDKVersion());
            ss.setJavaExecutable(platform.getPlatformJavaFile());
        }
    }
    
    @Override
    @NbBundle.Messages({
        "NoMainMethodMsg=Class \"{0}\" does not have a main method."
    })
    public boolean checkProjectCanBeProfiled(FileObject file) {
        if (file != null) {
            JavaProfilerSource src = JavaProfilerSource.createFrom(file);
            if (src != null && !src.isRunnable()) {
                ProfilerDialogs.displayInfo(Bundle.NoMainMethodMsg(src.getTopLevelClass().getQualifiedName()));
                return false;
            }
            if (!isFileObjectSupported(file)) return false;
        }
        return super.checkProjectCanBeProfiled(file);
    }

    @Override
    @NbBundle.Messages({
        "UnsupportedJavaPlatform=Project Java platform is not supported for profiling.",
        "IncorrectJavaSpecVersionDialogCaption=Warning",
        "IncorrectJavaSpecVersionDialogMsg=The specification version of project Java Platform is greater than specification version of the\nplatform that will be used for profiling. You may experience problems unless you set the compiler\nparameter to generate bytecode compatible with the platform that will be used.\n\nDo you want to continue with the current settings?"
    })
    synchronized public JavaPlatform getProjectJavaPlatform() {
        JavaPlatform projectJavaPlatform = resolveProjectJavaPlatform();
        
        if (projectJavaPlatform != null && !MiscUtils.isSupportedJVM(projectJavaPlatform.getSystemProperties())) {
            ProfilerDialogs.displayError(Bundle.UnsupportedJavaPlatform());
            return null;
        }
        
        return projectJavaPlatform;
        
//        // 1. check if we have a Java platform to use for profiling
//        final ProfilerIDESettings gps = ProfilerIDESettings.getInstance();
//        JavaPlatform platform = JavaPlatform.getJavaPlatformById(gps.getJavaPlatformForProfiling());
//        JavaPlatform projectJavaPlatform = resolveProjectJavaPlatform();
//
//        if (platform == null) { // should use the one defined in project
//            platform = projectJavaPlatform;
//
//            if ((platform == null) || !MiscUtils.isSupportedJVM(platform.getSystemProperties())) {
//                if (customProjectJavaPlatform == null) {
//                    customProjectJavaPlatform = JavaPlatformSelector.getDefault().selectPlatformToUse();
//
//                    if (customProjectJavaPlatform == null) {
//                        return null;
//                    }
//                }
//                platform = customProjectJavaPlatform;
//            }
//        }
//
//        if (projectJavaPlatform != null) { // check that the project platform is not newer than platform to use
//
//            while (true) {
//                if (projectJavaPlatform.getVersion().compareTo(platform.getVersion()) > 0) {
//                    Boolean ret = ProfilerDialogs.displayCancellableConfirmation(
//                            Bundle.IncorrectJavaSpecVersionDialogMsg(),
//                            Bundle.IncorrectJavaSpecVersionDialogCaption());
//
//                    if (Boolean.TRUE.equals(ret)) {
//                        break;
//                    } else if (Boolean.FALSE.equals(ret)) {
//                        customProjectJavaPlatform = JavaPlatformSelector.getDefault().selectPlatformToUse();
//
//                        if (customProjectJavaPlatform == null) {
//                            return null; // cancelled by the user
//                        }
//                        platform = customProjectJavaPlatform;
//                    } else { // cancelled
//
//                        return null;
//                    }
//                } else {
//                    break; // version comparison OK.
//                }
//            }
//        }
//        return platform;
    }
    
    abstract protected JavaPlatform resolveProjectJavaPlatform();
    
    protected final JavaPlatform getPlatformByName(String platformName) {
        if (platformName == null || platformName.equals("default_platform")) { // NOI18N
            return JavaPlatform.getDefaultPlatform(); 
        }

        return JavaPlatform.getJavaPlatformById(platformName);
    }
    
    protected final Project getProject() {
        return project;
    }
    
    
    protected JavaProjectProfilingSupportProvider(Project project) {
        this.project = project;
    }
    
}
