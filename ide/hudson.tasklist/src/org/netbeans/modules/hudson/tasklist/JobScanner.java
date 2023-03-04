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

package org.netbeans.modules.hudson.tasklist;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.spi.tasklist.Task;

/**
 * One method of finding tasks relating to a Hudson job.
 */
public interface JobScanner {

    /**
     * Search for tasks.
     * This method may block, but ought to check {@link Thread#interrupted} if possible.
     * @param p a project being scanned
     * @param job a job to consider
     * @param buildNumber the build number to scan
     * @param callback a callback to use to report the tasks as they come in
     * @throws IOException in case of problems retrieving job metadata etc.
     */
    void findTasks(Project p, HudsonJob job, int buildNumber, TaskAdder callback) throws IOException;

    /**
     * @see #findTasks
     */
    interface TaskAdder {

        /**
         * Call to add a task.
         * @param task a new task
         */
        void add(Task task);

    }

}
