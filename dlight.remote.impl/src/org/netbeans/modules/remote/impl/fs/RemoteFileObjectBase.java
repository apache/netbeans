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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FileProxyI;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.IOHandler;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class RemoteFileObjectBase {

    private final RemoteFileSystem fileSystem;
    private final RemoteFileObjectBase parent;
    private volatile String remotePath;
    private final CopyOnWriteArrayList<FileChangeListener> listeners = new CopyOnWriteArrayList<>();
    public static final boolean USE_VCS;
    static {
        if ("false".equals(System.getProperty("remote.vcs.suport"))) { //NOI18N
            USE_VCS = false;
        } else {
            USE_VCS = true;
        }
    }

    private volatile short flags;

    private final RemoteFileObject fileObject;

    private static final short MASK_VALID = 1;
    private static final short CHECK_CAN_WRITE = 2;
    private static final short BEING_UPLOADED = 4;
    protected static final short CONNECTION_ISSUES = 8;
    protected static final short MASK_WARMUP = 16;
    protected static final short MASK_CYCLIC_LINK = 32;
    protected static final short MASK_SUSPEND_WRITES = 64;
    protected static final short MASK_SUSPENDED_DUMMY = 128;

    protected RemoteFileObjectBase(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv,
            RemoteFileObjectBase parent, String remotePath) {
        RemoteLogger.assertTrue(execEnv.isRemote());
        //RemoteLogger.assertTrue(cache.exists(), "Cache should exist for " + execEnv + "@" + remotePath); //NOI18N
        this.parent = parent;
        this.remotePath = remotePath; // RemoteFileSupport.fromFixedCaseSensitivePathIfNeeded(remotePath);
        setFlag(MASK_VALID, true);
        this.fileSystem = wrapper.getFileSystem();
        this.fileObject = wrapper;
        wrapper.setImplementor(this);
    }

    public abstract boolean isFolder();
    public abstract boolean isData();
    public abstract RemoteFileObject getFileObject(String name, String ext, @NonNull Set<String> antiLoop);
    public abstract RemoteFileObject getFileObject(String relativePath, @NonNull Set<String> antiLoop);
    public abstract InputStream getInputStream(boolean checkLock) throws FileNotFoundException;
    public abstract RemoteFileObject[] getChildren();
    public abstract FileType getType();

    public RemoteFileObject getOwnerFileObject() {
        return fileObject;
    }

    /** conveniency shortcut */
    protected final void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileChangedEvent(en, fe);
    }

    /** conveniency shortcut */
    protected final void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileDeletedEvent(en, fe);
    }

    /** conveniency shortcut */
    protected final void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
        getOwnerFileObject().fireFileAttributeChangedEvent(en, fe);
    }

    /** conveniency shortcut */
    protected final void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileDataCreatedEvent(en, fe);
    }

    /** conveniency shortcut */
    protected final void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        getOwnerFileObject().fireFileFolderCreatedEvent(en, fe);
    }

    /** conveniency shortcut */
    protected final void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
        getOwnerFileObject().fireFileRenamedEvent(en, fe);
    }

    protected boolean getFlag(short mask) {
        return (flags & mask) == mask;
    }

    protected final void setFlag(short mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask;
        }
    }

    /*package*/ boolean isPendingRemoteDelivery() {
        return getFlag(BEING_UPLOADED);
    }

    /*package*/ void setPendingRemoteDelivery(boolean value) {
        setFlag(BEING_UPLOADED, value);
    }

    public final ExecutionEnvironment getExecutionEnvironment() {
        return fileSystem.getExecutionEnvironment();
    }

    /**
     * local cache of this FileObject (for directory - local dir, for file - local file with content)
     * @return
     */
    protected File getCache() {
        return null;
    }

    public boolean hasCache() {
        return false;
    }

    public final String getPath() {
        return this.remotePath;
    }

    public void addFileChangeListener(FileChangeListener fcl) {
        listeners.add(fcl);
    }

    public void removeFileChangeListener(FileChangeListener fcl) {
        listeners.remove(fcl);
    }

    protected final Enumeration<FileChangeListener> getListeners() {
        return Collections.enumeration(listeners);
    }

    protected final Enumeration<FileChangeListener> getListenersWithParent() {
        return joinListeners(this, getParent());
    }

    protected static final Enumeration<FileChangeListener> joinListeners(RemoteFileObjectBase fo1, RemoteFileObjectBase fo2) {
        if (fo1 == null || fo1.listeners.isEmpty()) {
            return (fo2 == null) ? Collections.<FileChangeListener>emptyEnumeration() : fo2.getListeners();
        } else if (fo2 == null || fo2.listeners.isEmpty()) {
            return fo1.getListeners();
        } else {
            List<FileChangeListener> result = new ArrayList<>(fo1.listeners.size() + fo2.listeners.size());
            result.addAll(fo1.listeners);
            result.addAll(fo2.listeners);
            return Collections.enumeration(result);
        }
    }

    public void addRecursiveListener(FileChangeListener fcl) {
        if (isFolder()) {
            getFileSystem().addFileChangeListener(new RecursiveListener(getOwnerFileObject(), fcl, false));
        } else {
            addFileChangeListener(fcl);
        }
    }

    public void removeRecursiveListener(FileChangeListener fcl) {
        if (isFolder()) {
            getFileSystem().removeFileChangeListener(new RecursiveListener(getOwnerFileObject(), fcl, false));
        } else {
            removeFileChangeListener(fcl);
        }
    }

    public final FileObject createData(String name) throws IOException {
        return createDataImpl(name, "", this);
    }

    public final FileObject createData(String name, String ext) throws IOException {
        return createDataImpl(name, ext, this);
    }

    abstract protected RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException;

    public final FileObject createFolder(String name) throws IOException {
        return createFolderImpl(name, this);
    }

    abstract protected RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException;

    /**
     * Deletes the file, returns parent directory content.
     * Returning parent directory content is for the sake of optimization.
     * For example, fs_server, can do remove and return refreshed content in one call.
     * It can return null if there is no way of doing that more effective than
     * just calling RemoteFileSystemTransport.readDirectory
     * @return parent directory content (can be null - see above)
     */
    protected abstract DirEntryList deleteImpl(FileLock lock) throws IOException;

    /**
     * Called after child creation (sometimes - for now only when copying or moving) or removal.
     * TODO: call after child creation via createData/createFolder
     * @param child is NULL if the file was created (creation is always external => we don't know file object yet),
     * not null if the file was deleted
     */
    protected abstract void postDeleteOrCreateChild(RemoteFileObject child, DirEntryList entryList);


    public final void delete(FileLock lock) throws IOException {
        fileSystem.setBeingRemoved(this);
        try {
            deleteImpl(lock, this);
        } finally {
            fileSystem.setBeingRemoved(null);
        }
    }

    private void deleteImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        if (!checkLock(lock)) {
            throw RemoteExceptions.createIOException(
                    NbBundle.getMessage(RemoteFileObjectBase.class, "EXC_WrongLock")); //NOI18N
        }
        FilesystemInterceptor interceptor = null;
        if (USE_VCS) {
            interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
        }
        DirEntryList entryList = null;
        if (interceptor != null) {
            try {
                getFileSystem().setInsideVCS(true);
                FileProxyI fileProxy = FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject());
                IOHandler deleteHandler = interceptor.getDeleteHandler(fileProxy);
                if (deleteHandler != null) {
                    deleteHandler.handle();
                    getFileSystem().getFactory().vcsRegisterUnconfirmedDeletion(remotePath);
                } else {
                    entryList = deleteImpl(lock);
                }
                // TODO remove attributes
                // TODO clear cache?
                // TODO fireFileDeletedEvent()?
                interceptor.deleteSuccess(fileProxy);
            } finally {
                getFileSystem().setInsideVCS(false);
            }
        } else {
            entryList = deleteImpl(lock);
        }
        RemoteFileObject fo = getOwnerFileObject();
        for(Map.Entry<String, Object> entry : getAttributesMap().entrySet()) {
            fo.fireFileAttributeChangedEvent(getListenersWithParent(), new FileAttributeEvent(fo, fo, entry.getKey(), entry.getValue(), null));
        }
