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

package org.netbeans.api.progress;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEnvironment;
import org.netbeans.modules.progress.spi.UIInternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.ProgressUIWorker;
import org.netbeans.modules.progress.spi.SwingController;
import org.openide.util.Cancellable;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public class ProgressHandleFactoryTest extends NbTestCase {

    public ProgressHandleFactoryTest(String testName) {
        super(testName);
    }

    /**
     * Test of createHandle method, of class org.netbeans.progress.api.ProgressHandleFactory.
     */
    public void testCreateHandle() {
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("task 1");
        UIInternalHandle internal = (UIInternalHandle)handle.getInternalHandle();
        assertEquals("task 1", internal.getDisplayName());
        assertFalse(internal.isAllowCancel());
        assertFalse(internal.isCustomPlaced());
        assertEquals(UIInternalHandle.STATE_INITIALIZED, internal.getState());
        
        handle = ProgressHandleFactory.createHandle("task 2", new TestCancel());
        internal = (UIInternalHandle)handle.getInternalHandle();
        assertEquals("task 2", internal.getDisplayName());
        assertTrue(internal.isAllowCancel());
        assertFalse(internal.isCustomPlaced());
        assertEquals(UIInternalHandle.STATE_INITIALIZED, internal.getState());
        
    }

    @RandomlyFails // NB-Core-Build #1176
    public void testCustomComponentIsInitialized() {
        Controller.defaultInstance = new TestController();
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("task 1");

        // Warning, this will make the handle to work with a new Controller, not TestController.
        JComponent component = ProgressHandleFactory.createProgressComponent(handle);
        handle.start(15);
        handle.progress(2);
        waitForTimerFinish();
        
        assertEquals(15, ((JProgressBar) component).getMaximum());
        assertEquals(2, ((JProgressBar) component).getValue());
        
        handle = ProgressHandleFactory.createHandle("task 2");
        component = ProgressHandleFactory.createProgressComponent(handle);
        
        handle.start(20);
        waitForTimerFinish();
        
        assertEquals(20, ((JProgressBar) component).getMaximum());
        assertEquals(0, ((JProgressBar) component).getValue());
        
    }
    
    private static class AwtBlocker implements Runnable {

        public AwtBlocker(int blockingTime) {
            this.blockingTime = blockingTime;
        }

        public void run() {
            UIDefaults uidef = UIManager.getDefaults();
            synchronized (uidef) {
                blocking = true;
                sleep();
            }
        }
        synchronized void sleep() {
            try {
                wait(blockingTime);
            } catch (InterruptedException ex) {
            }
        }
        synchronized void wakeup() {
            notify();
        }
        public volatile boolean blocking = false;
        private int blockingTime;
    }

    /**
     * Tests if ProgressUIWorkerProvider is created inside awt thread (if not deadlock is possible)
     */
    public void testProgressCanBeCreatedOutOfSyncAwt() {
        Controller.defaultInstance = null;
        final int blockingTime = 10000;
        AwtBlocker blocker = new AwtBlocker(blockingTime);
        long start = System.currentTimeMillis();
        SwingUtilities.invokeLater(blocker);
        while (!blocker.blocking) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
        }

        ProgressHandle pHandle = ProgressHandleFactory.createHandle("Peforming operation...");
        pHandle.start();
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("Possible deadlock detected, ProgressUIWorkerProvider is creating UI outside AWT thread.", elapsed < blockingTime);
        pHandle.finish();
        blocker.wakeup();
    }

     private static class TestCancel implements Cancellable {
         public boolean cancel() {
             return true;
         }
         
   }
   
     
    private class TestController extends SwingController {
        public TestController() {
            super(new ProgressUIWorker() {
                public void processProgressEvent(ProgressEvent event) { }
                public void processSelectedProgressEvent(ProgressEvent event) { }
            });
        }
        
        public Timer getTestTimer() {
            return getTimer();
        }
    }
    
    private void waitForTimerFinish() {
        TestController tc = (TestController)Controller.defaultInstance;
        int count = 0;
        do {
            if (count > 10) {
                fail("Takes too much time");
            }
            try {
                count = count + 1;
                Thread.sleep(300);
            } catch (InterruptedException exc) {
                System.out.println("interrupted");
            }        
        } while (tc.getTestTimer().isRunning());

    }
    
    /**
     * Checks that handles produced by other env than org.netbeans.modules.progress.ui module
     * can still get some +- suitable Progress components.
     */
    public void testUnexpectedInternalHandleExtraction() throws Exception {
        TestProgressEnvironment.withEnvironment(new StrangeEnvironment(), () -> {
        
        ProgressHandle handle = ProgressHandle.createHandle("task 1");
        assertFalse(handle.getInternalHandle() instanceof UIInternalHandle);
        
        InternalHandle ih = handle.getInternalHandle();
        assertFalse(ih.isCustomPlaced());
        
        // now attempt to extract a component from it:
        JLabel l = ProgressHandleFactory.createMainLabelComponent(handle);
        assertNotNull(l);
        // the handle changed its placement:
        assertTrue(ih.isCustomPlaced());
        return null;
        });
    }

    /**
     * Checks semantics of the InternalHandle + extraction after the progress starts.
     */
    public void testUnexpectedInternalHandleExtractFailsAfterStart() throws Exception {
        TestProgressEnvironment.withEnvironment(new StrangeEnvironment(), () -> {
        ProgressHandle handle = ProgressHandle.createHandle("task 1");
        assertFalse(handle.getInternalHandle() instanceof UIInternalHandle);

        handle.start(100);
        
        // should throw an exception:
        try {
            JLabel l = ProgressHandleFactory.createMainLabelComponent(handle);
            fail("Exppected ISE.");
        } catch (IllegalStateException ex) {
            // OK
        }
        return null;
        });
    }
    
    public class StrangeHandle extends InternalHandle {
        public StrangeHandle(String displayName, Cancellable cancel, boolean userInitiated) {
            super(displayName, cancel, userInitiated);
        }
    }
    
    public class StrangeEnvironment implements ProgressEnvironment {

        @Override
        public ProgressHandle createHandle(String displayname, Cancellable c, boolean userInit) {
            return new StrangeHandle(displayname, c, userInit).createProgressHandle();
        }

        @Override
        public Controller getController() {
            return SwingController.getDefault();
        }
    }
}
