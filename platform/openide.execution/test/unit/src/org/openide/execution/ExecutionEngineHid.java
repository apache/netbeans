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

package org.openide.execution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertSame;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.InputOutput;

/** A piece of the test compatibility suite for the execution APIs.
 *
 * @author Jaroslav Tulach
 */
public class ExecutionEngineHid extends TestCase {
    
    public ExecutionEngineHid(String testName) {
        super(testName);
    }
    
    public void testGetDefault() {
        ExecutionEngine result = ExecutionEngine.getDefault();
        assertNotNull(result);
    }
    
    public void testExecuteIsNonBlocking() throws Exception {
        class Block implements Runnable {
            public boolean here;
            
            
            public synchronized void run() {
                here = true;
                notifyAll();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
            public synchronized void waitForRun() throws InterruptedException {
                while(!here) {
                    wait();
                }
                notifyAll();
            }
        }
        Block block = new Block();
        
        
        ExecutionEngine instance = ExecutionEngine.getDefault();
        ExecutorTask result = instance.execute("My task", block, InputOutput.NULL);
        
        assertFalse("Of course it is not finished, as it is blocked", result.isFinished());
        block.waitForRun();
        
        int r = result.result();
        assertEquals("Default result is 0", 0, r);
    }
    
    /**
     * An extended version of compatibility test that checks the ExecutionEngine impl
     * restores the contents of the default Lookup for the executing task. Recommended
     * for all ExecutionEngine implementations.
     */
    public static class WithLookup extends ExecutionEngineHid {
        
        private static class Token {}
        
        Token check = new Token();
        Token check2 = new Token();

        public WithLookup(String testName) {
            super(testName);
        }
        
        /**
         * Checks that the same contents of Lookup.getDefault() effective when the
         * execution task is created is also in place when the task is run.
         */
        public void testExecutionMaintainLookup() throws Exception {
            Worker w = new Worker();
            
            assertNull(Lookup.getDefault().lookup(Token.class));
            w.checkInExecutedRunnable().get();
        }
        
        /**
         * Checks that the main Lookup's contents is not affected at the time
         * the Runnable already reports the changed Lookup.
         */
        public void testRunnableAndMainDiffers() throws Exception {
            Worker w = new Worker();
            
            assertNull(Lookup.getDefault().lookup(Token.class));
            // stop the runnable after the first check.
            w.s.drainPermits();
            CompletableFuture<Token> cf = w.checkInExecutedRunnable();
            
            // wait until after the 1st assert
            w.s2.acquire();
            // check the main Lookup is still unaffected
            assertNull(Lookup.getDefault().lookup(Token.class));
            // release the runnable for 2nd check
            w.s.release();
            
            assertSame(w.check, cf.get());
        }
        
        /**
         * Checks that two tasks running in parallel has separate Lookup contents
         * and the main Lookup is unaffected.
         * 
         * @throws Exception 
         */
        public void testSeparateRunnableContexts() throws Exception {
            Worker w = new Worker();
            Worker w2 = new Worker();
            
            assertNull(Lookup.getDefault().lookup(Token.class));
            // stop the runnable after the first check.
            w.s.drainPermits();
            CompletableFuture<Token> cf = w.checkInExecutedRunnable();
            // wait until after the 1st assert
            w.s2.acquire();
            
            CompletableFuture<Token> cf2 = w2.checkInExecutedRunnable();
            // also stop after the 1st check
            w2.s.drainPermits();
            
            // check the main Lookup is still unaffected
            assertNull(Lookup.getDefault().lookup(Token.class));
            
            // release the runnable for 2nd check
            w.s.release();
            w2.s.release();
            
            assertSame(w.check, cf.get());
            assertSame(w2.check, cf2.get());
        }
        
        static class Worker {
            Semaphore s = new Semaphore(1);
            Semaphore s2 = new Semaphore(0);
            CountDownLatch l = new CountDownLatch(1);
            volatile Token check = new Token();
            volatile Exception e = null;
            volatile Error e2 = null;
            volatile CompletableFuture<Token> future = new CompletableFuture<>();
            
            public CompletableFuture<Token> checkInExecutedRunnable() throws Exception {
                Lookup newLkp = new ProxyLookup(Lookups.fixed(check), Lookup.getDefault());
                Lookups.executeWith(newLkp, this::taskLauncher);
                return future;
            }

            private void executedRunnable() {
                Token t = null;
                try {
                    assertSame(check, t = Lookup.getDefault().lookup(Token.class));
                    s2.release();
                    s.acquire();
                    assertSame(check, t = Lookup.getDefault().lookup(Token.class));
                } catch (Exception | Error e) {
                    future.completeExceptionally(e);
                } finally {
                    future.complete(t);
                }
            }

            private void taskLauncher() {
                assertSame(check, Lookup.getDefault().lookup(Token.class));
                ExecutionEngine.getDefault().execute("Test", this::executedRunnable, InputOutput.NULL);
            }
        }
    }
}
