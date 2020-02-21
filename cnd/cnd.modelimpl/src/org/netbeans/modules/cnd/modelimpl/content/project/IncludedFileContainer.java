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
package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer.FileEntry;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.IncludedFileStorageKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 * container to keep files included from project.
 */
public final class IncludedFileContainer extends ProjectComponent {
    private final List<Entry> list;

    public IncludedFileContainer(ProjectBase startProject) {
        super(new IncludedFileStorageKey(startProject));
        list = new CopyOnWriteArrayList<>();
        put();
    }

    public IncludedFileContainer(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        int count = aStream.readInt();
        Collection<Entry> aList = new ArrayList<>(count);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
//        KeyFactory keyFactory = KeyFactory.getDefaultFactory();
        for (int i = 0; i < count; i++) {
            CsmUID<CsmProject> includedProjectUID = factory.<CsmProject>readUID(aStream);
//            list.add(new Entry(includedProjectUID, startProject, storageKey));
//            Key storageKey = keyFactory.readKey(aStream);
            Storage storage = new Storage(aStream);
            aList.add(new Entry(includedProjectUID, storage));
        }
        this.list = new CopyOnWriteArrayList<>(aList);
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        List<Entry> aList = new ArrayList<>(list);
        aStream.writeInt(aList.size());
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
//        KeyFactory keyFactory = KeyFactory.getDefaultFactory();
        for (Entry entry : aList) {
            factory.writeUID(entry.prjUID, aStream);
            entry.getStorage().write(aStream);
//            keyFactory.writeKey(entry.storageKey, aStream);
        }
    }

    public void clear() {
        synchronized (list) {
            list.clear();
        }
    }

    public void invalidateIncludeStorage(CsmUID<CsmProject> libraryUID) {
        synchronized (list) {
            for (int i = 0; i < list.size(); i++) {
                Entry entry = list.get(i);
                if (entry.prjUID.equals(libraryUID)) {
                    list.remove(i);
                    return;
                }
            }
        }
    }

    public void prepareIncludeStorage(ProjectBase includedProject) {
        CsmUID<CsmProject> uid = includedProject.getUID();
        for (Entry entry : list) {
            if (entry.prjUID.equals(uid)) {
                return;
            }
        }
        synchronized (list) {
            for (Entry entry : list) {
                if (entry.prjUID.equals(uid)) {
                    return;
                }
            }
            Storage storage = new Storage(includedProject);
//            Entry includedProjectEntry = new Entry(includedProject.getUID(), srorageListOwner, storage.getKey());
            Entry includedProjectEntry = new Entry(includedProject.getUID(), storage);
            list.add(includedProjectEntry);
            put();
        }
    }

    public Storage getStorageForProject(ProjectBase includedFileOwner) {
        CsmUID<CsmProject> uid = includedFileOwner.getUID();
        for (Entry entry : list) {
            if (entry.prjUID.equals(uid)) {
                return entry.getStorage();
            }
        }
        return null;
    }

    public boolean putStorage(ProjectBase includedProject) {
        Storage storage = getStorageForProject(includedProject);
        if (storage != null) {
            put();
            return true;
        }
        return false;
    }

    public FileEntry getOrCreateEntryForIncludedFile(FileEntry entryToLockOn, ProjectBase includedProject, FileImpl includedFile) {
        assert Thread.holdsLock(entryToLockOn.getLock()) : "does not hold lock for " + includedFile;
        Storage storage = getStorageForProject(includedProject);
        if (storage != null) {
            return storage.getOrCreateFileEntry(includedFile);
        } else {
            return null;
        }
    }

    /**
     * for tracing purpose only.
     */
    public Map<CsmUID<CsmProject> , Collection<PreprocessorStatePair>> getPairsToDump(FileImpl fileToSearch) {
        Map<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> out = new HashMap<>();
        CharSequence fileKey = FileContainer.getFileKey(fileToSearch.getAbsolutePath(), false);
        for (Entry entry : list) {
            FileEntry fileEntry = entry.getStorage().getFileEntry(fileKey);
            if (fileEntry != null) {
                Collection<PreprocessorStatePair> pairs = fileEntry.getStatePairs();
                if (!pairs.isEmpty()) {
                    out.put(entry.prjUID, pairs);
                }
            }
        }
        return out;
    }

    public void invalidate(Object lock, ProjectBase includedFileOwner, CharSequence fileKey) {
        assert Thread.holdsLock(lock) : "does not hold lock for " + fileKey;
        fileKey = FileContainer.getFileKey(fileKey, false);
        Storage storage = getStorageForProject(includedFileOwner);
        if (storage != null) {
            storage.invalidate(fileKey);
            put();
        }
    }

