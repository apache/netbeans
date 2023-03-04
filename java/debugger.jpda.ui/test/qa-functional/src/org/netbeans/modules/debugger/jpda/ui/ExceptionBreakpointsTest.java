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
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import org.openide.util.Exceptions;



/**
 *
 * @author ehucka, Petr Cyhelsky, Jiri Kovalsky
 */
public class ExceptionBreakpointsTest extends DebuggerTestCase {

    private static String[] tests = new String[] {
        "testExceptionBreakpointCreation",
        "testExceptionBreakpointFunctionality",
        "testExceptionBreakpointMatchClasses",
        "testExceptionBreakpointExcludeClasses",
        "testExceptionBreakpointHitCount",
        "testConditionalExceptionBreakpoint"
    };

    /**
     *
     * @param name
     */
    public ExceptionBreakpointsTest(String name) {
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
        return createModuleTest(ExceptionBreakpointsTest.class, tests);
    }

    /**
     *
     */
    @Override
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  ####### ");
        
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode);
    }

    /**
     *
     */
    public void testExceptionBreakpointCreation() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Exception");

        new JEditorPaneOperator(dialog, 0).setText("java.lang.NullPointerException");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
        dialog.ok();
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Thread breakpoint was not created.", "Exception NullPointerException caught", jTableOperator.getValueAt(0, 0).toString());
    }

    /**
     *
     */
    public void testExceptionBreakpointFunctionality() throws Throwable {
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Exception");
        new JEditorPaneOperator(dialog, 0).setText("java.lang.ClassNotFoundException");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
        dialog.ok();
        new EventTool().waitNoEvent(1500);
        Utilities.startDebugger();
        try {
            Utilities.waitStatusText(Utilities.runningStatusBarText);
        } catch (Throwable e) {
            if (!Utilities.checkConsoleForText(Utilities.runningStatusBarText, 0)) {
                System.err.println(e.getMessage());
                throw e;
            }
        }
        new ContinueAction().perform();
        try {
            Utilities.waitStatusText("Thread main stopped at URLClassLoader.java");
        } catch (Throwable e) {
            if (!Utilities.checkConsoleForText("Thread main stopped at URLClassLoader.java", 0)) {
                System.err.println(e.getMessage());
                throw e;
            }
        }
    }

    /**
     *
     */
    public void testExceptionBreakpointMatchClasses() throws Throwable {
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Exception");
        new JEditorPaneOperator(dialog, 0).setText("java.lang.ClassCastException");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Uncatched"));
        new JCheckBoxOperator(dialog, 0).changeSelection(true);
        new JTextFieldOperator(dialog, 0).setText("tests.ThrowException");
        dialog.ok();
        new EventTool().waitNoEvent(1500);

        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "tests|ThrowException.java"); //NOI18N
        new DebugJavaFileAction().perform(beanNode);
        
        try {
            Utilities.waitStatusOrConsoleText("Thread main stopped at ThrowException.java:21.");
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.out.println("Problem: console last line - " + Utilities.getConsoleLastLineText());
            throw e;
        }
        assertFalse("The debugger hit not matching breakpoint", Utilities.checkConsoleForText("Thread main stopped at Vector.java:694.", 3));
    }

    /**
     *
     */
    public void testExceptionBreakpointExcludeClasses() throws Throwable{
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Exception");
        new JEditorPaneOperator(dialog, 0).setText("java.lang.ArrayIndexOutOfBoundsException");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
        new JCheckBoxOperator(dialog, 0).changeSelection(true);
        new JTextFieldOperator(dialog, 1).setText("java.util.Vector");
        dialog.ok();
        new EventTool().waitNoEvent(1500);
        
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "tests|ThrowException.java"); //NOI18N
        new DebugJavaFileAction().perform(beanNode);
        
        try {
            Utilities.waitStatusOrConsoleText("User program finished");
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.out.println("Problem: console last line - " + Utilities.getConsoleLastLineText());
            throw e;
        }
        assertTrue("The debugger incorrectly hit breakpoint", Utilities.checkOuputForText("debugTestProject (debug-single)", "Exception in thread \"main\" java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String", 24));
    }

    /**
     *
     */
    public void testExceptionBreakpointHitCount() {        
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Exception");
        new JEditorPaneOperator(dialog, 0).setText("java.lang.ClassCastException");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
        new JCheckBoxOperator(dialog, 2).changeSelection(true);
        new JComboBoxOperator(dialog, 3).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.equals"));
        new JTextFieldOperator(dialog, 2).setText("3");
        dialog.ok();
        new EventTool().waitNoEvent(1500);

        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "tests|ThrowMultipleExceptions.java"); //NOI18N
        new DebugJavaFileAction().perform(beanNode);
        
        try {
            Utilities.waitStatusOrConsoleText("Thread main stopped at ThrowMultipleExceptions.java:17.");
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
        assertEquals("The debugger didn't hit breakpoint 2 times,", 2, Utilities.checkOutputForNumberOfOccurrences("debugTestProject (debug-single)", "Error: Item is not a string.", 0));
    }

    /**
     *
     */
    public void testConditionalExceptionBreakpoint() {        
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Exception");
        new JEditorPaneOperator(dialog, 0).setText("java.lang.ClassNotFoundException");
        new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
        new JCheckBoxOperator(dialog, 1).changeSelection(true);
        new JEditorPaneOperator(dialog, 0).setText("false");
        dialog.ok();
        Utilities.startDebugger();
        Utilities.waitStatusText(Utilities.runningStatusBarText);
        assertFalse("The debugger hit disabled breakpoint", Utilities.checkConsoleForText("Thread main stopped", 0));
    }

    protected void setBreakpointType(NbDialogOperator dialog, String type) {
        new JComboBoxOperator(dialog, 0).selectItem("Java");
        new JComboBoxOperator(dialog, 1).selectItem(type);
    }
}
