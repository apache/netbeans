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
import java.util.Collection;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.projectapi.SPIAccessor;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/**
 * The SPI class for {@link ProjectManager}.
 * @author Tomas Zezula
 * @since 1.59
 */
public interface ProjectManagerImplementation {

    /**
     * Configures {@link ProjectManagerImplementation}.
     * Called before the {@link ProjectManager} starts to use
     * the implementation.
     * @param callBack the callBack
     */
    void init(@NonNull final ProjectManagerCallBack callBack);

    /**
     * Get a read/write lock to be used for all project metadata accesses.
     * All methods relating to recognizing and loading projects, saving them,
     * getting or setting their metadata, etc. should be controlled by this
     * mutex and be marked as read operations or write operations. Unless
     * otherwise stated, project-related methods automatically acquire the
     * mutex for you, so you do not necessarily need to pay attention to it;
     * but you may directly acquire the mutex in order to ensure that a block
     * of reads does not have any interspersed writes, or in order to ensure
     * that a write is not clobbering an unrelated write, etc.
     * @return a general read/write lock for project metadata operations of all sorts
     */
    @NonNull
    Mutex getMutex();

    /**
     * Get a read/write lock to be used for project metadata accesses.
     * The returned lock may be optimized to limit contention only on given
     * project(s).
     * @param autoSave if true the other most write operation automatically saves
     * the passed project(s)
     * @param project the project to lock
     * @param otherProjects other projects to lock
     * @return a general read/write lock for project metadata operations
     */
    @NonNull
    Mutex getMutex(
        boolean autoSave,
        @NonNull Project project,
        @NonNull Project... otherProjects);


    /**
     * Find an open project corresponding to a given project directory.
     * Will be created in memory if necessary.
     * <p>
     * Acquires read access.
     * </p>
     * <p>
     * It is <em>not</em> guaranteed that the returned instance will be identical
     * to that which is created by the appropriate {@link ProjectFactory}. In
     * particular, the project manager is free to return only wrapper <code>Project</code>
     * instances which delegate to the factory's implementation. If you know your
     * factory created a particular project, you cannot safely cast the return value
     * of this method to your project type implementation class; you should instead
     * place an implementation of some suitable private interface into your project's
     * lookup, which would be safely proxied.
     * </p>
     * @param projectDirectory the project top directory
     * @return the project (object identity may or may not vary between calls)
     *         or null if the directory is not recognized as a project by any
     *         registered {@link ProjectFactory}
     *         (might be null even if {@link #isProject} returns true)
     * @throws IOException if the project was recognized but could not be loaded
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    @CheckForNull
    Project findProject(@NonNull FileObject projectDirectory) throws IOException, IllegalArgumentException;

    /**
     * Check whether a given directory is likely to contain a project without
     * actually loading it. The returned {@link org.netbeans.api.project.ProjectManager.Result} object contains additional
     * information about the found project.
     * Should be faster and use less memory than {@link #findProject} when called
     * on a large number of directories.
     * <p>The result is not guaranteed to be accurate; there may be false positives
     * (directories for which <code>isProject</code> is non-null but {@link #findProject}
     * will return null), for example if there is trouble loading the project.
     * False negatives are possible only if there are bugs in the project factory.</p>
     * <p>Acquires read access.</p>
     * <p class="nonnormative">
     * You do <em>not</em> need to call this method if you just plan to call {@link #findProject}
     * afterwards. It is intended for only those clients which would discard the
     * result of {@link #findProject} other than to check for null, and which
     * can also tolerate false positives.
     * </p>
     * @param projectDirectory a directory which may be some project's top directory
     * @return Result object if the directory is likely to contain a project according to
     *              some registered {@link ProjectFactory}, or null if not a project folder.
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    @CheckForNull
    ProjectManager.Result isProject(@NonNull FileObject projectDirectory) throws IllegalArgumentException;

    /**
     * Clear the cached list of folders thought <em>not</em> to be projects.
     * This may be useful after creating project metadata in a folder, etc.
     * Cached project objects, i.e. folders that <em>are</em> known to be
     * projects, are not affected.
     */
    void clearNonProjectCache();

    /**
     * Get a list of all projects which are modified and need to be saved.
     * <p>Acquires read access.
     * @return an immutable set of projects
     */
    @NonNull
    Set<Project> getModifiedProjects();

    /**
     * Check whether a given project is current modified.
     * <p>Acquires read access.
     * @param p a project loaded by this manager
     * @return true if it is modified, false if has been saved since the last modification
     */
    boolean isModified(@NonNull Project p);

    /**
     * Checks whether a project is still valid.
     * <p>Acquires read access.</p>
     *
     * @param p a project
     * @return true if the project is still valid, false if it has been deleted
     */
    boolean isValid(@NonNull Project p);

    /**
     * Save one project (if it was in fact modified).
     * <p>Acquires write access.</p>
     * @param p the project to save
     * @throws IOException if it cannot be saved
     * @see ProjectFactory#saveProject
     */
    void saveProject(@NonNull Project p) throws IOException;

    /**
     * Save all modified projects.
     * <p>Acquires write access.
     * @throws IOException if any of them cannot be saved
     * @see ProjectFactory#saveProject
     */
    void saveAllProjects() throws IOException;

    /**
     * Callback to notify the {@link ProjectManager} about changes.
     */
    final class ProjectManagerCallBack {

        static {
            SPIAccessor.setInstance(new SPIAccessorImpl());
        }

        private ProjectManagerCallBack(){
        }

        /**
         * Project was modified.
         * @param project the modified project
         */
        public void notifyModified(@NonNull Project project) {

        }

        /**
         * Project was deleted or renamed.
         * @param project the deleted (renamed) project
         */
        public void notifyDeleted(@NullAllowed Project project) {
            //Reset SimpleFileOwnerQueryImplementation cache
            final Collection<? extends FileOwnerQueryImplementation> col = Lookup.getDefault().lookupAll(FileOwnerQueryImplementation.class);
            for (FileOwnerQueryImplementation impl : col) {
                if (impl instanceof SimpleFileOwnerQueryImplementation) {
                    ((SimpleFileOwnerQueryImplementation)impl).resetLastFoundReferences();
                }
            }
        }

        private static final class SPIAccessorImpl extends SPIAccessor {
            @NonNull
            @Override
            public ProjectManagerCallBack createProjectManagerCallBack() {
                return new ProjectManagerCallBack();
            }
        }
    }
}
