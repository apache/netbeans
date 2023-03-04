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
package org.netbeans.modules.web.clientproject.problems;

import java.util.concurrent.Future;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;

/**
 * Problem resolver using project customizer.
 */
public class CustomizerProblemResolver implements ProjectProblemResolver {

    private final ClientSideProject project;
    private final String category;
    private final String propertyName;


    public CustomizerProblemResolver(ClientSideProject project, String category, String propertyName) {
        this.project = project;
        this.category = category;
        this.propertyName = propertyName;
    }

    @Override
    public Future<ProjectProblemsProvider.Result> resolve() {
        project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(category);
        return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.project != null ? this.project.hashCode() : 0);
        hash = 59 * hash + (this.propertyName != null ? this.propertyName.hashCode() : 0);
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
        if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
            return false;
        }
        if ((this.propertyName == null) ? (other.propertyName != null) : !this.propertyName.equals(other.propertyName)) {
            return false;
        }
        return true;
    }

}
