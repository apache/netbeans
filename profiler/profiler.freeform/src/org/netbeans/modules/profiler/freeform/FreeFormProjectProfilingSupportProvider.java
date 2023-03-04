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

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * A class providing basic support for profiling free-form projects.
 *
 * @author Ian Formanek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider.class, 
                        projectTypes={@ProjectType(id="org-netbeans-modules-ant-freeform", position=1210)}) // NOI18N
public final class FreeFormProjectProfilingSupportProvider extends JavaProjectProfilingSupportProvider {
    private static final String NBJDK_PROPERTIES = "nbproject/nbjdk.properties"; // NOI18N
    private static final String NBJDK_ACTIVE = "nbjdk.active"; // NOI18N

    @Override
    public boolean checkProjectCanBeProfiled(final FileObject profiledClassFile) {
        Project project = getProject();
        Element e = ProjectUtils.getAuxiliaryConfiguration(project).getConfigurationFragment("data", // NOI18N
                ProjectUtilities.PROFILER_NAME_SPACE, false);

        if (e != null) {
            final String profileTarget = e.getAttribute(FreeFormProjectsSupport.PROFILE_TARGET_ATTRIBUTE);
            final String profileSingleTarget = e.getAttribute(FreeFormProjectsSupport.PROFILE_SINGLE_TARGET_ATTRIBUTE);
            
            if (profileTarget == null && profileSingleTarget == null) return false;
        } else {
            if (!FreeFormProjectsSupport.saveProfilerConfig(project, null, null)) return false;
        }

        return super.checkProjectCanBeProfiled(profiledClassFile);
    }

    @Override
    public JavaPlatform resolveProjectJavaPlatform() {
        ProjectAccessor acc = getProject().getLookup().lookup(ProjectAccessor.class);
        EditableProperties ep = acc.getHelper().getProperties(NBJDK_PROPERTIES);
        String platformName = ep.getProperty(NBJDK_ACTIVE);

        return getPlatformByName(platformName);
    }

    @Override
    public boolean isProfilingSupported() {
        boolean supported = super.isProfilingSupported();
        if (!supported) return false;
        
        final String WEB_MODULE_PROVIDER_TYPE = "org.netbeans.modules.web.freeform.WebModules";
        
        for(Object x : getProject().getLookup().lookupAll(Object.class)) {
            if (WEB_MODULE_PROVIDER_TYPE.equals(x.getClass().getName())) {
                return false; // web freeforms are not supported
            }
        }
        return true;
    }
    
    public FreeFormProjectProfilingSupportProvider(Project project) {
        super(project);
    }
}
