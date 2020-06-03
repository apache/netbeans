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
package org.netbeans.modules.dlight.libs.common.invalid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public class InvalidFileObject extends FileObject {
    
    private final FileSystem fileSystem;
    private final String path;
    private static Date lastModifiedDate = new Date();

    public InvalidFileObject(FileSystem fileSystem, String path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    private String fileNotFoundExceptionMessage() {
        return "File not found: " + path; //NOI18N
    }

    @Override
    public void addFileChangeListener(FileChangeListener fcl) {
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @Override
    public void delete(FileLock lock) throws IOException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @Override
    public Object getAttribute(String attrName) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributes() {
        return Collections.<String>enumeration(Collections.<String>emptyList());
    }

    @Override
    public FileObject[] getChildren() {
        return new FileObject[0];
    }

    @Override
    public String getExt() {
        int pos = path.lastIndexOf('.'); //NOI18N
        if (pos == 0 || pos == -1) {
            return ""; //NOI18N
        } else {
            return path.subSequence(pos + 1, path.length()).toString();
        }
    }

    @Override
    public FileObject getFileObject(String name, String ext) {
        return null;
    }

    @Override
    public FileSystem getFileSystem() throws FileStateInvalidException {
        return fileSystem;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @Override
    public String getName() {
        String nameExt = getNameExt();
        int pos = nameExt.lastIndexOf('.'); //NOI18N
        if (pos == 0 || pos == -1) {
            return nameExt; //NOI18N
        } else {
            return nameExt.substring(0, pos);
        }
    }

    @Override
    public String getNameExt() {
        return PathUtilities.getBaseName(path.toString());
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @Override
    public FileObject getParent() {
        if (path.length() > 0) {
            if (isRoot()) {
                return null;
            }
            String parentPath = PathUtilities.getDirName(path);
            if (parentPath == null) {
                // should there be an assertion? it's just an invalid file object...
                //CndUtils.assertTrueInConsole(false, getClass().getSimpleName() + ": should be root, but it isn't"); //NOI18N
                return null;
            } else {
                FileObject parentFO = fileSystem.findResource(parentPath);
                return (parentFO != null) ? parentFO : InvalidFileObjectSupport.getInvalidFileObject(fileSystem, parentPath);
            }
        } else {
            return null;
        }
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public boolean isData() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @SuppressWarnings(value = "deprecation")
    @Override
    @Deprecated
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return path.length() == 0;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Date lastModified() {
        return lastModifiedDate;
    }

    @Override
    public FileLock lock() throws IOException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @Override
    public void removeFileChangeListener(FileChangeListener fcl) {
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        throw new FileNotFoundException(fileNotFoundExceptionMessage());
    }

    @SuppressWarnings(value = "deprecation")
    @Override
    @Deprecated
    public void setImportant(boolean b) {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InvalidFileObject other = (InvalidFileObject) obj;
        if (this.fileSystem != other.fileSystem && (this.fileSystem == null || !this.fileSystem.equals(other.fileSystem))) {
            return false;
        }
        if (this.path != other.path && (this.path == null || !this.path.equals(other.path))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.fileSystem != null ? this.fileSystem.hashCode() : 0);
        hash = 11 * hash + (this.path != null ? this.path.hashCode() : 0);
        return hash;
    }
    
}
