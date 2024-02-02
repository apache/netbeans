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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Provides information on files affecting the project reload, and allows to reload project metadata.
 * The project infrastructure usually monitors the on-disk file changes and manages background project reloads.
 * But with programmatic changes to project files, it may be necessary to wait for the project reload to pick 
 * the new project's metadata. The project infrastructure may not be able to pick in-memory document changes
 * to the project settings; especially when invokes external tools such as Maven, Gradle etc. This interface
 * also allows to collect project files, that should be saved before project reload can pick fresh data.
 * @since 1.7
 * @author sdedic
 */
public interface ProjectReloadImplementation {
    /**
     * Attempts to find the set of files. It will return FileObjects representing
     * files that contain project's definition. The implementation may also indicate
     * that it needs to sync project to disk in order to do project reload. If 
     * `forProjectLoad` is true, then reported files should be saved before reloading
     * the project, otherwise the project metadata can still contain obsolete info. Note
     * that the set of files is computed from the current project's metadata, so if the
     * unsaved change contains gross changes, such pas parent POM change, the reported set
     * of files may not be complete. The report for project load may also contain
     * files from other projects.
     * <p/>
     * Implementations, that can analyze in-memory state may return an empty set for this
     * case.
     * @param forProjectLoad if true, implementation should report files that must be
     * saved before project load could load fresh information
     * @return set of project files.
     */
    public Set<FileObject>  findProjectFiles(boolean forProjectLoad);
    
    /**
     * Attempts to reload project metadata, to reflect the current project state. Note that
     * the resulting Future may report an {@link IOException} instead of a Project instance in
     * the case that the project loading fails.
     * 
     * @return a Future that will be completed when the project reloads.
     */
    public CompletableFuture<Project> reloadProject();
}
