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

/**
 *
 * @author sdedic
 */
public final class Scopes {
    /**
     * Build process dependencies. Annotation processors, buildtime tools, code generators
     */
    public static final Scope PROCESS = new Scope("compileProcessing");

    /**
     * External dependencies, not distributed with the application, but provided by the environment (= provided dependencies in Maven)
     */
    public static final Scope EXTERNAL = new Scope("external");
    
    /**
     * Compile API dependencies. Optional, if the build system supports it. Otherwise should be equal to 
     * {@link #COMPILE}. Gradle makes a difference between API and implementation. 
     */
    public static final Scope API = new Scope("api");
    
    /**
     * Compile dependencies. Resources used by build tools to build the application. 
     */
    public static final Scope COMPILE = new Scope("compilation");
    
    /**
     * Runtime dependencies. Includes compile dependencies, but not necessarily all of them.
     */
    public static final Scope RUNTIME = new Scope("runtime");
    
    /**
     * Test compile dependencies. Optional, if the build system supports it.
     */
    public static final Scope TEST_COMPILE = new Scope("testCompile");
    
    /**
     * Test runtime dependencies. Optional, if the build system supports it.
     */
    public static final Scope TEST_RUNTIME = new Scope("testRuntime");
    
    /**
     * Generic test dependencies. 
     */
    public static final Scope TEST = new Scope("test");
    
    /**
     * Dependencies directly declared by the project definition. Can be combined with other types to select just specific
     * dependencies. Note that dependencies obtained using this modifier may be incomplete or version-unresolved, if they appear so
     * in the build file. 
     * <p>
     * Note that it is not possible to add dependencies with this scope (exception will be thrown), it only serves as marker. Also no dependency
     * will not be marked with this scope, all dependencies retain the scope they are declared for in the project's metadata.
     */
    public static final Scope DECLARED = new Scope("*declared");

    /**
     * Represents a plugin dependency. Plugin dependency extends applies a plugin to a build. PLUGIN dependency artifacts
     * specify names and versions of those plugins. 
     */
    public static final Scope PLUGIN = new org.netbeans.modules.project.dependency.Scope("*plugin");
}
