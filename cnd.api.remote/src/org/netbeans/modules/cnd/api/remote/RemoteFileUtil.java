/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.remote;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 */
public class RemoteFileUtil {

    /**
     * Checks whether file exists or not
     * @param absolutePath - should be ABSOLUTE, but not necessarily normalized
     */
    public static boolean fileExists(String absolutePath, ExecutionEnvironment executionEnvironment) {
        FileObject fo = getFileObject(normalizeAbsolutePath(absolutePath, executionEnvironment), executionEnvironment);
        return (fo != null && fo.isValid());
    }

    public static boolean isDirectory(String absolutePath, ExecutionEnvironment executionEnvironment) {
        FileObject fo = getFileObject(absolutePath, executionEnvironment);
        return (fo != null && fo.isFolder());
    }

    /**
     * In many places, standard sequence is as follows:
     *  - convert path to absolute if need
     *  - normalize it
     *  - find file object
     * In the case of non-local file systems we should delegate it to correspondent file systems.
     */
    public static FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        FileObject result = FileSystemProvider.getFileObject(baseFileObject, relativeOrAbsolutePath);
        if (result == null) {
            String absRootPath = CndPathUtilities.toAbsolutePath(baseFileObject, relativeOrAbsolutePath);
            try {
                // XXX:fullRemote we use old logic for local and new for remote
                // but remote approach for local gives #197093 -  Exception: null file
                final FileSystem fs = baseFileObject.getFileSystem();
                if (CndFileUtils.isLocalFileSystem(fs)) {
                    result = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(absRootPath));
                } else {
                    result = InvalidFileObjectSupport.getInvalidFileObject(fs, absRootPath);
                }
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                result = InvalidFileObjectSupport.getInvalidFileObject(InvalidFileObjectSupport.getDummyFileSystem(), absRootPath);
            }
        }
        return result;
    }

    private RemoteFileUtil() {}

    public static FileObject getFileObject(String absolutePath, ExecutionEnvironment execEnv) {
        CndUtils.assertAbsolutePathInConsole(absolutePath, "path for must be absolute"); //NOI18N
        if (execEnv.isRemote()) {
            if (CndUtils.isDebugMode()) {
                String normalizedPath = normalizeAbsolutePath(absolutePath, execEnv);
                if (! normalizedPath.equals(absolutePath)) {
                    CndUtils.assertTrueInConsole(false, "Warning: path is not normalized:  absolute path is _" + absolutePath + "_ normailzed path is _"  + normalizedPath + "_");
                }
                //absolutePath = normalizedPath;
            }
            return FileSystemProvider.getFileSystem(execEnv).findResource(absolutePath); //NOI18N
        } else {
            return CndFileUtils.toFileObject(absolutePath);
        }
    }

    public static FileSystem getProjectSourceFileSystem(Lookup.Provider project) {
        if (project != null) {
            RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
            if (rp == null) {
                return null;
            }
            FileObject projectDir = rp.getSourceBaseDirFileObject();
            if (projectDir != null) {
                try {
                return projectDir.getFileSystem();
                } catch (FileStateInvalidException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return CndFileUtils.getLocalFileSystem();
    }
    
    public static FileObject getProjectSourceBaseFileObject(Lookup.Provider project) {
        if (project != null) {
            RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
            if (rp == null) {
                return null;
            }
            return rp.getSourceBaseDirFileObject();
        }
        return null;
    }

    public static ExecutionEnvironment getProjectSourceExecutionEnvironment(Project project) {
        if (project != null) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                return remoteProject.getSourceFileSystemHost();
            }
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    // it should take not-normalized path ok, since the caller can not normalize
    // because it does not know execution environment
    public static FileObject getFileObject(String absolutePath, Project project) {
        ExecutionEnvironment execEnv = getProjectSourceExecutionEnvironment(project);
        absolutePath = FileSystemProvider.normalizeAbsolutePath(absolutePath, execEnv);
        if (execEnv != null && execEnv.isRemote()) {
            return getFileObject(absolutePath, execEnv);
        }
        FileObject projectDir = project.getProjectDirectory();
        CndUtils.assertNotNull(projectDir, "Null project dir for ", project); //NOI18N
        final FileSystem fs;
        try {
            fs = projectDir.getFileSystem();            
            return fs.findResource(absolutePath);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static String normalizeAbsolutePath(String absPath, Project project) {
        ExecutionEnvironment execEnv = getProjectSourceExecutionEnvironment(project);
        if (execEnv != null && execEnv.isRemote()) {
            return normalizeAbsolutePath(absPath, execEnv);
        } else {
            return CndFileUtils.normalizeAbsolutePath(absPath);
        }

    }

    public static String normalizeAbsolutePath(String absPath, ExecutionEnvironment execEnv) {
        if (execEnv.isRemote()) {
            return FileSystemProvider.normalizeAbsolutePath(absPath, execEnv);
        } else {
            return FileUtil.normalizePath(absPath);
        }
    }

    public static String getAbsolutePath(FileObject fileObject) {
        return fileObject.getPath();
    }

    public static String getCanonicalPath(FileObject fo) throws IOException {
        //XXX:fullRemote
        if (FileSystemProvider.getExecutionEnvironment(fo).isLocal()) {
            File file = FileUtil.toFile(fo);
            return (file == null) ? fo.getPath() : file.getCanonicalPath();
        } else {
            FileObject file = FileSystemProvider.getCanonicalFileObject(fo);
            return (file == null) ? fo.getPath() : file.getPath();
        }
    }

    public static boolean isRemote(FileSystem fs) {
        if (fs != null) {
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
            return (env == null) ? false : env.isRemote();
        }
        return false;
    }
    
    /**
     * Returns the folder last used for creating a new project.
     * @return File the folder
     */
    public static String getProjectsFolder(ExecutionEnvironment env) {
        Preferences pref = NbPreferences.forModule(RemoteFileUtil.class);
        String envID = ExecutionEnvironmentFactory.toUniqueID(env);
        return pref.get("ProjectPath"+envID, null); // NOI18N
    }
    /**
     * Sets the folder last used for creating a new project.
     * @param folder The folder to be set as last used. Must not be null
     */
    public static void setProjectsFolder(String folder, ExecutionEnvironment env) {
        Preferences pref = NbPreferences.forModule(RemoteFileUtil.class);
        String envID = ExecutionEnvironmentFactory.toUniqueID(env);
        pref.put("ProjectPath"+envID, folder); // NOI18N
    }

}
