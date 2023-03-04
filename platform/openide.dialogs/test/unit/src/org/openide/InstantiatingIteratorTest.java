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


import org.netbeans.junit.NbTestSuite;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.*;
import org.openide.util.HelpCtx;

/** Testing functional implementation calling the methods to interface <code>WizardDescriptor.InstantiatingIterator</code>
 * from WizardDescriptor.
 */
public class InstantiatingIteratorTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InstantiatingIteratorTest.class);
    }

    public InstantiatingIteratorTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (InstantiatingIteratorTest.class));
        System.exit (0);
    }
    
    protected WizardDescriptor wd;
    protected String exceptedValue;
    private Iterator iterator;
    protected int attachedInIterator = 0;
    protected int attachedInPanel = 0;
    protected boolean checkOrder = false;
    protected boolean shouldThrowException = false;
    protected boolean uninitializeShouldThrowException = false;
    protected Set/*<ChangeListener>*/ changeListenersInIterator = new HashSet ();
    protected Set/*<ChangeListener>*/ changeListenersInPanel = new HashSet ();
    protected boolean checkIfInAWT;

    protected void setUp () {
        iterator = new Iterator ();
        wd = new WizardDescriptor (iterator);
        wd.addPropertyChangeListener(new Listener ());
        java.awt.Dialog d = DialogDisplayer.getDefault ().createDialog (wd);
        checkOrder = false;
        shouldThrowException = false;
        //d.show();
    }
    
    /** Run all tests in AWT thread */
    protected boolean runInEQ() {
        return true;
    }
    
    public void testCleanChangeListenerAfterFinish () {
        assertEquals ("One listener is attached.", 1, changeListenersInIterator.size ());
        wd.doNextClick ();
        assertEquals ("Still only one listener is attached after Next.", 1, changeListenersInIterator.size ());
        wd.doPreviousClick ();
        assertEquals ("Still only one listener is attached after Previous.", 1, changeListenersInIterator.size ());
        finishWizard (wd);
        assertEquals ("No one listener is attached after Finish.", 0, changeListenersInIterator.size ());
        assertEquals ("No one listener is attached in WD.Panel after Finish.", 0, changeListenersInPanel.size ());
    }
    
    public void testCleanChangeListenerAfterCancel () {
        assertEquals ("One listener is attached.", 1, changeListenersInIterator.size ());
        wd.doCancelClick ();
        assertEquals ("No one listener is attached after Cancel.", 0, changeListenersInIterator.size ());
        assertEquals ("No one listener is attached in WD.Panel after Finish.", 0, changeListenersInPanel.size ());
    }
    
    public void testInitializeIterator () throws Exception {
        assertTrue ("InstantiatingIterator was initialized.", getInitialized ().booleanValue ());
        assertNull ("InstantiatingIterator wasn't instantiated.", getResult ());
    }

    public void testUninitializeIterator () throws Exception {
        assertTrue ("InstantiatingIterator was initialized at start.", getInitialized ().booleanValue ());
        wd.doCancelClick ();
        assertFalse ("InstantiatingIterator was uninitialized after cancel.", getInitialized ().booleanValue ());
        assertNull ("InstantiatingIterator wasn't instantiated.", getResult ());
    }

    public void testFinishAndUninitializeIterator () throws Exception {
        assertTrue ("InstantiatingIterator was initialized at start.", getInitialized ().booleanValue ());
        wd.doNextClick ();
        assertTrue ("InstantiatingIterator wasn't uninitialized after next.", getInitialized ().booleanValue ());
        finishWizard (wd);
        assertFalse ("InstantiatingIterator wasn uninitialized after finish.", getInitialized ().booleanValue ());
        assertNotNull ("InstantiatingIterator was instantiated.", getResult ());
    }

    public void testUninitializeIteratorAndCalledCurrent () throws Exception {
        assertTrue ("InstantiatingIterator was initialized at start.", getInitialized ().booleanValue ());
        wd.doNextClick ();
        assertTrue ("InstantiatingIterator wasn't uninitialized after next.", getInitialized ().booleanValue ());
        finishWizard (wd);
        assertFalse ("InstantiatingIterator was uninitialized after finish.", getInitialized ().booleanValue ());
        assertNotNull ("InstantiatingIterator was instantiated.", getResult ());
    }

    public void testOrderStoreSettingAndInstantiate () throws Exception {
        checkOrder = true;
        wd.doNextClick ();
        finishWizard (wd);
        assertNotNull ("InstantiatingIterator was instantiated.", getResult ());
    }

    public void testGetInstantiatedObjects () throws Exception {
        wd.doNextClick ();
        finishWizard (wd);
        assertNotNull ("InstantiatingIterator was instantiated.", getResult ());
        Set newObjects = wd.getInstantiatedObjects ();
        assertEquals ("WD returns same objects as InstantiatingIterator instantiated.", getResult (), newObjects);
        
    }
    
    public void testInstantiateInAWTQueueOrNot () {
        checkIfInAWT = true;

        wd.doNextClick ();
        finishWizard (wd);
        try {
            Set newObjects = wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            fail ("IllegalStateException was caught because WD.instantiate() called outside AWT queue.");
        }
        assertNotNull ("InstantiatingIterator was correctly instantiated.", getResult ());
    }
    
    public void testFinishOptionWhenInstantiateFails () throws Exception {
        shouldThrowException = true;

        wd.doNextClick ();
        Object state = wd.getValue();
        finishWizard (wd);
        
        assertNull ("InstantiatingIterator was not correctly instantiated.", getResult ());
        try {
            Set newObjects = wd.getInstantiatedObjects ();
            fail ("No IllegalStateException was caught. Should be thrown when invoked getInstantiatedObjects() on unfinished wizard.");
        } catch (IllegalStateException ise) {
            // correct behavior
        }
        assertEquals ("The state is same as before instantiate()", state, wd.getValue ());
    }
    
    public void testFinishOptionWhenUninitializeThrowsError () throws Exception {
        shouldThrowException = false;
        uninitializeShouldThrowException = true;

        wd.doFinishClick();
        Object state = wd.getValue();
        finishWizard (wd);
        
        assertEquals ("The state is same as before instantiate()", state, wd.getValue ());
    }
    
    public class Panel implements WizardDescriptor.FinishablePanel {
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
            changeListenersInPanel.add (l);
        }
        
        public HelpCtx getHelp() {
            return null;
        }
        
        public boolean isValid() {
            return true;
        }
        
        public void readSettings(Object settings) {
            log ("readSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
        
        public void removeChangeListener(ChangeListener l) {
            changeListenersInPanel.remove (l);
        }
        
        public void storeSettings(Object settings) {
            if (checkOrder) {
                assertNull ("WD.P.storeSettings() called before WD.I.instantiate()", getResult ());
                // bugfix #45093, remember storeSettings could be called multiple times
                // do check order only when the first time
                checkOrder = false;
            }
            log ("storeSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
            if (exceptedValue != null) {
                assertEquals ("WD.getValue() returns excepted value.", exceptedValue, handleValue (wd.getValue ()));
            }
        }
        
        public boolean isFinishPanel () {
            return true;
        }
        
    }
    
    protected Boolean getInitialized () {
        return iterator.initialized;
    }
    
    protected Set getResult () {
        return iterator.result;
    }
    
    public class Iterator implements WizardDescriptor.InstantiatingIterator {
        int index = 0;
        WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[2];
        java.util.Set helpSet;
        
        private Boolean initialized = null;
        private Set result = null;
        
        public WizardDescriptor.Panel current () {
            assertTrue ("WD.current() called on initialized iterator.", initialized != null && initialized.booleanValue ());
            return panels[index];
        }
        public String name () {
            return "Test iterator";
        }
        public boolean hasNext () {
            return index < 1;
        }
        public boolean hasPrevious () {
            return index > 0;
        }
        public void nextPanel () {
            if (!hasNext ()) throw new NoSuchElementException ();
            index ++;
        }
        public void previousPanel () {
            if (!hasPrevious ()) throw new NoSuchElementException ();
            index --;
        }
        public void addChangeListener (ChangeListener l) {
            changeListenersInIterator.add (l);
        }
        public void removeChangeListener (ChangeListener l) {
            changeListenersInIterator.remove (l);
        }
        public java.util.Set instantiate () throws IOException {
            if (checkIfInAWT) {
                if (! SwingUtilities.isEventDispatchThread ()) {
                    throw new IOException ("Must run in AWT queue.");
                }
            }
            if (shouldThrowException) {
                throw new IOException ("Test throw IOException during instantiate().");
            }
            if (initialized.booleanValue ()) {
                helpSet.add ("member");
                result = helpSet;
            } else {
                result = null;
            }
            return result;
        }
        public void initialize (WizardDescriptor wizard) {
            helpSet = new HashSet ();
            panels[0] = new Panel("first panel");
            panels[1] = new Panel("second panel");
            initialized = Boolean.TRUE;
        }
        public void uninitialize (WizardDescriptor wizard) {
            if (uninitializeShouldThrowException) {
                throw new RuntimeException ("test");
            }
            helpSet.clear ();
            initialized = Boolean.FALSE;
            panels = null;
        }
    }
    
    public class Listener implements PropertyChangeListener {
        
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
    
    public static void finishWizard (WizardDescriptor wd) {
        wd.doFinishClick ();
        WizardDescriptor.ASYNCHRONOUS_JOBS_RP.post (new Runnable () {
            public void run () {}
        }).waitFinished ();
    }
}
