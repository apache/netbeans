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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
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
    private ByteBuffersDirectory dir;
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
            @NullAllowed Set<String> selector,
            @NullAllowed AtomicBoolean cancel,
            @NonNull Query... queries) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);   //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        Parameters.notNull("result", result);       //NOI18N   

        lock.readLock().lock();
        try {
            IndexReader in = getReader();
            if (in == null) {
                return;
            }
            IndexSearcher searcher = new IndexSearcher(in);
            BitSet bs = new BitSet(in.maxDoc());
            Collector c = new BitSetCollector(bs);
            for (Query q : queries) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException();
                }
                searcher.search(q, c);
            }
            for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                Document doc = in.storedFields().document(docNum, selector);
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
            @NonNull Convertor<? super BytesRef, S> termConvertor,
            @NullAllowed Set<String> selector,
            @NullAllowed AtomicBoolean cancel,
            @NonNull Query... queries) throws IOException, InterruptedException {
        Parameters.notNull("result", result);   //NOI18N
        Parameters.notNull("convertor", convertor);   //NOI18N
        Parameters.notNull("termConvertor", termConvertor); //NOI18N
        Parameters.notNull("queries", queries);   //NOI18N

        lock.readLock().lock();
        try (IndexReader in = getReader()) {
            if (in == null) {
                return;
            }
            for (LeafReaderContext lrc : in.leaves()) {
                LeafReader lr = lrc.reader();
                Map<Integer,Set<BytesRef>> docTermMap = new HashMap<>();
                for (Query q : queries) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException();
                    }
                    switch (q) {
                        case TermQuery tq -> {
                            Terms terms = lr.terms(tq.getTerm().field());
                            if (terms != null) {
                                TermsEnum te = terms.iterator();
                                if (te.seekExact(tq.getTerm().bytes())) {
                                    PostingsEnum pe = te.postings(null);
                                    for (int doc = pe.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = pe.nextDoc()) {
                                        docTermMap.computeIfAbsent(doc, s -> new HashSet<>())
                                                .add(tq.getTerm().bytes());
                                    }
                                }
                            }
                        }
                        case PrefixQuery pq -> {
                            Terms terms = lr.terms(pq.getField());
                            if (terms != null) {
                                TermsEnum te = pq.getCompiled().getTermsEnum(terms);
                                for (BytesRef termValue = te.next(); termValue != null; termValue = te.next()) {
                                    PostingsEnum pe = te.postings(null);
                                    for (int doc = pe.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = pe.nextDoc()) {
                                        docTermMap.computeIfAbsent(doc, s -> new HashSet<>())
                                                .add(termValue);
                                    }
                                }
                            }
                        }
                        case RegexpFilter rf -> {
                            Terms terms = lr.terms(rf.getField());
                            if (terms != null) {
                                TermsEnum te = rf.getTermsEnum(terms);
                                for (BytesRef termValue = te.next(); termValue != null; termValue = te.next()) {
                                    PostingsEnum pe = te.postings(null);
                                    for (int doc = pe.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = pe.nextDoc()) {
                                        docTermMap.computeIfAbsent(doc, s -> new HashSet<>())
                                                .add(termValue);
                                    }
                                }
                            }
                        }
                        default -> throw new IllegalArgumentException(
                                "Query: %s does not implement TermCollecting".formatted(q.getClass().getName())); //NOI18N
                    }
                }

                for (Map.Entry<Integer,Set<BytesRef>> docNum: docTermMap.entrySet()) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException();
                    }
                    Document doc = lr.storedFields().document(docNum.getKey(), selector);
                    T value = convertor.convert(doc);
                    if (value != null) {
                        final Set<BytesRef> terms = docNum.getValue();
                        if (terms != null) {
                            result.put(value, convertTerms(termConvertor, terms));
                        }
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
            @NonNull String field,
            @NullAllowed String startValue,
            @NonNull StoppableConvertor<BytesRef, T> filter,
            @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        Parameters.notNull("result", result);   //NOI18N
        Parameters.notNull("filter", filter); //NOI18N

        BytesRef startBytesRef;

        if (startValue == null) {
            startBytesRef = new BytesRef("");
        } else {
            startBytesRef = new BytesRef(startValue);
        }

        lock.readLock().lock();
        try {
            IndexReader in = getReader();
            if (in == null) {
                return;
            }
            for(LeafReaderContext lrc: in.leaves()) {
                TermsEnum te = lrc.reader().terms(field).iterator();
                if (te.seekCeil(startBytesRef) != TermsEnum.SeekStatus.END) {
                    do {
                        if (cancel != null && cancel.get()) {
                            throw new InterruptedException();
                        }
                        T vote = filter.convert(te.term());
                        if (vote != null) {
                            result.add(vote);
                        }
                    } while (te.next() != null);
                }
            }
        } catch (StoppableConvertor.Stop stop) {
            //Stop iteration of TermEnum
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
            cachedReader = DirectoryReader.open(getDirectory());
        }
        return cachedReader;
    }
    
    private synchronized void refreshReader() throws IOException {
        assert lock.isWriteLockedByCurrentThread();
        if (cachedReader != null) {
            IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) cachedReader);
            if (newReader != null) {
                cachedReader.close();
                cachedReader = newReader;
            }
        }
    }

    private synchronized IndexWriter getWriter() throws IOException {
        IndexWriterConfig conf = new IndexWriterConfig(new LimitTokenCountAnalyzer(analyzer, 10_000));
        return new IndexWriter (getDirectory(), conf);
    }
    
    private synchronized Directory getDirectory() {
        if (dir == null) {
            dir = new ByteBuffersDirectory();
        }
        return dir;
    }
    
    private static <T> Set<T> convertTerms(final Convertor<? super BytesRef, T> convertor, final Set<? extends BytesRef> terms) {
        Set<T> result = new HashSet<>(terms.size());
        for (BytesRef term : terms) {
            result.add(convertor.convert(term));
        }
        return result;
    }
    
}
