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
package org.netbeans.modules.cloud.oracle.assets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;

/**
 *
 * @author Jan Horvath
 */
public final class DependenciesAnalyzer implements SuggestionAnalyzer {

    private static final Map<String, String> DEPENDENCIES = new HashMap() {
        {
            put("micronaut-oraclecloud-atp", "Databases"); //NOI18N
            put("micronaut-object-storage-oracle-cloud", "Bucket"); //NOI18N
            put("micronaut-oraclecloud-vault", "Vault"); //NOI18N
        }
    };

    @Override
    public Set<SuggestedItem> findSuggestions(Project[] projects) {
        Set<SuggestedItem> result = new HashSet<>();
        if (projects == null) {
            return null;
        }
        for (int i = 0; i < projects.length; i++) {
            ProjectDependencies.DependencyQueryBuilder b = ProjectDependencies.newBuilder().online();
            DependencyResult r = ProjectDependencies.findDependencies(projects[i], b.build());
            List<Dependency> children = r.getRoot().getChildren();
            for (Dependency dependency : children) {
                String artifactId = dependency.getArtifact().getArtifactId();
                if (DEPENDENCIES.containsKey(artifactId)) {
                    String path = DEPENDENCIES.get(artifactId);
                    result.add(SuggestedItem.forPath(path));
                }
            }
        }
        result.add(SuggestedItem.forPath("Cluster"));
        result.add(SuggestedItem.forPath("ComputeInstance"));
        return result;
    }

}
