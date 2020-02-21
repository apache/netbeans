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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardEnum;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.UniqueNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 * Storage for project or namespace declarations.
 */
public abstract class DeclarationContainer extends ProjectComponent {

    private final TreeMap<CharSequence, Object> declarations;
    private final ReadWriteLock declarationsLock = new ReentrantReadWriteLock();

    protected DeclarationContainer(Key key) {
        super(key);
        declarations = new TreeMap<>(CharSequences.comparator());
    }

    protected DeclarationContainer(RepositoryDataInput input) throws IOException {
        super(input);
        declarations = UIDObjectFactory.getDefaultFactory().readStringToArrayUIDMap(input, UniqueNameCache.getManager());
    }

    public void removeDeclaration(CsmOffsetableDeclaration decl) {
        CharSequence uniqueName = CharSequences.create(decl.getUniqueName());
        CsmUID<CsmOffsetableDeclaration> anUid = UIDCsmConverter.declarationToUID(decl);
        Object o;
        try {
            declarationsLock.writeLock().lock();
            o = declarations.get(uniqueName);

            if (o instanceof CsmUID<?>[]) {
                @SuppressWarnings("unchecked")
                CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID<CsmOffsetableDeclaration>[]) o;
                int size = uids.length;
                CsmUID<CsmOffsetableDeclaration> res = null;
                int k = size;
                for (int i = 0; i < size; i++) {
                    CsmUID<CsmOffsetableDeclaration> uid = uids[i];
                    if (anUid.equals(uid)) {
                        uids[i] = null;
                        k--;
                    } else {
                        res = uid;
                    }
                }
                if (k == 0) {
                    declarations.remove(uniqueName);
                } else if (k == 1) {
                    declarations.put(uniqueName, res);
                } else {
                    @SuppressWarnings("unchecked")
                    CsmUID<CsmOffsetableDeclaration>[] newUids = new CsmUID[k];
                    k = 0;
                    for (int i = 0; i < size; i++) {
                        CsmUID<CsmOffsetableDeclaration> uid = uids[i];
                        if (uid != null) {
                            newUids[k] = uid;
                            k++;
                        }
                    }
                    declarations.put(uniqueName, newUids);
                }
            } else if (o instanceof CsmUID<?>) {
                declarations.remove(uniqueName);
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        onRemoveDeclaration(decl);
        put();
    }

    protected void onRemoveDeclaration(CsmOffsetableDeclaration decl) {
    }

    protected ReadWriteLock getLock() {
        return declarationsLock;
    }

    public void putDeclaration(CsmOffsetableDeclaration decl) {
        CharSequence name = UniqueNameCache.getManager().getString(decl.getUniqueName());
        CsmUID<CsmOffsetableDeclaration> uid = RepositoryUtils.put(decl);
        assert uid != null;
        if (!(uid instanceof SelfPersistent)) {
            String line = " ["+decl.getStartPosition().getLine()+":"+decl.getStartPosition().getColumn()+"-"+ // NOI18N
                          decl.getEndPosition().getLine()+":"+decl.getEndPosition().getColumn()+"]"; // NOI18N
            new Exception("attempt to put local declaration " + decl + line).printStackTrace(System.err); // NOI18N
        }
        try {
            declarationsLock.writeLock().lock();

            Object o = declarations.get(name);
            // there could be at max only one forward class and
            // we don't want forward class to overwrite anything
            if (o != null && (ForwardClass.isForwardClass(decl) || ForwardEnum.isForwardEnum(decl))) {
                return;
            }
            if (o instanceof CsmUID<?>[]) {
                @SuppressWarnings("unchecked")
                CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID[]) o;
                boolean found = false;
                for (int i = 0; i < uids.length; i++) {
                    if (UIDUtilities.isSameFile(uids[i], uid) || UIDUtilities.isForwardClass(uids[i])) {
                        uids[i] = uid;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    @SuppressWarnings("unchecked")
                    CsmUID<CsmOffsetableDeclaration>[] res = new CsmUID[uids.length + 1];
                    res[0] = uid;
                    System.arraycopy(uids, 0, res, 1, uids.length);
                    declarations.put(name, res);
                }
            } else if (o instanceof CsmUID<?>) {
                @SuppressWarnings("unchecked")
                CsmUID<CsmOffsetableDeclaration> oldUid = (CsmUID<CsmOffsetableDeclaration>) o;
                if (UIDUtilities.isSameFile(oldUid, uid) || UIDUtilities.isForwardClass(oldUid)) {
                    declarations.put(name, uid);
                } else {
                    CsmUID<?>[] uids = new CsmUID<?>[]{uid, oldUid};
                    declarations.put(name, uids);
                }
            } else {
                declarations.put(name, uid);
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        onPutDeclaration(decl);
        put();
    }

    protected void onPutDeclaration(CsmOffsetableDeclaration decl) {
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getUIDsRange(CharSequence from, CharSequence to) {
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<>();
        from = CharSequences.create(from);
        to = CharSequences.create(to);
        try {
            declarationsLock.readLock().lock();
            for (Map.Entry<CharSequence, Object> entry : declarations.subMap(from, to).entrySet()) {
                addAll(list, entry.getValue());
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return list;
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> findExternalUIDsFile(CsmFile file) {
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<>();
        int fileID = UIDUtilities.getFileID(UIDs.get(file));
        try {
            declarationsLock.readLock().lock();
            for (Map.Entry<CharSequence, Object> entry : declarations.entrySet()) {
                Object o = entry.getValue();
                if (o instanceof CsmUID<?>[]) {
                    // we know the template type to be CsmOffsetableDeclaration
                    @SuppressWarnings("unchecked") // checked
                    final CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID<CsmOffsetableDeclaration>[]) o;
                    for(CsmUID<CsmOffsetableDeclaration> u : uids) {
                        if (UIDUtilities.getFileID(u) == fileID) {
                            if (OffsetableDeclarationBase.isIncludedDeclaration(u)) {
                                list.add(u);
                            }
                        }
                    }
                } else if (o instanceof CsmUID<?>) {
                    // we know the template type to be CsmOffsetableDeclaration
                    @SuppressWarnings("unchecked") // checked
                    final CsmUID<CsmOffsetableDeclaration> uid = (CsmUID<CsmOffsetableDeclaration>) o;
                    if (UIDUtilities.getFileID(uid) == fileID) {
                        if (OffsetableDeclarationBase.isIncludedDeclaration(uid)) {
                            list.add(uid);
                        }
                    }
                }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return list;
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getUIDsFQN(CharSequence fqn, Kind[] kinds) {
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<>();
        char maxChar = 255; //Character.MAX_VALUE;
        for(Kind kind : kinds) {
            String prefix = CharSequenceUtils.toString(""+Utils.getCsmDeclarationKindkey(kind), OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR, fqn);
            CharSequence from  = CharSequences.create(prefix);
            CharSequence to  = CharSequences.create(prefix+maxChar);
            try {
                declarationsLock.readLock().lock();
                for (Map.Entry<CharSequence, Object> entry : declarations.subMap(from, to).entrySet()) {
                    addAll(list, entry.getValue());
                }
            } finally {
                declarationsLock.readLock().unlock();
            }
        }
        return list;
    }

    // for unit test
    public SortedMap<CharSequence, Object> getTestDeclarations() {
        try {
            declarationsLock.readLock().lock();
            return new TreeMap<>(declarations);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }

    /**
     * Adds ether object to the collection or array of objects
     * @param list
     * @param o - can be CsmUID or CsmUID[]
     */
    private static void addAll(Collection<CsmUID<CsmOffsetableDeclaration>> list, Object o) {
        if (o instanceof CsmUID<?>[]) {
            // we know the template type to be CsmOffsetableDeclaration
            @SuppressWarnings("unchecked") // checked
            final CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID<CsmOffsetableDeclaration>[]) o;
            list.addAll(Arrays.asList(uids));
        } else if (o instanceof CsmUID<?>) {
            // we know the template type to be CsmOffsetableDeclaration
            @SuppressWarnings("unchecked") // checked
            final CsmUID<CsmOffsetableDeclaration> uid = (CsmUID<CsmOffsetableDeclaration>) o;
            list.add(uid);
        }
    }

    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(CharSequence from, CharSequence to) {
        return UIDCsmConverter.UIDsToDeclarations(getUIDsRange(from, to));
    }

    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(CharSequence fqn, Kind[] kinds) {
        return UIDCsmConverter.UIDsToDeclarations(getUIDsFQN(fqn, kinds));
    }

    public Collection<CsmOffsetableDeclaration> findExternalDeclarations(CsmFile file) {
        return UIDCsmConverter.UIDsToDeclarations(findExternalUIDsFile(file));
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarationsUIDs() {
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<>();
        try {
            declarationsLock.readLock().lock();
            for (Object o : declarations.values()) {
                addAll(list, o);
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return list;
    }

    public Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<>();
        uniqueName = CharSequences.create(uniqueName);
        try {
            declarationsLock.readLock().lock();
            addAll(list, declarations.get(uniqueName));
        } finally {
            declarationsLock.readLock().unlock();
        }
        return UIDCsmConverter.UIDsToDeclarations(list);
    }

    public CsmDeclaration getDeclaration(CharSequence uniqueName) {
        CsmDeclaration result;
        CsmUID<CsmDeclaration> uid = null;
        uniqueName = CharSequences.create(uniqueName);
        try {
            declarationsLock.readLock().lock();
            Object o = declarations.get(uniqueName);
            if (o instanceof CsmUID<?>[]) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                final CsmUID<CsmDeclaration>[] uids = (CsmUID<CsmDeclaration>[]) o;
                uid = uids[0];
            } else if (o instanceof CsmUID<?>) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                final CsmUID<CsmDeclaration> uidt = (CsmUID<CsmDeclaration>) o;
                uid = uidt;
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        result = UIDCsmConverter.UIDtoDeclaration(uid);
        if (uid != null && result == null) {
            DiagnosticExceptoins.registerIllegalRepositoryStateException("no declaration for UID ", uid); // NOI18N
        }
        return result;
    }

    public void clearDeclarations() {
        try {
            declarationsLock.writeLock().lock();
            declarations.clear();
            put();
        } finally {
            declarationsLock.writeLock().unlock();
        }
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        try {
            declarationsLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeStringToArrayUIDMap(declarations, aStream, false);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }
}
