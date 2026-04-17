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
package org.netbeans.api.project;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides list of projects the project action should apply to
 * 
 * <p>
 * An action that processes multiple projects might use <code>ContainedProjectFilter</code>
 * to operate only on a specific subset of projects.
 * The use of <code>ContainedProjectFilter</code> is optional and determined
 * by the requirements of individual actions.
 * Actions employing this class must document their specific filtering logic
 * and behavior.
 * </p>
 * 
 * @author Dusan Petrovic
 * 
 * @since 1.99
 */
public final class ContainedProjectFilter {
    
    private final List<Project> projectsToProcess;

    private ContainedProjectFilter(List<Project> projectsToProcess) {
        this.projectsToProcess = projectsToProcess;
    }

    /**
     * Static factory method to create an instance of ContainedProjectFilter.
     *
     * @param projectsToProcess the list of projects to include in the filter
     * @return an Optional containing ContainedProjectFilter, or Optional.empty() if the list is null or empty
     */
    public static Optional<ContainedProjectFilter> of(List<Project> projectsToProcess) {
         if (projectsToProcess == null || projectsToProcess.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ContainedProjectFilter(projectsToProcess));
    }
    
    public List<Project> getProjectsToProcess() {
        return Collections.unmodifiableList(projectsToProcess);
    }
}
