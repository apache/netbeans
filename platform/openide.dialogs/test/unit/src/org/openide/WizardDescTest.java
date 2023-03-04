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
package org.openide;


import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Dialog;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.*;
import org.openide.util.HelpCtx;

/** Testing behaviour of WizardDescription in order to fix bug 35266
 ** @see issue 35266
 */
public class WizardDescTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(WizardDescTest.class);
    }

    public WizardDescTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (WizardDescTest.class));
        System.exit (0);
    }
    
    WizardDescriptor wd;
    String exceptedValue;

    protected final void setUp () {
        WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[2];
        panels[0] = new Panel("first panel");
        panels[1] = new Panel("second panel");
        wd = new WizardDescriptor(panels);
        wd.addPropertyChangeListener(new Listener());
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog (wd);
        //d.show();
    }
    
    public boolean runInEQ () {
        return true;
    }
    
    public void testNextOption () throws Exception {
        exceptedValue = "NEXT_OPTION";
        log ("Do click Next button.");
        wd.doNextClick ();

        assertEquals ("Closed with next option.", WizardDescriptor.NEXT_OPTION, wd.getValue ());
    }

    public void testPreviousOption () throws Exception {
        exceptedValue = "NEXT_OPTION";
        log ("Do click Next button.");
        wd.doNextClick ();

        exceptedValue = "PREVIOUS_OPTION";
        log ("Do click Previous button.");
        wd.doPreviousClick ();

        // failed because PREVIOUS_OPTION is replaced with NEXT_OPTION by WD.updateState()
        assertEquals ("Closed with previous option. \n (failed because PREVIOUS_OPTION is replaced with NEXT_OPTION by WD.updateState())", WizardDescriptor.PREVIOUS_OPTION, wd.getValue ());
    }

    public void testFinishOption () throws Exception {
        exceptedValue = "NEXT_OPTION";
        log ("Do click Next button.");
        wd.doNextClick ();

        exceptedValue = "FINISH_OPTION";
        log ("Do click Finish button.");
        wd.doFinishClick ();

        assertEquals ("Closed with finish option.", WizardDescriptor.FINISH_OPTION, wd.getValue ());
    }

    public void testCancelOption () throws Exception {
        exceptedValue = "NEXT_OPTION";
        log ("Do click Next button.");
        wd.doNextClick ();

        exceptedValue = "CANCEL_OPTION";
        log ("Do click Cancel button.");
        wd.doCancelClick ();

        assertEquals ("Closed with cancel option.", WizardDescriptor.CANCEL_OPTION, wd.getValue ());
    }

    public void testPerPanelHelpCtx () throws Exception {
        assertEquals( new HelpCtx("first panel"), wd.getHelpCtx() );
        log ("Do click Next button.");
        wd.doNextClick ();

        assertEquals( new HelpCtx("second panel"), wd.getHelpCtx() );
    }

    public void testWizarWideHelpCtx () throws Exception {
        wd.setHelpCtx( new HelpCtx("all panels") );
        assertEquals( new HelpCtx("all panels"), wd.getHelpCtx() );
        log ("Do click Next button.");
        wd.doNextClick ();

        assertEquals( new HelpCtx("all panels"), wd.getHelpCtx() );
    }

    public void testNextOptionWhenLazyValidationFails () throws Exception {
        Panel panels[] = new Panel[3];
        
        class MyPanel extends Panel implements WizardDescriptor.ValidatingPanel {
            public String validateMsg;
            public String failedMsg;
            
            public MyPanel () {
                super ("enhanced panel");
            }
            
            public void validate () throws WizardValidationException {
                if (validateMsg != null) {
                    failedMsg = validateMsg;
                    throw new WizardValidationException (null, "MyPanel.validate() failed.", validateMsg);
                }
                return;
            }
        }
        
        class MyFinishPanel extends MyPanel implements WizardDescriptor.FinishablePanel {
            public boolean isFinishPanel () {
                return true;
            }
        }
        
        MyPanel mp = new MyPanel ();
        MyFinishPanel mfp = new MyFinishPanel ();
        panels[0] = mp;
        panels[1] = mfp;
        panels[2] = new Panel ("Last one");
        wd = new WizardDescriptor(panels);
        Dialog dlg = DialogDisplayer.getDefault().createDialog( wd );
        
        assertNull ("Component has not been yet initialized", panels[1].component);
        mp.failedMsg = null;
        mp.validateMsg = "xtest-fail-without-msg";
        wd.doNextClick ();
        assertEquals ("The lazy validation failed on Next.", mp.validateMsg, mp.failedMsg);
        assertNull ("The lazy validation failed, still no initialiaation", panels[1].component);
        assertNull ("The lazy validation failed, still no initialiaation", panels[2].component);
        mp.failedMsg = null;
        mp.validateMsg = null;
        wd.doNextClick ();
        assertNull ("Validation on Next passes", mp.failedMsg);
        assertNotNull ("Now we switched to another panel", panels[1].component);
        assertNull ("The lazy validation failed, still no initialiaation", panels[2].component);
        
        // remember previous state
        Object state = wd.getValue();
        mfp.validateMsg = "xtest-fail-without-msg";
        mfp.failedMsg = null;
        wd.doFinishClick();
        assertEquals ("The lazy validation failed on Finish.", mfp.validateMsg, mfp.failedMsg);
        assertNull ("The validation failed, still no initialiaation", panels[2].component);
        assertEquals ("State is not FINISH if validation failed (#209510)", wd.getDefaultValue(), wd.getValue());
        
        mfp.validateMsg = null;
        mfp.failedMsg = null;
        wd.doFinishClick ();
        assertNull ("Validation on Finish passes", mfp.failedMsg);        
        assertNull ("Finish was clicked, no initialization either", panels[2].component);
        assertEquals ("The state is finish", WizardDescriptor.FINISH_OPTION, wd.getValue ());
    }
    
    public void testDynamicallyEnabledFinish () throws Exception {
        WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel [2];
        
        class MaybeFinishPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
            private JLabel component;
            private String text;
            
            public boolean isValid = true;
            public boolean isFinishPanel = true;
            
            public MaybeFinishPanel () {
                text = "maybe finish panel";
            }
            
            public boolean isFinishPanel () {
                return isFinishPanel;
            }
            
            public boolean isValid () {
                return isValid;
            }
            
            public MaybeFinishPanel (String text) {
                this.text = text;
            }

            public Component getComponent() {
                if (component == null) {
                    component = new JLabel (text);
                }
                return component;
            }

            public void addChangeListener(ChangeListener l) {
            }

            public HelpCtx getHelp() {
                return null;
            }

            public void readSettings(Object settings) {
            }

            public void removeChangeListener(ChangeListener l) {
            }

            public void storeSettings(Object settings) {
            }
        }
        
        MaybeFinishPanel firstPanel = new MaybeFinishPanel ();
        Panel normalPanel = new Panel ("normal panel");
        panels[0] = firstPanel;
        panels[1] = normalPanel;
        wd = new WizardDescriptor(panels);
        
        // if 1. panel is not valid then both button are disabled
        firstPanel.isValid = false;
        firstPanel.isFinishPanel = false;
        wd.updateState ();
        assertFalse ("Panel is not valid and Next button is disabled.", wd.isNextEnabled ());
        assertFalse ("Panel is not valid and Finish button is disabled as well.", wd.isFinishEnabled ());
        
        // now will be panel valid => next will be enabled and finish 
        // button disabled because this panel doesn't implement WD.FinishPanel
        firstPanel.isValid = true;
        wd.updateState ();
        assertTrue ("Panel is valid then Next button is enabled.", wd.isNextEnabled ());
        assertFalse ("Panel doesn't implement WD.FinishPanel.", panels[0] instanceof WizardDescriptor.FinishPanel);
        assertFalse ("Panel is valid but Finish button is disabled because not FinishPanel.", wd.isFinishEnabled ());

        // panel is valid and finish is enabled => next will be enabled and finish 
        // button enabled too because this panel implements WD.FinishablePanel
        // isFinishEnabled() returns true despite doesn't implement WD.FinishPanel
        firstPanel.isValid = true;
        firstPanel.isFinishPanel = true;
        wd.updateState ();
        assertTrue ("Panel is valid then Next button is enabled.", wd.isNextEnabled ());
        assertFalse ("Panel doesn't implement WD.FinishPanel.", panels[0] instanceof WizardDescriptor.FinishPanel);
        assertTrue ("Panel implements WD.FinishablePanel.", panels[0] instanceof WizardDescriptor.FinishablePanel);
        assertTrue ("Panel is enabled because implements FinishablePanel.", wd.isFinishEnabled ());
    }
    
    public void testGetInstantiatedObjectsWhenFinished () {
        boolean exceptionCaught = false;
        try {
            wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            exceptionCaught = true;
        }
        if (!exceptionCaught) {
            fail ("Call getInstantiatedObjects() only on finished wizard, not on start.");
            exceptionCaught = false;
        }
        
        log ("Do click Next button.");
        wd.doNextClick ();
        try {
            wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            exceptionCaught = true;
        }
        if (!exceptionCaught) {
            fail ("Call getInstantiatedObjects() only on finished wizard, not on next.");
            exceptionCaught = false;
        }
        

        log ("Do click Cancel button.");
        wd.doFinishClick ();
        try {
            wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            fail ("Called getInstantiatedObjects() on finished wizard.");
        }
    }
    
    public void testGetInstantiatedObjectsWhenCanceled () {
        boolean exceptionCaught = false;
        try {
            wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            exceptionCaught = true;
        }
        if (!exceptionCaught) {
            fail ("Call getInstantiatedObjects() only on finished wizard, not on start.");
            exceptionCaught = false;
        }
        
        log ("Do click Next button.");
        wd.doNextClick ();
        try {
            wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            exceptionCaught = true;
        }
        if (!exceptionCaught) {
            fail ("Call getInstantiatedObjects() only on finished wizard, not on next.");
            exceptionCaught = false;
        }
        

        log ("Do click Cancel button.");
        wd.doCancelClick ();
        try {
            wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            exceptionCaught = true;
        }
        if (!exceptionCaught) {
            fail ("Call getInstantiatedObjects() only on finished wizard, not when wizard canceled.");
            exceptionCaught = false;
        }
    }
    
    public class Panel implements WizardDescriptor.Panel, WizardDescriptor.FinishPanel {
        private JLabel component;
        private String text;
        public Panel(String text) {
            this.text = text;
        }
        
        public Component getComponent() {
            if (component == null) {
                component = new JLabel (text);
            }
            return component;
        }
        
        public void addChangeListener(ChangeListener l) {
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx(text);
        }
        
        public boolean isValid() {
            return true;
        }
        
        public void readSettings(Object settings) {
            log ("readSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public void storeSettings(Object settings) {
            log ("storeSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
            if (exceptedValue != null) {
                assertEquals ("WD.getValue() returns excepted value.", exceptedValue, handleValue (wd.getValue ()));
            }
        }
        
    }
    
    public class Listener implements java.beans.PropertyChangeListener {
        
        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
            if (WizardDescriptor.PROP_VALUE.equals(propertyChangeEvent.getPropertyName ())) {
                log("propertyChange [time: " + System.currentTimeMillis () +
                                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));

            }
        }
        
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
