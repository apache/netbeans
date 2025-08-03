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

package org.netbeans.modules.parsing.lucene.support;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Lucene based index supporting queries and stores. The index instances
 * do resource management (memory cache of used indexes, LRU cache of open
 * file handles). The user is responsible for closing the index when it's no
 * more used to keep the index consistent.
 * @author Tomas Zezula
 */
public interface Index {

    /**
     * Index status returned by {@link Index#getStatus(boolean)} method
     * @since 2.1
     */
    enum Status {
        /**
         * Index does not exist
         */
        EMPTY,
        /**
         * Index is broken
         */
        INVALID,
        /**
         * Index is valid
         */
        VALID, 
        /**
         * The index is being written to
         * @since 2.7
         */
        WRITING;
    }
    
    /**
     * An exception thrown by {@link Index} when operation is called on
     * a closed index.
     */
    public static final class IndexClosedException extends IOException {        
    }
        
    
    /**
     * Checks the validity of the index. The index is invalid when it's broken.
     * @param tryOpen when true the {@link Index} does exact but more expensive check.
     * @return {@link Status#INVALID} when the index is broken, {@link Status#EMPTY}
     * when the index does not exist or {@link  Status#VALID} if the index is valid
     * @throws IOException in case of IO problem
     * @since 2.1
     */
    Status getStatus (boolean tryOpen) throws IOException;
    
    /**
     * Queries the {@link Index} by given queries.
     * @param result the {@link Collection} to store query results into
     * @param convertor the {@link Convertor} used to convert lucene documents into the user objects added into the result
     * @param selector the selector used to select document's fields which should be loaded, if null all fields are loaded
     * @param cancel the {@link AtomicBoolean} used to cancel the index iteration by the caller. When set to true the iteration
     * is stopped.
     * @param queries the queries to be performed on the {@link Index}
     * @throws IOException in case of IO problem
     * @throws InterruptedException when query was canceled
     */
    <T> void query (Collection<? super T> result, @NonNull Convertor<? super Document, T> convertor, @NullAllowed FieldSelector selector, @NullAllowed AtomicBoolean cancel, @NonNull Query... queries) throws IOException, InterruptedException;
    
    /**
     * Queries the {@link Index} by given queries. In addition to documents it also collects the terms which matched the queries.
     * @param result the {@link Collection} to store query results into
     * @param convertor the {@link Convertor} used to convert lucene documents into the user objects added into the result
     * @param termConvertor the {@link Convertor} used to convert lucene terms into the user objects added into the result
     * @param selector the selector used to select document's fields which should be loaded, if null all fields are loaded
     * @param cancel the {@link AtomicBoolean} used to cancel the index iteration by the caller. When set to true the iteration
     * is stopped.
     * @param queries the queries to be performed on the {@link Index}
     * @throws IOException in case of IO problem
     * @throws InterruptedException when query was canceled
     */
    <S, T> void queryDocTerms(Map<? super T, Set<S>> result, @NonNull Convertor<? super Document, T> convertor, @NonNull Convertor<? super Term, S> termConvertor,@NullAllowed FieldSelector selector, @NullAllowed AtomicBoolean cancel, @NonNull Query... queries) throws IOException, InterruptedException;
    
    /**
     * Queries the {@link Index}'s b-tree for terms starting by the start term and accepted by the filter.
     * @param result the {@link Collection} to store results into
     * @param start the first term to start the b-tree iteration with, if null the iteration start on the first term.
     * @param filter converting the terms into the user objects which are added into the result or null to skeep them.
     * The filter can stop the iteration by throwing the {@link StoppableConvertor.Stop}.
     * @param cancel the {@link AtomicBoolean} used to cancel the index iteration by the caller. When set to true the iteration
     * is stopped.
     * @throws IOException in case of IO problem
     * @throws InterruptedException when query was canceled
     */
    <T> void queryTerms(@NonNull Collection<? super T> result, @NullAllowed Term start, @NonNull StoppableConvertor<Term,T> filter, @NullAllowed AtomicBoolean cancel) throws  IOException, InterruptedException;
    
