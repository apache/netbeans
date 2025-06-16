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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
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
            IndexReader in = getReader();
            if (in == null) {
                return;
            }
            BitSet bs = new BitSet(in.maxDoc());
            Collector c = new BitSetCollector(bs);
            try (IndexSearcher searcher = new IndexSearcher(in)) {
                for (Query q : queries) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    searcher.search(q, c);
                }
            }        
            for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                Document doc = in.document(docNum, selector);
                T value = convertor.convert(doc);
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
            IndexReader in = getReader();
            if (in == null) {
                return;
            }
            BitSet bs = new BitSet(in.maxDoc());
            Collector c = new BitSetCollector(bs);
            TermCollector termCollector = new TermCollector(c);
            try (IndexSearcher searcher = new IndexSearcher(in)) {
                for (Query q : queries) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    if (q instanceof TermCollector.TermCollecting termCollecting) {
                        termCollecting.attach(termCollector);
                    } else {
                        throw new IllegalArgumentException (
                                "Query: %s does not implement TermCollecting".formatted(q.getClass().getName())); //NOI18N
                    }
                    searcher.search(q, termCollector);
                }
            }

            for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                Document doc = in.document(docNum, selector);
                T value = convertor.convert(doc);
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
            IndexReader in = getReader();
            if (in == null) {
                return;
            }
            try (TermEnum terms = start == null ? in.terms() : in.terms(start)) {
                do {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    Term currentTerm = terms.term();
                    if (currentTerm != null) {                    
                        T vote = filter.convert(currentTerm);
                        if (vote != null) {
                            result.add(vote);
                        }
                    }
                } while (terms.next());
            } catch (StoppableConvertor.Stop stop) {
                //Stop iteration of TermEnum
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <S, T> void store(Collection<T> toAdd, Collection<S> toDelete, Convertor<? super T, ? extends Document> docConvertor, Convertor<? super S, ? extends Query> queryConvertor, boolean optimize) throws IOException {
        lock.writeLock().lock();
        try {
            try (IndexWriter out = getWriter()) {
                for (S td : toDelete) {
                    out.deleteDocuments(queryConvertor.convert(td));
                }
                if (toAdd.isEmpty()) {
                    return;
                }
                for (Iterator<T> it = toAdd.iterator(); it.hasNext();) {
                    T entry = it.next();
                    it.remove();
                    Document doc = docConvertor.convert(entry);
                    out.addDocument(doc);
                }
            } finally {
                refreshReader();
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
                cachedReader = IndexReader.open(getDirectory());
            } catch (FileNotFoundException fnf) {
                //pass - returns null
            }
        }
        return cachedReader;
    }
    
    private synchronized void refreshReader() throws IOException {
        assert lock.isWriteLockedByCurrentThread();
        if (cachedReader != null) {
            IndexReader newReader = IndexReader.openIfChanged(cachedReader);
            if (newReader != null) {
                cachedReader.close();
                cachedReader = newReader;
            }
        }
    }

    private synchronized IndexWriter getWriter() throws IOException {
        IndexWriterConfig conf = new IndexWriterConfig(
                Version.LUCENE_36,
                new LimitTokenCountAnalyzer(analyzer, IndexWriter.DEFAULT_MAX_FIELD_LENGTH)
        );
        return new IndexWriter (getDirectory(), conf);
    }
    
    private synchronized Directory getDirectory() {
        if (dir == null) {
            dir = new RAMDirectory();
        }
        return dir;
    }
    
    private static <T> Set<T> convertTerms(final Convertor<? super Term, T> convertor, final Set<? extends Term> terms) {
        Set<T> result = new HashSet<>(terms.size());
        for (Term term : terms) {
            result.add(convertor.convert(term));
        }
        return result;
    }
    
}
