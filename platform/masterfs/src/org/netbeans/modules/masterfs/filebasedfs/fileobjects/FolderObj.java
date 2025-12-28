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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem.FSCallable;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * @author rm111737
 */
public final class FolderObj extends BaseFileObj {    
    static final long serialVersionUID = -1022430210876356809L;
    private static final Logger LOG = Logger.getLogger(FolderObj.class.getName());

    private FolderChildrenCache folderChildren;
    volatile boolean valid = true;
    private volatile FileObjectKeeper keeper;

    /**
     * Creates a new instance of FolderImpl
     */
    public FolderObj(final File file, final FileNaming name) {
        super(file, name);
        //valid = true;
    }

    @Override
    public final boolean isFolder() {
        return true;
    }

    @Override
    public FileObject getFileObject(String relativePath, boolean onlyExisting) {
        if (relativePath.equals(".")) { // NOI18N
            return this;
        }
        if(relativePath.indexOf('\\') != -1) {
            // #47885 - relative path must not contain back slashes
            return null;
        }
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        File file = new File(getFileName().getFile(), relativePath);
        if (relativePath.contains("..") || // NOI18N
            relativePath.contains("./") || // NOI18N
            relativePath.contains("/.") // NOI18N
        ) {
            file = FileUtil.normalizeFile(file);
        }
        
        FileObjectFactory factory = getFactory();
        assert factory != null : "No factory for " + getPath() + " this: " + this;
        return factory.getValidFileObject(file, FileObjectFactory.Caller.GetFileObject, onlyExisting);
    }


    @Override
    public final FileObject getFileObject(final String name, final String ext) {
        File file = BaseFileObj.getFile(getFileName().getFile(), name, ext);
        FileObjectFactory factory = getFactory();
        return (name.indexOf("/") == -1) ? factory.getValidFileObject(file, FileObjectFactory.Caller.GetFileObject, true) : null;
    }

