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

import org.netbeans.modules.masterfs.filebasedfs.Statistics;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.providers.Attributes;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions.IOHandler;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.Mutex;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.BaseUtilities;


/**
 * Implements FileObject methods as simple as possible.
 *
 * @author Radek Matous
 */
//TODO: listeners still kept in EventListenerList

public abstract class BaseFileObj extends FileObject {
    //constants
    private static final String PATH_SEPARATOR = File.separator;//NOI18N
    private static final char EXT_SEP = '.';//NOI18N
    static final Logger LOG = Logger.getLogger(BaseFileObj.class.getName());
    static final ThreadLocal<Long> MOVED_FILE_TIMESTAMP = new ThreadLocal<>(); //#244286: move in progress

    //static fields 
    static final long serialVersionUID = -1244650210876356809L;
    static final Attributes attribs;
    static {
        final BridgeForAttributes attrBridge = new BridgeForAttributes();
        attribs = new Attributes(attrBridge, attrBridge, attrBridge);
    }

    private static final Object EVENT_SUPPORT_LOCK = new Object();
    //private fields
    private EventListenerList eventSupport;
    private FileNaming fileName;


    protected BaseFileObj(final File file) {
        this(file, NamingFactory.fromFile(file));
    }
    
    protected BaseFileObj(final File file, final FileNaming name) {
        assert name != null;
        this.fileName = name;
    }
       
    @Override
    public final String toString() {
        return String.format("%s@%h:%h%s", fileName, fileName, this, isValid() ? "" : "[invalid]"); // NOI18N
    }

    @Override
    public final String getNameExt() {
        String ne = getFileName().getName();
        while (ne.endsWith("\\")) {
            ne = ne.substring(0, ne.length() - 1);
        }
        return ne;
    }

    /** Returns true is file is \\ComputerName\sharedFolder. */
    private static boolean isUncRoot(final File file) {
        if(file.getPath().startsWith("\\\\")) { //NOI18N
            File parent = file.getParentFile();
            if(parent != null) {
                parent = parent.getParentFile();
                if(parent != null) {
                    return parent.getPath().equals("\\\\"); // NOI18N
                }
            }
        }
        return false;
    }
    
    static String getNameExt(final File file) {
        String retVal = (file.getParent() == null || isUncRoot(file)) ? file.getAbsolutePath() : file.getName();
        if (retVal.endsWith(PATH_SEPARATOR)) {//NOI18N
            final boolean isPermittedToStripSlash = !(file.getParent() == null && new FileInfo(file).isUNCFolder());
            if (isPermittedToStripSlash) {
                retVal = retVal.substring(0, retVal.length() - 1);
            }
        }
        return retVal;
    }

    @Override
    public boolean canRead() {
        final File file = getFileName().getFile();        
        return file.canRead();
    }

    @Override
    public boolean canWrite() {
        final File file = getFileName().getFile();        
        ProvidedExtensions extension = getProvidedExtensions();
        return extension.canWrite(file);
    }

    @Override
    public final boolean isData() {
        return !isFolder();
    }

    @Override
    public final String getName() {
        return FileInfo.getName(getNameExt());
    }

    @Override
    public final String getExt() {
        return FileInfo.getExt(getNameExt());
    }

    @Override
    public final String getPath() {
        FileNaming fileNaming = getFileName();
        Deque<String> stack = new ArrayDeque<>();
        while (fileNaming != null) {
            stack.addFirst(fileNaming.getName());
            fileNaming = fileNaming.getParent();
        }
        String rootName = stack.removeFirst();
        if (BaseUtilities.isWindows()) {
            rootName = rootName.replace(File.separatorChar, '/');
            if(rootName.startsWith("//")) {  //NOI18N
                // UNC root like //computer/sharedFolder
                rootName += "/";  //NOI18N
            }
        }
        StringBuilder path = new StringBuilder();
        path.append(rootName);
        while (!stack.isEmpty()) {
            path.append(stack.removeFirst());
            if (!stack.isEmpty()) {
                path.append('/');  //NOI18N
            }
        }
        return path.toString();
    }

    @Override
    public final FileSystem getFileSystem() throws FileStateInvalidException {
        return FileBasedFileSystem.getInstance();
    }

    @Override
    public final boolean isRoot() {
        return false;
    }

    @Override
    public final java.util.Date lastModified() {
        final File f = getFileName().getFile();
        final long lastModified = f.lastModified();
        return new Date(lastModified);
    }
     
