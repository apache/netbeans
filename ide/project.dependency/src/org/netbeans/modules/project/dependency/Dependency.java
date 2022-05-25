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
package org.netbeans.modules.project.dependency;

import java.util.List;

/**
 * Represents a dependency of an artifact. The {@link #getChildren()} artifacts are
 * needed in a certain {@link #getScope()}; should be ignored in unrelated scopes.
 * The artifact that is subject of this dependency is the {@link #getArtifact()}.
 * Children are expressed using other {@link Dependency} instances.
 * <p>
 * Dependency does not have well-defined {@link #equals} and {@link #hashCode}, use
 * {@link #getArtifact()} as key in Maps.
 * 
 * @author sdedic
 */
public final class Dependency {
    private final ArtifactSpec  artifact;
    private final List<Dependency> children;
    private final Scope scope;
    final Object data;

    Dependency(ArtifactSpec artifact, List<Dependency> children, Scope scope, Object data) {
        this.artifact = artifact;
        this.children = children;
        this.scope = scope;
        this.data = data;
    }

    public ArtifactSpec getArtifact() {
        return artifact;
    }

    public List<Dependency> getChildren() {
        return children;
    }

    public Scope getScope() {
        return scope;
    }
    
    public Object getProjectData() {
        return data;
    }
    
    public String toString() {
        return getArtifact() + "[" + scope + "]";
    }
    
    public static Dependency create(ArtifactSpec artifact, Scope scope, List<Dependency> children, Object data) {
        return new Dependency(artifact, children, scope, data);
    }
    
    /**
     * Allows to filter artifacts and their dependency subtrees.
     */
    public interface Filter {
        /**
         * Decide if the artifact 'a' and its dependencies should be included in the report.
         * @param s the scope which requires dependency on "a"
         * @param a the artifact
         * @return true, if "a" should be included in the result; false to exclude it and its
         * dependencies.
         */
        public boolean accept(Scope s, ArtifactSpec a);
    }
}
