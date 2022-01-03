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
package org.netbeans.modules.cnd.modelimpl.content.file;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentReferences.ReferenceImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.*;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.*;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
public final class ReferencesIndex implements SelfPersistent, Persistent {

    private static final class ComparatorImpl implements Comparator<CsmUID<?>> {

        public ComparatorImpl() {
        }

        @Override
        public int compare(CsmUID<?> o1, CsmUID<?> o2) {
            int projectID1 = UIDUtilities.getProjectID(o1);
            int projectID2 = UIDUtilities.getProjectID(o2);
            if (projectID1 != projectID2) {
                return projectID1 - projectID2;
            }
            int fileID1 = UIDUtilities.getFileID(o1);
            int fileID2 = UIDUtilities.getFileID(o2);
            if (fileID1 != fileID2) {
                try {
//                    CharSequence fileName1 = KeyUtilities.getFileNameById(projectID1, fileID1);
//                    CharSequence fileName2 = KeyUtilities.getFileNameById(projectID2, fileID2);
                    return fileID1 - fileID2;
                } catch (IndexOutOfBoundsException e) {
                    System.err.printf("exception %s, when compare%n%s%nvs.%n%s%n", e.getMessage(), o1.getObject(), o2.getObject()); // NOI18N
                }
            }
            int startOffset1 = UIDUtilities.getStartOffset(o1);
            int startOffset2 = UIDUtilities.getStartOffset(o2);
            return startOffset1 - startOffset2;
        }
    }

    private static final class RefComparator implements Comparator<FileComponentReferences.ReferenceImpl> {

        public RefComparator() {
        }

        @Override
        public int compare(FileComponentReferences.ReferenceImpl o1, FileComponentReferences.ReferenceImpl o2) {
            final CsmUID<CsmFile> containingFileUID1 = o1.getContainingFileUID();
            final CsmUID<CsmFile> containingFileUID2 = o2.getContainingFileUID();
            int res;
            if (containingFileUID1 != containingFileUID2) {
                int projectID1 = UIDUtilities.getProjectID(containingFileUID1);
                int projectID2 = UIDUtilities.getProjectID(containingFileUID2);
                res = projectID1 - projectID2;
                if (res != 0) {
                    return res;
                }
    //            CharSequence containingFile1 = UIDUtilities.getFileName(o1.getContainingFileUID());
    //            CharSequence containingFile2 = UIDUtilities.getFileName(o2.getContainingFileUID());
    //            int res = CharSequenceUtils.ComparatorIgnoreCase.compare(containingFile1, containingFile2);
                int containingFileID1 = UIDUtilities.getFileID(containingFileUID1);
                int containingFileID2 = UIDUtilities.getFileID(containingFileUID2);
                res = containingFileID1 - containingFileID2;
                if (res != 0) {
                    return res;
                }
            }
            res = o1.getStartOffset() - o2.getStartOffset();
            if (res != 0) {
                return res;
            }
            return o1.getEndOffset() - o2.getEndOffset();
        }
    }

    static final Comparator<FileComponentReferences.ReferenceImpl> REF_COMPARATOR = new RefComparator();

    private static final class RefImpl implements CsmReference {
        private final CsmUID<CsmFile> containingFile;
        private final int start;
        private final int end;
        private final CsmReferenceKind kind;
        public RefImpl(CsmUID<CsmFile> fileUID, int start, int end, CsmReferenceKind kind) {
            this.containingFile = fileUID;
            this.start = start;
            this.end = end;
            this.kind = kind;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RefImpl other = (RefImpl) obj;
            if (this.containingFile != other.containingFile && (this.containingFile == null || !this.containingFile.equals(other.containingFile))) {
                return false;
            }
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.containingFile != null ? this.containingFile.hashCode() : 0);
            hash = 37 * hash + this.start;
            hash = 37 * hash + this.end;
            hash = 37 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "RefImpl{" + "start=" + start + ", end=" + end + ", kind=" + kind + '}'; // NOI18N
        }

        @Override
        public CsmReferenceKind getKind() {
            return kind;
        }

        @Override
        public CsmObject getReferencedObject() {
            return null;
        }

        @Override
        public CsmObject getOwner() {
            return null;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return null;
        }

        @Override
        public CsmFile getContainingFile() {
            return UIDCsmConverter.UIDtoFile(containingFile);
        }

        @Override
        public int getStartOffset() {
            return start;
        }

