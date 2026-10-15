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

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class CacheFolderArchive implements Archive, FileChangeListener {

    private volatile Archive delegate;
    private final File cache;

    public CacheFolderArchive(
            @NonNull File cache) {
        Parameters.notNull("cache", cache); //NOI18N
        this.cache = cache;
        if (cache.isDirectory()) {
            delegate = new FolderArchive(cache);
        } else {
            FileUtil.addFileChangeListener(this, cache);
            delegate = Archive.EMPTY;
        }
    }

    @Override
    public JavaFileObject getFile(String name) throws IOException {
        return delegate.getFile(name);
    }

    @Override
    public URI getDirectory(String dirName) throws IOException {
        return delegate.getDirectory(dirName);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public JavaFileObject create(String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException {
        return delegate.create(relativeName, filter);
    }

    @Override
    public boolean isMultiRelease() {
        return delegate.isMultiRelease();
    }

    @Override
    public Iterable<JavaFileObject> getFiles(String folderName, Entry entry, Set<Kind> kinds, JavaFileFilterImplementation filter, boolean recursive) throws IOException {
        return delegate.getFiles(folderName, entry, kinds, filter, recursive);
    }

    private void update() {
        if (cache.isDirectory()) {
            delegate = new FolderArchive(cache);
            FileUtil.removeFileChangeListener(this, cache);
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        update();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        update();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        update();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        update();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        update();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        update();
    }

}
