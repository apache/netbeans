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

package org.netbeans.modules.maven.profiler;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider.class, 
                        projectType="org-netbeans-modules-maven") // NOI18N
public class MavenProjectProfilingSupportProvider extends JavaProjectProfilingSupportProvider {
    
    private final Set<String> supportedJ2eePTypes = new HashSet<String>() {
        {
            add(NbMavenProject.TYPE_WAR);
            add(NbMavenProject.TYPE_EJB);
        }
    };
    
    private final Set<String> supportedPTypes = new HashSet<String>() {
        {
            add(NbMavenProject.TYPE_JAR);
            addAll(supportedJ2eePTypes);
            add(NbMavenProject.TYPE_NBM);
            add(NbMavenProject.TYPE_NBM_APPLICATION);
            add(NbMavenProject.TYPE_OSGI);
        }
    };
    
    
    @Override
    public JavaPlatform resolveProjectJavaPlatform() {
        return JavaPlatform.getDefaultPlatform();
    }

    @Override
    public boolean isProfilingSupported() {
        NbMavenProject mproject = getMavenProject();
        return mproject == null ? false : supportedPTypes.contains(mproject.getPackagingType());
    }
    
    @Override
    public boolean checkProjectCanBeProfiled(FileObject file) {
        if (isJ2EEProject(getMavenProject())) {
            // Java EE project
            return true;
        } else {
            // Java SE project
            return super.checkProjectCanBeProfiled(file);
        }
    }
    
    @Override
    public boolean isFileObjectSupported(FileObject file) {
        if (isJ2EEProject(getMavenProject())) {
            // Java EE project
            return isHttpServlet(file) || isJSP(file);
        } else {
            // Java SE project
            return super.isFileObjectSupported(file);
        }
    }
        
    
    private boolean isHttpServlet(FileObject fo) {
        JavaProfilerSource src = JavaProfilerSource.createFrom(fo);
        return src != null && src.isInstanceOf("javax.servlet.http.HttpServlet"); // NOI18N
    }

    private boolean isJSP(FileObject fo) {
        return "jsp".equals(fo.getExt()); // NOI18N
    }
    
    private boolean isJ2EEProject(NbMavenProject mproject) {
        return supportedJ2eePTypes.contains(mproject.getPackagingType());
    }
    
    private NbMavenProject getMavenProject() {
        return getProject().getLookup().lookup(NbMavenProject.class);
    }
    
    
    public MavenProjectProfilingSupportProvider(Project project) {
        super(project);
    }
}
