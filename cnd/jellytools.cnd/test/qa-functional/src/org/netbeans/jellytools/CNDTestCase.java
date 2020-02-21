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

import java.io.IOException;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 */
public class CNDTestCase extends JellyTestCase {

    private static final String projectName = "NewTestProject";

    public CNDTestCase(String testName) {
        super(testName);
    }


    /**
     * Opens the test project according to the platform the test runs on.
     *
     * @throws java.io.IOException
     */
    public void createAndOpenTestProject() throws IOException
    {
        NewProjectWizardOperator pop = NewProjectWizardOperator.invoke();
        // Standard
        pop.selectCategory(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "Templates/Project/Native"));
        // C/C++ Application
        pop.selectProject(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle", "Templates/Project/Native/newApplication.xml"));
        pop.next();
        NewCNDProjectNameLocationStepOperator stpop = new NewCNDProjectNameLocationStepOperator();
        stpop.txtProjectName().setText(projectName);   // NOI18N
        stpop.btBrowseProjectLocation().pushNoBlock();
        //Select Project Location
        String selectProjectLocation = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.txtProjectLocation().setText(getWorkDir().getAbsolutePath()); //NOI18N
        stpop.txtProjectFolder().getText();
        stpop.cbCreateMainFile().setSelected(true);
        stpop.cbSetAsMainProject().setSelected(true);
        stpop.btFinish().pushNoBlock();
    }

    /**
     * Sets the toolchain used for the test project - currenty either GNU or SunStudio
     *
     * @param toolchainName
     */
    public void setToolchain(String toolchainName)
    {
        //Open projects tab
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode projectNode = pto.getProjectRootNode(projectName);
        //invoke properties on the test project node
        projectNode.properties();

        String propertiesDlgTitle = Bundle.getString("org.netbeans.modules.cnd.makeproject.api.Bundle", "LBL_Project_Customizer_Title", new Object[] {projectName});
        NbDialogOperator propertiesDlg = new NbDialogOperator(propertiesDlgTitle);

        JTreeOperator treeCategories = new JTreeOperator(propertiesDlg);
        (new Node(treeCategories, 
                Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.customizer.Bundle",
                "LBL_Config_Build"))).select();
        String toolCollectionTitle = Bundle.getString("org.netbeans.modules.cnd.makeproject.api.configurations.Bundle", "CompilerCollectionTxt");
        //select "Build" from the "Categories" tree

        Property propToolChain = new Property(new PropertySheetOperator(propertiesDlg), toolCollectionTitle);
        //set property "Tool Collection" to "GNU"
        propToolChain.setValue(toolchainName);
        propertiesDlg.ok();
    }

    /**
     * Returns the project name according to the platform the test runs on.
     *
     * @return String project name
     */
    public String getTestProjectName()
    {
        return projectName;
    }

}
