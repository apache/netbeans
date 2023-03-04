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
package org.netbeans.modules.gradle.queries;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.DependencyProjectProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = DependencyProjectProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class DependencyProjectProviderImpl extends AbstractProjectChangeAdaptor implements DependencyProjectProvider {

    public DependencyProjectProviderImpl(Project project) {
        super(project);
    }

    @Override
    public Result getDependencyProjects() {
        GradleBaseProject gp = GradleBaseProject.get(project);
        Set<Project> projects = new HashSet<>();
        try {
            for (GradleDependency.ProjectDependency dep : gp.getProjectDependencies()) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                FileObject fo = FileUtil.toFileObject(dep.getPath());
                if (fo != null) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(fo);
                        //project sometimes can't be detected due to #84
                        if (p != null) {
                            projects.add(p);
                        }
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        } catch (InterruptedException ex) {
            return new Result(Collections.emptySet(), false);
        }
        return new Result(projects, false);
    }
}
