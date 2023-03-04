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

package org.netbeans.modules.java.source.indexing;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
public final class JavaIndexerWorker {

    private static final Logger LOG = Logger.getLogger(JavaIndexerWorker.class.getName());
    private static final int DEFAULT_PROC_COUNT = 2;
    private static final int DEFAULT_BUFFER_SIZE = 1024*1024;
    private static final int MIN_PROC = 4;
    private static final int MIN_FILES = 10;    //Trivial problem size
    private static final boolean PREFETCH_DISABLED = Boolean.getBoolean("SourcePrefetcher.disabled");   //NOI18N
    private static final int PROC_COUNT = Integer.getInteger(
            "SourcePrefetcher.proc.count",  //NOI18N
            DEFAULT_PROC_COUNT);
    /*test - never change it during IDE run*/
    static int BUFFER_SIZE = Integer.getInteger("SourcePrefetcher.buffer.size", DEFAULT_BUFFER_SIZE); //NOI18N
    /*test*/ static Boolean TEST_DO_PREFETCH;

    private static final RequestProcessor RP = new RequestProcessor(
        JavaIndexerWorker.class.getName(),
        PROC_COUNT,
        false,
        false);

    private JavaIndexerWorker() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    public static boolean supportsConcurrent() {
        final int procCount = Runtime.getRuntime().availableProcessors();
        LOG.log(
            Level.FINER,
            "Proc Count: {0}, Prefetch disabled: {1}",  //NOI18N
            new Object[]{
                procCount,
                PREFETCH_DISABLED
            });
        return procCount >= MIN_PROC && !PREFETCH_DISABLED;
    }

    @NonNull
    public static Executor getExecutor() {
        return RP;
    }

    @CheckForNull
    static <T> T reduce(
        @NullAllowed final T initialValue,
        @NonNull final BinaryOperator<T> f,
        @NonNull final Callable<T>... actions) throws ExecutionException, InterruptedException {
        T result = initialValue;
        if (supportsConcurrent()) {
            LOG.log(
                Level.FINE,
                "Using concurrent reduce, {0} workers",    //NOI18N
                PROC_COUNT);
            final Iterable<Future<T>> coResults = RP.invokeAll(Arrays.asList(actions));
            for (Future<T> coResult : coResults) {
                result = f.apply(result, coResult.get());
            }
        } else {
            LOG.log(
                Level.FINE,
                "Using sequential reduce");    //NOI18N
            for (Callable<? extends T> action : actions) {
                try {
                    result = f.apply(result, action.call());
                } catch (Exception ex) {
                    throw new ExecutionException(ex);
                }
            }
        }
        return result;
    }

    @NonNull
    static Iterator<? extends CompileTuple> getCompileTupleIterator(
            @NonNull final Collection<? extends CompileTuple> files,
            @NonNull final SuspendStatus suspendStatus) {
        final int probSize = files.size();
        LOG.log(
            Level.FINER,
            "File count: {0}",  //NOI18N
            probSize);
        final boolean supportsPar = supportsConcurrent() && probSize > MIN_FILES;
        final boolean doPrefetch = TEST_DO_PREFETCH != null?
                TEST_DO_PREFETCH:
                supportsPar;
        if (doPrefetch && suspendStatus.isSuspendSupported()) {
            LOG.log(
                Level.FINE,
                "Using concurrent iterator, {0} workers",    //NOI18N
                PROC_COUNT);
            return new ConcurrentIterator(files, suspendStatus);
        } else {
            LOG.fine("Using sequential iterator");    //NOI18N
            return new NopRemoveItDecorator(files.iterator(), suspendStatus);
        }
    }

    @NonNull
    private static <T> CompletionService<T> newCompletionService() {
        return new ExecutorCompletionService<>(RP);
    }    

    interface BinaryOperator<T> {
        T apply(T left, T right);
    }

    enum Bool implements BinaryOperator<Boolean> {
        AND{
            @NonNull
            @Override
            public Boolean apply(
                @NonNull final Boolean left,
                @NonNull final Boolean right) {
                return left == Boolean.TRUE ?
                    right :
                    Boolean.FALSE;
            }
        },
        OR {
            @NonNull
            @Override
            public Boolean apply(
                @NonNull final Boolean left,
                @NonNull final Boolean right) {
                return left == Boolean.FALSE ?
                    right :
                    Boolean.TRUE;
            }
        }
    }
    
