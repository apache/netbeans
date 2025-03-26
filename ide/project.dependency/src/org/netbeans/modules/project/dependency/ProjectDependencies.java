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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.impl.ProjectModificationResultImpl;
import org.netbeans.modules.project.dependency.spi.ProjectDependenciesImplementation;
import org.netbeans.modules.project.dependency.spi.ProjectDependencyModifier;

/**
 * Project Query that collects dependencies using project-specific services.
 * @author sdedic
 */
public final class ProjectDependencies {
    /**
     * Finds project dependencies.
     * 
     * @param target the project to query
     * @param query the query
     * @return the dependency tree, result of the query.
     * @throws ProjectOperationException 
     */
    public static DependencyResult findDependencies(Project target, DependencyQuery query) throws ProjectOperationException {
        ProjectDependenciesImplementation pds = target.getLookup().lookup(ProjectDependenciesImplementation.class);
        if (pds == null) {
            return null;
        } else {
            return pds.findDependencies(query);
        }
    }
    
    /**
     * Creates the dependency query builder
     * @return the builder instance.
     */
    public static DependencyQueryBuilder newBuilder() {
        return new DependencyQueryBuilder();
    }
    
    /**
     * Creates a simple query. The query runs offline and uses available caches. Different
     * behaviour can be configured by using {@link DependencyQueryBuilder}
     * @param scopes scope(s) to query
     * @return the query instance
     */
    public static DependencyQuery newQuery(Scope... scopes) {
        return newBuilder().scope(scopes).build();
    }
    
    /**
     * Builder that can create {@link DependencyQuery} instance.
     */
    public static final class DependencyQueryBuilder {
        private Set<Scope> scopes;
        private Dependency.Filter filter;
        private boolean offline = true;
        private boolean flush;
        
        private DependencyQueryBuilder() {
        }
        
        public DependencyQuery build() {
            if (scopes == null) {
                scope(Scopes.COMPILE);
            }
            return new DependencyQuery(scopes, filter, offline, flush);
        }
        
        public DependencyQueryBuilder filter(Dependency.Filter f) {
            this.filter = f;
            return this;
        }
        
        public DependencyQueryBuilder scope(Scope... s) {
            if (s == null || s.length == 0) {
                return this;
            }
            if (scopes == null) {
                scopes = new LinkedHashSet<>();
            }
            scopes.addAll(Arrays.asList(s));
            return this;
        }
        
        public DependencyQueryBuilder online() {
            this.offline = false;
            return this;
        }
        
        public DependencyQueryBuilder offline() {
            this.offline = true;
            return this;
        }
        
        public DependencyQueryBuilder withoutCaches() {
            flush = true;
            return this;
        }
    }
    
    public static final class DependencyQuery {
        private final Set<Scope> scopes;
        private final Dependency.Filter filter;
        private final boolean offline;
        private final boolean flushChaches;

        private DependencyQuery(Set<Scope> scopes, Dependency.Filter filter, boolean offline, boolean flushCaches) {
            this.scopes = scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(scopes));
            this.filter = filter;
            this.offline = offline;
            this.flushChaches = flushCaches;
        }

        public Set<Scope> getScopes() {
            return scopes;
        }

        public Dependency.Filter getFilter() {
            return filter;
        }

        public boolean isOffline() {
            return offline;
        }

        public boolean isFlushChaches() {
            return flushChaches;
        }
    }
    
    /**
     * Convenience method that modifies the project to add dependencies.
     * @param project the target project
     * @param dependencies list of dependencies to add
     * @return modification result
     * @see #modifyDependencies(org.netbeans.api.project.Project, org.netbeans.modules.project.dependency.DependencyChange) 
     * @since 1.7
     */
    public static ProjectModificationResult addDependencies(Project project, Dependency... dependencies) throws DependencyChangeException, ProjectOperationException {
        return modifyDependencies(project, new DependencyChangeRequest(
                Collections.singletonList(
                        DependencyChange.builder(DependencyChange.Kind.ADD).dependency(dependencies).create())
        ));
    }

    /**
     * Convenience method that modifies the project to remove dependencies.
     * @param project the target project
     * @param dependencies list of dependencies to remove
     * @return modification result
     * @see #modifyDependencies(org.netbeans.api.project.Project, org.netbeans.modules.project.dependency.DependencyChange) 
     * @since 1.7
     */
    public static ProjectModificationResult removeDependencies(Project project, List<Dependency> dependencies) throws DependencyChangeException, ProjectOperationException {
        return modifyDependencies(project, new DependencyChangeRequest(
                Collections.singletonList(DependencyChange.builder(DependencyChange.Kind.REMOVE).dependency(dependencies).create())));
    }
    
    /**
     * Convenience method that makes simple dependency change, either add or remove. For detailed information,
     * see {@link #modifyDependencies(org.netbeans.api.project.Project, org.netbeans.modules.project.dependency.DependencyChangeRequest)}.
     * @param p the project
     * @param change add or remove change
     * @return modification result.
     * @throws DependencyChangeException if the modification fails because of project constraints
     * @throws ProjectOperationException in case of project system failure.
     */
    public static ProjectModificationResult modifyDependencies(Project p, DependencyChange change) throws DependencyChangeException, ProjectOperationException {
        return modifyDependencies(p, new DependencyChangeRequest(Collections.singletonList(change)));
    }
    
    /**
     * Makes modifications to project dependencies. The modifications are specified in the request. Note that the project system
     * is likely to require reload of the project after the change, since dependency changes change resolution of everything, and if plugins
     * are added/removed, the entire build system may work differently. All dependency changes are better done in one request, not
     * sequentially, so the disruption to the project model is minimized.
     * <p/>
     * The operation may also throw {@link ProjectOperationException} to indicate that the operation cannot be done safely, or there's not enough
     * project information to perform the operation.
     * 
     * @since 1.7
     * @param p the project
     * @param  the change to made
     * @return proposed changes to make.
     * @throws DependencyChangeException in case of error or dependency conflicts
     * @throws ProjectOperationException if the project could not be properly loaded
     */
    public static ProjectModificationResult modifyDependencies(Project p, DependencyChangeRequest change) throws DependencyChangeException, ProjectOperationException {
        Collection<? extends ProjectDependencyModifier> modifiers = p.getLookup().lookupAll(ProjectDependencyModifier.class);
        if (modifiers.isEmpty()) {
            // simply unsupported.
            return null;
        }
        
        ProjectModificationResultImpl impl = new ProjectModificationResultImpl(p);
        List<ProjectDependencyModifier.Result> results = new ArrayList<>();
        Set<String> exclude = new HashSet<>();
        
        for (ProjectDependencyModifier m : modifiers) {
            ProjectDependencyModifier.Result res = m.computeChange(change);
            if (res != null) {
                results.add(res);
            }
        }
        if (results.isEmpty()) {
            return null;
        }
        
        // remove excluded results
        for (ProjectDependencyModifier.Result r : results) {
            for (ProjectDependencyModifier.Result c : results) {
                if (exclude.contains(c.getId())) {
                    continue;
                }
                
                if (c.suppresses(r)) {
                    exclude.add(r.getId());
                    break; // inner cycle
                }
            }
        }
        
        for (ProjectDependencyModifier.Result r : results) {
            if (!exclude.contains(r.getId())) {
                impl.add(r);
            }
        }
        
        return new ProjectModificationResult(impl);
    }
}
