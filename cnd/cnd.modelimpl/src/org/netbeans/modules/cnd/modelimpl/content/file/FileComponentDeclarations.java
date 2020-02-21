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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.repository.FileDeclarationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;

/**
 *
 */
public class FileComponentDeclarations extends FileComponent {

    private final TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> declarations;
    private WeakReference<Map<CsmDeclaration.Kind,SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>>> sortedDeclarations;
    private final ReadWriteLock declarationsLock = new ReentrantReadWriteLock();
    /**
     * Stores the UIDs of the static functions declarations (not definitions)
     * This is necessary for finding definitions/declarations
     * since file-level static functions (i.e. c-style static functions) aren't registered in project
     */
    private final Collection<CsmUID<CsmFunction>> staticFunctionDeclarationUIDs;
    private final Collection<CsmUID<CsmVariable>> staticVariableUIDs;
    private final ReadWriteLock staticLock = new ReentrantReadWriteLock();

    // empty stub
    private static final FileComponentDeclarations EMPTY = new FileComponentDeclarations() {

        @Override
        CsmUID<CsmOffsetableDeclaration> addDeclaration(CsmOffsetableDeclaration decl) {
            return null;
        }

        @Override
        void put() {
        }
    };

    public static FileComponentDeclarations empty() {
        return EMPTY;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("UL")
    FileComponentDeclarations(FileComponentDeclarations other, boolean empty) {
        super(other);
        try {
            if (!empty) {
                other.declarationsLock.readLock().lock();
            }
            declarations = new TreeMap<>(
                    empty ? Collections.<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>emptyMap() : other.declarations);
        } finally {
            if (!empty) {
                other.declarationsLock.readLock().unlock();
            }
        }
        try {
            if (!empty) {
                other.staticLock.readLock().lock();
            }
            staticFunctionDeclarationUIDs = new ArrayList<>(
                    empty ? Collections.<CsmUID<CsmFunction>>emptyList() : other.staticFunctionDeclarationUIDs);
            staticVariableUIDs = new ArrayList<>(
                    empty ? Collections.<CsmUID<CsmVariable>>emptyList() : other.staticVariableUIDs);
        } finally {
            if (!empty) {
                other.staticLock.readLock().unlock();
            }
        }
    }

    public FileComponentDeclarations(FileImpl file) {
        super(new FileDeclarationsKey(file));
        declarations = new TreeMap<>();
        staticFunctionDeclarationUIDs = new ArrayList<>(0);
        staticVariableUIDs = new ArrayList<>(0);
    }

    public FileComponentDeclarations(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.declarations = factory.readOffsetSortedToUIDMap(input, null);
        int collSize = input.readInt();
        if (collSize <= 0) {
            staticFunctionDeclarationUIDs = new ArrayList<>(0);
        } else {
            staticFunctionDeclarationUIDs = new ArrayList<>(collSize);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(staticFunctionDeclarationUIDs, input, collSize);
        collSize = input.readInt();
        if (collSize <= 0) {
            staticVariableUIDs = new ArrayList<>(0);
        } else {
            staticVariableUIDs = new ArrayList<>(collSize);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(staticVariableUIDs, input, collSize);
    }

    // only for EMPTY static field
    private FileComponentDeclarations() {
        super((org.netbeans.modules.cnd.repository.spi.Key)null);
        declarations = new TreeMap<>();
        staticFunctionDeclarationUIDs = new ArrayList<>(0);
        staticVariableUIDs = new ArrayList<>(0);
    }

    Collection<CsmUID<CsmOffsetableDeclaration>> clean() {
        Collection<CsmUID<CsmOffsetableDeclaration>> uids;
        try {
            declarationsLock.writeLock().lock();
            uids = new ArrayList<>(declarations.values());
            sortedDeclarations = null;
            declarations.clear();
        } finally {
            declarationsLock.writeLock().unlock();
        }
        try {
            staticLock.writeLock().lock();
            staticFunctionDeclarationUIDs.clear();
            staticVariableUIDs.clear();
        } finally {
            staticLock.writeLock().unlock();
        }
        // PUT should be done by FileContent
//        put();
        return uids;
    }

    public boolean hasDeclarations() {
        return declarations.size() != 0;
    }

    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        Collection<CsmOffsetableDeclaration> decls;
        try {
            declarationsLock.readLock().lock();
            Collection<CsmUID<CsmOffsetableDeclaration>> uids = declarations.values();
            decls = UIDCsmConverter.UIDsToDeclarations(uids);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return decls;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        // can be called only from FileImpl.getDeclarations(fileter)
        Iterator<CsmOffsetableDeclaration> out;
        try {
            declarationsLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(declarations.values(), filter);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return out;
    }

    public int getDeclarationsSize(){
        try {
            declarationsLock.readLock().lock();
            return declarations.size();
        } finally {
            declarationsLock.readLock().unlock();
        }
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(CsmDeclaration.Kind[] kinds, CharSequence prefix) {
        Collection<CsmUID<CsmOffsetableDeclaration>> out = null;
        try {
            declarationsLock.readLock().lock();
            Map<CsmDeclaration.Kind, SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>> map = null;
            if (sortedDeclarations != null) {
                map = sortedDeclarations.get();
            }
            if (map == null) {
                map = new EnumMap<>(CsmDeclaration.Kind.class);
                for(CsmUID<CsmOffsetableDeclaration> anUid : declarations.values()){
                    CsmDeclaration.Kind kind = UIDUtilities.getKind(anUid);
                    SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>> val = map.get(kind);
                    if (val == null){
                        val = new TreeMap<>();
                        map.put(kind, val);
                    }
                    val.put(new NameKey(anUid), anUid);
                }
                sortedDeclarations = new WeakReference<>(map);
            }
            out = new ArrayList<>();
            for(CsmDeclaration.Kind kind : kinds) {
                 SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>> val = map.get(kind);
                 if (val != null) {
                     if (prefix == null) {
                         out.addAll(val.values());
                     } else {
                         NameKey fromKey = new NameKey(prefix, 0);
                         NameKey toKey = new NameKey(prefix, Integer.MAX_VALUE);
                         out.addAll(val.subMap(fromKey, toKey).values());
                     }
                 }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return out;
    }

    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, int endOffset, CharSequence name) {
        OffsetSortedKey key = new OffsetSortedKey(startOffset, CharSequences.create(name).hashCode());
        CsmUID<CsmOffsetableDeclaration> anUid = null;
        try {
            declarationsLock.readLock().lock();
            anUid = declarations.get(key);
            // It seems next line wrong, so commented when method was moved from FileImpl
            //sortedDeclarations = null;
        } finally {
            declarationsLock.readLock().unlock();
        }
        if (anUid != null && UIDUtilities.getEndOffset(anUid) != endOffset) {
            anUid = null;
        }
        return UIDCsmConverter.UIDtoDeclaration(anUid);
    }

    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, CharSequence name, CsmDeclaration.Kind kind) {
        OffsetSortedKey key = new OffsetSortedKey(startOffset, CharSequences.create(name).hashCode());
        CsmUID<CsmOffsetableDeclaration> anUid = null;
        try {
            declarationsLock.readLock().lock();
            anUid = declarations.get(key);
            // It seems next line wrong, so commented when method was moved from FileImpl
            //sortedDeclarations = null;
        } finally {
            declarationsLock.readLock().unlock();
        }
        if (anUid != null && UIDUtilities.getKind(anUid) != kind) {
            anUid = null;
        }
        return UIDCsmConverter.UIDtoDeclaration(anUid);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(int startOffset, int endOffset) {
        List<CsmUID<CsmOffsetableDeclaration>> res;
        try {
            declarationsLock.readLock().lock();
            res = getDeclarationsByOffset(startOffset-1);
            if (startOffset < endOffset) {
                OffsetSortedKey fromKey = new OffsetSortedKey(startOffset, Integer.MIN_VALUE);
                OffsetSortedKey toKey = new OffsetSortedKey(endOffset, Integer.MIN_VALUE);
                SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> map = declarations.subMap(fromKey, toKey);
                for(Map.Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> entry : map.entrySet()){
                    CsmUID<CsmOffsetableDeclaration> anUid = entry.getValue();
                    int start = UIDUtilities.getStartOffset(anUid);
                    int end = UIDUtilities.getEndOffset(anUid);
                    if (start >= endOffset && (start != -1 && end != -1)) { // (-1;-1) is a special case for forward classifiers
                        break;
                    }
                    if(end >= startOffset && start < endOffset) {
                        res.add(anUid);
                    }
                }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return res;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(int offset) {
        List<CsmUID<CsmOffsetableDeclaration>> res;
        try {
            declarationsLock.readLock().lock();
            res = getDeclarationsByOffset(offset);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return UIDCsmConverter.UIDsToDeclarations(res).iterator();
    }

    // call under read lock
    private List<CsmUID<CsmOffsetableDeclaration>> getDeclarationsByOffset(int offset){
        List<CsmUID<CsmOffsetableDeclaration>> res = new ArrayList<>();
        OffsetSortedKey key = new OffsetSortedKey(offset+1, Integer.MIN_VALUE);
        outer : while(true) {
            SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> head = declarations.headMap(key);
            if (head.isEmpty()) {
                break;
            }
            OffsetSortedKey last = head.lastKey();
            while(true) {
                if (last == null) {
                    break outer;
                }
                CsmUID<CsmOffsetableDeclaration> aUid = declarations.get(last);
                int from = UIDUtilities.getStartOffset(aUid);
                int to = UIDUtilities.getEndOffset(aUid);
                if (from <= offset && offset <= to) {
                    res.add(0, aUid);
                    key = last;
                } else {
                    SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> headMap = head.headMap(last);
                    if(!headMap.isEmpty()) {
                        OffsetSortedKey higherKey = headMap.lastKey();
                        if(higherKey != null) {
                            CsmUID<CsmOffsetableDeclaration> higher = head.get(higherKey);
                            int higherTo = UIDUtilities.getEndOffset(higher);
                            if(higherTo >= to) {
                                last = higherKey;
                                continue;
                            }
                        }
                    }
                    break outer;
                }
                break;
            }
        }
        return res;
    }

    /**
     * Gets the list of the static functions declarations (not definitions)
     * This is necessary for finding definitions/declarations
     * since file-level static functions (i.e. c-style static functions) aren't registered in project
     */
    public Collection<CsmFunction> getStaticFunctionDeclarations() {
        Collection<CsmFunction> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarations(staticFunctionDeclarationUIDs);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmFunction> getStaticFunctionDeclarations(CsmFilter filter) {
        Iterator<CsmFunction> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(staticFunctionDeclarationUIDs, filter);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }

    public Collection<CsmVariable> getStaticVariableDeclarations() {
        Collection<CsmVariable> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarations(staticVariableUIDs);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmVariable> getStaticVariableDeclarations(CsmFilter filter) {
        Iterator<CsmVariable> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(staticVariableUIDs, filter);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }


    private OffsetSortedKey getOffsetSortKey(CsmOffsetableDeclaration declaration) {
        return new OffsetSortedKey(declaration);
    }

    CsmUID<CsmOffsetableDeclaration> addDeclaration(CsmOffsetableDeclaration decl) {
        CsmUID<CsmOffsetableDeclaration> uidDecl = RepositoryUtils.put(decl);
        try {
            declarationsLock.writeLock().lock();
            declarations.put(getOffsetSortKey(decl), uidDecl);
            sortedDeclarations = null;
        } finally {
            declarationsLock.writeLock().unlock();
        }
        // TODO: remove this dirty hack!
        if (decl instanceof VariableImpl<?>) {
            VariableImpl<?> v = (VariableImpl<?>) decl;
            if (!NamespaceImpl.isNamespaceScope(v, true)) {
                v.setScope(decl.getContainingFile());
                addStaticVariableDeclaration(uidDecl);
            }
        }
        if (CsmKindUtilities.isFunction(decl)) {
            if (decl instanceof FunctionImpl<?>) {
                FunctionImpl<?> fi = (FunctionImpl<?>) decl;
                if (fi.isCStyleStatic()) {
                    if (!NamespaceImpl.isNamespaceScope(fi)) {
                        fi.setScope(decl.getContainingFile());
                        addStaticFunctionDeclaration(uidDecl);
                    }
                }
            }
        }
        // PUT should be done by FileContent
//        put();
        return uidDecl;
    }

    @SuppressWarnings("unchecked")
    private void addStaticFunctionDeclaration(CsmUID<?> uidDecl) {
        try {
            staticLock.writeLock().lock();
            staticFunctionDeclarationUIDs.add((CsmUID<CsmFunction>) uidDecl);
        } finally {
            staticLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void addStaticVariableDeclaration(CsmUID<?> uidDecl) {
        try {
            staticLock.writeLock().lock();
            staticVariableUIDs.add((CsmUID<CsmVariable>) uidDecl);
        } finally {
            staticLock.writeLock().unlock();
        }
    }

    void removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> uidDecl;
        try {
            declarationsLock.writeLock().lock();
            uidDecl = declarations.remove(getOffsetSortKey(declaration));
            sortedDeclarations = null;
        } finally {
            declarationsLock.writeLock().unlock();
        }
        RepositoryUtils.remove(uidDecl, declaration);
        // update repository
        // PUT should be done by FileContent
//        put();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        try {
            declarationsLock.readLock().lock();
            factory.writeOffsetSortedToUIDMap(this.declarations, output, false);
        } finally {
            declarationsLock.readLock().unlock();
        }
        try {
            staticLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeUIDCollection(staticFunctionDeclarationUIDs, output, false);
            UIDObjectFactory.getDefaultFactory().writeUIDCollection(staticVariableUIDs, output, false);
        } finally {
            staticLock.readLock().unlock();
        }
    }

    public static final class OffsetSortedKey implements Comparable<OffsetSortedKey>, Persistent, SelfPersistent {

        private final int start;
        private final int name;

        public OffsetSortedKey(CsmOffsetableDeclaration declaration) {
            start = ((CsmOffsetable) declaration).getStartOffset();
            name = declaration.getName().hashCode();
        }

        public OffsetSortedKey(int offset, int name) {
            start = offset;
            this.name = name;
        }

        @Override
        public int compareTo(OffsetSortedKey o) {
            int res = start - o.start;
            if (res == 0) {
                if (name < o.name) {
                    return -1;
                } else if (name > o.name) {
                    return 1;
                }
            }
            return res;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OffsetSortedKey) {
                OffsetSortedKey key = (OffsetSortedKey) obj;
                return compareTo(key)==0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.start;
            hash = 37 * hash + this.name;
            return hash;
        }

        @Override
        public String toString() {
            return "OffsetSortedKey: " + this.name + "[" + this.start; // NOI18N
        }

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            output.writeInt(start);
            output.writeInt(name);
        }

        public OffsetSortedKey(RepositoryDataInput input) throws IOException {
            start = input.readInt();
            name = input.readInt();
        }
    }

    public static final class NameKey implements Comparable<NameKey> {
        private int start = 0;
        private final CharSequence name;
        public NameKey(CsmUID<CsmOffsetableDeclaration> anUid) {
            name = UIDUtilities.getName(anUid);
            start = UIDUtilities.getStartOffset(anUid);
        }

        public NameKey(CharSequence name, int offset) {
            this.name = name;
            start = offset;
        }

        @Override
        public int compareTo(NameKey o) {
            int res = CharSequences.comparator().compare(name, o.name);
            if (res == 0) {
                res = start - o.start;
            }
            return res;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + this.start;
            hash = 89 * hash + Objects.hashCode(this.name);
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
            final NameKey other = (NameKey) obj;
            if (this.start != other.start) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }

    }
}