    @Override
    public FileObject copy(FileObject target, String name, String ext) throws IOException {
        if (FileUtil.isParentOf(this, target)) {
            FSException.io("EXC_CopyChild", this, target); // NOI18N                
        }
        ProvidedExtensions extensions = getProvidedExtensions();

        File to = getToFile(target, name, ext);

        extensions.beforeCopy(target, to);
        FileObject result = null;
        Long last = MOVED_FILE_TIMESTAMP.get();
        try {
            if (last != null) {
                MOVED_FILE_TIMESTAMP.set(lastModified().getTime());
            }
            final IOHandler copyHandler = extensions.getCopyHandler(getFileName().getFile(), to);
            if (copyHandler != null) {
                if (target instanceof FolderObj folderObj) {
                    result = handleMoveCopy(folderObj, name, ext, copyHandler);
                } else {
                    copyHandler.handle();
                    refresh(true);
                    //perfromance bottleneck to call refresh on folder
                    //(especially for many files to be copied)
                    target.refresh(true); // XXX ?
                    result = target.getFileObject(name, ext); // XXX ?
                    assert result != null : "Cannot find " + target + " with " + name + "." + ext;
                }
                FileUtil.copyAttributes(this, result);
            } else {
                if (isFolder() && ext != null && !ext.isEmpty()) {
                    name = name + '.' + ext;
                }
                result = super.copy(target, name, ext);
            }
        } catch (IOException ioe) {
            extensions.copyFailure(this, to);
            throw ioe;
        } finally {
            if (last != null) {
                MOVED_FILE_TIMESTAMP.set(last);
            }
        }
        extensions.copySuccess(this, to);
        return result;
    }

    @Override
    public final FileObject move(FileLock lock, FileObject target, String name, String ext) throws IOException {
        if (FileUtil.isParentOf(this, target)) {
            FSException.io("EXC_MoveChild", this, target); // NOI18N                
        }
        ProvidedExtensions extensions = getProvidedExtensions();
        File to = getToFile(target, name, ext);

        extensions.beforeMove(this, to);
        FileObject result = null;
        try {
        if (!checkLock(lock)) {
            FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N
        }

        Watcher.lock(target);
        Watcher.lock(getParent());
        final IOHandler moveHandler = extensions.getMoveHandler(getFileName().getFile(), to);
        if (moveHandler != null) {
            if (target instanceof FolderObj folderObj) {
                result = move(lock, folderObj, name, ext,moveHandler);
            } else {
                moveHandler.handle();
                refresh(true);
                //perfromance bottleneck to call refresh on folder
                //(especially for many files to be moved)
                target.refresh(true);
                result = target.getFileObject(name, ext);
                assert result != null : "Cannot find " + target + " with " + name + "." + ext;
            }
        } else {
            MOVED_FILE_TIMESTAMP.set(lastModified().getTime());
            try {
                result = super.move(lock, target, name, ext);
            } finally {
                MOVED_FILE_TIMESTAMP.remove();
            }
        }

        FileUtil.copyAttributes(this, result);
        Utils.reassignLkp(this, result);
        } catch (IOException ioe) {
            extensions.moveFailure(this, to);
            throw ioe;
        }
        extensions.moveSuccess(this, to);
        return result;                        
    }
    
    public BaseFileObj move(FileLock lock, FolderObj target, String name, String ext, ProvidedExtensions.IOHandler moveHandler) throws IOException {
        return handleMoveCopy(target, name, ext, moveHandler);
    }

    private File getToFile(FileObject target, String name, String ext) {
        if (target instanceof FolderObj) {
            final File tf = ((BaseFileObj) target).getFileName().getFile();
            return new File(tf, FileInfo.composeName(name, ext));
        }
        final File tf = FileUtil.toFile(target);
        return tf == null ? null : new File(tf, FileInfo.composeName(name, ext));
    }
    
    static void dumpFileInfo(final File f, Throwable ex) {
        for (File p = f.getParentFile(); p != null; p = p.getParentFile()) {
            if (p.exists()) {
                Exceptions.attachMessage(ex, "\nParent exists: " + p); // NOI18N
                Exceptions.attachMessage(ex, "\nHas children " + Arrays.toString(p.list())); // NOI18N
                break;
            } else {
                Exceptions.attachMessage(ex, "\nParent does not exist " + p); // NOI18N
            }
        }
    }


