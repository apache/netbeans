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
package org.netbeans.modules.profiler.nbmodule;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider.class, 
                        projectTypes={
                            @ProjectType(id="org-netbeans-modules-apisupport-project"), //NOI18N
                            @ProjectType(id="org-netbeans-modules-apisupport-project-suite") //NOI18N
                        }
)
public final class NbModuleAntProjectSupportProvider extends AntProjectSupportProvider.Abstract {
    
    private static final String TEST_TYPE_UNIT = "unit"; // NOI18N
    private static final String TEST_TYPE_QA_FUNCTIONAL = "qa-functional"; // NOI18N
    
    public NbModuleAntProjectSupportProvider(Project project) {
        super(project);
    }
    
    @Override
    public FileObject getProjectBuildScript() {
        return getProject().getProjectDirectory().getFileObject("build.xml"); //NOI18N
    }
    
    @Override
    public void configurePropertiesForProfiling(final Map<String, String> props, final FileObject profiledClassFile) {
        // FIXME
        JavaProfilerSource src = JavaProfilerSource.createFrom(profiledClassFile);
        if (src != null) {
            final String profiledClass = src.getTopLevelClass().getQualifiedName();
            props.put("profile.class", profiledClass); //NOI18N
            // Set for all cases (incl. Profile Project, Profile File) but should only
            // be taken into account when profiling single test
            props.put("test.type", getTestType(profiledClassFile)); //NOI18N
        }
    }
    
    private static String getTestType(FileObject testFile) {
        String testPath = testFile.getPath();
        if (testPath.contains(TEST_TYPE_QA_FUNCTIONAL)) return TEST_TYPE_QA_FUNCTIONAL;
        else return TEST_TYPE_UNIT;
    }
    
}
