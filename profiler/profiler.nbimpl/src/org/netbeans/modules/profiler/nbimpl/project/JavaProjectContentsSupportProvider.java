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

import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.modules.profiler.spi.project.ProjectContentsSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
@ProjectServiceProvider(projectTypes={
                            @ProjectType(id="org-netbeans-modules-java-j2seproject"),
                            @ProjectType(id="org-netbeans-modules-ant-freeform", position=1203),
                            @ProjectType(id="org-netbeans-modules-apisupport-project"),
                            @ProjectType(id="org-netbeans-modules-apisupport-project-suite"),
                            @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),
                            @ProjectType(id="org-netbeans-modules-web-project"),
                            @ProjectType(id="org-netbeans-modules-maven")},
                        service=ProjectContentsSupportProvider.class)
public class JavaProjectContentsSupportProvider extends ProjectContentsSupportProvider {
    
    private final String[][] packages = new String[2][];

    private final Project project;
    
    
    public JavaProjectContentsSupportProvider(Project project) {
        this.project = project;
    }

    @Override
    public String getInstrumentationFilter(boolean profileSubprojects) {
        return ProjectUtilities.computeProjectOnlyInstrumentationFilter(project, profileSubprojects, packages);
    }

    @Override
    public ClientUtils.SourceCodeSelection[] getProfilingRoots(FileObject profiledClassFile, boolean profileSubprojects) {
        if (profiledClassFile == null) {
            return ProjectUtilities.getProjectDefaultRoots(project, packages);
        } else {
            JavaProfilerSource src = JavaProfilerSource.createFrom(profiledClassFile);
            if (src != null) {
                Set<SourceClassInfo> clzs = src.getClasses();
                SourceCodeSelection[] rslt = new SourceCodeSelection[clzs.size()];
                
                int index = 0;
                for(SourceClassInfo ci : clzs) {
                    rslt[index++] = new SourceCodeSelection(ci.getQualifiedName(), "<all>", ""); // NOI18N
                }
                return rslt;
            }
            return null;
        }
    }
    
    @Override
    public void reset() {
        packages[0] = new String[0];
        packages[1] = new String[0];
    }

}
