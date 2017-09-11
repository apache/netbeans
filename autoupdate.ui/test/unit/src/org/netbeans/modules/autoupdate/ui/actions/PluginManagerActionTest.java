/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.ui.actions;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;

/**
 *
 * @author Jiri Rechtacek
 */
public class PluginManagerActionTest extends NbTestCase {
    
    public PluginManagerActionTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected boolean runInEQ () {
        return false;
    }
    
    private PluginManagerUI ui;
    private Reference<PluginManagerUI> ref;
    private JDialog dlg;
    private JButton close = null;
    
    private static Object help;

    public void testMemLeakPluginManagerUI () throws Exception {
        help = this;
        
        close = new JButton ();
        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                ui = new PluginManagerUI (close);
            }
        });
        
        assertNotNull (ui);
        ref = new WeakReference<PluginManagerUI> (ui);
        assertNotNull (ref.get ());
        
        dlg = new JDialog (new JFrame ());
        dlg.setBounds (100, 100, 800, 500);
        dlg.add (ui);

        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                dlg.setVisible (true);
            }
        });
        ui.initTask.waitFinished ();
        Thread.sleep (1000);
        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                dlg.setVisible (false);
                dlg.getContentPane ().removeAll ();
                dlg.dispose ();
            }
        });
        Thread.sleep (10500);
        //dlg = null;
        close = null;

        ui = null;
        assertNull (ui);
        
        ToolTipManager.sharedInstance ().mousePressed (null);
        // sun.management.ManagementFactoryHelper.getDiagnosticMXBean().dumpHeap("/tmp/heapdump2.out", true);
        assertGC ("Reference to PluginManagerUI is empty.", ref);

        assertNotNull (ref);
        assertNull (ref.get ());
    }

}
