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
package org.netbeans.modules.gradle.java.queries;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.ProjectScopes;
import org.netbeans.modules.project.dependency.Scope;

/**
 * Implementation of ProjectScopes that bridges Gradle configutations.
 * 
 * @author sdedic
 */
public final class GradleScopes implements ProjectScopes{
    private final Project   project;
    private final Map<String, GradleScope> scopes;

    public GradleScopes(Project project, Map<String, GradleScope> scopes) {
        this.project = project;
        this.scopes = Collections.unmodifiableMap(scopes);
    }

    @Override
    public Collection<GradleScope> scopes() {
        return scopes.values();
    }
    
    public GradleScope toGradleScope(String n) {
        return scopes.get(n);
    }
    
    public GradleScope toGradleScope(Scope s) {
        return scopes.getOrDefault(s.name(), scopes.get(GradleScope.IMPLEMENTATION));
    }

    @Override
    public Collection<? extends Scope> implies(Scope s, boolean direct) {
        GradleScope scope = scopes.get(s.name());
        if (scope == null) {
            return Collections.emptyList();
        }
        if (direct) {
            return scope.getInheritedInto();
        }
        return Collections.emptySet();
    }
}
