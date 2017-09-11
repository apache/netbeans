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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.performance.j2se.startup;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;

/**
 * Prepare user directory for complex measurements (startup time and memory
 * consumption) of IDE with opened project and 10 files. Open 10 java files and
 * shut down ide. Created user directory will be used to measure startup time
 * and memory consumption of IDE with opened files.
 *
 * @author Marian Mirilovic
 */
public class PrepareIDEForComplexMeasurements extends JellyTestCase {

    public static final String suiteName = "J2SE Prepare suite";

    /**
     * Error output from the test.
     */
    protected static PrintStream err;

    /**
     * Logging output from the test.
     */
    protected static PrintStream log;

    /**
     * If true - at least one test failed
     */
    protected static boolean test_failed = false;

    /**
     * Define testcase
     *
     * @param testName name of the testcase
     */
    public PrepareIDEForComplexMeasurements(String testName) {
        super(testName);
    }

    /**
     * Testsuite
     *
     * @return testuite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("Prepare IDE for Complex Measurements Suite");
        System.setProperty("suitename", PrepareIDEForComplexMeasurements.class.getCanonicalName());
        suite.addTest(JellyTestCase.emptyConfiguration()
                .addTest(PrepareIDEForComplexMeasurements.class)
                .addTest("testCloseAllDocuments")
                .addTest("testCloseMemoryToolbar")
                .addTest("testOpenFiles")
                .addTest("testSaveStatus")
                .suite());
        return suite;
    }

    @Override
    public void setUp() {
//        err = System.out;
        err = getLog();
        log = getRef();
    }

    /**
     * Close All Documents.
     */
    public void testCloseAllDocuments() {
        EditorOperator.closeDiscardAll();
    }

    /**
     * Close Memory Toolbar.
     */
    public static void testCloseMemoryToolbar() {
        closeToolbar(Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/View") + "|"
                + Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Toolbars") + "|"
                + Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Toolbars/Memory"));
    }

    private static void closeToolbar(String menu) {
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
        JMenuItemOperator menuItem = menuBar.showMenuItem(menu, "|");

        if (menuItem.isSelected()) {
            menuItem.push();
        } else {
            menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
    }

    /**
     * Open 10 selected files from jEdit project.
     */
    public void testOpenFiles() {
        String zipPath = Utilities.projectOpen("http://hg.netbeans.org/binaries/BBD005CDF8785223376257BD3E211C7C51A821E7-jEdit41.zip", "jEdit41.zip");
        Utilities.unzip(new File(zipPath), getWorkDirPath());
        try {
            openProjects(getWorkDirPath() + "/jEdit");
            String[][] files_path = {
                {"bsh", "Interpreter.java"},
                {"bsh", "JThis.java"},
                {"bsh", "Name.java"},
                {"bsh", "Parser.java"},
                {"bsh", "Primitive.java"},
                {"com.microstar.xml", "XmlParser.java"},
                {"org.gjt.sp.jedit", "BeanShell.java"},
                {"org.gjt.sp.jedit", "Buffer.java"},
                {"org.gjt.sp.jedit", "EditPane.java"},
                {"org.gjt.sp.jedit", "EditPlugin.java"},
                {"org.gjt.sp.jedit", "EditServer.java"}
            };

            Node[] openFileNodes = new Node[files_path.length];
            Node node;

            // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
            ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("jEdit");
            projectNode.expand();

            SourcePackagesNode sourceNode = new SourcePackagesNode(projectNode);
            sourceNode.expand();

            // create exactly (full match) and case sensitively comparing comparator
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            sourceNode.setComparator(comparator);

            for (int i = 0; i < files_path.length; i++) {
                node = new Node(sourceNode, files_path[i][0]);
                node.expand();

                openFileNodes[i] = new Node(node, files_path[i][1]);

                //try to avoid issue 56825
                openFileNodes[i].select();

                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
            }

            // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
            // it doesn't finish in the real-time -> hard to reproduced by hand
            try {
                new OpenAction().performAPI(openFileNodes);
            } catch (Exception exc) {
                err.println("---------------------------------------");
                err.println("issue 56825 : EXCEPTION catched during OpenAction");
                exc.printStackTrace(err);
                err.println("---------------------------------------");
                err.println("issue 56825 : Try it again");
                new OpenAction().performAPI(openFileNodes);
                err.println("issue 56825 : Success");
            }
            for (String[] path : files_path) {
                new EditorOperator(path[1]);
            }
//        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);

        } catch (Exception exc) {
            test_failed = true;
            fail(exc);
        }
    }

    /**
     * Save status, if one of the above defined test failed, this method creates
     * file in predefined path and it means the complex tests will not run.
     *
     * @throws IOException
     */
    public void testSaveStatus() throws IOException {
        System.setProperty("userdir.prepared", System.getProperty("netbeans.user"));
        if (test_failed) {
            MeasureStartupTimeTestCase.createStatusFile();
        }
    }
}
