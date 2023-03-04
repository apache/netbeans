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
package org.netbeans.modules.project.dependency;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.spi.ProjectDependenciesImplementation;

/**
 * Project Query that collects dependencies using project-specific services.
 * @author sdedic
 */
public class ProjectDependencies {
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
}
