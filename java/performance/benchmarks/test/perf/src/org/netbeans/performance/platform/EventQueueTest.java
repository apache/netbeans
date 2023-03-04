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

package org.netbeans.performance.platform;

import java.awt.EventQueue;
import org.netbeans.performance.Benchmark;

/**
 * Benchmark measuring speed of EventQueue methods.
 * @author Jesse Glick
 */
public class EventQueueTest extends Benchmark {

    public static void main(String[] args) {
        simpleRun(EventQueueTest.class);
    }

    public EventQueueTest(String name) {
        super(name);
    }

    public void testIsDispatchThreadWhenFalse() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            assertTrue(!EventQueue.isDispatchThread());
        }
    }
    
    public void testIsDispatchThreadWhenTrue() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                int count = getIterationCount();
                for (int i = 0; i < count; i++) {
                    assertTrue(EventQueue.isDispatchThread());
                }
            }
        });
    }
    
    public void testInvokeAndWait() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    assertTrue(EventQueue.isDispatchThread());
                }
            });
        }
    }
    
    public void testAcquireMonitor() throws Exception {
        Object lock = new Object();
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            synchronized (lock) {
                assertTrue(true);
            }
        }
    }
    
    public void testCheckAcquiredMonitor() throws Exception {
        Object lock = new Object();
        synchronized (lock) {
            int count = getIterationCount();
            for (int i = 0; i < count; i++) {
                assertTrue(Thread.holdsLock(lock));
            }
        }
    }
    
}
