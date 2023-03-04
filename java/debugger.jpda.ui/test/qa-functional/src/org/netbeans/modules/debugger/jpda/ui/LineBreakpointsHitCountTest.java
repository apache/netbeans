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

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JTable;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.openide.nodes.Node.Property;
/**
 *
 * @author felipee, Jiri Kovalsky
 */
public class LineBreakpointsHitCountTest extends DebuggerTestCase{

       //MainWindowOperator.StatusTextTracer stt = null;
    /**
     *
     * @param name
     */
    public LineBreakpointsHitCountTest(String name) {
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
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(LineBreakpointsHitCountTest.class).addTest(           
                    "testLineBreakpointsHitCount" 
                )
            .enableModules(".*").clusters(".*"));
    }

    /**
     *
     */
    @Override
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  ####### ");
    }

     public void testLineBreakpointsHitCount() throws IllegalAccessException, InvocationTargetException {
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        //toggle breakpoints
        Utilities.toggleBreakpoint(eo, 64);
        Utilities.toggleBreakpoint(eo, 65);
        Utilities.toggleBreakpoint(eo, 66);
        //set hit conditions
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenuNoBlock("Properties");
        NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
        new JCheckBoxOperator(dialog, 1).changeSelection(true);
        new JComboBoxOperator(dialog, 0).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.equals"));
        new JTextFieldOperator(dialog, 2).setText("45");
        dialog.ok();

        new JPopupMenuOperator(jTableOperator.callPopupOnCell(1, 0)).pushMenuNoBlock("Properties");
        dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
        new JCheckBoxOperator(dialog, 1).changeSelection(true);
        new JComboBoxOperator(dialog, 0).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.greater"));
        new JTextFieldOperator(dialog, 2).setText("48");
        dialog.ok();

        new JPopupMenuOperator(jTableOperator.callPopupOnCell(2, 0)).pushMenuNoBlock("Properties");
        dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
        new JCheckBoxOperator(dialog, 1).changeSelection(true);
        new JComboBoxOperator(dialog, 0).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.multiple"));
        new JTextFieldOperator(dialog, 2).setText("47");
        dialog.ok();

        //start debugging
        Utilities.startDebugger();
        //check values
        StringComparator comp = new StringComparator() {

            public boolean equals(String arg0, String arg1) {
                return arg0.equals(arg1);
            }
        };
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:64");
        Utilities.showDebuggerView(Utilities.variablesViewTitle);
        jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        final int row = treeTableOperator.findCellRow("i", comp);

        // wait for the values to evaluate
        treeTableOperator.waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {

                Property prop = (Property)((JTable)comp).getValueAt(row, 2);
                boolean result = false;

                try
                {
                    result = ! (prop.getValue().toString().toLowerCase().contains("evaluating"));
                }
                catch (Exception e)
                {
                    result = false;
                }

                return result;
            }
            public String getDescription() {
                return "TreeTable contains any rows.";
            }
        });

        org.openide.nodes.Node.Property property = (org.openide.nodes.Node.Property) treeTableOperator.getValueAt(row, 2);
        assertEquals("44", property.getValue());
/* THIS PART IS ABOUT TO BE REVIEWED */
//TODO: Review this
        /*
        new ContinueAction().perform();




        try {
               System.out.println("Debugger Console Status: " + Utilities.getDebuggerConsoleStatus());
                System.out.println("Last Line is: " + Utilities.getConsoleLastLineText());

            Utilities.waitStatusText("Thread main stopped at MemoryView.java:66");
        } catch (Throwable e) {
            if (!Utilities.checkConsoleLastLineForText(Utilities.runningStatusBarText)) {
                System.err.println(e.getMessage());
                //System.out.println("Debugger Console Status: " + Utilities.getDebuggerConsoleStatus());
                //System.out.println("Last Line is: " + Utilities.getConsoleLastLineText());
                throw e;
            }
        }

        //Utilities.waitStatusText("Thread main stopped at MemoryView.java:66", 10000);
        Utilities.showDebuggerView(Utilities.localVarsViewTitle);
        jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        row = treeTableOperator.findCellRow("i", comp);
        property = (org.openide.nodes.Node.Property) treeTableOperator.getValueAt(row, 2);
        assertEquals("46", property.getValue());
        new ContinueAction().perform();

        Utilities.waitStatusText("Thread main stopped at MemoryView.java:65");
        Utilities.showDebuggerView(Utilities.localVarsViewTitle);
        jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        row = treeTableOperator.findCellRow("i", comp);
        property = (org.openide.nodes.Node.Property) treeTableOperator.getValueAt(row, 2);
        assertEquals("47", property.getValue());
        */

    }
      protected void setBreakpointType(NbDialogOperator dialog, String type) {
        new JComboBoxOperator(dialog, 0).selectItem("Java");
        new JComboBoxOperator(dialog, 1).selectItem(type);
    }

}
