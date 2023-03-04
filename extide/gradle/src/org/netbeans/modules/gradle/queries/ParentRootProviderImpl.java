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

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.ParentProjectProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.RootProjectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = {ParentProjectProvider.class, RootProjectProvider.class}, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class ParentRootProviderImpl implements ParentProjectProvider, RootProjectProvider {

    final Project project;

    public ParentRootProviderImpl(Project project) {
        this.project = project;
    }


    @Override
    public Project getPartentProject() {
        Project ret = null;
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if ((gbp != null) && !gbp.isRoot()) {
            int lastcol = gbp.getPath().lastIndexOf(':');
            if (lastcol == -1) {
                return null;
            }
            String parentPath = gbp.getPath().substring(0, lastcol);
            Project root = getRootProject();
            if (parentPath.isEmpty()) {
                return root;
            }
            GradleBaseProject rbp = GradleBaseProject.get(root);
            File parentDir = rbp.getSubProjects().get(parentPath);
            if (parentDir != null) {
                FileObject fo = FileUtil.toFileObject(parentDir);
                try {
                    ret = ProjectManager.getDefault().findProject(fo);
                } catch (IllegalArgumentException | IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        return ret;
    }

    @Override
    public Project getRootProject() {
        Project ret = project;
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            if (gbp.isRoot()) {
                ret = project;
            } else {
                FileObject fo = FileUtil.toFileObject(gbp.getRootDir());
                try {
                    ret = ProjectManager.getDefault().findProject(fo);
                } catch (IllegalArgumentException | IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        return ret;
    }

}
