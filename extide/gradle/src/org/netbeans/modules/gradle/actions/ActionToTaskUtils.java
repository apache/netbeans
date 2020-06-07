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

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class ActionToTaskUtils {

    private static final String MAPPING
            = "defaultActionMapping.properties";

    private static MappingContainer defaultActionMappings;
    private static Map<String, List<ActionMapping>> defaultActionMapping;

    private ActionToTaskUtils() {
    }

    @NonNull
    public static List<? extends GradleActionsProvider> actionProviders(@NonNull Project project) {
        List<GradleActionsProvider> providers = new ArrayList<>();
        providers.addAll(Lookup.getDefault().lookupAll(GradleActionsProvider.class));
        return providers;
    }

    public static boolean isActionEnabled(String action, Project project, Lookup lookup) {
        ActionMapping mapping = getActiveMapping(action, project);
        if (mapping != null) {
            List<? extends GradleActionsProvider> providers = actionProviders(project);
            for (GradleActionsProvider provider : providers) {
                if (provider.isActionEnabled(action, project, lookup)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static ActionMapping getActiveMapping(String action, Project project) {
        ProjectActionMappingProvider mappingProvider = project.getLookup().lookup(ProjectActionMappingProvider.class);
        return mappingProvider != null ? mappingProvider.findMapping(action) : null;
    }

}
