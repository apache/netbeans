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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.io.File;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 */
public abstract class FileProxy {

    public abstract String getAbsolutePath();
    public abstract boolean exists();
    public abstract FileProxy getChild(String childPath);
    public abstract FileProxy getParentFile();
    public abstract boolean canWrite();


    private FileProxy() { // disallow external creation
    }

    public static FileProxy createAbsolute(FileProxy file, String absPath) {
        Parameters.notNull("file", file);
        if (file instanceof FileProxyFile) {
            return new FileProxyFile(new File(absPath));
        } else if (file instanceof FileProxyFileObject) {
            return createAbsolute(((FileProxyFileObject) file).getBase(), absPath);
        } else {
            throw new IllegalArgumentException("Unexpected FileProxy class: " + file.getClass().getName()); // NOI18N
        }
    }

    public static FileProxy createAbsolute(FileObject fo, String absPath) {
        File file = FileUtil.toFile(fo);
        if (file == null) {
            try {
                return new FileProxyFileObject(fo.getFileSystem().getRoot(), absPath);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                // I don't know how this is possible; but to avoid NPE let's return FileProxyFile
                return new FileProxyFile(file);
            }
        } else {
            return new FileProxyFile(file);
        }
    }

    public static FileProxy create(FileObject fo) {
        File file = FileUtil.toFile(fo);
        return (file == null) ? new FileProxyFileObject(fo, "") : new FileProxyFile(file); //NOI18N
    }

    public static FileProxy createRelative(FileObject fo, String relPath) {
        File file = FileUtil.toFile(fo);
        if (file == null) {
            return new FileProxyFileObject(fo, relPath == null ? "" : relPath); //NOI18N
        } else {
            if (isEmptyPath(relPath)) {
                return new FileProxyFile(file);
            } else {
                return new FileProxyFile(new File(file, relPath));
            }
        }
    }

    public static FileProxy createAbsolute(Project project, String path) {
        FileObject projectFO = project.getProjectDirectory();
        File projectFile = FileUtil.toFile(projectFO);
        if (projectFile == null) {
            try {
                return new FileProxyFileObject(projectFO.getFileSystem().getRoot(), path);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                // I don't know how this is possible; but to avoid NPE let's return FileProxyFile
                return new FileProxyFile(new File(path));
            }
        } else {
            return new FileProxyFile(new File(path));
        }
    }

    private static boolean isEmptyPath(String path) {
        return path == null || path.isEmpty() || path.equals("/") || path.equals("."); // NOI18N
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + getAbsolutePath();
    }

    private static class FileProxyFileObject extends FileProxy {

        private final FileObject base;
        private final String relPath;

        public FileProxyFileObject(FileObject fo, String relPath) {
            this.base = fo;
            this.relPath = relPath;
        }

        @Override
        public String getAbsolutePath() {
            String basePath = base.getPath();
            if (!basePath.endsWith("/") && !relPath.startsWith("/")) { // NOI18N
                return base.getPath() + (basePath.endsWith("/") ? "" : "/" ) + relPath; // NOI18N
            } else {
                return base.getPath() + relPath;
            }
        }

        @Override
        public boolean exists() {
            FileObject fo = base.getFileObject(relPath);
            return fo != null && fo.isValid();
        }

        @Override
        public boolean canWrite() {
            FileObject fo = base.getFileObject(relPath);
            return fo != null && fo.canWrite();
        }

        @Override
        public FileProxy getParentFile() {
            if (isEmptyPath(relPath)) {
                FileObject parent = base.getParent();
                return (parent == null) ? null : new FileProxyFileObject(parent, ""); //NOI18N
            }
            String parentPath = getParentPath(relPath);
            if (isEmptyPath(relPath)) {
                return new FileProxyFileObject(base, ""); //NOI18N
            } else {
                return new FileProxyFileObject(base, parentPath);
            }
        }

        private String getParentPath(String path) {
            if (path != null) {
                if (path.endsWith("/")) { //NOI18N
                    path = path.substring(0, path.length() - 1);
                }
                // ignore trailing slashes
                int fromIndex = path.length() - 1;
                while (fromIndex >= 0 && path.charAt(fromIndex) == '/') {
                    fromIndex--;
                }
                int sep = path.lastIndexOf('/', fromIndex);
                if (sep != -1) {
                    return path.substring(0, sep);
                }
            }
            return null;
        }

        @Override
        public FileProxy getChild(String childPath) {
            return new FileProxyFileObject(base, relPath + '/' + childPath);
        }

        private FileObject getBase() {
            return base;
        }
    }

    private static class FileProxyFile extends FileProxy {

        private final File file;

        public FileProxyFile(File file) {
            this.file = file;
        }

        @Override
        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        @Override
        public boolean exists() {
            return file.exists();
        }



        @Override
        public FileProxy getParentFile() {
            File parentFile = file.getParentFile();
            return (parentFile == null) ? null : new FileProxyFile(parentFile);
        }

        @Override
        public FileProxy getChild(String childPath) {
            return new FileProxyFile(new File(file, childPath));
        }

        @Override
        public boolean canWrite() {
            return file.canWrite();
        }
    }
}
