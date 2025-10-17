/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.progress.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.JFrame;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.Workspace;

/**
 *
 * @author Tim Boudreau
 */
public class RunOffEDTImplTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(RunOffEDTImplTest.class);
    }

    public RunOffEDTImplTest(String name) {
        super(name);
    }

    public void setUp() {
        MockServices.setServices(WM.class, RunOffEDTImpl.class);
    }

    private static boolean canTestGlassPane() {
        Map<?,?> hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
        //Avoid translucent painting on, for example, remote X terminal
        return hintsMap == null || !RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.equals(hintsMap.get(RenderingHints.KEY_TEXT_ANTIALIASING));
    }

    public void testShowProgressDialogAndRun_3args_1_EQ() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    testShowProgressDialogAndRun_3args_1();
                } finally {
                    latch.countDown();
                }
            }
        });
        latch.await();
    }

    public void testShowProgressDialogAndRun_3args_2_EQ() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    testShowProgressDialogAndRun_3args_2();
                } catch (AssertionFailedError e) {
                    throw e;
                } catch (Exception ex) {
                    throw new RuntimeException (ex);
                } finally {
                    latch.countDown();
                }
            }
        });
        latch.await();
    }

    public void testShowProgressDialogAndRunLater() throws Exception {
        final WM wm = Lookup.getDefault().lookup(WM.class);
        //make sure main window is on screen before proceeding
        wm.await();
        final JFrame jf = (JFrame) WindowManager.getDefault().getMainWindow();
        final CountDownLatch countDown = new CountDownLatch(1);
        final AtomicBoolean glassPaneFound = new AtomicBoolean(false);
        final boolean testGlassPane = canTestGlassPane();
        class R implements ProgressRunnable<String> {
            volatile boolean hasRun;
            @Override
            public String run(ProgressHandle handle) {
                try {
                    wm.waitForGlassPane();
                    if (testGlassPane) {
                        glassPaneFound.set(jf.getGlassPane() instanceof RunOffEDTImpl.TranslucentMask);
                    }
                    hasRun = true;
                    return "Done";
                } finally {
                    countDown.countDown();
                }
            }
        }
        R r = new R();
        Future<String> f = ProgressUtils.showProgressDialogAndRunLater(r, ProgressHandle.createHandle("Something"), true);
        assertNotNull (f);
        assertEquals ("Done", f.get());
        countDown.await();
        assertTrue (r.hasRun);
        assertFalse (f.isCancelled());
        assertTrue (f.isDone());
        assertEquals ("Done", f.get());
        assertTrue ("Glass pane not set", !testGlassPane || glassPaneFound.get());
    }

    public void testShowProgressDialogAndRunLaterEQ() throws Throwable {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    testShowProgressDialogAndRunLater();
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            }
        });
    }

    public void testShowProgressDialogAndRun_3args_1() {
        assertEquals ("Done", ProgressUtils.showProgressDialogAndRun(new CB(), "Doing Stuff", true));
    }

    public void testShowProgressDialogAndRun_3args_2() throws InterruptedException {
        final WM wm = Lookup.getDefault().lookup(WM.class);
        //make sure main window is on screen before proceeding
        wm.await();
        final JFrame jf = (JFrame) WindowManager.getDefault().getMainWindow();
        final CountDownLatch countDown = new CountDownLatch(1);
        final AtomicBoolean glassPaneFound = new AtomicBoolean(false);
        final boolean testGlassPane = canTestGlassPane();
        class R implements Runnable {
            volatile boolean hasRun;
            @Override
            public void run() {
                try {
                    wm.waitForGlassPane();
                    if (testGlassPane) {
                        glassPaneFound.set(jf.getGlassPane() instanceof RunOffEDTImpl.TranslucentMask);
                    }
                    hasRun = true;
                } finally {
                    countDown.countDown();
                }
            }
        }
        R r = new R();
        ProgressUtils.showProgressDialogAndRun(r, "Something");
        countDown.await();
        assertTrue (r.hasRun);
        assertTrue ("Glass pane not set", !testGlassPane || glassPaneFound.get());
    }

    public void testStressWithRandomContention() throws InterruptedException, Exception {
        Random r = new Random(123456789);
        for (int i = 0; i < 30; i++) {
            testShowProgressDialogAndRun_3args_1();
            createRandomContention(r);
            testShowProgressDialogAndRun_3args_1_EQ();
            createRandomContention(r);
            testShowProgressDialogAndRun_3args_2();
            createRandomContention(r);
            testShowProgressDialogAndRun_3args_2_EQ();
        }
    }

    private void createRandomContention(final Random r) throws Exception {
        if (r.nextInt(10) > 7) {
            RequestProcessor rp = new RequestProcessor(this.getClass().getName());
            for (int i= 0; i < r.nextInt(5); i++) {
                rp.post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            //No, Jesse, this will not affect the test
                            //passing or failing unless something else is
                            //broken
                            Thread.sleep(r.nextInt(200));
                        } catch (InterruptedException ex) {
                            //do nothing
                        }
                    }
                });
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(r.nextInt(200));
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
    }

    private class CB implements ProgressRunnable<String> {

        @Override
        public String run(ProgressHandle handle) {
            handle.switchToDeterminate(5);
            for (int i= 0; i < 5; i++) {
                handle.progress("Job " + i, i);
            }
            return "Done";
        }

    }

    private static final class JF extends JFrame {
        boolean inited;
        private final CountDownLatch waitForGlassPane = new CountDownLatch(1);

        JF () {
            super ("Main Window");
            inited = true;
        }

        @Override
        public void addNotify() {
            super.addNotify();
            inited = true;
        }

        @Override
        public void setGlassPane(java.awt.Component glassPane) {
            super.setGlassPane(glassPane);
            if (inited && glassPane instanceof RunOffEDTImpl.TranslucentMask) {
                waitForGlassPane.countDown();
            }
        }

    }

    public static final class WM extends WindowManager implements WindowListener {

        private final JF jf = new JF();
        private CountDownLatch latch = new CountDownLatch(1);
        public WM() {
            jf.addWindowListener(this);
            jf.setPreferredSize(new Dimension(400, 400));
            jf.setVisible(true);
        }

        public void waitForGlassPane() {
            try {
                jf.waitForGlassPane.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void await() {
            if (!jf.isDisplayable()) {
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        @Override
        public Mode findMode(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Mode findMode(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<? extends Mode> getModes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Frame getMainWindow() {
            return jf;
        }

        @Override
        public void updateUI() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected Component createTopComponentManager(TopComponent c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace createWorkspace(String name, String displayName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace findWorkspace(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace[] getWorkspaces() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWorkspaces(Workspace[] workspaces) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace getCurrentWorkspace() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TopComponentGroup findTopComponentGroup(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentOpen(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentClose(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentRequestActive(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentRequestVisible(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentDisplayNameChanged(TopComponent tc, String displayName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentToolTipChanged(TopComponent tc, String toolTip) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentIconChanged(TopComponent tc, Image icon) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentActivatedNodesChanged(TopComponent tc, Node[] activatedNodes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean topComponentIsOpened(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected Action[] topComponentDefaultActions(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String topComponentID(TopComponent tc, String preferredID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TopComponent findTopComponent(String tcID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void windowOpened(WindowEvent e) {
            latch.countDown();
        }

        @Override
        public void windowClosing(WindowEvent e) {
            //do nothing
        }

        @Override
        public void windowClosed(WindowEvent e) {
            //do nothing
        }

        @Override
        public void windowIconified(WindowEvent e) {
            //do nothing
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            //do nothing
        }

        @Override
        public void windowActivated(WindowEvent e) {
            //do nothing
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            //do nothing
        }
    }
}
