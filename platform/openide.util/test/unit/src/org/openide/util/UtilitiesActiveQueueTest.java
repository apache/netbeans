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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;

public class UtilitiesActiveQueueTest extends NbTestCase {

    public UtilitiesActiveQueueTest(String testName) {
        super(testName);
    }

    public void testRunnableReferenceIsExecuted () throws Exception {
        Object obj = new Object ();
        RunnableRef ref = new RunnableRef (obj);
        synchronized (ref) {
            obj = null;
            assertGC ("Should be GCed quickly", ref);
            ref.wait ();
            assertTrue ("Run method has been executed", ref.executed);
        }
    }
    
    public void testRunnablesAreProcessedOneByOne () throws Exception {
        Object obj = new Object ();
        RunnableRef ref = new RunnableRef (obj);
        ref.wait = true;
        
        
        synchronized (ref) {
            obj = null;
            assertGC ("Is garbage collected", ref);
            ref.wait ();
            assertTrue ("Still not executed, it is blocked", !ref.executed);
        }    

        RunnableRef after = new RunnableRef (new Object ());
        synchronized (after) {
            assertGC ("Is garbage collected", after);
            after.wait (100); // will fail
            assertTrue ("Even if GCed, still not processed", !after.executed);
        }

        synchronized (after) {
            synchronized (ref) {
                ref.notify ();
                ref.wait ();
                assertTrue ("Processed", ref.executed);
            }
            after.wait ();
            assertTrue ("Processed too", after.executed);
        }
    }
    
    public void testManyReferencesProcessed() throws InterruptedException {
        int n = 10;
        Object[] objects = new Object[n];
        ExpensiveRef[] refs = new ExpensiveRef[n];
        for (int i = 0; i < n; i++) {
            objects[i] = new Object();
            refs[i] = new ExpensiveRef(objects[i], Integer.toString(i));
        }
        objects = null;
        for (int i = 0; i < n; i++) {
            assertGC("is GC'ed", refs[i]);
        }
        for (int i = 0; i < n; i++) {
            synchronized (refs[i]) {
                while (!refs[i].executed) {
                    refs[i].wait();
                }
            }
        }
    }
    
    public void testCallingPublicMethodsThrowsExceptions () {
        try {
            BaseUtilities.activeReferenceQueue().poll();
            fail ("One should not call public method from outside");
        } catch (RuntimeException ex) {
        }
        try {
            BaseUtilities.activeReferenceQueue ().remove ();
            fail ("One should not call public method from outside");
        } catch (InterruptedException ex) {
        }
        try {
            BaseUtilities.activeReferenceQueue ().remove (10);
            fail ("One should not call public method from outside");
        } catch (InterruptedException ex) {
        }
    }
    
    private static class RunnableRef extends WeakReference<Object>
    implements Runnable {
        public boolean wait;
        public boolean entered;
        public boolean executed;
        
        public RunnableRef (Object o) {
            this(o, BaseUtilities.activeReferenceQueue());
        }
        
        public RunnableRef(Object o, ReferenceQueue<Object> q) {
            super(o, q);
        }
        
        public synchronized void run () {
            entered = true;
            if (wait) {
                // notify we are here
                notify ();
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
            }
            executed = true;
            
            notifyAll ();
        }
    }
    
    private static class ExpensiveRef extends WeakReference<Object>
    implements Runnable {
        public boolean executed;
        private final String name;
        
        public ExpensiveRef (Object o, String name) {
            super(o, BaseUtilities.activeReferenceQueue());
            this.name = name;
        }
        
        @Override
        public synchronized void run () {
            executed = true;
            try {
                Thread.sleep(10);
                System.gc();
                Thread.sleep(10);
            } catch (InterruptedException iex) {}
            notifyAll ();
            System.err.println(name+" executed.");
        }
    }
}
