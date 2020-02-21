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

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;

/**
 * Keeps an index that maps keys to file extents.
 */
public interface FileIndex {

    /** 
     * Gets chunk info that describes the extent by the given key 
     */
    ChunkInfo get(LayerKey key);
    
    /** 
     * Puts information about the given extent to index.
     * @param key   key
     * @param offset new extent offset 
     * @param size new extent size
     * @return old chunk size
     */
    int put(LayerKey key, long offset, int size);
    
    /**
     * Removes entry by the given key
     * @return old chunk size
     */
    int remove(LayerKey key);
    
    /**
     * Returns the size of this index
     */
    int size();
    
    /**
     * Returns a collection of this index keys
     */
    Collection<LayerKey> keySet();
    
    /**
     * Returns an iterator for this index keys collection
     */
    Iterator<LayerKey> getKeySetIterator();
}
