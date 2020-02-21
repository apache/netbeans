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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 *
 */
public class PositionManager {
    private enum Impl {
        trivial,
        optimistic,
        full,
        map
    }
    private static final Impl IMPL = Impl.trivial;
    private static final ConcurrentHashMap<Integer,ConcurrentHashMap<Integer,TreeMap<Integer,Integer>>> map = new ConcurrentHashMap<>();

    private PositionManager() {
    }
    
    public static CsmOffsetable.Position getPosition(CsmUID<CsmFile> uid, int posID) {
        return getPosition(UIDCsmConverter.UIDtoFile(uid), posID);
    }

    public static CsmOffsetable.Position getPosition(CsmFile file, int posID) {
        if (file instanceof FileImpl) {
            return new LazyOffsPositionImpl((FileImpl) file, getOffset(file, posID));
        } else {
            return new PositionImpl(posID);
        }
    }

//    private static MapBasedTable getPositionTable(Key key) {
//        return (MapBasedTable) RepositoryAccessor.getRepository().getDatabaseTable(key, PositionStorageImpl.TABLE_NAME);
//    }
    
    public static int getOffset(CsmUID<CsmFile> uid, int posID) {
        if (IMPL == Impl.trivial) {
            return posID;
        }
        Key key = RepositoryUtils.UIDtoKey(uid);
//        if (IMPL == Impl.optimistic || IMPL == Impl.full) {
//            @SuppressWarnings("unchecked")
//            MapBasedTable table = getPositionTable(key);
//            FilePositionKey positionKey = new PositionStorageImpl.FilePositionKey(KeyUtilities.getProjectFileIndex(key), posID);
//            Position position = (Position) table.get(positionKey);
//            return position.getOffset();
//        } else {
            // map implementation
            Map<Integer, Integer> fileMap = getFileMap(key);
            return fileMap.get(posID);
//        }
    }

    public static int getOffset(CsmFile file, int posID) {
        if (IMPL == Impl.trivial) {
            return posID;
        }
        throw new UnsupportedOperationException("Not yet implemented "); // NOI18N
    }
    
    public static int createPositionID(CsmFile file, int offset, Position.Bias bias) {
        if (IMPL == Impl.trivial) {
            return offset;
        }
        throw new UnsupportedOperationException("Not yet implemented "); // NOI18N
    }
    
    public static int createPositionID(CsmUID<CsmFile> uid, int offset, Position.Bias bias) {
        if (IMPL == Impl.trivial) {
            return offset;
        }
        Key key = RepositoryUtils.UIDtoKey(uid);
        int maxPositionId = 0;
//        if (IMPL == Impl.optimistic || IMPL == Impl.full) {
//            @SuppressWarnings("unchecked")
//            MapBasedTable table = getPositionTable(key);
//            if (IMPL == Impl.full) {
//                FilePositionKey positionStart = new PositionStorageImpl.FilePositionKey(KeyUtilities.getProjectFileIndex(key), 0);
//                FilePositionKey positionEnd = new PositionStorageImpl.FilePositionKey(KeyUtilities.getProjectFileIndex(key), Integer.MAX_VALUE);
//                maxPositionId = 0;
//                for(Map.Entry<?, ?> entry : table.getSubMap(positionStart, positionEnd)) {
//                    FilePositionKey positionKey = (FilePositionKey) entry.getKey();
//                    Position position = (Position) entry.getValue();
//                    if (position.getOffset() == offset) {
//                        return positionKey.getPositionID();
//                    }
//                    if (maxPositionId < positionKey.getPositionID()) {
//                        maxPositionId = positionKey.getPositionID();
//                    }
//                }
//                maxPositionId++;
//            } else if (IMPL == Impl.optimistic) {
//                maxPositionId = offset;
//            }
//            FilePositionKey newPositionKey = new PositionStorageImpl.FilePositionKey(KeyUtilities.getProjectFileIndex(key), maxPositionId);
//            PositionDataImpl newPositionData = new PositionDataImpl(offset, -1, -1);
//            table.put(newPositionKey, newPositionData);
//            return maxPositionId;
//        } else {
            // map implementation
            TreeMap<Integer, Integer> fileMap = getFileMap(key);
            if (fileMap.isEmpty()) {
                maxPositionId = 1;
            } else {
                Integer lastKey = fileMap.lastKey();
                maxPositionId = lastKey.intValue()+1;
            }

            fileMap.put(maxPositionId, offset);

            return maxPositionId;
//        }
    }

    private static TreeMap<Integer,Integer> getFileMap(Key key) {
        int unit = key.getUnitId();
        int file = KeyUtilities.getProjectFileIndex(key);
        ConcurrentHashMap<Integer, TreeMap<Integer, Integer>> unitMap = map.get(unit);
        if (unitMap == null) {
            unitMap = new ConcurrentHashMap<>();
            ConcurrentHashMap<Integer,TreeMap<Integer,Integer>> old = map.putIfAbsent(unit, unitMap);
            if (old != null) {
                unitMap = old;
            }
        }
        TreeMap<Integer, Integer> fileMap = unitMap.get(file);
        if (fileMap == null) {
            fileMap = new TreeMap<>();
            TreeMap<Integer, Integer> old = unitMap.putIfAbsent(file, fileMap);
            if (old != null) {
                fileMap = old;
            }
        }
        return fileMap;
    }

    public interface Position {
        int getOffset();
        int getLine();
        int getColumn();
        public enum Bias {

            FOWARD,
            BACKWARD,
        }
    }
    
    private static class PositionImpl implements CsmOffsetable.Position {
        private final int offset;

        public PositionImpl(int offset) {
            this.offset = offset;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getLine() {
            return -1;
        }

        @Override
        public int getColumn() {
            return -1;
        }

        @Override
        public String toString() {
            return "offset=" + offset; // NOI18N
        }
    }
}
