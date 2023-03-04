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
package org.netbeans.modules.openide.loaders;

import java.awt.EventQueue;
import java.util.concurrent.Semaphore;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AWTTaskTest extends NbTestCase {

    public AWTTaskTest(String name) {
        super(name);
    }
    
    public void testWaitWithTimeOut() throws InterruptedException {
        class Block implements Runnable {
            Task toWait;
            
            @Override
            public synchronized void run() {
                while (toWait == null) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                try {
                    assertTrue("Finished", toWait.waitFinished(300));
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                toWait = null;
                notifyAll();
            }
            
            public synchronized void goOn(Task toWait) throws InterruptedException {
                this.toWait = toWait;
                notifyAll();
                while (this.toWait != null) {
                    wait();
                }
            }
        }
        Block b = new Block();
        EventQueue.invokeLater(b);
        
        class R implements Runnable {
            int run;

            @Override
            public void run() {
                run++;
            }
        }
        R run = new R();
        AWTTask at = new AWTTask(run, null);
        assertFalse("Does not finish", at.waitFinished(1000));
        b.goOn(at);
        assertEquals("Executed once", 1, run.run);
    }
    
    public void testWaitForItself() {
        final Semaphore s = new Semaphore(0);
        class R implements Runnable {
            int cnt;
            volatile Task waitFor;

            @Override
            public void run() {
                cnt++;
                try {
                    s.acquire(); // Ensure waitFor != null.
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                waitFor.waitFinished();
            }
        }
        
        R r = new R();
        r.waitFor = new AWTTask(r, null);
        s.release();
        r.waitFor.waitFinished();
        
        assertEquals("Executed once", 1, r.cnt);
    }
    
    public void testInvokedOnce() {
        assertInvokedOnce(false);
    }
    public void testInvokedOnceWithTimeOut() {
        assertInvokedOnce(true);
    }
    
    private void assertInvokedOnce(final boolean withTimeOut) {
        class Cnt implements Runnable {
            int cnt;

            @Override
            public void run() {
                assertTrue("In AWT", EventQueue.isDispatchThread());
                cnt++;
                AWTTask.flush();
            }
        }
        class CntAndWait implements Runnable {
            Cnt snd;
            int cnt;
            
            @Override
            public void run() {
                snd = new Cnt();
                final AWTTask[] waitFor = { null };
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        waitFor[0] = new AWTTask(snd, null);
                    }
                }).waitFinished();
                
                if (withTimeOut) {
                    try {
                        waitFor[0].waitFinished(1000);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else {
                    waitFor[0].waitFinished();
                }
                cnt++;
                assertEquals("Already invoked", 1, snd.cnt);
            }
        }
        CntAndWait first = new CntAndWait();
        new AWTTask(first, null).waitFinished();
        assertEquals("Main invoked", 1, first.cnt);
        Cnt third = new Cnt();
        new AWTTask(third, null).waitFinished();
        assertEquals("Invoked once 3rd", 1, third.cnt);
        assertEquals("Invoked once inner", 1, first.snd.cnt);
    }
}
