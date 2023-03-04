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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

public final class Tasks {

    private final List<AdvancedTask> advancedTasks;
    private final boolean showSimpleTasks;
    @NullAllowed
    private final List<String> simpleTasks;


    public Tasks(List<AdvancedTask> advancedTasks, boolean showSimpleTasks, @NullAllowed List<String> simpleTasks) {
        assert advancedTasks != null;
        this.advancedTasks = new CopyOnWriteArrayList<>(advancedTasks);
        this.showSimpleTasks = showSimpleTasks;
        this.simpleTasks = simpleTasks == null ? null : new CopyOnWriteArrayList<>(simpleTasks);
    }

    public List<AdvancedTask> getAdvancedTasks() {
        return Collections.unmodifiableList(advancedTasks);
    }

    public boolean isShowSimpleTasks() {
        return showSimpleTasks;
    }

    @CheckForNull
    public List<String> getSimpleTasks() {
        if (simpleTasks == null) {
            return null;
        }
        return Collections.unmodifiableList(simpleTasks);
    }

    @Override
    public String toString() {
        return "Tasks{" + "advancedTasks=" + advancedTasks + ", showSimpleTasks=" + showSimpleTasks + ", simpleTasks=" + simpleTasks + '}'; // NOI18N
    }

}
