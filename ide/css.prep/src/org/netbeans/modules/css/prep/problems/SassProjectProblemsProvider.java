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

import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.preferences.SassPreferences;
import org.netbeans.modules.css.prep.preferences.SassPreferencesValidator;
import org.netbeans.modules.css.prep.sass.SassCli;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.util.NbBundle;

public final class SassProjectProblemsProvider extends BaseProjectProblemsProvider {

    public SassProjectProblemsProvider(Project project) {
        super(project);
    }

    @NbBundle.Messages("SassProjectProblemsProvider.displayName=Sass")
    @Override
    String getDisplayName() {
        return Bundle.SassProjectProblemsProvider_displayName();
    }
    @Override
    boolean isEnabled(Project project) {
        return SassPreferences.getInstance().isEnabled(project);
    }

    @Override
    CssPreprocessorType getFileType() {
        return CssPreprocessorType.SASS;
    }

    @NbBundle.Messages({
        "SassProjectProblemsProvider.invalidCompiler.title=Invalid Sass compiler",
        "SassProjectProblemsProvider.invalidCompiler.description=The provided Sass compiler is not valid.",
    })
    @Override
    void checkCompiler(Collection<ProjectProblem> currentProblems) {
        try {
            SassCli.getDefault();
        } catch (InvalidExternalExecutableException ex) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.SassProjectProblemsProvider_invalidCompiler_title(),
                    Bundle.SassProjectProblemsProvider_invalidCompiler_description(),
                    OPTIONS_PROBLEM_RESOLVER);
            currentProblems.add(problem);
        }
    }

    @Override
    ValidationResult validatePreferences(Project project) {
        return new SassPreferencesValidator()
                .validate(project)
                .getResult();
    }

}
