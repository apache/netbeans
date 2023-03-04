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
package org.netbeans;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openide.util.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class TaskFuture implements Future<Boolean> {
    private final boolean result;
    private final Task WAIT;

    public TaskFuture(boolean result, Task WAIT) {
        this.result = result;
        this.WAIT = WAIT;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return WAIT.isFinished();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        WAIT.waitFinished();
        return result;
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!WAIT.waitFinished(unit.toMillis(timeout))) {
            throw new TimeoutException();
        }
        return result;
    }
    
}
