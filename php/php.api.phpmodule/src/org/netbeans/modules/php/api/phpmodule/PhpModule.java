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

package org.netbeans.modules.php.api.phpmodule;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * This class could be useful for extending a PHP project.
 * @since 2.32
 */
public interface PhpModule extends Lookup.Provider {

    /**
     * Property for frameworks.
     * @see #propertyChanged(PropertyChangeEvent)
     */
    String PROPERTY_FRAMEWORKS = "PROPERTY_FRAMEWORKS"; // NOI18N

    /**
     * Get name (identifier) of the PHP module.
     * @return name (identifier) of the PHP module
     * @see #getDisplayName()
     * @see org.netbeans.api.project.ProjectInformation#getName
     */
    @NonNull
    String getName();

    /**
     * Get display name of the PHP module.
     * @return display name of the PHP module
     * @see #getName()
     * @see org.netbeans.api.project.ProjectInformation#getDisplayName
     */
    @NonNull
    String getDisplayName();

    /**
     * CHeck whether the PHP module is broken (e.g. missing Source Files).
     * @return {@code true} if the PHP module is broken, {@code false} otherwise
     */
    boolean isBroken();

    /**
     * Get the project directory for this PHP module.
     * @return the project directory, never <code>null</code>
     */
    @NonNull
    FileObject getProjectDirectory();

    /**
     * Get the source directory of this PHP module.
     * @return the source directory, <b>can be {@code null} or
     *         {@link org.openide.filesystems.FileObject#isValid() invalid} if the project is {@link #isBroken() broken}.</b>
     */
    @CheckForNull
    FileObject getSourceDirectory();

    /**
     * Get the test directory of this PHP module for the given file.
     * @param file file to get test directory for, can be {@code null} (in such case,
     *        simply the first test directory is returned)
     * @return the test directory, can be {@code null} if no test directory set yet
     * @see #getTestDirectories()
     */
    @CheckForNull
    FileObject getTestDirectory(@NullAllowed FileObject file);

    /**
     * Get all test directories of this PHP module.
     * @return list of test directories, can be empty but never {@code null}
     * @see #getTestDirectory(FileObject)
     */
    List<FileObject> getTestDirectories();

    /**
     * Get any optional abilities of this PHP module.
     * @return a set of abilities
     */
    @Override
    Lookup getLookup();

    /**
     * Get {@link Preferences} of this PHP module.
     * This method is suitable for storing (and reading) PHP module specific properties.
     * For more information, see {@link org.netbeans.api.project.ProjectUtils#getPreferences(org.netbeans.api.project.Project, Class, boolean)}.
     * @param clazz a class which defines the namespace of preferences
     * @param shared whether the returned settings should be shared
     * @return {@link Preferences} for this PHP module and the given class
     * @see org.netbeans.api.project.ProjectUtils#getPreferences(org.netbeans.api.project.Project, Class, boolean)
     */
    @NonNull
    Preferences getPreferences(Class<?> clazz, boolean shared);

    /**
     * A way for informing PHP module that something has changed.
     * @param propertyChangeEvent property change event
     * @see #PROPERTY_FRAMEWORKS
     */
    void notifyPropertyChanged(@NonNull PropertyChangeEvent propertyChangeEvent);

    //~ Factories

    /**
     * Samoe useful factory methods for getting PHP module.
     */
    public static final class Factory {

        /**
         * Gets PHP module for the given {@link FileObject}.
         * @param fo {@link FileObject} to get PHP module for
         * @return PHP module or <code>null</code> if not found
         */
        @CheckForNull
        public static PhpModule forFileObject(FileObject fo) {
            Parameters.notNull("fo", fo); // NOI18N
            Project project = FileOwnerQuery.getOwner(fo);
            if (project == null) {
                return null;
            }
            return lookupPhpModule(project);
        }

        /**
         * Infers PHP module - from the currently selected top component, open projects etc.
         * @return PHP module or <code>null</code> if not found.
         */
        @CheckForNull
        public static PhpModule inferPhpModule() {
            // try current context firstly
            Node[] activatedNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (activatedNodes != null) {
                for (Node n : activatedNodes) {
                    PhpModule result = lookupPhpModule(n.getLookup());
                    if (result != null) {
                        return result;
                    }
                }
            }

            Lookup globalContext = Utilities.actionsGlobalContext();
            PhpModule result = lookupPhpModule(globalContext);
            if (result != null) {
                return result;
            }
            FileObject fo = globalContext.lookup(FileObject.class);
            if (fo != null) {
                result = forFileObject(fo);
                if (result != null) {
                    return result;
                }
            }

            // next try main project
            OpenProjects projects = OpenProjects.getDefault();
            Project mainProject = projects.getMainProject();
            if (mainProject != null) {
                result = lookupPhpModule(mainProject);
                if (result != null) {
                    return result;
                }
            }

            // next try other opened projects
            for (Project project : projects.getOpenProjects()) {
                result = lookupPhpModule(project);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        /**
         * Get {@link PhpModule PHP module} from the given project.
         * @param project a PHP project where to look for a PHP module for
         * @return PHP module or {@code null} if not found
         * @see 1.38
         */
        @CheckForNull
        public static PhpModule lookupPhpModule(Project project) {
            Parameters.notNull("project", project);

            return project.getLookup().lookup(PhpModule.class);
        }

        /**
         * Get {@link PhpModule PHP module} from the given lookup.
         * @param lookup a lookup where to look for a PHP module for
         * @return PHP module or {@code null} if not found
         * @see 1.38
         */
        @CheckForNull
        public static PhpModule lookupPhpModule(Lookup lookup) {
            Parameters.notNull("lookup", lookup);

            // try directly
            PhpModule result = lookup.lookup(PhpModule.class);
            if (result != null) {
                return result;
            }
            // try through Project instance
            Project project = lookup.lookup(Project.class);
            if (project == null) {
                return null;
            }
            return lookupPhpModule(project);
        }

    }

}
