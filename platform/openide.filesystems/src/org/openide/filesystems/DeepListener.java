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

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.BaseUtilities;
import org.openide.util.WeakSet;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class DeepListener extends WeakReference<FileChangeListener>
implements FileChangeListener, Runnable, Callable<Boolean>, FileFilter {
    static final Logger LOG = Logger.getLogger(FileUtil.class.getName() + ".recursive");
    private final File path;
    private FileObject watching;
    private boolean removed;
    private final Callable<Boolean> stop;
    private final FileFilter filter;
    private static List<DeepListener> keep = new ArrayList<DeepListener>();
    private final int hash;

    DeepListener(FileChangeListener listener, File path, FileFilter ff, Callable<Boolean> stop) {
        super(listener, BaseUtilities.activeReferenceQueue());
        this.path = path;
        this.stop = stop;
        this.filter = ff;
        this.hash = 11 * listener.hashCode() + 7 * path.hashCode();
    }
    
    final void init() {
        keep.add(this);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, null, new Throwable("listening to " + path));
        }
        relisten();
    }

    @Override
    public void run() {
        FileObject fo = FileUtil.toFileObject(path);
        if (fo != null) {
            fo.removeRecursiveListener(this);
        }
        removed = true;
        keep.remove(this);
    }

    private synchronized void relisten() {
        FileObject fo = FileUtil.toFileObject(path);
        if (fo == watching) {
            return;
        }
        if (watching != null) {
            watching.removeRecursiveListener(this);
            watching = null;
        }
        if (fo != null) {
            watching = fo;
            fo.addRecursiveListener(this);
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        fileRenamed(fe, false);
    }
    public void fileRenamed(FileRenameEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileRenamed(fe);
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        relisten();
        fileFolderCreated(fe, false);
    }
    public void fileFolderCreated(FileEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileFolderCreated(fe);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        fileDeleted(fe, false);
    }
    public void fileDeleted(FileEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileDeleted(fe);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        fileDataCreated(fe, false);
    }
    public void fileDataCreated(FileEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileDataCreated(fe);
    }

    @Override
    public void fileChanged(FileEvent fe) {
        fileChanged(fe, false);
    }
    public void fileChanged(FileEvent fe, boolean fromHolder) {
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileChanged(fe);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        FileChangeListener listener = get(fe, false);
        if (listener == null) {
            return;
        }
        listener.fileAttributeChanged(fe);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeepListener other = (DeepListener) obj;
        if (this.path != other.path && (this.path == null || !this.path.equals(other.path))) {
            return false;
        }
        final FileChangeListener ref = this.get();
        if (ref != other.get() && (ref == null || !ref.equals(other.get()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private Set<FileEvent> delivered = Collections.synchronizedSet(new WeakSet<FileEvent>());
    private FileChangeListener get(FileEvent fe, boolean fromHolder) {
        if (removed) {
            return null;
        }
        if (fromHolder) {
            if (fe.getFile() != fe.getSource()) {
                return null;
            }
        }
        if (!delivered.add(fe)) {
            return null;
        }
        return get();
    }

    @Override
    public Boolean call() throws Exception {
        return stop != null ? stop.call() : null;
    }

    @Override
    public boolean accept(File pathname) {
        return filter == null || filter.accept(pathname);
    }

    @Override
    public String toString() {
        return "DeepListener{" + get() + "@" + path + '}';
    }
}
