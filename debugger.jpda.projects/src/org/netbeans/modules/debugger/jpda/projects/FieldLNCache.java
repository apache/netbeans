/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
