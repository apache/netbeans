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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.protocol.PriorityQueueRun.Priority;
import org.openide.util.Exceptions;

public class PriorityQueueRunTest extends NbTestCase {

    public PriorityQueueRunTest(String name) {
        super(name);
    }

    public void testSimpleExecute() throws Exception {
        String normalInputData = new String("normal");
        int normalResultValue = -1;
        String priorityInputData = new String("priority");
        int priorityResultValue = 1;
        AtomicBoolean normalWaitCheckingForCancel = new AtomicBoolean();
        normalWaitCheckingForCancel.set(true);
        CountDownLatch normalIsWaiting = new CountDownLatch(1);
        List<Integer> resultsInOrder = new ArrayList<>();
        List<CompletableFuture<Void>> pendingFutures = new ArrayList<>();
        pendingFutures.add(
                PriorityQueueRun.getInstance()
                                .runTask(Priority.NORMAL,
                                         (data, check) -> {
                                             assertSame(normalInputData, data);

                                             while (normalWaitCheckingForCancel.get()) {
                                                 if (check.isCancelled()) {
                                                     return null;
                                                 }
                                                 normalIsWaiting.countDown();
                                                 try {
                                                     Thread.sleep(1);
                                                 } catch (InterruptedException ex) {
                                                     Exceptions.printStackTrace(ex);
                                                 }
                                             }

                                             return normalResultValue;
                                         }, normalInputData)
                                .thenAccept(resultsInOrder::add));
        //wait until the task is run:
        normalIsWaiting.await();
        pendingFutures.add(
                PriorityQueueRun.getInstance()
                                .runTask(Priority.HIGH,
                                         (data, check) -> {
                                             assertSame(priorityInputData, data);

                                             normalWaitCheckingForCancel.set(false);

                                             return priorityResultValue;
                                         }, priorityInputData)
                                .thenAccept(resultsInOrder::add));
        pendingFutures.forEach(cf -> {
            try {
                cf.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        });

        assertEquals(List.of(priorityResultValue, normalResultValue), resultsInOrder);
    }

}