    /**
     * Updates the {@link Index} by adding the toAdd objects and deleting toDelete objects.
     * @param toAdd the objects to be added into the index
     * @param toDelete the objects to be removed from the index
     * @param docConvertor the {@link Convertor} used to convert toAdd objects into lucene's Documents which are stored into the {@link Index}
     * @param queryConvertor the {@link Convertor} used to convert toDelete objects into lucene's Queries used to delete the documents from {@link Index}
     * @param optimize if true the index is optimized. The optimized index is faster for queries but optimize takes significant time.
     * @throws IOException in case of IO problem
     */
    <S, T> void store (@NonNull Collection<T> toAdd, @NonNull Collection<S> toDelete, @NonNull Convertor<? super T, ? extends Document> docConvertor, @NonNull Convertor<? super S, ? extends Query> queryConvertor, boolean optimize) throws IOException;
    
    /**
     * Completely deletes the {@link Index}
     * @throws IOException in case of IO problem
     */
    void clear () throws IOException;
    
    /**
     * Closes the {@link Index}
     * @throws IOException in case of IO problem
     */
    void close () throws IOException;
    
    /**
     * An extension to the basic Index interface, which allows to batch writes into
     * transactions. IndexReaders do not see content written by {@link #txStore}
     * until the transaction is {@link #commit}ted. Note that calling {@link #close}
     * without fist commit will <b>rollback</b> the opened transaction.
     * <p/>
     * A transaction is started implicitly  by a call to {@link #txStore}, and committed
     * or rolled back by {@link #commit} or {@link #rollback} methods.
     * @since 2.7
     */
    public interface Transactional extends Index {
        /**
         * Stores new data and/or deletes old one, just as {@link #store}, but does not
         * expose the written data to readers. You must call {@link #commit} to finish the 
         * transaction and make the data visible.
         * 
         * @param toAdd the objects to be added into the index
         * @param toDelete the objects to be removed from the index
         * @param docConvertor the {@link Convertor} used to convert toAdd objects into lucene's Documents which are stored into the {@link Index}
         * @param queryConvertor the {@link Convertor} used to convert toDelete objects into lucene's Queries used to delete the documents from {@link Index}
         * @throws IOException in case of IO problem
         * @see #store
         */
        <S, T> void txStore(
                @NonNull Collection<T> toAdd, 
                @NonNull Collection<S> toDelete, 
                @NonNull Convertor<? super T, ? extends Document> docConvertor, 
                @NonNull Convertor<? super S, ? extends Query> queryConvertor) throws IOException;

        /**
         * Commits the data written by txStore; no op, if a transaction is not opened
         * 
         * @throws IOException in case of I/O error during commit
         */
        void commit() throws IOException;
        
        /**
         * Rolls back the transaction, frees associated resources. No op if a transaction is not opened
         */
        void rollback() throws IOException;
    }
    
    /**
     * Index providing term frequencies.
     * @since 2.13
     */
    public interface WithTermFrequencies extends Index {

        /**
         * Term and frequency pair.
         * @since 2.13
         */
        public final class TermFreq {
            private int freq;
            private Term term;
            
            TermFreq() {}
                        
            void setTerm(@NonNull final Term term) {
                this.term = term;
            }
            
            void setFreq(final int freq) {
                this.freq = freq;
            }
            
            /**
             * Returns the {@link Term}.
             * @return the term.
             */
            @NonNull
            public Term getTerm() {
                return term;
            }
            
            /*
             * Returns the {@link Term} frequency estimate.
             * @return the term frequency estimate.
             */
            public int getFreq() {
                return freq;
            }
        }
        
        
        /**
         * Queries the {@link Index}'s b-tree for terms and frequencies estimate starting by the start term and accepted by the filter.
         * @param result the {@link Collection} to store results into
         * @param start the first term to start the b-tree iteration with, if null the iteration start on the first term.
         * @param filter converting the terms into the user objects which are added into the result or null to skeep them.
         * The filter can stop the iteration by throwing the {@link StoppableConvertor.Stop}.
         * @param cancel the {@link AtomicBoolean} used to cancel the index iteration by the caller. When set to true the iteration
         * is stopped.
         * @throws IOException in case of IO problem
         * @throws InterruptedException when query was canceled
         */
        <T> void queryTermFrequencies(
                @NonNull Collection<? super T> result,
                @NullAllowed Term start,
                @NonNull StoppableConvertor<TermFreq,T> filter,
                @NullAllowed AtomicBoolean cancel) throws  IOException, InterruptedException;
    }
}
