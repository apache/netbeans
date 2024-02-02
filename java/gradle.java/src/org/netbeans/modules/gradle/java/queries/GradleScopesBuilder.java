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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;

/**
 * Builds scopes for the specific project, using its configurations.
 * @author sdedic
 */
public final class GradleScopesBuilder {
    private final static Map<String, String> REMAP_ABSTRACT_SCOPES = new HashMap<>();
    private final Project project;
    private final GradleBaseProject gbp;

    public GradleScopesBuilder(Project project) {
        this.project = project;
        this.gbp = GradleBaseProject.get(project);
    }
    
    private Map<String, Collection<String>> extendsFrom = new HashMap<>();
    private Map<String, Collection<String>> inheritedInto = new HashMap<>();
    
    static {
        REMAP_ABSTRACT_SCOPES.put(Scopes.COMPILE.name(), "compileClasspath");
        REMAP_ABSTRACT_SCOPES.put(Scopes.RUNTIME.name(), "runtimeClasspath");
        REMAP_ABSTRACT_SCOPES.put(Scopes.TEST_COMPILE.name(), "testCompileClasspath");
        REMAP_ABSTRACT_SCOPES.put(Scopes.TEST_RUNTIME.name(), "testRuntimeClasspath");
        REMAP_ABSTRACT_SCOPES.put(Scopes.TEST.name(), Scopes.TEST_RUNTIME.name() + "," + Scopes.TEST_COMPILE.name() + "," + "testRuntimeClasspath,testCompileClasspath");
    }
    
    private void addToMap(Map<String, Collection<String>> map, String k, String c) {
        map.computeIfAbsent(k, n -> new LinkedHashSet<>(2)).add(c);
    }
    
    private void addDependency(String inheritedTo, String inheritedFrom) {
        addToMap(extendsFrom, inheritedTo, inheritedFrom);
        addToMap(inheritedInto, inheritedFrom, inheritedTo);
    }
    
    Map<String, GradleScope> scopes = new HashMap<>();
    
    private GradleScope createMetaScope(String metaName, String config, String targetConfig) {
        GS gs = new GS();
        scopeData.put(metaName, gs);
        addDependency(metaName, config);
        GradleScope s = new GradleScope(metaName, config, targetConfig, gs.extendsFrom, gs.inheritedInto);
        scopes.put(metaName, s);
        return s;
    }
    
    static class GS {
        Set<Scope> extendsFrom = new HashSet<>();
        Set<Scope> inheritedInto = new HashSet<>();
    }
    
    private Map<String, GS> scopeData = new HashMap<>();
    
    public GradleScopes build() {
        for (String cfg : gbp.getConfigurations().keySet()) {
            GradleConfiguration c = gbp.getConfigurations().get(cfg);
            c.getExtendsFrom().forEach(p -> {
                addDependency(cfg, p.getName());
            });
        }
        
        addDependency(Scopes.EXTERNAL.name(), "compileOnly");
        addDependency(Scopes.PROCESS.name(), "annotationProcessor");
        addDependency(Scopes.COMPILE.name(), "compileClasspath");
        addDependency(Scopes.RUNTIME.name(), "runtimeClasspath");
        addDependency(Scopes.TEST_COMPILE.name(), "testCompileClasspath");
        addDependency(Scopes.TEST_RUNTIME.name(), "testRuntimeClasspath");
        addDependency(Scopes.TEST.name(), Scopes.TEST_RUNTIME.name() + "," + Scopes.TEST_COMPILE.name());
        
        for (String cfg : gbp.getConfigurations().keySet()) {
            GS gs = new GS();
            scopeData.put(cfg, gs);
            scopes.put(cfg, new GradleScope(cfg, gs.extendsFrom, gs.inheritedInto));
        }
        
        createMetaScope(Scopes.EXTERNAL.name(), "compileOnly", "compileOnly");
        createMetaScope(Scopes.PROCESS.name(), "annotationProcessor", "annotationProcessor");
        createMetaScope(Scopes.COMPILE.name(), "compileClasspath", "implementation");
        createMetaScope(Scopes.RUNTIME.name(), "runtimeClasspath", "runtimeOnly");
        createMetaScope(Scopes.TEST_COMPILE.name(), "testCompileClasspath", "testCompileClasspath");
        createMetaScope(Scopes.TEST_RUNTIME.name(), "testRuntimeClasspath", "testRuntimeClasspath");
        createMetaScope(Scopes.TEST.name(), "testCompileClasspath", "testImplementation");
        
        for (GradleScope gs : scopes.values()) {
            GS data = scopeData.get(gs.name());
            
            extendsFrom.getOrDefault(gs.name(), Collections.emptyList()).
                    stream().
                    map(scopes::get).filter(Objects::nonNull).forEach(data.extendsFrom::add);
            inheritedInto.getOrDefault(gs.name(), Collections.emptyList()).
                    stream().
                    map(scopes::get).filter(Objects::nonNull).forEach(data.inheritedInto::add);
        }

        return new GradleScopes(project, scopes);
    }
}
