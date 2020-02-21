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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.PositionManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.repository.FileReferencesKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public class FileComponentReferences extends FileComponent {

    private static final boolean TRACE = false;
    //private static int request = 0;
    //private static int request_hit = 0;
    //private static int respons = 0;
    //private static int respons_hit = 0;

    public static boolean isKindOf(CsmReference ref, Set<CsmReferenceKind> kinds) {
        return ref instanceof FileComponentReferences.ReferenceImpl && kinds.contains(ref.getKind());
    }

    private final SortedMap<ReferenceImpl, CsmUID<CsmObject>> references;
    private final SortedMap<ReferenceImpl, CsmUID<CsmObject>> type2classifier;
    private final Map<CsmUID<?>, Collection<ReferenceImpl>> obj2refs = new HashMap<>();
    private final ReadWriteLock referencesLock = new ReentrantReadWriteLock();
    private final CsmUID<CsmFile> fileUID;

    // empty stub
    private static final FileComponentReferences EMPTY = new FileComponentReferences() {

        @Override
        public boolean addReference(CsmReference ref, CsmObject referencedObject) {
            return false;
        }

        @Override
        public boolean addResolvedReference(CsmReference ref, CsmObject cls) {
            return false;
        }

        @Override
        void put() {
        }
    };

    public static FileComponentReferences empty() {
        return EMPTY;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("UL")
    FileComponentReferences(FileComponentReferences other, boolean empty) {
        super(other);
        try {
            if (!empty) {
                other.referencesLock.readLock().lock();
            }
            references = new TreeMap<>(
                    empty ? Collections.<ReferenceImpl, CsmUID<CsmObject>>emptyMap() : other.references);
            type2classifier = new TreeMap<>(
                    empty ? Collections.<ReferenceImpl, CsmUID<CsmObject>>emptyMap() : other.type2classifier);
        } finally {
            if (!empty) {
                other.referencesLock.readLock().unlock();
            }
        }
        this.fileUID = other.fileUID;
    }

    public FileComponentReferences(FileImpl file) {
        super(new FileReferencesKey(file));
        references = new TreeMap<>();
        type2classifier = new TreeMap<>();
        this.fileUID = file.getUID();
    }

    public FileComponentReferences(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory defaultFactory = UIDObjectFactory.getDefaultFactory();
        fileUID = defaultFactory.readUID(input);
        references = defaultFactory.readReferencesSortedToUIDMap(input, fileUID);
        type2classifier = defaultFactory.readReferencesSortedToUIDMap(input, fileUID);
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            CsmUID<CsmObject> key = defaultFactory.readUID(input);
            int refSize = input.readInt();
            ArrayList<FileComponentReferences.ReferenceImpl> value = new ArrayList<>(refSize);
            for (int j = 0; j < refSize; j++) {
                FileComponentReferences.ReferenceImpl val = new FileComponentReferences.ReferenceImpl(fileUID, key, defaultFactory, input);
                value.add(val);
            }
            value.trimToSize();
            obj2refs.put(key, value);
        }
    }

    // only for EMPTY static field
    private FileComponentReferences() {
        super((org.netbeans.modules.cnd.repository.spi.Key)null);
        references = new TreeMap<>();
        type2classifier = new TreeMap<>();
        fileUID = null;
    }

    void clean() {
        referencesLock.writeLock().lock();
        try {
            references.clear();
            type2classifier.clear();
            obj2refs.clear();
        } finally {
            referencesLock.writeLock().unlock();
        }
        // PUT should be done by FileContent
        //put();
    }

    public Collection<CsmReference> getReferences(Collection<CsmObject> objects) {
        Set<CsmUID<CsmObject>> searchFor = new HashSet<>(objects.size());
        for(CsmObject obj : objects) {
            CsmUID<CsmObject> uid = UIDs.get(obj);
            searchFor.add(uid);
        }
        List<CsmReference> res = new ArrayList<>();
        referencesLock.readLock().lock();
        try {
            for (CsmUID<CsmObject> csmUID : searchFor) {
                Collection<ReferenceImpl> val = obj2refs.get(csmUID);
                if (val != null) {
                    res.addAll(val);
                }
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return res;
    }

    public Collection<CsmReference> getReferences() {
        List<CsmReference> res = new ArrayList<>();
        referencesLock.readLock().lock();
        try {
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : references.entrySet()) {
                res.add(entry.getKey());
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return res;
    }

    public CsmReference getReference(int offset) {
        return getReferenceImpl(offset, references);
    }

    public CsmReference getResolvedReference(CsmReference ref) {
        referencesLock.readLock().lock();
        try {
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : type2classifier.tailMap(new ReferenceImpl(ref.getStartOffset(), ref.getEndOffset(), ref.getText())).entrySet()) {
                if (entry.getKey().start == ref.getStartOffset() &&
                    entry.getKey().end == ref.getEndOffset() &&
                        entry.getKey().identifier.equals(ref.getText())) {
                    //request_hit++;
                    return entry.getKey();
                } else {
                    return null;
                }
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return null;
    }

    ReferenceImpl getReferenceImpl(int offset, SortedMap<ReferenceImpl, CsmUID<CsmObject>> storage) {
        //if (request > 0 && request%1000 == 0) {
        //    System.err.println("Reference statictic:");
        //    System.err.println("\tRequest:"+request+" hit "+request_hit);
        //    System.err.println("\tPut:"+respons+" hit "+respons_hit);
        //}
        //request++;
        referencesLock.readLock().lock();
        try {
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : storage.tailMap(new ReferenceImpl(offset)).entrySet()) {
                if (entry.getKey().start <= offset && offset < entry.getKey().end) {
                    //request_hit++;
                    return entry.getKey();
                } else {
                    return null;
                }
            }
        } finally {
            referencesLock.readLock().unlock();
        }
        return null;
    }

    public boolean addResolvedReference(CsmReference ref, CsmObject cls) {
         return addReferenceImpl(ref, cls, type2classifier, false);
    }

    public void removeResolvedReference(CsmReference ref) {
        CsmUID<CsmObject> remove;
        referencesLock.writeLock().lock();
        try {
            remove = type2classifier.remove(new ReferenceImpl(ref.getStartOffset(), ref.getEndOffset(), ref.getText()));
        } finally {
            referencesLock.writeLock().unlock();
        }
        if (remove != null) {
            // TODO: PUT should be done by FileContent?
            put();
        }
    }

    public boolean addReference(CsmReference ref, CsmObject referencedObject) {
         return addReferenceImpl(ref, referencedObject, references, true);
    }

    private boolean addReferenceImpl(CsmReference ref, CsmObject referencedObject, Map<ReferenceImpl, CsmUID<CsmObject>> storage, boolean index) {
        //respons++;
        if (!UIDCsmConverter.isIdentifiable(referencedObject)) {
            // ignore local references
            if (TRACE) {
                new Exception("Ignore reference to local object "+referencedObject).printStackTrace(System.err); // NOI18N
            }
            return false;
        }
        CsmUID<CsmObject> referencedUID = UIDs.get(referencedObject);
        if (!UIDProviderIml.isPersistable(referencedUID)) {
            // ignore local references
            if (TRACE) {
                new Exception("Ignore reference to local object "+referencedObject).printStackTrace(System.err); // NOI18N
            }
            return false;
        }
        CsmObject owner = ref.getOwner(); // storing
        CsmUID<CsmObject> ownerUID = getUID(owner, "Ignore local owners ", TRACE); // NOI18N
        CsmObject closestTopLevelObject = ref.getClosestTopLevelObject();
        CsmUID<CsmObject> closestTopLevelObjectUID = getUID(closestTopLevelObject, "Why local top level object? ", true); // NOI18N
        assert closestTopLevelObjectUID == null || UIDProviderIml.isPersistable(closestTopLevelObjectUID) : "not persistable top level object " + closestTopLevelObject;
        ReferenceImpl refImpl = new ReferenceImpl(fileUID, ref, referencedUID, ownerUID, closestTopLevelObjectUID);
        //if (ref.getContainingFile().getAbsolutePath().toString().endsWith("ConjunctionScorer.cpp")) {
        //    if (("sort".contentEquals(ref.getText())) && ref.getStartOffset() == 1478) {
        //        Logger.getLogger("xRef").log(Level.INFO, "{0} %n with {1} \n and owner {2}\n", new Object[]{ref, referencedObject, ownerUID});
        //    }
        //}
        referencesLock.writeLock().lock();
        try {
            CsmUID<CsmObject> old = storage.get(refImpl);
            if (old != null) {
                storage.remove(refImpl); // we have to remove key as well
            }
            storage.put(refImpl, referencedUID);
            if (index) {
                if (!referencedUID.equals(old) && old != null) {
                    Collection<ReferenceImpl> refsToOld = obj2refs.get(old);
                    if (refsToOld != null) {
                        refsToOld.remove(refImpl);
                        if (refsToOld.isEmpty()) {
                            obj2refs.remove(old);
                        }
                    }
                }
                Collection<FileComponentReferences.ReferenceImpl> value = obj2refs.get(referencedUID);
                if (value == null) {
                    value = new HashSet<>(1);
                    obj2refs.put(referencedUID, value);
                }
                value.add(refImpl);
            }
        } finally {
            referencesLock.writeLock().unlock();
        }
        // TODO: PUT should be done by FileContent?
        put();
        //respons_hit++;
        if (index) {
            ReferencesIndex.put(referencedUID, fileUID, refImpl);
        }
        return true;
    }

    private CsmUID<CsmObject> getUID(CsmObject csmObject, String warning, boolean trace) {
        CsmUID<CsmObject> csmObjectUID = null;
        if (csmObject != null) {
            if (UIDCsmConverter.isIdentifiable(csmObject)) {
                CsmUID<CsmObject> aClosestTopLevelObjectUID = UIDs.get(csmObject);
                if (UIDProviderIml.isPersistable(aClosestTopLevelObjectUID)) {
                    csmObjectUID = aClosestTopLevelObjectUID;
                } else {
                    if (trace) {
                        Utils.LOG.log(Level.WARNING, "{0} {1}\n {2}", new Object[] {warning, csmObject, new Exception()});
                    }
                }
            } else {
                if (trace) {
                    Utils.LOG.log(Level.WARNING, "{0} {1}\n {2}", new Object[] {warning, csmObject, new Exception()});
                }
            }
        }
        return csmObjectUID;
    }

    @Override
    public void write(RepositoryDataOutput out) throws IOException {
        super.write(out);
        UIDObjectFactory defaultFactory = UIDObjectFactory.getDefaultFactory();
        defaultFactory.writeUID(fileUID, out);
        referencesLock.readLock().lock();
        try {
            out.writeInt(references.size());
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : references.entrySet()) {
                defaultFactory.writeUID(entry.getValue(), out);
                entry.getKey().write(defaultFactory, out);
            }
            out.writeInt(type2classifier.size());
            for(Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : type2classifier.entrySet()) {
                defaultFactory.writeUID(entry.getValue(), out);
                entry.getKey().write(defaultFactory, out);
            }
            out.writeInt(obj2refs.size());
            for (Map.Entry<CsmUID<?>, Collection<FileComponentReferences.ReferenceImpl>> entry : obj2refs.entrySet()) {
                defaultFactory.writeUID(entry.getKey(), out);
                Collection<ReferenceImpl> value = entry.getValue();
                out.writeInt(value.size());
                for (ReferenceImpl referenceImpl : value) {
                    referenceImpl.write(defaultFactory, out);
                }
            }
        } finally {
            referencesLock.readLock().unlock();
        }
    }

    public void dump(PrintWriter printOut) {
        printOut.printf("Has %d references:%n", references.size());// NOI18N
        for (Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : references.entrySet()) {
            printOut.printf("ref %s%n\t%s:%n", entry.getKey().toString(true), entry.getValue());// NOI18N
        }
        printOut.printf("Has %d type2classifier:%n", type2classifier.size());// NOI18N
        for (Map.Entry<ReferenceImpl, CsmUID<CsmObject>> entry : type2classifier.entrySet()) {
            printOut.printf("type ref %s%n\t%s:%n", entry.getKey().toString(true), entry.getValue());// NOI18N
        }
        printOut.printf("Has %d obj2refs:%n", obj2refs.size());// NOI18N
        int refNum = 0;
        for (Map.Entry<CsmUID<?>, Collection<FileComponentReferences.ReferenceImpl>> entry : obj2refs.entrySet()) {
            printOut.printf("refs on %s:%n", entry.getKey());// NOI18N
            Collection<ReferenceImpl> value = new TreeSet<>(ReferencesIndex.REF_COMPARATOR);
            value.addAll(entry.getValue());
            refNum += value.size();
            int index = 1;
            for (ReferenceImpl referenceImpl : value) {
                printOut.printf("[%d] %s%n", index++, referenceImpl.toString(true));// NOI18N
            }
        }
        if (refNum != references.size()) {
            printOut.printf("DIFFERENT number of references refs=%d vs obj2refs=%d:%n", references.size(), refNum);// NOI18N
        }
    }

    public static final class ReferenceImpl implements CsmReference, Comparable<ReferenceImpl>{
        private final CsmUID<CsmFile> file;
        private final CsmReferenceKind refKind;
        private final CsmUID<CsmObject> refObj;
        private final int start;
        private final int end;
        private final CharSequence identifier;
        private final CsmUID<CsmObject> ownerUID;
        private final CsmUID<CsmObject> closestTopLevelObjectUID;

        // to search
        private ReferenceImpl(int start) {
            this.start = start;
            this.end = start;
            this.file = null;
            this.refKind = null;
            this.refObj = null;
            this.identifier = null;
            this.ownerUID = null;
            this.closestTopLevelObjectUID = null;
        }

        // to remove
        private ReferenceImpl(int start, int end, CharSequence identifier) {
            this.start = start;
            this.end = end;
            this.file = null;
            this.refKind = null;
            this.refObj = null;
            this.identifier = identifier;
            this.ownerUID = null;
            this.closestTopLevelObjectUID = null;
        }

        private ReferenceImpl(CsmUID<CsmFile> fileUID, CsmReference delegate, CsmUID<CsmObject> refObj, CsmUID<CsmObject> ownerUID, CsmUID<CsmObject> closestTopLevelObjectUID) {
            this.file = fileUID;
            this.refKind = delegate.getKind();
            this.refObj = refObj;
            assert refObj != null;
            this.start = PositionManager.createPositionID(fileUID, delegate.getStartOffset(), PositionManager.Position.Bias.FOWARD);
            this.end = PositionManager.createPositionID(fileUID, delegate.getEndOffset(), PositionManager.Position.Bias.BACKWARD);
            this.identifier = NameCache.getManager().getString(delegate.getText());
            this.ownerUID = ownerUID;
            this.closestTopLevelObjectUID = closestTopLevelObjectUID;
        }

        public ReferenceImpl(CsmUID<CsmFile> fileUID, CsmUID<CsmObject> refObj, UIDObjectFactory defaultFactory, RepositoryDataInput input) throws IOException {
            this.file = fileUID;
            this.refObj = refObj;
            assert refObj != null;
            this.start = input.readInt();
            this.end = input.readInt();
            this.identifier = PersistentUtils.readUTF(input, NameCache.getManager());
            this.refKind = CsmReferenceKind.values()[input.readByte()];
            this.ownerUID = defaultFactory.readUID(input);
            this.closestTopLevelObjectUID = defaultFactory.readUID(input);
        }

        void write(UIDObjectFactory defaultFactory, RepositoryDataOutput out) throws IOException {
            out.writeInt(this.start);
            out.writeInt(this.end);
            PersistentUtils.writeUTF(identifier, out);
            out.writeByte(this.refKind.ordinal());
            defaultFactory.writeUID(this.ownerUID, out);
            defaultFactory.writeUID(this.closestTopLevelObjectUID, out);
        }

        @Override
        public CsmReferenceKind getKind() {
            return refKind;
        }

        @Override
        public CsmObject getReferencedObject() {
            CsmObject out = UIDCsmConverter.UIDtoCsmObject(refObj);
            if (out == null) {
                Logger.getLogger("xRef").log(Level.FINE, "how can we store nulls? {0}", refObj); // NOI18N
            }
            return out;
        }

        @Override
        public CsmObject getOwner() {
            return UIDCsmConverter.UIDtoCsmObject(ownerUID);
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return UIDCsmConverter.UIDtoCsmObject(closestTopLevelObjectUID);
        }

        @Override
        public CsmFile getContainingFile() {
            return file.getObject();
        }

        public CsmUID<CsmFile> getContainingFileUID() {
            return file;
        }

        @Override
        public int getStartOffset() {
            return PositionManager.getOffset(file, start);
        }

        @Override
        public int getEndOffset() {
            return PositionManager.getOffset(file, end);
        }

        @Override
        public Position getStartPosition() {
            return PositionManager.getPosition(file, start);
        }

        @Override
        public Position getEndPosition() {
            return PositionManager.getPosition(file, end);
        }

        @Override
        public CharSequence getText() {
            return identifier;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + this.start;
            hash = 17 * hash + this.end;
            hash = 17 * hash + (this.identifier != null ? this.identifier.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ReferenceImpl other = (ReferenceImpl) obj;
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if (this.identifier != other.identifier && (this.identifier == null || !this.identifier.equals(other.identifier))) {
                return false;
            }
            return true;
        }
        @Override
        public int compareTo(ReferenceImpl o) {
            int res = start - o.end;
            if (res > 0) {
                return res;
            }
            res = end - o.start;
            if (res < 0) {
                return res;
            }
            // we are equal now
            res = 0;
            if (identifier != null && o.identifier != null) {
                res = identifier.hashCode() - o.identifier.hashCode();
            }
            return res;
        }

        @Override
        public String toString() {
            return toString(false);
        }

        /*package*/ String toString(boolean minimal) {
            if (minimal) {
                String stString = PositionManager.getPosition(file, start).toString();
                String endString = PositionManager.getPosition(file, end).toString();
                return identifier+"["+stString+"-"+endString+"] refKind=" + refKind; // NOI18N
            } else {
                return identifier+"["+start+","+end+"] file=" + file + ";refKind=" + refKind + ";refObj=" + refObj + ";topUID=" + closestTopLevelObjectUID + ";ownerUID=" + ownerUID + '}'; // NOI18N
            }
        }
    }
}
