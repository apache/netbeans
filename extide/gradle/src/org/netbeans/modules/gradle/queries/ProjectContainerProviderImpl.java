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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = {ProjectContainerProvider.class, SubprojectProvider.class}, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class ProjectContainerProviderImpl extends AbstractProjectChangeAdaptor implements SubprojectProvider, ProjectContainerProvider {

    private static final Logger LOG = Logger.getLogger(ProjectContainerProviderImpl.class.getName());
    public ProjectContainerProviderImpl(Project project) {
        super(project);
    }

    @Override
    public Result getContainedProjects() {
        return new Result(getSubprojects(), false);
    }

    @Override
    public Set<? extends Project> getSubprojects() {
        Set<Project> ret = null;

        Project root = ProjectUtils.rootOf(project);
        GradleBaseProject rgp = GradleBaseProject.get(root);
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if ((rgp != null) && (gbp != null)) {
            ret = new HashSet<>();
            String ourPath = gbp.isRoot() ? ":" : gbp.getPath() + ':';
            for (Map.Entry<String, File> sub : rgp.getSubProjects().entrySet()) {
                if ((sub.getKey().length() > ourPath.length()) && sub.getKey().startsWith(ourPath)) {
                    String subPath = sub.getKey().substring(ourPath.length());
                    if (subPath.indexOf(':') < 0) {
                        FileObject fo = FileUtil.toFileObject(sub.getValue());
                        if (fo != null) {
                            try {
                                Project p = ProjectManager.getDefault().findProject(fo);
                                if (p != null) {
                                    ret.add(p);
                                } else {
                                    LOG.log(Level.WARNING, "It seems {0} was not identified as a (sub-)project of {1}", new Object[]{fo.getPath(), project.toString()});
                                }
                            } catch (IllegalArgumentException | IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    }
                }
            }
        }
        return ret != null ? Collections.unmodifiableSet(ret) : Collections.emptySet();
    }

}
