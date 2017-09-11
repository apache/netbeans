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
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2005
 * All Rights Reserved.
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
