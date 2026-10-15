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
package org.netbeans.modules.project.dependency.spi;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.openide.filesystems.FileObject;

/**
 * Computes dependency modifications to project files. Must be registered in the project's
 * Lookup. More implementation can be registered for a project; they take precedence
 * in the order of the project's Lookup. Modifiers can exclude other modifier's work, even though
 * they come later in the chain. 
 * <p>
 * The Modifier may request that some or all modified files are saved after the operation. It must 
 * either specify FileObjects (can use virtual ones for files that do not exist yet), or 
 * use {@link Result#SAVE_ALL} special instance to indicate that all files should be saved.
 * 
 * @since 1.7
 * @author sdedic
 */
public interface ProjectDependencyModifier {
    /**
     * Computes changes to project files that apply the dependency change
     * @param request
     * @return result of the operation
     * @throws DependencyChangeException if the dependencies cannot be changed
     * @throws ProjectOperationException on general project system error conditions
     */
    public Result   computeChange(DependencyChangeRequest request) throws DependencyChangeException;
    
    /**
     * Result of dependency modification change.
     */
    public interface Result {
        public static final Collection<FileObject> SAVE_ALL = Collections.singleton(null);
        
        /**
         * Returns list of files that require save.
         * @return files to save.
         */
        public default Collection<FileObject> requiresSave() {
            return SAVE_ALL;
        }
        
        /**
         * ID of the partial result. Mainly used to override / suppress unwanted changes by
         * more specific Modified implementations.
         * @return ID of the result.
         */
        public String getId();
        
        /**
         * Return true, if the given result should be suppressed. The Modified should provide its own
         * replacement in that case.
         * @param check result to check
         * @return true, if the result should not be used.
         */
        public boolean suppresses(Result check);
        
        /**
         * Returns edits that make the change. 
         * @return edits that implement the requested dependency change.
         */
        public WorkspaceEdit getWorkspaceEdit();
    }
}
