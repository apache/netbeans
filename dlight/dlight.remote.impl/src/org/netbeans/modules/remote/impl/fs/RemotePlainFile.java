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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.util.NbBundle;

/**
 *
 */
public final class RemotePlainFile extends RemoteFileObjectWithCache {

    private static final int REFRESH_TIMEOUT = Integer.getInteger("remote.plain.file.refresh.timeout", 5000); // NOI18N
    
//    private SoftReference<CachedRemoteInputStream> fileContentCache = new SoftReference<CachedRemoteInputStream>(null);
    
    /*package*/ RemotePlainFile(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, 
            RemoteDirectory parent, String remotePath, File cache) {
        super(wrapper, fileSystem, execEnv, parent, remotePath, cache);
    }

    @Override
    public final RemoteFileObject[] getChildren() {
        return new RemoteFileObject[0];
    }

    @Override
    public final boolean isFolder() {
        return false;
    }

    @Override
    public boolean isData() {
        return true;
    }

    @Override
    public final RemoteFileObject getFileObject(String name, String ext, @NonNull Set<String> antiLoop) {
        return null;
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath, @NonNull Set<String> antiLoop) {
        // taken from FileObject.getFileObject(String relativePath)
        if (relativePath.startsWith("/")) { //NOI18N
            relativePath = relativePath.substring(1);
        }
        if (!relativePath.equals(".") && !relativePath.contains("..") && !relativePath.contains("/")) { // NOI18N
            return null;
        }
        RemoteFileObject res = this.getOwnerFileObject();
        StringTokenizer st = new StringTokenizer(relativePath, "/"); //NOI18N
        while ((res != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            if (nameExt.equals("..")) { // NOI18N
                res = res.getParent();
            } else {
                if (!nameExt.equals(".")) { //NOI18N
                    res = res.getFileObject(nameExt, antiLoop);
                }
            }
        }
        return res;
    }

    /** just a conveniency shortcut that allows not to cast each time */
    /*package*/ RemoteDirectory getParentImpl() {
        return (RemoteDirectory) getParent(); // cast guaranteed by constructor
    }

    private final class InputStreamWrapper extends InputStream {

        private final InputStream is;
        private boolean closed;
        private final Runnable postClose;

        public InputStreamWrapper(InputStream is, Runnable postClose) {
            this.is = is;
            this.postClose = postClose;
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }

        @Override
        public int available() throws IOException {
            return is.available();
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            try {
                is.close();
                closed = true;
            } finally {
                postClose.run();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            close();
        }        
        
        @Override
        public boolean equals(Object obj) {
            return is.equals(obj);
        }

        @Override
        public int hashCode() {
            return is.hashCode();
        }

        @Override
        public synchronized void mark(int readlimit) {
            is.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return is.markSupported();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return is.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return is.read(b, off, len);
        }

        @Override
        public synchronized void reset() throws IOException {
            is.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return is.skip(n);
        }
    }

    @Override
    public InputStream getInputStream(boolean checkLock) throws FileNotFoundException {
        // TODO: check error processing
        try {
//            CachedRemoteInputStream stream = fileContentCache.get();
//            if (stream != null) {
//                CachedRemoteInputStream reuse = stream.reuse();
//                if (reuse != null) {
//                    return reuse;
//                }
//                fileContentCache.clear();
//            }
//            RemoteDirectory parent = RemoteFileSystemUtils.getCanonicalParent(this);
//            if (parent == null) {
//                return RemoteFileSystemUtils.createDummyInputStream();
//            }
//            InputStream newStream = parent._getInputStream(this);
//            if (newStream instanceof CachedRemoteInputStream) {
//                fileContentCache = new SoftReference<CachedRemoteInputStream>((CachedRemoteInputStream) newStream);
//            } else {
//                if (stream != null) {
//                    fileContentCache.clear();
//                }
//
//            }

            if (RemoteLogger.getInstance().isLoggable(Level.FINEST) &&
                    ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment()) &&
                    !getCache().exists() &&  getOwnerFileObject().isMimeResolving()) {
                new Exception("Shouldn't come here in MIME resolved mode " + this).printStackTrace(System.err); //NOI18N
            }

            if (!checkLock) {
                // we can NOT call ensureChildSync without a lock - this leads to #248449
                // and can eventually cause user data loss
                File cache = getCache();
                if (cache.exists()) {
                    return new FileInputStream(cache);
                } else {
                    // Should we just return an empty stream?
                    final File tmpCache = File.createTempFile(getName().length() < 3 ? getName() + "___" : getName(), getExt()); //NOI18N
                    StringWriter errorWriter = new StringWriter();
                    Future<Integer> task = CommonTasksSupport.downloadFile(getPath(), getExecutionEnvironment(), tmpCache.getAbsolutePath(), errorWriter);
                    int rc = task.get().intValue();
                    if (rc != 0) {
                        throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteDirectory.class,
                                "EXC_CanNotDownload", getDisplayName(tmpCache.getAbsolutePath()), errorWriter.toString())); //NOI18N
                    }
                    return new InputStreamWrapper(new FileInputStream(tmpCache), new Runnable() {
                        @Override
                        public void run() {
                            tmpCache.delete();
                        }
                    });
                }
            } else {
                getLockSupport().tryReadLock(this);
                RemoteFileSystemUtils.getCanonicalParent(this).ensureChildSync(this);
                return new InputStreamWrapper(new FileInputStream(getCache()), new Runnable() {
                    @Override
                    public void run() {
                        getLockSupport().readUnlock(RemotePlainFile.this);
                    }
                });
            }

            //getParent().ensureChildSync(this);
        } catch (ConnectException ex) {
            return new ByteArrayInputStream(new byte[]{});
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
            throw newFileNotFoundException(ex);
        }
    }

    private FileNotFoundException newFileNotFoundException(Exception cause) {
        return RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemotePlainFile.class, 
                "EXC_DoesNotExistXX", cause.getLocalizedMessage()), cause); //NOI18N
    }

    @Override
    public RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException {
        throw RemoteExceptions.createIOException(NbBundle.getMessage(RemotePlainFile.class, 
                "EXC_PlainFileChildren", getDisplayName())); // NOI18N
    }

    @Override
    public RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException {
        throw RemoteExceptions.createIOException(NbBundle.getMessage(RemotePlainFile.class, 
                "EXC_PlainFileChildren", getDisplayName())); // NOI18N
    }

    @Override
    protected FileLock lockImpl(RemoteFileObjectBase orig) throws IOException {
        FilesystemInterceptorProvider.FilesystemInterceptor interceptor = null;
        if (USE_VCS) {
            interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
            if (interceptor != null) {
                if (!canWriteImpl(orig)) {
                    throw RemoteExceptions.createIOException(NbBundle.getMessage(RemotePlainFile.class, 
                            "EXC_CannotLockReadOnlyFile", this.getDisplayName())); // NOI18N
                }
            }
        }
        FileLock lock = super.lockImpl(orig);
        if (interceptor != null) {
            getFileSystem().setInsideVCS(true);
            try {
                interceptor.fileLocked(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()));
            } catch (IOException ex){
                lock.releaseLock();
                throw ex;
            } finally {
                getFileSystem().setInsideVCS(false);
            }
        }
        return lock;
    }

    @Override
    protected void postDeleteOrCreateChild(RemoteFileObject child, DirEntryList entryList) {
        RemoteLogger.getInstance().log(Level.WARNING, "postDeleteChild is called on {0}", getClass().getSimpleName());
    }

    @Override
    protected DirEntryList deleteImpl(FileLock lock) throws IOException {
        return RemoteFileSystemTransport.delete(getExecutionEnvironment(), getPath(), false);
    }

    @Override
    protected void renameChild(FileLock lock, RemoteFileObjectBase toRename, String newNameExt, RemoteFileObjectBase orig)
            throws ConnectException, IOException, InterruptedException, ExecutionException {
        // plain file can not be container of children
        RemoteLogger.assertTrueInConsole(false, "renameChild is not supported on " + this.getClass() + " path=" + getPath()); // NOI18N
    }

    @Override
    protected OutputStream getOutputStreamImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        try {
            if (!isValid()) {
                throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemotePlainFile.class, 
                        "EXC_InvalidFO", getDisplayName())); //NOI18N
            }
            FilesystemInterceptorProvider.FilesystemInterceptor interceptor = null;
            if (USE_VCS) {
                interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(getFileSystem());
            }
            getLockSupport().tryWriteLock(this); // if can't lock, throws FileAlreadyLockedException
            // setInsideVCS() is inside
            return new DelegateOutputStream(interceptor, orig);
        } catch (InterruptedException ex) {
            throw RemoteExceptions.createInterruptedIOException(ex.getLocalizedMessage(), ex); // NOI18N
        }
    }

    @Override
    public void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected, RefreshMode refreshMode) throws ConnectException, IOException, InterruptedException, ExecutionException {
        try {
            refreshImpl(recursive, antiLoop, expected, refreshMode, REFRESH_TIMEOUT);
        } catch (TimeoutException ex) {
            RemoteLogger.info("Timeout {0} ms when refreshing {0}", REFRESH_TIMEOUT, this);
        }
    }

    @Override
    public void refreshImpl(boolean recursive, Set<String> antiLoop, 
            boolean expected, RefreshMode refreshMode, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        if (refreshMode != RefreshMode.FROM_PARENT && Boolean.valueOf(System.getProperty("cnd.remote.refresh.plain.file", "true"))) { //NOI18N
            long time = System.currentTimeMillis();
            final DirEntry oldEntry = getParentImpl().getDirEntry(getNameExt());
            boolean refreshParent = false;
            boolean updateStat = false;
            boolean fireChangedRO = false;
            boolean removeCache = false;
            DirEntry newEntry = null;
            try {
                newEntry = RemoteFileSystemTransport.lstat(getExecutionEnvironment(), getPath(), timeoutMillis);
            } catch (ExecutionException ex) {
                if (!RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                    throw ex;
                }
            }
            if (newEntry == null || oldEntry == null || !oldEntry.isValid()) {
                refreshParent = true;
            } else {
                // oldEntry != null && oldEntry.isValid
                assert newEntry.getName().equals(oldEntry.getName());
                if (oldEntry.isSameType(newEntry)) {
                   if (!newEntry.isSameLastModified(oldEntry)) {
                       updateStat = true;
                       removeCache = true;
                   } else if (newEntry.getSize() != oldEntry.getSize()) {
                       updateStat = true;
                       removeCache = true;
                   } else if (newEntry.getDevice() != oldEntry.getDevice()) {
                       updateStat = true;
                       removeCache = true;
                   } else if (newEntry.getINode()!= oldEntry.getINode()) {
                       updateStat = true;
                       removeCache = true;
                   } 
                   if (!newEntry.isSameAccess(oldEntry)) {
                       updateStat = true;
                       // removeCache stays as it was: 
                       // of only r/o-r/w chanegd, no need to remove cache
                       fireChangedRO = true;
                   }
                } else {
                    refreshParent = true;
                }
            }
            if (refreshParent) {
                getParent().refreshImpl(false, antiLoop, expected, refreshMode);            
            } else if (updateStat) {
                if (removeCache) {
                    getCache().delete();
                }
                updateStatAndSendEvents(newEntry, fireChangedRO);
            }
            RemoteLogger.getInstance().log(Level.FINE, "Refreshing {0} took {1} ms", new Object[] { getPath(), System.currentTimeMillis() - time });
        }
    }

    @Override
    public FileType getType() {
        return FileType.Regular;
    }

    private void updateStatAndSendEvents(DirEntry dirEntry, boolean fireChangedRO) {
        getParentImpl().updateStat(this, dirEntry);
        FileEvent ev = new FileEvent(getOwnerFileObject(), getOwnerFileObject(), false, dirEntry.getLastModified().getTime());
        getOwnerFileObject().fireFileChangedEvent(getListeners(), ev);
        RemoteDirectory parent = getParentImpl();
        if (parent != null) {
            ev = new FileEvent(parent.getOwnerFileObject(), getOwnerFileObject(), false, dirEntry.getLastModified().getTime());
            parent.getOwnerFileObject().fireFileChangedEvent(parent.getListeners(), ev);
            if (fireChangedRO) {
                fireReadOnlyChangedEvent();
            }
        }
    }
    
    private class DelegateOutputStream extends OutputStream {

        private final FileOutputStream delegate;
        private boolean closed;

        public DelegateOutputStream(FilesystemInterceptorProvider.FilesystemInterceptor interceptor, RemoteFileObjectBase orig) throws IOException {
            if (interceptor != null) {
                try {
                    getFileSystem().setInsideVCS(true);
                    interceptor.beforeChange(FilesystemInterceptorProvider.toFileProxy(orig.getOwnerFileObject()));
                } finally {
                    getFileSystem().setInsideVCS(false);
                }
            }
            delegate = new FileOutputStream(RemotePlainFile.this.getCache());
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            try {
                delegate.close();
                RemotePlainFile.this.setPendingRemoteDelivery(true);
                SuspendInfo suspendInfo = getParentImpl().getFileSystem().getSuspendInfo(getParentImpl());
                if (suspendInfo != null) {
                    suspendInfo.addSuspended(RemotePlainFile.this);
                    return;
                }
                String pathToRename, pathToUpload;

                if (RemotePlainFile.this.getParent().canWrite()) {
                    // that's what emacs does:
                    pathToRename = RemotePlainFile.this.getPath();
                    pathToUpload = PathUtilities.getDirName(pathToRename) +
                            "/#" + PathUtilities.getBaseName(pathToRename) + "#"; //NOI18N
                } else {
                    ExecutionEnvironment env = RemotePlainFile.this.getExecutionEnvironment();
                    if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                        throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
                    }
                    pathToRename = null;
                    pathToUpload = RemotePlainFile.this.getPath(); //NOI18N
                }
                try {
                    DirEntry dirEntry = RemoteFileSystemTransport.uploadAndRename(
                            RemotePlainFile.this.getExecutionEnvironment(), RemotePlainFile.this.getCache(), pathToUpload, pathToRename);                    
                    updateStatAndSendEvents(dirEntry, false);
                } catch (TimeoutException ex) {
                    throw new IOException(ex);
                } catch (InterruptedException ex) {
                    throw newIOException(ex);
                } catch (ExecutionException ex) {
                    //Exceptions.printStackTrace(ex); // should never be the case - the task is done
                    if (!ConnectionManager.getInstance().isConnectedTo(RemotePlainFile.this.getExecutionEnvironment())) {
                        RemotePlainFile.this.getFileSystem().addPendingFile(RemotePlainFile.this);
                        throw RemoteExceptions.createConnectException(ex.getMessage());
                    } else {
                        if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                            throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemotePlainFile.class, 
                                    "EXC_DoesNotExist", RemotePlainFile.this.getDisplayName())); //NOI18N
                        } else if (ex.getCause() instanceof IOException) {
                            throw (IOException) ex.getCause();
                        } else {
                            throw newIOException(ex);
                        }
                    }
                }
                closed = true;
            } finally {
                getLockSupport().writeUnlock(RemotePlainFile.this);
            }
        }

        private IOException newIOException(Exception cause) {
            return RemoteExceptions.createIOException(NbBundle.getMessage(RemotePlainFile.class,
                    "EXC_ErrorUploading", RemotePlainFile.this.getCache().getAbsolutePath(), RemotePlainFile.this.getExecutionEnvironment(), // NOI18N
                    cause.getMessage()), cause);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }
    }
}
