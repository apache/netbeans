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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
/**
 *
 * @author felipee, Jiri Kovalsky
 */
public class StepsTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testStepInto",
        "testStepOver",
        "testRunToCursor",
        "testStepOut",
        "testStepOverExpression"
    };

    private Node projectNode;
    private Node beanNode;

    public StepsTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return createModuleTest(StepsTest.class, tests);
    }

    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");

        if (projectNode == null) {
            projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        }
        new OpenAction().performAPI(beanNode);
    }

public void testStepInto() throws Throwable {
    new EventTool().waitNoEvent(1000);
    EditorOperator eo = new EditorOperator("MemoryView.java");
    Utilities.toggleBreakpoint(eo, 80);
    new DebugProjectAction().perform(projectNode);
    //wait for breakpoint
    Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:80");
    new StepIntoAction().perform();
    Thread.sleep(2000);
    assertTrue("CurrentPC annotation is not on line 92", Utilities.checkAnnotation(eo, 92, "CurrentPC"));
    assertTrue("Call Site annotation is not on line 80", Utilities.checkAnnotation(eo, 80, "CallSite"));
}

public void testStepOver() throws Throwable {
    new EventTool().waitNoEvent(1000);
    EditorOperator eo = new EditorOperator("MemoryView.java");
    Utilities.toggleBreakpoint(eo, 80);
    new DebugProjectAction().perform(projectNode);
    //wait for breakpoint
    Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:80");
    new StepOverAction().performMenu();
    new EventTool().waitNoEvent(1000);
    assertFalse("CurrentPC annotation remains on line 80", Utilities.checkAnnotation(eo, 80, "CurrentPC"));
    assertTrue("CurrentPC annotation is not on line 82", Utilities.checkAnnotation(eo, 82, "CurrentPC"));
}

 public void testRunToCursor() throws Throwable {

    EditorOperator eo = new EditorOperator("MemoryView.java");
    Utilities.toggleBreakpoint(eo, 80);
    new DebugProjectAction().perform(projectNode);
    //wait for breakpoint
    Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:80");
    Utilities.deleteAllBreakpoints(); //removes the breakpoint in the way of run to cursor
    Utilities.setCaret(eo, 109);
    //run to cursor
    new RunToCursorAction().performMenu();
    new EventTool().waitNoEvent(1000);
    assertFalse("Current PC annotation remains on line 80", Utilities.checkAnnotation(eo, 80, "CurrentPC"));
    assertTrue("Current PC annotation is not on line 109", Utilities.checkAnnotation(eo, 109, "CurrentPC"));
}


  public void testStepOut() throws Throwable {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 94);
        new DebugProjectAction().perform(projectNode);
        //wait for breakpoint
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:94");
        new StepOutAction().performMenu();
        new EventTool().waitNoEvent(1000);
        assertFalse("Current PC annotation remains on line 94", Utilities.checkAnnotation(eo, 94, "CurrentPC"));
        assertTrue("Current PC annotation is not on line 80", Utilities.checkAnnotation(eo, 80, "CurrentExpressionLine"));
    }

   public void testStepOverExpression() throws Throwable {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 104);
        new DebugProjectAction().perform(projectNode);
        //wait for breakpoint
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104");
        Utilities.toggleBreakpoint(eo, 104, false);
        new EventTool().waitNoEvent(1000);

        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(2000);

        assertTrue("CurrentExpressionLine annotation is not on line 105", Utilities.checkAnnotation(eo, 105, "CurrentExpressionLine"));
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        assertTrue("CurrentExpressionLine annotation is not on line 106", Utilities.checkAnnotation(eo, 106, "CurrentExpressionLine"));
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        assertTrue("CurrentExpressionLine annotation is not on line 107", Utilities.checkAnnotation(eo, 107, "CurrentExpressionLine"));
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        assertTrue("CurrentExpressionLine annotation is not on line 104", Utilities.checkAnnotation(eo, 104, "CurrentExpressionLine"));
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        assertTrue("CurrentExpressionLine annotation is not on line 104", Utilities.checkAnnotation(eo, 104, "CurrentExpressionLine"));
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        assertTrue("Current PC annotation is not on line 109", Utilities.checkAnnotation(eo, 109, "CurrentPC"));
    }


}
