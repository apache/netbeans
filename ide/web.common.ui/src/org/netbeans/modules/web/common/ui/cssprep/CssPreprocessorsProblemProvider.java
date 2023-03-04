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
package org.netbeans.modules.web.common.ui.cssprep;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorUI;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.util.WeakListeners;

/**
 * Project problems for CSS preprocessors.
 */
public final class CssPreprocessorsProblemProvider implements ProjectProblemsProvider {

    final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    final Project project;
    final PreprocessorsListener preprocessorsListener = new PreprocessorsListener();


    private CssPreprocessorsProblemProvider(Project project) {
        assert project != null;
        this.project = project;
    }

    @SuppressWarnings("unchecked")
    public static CssPreprocessorsProblemProvider create(Project project) {
        CssPreprocessorsProblemProvider problemProvider = new CssPreprocessorsProblemProvider(project);
        CssPreprocessors cssPreprocessors = CssPreprocessors.getDefault();
        cssPreprocessors.addCssPreprocessorsListener(WeakListeners.create(CssPreprocessorsListener.class, problemProvider.preprocessorsListener, cssPreprocessors));
        return problemProvider;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.removePropertyChangeListener(listener);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<ProjectProblemsProvider.ProjectProblem> collectProblems() {
                Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>();
                for (CssPreprocessorUI preprocessor : CssPreprocessorsAccessor.getDefault().getPreprocessors()) {
                    ProjectProblemsProvider problemsProvider = CssPreprocessorAccessor.getDefault().createProjectProblemsProvider(preprocessor, project);
                    if (problemsProvider != null) {
                        currentProblems.addAll(problemsProvider.getProblems());
                    }
                }
                return currentProblems;
            }
        });
    }

    //~ Inner classes

    private final class PreprocessorsListener implements CssPreprocessorsListener {

        @Override
        public void preprocessorsChanged() {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void optionsChanged(CssPreprocessor cssPreprocessor) {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void customizerChanged(Project project, CssPreprocessor cssPreprocessor) {
            if (project.equals(project)) {
                problemsProviderSupport.fireProblemsChange();
            }
        }

        @Override
        public void processingErrorOccured(Project project, CssPreprocessor cssPreprocessor, String error) {
            if (project.equals(project)) {
                problemsProviderSupport.fireProblemsChange();
            }
        }

    }

}
