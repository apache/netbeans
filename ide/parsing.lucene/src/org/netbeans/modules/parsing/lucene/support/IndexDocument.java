/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.parsing.lucene.support;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Index document represents a single file produced by an Indexer.
 * It lets you store a series of [key,value] pairs in the
 * persistent store.
 * 
 * @since 1.1
 * 
 * @author Tomas Zezula
 * 
 */
public interface IndexDocument {            
    
    /**
     * Returns the value of the primary key of this document.
     * This key is used to delete document using the {@link DocumentIndex#removeDocument(java.lang.String)}
     * @return the value of the primary key
     */
    public @NonNull String getPrimaryKey ();
    
    
    /**
     * Add a [key,value] pair to this document. Note that the document really
     * contains a multi-map, so it is okay and normal to call addPair multiple
     * times with the same key. This just adds the value to the set of values
     * associated with the key.
     *
     * @param key The key that you will later search by. Note that you are NOT
     *   allowed to use the keys <code>filename</code> or <code>timestamp</code>
     *   since these are reserved (and in fact used) by GSF.
     * @param value The value that will be retrieved for this key
     * @param searchable A boolean which if set to true will store the pair with
     *   an indexed/searchable field key, otherwise with an un indexed field (that cannot be
     *   searched).  You <b>must</b> be consistent in how keys are identified
     *   as searchable; the same key must always be referenced with the same
     *   value for searchable when pairs are added (per document).
     */
    public void addPair (@NonNull String key, @NonNull String value, boolean searchable, boolean stored);
    
    /**
     * Returns the value of the field with the given name. If it does not exist
     * it returns null. If multiple fields exist it returns the first value added.
     * @param key to obtain the value for.
     * @return value or null
     */    
    public @CheckForNull String getValue (@NonNull String key);
    
    
    /**
     * Returns the values of the field with the given name. If it does not exist
     * it returns an empty array.
     * @return an array of value, never returns null
     */
    public @NonNull String[] getValues (@NonNull String key);    
}
