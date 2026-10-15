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

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Represents a dependency of an artifact. The {@link #getChildren()} artifacts are
 * needed in a certain {@link #getScope()}; should be ignored in unrelated scopes.
 * The artifact that is subject of this dependency is the {@link #getArtifact()} or {@link #getProject()}.
 * Children are expressed using other {@link Dependency} instances.
 * <p>
 * A project does not need to produce a publishable (identifiable) artifact; in such cases, the
 * {@link #getArtifact} may return {@code null}.
 * <p>
 * Dependency does not have well-defined {@link #equals} and {@link #hashCode}, use
 * {@link #getArtifact()} or {@link #getProject()} as key in Maps.
 * 
 * @author sdedic
 */
public final class Dependency {
    private final ArtifactSpec  artifact;
    private final ProjectSpec   project;
    private final List<Dependency> children;
    private final Scope scope;
    private Dependency parent;
    final Object data;

    Dependency(ProjectSpec project, ArtifactSpec artifact, List<Dependency> children, Scope scope, Object data) {
        this.project = project;
        this.artifact = artifact;
        this.children = children;
        this.scope = scope;
        this.data = data;
    }

    /**
     * Returns the artifact that represents this dependency. For project dependencies, the artifact returned may be
     * {@code null}, if the project does not translate to an identifiable artifact. But even such dependencies can have
     * further children.
     * 
     * @return 
     */
    @CheckForNull
    public ArtifactSpec getArtifact() {
        return artifact;
    }

    public List<Dependency> getChildren() {
        return children;
    }
    
    /**
     * Returns project description for project dependencies, otherwise {@code null}
     * The Dependency may also return {@link #getArtifact()}, but some projects do not produce
     * externalized artifacts or the artifact specification is not known.
     * @return project description for this dependency.
     */
    @CheckForNull
    public ProjectSpec getProject() {
        return project;
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
    
    /**
     * Returns parent Dependency that injected this one in the project. Returns
     * {@code null}, if this dependency is directly specified or configured for the
     * project itself.
     * @return parent dependency or {@code null}.
     */
    @CheckForNull
    public Dependency getParent() {
        return parent;
    }
    
    private static Dependency assignParent(Dependency d) {
        d.getChildren().forEach(c -> c.parent = d);
        return d;
    }
    
    /**
     * A convenience method to make a dependency descriptor.
     * @param spec artifact specification
     * @param scope the dependency scope
     * @return dependency instance
     */
    public static Dependency make(ArtifactSpec spec, Scope scope) {
        return create(spec, scope, Collections.emptyList(), null);
    }
    
    /**
     * Creates an artifact dependency. The artifact need not physically exist on the filesystem, but its coordinates
     * must be known. 
     * @param artifact
     * @param scope
     * @param children
     * @param data
     * @return 
     */
    public static Dependency create(ArtifactSpec artifact, Scope scope, List<Dependency> children, Object data) {
        return assignParent(new Dependency(null, artifact, children, scope, data));
    }
    
    /**
     * Creates a dependency on a project. The project identifies 
     * @param project
     * @param artifact
     * @param scope
     * @param children
     * @param data
     * @return 
     */
    public static Dependency create(ProjectSpec project, ArtifactSpec artifact, Scope scope, List<Dependency> children, Object data) {
        return assignParent(new Dependency(project, artifact, children, scope, data));
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
