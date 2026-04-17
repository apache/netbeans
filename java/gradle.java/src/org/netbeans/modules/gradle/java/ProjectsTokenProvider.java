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
package org.netbeans.modules.gradle.java;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.project.ContainedProjectFilter;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Dusan Petrovic
 */
@ProjectServiceProvider(
        service = ReplaceTokenProvider.class, 
        projectType = NbGradleProject.GRADLE_PROJECT_TYPE
)
public class ProjectsTokenProvider implements ReplaceTokenProvider {

    private static final String TASK_WITH_PROJECTS = "taskWithProjects"; //NOI18N
    private static final Set<String> SUPPORTED = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            TASK_WITH_PROJECTS
    )));
    
    @Override
    public Set<String> getSupportedTokens() {
        return SUPPORTED;
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup context) {
        String taskName = getTaskForAction(action);        
        if (taskName == null) {
            return new HashMap<>();
        }
        return getProjectsWithTaskReplacement(taskName, context);
    }

    private String getTaskForAction(String action) {
        return switch (action) {
            case ActionProvider.COMMAND_TEST_PARALLEL -> "test"; //NOI18N
            default -> null;
        };
    }
    
    private Map<String,String> getProjectsWithTaskReplacement(String taskName, Lookup context) {     
        ContainedProjectFilter parameters = context.lookup(ContainedProjectFilter.class);
        List<Project> projects = parameters == null ? null : parameters.getProjectsToProcess();
        if (projects == null || projects.isEmpty()) {
            return Map.of(TASK_WITH_PROJECTS, taskName);
        }
        StringBuilder resultTask = new StringBuilder();
        List<String> projectReplacements = createProjectsReplacement(projects);
        for (String project : projectReplacements) {
            resultTask.append(project)
                    .append(":") //NOI18N
                    .append(taskName)
                    .append(" ");//NOI18N
        }
        return Map.of(TASK_WITH_PROJECTS, resultTask.toString().trim());
    }
    
    private List<String> createProjectsReplacement(List<Project> projects) {
        return projects
                .stream()
                .map(prj -> prj.getProjectDirectory().getName())
                .toList();
    }
}
