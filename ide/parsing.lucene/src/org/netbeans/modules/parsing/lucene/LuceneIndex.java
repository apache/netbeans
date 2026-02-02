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

import org.apache.lucene.store.LockObtainFailedException;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexReaderInjection;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Note - there can be only a single IndexWriter at a time for the dir index. For consistency, the Writer is
 * kept in a thread-local variable until it is committed. Lucene will throw an exception if another Writer creation
 * attempt is done (by another thread, presumably). 
 * <p/>
 * It should be thread-safe (according to Lucene docs) to use an IndexWriter while Readers are opened.
 * <p/>
 * As Reader and Writers can be used in parallel, all query+store operations use readLock so they can run in parallel.
 * Operations which affect the whole index (close, clear) use write lock. RefreshReader called internally from writer's commit (close)
 * is incompatible with parallel reads, as it closes the old reader - uses writeLock.
 * <p/>
 * Locks must be acquired in the order [rwLock, LuceneIndex]. The do* method synchronize on the DirCache instance and must be called 
 * if the code already holds rwLock.
 *
 * @author Tomas Zezula
 */
//@NotTreadSafe
public class LuceneIndex implements Index.Transactional, Index.WithTermFrequencies, Runnable {

    private static final String PROP_INDEX_POLICY = "java.index.useMemCache";   //NOI18N
    private static final String PROP_DIR_TYPE = "java.index.dir";       //NOI18N
    private static final String DIR_TYPE_MMAP = "mmap";                 //NOI18N
    private static final String DIR_TYPE_NIO = "nio";                   //NOI18N
    private static final String DIR_TYPE_IO = "io";                     //NOI18N
    private static final CachePolicy DEFAULT_CACHE_POLICY = CachePolicy.DYNAMIC;
    private static final CachePolicy cachePolicy = getCachePolicy();
    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());

    private final DirCache dirCache;


    public static LuceneIndex create (final File cacheRoot, final Analyzer analyzer) throws IOException {
        return new LuceneIndex (cacheRoot, analyzer);
    }

    static boolean awaitPendingEvictors() throws InterruptedException {
        try {
            return DirCache.EVICTOR_RP.submit(() -> {}, Boolean.TRUE).get();
        } catch (ExecutionException e) {
            return false;
        }
    }

    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final File refCacheRoot, final Analyzer analyzer) throws IOException {
        assert refCacheRoot != null;
        assert analyzer != null;
        this.dirCache = new DirCache(
                refCacheRoot,
                cachePolicy,
                analyzer);
    }
    
    @Override
    public <T> void query (
            final @NonNull Collection<? super T> result,
            final @NonNull Convertor<? super Document, T> convertor,
            @NullAllowed Set<String> selector,
            final @NullAllowed AtomicBoolean cancel,
            final @NonNull Query... queries
            ) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);   //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        Parameters.notNull("result", result);       //NOI18N   
        
        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                LOGGER.log(Level.FINE, "{0} is invalid!", this);
                return;
            }
            IndexSearcher searcher = new IndexSearcher(in);
            final BitSet bs = new BitSet(in.maxDoc());
            final Collector c = new BitSetCollector(bs);
            for (Query q : queries) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                searcher.search(q, c);
            }
            changeIndexReader(convertor, in);
            try {
                for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
                    if (cancel != null && cancel.get()) {
                        throw new InterruptedException ();
                    }
                    final Document doc = in.storedFields().document(docNum, selector);
                    final T value = convertor.convert(doc);
                    if (value != null) {
                        result.add (value);
                    }
                }
            } finally {
                changeIndexReader(convertor, null);
            }
        } finally {
            dirCache.releaseReader(in);
        }
    }

    private static void changeIndexReader(Object convertor, IndexReader in) {
        if (convertor instanceof IndexReaderInjection iri) {
            iri.setIndexReader(in);
        }
    }

    @Override
    public <T> void queryTerms(
            final @NonNull Collection<? super T> result,
            final @NonNull String field,
            final @NullAllowed String seekTo,
            final @NonNull StoppableConvertor<BytesRef,T> filter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        queryTermsImpl(result, field, seekTo, Convertors.newTermEnumToTermConvertor(filter), cancel);
    }
    
    @Override
    public <T> void queryTermFrequencies(
            final @NonNull Collection<? super T> result,
            final @NonNull String field,
            final @NullAllowed String seekTo,
            final @NonNull StoppableConvertor<Index.WithTermFrequencies.TermFreq,T> filter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        queryTermsImpl(result, field, seekTo, Convertors.newTermEnumToFreqConvertor(filter), cancel);
    }
    
    //where
    private <T> void queryTermsImpl(
            final @NonNull Collection<? super T> result,
            final @NonNull String field,
            @NullAllowed String startValue,
            final @NonNull StoppableConvertor<TermsEnum,T> adapter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {

        BytesRef startBytesRef;

        if (startValue == null) {
            startBytesRef = new BytesRef("");
        } else {
            startBytesRef = new BytesRef(startValue);
        }

        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                LOGGER.log(Level.FINE, "{0} is invalid!", this);
                return;
            }

            changeIndexReader(adapter, in);
            try {
                for(LeafReaderContext lrc: in.leaves()) {
                    TermsEnum te = lrc.reader().terms(field).iterator();
                    if (te.seekCeil(startBytesRef) != TermsEnum.SeekStatus.END) {
                        do {
                            if (cancel != null && cancel.get()) {
                                throw new InterruptedException();
                            }
                            final T vote = adapter.convert(te);
                            if (vote != null) {
                                result.add(vote);
                            }
                        } while (te.next() != null);
                    }
                }
            } catch (StoppableConvertor.Stop stop) {
                //Stop iteration of TermEnum
            } finally {
                changeIndexReader(adapter, null);
            }
        } finally {
            dirCache.releaseReader(in);
        }
    }
    
    @Override
    public <S, T> void queryDocTerms(
            final @NonNull Map<? super T, Set<S>> result,
            final @NonNull Convertor<? super Document, T> convertor,
            final @NonNull Convertor<? super BytesRef, S> termConvertor,
            @NullAllowed Set<String> selector,
            final @NullAllowed AtomicBoolean cancel,
            final @NonNull Query... queries) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);             //NOI18N
        Parameters.notNull("slector", selector);            //NOI18N
        Parameters.notNull("convertor", convertor);         //NOI18N
        Parameters.notNull("termConvertor", termConvertor); //NOI18N
        Parameters.notNull("result", result);               //NOI18N

        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                LOGGER.log(Level.FINE, "{0} is invalid!", this);
                return;
            }
            for (LeafReaderContext lrc : in.leaves()) {
                final LeafReader lr = lrc.reader();
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

                boolean logged = false;
                changeIndexReader(convertor, in);
                try {
                    changeIndexReader(termConvertor, in);
                    try {
                        for (Entry<Integer,Set<BytesRef>> docNum: docTermMap.entrySet()) {
                            if (cancel != null && cancel.get()) {
                                throw new InterruptedException();
                            }
                            final Document doc = lr.storedFields().document(docNum.getKey(), selector);
                            final T value = convertor.convert(doc);
                            if (value != null) {
                                final Set<BytesRef> terms = docNum.getValue();
                                if (terms != null) {
                                    result.put(value, convertTerms(termConvertor, terms));
                                } else {
                                    if (!logged) {
                                        LOGGER.log(Level.WARNING, "Index info [maxDoc: {0} numDoc: {1} docs: {2}]",
                                                new Object[] {
                                                    in.maxDoc(),
                                                    in.numDocs(),
                                                    docNum.getValue()
                                                });
                                        logged = true;
                                    }
                                    LOGGER.log(Level.WARNING, "No terms found for doc: {0}", docNum);
                                }
                            }
                        }
                    } finally {
                        changeIndexReader(termConvertor, null);
                    }
                } finally {
                    changeIndexReader(convertor, null);
                }
            }
        } finally {
            dirCache.releaseReader(in);
        }
    }
    
    private static <T> Set<T> convertTerms(final Convertor<? super BytesRef, T> convertor, final Set<? extends BytesRef> terms) {
        final Set<T> result = new HashSet<T>(terms.size());
        for (BytesRef term : terms) {
            result.add(convertor.convert(term));
        }
        return result;
    }

    @Override
    public void run() {
        dirCache.beginTrans();
    }

    @Override
    public void commit() throws IOException {
        dirCache.closeTxWriter();
    }

    @Override
    public void rollback() throws IOException {
        dirCache.rollbackTxWriter();
    }

    @Override
    public <S, T> void txStore(
            final Collection<T> toAdd, 
            final Collection<S> toDelete, final Convertor<? super T, ? extends Document> docConvertor, 
            final Convertor<? super S, ? extends Query> queryConvertor) throws IOException {
        
        final IndexWriter wr = dirCache.acquireWriter();
        try {
            try {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Storing in TX {0}: {1} added, {2} deleted",
                            new Object[] { this, toAdd.size(), toDelete.size() }
                            );
                }
                _doStore(toAdd, toDelete, docConvertor, queryConvertor, wr);
            } finally {
                // nothing committed upon failure - readers not affected
                boolean ok = false;
                try {
                    ((FlushIndexWriter)wr).callFlush(false, true);
                    ok = true;
                } finally {
                    if (!ok) {
                        dirCache.rollbackTxWriter();
                    }
                }
            }
        } finally {
            dirCache.releaseWriter(wr);
        }
    }
    
    private <S, T> void _doStore(
            @NonNull final Collection<T> data, 
            @NonNull final Collection<S> toDelete,
            @NonNull final Convertor<? super T, ? extends Document> docConvertor, 
            @NonNull final Convertor<? super S, ? extends Query> queryConvertor,
            @NonNull final IndexWriter out) throws IOException {
        try {
            if (dirCache.exists()) {
                for (S td : toDelete) {
                    out.deleteDocuments(queryConvertor.convert(td));
                }
            }
            if (data.isEmpty()) {
                return;
            }
            final LowMemoryWatcher lmListener = LowMemoryWatcher.getInstance();
            Directory memDir = null;
            IndexWriter activeOut;
            if (lmListener.isLowMemory()) {
                activeOut = out;
            } else {
                memDir = new ByteBuffersDirectory ();
                activeOut = new IndexWriter (
                    memDir,
                    new IndexWriterConfig(
                        dirCache.getAnalyzer()));
            }
            for (Iterator<T> it = fastRemoveIterable(data).iterator(); it.hasNext();) {
                T entry = it.next();
                it.remove();
                final Document doc = docConvertor.convert(entry);
                activeOut.addDocument(doc);
                if (memDir != null && lmListener.isLowMemory()) {
                    activeOut.close();
                    out.addIndexes(memDir);
                    memDir = new ByteBuffersDirectory ();
                    activeOut = new IndexWriter (
                        memDir,
                        new IndexWriterConfig(
                            dirCache.getAnalyzer()));
                }
            }
            data.clear();
            if (memDir != null) {
                activeOut.close();
                out.addIndexes(memDir);
                activeOut = null;
                memDir = null;
            }
        } catch (RuntimeException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        } catch (IOException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        }
    }

    @Override
    public <S, T> void store (
            final @NonNull Collection<T> data,
            final @NonNull Collection<S> toDelete,
            final @NonNull Convertor<? super T, ? extends Document> docConvertor,
            final @NonNull Convertor<? super S, ? extends Query> queryConvertor,
            final boolean optimize) throws IOException {
        
        final IndexWriter wr = dirCache.acquireWriter();
        dirCache.storeCloseSynchronizer.enter();
        try {
            try {
                try {
                    _doStore(data, toDelete, docConvertor, queryConvertor, wr);
                } finally {
                    LOGGER.log(Level.FINE, "Committing {0}", this);
                    dirCache.releaseWriter(wr);
                }
            } finally {
                dirCache.close(wr);
            }
        } finally {
            dirCache.storeCloseSynchronizer.exit();
        }
    }
        
    @Override
    public Status getStatus (boolean force) throws IOException {
        return dirCache.getStatus(force);
    }

    @Override
    public void clear () throws IOException {
        dirCache.clear();
    }
    
    @Override
    public void close () throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Closing index: {0} {1}",  //NOI18N
                    new Object[]{
                        this.dirCache.toString(),
                        Thread.currentThread().getStackTrace()});
        }
        dirCache.close(true);
    }


    @Override
    public String toString () {
        return getClass().getSimpleName()+"["+this.dirCache.toString()+"]";  //NOI18N
    }    
           
    private static CachePolicy getCachePolicy() {
        final String value = System.getProperty(PROP_INDEX_POLICY);   //NOI18N
        if (Boolean.TRUE.toString().equals(value) ||
            CachePolicy.ALL.getSystemName().equals(value)) {
            return CachePolicy.ALL;
        }
        if (Boolean.FALSE.toString().equals(value) ||
            CachePolicy.NONE.getSystemName().equals(value)) {
            return CachePolicy.NONE;
        }
        if (CachePolicy.DYNAMIC.getSystemName().equals(value)) {
            return CachePolicy.DYNAMIC;
        }
        return DEFAULT_CACHE_POLICY;
    }

    private static <T> Iterable<T> fastRemoveIterable(final Collection<T> c) {
        return c instanceof ArrayList ?
                new Iterable<T>() {
                    @Override
                    public Iterator<T> iterator() {
                        return new Iterator<T>() {
                            private final ListIterator<T> delegate = ((List<T>)c).listIterator();

                            @Override
                            public boolean hasNext() {
                                return delegate.hasNext();
                            }

                            @Override
                            public T next() {
                                return delegate.next();
                            }

                            @Override
                            public void remove() {
                                delegate.set(null);
                            }
                        };
                    }
                } :
                c;
    }
    

    //<editor-fold defaultstate="collapsed" desc="Private classes (NoNormsReader, TermComparator, CachePolicy)">
        
    private enum CachePolicy {
        
        NONE("none", false),          //NOI18N
        DYNAMIC("dynamic", true),     //NOI18N
        ALL("all", true);             //NOI18N
        
        private final String sysName;
        private final boolean hasMemCache;
        
        CachePolicy(final String sysName, final boolean hasMemCache) {
            assert sysName != null;
            this.sysName = sysName;
            this.hasMemCache = hasMemCache;
        }
        
        String getSystemName() {
            return sysName;
        }
        
        boolean hasMemCache() {
            return hasMemCache;
        }
    }    
    
    private static final class DirCache implements Evictable {

        private static final RequestProcessor EVICTOR_RP =
            new RequestProcessor(LuceneIndex.DirCache.class.getName(), 1);

        private final File folder;
        private final RecordOwnerLockFactory lockFactory;
        private final CachePolicy cachePolicy;
        private final Analyzer analyzer;
        private final StoreCloseSynchronizer storeCloseSynchronizer;
        private volatile FSDirectory fsDir;
        //@GuardedBy("this")
        private ByteBuffersDirectory memDir;
        private CleanReference ref;
        private IndexReader reader;
        private volatile boolean closed;
        private volatile Throwable closeStackTrace;
        private volatile Status validCache;
        private final IndexWriterReference indexWriterRef = new IndexWriterReference();
        private final ReadWriteLock rwLock = new java.util.concurrent.locks.ReentrantReadWriteLock();

        private DirCache(
                final @NonNull File folder,
                final @NonNull CachePolicy cachePolicy,
                final @NonNull Analyzer analyzer) throws IOException {
            assert folder != null;
            assert cachePolicy != null;
            assert analyzer != null;
            this.folder = new Folder(folder);
            this.lockFactory = new RecordOwnerLockFactory();
            this.fsDir = createFSDirectory(this.folder, lockFactory);
            this.cachePolicy = cachePolicy;
            this.analyzer = analyzer;
            this.storeCloseSynchronizer = new StoreCloseSynchronizer();
        }

        Analyzer getAnalyzer() {
            return this.analyzer;
        }

        void clear() throws IOException {
            Future<Void> sync;
            while (true) {
                rwLock.writeLock().lock();
                try {
                    sync = storeCloseSynchronizer.getSync();
                    if (sync == null) {
                        doClear();
                        break;
                    }
                } finally {
                    rwLock.writeLock().unlock();
                }
                try {
                    sync.get();
                } catch (InterruptedException ex) {
                    break;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
                                
        private synchronized void doClear() throws IOException {
            checkPreconditions();
            // already write locked
            doClose(false, true);
            try {
                lockFactory.forceClearLocks();
                final String[] content = fsDir.listAll();
                boolean dirty = false;
                if (content != null) {
                    for (String file : content) {
                        try {
                            fsDir.deleteFile(file);
                        } catch (IOException e) {
                            dirty = true;
                        }
                    }
                }
                if (dirty) {
                    //Try to delete dirty files and log what's wrong
                    final File cacheDir = fsDir.getDirectory().toFile();
                    final File[] children = cacheDir.listFiles();
                    if (children != null) {
                        for (final File child : children) {
                            if (!child.delete()) {
                                throw RecordOwnerLockFactory.annotateException(
                                    new IOException("Cannot delete: " + child.getAbsolutePath()),
                                    folder,
                                    Thread.getAllStackTraces());  //NOI18N
                            }
                        }
                    }
                }
            } finally {
                //Need to recreate directory, see issue: #148374
                this.fsDir.close();
                this.fsDir = createFSDirectory(this.folder, this.lockFactory);
            }
        }
        
        void close(IndexWriter writer) throws IOException {
            if (writer == null) {
                return;
            }
            boolean success = false;
            try {
                writer.close();
                success = true;
            } finally {
                LOGGER.log(Level.FINE, "TX writer cleared for {0}", this);
                indexWriterRef.release();
                refreshReader();
            }
        }

        void close (final boolean closeFSDir) throws IOException {
            Future<Void> sync;
            while (true) {
                rwLock.writeLock().lock();
                try {
                    sync = storeCloseSynchronizer.getSync();
                    if (sync == null) {
                        doClose(closeFSDir, false);
                        break;
                    }
                } finally {
                    rwLock.writeLock().unlock();
                }
                try {
                    sync.get();
                } catch (InterruptedException ex) {
                    break;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        synchronized void doClose (
                final boolean closeFSDir,
                final boolean ensureClosed) throws IOException {
            try {
                rollbackTxWriter();
                if (this.reader != null) {
                    this.reader.close();
                    if (ensureClosed) {
                        try {
                            while (this.reader.getRefCount() > 0) {
                                this.reader.decRef();
                            }
                        } catch (IllegalStateException e) {
                            //pass closed
                        }
                    }
                    this.reader = null;
                }
            } finally {                        
                if (memDir != null) {
                    assert cachePolicy.hasMemCache();
                    if (this.ref != null) {
                        this.ref.clear();
                    }
                    final Directory tmpDir = this.memDir;
                    memDir = null;
                    tmpDir.close();
                }
                if (closeFSDir) {
                    this.closeStackTrace = new Throwable();
                    this.closed = true;
                    this.fsDir.close();
                }
            }
        }
        
        boolean exists() {
            try {
                return DirectoryReader.indexExists((Directory) this.fsDir);
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
                LOGGER.log(Level.INFO, "Broken index: " + folder.getAbsolutePath(), e);
                return false;
            }
        }
        
        Status getStatus (boolean force) throws IOException {
            checkPreconditions();
            Status valid = validCache;
            if (force ||  valid == null) {
                rwLock.writeLock().lock();
                try {
                    Status res = Status.INVALID;
                    if (lockFactory.hasLocks()) {
                        if (indexWriterRef.get() != null) {
                            res = Status.WRITING;
                        } else {
                            LOGGER.log(Level.WARNING, "Locked index folder: {0}", folder.getAbsolutePath());   //NOI18N
                            if (force) {
                                clear();
                            }
                        }
                    } else {
                        if (!exists()) {
                            res = Status.EMPTY;
                        } else if (force) {
                            try {
                                getReader();
                                res = Status.VALID;
                            } catch (IOException | RuntimeException e) {
                                clear();
                            }
                        } else {
                            res = Status.VALID;
                        }
                    }
                    valid = res;
                    validCache = valid;
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
            return valid;
        }
        
        boolean closeTxWriter() throws IOException {
            IndexWriter writer = indexWriterRef.get();
            if (writer != null) {
                LOGGER.log(Level.FINE, "Committing {0}", this);
                close(writer);
                return true;
            } else {
                return false;
            }
        }
        
        boolean rollbackTxWriter() throws IOException {
            final IndexWriter writer = indexWriterRef.get();
            if (writer != null) {
                try {
                    writer.rollback();
                    return true;
                } finally {
                    indexWriterRef.release();
                }
            } else {
                return false;
            }
        }

        void beginTrans() {
            indexWriterRef.beginTrans();
        }

        /**
         * The writer operates under readLock(!) since we do not want to lock out readers,
         * but just close, clear and commit operations. 
         * 
         * @return
         * @throws IOException 
         */
        IndexWriter acquireWriter () throws IOException {
            checkPreconditions();
            hit();
            boolean ok = false;
            rwLock.readLock().lock();
            try {
                try {
                    IndexWriter writer = indexWriterRef.acquire(() -> {
                        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
                        //Linux: The posix::fsync(int) is very slow on Linux ext3,
                        //minimize number of files sync is done on.
                        //http://netbeans.org/bugzilla/show_bug.cgi?id=208224
                        //All OS: The CFS is better for SSD disks.
                        TieredMergePolicy mergePolicy = new TieredMergePolicy();
                        mergePolicy.setNoCFSRatio(1.0);
                        iwc.setMergePolicy(mergePolicy);
                        return new FlushIndexWriter (fsDir, iwc);
                    });
                    ok = true;
                    return writer;
                } catch (LockObtainFailedException lf) {
                    //Already annotated
                    throw lf;
                } catch (IOException ioe) {
                    //Issue #149757 - logging
                    throw RecordOwnerLockFactory.annotateException (
                        ioe,
                        folder,
                        null);
                }
            } finally {
                if (!ok) {
                    rwLock.readLock().unlock();
                }
            }
        }
        
        void releaseWriter(@NonNull final IndexWriter w) {
            assert indexWriterRef.get() == w || indexWriterRef.get() == null;
            rwLock.readLock().unlock();
        }
        
        IndexReader acquireReader() throws IOException {
            rwLock.readLock().lock();
            IndexReader r = null;
            try {
                r = getReader();
                return r;
            } finally {
                if (r == null) {
                  rwLock.readLock().unlock();
                }
            }
        }
        
        void releaseReader(IndexReader r) {
            if (r == null) {
                return;
            }
            assert r == this.reader;
            rwLock.readLock().unlock();
        }
        
        private synchronized IndexReader getReader () throws IOException {
            checkPreconditions();
            hit();
            if (this.reader == null) {
                if (validCache != Status.VALID &&
                    validCache != Status.WRITING &&
                    validCache != null) {
                    return null;
                }
                //Issue #149757 - logging
                try {
                    Directory source;
                    if (cachePolicy.hasMemCache() && fitsIntoMem(fsDir)) {
                        memDir = new ByteBuffersDirectory();
                        for(String file: fsDir.listAll()) {
                            try(IndexOutput io = memDir.createOutput(file, IOContext.READONCE);
                                    IndexInput ii = fsDir.openInput(file, IOContext.READONCE)) {
                                io.copyBytes(ii, ii.length());
                            }
                        }
                        if (cachePolicy == CachePolicy.DYNAMIC) {
                            ref = new CleanReference (this.memDir);
                        }
                        source = memDir;
                    } else {
                        source = fsDir;
                    }
                    assert source != null;
                    this.reader = DirectoryReader.open(source);
                } catch (final FileNotFoundException | ClosedByInterruptException | InterruptedIOException e) {
                    //Either the index dir does not exist or the thread is interrupted
                    //pass - returns null
                } catch (IOException ioe) {
                    if (validCache == null) {
                        return null;
                    } else {
                        throw RecordOwnerLockFactory.annotateException(
                            ioe,
                            folder,
                            Thread.getAllStackTraces());
                    }
                }
            }
            hit();
            return this.reader;
        }


        void refreshReader() throws IOException {
            try {
                if (cachePolicy.hasMemCache()) {
                    close(false);
                } else {
                    rwLock.writeLock().lock();
                    try {
                        synchronized (this) {
                            if (reader != null) {
                                IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) reader);
                                if (newReader != null) {
                                    reader.close();
                                    reader = newReader;
                                }
                            }
                        }
                    } finally {
                        rwLock.writeLock().unlock();
                    }
                }
            } finally {
                 validCache = Status.VALID;
            }
        }
        
        @Override
        public String toString() {
            return this.folder.getAbsolutePath();
        }

        @Override
        public void evicted() {
            EVICTOR_RP.execute(new Runnable() {
                @Override
                public void run() {
                    boolean needsClose = true;
                    synchronized (this) {
                        if (memDir != null) {
                            if (ref != null) {
                                ref.clearHRef();
                            }
                            needsClose = false;
                        }
                    }
                    if (needsClose) {
                        try {
                            close(false);
                            LOGGER.log(Level.FINE, "Evicted index: {0}", folder.getAbsolutePath()); //NOI18N
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }

        private synchronized void hit() {
            if (reader != null) {
                final URI uri = BaseUtilities.toURI(folder);
                if (memDir != null) {
                    IndexCacheFactory.getDefault().getRAMCache().put(uri, this);
                    if (ref != null) {
                        ref.get();
                    }
                } else {
                    IndexCacheFactory.getDefault().getNIOCache().put(uri, this);
                }
            }
        }

        private void checkPreconditions () throws IndexClosedException {
            if (closed) {
                throw (IndexClosedException) new IndexClosedException().initCause(closeStackTrace);
            }
        }

        private static FSDirectory createFSDirectory (
                final File indexFolder,
                final LockFactory lockFactory) throws IOException {
            assert indexFolder != null;
            assert lockFactory != null;
            final String dirType = System.getProperty(PROP_DIR_TYPE);
            if (dirType == null) {
                return FSDirectory.open(indexFolder.toPath(), lockFactory);
            }
            return switch (dirType) {
                case DIR_TYPE_MMAP -> new MMapDirectory(indexFolder.toPath(), lockFactory);
                case DIR_TYPE_NIO -> new NIOFSDirectory(indexFolder.toPath(), lockFactory);
                default -> FSDirectory.open(indexFolder.toPath(), lockFactory);
            };
        }        

        private static boolean fitsIntoMem(@NonNull final Directory dir) {
            try {
                long size = 0;
                for (String path : dir.listAll()) {
                    size+=dir.fileLength(path);
                }
                return IndexCacheFactory.getDefault().getRAMController().shouldLoad(size);
            } catch (IOException ioe) {
                return false;
            }
        }

        private static final class IndexWriterReference {

            //@GuardedBy("this")
            private Pair<Thread,Pair<Long,Exception>> openThread;
            //@GuardedBy("this")
            private Pair<Thread,Pair<Long,Exception>> txThread;
            //@GuardedBy("this")
            private IndexWriter indexWriter;
            //@GuardedBy("this")
            private boolean modified;

            synchronized void beginTrans() {
                assertNoModifiedWriter();
                modified = false;
                txThread = trace();
            }

            @NonNull
            synchronized IndexWriter acquire (@NonNull final Callable<IndexWriter> indexWriterFactory) throws IOException {
                if (indexWriter != null) {
                    assert openThread != null;
                    assertSingleThreadWriter();
                } else {
                    try {
                        assert openThread == null;
                        indexWriter = indexWriterFactory.call();
                        openThread = trace();
                        modified = true;
                    } catch (IOException | RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return indexWriter;
            }

            @CheckForNull
            synchronized IndexWriter get() {
                return indexWriter;
            }

            synchronized void release() {
                openThread = null;
                txThread = null;
                indexWriter = null;
                modified = false;
            }

            private void assertSingleThreadWriter() {
                assert Thread.holdsLock(this);
                if (openThread.first() != Thread.currentThread()) {
                    final IllegalStateException e = new IllegalStateException(String.format(
                        "Other thread using opened writer, " +       //NOI18N
                        "old owner Thread %s(%d), " +          //NOI18N
                        "new owner Thread %s(%d).",             //NOI18N
                            openThread.first(),
                            openThread.first().getId(),
                            Thread.currentThread(),
                            Thread.currentThread().getId()),
                        openThread.second() != null ?
                                openThread.second().second() :
                                null);
                    throw e;
                }
            }

            private void assertNoModifiedWriter() {
                assert Thread.holdsLock(this);
                if (assertsEnabled() && txThread != null && modified) {
                    final Throwable t = new Throwable(
                        String.format(
                            "Using stale writer, possibly forgotten call to store, " +  //NOI18N
                            "old owner Thread %s(%d) enter time: %d, " +           //NOI18N
                            "new owner Thread %s(%d) enter time: %d.",            //NOI18N
                                txThread.first(),
                                txThread.first().getId(),
                                txThread.second().first(),
                                Thread.currentThread(),
                                Thread.currentThread().getId(),
                                System.currentTimeMillis()),
                        txThread.second().second());
                    LOGGER.log(
                        Level.WARNING,
                        "Using stale writer",   //NOI18N
                        t);
                }
            }

            @NonNull
            private static Pair<Thread,Pair<Long,Exception>> trace() {
                return Pair.of(
                    Thread.currentThread(),
                    assertsEnabled() ?
                        Pair.of(System.currentTimeMillis(), new Exception("Owner stack")) :  //NOI18N
                        null);
            }

            @SuppressWarnings("AssertWithSideEffects")
            private static boolean assertsEnabled() {
                boolean ae = false;
                assert ae = true;
                return ae;
            }
        }

        private final class CleanReference extends SoftReference<ByteBuffersDirectory> implements Runnable {
            
            @SuppressWarnings("VolatileArrayField")
            private volatile Directory hardRef; //clearHRef may be called by more concurrently (read lock).
            private final AtomicLong size = new AtomicLong();  //clearHRef may be called by more concurrently (read lock).

            private CleanReference(final ByteBuffersDirectory dir) {
                super (dir, BaseUtilities.activeReferenceQueue());
                final IndexCacheFactory.RAMController c = IndexCacheFactory.getDefault().getRAMController();
                final boolean doHardRef = !c.isFull();
                if (doHardRef) {
                    try {
                        this.hardRef = dir;
                        long _size = 0;
                        for(String file: dir.listAll()) {
                            _size += dir.fileLength(file);
                        }
                        size.set(_size);
                        c.acquire(_size);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                LOGGER.log(Level.FINEST, "Caching index: {0} cache policy: {1}",    //NOI18N
                new Object[]{
                    folder.getAbsolutePath(),
                    cachePolicy.getSystemName()
                });
            }
            
            @Override
            public void run() {
                try {
                    LOGGER.log(Level.FINEST, "Dropping cache index: {0} cache policy: {1}", //NOI18N
                    new Object[] {
                        folder.getAbsolutePath(),
                        cachePolicy.getSystemName()
                    });
                    close(false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            @Override
            public void clear() {
                clearHRef();
                super.clear();
            }
            
            void clearHRef() {
                this.hardRef = null;
                IndexCacheFactory.getDefault().getRAMController().release(
                    size.getAndSet(0));
            }
        }        
    }
    //</editor-fold>

    private static class FlushIndexWriter extends IndexWriter {

        public FlushIndexWriter(
                @NonNull final Directory d,
                @NonNull final IndexWriterConfig conf) throws CorruptIndexException, LockObtainFailedException, IOException {
            super(d, conf);
        }
        
        /**
         * Accessor to index flush for this package
         * @param triggerMerges
         * @param flushDeletes
         * @throws IOException 
         */
        void callFlush(boolean triggerMerges, boolean flushDeletes) throws IOException {
            // flushStores ignored in Lucene 3.5
            super.flush();
            if(flushDeletes) {
                super.forceMergeDeletes(true);
            }
            if(triggerMerges) {
                super.maybeMerge();
            }
        }
    }

    private static final class StoreCloseSynchronizer {

        private final ThreadLocal<Boolean> isWriterThread = new ThreadLocal<>(){
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };

        //@GuardedBy("this")
        private int depth;


        StoreCloseSynchronizer() {}


        synchronized void enter() {
            depth++;
            isWriterThread.set(Boolean.TRUE);
        }

        synchronized void exit() {
            assert depth > 0;
            depth--;
            isWriterThread.remove();
            if (depth == 0) {
                notifyAll();
            }
        }

        synchronized Future<Void> getSync() {
            if (depth == 0 || isWriterThread.get() == Boolean.TRUE) {
                return null;
            } else {
                return new Future<Void>() {
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        synchronized(StoreCloseSynchronizer.this) {
                            return depth == 0;
                        }
                    }

                    @Override
                    public Void get() throws InterruptedException, ExecutionException {
                        synchronized (StoreCloseSynchronizer.this) {
                            while (depth > 0) {
                                StoreCloseSynchronizer.this.wait();
                            }
                        }
                        return null;
                    }

                    @Override
                    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        if (unit != TimeUnit.MILLISECONDS) {
                            throw new UnsupportedOperationException();
                        }
                        synchronized (StoreCloseSynchronizer.this) {
                            while (depth > 0) {
                                StoreCloseSynchronizer.this.wait(timeout);
                            }
                        }
                        return null;
                    }
                };
            }
        }
    }

    private static final class Folder extends File {
        private static final java.nio.file.InvalidPathException IPE = new java.nio.file.InvalidPathException("", "") {    //NOI18N
            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        };

        Folder(@NonNull final File folder) {
            super(folder.getAbsolutePath());
        }

        @Override
        public File getAbsoluteFile() {
            assert isAbsolute();
            return this;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public boolean isFile() {
            return !isDirectory();
        }
    }
}
