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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;



/**
 *
 * @author ehucka, Revision Petr Cyhelsky, Jiri Kovalsky
 */
public class ThreadBreakpointsTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testThreadBreakpointCreation",
        "testThreadBreakpointFunctionality",
        "testThreadBreakpointFunctionalityHitCount"
    };

    //MainWindowOperator.StatusTextTracer stt = null;
    /**
     *
     * @param name
     */
    public ThreadBreakpointsTest(String name) {
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
        return createModuleTest(ThreadBreakpointsTest.class, tests);
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
    public void testThreadBreakpointCreation() {        
        //open source
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Thread");
        dialog.ok();
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        assertEquals("Thread breakpoint was not created.", "Thread started", jTableOperator.getValueAt(0, 0).toString());
    }

    /**
     *
     */
    public void testThreadBreakpointFunctionality() throws Throwable {

        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Thread");
        dialog.ok();

        Utilities.startDebugger();
        try {
            Utilities.waitStatusText("Thread main stopped.");
        } catch (Throwable e) {
            if (!Utilities.checkConsoleForText("Thread breakpoint hit by thread main (started).", 2)) {
                System.err.println(e.getMessage());
                throw e;
            }
        }
        new ContinueAction().perform();
        try {
            Utilities.waitStatusText("Thread Thread-0 stopped ");
        } catch (Throwable e) {
            if (!Utilities.checkConsoleForText("Thread breakpoint hit by thread Thread-0 (started).", 5)) {
                System.err.println(e.getMessage());
                throw e;
            }
        }
        new ContinueAction().perform();
        try {
            Utilities.waitStatusText(Utilities.runningStatusBarText);
        } catch (Throwable e) {
            if (!Utilities.checkConsoleLastLineForText(Utilities.runningStatusBarText)) {
                System.err.println(e.getMessage());
                throw e;
            }
        }
    }

    /**
     *
     */
    public void testThreadBreakpointFunctionalityHitCount() throws Throwable {        
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        setBreakpointType(dialog, "Thread");
        new JCheckBoxOperator(dialog, 0).changeSelection(true);
        new JTextFieldOperator(dialog, 0).setText("1");
        dialog.ok();

        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped.");
        assertTrue("Thread breakpoint does not work.", Utilities.checkConsoleForText("Thread breakpoint hit by thread", 2));

        new ContinueAction().perform();
        Utilities.checkConsoleLastLineForText(Utilities.runningStatusBarText);
        assertEquals("There were more than one hit of the breakpoint", Utilities.checkConsoleForNumberOfOccurrences(Utilities.runningStatusBarText, 0), 2);
    }

    protected void setBreakpointType(NbDialogOperator dialog, String type) {
        new JComboBoxOperator(dialog, 0).selectItem("Java");
        new JComboBoxOperator(dialog, 1).selectItem(type);
    }
}
