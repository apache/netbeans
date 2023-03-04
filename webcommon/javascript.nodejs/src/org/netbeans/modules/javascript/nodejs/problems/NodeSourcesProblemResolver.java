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
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsCustomizerProvider;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;

public class NodeSourcesProblemResolver implements ProjectProblemResolver {

    private final Project project;


    public NodeSourcesProblemResolver(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public Future<ProjectProblemsProvider.Result> resolve() {
        if (NodeJsSupport.forProject(project).getPreferences().isDefaultNode()) {
            OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
        } else {
            NodeJsCustomizerProvider.openCustomizer(project, NodeJsCustomizerProvider.CUSTOMIZER_IDENT);
        }
        return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(project);
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
        final NodeSourcesProblemResolver other = (NodeSourcesProblemResolver) obj;
        if (!Objects.equals(project, other.project)) {
            return false;
        }
        return true;
    }

}
