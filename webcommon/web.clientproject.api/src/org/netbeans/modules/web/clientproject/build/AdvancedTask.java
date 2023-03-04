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
package org.netbeans.modules.web.clientproject.build;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;

public final class AdvancedTask {

    private volatile String name;
    private volatile String options;
    private volatile String tasks;
    private volatile String parameters;
    private volatile boolean shared = true;


    @CheckForNull
    public String getName() {
        return name;
    }

    public AdvancedTask setName(String name) {
        this.name = name;
        return this;
    }

    @CheckForNull
    public String getOptions() {
        return options;
    }

    public AdvancedTask setOptions(String options) {
        this.options = options;
        return this;
    }

    @CheckForNull
    public String getTasks() {
        return tasks;
    }

    public AdvancedTask setTasks(String tasks) {
        this.tasks = tasks;
        return this;
    }

    @CheckForNull
    public String getParameters() {
        return parameters;
    }

    public AdvancedTask setParameters(String parameters) {
        this.parameters = parameters;
        return this;
    }

    public boolean isShared() {
        return shared;
    }

    public AdvancedTask setShared(boolean shared) {
        this.shared = shared;
        return this;
    }

    public String getFullCommand() {
        StringBuilder sb = new StringBuilder();
        if (StringUtilities.hasText(options)) {
            sb.append(options);
        }
        if (StringUtilities.hasText(tasks)) {
            if (sb.length() > 0) {
                sb.append(" "); // NOI18N
            }
            sb.append(tasks);
        }
        if (StringUtilities.hasText(parameters)) {
            if (sb.length() > 0) {
                sb.append(" "); // NOI18N
            }
            sb.append(parameters);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "AdvancedTask{" + "name=" + name + ", options=" + options + ", tasks=" + tasks + ", parameters=" + parameters + ", shared=" + shared + '}'; // NOI18N
    }

}
