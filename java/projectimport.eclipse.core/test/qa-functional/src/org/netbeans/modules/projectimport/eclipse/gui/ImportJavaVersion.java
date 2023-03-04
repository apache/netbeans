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

package org.netbeans.modules.projectimport.eclipse.gui;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 *
 * @author Maks
 */
public class ImportJavaVersion extends ProjectImporterTestCase {
    WizardOperator importWizard;
  
    public ImportJavaVersion(String testName) {
        super(testName);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ExtractToWorkDir(getDataDir(), "testdata.jar");
    }
    public void testImportJavaVersion13() {
        String projectName = "JavaVersion13";
        importProject(projectName);
        validateProjectJavaVersionProperties(projectName, "1.3");
    }
    public void testImportJavaVersion14() {
        String projectName = "JavaVersion14";
        importProject(projectName);
        validateProjectJavaVersionProperties(projectName, "1.4");
    }
    public void testImportJavaVersion15() {
        String projectName = "JavaVersion15";
        importProject(projectName);
        validateProjectJavaVersionProperties(projectName, "1.5");
    }
    public void testImportJavaVersion16() {
        String projectName = "JavaVersion16";
        importProject(projectName);
        validateProjectJavaVersionProperties(projectName, "1.6");
    }
    private void validateProjectJavaVersionProperties(String projectName, String expectedVersion) {
        NbDialogOperator propsDialog = invokeProjectPropertiesDialog(projectName,"Sources");        

        JComboBoxOperator versionCombo = new JComboBoxOperator(propsDialog, 0);
        if(!versionCombo.getSelectedItem().toString().endsWith(expectedVersion)) {
            fail("No expected java version set");
        }
        propsDialog.close();
    }    

    private void importProject(String projectName) {
        importWizard = invokeImporterWizard();
        selectProjectFromWS(importWizard,"testdata", projectName);
        importWizard.finish();

        waitForProjectsImporting();

        try {
            NbDialogOperator issuesWindow = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.Bundle", "MSG_ImportIssues"));
            issuesWindow.close();
        } catch (Exception e) {
            // ignore 
        }        
    }
}
