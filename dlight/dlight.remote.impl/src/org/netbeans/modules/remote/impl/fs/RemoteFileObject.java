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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.dlight.libs.common.FileStatistics;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 * The only class that comes outside.
 * Fixing #208084 - Remote file system should keep FileObject instances when a file is replaced with symlink and vice versa 
 */
public final class RemoteFileObject extends FileObject implements Serializable {

    static final long serialVersionUID = 1931650016889811086L;
    private final RemoteFileSystem fileSystem;
    private RemoteFileObjectBase implementor;

    private static final boolean MIME_SNIFFING = RemoteFileSystemUtils.getBoolean("remote.MIME.sniffing", true); //NOI18N
    
    /*package*/ RemoteFileObject(RemoteFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    /*package*/ void setImplementor(RemoteFileObjectBase implementor) {
        boolean assertions = false;
        assert (assertions = true);
        if (assertions) {
            // important consistency checks
            RemoteFileObject newWrapper = implementor.getOwnerFileObject();
            // new impl should have its wrapper set to this
            if (newWrapper != null && newWrapper != this) {
                RemoteLogger.assertTrue(false, "RFS inconsistency in {0}: delegate wrapper differs", this); // can't print neither this nor delegate since both are in ctors
            }
            // if replacing delegates, check that old one is invalid
            if (this.implementor != null && this.implementor.isValid()) {
                RemoteLogger.assertTrue(false, "RFS inconsistency in {0}: replacing valid delegate", this); // can't print neither this nor delegate since both are in ctors
            }
        }
        this.implementor = implementor;
    }

    public RemoteFileObjectBase getImplementor() {
        if (implementor == null) {
            String errMsg = "Null delegate"; // path is not avaliable! // NOI18N
            RemoteLogger.getInstance().log(Level.WARNING, errMsg, new NullPointerException(errMsg));
        }
        return implementor;
    }
    
    @Override
    public RemoteFileSystem getFileSystem() {
        return fileSystem;
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return fileSystem.getExecutionEnvironment();
    }

    // <editor-fold desc="Moved from RemoteFileObjectFile.">
    
    private final transient ThreadLocal<AtomicInteger> magic = new ThreadLocal<AtomicInteger>() {

        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger(0);
        }
    };

    @Override
    public String getMIMEType() {
        magic.get().incrementAndGet();
        try {
            return super.getMIMEType();
        } finally {
            magic.get().decrementAndGet();
        }
    }

    @Override
    public String getMIMEType(String... withinMIMETypes) {
        magic.get().incrementAndGet();
        try {
            return super.getMIMEType(withinMIMETypes);
        } finally {
            magic.get().decrementAndGet();
        }
    }

