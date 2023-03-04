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
package org.netbeans.modules.php.dbgp;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public abstract class SingleThread extends ThreadPoolExecutor implements Runnable, Cancellable {
    final Object sync = new Object();
    FutureTask task;

    public SingleThread() {
        super(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        setThreadFactory(getDaemonThreadFactory());
        setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                super.rejectedExecution(r, e);
                Logger.getLogger(getClass().getName()).info("rejectedExecution"); //NOI18N
            }
        });
    }

    public static ThreadFactory getDaemonThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
                Thread newThread = defaultThreadFactory.newThread(r);
                newThread.setDaemon(true);
                return newThread;
            }
        };
    }

    public Object getSync() {
        return sync;
    }

    public final FutureTask invokeLater() {
        synchronized (sync) {
            task = new FutureTask(this, null);
            execute(task);
            return task;
        }
    }

    public final void invokeAndWait() throws InterruptedException, ExecutionException {
        synchronized (sync) {
            task = new FutureTask(this, null);
            execute(task);
            task.get();
        }
    }

    protected final void waitFinished() {
        synchronized (sync) {
            if (task != null) {
                try {
                    task.get(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Thread.interrupted();
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (TimeoutException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    public abstract boolean cancel();

}
