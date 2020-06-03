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

package org.netbeans.modules.remote.impl.fs;

import java.lang.ref.WeakReference;
import java.util.*;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FileProxyI;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 */
final class RecursiveListener extends WeakReference<FileObject>
implements FileChangeListener {
    private final FileChangeListener fcl;
    private final Set<FileObject> kept;

    public RecursiveListener(RemoteFileObject source, FileChangeListener fcl, boolean keep) {
        super(source);
        this.fcl = fcl;
        this.kept = keep ? new HashSet<FileObject>() : null;
        addAll(source);
        try {
            init(source, -1, false);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void init(RemoteFileObject source, long previous, boolean expected) throws FileStateInvalidException {
        if (RemoteFileObjectBase.USE_VCS) {
            try {
                final FileSystem fileSystem = source.getFileSystem();
                source.getFileSystem().setInsideVCS(true);
                FilesystemInterceptor interseptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
                LinkedList<FileProxyI> list = new LinkedList<>();
                if (interseptor != null) {
                    long tc = interseptor.refreshRecursively(FilesystemInterceptorProvider.toFileProxy(source), previous, list);
                }
                for (FileProxyI proxy : list) {
                    FileObject fo = source.getFileSystem().findResource(proxy.getPath());
                    // TODO what should be fire?
                }
            } finally {
                source.getFileSystem().setInsideVCS(false);
            }
        }
    }

    private void addAll(FileObject folder) {
        if (kept != null) {
            kept.add(folder);
            Enumeration<? extends FileObject> en = folder.getChildren(true);
            while (en.hasMoreElements()) {
                FileObject fo = en.nextElement();
                kept.add(fo);
            }
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && isParentOf(thisFo, fe.getFile())) {
            fcl.fileRenamed(fe);
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && isParentOf(thisFo, file)) {
            fcl.fileFolderCreated(fe);
            addAll(file);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && isParentOf(thisFo, file)) {
            fcl.fileDeleted(fe);
            if (kept != null) {
                kept.remove(file);
            }
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && isParentOf(thisFo, file)) {
            fcl.fileDataCreated(fe);
            if (kept != null) {
                kept.add(file);
            }
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && isParentOf(thisFo, fe.getFile())) {
            fcl.fileChanged(fe);
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && isParentOf(thisFo, fe.getFile())) {
            fcl.fileAttributeChanged(fe);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RecursiveListener other = (RecursiveListener) obj;
        if (this.fcl != other.fcl && (this.fcl == null || !this.fcl.equals(other.fcl))) {
            return false;
        }
        final FileObject otherFo = other.get();
        final FileObject thisFo = this.get();
        if (thisFo != otherFo && (thisFo == null || !thisFo.equals(otherFo))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final FileObject thisFo = this.get();
        int hash = 3;
        hash = 37 * hash + (this.fcl != null ? this.fcl.hashCode() : 0);
        hash = 13 * hash + (thisFo != null ? thisFo.hashCode() : 0);
        return hash;
    }

    private boolean isParentOf(FileObject folder, FileObject fo) {
        Parameters.notNull("folder", folder);  //NOI18N
        Parameters.notNull("fileObject", fo);  //NOI18N
        if (folder.isFolder()) {
            try {
                if (folder.getFileSystem() != fo.getFileSystem()) {
                    return false;
                }
            } catch (FileStateInvalidException e) {
                return false;
            }
            FileObject parent = fo.getParent();
            while (parent != null) {
                if (parent.equals(folder)) { // links are wrapper, == does not suite here!
                    return true;
                }
                parent = parent.getParent();
            }
        }
        return false;        
    }
}
