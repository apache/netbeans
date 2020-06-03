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
package org.netbeans.modules.cnd.repository.disk.index;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.repository.util.LongHashMap;
import org.netbeans.modules.cnd.repository.util.SlicedLongHashMap;

/**
 * LongHashMap based implementation of FileIndex
 *
 */
public class CompactFileIndex implements FileIndex, SelfPersistent {

    private static final int shift = 37;
    private static final long mask = (1L << shift) - 1;
    private static final int DEFAULT_SLICE_CAPACITY;
    private static final int DEFAULT_SLICE_COUNT;

    static {
        int nrProc = Runtime.getRuntime().availableProcessors();
        if (nrProc <= 4) {
            DEFAULT_SLICE_COUNT = 32;
            DEFAULT_SLICE_CAPACITY = 512;
        } else {
            DEFAULT_SLICE_COUNT = 128;
            DEFAULT_SLICE_CAPACITY = 128;
        }
    }
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final SlicedLongHashMap<LayerKey> map = new SlicedLongHashMap<LayerKey>(DEFAULT_SLICE_COUNT, DEFAULT_SLICE_CAPACITY);

    public CompactFileIndex() {
    }

    public CompactFileIndex(final RepositoryDataInput input) throws IOException {

        assert input != null;

        final int size = input.readInt();

        for (int i = 0; i < size; i++) {
            LayerKey layerKey = LayerKey.read(input);
            long longValue = input.readLong();
            map.put(layerKey, longValue);
        }
    }

    @Override
    public int size() {
        try {
            lock.readLock().lock();
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<LayerKey> keySet() {
        try {
            lock.readLock().lock();
            return map.keySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<LayerKey> getKeySetIterator() {
        return keySet().iterator();
    }

    @Override
    public int remove(final LayerKey key) {
        long data = LongHashMap.NO_VALUE;
        try {
            lock.writeLock().lock();
            data = map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
        return (data == LongHashMap.NO_VALUE) ? 0 : convertToSize(data);
    }

    @Override
    public int put(final LayerKey key, final long offset, final int size) {
        long data = LongHashMap.NO_VALUE;
        try {
            lock.writeLock().lock();
            data = map.put(key, convertToLongData(offset, size));
        } finally {
            lock.writeLock().unlock();
        }
        return (data == LongHashMap.NO_VALUE) ? 0 : convertToSize(data);
    }

    private static long convertToLongData(final long offset, final int size) {
        assert (offset <= mask) : "Offset " + offset + " is too large";
        assert (size < (1 << (64 - shift))) : "Size " + size + " is too large";
        long data = size;
        data <<= shift;
        data |= (offset & mask);
        return data;
    }

    private static int convertToSize(final long data) {
        final int size = (int) (data >>> shift);
        return size;
    }

    private static long convertToOffset(final long data) {
        final long offset = data & mask;
        return offset;
    }

    @Override
    public ChunkInfo get(final LayerKey key) {
        try {
            lock.readLock().lock();
            final long entry = map.get(key);
            return (entry == LongHashMap.NO_VALUE) ? null : new LongChunkInfo(entry);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void write(final RepositoryDataOutput output) throws IOException {
        final Collection<LongHashMap.Entry<LayerKey>> collection = map.entrySet();
        output.writeInt(collection.size());
        for (LongHashMap.Entry<LayerKey> entry : collection) {
            LayerKey.write(entry.getKey(), output);
            output.writeLong(entry.getValue());
        }
    }

    private static class LongChunkInfo implements ChunkInfo, Comparable<ChunkInfo>, SelfPersistent {

        long entry;

        public LongChunkInfo(final long entry) {
            this.entry = entry;
        }

        @Override
        public int getSize() {
            return convertToSize(entry);
        }

        @Override
        public long getOffset() {
            return convertToOffset(entry);
        }

        @Override
        public int compareTo(final ChunkInfo o) {
            return (this.getOffset() < o.getOffset()) ? -1 : 1;
        }

        @Override
        public void setOffset(final long offset) {
            entry = convertToLongData(offset, getSize());
        }

        @Override
        public String toString() {
            final Formatter f = new Formatter();
            long offset = getOffset();
            f.format("ChunkInfo [offset=%d (%H) size=%d long=%d]", offset, offset, getSize(), entry); // NOI18N
            return f.toString();
        }

        @Override
        public void write(final RepositoryDataOutput output) throws IOException {
            output.writeLong(entry);
        }
    }
}
