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
package org.netbeans.performance.j2se.startup;

import java.io.File;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;

/**
 * Prepare user directory for complex measurements (startup time and memory
 * consumption) of IDE with opened NB plug-in. Open 3 java files and shut down
 * ide. Created user directory will be used to measure startup time and memory
 * consumption of IDE with opened files.
 *
 * @author Marian Mirilovic
 */
public class PrepareIDEForPluginComplexMeasurements extends PrepareIDEForComplexMeasurements {

    public static final String suiteName = "J2SE Prepare suite";

    /**
     * Define testcase
     *
     * @param testName name of the testcase
     */
    public PrepareIDEForPluginComplexMeasurements(String testName) {
        super(testName);
    }

    /**
     * Testsuite
     *
     * @return testuite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("Prepare IDE for Plugin Complex Measurements Suite");
        System.setProperty("suitename", PrepareIDEForPluginComplexMeasurements.class.getCanonicalName());
        suite.addTest(JellyTestCase.emptyConfiguration()
                .addTest(PrepareIDEForPluginComplexMeasurements.class)
                .addTest("testCloseAllDocuments")
                .addTest("testCloseMemoryToolbar")
                .addTest("testOpenFiles")
                .addTest("testSaveStatus")
                .suite());
        return suite;
    }

    /**
     * Open 3 selected files from jEdit project.
     */
    @Override
    public void testOpenFiles() {
        try {
            openDataProjects("SystemProperties");
            String[][] files_path = {
                {"org.myorg.systemproperties", "AllPropsChildren.java"},
                {"org.myorg.systemproperties", "AllPropsNode.java"},
                {"org.myorg.systemproperties", "OnePropNode.java"},
                {"org.myorg.systemproperties", "PropertiesNotifier.java"},
                {"org.myorg.systemproperties", "RefreshPropsAction.java"}
            };

            Node[] openFileNodes = new Node[files_path.length];
            Node node;

            // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
            ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("SystemProperties");
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

            // check whether files are opened in editor
            for (int i = 0; i < files_path.length; i++) {
                new EditorOperator(files_path[i][1]);
            }
//        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);

        } catch (Exception exc) {
            test_failed = true;
            fail(exc);
        }
    }

}
