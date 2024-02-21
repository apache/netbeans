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
package org.netbeans.modules.parsing.impl;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
public class RunWhenScanFinishedSupport {

    private static final Logger LOG = Logger.getLogger(RunWhenScanFinishedSupport.class.getName());
    //Scan lock
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    //Deferred task until scan is done
    private static final List<DeferredTask> todo = Collections.synchronizedList(new LinkedList<DeferredTask>());


    private RunWhenScanFinishedSupport() {}

    public static void performDeferredTasks() {
        DeferredTask[] _todo;
        synchronized (todo) {
            _todo = todo.toArray(new DeferredTask[0]);
            todo.clear();
        }
        for (final DeferredTask rq : _todo) {
            Lookups.executeWith(
                rq.context,
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TaskProcessor.runUserTask(rq.task, rq.sources);
                        } catch (ParseException e) {
                            Exceptions.printStackTrace(e);
                        } finally {
                            rq.sync.taskFinished();
                        }
                    }
                });
        }
    }

    public static void performScan (
            @NonNull final Runnable runnable,
            @NonNull final Lookup context) {
        lock.writeLock().lock();
        try {
            LOG.log(
                    Level.FINE,
                    "performScan:entry",    //NOI18N
                    runnable);
            Lookups.executeWith(context, runnable);
            LOG.log(
                    Level.FINE,
                    "performScan:exit",     //NOI18N
                    runnable);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public static boolean isScanningThread() {
        return lock.isWriteLockedByCurrentThread();
    }

    @NonNull
    public static Future<Void> runWhenScanFinished (
            @NonNull final Mutex.ExceptionAction<Void> task,
            @NonNull final Collection<Source> sources) throws ParseException {
        assert task != null;
        assert sources != null;
        final ScanSync sync = new ScanSync (task);
        final DeferredTask r = new DeferredTask (sources, task, sync, Lookup.getDefault());
        //0) Add speculatively task to be performed at the end of background scan
        todo.add (r);
        boolean indexing = TaskProcessor.getIndexerBridge().isIndexing();
        if (indexing) {
            return sync;
        }
        //1) Try to aquire javac lock, if successfull no task is running
        //   perform the given taks synchronously if it wasn't already performed
        //   by background scan.
        final boolean locked = lock.readLock().tryLock();
        if (locked) {
            try {
                LOG.log(
                    Level.FINE,
                    "runWhenScanFinished:entry",    //NOI18N
                    task);
                if (todo.remove(r)) {
                    try {
                        TaskProcessor.runUserTask(task, sources);
                    } finally {
                        sync.taskFinished();
                    }
                }
                LOG.log(
                    Level.FINE,
                    "runWhenScanFinished:exit",     //NOI18N
                    task);
            } finally {
                lock.readLock().unlock();
            }
        }
        return sync;
    }

    private static final class DeferredTask {
        final Collection<Source> sources;
        final Mutex.ExceptionAction<Void> task;
        final ScanSync sync;
        final Lookup context;

        public DeferredTask (
                @NonNull final Collection<Source> sources,
                @NonNull final Mutex.ExceptionAction<Void> task,
                @NonNull final ScanSync sync,
                @NonNull final Lookup context) {
            assert sources != null;
            assert task != null;
            assert sync != null;
            assert context != null;

            this.sources = sources;
            this.task = task;
            this.sync = sync;
            this.context = context;
        }
    }

    private static final class ScanSync implements Future<Void> {

        private Mutex.ExceptionAction<Void> task;
        private final CountDownLatch sync;
        private final AtomicBoolean canceled;

        public ScanSync (final Mutex.ExceptionAction<Void> task) {
            assert task != null;
            this.task = task;
            this.sync = new CountDownLatch (1);
            this.canceled = new AtomicBoolean (false);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (this.sync.getCount() == 0) {
                return false;
            }
            synchronized (todo) {
                boolean _canceled = canceled.getAndSet(true);
                if (!_canceled) {
                    for (Iterator<DeferredTask> it = todo.iterator(); it.hasNext();) {
                        DeferredTask t = it.next();
                        if (t.task == this.task) {
                            it.remove();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean isCancelled() {
            return this.canceled.get();
        }

        @Override
        public synchronized boolean isDone() {
            return this.sync.getCount() == 0;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            checkCaller();
            this.sync.await();
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            checkCaller();
            if (!this.sync.await(timeout, unit)) {
                throw new TimeoutException();
            } else {
                return null;
            }
        }

        private void taskFinished () {
            this.sync.countDown();
        }

        private void checkCaller() {
            if (TaskProcessor.getIndexerBridge().ownsProtectedMode()) {
                throw new IllegalStateException("ScanSync.get called by protected mode owner.");    //NOI18N
            }
            //In dev build check also that blocking get is not called from OpenProjectHook -> deadlock
            boolean ae = false;
            assert ae = true;
            if (ae) {
                for (StackTraceElement stElement : Thread.currentThread().getStackTrace()) {
                    if ("org.netbeans.spi.project.ui.ProjectOpenedHook$1".equals(stElement.getClassName()) &&   //NOI18N
                        ("projectOpened".equals(stElement.getMethodName()) || "projectClosed".equals(stElement.getMethodName()))) {    //NOI18N
                        throw new AssertionError("Calling ParserManager.parseWhenScanFinished().get() from ProjectOpenedHook"); //NOI18N
                    }
                }

            }
        }

    }
}
