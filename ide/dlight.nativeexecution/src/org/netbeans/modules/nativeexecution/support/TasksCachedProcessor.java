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
package org.netbeans.modules.nativeexecution.support;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

/**
 *
 * P - type of task parameters
 * R - type of result
 *
 */
public final class TasksCachedProcessor<P, R>
        implements Computable<P, R> {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private final ConcurrentMap<P, Future<R>> cache =
            new ConcurrentHashMap<>();
    private final Computable<P, R> computable;
    private final boolean removeOnCompletion;

    public TasksCachedProcessor(Computable<P, R> c, boolean removeOnCompletion) {
        this.computable = c;
        this.removeOnCompletion = removeOnCompletion;
    }

    public boolean isResultAvailable(final P arg) {
        Future<R> res = cache.get(arg);

        if (res == null) {
            return false;
        }

        return res.isDone() && !res.isCancelled();
    }

    /**
     * Here I implemented following logic:
     * if it is requested to fetch the data and the same request is in progress -
     * result that returned is taken from the original one.
     * once task is completed, it is removed from cache!
     *
     */
    @Override
    public R compute(final P arg) throws InterruptedException {
        Future<R> f = cache.get(arg);

        if (f == null) {
            Callable<R> evaluation = new Callable<R>() {

                @Override
                public R call() throws InterruptedException {
                    return computable.compute(arg);
                }
            };

            FutureTask<R> ft = new FutureTask<>(evaluation);
            f = cache.putIfAbsent(arg, ft);

            if (f == null) {
                f = ft;
                ft.run();
            }
        }

        try {
            return f.get();
        } catch (InterruptedException ex) {
            cache.remove(arg, f);
            throw new CancellationException(ex.getMessage());
        } catch (Throwable th) {
            cache.remove(arg, f);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "TasksCachedProcessor: exception while task execution:", th); // NOI18N
            }
            throw new CancellationException(th.getMessage());
        } finally {
            if (removeOnCompletion) {
                cache.remove(arg, f);
            }
        }
    }

    public void remove(P param) {
        Future<R> f = cache.get(param);

        if (f != null && !f.isDone()) {
            f.cancel(true);
        }

        cache.remove(param);
    }

    public void resetCache() {
        // Even if some tasks are in progress it's OK just to clear the cache.
        // Tasks will not be terminated though...
        cache.clear();
    }
}