    private BaseFileObj handleMoveCopy(FolderObj target, String name, String ext, IOHandler handler) throws IOException {
        handler.handle();
        String nameExt = FileInfo.composeName(name, ext);
        target.getChildrenCache().getChild(nameExt, true);
        //TODO: review
        BaseFileObj result = null;
        final File file = new File(target.getFileName().getFile(), nameExt);
        for (int i = 0; i < 10; i++) {
            result = (BaseFileObj) FileBasedFileSystem.getFileObject(file);
            if (result != null) {
                if (result.isData()) {
                    result.fireFileDataCreatedEvent(false);
                } else {
                    result.fireFileFolderCreatedEvent(false);
                }
                break;
            }
            // #179109 - result is sometimes null, probably when moved file
            // is not yet ready. We wait max. 1000 ms.
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
        boolean assertsOn = false;
        assert assertsOn = true;
        if (result == null && assertsOn) {
            AssertionError ae = new AssertionError("FileObject for " + file + " not found.");
            dumpFileInfo(file, ae);
            throw ae;
        }
        FolderObj parent = getExistingParent();
        if (parent != null) {
            parent.refresh(true);
        } else {
            refresh(true);
        }
        //fireFileDeletedEvent(false);
        return result;
    }

    void rename(final FileLock lock, final String name, final String ext, final ProvidedExtensions.IOHandler handler) throws IOException {
        if (!checkLock(lock)) {
            FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N
        }

        final File file = getFileName().getFile();
        final File parent = file.getParentFile();

        final String newNameExt = FileInfo.composeName(name, ext);

        if (newNameExt.equals(getNameExt())) {
            // nothing to rename
            return;
        }

        final File file2Rename = new File(parent, newNameExt);
        if (parent == null || !FileChangedManager.getInstance().exists(parent) ||
                // #128818 - slash or backslash not allowed in name
                newNameExt.contains("/") || newNameExt.contains("\\")) {  //NOI18N
            FileObject parentFo = getExistingParent();
            String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotRename", file.getName(), parentPath, newNameExt);// NOI18N
        }
        
        final String originalName = getName();
        final String originalExt = getExt();
        if (Utils.equals(file2Rename, file)) {
            boolean success;
            if (handler != null) {
                handler.handle();
                success = true;
            } else {
                success = file.renameTo(file2Rename);
            }
            if (!success) {
                FileObject parentFo = getExistingParent();
                String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
                FSException.io("EXC_CannotRename", file.getName(), parentPath, newNameExt);// NOI18N
            }
            // just a case sensitive update of the file name
            NamingFactory.checkCaseSensitivity(fileName, file2Rename);
            fireFileRenamedEvent(originalName, originalExt);
            return;
        }
        
        boolean targetFileExists = FileChangedManager.getInstance().exists(file2Rename) && !Utils.equals(file2Rename, file);

        if (targetFileExists) {
            FileObject parentFo = getExistingParent();
            String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotRename", file.getName(), parentPath, newNameExt);// NOI18N
        }


        //TODO: no lock used
        FileObjectFactory fs = getFactory();

        fs.allIBaseLock.writeLock().lock(); // #239302
        try {
        synchronized (FileObjectFactory.AllFactories) {
            FileNaming oldFileName = getFileName();
            assert oldFileName != null;
            FileNaming[] allRenamed = NamingFactory.rename(oldFileName, newNameExt, handler);
            if (allRenamed == null) {
                FileObject parentFo = getExistingParent();
                String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
                FSException.io("EXC_CannotRename", file.getName(), parentPath, newNameExt);// NOI18N
            }
            assert allRenamed[0] != null;
            fileName = allRenamed[0];
            Set<BaseFileObj> toRename = new HashSet<>(allRenamed.length * 2);
            toRename.add(this);
            BaseFileObj.attribs.renameAttributes(file.getAbsolutePath().replace('\\', '/'), file2Rename.getAbsolutePath().replace('\\', '/'));//NOI18N
            for (int i = 0; i < allRenamed.length; i++) {
                File affected = allRenamed[i].getFile();
                BaseFileObj obj = fs.getCachedOnly(affected, false);
                if (obj != null && i >= 1) {
                    obj.updateFileName(allRenamed[i], oldFileName, allRenamed[0]);
                    toRename.add(obj);
                }
                FileObject tmpPar = allRenamed[i].getParent() != null ? fs.getCachedOnly(affected.getParentFile(), false) : null;
                if (tmpPar instanceof FolderObj par) {
                    ChildrenCache childrenCache = par.getChildrenCache();
                    final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
                    if (mutexPrivileged != null) {
                        mutexPrivileged.enterWriteAccess();
                    }
                    try {
                        if (i >= 1) {
                            childrenCache.removeChild(allRenamed[i]);
                        }
                        childrenCache.getChild(allRenamed[i].getName(), true);
                    } finally {
                        if (mutexPrivileged != null) {
                            mutexPrivileged.exitWriteAccess();
                        }
                    }
                }
            }
            fs.rename(toRename);
        }
        } finally {
            fs.allIBaseLock.writeLock().unlock();
        }
        //TODO: RELOCK
        LockForFile.relock(file, file2Rename);
        
        afterRename();
        
        fireFileRenamedEvent(originalName, originalExt);    
    }

    protected void afterRename() {
    }

    @Override
    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        FileBasedFileSystem.runAsInconsistent(() -> {
            ProvidedExtensions extensions = getProvidedExtensions();
            rename(lock, name, ext, extensions.getRenameHandler(getFileName().getFile(), FileInfo.composeName(name, ext)));
            return true;
        });
    }
      
