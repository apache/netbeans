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
    public synchronized JavaPlatform getProjectJavaPlatform() {
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
    
    protected abstract JavaPlatform resolveProjectJavaPlatform();
    
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
