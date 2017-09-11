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
