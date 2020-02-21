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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
