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

package org.openide.nodes;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

public abstract class NodeAbstractNodeDeadlockTestBase extends NbTestCase {

    public NodeAbstractNodeDeadlockTestBase(String name) {
        super(name);
    }
    
    public void testDeadlock() throws Exception {
        CountDownLatch nodeInitWait = new CountDownLatch(1);
        CountDownLatch nodeInitCalled = new CountDownLatch(1);
        AtomicInteger taskFinishedCount = new AtomicInteger();
        Logger nodeInitLogger = Logger.getLogger("org.openide.nodes.Node-init");
        Level origLevel = nodeInitLogger.getLevel();
        nodeInitLogger.setLevel(Level.FINEST);
        Handler testHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("Initializing org.openide.nodes.Node".equals(record.getMessage())) {
                    try {
                        nodeInitCalled.countDown();
                        nodeInitWait.await();
                        taskFinishedCount.incrementAndGet();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            @Override
            public void flush() {}
            @Override
            public void close() {}
        };
        nodeInitLogger.addHandler(testHandler);
        try {
            Thread nodeLoader = new Thread(() -> {
                nodeInitCallback();
                taskFinishedCount.incrementAndGet();
            });
            UncaughtExceptionHandler uncaughtExceptionHandler = (t, e) -> {
                e.printStackTrace();
            };
            nodeLoader.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            Thread abstractNodeLoader = new Thread(() -> {
                new AbstractNode(Children.LEAF);
                taskFinishedCount.incrementAndGet();
            });
            abstractNodeLoader.setUncaughtExceptionHandler(uncaughtExceptionHandler);

            //main test:
            nodeLoader.start();
            assertTrue(nodeInitCalled.await(100, TimeUnit.MILLISECONDS));
            abstractNodeLoader.start();
            //need to wait for AbstractNode initialization, but no good hook there:
            Thread.sleep(100);
            nodeInitWait.countDown();
            assertNotNull(Node.EMPTY);
            nodeLoader.join();
            abstractNodeLoader.join();
            assertEquals(3, taskFinishedCount.get());
        } finally {
            nodeInitWait.countDown(); //ensure Node init is never stalled
            nodeInitLogger.setLevel(origLevel);
            nodeInitLogger.removeHandler(testHandler);
        }
    }

    protected abstract void nodeInitCallback();
}