//        FileEvent fe = new FileEvent(fo, fo, true);
//        for(RemoteFileObjectBase child: getExistentChildren(true)) {
//            fo.fireFileDeletedEvent(Collections.enumeration(child.listeners), fe);
//        }
        RemoteFileObjectBase p = getParent();
        if (p != null) {
            p.postDeleteOrCreateChild(getOwnerFileObject(), entryList);
        }
    }

    public String getExt() {
        String nameExt = getNameExt();
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? "" : nameExt.substring(pointPos + 1);
    }

    public RemoteFileSystem getFileSystem() {
        return fileSystem;
    }

    public String getName() {
        String nameExt = getNameExt();
        int pointPos = nameExt.lastIndexOf('.');
        return (pointPos < 0) ? nameExt : nameExt.substring(0, pointPos);
    }

    public String getNameExt() {
        int slashPos = this.getPath().lastIndexOf('/');
        return (slashPos < 0) ? "" : this.getPath().substring(slashPos + 1);
    }

    public final OutputStream getOutputStream(FileLock lock) throws IOException {
        return getOutputStreamImpl(lock, this);
    }

    protected OutputStream getOutputStreamImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        throw new ReadOnlyException();
    }

    protected byte[] getMagic() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.getMagic(this);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    private void populateWithChildren(RemoteFileObjectBase rfl, List<RemoteFileObjectBase> children) {
        children.add(rfl);
        for(RemoteFileObjectBase child: rfl.getExistentChildren()) {
            populateWithChildren(child, children);
        }
    }

    protected RemoteFileObjectBase[] getExistentChildren(boolean recursive) {
        if (!recursive) {
            return getExistentChildren();
        }
        List<RemoteFileObjectBase> children = new LinkedList<>();
        populateWithChildren(this, children);
        children.remove(this);
        return children.toArray(new RemoteFileObjectBase[0]);
    }

    protected RemoteFileObjectBase[] getExistentChildren() {
        return new RemoteFileObjectBase[0];
    }

    public final RemoteFileObjectBase getParent() {
        return parent;
    }

    public long getSize() {
        RemoteDirectory canonicalParent;
        try {
            canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.getSize(this);
            }
        } catch (IOException ex) {
            reportIOException(ex);
        }
        return 0;
    }

    @Deprecated
    public final boolean isReadOnly() {
        return isReadOnlyImpl(this);
    }

    protected boolean isReadOnlyImpl(RemoteFileObjectBase orig) {
        if (USE_VCS) {
            FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
            if (interceptor != null) {
                try {
                    getFileSystem().setInsideVCS(true);
                    return !canWriteImpl(orig) && isValid();
                } finally {
                    getFileSystem().setInsideVCS(false);
                }
            }
        }
        return !canRead();
    }

    public boolean canRead() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return true;
            } else {
                return canonicalParent.canRead(getNameExt());
            }
        } catch (IOException ex) {
            reportIOException(ex);
            return true;
        }
    }


    public boolean canExecute() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return true;
            } else {
                return canonicalParent.canExecute(getNameExt());
            }
        } catch (IOException ex) {
            reportIOException(ex);
            return true;
        }
    }

    void connectionChanged() {
        if (getFlag(CHECK_CAN_WRITE)) {
            setFlag(CHECK_CAN_WRITE, false);
            fireReadOnlyChangedEvent();
        }
    }

    final void fireReadOnlyChangedEvent() {
        fireFileAttributeChangedEvent("DataEditorSupport.read-only.refresh", null, null);  //NOI18N
    }

    final void fireFileAttributeChangedEvent(final String attrName, final Object oldValue, final Object newValue) {
        Enumeration<FileChangeListener> pListeners = (parent != null) ? parent.getListeners() : null;

        fireFileAttributeChangedEvent(getListeners(), new FileAttributeEvent(getOwnerFileObject(), getOwnerFileObject(), attrName, oldValue, newValue));

        if (parent != null && pListeners != null) {
            parent.fireFileAttributeChangedEvent(pListeners, new FileAttributeEvent(parent.getOwnerFileObject(), getOwnerFileObject(), attrName, oldValue, newValue));
        }
    }

    public final boolean canWrite() {
        return canWriteImpl(this);
    }

    public final FileSystemProvider.Stat getStat() {
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.getStat(getNameExt());
            } else {
                return FileSystemProvider.Stat.createInvalid();
            }
        } catch (ConnectException ex) {
            return FileSystemProvider.Stat.createInvalid();
        } catch (IOException ex) {
            reportIOException(ex);
            return FileSystemProvider.Stat.createInvalid();
        }
    }

    protected boolean canWriteImpl(RemoteFileObjectBase orig) {
        if (getFlag(MASK_SUSPENDED_DUMMY)) {
            return true;
        }
        setFlag(CHECK_CAN_WRITE, true);
        if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
            getFileSystem().addReadOnlyConnectNotification(this);
            return false;
        }
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent == null) {
                return false;
            } else {
                boolean result = canonicalParent.canWrite(getNameExt());
                if (!result && USE_VCS) {
                    FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
                    if (interceptor != null) {
                        try {
                            getFileSystem().setInsideVCS(true);
                            result = interceptor.canWriteReadonlyFile(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()));
                        } finally {
                            getFileSystem().setInsideVCS(false);
                        }
                    }
                }
                if (!result) {
                    setFlag(CHECK_CAN_WRITE, false); // even if we get disconnected, r/o status won't change
                }
                return result;
            }
        } catch (ConnectException ex) {
            return false;
        } catch (IOException ex) {
            reportIOException(ex);
            return false;
        }
    }

    protected void refreshThisFileMetadataImpl(boolean recursive, Set<String> antiLoop,
            boolean expected, RefreshMode refreshMode, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
    }

    public static enum RefreshMode {
        /** is called because its parent refresh() was called with recursive == true */
        FROM_PARENT,
        //FROM_REFRESH_MANAGER,
        /** other cases */
        DEFAULT
    }

    public void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected, RefreshMode refreshMode) throws ConnectException, IOException, InterruptedException, ExecutionException {
        try {
            refreshImpl(recursive, antiLoop, expected, refreshMode, 0);
        } catch (TimeoutException ex) {
            RemoteFileSystemUtils.reportUnexpectedTimeout(ex, this);
        }
    }

    public void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected,
            RefreshMode refreshMode, int timeout)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
    }

    /*package*/ void nonRecursiveRefresh() {
        try {
            refreshImpl(false, null, true, RefreshMode.DEFAULT);
        } catch (ConnectException | InterruptedException ex) {
            RemoteLogger.finest(ex, this);
        } catch (IOException ex) {
            RemoteLogger.info(ex, this);
        } catch (ExecutionException ex) {
            RemoteLogger.info(ex, this);
        }
    }

    public final void refresh(boolean expected) {
        try {
            refreshImpl(true, null, expected, RefreshMode.DEFAULT);
        } catch (ConnectException | InterruptedException ex) {
            RemoteLogger.finest(ex, this);
        } catch (IOException ex) {
            RemoteLogger.info(ex, this);
        } catch (ExecutionException ex) {
            RemoteLogger.info(ex, this);
        }
    }

    public final void refresh() {
        refresh(false);
    }

    public boolean isRoot() {
        return false;
    }

    protected final boolean isValidFast() {
        return getFlag(MASK_VALID);
    }

    protected final boolean isValidFastWithParents() {
        if (getFlag(MASK_VALID)) {
            RemoteFileObjectBase p = parent;
            while (p != null) {
                if (!p.getFlag(MASK_VALID)) {
                    return false;
                }
                p = p.parent;
            }
            return true;
        }
        return false;
    }

    public boolean isValid() {
        if(getFlag(MASK_VALID)) {
            RemoteFileObjectBase p = getParent();
            return (p == null) || p.isValid();
        }
        return false;
    }

    /*package*/ void invalidate() {
        setFlag(MASK_VALID, false);
    }

    public boolean isVirtual() {
        return false;
    }

    public Date lastModified() {
        if (isPendingRemoteDelivery()) {
            return new Date(-1);
        }
        try {
            RemoteDirectory canonicalParent = RemoteFileSystemUtils.getCanonicalParent(this);
            if (canonicalParent != null) {
                return canonicalParent.lastModified(this);
            }
        } catch (IOException ex) {
            reportIOException(ex);
        }
        return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
    }

    private void reportIOException(IOException ex) {
        System.err.printf("Error in %s: %s\n", remotePath, ex.getMessage());
    }

    public final FileLock lock() throws IOException {
        return lockImpl(this);
    }

    protected FileLock lockImpl(RemoteFileObjectBase orig) throws IOException {
        return getLockSupport().lock(this);
    }

    public boolean isLocked() {
        return getLockSupport().isLocked(this);
    }

    protected boolean checkLock(FileLock aLock) throws IOException {
        return getLockSupport().checkLock(this, aLock);
    }

    public final void rename(FileLock lock, String name, String ext) throws IOException {
        renameImpl(lock, name, ext, this);
    }

    protected void renameImpl(FileLock lock, String name, String ext, RemoteFileObjectBase orig) throws IOException {
        if (!checkLock(lock)) {
            throw RemoteExceptions.createIOException(
                    NbBundle.getMessage(RemoteFileObjectBase.class, "EXC_WrongLock")); //NOI18N
        }
        RemoteFileObjectBase p = getParent();
        if (p != null) {
            String newNameExt = composeName(name, ext);
            if (newNameExt.equals(getNameExt())) {
                // nothing to rename
                return;
            }
            if (!p.isValid()) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileObjectBase.class,
                        "EXC_CanNotRenameIn", p.getDisplayName())); //NOI18N
            }
            // Can not rename in read only folder
            if (!p.canWrite()) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileObjectBase.class,
                        "EXC_CanNotRenameRO", p.getDisplayName()));//NOI18N
            }
            // check there are no other child with such name
            if (p.getOwnerFileObject().getFileObject(newNameExt) != null) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileObjectBase.class,
                        "EXC_CannotRename_AlreadyExists", getNameExt(), newNameExt, // NOI18N
                        getParent().getPath(), getExecutionEnvironment().getDisplayName()));
            }

            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileObjectBase.class,
                        "EXC_CannotRenameNoConnect", p.getDisplayName())); //NOI18N
            }
            try {
                Map<String, Object> map = getAttributesMap();
                p.renameChild(lock, this, newNameExt, orig);
                setAttributeMap(map, this.getOwnerFileObject());
            } catch (ConnectException ex) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileObjectBase.class,
                        "EXC_CanNotRenameIn", p.getDisplayName()), ex); //NOI18N
            } catch (InterruptedException ex) {
                throw RemoteExceptions.createInterruptedIOException(NbBundle.getMessage(RemoteFileObjectBase.class,
                        "EXC_CanNotRenameIn", p.getDisplayName()), ex); // NOI18N
            } catch (ExecutionException ex) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileObjectBase.class,
                        "EXC_CanNotRenameExecutionException", newNameExt, ex.getLocalizedMessage()), ex); // NOI18N
            }
        }
    }

    public FileObject copy(FileObject target, String name, String ext) throws IOException {
        return copyImpl(target, name, ext, this);
    }

    protected FileObject copyImpl(FileObject target, String name, String ext, RemoteFileObjectBase orig) throws IOException {
        if (USE_VCS) {
            FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
            if (interceptor != null) {
                try {
                    getFileSystem().setInsideVCS(true);
                    FileProxyI to = FilesystemInterceptorProvider.toFileProxy(target, name, ext);
                    FileProxyI from = FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject());
                    interceptor.beforeCopy(from, to);
                    FileObject result = null;
                    try {
                        final IOHandler copyHandler = interceptor.getCopyHandler(from, to);
                        if (copyHandler != null) {
                            copyHandler.handle();
                            refresh(true);
                            //perfromance bottleneck to call refresh on folder
                            //(especially for many files to be copied)
                            target.refresh(true); // XXX ?
                            result = target.getFileObject(name, ext); // XXX ?
                            assert result != null : "Cannot find " + target + " with " + name + "." + ext;
                            FileUtil.copyAttributes(getOwnerFileObject(), result);
                        } else {
                            result = RemoteFileSystemUtils.copy(getOwnerFileObject(), target, name, ext);
                        }
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                    interceptor.copySuccess(from, to);
                    return result;
                } finally {
                    getFileSystem().setInsideVCS(false);
                }
            }
        }
        return RemoteFileSystemUtils.copy(getOwnerFileObject(), target, name, ext);
    }

    public final FileObject move(FileLock lock, FileObject target, String name, String ext) throws TimeoutException, IOException {
        return moveImpl(lock, target, name, ext, this);
    }

    protected FileObject moveImpl(FileLock lock, FileObject target, String name, String ext, RemoteFileObjectBase orig) throws TimeoutException, IOException {
        if (!checkLock(lock)) {
            throw RemoteExceptions.createIOException(
                    NbBundle.getMessage(RemoteFileObjectBase.class, "EXC_WrongLock")); //NOI18N
        }
        if (USE_VCS) {
            FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
            if (interceptor != null) {
                try {
                    getFileSystem().setInsideVCS(true);
                    FileProxyI to = FilesystemInterceptorProvider.toFileProxy(target, name, ext);
                    FileProxyI from = FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject());
                    FileObject result = null;
                    try {
                        final IOHandler moveHandler = interceptor.getMoveHandler(from, to);
                        if (moveHandler != null) {
                            Map<String,Object> attr = getAttributesMap();
                            moveHandler.handle();
                            getParent().nonRecursiveRefresh();
                            //perfromance bottleneck to call refresh on folder
                            //(especially for many files to be moved)
                            if (target instanceof RemoteFileObject) {
                                ((RemoteFileObject) target).getImplementor().nonRecursiveRefresh();
                            } else {
                                target.refresh(true);
                            }
                            result = target.getFileObject(name, ext); // XXX ?
                            assert result != null : "Cannot find " + target + " with " + name + "." + ext;
                            //FileUtil.copyAttributes(this, result);
                            if (result instanceof RemoteFileObject) {
                                setAttributeMap(attr, (RemoteFileObject)result);
                            }
                        } else {
                            result = superMove(lock, target, name, ext);
                        }
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                    interceptor.afterMove(from, to);
                    return result;
                } finally {
                    getFileSystem().setInsideVCS(false);
                }
            }
        }
        return superMove(lock, target, name, ext);
    }

    /** Copy-paste from FileObject.copy */
    private FileObject superMove(FileLock lock, FileObject target, String name, String ext) throws IOException, TimeoutException {
        if (getOwnerFileObject().getParent().equals(target)) {
            // it is possible to do only rename
            rename(lock, name, ext);
            return this.getOwnerFileObject();
        } else {
            // have to do copy
            final String from = getPath();
            final String newNameExt = composeName(name, ext);
            final String newPath = target.getPath() + '/' + newNameExt;
            if (target instanceof RemoteFileObject
                    && getExecutionEnvironment().equals(((RemoteFileObject) target).getExecutionEnvironment())
                    && RemoteFileSystemTransport.canMove(getExecutionEnvironment(), from, newPath)) {
                try {
                    RemoteFileSystemTransport.MoveInfo mi = RemoteFileSystemTransport.move(getExecutionEnvironment(), from, newPath);
                    //getParent().refreshImpl(false, null, true, RefreshMode.FROM_PARENT);
                    getParent().postDeleteOrCreateChild(getOwnerFileObject(), mi.from);
                    ((RemoteFileObject) target).getImplementor().postDeleteOrCreateChild(null, mi.to);
                    FileObject movedFO = target.getFileObject(newNameExt);
                    RemoteLogger.assertTrueInConsole(movedFO != null, "null file object after move of \n{0}\n into\n{1}\nwith name {2}", this, target, newNameExt);
                    if (movedFO == null) {
                        throw new IOException("Null file object after move " + getExecutionEnvironment() + ':' + newPath); //NOI18N // nerw IOException sic!
                    }
                    if (USE_VCS) {
                        FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fileSystem);
                        if (interceptor != null) {
                            try {
                                getFileSystem().setInsideVCS(true);
                                FileProxyI fileProxyFrom = FilesystemInterceptorProvider.toFileProxy(fileSystem, from);
                                IOHandler deleteHandler = interceptor.getDeleteHandler(fileProxyFrom);
                                if (deleteHandler != null) {
                                    deleteHandler.handle();
                                }
                                interceptor.deleteSuccess(fileProxyFrom);
                            } finally {
                                getFileSystem().setInsideVCS(false);
                            }
                        }
                    }
                    return movedFO;
                } catch (InterruptedException ex) {
                    throw RemoteExceptions.createIOException(ex.getLocalizedMessage(), ex); //NOI18N
                } catch (ExecutionException ex) {
                    if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                        throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemoteFileObjectBase.class,
                                "EXC_CantRenameFromTo", getDisplayName(getExecutionEnvironment(), from),  //NOI18N
                                newPath, ex.getLocalizedMessage()), ex);
                    } else {
                        throw RemoteExceptions.createIOException(ex.getLocalizedMessage(), ex); // NOI18N
                    }
                }
            } else {
                FileObject dest = getOwnerFileObject().copy(target, name, ext);
                delete(lock);
                return dest;
            }
        }
    }


    private Map<String,Object> getAttributesMap() throws IOException {
        Map<String,Object> map = new HashMap<>();
        Enumeration<String> attributes = getAttributes();
        while(attributes.hasMoreElements()) {
            String attr = attributes.nextElement();
            map.put(attr, getAttribute(attr));
        }
        return map;
    }

    private void setAttributeMap(Map<String,Object> map, RemoteFileObject to) throws IOException {
        for(Map.Entry<String,Object> entry : map.entrySet()) {
            to.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public Object getAttribute(String attrName) {
        return getFileSystem().getAttribute(this, attrName);
    }

    public Enumeration<String> getAttributes() {
        return getFileSystem().getAttributes(this);
    }

    public void setAttribute(String attrName, Object value) throws IOException {
        getFileSystem().setAttribute(this, attrName, value);
    }

    @Deprecated
    public void setImportant(boolean b) {
        // Deprecated. Noithing to do.
    }

    protected abstract void renameChild(FileLock lock, RemoteFileObjectBase toRename, String newNameExt, RemoteFileObjectBase orig)
            throws ConnectException, IOException, InterruptedException, ExecutionException;

    final void renamePath(String newPath) {
        this.remotePath = newPath;
    }

    public void diagnostics(boolean recursive) {}

    private static class ReadOnlyException extends IOException {
        public ReadOnlyException() {
            super("The remote file system is read-only"); //NOI18N
        }
    }

    @Override
    public String toString() {
        String validity;
        if (isValid()) {
            validity = " [valid]"; //NOI18N
        } else {
            validity = getFlag(MASK_VALID) ? " [invalid] (flagged)" : " [invalid]"; //NOI18N
        }
        return getExecutionEnvironment().toString() + ":" + getPath() + validity; // NOI18N
    }

    public static String getDisplayName(ExecutionEnvironment env, String path) {
        return env.getDisplayName() + ':' + (path.isEmpty() ? "/" : path); //NOI18N
    }

    public String getDisplayName(String path) {
        return getDisplayName(getExecutionEnvironment(), path);
    }

    public String getDisplayName() {
        return getDisplayName(getPath());
    }

    public void warmup(FileSystemProvider.WarmupMode mode, Collection<String> extensions) {
    }

    public boolean isSymbolicLink() {
        return false;
    }

    public RemoteFileObjectBase readSymbolicLink() {
        return null;
    }

    public String readSymbolicLinkPath() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        // hash code should not be counted by volatale field.
        //int hash = 3;
        //hash = 11 * hash + (this.getFileSystem() != null ? this.getFileSystem().hashCode() : 0);
        //hash = 11 * hash + (this.getExecutionEnvironment() != null ? this.getExecutionEnvironment().hashCode() : 0);
        //String thisPath = this.getPath();
        //hash = 11 * hash + (thisPath != null ? thisPath.hashCode() : 0);
        //return hash;
        return System.identityHashCode(this);
    }

    protected static String composeName(String name, String ext) {
        return (ext != null && ext.length() > 0) ? (name + "." + ext) : name;//NOI18N
    }

    protected RemoteLockSupport getLockSupport() {
        return fileSystem.getLockSupport();
    }    
}
