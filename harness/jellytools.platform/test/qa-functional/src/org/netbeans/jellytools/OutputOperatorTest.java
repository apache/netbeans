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
package org.netbeans.jellytools;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTest;

/**
 *  Test of OutputOperator.
 *
 * @author Jiri Skrivanek
 */
public class OutputOperatorTest extends JellyTestCase {

    // OutputOperator instance used in tests
    private static OutputOperator outputOperator;
    private static final String OUTPUT_TITLE = "SampleProject (debug)";
    static final String[] tests = new String[]{
        "testInvoke",
        "testGetOutputTab",
        "testGetText",
        "testSelectAll",
        "testCopy",
        "testFind",
        "testFindNext",
        "testWrapText",
        "testSaveAs",
        "testClear",
        "testVerify"};

    public OutputOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static NbTest suite() {
        return (NbTest) createModuleTest(OutputOperatorTest.class, tests);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /**
     * Test of invoke method
     */
    public void testInvoke() {
        OutputOperator.invoke().close();
        // be sure it is opened
        outputOperator = OutputOperator.invoke();
    }

    /**
     * Test of getOutputTab method
     */
    public void testGetOutputTab() {
        // setup - open output tab
        new Action(null, "Debug").perform(Utils.getProjectRootNode("SampleProject"));
        // increase time to wait
        outputOperator.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        // wait for finish of debugging
        outputOperator.getOutputTab(OUTPUT_TITLE).waitText("total time");

        OutputTabOperator oto = outputOperator.getOutputTab(OUTPUT_TITLE);
        assertTrue("Wrong OutputTabOperator found.", oto.getName().indexOf(OUTPUT_TITLE) > -1);
    }

    /**
     * Test of getText method
     */
    public void testGetText() {
        String text = outputOperator.getText();
        assertTrue("Text is not from debugger term.", text.indexOf("debug") > -1); //NOI18N
    }

    /**
     * Test of selectAll method
     */
    public void testSelectAll() {
        startTest();
        outputOperator.getOutputTab(OUTPUT_TITLE);
        outputOperator.selectAll();
        endTest();
    }

    /**
     * Test of copy method
     */
    public void testCopy() throws Exception {
        startTest();
        clearTestStatus();
        outputOperator.copy();
        assertTrue("Copy doesn't work.", getClipboardText().indexOf("debug") > -1);   // NOI18N
    }

    /**
     * Test of find method
     */
    public void testFind() throws Exception {
        outputOperator.find();
        // "Find"
        String findTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "LBL_Find_Title");
        NbDialogOperator findDialog = new NbDialogOperator(findTitle);
        // assuming debug string is printed in output at least twice
        new JTextFieldOperator(findDialog).setText("b");   // NOI18N
        // "Find"
        String findButtonLabel = Bundle.getStringTrimmed("org.netbeans.core.output2.Bundle", "BTN_Find");
        new JButtonOperator(findDialog, findButtonLabel).push();
        // wait a little until "b" is selected
        new EventTool().waitNoEvent(500);
        // verify "b" is selected
        outputOperator.copy();
        if (!getClipboardText().equals("b")) {
            // repeat because find action was not executed
            outputOperator.find();
            findDialog = new NbDialogOperator(findTitle);
            new JTextFieldOperator(findDialog).setText("b");   // NOI18N
            new JButtonOperator(findDialog, findButtonLabel).push();
        }
    }

    /**
     * Test of findNext method
     */
    public void testFindNext() {
        outputOperator.findNext();
    }

    /** Test of nextError method. */
    public void testNextError() {
        // TODO add test some day
        //outputOperator.nextError();
    }

    /** Test of previousError method. */
    public void testPreviousError() {
        // TODO add test some day
        //outputOperator.previousError();
    }

    /** Test of wrapText method. */
    public void testWrapText() {
        // set
        outputOperator.wrapText();
        // unset
        outputOperator.wrapText();
    }

    /**
     * Test of saveAs method.
     */
    public void testSaveAs() {
        outputOperator.saveAs();
        // "Save As"
        String saveAsTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "TITLE_SAVE_DLG");
        new NbDialogOperator(saveAsTitle).close();
    }

    /** Test of clear method. */
    public void testClear() {
        outputOperator.clear();
        assertTrue("Text was not cleared.", outputOperator.getText().length() == 0);
    }

    /**
     * Test of verify method
     */
    public void testVerify() {
        // currently does nothing
        outputOperator.verify();
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
