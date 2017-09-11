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
