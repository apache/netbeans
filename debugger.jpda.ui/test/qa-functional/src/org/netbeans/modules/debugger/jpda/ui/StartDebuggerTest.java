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
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;

/**
 *
 * @author cincura, ehucka, Jiri Vagner, ppis, cyhelsky, vsigler, Jiri Kovalsky
 */
public class StartDebuggerTest extends DebuggerTestCase {

    public static String[] tests = new String[]{
        "testDebugProject",
        "testDebugFile",
        "testRunDebuggerStepInto",
        "testRunDebuggerRunToCursor",
        "testDebugMainProject"
    };

    private Node projectNode;
    private Node beanNode;

    public StartDebuggerTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        return createModuleTest(StartDebuggerTest.class, tests);
    }
    
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
        if (projectNode == null) {
            projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            new EventTool().waitNoEvent(500);
            new Action(null, "Unset as Main Project");
            new EventTool().waitNoEvent(500);
        }
    }
        
    public void testDebugProject() {        
        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        new DebugProjectAction().perform(projectNode);
        new EventTool().waitNoEvent(500);
        Utilities.getDebugToolbar().waitComponentVisible(true);
        assertTrue("The debugger toolbar did not show after start of debugging", Utilities.getDebugToolbar().isVisible());
        Utilities.checkConsoleLastLineForText(Utilities.runningStatusBarText);
    }

    public void testDebugFile() {                
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new Action(null, null, Utilities.debugFileShortcut).performShortcut();
        Utilities.getDebugToolbar().waitComponentVisible(true);
        Utilities.checkConsoleLastLineForText(Utilities.runningStatusBarText);
    }
 
    public void testRunDebuggerStepInto() throws InterruptedException {                
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.setCaret(eo, 75);
        new EventTool().waitNoEvent(1000);
        new StepIntoAction().perform();
        Thread.sleep(1000);
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:39");
        assertTrue("Current PC annotation is not on line 39", Utilities.checkAnnotation(eo, 39, "CurrentPC"));
    }

    /**
     * Tests start of debugger by run to cursor, run to cursor during an active session
     *
     * Testspec:
     * 1) Open MemoryView.java
     * 2) Place caret on line 75
     * 3) Invoke Run To Cursor
     * 4) Place caret on line 104
     * 5) Invoke Run To Cursor
     *
     * @throws Throwable
     */
    public void testRunDebuggerRunToCursor() throws Throwable
    {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.setCaret(eo, 75);
        new RunToCursorAction().perform();
        Thread.sleep(1000);
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:75");
        assertTrue("Current PC annotation is not on line 75", Utilities.checkAnnotation(eo, 75, "CurrentPC"));

        Utilities.setCaret(eo, 104);
        new RunToCursorAction().perform();
        Thread.sleep(1000);
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104");
        assertTrue("Current PC annotation is not on line 104", Utilities.checkAnnotation(eo, 104, "CurrentPC"));
    }

    public void testDebugMainProject() {                    
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new Action(Utilities.runMenu+"|"+Utilities.debugMainProjectItem, null).perform();
        Utilities.getDebugToolbar().waitComponentVisible(true);
        assertTrue("The debugger toolbar did not show after start of debugging", Utilities.getDebugToolbar().isVisible());
        Utilities.checkConsoleLastLineForText(Utilities.runningStatusBarText);
    }
}
