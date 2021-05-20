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
package org.netbeans.modules.gradle.spi.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public final class ProjectConfigurationSupport {
    
    private static final ThreadLocal<Map<NbGradleProject, GradleExecConfiguration>> selectedConfigs = new ThreadLocal<Map<NbGradleProject, GradleExecConfiguration>>();
    
    public static <T, E extends Exception> T executeWithConfiguration(NbGradleProject gp, GradleExecConfiguration c, Supplier<T> task) {
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = gp.projectLookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            // the project does not support configurations.
            return task.get();
        }
        Map<NbGradleProject, GradleExecConfiguration> m = selectedConfigs.get();
        try {
            Map<NbGradleProject, GradleExecConfiguration> n = new HashMap<>(m);
            m.put(gp, c);
            selectedConfigs.set(n);
            return task.get();
        } finally {
            selectedConfigs.set(m);
        }
    }
    
    private static <E extends Exception> void throwActualException(Exception exception) throws E {
        throw (E) exception;
    }

    public static void executeWithConfiguration(NbGradleProject gp, GradleExecConfiguration c, Runnable task) {
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = gp.projectLookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            // the project does not support configurations.
            task.run();
            return;
        }
        Map<NbGradleProject, GradleExecConfiguration> m = selectedConfigs.get();
        try {
            Map<NbGradleProject, GradleExecConfiguration> n = new HashMap<>(m);
            m.put(gp, c);
            selectedConfigs.set(n);
            task.run();
        } finally {
            selectedConfigs.set(m);
        }
    }
    
    public static GradleExecConfiguration getExplicitConfiguration(NbGradleProject p, Lookup context) {
        Map<NbGradleProject, GradleExecConfiguration> m = selectedConfigs.get();
        if (m == null) {
            return null;
        }
        return m.get(p);
    }
    
    public static GradleExecConfiguration getEffectiveConfiguration(NbGradleProject p, Lookup context) {
        GradleExecConfiguration c = getExplicitConfiguration(p, context);
        if (c != null) {
            return null;
        }
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.projectLookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            return null;
        }
        return pcp.getActiveConfiguration();
    }
}
