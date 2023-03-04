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
package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author novakm
 */
public class CloneTest extends JellyTestCase {

    public File projectPath;
    public PrintStream stream;
    String os_name;
    static File f;
    static Logger log;

    public CloneTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");
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
                NbModuleSuite.createConfiguration(CloneTest.class).addTest("testCloneProject").enableModules(".*").clusters(".*"));
    }

    public void testCloneProject() throws Exception {
        System.out.println("DEBUG: testCloneProject - start");
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
//        try {
//            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 15000);
//        } finally {
//            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
//        }
//
//        timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
//        try {
//            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 15000);
//        } finally {
//            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout);
//        }

        try {
            MessageHandler mh = new MessageHandler("Cloning");
            log.addHandler(mh);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            
            Node nodeFile;
            NbDialogOperator ndo;
            JButtonOperator bo;
            JTextFieldOperator tfo;
            TestKit.loadOpenProject(TestKit.PROJECT_NAME, getDataDir());
            String s = TestKit.getProjectAbsolutePath(TestKit.PROJECT_NAME);
            nodeFile = new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME);
            nodeFile.performMenuActionNoBlock("Team|Mercurial|Clone -");
            ndo = new NbDialogOperator("Clone Repository");
            bo = new JButtonOperator(ndo, "Clone");
            bo.push();
            String outputTabName = s;
            System.out.println(outputTabName);

            TestKit.waitText(mh);

            mh = new MessageHandler("Cloning");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            nodeFile = new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME);
            nodeFile.performMenuActionNoBlock("Team|Mercurial|Clone Other...");
            ndo = new NbDialogOperator("Clone External Repository");
            tfo = new JTextFieldOperator(ndo);
            String repoPath = "file://" + s.replace(File.separatorChar, "/".toCharArray()[0]);
            tfo.setText(repoPath);
            bo = new JButtonOperator(ndo, "Next");
            bo.push();
            bo.push();
            tfo = new JTextFieldOperator(ndo);
            tfo.setText(tfo.getText() + TestKit.CLONE_SUF_1);
            System.out.println(tfo.getText() + TestKit.CLONE_SUF_1);
            bo = new JButtonOperator(ndo, "Finish");
            bo.push();
            ndo = new NbDialogOperator("Checkout Completed");
            bo = new JButtonOperator(ndo, "Open Project");
            System.out.println(bo.getText());
            bo.push();
            outputTabName=repoPath;
            System.out.println(outputTabName);
            
            TestKit.waitText(mh);

            TestKit.closeProject(TestKit.PROJECT_NAME);
            TestKit.closeProject(TestKit.PROJECT_NAME);
            TestKit.closeProject(TestKit.PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(TestKit.PROJECT_NAME);
            TestKit.closeProject(TestKit.PROJECT_NAME);
            TestKit.closeProject(TestKit.PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
        System.out.println("DEBUG: testCloneProject - finish");
    }
}

