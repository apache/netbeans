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
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.SourcePackagesNode;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CommitDataTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    long timeout_c;
    long timeout_d;
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
                "testCommitPackage",
                "testRecognizeMimeType").enableModules(".*").clusters(".*"));
    }

    public void testCommitFile() throws Exception {
        // try {
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);
        TestKit.closeProject(PROJECT_NAME);
        if (TestKit.getOsName().indexOf("Mac") > -1) {
            new NewProjectWizardOperator().invoke().close();
        }
        new EventTool().waitNoEvent(2000);
        TestKit.showStatusLabels();
        //VersioningOperator vo = VersioningOperator.invoke();

        org.openide.nodes.Node nodeIDE;
        long start;
        long end;
        String color;
        JTableOperator table;

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
        new EventTool().waitNoEvent(2000);
        open.push();
        TestKit.waitForScanFinishedSimple();

        TestKit.createNewElement(PROJECT_NAME, "javaapp", "NewClass");

        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
        new EventTool().waitNoEvent(2000);
        nodeFile.performPopupAction("Subversion|Show Changes");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());

        TestKit.waitText(mh);

        VersioningOperator vo = VersioningOperator.invoke();
        vo = VersioningOperator.invoke();
        new EventTool().waitNoEvent(2000);
        table = vo.tabFiles();
        assertEquals("Wrong row count of table.", 1, table.getRowCount());
        assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);

        //invoke commit action but exlude the file from commit
        start = System.currentTimeMillis();
//            mh = new MessageHandler("Refreshing");
//            TestKit.removeHandlers(log);
//            log.addHandler(mh);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
        CommitOperator cmo = CommitOperator.invoke(nodeFile);

