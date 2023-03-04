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

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Document based index provides a higher level api than {@link Index}
 * It's document oriented. It supports adding, removing and searching of {@link IndexDocument}
 * @since 1.1
 * @author Tomas Zezula
 */
public interface DocumentIndex {
    
    /**
     * Adds a document into the index.
     * The document may not be added persistently until {@link DocumentIndex#store(boolean)} is called
     * @param document to be added
     */
    void addDocument (@NonNull IndexDocument document);        

    /**
     * Removes a document associated with given primary key from the index
     * @param primaryKey the primary key of the document which should be removed
     */
    void removeDocument (@NonNull String primaryKey);

    /**
     * Checks the validity of the index, see {@link Index#getStatus(boolean)} for details
     * @return {@link Status#INVALID} when the index is broken, {@link Status#EMPTY}
     * when the index does not exist or {@link  Status#VALID} if the index is valid
     * @throws IOException in case of IO error
     * @since 2.1
     */
    public Index.Status getStatus() throws IOException;
    
    /**
     * Closes the index.
     * @throws IOException in case of IO error
     */
    public void close() throws IOException;

    /**
     * Stores changes done on the index.
     * @param optimize if true Lucene optimizes the index. The optimized index is
     * faster but the optimization takes some time. In general small updates should
     * not be optimized.
     * @throws IOException in case of IO error
     */
    public void store (boolean optimize) throws IOException;

    /**
     * Performs a search on the index.
     * @param fieldName the name of the field to be searched
     * @param value of the field to be searched
     * @param kind of the query, see {@link Queries.QueryKind} for details
     * @param fieldsToLoad names of the field which should be loaded into the document.
     * Loading only needed fields speeds up the search. If null or empty all fields are loaded.
     * @return The collection of found documents
     * @throws IOException in case of IO error
     * @throws InterruptedException  when the search was interrupted
     */
    public @NonNull Collection<? extends IndexDocument> query (
            @NonNull String fieldName,
            @NonNull String value,
            @NonNull Queries.QueryKind kind,
            @NullAllowed String... fieldsToLoad) throws IOException, InterruptedException;
    
    
    /**
     * Performs a search on the index using primary key
     * @param value of the primary key
     * @param kind of the query, see {@link Queries.QueryKind} for details
     * @param fieldsToLoad names of the field which should be loaded into the document.
     * Loading only needed fields speeds up the search. If null or empty all fields are loaded.
     * @return The collection of found documents
     * @throws IOException in case of IO error
     * @throws InterruptedException  when the search was interrupted
     */
    public @NonNull Collection<? extends IndexDocument> findByPrimaryKey (
            @NonNull String primaryKeyValue,
            @NonNull Queries.QueryKind kind,
            @NullAllowed String... fieldsToLoad) throws IOException, InterruptedException;

    /**
     * Marks the primaryKey as dirty. Can be used by client to detect non up to date documents.
     * The dirty keys are cleaned during the save.
     * @param primaryKey of document which should be marked as dirty
     */
    public void markKeyDirty(@NonNull String primaryKey);
    
    /**
     * Cleans the dirty flag for Documents represented by given primary keys.
     * See {@link DocumentIndex#markKeyDirty}
     * @param dirtyKeys the primary keys to be un marked as dirty
     */
    public void removeDirtyKeys(@NonNull Collection<? extends String> dirtyKeys);

    /**
     * Returns the primary keys of dirty documents.
     * See {@link DocumentIndex#markKeyDirty}
     * @return the primary keys of dirty documents, never returns null
     */
    public @NonNull Collection<? extends String> getDirtyKeys();

    /**
     * Transactional document index.
     * @since 2.19
     */
    public interface  Transactional extends DocumentIndex {

        /**
         * Stores new data and/or deletes old one, just as {@link #store}, but does not
         * expose the written data to readers. You must call {@link #commit} to finish the
         * transaction and make the data visible.
         * @throws IOException in case of IO problem
         * @see #store
         */
        void txStore() throws IOException;

        /**
         * Commits the data written by txStore; no op, if a transaction is not opened
         * @throws IOException in case of I/O error during commit
         */
        void commit() throws IOException;

        /**
         * Rolls back changes.
         * @throws IOException in case of I/O error during rollback
         */
        void rollback() throws IOException;

        /**
         * Completely deletes the underlying {@link Index}
         * @throws IOException in case of IO problem
         * @since 2.20
         */
         void clear () throws IOException;
    }

}
