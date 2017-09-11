/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
