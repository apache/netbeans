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

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JViewport;
import junit.framework.Test;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.modules.debugger.actions.ApplyCodeChangesAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.PauseAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverExpressionAction;
import org.netbeans.jellytools.modules.debugger.actions.TakeGUISnapshotAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.openide.util.Exceptions;

/**
 * Set of basic tests to make sure that debugging of Ant based Java projects is
 * not seriously broken.
 *
 * @author Jiri Kovalsky
 */
public class AntSanityTest extends JellyTestCase {

    /**
     * Array of test names to be executed in this test suite.
     */
    public static String[] testNames = new String[]{
        "openDebugProject",
        "startDebuggingF7",
        "pause",
        "testLineBreakpoint",
        "runToCursor",
        "stepInto",
        "stepOut",
        "stepOver",
        "stepOverExpression",
        "finishDebugger",
        "applyCodeChanges",
        "takeGUISnapshot",
        "newWatch",
        "evaluateExpression"
    };

    /**
     * Name of tested project root node.
     */
    private static final String DEBUG_TEST_PROJECT_ANT = "debugTestProjectAnt";

    private static final String BREAKPOINTS_VIEW = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Breakpoints_view");
    private static final String VARIABLES_VIEW = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Variables_view");
    private static final String DEBUGGER_CONSOLE = "Debugger Console";

    /**
     * Constructor required by JUnit.
     *
     * @param testName Method name to be used as test case.
     */
    public AntSanityTest(String testName) {
        super(testName);
    }

    /**
     * Returns suite of tests to be executed.
     *
     * @return Suite with test cases to be executed by JUnit.
     */
    public static Test suite() {
        return createModuleTest(AntSanityTest.class, testNames);
    }

    /**
     * Sets up IDE before each test case.
     */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /**
     * Opens and builds Ant based test project for the next tests.
     *
     * @throws IOException Exception thrown if opening project failed.
     */
    public void openDebugProject() throws IOException {
        openDataProjects(DEBUG_TEST_PROJECT_ANT);
        new EventTool().waitNoEvent(1000);
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(DEBUG_TEST_PROJECT_ANT);
        new BuildJavaProjectAction().perform(projectNode);
        stt.waitText("Finished building " + DEBUG_TEST_PROJECT_ANT + " (clean,jar).");
        stt.stop();
    }

    /**
     * Starts debugging session by stepping into main method.
     */
    public void startDebuggingF7() {
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(DEBUG_TEST_PROJECT_ANT);
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new StepIntoAction().perform(projectNode);
        stt.waitText("Thread main stopped at MemoryView.java:276.");
        stt.stop();
        assertEquals(new EditorOperator("MemoryView.java").getLineNumber(), 276);
    }

    /**
     * Only pauses debugging session.
     */
    public void pause() {
        new PauseAction().perform();
        waitThreadsNode("'Finalizer' suspended at 'Object.wait'");
    }

