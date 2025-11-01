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

import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.BaseUtilities;

/**
 * @author rm111737
 */
public class FileObj extends BaseFileObj {
    static final long serialVersionUID = -1133540210876356809L;
    private static final MutualExclusionSupport<FileObj> MUT_EXCL_SUPPORT = new MutualExclusionSupport<>();
    private long lastModified = -1;
    private boolean realLastModifiedCached;
    private static final Logger LOGGER = Logger.getLogger(FileObj.class.getName());


    FileObj(final File file, final FileNaming name) {
        super(file, name);
        setLastModified(System.currentTimeMillis(), null, false);
    }
    @Override
    protected boolean noFolderListeners() {
        FolderObj p = getExistingParent();
        return p == null ? true : p.noFolderListeners();
    }

    @Override
    public OutputStream getOutputStream(final FileLock lock) throws IOException {
        ProvidedExtensions extensions = getProvidedExtensions();
        File file = getFileName().getFile();
        if (!BaseUtilities.isWindows() && !file.isFile()) {
            throw new IOException(file.getAbsolutePath());
        }
        return getOutputStream(lock, extensions, this);
    }
    
    @Messages(
        "EXC_INVALID_FILE=File {0} is not valid"
    )
    public OutputStream getOutputStream(final FileLock lock, ProvidedExtensions extensions, FileObject mfo) throws IOException {
        if (LOGGER.isLoggable(Level.FINE) && EventQueue.isDispatchThread()) {
            LOGGER.log(Level.WARNING, "writing " + this, new IllegalStateException("getOutputStream invoked in AWT"));
        }
        final File f = getFileName().getFile();
        if (!isValid()) {
            FileObject recreated = this.getFileSystem().findResource(getPath());
            if (recreated instanceof FileObj && recreated != this) {
                return ((FileObj)recreated).getOutputStream(lock, extensions, mfo);
            }
            FileNotFoundException fnf = new FileNotFoundException("FileObject " + this + " is not valid; isFile=" + f.isFile()); //NOI18N
            Exceptions.attachLocalizedMessage(fnf, Bundle.EXC_INVALID_FILE(this));
            throw fnf;
        }

        if (!BaseUtilities.isWindows() && !f.isFile()) {
            throw new IOException(f.getAbsolutePath());
        }
        
        final MutualExclusionSupport<FileObj>.Closeable closable = MUT_EXCL_SUPPORT.addResource(this, false);

        if (extensions != null) {
            extensions.beforeChange(mfo);
        }
        
        OutputStream retVal = null;
        try {
            final OutputStream delegate = Files.newOutputStream(f.toPath());
            retVal = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    delegate.write(b);
                }

                @Override
                public void close() throws IOException {
                    if (!closable.isClosed()) {
                        delegate.close();
                        LOGGER.log(Level.FINEST, "getOutputStream-close");
                        Long lastModif = MOVED_FILE_TIMESTAMP.get();
                        if (lastModif != null) {
                            f.setLastModified(lastModif);
                        }
                        setLastModified(f, false);
                        closable.close();
                        fireFileChangedEvent(false);
                    }
                }

                @Override
                public void flush() throws IOException {
                    delegate.flush();
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    delegate.write(b, off, len);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    delegate.write(b);
                }
            };
        } catch (FileNotFoundException e) {
            if (closable != null) {
                closable.close();
            }
            FileNotFoundException fex = e;                        
            if (!FileChangedManager.getInstance().exists(f)) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.canWrite()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (f.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!FileChangedManager.getInstance().exists(f.getParentFile())) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } 
            FSException.annotateException(fex);            
            throw fex;
        }
        return retVal;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        if (LOGGER.isLoggable(Level.FINE) && EventQueue.isDispatchThread()) {
            LOGGER.log(Level.WARNING, "reading " + this, new IllegalStateException("getInputStream invoked in AWT"));
        }
        final File f = getFileName().getFile();
        if (!isValid()) {
            FileObject recreated = null;
            try {
                recreated = this.getFileSystem().findResource(getPath());
            } catch (FileStateInvalidException ex) {
                LOGGER.log(Level.FINE, "Can't get filesystem for " + getPath(), ex);
            }
            if (recreated != null && recreated != this) {
                return recreated.getInputStream();
            }
            FileNotFoundException ex = new FileNotFoundException("FileObject " + this + " is not valid."); //NOI18N
            String msg = NbBundle.getMessage(FileBasedFileSystem.class, "EXC_CannotRead", f.getName(), f.getParent()); // NOI18N
            Exceptions.attachLocalizedMessage(ex, msg);
            dumpFileInfo(f, ex);
            throw ex;
        }
        LOGGER.log(Level.FINEST,"FileObj.getInputStream_after_is_valid");   //NOI18N - Used by unit test
        if (!f.exists()) {
            FileNotFoundException ex = new FileNotFoundException("Can't read " + f); // NOI18N
            String msg = NbBundle.getMessage(FileBasedFileSystem.class, "EXC_CannotRead", f.getName(), f.getParent()); // NOI18N
            Exceptions.attachLocalizedMessage(ex, msg);
            dumpFileInfo(f, ex);
            throw ex;
        }
        InputStream inputStream;
        MutualExclusionSupport<FileObj>.Closeable closeableReference = null;
        
        try {
            if (BaseUtilities.isWindows()) {
                // #157056 - don't try to open locked windows files (ntuser.dat, ntuser.dat.log1, ...)
                if (getNameExt().toLowerCase().startsWith("ntuser.dat")) {  //NOI18N
                    return new ByteArrayInputStream(new byte[] {});
                }
            } else if (!f.isFile()) {
                return new ByteArrayInputStream(new byte[] {});
            }
            final MutualExclusionSupport<FileObj>.Closeable closable = MUT_EXCL_SUPPORT.addResource(this, true);
            closeableReference = closable;            
            inputStream = new FileInputStream(f) {

                @Override
                public void close() throws IOException {
                    super.close();
                    closable.close();
                }
            };
        } catch (IOException e) {
            if (closeableReference != null) {
                closeableReference.close();    
            }
            
            FileNotFoundException fex;                        
            if (!FileChangedManager.getInstance().exists(f)) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.canRead()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (f.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!FileChangedManager.getInstance().exists(f.getParentFile())) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if ((new FileInfo(f)).isUnixSpecialFile()) {
                fex = (FileNotFoundException) new FileNotFoundException(e.toString()).initCause(e);
            } else {
                fex = (FileNotFoundException) new FileNotFoundException(e.toString()).initCause(e);
            }                        
            FSException.annotateException(fex);
            throw fex;
        }
        assert inputStream != null;
        return inputStream;
    }

    @Override
    public boolean isReadOnly() {
        final File f = getFileName().getFile();
        boolean res;
        if (!BaseUtilities.isWindows() && !f.isFile()) {
            res = true;
        } else {
            res = super.isReadOnly();
        }
        markReadOnly(res);
        return res;
    }

    @Override
    public boolean canWrite() {
        final File f = getFileName().getFile();        
        if (!BaseUtilities.isWindows() && !f.isFile()) {
            return false;
        }                
        return super.canWrite();
    }
        
    final void setLastModified(long lastModified, File forFile, boolean readOnly) {
        if (this.getLastModified() != 0) { // #130998 - don't set when already invalidated
            if (this.getLastModified() != -1 && !realLastModifiedCached) {
                realLastModifiedCached = true;
            }
            if (LOGGER.isLoggable(Level.FINER)) {
                Exception trace = LOGGER.isLoggable(Level.FINEST) ? new Exception("StackTrace") : null; // NOI18N
                LOGGER.log(Level.FINER, "setLastModified: " + this.getLastModified() + " -> " + lastModified + " (" + this + ") on " + forFile, trace);  //NOI18N
            }
            this.setLastModified(lastModified, readOnly);
        }
    }
    
    
    @Override
    public final FileObject createFolder(final String name) throws IOException {
        throw new IOException(getPath());//isn't directory - cannot create neither file nor folder
    }

    @Override
    public final FileObject createData(final String name, final String ext) throws IOException {
        throw new IOException(getPath());//isn't directory - cannot create neither file nor folder
    }


    @Override
    public final FileObject[] getChildren() {
        return new FileObject[]{};//isn't directory - no children
    }

    @Override
    public final FileObject getFileObject(final String name, final String ext) {
        return null;
    }

    @Override
    public boolean isValid() {
        //0 - because java.io.File.lastModififed returns 0 for not existing files        
        boolean retval = getLastModified() != 0;
        //assert checkCacheState(retval, getFileName().getFile());
        return retval && super.isValid();
    }

    @Override
    protected void setValid(boolean valid) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "setValid: " + valid + " (" + this + ")", new Exception("Stack trace"));  //NOI18N
        }
        if (valid) {
            //I can't make valid fileobject when it was one invalidated
            assert isValid() : this.toString();
        } else {
            //0 - because java.io.File.lastModififed returns 0 for not existing files
            setLastModified(0, true);
        }        
    }

    @Override
    public final boolean isFolder() {
        return false;
    }

    @Override
    public void refreshImpl(final boolean expected, boolean fire) {
        final long oldLastModified = getLastModified();
        final boolean isReadOnly = thinksReadOnly();
        boolean isReal = realLastModifiedCached;
        final File file = getFileName().getFile();
        setLastModified(file, !file.canWrite());
        boolean isModified = (isReal) ? (oldLastModified != getLastModified()) : (oldLastModified < getLastModified());
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(
                Level.FINER,
                "refreshImpl for {0} isReal: {1} isModified: {2} oldLastModified: {3} lastModified: {4}",
                new Object[]{
                    this, isReal, isModified, oldLastModified, getLastModified()}
            );
        }
        if (fire && oldLastModified != -1 && getLastModified() != -1 && getLastModified() != 0 && isModified) {
            if (!MUT_EXCL_SUPPORT.isBeingWritten(this)) {
                fireFileChangedEvent(expected);
            }
        }
        if (fire && isReal && isReadOnly != thinksReadOnly() && getLastModified() != 0) {
            // #129178 - event consumed in org.openide.text.DataEditorSupport and used to change editor read-only state
            fireFileAttributeChangedEvent("DataEditorSupport.read-only.refresh", null, null);  //NOI18N
        }
    }
    
    @Override
    public final void refresh(final boolean expected) {
        refresh(expected, true);
    }
    

    

    @Override
    public final Enumeration<FileObject> getChildren(final boolean rec) {
        return Enumerations.empty();
    }

    @Override
    public final Enumeration<FileObject> getFolders(final boolean rec) {
        return Enumerations.empty();
    }

    @Override
    public final Enumeration<FileObject> getData(final boolean rec) {
        return Enumerations.empty();
    }


    @Override
    public final FileLock lock() throws IOException {
        final File me = getFileName().getFile();
        if (!getProvidedExtensions().canWrite(me)) {
            FSException.io("EXC_CannotLock", me);
        }
        try {            
            LockForFile result = LockForFile.tryLock(me);
            try {
                getProvidedExtensions().fileLocked(this);
            } catch (IOException ex) {
                result.releaseLock(false);
                throw ex;
            }
            return result;
        } catch (FileNotFoundException ex) {
            FileNotFoundException fex = ex;                        
            if (!FileChangedManager.getInstance().exists(me)) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canRead()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canWrite()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (me.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!FileChangedManager.getInstance().exists(me.getParentFile())) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            }                                                             
            FSException.annotateException(fex);            
            throw fex;
        }
    }

    @Override
    public final boolean isLocked() {
        final File me = getFileName().getFile();
        final LockForFile l = LockForFile.findValid(me);
        return l != null && l.isValid();
    }

    @Override
    final boolean checkLock(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();
        return ((lock instanceof LockForFile) && Utils.equals(((LockForFile) lock).getFile(), f));
    }

    @Override
    public void rename(final FileLock lock, final String name, final String ext, ProvidedExtensions.IOHandler handler) throws IOException {
        super.rename(lock, name, ext, handler);
        final File rename = getFileName().getFile();
        setLastModified(rename, !rename.canWrite());
    }

    private long getLastModified() {
        long l = lastModified;
        if (l < -10) {
            return -l;
        }
        return l;
    }

    private void setLastModified(long lastModified, boolean readOnly) {
        if (lastModified >= -10 && lastModified < 10) {
            this.lastModified = lastModified;
            return;
        }
        this.lastModified = readOnly ? -lastModified : lastModified;
    }

    /**
     * Set last-modification to value retrieved from {@link File} instance, and
     * handle zero value correctly.
     *
     * See bug 254567.
     *
     * @param file The file to read last-modification-date from.
     * @param readOnly Read-only flag.
     */
    private void setLastModified(File file, boolean readOnly) {
        long lastMod = file.lastModified();
        if (lastMod == 0) { // see bug 254567
            try {
                BasicFileAttributes attrs = Files.readAttributes(
                        file.toPath(), BasicFileAttributes.class);
                lastMod = attrs.lastModifiedTime().toMillis();
                if (lastMod == 0) {
                    lastMod = 1;
                }
            } catch (UnsupportedOperationException | SecurityException ex) {
                if (file.exists()) {
                    lastMod = 1;
                }
            } catch (IOException | InvalidPathException ex) {
                // lastMod stays 0, the file is invalid.
            }
        }
        setLastModified(lastMod, file, readOnly);
    }

    private boolean thinksReadOnly() {
        return lastModified < -10;
    }
    
    private void markReadOnly(boolean readOnly) {
        if (thinksReadOnly() != readOnly) {
            setLastModified(getLastModified(), readOnly);
        }
    }
}
