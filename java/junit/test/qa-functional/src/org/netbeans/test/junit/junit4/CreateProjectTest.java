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
