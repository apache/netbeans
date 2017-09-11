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
package org.netbeans.jellytools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import junit.framework.Test;
import org.netbeans.jemmy.JemmyProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 * Test of org.netbeans.jellytools.NbDialogOperator.
 */
public class NbDialogOperatorTest extends JellyTestCase {

    private static final String TEST_DIALOG_TITLE = "Test Dialog";
    protected static final String TEST_DIALOG_LABEL = "  This is a test dialog.";
    public static String[] tests = new String[]{
        "testBtCancel",
        "testBtClose",
        "testBtHelp",
        "testBtNo",
        "testBtOK",
        "testBtYes",
        "testCancel",
        "testClose",
        "testHelp",
        "testNo",
        "testOK",
        "testYes"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public NbDialogOperatorTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(NbDialogOperatorTest.class, tests);
    }

    /** Shows dialog to test. */
    @Override
    protected void setUp() {
        System.out.println("### " + getName() + " ###");
        showTestDialog(TEST_DIALOG_TITLE);
    }

    /** Test Close button getter. */
    public void testBtClose() {
        new NbDialogOperator(TEST_DIALOG_TITLE).btClose().push();
        assertTrue("Close not detected correctly.", getResult().equals(DialogDescriptor.CLOSED_OPTION));
    }

    /** Test close button pushing. */
    public void testClose() {
        new NbDialogOperator(TEST_DIALOG_TITLE).closeByButton();
        assertTrue("Close not pushed.", getResult().equals(DialogDescriptor.CLOSED_OPTION));
    }

    /** Test Help button getter. */
    public void testBtHelp() {
        NbDialogOperator dialog = new NbDialogOperator(TEST_DIALOG_TITLE);
        dialog.btHelp().push();
        JemmyProperties.setCurrentTimeout("WindowWaiter.WaitWindowTimeout", 60000);
        new HelpOperator().close();
        dialog.close();
    }

    /** Test Help button. */
    public void testHelp() {
        NbDialogOperator dialog = new NbDialogOperator(TEST_DIALOG_TITLE);
        dialog.help();
        JemmyProperties.setCurrentTimeout("WindowWaiter.WaitWindowTimeout", 60000);
        new HelpOperator().close();
        dialog.close();
    }

    /** Test OK button getter. */
    public void testBtOK() {
        new NbDialogOperator(TEST_DIALOG_TITLE).btOK().push();
        assertTrue("OK not detected correctly.", getResult().equals(DialogDescriptor.OK_OPTION));
    }

    /** Test OK button pushing. */
    public void testOK() {
        new NbDialogOperator(TEST_DIALOG_TITLE).ok();
        assertTrue("OK not pushed.", getResult().equals(DialogDescriptor.OK_OPTION));
    }

    /** Test Cancel button getter. */
    public void testBtCancel() {
        new NbDialogOperator(TEST_DIALOG_TITLE).btCancel().push();
        assertTrue("Cancel not detected correctly.", getResult().equals(DialogDescriptor.CANCEL_OPTION));
    }

    /** Test Cancel button pushing. */
    public void testCancel() {
        new NbDialogOperator(TEST_DIALOG_TITLE).cancel();
        assertTrue("Cancel not pushed.", getResult().equals(DialogDescriptor.CANCEL_OPTION));
    }

    /** Test Yes button getter. */
    public void testBtYes() {
        new NbDialogOperator(TEST_DIALOG_TITLE).btYes().push();
        assertTrue("Yes not detected correctly.", getResult().equals(DialogDescriptor.YES_OPTION));
    }

    /** Test Yes button pushing. */
    public void testYes() {
        new NbDialogOperator(TEST_DIALOG_TITLE).yes();
        assertTrue("Yes not pushed.", getResult().equals(DialogDescriptor.YES_OPTION));
    }

    /** Test No button getter. */
    public void testBtNo() {
        new NbDialogOperator(TEST_DIALOG_TITLE).btNo().push();
        assertTrue("No not detected correctly.", getResult().equals(DialogDescriptor.NO_OPTION));
    }

    /** Test No button pushing. */
    public void testNo() {
        new NbDialogOperator(TEST_DIALOG_TITLE).no();
        assertTrue("No not pushed.", getResult().equals(DialogDescriptor.NO_OPTION));
    }
    private TestLabel label;

    /** Opens modal dialog with OK, Cancel, Yes, No, Close and Help buttons. 
     * @param testDialogTitle title of test dialog
     */
    protected void showTestDialog(String testDialogTitle) {
        Object[] options = new Object[]{
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION,
            DialogDescriptor.YES_OPTION,
            DialogDescriptor.NO_OPTION,
            DialogDescriptor.CLOSED_OPTION
        };
        label = new TestLabel(TEST_DIALOG_LABEL);
        DialogDescriptor dd = new DialogDescriptor(label, testDialogTitle, false,
                options, null, DialogDescriptor.BOTTOM_ALIGN, null, label);
        dd.setHelpCtx(new HelpCtx("org.netbeans.api.javahelp.MASTER_ID"));
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }

    /** Gets pushed button from Test Dialog. */
    private Object getResult() {
        return label.getEvent().getSource();
    }

    /** Label intended to use in tested dialog. It enables to find out which
     * button was pushed by getEvent() method.
     */
    private class TestLabel extends JLabel implements ActionListener {

        /** Create a new label. */
        public TestLabel(String text) {
            super(text);
        }
        private ActionEvent lastEvent = null;

        /** Called when a button is pushed. Stores event to be able to get it
         * later. */
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            lastEvent = actionEvent;
        }

        /** Gets last performed event. Need to detect what button was pushed. */
        public ActionEvent getEvent() {
            return lastEvent;
        }
    }
}
