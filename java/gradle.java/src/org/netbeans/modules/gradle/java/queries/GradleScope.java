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
import java.util.HashSet;
import org.netbeans.modules.project.dependency.Scope;

/**
 * Custom gradle scopes that copy the structure of Gradle's standard configurations.
 * Each scope must point to a configuration where the artifacts for the scope will be added.
 * 
 * @author sdedic
 */
public final class GradleScope extends Scope {
    private final String configurationName;
    private final String targetConfiguration;
    private Collection<Scope> includes;
    private Collection<Scope> implies;
    
    GradleScope(String name, Collection<Scope> includes, Collection<Scope> implies) {
        this(name, name, name, includes, implies);
    }

    GradleScope(String name, String cfgName, String modifyCfgName, Collection<Scope> includes, Collection<Scope> implies) {
        super(name); // NOI18N
        this.configurationName = cfgName;
        this.targetConfiguration = modifyCfgName;
        this.includes = includes == null ? new HashSet<>() : includes;
        this.implies = implies == null ? new HashSet<>() : implies;
    }
    
    public boolean includes(Scope s) {
        return includes.contains(s.name());
    }
    
    public Collection<? extends Scope> getIncluded() {
        return includes;
    }
    
    public Collection<? extends Scope> getInheritedInto() {
        return implies;
    }

    public boolean implies(Scope s) {
        return implies.contains(s.name());
    }
    
    public String getConfigurationName() {
        return configurationName;
    }

    public String getTargetConfigurationName() {
        return targetConfiguration;
    }
    
    public String toString() {
        return name();
    }
    
    /**
     * Name of the "implementation" scope
     */
    static final String IMPLEMENTATION = "implementation";
}
