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
package org.netbeans.modules.javascript.cdnjs;

import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Miscellaneous library utility methods.
 */
public final class LibraryUtils {

    private static final String DEFAULT_LIBRARY_FOLDER = "js/libs"; // NOI18N
    private static final String PREFERENCES_LIBRARY_FOLDER = "js.libs.folder"; // NOI18N


    private LibraryUtils() {
        assert false;
    }

    /**
     * Checks whether the given library is broken.
     * <p>
     * It means that if the library already is part of the given object,
     * its local files must exist on the disk otherwise it is broken.
     * @param project project to be used
     * @param library library to be checked
     * @return {@code true} if the given library is broken.
     */
    public static boolean isBroken(Project project, Library.Version library) {
        assert project != null;
        assert library != null;
        if (!library.isPersisted()) {
            return false;
        }
        String[] localFiles = library.getLocalFiles();
        if (localFiles == null) {
            return true;
        }
        FileObject projectDirectory = project.getProjectDirectory();
        for (String localFile : localFiles) {
            FileObject file = projectDirectory.getFileObject(localFile);
            if (file == null
                    || !file.isValid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets web root of the given project. If web root does not exist,
     * project directory is returned.
     * @param project project to be used
     * @return web root of the given project or project directory.
     */
    @NonNull
    public static File getWebRoot(Project project) {
        for (FileObject webRoot : ProjectWebRootQuery.getWebRoots(project)) {
            return FileUtil.toFile(webRoot);
        }
        return FileUtil.toFile(project.getProjectDirectory());
    }

    /**
     * Returns the library folder for the given project.
     *
     * @param project project whose library folder should be returned.
     * @return library folder for the given project.
     */
    public static String getLibraryFolder(Project project) {
        return getProjectPreferences(project).get(PREFERENCES_LIBRARY_FOLDER, DEFAULT_LIBRARY_FOLDER);
    }

    /**
     * Store the library folder for the given project.
     *
     * @param project project whose library folder should be stored.
     * @param libraryFolder library folder to store.
     */
    public static void storeLibraryFolder(Project project, String libraryFolder) {
        getProjectPreferences(project).put(PREFERENCES_LIBRARY_FOLDER, libraryFolder);
    }

    private static Preferences getProjectPreferences(Project project) {
        // Using class from web.clientproject.api for backward compatibility
        return ProjectUtils.getPreferences(project, WebClientProjectConstants.class, true);
    }

}
