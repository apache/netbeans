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
package org.netbeans.modules.parsing.lucene;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class MemoryIndex implements Index {
    
    private final Analyzer analyzer;
    private final ReentrantReadWriteLock lock;
    //@GuardedBy("this")
    private RAMDirectory dir;
    //@GuardedBy("this")
    private IndexReader cachedReader;
    
    
    private MemoryIndex(@NonNull final Analyzer analyzer) {
        assert analyzer != null;
        this.analyzer = analyzer;
        this.lock = new ReentrantReadWriteLock();
    }
    
    @NonNull
    static MemoryIndex create(@NonNull Analyzer analyzer) {
        return new MemoryIndex(analyzer);
    }

    @NonNull
    @Override
    public Status getStatus(boolean tryOpen) throws IOException {
        return Status.VALID;
    }

    @Override
    public <T> void query(
            @NonNull Collection<? super T> result,
            @NonNull Convertor<? super Document, T> convertor,
            @NullAllowed FieldSelector selector,
            @NullAllowed AtomicBoolean cancel,
            @NonNull Query... queries) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);   //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        Parameters.notNull("result", result);       //NOI18N   
        
        if (selector == null) {
            selector = AllFieldsSelector.INSTANCE;
        }
        
        lock.readLock().lock();
        try {
            final IndexReader in = getReader();
            if (in == null) {
                return;
            }
            final BitSet bs = new BitSet(in.maxDoc());
            final Collector c = new BitSetCollector(bs);
            final Searcher searcher = new IndexSearcher(in);
            try {
                for (Query q : queries) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    searcher.search(q, c);
                }
            } finally {
                searcher.close();
            }        
            for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                final Document doc = in.document(docNum, selector);
                final T value = convertor.convert(doc);
                if (value != null) {
                    result.add (value);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <S, T> void queryDocTerms(
            @NonNull Map<? super T, Set<S>> result,
            @NonNull Convertor<? super Document, T> convertor,
            @NonNull Convertor<? super Term, S> termConvertor,
            @NullAllowed FieldSelector selector,
            @NullAllowed AtomicBoolean cancel,
            @NonNull Query... queries) throws IOException, InterruptedException {
        Parameters.notNull("result", result);   //NOI18N
        Parameters.notNull("convertor", convertor);   //NOI18N
        Parameters.notNull("termConvertor", termConvertor); //NOI18N
        Parameters.notNull("queries", queries);   //NOI18N
        
        
        if (selector == null) {
            selector = AllFieldsSelector.INSTANCE;
        }

        lock.readLock().lock();
        try {
            final IndexReader in = getReader();
            if (in == null) {
                return;
            }
            final BitSet bs = new BitSet(in.maxDoc());
            final Collector c = new BitSetCollector(bs);
            final Searcher searcher = new IndexSearcher(in);
            final TermCollector termCollector = new TermCollector(c);
            try {
                for (Query q : queries) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    if (q instanceof TermCollector.TermCollecting) {
                        ((TermCollector.TermCollecting)q).attach(termCollector);
                    } else {
                        throw new IllegalArgumentException (
                                String.format("Query: %s does not implement TermCollecting",    //NOI18N
                                q.getClass().getName()));
                    }
                    searcher.search(q, termCollector);
                }
            } finally {
                searcher.close();
            }

            for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                final Document doc = in.document(docNum, selector);
                final T value = convertor.convert(doc);
                if (value != null) {
                    final Set<Term> terms = termCollector.get(docNum);
                    if (terms != null) {
                        result.put (value, convertTerms(termConvertor, terms));
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <T> void queryTerms(
            @NonNull Collection<? super T> result,
            @NullAllowed Term start,
            @NonNull StoppableConvertor<Term, T> filter,
            @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        Parameters.notNull("result", result);   //NOI18N
        Parameters.notNull("filter", filter); //NOI18N
        
        lock.readLock().lock();
        try {
            final IndexReader in = getReader();
            if (in == null) {
                return;
            }
            final TermEnum terms = start == null ? in.terms () : in.terms (start);
            try {
                do {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    final Term currentTerm = terms.term();
                    if (currentTerm != null) {                    
                        final T vote = filter.convert(currentTerm);
                        if (vote != null) {
                            result.add(vote);
                        }
                    }
                } while (terms.next());
            } catch (StoppableConvertor.Stop stop) {
                //Stop iteration of TermEnum
            } finally {
                terms.close();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <S, T> void store(Collection<T> toAdd, Collection<S> toDelete, Convertor<? super T, ? extends Document> docConvertor, Convertor<? super S, ? extends Query> queryConvertor, boolean optimize) throws IOException {
        lock.writeLock().lock();
        try {
            final IndexWriter out = getWriter();
            try {
                for (S td : toDelete) {
                    out.deleteDocuments(queryConvertor.convert(td));
                }
                if (toAdd.isEmpty()) {
                    return;
                }
                for (Iterator<T> it = toAdd.iterator(); it.hasNext();) {
                    T entry = it.next();
                    it.remove();
                    final Document doc = docConvertor.convert(entry);
                    out.addDocument(doc);
                }
            } finally {

                try {
                    out.close();
                } finally {
                    refreshReader();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() throws IOException {
        lock.writeLock().lock();
        close();
        try {
            synchronized (MemoryIndex.this) {
                if (dir != null) {
                    dir.close();
                    dir = null;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void close() throws IOException {
        lock.writeLock().lock();
        try {
            synchronized (MemoryIndex.this) {
                if (cachedReader != null) {
                    cachedReader.close();
                    cachedReader = null;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @CheckForNull
    private synchronized IndexReader getReader() throws IOException {
        if (cachedReader == null) {
            try {
                cachedReader = IndexReader.open(getDirectory(),true);
            } catch (FileNotFoundException fnf) {
                //pass - returns null
            }
        }
        return cachedReader;
    }
    
    private synchronized void refreshReader() throws IOException {
        assert lock.isWriteLockedByCurrentThread();
        if (cachedReader != null) {
            final IndexReader newReader = cachedReader.reopen();
            if (newReader != cachedReader) {
                cachedReader.close();
                cachedReader = newReader;
            }
        }
    }
    
    private synchronized IndexWriter getWriter() throws IOException {
        return new IndexWriter (getDirectory(), analyzer, IndexWriter.MaxFieldLength.LIMITED);
    }
    
    private synchronized Directory getDirectory() {
        if (dir == null) {
            dir = new RAMDirectory();
        }
        return dir;
    }
    
    private static <T> Set<T> convertTerms(final Convertor<? super Term, T> convertor, final Set<? extends Term> terms) {
        final Set<T> result = new HashSet<T>(terms.size());
        for (Term term : terms) {
            result.add(convertor.convert(term));
        }
        return result;
    }
    
}
