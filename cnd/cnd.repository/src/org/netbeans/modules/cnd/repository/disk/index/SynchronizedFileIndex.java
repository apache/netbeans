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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;


/**
 * Synchronized index wrapper:
 * Delegate work to the given index, is responsible for synchronization
 */
class SynchronizedFileIndex implements FileIndex {
    
    private final FileIndex delegate;
    private final ReadWriteLock indexLock = new ReentrantReadWriteLock();
    
    public SynchronizedFileIndex(FileIndex delegate) {
	this.delegate = delegate;
    }
    
    @Override
    public int size() {
	indexLock.readLock().lock();
	try {
	    return delegate.size();
	} finally {
	    indexLock.readLock().unlock();
	}
    }
    
    private class SyncRemoveIterator<K> implements Iterator<K> {
	
	Iterator<K> delegate;
	
	SyncRemoveIterator(Iterator<K> delegate) {
	    this.delegate = delegate;
	}

        @Override
	public void remove() {
	    indexLock.writeLock().lock();
	    try {
		delegate.remove();
	    } finally {
		indexLock.writeLock().unlock();
	    }
	}
	
        @Override
	public K next() {
	    return delegate.next();
	}
	
        @Override
	public boolean hasNext() {
	    return delegate.hasNext();
	}
    }
    
    @Override
    public Iterator<LayerKey> getKeySetIterator() {
        return new SyncRemoveIterator<LayerKey>(delegate.getKeySetIterator());
    }
    
    @Override
    public Collection<LayerKey> keySet() {
        indexLock.readLock().lock();
        try {
            return new ArrayList<LayerKey>(delegate.keySet());
        }  finally {
            indexLock.readLock().unlock();
        }
    }
    
    @Override
    public int put(LayerKey key, long offset, int size) {
        indexLock.writeLock().lock();
        try {
            return delegate.put(key, offset, size);
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    @Override
    public int remove(LayerKey key) {
        indexLock.writeLock().lock();
        try {
            return delegate.remove(key);
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    @Override
    public ChunkInfo get(LayerKey key) {
        indexLock.readLock().lock();
        try {
            return delegate.get(key);
        } finally {
            indexLock.readLock().unlock();
        }
    }    
}
