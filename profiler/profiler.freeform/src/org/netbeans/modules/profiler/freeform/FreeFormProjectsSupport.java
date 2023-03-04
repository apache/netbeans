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

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "FreeFormProjectTypeProfiler_ErrorParsingBuildFileMsg=Error parsing the build.xml for project {0}",
    "FreeFormProjectTypeProfiler_OkButtonName=OK",
    "FreeFormProjectTypeProfiler_SelectProfilingTaskDialogCaption=Select Task for Profiling",
    "FreeFormProjectTypeProfiler_NoProfilerTaskMsg=The selected target does not appear to be using the nbprofiledirect task.\nDo you still want to use the target for profiling?"
})
public final class FreeFormProjectsSupport {
    public static final String PROFILE_TARGET_ATTRIBUTE = "profile-target"; // NOI18N
    public static final String PROFILE_SINGLE_TARGET_ATTRIBUTE = "profile-file-target"; // NOI18N
    public static final String PROFILE_VERSION_ATTRIBUTE = "version"; // NOI18N
    public static final String VERSION_NUMBER = "0.4"; // NOI18N
    
    
    public static boolean saveProfilerConfig(final Project project, final String profileTarget, final String profileSingleTarget) {
        // not yet modified for profiler => create profiler-build-impl & modify build.xml and project.xml
        final Element profilerFragment = XMLUtil.createDocument("ignore", null, null, null) // NOI18N
                .createElementNS(ProjectUtilities.PROFILER_NAME_SPACE, "data"); // NOI18N

        profilerFragment.setAttribute(PROFILE_VERSION_ATTRIBUTE, VERSION_NUMBER);
        
        // TODO: shouldn't the user select the profiler target here? See revision 208377.

        if (profileTarget != null) {
            profilerFragment.setAttribute(PROFILE_TARGET_ATTRIBUTE, profileTarget);
        }

        if (profileSingleTarget != null) {
            profilerFragment.setAttribute(PROFILE_SINGLE_TARGET_ATTRIBUTE, profileSingleTarget);
        }

        ProjectUtils.getAuxiliaryConfiguration(project).putConfigurationFragment(profilerFragment, false);

        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException e1) {
            Profiler.getDefault().notifyException(Profiler.EXCEPTION, e1);
            ProfilerLogger.log(e1);

            return false;
        }

        return true;
    }
    
}
