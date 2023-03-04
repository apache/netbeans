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
package org.netbeans.modules.micronaut.maven;

/**
 *
 * @author sdedic
 */
public final class MicronautMavenConstants {
    
    /**
     * Plugin ID of the native-image plugin
     */
    public static final String NATIVE_BUILD_PLUGIN_ID = "native-maven-plugin"; // NOI18N

    /**
     * Group ID of the native-image plugin
     */
    public static final String NATIVE_BUILD_PLUGIN_GROUP = "org.graalvm.buildtools"; // NOI18N

    /**
     * Type of the native-executable artifact. Unofficial type, as the artifact is not installed
     * by Maven and has no GAV.
     */
    public static final String TYPE_EXECUTABLE = "exe";
    
    /**
     * Type of the dynamic library artifact. Unofficial type.
     */
    public static final String TYPE_DYNAMIC_LIBRARY = "dynlib";
    
    /**
     * Classifier for the native-image artifact. Unofficial, as the install
     * plugin will not install this artifact into local repository.
     */
    public static final String CLASSIFIER_NATIVE = "native-image";
    
    /**
     * Native image plugin's goal to compile image without fork. Available from
     * 0.9.14 version, DOES NOT exist on earlier versions.
     */
    public static final String PLUGIN_GOAL_COMPILE_NOFORK = "compile-no-fork";

    /**
     * Now deprecated goal, which produces a warning starting from 0.9.14 .
     */
    public static final String PLUGIN_GOAL_COMPILE_NOFORK_OLD = "build";

    /**
     * Native image plugin's goal to compile image from commandline
     */
    public static final String PLUGIN_GOAL_COMPILE = "compile";

    /**
     * Performs native compilation instead of the java one.
     */
    public static final String ACTION_NATIVE_COMPILE = "native-build";
    
    /**
     * Packaging that will trigger the native-image compiler.
     */
    public static final String PACKAGING_NATIVE = "native-image";
}
