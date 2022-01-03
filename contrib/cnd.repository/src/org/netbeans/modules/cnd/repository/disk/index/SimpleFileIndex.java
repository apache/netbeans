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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * A simple FileIndex implementation
 */
public class SimpleFileIndex implements FileIndex, SelfPersistent {
    private static final int DEFAULT_INDEX_CAPACITY = 53711;
    private final Map<LayerKey, ChunkInfoBase> map = new ConcurrentHashMap<LayerKey, ChunkInfoBase>(DEFAULT_INDEX_CAPACITY);
    
    public SimpleFileIndex (){
    }
   
    public SimpleFileIndex (RepositoryDataInput input) throws IOException {
        int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            map.put(LayerKey.read(input),
                    new ChunkInfoBase(input));
        }
    }
        
    @Override
    public int size() {
	return map.size();
    }

    @Override
    public int remove(LayerKey key) {
	ChunkInfo oldInfo = map.remove(key);
	return (oldInfo == null) ? 0 : oldInfo.getSize();
    }
    
    @Override
    public Collection<LayerKey> keySet() {
	return map.keySet();
    }
    
    @Override
    public Iterator<LayerKey> getKeySetIterator() {
	return map.keySet().iterator();
    }

    @Override
    public int put(final LayerKey key, final long offset, final int size) {
	final ChunkInfo oldInfo = map.put(key, new ChunkInfoBase(offset, size));
	return (oldInfo == null) ? 0 : oldInfo.getSize();
    }

    @Override
    public ChunkInfo get(final LayerKey key) {
	final ChunkInfo info = map.get(key);
//	if( info == null ) {
//	    info = new ChunkInfo()
	return info;
    }    

    @Override
    public void write(final RepositoryDataOutput output) throws IOException {
        output.writeInt(size());
        Set<Entry<LayerKey, ChunkInfoBase>> aSet = map.entrySet();
        Iterator<Entry<LayerKey, ChunkInfoBase>> setIterator = aSet.iterator();
        while (setIterator.hasNext()) {
            Entry<LayerKey, ChunkInfoBase> anEntry = setIterator.next();
            LayerKey.write(anEntry.getKey(), output);
            anEntry.getValue().write(output);
        }
    }

    /**
     * Represents a file extent
     *
     * Should we keep size in index? 
     * keeping size in index waistes memory;
     * on the other hand, it allows to allocate buffer easily and effectively 
     */
    private static final class ChunkInfoBase implements ChunkInfo, Comparable<ChunkInfo>, SelfPersistent {

	private long offset;
	private int size;
	
	public ChunkInfoBase(long offset, int size) {
	    this.offset = offset;
	    this.size = size;
	}
        
        public ChunkInfoBase (RepositoryDataInput input) throws IOException {
            this.offset = input.readLong();
            this.size   = input.readInt();                    
        }
	
        @Override
	public String toString() {
	    StringBuilder sb = new StringBuilder("ChunkInfo ["); // NOI18N
	    sb.append("offset="); // NOI18N
	    sb.append(getOffset());
	    sb.append(" size="); // NOI18N
	    sb.append(getSize());
	    sb.append(']');
	    return sb.toString();
	}

        @Override
	public int compareTo(ChunkInfo o) {
            return (this.getOffset()  < o.getOffset()) ? -1 : 1;
	}

        @Override
	public long getOffset() {
	    return offset;
	}
	
        @Override
	public int getSize() {
	    return size;
	}
	
        @Override
	public void setOffset(long offset) {
	    this.offset = offset;
	}

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            output.writeLong(this.offset);
            output.writeInt(this.size);
        }
    }
}
