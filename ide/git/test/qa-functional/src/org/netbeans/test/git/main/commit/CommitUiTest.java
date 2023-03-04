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
package org.netbeans.test.git.main.commit;

import java.io.File;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.CommitOperator;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar
 */
public class CommitUiTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;

    String os_name;

    /**
     * Creates a new instance of CommitUITest
     */
    public CommitUiTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        System.out.println("### " + getName() + " ###");

    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CommitUiTest.class).addTest(
                        "testInvokeCloseCommit"
                ).enableModules(".*").clusters(".*"));
    }

    public void testInvokeCloseCommit() throws Exception {
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");

        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }

            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);
            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }
            TestKit.createNewElements(PROJECT_NAME, "xx", "NewClass");
            new EventTool().waitNoEvent(1000);
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass2");
            new EventTool().waitNoEvent(1000);
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass3");
            new EventTool().waitNoEvent(1000);
            Node packNode = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            CommitOperator co = CommitOperator.invoke(packNode);
            new EventTool().waitNoEvent(2000);

            co.selectCommitAction("NewClass.java", "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction("NewClass.java", "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction("NewClass.java", "Exclude from Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction(2, "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction(2, "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction(2, "Exclude from Commit");
            new EventTool().waitNoEvent(500);

            JTableOperator table = co.tabFiles();
            TableModel model = table.getModel();
            String[] expected = {"NewClass.java", "NewClass2.java", "NewClass3.java"};
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < model.getRowCount(); i++) {
                actual[i] = model.getValueAt(i, 1).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Commit table doesn't contain all files!!!", expected.length, result);

            co.verify();
            co.cancel();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
