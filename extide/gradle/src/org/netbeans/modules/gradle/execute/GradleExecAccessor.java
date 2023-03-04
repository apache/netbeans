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

import java.util.Collections;
import java.util.Map;
import org.gradle.tooling.ConfigurableLauncher;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Accessor to manipulate {@link GradleExecConfiguration} in ways not exposed in the API.
 * @author sdedic
 */
public abstract class GradleExecAccessor {
    
    private static final GradleExecConfiguration hookup = null;
    
    private static GradleExecAccessor INSTANCE;

    public static GradleExecAccessor instance() {
        return INSTANCE;
    }
    
    /**
     * Creates a new configuration instance.
     * @param id unique ID
     * @param dispName human-readable name, can be {@code null}.
     * @param projectProps project properties, can be {@code null} if none defined.
     * @param cmdline additional arguments, can be {@code null} if none defined.
     * @return new instance
     */
    public abstract GradleExecConfiguration create(
            String id, String dispName, Map<String, String> projectProps, String cmdline);
    
    /**
     * Configures launcher with the gradle home.
     * @param cfgL the launcher
     * @return gradle home setting
     */
    public abstract ConfigurableLauncher configureGradleHome(ConfigurableLauncher cfgL);
        
    /**
     * Updates an existing instance. Cannot change the {@link GradleExecConfiguration#getId()}.
     * @param conf the configuration to update
     * @param dispName new display name, or {@code null} to reset
     * @param projectProps new project properties, use {@code null} for none.
     * @param cmdline additional arguments, use {@code null} to undefine.
     * @return updated configuration instance.
     */
    public abstract GradleExecConfiguration update(
            GradleExecConfiguration conf,
            String dispName, Map<String, String> projectProps, String cmdline);

    /**
     * Makes a copy of an existing configuration.
     * @param orig original
     * @return copy
     */
    public GradleExecConfiguration copy(GradleExecConfiguration orig) {
        return create(orig.getId(), orig.getName(), orig.getProjectProperties(), orig.getCommandLineArgs());
    }

    /**
     * Makes a copy of an existing configuration.
     * @param orig original
     * @return copy
     */
    public GradleExecConfiguration copy(GradleExecConfiguration orig, String newId) {
        return create(newId, orig.getName(), orig.getProjectProperties(), orig.getCommandLineArgs());
    }
    
    /**
     * Creates the default configuration instance.
     * @return default instance.
     */
    @NbBundle.Messages({
        "CONFIG_DefaultConfigName=<default>"
    })
    public static GradleExecConfiguration createDefault() {
        return instance().create(GradleExecConfiguration.DEFAULT, Bundle.CONFIG_DefaultConfigName(), 
                Collections.emptyMap(), null);
    }
    
    protected GradleExecAccessor() {
    }
    
    public static void setInstance(GradleExecAccessor inst) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = inst;
    }
    
    static {
        try {
            Class.forName(GradleExecConfiguration.class.getName());
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