    @Override
    protected boolean noFolderListeners() {
        if (noListeners()) {
            for (FileObject f : computeChildren(true)) {
                if (f instanceof BaseFileObj bfo) {
                    if (!bfo.noListeners()) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
  
    @Override
    public final FileObject[] getChildren() {
        return computeChildren(false);
    }
    
    private FileObject[] computeChildren(boolean onlyExisting) {
        LOOP: for (int counter = 0; ; counter++) {
            final Map<FileNaming,FileObject> results = new LinkedHashMap<>();

            final ChildrenCache childrenCache = getChildrenCache();
            final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

            Set<FileNaming> fileNames = null;
            Runnable[] task = new Runnable[1];
            while (fileNames == null) {
                if (task[0] != null) {
                    task[0].run();
                }
                mutexPrivileged.enterWriteAccess();
                try {
                    Set<FileNaming> res = childrenCache.getChildren(counter >= 10, task);
                    if (res != null) {
                        fileNames = new HashSet<>(res);
                    }   
                } finally {
                    mutexPrivileged.exitWriteAccess();
                }
            }
            // sends message to TwoFileNamesForASingleFileTest to delay execution
            LOG.log(Level.FINEST, "computeChildren, filenames: {0}", fileNames); // NOI18N

            final FileObjectFactory lfs = getFactory();        
            for (FileNaming fileName : fileNames) {
                FileInfo fInfo = new FileInfo (fileName.getFile(), 1);
                fInfo.setFileNaming(fileName);

                final FileObject fo = onlyExisting ?
                    lfs.getCachedOnly(fileName.getFile()) :
                    lfs.getFileObject(fInfo, FileObjectFactory.Caller.GetChildern, true);
                if (fo != null) {
                    final FileNaming foFileName = ((BaseFileObj)fo).getFileName();
                    if (!fo.isValid()) {
                        final Level level = counter < 10 ? Level.FINE : Level.WARNING;
                        LOG.log(level, "Invalid fileObject {0}, trying again for {1} with {2}", new Object[] { fo, counter, onlyExisting }); // NOI18N
                        if (counter > 5) {
                            onlyExisting = false;
                        }
                        assert counter < 100;
                        if (counter < 100) {
                            continue LOOP;
                        }
                    }
                    if (fileName != foFileName && counter < 10) {
                        continue LOOP;
                    }
                    assert fileName == foFileName : 
                        dumpFileNaming(fileName) + "\n" + 
                        dumpFileNaming(((BaseFileObj)fo).getFileName()) + "\nfo: " +
                        fo + "\nContent of the nameMap cache:\n" +
                        NamingFactory.dumpId(fInfo.getID());
                    results.put(fileName, fo);
                }
            }
            return results.values().toArray(FileObject[]::new);
        }
    }
    
    private static String dumpFileNaming(FileNaming fn) {
        if (fn == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("FileName: ").append(fn).append("#").
           append(Integer.toHexString(fn.hashCode())).append("@").
           append(Integer.toHexString(System.identityHashCode(fn)))
           .append("\n");
        
        if (fn instanceof FileName fileName) {
            fileName.dumpCreation(sb);
        }
        return sb.toString();
    }

    public final FileObject createFolderImpl(final String name) throws java.io.IOException {
        if (name.indexOf('\\') != -1 || name.indexOf('/') != -1) {//NOI18N
            throw new IllegalArgumentException(name);
        }
        
        FolderObj retVal = null;
        File folder2Create;
        final ChildrenCache childrenCache = getChildrenCache();
        
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        final File myFile = getFileName().getFile();
        folder2Create = BaseFileObj.getFile( myFile, name, null);
        getProvidedExtensions().beforeCreate(this, folder2Create.getName(), true);
        mutexPrivileged.enterWriteAccess();

        try {
            if (!myFile.canWrite()) {
                FSException.io("EXC_CannotCreateFolder", folder2Create.getName(), getPath());// NOI18N
            }
            Watcher.lock(this);
            createFolder(folder2Create, name);

            FileNaming childName = this.getChildrenCache().getChild(folder2Create.getName(), true);
            if (childName != null && !childName.isDirectory()) {
                childName = NamingFactory.fromFile(getFileName(), folder2Create, true);
            }            
            if (childName != null) {
                childName = NamingFactory.checkCaseSensitivity(childName, folder2Create);
            }
        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        final FileObjectFactory factory = getFactory();
        if (factory != null) {
            BaseFileObj exists = factory.getValidFileObject(folder2Create, FileObjectFactory.Caller.Others, true);
            if (exists instanceof FolderObj folderObj) {
                retVal = folderObj;
            } else {
                FSException.io("EXC_CannotCreateFolder", folder2Create.getName(), getPath());// NOI18N                           
            }
        }
        if (retVal != null) {
            retVal.fireFileFolderCreatedEvent(false);
        } else {
            FSException.io("EXC_CannotCreateFolder", folder2Create.getName(), getPath());// NOI18N                           
        }
        getProvidedExtensions().createSuccess(retVal);
        return retVal;
    }

    private void createFolder(final File folder2Create, final String name) throws IOException {
        boolean isSupported = new FileInfo(folder2Create).isSupportedFile();
        ProvidedExtensions extensions =  getProvidedExtensions();

        if (!isSupported) { 
            extensions.createFailure(this, folder2Create.getName(), true);
            FSException.io("EXC_CannotCreateFolder", folder2Create.getName(), getPath());// NOI18N   
        } else if (FileChangedManager.getInstance().exists(folder2Create)) {
            extensions.createFailure(this, folder2Create.getName(), true);            
            SyncFailedException sfe = new SyncFailedException(folder2Create.getAbsolutePath()); // NOI18N               
            String msg = NbBundle.getMessage(FileBasedFileSystem.class, "EXC_CannotCreateFolder", folder2Create.getName(), getPath()); // NOI18N
            Exceptions.attachLocalizedMessage(sfe, msg);
            throw sfe;
        } else if (!folder2Create.mkdirs()) {
            extensions.createFailure(this, folder2Create.getName(), true);
            FSException.io("EXC_CannotCreateFolder", folder2Create.getName(), getPath());// NOI18N               
        }
        LogRecord r = new LogRecord(Level.FINEST, "FolderCreated: "+ folder2Create.getAbsolutePath());
        r.setParameters(new Object[] {folder2Create});
        Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj").log(r);
    }

    @Override
    public final FileObject createData(final String name, final String ext) throws java.io.IOException {
        return FileBasedFileSystem.runAsInconsistent(() -> createDataImpl(name, ext));
    }
    
    @Override
    public final FileObject createFolder(final String name) throws java.io.IOException {
        return FileBasedFileSystem.runAsInconsistent(() -> createFolderImpl(name));        
    }
    
    
    public final FileObject createDataImpl(final String name, final String ext) throws java.io.IOException {
        if (name.indexOf('\\') != -1 || name.indexOf('/') != -1) {//NOI18N
            throw new IOException("Requested name contains invalid characters: " + name); // NOI18N
        }
        
        final ChildrenCache childrenCache = getChildrenCache();        
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();
        
        ProvidedExtensions extensions =  getProvidedExtensions();
        File file2Create;
        file2Create = BaseFileObj.getFile(getFileName().getFile(), name, ext);
        extensions.beforeCreate(this, file2Create.getName(), false);
        mutexPrivileged.enterWriteAccess();

        FileObj retVal;
        FileNaming childName;
        try {
            Watcher.lock(this);
            createData(file2Create);
            childName = getChildrenCache().getChild(file2Create.getName(), true);
            if (childName != null && childName.isDirectory()) {
                childName = NamingFactory.fromFile(getFileName(), file2Create, true);
            }
            if (childName != null) {
                childName = NamingFactory.checkCaseSensitivity(childName, file2Create);
            }

        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        final FileObjectFactory factory = getFactory();
        retVal = null;
        if (factory != null) {
            final BaseFileObj fo = factory.getValidFileObject(file2Create, FileObjectFactory.Caller.Others, true);
            try {
                retVal = (FileObj) fo;
            } catch (ClassCastException ex) {
                boolean dir = file2Create.isDirectory();
                boolean file = file2Create.isFile();
                Exceptions.attachMessage(ex, "isDir: " + dir); // NOI18N
                Exceptions.attachMessage(ex, "isFile: " + file); // NOI18N
                Exceptions.attachMessage(ex, "file: " + file2Create); // NOI18N
                Exceptions.attachMessage(ex, "fo: " + fo); // NOI18N
                Exceptions.attachMessage(ex, "fn: " + Integer.toHexString(System.identityHashCode(childName))); // NOI18N
                Exceptions.attachMessage(ex, "dump: " + NamingFactory.dumpId(childName.getId())); // NOI18N
                throw ex;
            }
        }

        if (retVal != null) {            
            if (retVal instanceof FileObj) {
                retVal.setLastModified(file2Create.lastModified(), file2Create, false);
            }
            retVal.fireFileDataCreatedEvent(false);
        } else {
            FSException.io("EXC_CannotCreateData", file2Create.getName(), getPath());// NOI18N
        }
        getProvidedExtensions().createSuccess(retVal);
        return retVal;
    }

    private void createData(final File file2Create) throws IOException {
        boolean isSupported = new FileInfo(file2Create).isSupportedFile();                        
        ProvidedExtensions extensions =  getProvidedExtensions();
        
        if (!isSupported) {             
            extensions.createFailure(this, file2Create.getName(), false);
            FSException.io("EXC_CannotCreateData", file2Create.getName(), getPath());// NOI18N
        } else if (FileChangedManager.getInstance().exists(file2Create)) {
            extensions.createFailure(this, file2Create.getName(), false);
            SyncFailedException sfe = new SyncFailedException(file2Create.getAbsolutePath()); // NOI18N               
            String msg = NbBundle.getMessage(FileBasedFileSystem.class, "EXC_CannotCreateData", file2Create.getName(), getPath()); // NOI18N
            Exceptions.attachLocalizedMessage(sfe, msg);
            throw sfe;
        } else if (!file2Create.createNewFile()) {
            extensions.createFailure(this, file2Create.getName(), false);            
            FSException.io("EXC_CannotCreateData", file2Create.getName(), getPath());// NOI18N
        }        
        LogRecord r = new LogRecord(Level.FINEST, "DataCreated: "+ file2Create.getAbsolutePath());
        r.setParameters(new Object[] {file2Create});
        Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj").log(r);        
    }

    @Override
    public void delete(final FileLock lock, ProvidedExtensions.DeleteHandler deleteHandler) throws IOException {
        final Deque<FileObject> all = new ArrayDeque<>();

        final File file = getFileName().getFile();
        if (!deleteFile(file, all, getFactory(), deleteHandler)) {
            FileObject parent = getExistingParent();
            String parentPath = (parent != null) ? parent.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotDelete", file.getName(), parentPath);// NOI18N            
        }

        BaseFileObj.attribs.deleteAttributes(file.getAbsolutePath().replace('\\', '/'));//NOI18N
        setValid(false);
        for (FileObject fo : all) {
            final BaseFileObj toDel = (BaseFileObj) fo;            
            final FolderObj existingParent = toDel.getExistingParent();            
            final ChildrenCache childrenCache = (existingParent != null) ? existingParent.getChildrenCache() : null;            
            if (childrenCache != null) {
                final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
                if (mutexPrivileged != null) {
                    mutexPrivileged.enterWriteAccess();
                }
                try {      
                    if (deleteHandler != null) {
                        childrenCache.removeChild(toDel.getFileName());
                    } else {
                        childrenCache.getChild(BaseFileObj.getNameExt(file), true);
                    }
                    
                    
                } finally {
                    if (mutexPrivileged != null) {
                        mutexPrivileged.exitWriteAccess();
                    }
                }
            }                
            toDel.setValid(false);
            toDel.fireFileDeletedEvent(false);
        }        
    }

    @Override
    public void refreshImpl(final boolean expected, boolean fire) {
        final ChildrenCache cache = getChildrenCache();
        final Mutex.Privileged mutexPrivileged = cache.getMutexPrivileged();
        final long previous = keeper == null ? -1 : keeper.childrenLastModified();

        Set<FileNaming> oldChildren = null;
        Map<FileNaming, Integer> refreshResult = null;
        Runnable[] task = new Runnable[1];
        while (refreshResult == null) {
            if (task[0] != null) {
                task[0].run();
            }
            mutexPrivileged.enterWriteAccess();
            try {
                oldChildren = cache.getCachedChildren();
                refreshResult = cache.refresh(task);
            } finally {
                mutexPrivileged.exitWriteAccess();
            }
        }

        LOG.log(Level.FINER, "refreshImpl for {0} expected: {1} fire: {2} previous: {3}", new Object[]{this, expected, fire, previous});

        oldChildren.removeAll(refreshResult.keySet());
        for (final FileNaming child : oldChildren) {
            final BaseFileObj childObj = getFactory().getCachedOnly(child.getFile());
            if (childObj != null && childObj.isData()) {
                ((FileObj) childObj).refresh(expected);
            }
        }

        final FileObjectFactory factory = getFactory();
        for (final Map.Entry<FileNaming, Integer> entry : refreshResult.entrySet()) {
            final FileNaming child = entry.getKey();
            final Integer operationId = entry.getValue();

            BaseFileObj newChild = (ChildrenCache.ADDED_CHILD.equals(operationId)) ? 
                factory.getFileObject(new FileInfo(child.getFile()), FileObjectFactory.Caller.Refresh, true) 
                : 
                factory.getCachedOnly(child.getFile());
            newChild = (BaseFileObj) ((newChild != null) ? newChild : getFileObject(child.getName()));
            if (ChildrenCache.ADDED_CHILD.equals(operationId) && newChild != null) {

                if (newChild.isFolder()) {
                    if (fire) {
                        getProvidedExtensions().createdExternally(newChild);
                        newChild.fireFileFolderCreatedEvent(expected);
                    }
                } else {
                    if (fire) {
                        getProvidedExtensions().createdExternally(newChild);
                        newChild.fireFileDataCreatedEvent(expected);
                    }
                }

            } else if (ChildrenCache.REMOVED_CHILD.equals(operationId)) {
                if (newChild != null) {
                    if (newChild.isValid()) {
                        if (newChild instanceof FolderObj folderObj) {
                            getProvidedExtensions().deletedExternally(newChild);
                            folderObj.refreshImpl(expected, fire);
                            newChild.setValid(false);
                        } else {
                            newChild.setValid(false);
                            if (fire) {
                                getProvidedExtensions().deletedExternally(newChild);
                                newChild.fireFileDeletedEvent(expected);
                            }
                        }
                    }
                } else {
                    //TODO: should be rechecked
                    //assert false;
                    final File f = child.getFile();
                    if (!(new FileInfo(f).isConvertibleToFileObject())) {
                        final BaseFileObj fakeInvalid;
                        if (child.isFile()) {
                            fakeInvalid = new FileObj(f, child);
                        } else {
                            fakeInvalid = new FolderObj(f, child);
                        }

                        fakeInvalid.setValid(false);
                        if (fire) {
                            fakeInvalid.fireFileDeletedEvent(expected);
                        }
                    }
                }

            } 

        }
        boolean validityFlag = FileChangedManager.getInstance().exists(getFileName().getFile());
        if (!validityFlag) {
            getFactory().invalidateSubtree(this, fire, expected);
        }

        if (previous != -1) {
            assert keeper != null;
            keeper.init(previous, factory, expected);
        }
    }

    @Override
    public final void refresh(final boolean expected) {
        refresh(expected, true);
    }
    
    //TODO: rewrite partly and check FileLocks for existing FileObjects
    private boolean deleteFile(final File file, final Deque<FileObject> all, final FileObjectFactory factory, ProvidedExtensions.DeleteHandler deleteHandler) throws IOException {
        final boolean ret = (deleteHandler != null) ? deleteHandler.delete(file) : file.delete();

        if (ret) {
            final FileObject aliveFo = factory.getCachedOnly(file);
            if (aliveFo != null) {
                all.addFirst(aliveFo);
            }
            return true;
        }

        if (!FileChangedManager.getInstance().exists(file)) {
            return false;
        }

        if (file.isDirectory()) {
            // first of all delete whole content
            final File[] arr = file.listFiles();
            if (arr != null) {  // check for null in case of I/O errors
                for (int i = 0; i < arr.length; i++) {
                    final File f2Delete = arr[i];
                    if (!deleteFile(f2Delete, all, factory, deleteHandler)) {
                        return false;
                    }
                }
            }
        } 
        
        // delete the file itself
        //super.delete(lock());
        

        final boolean retVal = (deleteHandler != null) ? deleteHandler.delete(file) : file.delete();
        if (retVal) {
            final FileObject aliveFo = factory.getCachedOnly(file);
            if (aliveFo != null) {
                all.addFirst(aliveFo);
            }
        }


        return true;
    }

    @Override
    protected void setValid(final boolean valid) {
        if (valid) {
            //I can't make valid fileobject when it was one invalidated
            assert isValid() : this.toString();
        } else {
            this.valid = false;
        }        
        
    }

    @Override
    public boolean isValid() {
        //assert checkCacheState(valid, getFileName().getFile());        
        return valid && super.isValid();
    }

    @Override
    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(getPath());
    }

    @Override
    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        throw new IOException(getPath());
    }


    @Override
    public final FileLock lock() throws IOException {
        return new FileLock();
    }

    @Override
    final boolean checkLock(final FileLock lock) throws IOException {
        return true;
    }

    public final ChildrenCache getChildrenCache() {
        synchronized (FolderChildrenCache.class) {
            if (folderChildren == null) {
                folderChildren = new FolderChildrenCache();
            }
            return folderChildren;
        }
    }
    
    @Override
    protected void afterRename() {
        synchronized (FolderChildrenCache.class) {
            if (folderChildren != null) {
                folderChildren = folderChildren.cloneFor(getFileName());
            }
        }
    }
    
    public final boolean hasRecursiveListener() {
        FileObjectKeeper k = keeper;
        return k != null && k.isOn();
    }

    final synchronized FileObjectKeeper getKeeper(Collection<? super File> arr) {
        if (keeper == null) {
            keeper = new FileObjectKeeper(this);
            List<File> ch = keeper.init(-1, null, false);
            if (arr != null) {
                arr.addAll(ch);
            }
        } else if (arr != null) {
            List<File> ch = keeper.init(keeper.childrenLastModified(), null, false);
            arr.addAll(ch);
        }
        return keeper;
    }

    @Override
    public final void addRecursiveListener(FileChangeListener fcl) {
        getKeeper(null).addRecursiveListener(fcl);
    }

    @Override
    public final void removeRecursiveListener(FileChangeListener fcl) {
        getKeeper(null).removeRecursiveListener(fcl);
    }


    public final class FolderChildrenCache extends ChildrenSupport implements ChildrenCache {
        @Override
        public final Set<FileNaming> getChildren(final boolean rescan, Runnable[] task) {
            return getChildren(getFileName(), rescan, task);
        }

        @Override
        public final FileNaming getChild(final String childName, final boolean rescan) {
            return getChild(childName, getFileName(), rescan);
        }

        @Override
        public FileNaming getChild(String childName, boolean rescan,
                Runnable[] task) {
            return getChild(childName, getFileName(), rescan, task);
        }

        @Override
        public final Map<FileNaming, Integer> refresh(Runnable[] task) {
            return refresh(getFileName(), task);
        }

        @Override
        public final String toString() {
            return getFileName().toString();
        }

        @Override
        public void removeChild(FileNaming childName) {
            removeChild(getFileName(), childName);
        }

        final FolderChildrenCache cloneFor(FileNaming fileName) {
            FolderChildrenCache newCache = new FolderChildrenCache();
            copyTo(newCache, getFileName());
            return newCache;
        }
    }

}
