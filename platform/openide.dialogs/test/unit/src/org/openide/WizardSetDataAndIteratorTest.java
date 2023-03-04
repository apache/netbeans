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
import java.io.IOException;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;

/**
 *
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class WizardSetDataAndIteratorTest extends NbTestCase {

    public WizardSetDataAndIteratorTest(String s) {
        super(s);
    }

    @Override
    protected final boolean runInEQ () {
        return true;
    }
    
    public void testSetDataAndIterator() throws Exception {
        MyWizard w = new MyWizard(new MyIter ());
        assertTrue("Finish enabled", w.isFinishEnabled());        
        assertEquals("Right Settings passed", w, MyPanel.set);
    }

    public void testInitializeAfterSetDataAndIterator () {
        MyInstantiatingIter myIt = new MyInstantiatingIter ();
        MyWizard w = new MyWizard (myIt);
        /* fails because rollback of issue
        assertTrue ("InstantiatingIterator.initialize() called.", myIt.initialized);
        assertEquals ("Initialized called on correct wizard.", w, myIt.initializedOnWD);
         * */
    }

    private static class MyWizard extends WizardDescriptor {
        public MyWizard(WizardDescriptor.Iterator<MyWizard> it) {
            super();
            MyPanel.set = null;
            setPanelsAndSettings(it, this);
        }
    }
    
    private static class MyIter implements WizardDescriptor.Iterator<MyWizard> {
        private MyPanel myPanel = new MyPanel();

        public Panel<org.openide.WizardSetDataAndIteratorTest.MyWizard> current() {
            return myPanel;
        }

        public String name() {
            return "OneName";
        }

        public boolean hasNext() {
            return false;
        }

        public boolean hasPrevious() {
            return false;
        }

        public void nextPanel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void previousPanel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }

    private static class MyInstantiatingIter extends MyIter implements WizardDescriptor.InstantiatingIterator<MyWizard> {
        public boolean initialized = false;
        public WizardDescriptor initializedOnWD = null;
        public Set instantiate () throws IOException {
            fail ("No reason to call instantiate.");
            return null;
        }

        public void initialize (WizardDescriptor wizard) {
            initialized = true;
            initializedOnWD = wizard;
        }

        public void uninitialize (WizardDescriptor wizard) {
            initialized = false;
            initializedOnWD = null;
        }
    }

    private static class MyPanel implements WizardDescriptor.Panel<MyWizard> {
        private static MyWizard set;
        
        private JPanel cmp = new JPanel();
        
        public Component getComponent() {
            return cmp;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void readSettings(org.openide.WizardSetDataAndIteratorTest.MyWizard settings) {
            assertNull("Not yet set", set);
            set = settings;
        }

        public void storeSettings(org.openide.WizardSetDataAndIteratorTest.MyWizard settings) {
        }

        public boolean isValid() {
            return true;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }
}
