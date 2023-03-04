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

package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;



/**
 *
 * @author ehucka, Revision Petr Cyhelsky, Jiri Kovalsky
 */
public class LineBreakpointsTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testLineBreakpointCreation",
        "testLineBreakpointFunctionality",
        "testLineBreakpointFunctionalityAfterContinue",
        "testLineBreakpointFunctionalityInStaticMethod",
        "testLineBreakpointFunctionalityInInitializer",
        "testLineBreakpointFunctionalityInConstructor",
        "testLineBreakpointFunctionalityInInnerClass",
        "testLineBreakpointFunctionalityInSecondaryClass",
        "testConditionalLineBreakpointFunctionality",
        "testLineBreakpointActions",
        "testLineBreakpointsValidation",
        "testLineBreakpointsAdjustment"
    };

    private Node beanNode;

    //MainWindowOperator.StatusTextTracer stt = null;
    /**
     *
     * @param name
     */
    public LineBreakpointsTest(String name) {
        super(name);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     *
     * @return
     */
    public static Test suite() {
        return createModuleTest(LineBreakpointsTest.class, tests);
    }

    /**
     *
     */
    @Override
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  ####### ");
        if (beanNode == null)
        {
            beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
        }
    }

    /**
     *
     */
    public void testLineBreakpointCreation() throws Throwable {        
        //open source
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 73);
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Line MemoryView.java:73", jTableOperator.getValueAt(0, 0).toString());
        eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 73, false);
        new EventTool().waitNoEvent(1000);
        jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals(0, jTableOperator.getRowCount());
    }

    /**
     *
     */
    public void testLineBreakpointFunctionality() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 73);
        Utilities.startDebugger();
        try {
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:73");
        } catch (Throwable e) {
            if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:73")) {
                System.err.println(e.getMessage());
                throw e;
            }
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityAfterContinue() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 52);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52");
        eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 74);
        new ContinueAction().perform();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:74");
        
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInStaticMethod() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 114);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:114");
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInInitializer() throws Throwable {
        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 45);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:45");
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInConstructor() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 54);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:54");
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInInnerClass() throws Throwable {
        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 123);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread Thread-0 stopped at MemoryView.java:123");
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInSecondaryClass() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 154);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:154");
    }

    /**
     *
     */
    public void testConditionalLineBreakpointFunctionality() throws Throwable {
        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 63);
        Utilities.toggleBreakpoint(eo, 64);

        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Line MemoryView.java:64", jTableOperator.getValueAt(1, 0).toString());
        new JPopupMenuOperator(jTableOperator.callPopupOnCell(1, 0)).pushMenuNoBlock("Properties");
        NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
        new JCheckBoxOperator(dialog, 0).changeSelection(true);
        new JEditorPaneOperator(dialog, 0).setText("i > 0");
        dialog.ok();
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:63");
        new ContinueAction().perform();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:63");
        new ContinueAction().perform();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:64");
    }

    /**
     *
     */
    public void testLineBreakpointActions() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 102);

        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Line MemoryView.java:102", jTableOperator.getValueAt(0, 0).toString());
        new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenuNoBlock("Properties");
        NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);

        String nothread = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_CB_Actions_Panel_Suspend_None");
        new JComboBoxOperator(dialog, 2).selectItem(nothread);
        String breakpointHitText = "{lineNumber} line breakpoint hit in class {className}"; //noi18n
        new JTextFieldOperator(dialog, 4).setText(breakpointHitText);
        dialog.ok();
        Utilities.toggleBreakpoint(eo, 104);
        Utilities.startDebugger();
        Utilities.checkConsoleForText("102 line breakpoint hit in class examples.advanced.MemoryView", 4);
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104");
    }

    public void testLineBreakpointsValidation() throws Throwable {        
        int[] invalidBreakpoints = new int[]{33, 34, 37, 43, 49, 72, 83, 95, 96, 125, 143};
        EditorOperator eo = new EditorOperator("MemoryView.java");

        //try to toggle invalid line breakpoints
        for (int i = 0; i < invalidBreakpoints.length; i++) {
            Thread.sleep(5000);
            Utilities.toggleInvalidBreakpoint(eo, invalidBreakpoints[i]);
            Utilities.waitStatusText("A breakpoint cannot be set at this location.");
        }
    }
    
    public void testLineBreakpointsAdjustment() throws Throwable {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle non-breakable line breakpoint
        Utilities.toggleInvalidBreakpoint(eo, 108);
        Utilities.waitStatusText("Non-breakable location selected, breakpoint position adjusted.");
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Line MemoryView.java:104", jTableOperator.getValueAt(0, 0).toString());
        Utilities.deleteAllBreakpoints();
        
        //toggle line breakpoints to be adjusted
        Utilities.toggleInvalidBreakpoint(eo, 81);
        Utilities.toggleInvalidBreakpoint(eo, 105);
        assertTrue(jTableOperator.getValueAt(0, 0).toString().endsWith("81"));
        assertTrue(jTableOperator.getValueAt(1, 0).toString().endsWith("105"));
        
        Utilities.startDebugger();
        //check that both breakpoints were moved to nearest breakable locations
        Utilities.checkConsoleForText("LineBreakpoint MemoryView.java : 104 successfully submitted.", 2);
        Utilities.checkConsoleForText("LineBreakpoint MemoryView.java : 80 successfully submitted.", 3);
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:80");
    }

    protected void setBreakpointType(NbDialogOperator dialog, String type) {
        new JComboBoxOperator(dialog, 0).selectItem("Java");
        new JComboBoxOperator(dialog, 1).selectItem(type);
    }
}
