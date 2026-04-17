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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.Caller;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;

/** Keeps list of fileobjects under given root. Adapted from Jan Lahoda's work
 * in issue 168237
 */
final class FileObjectKeeper implements FileChangeListener {
    private static final Logger LOG = Logger.getLogger(FileObjectKeeper.class.getName());
    private static final Object TIME_STAMP_LOCK = new Object();
    private static final int RECURSION_LIMIT = Integer.getInteger(     //#256269
            "org.netbeans.modules.masterfs.filebasedfs.fileobjects"     //NOI18N
            + ".FileObjectKeeper.RECURSION_LIMIT", 128);                //NOI18N

    /** @GuardedBy("this") */
    private Set<FolderObj> kept;
    private Collection<FileChangeListener> listeners;
    private final FolderObj root;
    //@GuardedBy("TIME_STAMP_LOCK")
    private long timeStamp;

    public FileObjectKeeper(FolderObj root) {
        this.root = root;
    }

    public synchronized void addRecursiveListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
        }
        LOG.log(Level.FINEST, "addRecursiveListener for {0} isEmpty: {1}", new Object[]{root, listeners.isEmpty()});
        if (listeners.isEmpty()) {
            Callable<?> stop = null;
            final boolean deepClass = fcl.getClass().getName().equals("org.openide.filesystems.DeepListener"); // NOI18N
            if (fcl instanceof Callable && deepClass) {
                stop = (Callable<?>)fcl;
            }
            FileFilter filter = null;
            if (fcl instanceof FileFilter && deepClass) {
                filter = (FileFilter)fcl;
            }
            try {
                listenToAll(stop, filter);
            } catch (Error | RuntimeException e) {
                LOG.log(Level.WARNING, null, e);
                throw e;
            }
        }
        listeners.add(fcl);
    }

    public synchronized void removeRecursiveListener(FileChangeListener fcl) {
        if (listeners == null) {
            return;
        }
        listeners.remove(fcl);
        LOG.log(Level.FINEST, "removeRecursiveListener for {0} isEmpty: {1}", new Object[]{root, listeners.isEmpty()});
        if (listeners.isEmpty()) {
            try {
                listenNoMore();
            } catch (Error | RuntimeException e) {
                LOG.log(Level.WARNING, null, e);
                throw e;
            }
        }
    }
     public List<File> init(long previous, FileObjectFactory factory, boolean expected) {
         boolean recursive;
         synchronized (TIME_STAMP_LOCK) {
             recursive = timeStamp < -1;
             if (timeStamp > 0) {
                 timeStamp = -timeStamp;
             }
             if (timeStamp == 0) {
                 timeStamp = -2;
             }
         }

         File file = Watcher.wrap(root.getFileName().getFile(), root);
         List<File> arr = new ArrayList<>();
         long ts = root.getProvidedExtensions().refreshRecursively(file, previous, arr);
         try {
             for (File f : arr) {
                 if (f.isDirectory()) {
                     continue;
                 }
                 long lm = f.lastModified();
                 LOG.log(Level.FINE, "  check {0} for {1}", new Object[] { lm, f });
                 if (lm > ts) {
                     ts = lm;
                 }
                 if (lm > previous && factory != null && !recursive) {
                     final BaseFileObj prevFO = factory.getCachedOnly(f);
                     if (prevFO == null) {
                         BaseFileObj who = factory.getValidFileObject(f, Caller.GetChildern, true);
                         if (who != null) {
                             LOG.log(Level.FINE, "External change detected {0}", who);  //NOI18N
                             if (who.isData()) {
                                who.fireFileDataCreatedEvent(expected);
                             } else {
                                who.fireFileFolderCreatedEvent(expected);
                             }
                          } else {
                             LOG.log(Level.FINE, "Cannot get valid FileObject. File probably removed: {0}", f);  //NOI18N
                          }
                     } else {
                         LOG.log(Level.FINE, "Do classical refresh for {0}", prevFO);  //NOI18N
                         prevFO.refresh(expected, true);
                      }
                  }
             }
         } catch (StackOverflowError ex) {
             Exceptions.attachMessage(ex, 
                "FileObjectKeeper.init for " + this.root +  // NOI18N
                 " timeStamp: " + timeStamp + " recursive: " + recursive // NOI18N
             ); 
             throw ex;
         }
         synchronized (TIME_STAMP_LOCK) {
             if (!recursive) {
                 timeStamp = ts;
             }
         }
         LOG.log(Level.FINE, "Testing {0}, time {1}", new Object[]{file, timeStamp});
         return arr;
    }
                
     

    private void listenTo(FileObject fo, boolean add, Collection<? super File> children) {
        Set<FolderObj> k;
        if (add) {
            fo.addFileChangeListener(this);
            if (fo instanceof FolderObj folder) {
                folder.getKeeper(children);
                folder.getChildren();
                assert Thread.holdsLock(FileObjectKeeper.this);
                k = kept;
                if (k != null) {
                    k.add(folder);
                }
            }
            LOG.log(Level.FINER, "Listening to {0}", fo);
        } else {
            fo.removeFileChangeListener(this);
            LOG.log(Level.FINER, "Ignoring {0}", fo);
        }
    }

    private void listenToAll(Callable<?> stop, FileFilter filter) {
        assert Thread.holdsLock(FileObjectKeeper.this);
        assert kept == null : "Already listening to " + kept + " now requested for " + root;
        kept = new HashSet<>();
        listenToAllRecursion(root, null, stop, filter, 0);
    }

    /**
     * Recursive part of {@link #listenToAll(Callable, FileFilter)}. Invoke
     * {@link #listenTo(FileObject, boolean, Collection)} on a folder and its
     * subfolders.
     *
     * @param obj The folder to invoke {@code listenTo} on.
     * @param knownFactory {@link FileObjectFactory to use}, or null if it is
     * not known yet.
     * @param stop {@link Callable} to call to check whether the invocation
     * should be stopped.
     * @param filter Filter for ignored files.
     * @return True if the computation should continue, false if it should be
     * exited.
     */
    private boolean listenToAllRecursion(FolderObj obj,
            FileObjectFactory knownFactory, Callable<?> stop,
            FileFilter filter, int level) {

        if (level > RECURSION_LIMIT) {
            LOG.log(Level.INFO, "Exiting listenToAllRecursion "         //NOI18N
                    + "due to RECURSION_LIMIT (limit = {0}, fo = {1})", //NOI18N
                    new Object[] {RECURSION_LIMIT, obj});
            return true;
        }
        List<File> it = new ArrayList<>();
        listenTo(obj, true, it);
        FileObjectFactory factory = knownFactory;
        for (File f : it) {
            LOG.log(Level.FINEST, "listenToAll, processing {0}", f);
            if (f == null || isCyclicSymlink(f)) {
                return false;
            }
            if (factory == null) {
                factory = FileObjectFactory.getInstance(f);
            }
            FileObject fo = factory.getValidFileObject(f, Caller.Others, true);
            LOG.log(Level.FINEST, "listenToAll, check {0} for stop {1}", new Object[] { fo, stop });
            if (fo instanceof FolderObj child) {
                if (filter != null && !filter.accept(child.getFileName().getFile())) {
                    continue;
                }
                Object shallStop = null;
                if (stop != null) {
                    try {
                        shallStop = stop.call();
                    } catch (Exception ex) {
                        shallStop = Boolean.TRUE;
                    }
                }
                if (Boolean.TRUE.equals(shallStop)) {
                    LOG.log(Level.INFO, "addRecursiveListener to {0} interrupted", child); // NOI18N
                    return false;
                }
                if (!listenToAllRecursion(child, factory, stop, filter, level + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void listenNoMore() {
        assert Thread.holdsLock(FileObjectKeeper.this);

        listenTo(root, false, null);
        Set<FolderObj> k = kept;
        if (k != null) {
            for (FolderObj fo : k) {
                listenTo(fo, false, null);
            }
            kept = null;
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        final FileObject folder = fe.getFile();
        if (folder instanceof FolderObj obj) {
            synchronized (this) {
                fileFolderCreatedRecursion(obj, null);
            }
        }
        synchronized (this) {
            assert Thread.holdsLock(FileObjectKeeper.this);
            if (arr == null || kept == null) {  //#178378 - ignore queued events when no more listening (kept == null)
                return;
            }
        }
        for (FileChangeListener l : arr) {
            l.fileFolderCreated(fe);
        }
    }

    /**
     * Recursive part of {@link #fileFolderCreated(FileEvent)}. Invoke
     * {@link #listenTo(FileObject, boolean, Collection)} on a folder and its
     * subfolders.
     *
     * @param obj Root folder object to call {@code listenTo} on.
     * @param knownFactory {@link FileObjectFactory} for the folder object, or
     * null if it is not known yet.
     */
    private void fileFolderCreatedRecursion(FolderObj obj,
            FileObjectFactory knownFactory) {
        List<File> it = new ArrayList<>();
        listenTo(obj, true, it);
        FileObjectFactory factory = knownFactory;
        for (File f : it) {
            if (factory == null) {
                factory = FileObjectFactory.getInstance(f);
            }
            FileObject fo = factory.getValidFileObject(f, Caller.Others, true);
            if (fo instanceof FolderObj folderObj) {
                fileFolderCreatedRecursion(folderObj, factory);
            }
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileDataCreated(fe);
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileChanged(fe);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        final FileObject f = fe.getFile();
        if (f.isFolder() && fe.getSource() == f && f != root) {
            // there will be another event for parent folder
            return;
        }

        if (f instanceof FolderObj obj) {
            synchronized (this) {
                assert Thread.holdsLock(FileObjectKeeper.this);
                if (kept != null) {
                    kept.remove(obj);
                }
                listenTo(obj, false, null);
            }
        }
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileDeleted(fe);
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        final FileObject f = fe.getFile();
        if (f.isFolder() && fe.getSource() == f && f != root) {
            // there will be another event for parent folder
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileRenamed(fe);
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileAttributeChanged(fe);
        }
    }

    long childrenLastModified() {
        return timeStamp == -2 ? 0 : Math.abs(timeStamp);
    }

    synchronized boolean isOn() {
        assert Thread.holdsLock(FileObjectKeeper.this);
        if (kept != null) {
            return true;
        }
        FolderObj obj = root.getExistingParent();
        return obj != null && obj.hasRecursiveListener();
    }

    private static boolean isCyclicSymlink(File f) {
        Path file;
        try {
            file = f.toPath();
        } catch (InvalidPathException ex) {
            LOG.log(Level.INFO, null, ex);
            return false;
        }
        Path ancestor = file.getParent();
        Path realFile = null;
        for (;;) {
            if (ancestor == null || ancestor.getFileName() == null) {
                return false;
            }
            if (ancestor.getFileName().equals(file.getFileName())) {
                try {
                    if (realFile == null) { // #240120
                        realFile = file.toRealPath();
                    }
                    if (realFile.equals(ancestor.toRealPath())) {
                        return true;
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Can't convert to cannonical files {0} and {1}", new Object[]{file, ancestor});
                    LOG.log(Level.FINE, null, ex);
                }
            }
            ancestor = ancestor.getParent();
        }
        
    }

}
