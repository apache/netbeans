/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import junit.textui.TestRunner;

/**
 * Test of org.netbeans.jellytools.NameCNDProjectNameLocationStepOperator.
 */
public class NewCNDProjectNameLocationStepOperatorTest extends JellyTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static final String[] tests = new String[] {
        "testCNDApplicationPanel", "testCNDStaticLibraryPanel",
                "testCNDDynamicLibraryPanel"
                
    };
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {        
        return createModuleTest(NewCNDProjectNameLocationStepOperatorTest.class,
                tests);
    }
    
    @Override
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        standardLabel = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "Templates/Project/Native");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewCNDProjectNameLocationStepOperatorTest(String testName) {
        super(testName);
    }
    
    // Standard
    private static String standardLabel;
    
    /** Test components on CND Application panel */
    public void testCNDApplicationPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        // Standard
        op.selectCategory(standardLabel);
        // C/C++ Application
        op.selectProject(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle", "Templates/Project/Native/newApplication.xml"));
        op.next();
        NewCNDProjectNameLocationStepOperator stpop = new NewCNDProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("NewProject");   // NOI18N
        stpop.btBrowseProjectLocation().pushNoBlock();
        //Select Project Location
        String selectProjectLocation = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectFolder().getText();
        stpop.cbCreateMainFile().setSelected(false);
        stpop.cbSetAsMainProject().setSelected(false);

        stpop.cancel();
    }
    
    
    /** Test components on C/C++ Static Library */
    public void testCNDStaticLibraryPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // C/C++ Static Library
        op.selectProject(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "Templates/Project/Native/newStaticLibrary.xml"));
        op.next();
        NewCNDProjectNameLocationStepOperator stpop = new NewCNDProjectNameLocationStepOperator();
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectName().setText("NewLibraryProject");
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        //Select Project Location
        String selectProjectLocation = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N        
        stpop.cancel();
    }

    /** Test components on C/C++ Dynamic Library */
    public void testCNDDynamicLibraryPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // C/C++ Static Library
        op.selectProject(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "Templates/Project/Native/newDynamicLibrary.xml"));
        op.next();
        NewCNDProjectNameLocationStepOperator stpop = new NewCNDProjectNameLocationStepOperator();
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectName().setText("NewLibraryProject");
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        //Select Project Location
        String selectProjectLocation = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.cancel();
    }

}
