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
package org.openide;


import org.netbeans.api.progress.ProgressHandle;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.InstantiatingIteratorTest.Listener;
import org.openide.util.HelpCtx;

/** Testing functional implementation calling the methods to interface <code>WizardDescriptor.ProgressInstantiatingIterator</code>
 * from WizardDescriptor. Check if the method <code>instantiate()</code> is called outside AWT in particular.
 * @see Issue 58889
 */
public class ProgressInstantiatingIteratorTest extends AsynchronousInstantiatingIteratorTest {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ProgressInstantiatingIteratorTest.class);
    }

    public ProgressInstantiatingIteratorTest (String name) {
        super(name);
    }
    

    private Iterator iterator;

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
                assertNull ("WD.P.storeSettings() called before WD.I.instantiate()", iterator.result);
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
    
    public class Iterator implements WizardDescriptor.ProgressInstantiatingIterator {
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
        public void initialize (WizardDescriptor wizard) {
            helpSet = new HashSet ();
            panels[0] = new Panel("first panel");
            panels[1] = new Panel("second panel");
            initialized = Boolean.TRUE;
        }
        public void uninitialize (WizardDescriptor wizard) {
            helpSet.clear ();
            initialized = Boolean.FALSE;
            panels = null;
        }

        public Set instantiate (ProgressHandle handle) throws IOException {
            assertNotNull ("ProgressHandle cannot be null", handle);
            if (checkIfInAWT) {
                if (SwingUtilities.isEventDispatchThread ()) {
                    throw new IOException ("Cannot run in AWT queue.");
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

        public Set instantiate () throws IOException {
            fail ("Cannot call this method iff implement ProgressInstantiatingIterator!");
            return null;
        }
    }
    
}
