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

package org.netbeans.modules.extexecution;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class StopActionTest extends NbTestCase {

    public StopActionTest(String name) {
        super(name);
    }

    public void testStop() {
        StopAction stopAction = new StopAction();
        stopAction.actionPerformed(null); // must pass

        TestFuture<Integer> task = new TestFuture<Integer>();
        stopAction.setTask(task);
        stopAction.actionPerformed(null);

        assertTrue(task.isCancelled());
        assertTrue(task.isDone());
        assertTrue(task.isInterrupted());
    }

    private static class TestFuture<T> implements Future<T> {

        private boolean cancelled;

        private boolean interrupted;

        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            interrupted = mayInterruptIfRunning;
            return true;
        }

        public T get() throws InterruptedException, ExecutionException {
            return null;
        }

        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public boolean isDone() {
            return cancelled;
        }

        public boolean isInterrupted() {
            return interrupted;
        }
    }
}
