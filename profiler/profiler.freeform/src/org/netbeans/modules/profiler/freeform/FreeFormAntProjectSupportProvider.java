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
package org.netbeans.modules.profiler.freeform;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider.class, 
                        projectTypes={@ProjectType(id="org-netbeans-modules-ant-freeform", position=1220)}) // NOI18N
public final class FreeFormAntProjectSupportProvider extends AntProjectSupportProvider.Abstract {    
    
    public FreeFormAntProjectSupportProvider(Project project) {
        super(project);
    }
    
    @Override
    public FileObject getProjectBuildScript() {
        return Util.getProjectBuildScript(getProject());
    }
    
    @Override
    public void configurePropertiesForProfiling(final Map<String, String> props, final FileObject profiledClassFile) {
        if (profiledClassFile != null) { // In case the class to profile is explicitely selected (profile-single)
            // 1. specify profiled class name

            //FIXME 
            JavaProfilerSource src = JavaProfilerSource.createFrom(profiledClassFile);
            if (src != null) {
                final String profiledClass = src.getTopLevelClass().getQualifiedName();
                props.put("profile.class", profiledClass); //NOI18N

                // 2. include it in javac.includes so that the compile-single picks it up
                final String clazz = FileUtil.getRelativePath(ProjectUtilities.getRootOf(ProjectUtilities.getSourceRoots(getProject()),
                        profiledClassFile), profiledClassFile);
                props.put("javac.includes", clazz); //NOI18N
            }
        }
    }
    
}
