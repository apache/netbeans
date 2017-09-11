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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
