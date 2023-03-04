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

package org.openide.util;

import java.util.concurrent.CountDownLatch;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MutexTryTest extends NbTestCase {
    private Mutex.Privileged p;
    private Mutex m;

    public MutexTryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        p = new Mutex.Privileged();
        m = new Mutex(p);
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    public void testTimeOutTryRead() throws Exception {
        Block b = new Block(p, false);
        b.block();
        
        long now = System.currentTimeMillis();
        boolean res = p.tryReadAccess(1000);
        long then = System.currentTimeMillis();
        
        long delay = then - now;
        
        assertFalse("Locking has not succeeded", res);
        assertFalse("No read lock held", m.isReadAccess());
        assertFalse("No write lock held", m.isWriteAccess());
        
        assertTrue("Delay is 1s, was: " + delay + " ms", delay > 900);
    }

    public void testSuccessfulTryRead() throws Exception {
        Block b = new Block(p, true);
        b.block();
        
        boolean res = p.tryReadAccess(1000);
        
        assertTrue("Locking has succeeded", res);
        assertTrue("read lock held", m.isReadAccess());
        assertFalse("No write lock held", m.isWriteAccess());
    }
    
    public void testTimeOutTryWrite() throws Exception {
        Block b = new Block(p, false);
        b.block();
        
        long now = System.currentTimeMillis();
        boolean res = p.tryWriteAccess(1000);
        long then = System.currentTimeMillis();
        
        long delay = then - now;
        
        assertFalse("Locking has not succeeded", res);
        assertFalse("No read lock held", m.isReadAccess());
        assertFalse("No write lock held", m.isWriteAccess());
        
        assertTrue("Delay is 1s, was: " + delay + " ms", delay > 900);
    }
    
    public void testSuccessfulTryWrite() throws Exception {
        boolean res = p.tryWriteAccess(1000);
        assertTrue("Access granted", res);
        assertFalse("No read lock held", m.isReadAccess());
        assertTrue("Write lock held", m.isWriteAccess());
    }
    
    
    private static class Block implements Runnable {
        private final Mutex.Privileged p;
        private final boolean read;
        private final CountDownLatch runs = new CountDownLatch(1);
        
        Block(Mutex.Privileged p, boolean read) {
            this.p = p;
            this.read = read;
        }
        
        void block() throws InterruptedException {
            Thread t = new Thread(this, "Block mutex");
            t.start();
            runs.await();
        }
        
        @Override
        public synchronized void run() {
            if (read) {
                p.enterReadAccess();
            } else {
                p.enterWriteAccess();
            }
            runs.countDown();
            for (;;) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
    }
}
