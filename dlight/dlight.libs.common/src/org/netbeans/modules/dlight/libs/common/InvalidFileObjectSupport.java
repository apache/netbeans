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

package org.netbeans.modules.dlight.libs.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.dlight.libs.common.invalid.InvalidFileObject;

/**
 *
 */
public class InvalidFileObjectSupport {

    public static FileObject getInvalidFileObject(FileSystem fileSystem, CharSequence path) {
        InvalidFileObjectSupport instance;
        synchronized (instances) {
            instance = instances.get(fileSystem);
            if (instance == null) {
                instance = new InvalidFileObjectSupport(fileSystem);
                instances.put(fileSystem, instance);
            }
        }
        return instance.getInvalidFileObject(path);
    }

    public static FileObject getInvalidFileObject(File file) {
        file = FileUtil.normalizeFile(file);
        return getInvalidFileObject(getFileSystem(file), file.getAbsolutePath());
    }
    
    private static FileSystem getFileSystem(File file) {
        // NB: file should be normalized!
//        while (file != null) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                return fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                return getDummyFileSystem();
            }
        }
//            file = FileUtil.normalizeFile(file.getParentFile());
//        }
        return getDummyFileSystem();
    }

    public static FileSystem getDummyFileSystem() {
        return dummyFileSystem;
    }

    private InvalidFileObjectSupport(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    private FileObject getInvalidFileObject(CharSequence path) {
        // From performance perspective it's a pity that we need to use toString()
        // But anyhow InvalidFileObject will do so, so it does not matter here
        String normPath = PathUtilities.normalizeUnixPath(path.toString().replace('\\', '/')); // NOI18N
        synchronized (this) {
            if (DLightLibsCommonLogger.isDebugMode() && new File(normPath.toString()).exists()) {
                DLightLibsCommonLogger.getInstance().log(Level.INFO, "Creating an invalid file object for existing file {0}", path);
            }
            FileObject fo = fileObjects.putIfAbsent(new InvalidFileObject((fileSystem == null) ? InvalidFileObjectSupport.dummyFileSystem : fileSystem, normPath));
            return fo;
        }
    }
    
    
    private final FileSystem fileSystem;
    private final WeakSet<FileObject> fileObjects = new WeakSet<FileObject>();

    private static final Map<FileSystem, InvalidFileObjectSupport> instances = new WeakHashMap<FileSystem, InvalidFileObjectSupport>();
    private static final DummyFileSystem dummyFileSystem = new DummyFileSystem();
    
    private static class DummyFileSystem extends FileSystem {

        @Override
        public FileObject findResource(String name) {
            return InvalidFileObjectSupport.getInvalidFileObject(this, name);
        }

        @Override
        public String getDisplayName() {
            return "Dummy"; //NOI18N
        }

        @Override
        public FileObject getRoot() {
            return InvalidFileObjectSupport.getInvalidFileObject(this, "");
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

}
