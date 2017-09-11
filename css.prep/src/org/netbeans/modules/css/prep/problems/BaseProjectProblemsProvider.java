/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
