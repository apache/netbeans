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

package org.netbeans.test.junit.junit4;

import org.netbeans.jellytools.modules.junit.testcases.ExtJellyTestCaseForJunit4;
import java.io.File;
import java.util.ArrayList;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.test.junit.utils.Utilities;

public class CreateProjectTest extends ExtJellyTestCaseForJunit4 {

    public static String[] testNames = new String[]{
        "testCreateJUnit4Project",
        "testAddLibrary",
        "testGeneratedProjectSuiteFile",
        "testGeneratedMainTestFile"
    };

    public CreateProjectTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return createModuleTest(CreateProjectTest.class, testNames);
    }
        
    public void testCreateJUnit4Project() {
        
        Utilities.deleteDirectory(new File(Utilities.pathToProjects() + File.separator + TEST_PROJECT_NAME));
        
        // create anagram project
        new Action("File|New Project", null).perform();
        NewProjectWizardOperator newOp = new NewProjectWizardOperator();
        newOp.selectCategory("Java");
        newOp.selectProject("Java Application");
        newOp.next();
        new JTextFieldOperator(newOp, 0).typeText(TEST_PROJECT_NAME);
        new JTextFieldOperator(newOp, 1).setText(Utilities.pathToProjects());
        newOp.finish();
        new EventTool().waitNoEvent(5000);

        // select source packages node
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(TEST_PROJECT_NAME);
        prn.select();
        Node node = new Node(prn, "Source Packages"); // NOI18N
        node.setComparator(new Operator.DefaultStringComparator(true, false));
        node.select();
        node.expand();

        // create test
        new ActionNoBlock("Tools|Create/Update Tests", null).performMenu();
        try { new NbDialogOperator("Create Tests"); }
        catch (org.netbeans.junit.AssertionFailedErrorException e) {
            System.out.println("Another try...");
            node.select();
            new ActionNoBlock("Tools|Create/Update Tests", null).performMenu();
        }
        NbDialogOperator newTestOp = new NbDialogOperator("Create Tests");
        new JButtonOperator(newTestOp, "OK").clickMouse();
        
        new Action("Window|Close All Documents", null).perform();
    }

    public void testAddLibrary() {
        // useless method while executing this test internally
        // but there is a missing libraty while executing this test on external IDE
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        Node libNode = new Node(prn, "Test Libraries");
        new ActionNoBlock(null,"Add Library").perform(libNode);
        NbDialogOperator libDialog = new NbDialogOperator("Add Library");
        JTreeOperator treeOp = new JTreeOperator(libDialog);
        TreePath tp = treeOp.findPath("Global Libraries|Junit");
        treeOp.selectPath(tp);
        new JButtonOperator(libDialog, "Add Library").push();
    }
    
    public void testGeneratedRootSuiteFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing RootSuite.java
        openAc.perform(new Node(prn, "Test Packages|<default package>|RootSuite.java"));
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import org.junit.");
        lines.add("@RunWith(Suite.class)");
        lines.add("@Suite.SuiteClasses({junit4testproject.Junit4testprojectSuite.class})");
        lines.add("public static void tearDownClass() throws Exception");
        lines.add("@Before");
        
        findInCode(lines,new EditorOperator("RootSuite.java"));
    }

    public void testGeneratedProjectSuiteFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing Junit4testprojectSuite.java
        openAc.perform(new Node(prn, "Test Packages|junit4testproject|Junit4testprojectSuite.java"));
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import org.junit.");
        lines.add("@Suite.SuiteClasses({junit4testproject.JUnit4TestProjectTest.class})");
        lines.add("@RunWith(Suite.class)");
        lines.add("public static void tearDownClass() throws Exception");
        lines.add("@Before");
        
        findInCode(lines,new EditorOperator("Junit4testprojectSuite.java"));
    }

    public void testGeneratedMainTestFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing JUnit4TestProjectTest.java
        openAc.perform(new Node(prn, "Test Packages|junit4testproject|JUnit4TestProjectTest.java"));
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import org.junit.");        
        lines.add("import static org.junit.Assert.*;");
        lines.add("@Test");
        lines.add("public void testMain() {");
        lines.add("@BeforeClass");
        lines.add("@AfterClass");
        
        findInCode(lines,new EditorOperator("JUnit4TestProjectTest.java"));
    }
}
