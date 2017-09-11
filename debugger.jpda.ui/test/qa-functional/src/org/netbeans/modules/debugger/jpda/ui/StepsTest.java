/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
