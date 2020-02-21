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

package org.netbeans.modules.cnd.discovery.api;

import org.netbeans.api.project.Project;

/**
 *
 */
public interface ProjectProxy {

    /**
     * Return true if project can be divided on subprojects
     */
    boolean createSubProjects();
    
    /**
     * Returns native project if provider called for existent project
     */
    Project getProject();

    /**
     * Returns path to makefile
     */
    String getMakefile();

    /**
     * Returns path to sources
     */
    String getSourceRoot();

    /**
     * Returns path to build result
     */
    String getExecutable();

    /**
     * Returns path to working folder
     */
    String getWorkingFolder();
    
    /**
     * Returns true if discovered properties are merged with project properties
     */
    boolean mergeProjectProperties();
    
    /**
     * Case: binary file compiled from symbolic link.
     * By default project will contain only real compiled sources (i.e. links).
     * User expectation is: project contains original source files.
     * 
     * @return true if project should contain resolved links.
     */
    boolean resolveSymbolicLinks();
}