        @Override
        public int getEndOffset() {
            return end;
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }
    }

    private ReferencesIndex() {
    };

    public static SelfPersistent create(RepositoryDataInput stream) throws IOException {
        return new ReferencesIndex(stream);
    }

    private static final class Holder {
        private static final ReferencesIndex INSTANCE = ENABLED ? read() : new ReferencesIndex();
    }

    private static final class KeyHolder {
        private static final Key INDEX_KEY = new ReferencesIndexKey();
    }

    public static void shutdown() {
        if (ENABLED) {
            RepositoryUtils.closeUnit(KeyHolder.INDEX_KEY, null, !TraceFlags.PERSISTENT_REPOSITORY);
        }
    }

    public static void startup() {
        if (ENABLED) {
            RepositoryUtils.openUnit(KeyHolder.INDEX_KEY);
        }
    }

    private static final boolean TRACE = Boolean.getBoolean("cnd.model.global.index") || Boolean.getBoolean("cnd.model.index.enabled");
    private static ReferencesIndex read() {
        if (TRACE) {
            System.err.printf("Opening INDEX by key %s%n", KeyHolder.INDEX_KEY); // NOI18N
        }
        RepositoryUtils.openUnit(KeyHolder.INDEX_KEY);
        ReferencesIndex instance = (ReferencesIndex) RepositoryUtils.get(KeyHolder.INDEX_KEY);
        if (instance == null) {
            if (TRACE) {
                System.err.printf("NO REFERENCES INDEX IN REPOSITORY%n"); // NOI18N
            }
            return new ReferencesIndex();
        } else {
            if (TRACE) {
                System.err.printf("ReferencesIndex from repository has %d ref-entries%n", instance.obj2refs.size()); // NOI18N
                System.err.printf("ReferencesIndex from repository has %d file-entries%n", instance.obj2files.size()); // NOI18N
            }
            return instance;
        }
    }

    public static void dumpInfo(PrintWriter printOut) {
        Holder.INSTANCE.trace(printOut);
    }

    public static void clearIndex() {
        Holder.INSTANCE.clear();
    }

    static void put(CsmUID<?> refedObject, CsmUID<CsmFile> fileUID, FileComponentReferences.ReferenceImpl ref) {
        Holder.INSTANCE.addRef(refedObject, fileUID, ref);
    }

    public static Collection<CsmReference> getAllReferences(CsmUID<?> referedObject) {
        if (!ENABLED) {
            System.err.printf("INDEX IS DISABLED%n"); // NOI18N
        }
        final Collection<CsmReference> refs = Holder.INSTANCE.getRefs(referedObject);
        if (TRACE) {
            System.err.printf("getAllReferences for %s has %d refs%n", referedObject, refs.size()); // NOI18N
        }
        return refs;
    }

    public static Collection<CsmUID<CsmFile>> getRelevantFiles(CsmUID<?> referedObject) {
        final Collection<CsmUID<CsmFile>> refs = Holder.INSTANCE.getFiles(referedObject);
        if (TRACE) {
            System.err.printf("getAllFiles for %s has %d files%n", referedObject, refs.size()); // NOI18N
        }
        return refs;
    }

    // value either ref or collection of refs
    private final Map<CsmUID<?>, Collection<FileComponentReferences.ReferenceImpl>> obj2refs = new HashMap<>();
    private final Map<CsmUID<?>, Set<CsmUID<CsmFile>>> obj2files = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private void trace(PrintWriter printOut) {
        if (!ENABLED) {
            printOut.printf("INDEX IS DISABLED%n"); // NOI18N
            return;
        }
        if (obj2refs.isEmpty()) {
            printOut.printf("INDEX IS EMPTY%n"); // NOI18N
            return;
        }
        printOut.printf("INDEX has %d referenced objects%n", obj2refs.size()); // NOI18N
        List<CsmUID<?>> keys = new ArrayList<>(obj2refs.keySet());
        Collections.sort(keys, new ComparatorImpl());
        int lastProjectID = -1;
        int lastFileID = -1;
        int numKeys = 0;
        for (CsmUID<?> csmUID : keys) {
            int curProjectID = UIDUtilities.getProjectID(csmUID);
            if (lastProjectID != curProjectID) {
                if (lastProjectID >= 0) {
                    printOut.printf("PROJECT %s has %d referenced objects%n%n", KeyUtilities.getUnitName(lastProjectID), numKeys);// NOI18N
                }
                numKeys = 0;
                lastProjectID = curProjectID;
                printOut.printf("Elements of project [%d] %s%n", curProjectID, KeyUtilities.getUnitName(curProjectID));// NOI18N
            }
            numKeys++;
            int curFileID = UIDUtilities.getFileID(csmUID);
            if (lastFileID != curFileID) {
                lastFileID = curFileID;
                printOut.printf("Elements of project %s of file [%d] %s%n", KeyUtilities.getUnitName(curProjectID), curFileID, KeyUtilities.getFileNameById(curProjectID, curFileID));// NOI18N
            }
            Object obj = obj2refs.get(csmUID);
            if (obj == null) {
                printOut.printf("NO REFERENCES for %s%n", csmUID); // NOI18N
            }
            Collection<? extends CsmReference> refs = getRefs(csmUID);
            if (refs.isEmpty()) {
                printOut.printf("NO REFERENCES 2 for %s%n", csmUID); // NOI18N
            } else {
                printOut.printf("%s is referenced from:%n", csmUID); // NOI18N
                CsmFile prevFile = null;
                for (CsmReference ref : refs) {
                    FileComponentReferences.ReferenceImpl csmReference = (FileComponentReferences.ReferenceImpl) ref;
                    CsmFile containingFile = csmReference.getContainingFile();
                    if (containingFile != null) {
                        if (containingFile != prevFile) {
                            prevFile = containingFile;
                            printOut.printf("\tFILE %s%n", containingFile.getAbsolutePath()); // NOI18N
                        }
                        printOut.printf("\t%s%n", toString(csmReference)); // NOI18N
                    } else {
                        printOut.printf("NOT FROM FILE %s%n", toString(csmReference)); // NOI18N
                    }
                }
            }
        }
    }

    private String toString(FileComponentReferences.ReferenceImpl ref) {
        return ref.toString(true);
    }

    private void clear() {
        lock.writeLock().lock();
        try {
            obj2refs.clear();
        } finally {
            lock.writeLock().unlock();
        }
        if (ENABLED) {
            RepositoryUtils.put(KeyHolder.INDEX_KEY, this);
        }
    }

    private static final boolean ENABLED = Boolean.getBoolean("cnd.model.global.index");
    private void addRef(CsmUID<?> referedObject, CsmUID<CsmFile> fileUID, FileComponentReferences.ReferenceImpl ref) {
        if (!ENABLED) {
            // check memory after turning ON
            return;
        }
        lock.writeLock().lock();
        try {
            if (ENABLED) {
                Collection<FileComponentReferences.ReferenceImpl> value = obj2refs.get(referedObject);
                if (value == null) {
                    value = new TreeSet<>(REF_COMPARATOR);
                    obj2refs.put(referedObject, value);
                }
                value.add(ref);
            }
            Set<CsmUID<CsmFile>> files = obj2files.get(referedObject);
            if (files == null) {
                files = new HashSet<>();
                obj2files.put(referedObject, files);
            }
            files.add(fileUID);
        } finally {
            lock.writeLock().unlock();
        }
        RepositoryUtils.put(KeyHolder.INDEX_KEY, this);
    }

    private Collection<CsmReference> getRefs(CsmUID<?> refedObject) {
        lock.readLock().lock();
        try {
            Collection<FileComponentReferences.ReferenceImpl> value = obj2refs.get(refedObject);
            if (value == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<CsmReference>(value);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private Collection<CsmUID<CsmFile>> getFiles(CsmUID<?> refedObject) {
        lock.readLock().lock();
        try {
            Set<CsmUID<CsmFile>> value = obj2files.get(refedObject);
            if (value == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<>(value);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void write(RepositoryDataOutput out) throws IOException {
        UIDObjectFactory defaultFactory = UIDObjectFactory.getDefaultFactory();
        lock.readLock().lock();
        try {
            if (TRACE) {
                System.err.printf("writing REFERENCES INDEX [%s] with %d entries%n", KeyHolder.INDEX_KEY, obj2refs.size()); // NOI18N
            }
            out.writeInt(obj2refs.size());
            for (Map.Entry<CsmUID<?>, Collection<FileComponentReferences.ReferenceImpl>> entry : obj2refs.entrySet()) {
                defaultFactory.writeUID(entry.getKey(), out);
                Collection<ReferenceImpl> value = entry.getValue();
                out.writeInt(value.size());
                for (ReferenceImpl referenceImpl : value) {
                    defaultFactory.writeUID(referenceImpl.getContainingFileUID(), out);
                    referenceImpl.write(defaultFactory, out);
                }
            }
            out.writeInt(obj2files.size());
            for (Map.Entry<CsmUID<?>, Set<CsmUID<CsmFile>>> entry : obj2files.entrySet()) {
                defaultFactory.writeUID(entry.getKey(), out);
                Set<CsmUID<CsmFile>> value = entry.getValue();
                out.writeInt(value.size());
                for (CsmUID<CsmFile> csmUID : value) {
                    defaultFactory.writeUID(csmUID, out);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }


    private ReferencesIndex(RepositoryDataInput aStream) throws IOException {
        int size = aStream.readInt();
        UIDObjectFactory defaultFactory = UIDObjectFactory.getDefaultFactory();
        for (int i = 0; i < size; i++) {
            CsmUID<CsmObject> key = defaultFactory.readUID(aStream);
            Collection<FileComponentReferences.ReferenceImpl> value = new TreeSet<>(REF_COMPARATOR);
            int refSize = aStream.readInt();
            for (int j = 0; j < refSize; j++) {
                CsmUID<CsmFile> fileUID = defaultFactory.readUID(aStream);
                FileComponentReferences.ReferenceImpl val = new FileComponentReferences.ReferenceImpl(fileUID, key, defaultFactory, aStream);
                value.add(val);
            }
            obj2refs.put(key, value);
        }
        size = aStream.readInt();
        for (int i = 0; i < size; i++) {
            CsmUID<CsmObject> key = defaultFactory.readUID(aStream);
            int filesSize = aStream.readInt();
            Set<CsmUID<CsmFile>> value = new HashSet<>(filesSize);
            for (int j = 0; j < filesSize; j++) {
                CsmUID<CsmFile> fileUID = defaultFactory.readUID(aStream);
                value.add(fileUID);
            }
            obj2files.put(key, value);
        }
    }
}
