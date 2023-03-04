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
package org.netbeans.modules.profiler.j2ee;

import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.modules.profiler.spi.project.ProjectContentsSupportProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(projectType={"org-netbeans-modules-web-project", "org-netbeans-modules-maven/war"}, service=ProjectContentsSupportProvider.class)
public final class WebProjectContentsSupportProvider extends ProjectContentsSupportProvider {
    
    private final String[][] packages = new String[2][];
    
    private final Project project;
    

    @Override
    public ClientUtils.SourceCodeSelection[] getProfilingRoots(FileObject profiledClassFile, boolean profileSubprojects) {
        if (profiledClassFile == null) {
            // Profile Project, extract root methods from the project
            return WebProjectUtils.getJSPRootMethods(project, profileSubprojects);
        } else {
            // Profile Single, provide correct root methods
            if (WebProjectUtils.isJSP(profiledClassFile)) {
                // TODO: create list of jsp-specific methods (execute & all used Beans)
                return ProjectUtilities.getProjectDefaultRoots(project, packages);
            }
        }
        return null;
    }

    @Override
    public String getInstrumentationFilter(boolean profileSubprojects) {
        ClientUtils.SourceCodeSelection[] jspMethods = WebProjectUtils.getJSPRootMethods(project, profileSubprojects);

        StringBuilder buffer = new StringBuilder(jspMethods.length * 30);

        if (jspMethods != null) {
            for (int i = 0; i < jspMethods.length; i++) {
                buffer.append(jspMethods[i].getClassName()).append(' '); // NOI18N
            }
        }
        return buffer.toString().trim();
    }
    
    @Override
    public void reset() {
        packages[0] = new String[0];
        packages[1] = new String[0];
    }
    
    
    public WebProjectContentsSupportProvider(Project project) {
        this.project = project;
    }
    
}