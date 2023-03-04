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

import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;

/**
 * Create in-memory projects from disk directories.
 * Instances should be registered into default lookup as ProjectFactory instances.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.22
 *
 */
public interface ProjectFactory2 extends ProjectFactory {

    /**
     * Test whether a given directory probably refers to a project recognized by this factory
     * without actually trying to create it.
     * <p>Should be as fast as possible as it might be called sequentially on a
     * lot of directories.</p>
     * <p>Need not be definite; it is permitted to return null or throw an exception
     * from {@link #loadProject} even when returning <code>Result</code> instance from this
     * method, in case the directory looked like a project directory but in fact
     * had something wrong with it.</p>
     * <p>Will be called inside read access.</p>
     * @param projectDirectory a directory which might refer to a project
     * @return Result instance if this factory recognizes it, or null if the directory is not recognized
     */
    ProjectManager.Result isProject2(FileObject projectDirectory);

}
