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

package org.netbeans.modules.maven.apisupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import static org.netbeans.spi.project.ui.ProjectProblemsProvider.PROP_PROBLEMS;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=ProjectProblemsProvider.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class MissingNbInstallationProblemProvider implements ProjectProblemsProvider {
    private final Project project;
    private final PropertyChangeSupport pchs;
    private final PropertyChangeListener propertyListener;
    private final PropertyChangeListener weak;

    public MissingNbInstallationProblemProvider(Project prj) {
        pchs = new PropertyChangeSupport(this);
        project = prj;
        propertyListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName()) && OpenProjects.getDefault().isProjectOpen(project)) {
                    pchs.firePropertyChange(PROP_PROBLEMS, null, null);
                }
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    pchs.firePropertyChange(PROP_PROBLEMS, null, null);
                }
            }
        };
        weak = WeakListeners.propertyChange(propertyListener, this);
    }

    
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pchs.addPropertyChangeListener(listener);
        if (pchs.getPropertyChangeListeners().length == 1) {
            //add listeners to open projects and project changes
            NbMavenProject.addPropertyChangeListener(project, propertyListener);
            OpenProjects.getDefault().addPropertyChangeListener(weak);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pchs.removePropertyChangeListener(listener);
        if (pchs.getPropertyChangeListeners().length == 0) {
            //add listeners to open projects and project changes
            NbMavenProject.removePropertyChangeListener(project, propertyListener);
            OpenProjects.getDefault().removePropertyChangeListener(weak);
        }
    }

    @Override
    @NbBundle.Messages({
        "TIT_Missing_platform=No NetBeans platform/IDE associated with the project",
        "DESC_Missing_platform=For some features to work the IDE needs to associate each Maven NetBeans module project with a NetBeans Platform/IDE that the module will be part of at runtime. "
                + "Such features include running/debugging the project, context information in various nodes or wizards etc. To fix the problem, either open the NetBeans platform application project that this project is part of (maven packaging nbm-application) or "
                + "define a property named netbeans.installation in either the pom.xml file or ~/.m2/settings.xml file pointing to a binary distribution of NetBeans."
    })
    public Collection<? extends ProjectProblem> getProblems() {
        Project parent = MavenNbModuleImpl.findAppProject(project);
        if (parent == null) {
            File install = MavenNbModuleImpl.findIDEInstallation(project);
            if (install == null || !install.exists()) {
                return Collections.singleton(ProjectProblem.createWarning(Bundle.TIT_Missing_platform(), Bundle.DESC_Missing_platform(), new ProjectProblemResolverImpl(project)));
            }
        }
        return Collections.emptySet();     
    }

    private static class ProjectProblemResolverImpl implements ProjectProblemResolver {
        private final Project project;

        public ProjectProblemResolverImpl(Project p) {
            this.project = p;
        }

        @Override
        public Future<Result> resolve() {
            FutureTask<Result> toRet = new FutureTask<Result>(new Callable<Result>() {
                
                @Override
                public Result call() throws Exception {
                    NotifyDescriptor.Confirmation action = new NotifyDescriptor.Confirmation("You can either define netbeans.installation property in .m2/settings.xml file to point to currently running IDE or open the associated nbm-application project.", "Resolve missing NetBeans platform");
                    String prop = "Define property";
                    String open = "Open Application project";
                    action.setOptions(new Object[] { prop, open, NotifyDescriptor.CANCEL_OPTION});
                    Object result = DialogDisplayer.getDefault().notify(action);
                    if (prop.equals(result)) {
                        RunIDEInstallationChecker.setRunningIDEAsInstallation();
                        return Result.create(Status.RESOLVED);
                    }
                    if (open.equals(result)) {
                        final Action act = FileUtil.getConfigObject("Actions/Project/org-netbeans-modules-project-ui-OpenProject.instance", Action.class);
                        if (act != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    act.actionPerformed(null);
                                }
                            });
                        }
                    }
                    return Result.create(Status.UNRESOLVED);
                }
            });
            toRet.run();
            return toRet;
        }

        @Override
        public int hashCode() {
            int hash = MissingNbInstallationProblemProvider.class.hashCode();
            hash = 73 * hash + (this.project != null ? this.project.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProjectProblemResolverImpl other = (ProjectProblemResolverImpl) obj;
            if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
                return false;
            }
            return true;
        }

        
        
    }
    
}
