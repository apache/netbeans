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

package org.netbeans.modules.gradle.queries;

import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = {SubprojectProvider.class}, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class SubprojectProviderImpl implements SubprojectProvider {

    private final Project project;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final PropertyChangeListener propChange;

    public SubprojectProviderImpl(Project project) {
        this.project = project;
        propChange = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    cs.fireChange();
                }
            }
        };
    }

    @Override
    public Set<? extends Project> getSubprojects() {
        GradleBaseProject gp = GradleBaseProject.get(project);
        Set<Project> projects = new HashSet<>();
        Collection<File> paths;
        if (gp.isRoot()) {
            paths = gp.getSubProjects().values();
        } else {
            paths = new LinkedList<>();
            for (GradleDependency.ProjectDependency dep : gp.getProjectDependencies()) {
                paths.add(dep.getPath());
            }
        }
        try {
            for (File projectDir : paths) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                FileObject fo = FileUtil.toFileObject(projectDir);
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
            return Collections.<Project>emptySet();
        }
        return Collections.unmodifiableSet(projects);
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        if (!cs.hasListeners()) {
            project.getLookup().lookup(NbGradleProject.class).addPropertyChangeListener(propChange);
        }
        cs.addChangeListener(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        cs.removeChangeListener(cl);
        if (!cs.hasListeners()) {
            project.getLookup().lookup(NbGradleProject.class).removePropertyChangeListener(propChange);
        }
    }

}
