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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.api.GradleProjects;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook;
import java.io.PrintWriter;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = AfterBuildActionHook.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class ReloadProjectDependenciesDecorator implements AfterBuildActionHook {

    final Project project;

    public ReloadProjectDependenciesDecorator(Project project) {
        this.project = project;
    }

    @Override
    public void afterAction(String action, Lookup context, int result, PrintWriter out) {
        Map<String, Project> dependencies = GradleProjects.openedProjectDependencies(project);
        for (Project dep : dependencies.values()) {
            NbGradleProjectImpl impl = dep.getLookup().lookup(NbGradleProjectImpl.class);
            if ((impl != null) && impl.getAimedQuality().betterThan(impl.getGradleProject().getQuality())) {
                impl.forceReloadProject(null, false, impl.getAimedQuality());
            }
        }
    }

}
