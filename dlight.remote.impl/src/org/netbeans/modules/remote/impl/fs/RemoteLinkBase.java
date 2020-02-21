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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class RemoteLinkBase extends RemoteFileObjectBase implements FileChangeListener {
    
    protected RemoteLinkBase(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, RemoteFileObjectBase parent, String remotePath) {
        super(wrapper, fileSystem, execEnv, parent, remotePath);
    }

    /**
     * That's a kind of addition to constructor + kind of "destructor"
     * It will be called each time the instance is created *and* used
     * (i.e. not thrown away - see RemoteFileObjectFactor.putIfAbsent())
     * RemoteFileObjectFactory creates an instance each time before calling putIfAbsent,
     * so constructor should be very lightweight.
     *
     * It is also called after changing link target
     */
    protected void initListeners(boolean add) {
        if (add) {
            getFileSystem().getFactory().addFileChangeListener(getDelegateNormalizedPath(), this);
        } else {
            getFileSystem().getFactory().removeFileChangeListener(getDelegateNormalizedPath(), this);
        }
    }

    public abstract RemoteFileObjectBase getCanonicalDelegate();
    protected abstract String getDelegateNormalizedPath();
    protected abstract RemoteFileObjectBase getDelegateImpl();

    protected FileNotFoundException fileNotFoundException(String operation) {
        return RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemoteLinkBase.class,
                "EXC_CantPerformOpOnDeadLink", operation, getDisplayName())); //NOI18N
    }
    
    @Override
    public RemoteFileObject[] getChildren() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            RemoteFileObject[] children = delegate.getChildren();
            for (int i = 0; i < children.length; i++) {
                children[i] = wrapFileObject(children[i], null);
            }
            return children;
        }
        return new RemoteFileObject[0];
    }

    @Override
    public final boolean isSymbolicLink() {
        return true;
    }

    private RemoteFileObject wrapFileObject(RemoteFileObject fo, String relativePath) {
        String childAbsPath;
        if (relativePath == null) {
            childAbsPath = getPath() + '/' + fo.getNameExt();
        } else {
            childAbsPath = RemoteFileSystemUtils.normalize(getPath() + '/' + relativePath);
        }
        if (relativePath != null && (relativePath.contains("/") || relativePath.contains(".."))) { //NOI18N
            return fo;
        } else {
            // NB: here it can become not a remote link child (in the case it changed remotely and refreshed concurrently)
            RemoteFileObjectBase result = getFileSystem().getFactory().createRemoteLinkChild(this, childAbsPath, fo.getImplementor());
            return result.getOwnerFileObject();
        }
    }

    // ------------ delegating methods -------------------

    @Override
    public long getSize() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? 0 : delegate.getSize();        
    }

    @Override
    public boolean hasCache() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? false : delegate.hasCache();
    }

    @Override
    public RemoteFileObject getFileObject(String name, String ext, @NonNull Set<String> antiLoop) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            RemoteFileObject fo = delegate.getFileObject(name, ext, antiLoop);
            if (fo != null && fo.getImplementor().getParent() == delegate) {
                fo = wrapFileObject(fo, null);
            }
            return fo;
        }
        return null;
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath, @NonNull Set<String> antiLoop) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            RemoteFileObject fo = delegate.getFileObject(relativePath, antiLoop);
            if (fo != null  && fo.getImplementor().getParent() == delegate) {
                fo = wrapFileObject(fo, relativePath);
            }
            return fo;
        }
        return null;
    }

    @Override
    public boolean isFolder() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? false : delegate.isFolder();
    }

    @Override
    public boolean isData() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? true : delegate.isData();
    }

    @Override
    public InputStream getInputStream(boolean checkLock) throws FileNotFoundException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate == null) {
            throw fileNotFoundException("read"); //NOI18N
        }
        return delegate.getInputStream(checkLock);
    }

    @Override
    public boolean canRead() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? false : delegate.canRead();
    }

    @Override
    protected FileLock lockImpl(RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.lockImpl(orig);
        } else {
            return super.lockImpl(orig);
        }
    }

    @Override
    public Date lastModified() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.lastModified();
        } else {
            return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
        }
    }

    @Override
    protected boolean checkLock(FileLock aLock) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.checkLock(aLock);
        } else {
            return super.checkLock(aLock);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean isReadOnlyImpl(RemoteFileObjectBase orig) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? true : delegate.isReadOnlyImpl(orig);
    }

    @Override
    protected OutputStream getOutputStreamImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.getOutputStreamImpl(lock, orig);
        } else {
            throw fileNotFoundException("write"); //NOI18N
        }
    }  
  
    @Override
    protected final void refreshThisFileMetadataImpl(boolean recursive, Set<String> antiLoop, 
        boolean expected, RefreshMode refreshMode, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        // TODO: this dummy implementation is far from optimal in terms of performance. It needs to be improved.
        if (getParent() != null) {
            getParent().refreshImpl(false, antiLoop, expected, refreshMode, timeoutMillis);
        }
    }    
    
    @Override
    public final void refreshImpl(boolean recursive, Set<String> antiLoop, boolean expected, RefreshMode refreshMode, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        if (antiLoop == null) {
            antiLoop = new HashSet<>();
        }
        if (antiLoop.contains(getPath())) {
            return;
        } else {
            antiLoop.add(getPath());
        }
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        // For link we need to refresh both delegate and link metadata itself
        if (refreshMode != RefreshMode.FROM_PARENT) {
            refreshThisFileMetadataImpl(recursive, antiLoop, expected, refreshMode, timeoutMillis);
        }
        if (delegate != null) {
            delegate.refreshImpl(recursive, antiLoop, expected, refreshMode, timeoutMillis);
        } else {
            RemoteLogger.log(Level.FINEST, "Null delegate for link {0}", this); //NOI18N
        }
    }
    
    @Override
    protected void renameChild(FileLock lock, RemoteFileObjectBase toRename, String newNameExt, RemoteFileObjectBase orig) 
            throws ConnectException, IOException, InterruptedException, ExecutionException {
        // all work in it's wrapped delegate
        RemoteLogger.assertTrueInConsole(false, "renameChild is not supported on " + this.getClass() + " path=" + getPath()); // NOI18N
    }
    
    @Override
    protected RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return wrapFileObject(delegate.createFolderImpl(name, orig), null);
        } else {
            throw fileNotFoundException("create a folder in"); //NOI18N
        }
    }

    @Override
    protected RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return wrapFileObject(delegate.createDataImpl(name, ext, orig), null);
        } else {
            throw fileNotFoundException("create a file in"); //NOI18N
        }
    }

    @Override
    public boolean canWriteImpl(RemoteFileObjectBase orig) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? super.canWriteImpl(orig) : delegate.canWriteImpl(orig);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        fireFileAttributeChangedEvent(getListeners(), (FileAttributeEvent)transform(fe));
    }

    @Override
    public void fileChanged(FileEvent fe) {
        fireFileChangedEvent(getListeners(), transform(fe));
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        fireFileDataCreatedEvent(getListeners(), transform(fe));
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        if (!isCyclicLink()) {
            fireFileDeletedEvent(getListeners(), transform(fe));
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        fireFileFolderCreatedEvent(getListeners(), transform(fe));
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        fireFileRenamedEvent(getListeners(), (FileRenameEvent)transform(fe));
    }

    private boolean isCyclicLink() {
        Set<RemoteFileObjectBase> antiCycle = new HashSet<>();
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate == null && getPath() != null) {
            // self-referencing link
            return true;
        }
        while (delegate != null) {
            if (delegate instanceof RemoteLinkBase) {
                if (antiCycle.contains(delegate)) return true;
                antiCycle.add(delegate);
                delegate = ((RemoteLinkBase) delegate).getCanonicalDelegate();
            } else {
                break;
            }
        }        
        return false;
    }
    
    private FileEvent transform(FileEvent fe) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            FileObject src = transform((FileObject) fe.getSource(), delegate);
            FileObject file = transform(fe.getFile(), delegate);
            if (file != fe.getFile() || src != fe.getSource()) {
                if (fe instanceof FileRenameEvent) {
                    FileRenameEvent fre = (FileRenameEvent) fe;
                    fe = new FileRenameEvent(src, file, fre.getName(), fre.getExt(), fe.isExpected());
                } else if (fe instanceof FileAttributeEvent) {
                    FileAttributeEvent fae = (FileAttributeEvent) fe;
                    fe = new FileAttributeEvent(src, file, fae.getName(), fae.getOldValue(), fae.getNewValue(), fe.isExpected());
                } else {
                    fe = new FileEvent(src, file, fe.isExpected(), fe.getTime());
                }
            }
        }
        return fe;
    }

    private FileObject transform(FileObject fo, RemoteFileObjectBase delegate) {
        if (fo instanceof RemoteFileObject) {
            RemoteFileObjectBase originalFO = ((RemoteFileObject) fo).getImplementor();
            if (originalFO == delegate) {
                return this.getOwnerFileObject();
            }
            if (originalFO.getParent() == delegate) {
                String path = RemoteLinkBase.this.getPath() + '/' + fo.getNameExt();
                // NB: here it can become not a remote link child (in the case it changed remotely and refreshed concurrently)
                RemoteFileObjectBase linkChild = getFileSystem().getFactory().createRemoteLinkChild(this, path, originalFO);
                return linkChild.getOwnerFileObject();
            }
        }
        return fo;
    }
    
    @Override
    protected byte[] getMagic() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            return delegate.getMagic();
        }
        return null;
    }

    @Override
    public void warmup(FileSystemProvider.WarmupMode mode, Collection<String> extensions) {        
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            delegate.warmup(mode, extensions);
        }
    }    
    
    @Override
    public Object getAttribute(String attrName) {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? null : delegate.getAttribute(attrName);
    }

    @Override
    public Enumeration<String> getAttributes() {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        return (delegate == null) ? Collections.<String>emptyEnumeration() : delegate.getAttributes();
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        RemoteFileObjectBase delegate = getCanonicalDelegate();
        if (delegate != null) {
            delegate.setAttribute(attrName, value);
        }
    }    
}
