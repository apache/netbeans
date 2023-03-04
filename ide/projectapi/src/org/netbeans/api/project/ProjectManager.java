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

package org.netbeans.api.project;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.projectapi.SPIAccessor;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 * Manages loaded projects.
 * @author Jesse Glick
 * @author Tomas Zezula
 */
public final class ProjectManager {

    private static final Logger LOG = Logger.getLogger(ProjectManager.class.getName());
    private static final ProjectManager DEFAULT = new ProjectManager();
    private final ProjectManagerImplementation impl;
    
    private ProjectManager() {
        this.impl = Lookup.getDefault().lookup(ProjectManagerImplementation.class);
        if (this.impl == null) {
            throw new IllegalStateException("No ProjectManagerImplementation found in global Lookup."); //NOI18N
        }
        this.impl.init(SPIAccessor.getInstance().createProjectManagerCallBack());
        LOG.log(
            Level.FINE,
            "ProjectManager created with implementation: {0}", //NOI18N
            this.impl);
    }

    /**
     * Returns the singleton project manager instance.
     * @return the default instance
     */
    @NonNull
    public static ProjectManager getDefault() {
        return DEFAULT;
    }    

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
    public static Mutex mutex() {
        return getDefault().impl.getMutex();
    }

    /**
     * Get a read/write lock to be used for project metadata accesses.
     * The returned lock may be optimized to limit contention only on given
     * project(s).
     * @param autoSave if true the other most write operation automatically saves
     * the passed project(s)
     * @param project the project to lock
     * @param otherProjects other projects to lock
     * @return a general read/write lock for project metadata operations
     * @since 1.59
     */
    @NonNull
    public static Mutex mutex(
        final boolean autoSave,
        @NonNull Project project,
        @NonNull Project... otherProjects) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("otherProjects", otherProjects); //NOI18N
        return getDefault().impl.getMutex(autoSave, project, otherProjects);
    }
        
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
    public Project findProject(@NonNull final FileObject projectDirectory) throws IOException, IllegalArgumentException {
        if (projectDirectory == null) {
            throw new IllegalArgumentException("Attempted to pass a null directory to findProject"); // NOI18N
        }
        if (!projectDirectory.isFolder()) {
            throw new IllegalArgumentException("Attempted to pass a non-directory to findProject: " + projectDirectory); // NOI18N
        }
        return impl.findProject(projectDirectory);
    }
        
    
    /**
     * Check whether a given directory is likely to contain a project without
     * actually loading it.
     * Should be faster and use less memory than {@link #findProject} when called
     * on a large number of directories.
     * <p>The result is not guaranteed to be accurate; there may be false positives
     * (directories for which <code>isProject</code> is true but {@link #findProject}
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
     * @return true if the directory is likely to contain a project according to
     *              some registered {@link ProjectFactory}
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    public boolean isProject(@NonNull final FileObject projectDirectory) throws IllegalArgumentException {
        return isProject2(projectDirectory) != null;
    }

    /**
     * Check whether a given directory is likely to contain a project without
     * actually loading it. The returned {@link org.netbeans.api.project.ProjectManager.Result} object contains additional
     * information about the found project.
     * Should be faster and use less memory than {@link #findProject} when called
     * on a large number of directories.
     * <p>The result is not guaranteed to be accurate; there may be false positives
     * (directories for which <code>isProject2</code> is non-null but {@link #findProject}
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
     * @since org.netbeans.modules.projectapi 1.22
     */
    @CheckForNull
    public Result isProject2(@NonNull final FileObject projectDirectory) throws IllegalArgumentException {
        if (projectDirectory == null) {
            throw new IllegalArgumentException("Attempted to pass a null directory to isProject"); // NOI18N
        }
        if (!projectDirectory.isFolder() ) {
            //#78215 it can happen that a no longer existing folder is queried. throw
            // exception only for real wrong usage..
            if (projectDirectory.isValid()) {
                throw new IllegalArgumentException("Attempted to pass a non-directory to isProject: " + projectDirectory); // NOI18N
            } else {
                return null;
            }
        }
        return impl.isProject(projectDirectory);
    }
        
    /**
     * Clear the cached list of folders thought <em>not</em> to be projects.
     * This may be useful after creating project metadata in a folder, etc.
     * Cached project objects, i.e. folders that <em>are</em> known to be
     * projects, are not affected.
     */
    public void clearNonProjectCache() {
        impl.clearNonProjectCache();
        final Collection<? extends FileOwnerQueryImplementation> col = Lookup.getDefault().lookupAll(FileOwnerQueryImplementation.class);
        for (FileOwnerQueryImplementation foqi : col) {
            if (foqi instanceof SimpleFileOwnerQueryImplementation) {
                ((SimpleFileOwnerQueryImplementation)foqi).resetLastFoundReferences();
            }
        }
    }
    
    /**
     * Get a list of all projects which are modified and need to be saved.
     * <p>Acquires read access.
     * @return an immutable set of projects
     */
    @NonNull
    public Set<Project> getModifiedProjects() {
        return impl.getModifiedProjects();
    }
    
    /**
     * Check whether a given project is current modified.
     * <p>Acquires read access.
     * @param p a project loaded by this manager
     * @return true if it is modified, false if has been saved since the last modification
     */
    public boolean isModified(@NonNull final Project p) {
        return impl.isModified(p);
    }
    
    /**
     * Save one project (if it was in fact modified).
     * <p>Acquires write access.</p>
     * <p class="nonnormative">
     * Although the project infrastructure permits a modified project to be saved
     * at any time, current UI principles dictate that the "save project" concept
     * should be internal only - i.e. a project customizer should automatically
     * save the project when it is closed e.g. with an "OK" button. Currently there
     * is no UI display of modified projects; this module does not ensure that modified projects
     * are saved at system exit time the way modified files are, though the Project UI
     * implementation module currently does this check.
     * </p>
     * @param p the project to save
     * @throws IOException if it cannot be saved
     * @see ProjectFactory#saveProject
     */
    public void saveProject(@NonNull final Project p) throws IOException {
        Parameters.notNull("p", p); //NOI18N
        impl.saveProject(p);
    }
    
    /**
     * Save all modified projects.
     * <p>Acquires write access.
     * @throws IOException if any of them cannot be saved
     * @see ProjectFactory#saveProject
     */
    public void saveAllProjects() throws IOException {
        impl.saveAllProjects();
    }
    
    /**
     * Checks whether a project is still valid.
     * <p>Acquires read access.</p>
     *
     * @since 1.6
     *
     * @param p a project
     * @return true if the project is still valid, false if it has been deleted
     */
    public boolean isValid(@NonNull final Project p) {
        Parameters.notNull("p", p); //NOI18N
        return impl.isValid(p);
    }
    
    /**
     *  A result (immutable) object returned from {@link org.netbeans.api.project.ProjectManager#isProject2} method.
     *  To be created by {@link org.netbeans.spi.project.ProjectFactory2} project factories.
     *  @since org.netbeans.modules.projectapi 1.22
     */
    public static final class Result {
        private final Icon icon;
        private final String displayName;
        private final String projectType;

        public Result(Icon icon) {
            this(null, null, icon);
        }

        /**
         * C'tor.  
         * @param displayName a display name or null if not available
         * @param projectType a project type or null if not available
         * @param icon an icon or null if not available
         * @since org.netbeans.modules.projectapi 1.60
         */
        public Result(String displayName, String projectType, Icon icon) {
            this.icon = icon;
            this.displayName = displayName;
            this.projectType = projectType;
        }

        /**
          * Get a human-readable display name for the project.
          * May contain spaces, international characters, etc.
          * @return a display name for the project or null if the display name cannot be found this way.
          * @since org.netbeans.modules.projectapi 1.60
          */
        public String getDisplayName() {
            return displayName;
        }
         
        /**
         * Get the project type e.g. {@code "org-netbeans-modules-java-j2seproject"}
         * @return the project type or null if the project type cannot be found this way.
         * @since org.netbeans.modules.projectapi 1.60
         */
        public String getProjectType() {
            return projectType;
        }
         
        /**
         * Get the project icon.
         * @return project type icon for the result or null if the icon cannot be found this way.
         */
        public Icon getIcon() {
            return icon;
        }
    }
}
