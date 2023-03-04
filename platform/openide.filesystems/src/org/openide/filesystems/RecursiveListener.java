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

package org.openide.filesystems;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class RecursiveListener extends WeakReference<FileObject>
implements FileChangeListener {
    private final FileChangeListener fcl;
    private final Set<FileObject> kept;

    public RecursiveListener(FileObject source, FileChangeListener fcl, boolean keep) {
        super(source);
        this.fcl = fcl;
        this.kept = keep ? new HashSet<FileObject>() : null;
        addAll(source);
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

    public void fileRenamed(FileRenameEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileRenamed(fe);
        }
    }

    public void fileFolderCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && FileUtil.isParentOf(thisFo, file)) {
            fcl.fileFolderCreated(fe);
            addAll(file);
        }
    }

    public void fileDeleted(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && FileUtil.isParentOf(thisFo, file)) {
            fcl.fileDeleted(fe);
            if (kept != null) {
                kept.remove(file);
            }
        }
    }

    public void fileDataCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        final FileObject file = fe.getFile();
        if (thisFo != null && FileUtil.isParentOf(thisFo, file)) {
            fcl.fileDataCreated(fe);
            if (kept != null) {
                kept.add(file);
            }
        }
    }

    public void fileChanged(FileEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileChanged(fe);
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
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
}
