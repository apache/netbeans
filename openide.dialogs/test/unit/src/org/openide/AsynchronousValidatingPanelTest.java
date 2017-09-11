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

import java.awt.Component;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.*;
import org.openide.util.HelpCtx;

/** Test coveres implementation of issue 58530 - Background wizard validation.
 * @author Jiri Rechtacek
 */
public class AsynchronousValidatingPanelTest extends LoggingTestCaseHid {

    public AsynchronousValidatingPanelTest (String name) {
        super(name);
    }
    
    WizardDescriptor wd;
    String exceptedValue;

    private ErrorManager err;


    @Override
    @SuppressWarnings("unchecked")
    protected final void setUp () {
        WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel [2];
        panels[0] = new Panel("first panel");
        panels[1] = new Panel("second panel");
        wd = new WizardDescriptor(panels);
        wd.addPropertyChangeListener(new Listener());
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog (wd);
        //d.show();
        err = ErrorManager.getDefault ().getInstance ("test-" + getName ());
    }

    @RandomlyFails
    public void testAsynchronousLazyValidation () throws Exception {
        Panel panels[] = new Panel[3];
        
        class MyPanel extends Panel implements WizardDescriptor.ExtendedAsynchronousValidatingPanel<WizardDescriptor> {
            public String validateMsg;
            public String failedMsg;
            public boolean running;
            public boolean wasEnabled;
            public boolean validationFinished;
            
            public MyPanel () {
                super ("enhanced panel");
            }
            
            public void prepareValidation () {
                running = true;
                validationFinished = false;
            }
            
            public synchronized void validate () throws WizardValidationException {
                err.log ("validate() entry.");
                wasEnabled = wd.isNextEnabled () || wd.isFinishEnabled ();
                running = false;
                if (validateMsg != null) {
                    failedMsg = validateMsg;
                    err.log ("Throw WizardValidationException.");
                    throw new WizardValidationException (null, "MyPanel.validate() failed.", validateMsg);
                }
                err.log ("validate() exit.");
                return;
            }

            public synchronized void finishValidation() {
                assert SwingUtilities.isEventDispatchThread();
                validationFinished = true;
                notifyAll ();
        }
        }
        
        class MyFinishPanel extends MyPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
            @Override
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
        
        assertNull ("Component has not been yet initialized", panels[1].component);
        mp.failedMsg = null;
        mp.validateMsg = "xtest-fail-without-msg";
        assertTrue ("Next button must be enabled.", wd.isNextEnabled ());
        err.log ("Do Next. Validation will run with: validateMsg=" + mp.validateMsg + ", failedMsg=" + mp.failedMsg);
        synchronized (mp) {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                    wd.doNextClick ();
                }
            });
            assertTrue ("Validation runs.", mp.running);
            assertFalse ("Wizard is not valid when validation runs.",  wd.isForwardEnabled ());
            // let's wait till wizard is valid
            while (mp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mp.wait ();
            }
        }
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is ready to next validation however previous failed.",  wd.isForwardEnabled ());
        assertTrue ("finishValidation() was called even when validation failed.",  mp.validationFinished);
        assertEquals ("The lazy validation failed on Next.", mp.validateMsg, mp.failedMsg);
        assertNull ("The lazy validation failed, still no initialiaation", panels[1].component);
        assertNull ("The lazy validation failed, still no initialiaation", panels[2].component);
        mp.failedMsg = null;
        mp.validateMsg = null;
        synchronized (mp) {
            err.log ("Do Next. Validation will run with: validateMsg=" + mp.validateMsg + ", failedMsg=" + mp.failedMsg);
            assertTrue ("Wizard is valid before validation.",  wd.isForwardEnabled ());
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                    wd.doNextClick ();
                }
            });
            assertTrue ("Validation runs.", mp.running);
            while (mp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mp.wait ();
            }
        }
        Thread.sleep (1000);
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is valid when validation passes.",  wd.isForwardEnabled ());
        assertNull ("Validation on Next passes", mp.failedMsg);
        assertNotNull ("Now we switched to another panel", panels[1].component);
        assertNull ("The lazy validation failed, still no initialiaation", panels[2].component);
        assertTrue ("finishValidation() was called when validation finished.",  mp.validationFinished);

        // remember previous state
        Object state = wd.getValue();
        mfp.validateMsg = "xtest-fail-without-msg";
        mfp.failedMsg = null;
        synchronized (mfp) {
            err.log ("Do Finish. Validation will run with: validateMsg=" + mfp.validateMsg + ", failedMsg=" + mfp.failedMsg);
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                    wd.doFinishClick ();
                }
            });
            while (mfp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mfp.wait ();
            }
        }
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is ready to next validation.",  wd.isForwardEnabled ());
        assertEquals ("The lazy validation failed on Finish.", mfp.validateMsg, mfp.failedMsg);
        assertNull ("The validation failed, still no initialiaation", panels[2].component);
        assertEquals ("State has not changed", state, wd.getValue ());

        mfp.validateMsg = null;
        mfp.failedMsg = null;
        synchronized (mfp) {
            err.log ("Do Finish. Validation will run with: validateMsg=" + mfp.validateMsg + ", failedMsg=" + mfp.failedMsg);
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                    wd.doFinishClick ();
                }
            });
            while (mfp.running) {
                assertFalse ("Wizard is not valid during validation.",  wd.isForwardEnabled ());
                mfp.wait ();
            }
        }
        Thread.sleep (1000);
        assertFalse ("Finish is disabled during validation.", mp.wasEnabled);
        assertTrue ("Wizard is valid when validation passes.",  wd.isForwardEnabled ());
        assertNull ("Validation on Finish passes", mfp.failedMsg);
        assertNull ("Finish was clicked, no initialization either", panels[2].component);
        assertEquals ("The state is finish", WizardDescriptor.FINISH_OPTION, wd.getValue ());
    }

    public void testCancelDuringValidation() throws InterruptedException {
        final Panel panels[] = new Panel[2];

        class ValidatingPanel extends Panel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor> {
            public String validateMsg;
            public String failedMsg;
            public boolean running;
            public boolean wasEnabled;

            public ValidatingPanel () {
                super ("enhanced panel");
            }

            public void prepareValidation () {
                running = true;
            }

            public synchronized void validate () throws WizardValidationException {
                err.log ("validate() entry.");
                try {
                    Thread.sleep( 5000 );
                } catch( InterruptedException ex ) {
//                    Exceptions.printStackTrace( ex );
                }
                running = false;
                notifyAll();
                return;
            }
        }

        panels[0] = new ValidatingPanel();
        panels[1] = new Panel( "finish");

        final boolean[] currentInvokedOnUninitializedIterator = new boolean[1];
        currentInvokedOnUninitializedIterator[0] = false;

        WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> iterator = new WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor>() {

            private int index;
            private boolean uninitialized = false;

            @Override
            public Set instantiate() throws IOException {
                return null;
            }

            @Override
            public void initialize( WizardDescriptor wizard ) {
                index = 0;
            }

            @Override
            public void uninitialize( WizardDescriptor wizard ) {
                index = -1;
                uninitialized = true;
            }

            @Override
            public WizardDescriptor.Panel<WizardDescriptor> current() {
                if( uninitialized ) {
                    currentInvokedOnUninitializedIterator[0] = uninitialized;
                }
                return panels[index];
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public boolean hasNext() {
                return index < panels.length;
            }

            @Override
            public boolean hasPrevious() {
                return index > 0;
            }

            @Override
            public void nextPanel() {
                index++;
            }

            @Override
            public void previousPanel() {
                index--;
            }

            @Override
            public void addChangeListener( ChangeListener l ) {
            }

            @Override
            public void removeChangeListener( ChangeListener l ) {
            }
        };

        final WizardDescriptor descriptor = new WizardDescriptor( iterator );
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                descriptor.doNextClick();
            }
        });
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                descriptor.doCancelClick();
            }
        });

        Thread.sleep (3000);
        assertFalse( "Do not updateState() if the wizard has been canceled during validation.", currentInvokedOnUninitializedIterator[0] );
    }

    public class Panel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {
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
            return null;
        }
        
        public boolean isValid() {
            return true;
        }
        
        public boolean isFinishPanel () {
            return true;
        }
        
        public void readSettings(WizardDescriptor settings) {
            log ("readSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public void storeSettings(WizardDescriptor settings) {
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
