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

package org.netbeans.test.subversion.main.commit;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.SourcePackagesNode;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CommitUiTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;

    static Logger log;

    /** Creates a new instance of CheckoutUITest */
    public CommitUiTest(String name) {
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
                 NbModuleSuite.createConfiguration(CommitUiTest.class).addTest(
                    "testInvokeCloseCommit"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

    public void testInvokeCloseCommit() throws Exception {
//        try {
            MessageHandler mh = new MessageHandler("Committing");
            log.addHandler(mh);

            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                new NewProjectWizardOperator().invoke().close();
            
            new EventTool().waitNoEvent(3000);

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));

            System.out.println(TMP_PATH + File.separator + REPO_PATH +"  ,  "+ getDataDir().getCanonicalPath() + File.separator + "repo_dump");


            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getAbsolutePath() + File.separator + "repo_dump");
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            ImportWizardOperator iwo = ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
            new EventTool().waitNoEvent(3000);
            RepositoryStepOperator rso = new RepositoryStepOperator();
            new EventTool().waitNoEvent(3000);
            //rso.verify();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            new EventTool().waitNoEvent(3000);
            rso.next();
            new EventTool().waitNoEvent(3000);

            FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
            ftiso.setRepositoryFolder("trunk/Import_" + PROJECT_NAME);
            new EventTool().waitNoEvent(1000);
            ftiso.setImportMessage("initial import");
            new EventTool().waitNoEvent(1000);
            ftiso.next();
            new EventTool().waitNoEvent(3000);
            CommitStepOperator cso = new CommitStepOperator();
            cso.finish();

            TestKit.waitText(mh);

            TestKit.createNewElements(PROJECT_NAME, "xx", "NewClass");
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass2");
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass3");
            Node packNode = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            new EventTool().waitNoEvent(2000);
            CommitOperator co = CommitOperator.invoke(packNode);
            new EventTool().waitNoEvent(1000);
            co.selectCommitAction("NewClass.java", "Add As Text");
            co.selectCommitAction("NewClass.java", "Add As Binary");
            co.selectCommitAction("NewClass.java", "Exclude from Commit");
            co.selectCommitAction(2, "Add As Text");
//            co.selectCommitAction(2, "Add As Binary");
            co.selectCommitAction(2, "Exclude from Commit");

            JTableOperator table = co.tabFiles();
            TableModel model = table.getModel();
            String[] expected = {"xx", "NewClass.java", "NewClass2.java", "NewClass3.java"};
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < model.getRowCount(); i++) {
                actual[i] = model.getValueAt(i, 1).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Commit table doesn't contain all files!!!", expected.length, result);

            co.verify();
            co.cancel();
       // } catch (Exception e) {
         //   throw new Exception("Test failed: " + e);
       // } finally {
            TestKit.closeProject(PROJECT_NAME);
       // }
    }
}
