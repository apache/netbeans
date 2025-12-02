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
package org.netbeans.modules.masterfs.watcher;

import org.netbeans.modules.masterfs.providers.Notifier;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class NotifierKeyRef<KEY> extends WeakReference<FileObject> {
    private final KEY key;
    private final int hash;
    private final Notifier<KEY> outer;

    public NotifierKeyRef(FileObject fo, KEY key, ReferenceQueue<FileObject> queue, final Notifier<KEY> outer) {
        super(fo, queue);
        this.outer = outer;
        this.key = key;
        this.hash = fo.hashCode();
        if (key != null) {
            Watcher.LOG.log(Level.FINE, "Adding watch for {0}", key);
        }
    }

    @Override
    public FileObject get() {
        return super.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        try {
            NotifierKeyRef<?> kr = (NotifierKeyRef) obj;
            FileObject mine = get();
            FileObject theirs = kr.get();
            if (mine == null) {
                return theirs == null;
            } else {
                return mine.equals(theirs);
            }
        } catch (ClassCastException ex) {
            return false;
        }
    }

    final void removeWatch() throws IOException {
        Watcher.LOG.log(Level.FINE, "Removing watch for {0}", key);
        NotifierAccessor.getDefault().removeWatch(outer, key);
    }

    @Override
    public int hashCode() {
        return hash;
    }
    
}
