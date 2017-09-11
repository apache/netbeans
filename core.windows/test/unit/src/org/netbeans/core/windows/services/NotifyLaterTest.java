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