    @Override
    public Object getAttribute(final String attrName) {
        if (attrName.equals("FileSystem.rootPath")) {
            return "";//NOI18N
        } else if (attrName.equals("java.io.File")) {
            return getFileName().getFile();
        } else if (attrName.equals("ExistsParentNoPublicAPI")) {
            return getExistingParent() != null;
        } else if (attrName.startsWith("ProvidedExtensions")) {  //NOI18N
            // #158600 - delegate to ProvidedExtensions if attrName starts with ProvidedExtensions prefix
            ProvidedExtensions extension = getProvidedExtensions();
            return extension.getAttribute(getFileName().getFile(), attrName);
        }
   
        return BaseFileObj.attribs.readAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName);//NOI18N
    }

    @Override
    public final void setAttribute(final String attrName, final Object value) throws java.io.IOException {
        final Object oldValue = BaseFileObj.attribs.readAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName);//NOI18N
        BaseFileObj.attribs.writeAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName, value);//NOI18N
        fireFileAttributeChangedEvent(attrName, oldValue, value);
    }

    @Override
    public final java.util.Enumeration<String> getAttributes() {
        return BaseFileObj.attribs.attributes(getFileName().getFile().getAbsolutePath().replace('\\', '/'));//NOI18N
    }

    @Override
    public final void addFileChangeListener(final org.openide.filesystems.FileChangeListener fcl) {
        getEventSupport().add(FileChangeListener.class, fcl);
        Watcher.register(this);
    }

    @Override
    public final void removeFileChangeListener(final org.openide.filesystems.FileChangeListener fcl) {
        getEventSupport().remove(FileChangeListener.class, fcl);
        if (noFolderListeners()) {
            Watcher.unregister(this);
        }
    }
    
    protected abstract boolean noFolderListeners();
    
    final boolean noListeners() {
        return getEventSupport().getListenerCount() == 0;
    }

    @Override
    public void addRecursiveListener(FileChangeListener fcl) {
        addFileChangeListener(fcl);
    }
    
    @Override
    public void removeRecursiveListener(FileChangeListener fcl) {
        removeFileChangeListener(fcl);
    }

    private Enumeration<FileChangeListener> getListeners() {
        synchronized (EVENT_SUPPORT_LOCK) {
            if (eventSupport == null) {
                return Enumerations.empty();
            }
            return Enumerations.array(eventSupport.getListeners(FileChangeListener.class));
        }
    }


    @Override
    public final long getSize() {
        return getFileName().getFile().length();
    }

    @Override
    public final void setImportant(final boolean b) {
    }


    @Override
    public boolean isReadOnly() {
        final File f = getFileName().getFile();
        ProvidedExtensions extension = getProvidedExtensions();
        return !extension.canWrite(f) && FileChangedManager.getInstance().exists(f);
    }

    @Override
    public final FileObject getParent() {
        FileObject retVal = null;
        if (!isRoot()) {
            final FileNaming parent = getFileName().getParent();
            if (BaseUtilities.isWindows()) {
                if (parent == null) {
                    retVal = FileBasedFileSystem.getInstance().getRoot();
                } else {
                    final FileObjectFactory factory = getFactory();
                    final File file = parent.getFile();
                    retVal = factory.getCachedOnly(file);
                    retVal = (retVal == null) ? factory.getFileObject(new FileInfo(file), FileObjectFactory.Caller.GetParent, true) : retVal;
                }
            } else if ((parent != null)) {
                final FileObjectFactory factory = getFactory();
                final File file = parent.getFile();
                if (file.getParentFile() == null) {
                    retVal = FileBasedFileSystem.getInstance().getRoot();
                } else {
                    retVal = factory.getCachedOnly(file);
                    if (retVal == null) {
                        if (this.isValid()) {
                            retVal = factory.getFileObject(new FileInfo(file), FileObjectFactory.Caller.GetParent, true);
                        } else {
                            retVal = factory.getFileObject(new FileInfo(file), FileObjectFactory.Caller.Refresh, false);
                        }
                    }
                }
            }
            assert retVal != null : "getParent should not return null for " + this;
        }
        return retVal;
    }
        
    static File getFile(final File f, final String name, final String ext) {
        File retVal;

        final StringBuffer sb = new StringBuffer();
        sb.append(name);
        if (ext != null && ext.length() > 0) {
            sb.append(BaseFileObj.EXT_SEP);
            sb.append(ext);
        }
        retVal = new File(f, sb.toString());
        return retVal;
    }

    public final FileObjectFactory getFactory() {
        return FileObjectFactory.getInstance(getFileName().getFile());
    }

    final void fireFileDataCreatedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();

        final BaseFileObj parent = getExistingParent();
        Enumeration<FileChangeListener> pListeners = (parent != null) ? parent.getListeners() : null;
        
        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected, 0);
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileDataCreatedEvent(getListeners(), fe);
            parent.fireFileDataCreatedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected, 0);
            fireFileDataCreatedEvent(getListeners(), fe);
        }
        stopWatch.stop();
        LOG.log(Level.FINER, "fireFileDataCreatedEvent {0}", this);
    }


    final void fireFileFolderCreatedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        
        final BaseFileObj parent = getExistingParent();
        Enumeration<FileChangeListener> pListeners = (parent != null) ? parent.getListeners() : null;

        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected, 0);
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileFolderCreatedEvent(getListeners(), fe);
            parent.fireFileFolderCreatedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected, 0);
            fireFileFolderCreatedEvent(getListeners(), fe);
        }
        stopWatch.stop();
        LOG.log(Level.FINER, "fireFileFolderCreatedEvent {0}", this);
    }

    public final void fireFileChangedEvent(final boolean expected) {
        getProvidedExtensions().fileChanged(this);

        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        FileObject p = getExistingParent();
        final BaseFileObj parent = (BaseFileObj)((p instanceof BaseFileObj) ? p : null);//getExistingParent();
        Enumeration<FileChangeListener> pListeners = (parent != null) ? parent.getListeners() : null;
        
        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected, lastModified().getTime());
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileChangedEvent(getListeners(), fe);
            parent.fireFileChangedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected, lastModified().getTime());
            fireFileChangedEvent(getListeners(), fe);
        }
        stopWatch.stop();
        LOG.log(Level.FINER, "fireFileChangedEvent {0}", this);
    }


    final void fireFileDeletedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        FileObject p = getExistingParent();
        final BaseFileObj parent = (BaseFileObj)((p instanceof BaseFileObj) ? p : null);//getExistingParent();
        Enumeration<FileChangeListener> pListeners = (parent != null) ?parent.getListeners() : null;
        
        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected, 0);
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileDeletedEvent(getListeners(), fe);
            parent.fireFileDeletedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected, 0);
            fireFileDeletedEvent(getListeners(), fe);
        }
        stopWatch.stop();
        LOG.log(Level.FINER, "fireFileDeletedEvent {0}", this);
    }


    private void fireFileRenamedEvent(final String originalName, final String originalExt) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        final BaseFileObj parent = getExistingParent();
        Enumeration<FileChangeListener> pListeners = (parent != null) ? parent.getListeners() : null;

        fireFileRenamedEvent(getListeners(), new FileRenameEvent(this, originalName, originalExt));

        if (parent != null && pListeners != null) {
            parent.fireFileRenamedEvent(pListeners, new FileRenameEvent(parent, this, originalName, originalExt));
        }
        
        stopWatch.stop();
        LOG.log(Level.FINER, "fireFileRenamedEvent {0} oldName {1} ext {2}", new Object[] { this, originalName, originalExt });
    }

    final void fireFileAttributeChangedEvent(final String attrName, final Object oldValue, final Object newValue) {
        final BaseFileObj parent = getExistingParent();
        Enumeration<FileChangeListener> pListeners = (parent != null) ? parent.getListeners() : null;

        fireFileAttributeChangedEvent(getListeners(), new FileAttributeEvent(this, this, attrName, oldValue, newValue));

        if (parent != null && pListeners != null) {
            parent.fireFileAttributeChangedEvent(pListeners, new FileAttributeEvent(parent, this, attrName, oldValue, newValue));
        }
        LOG.log(Level.FINER, "fireFileAttributeChangedEvent {0} attribute {1}", new Object[] { this, attrName });
    }


    public final FileNaming getFileName() {
        return fileName;
    }
    
    @Override
    public final void delete(final FileLock lock) throws IOException {
        FileBasedFileSystem.runAsInconsistent(() -> {
            ProvidedExtensions pe = getProvidedExtensions();
            pe.beforeDelete(BaseFileObj.this);
            try {
                delete(lock, pe.getDeleteHandler(getFileName().getFile()));
            } catch (IOException iex) {
                getProvidedExtensions().deleteFailure(BaseFileObj.this);
                throw iex;
            }
            getProvidedExtensions().deleteSuccess(BaseFileObj.this);
            return true;
        });
    }    

    public void delete(final FileLock lock, ProvidedExtensions.DeleteHandler deleteHandler) throws IOException {        
        final File f = getFileName().getFile();

        final FolderObj existingParent = getExistingParent();
        final ChildrenCache childrenCache = (existingParent != null) ? existingParent.getChildrenCache() : null;
        final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;

        if (mutexPrivileged != null) {
            mutexPrivileged.enterWriteAccess();
        }
        try {
            if (!checkLock(lock)) {
                FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N                
            }

            boolean deleteStatus = (deleteHandler != null) ? deleteHandler.delete(f) : deleteFile(f);
            if (!deleteStatus) {
                FileObject parent = getExistingParent();
                String parentPath = (parent != null) ? parent.getPath() : f.getParentFile().getAbsolutePath();
                FSException.io("EXC_CannotDelete", f.getName(), parentPath);// NOI18N            
            } 
            BaseFileObj.attribs.deleteAttributes(f.getAbsolutePath().replace('\\', '/'));//NOI18N
            if (childrenCache != null) {
                if (deleteHandler != null) {
                    childrenCache.removeChild(getFileName());
                } else {
                    childrenCache.getChild(BaseFileObj.getNameExt(f), true);
                }
            }
        } finally {
            if (mutexPrivileged != null) {
                mutexPrivileged.exitWriteAccess();
            }
        }

        setValid(false);
        fireFileDeletedEvent(false);

    }

    /**
     * Delete a file. Mimic API of {@link java.io.File} (return false on
     * failure), but log the exception if the operation was not successful.
     *
     * @param f The file to delete.
     *
     * @return True if the file was deleted, false otherwise.
     */
    private static boolean deleteFile(File f) {
        try {
            Path p = f.toPath();
            try {
                Files.delete(p);
                return true;
            } catch (NoSuchFileException ex) {
                LOG.log(Level.INFO, "File not found: " + p, ex);        //NOI18N
                return false;
            } catch (DirectoryNotEmptyException ex) {
                LOG.log(Level.INFO, "Non-empty directory: " + p, ex);   //NOI18N
                return false;
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot delete file: " + p, ex);    //NOI18N
                return false;
            }
        } catch (InvalidPathException e) { // invalid Path, but valid File?
            LOG.log(Level.FINE, null, e);
            return f.delete();
        }
    }
    
    abstract boolean checkLock(FileLock lock) throws IOException;

    public Object writeReplace() {
        return new ReplaceForSerialization(getFileName().getFile());
    }

    protected abstract void setValid(boolean valid);
    abstract void refreshImpl(final boolean expected, boolean fire);

    @Override
    public boolean isValid() {
        return NamingFactory.isValid(getFileName());
    }

    public final void refresh(final boolean expected, boolean fire) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FILE);
        stopWatch.start();
        try {   
            if (isValid()) {
                refreshImpl(expected, fire);
                if (isValid()) {
                    final File file = getFileName().getFile();
                    final boolean isDir = file.isDirectory();
                    final boolean isFile = file.isFile();
                    if (isDir == isFile || isFolder() != isDir || isData() != isFile) {
                        invalidateFO(fire, expected, true);
                    }
                } else if (isData()) {
                    refreshExistingParent(expected, fire);
                }
            }
        } catch (Error e) { // #249301
            LOG.log(Level.INFO, "Cannot refresh file {0}", getPath());  //NOI18N
            throw e;
        } finally {
            stopWatch.stop();
        }
    }

    void refreshExistingParent(final boolean expected, boolean fire) {
        boolean validityFlag = FileChangedManager.getInstance().exists(getFileName().getFile());
        if (!validityFlag) {
            invalidateFO(fire, expected, true);
        } 
    }

    final void invalidateFO(boolean fire, final boolean expected, boolean createNewFN) {
        //fileobject is invalidated
        FolderObj parent = getExistingParent();
        if (parent != null) {
            ChildrenCache childrenCache = parent.getChildrenCache();
            final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
            if (mutexPrivileged != null) {
                mutexPrivileged.enterWriteAccess();
            }
            try {
                childrenCache.getChild(getFileName().getFile().getName(), true);
            } finally {
                if (mutexPrivileged != null) {
                    mutexPrivileged.exitWriteAccess();
                }
            }
        }
        setValid(false);
        if (createNewFN) {
            NamingFactory.fromFile(getFileName().getParent(), getFileName().getFile(), true);
        }
        if (fire) {
            notifyDeleted(expected);
        }
    }
    
    final void notifyDeleted(final boolean expected) {
        getProvidedExtensions().deletedExternally(this);
        fireFileDeletedEvent(expected);
    }

    private void updateFileName(FileNaming oldName, FileNaming oldRoot, FileNaming newRoot) {
        Deque<String> names = new ArrayDeque<>();

        while (oldRoot != oldName && oldName != null) {
            names.addLast(oldName.getName());
            oldName = oldName.getParent();
        }

        File prev = newRoot.getFile();
        while (!names.isEmpty()) {
            String n = names.removeLast();
            newRoot = NamingFactory.fromFile(newRoot, prev = new File(prev, n), true);
        }
        assert newRoot != null;
        fileName = newRoot;
        
        afterRename();
    }


    //TODO: attributes written by VCS must be readable by FileBaseFS and vice versa  
