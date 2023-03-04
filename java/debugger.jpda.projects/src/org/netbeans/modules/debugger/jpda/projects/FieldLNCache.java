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
package org.netbeans.modules.debugger.jpda.projects;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.BaseUtilities;
import org.openide.util.WeakSet;

/**
 * Field line number cache.
 * @author martin
 */
final class FieldLNCache {
    
    private final Set<FileObject> knownFiles = Collections.synchronizedSet(new WeakSet<>());
    private final Set<FileKey> knownFileRefs = Collections.synchronizedSet(new HashSet<>());
    private final Map<FieldKey, Integer> fieldLines = new LinkedHashMap<>();
    
    void putLine(String url, String className, String fieldName, FileObject fo,
                 int lineNumber) {
        if (!knownFiles.contains(fo)) {
            fo.addFileChangeListener(new FileChangeListenerImpl(url));
            knownFileRefs.add(new FileKey(fo, url));
        }
        synchronized (fieldLines) {
            fieldLines.put(new FieldKey(url, className, fieldName), lineNumber);
        }
    }
    
    Integer getLine(String url, String className, String fieldName) {
        synchronized (fieldLines) {
            return fieldLines.get(new FieldKey(url, className, fieldName));
        }
    }
    
    private void removeFieldLines(String url) {
        synchronized (fieldLines) {
            Iterator<FieldKey> fieldIt = fieldLines.keySet().iterator();
            while (fieldIt.hasNext()) {
                FieldKey field = fieldIt.next();
                if (field.url.equals(url)) {
                    fieldIt.remove();
                }
            }
        }
    }

    private static class FieldKey {
        private final String url;
        private final String className;
        private final String fieldName;
        private final int hash;
        
        public FieldKey(String url, String className, String fieldName) {
            this.url = url;
            this.className = className;
            this.fieldName = fieldName;
            this.hash = Objects.hash(url, className, fieldName);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FieldKey other = (FieldKey) obj;
            if (!Objects.equals(this.url, other.url)) {
                return false;
            }
            if (!Objects.equals(this.className, other.className)) {
                return false;
            }
            if (!Objects.equals(this.fieldName, other.fieldName)) {
                return false;
            }
            return true;
        }
    }
    
    private class FileKey extends WeakReference<FileObject> implements Runnable {

        private final String url;

        FileKey(FileObject fo, String url) {
            super(fo, BaseUtilities.activeReferenceQueue());
            this.url = url;
        }

        @Override
        public void run() {
            removeFieldLines(url);
            knownFileRefs.remove(this);
        }

    }
    
    private class FileChangeListenerImpl implements FileChangeListener {
        
        private final String url;
        
        FileChangeListenerImpl(String url) {
            this.url = url;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {}

        @Override
        public void fileDataCreated(FileEvent fe) {}

        @Override
        public void fileChanged(FileEvent fe) {
            removeFieldLines(url);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            removeFieldLines(url);
            knownFiles.remove(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            removeFieldLines(url);
            knownFiles.remove(fe.getFile()); // url changes
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {}
        
    }
    
}
