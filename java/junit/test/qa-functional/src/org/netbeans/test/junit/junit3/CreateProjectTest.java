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

package org.netbeans.test.junit.junit3;

import org.netbeans.jellytools.modules.junit.testcases.ExtJellyTestCaseForJunit3;
import java.util.ArrayList;
import javax.swing.tree.TreePath;
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
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.ide.ProjectSupport;

/**
 * Class for testing JUnit 3 based tests.
 * (Modified automated JUnit 4 tests by Jiri Vagner)
 *
 * @author Pavel Pribyl
 */
public class CreateProjectTest extends ExtJellyTestCaseForJunit3 {

    public CreateProjectTest(String testName) {
        super(testName);
    }

    /*
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateProjectTest.class).addTest(
           "testCreateJUnit3Project",
            "testAddLibrary",
            "testGeneratedProjectSuiteFile",
            "testGeneratedMainTestFile",
            "testCreateTestWithoutInitializerAndFinalizer",
            "testGeneratedMainTestFile2",
            "testDeteleJUnit3Project").enableModules(".*").clusters(".*"));
    }
    */

    public void dummyTest() {

    }

    public void testCreateJUnit3Project() {
        new Action("File|New Project", null).perform();
        NewProjectWizardOperator newOp = new NewProjectWizardOperator();
        newOp.selectCategory("Java");
        newOp.selectProject("Java Application");
        newOp.next();
        new JTextFieldOperator(newOp, 0).typeText(TEST_PROJECT_NAME);
        newOp.finish();

        new EventTool().waitNoEvent(5000);

        // select source packages node
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(TEST_PROJECT_NAME);
        prn.select();
        Node node = new Node(prn, "Source Packages"); // NOI18N
        node.setComparator(new Operator.DefaultStringComparator(true, false));
        node.select();

        // create test
        new ActionNoBlock(null,"Tools|Create JUnit Test").perform(node);

        // select junit version
        NbDialogOperator versionOp = new NbDialogOperator("Select jUnit Version");
        new JRadioButtonOperator(versionOp, 0).setSelected(true);
        new JButtonOperator(versionOp,"Select").clickMouse();

        NbDialogOperator newTestOp = new NbDialogOperator("Create Tests");
        new JButtonOperator(newTestOp, "OK").clickMouse();

        new Action("Window|Close All Documents", null).perform();
    }

    public void testAddLibrary() {
        // useless method while executing this test internally
        // but there is a missing library while executing this test on external IDE
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        Node libNode = new Node(prn, "Test Libraries");
        new ActionNoBlock(null,"Add Library").perform(libNode);
        NbDialogOperator libDialog = new NbDialogOperator("Add Library");
        JTreeOperator treeOp = new JTreeOperator(libDialog);
        TreePath tp = treeOp.findPath("Global Libraries|Junit");
        treeOp.selectPath(tp);
        new JButtonOperator(libDialog, "Add Library").push();
    }


    public void testGeneratedProjectSuiteFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing Junit3testprojectSuite.java
        openAc.perform(new Node(prn, "Test Packages|junit3testproject|Junit3testprojectSuite.java"));

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import junit.framework.");

        lines.add("public static Test suite()");
        lines.add("protected void setUp() throws Exception");
        lines.add("protected void tearDown() throws Exception");
        lines.add("@Override");

        findInCode(lines,new EditorOperator("Junit3testprojectSuite.java"));
    }

    public void testGeneratedMainTestFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing MainTest.java
        openAc.perform(new Node(prn, "Test Packages|junit3testproject|MainTest.java"));

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import junit.framework.");
        lines.add("protected void setUp() throws Exception");
        lines.add("protected void tearDown() throws Exception");
        lines.add("Main.main(args);");
        lines.add("fail(\"The test case is a prototype.\");");

        findInCode(lines,new EditorOperator("MainTest.java"));
    }

    public void testCreateTestWithoutInitializerAndFinalizer() {
        // select source packages node
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(TEST_PROJECT_NAME);
        prn.select();

        //deletes previous test package
        Node testPkgNode = new Node(prn, "Test Packages|junit3testproject");
        testPkgNode.select();
        new ActionNoBlock(null,"Delete").perform(testPkgNode);
        NbDialogOperator deleteOp = new NbDialogOperator("Delete");
        new JButtonOperator(deleteOp, "OK").clickMouse();

        // create test
        Node node = new Node(prn, "Source Packages|junit3testproject|Main.java"); // NOI18N
        node.select();
        new ActionNoBlock(null,"Tools|Create JUnit Test").perform(node);

        NbDialogOperator newTestOp = new NbDialogOperator("Create Tests");
        checkAllCheckboxes(newTestOp);
        new JCheckBoxOperator(newTestOp, 3).setSelected(false);
        new JCheckBoxOperator(newTestOp, 4).setSelected(false);

        new JButtonOperator(newTestOp, "OK").clickMouse();

    }

    /**
     * Tests generated test file without Initializer and Finalizer
     */
    public void testGeneratedMainTestFile2() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing MainTest.java
        openAc.perform(new Node(prn, "Test Packages|junit3testproject|MainTest.java"));

        ArrayList<String> linesYes = new ArrayList<String>();
        linesYes.add("import junit.framework.");
        linesYes.add("Main.main(args);");
        linesYes.add("fail(\"The test case is a prototype.\");");
        findInCode(linesYes,new EditorOperator("MainTest.java"));

        missInCode("setUp() throws Exception",new EditorOperator("MainTest.java"));
        missInCode("tearDown() throws Exception",new EditorOperator("MainTest.java"));
    }

    public void testDeteleJUnit3Project() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        new ActionNoBlock(null,"Delete").perform(prn);

        //confirm Delete
        NbDialogOperator opDelConfirm = new NbDialogOperator("Delete Project");
        new JCheckBoxOperator(opDelConfirm, 0).setSelected(true); //also sources
        new JButtonOperator(opDelConfirm,"Yes").clickMouse();
        waitAMoment();
        new Action("Window|Close All Documents", null).perform();

    }
}
