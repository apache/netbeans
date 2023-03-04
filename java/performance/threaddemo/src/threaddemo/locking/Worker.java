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

package threaddemo.locking;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Worker thread (off-AWT) that can run tasks asynch.
 * Convenience wrapper for {@link ExecutorService}.
 * @author Jesse Glick
 */
public final class Worker {

    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    private Worker() {}

    /**
     * Start a task.
     * It will be run soon.
     * At most one task will be run at a time.
     */
    public static void start(Runnable run) {
        POOL.submit(run);
    }
    
    /**
     * Do something and wait for it to finish.
     */
    public static <T> T block(final LockAction<T> act) {
        Future<T> f = POOL.submit(new Callable<T>() {
            public T call() {
                return act.run();
            }
        });
        try {
            return f.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw (RuntimeException) t;
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Do something and wait for it to finish.
     * May throw exceptions.
     */
    public static <T, E extends Exception> T block(final LockExceptionAction<T,E> act) throws E {
        Future<T> f = POOL.submit(new Callable<T>() {
            public T call() throws Exception {
                return act.run();
            }
        });
        try {
            return f.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                @SuppressWarnings("unchecked")
                E _e = (E) e;
                throw _e;
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
    
}
