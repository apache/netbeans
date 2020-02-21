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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem.FileInfo;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 */
public class RemoteDirectory extends RemoteFileObjectWithCache {

    private static final boolean trace = Boolean.getBoolean("cnd.remote.directory.trace"); //NOI18N

    private Reference<DirectoryStorage> storageRef = new SoftReference<>(null);
    private Reference<MagicCache> magicCache = new SoftReference<>(null);

    private static final class RefLock {}
    private final Object refLock = new RefLock();

    private static final class MagicLock {}
    private final Object magicLock = new MagicLock();

    private volatile RemoteFileSystemTransport.Warmup warmup;

    /*package*/ RemoteDirectory(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv,
            RemoteDirectory parent, String remotePath, File cache) {
        super(wrapper, fileSystem, execEnv, parent, remotePath, cache);
        if (getStorageFile().exists()) {
            RemoteFileSystemTransport.registerDirectory(this);
        }
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean isData() {
        return false;
    }

    @Override
    public RemoteFileObject getFileObject(String name, String ext,  @NonNull Set<String> antiLoop) {
         return getFileObject(composeName(name, ext), antiLoop);
    }

    public final FileSystemProvider.Stat getStat(String childNameExt) throws IOException {
        DirEntry entry = getEntry(childNameExt);
        return FileSystemProvider.Stat.create(entry.getDevice(), entry.getINode());
    }

    private DirEntry getEntry(String childNameExt) throws IOException {
        try {
            DirectoryStorage storage = getDirectoryStorage(childNameExt);
            DirEntry entry = storage.getValidEntry(childNameExt);
            return entry;
        } catch (ConnectException ex) {
            throw ex;
        } catch (InterruptedIOException | ExecutionException | InterruptedException | TimeoutException ex) {
            RemoteLogger.finest(ex, this);
            return null; // don't report
        }
    }

    /*package*/ boolean canWrite(String childNameExt) throws IOException, ConnectException {
        DirEntry entry = getEntry(childNameExt);
        if (entry == null) {
            return isSuspendedWritesUpload();
        }
        return entry.canWrite();
    }

    /*package*/ boolean canRead(String childNameExt) throws IOException {
        DirEntry entry = getEntry(childNameExt);
        if (entry == null) {
            return isSuspendedWritesUpload();
        }
        return entry.canRead();
    }

    /*package*/ boolean canExecute(String childNameExt) throws IOException {
        DirEntry entry = getEntry(childNameExt);
        return entry != null && entry.canExecute();
    }

    @Override
    public RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException {
        return create(composeName(name, ext), false, orig);
    }

    @Override
    public RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException {
        return create(name, true, orig);
    }

    /**
     * Called after child creation (sometimes - for now only when copying or moving) or removal.
     * TODO: call after child creation via createData/createFolder
     * @param child is NULL if the file was created (creation is always external => we don't know file object yet),
     * not null if the file was deleted
     */
    @Override
    protected void postDeleteOrCreateChild(RemoteFileObject child, DirEntryList entryList) {
        // leave old implementation for a while (under a flag, by default use new impl.)
        String childNameExt = (child == null) ? null : child.getNameExt();
        if (RemoteFileSystemUtils.getBoolean("remote.fast.delete", true)) {
            Lock writeLock = getLockSupport().getCacheLock(this).writeLock();
            writeLock.lock();
            boolean sendEvents = true;
            try {
                DirectoryStorage storage = getExistingDirectoryStorage();
                if (child != null && storage == DirectoryStorage.EMPTY) {
                    Exceptions.printStackTrace(new IllegalStateException("postDeleteOrCreateChild stat is called but remote directory cache does not exist")); // NOI18N
                }
                List<DirEntry> entries;
                if (entryList == null) {
                    entries = storage.listValid(childNameExt);
                    DirectoryStorage newStorage = new DirectoryStorage(getStorageFile(), entries);
                    try {
                        newStorage.store();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex); // what else can we do?..
                    }
                    synchronized (refLock) {
                        storageRef = new SoftReference<>(newStorage);
                    }
                    if (child != null) {
                        getFileSystem().getFactory().invalidate(child.getPath());
                    }
                } else {
                    try {
                        updateChildren(toMap(entryList), storage, true, childNameExt, null, false);
                    } catch (IOException ex) {
                        RemoteLogger.finest(ex, this);
                    }
                    sendEvents = false;
                }
            } finally {
                writeLock.unlock();
            }
            if (sendEvents) {
                if (child != null) {
                    RemoteLogger.assertTrue(!child.isValid(), "Calling postDelete ob valid child " + child);
                    fireDeletedEvent(this.getOwnerFileObject(), child, false, true);
                }
            }
            //RemoteFileSystemTransport.scheduleRefresh(getExecutionEnvironment(), Arrays.asList(getPath()));
        } else {
            try {
                DirectoryStorage ds = refreshDirectoryStorage(childNameExt, false); // it will fire events itself
            } catch (ConnectException ex) {
                RemoteLogger.getInstance().log(Level.INFO, "Error post removing/creating child " + child, ex);
            } catch (IOException | ExecutionException | TimeoutException ex) {
                RemoteLogger.finest(ex, this);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                RemoteLogger.finest(ex, this);
            }
        }
    }

    @Override
    protected DirEntryList deleteImpl(FileLock lock) throws IOException {
        return RemoteFileSystemTransport.delete(getExecutionEnvironment(), getPath(), true);
    }

