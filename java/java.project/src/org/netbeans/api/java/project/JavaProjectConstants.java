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

package org.netbeans.api.java.project;

/**
 * Constants useful for Java-based projects.
 * @author Jesse Glick
 */
public class JavaProjectConstants {

    private JavaProjectConstants() {}

    /**
     * Java package root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_JAVA = "java"; // NOI18N

    /**
     * Package root sources type for resources, if these are not put together with Java sources.
     * @see org.netbeans.api.project.Sources
     * @since org.netbeans.modules.java.project/1 1.11
     */
    public static final String SOURCES_TYPE_RESOURCES = "resources"; // NOI18N

    /**
     * Java module root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_MODULES = "modules"; // NOI18N

    /**
     * Hint for <code>SourceGroupModifier</code> to create a <code>SourceGroup</code>
     * for main project codebase.
     * @see org.netbeans.api.project.SourceGroupModifier
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static final String SOURCES_HINT_MAIN = "main"; //NOI18N

    /**
     * Hint for <code>SourceGroupModifier</code> to create a <code>SourceGroup</code>
     * for project's tests.
     * @see org.netbeans.api.project.SourceGroupModifier
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static final String SOURCES_HINT_TEST = "test"; //NOI18N

    /**
     * Standard artifact type representing a JAR file, presumably
     * used as a Java library of some kind.
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_JAR = "jar"; // NOI18N
    
    
    /**
     * Standard artifact type representing a folder containing classes, presumably
     * used as a Java library of some kind.
     * @see org.netbeans.api.project.ant.AntArtifact
     * @since org.netbeans.modules.java.project/1 1.4
     */
    public static final String ARTIFACT_TYPE_FOLDER = "folder"; //NOI18N

    /**
     * Standard command for running Javadoc on a project.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_JAVADOC = "javadoc"; // NOI18N
    
    /** 
     * Standard command for reloading a class in a foreign VM and continuing debugging.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_DEBUG_FIX = "debug.fix"; // NOI18N
    
}
