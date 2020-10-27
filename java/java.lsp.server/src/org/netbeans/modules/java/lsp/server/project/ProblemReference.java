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
package org.netbeans.modules.java.lsp.server.project;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages(value = {"FMT_ProblemInProject={1} (in {0})"})
final class ProblemReference {
    
    private final boolean global;
    private final Project project;
    final ProjectProblemsProvider.ProjectProblem problem;
    volatile boolean resolved;

    ProblemReference(@NonNull final ProjectProblemsProvider.ProjectProblem problem, @NonNull final Project project, final boolean global) {
        assert problem != null;
        this.problem = problem;
        this.project = project;
        this.global = global;
    }

    String getDisplayName() {
        final String displayName = problem.getDisplayName();
        String message;
        if (global) {
            final String projectName = ProjectUtils.getInformation(project).getDisplayName();
            message = Bundle.FMT_ProblemInProject(projectName, displayName);
        } else {
            message = displayName;
        }
        return message;
    }

    @Override
    @NonNull
    public String toString() {
        return String.format("Problem: %s %s", //NOI18N
        problem, resolved ? "resolved" : "unresolved"); //NOI18N
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + problem.hashCode();
        hash = 31 * hash + project.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProblemReference)) {
            return false;
        }
        final ProblemReference otherRef = (ProblemReference) other;
        return problem.equals(otherRef.problem) && project.equals(otherRef.project);
    }
    
}
