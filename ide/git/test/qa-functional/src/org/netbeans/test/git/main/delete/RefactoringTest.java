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
package org.netbeans.test.git.main.delete;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.CommitOperator;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.test.git.operators.VersioningOperator;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar
 */
public class RefactoringTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public PrintStream stream;
    static Logger log;

    /**
     * Creates a new instance of RefactoringTest
     */
    public RefactoringTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }

    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(RefactoringTest.class)
                .addTest("testRefactoring")
                .enableModules(".*")
                .clusters(".*")
        );
    }

    public void testRefactoring() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }
            
            NbDialogOperator dialog;
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);

            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Git|Show Changes");
            new EventTool().waitNoEvent(2000);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.performPopupActionNoBlock("Refactor|Rename...");
            dialog = new NbDialogOperator("Rename");
            new EventTool().waitNoEvent(5000);

            JTextFieldOperator txt = new JTextFieldOperator(dialog);
            txt.setText("javaapp_ren");
            JButtonOperator btn = new JButtonOperator(dialog, "Refactor");
            btn.push();
            dialog.waitClosed();
            new EventTool().waitNoEvent(3000);

            VersioningOperator vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(3000);
            String[] expected = new String[]{"Main.java"};
            String[] actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getValueAt(i, 0).toString().trim();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong files in Versioning View", 1, result);

            new EventTool().waitNoEvent(3000);
            expected = new String[]{"Renamed/Modified"};
            vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(3000);
            actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getValueAt(i, 1).toString().trim();
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong status in Versioning View", 1, result);

            node = new SourcePackagesNode(PROJECT_NAME);
            CommitOperator cmo = CommitOperator.invoke(node);

            new EventTool().waitNoEvent(3000);
            expected = new String[]{"Main.java", "Main.java"};
            new EventTool().waitNoEvent(3000);
            actual = new String[cmo.tabFiles().getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = cmo.tabFiles().getValueAt(i, 1).toString();
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong files in Commit dialog", 2, result);

            expected = new String[]{"Deleted/-", "Renamed/Modified"};
            new EventTool().waitNoEvent(3000);
            actual = new String[cmo.tabFiles().getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = cmo.tabFiles().getValueAt(i, 2).toString();
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong status in Commit dialog", 2, result);
            cmo.commit();

            JTableOperator table;
            Exception e = null;
            try {
                Thread.sleep(2000);
                vo = VersioningOperator.invoke();
                table = vo.tabFiles();
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Unexpected behavior - Versioning view should be empty!!!", e);

            new EventTool().waitNoEvent(2000);
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }

}
