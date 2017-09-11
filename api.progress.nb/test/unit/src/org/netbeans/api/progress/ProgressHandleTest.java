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

package org.netbeans.api.progress;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.UIInternalHandle;
import org.netbeans.modules.progress.spi.ProgressUIWorker;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.SwingController;
import org.openide.util.Cancellable;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public class ProgressHandleTest extends NbTestCase {
    
    ProgressHandle proghandle;
    UIInternalHandle handle;
    public ProgressHandleTest(String testName) {
        super(testName);
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    @Override
    protected void setUp() throws Exception {
        Controller.defaultInstance = new TestController();
        proghandle = ProgressHandleFactory.createHandle("displayName",new Cancellable() {
            public boolean cancel() {
                // empty
                return true;
            }
        });
        handle = (UIInternalHandle)proghandle.getInternalHandle();
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

    /**
     * Test of getDisplayName method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testGetDisplayName() {
        assertEquals("displayName", handle.getDisplayName());
    }

    /**
     * Test of getState method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testGetState() {
        assertEquals(UIInternalHandle.STATE_INITIALIZED, handle.getState());

        // finishing task before it's started does not throw ISE any more - #186366
        proghandle.finish();
        
        proghandle.start();
        assertEquals(UIInternalHandle.STATE_RUNNING, handle.getState());
        
        // restarting already started task does not throw an ISE any more - #186366
        proghandle.start();
        // package private call, user triggered cancel action.
        handle.requestCancel();
        assertEquals(UIInternalHandle.STATE_REQUEST_STOP, handle.getState());
        proghandle.finish();
        assertEquals(UIInternalHandle.STATE_FINISHED, handle.getState());
    }

    /**
     * Test of isAllowCancel method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testIsAllowCancel() {
        assertTrue(handle.isAllowCancel());
        ProgressHandle h2 = ProgressHandleFactory.createHandle("ds2");
        UIInternalHandle handle2 = (UIInternalHandle)h2.getInternalHandle();
        assertFalse(handle2.isAllowCancel());
    }

    /**
     * Test of isCustomPlaced method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testIsCustomPlaced() {
        assertFalse(handle.isCustomPlaced());
        JComponent comp = ProgressHandleFactory.createProgressComponent(proghandle);
        assertTrue(handle.isCustomPlaced());
        JLabel main = ProgressHandleFactory.createMainLabelComponent(proghandle);
        assertTrue(handle.isCustomPlaced());
        assertNotNull(main);
        JLabel detail = ProgressHandleFactory.createDetailLabelComponent(proghandle);
        assertTrue(handle.isCustomPlaced());
        assertNotNull(detail);
                
        boolean ok = false;
        try {
            // cannot get the custom component multiple times..
            comp = ProgressHandleFactory.createProgressComponent(proghandle);
        } catch (IllegalStateException exc) {
            ok = true;
        }
        
        assertTrue(ok);
    }
    
    /**
     * Test of custom placed labels of class org.netbeans.progress.api.ProgressHandle.
     */
    @RandomlyFails // NB-Core-Build #1175
    public void testCustomPlacedLabels() throws Exception {
        assertFalse(handle.isCustomPlaced());
        ProgressHandleFactory.createProgressComponent(proghandle);
        JLabel main = ProgressHandleFactory.createMainLabelComponent(proghandle);
        JLabel detail = ProgressHandleFactory.createDetailLabelComponent(proghandle);
        proghandle.start();
        proghandle.setDisplayName("test1");
        proghandle.progress("message1");
        // kind of bad to have the wait here to overcome the scheduling..
        // cannot use the TestController here.. the custom placed stuff has own controller.
        try {
            Thread.sleep(600);
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    //oh well, anything that posts to other threads is not really testable
                    //this could help in corner cases when sleep alone doesn't help
                }
            });
            Thread.sleep(600);
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    //oh well, anything that posts to other threads is not really testable
                    //this could help in corner cases when sleep alone doesn't help
                }
            });
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        assertEquals("test1", main.getText());
        assertEquals("message1", detail.getText());
    }
    
    
    // tasks shorter than the InternalHandle.INITIAL_DELAY should be discarded.
    @RandomlyFails // NB-Core-Build #1210
    public void testIfShortOnesGetDiscarded() throws Exception {
        OneThreadController control = new OneThreadController(new FailingUI());
        Controller.defaultInstance = control;
        proghandle = ProgressHandleFactory.createHandle("a1");
        proghandle.start();
        proghandle.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.runNow();
        //after running the timer sould be stopped
        assertTrue(control.tobeRestartedDelay == -1);

        
        proghandle = ProgressHandleFactory.createHandle("a2");
        ProgressHandle h2 = ProgressHandleFactory.createHandle("a3");
        proghandle.start();
        proghandle.progress("");
        try {
            Thread.sleep(300);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        
        //simulate timer run
        control.runNow();
        // timer should continue
        assertFalse(control.tobeRestartedDelay == -1);
        
        h2.start();
        h2.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.runNow();
        // timer should be continuing
        assertFalse(control.tobeRestartedDelay == -1);
        
        try {
            Thread.sleep(300);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        h2.finish();
        //simulate timer run
        control.runNow();
        // timer should be stopped
        assertTrue(control.tobeRestartedDelay == -1);
        
    }
    
    // tasks shorter than the custom init delay should be discarded.
    @RandomlyFails // NB-Core-Build #1204
    public void testIfCustomShortOnesGetDiscarded() throws Exception {
        System.out.println("testIfCustomShortOnesGetDiscarded");
        OneThreadController control = new OneThreadController(new FailingUI());
        Controller.defaultInstance = control;
        proghandle = ProgressHandleFactory.createHandle("c1");
        proghandle.setInitialDelay(100);
        proghandle.start();
        proghandle.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.runNow();
        //after running the timer sould be stopped
        assertTrue(control.tobeRestartedDelay == -1);
        
        proghandle = ProgressHandleFactory.createHandle("c2");
        ProgressHandle h2 = ProgressHandleFactory.createHandle("c3");
        proghandle.setInitialDelay(100);
        proghandle.start();
        proghandle.progress("");
        try {
            Thread.sleep(50);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        //simulate timer run
        control.runNow();
        // timer should continue
        assertFalse(control.tobeRestartedDelay == -1);
        
        h2.setInitialDelay(1000);
        h2.start();
        h2.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.runNow();
        // timer should be continuing
        assertFalse(control.tobeRestartedDelay == -1);
        
        try {
            Thread.sleep(600);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        h2.finish();
        control.runNow();
        // timer should NOT continue
        assertTrue(control.tobeRestartedDelay == -1);
    }    
    
    private class FailingUI implements ProgressUIWorker {
            public void processProgressEvent(ProgressEvent event) {
                fail("How come we are processing a short one - " + event.getSource().getDisplayName());
            }
            public void processSelectedProgressEvent(ProgressEvent event) {
                fail("How come we are processing a short one - " + event.getSource().getDisplayName());
            }
    }
    
    private class OneThreadController extends SwingController {
        
        public int tobeRestartedDelay = -1;
        
        public OneThreadController(ProgressUIWorker comp) {
            super(comp);
        }
        
        @Override
        protected void resetTimer(int initialDelay, boolean restart) {
            super.resetTimer(initialDelay, restart);
            if (restart) {
                tobeRestartedDelay = initialDelay;
            } else {
                tobeRestartedDelay = -1;
            }
        }
    }

    @RandomlyFails // NB-Core-Build #808
    public void testIfLongOnesGetProcessed() throws Exception {
        assert !SwingUtilities.isEventDispatchThread();
        PingingUI ui = new PingingUI();
        Controller.defaultInstance = new Controller(ui);
        proghandle = ProgressHandleFactory.createHandle("b1");
        proghandle.start();
        try {
            Thread.sleep(800);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        proghandle.finish();
        assertTrue(ui.pinged);
    } 

    @RandomlyFails // if sleep is too short
    public void testIfCustomLongOnesGetProcessed() throws Exception {
        assert !SwingUtilities.isEventDispatchThread();
        PingingUI ui = new PingingUI();
        Controller.defaultInstance = new Controller(ui);
        proghandle = ProgressHandleFactory.createHandle("b1");
        proghandle.setInitialDelay(100);
        proghandle.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        proghandle.finish();

        assertTrue(ui.pinged);
    }        
    
    private class PingingUI implements ProgressUIWorker {
        public boolean pinged = false;
            public void processProgressEvent(ProgressEvent event) {
                pinged = true;
            }
            public void processSelectedProgressEvent(ProgressEvent event) {
                pinged = true;
            }
    }
    

/**
 * test switching in non-status bar component
 */
    @RandomlyFails // NB-Core-Build #7997
    public void testSwitch() throws Exception {
        final int WAIT = 1500;

class MyFrame extends JFrame implements Runnable {
    
    JComponent component;
    
    public MyFrame(JComponent component) {
        getContentPane().add(component);
    }
    
    public void run() {
        setVisible(true);
        setBounds(0, 0, 400, 50);
    }
}
        
        assertFalse(SwingUtilities.isEventDispatchThread());
        ProgressHandle h = ProgressHandleFactory.createHandle("foo");
        JComponent component = ProgressHandleFactory.createProgressComponent(h);
        

        
        h.start();
        
        final MyFrame frm = new MyFrame(component);
        SwingUtilities.invokeLater(frm);
        
        try {
            Thread.sleep(WAIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        
        
        h.switchToDeterminate(100);
        h.progress(50);
        
        try {
            Thread.sleep(WAIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            Method meth = component.getClass().getMethod("isIndeterminate");
            Boolean bool = (Boolean) meth.invoke(component);
            assertFalse("The progress bar is still indeterminate!", bool);
        h.finish();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frm.setVisible(false);
                frm.dispose();
            }
        });
    }    

    @Override
    protected boolean runInEQ() {
        return false;
    }

    
    public void testManyWorkUnits () {
        int units = 500000;
        int step = 500;
        proghandle.start (units * step);
        for (int i = 200; i < units; i = i+ 50) {
            proghandle.progress (i * step);
            assertTrue(handle.getPercentageDone() >= 0);
        }
        proghandle.finish (); 
    }
    
    
}
