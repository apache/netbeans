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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.modules.profiler.spi.project.ProjectContentsSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Support for configuring profiling roots and instrumentation filter from a project.
 *
 * @author Jiri Sedlacek
 */
public final class ProjectContentsSupport {
    
    private static final ClientUtils.SourceCodeSelection[] EMPTY_SELECTION = new ClientUtils.SourceCodeSelection[0];
    private static ProjectContentsSupport DEFAULT;
    
    private final Collection<? extends ProjectContentsSupportProvider> providers;
    
    
    /**
     * Returns array of profiling roots for the defined context.
     * 
     * @param profiledClassFile profiled file or null for profiling the entire project
     * @param profileSubprojects true if profiling also project's subprojects, false for profiling just the project
     * @return array of profiling roots for the defined context
     */
    public ClientUtils.SourceCodeSelection[] getProfilingRoots(FileObject profiledClassFile,
                                                               boolean profileSubprojects) {
        if (providers == null) {
            return EMPTY_SELECTION;
        } else {
            Set<ClientUtils.SourceCodeSelection> allRoots = new HashSet<ClientUtils.SourceCodeSelection>();
            for (ProjectContentsSupportProvider provider : providers) {
                ClientUtils.SourceCodeSelection[] roots = provider.getProfilingRoots(profiledClassFile, profileSubprojects);
                if (roots != null && roots.length > 0) allRoots.addAll(Arrays.asList(roots));
            }
            return allRoots.toArray(new ClientUtils.SourceCodeSelection[0]);
        }
    }
    
    /**
     * Returns instrumentation filter for the defined context.
     * 
     * @param profileSubprojects true if profiling also project's subprojects, false for profiling just the project
     * @return instrumentation filter for the defined context
     */
    public String getInstrumentationFilter(boolean profileSubprojects) {
        if (providers == null) {
            return ""; // NOI18N
        } else {
            StringBuilder buffer = new StringBuilder();
            for( ProjectContentsSupportProvider provider : providers) {
                String filter = provider.getInstrumentationFilter(profileSubprojects);
                if (filter != null && !filter.isEmpty()) {
                    buffer.append(filter).append(" "); // NOI18N
                }
            }
            return buffer.toString().trim();
        }
    }
    
    /**
     * Resets the ProjectContentsSupport instance after submitting or cancelling the Select Profiling Task dialog.
     */
    public void reset() {
        if (providers != null)
            for (ProjectContentsSupportProvider provider : providers) 
                provider.reset();
    }
    
    
    private ProjectContentsSupport(Collection<? extends ProjectContentsSupportProvider> providers) {
        this.providers = providers;
    }
    
    private static synchronized ProjectContentsSupport defaultImpl() {
        if (DEFAULT == null)
            DEFAULT = new ProjectContentsSupport(null);
        return DEFAULT;
    }
    

    /**
     * Returns ProjectContentsSupport instance for the provided project.
     * 
     * @param project project
     * @return ProjectContentsSupport instance for the provided project
     */
    public static ProjectContentsSupport get(Lookup.Provider project) {
        Collection<? extends ProjectContentsSupportProvider> providers =
                project != null ? project.getLookup().lookupAll(ProjectContentsSupportProvider.class) : null;
        if (providers == null || providers.isEmpty()) return defaultImpl();
        else return new ProjectContentsSupport(providers);
    }
    
}
