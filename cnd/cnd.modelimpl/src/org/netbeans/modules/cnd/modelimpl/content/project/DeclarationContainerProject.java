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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.ProjectDeclarationContainerKey;
import org.netbeans.modules.cnd.modelimpl.textcache.UniqueNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 *
 */
public class DeclarationContainerProject extends DeclarationContainer {
    private final ReadWriteLock friendsLock = new ReentrantReadWriteLock();
    private final Map<CharSequence, Set<CsmUID<CsmFriend>>> friends;
    private static final boolean TEST_DATABASE = false;

    private static final DeclarationContainerProject EMPTY = new DeclarationContainerProject() {

        @Override
        public void put() {
        }

        @Override
        public void putDeclaration(CsmOffsetableDeclaration decl) {
        }
    };

    public DeclarationContainerProject(ProjectBase project) {
        super(new ProjectDeclarationContainerKey(project.getUnitId()));
        friends = new HashMap<>();
        put();
    }

    public DeclarationContainerProject(RepositoryDataInput input) throws IOException {
        super(input);
        int colSize = input.readInt();
        friends = new HashMap<>(colSize);
        UIDObjectFactory.getDefaultFactory().readStringToUIDMapSet(friends, input, UniqueNameCache.getManager(), colSize);
    }

    // only for EMPTY static field
    private DeclarationContainerProject() {
        super((Key) null);
        friends = new ConcurrentHashMap<>();
    }

    public static DeclarationContainerProject empty() {
        return EMPTY;
    }

