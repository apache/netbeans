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
package org.netbeans.test.subversion.main.branches;

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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.SourcePackagesNode;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CopyToOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SwitchOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter pis
 */
public class CopyTest extends JellyTestCase {

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
     * Creates a new instance of CopyTest
     */
    public CopyTest(String name) {
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
                NbModuleSuite.createConfiguration(CopyTest.class).addTest(
                        "testCreateNewCopySwitch",
                        "testCreateNewCopy").enableModules(".*").clusters(".*"));
    }

    public void testCreateNewCopySwitch() throws Exception {

        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);

        TestKit.closeProject(PROJECT_NAME);
        if (TestKit.getOsName().indexOf("Mac") > -1) {
            NewProjectWizardOperator.invoke().close();
        }

        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();

        //create repository...
        File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
        new File(TMP_PATH).mkdirs();
        work.mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
        new EventTool().waitNoEvent(2000);
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

        new EventTool().waitNoEvent(2000);
        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        new EventTool().checkNoEvent(3000);
        wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
        wdso.setLocalFolder(work.getCanonicalPath());
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();

        TestKit.waitText(mh);

        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        TestKit.waitForScanFinishedSimple();

        mh = new MessageHandler("Copying");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
        CopyToOperator cto = CopyToOperator.invoke(projNode);
        cto.setRepositoryFolder("branches/release01/" + PROJECT_NAME);
        cto.setCopyPurpose("New branch for project.");
        cto.checkSwitchToCopy(true);
        cto.copy();

        new EventTool().waitNoEvent(2000);
        TestKit.waitText(mh);
        //new EventTool().waitNoEvent(2000);

        new EventTool().checkNoEvent(3000);
        TestKit.showStatusLabels();
        new EventTool().checkNoEvent(3000);
        Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
        org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        new EventTool().waitNoEvent(1000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        String HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        String status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", "[ release01]", status);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        new EventTool().waitNoEvent(1000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", "[ release01]", status);
        stream.flush();
        stream.close();

        TestKit.closeProject(PROJECT_NAME);

    }

    public void testCreateNewCopy() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);

        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);
        TestKit.closeProject(PROJECT_NAME);
        new EventTool().checkNoEvent(3000);
        TestKit.showStatusLabels();

        stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();

        //create repository...
        File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
        new File(TMP_PATH).mkdirs();
        work.mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        //RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        new EventTool().checkNoEvent(2000);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        new EventTool().checkNoEvent(2000);
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        new EventTool().checkNoEvent(2000);
        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        new EventTool().checkNoEvent(2000);
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

        mh = new MessageHandler("Copying");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
        CopyToOperator cto = CopyToOperator.invoke(projNode);
        cto.setRepositoryFolder("branches/release01");
        cto.setCopyPurpose("New branch for project.");
        cto.checkSwitchToCopy(false);
        cto.copy();
        TestKit.waitText(mh);
        Thread.sleep(1000);

        Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
        org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(1000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        String HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        String status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(1000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);
        //to do

        //switch to branch
        mh = new MessageHandler("Switching");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
        SwitchOperator so = SwitchOperator.invoke(projNode);
        so.setRepositoryFolder("branches/release01/" + PROJECT_NAME);
        so.switchBt();

        new EventTool().waitNoEvent(2000);
        TestKit.waitText(mh);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(10000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", "[ release01]", status);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(1000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", "[ release01]", status);

        mh = new MessageHandler("Switching");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
        so = SwitchOperator.invoke(projNode);
        so.setRepositoryFolder("trunk/" + PROJECT_NAME);
        so.switchBt();

        TestKit.waitText(mh);

        Thread.sleep(2000);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        new EventTool().waitNoEvent(1000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);

        nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        new EventTool().waitNoEvent(1000);
        //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        HtmlName = nodeIDE.getHtmlDisplayName();
        new EventTool().waitNoEvent(1000);
        status = TestKit.getStatus(HtmlName);
        assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);

        stream.flush();
        stream.close();

        TestKit.closeProject(PROJECT_NAME);

    }
}
