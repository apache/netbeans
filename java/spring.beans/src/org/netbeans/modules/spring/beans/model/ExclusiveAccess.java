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

package org.netbeans.modules.spring.beans.model;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/**
 * An utility for managing exclusive access.
 *
 * @author Andrei Badea
 */
public final class ExclusiveAccess {

    // TODO improve the priority of runSyncTask() tasks.

    private static final ExclusiveAccess INSTANCE = new ExclusiveAccess();

    private final RequestProcessor rp = new RequestProcessor("Spring config file access thread", 1, false); // NOI18N
    private final ReentrantLock lock = new ReentrantLock();

    public static ExclusiveAccess getInstance() {
        return INSTANCE;
    }

    /**
     * Posts a task which will be run with exclusive access at some time
     * in the future and returns immediately.
     *
     * @param  run the task.
     */
    public AsyncTask createAsyncTask(Runnable run) {
        return new AsyncTask(rp.create(new TaskWrapper(run), true));
    }

    /**
     * Runs a priority task synchronously (the method returns after the task
     * has run).
     *
     * @param  run the task.
     */
    public <V> V runSyncTask(Callable<V> task) throws Exception {
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks whether the current thread has exclusive access (that is,
     * it is running a task posted through {@link #postTask} or {@link #runPriorityTask}.
     *
     * @return true if the current thread has exclusive access, false otherwise.
     */
    public boolean isCurrentThreadAccess() {
        return lock.isHeldByCurrentThread();
    }

    public static final class AsyncTask {

        private final Task task;

        AsyncTask(RequestProcessor.Task task) {
            this.task = task;
        }

        public void schedule(int delay) {
            task.schedule(delay);
        }

        public boolean cancel() {
            if (task.cancel()) {
                return true;
            }
            return false;
        }

        public boolean isFinished() {
            return task.isFinished();
        }
    }

    /**
     * Wraps a runnable inside the lock to make sure it has exclusive access.
     */
    private final class TaskWrapper implements Runnable {

        private final Runnable delegate;

        public TaskWrapper(Runnable delegate) {
            this.delegate = delegate;
        }

        public void run() {
            lock.lock();
            try {
                delegate.run();
            } finally {
                lock.unlock();
            }
        }
    }
}
