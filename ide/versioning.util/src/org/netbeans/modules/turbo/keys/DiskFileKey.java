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

package org.netbeans.modules.turbo.keys;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.ErrorManager;

import java.io.File;

/**
 * Key for FileObject with identity given by disk files.
 * It means that keys can be equal for non-equal FileObjects.
 *
 * @author Petr Kuzel
 */
public final class DiskFileKey {
    private final FileObject fileObject;
    private final int hashCode;
    private String absolutePath;


    public static DiskFileKey createKey(FileObject fo) {
        return new DiskFileKey(fo);
    }

    private DiskFileKey(FileObject fo) {

        // PERFORMANCE optimalization, it saves memory because elimintes nedd for creating absolute paths.
        // XXX unwrap from MasterFileSystem, hidden dependency on "VCS-Native-FileObject" attribute knowledge
        // Unfortunately MasterFileSystem API does not support generic unwrapping.
        FileObject nativeFileObject = (FileObject) fo.getAttribute("VCS-Native-FileObject");  // NOI18N
        if (nativeFileObject == null) nativeFileObject = fo;


        fileObject = fo;
        hashCode = fo.getNameExt().hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof DiskFileKey) {

            DiskFileKey key = (DiskFileKey) o;

            if (hashCode != key.hashCode) return false;
            FileObject fo2 = key.fileObject;
            FileObject fo = fileObject;

            if (fo == fo2) return true;

            try {
                FileSystem fs = fo.getFileSystem();
                FileSystem fs2 = fo2.getFileSystem();
                if (fs.equals(fs2)) {
                    return fo.equals(fo2);
                } else {
                    // fallback use absolute paths (cache them)
                    if (absolutePath == null) {
                        File f = FileUtil.toFile(fo);
                        absolutePath = f.getAbsolutePath();
                    }
                    if (key.absolutePath == null) {
                        File f2 = FileUtil.toFile(fo2);
                        key.absolutePath = f2.getAbsolutePath();
                    }
                    return absolutePath.equals(key.absolutePath);
                }
            } catch (FileStateInvalidException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
        }
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        if (absolutePath != null) {
            return absolutePath;
        }
        return fileObject.toString();
    }
}
