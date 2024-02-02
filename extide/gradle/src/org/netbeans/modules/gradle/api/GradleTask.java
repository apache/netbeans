/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

package org.netbeans.modules.gradle.api;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represent a task in a Gradle project.
 * 
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class GradleTask implements Serializable {

    private static final Pattern CAMLE_CASE_SPLITTER = Pattern.compile("(?<!^)(?=[A-Z0-9])");

    final String path;
    final String group;
    final String name;
    final String description;
    final boolean external;

    GradleTask(String path, String group, String name, String description) {
        this.path = path;
        this.group = group;
        this.name = name;
        this.description = description;
        this.external = false;
    }
    
    GradleTask(String path, String name) {
        this.path = path;
        this.name = name;
        this.description = null;
        this.group = null;
        this.external = true;
    }

    public String getPath() {
        return path;
    }

    public String getGroup() {
        return group != null ? group : GradleBaseProject.PRIVATE_TASK_GROUP;
    }

    public String getName() {
        return name;
    }
    
    /**
     * Determines if the task is external, from another project. External tasks do not report
     * all properties, such as group or description - the task must be fetched from its project.
     * Such tasks are never served from the project itself, i.e. {@link GradleBaseProject#getTasks()},
     * but may represent project task's dependency.
     * <p>
     * To get the task's details, lookup its project (identified by {@link #getProjectPath()}, and
     * use {@link GradleBaseProject#getTaskByName(java.lang.String)} to get the external task's declaration
     * info.
     * @return true, if the task is external, from another project.
     * @since 2.29
     */
    public boolean isExternal() {
        return external;
    }
    
    /**
     * Returns path of the task's declaring project. Returns path, except the last component,
     * which is the task name.
     * @return project path.
     * @since 2.29
     */
    public String getProjectPath() {
        int lastColon = path.lastIndexOf(':');
        return lastColon == 0 ? ":" : path.substring(0, lastColon);
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    /**
     * Tasks without groups are considered private in Gradle.
     * 
     * @return true if this task is private.
     */
    public boolean isPrivate() {
        // if a task is visible from an external project, it cannot be private.
        return group == null && !external;
    }

    /**
     * Returns true if the given CamelCase abbreviation matches this task name.
     * @param abbrev the camel case abbreviation
     * @return true if the abbreviation can be matched to this task name.
     */
    public boolean matches(String abbrev) {
        return abbrevMatch(abbrev, name);
    }

    static boolean abbrevMatch(String abbrev, String name) {
        if (abbrev.isEmpty()) {
            return true;
        }
        if (abbrev.length() > name.length()) {
            return false;
        }
        String[] abbrevParts = CAMLE_CASE_SPLITTER.split(abbrev);
        String[] nameParts = CAMLE_CASE_SPLITTER.split(name);
        if (abbrevParts.length > nameParts.length) {
            return false;
        }
        for (int i = 0; i < abbrevParts.length; i++) {
            String part = abbrevParts[i];
            if (!nameParts[i].startsWith(part)) {
                return false;
            }
        }
        return true;
    }
}
