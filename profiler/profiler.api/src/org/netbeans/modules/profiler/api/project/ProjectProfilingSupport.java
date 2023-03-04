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
package org.netbeans.modules.profiler.api.project;

import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Support for profiling projects.
 *
 * @author Jiri Sedlacek
 */
public final class ProjectProfilingSupport {
    
    private static ProjectProfilingSupport DEFAULT;
    
    private final ProjectProfilingSupportProvider provider;
    
    
    /**
     * Returns true if profiling a project is supported.
     * 
     * @return true if profiling a project is supported, false otherwise
     */
    public boolean isProfilingSupported() {
        return provider.isProfilingSupported();
    }
    
    /**
     * Returns true if attaching to a running project is supported.
     * 
     * @return true if attaching to a running project is supported, false otherwise.
     */
    public boolean isAttachSupported() {
        return provider.isAttachSupported();
    }
    
    /**
     * Returns true if profiling the provided file is supported.
     * 
     * @param fo file
     * @return true if profiling the provided file is supported, false otherwise
     */
    public boolean isFileObjectSupported(FileObject fo) {
        return provider.isFileObjectSupported(fo);
    }
    
    /**
     * Returns true if Profiling Points can be processed by this project.
     * 
     * @return true if Profiling Points can be processed by this project, false otherwise.
     */
    public boolean areProfilingPointsSupported() {
        return provider.areProfilingPointsSupported();
    }
    
    /**
     * Returns the Java platform configured for running the project.
     * 
     * @return Java platform configured for running the project
     */
    public JavaPlatform getProjectJavaPlatform() {
        return provider.getProjectJavaPlatform();
    }
    
    /**
     * Returns true if the project is configured and properly set up to be profiled (e.g. profiler is integrated with the project, main class has a main method etc.).
     * 
     * @param profiledClassFile profiled file or null for profiling the entire project
     * @return true if the project is configured and properly set up to be profiled, false otherwise
     */
    public boolean checkProjectCanBeProfiled(FileObject profiledClassFile) {
        return provider.checkProjectCanBeProfiled(profiledClassFile);
    }
    
    /**
     * Configures project-specific session settings.
     * 
     * @param ss session settings
     */
    public void setupProjectSessionSettings(SessionSettings ss) {
        provider.setupProjectSessionSettings(ss);
    }
    
    /**
     * Allows to start a profiling session directly by the ProjectProfilingSupport instance.
     * 
     * @param profiledClassFile profiled file
     * @param isTest true if profiledClassFile is a test, false otherwise
     * @return true if the ProjectProfilingSupport instance started a profiling session, false otherwise
     */
    public boolean startProfilingSession(FileObject profiledClassFile, boolean isTest) {
        return provider.startProfilingSession(profiledClassFile, isTest);
    }
    
    
    private ProjectProfilingSupport(ProjectProfilingSupportProvider provider) {
        this.provider = provider;
    }
    
    private static synchronized ProjectProfilingSupport defaultImpl() {
        if (DEFAULT == null)
            DEFAULT = new ProjectProfilingSupport(new ProjectProfilingSupportProvider.Basic());
        return DEFAULT;
    }
    
    
    /**
     * Returns ProjectProfilingSupport instance for the provided project.
     * 
     * @param project project
     * @return ProjectProfilingSupport instance for the provided project
     */
    public static ProjectProfilingSupport get(Lookup.Provider project) {
        ProjectProfilingSupportProvider provider =
                project != null ? project.getLookup().lookup(ProjectProfilingSupportProvider.class) : null;
        if (provider == null) return defaultImpl();
        else return new ProjectProfilingSupport(provider);
    }
    
    
}
