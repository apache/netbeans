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
 * @author ehucka, Revision Petr Cyhelsky, Jiri Kovalsky
 */
public class FieldBreakpointsTest extends DebuggerTestCase {

    private static String[] tests = new String[] {
        "testFieldBreakpointCreation",
        "testFieldBreakpointPrefilledValues",
        "testFieldBreakpointFunctionalityAccess",
        "testFieldBreakpointFunctionalityModification",
        "testConditionalFieldBreakpointFunctionality",
        "testFieldBreakpointsValidation"
    };

    private static boolean initialized = false;

    /**
     *
     * @param name
     */
    public FieldBreakpointsTest(String name) {
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
        return createModuleTest(FieldBreakpointsTest.class, tests);
    }

    /**
     *
     */
    @Override
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  ####### ");        
    }

    /**
     *
     */
    public void testFieldBreakpointCreation() {        
        //open source
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
        setBreakpointType(dialog, "Field");
        new JEditorPaneOperator(dialog, 0).setText("examples.advanced.MemoryView");
        new JEditorPaneOperator(dialog, 1).setText("msgMemory");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Access"));
        dialog.ok();
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Field breakpoint was not created.", "Field MemoryView.msgMemory access", jTableOperator.getValueAt(0, 0).toString());
    }

    /**
     *
     */
    public void testFieldBreakpointPrefilledValues() {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            new EventTool().waitNoEvent(500);
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JEditorPaneOperator(dialog, 0).getText());
            assertEquals("Field Name was not set to correct value.", "msgMemory", new JEditorPaneOperator(dialog, 1).getText());
            dialog.cancel();        
    }

    /**
     *
     */
    public void testFieldBreakpointFunctionalityAccess() {        
        NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
        setBreakpointType(dialog, "Field");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Access"));
        new EventTool().waitNoEvent(500);
        dialog.ok();
        new EventTool().waitNoEvent(1500);
        Utilities.startDebugger();
        assertTrue("Thread didn't stop at field breakpoint.", Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104."));
    }

    /**
     *
     */
    public void testFieldBreakpointFunctionalityModification() {        
        NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
        setBreakpointType(dialog, "Field");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Modification"));
        dialog.ok();
        Utilities.startDebugger();
        assertTrue("Thread didn't stop at field breakpoint.", Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:45."));
    }

    public void testConditionalFieldBreakpointFunctionality() throws Throwable {        
        NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
        setBreakpointType(dialog, "Field");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Access"));
        new JCheckBoxOperator(dialog, 0).changeSelection(true);
        new JEditorPaneOperator(dialog, 2).setText("UPDATE_TIME >= 1001");
        dialog.ok();

        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 109);

        Utilities.startDebugger();
        assertTrue("Thread didn't stop at field breakpoint.", Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:109"));
        new ContinueAction().perform();
        Thread.sleep(1000);
        assertTrue("Thread didn't stop at field breakpoint.", Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104"));
    }

    public void testFieldBreakpointsValidation() {        
        NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
        setBreakpointType(dialog, "Field");
        String wrongname = "wrongname";
        new JEditorPaneOperator(dialog, 1).setText(wrongname);
        dialog.ok();

        Utilities.startDebugger();
        assertTrue("Warning about wrong field breakpoint missing.", Utilities.checkConsoleLastLineForText("Not able to submit breakpoint FieldBreakpoint examples.advanced.MemoryView.wrongname"));
        dialog = Utilities.newBreakpoint(36, 36);
        setBreakpointType(dialog, "Field");
        wrongname = "wrongname2";
        new JEditorPaneOperator(dialog, 1).setText(wrongname);
        dialog.ok();
        assertTrue("Warning about wrong field breakpoint missing.", Utilities.checkConsoleLastLineForText("Not able to submit breakpoint FieldBreakpoint examples.advanced.MemoryView.wrongname2"));
    }

    protected void setBreakpointType(NbDialogOperator dialog, String type) {
        new JComboBoxOperator(dialog, 0).selectItem("Java");
        new JComboBoxOperator(dialog, 1).selectItem(type);
    }
}
