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
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JDialogOperator.JDialogFinder;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.operators.CommitOperator;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author novakm
 */
public class InitializeTest extends JellyTestCase {

    public File projectPath;
    public PrintStream stream;
    String os_name;
    static File f;
    static Logger log;

    public InitializeTest(String name) {
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
                NbModuleSuite.createConfiguration(InitializeTest.class).addTest("testInitializeAndFirstCommit").enableModules(".*").clusters(".*"));
    }
    
    public void testInitializeAndFirstCommit() throws Exception {
        System.out.println("DEBUG: testInitializeAndFirstCommit - start");
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
//        try {
//            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 5000);
//        } finally {
//            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
//        }
//
//        timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
//        try {
//            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
//        } finally {
//            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout);
//        }
         NbDialogOperator ndo = null;
        try {
            long start;
            long end;
            JTableOperator table;
            Node nodeFile;
            TestKit.showStatusLabels();
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            
            
            f = TestKit.prepareProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(1000);
            String s = f.getAbsolutePath() + File.separator + TestKit.PROJECT_NAME;
            new EventTool().waitNoEvent(1000);
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            new EventTool().waitNoEvent(1000);

            MessageHandler mh ;//= new MessageHandler("Initializing");
            TestKit.TIME_OUT = 25;
   //         log.addHandler(mh);

//            MessageHandler mh2 = new MessageHandler("Adding");
//            log.addHandler(mh2);

            nodeFile = new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME);
            nodeFile.performPopupActionNoBlock("Versioning|Initialize Mercurial Project");

            ndo = new NbDialogOperator("Repository root path");
            JButtonOperator buttOperator = new JButtonOperator(ndo,"OK");
            buttOperator.push();


            System.out.println(s);
            System.out.println("tady1");

  //          TestKit.waitText(mh);
//            TestKit.waitText(mh2);

            new EventTool().waitNoEvent(1000);



//            mh = new MessageHandler("Refreshing");
            TestKit.removeHandlers(log);
//            log.addHandler(mh);
            nodeFile.performPopupAction("Mercurial|Status");

//            TestKit.waitText(mh);

            new EventTool().waitNoEvent(1000);
            VersioningOperator vo = VersioningOperator.invoke();

            System.out.println("tady2");

            table = vo.tabFiles();
            assertEquals("Wrong row count of table.", 8, table.getRowCount());
            start = System.currentTimeMillis();
            CommitOperator cmo = CommitOperator.invoke(nodeFile);
            end = System.currentTimeMillis();
            System.out.println("Duration of invoking Commit dialog: " + (end - start));
            //print message to log file.
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));

            mh = new MessageHandler("Committing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            cmo.setCommitMessage("init");
            cmo.commit();
            
            TestKit.waitText(mh);

            TestKit.TIME_OUT = 15;

            new EventTool().waitNoEvent(1000);
            vo = VersioningOperator.invoke();
            TimeoutExpiredException tee = null;
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            stream.flush();
            stream.close();
            TestKit.closeProject(TestKit.PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(TestKit.PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
        System.out.println("DEBUG: testInitializeAndFirstCommit - finish");
    }
}
