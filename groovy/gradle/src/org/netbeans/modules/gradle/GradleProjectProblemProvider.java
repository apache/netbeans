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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;

import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.*;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleProjectProblemProvider implements ProjectProblemsProvider {

    static final RequestProcessor GRADLE_RESOLVER_RP = new RequestProcessor("gradle-project-resolver", 1); //NOI18N

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final Project project;
    private PropertyChangeListener listener;
    private final ProjectProblemResolver resolver = new GradleProjectProblemResolver();;

    public GradleProjectProblemProvider(Project project) {
        this.project = project;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    @NbBundle.Messages("LBL_BrokenPlatform=Broken Platform.")
    public Collection<? extends ProjectProblem> getProblems() {
        List<ProjectProblem> ret = new ArrayList<>();
        synchronized (this) {
            if (listener == null) {
                 listener = new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                            support.firePropertyChange(PROP_PROBLEMS, null, null);
                        }
                    }
                };
                NbGradleProject.addPropertyChangeListener(project, listener);
            }
        }
        GradleProject gp = project.getLookup().lookup(NbGradleProjectImpl.class).getGradleProject();
        for (String problem : gp.getProblems()) {
            String[] lines = problem.split("\\n"); //NOI18N
            ret.add(ProjectProblem.createWarning(lines[0], problem.replaceAll("\\n", "<br/>"), resolver)); //NOI18N
        }
        if (RunUtils.getActivePlatform(project) == null) {
            ret.add(ProjectProblem.createWarning(Bundle.LBL_BrokenPlatform(), Bundle.LBL_BrokenPlatform()));
        }
        return ret;
    }

    private class GradleProjectProblemResolver implements ProjectProblemResolver, Callable<Result> {


        @Override
        public Future<Result> resolve() {
            return GRADLE_RESOLVER_RP.submit(this);
        }

        @Override
        public Result call() throws Exception {
            NbGradleProjectImpl impl = project.getLookup().lookup(NbGradleProjectImpl.class);
            GradleProject gradleProject = GradleProjectCache.loadProject(impl, FULL_ONLINE, true);
            impl.fireProjectReload(false);
            Quality q = gradleProject.getQuality();
            Status st = q.worseThan(SIMPLE) ? Status.UNRESOLVED :
                    q.worseThan(FULL) ? Status.RESOLVED_WITH_WARNING : Status.RESOLVED;
            return Result.create(st);
        }

    }
}
