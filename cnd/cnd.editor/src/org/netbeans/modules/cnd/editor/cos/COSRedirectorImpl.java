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

package org.netbeans.modules.cnd.editor.cos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.OpenSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableOpenSupportRedirector;

/**
 *
 */
@ServiceProvider(service = CloneableOpenSupportRedirector.class, position = 1000)
public class COSRedirectorImpl extends CloneableOpenSupportRedirector {

    private static final Logger LOG = Logger.getLogger(COSRedirectorImpl.class.getName());
    private static final boolean ENABLED;
    private static final boolean ENABLED_REMOTE;
    private static final int L1_CACHE_SIZE = 10;
    private final static FileKey INVALID_INODE = FileKey.createInvalid();

    private static class FileKey {

        private final int fs;
        private final long dev;
        private final long inode;

        private FileKey(int fs, long dev, long inode) {
            this.fs = fs;
            this.dev = dev;
            this.inode = inode;
        }

        public static FileKey createLocal(long fileID) {
            return new FileKey(0, 0, fileID);
        }

        public static FileKey createRemote(FileSystem fs, CndFileSystemProvider.CndStatInfo statInfo) {
            return new FileKey(System.identityHashCode(fs), statInfo.device, statInfo.inode);
        }

        private static FileKey createInvalid() {
            return new FileKey(0, 0, -1);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + this.fs;
            hash = 47 * hash + (int) (this.dev ^ (this.dev >>> 32));
            hash = 47 * hash + (int) (this.inode ^ (this.inode >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileKey other = (FileKey) obj;
            if (this.fs != other.fs) {
                return false;
            }
            if (this.dev != other.dev) {
                return false;
            }
            if (this.inode != other.inode) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Key{" + "fs=" + fs + ", dev=" + dev + ", inode=" + inode + '}'; // NOI18N
        }
    }

    private static final Method getDataObjectMethod;

    static {
        Method m = null;
        try {
           m = OpenSupport.Env.class.getDeclaredMethod("getDataObject", new Class[0]); //NOI18N
           m.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            // ignoring
        } catch (SecurityException ex) {
            // ignoring
        } finally {
            getDataObjectMethod = m;
        }
    }

    static {
        String prop = System.getProperty("nb.cosredirector", "true");
        boolean enabled = true;
        try {
            enabled = Boolean.parseBoolean(prop);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        ENABLED = enabled;
        ENABLED_REMOTE = enabled && CndUtils.getBoolean("nb.cosredirector.remote", true); //NOI18N
    }
    private final Map<FileKey, COSRedirectorImpl.Storage> imap = new HashMap<FileKey, COSRedirectorImpl.Storage>();
    private final LinkedList<FileKey> cache = new LinkedList<FileKey>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    protected CloneableOpenSupport redirect(CloneableOpenSupport.Env env) {
        DataObject dobj = getDataObjectIfApplicable(env);
        if (dobj == null) {
            return null;
        }
        Lookup dobjLookup = dobj.getLookup();
        if (dobjLookup == null) {
            return null;
        }
        CloneableOpenSupport cos = env.findCloneableOpenSupport();
        COSRedirectorImpl.Storage storage = getCachedStorage(dobj);
        if (storage != null) {
            CloneableOpenSupport aCes = storage.getCloneableOpenSupport(dobj, cos);
            if (aCes != null) {
                if (cos != aCes && LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Use {0}:\n\t{1}\n for {2}", new Object[]{storage.cosPath, aCes, dobj.getPrimaryFile().getPath()});
                }
                return aCes;
            }
        }
        FileKey inode = getINode(dobj);
        if (inode == INVALID_INODE) {
            return null;
        } 
        { // update L1 cache
            lock.writeLock().lock();
            try {
                cache.remove(inode);
                cache.addFirst(inode);
                if (cache.size() > L1_CACHE_SIZE) {
                    cache.removeLast();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        storage = findOrCreateINodeList(inode);
        if (storage.addDataObject(inode, dobj, cos)) {
            return null;
        }
        CloneableOpenSupport aCes = storage.getCloneableOpenSupport(dobj, cos);
        if (cos != aCes && LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Use {0}:\n\t{1}\n for {2}", new Object[]{storage.cosPath, aCes, dobj.getPrimaryFile().getPath()});
        }
        return aCes;
    }

    private Storage findOrCreateINodeList(FileKey inode) {
        assert inode != INVALID_INODE;
        COSRedirectorImpl.Storage list;
        lock.writeLock().lock();
        try {
            list = imap.get(inode);
            if (list == null) {
                list = new COSRedirectorImpl.Storage();
                imap.put(inode, list);
            }
        } finally {
            lock.writeLock().unlock();
        }
        return list;
    }

    @Override
    protected void opened(CloneableOpenSupport.Env env) {
        redirect(env);
    }

    @Override
    protected void closed(CloneableOpenSupport.Env env) {
        DataObject dobj = getDataObjectIfApplicable(env);
        if (dobj == null) {
            return;
        }
        COSRedirectorImpl.Storage storage = getCachedStorage(dobj);
        // calculate out of lock if needed
        FileKey inode = INVALID_INODE;
        if (storage == null) {
            inode = getINode(dobj);
        }
        lock.writeLock().lock();
        try {
            if (storage == null) {
                storage = imap.get(inode);
                if (storage == null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "clear not existing for inode={0}:dobj={1}\nimap.get={2} cache.contains={3} cache.size={4}, imap.size={5}\n", 
                                new Object[] {inode, dobj, imap.get(inode), cache.contains(inode), cache.size(), imap.size()});
                    }                     
                    return;
                }
            }
            storage.clear();
            cache.remove(inode);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "clear for inode={0}:dobj={1}\nimap.get={2} cache.contains={3} cache.size={4}, imap.size={5}\n", 
                        new Object[] {inode, dobj, imap.get(inode), cache.contains(inode), cache.size(), imap.size()});
            }            
        } finally {
            lock.writeLock().unlock();
        }        
    }

    private COSRedirectorImpl.Storage getCachedStorage(DataObject dobj) {
        lock.writeLock().lock();
        try {
            for (FileKey n : cache) {
                COSRedirectorImpl.Storage storage = imap.get(n);
                if (storage != null) {
                    if (storage.hasDataObject(dobj)) {
                        // found
                        return storage;
                    }
                }
            }
            return null;
        } finally {
            lock.writeLock().unlock();
        }        
    }

    private DataObject getDataObjectIfApplicable(CloneableOpenSupport.Env env) {
        if (!ENABLED) {
            return null;
        }
        if (!(env instanceof OpenSupport.Env)) {
            return null;
        }
        DataObject dobj = null;
        if (getDataObjectMethod != null) {
            try {
                dobj = (DataObject) getDataObjectMethod.invoke(env);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (dobj == null) { // See CR#7117235
            return null;
        }
        if (!dobj.isValid()) {
            return null;
        }
        FileObject primaryFile = dobj.getPrimaryFile();
        if (primaryFile == null) {
            return null;
        }
        try {
            if (CndFileUtils.isLocalFileSystem(primaryFile.getFileSystem())) {
                // disable on windows (for local files) for now
                if (Utilities.isWindows()) {
                    return null;
                }
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        return dobj;
    }

    private static final class Storage {

        private final List<COSRedirectorImpl.StorageItem> list = new LinkedList<COSRedirectorImpl.StorageItem>();
        private WeakReference<CloneableOpenSupport> cosRef;
        private String cosPath = ""; // NOI18N

        private Storage() {
        }

        private synchronized boolean addDataObject(FileKey origINode, DataObject dao, CloneableOpenSupport cos) {
            Iterator<COSRedirectorImpl.StorageItem> iterator = list.iterator();
            boolean found = false;
            while (iterator.hasNext()) {
                COSRedirectorImpl.StorageItem next = iterator.next();
                DataObject aDao = next.getValidDataObject();
                if (aDao == null) {
                    iterator.remove();
                } else if (aDao.equals(dao)) {
                    found = true;
                }
            }
            // clean up if removed all due to invalid data objects
            if (list.isEmpty()) {
                cosRef = null;
            }
            if (!found) {
                list.add(createItem(this, origINode, dao, cos));
            }
            if (cosRef == null || cosRef.get() == null) {
                cosRef = new WeakReference<CloneableOpenSupport>(cos);
                cosPath = dao.getPrimaryFile().getPath();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Store SES for {0}", cosPath);
                }
                return true;
            }
            return false;
        }

        private synchronized void clear() {
            list.clear();
            cosRef = null;
        }

        private synchronized boolean hasDataObject(DataObject dao) {
            Iterator<COSRedirectorImpl.StorageItem> iterator = list.iterator();
            while (iterator.hasNext()) {
                COSRedirectorImpl.StorageItem next = iterator.next();
                DataObject aDao = next.getValidDataObject();
                if (aDao == null) {
                    iterator.remove();
                } else if (aDao.equals(dao)) {
                    return true;
                }
            }
            return false;
        }

        private synchronized CloneableOpenSupport getCloneableOpenSupport(DataObject dao, CloneableOpenSupport cos) {
            CloneableOpenSupport aCos = null;
            if (cosRef != null) {
                aCos = cosRef.get();
                if (aCos == null) {
                    list.clear();
                    cosRef = null;
                    cosPath = "";
                } else {
                    return aCos;
                }
            }
            Iterator<COSRedirectorImpl.StorageItem> iterator = list.iterator();
            while (iterator.hasNext()) {
                COSRedirectorImpl.StorageItem next = iterator.next();
                DataObject aDao = next.getValidDataObject();
                if (aDao == null) {
                    iterator.remove();
                } else if (aDao.equals(dao)) {
                    cosRef = new WeakReference<CloneableOpenSupport>(cos);
                    cosPath = dao.getPrimaryFile().getPath();
                    aCos = cos;
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Store SES for {0}", cosPath);
                    }
                    break;
                }
            }
            return aCos;
        }

        @Override
        public String toString() {
            return cosPath + ":" + list + "->" + cosRef;  // NOI18N
        }

        private void removeItem(StorageItem item) {
            assert item.removed.get();
            item.dao.removePropertyChangeListener(item);
            item.file.removeFileChangeListener(item);
            synchronized (this) {
                list.remove(item);
            }
        }
    }

    private static StorageItem createItem(Storage owner, FileKey origINode, DataObject dao, CloneableOpenSupport origCOS) {
        FileObject primaryFile = dao.getPrimaryFile();
        StorageItem out = new COSRedirectorImpl.StorageItem(owner, origINode, dao, primaryFile, origCOS);
        dao.addPropertyChangeListener(out);
        primaryFile.addFileChangeListener(out);
        return out;
    }
    
    private static FileKey getINode(DataObject dao) {
        if (!dao.isValid()) {
            return INVALID_INODE;
        }
        FileObject fo = dao.getPrimaryFile();
        try {
            FileSystem fs = fo.getFileSystem();
            if (CndFileSystemProvider.isRemote(fs)) {
                if (ENABLED_REMOTE) {
                    try {
                        FileObject canonicalFO = fo.getCanonicalFileObject();
                        CndFileSystemProvider.CndStatInfo statInfo = CndFileSystemProvider.getStatInfo(canonicalFO);
                        if (statInfo.isValid()) {
                            return FileKey.createRemote(fs, statInfo);
                        } else {
                            return INVALID_INODE;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                } else {
                    return INVALID_INODE;
                }
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return INVALID_INODE;
        }

        BasicFileAttributes attrs = null;
        try {
            Path path = FileSystems.getDefault().getPath(FileUtil.getFileDisplayName(dao.getPrimaryFile()));
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (FileNotFoundException ex) {
            // it is OK for file to be deleted
            LOG.log(Level.FINE, "FileNotFoundException: can not get inode for {0}:\n{1}", new Object[] {dao, ex.getMessage()});
        } catch (NoSuchFileException ex) {
            // it is OK for file to be deleted
            LOG.log(Level.FINE, "NoSuchFileException: can not get inode for {0}:\n{1}", new Object[] {dao, ex.getMessage()});
        } catch (InvalidPathException ex) {
            LOG.log(Level.INFO, "InvalidPathException: can not get inode for {0}:\n{1}", new Object[]{dao, ex.getMessage()});
        } catch (IOException ex) {
            LOG.log(Level.INFO, "{0}: can not get inode for {1}:\n{2}", new Object[] {ex.getClass().getName(), dao, ex.getMessage()});
        }
        Object key = null;
        if (attrs != null) {
            key = attrs.fileKey();
        }
        FileKey inode = INVALID_INODE;
        if (key != null) {
            inode = FileKey.createLocal(key.hashCode());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "getInode {0}[{1}], {2}", new Object[] {key, dao, inode});
            }
        }
        return inode;
    }
    
    private static final class StorageItem implements PropertyChangeListener, FileChangeListener {

        private final DataObject dao;
        private final FileKey origINode;
        private final AtomicBoolean removed = new AtomicBoolean(false);
        private final CloneableOpenSupport origCOS;
        private final Storage owner;
        private final FileObject file;

        private StorageItem(Storage owner, FileKey origINode, DataObject dao, FileObject primaryFile, CloneableOpenSupport cos) {
            this.owner = owner;
            this.dao = dao;
            this.file = primaryFile;
            this.origINode = origINode;
            this.origCOS = cos;
        }

        private DataObject getValidDataObject() {
            if (!removed.get() && dao.isValid()) {
                return dao;
            }
            return null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!removed.get()) {
                if (evt.getPropertyName().equals(DataObject.PROP_NAME) ||
                        evt.getPropertyName().equals(DataObject.PROP_VALID) ||
                        evt.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
                    if (!(evt.getSource() instanceof DataObject)) {
                        return;
                    }
                    DataObject toBeRemoved = (DataObject) evt.getSource();
                    if (dao.equals(toBeRemoved)) {
                        checkAndUpdateIfNeeded();
                    }
                }
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            checkAndUpdateIfNeeded();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            removed.set(true);
            owner.removeItem(this);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            checkAndUpdateIfNeeded();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        private void checkAndUpdateIfNeeded() {
            FileKey curINode = getINode(dao);
            // track file remove followed by create with the same name
            // also handles file removes where curInode is invalid
            if (origINode != curINode) {
                LOG.log(Level.INFO, "inode file Changed {0} {1}->{2}", new Object[] {dao, origINode, curINode});
                if (removed.compareAndSet(false, true)) {
                    if (curINode != INVALID_INODE) {
                        // register orig COS under new INode 
                        COSRedirectorImpl instance = Lookup.getDefault().lookup(COSRedirectorImpl.class);
                        Storage list = instance.findOrCreateINodeList(curINode);
                        list.addDataObject(curINode, dao, origCOS);
                    }
                    owner.removeItem(this);
                }
            }
        }

        @Override
        public String toString() {
            return origINode + "[" + removed + "]" + dao + "->" + origCOS; // NOI18N
        }
    }
}
