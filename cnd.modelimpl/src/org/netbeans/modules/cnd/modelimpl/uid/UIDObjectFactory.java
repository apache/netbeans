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
package org.netbeans.modules.cnd.modelimpl.uid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes.BuiltInUID;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation.InstantiationSelfUID;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl.FileNameSortedKey;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentDeclarations.OffsetSortedKey;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentMacros.NameSortedKey;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentReferences.ReferenceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.SystemMacroImpl.BuiltInMacroUID;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ForwardClassUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ClassifierUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.DeclarationUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.FileUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.IncludeUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.InheritanceUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.InstantiationUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.MacroUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.NamespaceUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.ProjectUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.TypedefUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnnamedClassifierUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnnamedOffsetableDeclarationUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedClassUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedFileUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.UnresolvedNamespaceUID;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;

/**
 *
 */
public class UIDObjectFactory extends AbstractObjectFactory {

    private static UIDObjectFactory theFactory;

    /** Creates a new instance of UIDObjectFactory */
    protected UIDObjectFactory() {
    }

    public static UIDObjectFactory getDefaultFactory() {
        UIDObjectFactory out = theFactory;
        if (out == null) {
            out = theFactory;
            synchronized (UIDObjectFactory.class) {
                out = theFactory;
                if (out == null) {
                    theFactory = out = new UIDObjectFactory();
                }
            }
        }
        return theFactory;
    }

    public void writeUID(CsmUID<?> anUID, RepositoryDataOutput aStream) throws IOException {
        if (anUID instanceof SelfPersistent) {
            super.writeSelfPersistent((SelfPersistent) anUID, aStream);
        } else if (anUID != null){
            CndUtils.assertUnconditional(anUID + ", " +  anUID.getObject());
            super.writeSelfPersistent(null, aStream);
        } else {
            //CndUtils.assertUnconditional("NULL UID");
            super.writeSelfPersistent(null, aStream);
        }
    }

    @SuppressWarnings("unchecked") // okay
    public <T> CsmUID<T> readUID(RepositoryDataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out == null || out instanceof CsmUID<?>;
        return (CsmUID<T>) out;
    }

    public <T> void writeUIDCollection(Collection<CsmUID<T>> aCollection, RepositoryDataOutput aStream, boolean sync) throws IOException {
        assert aStream != null;
        if (aCollection == null) {
            aStream.writeInt(NULL_POINTER);
        } else {
            aCollection = sync ? copySyncCollection(aCollection) : aCollection;
            int collSize = aCollection.size();
            aStream.writeInt(collSize);

            for (CsmUID<T> uid : aCollection) {
                assert uid != null;
                writeUID(uid, aStream);
            }
        }
    }

