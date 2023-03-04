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
package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import org.apache.lucene.search.Query;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex2;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.Index.Status;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.modules.parsing.lucene.support.Queries.QueryKind;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;

/**
 * This proxy records documents maintained by the index; the list of documents is
 * then used in the test. Note the LayeredDocumentIndex is created on top of the
 * TestIndexImpl, which may not be the brightest idea; LayeredDocumentIndex could 
 * allow subclassing for testing purposes.
 * 
 * @author sdedic
 */
public class TestIndexFactoryImpl implements IndexFactoryImpl {
            // --------------------------------------------------------------------
        // IndexFactoryImpl implementation
        // --------------------------------------------------------------------
    
        private static final IndexFactoryImpl delegate = LuceneIndexFactory.getDefault();

        public @Override IndexDocument createDocument(Indexable indexable) {
            return new TestIndexDocumentImpl(indexable, delegate.createDocument(indexable));
        }

        public @Override LayeredDocumentIndex createIndex(Context ctx) throws IOException {
            DocumentIndex2.Transactional ii = delegate.createIndex(ctx);
            Reference<LayeredDocumentIndex> ttiRef = indexImpls.get(ii);
            LayeredDocumentIndex lii = ttiRef != null ? ttiRef.get() : null;
            
            if (lii == null) {
                TestIndexImpl tii = new TestIndexImpl(ii);
                testImpls.put(ii, new WeakReference<TestIndexImpl>(tii));
                lii = new LayeredDocumentIndex(tii);
                indexImpls.put(ii, new SoftReference<LayeredDocumentIndex>(lii));
            }
            return lii;
        }

        @Override
        public DocumentIndexCache getCache(Context ctx) throws IOException {
            return null;
        }


        
        public TestIndexImpl getTestIndex(FileObject indexFolder) throws IOException {
            DocumentIndex ii = delegate.getIndex(indexFolder);
            Reference<TestIndexImpl> tii = testImpls.get(ii);
            return tii == null ? null : tii.get();
        }

        public @Override LayeredDocumentIndex getIndex(FileObject indexFolder) throws IOException {
            DocumentIndex ii = delegate.getIndex(indexFolder);
            Reference<LayeredDocumentIndex> ttiRef = indexImpls.get(ii);
            return ttiRef != null ? ttiRef.get() : null;
        }

        private final Map<DocumentIndex, Reference<TestIndexImpl>> testImpls = new WeakHashMap<DocumentIndex, Reference<TestIndexImpl>>();
        private final Map<DocumentIndex, Reference<LayeredDocumentIndex>> indexImpls = new WeakHashMap<DocumentIndex, Reference<LayeredDocumentIndex>>();

    public static final class TestIndexImpl implements DocumentIndex2.Transactional {

        public TestIndexImpl(DocumentIndex2.Transactional original) {
            this.original = original;
        }

        @Override
        public Status getStatus() throws IOException {
            return Status.VALID;
        }

        // --------------------------------------------------------------------
        // IndexImpl implementation
        // --------------------------------------------------------------------

        @Override
        public void addDocument(IndexDocument document) {
            assert document instanceof TestIndexDocumentImpl;

            original.addDocument(((TestIndexDocumentImpl) document).getOriginal());

            TestIndexDocumentImpl tidi = (TestIndexDocumentImpl) document;
            List<TestIndexDocumentImpl> list = documents.get(tidi.getIndexable().getRelativePath());
            if (list == null) {
                list = new ArrayList<TestIndexDocumentImpl>();
                documents.put(tidi.getIndexable().getRelativePath(), list);
            }
            list.add(tidi);
        }

        @Override
        public void removeDocument(String relativePath) {
            original.removeDocument(relativePath);

            Collection<String> toRemove = new HashSet<String>();
            for(String rp : documents.keySet()) {
                if (rp.equals(relativePath)) {
                    toRemove.add(rp);
                }
            }
            documents.keySet().removeAll(toRemove);
        }

        @Override
        public void store(boolean optimize) throws IOException {
            original.store(optimize);
        }

        @Override
        public void commit() throws IOException {
            original.commit();
        }

        @Override
        public void rollback() throws IOException {
            original.rollback();
        }

        @Override
        public void txStore() throws IOException {
            original.txStore();
        }

        @Override
        public void clear() throws IOException {
            original.clear();
        }

        @Override
        public Collection<? extends IndexDocument> query(String fieldName, String value, Queries.QueryKind kind, String... fieldsToLoad) throws IOException, InterruptedException {
            return original.query(fieldName, value, kind, fieldsToLoad);
        }

        @Override
        public <T> Collection<? extends T> query(Query query, Convertor<? super IndexDocument, ? extends T> convertor, String... fieldsToLoad) throws IOException, InterruptedException {
            return original.query(query, convertor, fieldsToLoad);
        }

        @Override
        public Collection<? extends IndexDocument> findByPrimaryKey(String primaryKeyValue, QueryKind kind, String... fieldsToLoad) throws IOException, InterruptedException {
            return original.findByPrimaryKey(primaryKeyValue, kind, fieldsToLoad);
        }                
        
        @Override
        public void close() throws IOException {
            original.close();
        }

        @Override
        public void markKeyDirty(String primaryKey) {            
        }

        @Override
        public void removeDirtyKeys(Collection<? extends String> dirtyKeys) {
        }

        @Override
        public Collection<? extends String> getDirtyKeys() {
            return Collections.<String>emptySet();
        }
        
        // --------------------------------------------------------------------
        // private implementation
        // --------------------------------------------------------------------

        private final DocumentIndex2.Transactional original;
        public Map<String, List<TestIndexDocumentImpl>> documents = new HashMap<String, List<TestIndexDocumentImpl>>();
        
    } // End of TestIndexImpl class

    public static final class TestIndexDocumentImpl implements IndexDocument {

        public final List<String> indexedKeys = new ArrayList<String>();
        public final List<String> indexedValues = new ArrayList<String>();
        public final List<String> unindexedKeys = new ArrayList<String>();
        public final List<String> unindexedValues = new ArrayList<String>();

        private final Indexable indexable;
        private final IndexDocument original;

        public TestIndexDocumentImpl(Indexable indexable, IndexDocument original) {
            this.indexable = indexable;
            this.original = original;
        }

        public Indexable getIndexable() {
            return indexable;
        }

        public IndexDocument getOriginal() {
            return original;
        }

        public @Override void addPair(String key, String value, boolean searchable, boolean stored) {
            original.addPair(key, value, searchable, stored);

            if (searchable) {
                indexedKeys.add(key);
                indexedValues.add(value);
            } else {
                unindexedKeys.add(key);
                unindexedValues.add(value);
            }
        }

        public String getValue(String key) {
            return original.getValue(key);
        }

        public String[] getValues(String key) {
            return original.getValues(key);
        }

        public String getPrimaryKey() {
            return original.getPrimaryKey();
        }

    } // End of TestIndexFactoryImpl class

} // End of TestIndexFactoryImpl class

