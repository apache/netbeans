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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/** Tests issue 96282 - Memory leak in org.netbeans.core.windows.services.NbPresenter
 *
 * @author Jiri Rechtacek
 */
public class NbPresenterLeakTest extends NbTestCase {

    public NbPresenterLeakTest (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return false;
    }

    @RandomlyFails // NB-Core-Build #1189
    public void testLeakingNbPresenterDescriptor () throws InterruptedException, InvocationTargetException {
        try {
            Class.forName("java.lang.AutoCloseable");
        } catch (ClassNotFoundException ex) {
            // this test is known to fail due to JDK bugs 7070542 & 7072167
            // which are unlikely to be fixed on JDK6. Thus, if AutoCloseable
            // is not present, skip the test
            return;
        }
        
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels(), null);
        wizardDescriptor.setModal (false);
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        WeakReference<WizardDescriptor> w = new WeakReference<WizardDescriptor> (wizardDescriptor);
        
        SwingUtilities.invokeAndWait (new EDTJob(dialog, true));
        assertShowing("button is visible", true, dialog);
        SwingUtilities.invokeAndWait (new EDTJob(dialog, false));
        assertShowing("button is no longer visible", false, dialog);
        boolean cancelled = wizardDescriptor.getValue() !=
            WizardDescriptor.FINISH_OPTION;
        Dialog d = new JDialog();
        
        // workaround for JDK bug 6575402
        JPanel p = new JPanel();
        d.setLayout(new BorderLayout());
        d.add(p, BorderLayout.CENTER);
        JButton btn = new JButton("Button");
        p.add(btn, BorderLayout.NORTH);
        
        SwingUtilities.invokeAndWait (new EDTJob(d, true));
        assertShowing("button is visible", true, btn);
        dialog.setBounds(Utilities.findCenterBounds(dialog.getSize()));
        SwingUtilities.invokeAndWait (new EDTJob(d, false));
        assertShowing("button is no longer visible", false, btn);

        assertNull ("BufferStrategy was disposed.", dialog.getBufferStrategy ());

        RepaintManager rm = RepaintManager.currentManager(dialog);
        rm.setDoubleBufferingEnabled(!rm.isDoubleBufferingEnabled());
        rm.setDoubleBufferingEnabled(!rm.isDoubleBufferingEnabled());
        
        dialog = null;
        wizardDescriptor = null;
        
        SwingUtilities.invokeAndWait (new Runnable() {

            @Override
            public void run() {
                Frame f = new Frame();
                f.setPreferredSize( new Dimension(100,100));
                f.setVisible( true );
                JDialog dlg = new JDialog(f);
                dlg.setVisible(true);
            }
        });

        assertGC ("Dialog disappears.", w);
    }

    private static void assertShowing(String msg, boolean showing, final Component c)
    throws InterruptedException, InvocationTargetException {
        final boolean[] res = { false };
        for (int i = 0; i < 50; i++) {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    res[0] = c.isShowing();
                }
            });
            if (showing == res[0]) {
                break;
            }
            Thread.sleep(100);
        }
        assertEquals(msg, showing, res[0]);
    }
    
    private static class EDTJob implements Runnable {
        private Dialog d;
        private boolean visibility;
        
        EDTJob (Dialog d, boolean vis) {
            this.d = d;
            visibility = vis;
        }
        public void run() {
            d.setVisible(visibility);
            if (!visibility) {
                d.dispose();
            }
        }
    }
    
    private WizardDescriptor.Panel<Object>[] getPanels () {
        WizardDescriptor.Panel p1 = new WizardDescriptor.Panel () {
            public Component getComponent() {
                return new JLabel ("test");
            }

            public HelpCtx getHelp() {
                return null;
            }

            public void readSettings(Object settings) {
            }

            public void storeSettings(Object settings) {
            }

            public boolean isValid() {
                return true;
            }

            public void addChangeListener(ChangeListener l) {
            }

            public void removeChangeListener(ChangeListener l) {
            }
        };
        
        return new WizardDescriptor.Panel [] {p1};
    }
    
    
    
}
