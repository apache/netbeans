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
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 *
 * @author ehucka, Revision Petr Cyhelsky
 */
public class MethodBreakpointsTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testMethodBreakpointCreation",
        "testMethodBreakpointPrefilledConstructor",
        "testMethodBreakpointPrefilledMethod",
        "testMethodBreakpointFunctionalityInPrimaryClass",
        "testMethodBreakpointFunctionalityInSecondClass",
        "testMethodBreakpointFunctionalityOnAllMethods",
        "testMethodBreakpointFunctionalityOnExit",
        "testConditionalMethodBreakpointFunctionality",
        "testMethodBreakpointsValidation"
    };
    
    //MainWindowOperator.StatusTextTracer stt = null;
    private Node beanNode;

    /**
     *
     * @param name
     */
    public MethodBreakpointsTest(String name) {
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
        return createModuleTest(MethodBreakpointsTest.class, tests);
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
    public void testMethodBreakpointCreation() {                
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        NbDialogOperator dialog = Utilities.newBreakpoint(92);
        setBreakpointType(dialog, "Method");
        new JEditorPaneOperator(dialog, 0).setText("examples.advanced.MemoryView");
        new JEditorPaneOperator(dialog, 1).setText("updateStatus()");
        dialog.ok();
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Method MemoryView.updateStatus", jTableOperator.getValueAt(0, 0).toString());
    }
    
    /**
     *
     */
    public void testMethodBreakpointPrefilledConstructor() {        
        //open source        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        NbDialogOperator dialog = Utilities.newBreakpoint(53);
        setBreakpointType(dialog, "Method");
        assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JEditorPaneOperator(dialog, 0).getText());
        assertEquals("Method Name was not set to correct value.", "MemoryView ()", new JEditorPaneOperator(dialog, 1).getText());
        dialog.cancel();
    }

    /**
     *
     */
    public void testMethodBreakpointPrefilledMethod() {        
        NbDialogOperator dialog = Utilities.newBreakpoint(92);
        setBreakpointType(dialog, "Method");
        assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JEditorPaneOperator(dialog, 0).getText());
        assertEquals("Method Name was not set to correct value.", "updateStatus ()", new JEditorPaneOperator(dialog, 1).getText());
        dialog.cancel();
    }

    /**
     *
     */
    public void testMethodBreakpointFunctionalityInPrimaryClass() throws Throwable {
        NbDialogOperator dialog = Utilities.newBreakpoint(92);
        setBreakpointType(dialog, "Method");
        dialog.ok();
        try {
            Utilities.startDebugger();
        } catch (Throwable th) {
            new EventTool().waitNoEvent(500);
            dialog.ok();
            new EventTool().waitNoEvent(1500);
            Utilities.startDebugger();
        }
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
    }

    /**
     *
     */
    public void testMethodBreakpointFunctionalityInSecondClass() throws Throwable{
        NbDialogOperator dialog = Utilities.newBreakpoint(154);
        setBreakpointType(dialog, "Method");
        dialog.ok();
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:154");
    }

    /**
     *
     */
    public void testMethodBreakpointFunctionalityOnAllMethods() throws Throwable {
        NbDialogOperator dialog = Utilities.newBreakpoint(37);
        setBreakpointType(dialog, "Method");
        dialog.ok();
        Utilities.startDebugger();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:39");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:114");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:50");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:121");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:79");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:92");
    }

    public void testMethodBreakpointFunctionalityOnExit() throws Throwable {        
        NbDialogOperator dialog = Utilities.newBreakpoint(54);
        setBreakpointType(dialog, "Method");
        new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Entry_or_Exit")); //method entry
        dialog.ok();

        dialog = Utilities.newBreakpoint(80);
        setBreakpointType(dialog, "Method");
        new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Entry")); //method entry
        dialog.ok();

        dialog = Utilities.newBreakpoint(102);
        setBreakpointType(dialog, "Method");
        new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Exit")); //method entry
        dialog.ok();

        Utilities.startDebugger();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:50");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:76");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:79");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:109");
    }

    public void testConditionalMethodBreakpointFunctionality() throws Throwable {        
        NbDialogOperator dialog = Utilities.newBreakpoint(104);
        setBreakpointType(dialog, "Method");
        new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Entry")); //method entry
        new JCheckBoxOperator(dialog, 1).changeSelection(true);
        new JEditorPaneOperator(dialog, 2).setText("UPDATE_TIME >= 1001");

        dialog.ok();
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //toggle control line breakpoint
        Utilities.toggleBreakpoint(eo, 104);

        Utilities.startDebugger();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:104");
        new ContinueAction().perform();
        Utilities.waitStatusOrConsoleText("Thread main stopped at MemoryView.java:92");
    }

    public void testMethodBreakpointsValidation() {

        NbDialogOperator dialog = Utilities.newBreakpoint(104);
        setBreakpointType(dialog, "Method");
        String wrongname = "wrong";
        new JEditorPaneOperator(dialog, 1).setText(wrongname);
        dialog.ok();

        Utilities.startDebugger();
        assertTrue("Warning about wrong method breakpoint missing.", Utilities.checkConsoleLastLineForText("Not able to submit breakpoint MethodBreakpoint [examples.advanced.MemoryView]." + wrongname));
        dialog = Utilities.newBreakpoint(104);
        setBreakpointType(dialog, "Method");
        wrongname = "wrong2";
        new JEditorPaneOperator(dialog, 1).setText(wrongname);
        dialog.ok();
        assertTrue("Warning about wrong method breakpoint missing.", Utilities.checkConsoleLastLineForText("Not able to submit breakpoint MethodBreakpoint [examples.advanced.MemoryView]." + wrongname));
    }

    protected void setBreakpointType(NbDialogOperator dialog, String type) {
        new JComboBoxOperator(dialog, 0).selectItem("Java");
        new JComboBoxOperator(dialog, 1).selectItem(type);
    }
}
