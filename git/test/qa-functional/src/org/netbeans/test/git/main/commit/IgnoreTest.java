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
package org.netbeans.test.git.main.commit;

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
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.test.git.operators.VersioningOperator;
import org.netbeans.test.git.utils.MessageHandler;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar
 */
public class IgnoreTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
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
                        "testIgnoreUnignoreFile"
                ).enableModules(".*").clusters(".*"));
    }

    public void testIgnoreUnignoreFile() throws Exception {
        NbDialogOperator ndo = null;
        try {
            MessageHandler mh = new MessageHandler("Ignoring");

            log.addHandler(mh);
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }

            TestKit.showStatusLabels();
            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);
            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));

            TestKit.createNewPackage(PROJECT_NAME, "javaappignunign");
            new EventTool().waitNoEvent(1000);
            TestKit.createNewElement(PROJECT_NAME, "javaappignunign", "NewClassIgnUnign");
            new EventTool().waitNoEvent(1000);

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            node.performPopupAction("Git|Show Changes");
            new EventTool().waitNoEvent(8000);

            node.performPopupAction("Git|Ignore|Ignore");
            new EventTool().waitNoEvent(3000);
            //TestKit.waitText(mh);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();

            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be ignored!!!", TestKit.IGNORED_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be ignored!!!", TestKit.IGNORED_STATUS, status);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();

            //unignore file
            mh = new MessageHandler("Ignoring");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            node.performPopupAction("Git|Ignore|Unignore");
            //TestKit.waitText(mh);
            new EventTool().waitNoEvent(3000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            new EventTool().waitNoEvent(5000);
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.NEW_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.NEW_STATUS, status);

            //verify content of Versioning view
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");

            mh = new MessageHandler("Refreshing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node.select();
            node.performPopupAction("Git|Show Changes");
            //TestKit.waitText(mh);
            new EventTool().waitNoEvent(3000);
            VersioningOperator vo = VersioningOperator.invoke();
            TableModel model = vo.tabFiles().getModel();
            assertEquals("Versioning view should be empty", 1, model.getRowCount());
            assertEquals("File should be listed in Versioning view", "NewClassIgnUnign.java", model.getValueAt(0, 0).toString());

            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
