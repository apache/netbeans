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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import static org.netbeans.spi.project.ui.ProjectProblemsProvider.PROP_PROBLEMS;
import org.openide.util.NbBundle;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleBrokenRuntimeProblemProvider implements ProjectProblemsProvider {
    private final Project project;
    private final PropertyChangeListener listener;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public GradleBrokenRuntimeProblemProvider(Project project) {
        this.project = project;
        listener = (PropertyChangeEvent evt) -> {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                support.firePropertyChange(PROP_PROBLEMS, null, null);
            }
        };
        NbGradleProject.addPropertyChangeListener(project, listener);
    }

    @Override
    @NbBundle.Messages({
        "LBL_BrokenPlatform=Broken Platform.",
    })
    public Collection<? extends ProjectProblemsProvider.ProjectProblem> getProblems() {
        return RunUtils.getActiveRuntime(project).isBroken()
                ? Collections.singleton(ProjectProblemsProvider.ProjectProblem.createWarning(Bundle.LBL_BrokenPlatform(), Bundle.LBL_BrokenPlatform()))
                : Collections.emptySet();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

}
