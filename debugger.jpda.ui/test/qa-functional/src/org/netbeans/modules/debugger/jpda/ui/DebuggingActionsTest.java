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
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
/**
 *
 * @author Filip Zamboj, Jiri Kovalsky
 */
public class DebuggingActionsTest extends DebuggerTestCase{

    private static Node beanNode;

    private static String[] tests = new String[]{
            "testStartDebugging",
            "testContinue",
            //TODO: Where is this test???
           // "testStepOverExpression",
            "testPause"
    };

    public DebuggingActionsTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return createModuleTest(DebuggingActionsTest.class, tests);
    }

    /** setUp method  */
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");

        if (beanNode == null)
        {
            beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N          
        }
    }

    static int lastLineNumber = 0;

    /**
     * Tests start debugging and line breakpoint functionality.
     *
     * Testspec:
     * 1) Open MemoryView.java
     * 2) Toggle breakpoint on line 80
     * 3) Debug file
     *
     * @throws Throwable
     */
    public void testStartDebugging() throws Throwable {                    
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 80);
            new DebugJavaFileAction().perform(beanNode);
            //wait for breakpoint
            Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:80");
            assertTrue("Breakpoint annotation is not on line 80", Utilities.checkAnnotation(eo, 80, "Breakpoint"));
            assertTrue("Current PC annotation is not on line 80", Utilities.checkAnnotation(eo, 80, "CurrentPC"));
    }

    
    public void testContinue() throws Throwable {        
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 80);
            new DebugJavaFileAction().perform(beanNode);
            //wait for breakpoint
            Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:80");
            Utilities.deleteAllBreakpoints();
            Utilities.toggleBreakpoint(eo, 104);
            new ContinueAction().performMenu();
            new EventTool().waitNoEvent(1000);
            assertFalse("Current PC annotation remains on line 80", Utilities.checkAnnotation(eo, 80, "CurrentPC"));
            assertTrue("Current PC annotation is not on line 104", Utilities.checkAnnotation(eo, 104, "CurrentPC"));        
    }



    public void testPause() throws Throwable {        
            
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.toggleBreakpoint(eo, 80);
        new DebugJavaFileAction().perform(beanNode);
        //wait for breakpoint
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:80");
        new ContinueAction().performMenu();
        //continue
        new ContinueAction().performMenu();
        //remove breakpoint
        Utilities.toggleBreakpoint(eo, 80, false);
       //continue
       //new EventTool().waitNoEvent(1000);
       new ContinueAction().performMenu();
        String pausePath = Utilities.runMenu + "|" + Utilities.pauseItem;
        for (int i = 0; i < 10; i++) {
            if (MainWindowOperator.getDefault().menuBar().showMenuItem(pausePath).isEnabled()) {
                new Action(pausePath, null).performMenu();
            }
            MainWindowOperator.getDefault().menuBar().closeSubmenus();
            new EventTool().waitNoEvent(500);
        }

        eo = new EditorOperator("MemoryView.java");

        boolean found = false;
        for (int i = 79; i < 87; i++) {
            if (Utilities.checkAnnotation(eo, i, "CallSite")) {
                found = true;
                break;
            }
        }
        assertTrue("Call Site annotation is not in for cycle", found);
        //there should not be any other opened classes - issue 83704
        eo.closeAllDocuments();
    }
}
