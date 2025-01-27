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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.RequestProcessor;

public class PriorityQueueRun {

    private static final PriorityQueueRun INSTANCE = new PriorityQueueRun();
    private static final RequestProcessor WORKER = new RequestProcessor(PriorityQueueRun.class.getName() + "-worker", 1, false, false);
    private static final RequestProcessor DELAY = new RequestProcessor(PriorityQueueRun.class.getName() + "-delay", 1, false, false);

    public static PriorityQueueRun getInstance() {
        return INSTANCE;
    }

    private final SortedMap<Priority, List<TaskDescription<?, ?>>> priority2Tasks = new TreeMap<>((p1, p2) -> -p1.compareTo(p2));
    private Priority currentPriority;
    private CancelCheck currentTaskCheck;

    public <P, R> CompletableFuture<R> runTask(Priority priority, CancellableTask<P, R> task, P data) {
        CompletableFuture<R> result = new CompletableFuture<>();

        synchronized (this) {
            priority2Tasks.computeIfAbsent(priority, __ -> new ArrayList<>())
                          .add(new TaskDescription(task, data, result));

            scheduleNext();
        }

        return result;
    }

    public <P, R> CompletableFuture<R> runTask(Priority priority, CancellableTask<P, R> task, P data, int delay) {
        CompletableFuture<R> result = new CompletableFuture<>();
        DELAY.post(() -> {
            if (result.isCancelled()) {
                return ; //already cancelled
            }
            synchronized (this) {
                priority2Tasks.computeIfAbsent(priority, __ -> new ArrayList<>())
                              .add(new TaskDescription(task, data, result));

                scheduleNext();
            }
        }, delay);

        return result;
    }

    private synchronized void scheduleNext() {
        Priority foundPriority = null;
        List<TaskDescription<?, ?>> foundTasks = null;

        for (Entry<Priority, List<TaskDescription<?, ?>>> e : priority2Tasks.entrySet()) {
            if (!e.getValue().isEmpty()) {
                foundPriority = e.getKey();
                foundTasks = e.getValue();
                break;
            }
        }

        if (foundPriority == null) {
            return ;
        }

        if (currentPriority == null) {
            Priority thisPriority = currentPriority = foundPriority;
            TaskDescription thisTask = foundTasks.remove(0);
            CancelCheck thisTaskCheck = currentTaskCheck = new CancelCheck();

            WORKER.post(() -> {
                thisTask.result.whenComplete((__, exc) -> {
                    if (exc instanceof CancellationException) {
                        thisTaskCheck.cancel();
                    }
                });

                if (thisTask.result.isCancelled()) {
                    //nothing to do anymore:
                    return ;
                }

                Object result = null;
                Throwable exception = null;

                try {
                    result = thisTask.task.compute(thisTask.data, thisTaskCheck);
                } catch (Throwable t) {
                    exception = t;
                }

                synchronized (PriorityQueueRun.this) {
                    currentPriority = null;
                    currentTaskCheck = null;

                    if (!thisTask.result.isCancelled()) {
                        if (exception != null) {
                            thisTask.result.completeExceptionally(exception);
                        } else if (thisTaskCheck.isCancelled()) {
                            priority2Tasks.computeIfAbsent(thisPriority, __ -> new ArrayList<>())
                                          .add(0, thisTask);
                        } else {
                            thisTask.result.complete(result);
                        }
                    }
                }

                scheduleNext();
            });
        }

        if (currentPriority != null && currentPriority.compareTo(foundPriority) < 0) {
            //cancel the currently running task:
            currentTaskCheck.cancel();
        }
    }

    private static final class TaskDescription<P, R> {
        private final CancellableTask<P, R> task;
        private final P data;
        private final CompletableFuture<R> result;

        public TaskDescription(CancellableTask<P, R> task, P data, CompletableFuture<R> result) {
            this.task = task;
            this.data = data;
            this.result = result;
        }
    }

    public interface CancellableTask<P, R> {
        public R compute(P param, CancelCheck cancel) throws Exception;
    }

    public interface CancelCallback {
        public void cancel();
    }

    public final class CancelCheck {
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicReference<CancelCallback> cancelCallback = new AtomicReference<>();

        private CancelCheck() {}

        public boolean isCancelled() {
            return cancelled.get();
        }

        public void registerCancel(CancelCallback callback) {
            cancelCallback.set(callback);
            if (cancelled.get()) {
                callback.cancel();
            }
        }

        void cancel() {
            cancelled.set(true);

            CancelCallback callback = cancelCallback.get();

            if (callback != null) {
                callback.cancel();
            }
        }
    }

    public enum Priority {
        BELOW_LOW,
        LOW,
        NORMAL,
        HIGH,
        HIGHER;
    }
}
