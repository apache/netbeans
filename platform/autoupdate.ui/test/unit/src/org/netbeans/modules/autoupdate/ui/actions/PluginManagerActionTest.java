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
