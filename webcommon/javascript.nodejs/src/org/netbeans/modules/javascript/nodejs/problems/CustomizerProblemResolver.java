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
package org.netbeans.modules.javascript.nodejs.problems;

import java.util.Objects;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsCustomizerProvider;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;

public class CustomizerProblemResolver implements ProjectProblemResolver {

    private final Project project;
    private final String ident;
    private final ValidationResult result;
    private final String category;


    CustomizerProblemResolver(Project project, String ident, ValidationResult result) {
        this(project, ident, result, null);
    }

    CustomizerProblemResolver(Project project, String ident, String category) {
        this(project, ident, null, category);
    }

    private CustomizerProblemResolver(Project project, String ident, ValidationResult result, String category) {
        assert project != null;
        assert ident != null;
        this.project = project;
        this.ident = ident;
        this.result = result;
        this.category = category;
    }

    @Override
    public Future<ProjectProblemsProvider.Result> resolve() {
        if (result != null) {
            NodeJsCustomizerProvider.openCustomizer(project, result);
        } else {
            assert category != null;
            NodeJsCustomizerProvider.openCustomizer(project, category);
        }
        return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.project);
        hash = 83 * hash + Objects.hashCode(this.ident);
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
        final CustomizerProblemResolver other = (CustomizerProblemResolver) obj;
        if (!Objects.equals(this.project, other.project)) {
            return false;
        }
        if (!Objects.equals(this.ident, other.ident)) {
            return false;
        }
        return true;
    }


}