    public boolean remove(Object lock, ProjectBase includedFileOwner, CharSequence fileKey) {
        assert Thread.holdsLock(lock) : "does not hold lock for " + fileKey;
        fileKey = FileContainer.getFileKey(fileKey, false);
        boolean out = false;
        Storage storage = getStorageForProject(includedFileOwner);
        if (storage != null) {
            out = storage.remove(fileKey) != null;
            put();
        }
        return out;
    }

    public FileContainer.FileEntry getIncludedFileEntry(Object lock, ProjectBase includedFileOwner, CharSequence fileKey) {
        assert Thread.holdsLock(lock) : "does not hold lock for " + fileKey;
        fileKey = FileContainer.getFileKey(fileKey, false);
        Storage storage = getStorageForProject(includedFileOwner);
        FileEntry fileEntry = null;
        if (storage != null) {
            fileEntry = storage.getFileEntry(fileKey);
        }
        return fileEntry;
    }

    /*tests-only*/public void debugClearState() {
        for (Entry entry : list) {
            entry.storage.debugClearState();
        }
        put();
    }

    public final static class Storage {

        private FileEntry getFileEntry(CharSequence fileKey) {
            assert CharSequences.isCompact(fileKey);
            return myFiles.get(fileKey);
        }

        private final ConcurrentMap<CharSequence, FileContainer.FileEntry> myFiles = new ConcurrentHashMap<>();
        private final FileSystem fileSystem;
        private final CsmUID<CsmProject> includedProjectUID;

        private Storage(ProjectBase includedProject) {
            this.fileSystem = includedProject.getFileSystem();
            includedProjectUID = UIDs.get((CsmProject)includedProject);
        }

        public Map<CharSequence, FileContainer.FileEntry> getInternalMap() {
            return Collections.unmodifiableMap(myFiles);
        }
        
        private void invalidate(CharSequence fileKey) {
            assert CharSequences.isCompact(fileKey);
            FileEntry entry = myFiles.get(fileKey);
            if (entry != null) {
                entry.invalidateStates();
            }
        }

        private FileEntry remove(CharSequence fileKey) {
            assert CharSequences.isCompact(fileKey);
            return myFiles.remove(fileKey);
        }

        private FileEntry getOrCreateFileEntry(FileImpl includedFile) {
            CharSequence fileKey = FileContainer.getFileKey(includedFile.getAbsolutePath(), false);
            FileEntry entry = myFiles.get(fileKey);
            if (entry == null) {
                entry = FileContainer.createFileEntry(includedFile);
                FileEntry prev = myFiles.putIfAbsent(fileKey, entry);
                if (prev != null) {
                    // must be called under FileImpl's entry lock
                    throw new ConcurrentModificationException("someone put the same file entry for " + includedFile); // NOI18N
                }
            }
            return entry;
        }

        private Storage(RepositoryDataInput aStream) throws IOException {
            fileSystem = PersistentUtils.readFileSystem(aStream);
            includedProjectUID = UIDObjectFactory.getDefaultFactory().readUID(aStream);
            FileContainer.readFilePathsForFileSystemToFileEntryMap(fileSystem, aStream, myFiles);
        }

        private void write(RepositoryDataOutput aStream) throws IOException {
            PersistentUtils.writeFileSystem(fileSystem, aStream);
            UIDObjectFactory.getDefaultFactory().writeUID(includedProjectUID, aStream);
            FileContainer.writeFilePathsForFileSystemToFileEntryMap(fileSystem, aStream, myFiles);
        }
        
        @Override
        public String toString() {
            return "Storage:" + includedProjectUID; // NOI18N
        }

        private void debugClearState() {
            List<FileEntry> files;
            files = new ArrayList<>(myFiles.values());
            for (FileEntry file : files) {
                file.debugClearState();
            }
        }

        private int getIncludedUnitId(CsmUID<CsmProject> uid) {
            if (uid instanceof KeyBasedUID<?>) {
                Key k = ((KeyBasedUID<?>) uid).getKey();
                return KeyUtilities.getProjectIndex(k);
            }
            throw new IllegalArgumentException();
        }
    
    }
    
    private static final class Entry {

        private final CsmUID<CsmProject> prjUID;
//        private final Key storageKey;
//        private final WeakContainer<Storage> storage;
        private final Storage storage;

//        public Entry(CsmUID<CsmProject> prj, CsmValidable stateOwner, Key storageKey) {
        private Entry(CsmUID<CsmProject> prj, Storage storage) {
            this.prjUID = prj;
//            this.storageKey = storageKey;
//            this.storage = new WeakContainer<Storage>(stateOwner, storageKey);
            this.storage = storage;
        }

        private Storage getStorage() {
//            Storage container = storage.getContainer();
//            assert container != null;
//            return container;
            assert storage != null;
            return storage;
        }

        @Override
        public String toString() {
            return "Entry{" + "prjUID=" + prjUID + ", storage=" + storage + '}'; // NOI18N
        }
    }
}