    private RemoteFileObject create(String name, boolean directory, RemoteFileObjectBase orig) throws IOException {
        SuspendInfo suspendInfo = getFileSystem().getSuspendInfo(this);
        if(suspendInfo != null) {
            FileType type = directory ? FileType.Directory : FileType.Regular;
            DirEntry entry = DirEntryImpl.create(name, 0, System.currentTimeMillis(), true, true, true, type.toChar(), -1, -1, null);
            final RemoteFileObjectBase fo = getFileSystem().getFactory().createFileObject(this, entry, true);
            if (fo.getFlag(MASK_SUSPENDED_DUMMY)) {                
                if (fo instanceof RemotePlainFile) {
                    suspendInfo.addDummyChild((RemotePlainFile) fo); // in turn calls addSuspendsd()
                    fo.setPendingRemoteDelivery(true);
                    fo.getCache().createNewFile();
                } else if (fo instanceof RemoteDirectory) {
                    suspendInfo.addDummyChild((RemoteDirectory) fo); // in turn calls addSuspendsd()
                    fo.getCache().mkdirs();
                }
            }
            return fo.getOwnerFileObject();
        }
        // Have to comment this out since NB does lots of stuff in the UI thread and I have no way to control this :(
        // RemoteLogger.assertNonUiThread("Remote file operations should not be done in UI thread");
        String path = getPath() + '/' + name;
        if (name.contains("\\") || name.contains("/")) { //NOI18N
            throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                    "EXC_CannotCreateFile", getDisplayName(path))); //NOI18N
        }
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            throw RemoteExceptions.createConnectException(NbBundle.getMessage(RemoteDirectory.class,
                    "EXC_CantCreateNoConnect", getDisplayName(path))); //NOI18N
        }
        if (USE_VCS) {
            FilesystemInterceptorProvider.FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
            if (interceptor != null) {
                try {
                    getFileSystem().setInsideVCS(true);
                    getFileSystem().setBeingCreated(new FileInfo(path, directory ? FileType.Directory : FileType.Regular));
                    interceptor.beforeCreate(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()), name, directory);                    
                } finally {
                    getFileSystem().setInsideVCS(false);
                    getFileSystem().setBeingCreated(null);
                }
            }
        }
        ProcessUtils.ExitStatus res;
        if (directory) {
            res = ProcessUtils.execute(getExecutionEnvironment(), "mkdir", path); //NOI18N
        } else {
            String script = String.format("ls ./\"%s\" || touch ./\"%s\"", name, name); // NOI18N
            res = ProcessUtils.executeInDir(getPath(), getExecutionEnvironment(), "sh", "-c", script); // NOI18N
            if (res.isOK() && res.getErrorLines().isEmpty()) {
                creationFalure(name, directory, orig);
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_AlreadyExists", getDisplayName(path))); // NOI18N
            }
        }
        if (res.isOK()) {
            try {
                refreshDirectoryStorage(name, false);
                RemoteFileObject fo = getFileObject(name, new HashSet<String>());
                if (fo == null) {
                    creationFalure(name, directory, orig);
                    throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemoteDirectory.class,
                            "EXC_CannotCreateFile", getDisplayName(path))); //NOI18N
                }
                if (USE_VCS) {
                    try {
                        getFileSystem().setInsideVCS(true);
                        getFileSystem().setBeingCreated(new FileInfo(fo.getImplementor()));
                        FilesystemInterceptorProvider.FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
                        if (interceptor != null) {
                            if (this == orig) {
                                interceptor.createSuccess(FilesystemInterceptorProvider.toFileProxy(fo));
                            } else {
                                RemoteFileObject originalFO = orig.getFileObject(name, new HashSet<String>());
                                if (originalFO == null) {
                                    throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemoteDirectory.class,
                                            "EXC_CannotCreateFile", getDisplayName(path))); //NOI18N
                                }
                                interceptor.createSuccess(FilesystemInterceptorProvider.toFileProxy(originalFO));
                            }
                        }
                    } finally {
                        getFileSystem().setInsideVCS(false);
                        getFileSystem().setBeingCreated(null);
                    }
                }
                return fo;
            } catch (ConnectException ex) {
                creationFalure(name, directory, orig);
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_CannotCreateFileWithReason", getDisplayName(path), "not connected"), ex); // NOI18N
            } catch (InterruptedIOException ex) {
                creationFalure(name, directory, orig);
                throw RemoteExceptions.createInterruptedIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_CannotCreateFileWithReason", getDisplayName(path), "interrupted"), ex); // NOI18N
            } catch (TimeoutException ex) {
                creationFalure(name, directory, orig);
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_CannotCreateFileWithReason", getDisplayName(path), ex.getLocalizedMessage()), ex); // NOI18N
            } catch (IOException ex) {
                creationFalure(name, directory, orig);
                throw ex;
            } catch (ExecutionException ex) {
                creationFalure(name, directory, orig);
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_CannotCreateFileWithReason2", getDisplayName(path), //NOI18N
                        "exception occurred", ex.getLocalizedMessage()), ex); // NOI18N
            } catch (InterruptedException ex) {
                creationFalure(name, directory, orig);
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_CannotCreateFileWithReason", getDisplayName(path), "interrupted"), ex); // NOI18N
            }
        } else {
            creationFalure(name, directory, orig);
            throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                    "EXC_CannotCreateFileWithReason", getDisplayName(path), res.getErrorString())); // NOI18N
        }
    }

    private void creationFalure(String name, boolean directory, RemoteFileObjectBase orig) {
        if (USE_VCS) {
            try {
                getFileSystem().setInsideVCS(true);
                FilesystemInterceptorProvider.FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
                if (interceptor != null) {
                    interceptor.createFailure(FilesystemInterceptorProvider.toFileProxy(getOwnerFileObject()), name, directory);
                }
            } finally {
                getFileSystem().setInsideVCS(false);
            }
        }
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath, @NonNull Set<String> antiLoop) {
        Parameters.notNull("path", relativePath);
        relativePath = PathUtilities.normalizeUnixPath(relativePath);
        if ("".equals(relativePath)|| ".".equals(relativePath)) { // NOI18N
            return getOwnerFileObject();
        }
        if (relativePath.startsWith("..")) { //NOI18N
            String absPath = getPath() + '/' + relativePath;
            absPath = PathUtilities.normalizeUnixPath(absPath);
            return getFileSystem().findResource(absPath, antiLoop);
        }
        if (relativePath.length()  > 0 && relativePath.charAt(0) == '/') { //NOI18N
            relativePath = relativePath.substring(1);
        }
        if (relativePath.endsWith("/")) { // NOI18N
            relativePath = relativePath.substring(0,relativePath.length()-1);
        }
        int slashPos = relativePath.lastIndexOf('/');
        if (slashPos > 0) { // can't be 0 - see the check above
            // relative path contains '/' => delegate to direct parent
            String parentRemotePath = getPath() + '/' + relativePath.substring(0, slashPos); //TODO:rfs: process ../..
            if (antiLoop != null) {
                String absPath = getPath() + '/' + relativePath;
                if (antiLoop.contains(absPath)) {
                    return null;
                }
                antiLoop.add(absPath);
            }
            String childNameExt = relativePath.substring(slashPos + 1);
            RemoteFileObject parentFileObject = getFileSystem().findResource(parentRemotePath, antiLoop);
            if (parentFileObject != null &&  parentFileObject.isFolder()) {
                RemoteFileObject result = parentFileObject.getFileObject(childNameExt, antiLoop);
                return result;
            } else {
                return null;
            }
        }
        RemoteLogger.assertTrue(slashPos == -1);
        SuspendInfo suspendInfo = getFileSystem().getSuspendInfo(this);
        if (suspendInfo != null) {
            RemoteFileObjectBase dummyChild = suspendInfo.getDirectDummyChild(this, relativePath);
            if (dummyChild == null) {
                dummyChild = suspendInfo.getDirectDummyChild(this, relativePath);
                if (dummyChild == null) {
                    dummyChild = suspendInfo.getDirectDummyChild(this, relativePath);
                }
            }
            if (dummyChild != null) {
                return dummyChild.getOwnerFileObject();
            } else if(this.getFlag(MASK_SUSPENDED_DUMMY)) {
                return null;
            }
        }
        try {
            DirectoryStorage storage = getDirectoryStorage(relativePath);
            DirEntry entry = storage.getValidEntry(relativePath);
            if (entry == null) {
                return null;
            }
            return getFileSystem().getFactory().createFileObject(this, entry).getOwnerFileObject();
        } catch (InterruptedException | InterruptedIOException
                | ExecutionException | FileNotFoundException | TimeoutException ex) {
            RemoteLogger.finest(ex, this);
            return null;
        } catch (ConnectException ex) {
            // don't report, this just means that we aren't connected
            setFlag(CONNECTION_ISSUES, true);
            RemoteLogger.finest(ex, this);
            return null;
        } catch (IOException ex) {
            RemoteLogger.fine(ex);
            return null;
        }
    }

    private void fireRemoteFileObjectCreated(RemoteFileObject fo) {
        FileEvent e = new FileEvent(this.getOwnerFileObject(), fo);
        RemoteFileObjectBase delegate = fo.getImplementor();
        if (delegate instanceof RemoteDirectory) { // fo.isFolder() very slow if it is a link
            fireFileFolderCreatedEvent(getListeners(), e);
        } else if (delegate instanceof RemotePlainFile) {
            fireFileDataCreatedEvent(getListeners(), e);
        } else {
            if (delegate instanceof RemoteLinkBase) {
                RemoteLogger.warning("firing fireFileDataCreatedEvent for a link {0} [{1}]", delegate, delegate.getClass().getSimpleName());
            }
            fireFileDataCreatedEvent(getListeners(), e);
        }
//            if (fo.isFolder()) { // fo.isFolder() very slow if it is a link
//                fireFileFolderCreatedEvent(getListeners(), e);
//            } else {
//                fireFileDataCreatedEvent(getListeners(), e);
//            }
    }

    @Override
    protected RemoteFileObjectBase[] getExistentChildren() {
        return getExistentChildren(getExistingDirectoryStorage());
    }

    private DirectoryStorage getExistingDirectoryStorage() {

        DirectoryStorage storage;
        synchronized (refLock) {
            storage = storageRef.get();
        }
        if (storage == null) {
            File storageFile = getStorageFile();
            if (storageFile.exists()) {
                Lock readLock = getLockSupport().getCacheLock(this).readLock();
                readLock.lock();
                try {
                    storage = DirectoryStorage.load(storageFile, getExecutionEnvironment());
                } catch (FormatException e) {
                    FormatException.reportIfNeeded(e);
                    storageFile.delete();
                } catch (InterruptedIOException e) {
                    // nothing
                } catch (FileNotFoundException e) {
                    // this might happen if we switch to different DirEntry implementations, see storageFile.delete() above
                    RemoteLogger.finest(e, this);
                } catch (IOException e) {
                    RemoteLogger.finest(e, this);
                } finally {
                    readLock.unlock();
                }
            }
        }
        return  storage == null ? DirectoryStorage.EMPTY : storage;
    }

    private RemoteFileObjectBase[] getExistentChildren(DirectoryStorage storage) {
        List<DirEntry> entries = storage.listValid();
        List<RemoteFileObjectBase> result = new ArrayList<>(entries.size());
        for (DirEntry entry : entries) {
            RemoteFileObjectBase fo = getCachedChild(entry.getName());
            if (fo != null) {
                result.add(fo);
            }
        }
        return result.toArray(new RemoteFileObjectBase[result.size()]);
    }

    @Override
    public RemoteFileObject[] getChildren() {
        if (getFlag(MASK_SUSPENDED_DUMMY)) {
            SuspendInfo suspendInfo = getFileSystem().removeSuspendInfo(this);
            if (suspendInfo != null) {
                return suspendInfo.getDirectDummyChildren(this);
            }
            return new RemoteFileObject[0];
        }        
        try {
            DirectoryStorage storage = getDirectoryStorage(null);
            List<DirEntry> entries = storage.listValid();
            RemoteFileObject[] childrenFO = new RemoteFileObject[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                DirEntry entry = entries.get(i);
                childrenFO[i] = getFileSystem().getFactory().createFileObject(this, entry).getOwnerFileObject();
            }
            return childrenFO;
        } catch (InterruptedException | InterruptedIOException | 
                FileNotFoundException | ExecutionException | TimeoutException ex ) {
            // InterruptedException:
            //      don't report, this just means that we aren't connected
            //      or just interrupted (for example by FileChooser UI)
            //      or cancelled
            // ExecutionException: should we report it?
            RemoteLogger.finest(ex, this);
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex, this);
            // don't report, this just means that we aren't connected
            setFlag(CONNECTION_ISSUES, true);
        } catch (IOException ex) {
            RemoteLogger.info(ex, this); // undo won't show a red brick dialog, but print
        }
        return new RemoteFileObject[0];
    }

    private DirectoryStorage getDirectoryStorage(String childName) throws
            TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        long time = System.currentTimeMillis();
        try {
            return getDirectoryStorageImpl(false, null, childName, false);
        } catch (StackOverflowError soe) { // workaround for #130929
            String text = "StackOverflowError when accessing " + getPath(); //NOI18N
            Exceptions.printStackTrace(new Exception(text, soe));
            throw new IOException(text, soe); // new IOException sic! this should never happen
        } finally {
            if (trace) {
                trace("getDirectoryStorage for {1} took {0} ms", this, System.currentTimeMillis() - time); // NOI18N
            }
        }
    }

    private DirectoryStorage refreshDirectoryStorage(String expectedName, boolean expected) throws
            TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        long time = System.currentTimeMillis();
        try {
            return getDirectoryStorageImpl(true, expectedName, null, expected);
        } finally {
            if (trace) {
                trace("refreshDirectoryStorage for {1} took {0} ms", this, System.currentTimeMillis() - time); // NOI18N
            }
        }
    }

    private void warmupDirs() {
        if (RemoteFileSystemUtils.getBoolean("remote.warmup", true)) {
            setFlag(MASK_WARMUP, true);
        }
    }

    /** just a conveniency shortcut that allows not to cast each time */
    /*package*/ RemoteDirectory getParentImpl() {
        return (RemoteDirectory) getParent(); // see constructor
    }

    private boolean isFlaggedForWarmup() {
        if(getFlag(MASK_WARMUP)) {
            return true;
        } else {
            RemoteDirectory p = getParentImpl();
            if (p != null) {
                return p.isFlaggedForWarmup();
            }
        }
        return false;
    }

    private RemoteFileSystemTransport.Warmup getWarmup() {
        RemoteFileSystemTransport.Warmup w = warmup;
        if (w == null) {
            RemoteDirectory p = getParentImpl();
            if (p != null) {
                return p.getWarmup();
            }
        }
        return w;
    }

    private Map<String, DirEntry> toMap(DirEntryList entryList) {
        Map<String, DirEntry> map = new HashMap<>();
        for (DirEntry entry : entryList.getEntries()) {
            map.put(entry.getName(), entry);
        }
        return map;
    }

    private static final AtomicInteger warmupHints = new AtomicInteger();
    private static final AtomicInteger warmupReqs = new AtomicInteger();
    private static final AtomicInteger readEntryReqs = new AtomicInteger();

    /** for test purposes */
    /*package*/ int getReadEntriesCount() {
        return readEntryReqs.get();
    }

    private Map<String, DirEntry> readEntries(DirectoryStorage oldStorage, boolean forceRefresh, String childName) 
            throws TimeoutException, IOException, InterruptedException, ExecutionException {
        if (getFileSystem().isProhibitedToEnter(getPath())) {
            return Collections.<String, DirEntry>emptyMap();
        }
        readEntryReqs.incrementAndGet();
        try {
            if (isFlaggedForWarmup()) {
                RemoteFileSystemTransport.Warmup w = getWarmup();
                if (forceRefresh) {
                    if (w != null) {
                        w.remove(getPath());
                    }
                } else {
                    warmupReqs.incrementAndGet();
                    DirEntryList entryList = null;
                    if (w == null) {
                        warmup = RemoteFileSystemTransport.createWarmup(getExecutionEnvironment(), getPath());
                        if (warmup != null) {
                            entryList = warmup.getAndRemove(getPath());
                        }
                    } else {
                        entryList = w.tryGetAndRemove(getPath());
                    }
                    if (entryList != null) {
                        warmupHints.incrementAndGet();
                        return toMap(entryList);
                    }
                }
            }
        } finally {
            if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
                RemoteLogger.finest("Warmup hits: {0} of {1} (total {2} dir.read reqs)", warmupHints.get(), warmupReqs.get(), readEntryReqs.get());
            }
        }
        Map<String, DirEntry> newEntries = new HashMap<>();
        boolean canLs = canLs();
        if (canLs) {
            DirEntryList entryList = RemoteFileSystemTransport.readDirectory(getExecutionEnvironment(), getPath());
            newEntries = toMap(entryList);
        }
        if (canLs && !isAutoMount()) {
            return newEntries;
        }
        if (childName != null) {
            String absPath = getPath() + '/' + childName;
            RemoteLogger.assertTrueInConsole(!oldStorage.isKnown(childName) || forceRefresh, "should not get here: " + absPath); //NOI18N
            if (!newEntries.containsKey(childName)) {
                DirEntry entry = getSpecialDirChildEntry(absPath, childName);
                newEntries.put(entry.getName(), entry);
            }
        }
        for (DirEntry oldEntry : oldStorage.listAll()) {
            String oldChildName = oldEntry.getName();
            if (!newEntries.containsKey(oldChildName)) {
                if (forceRefresh) {
                    if (oldEntry.isValid()) {
                        String absPath = getPath() + '/' + oldChildName;
                        DirEntry newEntry = getSpecialDirChildEntry(absPath, oldChildName);
                        newEntries.put(oldChildName, newEntry);
                    }
                } else {
                    newEntries.put(oldChildName, oldEntry);
                }
            }
        }
        return newEntries;
    }

    private DirEntry getSpecialDirChildEntry(String absPath, String childName) 
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        DirEntry entry;
        try {
            entry = RemoteFileSystemTransport.lstat(getExecutionEnvironment(), absPath);
        } catch (ExecutionException e) {
            if (RemoteFileSystemUtils.isFileNotFoundException(e)) {
                entry = null;
            } else {
                throw e;
            }
        }
        return (entry != null) ? entry : new DirEntryInvalid(childName);
    }

    private boolean isAutoMount() {
        return getFileSystem().isAutoMount(getPath());
    }

    private boolean canLs() {
        return canRead();
    }

    private boolean isSpecialDirectory() {
        return isAutoMount() || !canLs();
    }

    private boolean isAlreadyKnownChild(DirectoryStorage storage, String childName) {
        if (childName != null && storage != null) {
            if (!storage.isKnown(childName)) {
                if (ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    if (isSpecialDirectory()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected final void renameChild(FileLock lock, RemoteFileObjectBase directChild2Rename, String newNameExt, RemoteFileObjectBase orig) throws
            ConnectException, IOException, InterruptedException, ExecutionException {
        String nameExt2Rename = directChild2Rename.getNameExt();
        String name2Rename = directChild2Rename.getName();
        String ext2Rename = directChild2Rename.getExt();
        String path2Rename = directChild2Rename.getPath();

        checkConnection(this, true);

        Lock writeLock = getLockSupport().getCacheLock(this).writeLock();
        if (trace) {trace("waiting for lock");} // NOI18N
        writeLock.lock();
        try {
            DirectoryStorage storage = getExistingDirectoryStorage();
            if (storage.getValidEntry(nameExt2Rename) == null) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_NotExistingChild", nameExt2Rename, getDisplayName())); // NOI18N
            }
            if (!getCache().exists()) {
                getCache().mkdirs();
                if (!getCache().exists()) {
                    throw new IOException("Can not create cache directory " + getCache()); // NOI18N   new IOException sic - should never happen
                }
            }
            if (trace) {trace("renaming");} // NOI18N
            boolean isRenamed = false;
            if (USE_VCS) {
                try {
                    getFileSystem().setInsideVCS(true);
                    FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
                    if (interceptor != null) {
                        FilesystemInterceptorProvider.IOHandler renameHandler = interceptor.getRenameHandler(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()), newNameExt);
                        if (renameHandler != null) {
                            renameHandler.handle();
                            isRenamed = true;
                        }
                    }
                } finally {
                    getFileSystem().setInsideVCS(false);
                }
            }
            if (!isRenamed) {
                ProcessUtils.ExitStatus ret = ProcessUtils.executeInDir(getPath(), getExecutionEnvironment(), "mv", nameExt2Rename, newNameExt);// NOI18N
                if (!ret.isOK()) {
                    throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                            "EXC_CanNotRename", ret.getErrorString())); //NOI18N
                }
            }

            if (trace) {trace("synchronizing");} // NOI18N
            Exception problem = null;
            Map<String, DirEntry> newEntries = Collections.emptyMap();
            try {
                newEntries = readEntries(storage, true, newNameExt);
            } catch (FileNotFoundException ex) {
                throw ex;
            } catch (IOException | ExecutionException | TimeoutException ex) {
                problem = ex;
            }
            if (problem != null) {
                if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    // connection was broken while we read directory content - add notification
                    getFileSystem().addPendingFile(this);
                    throw RemoteExceptions.createConnectException(problem.getMessage());
                } else {
                    boolean fileNotFoundException = RemoteFileSystemUtils.isFileNotFoundException(problem);
                    if (fileNotFoundException) {
                        getFileSystem().getFactory().invalidate(this);
                        synchronized (refLock) {
                            storageRef = new SoftReference<>(DirectoryStorage.EMPTY);
                        }
                    }
                    if (!fileNotFoundException) {
                        if (problem instanceof IOException) {
                            throw (IOException) problem;
                        } else if (problem instanceof ExecutionException) {
                            throw (ExecutionException) problem;
                        } else {
                            throw new IllegalStateException("Unexpected exception class: " + problem.getClass().getName(), problem); //NOI18N
                        }
                    }
                }
            }
            getFileSystem().incrementDirSyncCount();
            Map<String, List<DirEntry>> dupLowerNames = new HashMap<>();
            boolean hasDups = false;
            boolean changed = true;
            Set<DirEntry> keepCacheNames = new HashSet<>();
            List<DirEntry> entriesToFireChanged = new ArrayList<>();
            List<DirEntry> entriesToFireChangedRO = new ArrayList<>();
            List<DirEntry> entriesToFireCreated = new ArrayList<>();
            List<RemoteFileObject> filesToFireDeleted = new ArrayList<>();
            for (DirEntry newEntry : newEntries.values()) {
                if (newEntry.isValid()) {
                    String cacheName;
                    DirEntry oldEntry = storage.getValidEntry(newEntry.getName());
                    if (oldEntry == null) {
                        cacheName = RemoteFileSystemUtils.escapeFileName(newEntry.getName());
                        if (newEntry.getName().equals(newNameExt)) {
                            DirEntry renamedEntry = storage.getValidEntry(nameExt2Rename);
                            RemoteLogger.assertTrueInConsole(renamedEntry != null, "original DirEntry is absent for " + path2Rename + " in " + this); // NOI18N
                            // reuse cache from original file
                            if (renamedEntry != null) {
                                cacheName = renamedEntry.getCache();
                                newEntry.setCache(cacheName);
                                keepCacheNames.add(newEntry);
                            }
                        } else {
                            entriesToFireCreated.add(newEntry);
                        }
                    } else {
                        if (oldEntry.isSameType(newEntry)) {
                            cacheName = oldEntry.getCache();
                            keepCacheNames.add(newEntry);
                            boolean fire = false;
                            if (!newEntry.isSameLastModified(oldEntry) || newEntry.getSize() != oldEntry.getSize()) {
                                if (newEntry.isPlainFile()) {
                                    changed = fire = true;
                                    File entryCache = new File(getCache(), oldEntry.getCache());
                                    if (entryCache.exists()) {
                                        if (trace) {trace("removing cache for updated file {0}", entryCache.getAbsolutePath());} // NOI18N
                                        entryCache.delete(); // TODO: We must just mark it as invalid instead of physically deleting cache file...
                                    }
                                }
                            }
                            if (!equals(newEntry.getLinkTarget(), oldEntry.getLinkTarget())) {
                                changed = fire = true; // TODO: we forgot old link path, probably should be passed to change event
                                getFileSystem().getFactory().setLink(this, getPath() + '/' + newEntry.getName(), newEntry.getLinkTarget());
                            }
                            if (! newEntry.isSameAccess(oldEntry)) {
                                entriesToFireChangedRO.add(newEntry);
                                changed = fire = true;
                            }
                            if (!newEntry.isDirectory() && (newEntry.getSize() != oldEntry.getSize())) {
                                changed = fire = true;// TODO: shouldn't it be the same as time stamp change?
                            }
                            if (fire) {
                                entriesToFireChanged.add(newEntry);
                            }
                        } else {
                            changed = true;
                            getFileSystem().getFactory().changeImplementor(this, oldEntry, newEntry);
                            entriesToFireChanged.add(newEntry);
                            cacheName = null; // unchanged
                        }
                    }
                    if (cacheName !=null) {
                        newEntry.setCache(cacheName);
                    }
                    String lowerCacheName = RemoteFileSystemUtils.isSystemCaseSensitive() ? newEntry.getCache() : newEntry.getCache().toLowerCase();
                    List<DirEntry> dupEntries = dupLowerNames.get(lowerCacheName);
                    if (dupEntries == null) {
                        dupEntries = new ArrayList<>();
                        dupLowerNames.put(lowerCacheName, dupEntries);
                    } else {
                        hasDups = true;
                    }
                    dupEntries.add(newEntry);
                } else {
                    changed = true;
                }
            }
            if (changed) {
                // Check for removal
                for (DirEntry oldEntry : storage.listValid()) {
                    if (!oldEntry.getName().equals(nameExt2Rename)) {
                        DirEntry newEntry = newEntries.get(oldEntry.getName());
                        if (newEntry == null || !newEntry.isValid()) {
                            RemoteFileObject removedFO = invalidate(oldEntry);
                            if (removedFO != null) {
                                filesToFireDeleted.add(removedFO);
                            }
                        }
                    }
                }
                if (hasDups) {
                    for (Map.Entry<String, List<DirEntry>> mapEntry :
                            new ArrayList<>(dupLowerNames.entrySet())) {

                        List<DirEntry> dupEntries = mapEntry.getValue();
                        if (dupEntries.size() > 1) {
                            for (int i = 0; i < dupEntries.size(); i++) {
                                DirEntry entry = dupEntries.get(i);
                                if (keepCacheNames.contains(entry)) {
                                    continue; // keep the one that already exists
                                }
                                // all duplicates will have postfix
                                for (int j = 0; j < Integer.MAX_VALUE; j++) {
                                    String cacheName = mapEntry.getKey() + '_' + j;
                                    String lowerCacheName = cacheName.toLowerCase();
                                    if (!dupLowerNames.containsKey(lowerCacheName)) {
                                        if (trace) {
                                            trace("resolving cache names conflict in {0}: {1} -> {2}", // NOI18N
                                                    getCache().getAbsolutePath(), entry.getCache(), cacheName);
                                        }
                                        entry.setCache(cacheName);
                                        dupLowerNames.put(lowerCacheName, Collections.singletonList(entry));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                storage = new DirectoryStorage(getStorageFile(), newEntries.values());
                storage.store();
            } else {
                storage.touch();
            }
            // always put new content in cache
            // do it before firing events, to give liseners real content
            synchronized (refLock) {
                storageRef = new SoftReference<>(storage);
            }
            // fire all event under lockImpl
            if (changed) {
                dropMagic();
                for (FileObject deleted : filesToFireDeleted) {
                    fireFileDeletedEvent(getListeners(), new FileEvent(this.getOwnerFileObject(), deleted));
                }
                for (DirEntry entry : entriesToFireCreated) {
                    RemoteFileObjectBase fo = getFileSystem().getFactory().createFileObject(this, entry);
                    fireRemoteFileObjectCreated(fo.getOwnerFileObject());
                }
                for (DirEntry entry : entriesToFireChanged) {
                    RemoteFileObjectBase fo = getCachedChild(entry.getName());
                    if (fo != null) {
                        RemoteFileObject ownerFileObject = fo.getOwnerFileObject();
                        fireFileChangedEvent(getListeners(), new FileEvent(ownerFileObject, ownerFileObject, false, ownerFileObject.lastModified().getTime()));
                    }
                }
                // rename itself
                String newPath = getPath() + '/' + newNameExt;
                getFileSystem().getFactory().rename(path2Rename, newPath, directChild2Rename);
                // fire rename
                fireFileRenamedEvent(directChild2Rename.getListeners(),
                        new FileRenameEvent(directChild2Rename.getOwnerFileObject(), directChild2Rename.getOwnerFileObject(), name2Rename, ext2Rename));
                fireFileRenamedEvent(this.getListeners(),
                        new FileRenameEvent(this.getOwnerFileObject(), directChild2Rename.getOwnerFileObject(), name2Rename, ext2Rename));
                fireReadOnlyChangedEventsIfNeed(entriesToFireChangedRO);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /*package */ DirEntry getDirEntry(String childName) {
        Lock writeLock = getLockSupport().getCacheLock(this).writeLock();
        if (trace) {trace("waiting for lock");} // NOI18N
        writeLock.lock();
        try {
            DirectoryStorage storage = getExistingDirectoryStorage();
            if (storage == DirectoryStorage.EMPTY) {
                return null;
            }
            return storage.getValidEntry(childName);
        } finally {
            writeLock.unlock();
        }        
    }

    /*package */void updateStat(RemotePlainFile fo, DirEntry entry) {
        RemoteLogger.assertTrue(fo.getNameExt().equals(entry.getName()));
        RemoteLogger.assertTrue(fo.getParent() == this);
        RemoteLogger.assertFalse(entry.isDirectory());
        RemoteLogger.assertFalse(entry.isLink());
        Lock writeLock = getLockSupport().getCacheLock(this).writeLock();
        if (trace) {trace("waiting for lock");} // NOI18N
        writeLock.lock();
        try {
            DirectoryStorage storage = getExistingDirectoryStorage();
            if (storage == DirectoryStorage.EMPTY) {
                Exceptions.printStackTrace(new IllegalStateException("Update stat is called but remote directory cache does not exist")); // NOI18N
            } else {
                List<DirEntry> entries = storage.listValid(fo.getNameExt());
                entry.setCache(fo.getCache().getName());
                entries.add(entry);
                DirectoryStorage newStorage = new DirectoryStorage(getStorageFile(), entries);
                try {
                    newStorage.store();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex); // what else can we do?..
                }
                synchronized (refLock) {
                    storageRef = new SoftReference<>(newStorage);
                }
                fo.setPendingRemoteDelivery(false);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private DirectoryStorage getDirectoryStorageImpl(final boolean forceRefresh, final String expectedName, final String childName, final boolean expected) throws
            TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        if (forceRefresh && ! ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            //RemoteLogger.getInstance().warning("refreshDirectoryStorage is called while host is not connected");
            //force = false;
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(getExecutionEnvironment()));
        }

        DirectoryStorage storage;

        File storageFile = getStorageFile();

        // check whether it is cached in memory
        synchronized (refLock) {
            storage = storageRef.get();
        }
        boolean fromMemOrDiskCache;

        if (storage == null) {
            // try loading from disk
            fromMemOrDiskCache = false;
            storage = DirectoryStorage.EMPTY;
            if (storageFile.exists()) {
                Lock readLock = getLockSupport().getCacheLock(this).readLock();
                try {
                    readLock.lock();
                    try {
                        storage = DirectoryStorage.load(storageFile, getExecutionEnvironment());
                        fromMemOrDiskCache = true;
                        // try to keep loaded cache in memory
                        synchronized (refLock) {
                            DirectoryStorage s = storageRef.get();
                            // it could be cache put in memory by writer (the best content)
                            // or by previous reader => it's the same as loaded
                            if (s != null) {
                                if (trace) { trace("using storage that was kept by other thread"); } // NOI18N
                                storage = s;
                            } else {
                                storageRef = new SoftReference<>(storage);
                            }
                        }
                    } catch (FormatException e) {
                        FormatException.reportIfNeeded(e);
                        storageFile.delete();
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (FileNotFoundException e) {
                        // this might happen if we switch to different DirEntry implementations, see storageFile.delete() above
                        RemoteLogger.finest(e, this);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                } finally {
                    readLock.unlock();
                }
            }
        } else {
            if (trace) { trace("use memory cached storage"); } // NOI18N
            fromMemOrDiskCache = true;
        }

        if (fromMemOrDiskCache && !forceRefresh && isAlreadyKnownChild(storage, childName)) {
            RemoteLogger.assertTrue(storage != null);
            if (trace) { trace("returning cached storage"); } // NOI18N
            return storage;
        }
        if (childName != null && RemoteFileSystem.isSniffing(childName)) {
            if (isAutoMount() || getFileSystem().isDirectAutoMountChild(getPath())) {
                return DirectoryStorage.EMPTY;
            }
        }
        // neither memory nor disk cache helped or was request to force refresh
        // proceed with reading remote content

        checkConnection(this, true);

        Lock writeLock = getLockSupport().getCacheLock(this).writeLock();
        if (trace) { trace("waiting for lock"); } // NOI18N
        writeLock.lock();
        try {
            // in case another writer thread already synchronized content while we were waiting for lockImpl
            // even in refresh mode, we need this content, otherwise we'll generate events twice
            synchronized (refLock) {
                DirectoryStorage s = storageRef.get();
                if (s != null) {
                    if (trace) { trace("got storage from mem cache after waiting on writeLock: {0} expectedName={1}", getPath(), expectedName); } // NOI18N
                    if (forceRefresh || !isAlreadyKnownChild(s, childName)) {
                        storage = s;
                    } else {
                        return s;
                    }
                }
            }
            if (!getCache().exists()) {
                getCache().mkdirs();
                if (!getCache().exists()) {
                    throw new IOException("Can not create cache directory " + getCache()); // NOI18N // new IOException sic - should never happen
                }
            }
            if (trace) { trace("synchronizing"); } // NOI18N

            if (childName != null && RemoteLogger.isLoggable(Level.FINEST)) {
                RemoteLogger.finest("{0} is asked for child {1} while not having cache", getPath(), childName);
            }

            Exception problem = null;
            Map<String, DirEntry> newEntries = Collections.emptyMap();
            try {
                newEntries = readEntries(storage, forceRefresh, childName);
            }  catch (FileNotFoundException ex) {
                throw ex;
            }  catch (IOException | ExecutionException | TimeoutException ex) {
                problem = ex;
            }
            if (problem != null) {
                if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                    // connection was broken while we read directory content - add notification
                    getFileSystem().addPendingFile(this);
                    throw RemoteExceptions.createConnectException(problem.getMessage());
                } else {
                    boolean fileNotFoundException = RemoteFileSystemUtils.isFileNotFoundException(problem);
                    if (fileNotFoundException) {
                        synchronized (refLock) {
                            storageRef = new SoftReference<>(DirectoryStorage.EMPTY);
                        }
                    }
                    if (!fileNotFoundException) {
                        if (problem instanceof IOException) {
                            throw (IOException) problem;
                        } else if (problem instanceof ExecutionException) {
                            throw (ExecutionException) problem;
                        } else if (problem instanceof TimeoutException) {
                            throw (TimeoutException) problem;
                        } else {
                            throw new IllegalStateException("Unexpected exception class: " + problem.getClass().getName(), problem); //NOI18N
                        }
                    }
                }
            }
            storage = updateChildren(newEntries, storage, fromMemOrDiskCache, expectedName, childName, expected);
        } finally {
            writeLock.unlock();
        }
        return storage;
    }

    private RemoteFileObjectBase getCachedChild(String name) {
        String childAbsPath = getPath() + '/' + name;
        RemoteFileObjectBase child = getFileSystem().getFactory().getCachedFileObject(childAbsPath);
        return child;
    }

    private boolean isPendingDelivery(DirEntry entry) {
        String name = entry.getName();
        if (name.startsWith("#") && name.endsWith("#")) { // NOI18N
            name = name.substring(1, name.length() - 1);
            RemoteFileObjectBase child = getCachedChild(name);
            if (child != null && child instanceof RemotePlainFile) {
                RemotePlainFile pf = (RemotePlainFile) child;
                if (pf.isPendingRemoteDelivery()) {
                    return true;
                }
            }
        }
        return false;
    }

    private DirectoryStorage updateChildren(Map<String, DirEntry> newEntries, DirectoryStorage storage,
            boolean fromMemOrDiskCache, final String expectedName, final String childName,
            final boolean expected) throws IOException {

        getFileSystem().incrementDirSyncCount();
        Map<String, List<DirEntry>> dupLowerNames = new HashMap<>();
        boolean hasDups = false;
        boolean changed = (newEntries.size() != storage.listAll().size()) || (storage == DirectoryStorage.EMPTY);
        Set<DirEntry> keepCacheNames = new HashSet<>();
        List<DirEntry> entriesToFireChanged = new ArrayList<>();
        List<DirEntry> entriesToFireChangedRO = new ArrayList<>();
        List<DirEntry> entriesToFireCreated = new ArrayList<>();
        DirEntry expectedCreated = null;
        List<RemoteFileObject> filesToFireDeleted = new ArrayList<>();
        Iterator<Map.Entry<String, DirEntry>> it = newEntries.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DirEntry> mapEntry = it.next();
            if (isPendingDelivery(mapEntry.getValue())) {
                it.remove();
            }
        }
        for (DirEntry newEntry : newEntries.values()) {
            if (newEntry.isValid()) {
                String cacheName;
                DirEntry oldEntry = storage.getValidEntry(newEntry.getName());
                if (oldEntry == null || !oldEntry.isValid()) {
                        changed = true;
                        cacheName = RemoteFileSystemUtils.escapeFileName(newEntry.getName());
                        if (fromMemOrDiskCache || newEntry.getName().equals(expectedName) || getFlag(CONNECTION_ISSUES)) {
                            entriesToFireCreated.add(newEntry);
                            expectedCreated = newEntry;
                        }
                } else {
                    if (oldEntry.isSameType(newEntry)) {
                        cacheName = oldEntry.getCache();
                        keepCacheNames.add(newEntry);
                        boolean fire = false;
                        if (!newEntry.isSameLastModified(oldEntry) || newEntry.getSize() != oldEntry.getSize()) {
                            if (newEntry.isPlainFile()) {
                                changed = fire = true;
                                File entryCache = new File(getCache(), oldEntry.getCache());
                                if (entryCache.exists()) {
                                    if (trace) { trace("removing cache for updated file {0}", entryCache.getAbsolutePath()); } // NOI18N
                                    entryCache.delete(); // TODO: We must just mark it as invalid instead of physically deleting cache file...
                                }
                            }

                        }
                        if (!equals(newEntry.getLinkTarget(), oldEntry.getLinkTarget())) {
                            changed = fire = true; // TODO: we forgot old link path, probably should be passed to change event
                            getFileSystem().getFactory().setLink(this, getPath() + '/' + newEntry.getName(), newEntry.getLinkTarget());
                        }
                        if (!newEntry.isSameAccess(oldEntry)) {
                            entriesToFireChangedRO.add(newEntry);
                            changed = fire = true;
                        }
                        if (!newEntry.isDirectory() && (newEntry.getSize() != oldEntry.getSize())) {
                            changed = fire = true;// TODO: shouldn't it be the same as time stamp change?
                        }
                        // It is unlikely that inode changed. But it can happen. Since we cache it, we need to check
                        if (newEntry.hasINode() && !newEntry.isSameINode(oldEntry)) {
                            changed = fire = true;
                        }
                        if (fire) {
                            entriesToFireChanged.add(newEntry);
                        }
                    } else {
                        changed = true;
                        getFileSystem().getFactory().changeImplementor(this, oldEntry, newEntry);
                        if (oldEntry.isLink() && newEntry.isPlainFile() && newEntry.canWrite()) {
                            entriesToFireChangedRO.add(newEntry);
                        } else {
                            entriesToFireChanged.add(newEntry);
                        }
                        cacheName = null; // unchanged
                    }
                }
                if (cacheName !=null) {
                    newEntry.setCache(cacheName);
                }
                String lowerCacheName = RemoteFileSystemUtils.isSystemCaseSensitive() ? newEntry.getCache() : newEntry.getCache().toLowerCase();
                List<DirEntry> dupEntries = dupLowerNames.get(lowerCacheName);
                if (dupEntries == null) {
                    dupEntries = new ArrayList<>();
                    dupLowerNames.put(lowerCacheName, dupEntries);
                } else {
                    hasDups = true;
                }
                dupEntries.add(newEntry);
            } else {
                if (!storage.isKnown(childName)) {
                    changed = true;
                }
            }
        }
        if (changed) {
            // Check for removal
            for (DirEntry oldEntry : storage.listValid()) {
                DirEntry newEntry = newEntries.get(oldEntry.getName());
                if (newEntry == null || !newEntry.isValid()) {
                    RemoteFileObject removedFO = invalidate(oldEntry);
                    if (removedFO != null) {
                        filesToFireDeleted.add(removedFO);
                    }
                }
            }
            if (hasDups) {
                for (Map.Entry<String, List<DirEntry>> mapEntry :
                        new ArrayList<>(dupLowerNames.entrySet())) {

                    List<DirEntry> dupEntries = mapEntry.getValue();
                    if (dupEntries.size() > 1) {
                        for (int i = 0; i < dupEntries.size(); i++) {
                            DirEntry entry = dupEntries.get(i);
                            if (keepCacheNames.contains(entry)) {
                                continue; // keep the one that already exists
                            }
                            // all duplicates will have postfix
                            for (int j = 0; j < Integer.MAX_VALUE; j++) {
                                String cacheName = mapEntry.getKey() + '_' + j;
                                String lowerCacheName = cacheName.toLowerCase();
                                if (!dupLowerNames.containsKey(lowerCacheName)) {
                                    if (trace) { trace("resolving cache names conflict in {0}: {1} -> {2}", // NOI18N
                                            getCache().getAbsolutePath(), entry.getCache(), cacheName); }
                                    entry.setCache(cacheName);
                                    dupLowerNames.put(lowerCacheName, Collections.singletonList(entry));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            storage = new DirectoryStorage(getStorageFile(), newEntries.values());
            storage.store();
        } else {
            storage.touch();
        }
        setFlag(CONNECTION_ISSUES, false);
        // always put new content in cache
        // do it before firing events, to give liseners real content
        synchronized (refLock) {
            storageRef = new SoftReference<>(storage);
        }
        // fire all event under lockImpl
        if (changed) {
            dropMagic();
            for (RemoteFileObject deleted : filesToFireDeleted) {
                fireDeletedEvent(this.getOwnerFileObject(), deleted, expected, true);
            }

            FilesystemInterceptorProvider.FilesystemInterceptor interceptor =
                    USE_VCS ? FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem()) : null;

            for (DirEntry entry : entriesToFireCreated) {
                RemoteFileObject fo = getFileSystem().getFactory().createFileObject(this, entry).getOwnerFileObject();
                if (interceptor != null && expectedCreated != null && !expectedCreated.equals(entry)) {
                    try {
                        getFileSystem().setInsideVCS(true);
                        interceptor.createdExternally(FilesystemInterceptorProvider.toFileProxy(fo));
                    } finally {
                        getFileSystem().setInsideVCS(false);
                    }
                }
                fireRemoteFileObjectCreated(fo);
            }
            for (DirEntry entry : entriesToFireChanged) {
                RemoteFileObjectBase fo = getCachedChild(entry.getName());
                if (fo != null) {
                    if (fo.isPendingRemoteDelivery()) {
                        RemoteLogger.getInstance().log(Level.FINE, "Skipping change event for pending file {0}", fo);
                    } else {
                        final long time = fo.lastModified().getTime();
                        fo.fireFileChangedEvent(fo.getListeners(), new FileEvent(fo.getOwnerFileObject(), fo.getOwnerFileObject(), expected, time));
                        this.fireFileChangedEvent(this.getListeners(), new FileEvent(this.getOwnerFileObject(), fo.getOwnerFileObject(), expected, time));
                    }
                }
            }
            fireReadOnlyChangedEventsIfNeed(entriesToFireChangedRO);
            // we check "org.netbeans.modules.masterfs.watcher.disable" property to be on par with masterfs,
            // which does the same and also sets this flag in tests 
            if (interceptor != null && !Boolean.getBoolean("org.netbeans.modules.masterfs.watcher.disable")) {
                try {
                    getFileSystem().setInsideVCS(true);
                    getFileSystem().setGettingDirectoryStorage(true);
                    interceptor.refreshRecursively(FilesystemInterceptorProvider.toFileProxy(getOwnerFileObject()), 
                            lastModified().getTime(), new LinkedList<>()); // Collections.emptyList() does not suite - implementor can add elements

                } finally {
                    getFileSystem().setInsideVCS(false);
                    getFileSystem().setGettingDirectoryStorage(false);
                }
            }
            //fireFileChangedEvent(getListeners(), new FileEvent(this));
        }
        return storage;
    }

    private void fireReadOnlyChangedEventsIfNeed(List<DirEntry> entriesToFireChangedRO) {
        for (DirEntry entry : entriesToFireChangedRO) {
            RemoteFileObjectBase fo = getCachedChild(entry.getName());
            if (fo != null) {
                if (fo.isPendingRemoteDelivery()) {
                    RemoteLogger.getInstance().log(Level.FINE, "Skipping change r/o event for pending file {0}", fo);
                } else {
                    fo.fireReadOnlyChangedEvent();
                }
            }
        }
    }

    private void fireDeletedEvent(RemoteFileObject parent, RemoteFileObject fo, boolean expected, boolean recursive) {

        FilesystemInterceptorProvider.FilesystemInterceptor interceptor =
                USE_VCS ? FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem()) : null;

        if (recursive) {
            RemoteFileObjectBase[] children = fo.getImplementor().getExistentChildren(true);
            for (RemoteFileObjectBase c : children) {
                Enumeration<FileChangeListener> listeners = c.getListeners();
                RemoteFileObject childFO = c.getOwnerFileObject();
                if (interceptor != null) {
                    try {
                        getFileSystem().setInsideVCS(true);
                        getFileSystem().setExternallyRemoved(childFO.getImplementor());
                        try {
                            interceptor.deletedExternally(FilesystemInterceptorProvider.toFileProxy(childFO));
                        } finally {
                            getFileSystem().setExternallyRemoved(null);
                        }
                    } finally {
                        getFileSystem().setInsideVCS(false);
                    }
                }
                c.fireFileDeletedEvent(listeners, new FileEvent(childFO, childFO, expected));
                RemoteFileObjectBase p = c.getParent();
                p.fireFileDeletedEvent(p.getListeners(), new FileEvent(p.getOwnerFileObject(), childFO, expected));
            }
        }
        if (interceptor != null) {
            getFileSystem().setExternallyRemoved(fo.getImplementor());
            try {
                getFileSystem().setInsideVCS(true);
                interceptor.deletedExternally(FilesystemInterceptorProvider.toFileProxy(fo));
            } finally {
                getFileSystem().setInsideVCS(false);
                getFileSystem().setExternallyRemoved(null);
            }
        }
        fo.fireFileDeletedEvent(fo.getImplementor().getListeners(), new FileEvent(fo, fo, expected));
        parent.fireFileDeletedEvent(parent.getImplementor().getListeners(), new FileEvent(parent, fo, expected));
    }

//    InputStream _getInputStream(RemotePlainFile child) throws
//            ConnectException, IOException, InterruptedException, CancellationException, ExecutionException {
//        Lock lock = RemoteFileSystem.getLock(child.getCache()).readLock();
//        lock.lock();
//        try {
//            if (child.getCache().exists()) {
//                return new FileInputStream(child.getCache());
//            }
//        } finally {
//            lock.unlock();
//        }
//        checkConnection(child, true);
//        DirectoryStorage storage = getDirectoryStorage(child.getNameExt()); // do we need this?
//        return new CachedRemoteInputStream(child, getExecutionEnvironment());
//    }

    @Override
    public void warmup(FileSystemProvider.WarmupMode mode, Collection<String> extensions) {
        switch(mode) {
            case FILES_CONTENT:
                warmupFiles(extensions);
                break;
            case RECURSIVE_LS:
                warmupDirs();
                break;
            default:
                Exceptions.printStackTrace(new IllegalAccessException("Unexpected warmup mode: " + mode)); //NOI18N
        }
    }

    private void warmupFiles(Collection<String> extensions) {
        if (ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            File zipFile = new File(getCache(), RemoteFileSystem.CACHE_ZIP_FILE_NAME);
            if (!zipFile.exists()) {
                File zipPartFile = new File(getCache(), RemoteFileSystem.CACHE_ZIP_PART_NAME);
                getFileSystem().getZipper().schedule(zipFile, zipPartFile, getPath(), extensions);
            }
        }
    }

    boolean isSuspendedWritesUpload() {
        //return getFileSystem().getSuspendInfo(this) != null;
        for(RemoteFileObjectBase fo = this; fo != null; fo = fo.getParent()) {
            if (fo.getFlag(MASK_SUSPEND_WRITES)) {
                return true;
            }
        }
        return false;
    }

    void suspendWritesUpload() {
        setFlag(MASK_SUSPEND_WRITES, true);
    }

    /**
     * NB: there are several flaws in the implementation:
     * #1: it does NOT support links inside directory
     * #2: it does NOT file and caches names transformation, they will be exactly the same, 
     * so you can get into trouble it 2 situations:
     *  a) if your local file system is case insensitive and there are files that differ only in case, 
     *  b) if your file name is forbidden on the local file system (like COM1, etc on Windows)
     * #3: it's callers responsibility to call resume in finally block 
     * and to call it on the same directory suspend was called
     * #4: Weird usages such as "suspend and never resume", "suspend twice", "resume twicw" lead to unpredictable results,
     * However, this works well when creating projects - and that was the main goal of introducing this
     */
    void resumeWritesUpload() throws IOException, InterruptedException, ConnectException {
        SuspendInfo suspendInfo = getFileSystem().removeSuspendInfo(this);
        setFlag(MASK_SUSPEND_WRITES, false);
        if (suspendInfo == null) {
            return;
        }
        try {
            final ExecutionEnvironment env = getExecutionEnvironment();
            if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
            }
            List<RemoteFileObjectBase> files = suspendInfo.getAllSuspended();
            if (files == null || files.isEmpty()) {
                return;
            }
            File zipFile = File.createTempFile("rfs_local", ".zip"); // NOI18N
            try {
                try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                    for (RemoteFileObjectBase fo : files) {
                        if (fo instanceof RemotePlainFile) {
                            String path = fo.getPath();
                            if (path.startsWith(getPath()) && path.length() > getPath().length() + 1 && path.charAt(getPath().length()) == '/') {
                                String relPath = path.substring(getPath().length() + 1);
                                ZipEntry entry = new ZipEntry(relPath);
                                entry.setTime(System.currentTimeMillis() - TimeZone.getDefault().getRawOffset());
                                zipStream.putNextEntry(entry);
                                try (FileInputStream fis = new FileInputStream(fo.getCache())) {
                                    FileUtil.copy(fis, zipStream);
                                }
                            } else {
                                // TODO: log it!
                            }
                        }
                    }
                }
                uploadAndUnzip(zipFile, false);
            } finally {
                zipFile.delete();
                this.refresh(true);
            }
        } finally {
            for (RemoteFileObjectBase fo : suspendInfo.getAllSuspended()) {
                fo.setFlag(MASK_SUSPENDED_DUMMY, false);
            }
            suspendInfo.dispose();
        }
    }

    /** NB: zip entries time should be in UTC */
    void uploadAndUnzip(InputStream zipStream) throws ConnectException, InterruptedException, IOException {        
        final ExecutionEnvironment env = getExecutionEnvironment();        
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            zipStream.close();
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
        }        
        // we have to copy the content into temporary local file, since we need to use it twice:
        // 1) to copy to remote and 2) to unzip into local cache
        File localZipFile = File.createTempFile(".rfs_local", ".zip"); // NOI18N
        try {
            // copy zip stream to local zip file
            try (FileOutputStream os = new FileOutputStream(localZipFile)) {
                FileUtil.copy(zipStream, os);
            } finally {
                zipStream.close();
            }
            uploadAndUnzip(localZipFile, true);
        } finally {
            if (localZipFile != null) {
                localZipFile.delete();
            }
        }        
    }

    /** NB: zip entries time should be in UTC */
    @SuppressWarnings("ReplaceStringBufferByString")
    private void uploadAndUnzip(File localZipFile, boolean alsoUnzipToCache) throws InterruptedException, IOException  {
        final ExecutionEnvironment env = getExecutionEnvironment();
        // Copy local zip file to remote
        String remoteZipPath = getPath() + '/' + ".rfs_tmp_" + System.currentTimeMillis() + ".zip"; // NOI18N
        boolean success;
        String errorMessage = null;
        try {
            CommonTasksSupport.UploadStatus uploadStatus = CommonTasksSupport.uploadFile(localZipFile, env, remoteZipPath, 0600).get();
            success = uploadStatus.isOK();
            if (!success) {
                errorMessage = uploadStatus.getError();
            }
        } catch (ExecutionException ex) {
            success = false;
            errorMessage = ex.getMessage();
        }
        if (!success) {
            CommonTasksSupport.rmFile(env, remoteZipPath, null);
            throw new IOException(errorMessage + " when uploading " + localZipFile + " to " + remoteZipPath); //NOI18N
        }
        StringBuilder script = new StringBuilder("TZ=UTC "); // NOI18N
        script.append("unzip -q -o \"").append(remoteZipPath); // NOI18N
        script.append("\" && rm \"").append(remoteZipPath).append("\""); //NOI18N
//            if (adjustLineEndings && Utils.isWindows()) {
//                script.append(" && (which dos2unix > /dev/null; if [ $? = 0 ]; then find . -name \"*[Mm]akefile*\" -exec dos2unix {}  \\; ; else echo \"no_dos2unix\"; fi)"); //NOI18N
//            }
        ProcessUtils.ExitStatus rc = ProcessUtils.executeInDir(getPath(), env,
                "sh", "-c", script.toString()); //NOI18N
        if (!rc.isOK()) {
            throw new IOException(rc.getErrorString() + " when unzipping and removing " + remoteZipPath + " in " + this); //NOI18N
        }
        if (alsoUnzipToCache) {
            getCache().mkdirs();
            try (InputStream is = new FileInputStream(localZipFile)) {
                RemoteFileSystemUtils.unpackZipFile(is, getCache());
            }
        }
        class CacheFiller {
            void fillRecursively(File cacheDir) throws IOException {
                File cacheList = new File(cacheDir, RemoteFileSystem.CACHE_FILE_NAME);
                if (!cacheList.exists()) {
                    // We need to create a .rfs_cache files for each directory, otherwise it won't be refreshed -
                    // and changing this logic in refreshImpl is too dangerous.                    
                    //final Collection<DirEntryImpl> entries = DirEntryImpl.createFromCacheDir(cacheDir);
                    // We create an empty cache file: in suspend mode no events were raised, 
                    // so let all "file created" events be raised now.
                    DirectoryStorage.store(cacheList, Collections.<DirEntryImpl>emptyList() /*entries*/);
                }
            }
        }
        new CacheFiller().fillRecursively(getCache());
        try {
            refreshImpl(true, null, true, RefreshMode.DEFAULT, 0);
        } catch (TimeoutException ex) {
            RemoteFileSystemUtils.reportUnexpectedTimeout(ex, this);
        } catch (ExecutionException ex) {
            throw new IOException(ex);
        }
    }
    
    private boolean ensureChildSyncFromZip(RemotePlainFile child) {
        File file = new File(getCache(), RemoteFileSystem.CACHE_ZIP_FILE_NAME);
        if (file.exists()) {
            ZipFile zipFile = null;
            InputStream is = null;
            OutputStream os = null;
            boolean ok = false;
            try {
                zipFile = new ZipFile(file);
                String path = child.getPath();
                RemoteLogger.assertTrue(path.startsWith("/")); //NOI18N
                path = path.substring(1); // remove starting '/'
                ZipEntry zipEntry = zipFile.getEntry(path);
                if (zipEntry != null) {
                    if (zipEntry.getSize() != child.getSize()) {
                        return false;
                    }
                    long zipTime = zipEntry.getTime();
                    long childTime = child.lastModified().getTime() - TimeZone.getDefault().getRawOffset();
                    zipTime /= 1000;
                    childTime /= 1000;
                    if (childTime%2 == 1 && zipTime%2 == 0) {
                        childTime ++; // zip rounds up to 2 seconds
                    }
                    long delta = zipTime - childTime;
                    boolean same;
                    if (delta == 0) {
                        same = true;
                    } else {
                        // on some servers (e.g. townes) timezone for /usr/include and /export/home differs
                        // below is a temporary workaround
                        if (delta%3600 == 0) {
                            long hours = delta / 3600;
                            same = -23 <= hours && hours <= 23;
                        } else {
                            same = false;
                        }
                    }
                    if (same) {
                        is = zipFile.getInputStream(zipEntry);
                        os = new FileOutputStream(child.getCache());
                        FileUtil.copy(is, os);
                        ok = true;
                    } else {
                        RemoteLogger.finest("Zip timestamp differ for {0}", child); //NOI18N
                    }
                }
            } catch (IOException ex) {
                RemoteLogger.fine(ex);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        ok = false;
                        RemoteLogger.fine(ex);
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        RemoteLogger.fine(ex);
                    }
                }
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException ex) {
                        RemoteLogger.fine(ex);
                    }
                }
                return ok;
            }
        } else {
            RemoteDirectory parent = getParentImpl();
            if (parent != null) {
                return parent.ensureChildSyncFromZip(child);
            }
        }
        return false;
    }

    private static boolean isLoadingInEditor() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if ("org.openide.text.DocumentOpenClose$DocumentLoad".equals(element.getClassName())) { //NOI18N
                if ("atomicLockedRun".equals(element.getMethodName())) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    private boolean cacheExists(RemotePlainFile child) {
        Lock lock = getLockSupport().getCacheLock(child).readLock();
        lock.lock();
        try {
            return child.getCache().exists();
        } finally {
            lock.unlock();
        }        
    }
    
    /*package*/ void ensureChildSync(RemotePlainFile child) throws
            TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        if (cacheExists(child)) {
            if(isLoadingInEditor() && ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                child.refreshImpl(false, null, false, RefreshMode.DEFAULT);
            }
            if (cacheExists(child)) {
                return;
            }
        }        
        checkConnection(child, true);
        DirectoryStorage storage = getDirectoryStorage(child.getNameExt()); // do we need this?
        Lock lock = getLockSupport().getCacheLock(child).writeLock();
        lock.lock();
        try {
            if (child.getCache().exists()) {
                return;
            }
            final File cacheParentFile = child.getCache().getParentFile();
            if (!cacheParentFile.exists()) {
                cacheParentFile.mkdirs();
                if (!cacheParentFile.exists()) {
                    throw new IOException("Unable to create parent firectory " + cacheParentFile.getAbsolutePath()); //NOI18N // new IOException sic - should never happen
                }
            }
            if (ensureChildSyncFromZip(child)) {
                return; // cleanup is in finally block
            }
            StringWriter errorWriter = new StringWriter();
            Future<Integer> task = CommonTasksSupport.downloadFile(child.getPath(), getExecutionEnvironment(), child.getCache().getAbsolutePath(), errorWriter);
            int rc = task.get().intValue();
            if (rc == 0) {
                getFileSystem().incrementFileCopyCount();
            } else {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                        "EXC_CanNotDownload", getDisplayName(child.getPath()), errorWriter.toString())); //NOI18N
            }
        } catch (InterruptedException | ExecutionException ex) {
            child.getCache().delete();
            throw ex;
        } finally {
            lock.unlock();
        }
    }

    private void checkConnection(RemoteFileObjectBase fo, boolean throwConnectException) throws ConnectException {
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            getFileSystem().addPendingFile(fo);
            if (throwConnectException) {
                throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(getExecutionEnvironment()));
            }
        }
    }

    @Override
    public FileType getType() {
        return FileType.Directory;
    }

    @Override
    public final InputStream getInputStream(boolean checkLock) throws FileNotFoundException {
        throw new FileNotFoundException(getPath()); // new IOException sic!- should never be called
    }

    public byte[] getMagic(RemoteFileObjectBase file) {
        return getMagicCache().get(file.getNameExt());
    }

    private MagicCache getMagicCache() {
        MagicCache magic;
        synchronized (magicLock) {
            magic = magicCache.get();
            if (magic == null) {
                magic = new MagicCache(this);
                magicCache = new SoftReference<>(magic);
            }
        }
        return magic;
    }

    private void dropMagic() {
        synchronized (magicLock) {
            MagicCache magic = magicCache.get();
            if (magic != null) {
                magic.clean(null);
                magicCache = new SoftReference<>(null);
            } else {
                new MagicCache(this).clean(null);
            }
        }
    }

    @Override
    protected final OutputStream getOutputStreamImpl(final FileLock lock, RemoteFileObjectBase orig) throws IOException {
        throw new IOException("Can not write into a directory " + getDisplayName()); // new IOException sic!- should never be called // NOI18N
    }

    private RemoteFileObject invalidate(DirEntry oldEntry) {
        RemoteFileObject fo = getFileSystem().getFactory().invalidate(getPath() + '/' + oldEntry.getName());
        File oldEntryCache = new File(getCache(), oldEntry.getCache());
        removeFile(oldEntryCache);
        return fo;
    }

    private void removeFile(File cache) {
        if (cache.isDirectory()) {
            File[] children = cache.listFiles();
            if (children != null) {
                for (File child : children) {
                    removeFile(child);
                }
            }
        }
        cache.delete();
    }

    @Override
    public void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected, RefreshMode refreshMode, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        if (antiLoop != null) {
            if (antiLoop.contains(getPath())) {
                return;
            } else {
                antiLoop.add(getPath());
            }
        }
        DirectoryStorage storage = getExistingDirectoryStorage();
        if (storage ==  null ||storage == DirectoryStorage.EMPTY) {
            if (!getFlag(CONNECTION_ISSUES)) {
                return;
            }
        }
        // unfortunately we can't skip refresh if there is a storage but no children exists
        // in this case we have to reafresh just storage - but for the time being only RemoteDirectory can do that
        // TODO: revisit this after refactoring cache into a separate class(es)
        try {
            DirectoryStorage refreshedStorage = refreshDirectoryStorage(null, expected);
            if (recursive) {
                for (RemoteFileObjectBase child : getExistentChildren(refreshedStorage)) {
                    child.refreshImpl(true, antiLoop, expected, RefreshMode.FROM_PARENT);
                }
            }
        } catch (FileNotFoundException ex) {
            final RemoteDirectory parent = getParentImpl();
            if (parent != null) {
                parent.refreshImpl(false, antiLoop, expected, refreshMode);
            } else {
                throw ex;
            }
        }
    }

    private void trace(String message, Object... args) {
        if (trace) {
            message = "SYNC [" + getPath() + "][" + System.identityHashCode(this) + "][" + Thread.currentThread().getId() + "]: " + message; // NOI18N
            RemoteLogger.getInstance().log(Level.FINEST, message, args);
        }
    }

    private static boolean equals(String s1, String s2) {
        return (s1 == null) ? (s2 == null) : s1.equals(s2);
    }

    private DirEntry getChildEntry(RemoteFileObjectBase child) {
        try {
            DirectoryStorage directoryStorage = getDirectoryStorage(child.getNameExt());
            if (directoryStorage != null) {
                DirEntry entry = directoryStorage.getValidEntry(child.getNameExt());
                if (entry != null) {
                    return entry;
                } else {
                    RemoteLogger.getInstance().log(Level.INFO, "Not found entry for file {0}", child); // NOI18N
                }
            }
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex, this);
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException ex) {
            RemoteLogger.finest(ex, this);
        }
        return null;
    }

    long getSize(RemoteFileObjectBase child) {
        DirEntry childEntry = getChildEntry(child);
        if (childEntry != null) {
            return childEntry.getSize();
        }
        return 0;
    }

    /*package*/ Date lastModified(RemoteFileObjectBase child) {
        DirEntry childEntry = getChildEntry(child);
        if (childEntry != null) {
            return childEntry.getLastModified();
        }
        return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
    }

    /** for tests ONLY! */
    /*package*/ DirectoryStorage testGetExistingDirectoryStorage() {
        return getExistingDirectoryStorage();
    }

    private File getStorageFile() {
        return new File(getCache(), RemoteFileSystem.CACHE_FILE_NAME);
    }

    @Override
    public boolean hasCache() {
        return getStorageFile().exists();
    }

    @Override
    public void diagnostics(boolean recursive) {
        RemoteFileObjectBase[] existentChildren = getExistentChildren();
        System.err.printf("\nRemoteFS diagnostics for %s\n", this); //NOI18N
        System.err.printf("Existing children count: %d\n", existentChildren.length); //NOI18N
        File cache = getStorageFile();
        System.err.printf("Cache file: %s\n", cache.getAbsolutePath()); //NOI18N
        System.err.printf("Cache content: \n"); //NOI18N
        printFile(cache, System.err);
        System.err.printf("Existing children:\n"); //NOI18N
        for (RemoteFileObjectBase fo : existentChildren) {
            System.err.printf("\t%s [%s] %d\n",  //NOI18N
                    fo.getNameExt(), fo.getCache().getName(), fo.getCache().length());
        }
        if (recursive) {
            for (RemoteFileObjectBase fo : existentChildren) {
                fo.diagnostics(recursive);
            }
        }
    }

    private static void printFile(File file, PrintStream out) {
        BufferedReader rdr = null;
        try {
            rdr = Files.newBufferedReader(file.toPath(), Charset.forName("UTF-8")); // NOI18N
            try {
                String line;
                while ((line = rdr.readLine()) != null) {
                    out.printf("%s\n", line); // NOI18N
                }
            } finally {
                try {
                    rdr.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            try {
                if (rdr != null) {
                    rdr.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

}
