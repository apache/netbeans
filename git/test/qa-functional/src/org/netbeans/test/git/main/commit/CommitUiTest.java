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
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.CommitOperator;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar
 */
public class CommitUiTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;

    String os_name;

    /**
     * Creates a new instance of CommitUITest
     */
    public CommitUiTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        System.out.println("### " + getName() + " ###");

    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CommitUiTest.class).addTest(
                        "testInvokeCloseCommit"
                ).enableModules(".*").clusters(".*"));
    }

    public void testInvokeCloseCommit() throws Exception {
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");

        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }

            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);
            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }
            TestKit.createNewElements(PROJECT_NAME, "xx", "NewClass");
            new EventTool().waitNoEvent(1000);
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass2");
            new EventTool().waitNoEvent(1000);
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass3");
            new EventTool().waitNoEvent(1000);
            Node packNode = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            CommitOperator co = CommitOperator.invoke(packNode);
            new EventTool().waitNoEvent(2000);

            co.selectCommitAction("NewClass.java", "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction("NewClass.java", "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction("NewClass.java", "Exclude from Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction(2, "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction(2, "Commit");
            new EventTool().waitNoEvent(500);
            co.selectCommitAction(2, "Exclude from Commit");
            new EventTool().waitNoEvent(500);

            JTableOperator table = co.tabFiles();
            TableModel model = table.getModel();
            String[] expected = {"NewClass.java", "NewClass2.java", "NewClass3.java"};
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < model.getRowCount(); i++) {
                actual[i] = model.getValueAt(i, 1).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Commit table doesn't contain all files!!!", expected.length, result);

            co.verify();
            co.cancel();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
