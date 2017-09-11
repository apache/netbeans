/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 * finds subprojects (projects this one depends on) that are locally available
 * and can be build as one unit. Uses maven multiproject infrastructure. (maven.multiproject.includes)
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=ProjectContainerProvider.class, projectType="org-netbeans-modules-maven")
public class ProjectContainerProviderImpl implements ProjectContainerProvider {

    private final Project project;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final PropertyChangeListener propertyChange;

    public ProjectContainerProviderImpl(Project proj) {
        project = proj;
        propertyChange = new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                //TODO effectively we should listen on all "pom" child projects as well here.
                //but is someone actually currently listening on SubprojectProvider changes?
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    cs.fireChange();
                }
            }
        };
    }


   @Override
    public Result getContainedProjects() {
        Set<Project> projects = new HashSet<Project>();
        File basedir = FileUtil.toFile(project.getProjectDirectory());
        try {
            addProjectModules(basedir, projects, project.getLookup().lookup(NbMavenProject.class).getMavenProject().getModules());
        } catch (InterruptedException x) {
            // can be interrupted in the open project dialog..
            return new Result(Collections.<Project>emptySet(), false);
        }
        projects.remove(project);
        return new Result(projects, false);
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
                        resultset.add(proj);
//                        if (resultset.add(proj)) { //prevent cycles
                            //no recursion
                            
//                            addProjectModules(FileUtil.toFile(mv.getProjectDirectory()),
//                                    resultset, mv.getOriginalMavenProject().getModules());
//                        }
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
