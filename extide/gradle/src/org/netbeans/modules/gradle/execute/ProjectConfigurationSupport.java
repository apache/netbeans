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
package org.netbeans.modules.gradle.execute;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.util.Lookup;

/**
 * Allows temporary configuration activation for length of some {@link Runnable} or {@link Supplier} invocation. During that time,
 * {@link ProjectConfigurationProvider#getActiveConfiguration()} returns the temporary config as the active one.
 * <p/>
 * Used for execution when an explicit configuration can be requested by the caller, but older code (before configurations were introduced) may
 * call methods that rely on the active configuration. So during the hooks or invocations, the requested config will be in effect.
 * 
 * @author sdedic
 */
public final class ProjectConfigurationSupport {
    
    /**
     * For each thread that executes {@link #executeWithConfiguration(org.netbeans.api.project.Project, org.netbeans.modules.gradle.api.execute.GradleExecConfiguration, java.lang.Runnable)}
     * contains a Map of overriden projects. The map is copied+modified and restored in nested executeWith() invocations.
     */
    private static final ThreadLocal<Map<Project, GradleExecConfiguration>> selectedConfigs = new ThreadLocal<>();
    
    /**
     * Executes the passed {@link Supplier} with temporarily active configuration. During Supplier invocation, the 
     * {@link ProjectConfigurationProvider#getActiveConfiguration()} will return `c'.
     * 
     * @param <T> type of return value
     * @param project project to apply on
     * @param c the selected configuration. Use {@code null} to reset to the globally active one.
     * @param task task to execute
     * @return value returned from the Supplier.
     */
    public static <T> T executeWithConfiguration(Project project, GradleExecConfiguration c, Supplier<T> task) {
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            // the project does not support configurations.
            return task.get();
        }
        Map<Project, GradleExecConfiguration> m = selectedConfigs.get();
        try {
            Map<Project, GradleExecConfiguration> n = m == null ? new HashMap<>() : new HashMap<>(m);
            n.put(project, c);
            selectedConfigs.set(n);
            return task.get();
        } finally {
            selectedConfigs.set(m);
        }
    }
    
    /**
     * Executes the passed {@link Runnable} with temporarily active configuration. During invocation, the 
     * {@link ProjectConfigurationProvider#getActiveConfiguration()} will return `c'.
     * 
     * @param project project to apply on
     * @param c the selected configuration. Use {@code null} to reset to the globally active one.
     * @param task task to execute
     */
    public static void executeWithConfiguration(Project project, GradleExecConfiguration c, Runnable task) {
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            // the project does not support configurations.
            task.run();
            return;
        }
        Map<Project, GradleExecConfiguration> m = selectedConfigs.get();
        try {
            Map<Project, GradleExecConfiguration> n = m == null ? new HashMap<>() : new HashMap<>(m);
            n.put(project, c);
            selectedConfigs.set(n);
            task.run();
        } finally {
            selectedConfigs.set(m);
        }
    }

    /**
     * Finds an explicit configuration requested. Can be either a configuration passed in the context, or one set by {@link #executeWithConfiguration(org.netbeans.api.project.Project, org.netbeans.modules.gradle.api.execute.GradleExecConfiguration, java.lang.Runnable)}.
     * If no explicit configuration is in effect, returns {@code null} (= active configuration is to be used).
     * @param p the project
     * @param context context Lookup, i.e. action context one
     * @return explicitly requested configuration or {@code null}
     */
    public static @CheckForNull GradleExecConfiguration getExplicitConfiguration(@NonNull Project p, @NullAllowed Lookup context) {
        Map<Project, GradleExecConfiguration> m = selectedConfigs.get();
        if (m == null) {
            return context != null ? context.lookup(GradleExecConfiguration.class) : null;
        }
        return m.get(p);
    }
    
    /**
     * Returns the configuration in effect. Either the {@link #getExplicitConfiguration(org.netbeans.api.project.Project, org.openide.util.Lookup)}, if defined, or
     * the project's active configuration.
     * 
     * @param p the project
     * @param context context, i.e. action Lookup
     * @return the effective configuration
     */
    public static GradleExecConfiguration getEffectiveConfiguration(Project p, Lookup context) {
        GradleExecConfiguration c = getExplicitConfiguration(p, context);
        if (c != null) {
            return c;
        }
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            return null;
        }
        return pcp.getActiveConfiguration();
    }
}
