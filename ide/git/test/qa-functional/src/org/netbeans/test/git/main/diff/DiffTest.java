/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.git.main.diff;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.DiffOperator;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.test.git.operators.VersioningOperator;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar 
 */
public class DiffTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public PrintStream stream;
    static Logger log;

    /**
     * Creates a new instance of DiffTest
     */
    public DiffTest(String name) {
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
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(DiffTest.class)
                .addTest("testDiffFile")
                .enableModules(".*")
                .clusters(".*")
        );
    }

    public void testDiffFile() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }
            NbDialogOperator dialog;
            
            TestKit.showStatusLabels();
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);

            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Git|Show Changes");
            new EventTool().waitNoEvent(2000);
            
            //modify file
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Open");
            EditorOperator eo = new EditorOperator("Main.java");
            eo.deleteLine(2);
            eo.insert(" insert", 10, 18);
            eo.insert("\tSystem.out.println(\"\");\n", 19, 1);
            eo.save();
            
            new EventTool().waitNoEvent(2000);
            VersioningOperator vo = VersioningOperator.invoke();
            vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(8000);
            //Save action should change the file annotations
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.MODIFIED_COLOR, color.toUpperCase());
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.MODIFIED_STATUS, status);
            assertEquals("Wrong number of records in Versioning view!!!", 1, vo.tabFiles().getRowCount());
            
            new EventTool().waitNoEvent(2000);
            node.performPopupAction("Git|Diff|Diff To HEAD");
            new EventTool().waitNoEvent(2000);

            DiffOperator diffOp = new DiffOperator("Main.java");
            //
            try {
                TimeoutExpiredException afee = null;
                diffOp.next();
                diffOp.next();
                try {
                    diffOp.next();
                } catch (TimeoutExpiredException e) {
                    afee = e;
                }
                assertNotNull("TimeoutExpiredException was expected.", afee);

                //verify previous button
                afee = null;
                diffOp.previous();
                diffOp.previous();
                try {
                    diffOp.previous();
                } catch (TimeoutExpiredException e) {
                    afee = e;
                }
                assertNotNull("TimeoutExpiredException was expected.", afee);
            } catch (Exception e) {
                System.out.println("Problem with buttons of differences");
            }

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
