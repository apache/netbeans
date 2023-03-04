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

package org.netbeans.spi.project;

import org.openide.util.Lookup;

/**
 * Ability for a project to have various actions (e.g. Build) invoked on it.
 * Should be registered in a project's lookup and will be used by UI infrastructure.
 * <p>
 * Implementations supporting single file commands (command constants ending with
 * {@code _SINGLE}) can also be registered in default lookup. If a provider in project
 * lookup does not enable the action for a given command on the selected file then
 * the first implementation found in default lookup that is enabled will be used.
 * </p>
 * If the project supports {@link ProjectConfiguration}s, the ActionProvider implementation 
 * must check {@link ProjectConfiguration} presence in the action's context Lookup whether the caller
 * requested a specific configuration, and use it to process the requested action, if found.
 * @see org.netbeans.api.project.Project#getLookup
 * @see <a href="@org-apache-tools-ant-module@/org/apache/tools/ant/module/api/support/ActionUtils.html"><code>ActionUtils</code></a>
 * @see <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/support/ProjectSensitiveActions.html#projectCommandAction(java.lang.String,%20java.lang.String,%20javax.swing.Icon)"><code>ProjectSensitiveActions.projectCommandAction(...)</code></a>
 * @see SingleMethod
 * @author Jesse Glick
 */
public interface ActionProvider {
    
    /**
     * Standard command to incrementally build the project.
     */
    String COMMAND_BUILD = "build"; // NOI18N
    
    /** 
     * Standard command for compiling set of files
     */
    String COMMAND_COMPILE_SINGLE = "compile.single"; // NOI18N
        
    /**
     * Standard command to clean build products.
     */
    String COMMAND_CLEAN = "clean"; // NOI18N
    
    /**
     * Standard command to do a "clean" (forced) rebuild.
     */
    String COMMAND_REBUILD = "rebuild"; // NOI18N
        
    /** 
     * Standard command for running the project
     */
    String COMMAND_RUN = "run"; // NOI18N

    /** 
     * Standard command for running one file
     *
     * @see  SingleMethod#COMMAND_RUN_SINGLE_METHOD
     */
    String COMMAND_RUN_SINGLE = "run.single"; // NOI18N
    
    /** 
     * Standard command for running tests on given projects
     */
    String COMMAND_TEST = "test"; // NOI18N
    
    /** 
     * Standard command for running one test file
     */    
    String COMMAND_TEST_SINGLE = "test.single";  // NOI18N
    
    /**
     * Standard command for running the project in debugger
     */    
    String COMMAND_DEBUG = "debug"; // NOI18N
    
    /**
     * Standard command for running single file in debugger
     *
     * @see  SingleMethod#COMMAND_DEBUG_SINGLE_METHOD
     */    
    String COMMAND_DEBUG_SINGLE = "debug.single"; // NOI18N
    
    /** 
     * Standard command for running one test in debugger
     */
    String COMMAND_DEBUG_TEST_SINGLE = "debug.test.single"; // NOI18N
    
    /** 
     * Standard command for starting app in debugger and stopping at the 
     * beginning of app whatever that means.
     */
    String COMMAND_DEBUG_STEP_INTO = "debug.stepinto"; // NOI18N
    
    /**
     * Standard command for running the project in profiler
     * @since 1.43
     */
    String COMMAND_PROFILE = "profile"; // NOI18N
    
    /**
     * Standard command for running single file in profiler
     * @since 1.43
    */
    String COMMAND_PROFILE_SINGLE = "profile.single"; // NOI18N
    
    /** 
     * Standard command for running one test in profiler
     * @since 1.43
    */
    String COMMAND_PROFILE_TEST_SINGLE = "profile.test.single"; // NOI18N
    
    /**
     * Standard command for deleting the project.
     *
     * @since 1.6
     */
    String COMMAND_DELETE = "delete"; // NOI18N
    
    /**
     * Standard command for deleting the project.
     *
     * @since 1.7
     */
    String COMMAND_COPY = "copy"; // NOI18N
    
    /**
     * Standard command for moving the project.
     *
     * @since 1.7
     */
    String COMMAND_MOVE = "move"; // NOI18N

    /**
     * Standard command for renaming the project.
     *
     * @since 1.7
     */
    String COMMAND_RENAME = "rename"; // NOI18N
    
    /**
     * Standard command for priming / initializing
     * the project.
     * @since 1.80
     */
    String COMMAND_PRIME = "prime"; // NOI18N
    
    /**
     * Get a list of all commands which this project supports.
     * @return a list of command names suitable for {@link #invokeAction}
     * @see #COMMAND_BUILD
     * @see #COMMAND_CLEAN
     * @see #COMMAND_REBUILD
     */
    String[] getSupportedActions();
    
    /**
     * Run a project command.
     * Will be invoked in the event thread.
     * The context may be ignored by some commands, but some may need it in order
     * to get e.g. the selected source file to build by itself, etc.
     * @param command a predefined command name (must be among {@link #getSupportedActions})
     * @param context any action context, e.g. for a node selection
     *                (as in {@link ContextAwareAction})
     * @throws IllegalArgumentException if the requested command is not supported
     * @see ActionProgress
     */
    void invokeAction(String command, Lookup context) throws IllegalArgumentException;
    
    /**
     * Tells whether the command can be invoked in given context and thus if
     * actions representing this command should be enabled or disabled.
     * The context may be ignored by some commands, but some may need it in order
     * to get e.g. the selected source file to build by itself, etc.
     * @param command a predefined command name (must be among {@link #getSupportedActions})
     * @param context any action context, e.g. for a node selection
     *                (as in {@link ContextAwareAction})
     * @throws IllegalArgumentException if the requested command is not supported
     */
    boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException;
}
