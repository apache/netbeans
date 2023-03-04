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

package org.netbeans.modules.gradle.api.execute;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;

/**
 * This object represents the configuration context of a Gradle command execution.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class RunConfig {


    public enum ExecFlag {REPEATABLE}


    final Project project;
    final Set<ExecFlag> execFlags;
    final GradleCommandLine commandLine;

    final String action;
    final String displayName;

    final GradleExecConfiguration execConfig;

    public RunConfig(Project project, String action, String displayName, Set<ExecFlag> execFlags, GradleCommandLine commandLine) {
        this(project, action, displayName, execFlags, commandLine, null);
    }
    
    public RunConfig(Project project, String action, String displayName, Set<ExecFlag> execFlags, GradleCommandLine commandLine, GradleExecConfiguration execConfig) {
        this.project = project;
        this.action = action;
        this.displayName = displayName;
        this.execFlags = Collections.unmodifiableSet(execFlags);
        this.commandLine = commandLine;
        this.execConfig = execConfig;
    }

    public Project getProject() {
        return project;
    }

    public GradleCommandLine getCommandLine() {
        return commandLine;
    }

    public RunConfig withCommandLine(GradleCommandLine cmd) {
        return new RunConfig(project, action, displayName, execFlags, cmd, execConfig);
    }

    public Set<ExecFlag> getExecFlags() {
        return execFlags;
    }

    public String getTaskDisplayName() {
        return displayName;
    }

    public String getActionName() {
        return action;
    }

    /**
     * Selected executable configuration. Should be never {@code null}, as the default
     * configuration always exists. May be {@code null}; in that case the active configuration
     * should be used.
     * @return configuration or {@code null} to use the active one.
     */
    @CheckForNull
    public GradleExecConfiguration getExecConfig() {
        return execConfig;
    }
}
