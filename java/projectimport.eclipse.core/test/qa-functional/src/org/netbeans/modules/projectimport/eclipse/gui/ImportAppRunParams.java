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
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author Maks
 */
public class ImportAppRunParams extends ProjectImporterTestCase {
    WizardOperator importWizard;
  
    public ImportAppRunParams(String testName) {
        super(testName);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ExtractToWorkDir(getDataDir(), "testdata.jar");
    }
    public void testImportJavaVMParams() {
        String projectName = "JavaRunParams";
        importProject(projectName);
        validateProjectJavaVMParams(projectName);
    }

    public void testImportMultipleJavaVMParams() {
        String projectName = "MultipleRunConfigs";
        importProject(projectName);
        validateRunConfigs(projectName);
    }
    private void validateRunConfigs(String projectName) {
        NbDialogOperator propsDialog = invokeProjectPropertiesDialog(projectName,"Run");
        JComboBoxOperator configs =new JComboBoxOperator(propsDialog,0);
        int listLength = configs.getModel().getSize();
        log("Found "+listLength+" items");
        if(listLength != 3) {
            fail("Some Run configurations not imported");
        }
        configs.selectItem("Run Configuration One");
        configs.selectItem("Run Configuration One");
        propsDialog.close();
    }
    
    private void validateProjectJavaVMParams(String projectName) {
        NbDialogOperator propsDialog = invokeProjectPropertiesDialog(projectName,"Run");
        
        JTextFieldOperator runClass = new JTextFieldOperator(propsDialog, 0);
        if(!runClass.getText().toString().equals("c.s.t2.SecondRunClass")) {
            fail("No expected main class set");
        }
        JTextFieldOperator runParams = new JTextFieldOperator(propsDialog, 1);
        if(!runParams.getText().toString().equals("param1 param2 param3")) {
            fail("No run parameters passed");
        }
        JTextFieldOperator JVMParams = new JTextFieldOperator(propsDialog, 3);
        if(!JVMParams.getText().toString().equals("-Xms25m")) {
            fail("No JVM parameters passed");
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
