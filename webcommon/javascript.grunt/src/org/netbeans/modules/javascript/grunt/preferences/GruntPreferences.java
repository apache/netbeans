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
package org.netbeans.modules.javascript.grunt.preferences;

import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

public final class GruntPreferences {

    private static final String COMMAND_PREFIX = "action."; // NOI18N
    private static final String TASKS = "tasks"; // NOI18N

    private final Project project;

    // @GuardedBy("this")
    private Preferences sharedPreferences;


    public GruntPreferences(Project project) {
        assert project != null;
        this.project = project;
    }

    @CheckForNull
    public String getTask(String commandId) {
        return getPreferences().get(COMMAND_PREFIX + commandId, null);
    }

    public void setTask(String commandId, @NullAllowed String value) {
        if (value != null) {
            getPreferences().put(COMMAND_PREFIX + commandId, value);
        } else {
            getPreferences().remove(COMMAND_PREFIX + commandId);
        }
    }

    @CheckForNull
    public String getTasks() {
        return getPreferences().get(TASKS, null);
    }

    public void setTasks(@NullAllowed String tasks) {
        if (tasks == null) {
            getPreferences().remove(TASKS);
        } else {
            getPreferences().put(TASKS, tasks);
        }
    }

    public Project getProject() {
        return project;
    }

    private synchronized Preferences getPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = ProjectUtils.getPreferences(project, GruntPreferences.class, true);
        }
        return sharedPreferences;
    }

}
