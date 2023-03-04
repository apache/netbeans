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

package org.netbeans.core.execution;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.ProgressUIWorker;
import org.openide.actions.ActionManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class PendingTaskTest extends NbTestCase {
    
    public PendingTaskTest(String testName) {
	super(testName);
    }

    public void testActionManagersInvokeAction() throws InterruptedException {
        class BlockingAction extends AbstractAction implements Runnable {
            public synchronized void actionPerformed(ActionEvent e) {
                notifyAll();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail("No InterruptedException please");
                }
            }

            
            public void run() {
                ActionManager.getDefault().invokeAction(this, new ActionEvent(this, 0, ""));
            }
        }
        
        BlockingAction b = new BlockingAction();
        

        assertEquals("No tasks now", Install.getPendingTasks().size(), 0);
        
        RequestProcessor.Task t;
        synchronized (b) {
            t = RequestProcessor.getDefault().post(b);
            b.wait();
        }
        
        assertEquals("One action in progress", 1, Install.getPendingTasks().size());
        
        synchronized (b) {
            b.notifyAll();
        }
        t.waitFinished();
        
    	assertEquals("Action finished", Install.getPendingTasks().size(), 0);
    }

    @RandomlyFails // NB-Core-Build #8375
    public void testProgressTasks() throws InterruptedException {
        class MyWorker implements ProgressUIWorker {
            int cnt;
        
            public synchronized void processProgressEvent(ProgressEvent event) {
                cnt++;
                getLog().println("processProgressEvent: " + event);
                notifyAll();
            }
            public void processSelectedProgressEvent(ProgressEvent event) {
                getLog().println("processSelectedProgressEvent: " + event);
            }

            public synchronized void waitForEvent() throws InterruptedException {
                int prev = cnt;
                getLog().println("waitForEvent before wait");
                wait(5000);
                getLog().println("waitForEvent after wait");
                if (prev == cnt) {
                    fail("Time out - no event delivered");
                }
            }
        }
        
        MyWorker worker = new MyWorker();
        Controller.defaultInstance = new Controller(worker);
        
        ProgressHandle proghandle = ProgressHandleFactory.createHandle("a1");
        proghandle.setInitialDelay(0);
        
        assertEquals("None before", 0, Install.getPendingTasks().size());

        synchronized (worker) {
            getLog().println("proghandle - start");
            proghandle.start();
            worker.waitForEvent();
        }
            
        assertEquals("One now", 1, Install.getPendingTasks().size());
	
        // waiting a while to overcome possible optimizations in progress api
        // that prevent the finish event to be delivered
        Thread.sleep(1000);
        
        synchronized (worker) {
            getLog().println("proghandle - finish");
            proghandle.finish();
            worker.waitForEvent();
        }
        
        assertEquals("None after", 0, Install.getPendingTasks().size());
    }

}
