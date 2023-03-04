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

/**
 * Callback permitting {@link org.netbeans.api.project.Project}s to inform the
 * {@link org.netbeans.api.project.ProjectManager}
 * of important lifecycle events.
 * Currently the only available events are modification of the project metadata
 * and project deletion notification.
 * However in the future other events may be added, such as moving
 * the project, which the project manager would need to be informed of.
 * <p>
 * This interface may only be implemented by the project manager. A
 * {@link ProjectFactory} will receive an instance in
 * {@link ProjectFactory#loadProject}.
 * </p>
 * @author Jesse Glick
 */
public interface ProjectState {
    
    /**
     * Inform the manager that the project's in-memory state has been modified
     * and that a call to {@link ProjectFactory#saveProject} may be needed.
     * May not be called during {@link ProjectFactory#loadProject}.
     * <p>Acquires write access.
     */
    void markModified();
    
    /**
     * <p>Inform the manager that the project has been deleted. The project will
     * be removed from any {@link org.netbeans.api.project.ProjectManager}'s  mappings.
     * If {@link org.netbeans.api.project.ProjectManager#findProject} is called on the project directory,
     * the {@link ProjectFactory ProjectFactories} are asked again to recognize
     * the project.</p>
     *
     * <p>The project is no longer recognized as created by the {@link org.netbeans.api.project.ProjectManager}.</p>
     *
     * <p>Acquires write access.</p>
     *
     * @throws IllegalStateException if notifyDeleted is called more than once for a project.
     * @since 1.6
     */
    void notifyDeleted() throws IllegalStateException;
    
}
