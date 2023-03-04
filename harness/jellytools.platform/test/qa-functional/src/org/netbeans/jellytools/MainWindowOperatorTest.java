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

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.Toolbar;

/**
 * Test of org.netbeans.jellytools.MainWindowOperator.
 */
public class MainWindowOperatorTest extends JellyTestCase {

    public static final String[] tests = {
        "testGetDefault",
        "testMenuBar",
        "testGetSetStatusText",
        "testWaitStatusText",
        "testGetToolbarInt",
        "testGetToolbarString",
        "testGetToolbarCount",
        "testGetToolbarName",
        "testGetToolbarButtonInt",
        "testGetToolbarButtonString",
        "testPushToolbarPopupMenu",
        "testPushToolbarPopupMenuNoBlock",
        // not stable enough - "testDragNDropToolbar",
        "testStatusTextTracer"
    };
    /** Instance of MainWindowOperator (singleton) to test. */
    private MainWindowOperator mainWindowOper;

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public MainWindowOperatorTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     * @return Test suite.
     */
    public static Test suite() {
        return createModuleTest(MainWindowOperatorTest.class, tests);
    }

    /** Redirect output to log files, wait before each test case and
     * show dialog to test. */
    @Override
    protected void setUp() {
        System.out.println("### " + getName() + " ###");
        mainWindowOper = MainWindowOperator.getDefault();
    }

    /** Tear down after test case. */
    @Override
    protected void tearDown() {
    }

    /** Test of getDefault() method. */
    public void testGetDefault() {
        MainWindowOperator.getDefault();
    }

    /** Test of testMenuBar method. */
    public void testMenuBar() {
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.menuBar();
    }

    /** Test of getStatusText method. */
    public void testGetSetStatusText() {
        String expectedText = "Hello World!!!";
        mainWindowOper.setStatusText(expectedText);
        String text = mainWindowOper.getStatusText();
        assertEquals("Wrong status text.", expectedText, text);
    }

    /** Test of waitStatusText method. */
    public void testWaitStatusText() {
        String expectedText = "Hello World!!!";
        StatusDisplayer.getDefault().setStatusText(expectedText);
        mainWindowOper.waitStatusText(expectedText);
    }

    /***************** methods for toolbars manipulation *******************/
    /** Test of getToolbar(int) method. */
    public void testGetToolbarInt() {
        mainWindowOper.getToolbar(0);
    }

    /** Test of getToolbar(String) method. */
    public void testGetToolbarString() {
        mainWindowOper.getToolbar("File"); // NOI18N
    }

    /** Test of getToolbarCount method. */
    public void testGetToolbarCount() {
        //assertEquals("Wrong toolbar count.", 3, mainWindowOper.getToolbarCount());
        assertTrue("Wrong toolbar count.", mainWindowOper.getToolbarCount() >= 3);
    }

    /** Test of getToolbarName method. */
    public void testGetToolbarName() {
        String toolbarName = mainWindowOper.getToolbarName(0);
        String expected = ((Toolbar) mainWindowOper.getToolbar(0).getSource()).getDisplayName();
        assertEquals("Wrong toolbar name", expected, toolbarName);
    }

    /** Test of getToolbarButton method. Finds Build toolbar and checks if
     * getToolbarButton(1) returns Build Main Project button. */
    public void testGetToolbarButtonInt() {
        ContainerOperator toolbarOper = mainWindowOper.getToolbar("File"); // NOI18N        

        String tooltip = mainWindowOper.getToolbarButton(toolbarOper, 0).getToolTipText();
        String expected = Bundle.getString("org.netbeans.modules.project.ui.actions.Bundle",
                "LBL_NewFileAction_Tooltip");
        assertTrue("Wrong toolbar button.", tooltip.indexOf(expected) != -1);
    }

    /** Test of getToolbarButton method. Finds Build toolbar and checks if
     * getToolbarButton() finds Build All button. */
    public void testGetToolbarButtonString() {
        ContainerOperator toolbarOper = mainWindowOper.getToolbar("File"); // NOI18N
        String buildMainProject = Bundle.getStringTrimmed("org.openide.loaders.Bundle", "SaveAll");
        mainWindowOper.getToolbarButton(toolbarOper, buildMainProject);
    }

    /** Test of pushToolbarPopupMenu method. Pushes popup menu Edit
     * checks whether toolbar Edit dismissed and push again to enable it. */
    public void testPushToolbarPopupMenu() {
        int expectedToolbarsCount = mainWindowOper.getToolbarCount();
        // "File"
        String popupPath = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Toolbars/File");
        mainWindowOper.pushToolbarPopupMenu(popupPath);
        int actualToolbarCount = mainWindowOper.getToolbarCount();
        mainWindowOper.pushToolbarPopupMenu(popupPath);
        assertEquals("Toolbar popup menu not pushed. Toolbars count should differ:", expectedToolbarsCount, actualToolbarCount + 1);
    }

