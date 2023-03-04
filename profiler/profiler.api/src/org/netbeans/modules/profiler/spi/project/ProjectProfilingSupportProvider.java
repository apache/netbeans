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
package org.netbeans.modules.profiler.spi.project;

import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.openide.filesystems.FileObject;

/**
 * Provider of support for profiling projects.
 *
 * @author Jiri Sedlacek
 */
public abstract class ProjectProfilingSupportProvider {
    
    /**
     * Returns true if profiling a project is supported.
     * 
     * @return true if profiling a project is supported, false otherwise
     */
    public abstract boolean isProfilingSupported();
    
    /**
     * Returns true if attaching to a running project is supported.
     * 
     * @return true if attaching to a running project is supported, false otherwise.
     */
    public abstract boolean isAttachSupported();
    
    /**
     * Returns true if profiling the provided file is supported.
     * 
     * @param fo file
     * @return true if profiling the provided file is supported, false otherwise
     */
    public abstract boolean isFileObjectSupported(FileObject fo);
    
    /**
     * Returns true if Profiling Points can be processed by this project.
     * 
     * @return true if Profiling Points can be processed by this project, false otherwise.
     */
    public abstract boolean areProfilingPointsSupported();
    
    /**
     * Returns the Java platform configured for running the project.
     * 
     * @return Java platform configured for running the project
     */
    public abstract JavaPlatform getProjectJavaPlatform();
    
    /**
     * Returns true if the project is configured and properly set up to be profiled (e.g. main class has a main method etc.).
     * 
     * @param profiledClassFile profiled file or null for profiling the entire project
     * @return true if the project is configured and properly set up to be profiled, false otherwise
     */
    public abstract boolean checkProjectCanBeProfiled(FileObject profiledClassFile);
    
    /**
     * Configures project-specific session settings.
     * 
     * @param ss session settings
     */
    public abstract void setupProjectSessionSettings(SessionSettings ss);
    
    /**
     * Allows to start a profiling session directly by the ProjectProfilingSupport instance.
     * 
     * @param profiledClassFile profiled file
     * @param isTest true if profiledClassFile is a test, false otherwise
     * @return true if the ProjectProfilingSupport instance started a profiling session, false otherwise
     */
    public abstract boolean startProfilingSession(FileObject profiledClassFile, boolean isTest);
    
    
    public static class Basic extends ProjectProfilingSupportProvider {

        @Override
        public boolean isProfilingSupported() {
            return false;
        }

        @Override
        public boolean isAttachSupported() {
            return false;
        }

        @Override
        public boolean isFileObjectSupported(FileObject fo) {
            return false;
        }
        
        @Override
        public boolean areProfilingPointsSupported() {
            return false;
        }

        @Override
        public JavaPlatform getProjectJavaPlatform() {
            return null;
        }

        @Override
        public boolean checkProjectCanBeProfiled(FileObject profiledClassFile) {
            return true;
        }

        @Override
        public void setupProjectSessionSettings(SessionSettings ss) {
        }

        @Override
        public boolean startProfilingSession(FileObject profiledClassFile, boolean isTest) {
            return false;
        }
        
    }
    
}
