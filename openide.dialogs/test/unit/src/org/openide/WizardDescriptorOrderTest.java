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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.openide;


import org.netbeans.junit.NbTestSuite;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.*;
import org.openide.util.HelpCtx;

/** Testing the order of calling the WizardDescriptor support methods and
 * the methods in implemented wizard's interfaces.
 * @see issue 46587, issue 46589
 * @author  Jiri Rechtacek, Jesse Glick
 * 
 */
public class WizardDescriptorOrderTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(WizardDescriptorOrderTest.class);
    }

    public WizardDescriptorOrderTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (WizardDescTest.class));
        System.exit (0);
    }
    
    WizardDescriptor wd;
    TestPanel[] panels;
    
    // help fields to control the order of calls to panel
    int[] readSettingsCalls;
    int[] storeSettingsCalls;
    
    // help variables
    boolean checkStoreBeforeNext;
    boolean checkStoreOldeBeforeReadNew;
    boolean checkPCH2FinishOption;

    @Override
    protected final void setUp () {
        panels = new TestPanel[] {new TestPanel (0), new TestPanel (1)};
        readSettingsCalls = new int[] {0, 0};
        storeSettingsCalls = new int[] {0, 0};
        wd = new WizardDescriptor (new TestIterator (panels[0], panels[1]));
        wd.addPropertyChangeListener(new Listener());
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog (wd);
        //d.show();

        checkStoreBeforeNext = false;
        checkStoreOldeBeforeReadNew = false;
        checkPCH2FinishOption = false;
    }

    @Override
    protected final boolean runInEQ () {
        return true;
    }
    
    public void testReadSettingsOnFirstPanel () throws Exception {
        log ("Wizard has been initialized.");
        assertTrue ("WD.P.readSettings on the first panel has been called.", readSettingsCalls[0] > 0);
    }

    public void testOrderNextPanelAndStoreSettings () throws Exception {
        checkStoreBeforeNext = true;
        log ("Do click Next button.");
        wd.doNextClick ();
    }

    public void testStoreOldBeforeReadNew () throws Exception {
        checkStoreOldeBeforeReadNew = true;
        log ("Do click Next button.");
        wd.doNextClick ();
    }

    public void testStoreLastBeforePCH2Finish () throws Exception {
        log ("Do click Next button.");
        wd.doNextClick ();
        checkPCH2FinishOption = true;
        log ("Do click Finish button.");
        wd.doFinishClick ();
    }

    public class Listener implements java.beans.PropertyChangeListener {
        
        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
            if (WizardDescriptor.PROP_VALUE.equals(propertyChangeEvent.getPropertyName ())) {
                log("propertyChange [time: " + System.currentTimeMillis () +
                                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
                
                if (checkPCH2FinishOption && WizardDescriptor.FINISH_OPTION.equals (propertyChangeEvent.getNewValue ())) {
                    assertTrue ("WD.P.storeSettings on the last panel has been called before propertyChangeEvent to FINISH_OPTION was fired.", storeSettingsCalls[1] > 0);
                }
            }
        }
        
    }
    
    private final class TestPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
        
        private final int x;
        
        public TestPanel(int x) {
            this.x = x;
        }
        
        public void readSettings(WizardDescriptor settings) {
            readSettingsCalls[x]++;
            log ("readSettings of panel: " + x + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
        
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void validate() throws WizardValidationException {
            assertEquals( "validate() must be called before storeSettings()", 0, storeSettingsCalls[x] );
        }
        
        public void storeSettings(WizardDescriptor settings) {
            if (checkStoreOldeBeforeReadNew && (x > 0)) {
                assertTrue ("WD.P.storeSettings on the previous panel has been called before WD.P.readSettings on next panel.", readSettingsCalls[x - 1] > 0);
            }
            storeSettingsCalls[x] ++;
            log ("storeSettings of panel: " + x + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
            
        public void removeChangeListener(ChangeListener l) {}
        
        public boolean isValid() {
            return true;
        }
        public HelpCtx getHelp() {
            return null;
        }
        
        public Component getComponent() {
            return new JLabel("panel #" + x);
        }
    }
    
    private final class TestIterator implements WizardDescriptor.Iterator<WizardDescriptor> {
        private final TestPanel panel1, panel2;
        private int which = 0;
        public TestIterator(TestPanel panel1, TestPanel panel2) {
            this.panel1 = panel1;
            this.panel2 = panel2;
        }
        public boolean hasNext () {
            return which == 0;
        }
        public WizardDescriptor.Panel<WizardDescriptor> current() {
            log ("current: " + name());
            TestPanel currentPanel = which == 0 ? panel1 : panel2;
            return currentPanel;
        }
        public void nextPanel() {
            if (checkStoreBeforeNext) {
                assertTrue ("WD.P.storeSettings on the previous panel has been called before WD.I.nextPanel.", storeSettingsCalls[which] > 0);
            }
            which ++;
        }
        public void removeChangeListener(ChangeListener l) {}
        public boolean hasPrevious() {
            return which > 0;
        }
        public void previousPanel() {
            which --;
        }
        public String name() {
            return which == 0 ? "First Panel" : "Second Panel";
        }
        public void addChangeListener(ChangeListener l) {}
    }
    
    public String handleValue (Object val) {
        if (val == null) return "NULL";
        if (val instanceof String) return (String) val;
        if (WizardDescriptor.FINISH_OPTION.equals (val)) return "FINISH_OPTION";
        if (WizardDescriptor.CANCEL_OPTION.equals (val)) return "CANCEL_OPTION";
        if (WizardDescriptor.CLOSED_OPTION.equals (val)) return "CLOSED_OPTION";
        if (val instanceof JButton) {
            JButton butt = (JButton) val;
            ResourceBundle b = NbBundle.getBundle ("org.openide.Bundle"); // NOI18N
            if (b.getString ("CTL_NEXT").equals (butt.getText ())) return "NEXT_OPTION";
            if (b.getString ("CTL_PREVIOUS").equals (butt.getText ())) return "NEXT_PREVIOUS";
            if (b.getString ("CTL_FINISH").equals (butt.getText ())) return "FINISH_OPTION";
            if (b.getString ("CTL_CANCEL").equals (butt.getText ())) return "CANCEL_OPTION";
        }
        return "UNKNOWN OPTION: " + val;
    }
    
}
