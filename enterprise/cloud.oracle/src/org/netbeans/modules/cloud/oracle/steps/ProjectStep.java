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
package org.netbeans.modules.cloud.oracle.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.OpenProjectsFinder;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * The purpose of this step is to select a project to update dependencies.
 */
@NbBundle.Messages({
    "SelectProject=Select Project to Update Dependencies",
    "NoProjects=No Project Found",})
public class ProjectStep extends AbstractStep<Project> {

    private final CompletableFuture<Project[]> projectsFuture;
    private final Map<String, Project> projects;
    private Project selectedProject;

    public ProjectStep() {
        projectsFuture = OpenProjectsFinder.getDefault().findTopLevelProjects();
        this.projects = new HashMap<>();
    }

    @Override
    public void prepare(ProgressHandle h, Values values) {
        try {
            Project[] p = projectsFuture.get();
            for (int i = 0; i < p.length; i++) {
                ProjectInformation pi = ProjectUtils.getInformation(p[i]);
                projects.put(pi.getDisplayName(), p[i]);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public NotifyDescriptor createInput() {
        List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<>(projects.size());
        for (Map.Entry<String, Project> entry : projects.entrySet()) {
            items.add(new NotifyDescriptor.QuickPick.Item(entry.getKey(), entry.getValue().getProjectDirectory().getName()));
        }
        String title = Bundle.SelectProject();
        if (projects.isEmpty()) {
            title = Bundle.NoProjects();
        }
        return new NotifyDescriptor.QuickPick(title, title, items, false);
    }

    @Override
    public boolean onlyOneChoice() {
        return projects.size() == 1;
    }

    @Override
    public void setValue(String selected) {
        selectedProject = projects.get(selected);
    }

    @Override
    public Project getValue() {
        if (projects.size() == 1) {
            return (Project) projects.values().toArray()[0];
        }
        return selectedProject;
    }

}
