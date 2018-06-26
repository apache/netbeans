/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
