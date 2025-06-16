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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex2;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.modules.parsing.lucene.support.Queries.QueryKind;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class DocumentIndexImpl implements DocumentIndex2, Runnable {
                            
    private static final Convertor<IndexDocument,Document> DEFAULT_ADD_CONVERTOR = Convertors.newIndexDocumentToDocumentConvertor();    
    private static final Convertor<Document,IndexDocumentImpl> DEFAULT_QUERY_CONVERTOR = Convertors.newDocumentToIndexDocumentConvertor();
    private static final Convertor<String,Query> REMOVE_CONVERTOR = Convertors.newSourceNameToQueryConvertor();
    private static final Logger LOGGER = Logger.getLogger(DocumentIndexImpl.class.getName());
    //@GuardedBy ("this")
    private final Set<String> dirtyKeys = new HashSet<>();
    //@GuardedBy ("this")
    private final DocumentIndexCache cache;
    private final Index luceneIndex;
    private final Convertor<? super IndexDocument, ? extends Document> addConvertor;
    private final Convertor<? super Document, ? extends IndexDocument> queryConvertor;
    /**
     * Transactional extension to the index
     */
    final Index.Transactional txLuceneIndex;
    final AtomicBoolean requiresRollBack = new AtomicBoolean();

    private DocumentIndexImpl (
            @NonNull final Index index,
            @NonNull final DocumentIndexCache cache) {
        assert index != null;
        assert cache != null;
        this.luceneIndex = index;
        this.cache = cache;
        Convertor<IndexDocument,Document> _addConvertor = null;
        Convertor<Document,IndexDocument> _queryConvertor = null;
        if (cache instanceof DocumentIndexCache.WithCustomIndexDocument custom) {
            _addConvertor = custom.createAddConvertor();
            _queryConvertor = custom.createQueryConvertor();
        }
        addConvertor = _addConvertor != null ? _addConvertor : DEFAULT_ADD_CONVERTOR;
        queryConvertor = _queryConvertor != null ? _queryConvertor : DEFAULT_QUERY_CONVERTOR;
        if (index instanceof Index.Transactional transactional) {
            this.txLuceneIndex = transactional;
        } else {
            this.txLuceneIndex = null;
        }
    }
    
    /**
     * Adds document
     * @param document
     */
    @Override
    public void addDocument(IndexDocument document) {
        final boolean forceFlush;
        synchronized (this) {
            forceFlush = cache.addDocument(document);
        }
        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store(false, true);
                System.gc();
            } catch (IOException ioe) {
                //Reindexed in RU.storeChanges
                LOGGER.log(Level.WARNING, ioe.getMessage());
                requiresRollBack.set(true);
            }
        }
    }

    /**
     * Removes all documents for given path
     * @param relativePath
     */
    @Override
    public void removeDocument(String primaryKey) {
        final boolean forceFlush;
        synchronized (this) {
            forceFlush = cache.removeDocument(primaryKey);
        }
        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store(false, true);
            } catch (IOException ioe) {
                //Reindexed in RU.storeChanges
                LOGGER.log(Level.WARNING, ioe.getMessage());
                requiresRollBack.set(true);
            }
        }
    }

    
    /**
     * Checks if the Lucene index is valid.
     * @return {@link Status#INVALID} when the index is broken, {@link Status#EMPTY}
     * when the index does not exist or {@link  Status#VALID} if the index is valid
     * @throws IOException when index is already closed
     */
    @Override
    public Index.Status getStatus() throws IOException {
        return luceneIndex.getStatus(true);
    }
    
    @Override
    public void close() throws IOException {
        luceneIndex.close();
    }
    
    @Override
    public void store(boolean optimize) throws IOException {
        checkRollBackNeeded();
        store(optimize, false);
    }

    @Override
    public void run() {
        if (luceneIndex instanceof Runnable runnable) {
            runnable.run();
        }
    }
    
    private void store(boolean optimize, boolean flushOnly) throws IOException {
        final  boolean change = storeImpl(optimize, flushOnly);
        if (!change && !flushOnly && txLuceneIndex != null) {
            commitImpl();
        }
    }

    private boolean storeImpl(
            final boolean optimize,
            final boolean flushOnly) throws IOException {
        final Collection<? extends IndexDocument> _toAdd;
        final Collection<? extends String> _toRemove;
        synchronized (this) {
            _toAdd = cache.getAddedDocuments();
            _toRemove = cache.getRemovedKeys();
            cache.clear();
            if (!dirtyKeys.isEmpty()) {
                for(IndexDocument ldoc : _toAdd) {
                    this.dirtyKeys.remove(ldoc.getPrimaryKey());
                }
                this.dirtyKeys.removeAll(_toRemove);
            }
        }
        if (!_toAdd.isEmpty() || !_toRemove.isEmpty()) {
            LOGGER.log(Level.FINE, "Flushing: {0}", luceneIndex.toString()); //NOI18N
            if (flushOnly && txLuceneIndex != null) {
                txLuceneIndex.txStore(
                        _toAdd,
                        _toRemove,
                        addConvertor,
                        REMOVE_CONVERTOR
                );
            } else {
                luceneIndex.store(
                        _toAdd,
                        _toRemove,
                        addConvertor,
                        REMOVE_CONVERTOR,
                        optimize);
            }
            return true;
        }
        return false;
    }

    private void commitImpl() throws IOException {
        checkRollBackNeeded();
        txLuceneIndex.commit();
    }

    private void checkRollBackNeeded() throws IOException {
        if (requiresRollBack.get()) {
            throw new IOException("Index requires rollback.");   //NOI18N
        }
    }

    @Override
    public Collection<? extends IndexDocument> query(String fieldName, String value, QueryKind kind, String... fieldsToLoad) throws IOException, InterruptedException {
        assert fieldName != null;
        assert value != null;
        assert kind != null;
        Query query = Queries.createQuery(fieldName, fieldName, value, kind);
        return query(query, org.netbeans.modules.parsing.lucene.support.Convertors.identity(), fieldsToLoad);
    }
    
    @Override
    public Collection<? extends IndexDocument> findByPrimaryKey (
            final String primaryKeyValue,
            final Queries.QueryKind kind,
            final String... fieldsToLoad) throws IOException, InterruptedException {
        return query(IndexDocumentImpl.FIELD_PRIMARY_KEY, primaryKeyValue, kind, fieldsToLoad);
    }

    @Override
    @NonNull
    public <T> Collection<? extends T> query(
            @NonNull final Query query,
            @NonNull final Convertor<? super IndexDocument, ? extends T> convertor,
            @NullAllowed final String... fieldsToLoad) throws IOException, InterruptedException {
        Parameters.notNull("query", query); //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        final Collection<T> result = new ArrayDeque<>();
        FieldSelector selector = null;
        if (fieldsToLoad != null && fieldsToLoad.length > 0) {
            final String[] fieldsWithSource = Arrays.copyOf(fieldsToLoad, fieldsToLoad.length+1);
            fieldsWithSource[fieldsToLoad.length] = IndexDocumentImpl.FIELD_PRIMARY_KEY;
            selector = Queries.createFieldSelector(fieldsWithSource);
        }
        luceneIndex.query(
            result,
            org.netbeans.modules.parsing.lucene.support.Convertors.compose(queryConvertor, convertor),
            selector,
            null,
            query);
        return result;
    }

    @Override
    public void markKeyDirty(final String primaryKey) {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, adding dirty key: {1}", new Object[]{this, primaryKey}); //NOI18N
            }
            dirtyKeys.add(primaryKey);
        }
    }

    @Override
    public void removeDirtyKeys(final Collection<? extends String> keysToRemove) {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, Removing dirty keys: {1}", new Object[]{this, keysToRemove}); //NOI18N
            }
            dirtyKeys.removeAll(keysToRemove);
        }
    }

    @Override
    public Collection<? extends String> getDirtyKeys() {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, dirty keys: {1}", new Object[]{this, dirtyKeys}); //NOI18N
            }
            return new ArrayList<>(dirtyKeys);
        }
    }
    
    
    @Override
    public String toString () {
        return "DocumentIndexImpl[%s]".formatted(luceneIndex.toString()); //NOI18N
    }

    @NonNull
    public static DocumentIndex2 create(
            @NonNull final Index index,
            @NonNull final DocumentIndexCache cache) {
        return new DocumentIndexImpl(index, cache);
    }

    @NonNull
    public static DocumentIndex2.Transactional createTransactional(
            @NonNull final Index.Transactional index,
            @NonNull final DocumentIndexCache cache) {
        return new DocumentIndexImpl.Transactional(index, cache);
    }

    private static final class Transactional extends DocumentIndexImpl implements DocumentIndex2.Transactional {

        private Transactional(
            @NonNull final Index.Transactional index,
            @NonNull final DocumentIndexCache cache) {
            super(index, cache);
        }

        @Override
        public void txStore() throws IOException {
            super.storeImpl(false, true);
        }

        @Override
        public void commit() throws IOException {
            super.commitImpl();
        }

        @Override
        public void rollback() throws IOException {
            this.requiresRollBack.set(false);
            this.txLuceneIndex.rollback();
        }

        @Override
        public void clear() throws IOException {
            this.requiresRollBack.set(false);
            this.txLuceneIndex.clear();
        }

        @Override
        public String toString () {
            return "DocumentIndex.Transactional ["+super.luceneIndex.toString()+"]";  //NOI18N
        }

    }
                    
}
