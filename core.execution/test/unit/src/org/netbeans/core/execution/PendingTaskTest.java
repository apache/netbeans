/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
