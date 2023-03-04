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

package org.netbeans.modules.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * finds subprojects (projects this one depends on) that are locally available
 * and can be build as one unit. Uses maven multiproject infrastructure. (maven.multiproject.includes)
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=SubprojectProvider.class, projectType="org-netbeans-modules-maven")
public class SubprojectProviderImpl implements SubprojectProvider {

    private final Project project;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final ChangeListener listener2;
    private final PropertyChangeListener propertyChange;

    public SubprojectProviderImpl(Project proj) {
        project = proj;
        propertyChange = new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    cs.fireChange();
                }
            }
        };
        listener2 = new ChangeListener() {
            @Override public void stateChanged(ChangeEvent event) {
                cs.fireChange();
            }
        };
        MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                WeakListeners.change(listener2,
                MavenFileOwnerQueryImpl.getInstance()));
    }


    @Override public Set<? extends Project> getSubprojects() {
        Set<Project> projects = new HashSet<Project>();
        File basedir = FileUtil.toFile(project.getProjectDirectory());
        try {
            addProjectModules(basedir, projects, project.getLookup().lookup(NbMavenProject.class).getMavenProject().getModules());
        } catch (InterruptedException x) {
            // can be interrupted in the open project dialog..
            return Collections.emptySet();
        }
        addKnownOwners(projects);
        projects.remove(project);
        return projects;
    }

    private void addKnownOwners(Set<Project> resultset) {
        List<Artifact> compileArtifacts = project.getLookup().lookup(NbMavenProject.class).getMavenProject().getCompileArtifacts();
        for (Artifact ar : compileArtifacts) {
            File f = ar.getFile();
            if (f != null) {
                Project p = MavenFileOwnerQueryImpl.getInstance().getOwner(Utilities.toURI(f));
                if (p != null) {
                    resultset.add(p);
                }
            }
        }
    }

    private boolean isProcessed(Set<Project> resultset, FileObject projectDir) {

        for (Project p : resultset) {
            if (p.getProjectDirectory().equals(projectDir)) {
                return true;
            }
        }

        return false;
    }

    private void addProjectModules(File basedir, Set<Project> resultset, List<String> modules) throws InterruptedException {
        if (modules == null) {
            return;
        }
        for (String path : modules) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (path.trim().length() == 0) {
                //#175331
                continue;
            }
            File sub = new File(basedir, path);
            File projectFile = FileUtil.normalizeFile(sub);
            if (!projectFile.equals(basedir) //#175331
                 && projectFile.exists()) {
                FileObject projectDir = FileUtil.toFileObject(projectFile);
                if (projectDir != null && projectDir.isFolder() && !isProcessed(resultset, projectDir)) {
                    Project proj = processOneSubproject(projectDir);
                    NbMavenProjectImpl mv = proj != null ? proj.getLookup().lookup(NbMavenProjectImpl.class) : null;
                    if (mv != null) {
                        // ignore the pom type projects when resolving subprojects..
                        // maybe make an user settable option??
                        if (!NbMavenProject.TYPE_POM.equalsIgnoreCase(mv.getProjectWatcher().getPackagingType())) {
                            resultset.add(proj);
                        }
                        addProjectModules(FileUtil.toFile(mv.getProjectDirectory()),
                                resultset, mv.getOriginalMavenProject().getModules());
                    }
                } else {
                    // HUH?
                    ErrorManager.getDefault().log("fileobject not found=" + sub); //NOI18N
                }
            } else {
                ErrorManager.getDefault().log("project file not found=" + sub); //NOI18N
            }
        }
    }

    private Project processOneSubproject(FileObject projectDir) {


        try {
            return ProjectManager.getDefault().findProject(projectDir);
        } catch (IOException exc) {
            ErrorManager.getDefault().notify(exc);
        }

        return null;
    }

    @Override public synchronized void addChangeListener(ChangeListener changeListener) {
        if (!cs.hasListeners()) {
            project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(propertyChange);
        }
        cs.addChangeListener(changeListener);
    }

    @Override public synchronized void removeChangeListener(ChangeListener changeListener) {
        cs.removeChangeListener(changeListener);
        if (!cs.hasListeners()) {
            project.getLookup().lookup(NbMavenProject.class).removePropertyChangeListener(propertyChange);
        }
    }

}
