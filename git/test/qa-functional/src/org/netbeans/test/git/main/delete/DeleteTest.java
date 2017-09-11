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
package org.netbeans.test.git.main.delete;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.CommitOperator;
import org.netbeans.test.git.operators.RevertModificationsOperator;
import org.netbeans.test.git.operators.VersioningOperator;
import org.netbeans.test.git.utils.MessageHandler;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar
 */
public class DeleteTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    static Logger log;

    /**
     * Creates a new instance of DeleteUpdateTest
     */
    public DeleteTest(String name) {
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
                NbModuleSuite.createConfiguration(DeleteTest.class).addTest(
                        "testDeleteRevert",
                        "testDeleteCommit"
                ).enableModules(".*").clusters(".*"));
    }

    public void testDeleteRevert() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);
            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            MessageHandler mh = new MessageHandler("Refreshing");
            log.addHandler(mh);

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");

            //node.performPopupAction("Git|Show Changes");
            VersioningOperator vo = VersioningOperator.invoke();
            //TestKit.waitText(mh);

            node.performPopupActionNoBlock("Delete");
            new EventTool().waitNoEvent(1000);
            NbDialogOperator dialog = new NbDialogOperator("Delete");
            dialog.ok();

            Thread.sleep(1000);
            //new EventTool().waitNoEvent(5000);
            //VersioningOperator vo = VersioningOperator.invoke();
            JTableOperator table;
            Exception e = null;
            Thread.sleep(1000);
            try {
                table = vo.tabFiles();
                assertEquals("Files should have been [Deleted/-]", "Deleted/-", table.getValueAt(0, 1).toString());
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Unexpected behavior - file should appear in Versioning view!!!", e);

            e = null;
            Thread.sleep(1000);
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("TimeoutExpiredException should have been thrown. Deleted file can't be visible!!!", e);

            //revert local changes
            mh = new MessageHandler("Reverting");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node = new SourcePackagesNode(PROJECT_NAME);
            RevertModificationsOperator rmo = RevertModificationsOperator.invoke(node);
            //rmo.rbLocalChanges().push();
            rmo.revert();
            //TestKit.waitText(mh);

            e = null;
            Thread.sleep(1000);
            try {
                vo = VersioningOperator.invoke();
                table = vo.tabFiles();
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Unexpected behavior - file should disappear in Versioning view!!!", e);

            e = null;
            Thread.sleep(1000);
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Reverted file should be visible!!!", e);

            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }

    public void testDeleteCommit() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);
            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            MessageHandler mh = new MessageHandler("Refreshing");
            log.addHandler(mh);

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");

            VersioningOperator vo = VersioningOperator.invoke();

            //TestKit.waitText(mh);
            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Delete");
            dialog.ok();

            Thread.sleep(1000);
            //vo = VersioningOperator.invoke();
            JTableOperator table;
            Exception e = null;
            Thread.sleep(1000);
            try {
                table = vo.tabFiles();
                assertEquals("Files should have been [Deleted/-]", "Deleted/-", table.getValueAt(0, 1).toString());
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Unexpected behavior - file should appear in Versioning view!!!", e);

            e = null;
            Thread.sleep(1000);
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("TimeoutExpiredException should have been thrown. Deleted file can't be visible!!!", e);

            //commit deleted file
            mh = new MessageHandler("Committing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node = new SourcePackagesNode(PROJECT_NAME);
            CommitOperator cmo = CommitOperator.invoke(node);
            new EventTool().waitNoEvent(2000);
            assertEquals("There should be \"Main.java\" file in Commit dialog!!!", "Main.java", cmo.tabFiles().getValueAt(0, 1));
            cmo.commit();

            //TestKit.waitText(mh);
            e = null;
            Thread.sleep(1000);
            try {
                vo = VersioningOperator.invoke();
                new EventTool().waitNoEvent(2000);
                vo.refresh();
                new EventTool().waitNoEvent(2000);
                table = vo.tabFiles();
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Unexpected behavior - file should disappear in Versioning view!!!", e);

            e = null;
            Thread.sleep(1000);
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Deleteted file should be visible!!!", e);

            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
