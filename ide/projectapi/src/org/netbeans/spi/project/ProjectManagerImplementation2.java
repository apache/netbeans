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
package org.netbeans.spi.project;

import java.io.IOException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;

/**
 * Trivial extension of {@link ProjectManagerImplementation} to support access
 * to multiple project types per project directories.
 *
 * @since 1.73
 * @author lkishalmi
 */
public interface ProjectManagerImplementation2 extends ProjectManagerImplementation {
    /**
     * Find a NetBeans {@link Project} for the given directory and project type.
     *
     * @param projectDirectory the project folder
     * @param projectType the project type to load. This can be {@code null}, that
     *        case the first identified project shall be used and the call
     *        is equivalent with {@link #findProject(org.openide.filesystems.FileObject)}.
     * @see ProjectManager.Result#getProjectType()
     * @return a project with the requested type or {@code null}.
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @CheckForNull
    Project findProject(@NonNull FileObject projectDirectory, String projectType) throws IOException, IllegalArgumentException;

    /**
     * Returns all {@link  ProjectManager.Result} that can be associated with
     * the given folder.
     *
     * @since 1.73
     * @param projectDirectory the folder for inspection
     * @return {@link  ProjectManager.Result} that can be associated with the
     *         folder or an empty array if none has found.
     * @throws IllegalArgumentException
     */
   ProjectManager.Result[] checkProject(@NonNull FileObject projectDirectory) throws IllegalArgumentException;
}
