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

package org.netbeans.modules.maven.api.classpath;

import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.project.Project;

/**
 * list of dependency projects that map to artifacts on classpath(s) without scope considerations.
 * @author mkleint
 * @since 2.75
 */
public interface DependencyProjectsProvider {
    
    public Set<Pair> getDependencyProjects();
    
    /**
     * result returned from <code>getDependencyProjects()</code>
     */
    public static final class Pair {

        private final Project project;
        private final Artifact artifact;
        
        public Pair(Project project, Artifact artifact) {
            this.project = project;
            this.artifact = artifact;
        }
        
        public Project getProject() {
            return project;
        }

        public Artifact getArtifact() {
            return artifact;
        }
        
        public boolean isIncludedAtRuntime() {
            return Artifact.SCOPE_RUNTIME.equals(artifact.getScope()) || Artifact.SCOPE_COMPILE.equals(artifact.getScope());
        }
        public boolean isIncludedAtTests() {
            return Artifact.SCOPE_TEST.equals(artifact.getScope()) || Artifact.SCOPE_COMPILE.equals(artifact.getScope()) || Artifact.SCOPE_RUNTIME.equals(artifact.getScope());
        }
        public boolean isIncludedAtCompile() {
            return Artifact.SCOPE_PROVIDED.equals(artifact.getScope()) || Artifact.SCOPE_COMPILE.equals(artifact.getScope());
        }
        
    }    
}
