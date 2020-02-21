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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileChangeListener;

/**
 *
 */
public class RemoteFileObjectFactory {

    private final ExecutionEnvironment env;
    private final RemoteFileSystem fileSystem;

    private final WeakCache<String, RemoteFileObjectBase> fileObjectsCache = new WeakCache<>();
    private final  ConcurrentHashMap<String, Boolean> unconfirmedDeletions = new ConcurrentHashMap<>();

    /** lockImpl for both fileObjectsCache and pendingListeners */
    private final Object lock = new Object();

    private final Map<String, List<FileChangeListener>> pendingListeners =
            new HashMap<>();

    private int cacheRequests = 0;
    private int cacheHits = 0;

    public RemoteFileObjectFactory(RemoteFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.env = fileSystem.getExecutionEnvironment();
    }

    /*package*/ Collection<RemoteFileObjectBase> getCachedFileObjects() {
        return fileObjectsCache.values(); // WeakCache returns a copy => no need to copy here
    }

    /*package*/ int getCachedFileObjectsCount() {
        return fileObjectsCache.size();
    }

    /**
     * Path <code>path</path> will be normalized as we will keep in cache only normalized paths as a key
     * @param path
     * @return
     */
    public RemoteFileObjectBase getCachedFileObject(String path) {
        String normalizedPath = PathUtilities.normalizeUnixPath(path);
        return fileObjectsCache.get(normalizedPath);
    }

    public void changeImplementor(RemoteDirectory parent, DirEntry oldEntry, DirEntry newEntry) {
        String path = parent.getPath() + '/' + oldEntry.getName();
        synchronized (lock) {
            RemoteFileObject owner = invalidate(path);
            RemoteFileObjectBase newImpl = createFileObject(parent, newEntry, owner);
        }
    }

    public RemoteFileObjectBase createFileObject(RemoteDirectory parent, DirEntry entry) {
        return createFileObject(parent, entry, null, false);
    }
    
    public RemoteFileObjectBase createFileObject(RemoteDirectory parent, DirEntry entry, boolean dummy) {
        return createFileObject(parent, entry, null, dummy);
    }

    public RemoteFileObjectBase createFileObject(RemoteDirectory parent, DirEntry entry, RemoteFileObject owner) {
        return createFileObject(parent, entry, owner, false);
    }
    
    public RemoteFileObjectBase createFileObject(RemoteDirectory parent, DirEntry entry, RemoteFileObject owner, boolean dummy) {
        File childCache = new File(parent.getCache(), entry.getCache());
        String childPath = parent.getPath() + '/' + entry.getName();
        RemoteFileObjectBase fo;
        if (entry.isDirectory()) {
            fo = createRemoteDirectory(parent, childPath, childCache, owner, dummy);
        }  else if (entry.isLink()) {
            fo = createRemoteLink(parent, childPath, entry.getLinkTarget(), owner, dummy);
        } else if (entry.isPlainFile()) {
            fo = createRemotePlainFile(parent, childPath, childCache, owner, dummy);
        } else {
            fo = createSpecialFile(parent, childPath, childCache, entry.getFileType(), owner, dummy);
        }
        // in -J-da mode we'll get an NPE in all the callers - and this always worged that way
        assert (fo != null) : "Returning null file object for " + entry + " in " + parent; //NOI18N
        return fo;
    }

    public RemoteFileObjectBase register(RemoteFileObjectBase fo) {
        return putIfAbsent(fo.getPath(), fo);
    }

