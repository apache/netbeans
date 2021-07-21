/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.openide.util.Lookup;
import org.netbeans.modules.gradle.execute.ConfigurableActionProvider;
import org.netbeans.modules.gradle.execute.ProjectConfigurationSupport;
import org.netbeans.spi.project.ProjectConfigurationProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class ActionToTaskUtils {
    private ActionToTaskUtils() {
    }

    @NonNull
    public static List<? extends GradleActionsProvider> actionProviders(@NonNull Project project) {
        List<GradleActionsProvider> providers = new ArrayList<>();
        providers.addAll(project.getLookup().lookupAll(GradleActionsProvider.class));
        providers.addAll(Lookup.getDefault().lookupAll(GradleActionsProvider.class));
        return providers;
    }

    public static boolean isActionEnabled(String action, Project project, Lookup lookup) {
        ActionMapping mapping = getActiveMapping(action, project, lookup);
        if (!ActionMapping.isDisabled(mapping)) {
            List<? extends GradleActionsProvider> providers = actionProviders(project);
            for (GradleActionsProvider provider : providers) {
                if (provider.isActionEnabled(action, project, lookup)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static GradleExecConfiguration findProjectConfiguration(Project p) {
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        return pcp == null ? null : pcp.getActiveConfiguration();
    }


    public static ActionMapping getActiveMapping(String action, Project project, Lookup context) {
        GradleExecConfiguration c = ProjectConfigurationSupport.getEffectiveConfiguration(project, context);
        ConfigurableActionProvider contextProvider = project.getLookup().lookup(ConfigurableActionProvider.class);
        
        if (c == null) {
            ProjectConfigurationProvider<GradleExecConfiguration> cprov = project.getLookup().lookup(ProjectConfigurationProvider.class);
            if (cprov != null) {
                c = cprov.getActiveConfiguration();
            }
        }
        
        if (c != null) {
            ProjectActionMappingProvider mp = contextProvider.findActionProvider(c == null ? null : c.getId());
            if (mp != null) {
                ActionMapping m = mp.findMapping(action);
                if (m != null) {
                    return m;
                }
            }
        }

        ProjectActionMappingProvider mappingProvider = null;
        
        for (ProjectActionMappingProvider prov : project.getLookup().lookupAll(ProjectActionMappingProvider.class)) {
            if (!(prov instanceof ConfigurableActionProvider)) {
                mappingProvider = prov;
                break;
            }
        }
        // in case the Mapping Provider asks for the configuration, it should get some:
        if (mappingProvider == null) {
            return null;
        }
        ProjectActionMappingProvider mp = mappingProvider;
        ActionMapping am = ProjectConfigurationSupport.executeWithConfiguration(project, c, () -> mp.findMapping(action));
        if (ActionMapping.isDisabled(am)) {
            return null;
        } else {
            return am;
        }
    }
}
