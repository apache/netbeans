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

import java.util.Map;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Provider of support for profiling Ant projects.
 *
 * @author Jiri Sedlacek
 */
public abstract class AntProjectSupportProvider {
    /**
     * Returns build script of a project.
     * 
     * @return build script of a project
     */
    public abstract FileObject getProjectBuildScript();
    
    /**
     * Returns build script according to provided file name.
     * 
     * @param buildFileName file name of the build script
     * @return build script according to provided file name
     */
    public abstract FileObject getProjectBuildScript(String buildFileName);
    
    /**
     * Configures profiling properties passed to the Ant environment.
     * 
     * @param props properties
     * @param profiledClassFile profiled file or null for profiling the entire project
     */
    public abstract void configurePropertiesForProfiling(Map<String, String> props, FileObject profiledClassFile);
    
    
    static class Basic extends AntProjectSupportProvider {
        
        @Override
        public FileObject getProjectBuildScript() {
            return null;
        }

        @Override
        public FileObject getProjectBuildScript(String buildFileName) {
            return null;
        }
        
        @Override
        public void configurePropertiesForProfiling(Map<String, String> props, FileObject profiledClassFile) {
        }
        
    }
    
    
    public abstract static class Abstract extends AntProjectSupportProvider {
        
        private final Project project;
    
        @Override
        public FileObject getProjectBuildScript() {
            FileObject buildFile = null;

            Properties props = org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities.getProjectProperties(getProject());
            String buildFileName = props != null ? props.getProperty("buildfile") : null; // NOI18N
            if (buildFileName != null) {
                buildFile = getProjectBuildScript(buildFileName);
            }
            if (buildFile == null) {
                buildFile = getProjectBuildScript("build.xml"); //NOI18N
            }
            return buildFile;
        }

        @Override
        public FileObject getProjectBuildScript(String buildFileName) {
            return getProject().getProjectDirectory().getFileObject(buildFileName);
        }
        
        @Override
        public void configurePropertiesForProfiling(Map<String, String> props, FileObject profiledClassFile) {
        }

        protected final Project getProject() {
            return project;
        }


        protected Abstract(Project project) {
            this.project = project;
        }
        
    }
    
}
