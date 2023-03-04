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

package org.netbeans.core.windows.services;

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class NotifyLaterTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NotifyLaterTest.class);
    }

    Logger LOG;
    
    
    public NotifyLaterTest (String testName) {
        super (testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }
    
    
    
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
    }

    @Override
    protected boolean runInEQ () {
        return false;
    }
    private void waitAWT() throws Exception {
        LOG.fine("waitAWT - enter");
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { 
            LOG.fine("waitAWT - in AWT");
        } });
        LOG.fine("waitAWT - done");
    }
    
    protected NotifyDescriptor createDescriptor(Object msg) {
        return new NotifyDescriptor.Message(msg);
    }

    public void testIfLasterWhenSplashShownThanWaitTillItFinished() throws Exception {
        class MyObj extends JComponent {
            public int called;

            @Override
            public void addNotify() {
                called = 1;
                LOG.log(Level.INFO, "addNotify called=" + called, new Exception("Stacktrace"));
                super.addNotify();
            }
        }
        MyObj obj = new MyObj();

        LOG.info("createDescriptor");
        NotifyDescriptor ownerDD = createDescriptor(obj);
        LOG.info("createDescriptor = " + ownerDD);

        LOG.info("notifyLater");
        DialogDisplayer.getDefault ().notifyLater(ownerDD);
        LOG.info("done notifyLater");
        waitAWT();
        LOG.info("check");
        assertEquals("No notify yet", 0, obj.called);//fail("Ok");
        
        DialogDisplayerImplTest.postInAwtAndWaitOutsideAwt(new Runnable () {
            public void run() {
                DialogDisplayerImpl.runDelayed();
            }
        });
        
        
        waitAWT();
        assertEquals("Now it is showing", 1, obj.called);
        
        assertTrue("Is visible", obj.isShowing());
        Window root = SwingUtilities.getWindowAncestor(obj);
        assertNotNull("There is parent window", root);
        assertTrue("It is a dialog", root instanceof JDialog);
        JDialog d = (JDialog)root;
        assertEquals("The owner of d is the same as owner of dialog without owner", new JDialog().getParent(), d.getParent());
        
        SwingUtilities.invokeAndWait(new Runnable () {
            public void run() {
                DialogDisplayerImpl.runDelayed();
            }
        });
    }
}