    @Override
    protected void onRemoveDeclaration(CsmOffsetableDeclaration decl) {
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriend cls = (CsmFriend) decl;
            CharSequence name = CharSequences.create(cls.getName());
            try {
                friendsLock.writeLock().lock();
                Set<CsmUID<CsmFriend>> set = friends.get(name);
                if (set != null) {
                    set.remove(UIDs.get(cls));
                    if (set.isEmpty()) {
                        friends.remove(name);
                    }
                }
            } finally {
               friendsLock.writeLock().unlock();
            }
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriend fun = (CsmFriend) decl;
            CharSequence name = CharSequences.create(((CsmFriendFunction)fun).getSignature());
            try {
                friendsLock.writeLock().lock();
                Set<CsmUID<CsmFriend>> set = friends.get(name);
                if (set != null) {
                    set.remove(UIDs.get(fun));
                    if (set.isEmpty()) {
                        friends.remove(name);
                    }
                }
            } finally {
                friendsLock.writeLock().unlock();
            }
        }
    }

    @Override
    public void removeDeclaration(CsmOffsetableDeclaration decl) {
        super.removeDeclaration(decl);
        if (TEST_DATABASE) {
//            CsmUID<CsmOffsetableDeclaration> uid = UIDCsmConverter.declarationToUID(decl);
//            Key key = RepositoryUtils.UIDtoKey(uid);
//            @SuppressWarnings("unchecked")
//            MapBasedTable table = (MapBasedTable) RepositoryAccessor.getRepository().getDatabaseTable(key, DeclarationContainerProjectStorage.TABLE_NAME);
//            KeyDataPresentation dataPresentation = key.getDataPresentation();
//            KeyDataPresentationImpl keyImpl = new KeyDataPresentationImpl(
//                    dataPresentation.getUnitPresentation(), dataPresentation.getNamePresentation(),
//                    dataPresentation.getKindPresentation(), dataPresentation.getFilePresentation(),
//                    dataPresentation.getStartPresentation(), dataPresentation.getEndPresentation());
//            try {
//                getLock().writeLock().lock();
//                DataPresentationImpl removedKeyImpl = (DataPresentationImpl) table.remove(keyImpl);
////                CharSequence uin = decl.getUniqueName();
////                UniqueNameImpl uinImpl = new UniqueNameImpl(uin);
////                DataPresentationImpl valueImpl = new DataPresentationImpl(keyImpl,uinImpl);
////                if (!valueImpl.equals(removedKeyImpl)) {
////                    if (removedKeyImpl == null) {
////                        new Exception("Declaration is not found in database\n"+
////                                      "\tDeclaration="+decl+"\n"+
////                                      "\tUIN="+uinImpl.getUin()).printStackTrace();
////                    } else {
////                        new Exception("Remove declaration with changed UIN\n"+
////                                      "\tDeclaration="+decl+"\n"+
////                                      "\tOld UIN="+removedKeyImpl.getUin()+"\n"+
////                                      "\tNew UIN="+uinImpl.getUin()).printStackTrace();
////                    }
////                }
//            } finally {
//                getLock().writeLock().unlock();
//            }
        }
    }

    @Override
    public void putDeclaration(CsmOffsetableDeclaration decl) {
        super.putDeclaration(decl);
        if (TEST_DATABASE) {
//            CharSequence uin = decl.getUniqueName();
//            CsmUID<CsmOffsetableDeclaration> uid = UIDCsmConverter.declarationToUID(decl);
//            Key key = RepositoryUtils.UIDtoKey(uid);
//            @SuppressWarnings("unchecked")
//            MapBasedTable table = (MapBasedTable) RepositoryAccessor.getRepository().getDatabaseTable(key, DeclarationContainerProjectStorage.TABLE_NAME);
//            KeyDataPresentation dataPresentation = key.getDataPresentation();
//            KeyDataPresentationImpl keyImpl = new KeyDataPresentationImpl(
//                    dataPresentation.getUnitPresentation(), dataPresentation.getNamePresentation(),
//                    dataPresentation.getKindPresentation(), dataPresentation.getFilePresentation(),
//                    dataPresentation.getStartPresentation(), dataPresentation.getEndPresentation());
//            UniqueNameImpl uinImpl = new UniqueNameImpl(uin);
//            DataPresentationImpl valueImpl = new DataPresentationImpl(keyImpl,uinImpl);
//            try {
//                getLock().writeLock().lock();
//                table.put(keyImpl, valueImpl);
//            } finally {
//                getLock().writeLock().unlock();
//            }
//            try {
//                getLock().readLock().lock();
//                assert table.get(keyImpl).equals(valueImpl);
//            } finally {
//                getLock().readLock().unlock();
//            }
        }
    }

    @Override
    public Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
        Collection<CsmOffsetableDeclaration> res = super.findDeclarations(uniqueName);
        if (TEST_DATABASE) {
//            UniqueNameImpl uinImpl = new UniqueNameImpl(uniqueName);
//            Collection<DataPresentationImpl> res2 = new ArrayList<DataPresentationImpl>();
//            try {
//                getLock().readLock().lock();
//                @SuppressWarnings("unchecked")
//                Collection<DataPresentationImpl> index = (Collection<DataPresentationImpl>)
//                        ((MapBasedTable)RepositoryAccessor.getRepository().getDatabaseTable(getKey(), DeclarationContainerProjectStorage.TABLE_INDEX)).duplicates(uinImpl);
//                res2.addAll(index);
//            } finally {
//                getLock().readLock().unlock();
//            }
//            Collection<CsmOffsetableDeclaration> res3 = new ArrayList<CsmOffsetableDeclaration>();
//            for (DataPresentationImpl entry : res2){
//                Key aKey = KeyPresentationFactorySupport.create(entry);
//                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) RepositoryAccessor.getRepository().get(aKey);
//                res3.add(decl);
//            }
//            if (res.size() != res3.size()) {
//                System.err.println("Find gets different results");
//                System.err.println("Map:");
//                for(CsmOffsetableDeclaration decl : res) {
//                    System.err.println("\t"+decl);
//                }
//                System.err.println("Database:");
//                for(CsmOffsetableDeclaration decl : res3) {
//                    System.err.println("\t"+decl);
//                }
//            }
//            return res3;
        }
        return res;
    }

    @Override
    public CsmDeclaration getDeclaration(CharSequence uniqueName) {
        CsmDeclaration res = super.getDeclaration(uniqueName);
        if (TEST_DATABASE) {
//            UniqueNameImpl uinImpl = new UniqueNameImpl(uniqueName);
//            DataPresentationImpl res2;
//            try {
//                getLock().readLock().lock();
//                res2 = (DataPresentationImpl)
//                        ((MapBasedTable)RepositoryAccessor.getRepository().getDatabaseTable(getKey(), DeclarationContainerProjectStorage.TABLE_INDEX)).get(uinImpl);
//            } finally {
//                getLock().readLock().unlock();
//            }
//            CsmOffsetableDeclaration res3 = null;
//            if (res2 != null) {
//                Key aKey = KeyPresentationFactorySupport.create(res2);
//                res3 = (CsmOffsetableDeclaration) RepositoryAccessor.getRepository().get(aKey);
//            }
//            if (res != null &&  res3 != null && !res.equals(res3)) {
//                System.err.println("Find gets different results");
//                System.err.println("Map:");
//                System.err.println("\t"+res);
//                System.err.println("Database:");
//                System.err.println("\t"+res3);
//            }
//            return res3;
        }
        return res;
    }


    @Override
    protected void onPutDeclaration(CsmOffsetableDeclaration decl) {
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriend cls = (CsmFriend) decl;
            CharSequence name = CharSequences.create(cls.getName());
            try {
                friendsLock.writeLock().lock();
                Set<CsmUID<CsmFriend>> set = friends.get(name);
                if (set == null) {
                    set = new HashSet<>();
                    friends.put(name, set);
                }
                set.add(UIDs.get(cls));
            } finally {
                friendsLock.writeLock().unlock();
            }
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriend fun = (CsmFriend) decl;
            CharSequence name = CharSequences.create(((CsmFriendFunction)fun).getSignature());
            try {
                friendsLock.writeLock().lock();
                Set<CsmUID<CsmFriend>> set = friends.get(name);
                if (set == null) {
                    set = new HashSet<>();
                    friends.put(name, set);
                }
                set.add(UIDs.get(fun));
            } finally {
                friendsLock.writeLock().unlock();
            }
        }
    }

    public SortedMap<CharSequence, Set<CsmUID<CsmFriend>>> getTestFriends(){
        try {
            friendsLock.readLock().lock();
            TreeMap<CharSequence, Set<CsmUID<CsmFriend>>> res = new TreeMap<>();
            for(Map.Entry<CharSequence, Set<CsmUID<CsmFriend>>> entry : friends.entrySet()) {
                res.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            return res;
        } finally {
            friendsLock.readLock().unlock();
        }
    }

    public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl) {
        CharSequence name = null;
        if (CsmKindUtilities.isClass(decl)) {
            CsmClass cls = (CsmClass) decl;
            name = cls.getName();
        } else if (CsmKindUtilities.isFunction(decl)) {
            CsmFunction fun = (CsmFunction) decl;
            name = fun.getSignature();
        }
        if (name != null) {
            name = CharSequences.create(name);
            List<CsmUID<? extends CsmFriend>> list = new ArrayList<>();
            try {
                friendsLock.readLock().lock();
                Set<CsmUID<CsmFriend>> set = friends.get(name);
                if (set != null) {
                    list.addAll(set);
                }
            } finally {
                friendsLock.readLock().unlock();
            }
            if (list.size() > 0) {
                Collection<CsmFriend> res = new ArrayList<>();
                for (CsmUID<? extends CsmFriend> friendUID : list) {
                    CsmFriend friend = friendUID.getObject();
                    if (CsmKindUtilities.isFriendClass(friend)) {
                        CsmFriendClass cls = (CsmFriendClass) friend;
                        if (decl.equals(cls.getReferencedClass())) {
                            res.add(cls);
                        }
                    } else if (CsmKindUtilities.isFriendMethod(friend)) {
                        CsmFriendFunction fun = (CsmFriendFunction) friend;
                        if (decl.equals(fun.getReferencedFunction())) {
                            res.add(fun);
                        }
                    }
                }
                return res;
            }
        }
        return Collections.<CsmFriend>emptyList();
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        try {
            friendsLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMapSet(friends, aStream);
        } finally {
            friendsLock.readLock().unlock();
        }
    }
}
