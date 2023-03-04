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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;

/**
 *
 * @author Vojtech Sigler, Jiri Kovalsky
 */
public class ApplyCodeChangesTest  extends DebuggerTestCase {

    public static final String[] tests = new String[] {
        "testModifyLine",
        "testAddLine"

    };

    private static String outputConsoleTitle = "debugTestProject (debug-single)";

    public ApplyCodeChangesTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return createModuleTest(ApplyCodeChangesTest.class, tests);
    }

    /** setUp method  */
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    public void testModifyLine() {

        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);

        Node testFile = new Node(new SourcePackagesNode(projectNode), "tests|FixAndContinue.java");
        new OpenAction().perform(testFile);
        EditorOperator eo = new EditorOperator("FixAndContinue.java");
        Utilities.toggleBreakpoint(eo, 25);
        Utilities.toggleBreakpoint(eo, 26);
        new DebugJavaFileAction().perform(testFile);
        Utilities.waitStatusText("Thread main stopped at FixAndContinue.java:25");

        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);
        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);

        assertTrue("Current PC annotation is not on line 34", Utilities.checkAnnotation(eo, 34, "CurrentPC"));
        
        eo.replace("beforeFix()", "afterFix()");

        new SaveAction().perform();

        new Action(Utilities.runMenu + "|" + Utilities.applyCodeChangesItem, null).perform();
        Utilities.waitStatusText("Finished building debugTestProject (debug-fix)");
        new ContinueAction().perform();
        Utilities.waitStatusText("Thread main stopped at FixAndContinue.java:26");

        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);
        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);

        assertTrue("Current PC annotation is not on line 38", Utilities.checkAnnotation(eo, 38, "CurrentPC"));
        new StepOverAction().perform();
        new EventTool().waitNoEvent(1000);

        assertTrue("No 'Before code changes' text in output", checkOutputForText("Before code changes"));
        assertTrue("No 'After code changes' text in output", checkOutputForText("After code changes"));

        //cleanup for next test
        eo.replace("afterFix()", "beforeFix()");
    }

    public void testAddLine() {

        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);

        Node testFile = new Node(new SourcePackagesNode(projectNode), "tests|FixAndContinue.java");
        new OpenAction().perform(testFile);
        EditorOperator eo = new EditorOperator("FixAndContinue.java");
        Utilities.toggleBreakpoint(eo, 25);
        Utilities.toggleBreakpoint(eo, 26);
        new DebugJavaFileAction().perform(testFile);
        Utilities.waitStatusText("Thread main stopped at FixAndContinue.java:25");

        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);
        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);

        assertTrue("Current PC annotation is not on line 34", Utilities.checkAnnotation(eo, 34, "CurrentPC"));

        eo.setCaretPositionToEndOfLine(29);
        eo.insert("\n");
        eo.insert("System.out.println(\"Added line\");");
        eo.replace("beforeFix()", "afterFix()");

        new SaveAction().perform();

        new Action(Utilities.runMenu + "|" + Utilities.applyCodeChangesItem, null).perform();
        Utilities.waitStatusText("Finished building debugTestProject (debug-fix)");
        new ContinueAction().perform();
        Utilities.waitStatusText("Thread main stopped at FixAndContinue.java:26");

        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);
        new StepOverAction().perform();
        new EventTool().waitNoEvent(1000);
        new StepIntoAction().perform();
        new EventTool().waitNoEvent(1000);

        assertTrue("Current PC annotation is not on line 39", Utilities.checkAnnotation(eo, 39, "CurrentPC"));
        new StepOverAction().perform();
        new EventTool().waitNoEvent(1000);

        assertTrue("No 'Before code changes' text in output", checkOutputForText("Before code changes"));
        assertTrue("No 'Added line' text in output", checkOutputForText("Added line"));
        assertTrue("No 'After code changes' text in output", checkOutputForText("After code changes"));

    }

    private static boolean checkOutputForText(String text) {
        OutputTabOperator op = new OutputTabOperator(outputConsoleTitle);
        for (int i = op.getLineCount() - 5; i < op.getLineCount(); i++) {
            if (op.getLine(i).startsWith(text)) {
                return true;
            }
        }
        return false;
    }
}
