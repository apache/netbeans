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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.openide.util.RequestProcessor;

/**
 * Default implementation of tasks executor service.
 * Uses RequestProcessor but allows submit Callable tasks.
 */
public final class NativeTaskExecutorService {

    private static final String PREFIX = "NATIVEEXECUTOR: "; // NOI18N
    private static final RequestProcessor processor = new RequestProcessor(PREFIX, 50);

    private NativeTaskExecutorService() {
    }

    public static <T> Future<T> submit(final Callable<T> task, final String name) {
        final FutureTask<T> ftask = new FutureTask<>(new Callable<T>() {

            @Override
            public T call() throws Exception {
                Thread.currentThread().setName(PREFIX + name);
                return task.call();
            }
        });


        processor.post(ftask);
        return ftask;
    }

    public static void submit(final Runnable task, final String name) {
        processor.post(new Runnable() {

            @Override
            public void run() {
                Thread.currentThread().setName(PREFIX + name);
                task.run();
            }
        });
    }
}
