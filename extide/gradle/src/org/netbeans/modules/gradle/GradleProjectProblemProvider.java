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

import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;

import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleProjectProblemProvider implements ProjectProblemsProvider {
    /**
     * Maximum number of lines presented from a report. Prevents stacktrace errors to flood everything, but Gradle has deep
     * stacks...
     */
    private static final int MAX_REPORT_LINES = 100;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final Project project;
    private final PropertyChangeListener listener;
    private final ProjectProblemResolver resolver = new GradleProjectProblemResolver();

    public GradleProjectProblemProvider(Project project) {
        this.project = project;
        listener = (PropertyChangeEvent evt) -> {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                support.firePropertyChange(PROP_PROBLEMS, null, null);
            }
        };
        NbGradleProject.addPropertyChangeListener(project, listener);
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
    @NbBundle.Messages({
        "LBL_PrimingRequired=Priming Build Required.",
        "TXT_PrimingRequired=In order to be able to read this project, "
        + "NetBeans needs to execute its Gradle scripts as priming build."
        + "\n\n"
        + "Executing Gradle scripts allows arbitrary code execution, "
        + "as current user, on this system."
    })
    public Collection<? extends ProjectProblem> getProblems() {
        List<ProjectProblem> ret = new ArrayList<>();
        GradleProject gp = project.getLookup().lookup(NbGradleProjectImpl.class).getGradleProject();
        // untrusted project can't have 'real' problems: the execution could not happen
        boolean trusted = ProjectTrust.getDefault().isTrusted(project);
        if (!trusted || gp.getProblems().isEmpty()) {
            if (gp.getQuality().notBetterThan(EVALUATED)) {
                ret.add(ProjectProblem.createError(Bundle.LBL_PrimingRequired(), Bundle.TXT_PrimingRequired(), resolver));
            }
        } else {
            for (GradleReport report : gp.getProblems()) {
                String problem = formatReport(report);
                String m;
                String d;
                if (report.getDetails() == null || report.getDetails().length == 0) {
                    String[] lines = problem.split("\n"); //NOI18N
                    m = lines[0];
                    d = problem.replaceAll("\n", "<br/>");
                } else {
                    m = problem;
                    d = String.join("\n", Arrays.asList(report.getDetails()).subList(0, Math.min(report.getDetails().length, MAX_REPORT_LINES)));
                }
                switch (report.getSeverity()) {
                    case ERROR:
                    case EXCEPTION:
                        ret.add(ProjectProblem.createError(m, d, null)); //NOI18N
                        break;
                    case WARNING:
                        ret.add(ProjectProblem.createWarning(m, d, null)); //NOI18N
                        break;
                }
            }
        }
        return ret;
    }
    
    private String formatReport(GradleReport r) {
        return r.formatReportForHintOrProblem(true, project.getProjectDirectory());
    }
    
    private class GradleProjectProblemResolver implements ProjectProblemResolver {

        @Override
        public Future<Result> resolve() {
            NbGradleProjectImpl impl = project.getLookup().lookup(NbGradleProjectImpl.class);
            return impl.primeProject().thenApply(gradleProject -> {
                Quality q = gradleProject.getQuality();
                Status st = q.worseThan(SIMPLE) ? Status.UNRESOLVED
                        : q.worseThan(FULL) ? Status.RESOLVED_WITH_WARNING : Status.RESOLVED;
                Set<GradleReport> problems = gradleProject.getProblems();
                if (problems.isEmpty()) {
                    return Result.create(st);
                } else {
                    return Result.create(st, formatReport(problems.iterator().next()));
                }
            });
       }
    }
}
