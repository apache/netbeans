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

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;

/**
 * Create in-memory projects from disk directories.
 * Instances should be registered into default lookup.
 * @author Jesse Glick
 */
public interface ProjectFactory {

    /**
     * Test whether a given directory probably refers to a project recognized by this factory
     * without actually trying to create it.
     * <p>Should be as fast as possible as it might be called sequentially on a
     * lot of directories.</p>
     * <p>Need not be definite; it is permitted to return null or throw an exception
     * from {@link #loadProject} even when returning <code>true</code> from this
     * method, in case the directory looked like a project directory but in fact
     * had something wrong with it.</p>
     * <p>Will be called inside read access by {@link ProjectManager#isProject}
     * or {@link ProjectManager#isProject2}.</p>
     * @param projectDirectory a directory which might refer to a project
     * @return true if this factory recognizes it
     */
    boolean isProject(FileObject projectDirectory);
    
    /**
     * Create a project that resides on disk.
     * If this factory does not
     * in fact recognize the directory, it should just return null.
     * <p>Will be called inside read access by {@link ProjectManager#findProject}.
     * <p>Do not do your own caching! The project manager caches projects for you, properly.
     * <p>Do not attempt to recognize subdirectories of your project directory (just return null),
     * unless they are distinct nested projects.
     * @param projectDirectory some directory on disk
     * @param state a callback permitting the project to indicate when it is modified
     * @return a matching project implementation, or null if this factory does not recognize it
     */
    Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException;

    /**
     * Save a project to disk.
     * <p>Will be called inside write access, by {@link ProjectManager#saveProject}
     * or {@link ProjectManager#saveAllProjects}.
     * @param project a project created with this factory's {@link #loadProject} method
     * @throws IOException if there is a problem saving
     * @throws ClassCastException if this factory did not create this project
     */
    void saveProject(Project project) throws IOException, ClassCastException;
    
}