    private abstract static class SuspendableIterator implements Iterator<CompileTuple> {
        
        private final SuspendStatus suspendStatus;
        
        protected SuspendableIterator(@NonNull final SuspendStatus suspendStatus) {
            assert suspendStatus != null;
            this.suspendStatus = suspendStatus;
        }
        
        protected final void safePark() {
            try {
                suspendStatus.parkWhileSuspended();
            } catch (InterruptedException ex) {
                //NOP - safe to ignore
            }
        }
    }

    private static final class NopRemoveItDecorator extends SuspendableIterator {

        private final Iterator<? extends CompileTuple> delegate;

        private NopRemoveItDecorator(
                @NonNull final Iterator<? extends CompileTuple> delegate,
                @NonNull final SuspendStatus suspendStatus) {
            super(suspendStatus);
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public CompileTuple next() {
            return delegate.next();
        }

        @Override
        public void remove() {
            //NOP
        }

    }

    private static final class ConcurrentIterator extends SuspendableIterator implements Closeable {

        private static final CompileTuple DUMMY = new CompileTuple(
            null,
            null,
            true,   //DUMMY is virtual not scheduled to worker thread
            false);


        private final CompletionService<CompileTuple> cs;
        private final Semaphore sem;
        private final Deque<CompileTuple> virtuals;
        //@NotThreadSafe
        private int count;
        //@NotThreadSafe
        private CompileTuple active;
        private volatile boolean closed;


        private ConcurrentIterator(
                @NonNull final Iterable<? extends CompileTuple> files,
                @NonNull final SuspendStatus suspendStatus) {
            super(suspendStatus);
            this.cs = JavaIndexerWorker.newCompletionService();
            this.sem = new Semaphore(BUFFER_SIZE);
            this.virtuals = new ArrayDeque<>();

            for (final CompileTuple ct : files) {
                if (ct.virtual) {
                    //Virtual sources are already in memory no need to post them to other thread.
                    virtuals.offer(ct);
                } else {
                    cs.submit(new Callable<CompileTuple>() {
                        @NonNull
                        @Override
                        public CompileTuple call() throws Exception {
                            safePark();
                            if (closed) {
                                LOG.finest("Skipping prefetch due to close.");  //NOI18N
                                return ct;
                            }
                            final int len = Math.min(BUFFER_SIZE,ct.jfo.prefetch());
                            if (LOG.isLoggable(Level.FINEST) &&
                                (sem.availablePermits() - len) < 0) {
                                LOG.finest("Buffer full");  //NOI18N
                            }
                            sem.acquire(len);
                            return ct;
                        }
                    });
                }
                count++;
            }
        }

        @Override
        public boolean hasNext() {
            ensureNotClosed();
            return count > 0;
        }

        @Override
        public CompileTuple next() {
            ensureNotClosed();
            if (active != null) {
                throw new IllegalStateException("Call remove to free resources");   //NOI18N
            }
            if (!hasNext()) {
                throw new IllegalStateException("No more tuples."); //NOI18N
            }
            safePark();
            try {
                active = virtuals.isEmpty() ?
                    cs.take().get() :
                    virtuals.removeFirst();
            } catch (InterruptedException ex) {
                active = DUMMY;
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                active = DUMMY;
                final Throwable rootCause = ex.getCause();
                if (rootCause instanceof IOException) {
                    LOG.log(Level.INFO, rootCause.getLocalizedMessage());
                } else {
                    Exceptions.printStackTrace(ex);
                }
            } finally {
                count--;
            }
            return active == DUMMY ? null : active;
        }

        @Override
        public void remove() {
            ensureNotClosed();
            if (active == null) {
                throw new IllegalStateException("Call next before remove");   //NOI18N
            }
            try {
                if (!active.virtual) {
                    final int len = Math.min(BUFFER_SIZE, active.jfo.dispose());
                    sem.release(len);
                }
            } finally {
                active = null;
            }
        }

        @Override
        public void close() throws IOException {
            ensureNotClosed();
            closed = true;
            //Actually the threads may be blocked in semaphore requiring at most PROC_COUNT * BUFFER_SIZE grants
            //as we are at the end of the life cycle it's safe to break invariants and unblock the
            //threads.
            sem.release(PROC_COUNT * BUFFER_SIZE);
        }

        private void ensureNotClosed() {
            if (closed) {
                throw new IllegalStateException("Already closed SourcePrefetcher instance.");
            }
        }

    }

}
