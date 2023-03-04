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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.util.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** 
 * @see Issue 160712
 * @author Jiri Rechtacek
 */
public class WizardDescriptorWhenClosedWindowTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(WizardDescriptorWhenClosedWindowTest.class);
    }

    public WizardDescriptorWhenClosedWindowTest (String name) {
        super (name);
    }
    private Iterator iterator;
    private WizardDescriptor wd;
    private Dialog d;
    private Object expectedValue = null;
    private Object resultValue = null;

    @Override
    protected void setUp () {
        iterator = new Iterator ();
        wd = new WizardDescriptor (iterator);
        wd.setModal (false);
        d = DialogDisplayer.getDefault ().createDialog (wd);
        d.setVisible (true);
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    /** Run all tests in AWT thread */
    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testCloseOnCancel () {
        expectedValue = WizardDescriptor.CANCEL_OPTION;
        wd.doCancelClick ();
        assertEquals ("The resultValue is " + expectedValue, expectedValue, resultValue);
    }

    public void testCloseOnFinish () {
        expectedValue = WizardDescriptor.FINISH_OPTION;
        wd.doFinishClick ();
        assertEquals ("The resultValue is " + expectedValue, expectedValue, resultValue);
    }

    public void testCloseOnCloseWidget () throws InterruptedException {
        expectedValue = WizardDescriptor.CLOSED_OPTION;
        d.dispatchEvent (new WindowEvent (d, WindowEvent.WINDOW_CLOSING));
        assertEquals ("The resultValue is " + expectedValue, expectedValue, resultValue);
    }

    public void testDoNext () {
        expectedValue = WizardDescriptor.NEXT_OPTION;
        wd.doNextClick ();
        assertEquals ("The resultValue is " + expectedValue, expectedValue, resultValue);
    }

    public void testDoNextThenCancel () {
        expectedValue = WizardDescriptor.NEXT_OPTION;
        wd.doNextClick ();
        expectedValue = WizardDescriptor.CANCEL_OPTION;
        wd.doCancelClick ();
        assertEquals ("The resultValue is " + expectedValue, expectedValue, resultValue);
    }

    public void testDoNextThenCloseOnCloseWidget () throws InterruptedException {
        expectedValue = WizardDescriptor.NEXT_OPTION;
        wd.doNextClick ();
        expectedValue = WizardDescriptor.CLOSED_OPTION;
        d.dispatchEvent (new WindowEvent (d, WindowEvent.WINDOW_CLOSING));
        assertEquals ("The resultValue is " + expectedValue, expectedValue, resultValue);
    }

    public void testDoNextThenFinish () {
        expectedValue = WizardDescriptor.NEXT_OPTION;
        wd.doNextClick ();
        expectedValue = WizardDescriptor.FINISH_OPTION;
        wd.doFinishClick ();
        assertEquals ("The resultValue is " + expectedValue, expectedValue, resultValue);
    }

    public class Panel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

        private JLabel component;
        private String text;

        public Panel (String text) {
            this.text = text;
        }

        public Component getComponent () {
            if (component == null) {
                component = new JLabel (text);
            }
            return component;
        }

        public void addChangeListener (ChangeListener l) {
        }

        public HelpCtx getHelp () {
            return null;
        }

        public boolean isValid () {
            return true;
        }

        public void readSettings (WizardDescriptor settings) {
        }

        public void removeChangeListener (ChangeListener l) {
        }

        public void storeSettings (WizardDescriptor settings) {
            assertEquals ("The expectedValue is " + expectedValue, expectedValue, settings.getValue ());
            resultValue = expectedValue;
        }

        public boolean isFinishPanel () {
            return true;
        }
    }

    public class Iterator implements WizardDescriptor.Iterator<WizardDescriptor> {
        public Iterator () {
            panels[0] = new Panel ("first panel");
            panels[1] = new Panel ("second panel");
        }

        int index = 0;
        WizardDescriptor.Panel<WizardDescriptor> panels[] = new Panel[2];

        public WizardDescriptor.Panel<WizardDescriptor> current () {
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
            if ( ! hasNext ()) {
                throw new NoSuchElementException ();
            }
            index ++;
        }

        public void previousPanel () {
            if ( ! hasPrevious ()) {
                throw new NoSuchElementException ();
            }
            index --;
        }

        public void addChangeListener (ChangeListener l) {
        }

        public void removeChangeListener (ChangeListener l) {
        }

    }
}
