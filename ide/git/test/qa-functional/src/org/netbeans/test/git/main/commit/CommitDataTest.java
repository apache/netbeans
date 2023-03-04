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
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.CommitOperator;
import org.netbeans.test.git.operators.VersioningOperator;
import org.netbeans.test.git.utils.MessageHandler;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar
 */
public class CommitDataTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    static Logger log;

    /**
     * Creates a new instance of CommitDataTest
     */
    public CommitDataTest(String name) {
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
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CommitDataTest.class).addTest(
                        "testCommitFile",
                        "testRecognizeMimeType"
                ).enableModules(".*").clusters(".*"));
    }

    public void testCommitFile() throws Exception {
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");

        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }
            TestKit.showStatusLabels();

            org.openide.nodes.Node nodeIDE;
            long start;
            long end;
            String color;
            JTableOperator table;

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);

            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            MessageHandler mh = new MessageHandler("Refreshing");
            log.addHandler(mh);
            TestKit.createNewElement(PROJECT_NAME, "javaapp", "NewClass");

            Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeFile.performPopupAction("Git|Show Changes");
            new EventTool().waitNoEvent(8000);
            //TestKit.waitText(mh);

            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            new EventTool().waitNoEvent(3000);
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            VersioningOperator vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(2000);
            table = vo.tabFiles();
            assertEquals("Wrong row count of table.", 1, table.getRowCount());
            assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);

            //invoke commit action but exlude the file from commit
            start = System.currentTimeMillis();
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            mh = new MessageHandler("Preparing Commit");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            CommitOperator cmo = CommitOperator.invoke(nodeFile);
            //TestKit.waitText(mh);

            end = System.currentTimeMillis();
            //print message to log file.
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            new EventTool().waitNoEvent(2000);
            cmo.selectCommitAction("NewClass.java", "Exclude from Commit");
            TimeoutExpiredException tee = null;
            assertFalse(cmo.btCommit().isEnabled());
            new EventTool().waitNoEvent(1000);
            cmo.cancel();
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            new EventTool().waitNoEvent(1500);
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            table = vo.tabFiles();
            new EventTool().waitNoEvent(1000);
            assertEquals("Wrong row count of table.", 1, table.getRowCount());
            assertEquals("Expected file is missing.", "NewClass.java", table.getModel().getValueAt(0, 0).toString());
            assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);

            mh = new MessageHandler("Preparing Commit");
            log.addHandler(mh);

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            cmo = CommitOperator.invoke(nodeFile);
            //TestKit.waitText(mh);
            new EventTool().waitNoEvent(2000);
            //cmo.selectCommitAction("NewClass.java", "Commit");
            start = System.currentTimeMillis();

            mh = new MessageHandler("Committing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            cmo.commit();

            //TestKit.waitText(mh);
            end = System.currentTimeMillis();

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            new EventTool().waitNoEvent(1000);
            vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(5000);
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }

    public void testRecognizeMimeType() throws Exception {
        try {
            TestKit.showStatusLabels();
            org.openide.nodes.Node nodeIDE;
            JTableOperator table;
            String color;
            String status;
            String[] expected = {"pp.bmp", "pp.dib", "pp.GIF", "pp.JFIF", "pp.JPE", "pp.JPEG", "pp.JPG", "pp.PNG", "pp.TIF", "pp.TIFF", "pp.zip", "text.txt", "test.jar"};

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            File work = TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);

            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            //create various types of files
            String src = getDataDir().getCanonicalPath() + File.separator + "files" + File.separator;
            String dest = work.getCanonicalPath() + File.separator + PROJECT_NAME + File.separator + "src" + File.separator + "javaapp" + File.separator;

            for (int i = 0; i < expected.length; i++) {
                TestKit.copyTo(src + expected[i], dest + expected[i]);
            }
            new EventTool().waitNoEvent(1000);
            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            Node nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeSrc.performPopupAction("Git|Show Changes");
            nodeSrc.expand();
            new EventTool().waitNoEvent(8000);
            nodeSrc.select();

            Node nodeTest;
            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
                status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
                color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
                assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
                assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
            }

            nodeSrc.select();
            VersioningOperator vo = VersioningOperator.invoke();
            nodeSrc.select();
            new EventTool().waitNoEvent(2000);
            TableModel model = vo.tabFiles().getModel();
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 0).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);

            nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            CommitOperator cmo = CommitOperator.invoke(nodeSrc);
            new EventTool().waitNoEvent(1500);
            table = cmo.tabFiles();
            model = table.getModel();
            actual = new String[model.getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 1).toString();
                if (actual[i].endsWith(".txt")) {
                    assertEquals("Expected text file.", "-/Added", model.getValueAt(i, 2).toString());
                } else {
                    assertEquals("Expected text file.", "-/Added", model.getValueAt(i, 2).toString());
                }
            }

            MessageHandler mh = new MessageHandler("Committing");
            log.addHandler(mh);

            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);
            cmo.commit();

            //TestKit.hideStatusLabels();
            nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeTest.expand();
            new EventTool().waitNoEvent(8000);

            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
                stream.print(expected[i] + ": " + nodeIDE.getHtmlDisplayName());
                //color = TestKit.getColor(expected[i], nodeIDE.getHtmlDisplayName());
                //assertEquals("", color);
                assertNull("Wrong status or color of node!!!", nodeIDE.getHtmlDisplayName());
            }

            //verify versioning view
            nodeSrc.select();
            vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(1500);
            TimeoutExpiredException tee = null;
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);

        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
