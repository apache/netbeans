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
package org.netbeans.modules.jshell.launch;

/**
 *
 * @author sdedic
 */
public class PropertyNames {
    /**
     * JShell execution enabled for the configuration
     */
    public static final String JSHELL_ENABLED = "jshell.run.enable"; // NOI18N
    
    public static final String JSHELL_AUTO_OPEN = "jshell.run.show"; // NOI18N
    
    /**
     * JShell class loading policy: {system, class, eval}, see LoaderPolicy enum
     */
    public static final String JSHELL_CLASS_LOADING = "jshell.run.classloader"; // NOI18N
    
    /**
     * Reference class name, or declaring class name, depending on class loading policy
     */
    public static final String JSHELL_CLASSNAME = "jshell.classloader.from.class"; // NOI18N
    
    /**
     * Invoked method name
     */
    public static final String JSHELL_FROM_METHOD = "jshell.classloader.from.method"; // NOI18N
    
    /**
     * Classloader field name
     */
    public static final String JSHELL_FROM_FIELD = "jshell.classloader.from.field"; // NOI18N
    
    /**
     * Classname of the remote executor service
     */
    public static final String JSHELL_EXECUTOR = "jshell.executor"; // NOI18N
    
    public static final String EXECUTOR_CLASS_SWING = "org.netbeans.lib.jshell.agent.SwingExecutor"; // NOI18N
}
