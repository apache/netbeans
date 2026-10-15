/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        "testBtOK",
        "testCancel",
        "testClose",
        "testHelp",
        "testOK",
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

    private TestLabel label;

    /** Opens modal dialog with OK, Cancel, Yes, No, Close and Help buttons. 
     * @param testDialogTitle title of test dialog
     */
    protected void showTestDialog(String testDialogTitle) {
        Object[] options = new Object[]{
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION,
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
