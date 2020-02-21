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

package org.netbeans.modules.cnd.utils;

import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;  

/**
 * A FileSystem / path pair
 */
public final class FSPath {

    /**
     * Converts a FileObject to FSPath
     * NB: throws IllegalStateException if FileObject.getFileSystem throws FileStateInvalidException!
     * @param fo
     * @throws IllegalStateException if FileObject.getFileSystem throws FileStateInvalidException
     * @return 
     */
    public static FSPath toFSPath(FileObject fo) {
        try {
            return new FSPath(fo.getFileSystem(), fo.getPath());
        } catch (FileStateInvalidException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private final FileSystem fileSystem;
    private final String path;

    public FSPath(FileSystem fileSystem, String path) {
        this.fileSystem = fileSystem;
        this.path = CndFileUtils.normalizeAbsolutePath(fileSystem, path);
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public String getPath() {
        return path;
    }
    
    public FileObject getFileObject() {
        return fileSystem.findResource(path);
    }
    
    public FSPath getParent() {
        String parentPath = PathUtilities.getDirName(path);
        return (parentPath == null) ? null : new FSPath(fileSystem, parentPath);
    }
    
    public FSPath getChild(String childName) {
        return new FSPath(fileSystem, path + CndFileUtils.getFileSeparatorChar(fileSystem) + childName);
    }

    public CharSequence getURL() {
        return CndFileSystemProvider.toUrl(this);
    }

    @Override
    public String toString() {
        return "" + fileSystem + ':' + path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FSPath other = (FSPath) obj;
        if (this.fileSystem != other.fileSystem && (this.fileSystem == null || !this.fileSystem.equals(other.fileSystem))) {
            return false;
        }
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.fileSystem != null ? this.fileSystem.hashCode() : 0);
        hash = 97 * hash + (this.path != null ? this.path.hashCode() : 0);
        return hash;
    }    
}