    public <A, T extends Collection<CsmUID<A>>> T readUIDCollection(T aCollection, RepositoryDataInput aStream) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        int collSize = aStream.readInt();
        return readUIDCollection(aCollection, aStream, collSize);
    }

    public <A, T extends Collection<CsmUID<A>>> T readUIDCollection(T aCollection, RepositoryDataInput aStream, int collSize) throws IOException {
        if (collSize == NULL_POINTER) {
            return null;
        } else {
            for (int i = 0; i < collSize; ++i) {
                CsmUID<A> anUID = readUID(aStream);
                assert anUID != null;
                aCollection.add(anUID);
            }
            return aCollection;
        }
    }


    public <T> void writeStringToUIDMap(Map<CharSequence, CsmUID<T>> aMap, RepositoryDataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<CharSequence, CsmUID<T>> anEntry : aMap.entrySet()) {
            CharSequence key = anEntry.getKey();
            assert key != null;
            PersistentUtils.writeUTF(key, aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public <T> void writeStringToUIDMapSet(Map<CharSequence, Set<CsmUID<T>>> aMap, RepositoryDataOutput aStream) throws IOException {
        assert aMap != null;
        assert aStream != null;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<CharSequence, Set<CsmUID<T>>> anEntry : aMap.entrySet()) {
            CharSequence key = anEntry.getKey();
            assert key != null;
            PersistentUtils.writeUTF(key, aStream);
            collSize = anEntry.getValue().size();
            aStream.writeInt(collSize);
            for(CsmUID<T> anUID : anEntry.getValue()) {
                assert anUID != null;
                writeUID(anUID, aStream);
            }
        }
    }

    public <T> void writeOffsetSortedToUIDMap(Map<OffsetSortedKey, CsmUID<T>> aMap, RepositoryDataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<OffsetSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public <T> void writeNameSortedToUIDMap(Map<NameSortedKey, CsmUID<T>> aMap, RepositoryDataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<NameSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public <T> void writeNameSortedToUIDMap2(Map<NamespaceImpl.FileNameSortedKey, CsmUID<T>> aMap, RepositoryDataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<NamespaceImpl.FileNameSortedKey, CsmUID<T>> anEntry : aMap.entrySet()) {
            anEntry.getKey().write(aStream);
            CsmUID<T> anUID = anEntry.getValue();
            assert anUID != null;
            writeUID(anUID, aStream);
        }
    }

    public void writeStringToArrayUIDMap(Map<CharSequence, Object> aMap, RepositoryDataOutput aStream, boolean sync) throws IOException {
        assert aMap != null;
        assert aStream != null;
        aMap = sync ? copySyncMap(aMap) : aMap;
        int collSize = aMap.size();
        aStream.writeInt(collSize);

        for (Map.Entry<CharSequence, Object> anEntry : aMap.entrySet()) {
            CharSequence key = anEntry.getKey();
            assert key != null;
            PersistentUtils.writeUTF(key, aStream);
            Object o = anEntry.getValue();
            if (o instanceof CsmUID<?>) {
                aStream.writeInt(1);
                writeUID((CsmUID<?>) o, aStream);
            } else {
                CsmUID<?>[] arr = (CsmUID<?>[]) o;
                aStream.writeInt(arr.length);
                for (CsmUID<?> uid : arr) {
                    assert uid != null;
                    writeUID(uid, aStream);
                }
            }
        }
    }

    private static <T> Collection<CsmUID<T>> copySyncCollection(Collection<CsmUID<T>> col) {
        Collection<CsmUID<T>> out;
        synchronized (col) {
            out = new ArrayList<>(col);
        }
        return out;
    }

    private static <K, V> Map<K, V> copySyncMap(Map<K, V> map) {
        Map<K, V> out;
        synchronized (map) {
            out = new HashMap<>(map);
        }
        return out;
    }

    public <T> void readStringToUIDMap(Map<CharSequence, CsmUID<T>> aMap, RepositoryDataInput aStream, APTStringManager manager, int collSize) throws IOException {
        for (int i = 0; i < collSize; ++i) {
            CharSequence key = PersistentUtils.readUTF(aStream, manager);
            assert key != null;
            CsmUID<T> uid = readUID(aStream);
            assert uid != null;
            aMap.put(key, uid);
        }
    }

    public <T> void readStringToUIDMapSet(Map<CharSequence, Set<CsmUID<T>>> aMap, RepositoryDataInput aStream, APTStringManager manager, int collSize) throws IOException {
        for (int i = 0; i < collSize; ++i) {
            CharSequence key = PersistentUtils.readUTF(aStream, manager);
            assert key != null;
            int aSize = aStream.readInt();
            Set<CsmUID<T>> set = new HashSet<>(aSize);
            for(int j = 0; j < aSize; j++) {
                CsmUID<T> uid = readUID(aStream);
                assert uid != null;
                set.add(uid);
            }
            aMap.put(key, set);
        }
    }

    public TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> readOffsetSortedToUIDMap(RepositoryDataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperDeclarationsSortedMap helper = new HelperDeclarationsSortedMap(this, aStream, manager);
        return new TreeMap<>(helper);
    }

    public TreeMap<NameSortedKey, CsmUID<CsmMacro>> readNameSortedToUIDMap(RepositoryDataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperMacrosSortedMap helper = new HelperMacrosSortedMap(this, aStream, manager);
        return new TreeMap<>(helper);
    }

    public TreeMap<ReferenceImpl, CsmUID<CsmObject>> readReferencesSortedToUIDMap(RepositoryDataInput aStream, CsmUID<CsmFile> fileUID) throws IOException {
        assert aStream != null;
        HelperReferencesSortedMap helper = new HelperReferencesSortedMap(this, aStream, fileUID);
        return new TreeMap<>(helper);
    }


    public TreeMap<NamespaceImpl.FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> readNameSortedToUIDMap2(RepositoryDataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperNamespaceDefinitionSortedMap helper = new HelperNamespaceDefinitionSortedMap(this, aStream, manager);
        return new TreeMap<>(helper);
    }

    public TreeMap<CharSequence, Object> readStringToArrayUIDMap(RepositoryDataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperCharSequencesSortedMap helper = new HelperCharSequencesSortedMap(this, aStream, manager);
        return new TreeMap<>(helper);
    }

    public TreeMap<CharSequence,CsmUID<CsmNamespaceDefinition>> readStringToUIDMap(RepositoryDataInput aStream, APTStringManager manager) throws IOException {
        assert aStream != null;
        HelperCharSequencesSortedMap2 helper = new HelperCharSequencesSortedMap2(this, aStream, manager);
        return new TreeMap<>(helper);
    }

    @Override
    protected short getHandler(Object object) {
        short aHandler;

        if (object instanceof ProjectUID) {
            aHandler = UID_PROJECT_UID;
        } else if (object instanceof NamespaceUID) {
            aHandler = UID_NAMESPACE_UID;
        } else if (object instanceof FileUID) {
            aHandler = UID_FILE_UID;
        } else if (object instanceof TypedefUID) {
            aHandler = UID_TYPEDEF_UID;
        } else if (object instanceof ClassifierUID<?>) {
            aHandler = UID_CLASSIFIER_UID;
        } else if (object instanceof ForwardClassUID<?>) {
            aHandler = UID_FORWARD_CLASS_UID;
        } else if (object instanceof UnnamedClassifierUID<?>) {
            aHandler = UID_UNNAMED_CLASSIFIER_UID;
        } else if (object instanceof MacroUID) {
            aHandler = UID_MACRO_UID;
        } else if (object instanceof BuiltInMacroUID) {
            aHandler = UID_BUILT_IN_MACRO_UID;
        } else if (object instanceof IncludeUID) {
            aHandler = UID_INCLUDE_UID;
        } else if (object instanceof UIDUtilities.InheritanceUID) {
            aHandler = UID_INHERITANCE_UID;
        } else if (object instanceof UnnamedOffsetableDeclarationUID<?>) {
            aHandler = UID_UNNAMED_OFFSETABLE_DECLARATION_UID;
        } else if (object instanceof DeclarationUID<?>) {
            aHandler = UID_DECLARATION_UID;
        } else if (object instanceof BuiltInUID) {
            aHandler = UID_BUILT_IN_UID;
        } else if (object instanceof InstantiationSelfUID) {
            aHandler = UID_INSTANTIATION_SELF_UID;
        } else if (object instanceof InstantiationUID) {
            aHandler = UID_INSTANTIATION_UID;
        } else if (object instanceof UnresolvedClassUID) {
            aHandler = UID_UNRESOLVED_CLASS;
        } else if (object instanceof UnresolvedFileUID) {
            aHandler = UID_UNRESOLVED_FILE;
        } else if (object instanceof UnresolvedNamespaceUID) {
            aHandler = UID_UNRESOLVED_NAMESPACE;
        } else {
            throw new IllegalArgumentException("The UID is an instance of unknow class"); //NOI18N
        }

        return aHandler;
    }

    @Override
    protected SelfPersistent createObject(short handler, RepositoryDataInput aStream) throws IOException {

        SelfPersistent anUID;
        boolean share = false;
        switch (handler) {
            case UID_PROJECT_UID:
                share = true;
                anUID = new ProjectUID(aStream);
                break;

            case UID_NAMESPACE_UID:
                share = true;
                anUID = new NamespaceUID(aStream);
                break;

            case UID_FILE_UID:
                share = true;
                anUID = new FileUID(aStream);
                break;

            case UID_TYPEDEF_UID:
                anUID = new TypedefUID(aStream);
                break;

            case UID_CLASSIFIER_UID:
                anUID = new ClassifierUID<>(aStream);
                break;

            case UID_FORWARD_CLASS_UID:
                anUID = new ForwardClassUID<>(aStream);
                break;

            case UID_UNNAMED_CLASSIFIER_UID:
                anUID = new UnnamedClassifierUID<>(aStream);
                break;

            case UID_MACRO_UID:
                share = true;
                anUID = new MacroUID(aStream);
                break;

            case UID_BUILT_IN_MACRO_UID:
                share = true;
                anUID = new BuiltInMacroUID(aStream);
                break;

            case UID_INCLUDE_UID:
                share = true;
                anUID = new IncludeUID(aStream);
                break;

            case UID_INHERITANCE_UID:
                share = true;
                anUID = new InheritanceUID(aStream);
                break;

            // no reason to cache declaration and more detailed uids.

            case UID_UNNAMED_OFFSETABLE_DECLARATION_UID:
                anUID = new UnnamedOffsetableDeclarationUID<>(aStream);
                break;

            case UID_DECLARATION_UID:
                anUID = new DeclarationUID<>(aStream);
                break;

            case UID_BUILT_IN_UID:
                anUID = (SelfPersistent) BuiltinTypes.readUID(aStream);
                share = false;
                break;

            case UID_INSTANTIATION_SELF_UID:
                anUID = new Instantiation.InstantiationSelfUID(aStream);
                share = false;
                break;

            case UID_INSTANTIATION_UID:
                anUID = new InstantiationUID(aStream);
                share = false;
                break;

            case UID_UNRESOLVED_CLASS:
                anUID = new UIDUtilities.UnresolvedClassUID(aStream);
                break;

            case UID_UNRESOLVED_FILE:
                anUID = new UIDUtilities.UnresolvedFileUID(aStream);
                break;

            case UID_UNRESOLVED_NAMESPACE:
                anUID = new UIDUtilities.UnresolvedNamespaceUID(aStream);
                break;
            default:
                throw new IllegalArgumentException("The UID is an instance of unknown class: " + handler); //NOI18N
        }
        if (share) {
            assert anUID != null;
            assert anUID instanceof CsmUID<?>;
            CsmUID<?> shared = UIDManager.instance().getSharedUID((CsmUID<?>) anUID);
            assert shared != null;
            assert shared instanceof SelfPersistent;
            anUID = (SelfPersistent) shared;
        }
        return anUID;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    //  constants which defines the handle of an UID in the stream
    private static final short FIRST_INDEX = KeyObjectFactory.LAST_INDEX + 1;
    private static final short UID_PROJECT_UID = FIRST_INDEX;
    private static final short UID_NAMESPACE_UID = UID_PROJECT_UID + 1;
    private static final short UID_FILE_UID = UID_NAMESPACE_UID + 1;
    private static final short UID_TYPEDEF_UID = UID_FILE_UID + 1;
    private static final short UID_CLASSIFIER_UID = UID_TYPEDEF_UID + 1;
    private static final short UID_FORWARD_CLASS_UID = UID_CLASSIFIER_UID + 1;
    private static final short UID_UNNAMED_CLASSIFIER_UID = UID_FORWARD_CLASS_UID + 1;
    private static final short UID_MACRO_UID = UID_UNNAMED_CLASSIFIER_UID + 1;
    private static final short UID_BUILT_IN_MACRO_UID = UID_MACRO_UID + 1;
    private static final short UID_INCLUDE_UID = UID_BUILT_IN_MACRO_UID + 1;
    private static final short UID_INHERITANCE_UID = UID_INCLUDE_UID + 1;
    private static final short UID_UNNAMED_OFFSETABLE_DECLARATION_UID = UID_INHERITANCE_UID + 1;
    private static final short UID_DECLARATION_UID = UID_UNNAMED_OFFSETABLE_DECLARATION_UID + 1;
    private static final short UID_BUILT_IN_UID = UID_DECLARATION_UID + 1;
    private static final short UID_INSTANTIATION_UID = UID_BUILT_IN_UID + 1;
    private static final short UID_INSTANTIATION_SELF_UID = UID_INSTANTIATION_UID + 1;
    private static final short UID_UNRESOLVED_CLASS = UID_INSTANTIATION_SELF_UID + 1;
    private static final short UID_UNRESOLVED_FILE = UID_UNRESOLVED_CLASS + 1;
    private static final short UID_UNRESOLVED_NAMESPACE = UID_UNRESOLVED_FILE + 1;
    // index to be used in another factory (but only in one)
    // to start own indeces from the next after LAST_INDEX
    public static final short LAST_INDEX = UID_UNRESOLVED_NAMESPACE;

    private static final Comparator<OffsetSortedKey> OSKComparator = new Comparator<OffsetSortedKey>() {
        @Override
       public int compare(OffsetSortedKey o1, OffsetSortedKey o2) {
            return o1.compareTo(o2);
        }
    };

    private static final class HelperDeclarationsSortedMap implements SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> {
        private final RepositoryDataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperDeclarationsSortedMap(UIDObjectFactory factory, RepositoryDataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        @Override
        public Comparator<? super OffsetSortedKey> comparator() {
            return OSKComparator;
        }
        @Override
        public SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> subMap(OffsetSortedKey fromKey, OffsetSortedKey toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> headMap(OffsetSortedKey toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> tailMap(OffsetSortedKey fromKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public OffsetSortedKey firstKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public OffsetSortedKey lastKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return size;
        }
        @Override
        public boolean isEmpty() {
            return size == 0;
        }
        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmOffsetableDeclaration> get(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmOffsetableDeclaration> put(OffsetSortedKey key, CsmUID<CsmOffsetableDeclaration> value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmOffsetableDeclaration> remove(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void putAll(Map<? extends OffsetSortedKey, ? extends CsmUID<CsmOffsetableDeclaration>> t) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<OffsetSortedKey> keySet() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Collection<CsmUID<CsmOffsetableDeclaration>> values() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>> entrySet() {
            return new Set<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>>(){
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return size == 0;
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterator<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>> iterator() {
                    return new Iterator<Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>>(){
                        private int current = 0;
                        @Override
                        public boolean hasNext() {
                            return current < size;
                        }
                        @Override
                        public Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final OffsetSortedKey key = new OffsetSortedKey(aStream);
                                    assert key != null;
                                    final CsmUID<CsmOffsetableDeclaration> uid = factory.<CsmOffsetableDeclaration>readUID(aStream);
                                    assert uid != null;
                                    return new Map.Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>(){
                                        @Override
                                        public OffsetSortedKey getKey() {
                                            return key;
                                        }
                                        @Override
                                        public CsmUID<CsmOffsetableDeclaration> getValue() {
                                            return uid;
                                        }
                                        @Override
                                        public CsmUID<CsmOffsetableDeclaration> setValue(CsmUID<CsmOffsetableDeclaration> value) {
                                            throw new UnsupportedOperationException();
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            throw new NoSuchElementException();
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean add(Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean addAll(Collection<? extends Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static final Comparator<NameSortedKey> NSKComparator = new Comparator<NameSortedKey>() {
        @Override
       public int compare(NameSortedKey o1, NameSortedKey o2) {
            return o1.compareTo(o2);
        }
    };

    private static final class HelperMacrosSortedMap implements SortedMap<NameSortedKey, CsmUID<CsmMacro>> {
        private final RepositoryDataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperMacrosSortedMap(UIDObjectFactory factory, RepositoryDataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        @Override
        public Comparator<? super NameSortedKey> comparator() {
            return NSKComparator;
        }
        @Override
        public SortedMap<NameSortedKey, CsmUID<CsmMacro>> subMap(NameSortedKey fromKey, NameSortedKey toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<NameSortedKey, CsmUID<CsmMacro>> headMap(NameSortedKey toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<NameSortedKey, CsmUID<CsmMacro>> tailMap(NameSortedKey fromKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public NameSortedKey firstKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public NameSortedKey lastKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return size;
        }
        @Override
        public boolean isEmpty() {
            return size > 0;
        }
        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmMacro> get(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmMacro> put(NameSortedKey key, CsmUID<CsmMacro> value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmMacro> remove(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void putAll(Map<? extends NameSortedKey, ? extends CsmUID<CsmMacro>> t) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<NameSortedKey> keySet() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Collection<CsmUID<CsmMacro>> values() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<Entry<NameSortedKey, CsmUID<CsmMacro>>> entrySet() {
            return new Set<Entry<NameSortedKey, CsmUID<CsmMacro>>>(){
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return size > 0;
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterator<Entry<NameSortedKey, CsmUID<CsmMacro>>> iterator() {
                    return new Iterator<Entry<NameSortedKey, CsmUID<CsmMacro>>>(){
                        private int current = 0;
                        @Override
                        public boolean hasNext() {
                            return current < size;
                        }
                        @Override
                        public Entry<NameSortedKey, CsmUID<CsmMacro>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final NameSortedKey key = new NameSortedKey(aStream);
                                    assert key != null;
                                    final CsmUID<CsmMacro> uid = factory.<CsmMacro>readUID(aStream);
                                    assert uid != null;
                                    return new Map.Entry<NameSortedKey, CsmUID<CsmMacro>>(){
                                        @Override
                                        public NameSortedKey getKey() {
                                            return key;
                                        }
                                        @Override
                                        public CsmUID<CsmMacro> getValue() {
                                            return uid;
                                        }
                                        @Override
                                        public CsmUID<CsmMacro> setValue(CsmUID<CsmMacro> value) {
                                            throw new UnsupportedOperationException();
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            throw new NoSuchElementException();
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean add(Entry<NameSortedKey, CsmUID<CsmMacro>> o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean addAll(Collection<? extends Entry<NameSortedKey, CsmUID<CsmMacro>>> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static final class HelperNamespaceDefinitionSortedMap implements SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> {
        private final RepositoryDataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperNamespaceDefinitionSortedMap(UIDObjectFactory factory, RepositoryDataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        @Override
        public Comparator<? super FileNameSortedKey> comparator() {
            return NamespaceImpl.defenitionComparator;
        }
        @Override
        public SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> subMap(FileNameSortedKey fromKey, FileNameSortedKey toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> headMap(FileNameSortedKey toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> tailMap(FileNameSortedKey fromKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public FileNameSortedKey firstKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public FileNameSortedKey lastKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return size;
        }
        @Override
        public boolean isEmpty() {
            return size == 0;
        }
        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmNamespaceDefinition> get(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmNamespaceDefinition> put(FileNameSortedKey key, CsmUID<CsmNamespaceDefinition> value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmNamespaceDefinition> remove(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void putAll(Map<? extends FileNameSortedKey, ? extends CsmUID<CsmNamespaceDefinition>> t) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<FileNameSortedKey> keySet() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Collection<CsmUID<CsmNamespaceDefinition>> values() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>> entrySet() {
            return new Set<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>>(){
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return size == 0;
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterator<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>> iterator() {
                    return new Iterator<Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>>(){
                        int current = 0;
                        @Override
                        public boolean hasNext() {
                            return current < size;
                        }
                        @Override
                        public Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final FileNameSortedKey key = new FileNameSortedKey(aStream);
                                    final CsmUID<CsmNamespaceDefinition> value = factory.readUID(aStream);
                                    assert value != null;
                                    return new Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>(){
                                        @Override
                                        public FileNameSortedKey getKey() {
                                            return key;
                                        }
                                        @Override
                                        public CsmUID<CsmNamespaceDefinition> getValue() {
                                            return value;
                                        }
                                        @Override
                                        public CsmUID<CsmNamespaceDefinition> setValue(CsmUID<CsmNamespaceDefinition> value) {
                                            throw new UnsupportedOperationException();
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            throw new NoSuchElementException();
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean add(Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean addAll(Collection<? extends Entry<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static final class HelperCharSequencesSortedMap implements SortedMap<CharSequence, Object> {
        private final RepositoryDataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperCharSequencesSortedMap(UIDObjectFactory factory, RepositoryDataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        @Override
        public Comparator<? super CharSequence> comparator() {
            return CharSequences.comparator();
        }
        @Override
        public SortedMap<CharSequence, Object> subMap(CharSequence fromKey, CharSequence toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<CharSequence, Object> headMap(CharSequence toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<CharSequence, Object> tailMap(CharSequence fromKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CharSequence firstKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public CharSequence lastKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return size;
        }
        @Override
        public boolean isEmpty() {
            return size == 0;
        }
        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Object get(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Object put(CharSequence key, Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void putAll(Map<? extends CharSequence, ? extends Object> t) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<CharSequence> keySet() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Collection<Object> values() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<Entry<CharSequence, Object>> entrySet() {
            return new Set<Entry<CharSequence, Object>>(){
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return size == 0;
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterator<Entry<CharSequence, Object>> iterator() {
                    return new Iterator<Entry<CharSequence, Object>>(){
                        private int current = 0;
                        @Override
                        public boolean hasNext() {
                            return current < size;
                        }
                        @Override
                        public Entry<CharSequence, Object> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final CharSequence key = PersistentUtils.readUTF(aStream, manager);
                                    assert key != null;
                                    int arrSize = aStream.readInt();
                                    final Object value;
                                    if (arrSize == 1) {
                                        value = factory.readUID(aStream);
                                        assert value != null;
                                    } else {
                                        CsmUID<?>[] uids = new CsmUID<?>[arrSize];
                                        for (int k = 0; k < arrSize; k++) {
                                            CsmUID<?> uid = factory.readUID(aStream);
                                            assert uid != null;
                                            uids[k] = uid;
                                        }
                                        value = uids;
                                    }
                                    return new Map.Entry<CharSequence, Object>(){
                                        @Override
                                        public CharSequence getKey() {
                                            return key;
                                        }
                                        @Override
                                        public Object getValue() {
                                            return value;
                                        }
                                        @Override
                                        public Object setValue(Object value) {
                                            throw new UnsupportedOperationException();
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            throw new NoSuchElementException();
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean add(Entry<CharSequence, Object> o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean addAll(Collection<? extends Entry<CharSequence, Object>> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static final class HelperCharSequencesSortedMap2 implements SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> {
        private final RepositoryDataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final APTStringManager manager;

        private HelperCharSequencesSortedMap2(UIDObjectFactory factory, RepositoryDataInput aStream, APTStringManager manager) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.manager = manager;
        }
        @Override
        public Comparator<? super CharSequence> comparator() {
            return CharSequences.comparator();
        }
        @Override
        public SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> subMap(CharSequence fromKey, CharSequence toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> headMap(CharSequence toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<CharSequence, CsmUID<CsmNamespaceDefinition>> tailMap(CharSequence fromKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CharSequence firstKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public CharSequence lastKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return size;
        }
        @Override
        public boolean isEmpty() {
            return size == 0;
        }
        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmNamespaceDefinition> get(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmNamespaceDefinition> put(CharSequence key, CsmUID<CsmNamespaceDefinition> value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmNamespaceDefinition> remove(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void putAll(Map<? extends CharSequence, ? extends CsmUID<CsmNamespaceDefinition>> t) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<CharSequence> keySet() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Collection<CsmUID<CsmNamespaceDefinition>> values() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>> entrySet() {
            return new Set<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>>(){
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return size == 0;
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterator<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>> iterator() {
                    return new Iterator<Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>>(){
                        private int current = 0;
                        @Override
                        public boolean hasNext() {
                            return current < size;
                        }
                        @Override
                        public Entry<CharSequence, CsmUID<CsmNamespaceDefinition>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final CharSequence key = PersistentUtils.readUTF(aStream, manager);
                                    assert key != null;
                                    final CsmUID<CsmNamespaceDefinition> uid = factory.readUID(aStream);
                                    assert uid != null;
                                    return new Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>(){
                                        @Override
                                        public CharSequence getKey() {
                                            return key;
                                        }
                                        @Override
                                        public CsmUID<CsmNamespaceDefinition> getValue() {
                                            return uid;
                                        }
                                        @Override
                                        public CsmUID<CsmNamespaceDefinition> setValue(CsmUID<CsmNamespaceDefinition> value) {
                                            throw new UnsupportedOperationException();
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            throw new NoSuchElementException();
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean add(Entry<CharSequence, CsmUID<CsmNamespaceDefinition>> o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean addAll(Collection<? extends Entry<CharSequence, CsmUID<CsmNamespaceDefinition>>> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static final Comparator<ReferenceImpl> ReferenceComparator = new Comparator<ReferenceImpl>() {
        @Override
       public int compare(ReferenceImpl o1, ReferenceImpl o2) {
            return o1.compareTo(o2);
        }
    };

    private static final class HelperReferencesSortedMap implements SortedMap<ReferenceImpl, CsmUID<CsmObject>> {
        private final RepositoryDataInput aStream;
        private final int size;
        private final UIDObjectFactory factory;
        private final CsmUID<CsmFile> fileUID;

        private HelperReferencesSortedMap(UIDObjectFactory factory, RepositoryDataInput aStream, CsmUID<CsmFile> fileUID) throws IOException {
            size = aStream.readInt();
            this.aStream = aStream;
            this.factory = factory;
            this.fileUID = fileUID;
        }
        @Override
        public Comparator<? super ReferenceImpl> comparator() {
            return ReferenceComparator;
        }
        @Override
        public SortedMap<ReferenceImpl, CsmUID<CsmObject>> subMap(ReferenceImpl fromKey, ReferenceImpl toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<ReferenceImpl, CsmUID<CsmObject>> headMap(ReferenceImpl toKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public SortedMap<ReferenceImpl, CsmUID<CsmObject>> tailMap(ReferenceImpl fromKey) {
            throw new UnsupportedOperationException();
        }
        @Override
        public ReferenceImpl firstKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public ReferenceImpl lastKey() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return size;
        }
        @Override
        public boolean isEmpty() {
            return size > 0;
        }
        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmObject> get(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmObject> put(ReferenceImpl key, CsmUID<CsmObject> value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmUID<CsmObject> remove(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void putAll(Map<? extends ReferenceImpl, ? extends CsmUID<CsmObject>> t) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<ReferenceImpl> keySet() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Collection<CsmUID<CsmObject>> values() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<Entry<ReferenceImpl, CsmUID<CsmObject>>> entrySet() {
            return new Set<Entry<ReferenceImpl, CsmUID<CsmObject>>>(){
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return size > 0;
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterator<Entry<ReferenceImpl, CsmUID<CsmObject>>> iterator() {
                    return new Iterator<Entry<ReferenceImpl, CsmUID<CsmObject>>>(){
                        private int current = 0;
                        @Override
                        public boolean hasNext() {
                            return current < size;
                        }
                        @Override
                        public Entry<ReferenceImpl, CsmUID<CsmObject>> next() {
                            if (current < size) {
                                current++;
                                try {
                                    final CsmUID<CsmObject> uid = factory.<CsmObject>readUID(aStream);
                                    assert uid != null;
                                    final ReferenceImpl key = new ReferenceImpl(fileUID, uid, factory, aStream);
                                    assert key != null;
                                    return new Map.Entry<ReferenceImpl, CsmUID<CsmObject>>(){
                                        @Override
                                        public ReferenceImpl getKey() {
                                            return key;
                                        }
                                        @Override
                                        public CsmUID<CsmObject> getValue() {
                                            return uid;
                                        }
                                        @Override
                                        public CsmUID<CsmObject> setValue(CsmUID<CsmObject> value) {
                                            throw new UnsupportedOperationException();
                                        }
                                    };
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            throw new NoSuchElementException();
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean add(Entry<ReferenceImpl, CsmUID<CsmObject>> o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean addAll(Collection<? extends Entry<ReferenceImpl, CsmUID<CsmObject>>> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
