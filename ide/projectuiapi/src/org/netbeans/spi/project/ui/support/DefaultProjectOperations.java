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
package org.netbeans.spi.project.ui.support;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation;

/**Support class to allow the project type implementors to perform {@link ProjectOperations}
 * by simply calling a method in this class. Each method in this class provides a default
 * confirmation dialog and default behavior.
 *
 * If the project type requires a different behavior of an operation, it is required to provide its
 * own implementation of the operation.
 *
 * @since 1.10
 * @author Jan Lahoda
 */
public final class DefaultProjectOperations {
    
    /**
     * Creates a new instance of DefaultProjectOperations
     */
    private DefaultProjectOperations() {
    }
    
    /**Perform default delete operation. Gathers all necessary data, shows a confirmation
     * dialog and deletes the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to delete
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * if {@link org.netbeans.spi.project.support.ProjectOperations#isDeleteOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultDeleteOperation(Project p) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isDeleteOperationSupported(p)) {
            throw new IllegalArgumentException("Attempt to delete project that does not support deletion.");
        }
        
        DefaultProjectOperationsImplementation.deleteProject(p);
    }
    
    /**Perform default copy operation. Gathers all necessary data, shows a confirmation
     * dialog and copies the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to copy
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * {@link org.netbeans.spi.project.support.ProjectOperations#isCopyOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultCopyOperation(Project p) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isCopyOperationSupported(p)) {
            throw new IllegalArgumentException("Attempt to copy project that does not support copy.");
        }
        
        DefaultProjectOperationsImplementation.copyProject(p);
    }
    
    /**Perform default move operation. Gathers all necessary data, shows a confirmation
     * dialog and moves the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to move
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * {@link ProjectOperations#isMoveOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultMoveOperation(Project p) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isMoveOperationSupported(p)) {
            throw new IllegalArgumentException("Attempt to move project that does not support move.");
        }
        
        DefaultProjectOperationsImplementation.moveProject(p);
    }
    
    /**Perform default rename operation. Gathers all necessary data, shows a confirmation
     * dialog and renames the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to move
     * @param newName new project's name or null
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * {@link ProjectOperations#isMoveOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultRenameOperation(Project p, String newName) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isMoveOperationSupported(p)) {
            throw new IllegalArgumentException("Attempt to rename project that does not support move.");
        }
        
        DefaultProjectOperationsImplementation.renameProject(p, newName);
    }
    
}
