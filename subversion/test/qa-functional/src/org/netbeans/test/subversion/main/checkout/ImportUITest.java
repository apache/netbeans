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
package org.netbeans.test.subversion.main.checkout;

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
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.CreateNewFolderOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserImpOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class ImportUITest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    long timeout_c;
    long timeout_d;
    static Logger log;

    /**
     * Creates a new instance of ImportUITest
     */
    public ImportUITest(String name) {
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
                NbModuleSuite.createConfiguration(ImportUITest.class).addTest(
                "testInvoke",
                "testWarningMessage",
                //"testRepositoryFolderLoad"//,
                "testCommitStep").enableModules(".*").clusters(".*"));
    }

    public void testInvoke() throws Exception {
        try {
            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                new NewProjectWizardOperator().invoke().close();
            }
            new EventTool().waitNoEvent(2000);
            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            Operator.setDefaultStringComparator(comOperator);
            ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
            Operator.setDefaultStringComparator(oldOperator);
            new EventTool().waitNoEvent(2000);
            iwo.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }

    public void testWarningMessage() throws Exception {
        
        TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                new NewProjectWizardOperator().invoke().close();
        new EventTool().waitNoEvent(2000);   
        
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        new EventTool().waitNoEvent(2000);   
        //rso.verify();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        rso.next();
        new EventTool().waitNoEvent(3000);
        FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
        ftiso.verify();

        //Warning message for empty REPOSITORY FOLDER
        ftiso.setRepositoryFolder("");
        new EventTool().waitNoEvent(1500);
        assertEquals("Repository Folder must be specified", "\nRepository Folder must be specified", ftiso.lblImportMessageRequired().getDisplayedText());
        assertFalse("Next button should be disabled", ftiso.btNext().isEnabled());
        assertFalse("Finish button should be disabled", ftiso.btFinish().isEnabled());

        //Warning message for empty import message
        ftiso.setRepositoryFolder(PROJECT_NAME);
        ftiso.setImportMessage("");
        assertEquals("Import message required", "\nImport Message required", ftiso.lblImportMessageRequired().getDisplayedText());
        assertFalse("Next button should be disabled", ftiso.btNext().isEnabled());
        assertFalse("Finish button should be disabled", ftiso.btFinish().isEnabled());

        //NO Warning message if both are setup correctly.
        ftiso.setRepositoryFolder(PROJECT_NAME);
        ftiso.setImportMessage("initial import");
        assertEquals("No Warning message", "", ftiso.lblImportMessageRequired().getDisplayedText());
        assertTrue("Next button should be enabled", ftiso.btNext().isEnabled());
        //Finish button should be enabled.
        assertTrue("Finish button should be enabled", ftiso.btFinish().isEnabled());
        iwo.cancel();

        TestKit.closeProject(PROJECT_NAME);
    }

    public void testRepositoryFolderLoad() throws Exception {


        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
        projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //rso.verify();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        rso.next();
        Thread.sleep(2000);

        FolderToImportStepOperator ftiso = new FolderToImportStepOperator();

        //only required nodes are expended - want to see all in browser
        ftiso.setRepositoryFolder("");
        RepositoryBrowserImpOperator rbo = ftiso.browseRepository();
        rbo.selectFolder("branches");
        rbo.selectFolder("tags");
        rbo.selectFolder("trunk");
        rbo.selectFolder("trunk|JavaApp|src|javaapp");
        rbo.ok();
        assertEquals("Wrong folder selection!!!", "trunk/JavaApp/src/javaapp", ftiso.getRepositoryFolder());
        //
        ftiso.setRepositoryFolder("trunk");
        rbo = ftiso.browseRepository();
        rbo.selectFolder("trunk");
        CreateNewFolderOperator cnfo = rbo.createNewFolder();
        cnfo.setFolderName(PROJECT_NAME);
        cnfo.ok();
        rbo.selectFolder("trunk|" + PROJECT_NAME);
        rbo.ok();
        assertEquals("Wrong folder selection!!!", "trunk/" + PROJECT_NAME, ftiso.getRepositoryFolder());

        //
        ftiso.setRepositoryFolder("");
        rbo = ftiso.browseRepository();
        rbo.selectFolder("branches");
        cnfo = rbo.createNewFolder();
        cnfo.setFolderName("release_01");
        cnfo.ok();
        rbo.ok();
        assertEquals("Wrong folder selection!!!", "branches/release_01", ftiso.getRepositoryFolder());

        rbo = ftiso.browseRepository();
        rbo.selectFolder("branches|release_01");
        cnfo = rbo.createNewFolder();
        cnfo.setFolderName(PROJECT_NAME);
        cnfo.ok();
        rbo.selectFolder("branches|release_01|" + PROJECT_NAME);

        rbo.ok();
        assertEquals("Wrong folder selection!!!", "branches/release_01/" + PROJECT_NAME, ftiso.getRepositoryFolder());

        iwo.cancel();

        TestKit.closeProject(PROJECT_NAME);

    }

    public void testCommitStep() throws Exception {

        MessageHandler mh = new MessageHandler("Committing");
        log.addHandler(mh);
//            TestKit.showStatusLabels();
        TestKit.closeProject(PROJECT_NAME);
        if (TestKit.getOsName().indexOf("Mac") > -1) {
            new NewProjectWizardOperator().invoke().close();
        }
        new EventTool().waitNoEvent(2000);
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
        projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Operator.setDefaultStringComparator(comOperator);
        new EventTool().waitNoEvent(2000);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        new EventTool().waitNoEvent(2000);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //rso.verify();
        new EventTool().waitNoEvent(3000);
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        rso.next();
        new EventTool().waitNoEvent(2000);

        FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
        ftiso.setRepositoryFolder("trunk/Import" + PROJECT_NAME);
        ftiso.setImportMessage("initial import");
        ftiso.next();
        new EventTool().waitNoEvent(2000);
        CommitStepOperator cso = new CommitStepOperator();
        cso.verify();

        new EventTool().waitNoEvent(2000);
        JTableOperator table = cso.tabFiles();
        TableModel model = table.getModel();
        new EventTool().waitNoEvent(2000);
        String[] expected = {"genfiles.properties", "build-impl.xml", "JavaApp.java", "manifest.mf", "src", "project.xml", PROJECT_NAME.toLowerCase(), "nbproject", "project.properties", "test", "build.xml"};
        String[] actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 1).toString();
        }

        assertEquals("Incorrect count of records for addition!!!", 10, model.getRowCount());


        //   assertEquals("Some records were omitted from addition", 10, TestKit.compareThem(expected, actual, false));
        //try to change commit actions
        cso.selectCommitAction("project.xml", "Add As Text");
        cso.selectCommitAction("project.xml", "Add As Binary");
        cso.selectCommitAction("project.xml", "Exclude from Commit");
        //cso.selectCommitAction("test", "Exclude Recursively");
        //cso.selectCommitAction("test", "Include Recursively");
        iwo.cancel();

        TestKit.closeProject(PROJECT_NAME);

    }

    public void testStopProcess() throws Exception {

        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
        projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //rso.verify();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        rso.next();
        //Stop process in 1st step of Import wizard
        rso.btStop().push();
        assertEquals("Warning message - process was cancelled by user", "Action canceled by user", rso.txtPaneWarning().getText());
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        rso = new RepositoryStepOperator();
        //rso.verify();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        rso.next();

        FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
        ftiso.setRepositoryFolder("trunk/" + PROJECT_NAME);
        ftiso.setImportMessage("initial import");
        ftiso.next();
        //Stop process in 2st step of Import wizard
        ftiso.btStop().push();

        ftiso = new FolderToImportStepOperator();
        //ftiso.verify();
        ftiso.back();

        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        rso = new RepositoryStepOperator();
        rso.verify();

        TestKit.closeProject(PROJECT_NAME);

    }
}
