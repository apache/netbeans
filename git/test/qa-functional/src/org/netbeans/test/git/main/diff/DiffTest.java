/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.test.git.main.diff;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.DiffOperator;
import org.netbeans.test.git.operators.SourcePackagesNode;
import org.netbeans.test.git.operators.VersioningOperator;
import org.netbeans.test.git.utils.TestKit;

/**
 *
 * @author kanakmar 
 */
public class DiffTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public PrintStream stream;
    static Logger log;

    /**
     * Creates a new instance of DiffTest
     */
    public DiffTest(String name) {
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
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(DiffTest.class)
                .addTest("testDiffFile")
                .enableModules(".*")
                .clusters(".*")
        );
    }

    public void testDiffFile() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1) {
                NewProjectWizardOperator.invoke().close();
            }
            NbDialogOperator dialog;
            
            TestKit.showStatusLabels();
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.prepareGitProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            new EventTool().waitNoEvent(2000);

            while (IndexingBridge.getInstance().isIndexingInProgress()) {
                Thread.sleep(3000);
            }

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Git|Show Changes");
            new EventTool().waitNoEvent(2000);
            
            //modify file
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Open");
            EditorOperator eo = new EditorOperator("Main.java");
            eo.deleteLine(2);
            eo.insert(" insert", 10, 18);
            eo.insert("\tSystem.out.println(\"\");\n", 19, 1);
            eo.save();
            
            new EventTool().waitNoEvent(2000);
            VersioningOperator vo = VersioningOperator.invoke();
            vo = VersioningOperator.invoke();
            new EventTool().waitNoEvent(8000);
            //Save action should change the file annotations
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.MODIFIED_COLOR, color.toUpperCase());
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.MODIFIED_STATUS, status);
            assertEquals("Wrong number of records in Versioning view!!!", 1, vo.tabFiles().getRowCount());
            
            new EventTool().waitNoEvent(2000);
            node.performPopupAction("Git|Diff|Diff To HEAD");
            new EventTool().waitNoEvent(2000);

            DiffOperator diffOp = new DiffOperator("Main.java");
            //
            try {
                TimeoutExpiredException afee = null;
                diffOp.next();
                diffOp.next();
                try {
                    diffOp.next();
                } catch (TimeoutExpiredException e) {
                    afee = e;
                }
                assertNotNull("TimeoutExpiredException was expected.", afee);

                //verify previous button
                afee = null;
                diffOp.previous();
                diffOp.previous();
                try {
                    diffOp.previous();
                } catch (TimeoutExpiredException e) {
                    afee = e;
                }
                assertNotNull("TimeoutExpiredException was expected.", afee);
            } catch (Exception e) {
                System.out.println("Problem with buttons of differences");
            }

            new EventTool().waitNoEvent(2000);
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }

}
