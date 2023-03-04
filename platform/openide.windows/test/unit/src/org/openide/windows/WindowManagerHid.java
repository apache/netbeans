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

package org.openide.windows;

import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;

/** A piece of the test compatibility suite for the execution APIs.
 *
 * @author Jaroslav Tulach
 */
public class WindowManagerHid extends NbTestCase {
    
    public WindowManagerHid(String testName) {
        super(testName);
    }
    
    public void testGetDefault() {
        WindowManager result = WindowManager.getDefault();
        assertNotNull(result);
    }
    
    public void testInvokeWhenUIReady() throws Exception {
        class R implements Runnable {
            public boolean started;
            public boolean finished;
            public boolean block;
            
            public synchronized void run() {
                assertTrue("Runs only in AWT thread", SwingUtilities.isEventDispatchThread());
                try {
                    started = true;
                    notifyAll();
                    if (block) {
                        wait();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                finished = true;
                notifyAll();
            }
        }
        
        R run = new R();
        R snd = new R();
        
        WindowManager wm = WindowManager.getDefault();
        synchronized (run) {
            wm.invokeWhenUIReady(run);
            run.block = true;
            run.wait();
        }
        assertTrue("started", run.started);
        assertFalse("but not finished", run.finished);
        
        wm.invokeWhenUIReady(snd);
        
        Thread.sleep(100);
        
        assertFalse("Not started", snd.started);
        synchronized (snd) {
            synchronized (run) {
                run.notifyAll();
                run.wait();
            }
            assertTrue("run is finished", run.finished);
            snd.wait();
            assertTrue("snd also finished", snd.finished);
        }
    }
}