    protected boolean isMimeResolving() {
        if (magic.get().intValue() > 0) {
            return true;
        }
        for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if ("org.openide.filesystems.MIMESupport".equals(element.getClassName()) && "findMIMEType".equals(element.getMethodName()) ||  //NOI18N
                "org.openide.loaders.DefaultDataObject".equals(element.getClassName()) && "fixCookieSet".equals(element.getMethodName())) { //NOI18N
                return true;
            }
        }
        return false;
    }
    
    // <editor-fold">
    
    // <editor-fold desc="Moved from RemoteFileObjectBase.">
    
    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileChangedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileDeletedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
        super.fireFileAttributeChangedEvent(en, fe);
    }
    
    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileDataCreatedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        super.fireFileFolderCreatedEvent(en, fe);
    }

    /** Overridden to make possible calls from other package classes */
    @Override
    protected void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
        super.fireFileRenamedEvent(en, fe);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Delegating all methods. Keep collapsed.">
    
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return getImplementor().toString();
    }

    @Override
    @SuppressWarnings("deprecation") // we have to override abstract deprecated method
    public void setImportant(boolean b) {
        getImplementor().setImportant(b);
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
        getImplementor().setAttribute(attrName, value);
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        getImplementor().rename(lock, name, ext);
    }

    @Override
    public void removeRecursiveListener(FileChangeListener fcl) {
        getImplementor().removeRecursiveListener(fcl);
    }

    @Override
    public void removeFileChangeListener(FileChangeListener fcl) {
        getImplementor().removeFileChangeListener(fcl);
    }

    @Override
    public void refresh() {
        getImplementor().refresh();
    }
    
    public void nonRecursiveRefresh() {
        getImplementor().nonRecursiveRefresh();
    }

    @Override
    public void refresh(boolean expected) {
        getImplementor().refresh(expected);
    }

    @Override
    public FileObject copy(FileObject target, String name, String ext) throws IOException {
        return getImplementor().copy(target, name, ext);
    }

    @Override
    public FileObject move(FileLock lock, FileObject target, String name, String ext) throws IOException {
        try {
            FileObject result = getImplementor().move(lock, target, name, ext);
            reassignLkp(this, result);
            return result;
        } catch (TimeoutException ex) {
            throw new IOException(ex);
        }
    }

    public static void reassignLkp(FileObject from, FileObject to) {
        try {
            Class<?> c = Class.forName("org.openide.filesystems.FileObjectLkp"); //NOI18N
            Method m = c.getDeclaredMethod("reassign", FileObject.class, FileObject.class); //NOI18N
            m.setAccessible(true);
            m.invoke(null, from, to);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            if (ex.getCause() instanceof Error) {
                throw (Error) ex.getCause();
            }
            throw new IllegalStateException(ex);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public FileLock lock() throws IOException {
        return getImplementor().lock();
    }

    @Override
    public Date lastModified() {
        return getImplementor().lastModified();
    }

    @Override
    public boolean isVirtual() {
        return getImplementor().isVirtual();
    }

    @Override
    public boolean isValid() {
        return getImplementor().isValid();
    }

    @Override
    public boolean isRoot() {
        return getImplementor().isRoot();
    }

    @Override
    @SuppressWarnings("deprecation") // we have to override abstract deprecated method
    public boolean isReadOnly() {
        return getImplementor().isReadOnly();
    }

    @Override
    public boolean isLocked() {
        return getImplementor().isLocked();
    }

    @Override
    public boolean isFolder() {
        return getImplementor().isFolder();
    }

    @Override
    public boolean isData() {
        return getImplementor().isData();
    }

    @Override
    public long getSize() {
        return getImplementor().getSize();
    }

    @Override
    public String getPath() {
        return getImplementor().getPath();
    }

    @Override
    public RemoteFileObject getParent() {
        RemoteFileObjectBase parent = getImplementor().getParent();
        return (parent == null) ? null : parent.getOwnerFileObject();
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        return getImplementor().getOutputStream(lock);
    }

    @Override
    public String getNameExt() {
        return getImplementor().getNameExt();
    }

    @Override
    public String getName() {
        return getImplementor().getName();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        FileStatistics.getInstance(fileSystem).logPath(getPath());
        if (!getImplementor().hasCache()) {
            if (isMimeResolving()) {
                if (!MIME_SNIFFING || getSize() == 0) {
                    return new InputStream() {
                        @Override
                        public int read() throws IOException {
                            return -1;
                        }
                        
                    };
                }
                byte[] b = getImplementor().getMagic();
                if (b != null) {
                    return new ByteArrayInputStream(b);
                }
            }
        }
        return getImplementor().getInputStream(true);
    }

    public InputStream getInputStream(int maxSize) throws FileNotFoundException {
        if (maxSize <= MagicCache.BUF_LENGTH) {
            byte[] b = getImplementor().getMagic();
            if (b != null) {
                return new ByteArrayInputStream(b);
            }
        }
        return getInputStream();
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath) {
        return getFileObject(relativePath, new HashSet<String>());
    }

    public RemoteFileObject getFileObject(String relativePath, @NonNull Set<String> antiLoop) {
        // surprisingly there are some clients that do this
        if (relativePath.equals(".")) { //NOI18N
            return this;
        }
        return getImplementor().getFileObject(relativePath, antiLoop);
    }

    @Override
    public RemoteFileObject getFileObject(String name, String ext) {
        return getFileObject(name, ext, new HashSet<String>());
    }

    public RemoteFileObject getFileObject(String name, String ext, @NonNull Set<String> antiLoop) {
        return getImplementor().getFileObject(name, ext, antiLoop);
    }

    @Override
    public String getExt() {
        return getImplementor().getExt();
    }

    @Override
    public RemoteFileObject[] getChildren() {
        return getImplementor().getChildren();
    }

    @Override
    public Enumeration<String> getAttributes() {
        return getImplementor().getAttributes();
    }

    @Override
    public Object getAttribute(String attrName) {
        return getImplementor().getAttribute(attrName);
    }

    @Override
    public void delete(FileLock lock) throws IOException {
        getImplementor().delete(lock);
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        return getImplementor().createFolder(name);
    }

    @Override
    public FileObject createData(String name) throws IOException {
        return getImplementor().createData(name);
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        return getImplementor().createData(name, ext);
    }

    @Override
    public boolean canWrite() {
        return getImplementor().canWrite();
    }

    @Override
    public boolean canRead() {
        return getImplementor().canRead();
    }

    @Override
    public void addRecursiveListener(FileChangeListener fcl) {
        getImplementor().addRecursiveListener(fcl);
    }

    @Override
    public void addFileChangeListener(FileChangeListener fcl) {
        getImplementor().addFileChangeListener(fcl);
    }
    
    @Override
    public boolean isSymbolicLink() {
        return getImplementor().isSymbolicLink();
    }

    @Override
    public FileObject readSymbolicLink() {
        RemoteFileObjectBase target = getImplementor().readSymbolicLink();
        return (target == null) ? null : target.getOwnerFileObject();
    }

    @Override
    public String readSymbolicLinkPath() {
        return getImplementor().readSymbolicLinkPath();
    }

    @Override
    public FileObject getCanonicalFileObject() throws IOException {
        return RemoteFileSystemUtils.getCanonicalFileObject(this);
    }
    
    // </editor-fold>
    
   /* Java serialization*/ Object writeReplace() throws ObjectStreamException {
        return new SerializedForm(getExecutionEnvironment(), getPath());
    }
    
    private static class SerializedForm implements Serializable {
        
        static final long serialVersionUID = -1;
        private final ExecutionEnvironment env;
        private final String remotePath;

        public SerializedForm(ExecutionEnvironment env, String remotePath) {
            this.env = env;
            this.remotePath = remotePath;
        }
                
        /* Java serialization*/ Object readResolve() throws ObjectStreamException {
            RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
            FileObject fo = fs.findResource(remotePath);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(fs, remotePath);
            }
            return fo;
        }
    }    
}
