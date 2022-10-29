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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sdedic
 */
public final class Scopes {
    /**
     * Build process dependencies. Annotation processors, buildtime tools, code generators
     */
    public static final Scope PROCESS = new DefaultScope("compileProcessing", Collections.emptySet(), Collections.emptySet());

    /**
     * External dependencies, not distributed with the application, but provided by the environment (= provided dependencies in Maven)
     */
    public static final Scope EXTERNAL = new DefaultScope("external", Collections.emptySet(), Collections.emptySet());
    
    /**
     * Compile dependencies. Resources used by build tools to build the application. Includes 
     * {@link #PROCESS} but does not export it further.
     */
    public static final Scope COMPILE = new DefaultScope("compilation", 
            Collections.singleton(PROCESS), Collections.singleton(PROCESS));
    
    /**
     * Runtime dependencies. Includes compile dependencies.
     */
    public static final Scope RUNTIME = new DefaultScope("runtime", Collections.singleton(COMPILE), Collections.emptySet());
    
    /**
     * Test compile dependencies.
     */
    public static final Scope TEST_COMPILE = new DefaultScope("testCompile", 
            new HashSet<>(Arrays.asList(PROCESS, COMPILE)), Collections.emptySet());
    
    /**
     * Test compile dependencies.
     */
    public static final Scope TEST_RUNTIME = new DefaultScope("testRuntime", 
            new HashSet<>(Arrays.asList(TEST_COMPILE)), Collections.emptySet());
    
    /**
     * Test dependencies.
     */
    public static final Scope TEST = new DefaultScope("test", 
            new HashSet<>(Arrays.asList(PROCESS, COMPILE, RUNTIME)), Collections.emptySet()).imply(TEST_RUNTIME, TEST_COMPILE);
    
    /**
     * Included resources.
    public static final Scope INCLUDED = new DefaultScope("included", Collections.emptySet(), Collections.emptySet());
     */
    
    static final class DefaultScope extends Scope {
        private final Set<Scope> includes;
        private final Set<Scope> stops;
        private Set<Scope> implies;

        public DefaultScope(String name, Set<Scope> includes, Set<Scope> stops) {
            super(name);
            this.includes = includes;
            this.stops = stops;
        }

        @Override
        public boolean includes(Scope s) {
            return s == this || includes.contains(s);
        }

        @Override
        public boolean exports(Scope s) {
            return s == this || (!stops.contains(s) && includes(s));
        }

        @Override
        public String toString() {
            return name();
        }

        @Override
        public boolean implies(Scope s) {
            return implies != null && implies.contains(s);
        }
        
        public DefaultScope imply(Scope... scopes) {
            this.implies = new HashSet<>(Arrays.asList(scopes));
            return this;
        }
    }
}
