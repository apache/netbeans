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
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.SourcePackagesNode;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter pis
 */
public class IgnoreTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    static Logger log;

    /**
     * Creates a new instance of IgnoreTest
     */
    public IgnoreTest(String name) {
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
                NbModuleSuite.createConfiguration(IgnoreTest.class).addTest(
                "testIgnoreUnignoreFile",
                "testIgnoreUnignorePackage",
                "testIgnoreUnignoreFilePackage",
                "testFinalRemove").enableModules(".*").clusters(".*"));
    }

    public void testIgnoreUnignoreFile() throws Exception {
        // try {
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);

        TestKit.closeProject(PROJECT_NAME);
        if (TestKit.getOsName().indexOf("Mac") > -1) {
            new NewProjectWizardOperator().invoke().close();
        }

        //VersioningOperator vo = VersioningOperator.invoke();
        TestKit.showStatusLabels();

        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();

        //create repository...
        File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
        new File(TMP_PATH).mkdirs();
        work.mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getAbsolutePath() + File.separator + "repo_dump");
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
        wdso.setLocalFolder(work.getCanonicalPath());
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project

        TestKit.waitText(mh);

        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        TestKit.waitForScanFinishedSimple();

        TestKit.createNewElement(PROJECT_NAME, "javaapp", "NewClass");

        mh = new MessageHandler("Ignoring");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
        node.performPopupAction("Subversion|Ignore|Ignore");
        new EventTool().waitNoEvent(2000);
        TestKit.waitText(mh);

        node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
        org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        new EventTool().waitNoEvent(5000);
        String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node - file color should be ignored!!!", TestKit.IGNORED_COLOR, color);
        assertEquals("Wrong annotation of node - file status should be ignored!!!", TestKit.IGNORED_STATUS, status);
        /*
         * node = new Node(new SourcePackagesNode(PROJECT_NAME),
         * "javaapp|NewClass"); JemmyException tee = null; try {
         * JPopupMenuOperator jpmo=node.callPopup();
         * jpmo.pushMenu("Subversion|Ignore");
         *
         * } catch (JemmyException e) {
         *
         * tee = e; } Thread.sleep(2000); assertNotNull("Ingnore action should
         * be disabled!!!", tee);
         *
         * //unignore file Thread.sleep(2000);
         *
         * mh = new MessageHandler("Unignoring"); TestKit.removeHandlers(log);
         * log.addHandler(mh); Thread.sleep(2000); node = new Node(new
         * SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.select();
         */
        new EventTool().waitNoEvent(2000);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
        new EventTool().waitNoEvent(2000);
        node.performPopupAction("Subversion|Ignore|Unignore");
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(2000);

        node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
        nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        new EventTool().waitNoEvent(2000);
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node - file color should be new!!!", TestKit.NEW_COLOR, color);
        assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.NEW_STATUS, status);

        //verify content of Versioning view
        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
        node.performPopupAction("Subversion|Show Changes");
        new EventTool().waitNoEvent(2000);
        TestKit.waitText(mh);

        new EventTool().waitNoEvent(2000);
        VersioningOperator vo = VersioningOperator.invoke();
        vo = VersioningOperator.invoke();
        TableModel model = vo.tabFiles().getModel();
        assertEquals("Versioning view should be empty", 1, model.getRowCount());
        assertEquals("File should be listed in Versioning view", "NewClass.java", model.getValueAt(0, 0).toString());

        stream.flush();
        stream.close();
        //} catch (Exception e) {
        //   throw new Exception("Test failed: " + e);
        // } finally {
        TestKit.closeProject(PROJECT_NAME);
        // }    
    }

    public void testIgnoreUnignorePackage() throws Exception {
        //   try {
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);
        TestKit.closeProject(PROJECT_NAME);
        new EventTool().waitNoEvent(2000);
        //VersioningOperator vo = VersioningOperator.invoke();
        TestKit.showStatusLabels();

        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();

        //create repository...
        File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
        new File(TMP_PATH).mkdirs();
        work.mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getAbsolutePath() + File.separator + "repo_dump");
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
        wdso.setLocalFolder(work.getCanonicalPath());
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project

        TestKit.waitText(mh);

        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();

        TestKit.waitForScanFinishedSimple();

        TestKit.createNewPackage(PROJECT_NAME, "xx");

        mh = new MessageHandler("Ignoring");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        node.performPopupAction("Subversion|Ignore|Ignore");

        TestKit.waitText(mh);

        new EventTool().waitNoEvent(2000);


        node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        new EventTool().waitNoEvent(2000);
        String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(2000);
        assertEquals("Wrong annotation of node - package status should be ignored!!!", TestKit.IGNORED_STATUS, status);
        /*
         * node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
         * TimeoutExpiredException tee = null; try {
         * node.performPopupAction("Subversion|Ignore"); } catch (Exception e) {
         * tee = (TimeoutExpiredException) e; } Thread.sleep(2000);
         * assertNotNull("Ingnore action should be disabled!!!", tee);
         *
         * //unignore file mh = new MessageHandler("Unignoring");
         * TestKit.removeHandlers(log);
            log.addHandler(mh);
         */
        new EventTool().waitNoEvent(2000);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        node.select();

        new EventTool().waitNoEvent(2000);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        new EventTool().waitNoEvent(2000);
        node.performPopupAction("Subversion|Ignore|Unignore");
        new EventTool().waitNoEvent(2000);
        TestKit.waitText(mh);

        node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        new EventTool().waitNoEvent(2000);
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong annotation of node - package status should be new!!!", TestKit.NEW_STATUS, status);

        //verify content of Versioning view
        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        node.performPopupAction("Subversion|Show Changes");

        TestKit.waitText(mh);

        new EventTool().waitNoEvent(4000);

        VersioningOperator vo = VersioningOperator.invoke();
        vo = VersioningOperator.invoke();
        TableModel model = vo.tabFiles().getModel();
        new EventTool().waitNoEvent(2000);
        assertEquals("Versioning view should be empty", 1, model.getRowCount());
        assertEquals("Package should be listed in Versioning view", "xx", model.getValueAt(0, 0).toString());

        stream.flush();
        stream.close();
        //  } catch (Exception e) {
        //     throw new Exception("Test failed: " + e);
        //   } finally {
        TestKit.closeProject(PROJECT_NAME);
        // }    
    }

    public void testIgnoreUnignoreFilePackage() throws Exception {
        //   try {
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);
        TestKit.closeProject(PROJECT_NAME);
        new EventTool().waitNoEvent(2000);
        //VersioningOperator vo = VersioningOperator.invoke();
        TestKit.showStatusLabels();

        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();

        //create repository...
        File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
        new File(TMP_PATH).mkdirs();
        work.mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getAbsolutePath() + File.separator + "repo_dump");
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
        wdso.setLocalFolder(work.getCanonicalPath());
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project

        TestKit.waitText(mh);

        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        TestKit.waitForScanFinishedSimple();

        TestKit.createNewElements(PROJECT_NAME, "xx", "NewClass");
        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        Node node2 = new Node(new SourcePackagesNode(PROJECT_NAME), "xx|NewClass");

        mh = new MessageHandler("Ignoring");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        node.performPopupAction("Subversion|Ignore|Ignore");

        TestKit.waitText(mh);
        new EventTool().waitNoEvent(6000);

        node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        node2 = new Node(new SourcePackagesNode(PROJECT_NAME), "xx|NewClass");
        org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        org.openide.nodes.Node nodeIDE2 = (org.openide.nodes.Node) node2.getOpenideNode();
        new EventTool().waitNoEvent(2000);
        String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        String status2 = TestKit.getStatus(nodeIDE2.getHtmlDisplayName());
        assertEquals("Wrong annotation of node - package status should be ignored!!!", TestKit.IGNORED_STATUS, status);
        assertEquals("Wrong annotation of file - package status should be ignored!!!", TestKit.IGNORED_STATUS, status2);

        //unignore file
        mh = new MessageHandler("Unignoring");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        node.performPopupAction("Subversion|Ignore|Unignore");

        TestKit.waitText(mh);

        new EventTool().waitNoEvent(5000);
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        node2 = new Node(new SourcePackagesNode(PROJECT_NAME), "xx|NewClass");
        nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        nodeIDE2 = (org.openide.nodes.Node) node2.getOpenideNode();
        String color = TestKit.getColor(nodeIDE2.getHtmlDisplayName());
        new EventTool().waitNoEvent(2000);
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        status2 = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node - file color should be new!!!", TestKit.NEW_COLOR, color);
        assertEquals("Wrong annotation of node - package status should be new!!!", TestKit.NEW_STATUS, status);
        assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.NEW_STATUS, status2);

        //verify content of Versioning view
        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        node.performPopupAction("Subversion|Show Changes");

        TestKit.waitText(mh);

        new EventTool().waitNoEvent(4000);
        VersioningOperator vo = VersioningOperator.invoke();
        vo = VersioningOperator.invoke();
        TableModel model = vo.tabFiles().getModel();
        new EventTool().waitNoEvent(2000);
        assertEquals("Versioning view should be empty", 2, model.getRowCount());
        String[] expected = {"xx", "NewClass.java"};
        String[] actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records in Versioning view", 2, result);
        stream.flush();
        stream.close();
        //} catch (Exception e) {
        //    throw new Exception("Test failed: " + e);
        // } finally {
        TestKit.closeProject(PROJECT_NAME);
        // }    
    }

    public void testFinalRemove() throws Exception {
        TestKit.finalRemove();
    }
}
