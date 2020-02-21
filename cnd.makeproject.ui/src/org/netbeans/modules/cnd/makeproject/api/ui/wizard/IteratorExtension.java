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

package org.netbeans.modules.cnd.makeproject.api.ui.wizard;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 */
public interface IteratorExtension {

    /**
     * Method discover additional project artifacts by folder or binary file
     * 
     * @param map input/output map
     */
    void discoverArtifacts(Map<String,Object> map);

    /**
     * Method delegates a project creating to discovery.
     * Instantiate make project in simple mode.
     * 
     * @param wizard
     * @return set of make projects
     * @throws java.io.IOException
     */
    Set<FileObject> createProject(WizardDescriptor wizard) throws IOException;

    /**
     * Method invoke discovery for created project.
     * 
     * @param map input map
     * @param project that will be configured or created
     * @param projectKind fullness of configured project
     */
    void discoverProject(Map<String,Object> map, Project project, ProjectKind projectKind);
    
    /**
     * Adds headers items in the project, changes exclude/include state of headers items
     * according to code model. Returns immediately, listens until parse in done,
     * tunes project on parse finish.
     * 
     * @param project 
     */
    void discoverHeadersByModel(Project project);

    /**
     * Method disable code model for project
     * 
     * @param project
     */
    void disableModel(Project project);

    public enum ProjectKind {
        Minimal, // include in project com
        IncludeDependencies,
        CreateDependencies
    }
}
