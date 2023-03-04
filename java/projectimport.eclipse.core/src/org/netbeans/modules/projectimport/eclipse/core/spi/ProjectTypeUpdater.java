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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.project.Project;

/**
 */
public interface ProjectTypeUpdater extends ProjectTypeFactory {

    /**
     * Returns identifier uniquely identifying data in given ProjectImportModel 
     * instance. Identifier will be used for comparison of different versions of
     * ProjectImportModel data and equality of identifier will mean that project
     * is up to data and does not require update. Identifier should also contain
     * enough data to calculate difference between ProjectImportModel it represents
     * and any given ProjectImportModel.
     * 
     * <p>Example of identifier could be: 
     * "src=src;con=org.eclipse.jdt.launching.JRE_CONTAINER;var=MAVEN_REPO/commons-lang-2.3.jar;output=bin"
     */
    String calculateKey(ProjectImportModel model);

    /**
     * Update given project.
     * 
     * <p>This method is permited to show blocking UI.
     * 
     * <p>Always called under project write mutex.
     * 
     * 
     * @param project
     * @param model
     * @param oldKey
     * @return resulting new key to be stored; it can be different from value returend by {@link #calculateKey}
     *  because it is result of actual update and may keep some items which could not be removed or added
     *  succesfully.
     */
    String update(Project project, ProjectImportModel model, String oldKey, List<String> importProblems) throws IOException;
    
}