/**
 * FileBaseFS 
 * <fileobject name="E:\work\nb_all8\openide\masterfs\src\org\netbeans\modules\masterfs">
 *      <attr name="OpenIDE-Folder-SortMode" stringvalue="S"/>
 *
 * VCS FS
 * </fileobject>
 * <fileobject name="e:|work|nb_all8openide|masterfs|src|org|netbeans|modules|masterfs">
 *      <attr name="OpenIDE-Folder-SortMode" stringvalue="F"/>
 *  
 */    
    private static final class BridgeForAttributes implements AbstractFileSystem.List, AbstractFileSystem.Change, AbstractFileSystem.Info {
        @Override
        public final Date lastModified(final String name) {
            final File file = new File(name);
            return new Date(file.lastModified());
        }

        @Override
        public final boolean folder(final String name) {
            final File file = new File(name);
            return file.isDirectory();
        }

        @Override
        public final boolean readOnly(final String name) {
            final File file = new File(name);
            return !file.canWrite();

        }

        @Override
        public final String mimeType(final String name) {
            return "content/unknown"; // NOI18N;
        }

        @Override
        public final long size(final String name) {
            final File file = new File(name);
            return file.length();
        }

        @Override
        public final InputStream inputStream(final String name) throws FileNotFoundException {
            final File file = new File(name);
            return new FileInputStream(file);

        }

        @Override
        public final OutputStream outputStream(final String name) throws IOException {
            final Path path = Path.of(name);
            return Files.newOutputStream(path);
        }

        @Override
        public final void lock(final String name) throws IOException {
        }

        @Override
        public final void unlock(final String name) {
        }

        @Override
        public final void markUnimportant(final String name) {
        }

        @Override
        public final String[] children(final String f) {
            final File file = new File(f);
            return file.list();
        }

        @Override
        public final void createFolder(final String name) throws IOException {
            final File file = new File(name);
            if (!file.mkdirs()) {
                final IOException ioException = new IOException(name);
                throw ioException;
            }
        }

        @Override
        public final void createData(final String name) throws IOException {
            final File file = new File(name);
            if (!file.createNewFile()) {
                throw new IOException(name);
            }
        }

        @Override
        public final void rename(final String oldName, final String newName) throws IOException {
            final File file = new File(oldName);
            final File dest = new File(newName);

            if (!file.renameTo(dest)) {
                FSException.io("EXC_CannotRename", file.getName(), "", dest.getName()); // NOI18N                
            }
        }

        @Override
        public final void delete(final String name) throws IOException {
            final File file = new File(name);
            final boolean isDeleted = (file.isFile()) ? deleteFile(file) : deleteFolder(file);
            if (isDeleted) {
                FSException.io("EXC_CannotDelete", file.getName(), ""); // NOI18N                                
            }
        }

        private boolean deleteFolder(final File file) throws IOException {

            boolean directory = false; // true if we are sure the file is folder
            try {
                Path p = file.toPath();
                try {
                    Files.delete(p);
                    return true;
                } catch (DirectoryNotEmptyException ex) {
                    // do not return, recurse the directory
                    directory = true;
                } catch (NoSuchFileException ex) {
                    LOG.log(Level.INFO, "File not found: " + p, ex);    //NOI18N
                    return false;
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Cannot delete: " + p, ex);     //NOI18N
                    return false;
                }
            } catch (InvalidPathException ex) { // invalid Path, valid File?
                LOG.log(Level.FINE, null, ex);
                if (file.delete()) {
                    return true;
                } else if (!FileChangedManager.getInstance().exists(file)) {
                    return false;
                }
            }

            if (directory || file.isDirectory()) {
                // first of all delete whole content
                final File[] arr = file.listFiles();
                if (arr != null) {  // check for null in case of I/O errors
                    for (int i = 0; i < arr.length; i++) {
                        final File f2Delete = arr[i];
                        if (!deleteFolder(f2Delete)) {
                            return false;
                        }
                    }
                }
            }

            return deleteFile(file);
        }

    }

    private EventListenerList getEventSupport() {
        synchronized (EVENT_SUPPORT_LOCK) {
            if (eventSupport == null) {
                eventSupport = new EventListenerList();
            }
            return eventSupport;
        }
    }

    final ProvidedExtensions getProvidedExtensions() {
        FileBasedFileSystem.StatusImpl status = (FileBasedFileSystem.StatusImpl) FileBasedFileSystem.getInstance().getDecorator();
        ProvidedExtensions extensions = status.getExtensions();
        return extensions;
    }

    public static FolderObj getExistingFor(File f, FileObjectFactory fbs) {
        if (fbs == null) {
            throw new NullPointerException("No factory for " + f); // NOI18N
        }
        FileObject retval = fbs.getCachedOnly(f);
        return (FolderObj) ((retval instanceof FolderObj) ? retval : null);
    }
    
    public static FolderObj getExistingParentFor(File f, FileObjectFactory fbs) {         
        final File parentFile = f.getParentFile();
        return (parentFile == null) ? null : getExistingFor(parentFile, fbs);
    }
    
    FolderObj getExistingParent() {         
        return getExistingParentFor(getFileName().getFile(), getFactory());
    }

    /**
     * Get {@link Path} object for this BaseFileObj.
     */
    private Path getNativePath() throws IOException {
        File file = getFileName().getFile();
        final Path path;
        try {
            path = file.toPath();
        } catch (RuntimeException e) {
            throw new IOException("Cannot get Path for " + this, e);    //NOI18N
        }
        return path;
    }

    @Override
    public boolean isSymbolicLink() throws IOException {
        Path p = getNativePath();
        return Files.isSymbolicLink(p);
    }

    @Override
    public FileObject readSymbolicLink() throws IOException {
        final Path path = getNativePath();
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<FileObject>() {

                        @Override
                        public FileObject run() throws Exception {
                            Path target = Files.readSymbolicLink(path);
                            Path absoluteTarget = target.isAbsolute()
                                ? target
                                : path.getParent().resolve(target);
                            File file = absoluteTarget.toFile();
                            File normFile = FileUtil.normalizeFile(file);
                            return FileBasedFileSystem.getFileObject(normFile);
                        }
                    });
        } catch (PrivilegedActionException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String readSymbolicLinkPath() throws IOException {
        final Path path = getNativePath();
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<String>() {

                        @Override
                        public String run() throws Exception {
                            Path target = Files.readSymbolicLink(path);
                            return target.toString();
                        }
                    });
        } catch (PrivilegedActionException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public FileObject getCanonicalFileObject() throws IOException {
        final Path path = getNativePath();
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<FileObject>() {

                        @Override
                        public FileObject run() throws Exception {
                            Path realPath = path.toRealPath();
                            File realFile = realPath.toFile();
                            return FileBasedFileSystem.getFileObject(realFile);
                        }
                    });
        } catch (PrivilegedActionException ex) {
            throw new IOException(ex);
        }
    }
    
    private static class FileEventImpl extends FileEvent implements Enumeration<FileEvent> {
        static {
            FileBasedFileSystem.getInstance().addFileChangeListener(new Delivered());
        }
        private FileEventImpl next;
        
        @Override
        public boolean hasMoreElements() {
            return next != null;
        }

        @Override
        public FileEvent nextElement() {
            if (next == null) {
                throw new NoSuchElementException(); 
            }
            return next;
        }        
        
        public FileEventImpl(FileObject src, FileObject file, boolean expected, long time) {
            super(src, file, expected, time);
        }
        
        public FileEventImpl(FileObject src, FileEventImpl next) {
            super(src, next.getFile(), next.isExpected(), next.getTime());
            this.next = next;
        }
    }
        
    /*private static class FileRenameEventImpl extends FileRenameEvent implements Enumeration<FileEvent> {
        private FileRenameEventImpl next;
        public boolean hasMoreElements() {
            return next != null;
        }

        public FileEvent nextElement() {
            if (next == null) {
                throw new NoSuchElementException(); 
            }
            return next;
        }        
        
        public FileRenameEventImpl(FileObject src, FileRenameEventImpl next) {
            this(src, next.getFile(), next.getName(), next.getExt());
            this.next = next;            
            
        }
        
        public FileRenameEventImpl(FileObject src, String name, String ext) {
            super(src, name, ext);
        }
        public FileRenameEventImpl(FileObject src, FileObject file, String name, String ext) {
            super(src, file, name, ext, false);
        }
    }*/
    
    private static final class Delivered implements FileChangeListener {
        private void unlock(FileEvent fe) {
            Watcher.unlock((FileObject)fe.getSource());
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            unlock(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            unlock(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            unlock(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            unlock(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            unlock(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            unlock(fe);
        }
    } // end Delivered
}
