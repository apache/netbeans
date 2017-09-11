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
package org.netbeans.test.git.main.commit;

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
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.CommitOperator;
import org.netbeans.test.git.operators.VersioningOperator;
import org.netbeans.test.git.utils.MessageHandler;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar
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
                NbModuleSuite.createConfiguration(InitializeTest.class).addTest(
                        "testInitializeAndFirstCommit"
                ).enableModules(".*").clusters(".*"));
    }

    public void testInitializeAndFirstCommit() throws Exception {
        try {
            long start;
            long end;
            JTableOperator table;
            Node nodeFile;
            NbDialogOperator ndo;
            TestKit.showStatusLabels();
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }

            f = TestKit.prepareProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(1000);
            String s = f.getAbsolutePath() + File.separator + TestKit.PROJECT_NAME;
            new EventTool().waitNoEvent(1000);
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            new EventTool().waitNoEvent(1000);

            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            MessageHandler mh;

            nodeFile = new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME);
            nodeFile.performPopupActionNoBlock("Versioning|Initialize Git Repository");

            ndo = new NbDialogOperator("Initialize a Git Repository");
            ndo.ok();

            new EventTool().waitNoEvent(1000);

            TestKit.removeHandlers(log);

//            TestKit.waitText(mh);
            new EventTool().waitNoEvent(1000);
            VersioningOperator vo = VersioningOperator.invoke();
            table = vo.tabFiles();
            new EventTool().waitNoEvent(3000);

            assertEquals("Wrong row count of table.", 7, table.getRowCount());
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

            //TestKit.waitText(mh);
            new EventTool().waitNoEvent(2000);
            vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(2000);
            vo.refresh();
            new EventTool().waitNoEvent(2000);
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
    }
}
