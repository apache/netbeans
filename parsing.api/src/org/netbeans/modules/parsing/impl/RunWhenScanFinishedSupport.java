/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    private final static List<DeferredTask> todo = Collections.synchronizedList(new LinkedList<DeferredTask>());


    private RunWhenScanFinishedSupport() {}

    public static void performDeferredTasks() {
        DeferredTask[] _todo;
        synchronized (todo) {
            _todo = todo.toArray(new DeferredTask[todo.size()]);
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