    private RemoteFileObjectBase createRemoteDirectory(final RemoteDirectory parent, final String remotePath, final File cacheFile, final RemoteFileObject owner, final boolean dummy) {
        cacheRequests++;
        final String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedRemotePath);
        if (fo instanceof RemoteDirectory && fo.isValid() && fo.getCache().equals(cacheFile)) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(normalizedRemotePath, fo);
        }
        Creator creator = new Creator() {
            @Override
            public RemoteFileObjectBase create() {
                RemoteFileObjectBase fo;
                fo = new RemoteDirectory((owner == null) ? new RemoteFileObject(fileSystem) : owner,
                        fileSystem, env, parent, normalizedRemotePath, cacheFile);
                if (dummy) {
                    fo.setFlag(RemoteFileObjectBase.MASK_SUSPENDED_DUMMY, true);
                }
                return fo;
            }
        };
        if (parent.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, creator);
            if (result instanceof RemoteDirectory && result.getParent() == parent) {
                return result;
            } else {
                // NB: previously, we returned the newly created file object (which was NOT put in cache)
                // TODO: should we replace old object with new one?
                reportUnexpectedPrevFileObject(result, "either not a RemoteDirectory or has different parent"); //NOI18N
                return result;
            }
        } else {
            return creator.create(); // it will be invalid since the parent is invalid => don't place in cache
        }
    }

    private RemoteFileObjectBase createRemotePlainFile(final RemoteDirectory parent, final String remotePath, final File cacheFile, final RemoteFileObject owner, final boolean dummy) {
        cacheRequests++;
        final String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedRemotePath);
        if (fo instanceof RemotePlainFile && fo.isValid() && fo.getCache().equals(cacheFile)) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(normalizedRemotePath, fo);
        }
        Creator creator = new Creator() {
            @Override
            public RemoteFileObjectBase create() {
                RemoteFileObjectBase fo;
                fo = new RemotePlainFile((owner == null) ? new RemoteFileObject(fileSystem) : owner,
                        fileSystem, env, parent, normalizedRemotePath, cacheFile);
                if (dummy) {
                    fo.setFlag(RemoteFileObjectBase.MASK_SUSPENDED_DUMMY, true);
                }
                return fo;
            }
        };
        if (parent.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, creator);
            if (result instanceof RemotePlainFile && result.getParent() == parent) {
                return result;
            } else {
                // NB: previously, we returned the newly created file object (which was NOT put in cache)
                // TODO: should we replace old object with new one?
                reportUnexpectedPrevFileObject(result, "either not a RemotePlainFile or has different parent"); //NOI18N
                return result;
            }
        } else {
            return creator.create(); // it will be invalid since the parent is invalid => don't place in cache
        }
    }

    private RemoteFileObjectBase createSpecialFile(final RemoteDirectory parent, final String remotePath, final File cacheFile, final FileType fileType, final RemoteFileObject owner, final boolean dummy) {
        cacheRequests++;
        final String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedRemotePath);
        if (fo instanceof SpecialRemoteFileObject && fo.isValid()) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(normalizedRemotePath, fo);
        }
        Creator creator = new Creator() {
            @Override
            public RemoteFileObjectBase create() {
                RemoteFileObjectBase fo;
                fo = new SpecialRemoteFileObject((owner == null) ? new RemoteFileObject(fileSystem) : owner,
                        fileSystem, env, parent, normalizedRemotePath, fileType);
                if (dummy) {
                    fo.setFlag(RemoteFileObjectBase.MASK_SUSPENDED_DUMMY, true);
                }
                return fo;
            }
        };
        if (parent.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, creator);
            if (result instanceof SpecialRemoteFileObject && result.getParent() == parent) {
                return result;
            } else {
                // NB: previously, we returned the newly created file object (which was NOT put in cache)
                // TODO: should we replace old object with new one?
                reportUnexpectedPrevFileObject(result, "either not a SpecialRemoteFileObject or has different parent"); //NOI18N
                return result;
            }
        } else {
            return creator.create(); // it will be invalid since the parent is invalid => don't place in cache
        }
    }

    private RemoteFileObjectBase createRemoteLink(final RemoteFileObjectBase parent, final String remotePath, final String link, final RemoteFileObject owner, final boolean dummy) {
        final String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        Creator creator = new Creator() {
            @Override
            public RemoteFileObjectBase create() {
                RemoteLink fo = new RemoteLink((owner == null) ? new RemoteFileObject(fileSystem) : owner,
                        fileSystem, env, parent, normalizedRemotePath, link);
                if (dummy) {
                    fo.setFlag(RemoteFileObjectBase.MASK_SUSPENDED_DUMMY, true);
                }
                fo.initListeners(true);  // TODO: shouldn't it be moved to constructor?
                return fo;
            }
        };
        RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, creator);
        return result;
    }

    public RemoteFileObjectBase createRemoteLinkChild(final RemoteLinkBase parent, final String remotePath, final RemoteFileObjectBase delegate) {
        final String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        final AtomicBoolean created = new AtomicBoolean(false);
        Creator creator = new Creator() {
            @Override
            public RemoteFileObjectBase create() {
                RemoteLinkChild fo = new RemoteLinkChild(new RemoteFileObject(fileSystem), fileSystem, env, parent, normalizedRemotePath, delegate);
                fo.initListeners(true); // TODO: should we move it to constructor?
                created.set(true);
                return fo;
            }
        };
        RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, creator);
        if (result instanceof RemoteLinkChild) {
            if (!created.get()) {
                RemoteFileObjectBase oldDelegate = ((RemoteLinkChild) result).getCanonicalDelegate();
                if (oldDelegate != delegate) {
                    // delegate has changed
                    final RemoteFileObject ownerFileObject = result.getOwnerFileObject();
                    result.invalidate();
                    fileObjectsCache.remove(normalizedRemotePath, result);
                    // recreate
                    creator = new Creator() {
                        @Override
                        public RemoteFileObjectBase create() {
                            RemoteLinkChild fo;
                            fo = new RemoteLinkChild(ownerFileObject, fileSystem, env, parent, normalizedRemotePath, delegate);
                            fo.initListeners(true); // TODO: should we move it to constructor?
                            return fo;
                        }
                    };
                    result = putIfAbsent(normalizedRemotePath, creator);
                    // TODO: is it possible that somebody has just placed another one? of different kind?
                }
            }
        }
        if (!(result instanceof RemoteLinkChild)) {
            reportUnexpectedPrevFileObject(result, "not a RemoteLinkChild"); //NOI18N
        }
        return result;
    }


    private static volatile boolean reportUnexpected = true;

    /*package*/ static void testSetReportUnexpected(boolean report) {
        reportUnexpected = report;
    }

    private void reportUnexpectedPrevFileObject(RemoteFileObjectBase prevFieObject, String message) {
        if (reportUnexpected && RemoteLogger.isLoggable(Level.INFO)) {
            RemoteLogger.info(new Exception(String.format("Unexpected file object in cache, found %s %s - %s", //NOI18N
                    prevFieObject.getClass().getSimpleName(), prevFieObject, message))); //NOI18N
        }
    }

    /**
     *
     * @param remotePath the path should be normalized
     * @param fo
     * @return
     */
    private RemoteFileObjectBase putIfAbsent(String remotePath, final RemoteFileObjectBase fo) {
        return putIfAbsent(remotePath, new Creator() {
            @Override
            public RemoteFileObjectBase create() {
                return fo;
            }
        });
    }

    private RemoteFileObjectBase putIfAbsent(String remotePath, Creator creator) {
        fileObjectsCache.tryCleaningDeadEntries();
        synchronized (lock) {
            RemoteFileObjectBase prev = fileObjectsCache.get(remotePath);
            if (prev == null || !prev.isValid()) {
                RemoteFileObjectBase fo = creator.create();
                List<FileChangeListener> listeners = pendingListeners.remove(remotePath);
                if (listeners != null) {
                    for (FileChangeListener l : listeners) {
                        fo.addFileChangeListener(l);
                    }
                }
                fileObjectsCache.put(remotePath, fo);
                unconfirmedDeletions.remove(remotePath);
                return fo;
            } else {
                return prev;
            }
        }
    }

    public void addFileChangeListener(String path, FileChangeListener listener) {
        String normalizedPath = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = getCachedFileObject(normalizedPath);
        if (fo == null) {
            synchronized (lock) {
                fo = getCachedFileObject(normalizedPath);
                if (fo == null) {
                    List<FileChangeListener> listeners = pendingListeners.get(normalizedPath);
                    if (listeners == null) {
                        listeners = new ArrayList<>();
                        pendingListeners.put(normalizedPath, listeners);
                    }
                    listeners.add(listener);
                } else {
                    fo.addFileChangeListener(listener);
                }
            }
        } else {
            fo.addFileChangeListener(listener);
        }
    }

    public void removeFileChangeListener(String path, FileChangeListener listener) {
        String normalizedPath = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = getCachedFileObject(normalizedPath);
        if (fo == null) {
            synchronized (lock) {
                fo = getCachedFileObject(normalizedPath);
                if (fo == null) {
                    List<FileChangeListener> listeners = pendingListeners.get(normalizedPath);
                    if (listeners != null) {
                        listeners.remove(listener);
                    }
                } else {
                    fo.removeFileChangeListener(listener);
                }
            }
        } else {
            fo.removeFileChangeListener(listener);
        }
    }

    public void invalidate(RemoteFileObjectBase fo) {
        fo.invalidate();
        String path = PathUtilities.normalizeUnixPath(fo.getPath());
        fileObjectsCache.remove(path);
    }

    /**
     * Removes file object from cache and invalidates it.
     * @return an invalidated object or null
     */
    public RemoteFileObject invalidate(String remotePath) {
        final String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.remove(normalizedRemotePath);
        if (fo != null) {
            fo.invalidate();
            return fo.getOwnerFileObject();
        }
        return null;
    }

    public void rename(String path2Rename, String newPath, RemoteFileObjectBase fo2Rename) {
        RemoteFileObjectBase[] existentChildren = (fo2Rename.isFolder()) ? fo2Rename.getExistentChildren() : null;
        String normalizedPath2Rename = PathUtilities.normalizeUnixPath(path2Rename);
        String normalizedNewPath = PathUtilities.normalizeUnixPath(newPath);
        fileObjectsCache.remove(normalizedPath2Rename, fo2Rename);
        fo2Rename.renamePath(normalizedNewPath);
        putIfAbsent(normalizedNewPath, fo2Rename);
        if (existentChildren != null && existentChildren.length > 0) {
            for (RemoteFileObjectBase fo : existentChildren) {
                String curPath = fo.getPath();
                String changedPath = normalizedNewPath + '/' + fo.getNameExt();
                rename(curPath, changedPath, fo);
            }
        }
    }

    public void setLink(RemoteDirectory parent, String linkRemotePath, String linkTarget) {
        String normalizedPath = PathUtilities.normalizeUnixPath(linkRemotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedPath);
        if (fo != null) {
            if (fo instanceof RemoteLink) {
                ((RemoteLink) fo).setLink(linkTarget, parent);
            } else {
                RemoteLogger.getInstance().log(Level.FINE, "Called setLink on {0} - invalidating", fo.getClass().getSimpleName());
                fo.invalidate();
            }
        }
    }

    public void vcsRegisterUnconfirmedDeletion(String path) {
        unconfirmedDeletions.put(path, Boolean.TRUE);
    }

    public void vcsUnregisterUnconfirmedDeletion(String path) {
        unconfirmedDeletions.remove(path);
    }

    public boolean vcsIsUnconfirmedDeletion(String path) {
        return unconfirmedDeletions.get(path) != null;
    }

    // @FunctionalInterface - No, we're still in java 7 as this should be in 8.1 patch as well
    private interface Creator {
        RemoteFileObjectBase create();
    }
}
