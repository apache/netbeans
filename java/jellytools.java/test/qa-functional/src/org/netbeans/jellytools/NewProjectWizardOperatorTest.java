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
package org.netbeans.jellytools;

import junit.framework.Test;
import org.netbeans.jellytools.actions.CloseAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;

/**
 * Test of org.netbeans.jellytools.NewProjectWizardOperator.
 * @author tb115823
 */
public class NewProjectWizardOperatorTest extends JellyTestCase {

    public static NewProjectWizardOperator op;
    // "Java Application"
    private static String javaApplicationLabel;
    public static final String[] tests = new String[]{
        "testInvokeTitle",
        "testInvoke",
        "testSelectCategoryAndProject",
        "testVerify",
        "testGetDescription",
        "testCreateTwo",
        "testCreate"
    };

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NewProjectWizardOperatorTest.class,
                tests);
    }

    protected void setUp() {
        System.out.println("### " + getName() + " ###");
        javaApplicationLabel =
                Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                "template_app");
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewProjectWizardOperatorTest(String testName) {
        super(testName);
    }

    /** Test of invoke method with title parameter. Opens new wizard, waits for the dialog and closes it. */
    public void testInvokeTitle() {
        // "New Project"
        String title = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewProjectWizard_Title");
        op = NewProjectWizardOperator.invoke(title);
        op.close();
    }

    /** Test of invoke method. Opens new wizard and waits for the dialog. */
    public void testInvoke() {
        op = NewProjectWizardOperator.invoke();
    }

    /** Test of methods selectCategory and selectProject. */
    public void testSelectCategoryAndProject() {
        // Standard
        String standardLabel = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
        op.selectCategory(standardLabel);
        op.selectProject(javaApplicationLabel);
    }

    /** Test of verify method. */
    public void testVerify() {
        op.verify();
    }

    /** Test of getDescription method. */
    public void testGetDescription() {
        assertTrue("Wrong description.", op.getDescription().indexOf("Java SE application") > 0);
        op.cancel();
    }

    public void testCreateTwo() {
        createJavaProject("MyJavaProjectOne");
        createJavaProject("MyJavaProjectTwo");
        ProjectsTabOperator projects = new ProjectsTabOperator();
        ProjectRootNode one = new ProjectRootNode(projects.tree(), "MyJavaProjectOne");
        ProjectRootNode two = new ProjectRootNode(projects.tree(), "MyJavaProjectTwo");
        new CloseAction().perform(one);
        new CloseAction().perform(two);
    }

    public void testCreate() {
        createJavaProject("MyJavaProject");
    }

    public void createJavaProject(String projectName) {
        //workaround for 142928
        //NewProjectWizardOperator.invoke().cancel();
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java");
        npwo.selectProject("Java Application");
        npwo.next();
        NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(projectName);
        npnlso.txtProjectLocation().setText(getDataDir().getAbsolutePath()); // NOI18N
        npnlso.finish();
        new ProjectsTabOperator().getProjectRootNode(projectName);
    }
}
