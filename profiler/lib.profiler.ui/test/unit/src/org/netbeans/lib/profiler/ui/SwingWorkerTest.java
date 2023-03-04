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
package org.netbeans.lib.profiler.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
public class SwingWorkerTest {
    
    public SwingWorkerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class SwingWorker.
     */
    @Test
    public void testExecute() throws Exception {
        System.out.println("execute");
        final boolean[] executed = new boolean[]{false};
        final CountDownLatch latch = new CountDownLatch(1);
        SwingWorker instance = new SwingWorkerImpl(0, true, null, new Runnable() {
            @Override
            public void run() {
                executed[0] = true;
            }
        }, new Runnable() {

            @Override
            public void run() {
                latch.countDown();
            }
        }, null, null);
        instance.execute();
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(executed[0]);
    }

    /**
     * Test of cancel method, of class SwingWorker.
     */
    @Test
    public void testCancel() throws Exception {
        System.out.println("cancel");
        final boolean[] canceled = new boolean[]{false};
        final boolean[] done = new boolean[]{false};
        final CountDownLatch latch = new CountDownLatch(1);
        SwingWorker instance = new SwingWorkerImpl(6000, true, null, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }, new Runnable() {

            @Override
            public void run() {
                done[0] = true;
            }
        }, new Runnable() {

            @Override
            public void run() {
                canceled[0] = true;
                latch.countDown();
            }
        }, null);
        instance.execute();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        instance.cancel();
        latch.await(5, TimeUnit.SECONDS);
        assertTrue(canceled[0]);
        assertFalse(done[0]);
    }

    /**
     * Test of nonResponding method, of class SwingWorker.
     */
    @Test
    public void testNonResponding() throws Exception {
        System.out.println("nonResponding");
        final boolean[] waiting = new boolean[]{false};
        final CountDownLatch latch = new CountDownLatch(1);
        SwingWorker instance = new SwingWorkerImpl(500, true, null, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }, new Runnable() {

            @Override
            public void run() {
                latch.countDown();
            }
        }, null, 
        new Runnable() {

            @Override
            public void run() {
                waiting[0] = true;
            }
        });
        instance.execute();
        latch.await(4, TimeUnit.SECONDS);
        
        assertTrue(waiting[0]);
    }
    
    @Test
    public void testSharedSemaphore() throws Exception  {
        System.out.println("sharedSemaphore");
        Semaphore s = new Semaphore(1);
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(2);
        
        final SwingWorker sw1 = new SwingWorkerImpl(0, true, s, new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
                try {
                    Thread.sleep(312);
                } catch (InterruptedException e) {
                }
                counter.decrementAndGet();
            }
        }, new Runnable() {

            @Override
            public void run() {
                latch.countDown();
            }
        }, null, null);
        SwingWorker sw2 = new SwingWorkerImpl(0, true, s, new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
                sw1.execute();                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                counter.decrementAndGet();
            }
        }, new Runnable() {

            @Override
            public void run() {
                latch.countDown();
            }
        }, null, null);
        
        sw2.execute();
        
        latch.await(3, TimeUnit.SECONDS);
        
        assertEquals(0, counter.get());
    }

    public class SwingWorkerImpl extends SwingWorker {
        private final Runnable task, onDone, onCancel, waiting;
        
        private final int warmup;

        public SwingWorkerImpl(int warmup, boolean forceEQ, Semaphore throughputSemaphore, Runnable task, Runnable onDone, Runnable onCancel, Runnable waiting) {
            super(forceEQ, throughputSemaphore);
            this.warmup = warmup;
            this.task = task;
            this.onDone = onDone;
            this.onCancel = onCancel;
            this.waiting = waiting;
        }
        
        public void doInBackground() {
            if (task != null) {
                task.run();
            }
        }

        @Override
        protected int getWarmup() {
            return warmup;
        }

        @Override
        protected void done() {
            if (onDone != null) {
                onDone.run();
            }
        }

        @Override
        protected void cancelled() {
            if (onCancel != null) {
                onCancel.run();
            }
        }

        @Override
        protected void nonResponding() {
            if (waiting != null) {
                waiting.run();
            }
        }
        
        
    }
}
