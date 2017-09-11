/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
