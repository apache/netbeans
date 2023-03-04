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

import java.util.Collection;
import org.apache.lucene.document.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Cache of deleted and added documents used by {@link DocumentIndex}.
 * Threading: The {@link DocumentIndex} is responsible for mutual exclusion,
 * no synchronization in this class is needed.
 * @author Tomas Zezula
 * @since 2.18.0
 */
public interface DocumentIndexCache {

    /**
     * Adds a document into document cache.
     * @param document the document to be added
     * @return true if the cache is full and should be
     * flushed.
     */
    boolean addDocument(@NonNull IndexDocument document);

    /**
     * Adds a primary key of document(s) to delete set.
     * @param primaryKey the primary key of document(s) which should be removed.
     * @return true if the cache is full and should be
     * flushed.
     */
    boolean removeDocument(@NonNull String primaryKey);

    /**
     * Clears the cache content.
     */
    void clear();

    /**
     * Returns a {@link Collection} of primary keys of documents
     * to be removed.
     * @return iterator
     */
    @NonNull
    Collection<? extends String> getRemovedKeys();

    /**
     * Returns a {@link Collection} of added documents.
     * @return iterator
     */
    @NonNull
    Collection<? extends IndexDocument> getAddedDocuments();


    /**
     * Cache which allows custom {@link IndexDocument}s implementations.
     * @since 2.22
     */
    interface WithCustomIndexDocument extends DocumentIndexCache {
        /**
         * Creates a {@link Convertor} from custom {@link IndexDocument} implementation.
         * The returned {@link Convertor} translates the custom {@link IndexDocument}s
         * returned by the {@link DocumentIndexCache#getAddedDocuments()} to {@link Document}s.
         * @return the {@link Convertor} or null if a default convertor, converting from
         * {@link IndexDocument}s created by {@link IndexManager#createDocument}, should be used.
         */
        @CheckForNull
        Convertor<IndexDocument, Document> createAddConvertor();

        /**
         * Creates a {@link Convertor} to custom {@link IndexDocument} implementation.
         * The returned {@link Convertor} translates the {@link Document}s
         * created by the {@link Index#query} to custom {@link IndexDocument}s.
         * @return the {@link Convertor} or null if a default convertor, converting to
         * {@link IndexDocument}s created by {@link IndexManager#createDocument}, should be used.
         */
        @CheckForNull
        Convertor<Document, IndexDocument> createQueryConvertor();
    }

}