//            TestKit.waitText(mh);

        end = System.currentTimeMillis();
        //print message to log file.
        TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
        new EventTool().waitNoEvent(2000);
        cmo.selectCommitAction("NewClass.java", "Exclude From Commit");
        new EventTool().waitNoEvent(2000);
        TimeoutExpiredException tee = null;
        assertFalse(cmo.btCommit().isEnabled());
        cmo.cancel();
        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        table = vo.tabFiles();
        new EventTool().waitNoEvent(2000);
        assertEquals("Wrong row count of table.", 1, table.getRowCount());
        assertEquals("Expected file is missing.", "NewClass.java", table.getModel().getValueAt(0, 0).toString());
        assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);

        mh = new MessageHandler("Committing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
        cmo = CommitOperator.invoke(nodeFile);
        cmo.selectCommitAction("NewClass.java", "Add as Text");
        start = System.currentTimeMillis();
        cmo.commit();

        TestKit.waitText(mh);

        end = System.currentTimeMillis();

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
       new EventTool().waitNoEvent(2000);
        vo = VersioningOperator.invoke();
        try {
            vo.tabFiles();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("There shouldn't be any table in Versioning view", tee);
        stream.flush();
        stream.close();
        //} catch (Exception e) {
        //    throw new Exception("Test failed: " + e);
        //} finally {        
        TestKit.closeProject(PROJECT_NAME);
        //}    
    }

    public void testCommitPackage() throws Exception {
//        try {
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);

        org.openide.nodes.Node nodeIDE;
        JTableOperator table;
        long start;
        long end;
        String status;
        
        TestKit.closeProject(PROJECT_NAME);
        if (TestKit.getOsName().indexOf("Mac") > -1) {
            new NewProjectWizardOperator().invoke().close();
        }
        new EventTool().waitNoEvent(2000);
        //VersioningOperator vo = VersioningOperator.invoke();

        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
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
        new EventTool().waitNoEvent(2000);
        open.push();
        TestKit.waitForScanFinishedSimple();

        TestKit.createNewPackage(PROJECT_NAME, "xx");
        Node nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");

        nodePack.performPopupAction("Subversion|Show Changes");
        new EventTool().waitNoEvent(2000);
        nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
        new EventTool().waitNoEvent(2000);
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(2000);
        VersioningOperator vo = VersioningOperator.invoke();
        vo = VersioningOperator.invoke();
        table = vo.tabFiles();
        new EventTool().waitNoEvent(2000);
        assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
        assertEquals("Wrong row count of table.", 1, table.getRowCount());
        assertEquals("Expected folder is missing.", "xx", table.getModel().getValueAt(0, 0).toString());

        //invoke commit action but exlude the file from commit
        start = System.currentTimeMillis();
        nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        CommitOperator cmo = CommitOperator.invoke(nodePack);
        end = System.currentTimeMillis();
        //print log message
        TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
        new EventTool().waitNoEvent(3000);
        cmo.selectCommitAction("xx", "Exclude From Commit");
        new EventTool().waitNoEvent(2000);
        assertFalse(cmo.btCommit().isEnabled());
        new EventTool().waitNoEvent(3000);
        cmo.cancel();
        nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
        new EventTool().waitNoEvent(2000);
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(2000);
        assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);

        mh = new MessageHandler("Committing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        cmo = CommitOperator.invoke(nodePack);
        //cmo.selectCommitAction("xx", "Exclude Recursively");
        //cmo.selectCommitAction("xx", "Include Recursively");
        start = System.currentTimeMillis();
        cmo.commit();
        new EventTool().waitNoEvent(2000);
        TestKit.waitText(mh);

        end = System.currentTimeMillis();

        nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
        nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
        TestKit.printLogStream(stream, "Duration of committing folder: " + (end - start));
        new EventTool().waitNoEvent(2000);
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong status of node!!!", TestKit.UPTODATE_STATUS, status);
        new EventTool().waitNoEvent(2000);
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
        // } catch (Exception e) {
        //     throw new Exception("Test failed: " + e);
        // } finally {
        TestKit.closeProject(PROJECT_NAME);
        //}    
    }

    public void testRecognizeMimeType() throws Exception {
        //try {
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);
        
        org.openide.nodes.Node nodeIDE;
        JTableOperator table;
        String color;
        String status;
        String[] expected = {"pp.bmp", "pp.dib", "pp.GIF", "pp.JFIF", "pp.JPE", "pp.JPEG", "pp.JPG", "pp.PNG", "pp.TIF", "pp.TIFF", "pp.zip", "text.txt", "test.jar"};
        TestKit.closeProject(PROJECT_NAME);
        if (TestKit.getOsName().indexOf("Mac") > -1) {
            new NewProjectWizardOperator().invoke().close();
        }
        new EventTool().waitNoEvent(2000);
        //VersioningOperator vo = VersioningOperator.invoke();

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

        //create various types of files
        String src = getDataDir().getCanonicalPath() + File.separator + "files" + File.separator;
        String dest = work.getCanonicalPath() + File.separator + PROJECT_NAME + File.separator + "src" + File.separator + "javaapp" + File.separator;

        for (int i = 0; i < expected.length; i++) {
            TestKit.copyTo(src + expected[i], dest + expected[i]);
        }

        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        Node nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        nodeSrc.performPopupAction("Subversion|Show Changes");
        TestKit.waitText(mh);

        Node nodeTest;
        nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        nodeTest.expand();
        new EventTool().waitNoEvent(5000);
        for (int i = 0; i < expected.length; i++) {
            nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
            nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
            assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
        }

        VersioningOperator vo = VersioningOperator.invoke();
        vo = VersioningOperator.invoke();
        new EventTool().waitNoEvent(2000);
        TableModel model = vo.tabFiles().getModel();
        String[] actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Not All files listed in Commit dialog", expected.length, result);

        mh = new MessageHandler("Committing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        CommitOperator cmo = CommitOperator.invoke(nodeSrc);
        new EventTool().waitNoEvent(5000);
        table = cmo.tabFiles();
        model = table.getModel();
        actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 1).toString();
            if (actual[i].endsWith(".txt")) {
                assertEquals("Expected text file.", "Add as Text", model.getValueAt(i, 3).toString());
            } else {
                assertEquals("Expected text file.", "Add as Binary", model.getValueAt(i, 3).toString());
            }
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Not All files listed in Commit dialog", expected.length, result);
        cmo.commit();
//            for (int i = 0; i < expected.length; i++) {
//                oto.waitText("add -N");
//                oto.waitText(expected[i]);
//            }
        TestKit.waitText(mh);
        //files have been committed,
        //verify explorer node
        
        nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        nodeTest.expand();
        new EventTool().waitNoEvent(5000);
        
        for (int i = 0; i < expected.length; i++) {
            nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
            nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
            stream.print(expected[i] + ": " + nodeIDE.getHtmlDisplayName());
            assertNull("Wrong status or color of node!!!", nodeIDE.getHtmlDisplayName());
        }
        //verify versioning view
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
        //} catch (Exception e) {
        //  throw new Exception("Test failed: " + e);
        //} finally {
        TestKit.closeProject(PROJECT_NAME);
        // }
    }
}
