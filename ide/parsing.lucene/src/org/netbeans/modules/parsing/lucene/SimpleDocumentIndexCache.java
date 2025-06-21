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
package org.netbeans.modules.parsing.lucene;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;

/**
 * The {@link DocumentIndexCache} implementation which uses {@link SoftReference}
 * to detect flush condition.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public class SimpleDocumentIndexCache implements DocumentIndexCache {

    /**
     * This flag is used in tests, in particular in java.source IndexerTranscationTest. System property must be set before
     * the indexing starts and will disable caching of document changes, all changes will be flushed (but not committed) immediately.
     */
    private boolean disableCache = Boolean.getBoolean("test." + DocumentIndexImpl.class.getName() + ".cacheDisable");   //NOI18N

    private List<IndexDocument> toAdd;
    private List<String> toRemove;
    private Reference<List<?>[]> dataRef;

    @Override
    public boolean addDocument(IndexDocument document) {
        if (!(document instanceof IndexDocumentImpl)) {
            throw new IllegalArgumentException(document.getClass().getName());
        }
        final Reference<List<?>[]> ref = getDataRef();
        assert ref != null;
        final boolean shouldFlush = disableCache || ref.get() == null;
        toAdd.add(document);
        toRemove.add(document.getPrimaryKey());
        return shouldFlush;
    }

    @Override
    public boolean removeDocument(String primaryKey) {
        final Reference<List<?>[]> ref = getDataRef();
        assert ref != null;
        final boolean shouldFlush = ref.get() == null;
        toRemove.add(primaryKey);
        return shouldFlush;
    }

    @Override
    public void clear() {
        toAdd = null;
        toRemove = null;
        this.dataRef = null;
    }

    @Override
    public Collection<? extends String> getRemovedKeys() {
        return toRemove != null ? toRemove : Collections.<String>emptySet();
    }

    @Override
    public Collection<? extends IndexDocument> getAddedDocuments() {
        return toAdd != null ? toAdd : Collections.<IndexDocument>emptySet();
    }

    /* Use in tests only ! Clears data ref, causing the next addDocument
     * or removeDocument to flush the buffered contents
     */
    void testClearDataRef() {
        dataRef.clear();
    }

    private Reference<List<?>[]> getDataRef() {
        if (toAdd == null || toRemove == null) {
            assert toAdd == null && toRemove == null;
            assert dataRef == null;
            toAdd = new ArrayList<>();
            toRemove = new ArrayList<>();
            dataRef = new SoftReference<>(new List<?>[] {toAdd, toRemove});
        }
        return dataRef;
    }

}