    /** Test of pushToolbarPopupMenuNoBlock method.  */
    public void testPushToolbarPopupMenuNoBlock() {
        // at the time no item in menu is blocking so we use testPushToolbarPopupMenu
        int expectedToolbarsCount = mainWindowOper.getToolbarCount();
        // "File"
        String popupPath = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Toolbars/File");
        mainWindowOper.pushToolbarPopupMenuNoBlock(popupPath);
        new EventTool().waitNoEvent(500);
        int actualToolbarCount = mainWindowOper.getToolbarCount();
        mainWindowOper.pushToolbarPopupMenu(popupPath);
        assertEquals("Toolbar popup menu not pushed. Toolbars count should differ:", expectedToolbarsCount, actualToolbarCount + 1);
    }

    /** Test of dragNDropToolbar method. Tries to move toolbar down and checks
     * whether main window is enlarged. */
    public void testDragNDropToolbar() throws InterruptedException {
        // need toolbar container to check drag and drop operation
        Component toolbarPool = mainWindowOper.findSubComponent(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().equals("org.openide.awt.ToolbarPool");
            }

            @Override
            public String getDescription() {
                return "org.openide.awt.ToolbarPool";
            }
        });
        ContainerOperator toolbarOper = mainWindowOper.getToolbar(0);
        int heightOrig = toolbarPool.getHeight();
        mainWindowOper.dragNDropToolbar(toolbarOper, 0, heightOrig);
        assertTrue("Toolbar not moved down - main window height the same.",
                heightOrig != toolbarPool.getHeight());
    }

    /** Test of MainWindowOperator.StatusTextTracer class. */
    public void testStatusTextTracer() {
        MainWindowOperator.StatusTextTracer stt = mainWindowOper.getStatusTextTracer();
        stt.start();
        // simulate compile action which produces at least two messages: "Compiling ..." and
        // "Finished ..."
        StatusDisplayer.getDefault().setStatusText("Compiling");
        StatusDisplayer.getDefault().setStatusText("Finished");
        //new CompileAction().performAPI();
        // waits for "Compiling" status text
        stt.waitText("Compiling");
        // waits for "Finished" status text
        stt.waitText("Finished");

        // order is not significant => following works as well
        stt.waitText("Finished");
        stt.waitText("Compiling");

        ArrayList list = stt.getStatusTextHistory();
        assertEquals("Method getStatusTextHistory returns wrong ArrayList.",
                "Compiling", list.get(0));
        assertEquals("Method getStatusTextHistory returns wrong ArrayList.",
                "Finished", list.get(1));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stt.printStatusTextHistory(new PrintStream(stream));
        assertTrue("Method printStatusTextHistory prints wrong values.",
                stream.toString().indexOf("Compiling") > -1);  // NOI18N
        assertTrue("Method printStatusTextHistory prints wrong values.",
                stream.toString().indexOf("Finished") > -1);  // NOI18N

        // to be order significant, set removedCompared parameter to true
        stt.waitText("Compiling", true);
        stt.waitText("Finished", true);

        // history was removed by above methods => need to produce a new messages
        StatusDisplayer.getDefault().setStatusText("Compiling");
        StatusDisplayer.getDefault().setStatusText("Finished");
        //new CompileAction().performAPI();

        // order is significant if removedCompared parameter is true =>
        // => following fails because Finished is shown as second
        stt.waitText("Finished", true);
        long oldTimeout = JemmyProperties.getCurrentTimeout("Waiter.WaitingTime");
        try {
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
            stt.waitText("Compiling", true);
            fail("waitText() should fail because of wrong order.");
        } catch (JemmyException e) {
            // OK. It fails.
        } finally {
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", oldTimeout);
        }

        String expectedText = "Should be traced.";
        StatusDisplayer.getDefault().setStatusText(expectedText);
        // stop tracing
        stt.stop();
        assertTrue("Text \"" + expectedText + "\" not traced.", stt.contains(expectedText, false));
        stt.clear();
        assertTrue("clear() doesn't work.", !stt.contains(expectedText, false));
        expectedText = "Should not be traced.";
        StatusDisplayer.getDefault().setStatusText(expectedText);
        assertTrue("stop() doesn't work. Text \"" + expectedText + "\" should not be traced.",
                !stt.contains(expectedText, false));
    }
}
