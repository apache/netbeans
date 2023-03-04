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

package org.netbeans.modules.php.project.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.classpath.CommonPhpSourcePath;
import org.netbeans.modules.php.project.classpath.IncludePathClassPathProvider;
import org.netbeans.modules.php.project.classpath.PhpSourcePathImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Parameters;

/**
 * @author Tomas Mysik
 * @since 2.1
 */
public final class PhpSourcePath {
    public static final String BOOT_CP = "classpath/php-boot"; //NOI18N
    /**
     * @since 2.71
     */
    public static final String PROJECT_BOOT_CP = "classpath/php-project-boot"; //NOI18N
    public static final String SOURCE_CP = "classpath/php-source"; //NOI18N

    private static final DefaultPhpSourcePath DEFAULT_PHP_SOURCE_PATH = new DefaultPhpSourcePath();
    // @GuardedBy(PhpSourcePath.class)
    private static FileObject phpStubsFolder = null;

    /**
     * Possible types of a file.
     */
    public static enum FileType {
        /** Internal files (signature files). */
        INTERNAL,
        /** PHP include path. */
        INCLUDE,
        /** Project sources. */
        SOURCE,
        /** Project test sources. */
        TEST,
        /** Unknown file type. */
        UNKNOWN,
    }

    private PhpSourcePath() {
    }

    /**
     * Get the file type for the given file object.
     * @param file the input file.
     * @return the file type for the given file object.
     * @see FileType
     */
    public static FileType getFileType(FileObject file) {
        Parameters.notNull("file", file);

        // #221482, #165738
        // check internal files (perhaps the most common use case)
        if (org.netbeans.modules.php.project.util.PhpProjectUtils.isInternalFile(file)) {
            return FileType.INTERNAL;
        }
        // then, check sources (typical use-case)
        PhpSourcePathImplementation phpSourcePath = getPhpSourcePathForProjectFile(file);
        if (phpSourcePath != null) {
            return phpSourcePath.getFileType(file);
        }
        // lastly, check classpath for project's specific include path (known to be very slow)
        FileType fileType = getFileTypeFromIncludeClassPath(file);
        if (fileType != null) {
            return fileType;
        }
        // perhaps a file without a project or a file on global include path
        // in fact, this is not supported by the editor yet (model does not work for a file without a project)
        return DEFAULT_PHP_SOURCE_PATH.getFileType(file);
    }

    /**
     * Get list of folders, where asignatures file for PHP runtime are.
     * These files are also preindexed.
     * @return list of folders
     */
    public static synchronized List<FileObject> getPreindexedFolders() {
        if (phpStubsFolder == null) {
            // Core classes: Stubs generated for the "builtin" php runtime and extenstions.
            File clusterFile = InstalledFileLocator.getDefault().locate("docs/phpsigfiles.zip", "org.netbeans.modules.php.project", true); //NOI18N

            if (clusterFile != null) {
                FileObject root = FileUtil.getArchiveRoot(FileUtil.toFileObject(clusterFile));
                phpStubsFolder = root.getFileObject("phpstubs/phpruntime"); //NOI18N
            }
        }
        if (phpStubsFolder == null) {
            // during tests
            return Collections.emptyList();
        }
        return Collections.singletonList(phpStubsFolder);
    }

    /**
     * Get all the possible path roots from PHP include path for the given file. If the file equals <code>null</code> then
     * just global PHP include path is returned.
     * @param file a file which could belong to a project or <code>null</code> for gettting global PHP include path.
     * @return all the possible path roots from PHP include path.
     */
    public static List<FileObject> getIncludePath(FileObject file) {
        if (file == null) {
            return DEFAULT_PHP_SOURCE_PATH.getIncludePath();
        }
        PhpSourcePathImplementation phpSourcePath = getPhpSourcePathForProjectFile(file);
        if (phpSourcePath != null) {
            return phpSourcePath.getIncludePath();
        }
        return DEFAULT_PHP_SOURCE_PATH.getIncludePath();
    }

    /**
     * Resolve absolute path for the given file name. The order is the given directory then PHP include path.
     * @param directory the directory to which the PHP <code>include()</code> or <code>require()</code> functions
     *                  could be resolved. Typically the directory containing the given script.
     * @param fileName a file name or a relative path delimited by '/'.
     * @return resolved file path or <code>null</code> if the given file is not found.
     */
    public static FileObject resolveFile(FileObject directory, String fileName) {
        Parameters.notNull("directory", directory);
        Parameters.notNull("fileName", fileName);
        if (!directory.isFolder()) {
            throw new IllegalArgumentException("valid directory needed");
        }

        PhpSourcePathImplementation phpSourcePath = getPhpSourcePathForProjectFile(directory);
        if (phpSourcePath != null) {
            return phpSourcePath.resolveFile(directory, fileName);
        }
        return DEFAULT_PHP_SOURCE_PATH.resolveFile(directory, fileName);
    }

    private static PhpSourcePathImplementation getPhpSourcePathForProjectFile(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        PhpSourcePathImplementation phpSourcePath = project.getLookup().lookup(PhpSourcePathImplementation.class);
        // XXX disabled because of runtime.php underneath nbbuild directory
        //assert phpSourcePath != null : "Not PHP project (interface PhpSourcePath not found in lookup)! [" + project + "]";
        return phpSourcePath;
    }

    private static FileType getFileTypeFromIncludeClassPath(FileObject file) {
        // now, check include path of opened projects
        ClassPath classPath = IncludePathClassPathProvider.findProjectIncludePath(file);
        if (classPath != null && classPath.contains(file)) {
            // internal?
            if (org.netbeans.modules.php.project.util.PhpProjectUtils.isInternalFile(file)) {
                return FileType.INTERNAL;
            }
            // include
            return FileType.INCLUDE;
        }
        return null;
    }

    // PhpSourcePathImplementation implementation for file which does not belong to any project
    private static class DefaultPhpSourcePath implements org.netbeans.modules.php.project.classpath.PhpSourcePathImplementation {

        @Override
        public FileType getFileType(FileObject file) {
            if (org.netbeans.modules.php.project.util.PhpProjectUtils.isInternalFile(file)) {
                return FileType.INTERNAL;
            }
            for (FileObject dir : getPlatformPath()) {
                if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                    return FileType.INCLUDE;
                }
            }
            return FileType.UNKNOWN;
        }

        @Override
        public List<FileObject> getIncludePath() {
            return new ArrayList<>(getPlatformPath());
        }

        @Override
        public FileObject resolveFile(FileObject directory, String fileName) {
            FileObject resolved = directory.getFileObject(fileName);
            if (resolved != null) {
                return resolved;
            }
            for (FileObject dir : getPlatformPath()) {
                resolved = dir.getFileObject(fileName);
                if (resolved != null) {
                    return resolved;
                }
            }
            return null;
        }

        // XXX cache?
        private List<FileObject> getPlatformPath() {
            String[] paths = PhpOptions.getInstance().getPhpGlobalIncludePathAsArray();
            List<FileObject> internalPath = CommonPhpSourcePath.getInternalPath();
            List<FileObject> dirs = new ArrayList<>(paths.length + internalPath.size());
            dirs.addAll(internalPath);
            for (String path : paths) {
                FileObject resolvedFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(path)));
                if (resolvedFile != null) { // XXX check isValid() as well?
                    dirs.add(resolvedFile);
                }
            }
            return dirs;
        }
    }
}