    /**
     * Sets line 278 breakpoint and continues with debugging.
     */
    public void testLineBreakpoint() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.setCaretPositionToLine(278);
        new ToggleBreakpointAction().perform();
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new ContinueAction().perform();
        stt.waitText("Thread main stopped at MemoryView.java:278.");
        stt.stop();
        assertEquals(new EditorOperator("MemoryView.java").getLineNumber(), 278);
    }

    /**
     * Sets line 280 breakpoint, disables it and then continues to run to cursor
     * at line 283.
     */
    public void runToCursor() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.setCaretPositionToLine(280);
        new ToggleBreakpointAction().perform();
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(BREAKPOINTS_VIEW));
        jTableOperator.waitCell("Line MemoryView.java:280", 1, 0);
        new JPopupMenuOperator(jTableOperator.callPopupOnCell(1, 0)).pushMenu("Disable");
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        eo.makeComponentVisible();
        eo.setCaretPositionToLine(283);
        new RunToCursorAction().perform();
        stt.waitText("Thread main stopped at MemoryView.java:283.");
        stt.stop();
        assertEquals(new EditorOperator("MemoryView.java").getLineNumber(), 283);
    }

    /**
     * Steps into Window.setLocation method.
     */
    public void stepInto() {
        new StepIntoAction().perform();
        EditorOperator eo = new EditorOperator("Window.java");
        int lineNumber = eo.getLineNumber();
        waitThreadsNode("'main' suspended at 'Window.setLocation:" + lineNumber + "'");
        assertEquals(eo.getText(lineNumber).trim(), "super.setLocation(x, y);");
    }

    /**
     * Steps out back from Window.setLocation method to MemoryView.main.
     */
    public void stepOut() {
        new StepOutAction().perform();
        EditorOperator eo = new EditorOperator("MemoryView.java");
        int lineNumber = eo.getLineNumber();
        waitThreadsNode("'main' suspended at 'MemoryView.main:" + lineNumber + "'");
        assertEquals(eo.getText(lineNumber).trim(), "mv.setVisible(true);");
    }

    /**
     * Sets line 141 breakpoint and then steps over it.
     */
    public void stepOver() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.setCaretPositionToLine(141);
        new ToggleBreakpointAction().perform();
        new ContinueAction().perform();
        waitThreadsNode("'AWT-EventQueue-0' at line breakpoint MemoryView.java : 141");
        new StepOverAction().perform();
        waitThreadsNode("'AWT-EventQueue-0' suspended at 'MemoryView.updateStatus:143'");
        JTableOperator breakpoints = new JTableOperator(new TopComponentOperator(BREAKPOINTS_VIEW));
        new JPopupMenuOperator(breakpoints.callPopupOnCell(1, 0)).pushMenu("Delete All");
    }

    /**
     * Steps over expression at line 143.
     */
    public void stepOverExpression() throws InterruptedException {
        new StepOverExpressionAction().perform();
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(VARIABLES_VIEW));
        jTableOperator.waitCell("Before call to '<init>()'", 1, 0);
        jTableOperator.waitCell("Arguments", 2, 0);
        jTableOperator.selectCell(2, 0);
        pressKey(KeyEvent.VK_RIGHT);
        Thread.sleep(3000); // Wait 3 seconds. Sometimes expanding Arguments node is slow.
        jTableOperator.waitCell("total", 3, 0);
        new StepOverExpressionAction().perform();
        jTableOperator.waitCell("free", 3, 0);
        jTableOperator.waitCell("Return values history", 4, 0);
        jTableOperator.waitCell("return <init>()", 5, 0);
        new StepOverExpressionAction().perform();
        jTableOperator.waitCell("taken", 3, 0);
        jTableOperator.waitCell("Return values history", 4, 0);
        jTableOperator.waitCell("return <init>()", 5, 0);
        jTableOperator.waitCell("return <init>()", 6, 0);
        new StepOverExpressionAction().perform();
        jTableOperator.waitCell("new Object[]{new Long(total), new Long(free), new Integer(taken)}", 3, 0);
        jTableOperator.waitCell("Return values history", 4, 0);
        jTableOperator.waitCell("return <init>()", 5, 0);
        jTableOperator.waitCell("return <init>()", 6, 0);
        jTableOperator.waitCell("return <init>()", 7, 0);
        new StepOverExpressionAction().perform();
        jTableOperator.waitCell("msgMemory.format(new Object[]{new Long(total), new Long(free), new Integer(taken)})", 3, 0);
        jTableOperator.waitCell("Return values history", 4, 0);
        jTableOperator.waitCell("return <init>()", 5, 0);
        jTableOperator.waitCell("return <init>()", 6, 0);
        jTableOperator.waitCell("return <init>()", 7, 0);
        jTableOperator.waitCell("return format()", 8, 0);
    }
    
    /**
     * Finishes debugging session.
     */
    public void finishDebugger() {
        new FinishDebuggerAction().perform();
        OutputTabOperator op = new OutputTabOperator(DEBUGGER_CONSOLE);
        assertEquals("User program finished", op.getLine(op.getLineCount() - 2));
    }
    
    /**
     * Applies simple code change during debugging session.
     */
    public void applyCodeChanges() {
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(DEBUG_TEST_PROJECT_ANT);
        Node testFile = new Node(new SourcePackagesNode(projectNode), "advanced|ApplyCodeChangesTest.java");
        new OpenAction().perform(testFile);
        EditorOperator eo = new EditorOperator("ApplyCodeChangesTest.java");
        eo.setCaretPositionToLine(50);
        new ToggleBreakpointAction().perform();
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new DebugJavaFileAction().perform(testFile);
        stt.waitText("Thread main stopped at ApplyCodeChangesTest.java:50");
        stt.stop();
        eo.select(54);
        eo.replace("beforeFix()", "afterFix()");
        new SaveAction().perform();
        new ApplyCodeChangesAction().perform();
        new StepOverAction().perform();
        new FinishDebuggerAction().perform();
        OutputTabOperator op = new OutputTabOperator("debugTestProjectAnt (debug-single)");
        int beforeCodeChangesLine = op.findLine("Before code changes");
        int afterCodeChangesLine = op.findLine("After code changes");
        assertTrue(beforeCodeChangesLine == afterCodeChangesLine - 1);
    }
    
    /**
     * Takes GUI snapshot of debugged application.
     */
    public void takeGUISnapshot() throws InterruptedException {
        new Action("Debug|Debug Project (debugTestProjectAnt)", null).perform();
        Thread.sleep(3000); // Wait 3 seconds. Sometimes starting debugging session was slow.
        OutputTabOperator op = new OutputTabOperator(DEBUGGER_CONSOLE);
        assertEquals("User program running", op.getLine(op.getLineCount() - 2));
        new TakeGUISnapshotAction().perform();
        TopComponentOperator guiSnapshot = new TopComponentOperator("Snapshot of \"Memory View\"");
        assertEquals(guiSnapshot.getComponent(1).getName(), "Snapshot Zoom Toolbar");
        JViewport viewPort = (JViewport) guiSnapshot.getComponent(0).getComponentAt(10, 10);
        assertTrue(viewPort.getComponent(0).toString().startsWith("org.netbeans.modules.debugger.jpda.visual.ui.ScreenshotComponent$ScreenshotCanvas"));
        new FinishDebuggerAction().perform();
    }
    
    /**
     * Tests that it is possible to add watches.
     */
    public void newWatch() throws IllegalAccessException, InvocationTargetException, InvalidExpressionException {
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(DEBUG_TEST_PROJECT_ANT);
        Node testFile = new Node(new SourcePackagesNode(projectNode), "advanced|VariablesTest.java");
        new OpenAction().perform(testFile);
        EditorOperator eo = new EditorOperator("VariablesTest.java");
        eo.setCaretPositionToLine(49);
        new ToggleBreakpointAction().perform();
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new DebugJavaFileAction().perform(testFile);
        stt.waitText("Thread main stopped at VariablesTest.java:49");
        stt.stop();
        new ActionNoBlock("Debug|New Watch...", null).perform();
        NbDialogOperator newWatchDialog = new NbDialogOperator("New Watch");
        new JEditorPaneOperator(newWatchDialog, 0).setText("n");
        newWatchDialog.ok();
        TopComponentOperator variablesView = new TopComponentOperator(new ContainerOperator(MainWindowOperator.getDefault(), VIEW_CHOOSER), "Variables");
        JTableOperator variablesTable = new JTableOperator(variablesView);
        assertEquals("n", variablesTable.getValueAt(0, 0).toString());
        org.openide.nodes.Node.Property property = (org.openide.nodes.Node.Property) variablesTable.getValueAt(0, 2);
        assertEquals("50", property.getValue());
        JPopupMenuOperator menu = new JPopupMenuOperator(variablesTable.callPopupOnCell(0, 0));
        menu.pushMenu("Delete All");
    }
    
    /**
     * Evaluates simple expression during debugging session.
     */
    public void evaluateExpression() throws IllegalAccessException, InvocationTargetException, InterruptedException, InvalidExpressionException {
        TopComponentOperator variablesView = new TopComponentOperator(new ContainerOperator(MainWindowOperator.getDefault(), VIEW_CHOOSER), "Variables");
        JToggleButtonOperator showEvaluationResultButton = new JToggleButtonOperator(variablesView, 0);
        showEvaluationResultButton.clickMouse();
        TopComponentOperator evaluationResultView = new TopComponentOperator("Evaluation Result");
        new Action("Debug|Evaluate Expression...", null).perform();
        TopComponentOperator expressionEvaluator = new TopComponentOperator("Evaluate Expression");
        JEditorPaneOperator expressionEditor = new JEditorPaneOperator(expressionEvaluator);
        new EventTool().waitNoEvent(1000);
        expressionEditor.setText("\"If n is: \" + n + \", then n + 1 is: \" + (n + 1)");
        JPanel buttonsPanel = (JPanel) expressionEvaluator.getComponent(2);
        JButton expressionEvaluatorButton = (JButton) buttonsPanel.getComponent(1);
        assertEquals("Evaluate code fragment (Ctrl + Enter)", expressionEvaluatorButton.getToolTipText());
        expressionEvaluatorButton.doClick();
        JTableOperator variablesTable = new JTableOperator(evaluationResultView);
        assertValue(variablesTable, 0, 2, "\"If n is: 50, then n + 1 is: 51\"");
        assertEquals("\"If n is: \" + n + \", then n + 1 is: \" + (n + 1)", variablesTable.getValueAt(0, 0).toString().trim());
    }
    
    /**
     * Using AWT robot presses and immediately releases certain key.
     * @param code Code of the key to be pressed.
     */
    private void pressKey(int code) {
        try {
            Robot robot = new Robot();
            robot.keyPress(code);
            robot.keyRelease(code);
        } catch (AWTException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Waits until given threads node shows up in Debugging view.
     * @param name Full display name of the required thread node to be found.
     */
    private void waitThreadsNode(String name) {
        TopComponentOperator debuggingView = new TopComponentOperator("Debugging");
        JTreeOperator threads = new JTreeOperator(debuggingView);
        try {
            new Node(threads, name);
        } catch (TimeoutExpiredException tee) {
            // if fails try second time because sometimes it fails for unknown reason
            new Node(threads, name);
        }
    }

    /**
     * Variables view component chooser ignoring opened VariablesTest.java editor tab.
     */
    private static final ComponentChooser VIEW_CHOOSER = new ComponentChooser() {

        @Override
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("org.netbeans.modules.debugger.ui.views.View");
        }

        @Override
        public String getDescription() {
            return "debugger view";// NOI18N
        }
    };

    /**
     * Tests that table contains required value in cell with given coordinates.
     * 
     * @param table Table to be used for the search.
     * @param row Row of the cell.
     * @param column Column of the cell.
     * @param value Value to be searched for.
     */
    private void assertValue(JTableOperator table, int row, int column, String value) throws IllegalAccessException, InvocationTargetException, InvalidExpressionException {
        org.openide.nodes.Node.Property property = null;
        for (int i = 0; i < 10; i++) {
            property = (org.openide.nodes.Node.Property) table.getValueAt(row, column);
            if (property != null)
                if (!"Evaluating...".equals(property.getValue())) break;
            new EventTool().waitNoEvent(1000);
        }
        assertNotNull(property);
        if (property.getValue() instanceof ObjectVariable) {
            assertEquals(value, ((ObjectVariable) property.getValue()).getToStringValue());
        } else {
            assertEquals(value, property.getValue().toString());
        }
    }
}