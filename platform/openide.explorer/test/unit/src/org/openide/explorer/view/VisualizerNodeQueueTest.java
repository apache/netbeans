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
package org.openide.explorer.view;

import java.awt.EventQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class VisualizerNodeQueueTest extends NbTestCase {

    public VisualizerNodeQueueTest(String name) {
        super(name);
    }
    public void testLongExecutionInEQInterrupted() throws Exception {
        final CountDownLatch slowCanFinish = new CountDownLatch(1);
        class Slow implements Runnable {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    slowCanFinish.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        Slow slow = new Slow();
        class InBetween implements Runnable {
            boolean executed;
            @Override
            public void run() {
                executed = true;
            }
        }
        final InBetween in = new InBetween();
        final CountDownLatch cdl = new CountDownLatch(1);
        class AtTheEnd implements Runnable {
            boolean state;
            @Override
            public void run() {
                state = in.executed;
                cdl.countDown();
            }
        }
        AtTheEnd at = new AtTheEnd();
        
        assertFalse("Outside of EDT", EventQueue.isDispatchThread());
        
        VisualizerNode.runSafe(slow);
        VisualizerNode.runSafe(at);
        EventQueue.invokeLater(in);
        slowCanFinish.countDown();
        
        cdl.await();
        
        assertTrue("InBetween was executed before AtTheEnd "
            + "because Slow was slow", at.state);
    }
    
}
