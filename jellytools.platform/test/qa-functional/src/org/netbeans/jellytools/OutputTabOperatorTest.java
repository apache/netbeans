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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OutputWindowViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTest;

/** Test of OutputTabOperator.
 *
 * @author Jiri Skrivanek
 */
public class OutputTabOperatorTest extends JellyTestCase {

    private static final String targetName = "compile-single";
    private static OutputTabOperator outputTabOperator;
    static final String[] tests = new String[]{
        "testMakeComponentVisible",
        "testToolbarButtons",
        "testFindLine",
        "testGetText",
        "testWaitText",
        "testGetLineCount",
        "testGetLine",
        "testGetLength",
        "testVerify",
        "testSelectAll",
        "testCopy",
        "testFind",
        "testFindNext",
        "testSaveAs",
        "testWrapText",
        "testClear",
        "testClose"
    };

    public OutputTabOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static NbTest suite() {
        return (NbTest) createModuleTest(OutputTabOperatorTest.class, tests);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Compiles a source which opens output tab to test. */
    private static void initTab() {
        Node sample1 = new Node(Utils.getSourcePackagesNode(), "sample1");  // NOI18N
        Node sampleClass1 = new Node(sample1, "SampleClass1.java");  // NOI18N
        new Action(null, "Compile").perform(sampleClass1);
        new OutputWindowViewAction().performMenu();
        outputTabOperator = new OutputTabOperator(targetName);
        // increase time to wait
        outputTabOperator.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        // wait build is finished
        MainWindowOperator.getDefault().waitStatusText("Finished building SampleProject (compile-single).");
        outputTabOperator.waitText("BUILD SUCCESSFUL");
    }

    /**
     * Test of makeComponentVisible method.
     */
    public void testMakeComponentVisible() {
        initTab();
        // should be improved to use 2 terms and activate the hidden one
        outputTabOperator.makeComponentVisible();
        assertTrue(targetName + " output tab should be visible.", outputTabOperator.isShowing());
    }

    /**
     * Tests presence and, where possible, functionality of buttons on the left
     * side (tool bar) of the output tab.
     */
    public void testToolbarButtons() {
        outputTabOperator.clear();
        outputTabOperator.btnReRun().push();
        outputTabOperator.waitText("BUILD SUCCESSFUL");
        outputTabOperator.btnReRunWithDifferentParameters().pushNoBlock();
        new NbDialogOperator("Run Ant Target").close();
        assertFalse("When there's no task running, the Stop button should be disabled!", outputTabOperator.btnStop().isEnabled());
        outputTabOperator.btnAntSettings().push();
        new OptionsOperator().close();
    }

    /**
     * Test of findLine method.
     */
    public void testFindLine() {
        assertEquals("Wrong row found.", 0, outputTabOperator.findLine(targetName)); // NOI18N
    }

    /**
     * Test of getText method.
     */
    public void testGetText() {
        String text = outputTabOperator.getText();
        assertTrue("Text is not from " + targetName + " output tab.", text.indexOf(targetName) > -1);
        String twoLines = outputTabOperator.getText(0, 2);
        assertTrue("Text from first three lines should contain at least two ':'", twoLines.indexOf(':') != twoLines.lastIndexOf(':'));
    }

    /**
     * Test of waitText method.
     */
    public void testWaitText() {
        outputTabOperator.waitText(targetName);
    }

    /**
     * Test of getLineCount method.
     */
    public void testGetLineCount() {
        assertTrue("Wrong line count.", outputTabOperator.getLineCount() > 0);
    }

    /**
     * Test of getLine() method.
     */
    public void testGetLine() {
        assertTrue("Wrong text found.", outputTabOperator.getLine(outputTabOperator.findLine(targetName)).indexOf(targetName) > -1);
    }

    /**
     * Test of getLength() method.
     */
    public void testGetLength() {
        assertEquals("Wrong length returned.", outputTabOperator.getLength(), outputTabOperator.getText().length());
    }

    /**
     * Test of verify method.
     */
    public void testVerify() {
        outputTabOperator.verify();
    }

    /**
     * Test of selectAll method.
     */
    public void testSelectAll() {
        clearTestStatus();
        startTest();
        outputTabOperator.selectAll();
        endTest();
    }

    /**
     * Test of copy method.
     */
    public void testCopy() throws Exception {
        startTest();
        clearTestStatus();
        outputTabOperator.copy();
        assertTrue("Copy doesn't work.", getClipboardText().indexOf(targetName) > -1);
    }

    /**
     * Test of find method.
     */
    public void testFind() {
        outputTabOperator.find();
        // "Find"
        String findTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "LBL_Find_Title");
        NbDialogOperator findDialog = new NbDialogOperator(findTitle);
        // assuming somthing with 'a' is printed in output
        JTextFieldOperator jtfo = new JTextFieldOperator(findDialog);
        jtfo.enterText("a");   // NOI18N
        try {
            // need to wait find action is finished
            findDialog.waitClosed();
        } catch (JemmyException e) {
            // sometimes it fails on Solaris => try it once more in Robot mode
            log("Dialog not closed first time. Trying once more.");
            // "Find"
            String findButtonLabel = Bundle.getStringTrimmed("org.netbeans.core.output2.Bundle", "BTN_Find");
            JButtonOperator findButtonOper = new JButtonOperator(findDialog, findButtonLabel);
            findButtonOper.getProperties().setDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            findButtonOper.push();
            findDialog.waitClosed();
        }
        new EventTool().waitNoEvent(1000);
    }

    /**
     * Test of findNext method.
     */
    public void testFindNext() {
        outputTabOperator.findNext();
    }

    /**
     * Test of saveAs method.
     */
    public void testSaveAs() {
        outputTabOperator.saveAs();
        // "Save As"
        String saveAsTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "TITLE_SAVE_DLG");
        new NbDialogOperator(saveAsTitle).close();
    }

    /** Test of nextError method. */
    public void testNextError() {
        // TODO add test some day
        //outputTabOperator.nextError();
    }

    /** Test of previousError method. */
    public void testPreviousError() {
        // TODO add test some day
        //outputTabOperator.previousError();
    }

    /** Test of wrapText method. */
    public void testWrapText() {
        // set
        outputTabOperator.wrapText();
        // unset
        outputTabOperator.wrapText();
    }

    /** Test of clear method. */
    public void testClear() {
        outputTabOperator.clear();
        assertTrue("Text was not cleared.", outputTabOperator.getText().length() == 0);
    }

    /**f
     * Test of close method.
     */
    public void testClose() {
        outputTabOperator.close();
        assertFalse("Output tab should be closed.", outputTabOperator.isShowing());
    }

    /** Wait until clipboard contains string data and returns the text. */
    private String getClipboardText() throws Exception {
        Waiter waiter = new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                if (contents == null) {
                    return null;
                } else {
                    return contents.isDataFlavorSupported(DataFlavor.stringFlavor) ? Boolean.TRUE : null;
                }
            }

            @Override
            public String getDescription() {
                return ("Wait clipboard contains string data");
            }
        });
        waiter.waitAction(null);
        return Toolkit.getDefaultToolkit().getSystemClipboard().
                getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
    }
}
