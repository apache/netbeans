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

/**
 * Test of org.netbeans.jellytools.NameLocationStepOperator.
 *
 * @author tb115823
 */
public class NewJavaProjectNameLocationStepOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testJavaApplicationPanel", "testJavaAntProjectPanel",
        "testJavaLibraryPanel", "testJavaWithExistingSourcesPanel"
    };
    // Standard
    private static String standardLabel;

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NewJavaProjectNameLocationStepOperatorTest.class, tests);
    }

    @Override
    protected void setUp() {
        System.out.println("### " + getName() + " ###");
        standardLabel = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                "Templates/Project/Standard");
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewJavaProjectNameLocationStepOperatorTest(String testName) {
        super(testName);
    }

    /** Test components on Java Application panel */
    public void testJavaApplicationPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        // Standard
        op.selectCategory(standardLabel);
        // Java Application
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "template_app"));
        op.next();
        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("NewProject");   // NOI18N
        stpop.btBrowseProjectLocation().pushNoBlock();
        String selectProjectLocation = org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectFolder().getText();
        stpop.cbCreateMainClass().setSelected(false);

        stpop.cancel();
    }

    /** Test components on Java Ant Project panel */
    public void testJavaAntProjectPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // Java Project with Existing Ant Script
        op.selectProject(Bundle.getString("org.netbeans.modules.java.freeform.resources.Bundle", "Templates/Project/Standard/j2sefreeform.xml"));
        op.next();
        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtLocation().setText("/tmp");
        stpop.txtBuildScript().setText("/path/to/antscript");//NOI18N
        stpop.txtProjectName().setText("ant project");//NOI18N
        stpop.txtProjectFolder().setText("/ant/folder");//NOI18N
        stpop.btBrowseLocation().pushNoBlock();

        String browseExistingAntProjectFolder =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_Browse_Location");
        new NbDialogOperator(browseExistingAntProjectFolder).cancel();// I18N
        stpop.btBrowseBuildScript().pushNoBlock();

        String browseExistingAntBuildScript =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_Browse_Build_Script");

        new NbDialogOperator(browseExistingAntBuildScript).cancel();// I18N
        stpop.btBrowseProjectFolder().pushNoBlock();

        String browseNewProjectFolder =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_Browse_Project_Folder");
        new NbDialogOperator(browseNewProjectFolder).cancel();// I18N
        stpop.cancel();
    }

    /** Test component on Java Library */
    public void testJavaLibraryPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // Java Class Library
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "template_library"));
        op.next();
        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectName().setText("NewLibraryProject");
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        String selectProjectLocation =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.cancel();
    }

    public void testJavaWithExistingSourcesPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // "Java Project with Existing Sources"
        op.selectProject(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "template_existing"));
        op.next();

        NewJavaProjectNameLocationStepOperator stpop = new NewJavaProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("MyNewProject");
        stpop.txtProjectFolder().setText("/tmp"); //NOI18N
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        String selectProjectLocation =
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.cancel();
    }
}
