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
package org.netbeans.performance.languages.actions;

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.Bundle;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

/**
 *
 * @author mrkam@netbeans.org
 */
public class SavingPHPProjectPropertiesTest extends PerformanceTestCase {

    public String category, project, projectName, projectType, editorName;
    private Node testNode;
    private JButtonOperator okButton;

    public SavingPHPProjectPropertiesTest(String testName) {
        super(testName);
        expectedTime = 1000;
    }

    public SavingPHPProjectPropertiesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(SavingPHPProjectPropertiesTest.class).suite();
    }

    @Override
    public void initialize() {
        closeAllModal();
        createProject();
        testNode = (Node) new ProjectsTabOperator().getProjectRootNode("PhpPerfTest");
    }

    private void createProject() {

        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();

        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard.finish();
    }

    @Override
    public void prepare() {
        new PropertiesAction().performPopup(testNode);
        NbDialogOperator propertiesDialog = new NbDialogOperator("Project Properties");
        new JCheckBoxOperator(propertiesDialog, Bundle.getStringTrimmed(
                "org.netbeans.modules.php.project.ui.customizer.Bundle",
                "CustomizerSources.shortTagsCheckBox.AccessibleContext.accessibleName"))
                .clickMouse();
        okButton = new JButtonOperator(propertiesDialog,
                Bundle.getStringTrimmed("org.netbeans.modules.project.uiapi.Bundle",
                "LBL_Customizer_Ok_Option"));
    }

    public ComponentOperator open() {
        okButton.push();
        return null;
    }

    public void testSavingPhpProjectProperties() {
        category = "PHP";
        project = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "Templates/Project/PHP/PHPProject.php");
        projectType = "PHPApplication";
        editorName = "index.php";
        doMeasurement();
    }

}
