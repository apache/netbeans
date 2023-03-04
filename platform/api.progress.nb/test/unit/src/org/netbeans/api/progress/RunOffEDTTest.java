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

import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Holy
 */
public class RunOffEDTTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(RunOffEDTTest.class);
    }

    {
        System.setProperty("org.netbeans.modules.progress.ui.WARNING_TIME", "1000");
    }

    public RunOffEDTTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    private static class R implements Runnable {

        int runCount;
        Thread runT;
        CountDownLatch l;

        public void run() {
            runCount++;
            runT = Thread.currentThread();
            if (l != null) {
                try {
                    l.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public void testOutOfEDTRunsImmediately() {
        R r = new R();
        ProgressUtils.runOffEventDispatchThread(r, "Simple", new AtomicBoolean(), true);
        assertSame("Should be invoked by calling thread", Thread.currentThread(), r.runT);
        assertEquals("Should run once", 1, r.runCount);
    }

    public void testCallerBlockedUntilFinished() throws Exception {
        final AtomicBoolean passed = new AtomicBoolean(false);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                final AtomicBoolean finished = new AtomicBoolean(false);
                final int[] cnt = new int[]{0};
                final Thread[] t = new Thread[]{null};
                Runnable r = new Runnable() {

                    public void run() {
                        cnt[0]++;
                        t[0] = Thread.currentThread();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        finished.set(true);
                    }
                };
                ProgressUtils.runOffEventDispatchThread(r, "Test", new AtomicBoolean(false), true);
                passed.set(finished.get() && cnt[0] == 1 && t[0] != Thread.currentThread());
            }
        });
        assertTrue(passed.get());
    }

    @RandomlyFails // NB-Core-Build #9702, #9871, #9876, #9895, #9938: Unstable
    public void testCallerBlockedUntilCanceledOperationFinished() throws Exception {
        final R r = new R();
        r.l = new CountDownLatch(1);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ProgressUtils.runOffEventDispatchThread(r, "Test", new AtomicBoolean(false), true, 10, 100);
            }
        });
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (w != null) {
                w.setVisible(false);
                break;
            }
        }

        final AtomicBoolean finished = new AtomicBoolean(false);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                finished.set(true);
            }
        });
        Thread.sleep(100);
        assertFalse("should not run yet", finished.get());
        r.l.countDown();
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
        assertTrue("should be finished now", finished.get());
        assertEquals("Should run once", 1, r.runCount);
    }

    public void testContinueBeforeCanceledOperationFinished() throws Exception {
        final R r = new R();
        r.l = new CountDownLatch(1);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                if (w != null) {
                    w.setVisible(false);
                }
            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                ProgressUtils.runOffEventDispatchThread(r, "Test", new AtomicBoolean(false), false, 10, 100);
            }
        });

        final AtomicBoolean finished = new AtomicBoolean(false);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                finished.set(true);
            }
        });
        assertTrue("Should be finished", finished.get());
        r.l.countDown();
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
        assertEquals("Should run once", 1, r.runCount);
    }

    @RandomlyFails
    public void testISEThrownIfCanceledOperationNotFinishedInTime() throws Exception {
        final R r = new R();
        r.l = new CountDownLatch(1);
        final AtomicBoolean ex = new AtomicBoolean(false);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    ProgressUtils.runOffEventDispatchThread(r, "testExceptionIfCanceledOperationNotFinishedInTime", new AtomicBoolean(false), true, 10, 500);
                } catch (IllegalStateException e) {
                    ex.set(true);
                }
            }
        });
        Window w = null;
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
            w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (w != null) {
                w.setVisible(false);
                break;
            }
        }
        assertNotNull(w);
        Thread.sleep(1100);
        assertTrue("ISE should be thrown", ex.get());
        r.l.countDown();
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
    }

    public void testDlgIsShown() throws Exception {
        final R r = new R();
        r.l = new CountDownLatch(1);
        final boolean[] shown = new boolean[] { false };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                if (w != null) {
                    r.l.countDown();
                    shown[0] = true;
                }
            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                ProgressUtils.runOffEventDispatchThread(r, "Test", new AtomicBoolean(false), true, 10, 100);
            }
        });
        if (!shown[0]) {
            fail("Dialog was not shown");
        }
    }

    /* No longer consistently pass after 42651596988d, TBD why:
    public void testNoWarningMsg() throws Exception {
        CharSequence s = Log.enable("org.netbeans.modules.progress.ui", Level.WARNING);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                ProgressUtils.runOffEventDispatchThread(new SR(1000), "Test", new AtomicBoolean(false), true);
                ProgressUtils.runOffEventDispatchThread(new SR(10), "Test", new AtomicBoolean(false), true);
                ProgressUtils.runOffEventDispatchThread(new SR(100), "Test", new AtomicBoolean(false), true);
            }
        });
        assertFalse("Warning should be logged", s.toString().indexOf("Lengthy operation") >= 0);
    }

    public void testWarningMsgIfOperationLengthyTooOften() throws Exception {
        CharSequence s = Log.enable("org.netbeans.modules.progress.ui", Level.WARNING);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                Runnable r = new SR(1000);
                ProgressUtils.runOffEventDispatchThread(r, "Test", new AtomicBoolean(false), true);
                ProgressUtils.runOffEventDispatchThread(r, "Test", new AtomicBoolean(false), true);
            }
        });
        assertTrue("Warning should be logged", s.toString().indexOf("Lengthy operation") >= 0);
    }
     */

    private static class SR implements Runnable {

        long sleepTime;

        public SR(long sleepTime) {
            this.sleepTime = sleepTime;
        }

        public void run() {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };
}
