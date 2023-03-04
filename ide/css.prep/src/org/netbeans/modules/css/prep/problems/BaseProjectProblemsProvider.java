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
package org.netbeans.modules.css.prep.problems;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.util.NbBundle;

abstract class BaseProjectProblemsProvider implements ProjectProblemsProvider {

    private static final Logger LOGGER = Logger.getLogger(BaseProjectProblemsProvider.class.getName());

    // one instance only so it is correctly found in project problems list
    protected static final ProjectProblemResolver OPTIONS_PROBLEM_RESOLVER = new OptionsProblemResolver();

    final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    final Project project;


    protected BaseProjectProblemsProvider(Project project) {
        assert project != null;
        this.project = project;
    }

    abstract String getDisplayName();
    abstract boolean isEnabled(Project project);
    abstract CssPreprocessorType getFileType();
    abstract void checkCompiler(Collection<ProjectProblem> currentProblems);
    abstract ValidationResult validatePreferences(Project project);

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
        if (!isEnabled(project)) {
            return Collections.emptyList();
        }
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<ProjectProblemsProvider.ProjectProblem> collectProblems() {
                Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>();
                checkCompiler(currentProblems);
                checkPreferences(currentProblems, project);
                return currentProblems;
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - preprocessor name",
        "# {1} - message",
        "BaseProjectProblemsProvider.error={0}: {1}",
    })
    protected void checkPreferences(Collection<ProjectProblem> currentProblems, Project project) {
        ValidationResult validationResult = validatePreferences(project);
        if (validationResult.isFaultless()) {
            return;
        }
        String message = validationResult.getFirstErrorMessage();
        if (message == null) {
            message = validationResult.getFirstWarningMessage();
        }
        assert message != null : "Message should be found for invalid preferences: " + getDisplayName();
        message = Bundle.BaseProjectProblemsProvider_error(getDisplayName(), message);
        ProjectProblem problem = ProjectProblem.createError(
                message,
                message,
                new CustomizerProblemResolver(project));
        currentProblems.add(problem);
    }

}
