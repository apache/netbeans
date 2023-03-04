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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ImportSourceFilters extends ProjectImporterTestCase {
    WizardOperator importWizard; 
    static final String projectName = "ExcludesIncludesProject"; 
    public ImportSourceFilters(String testName) {
        super(testName);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ExtractToWorkDir(getDataDir(), "testdata.jar");
    }

    public void testImportSourceFilters() {
        importProject(projectName);
        validate();
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

    private void validate() {
        NbDialogOperator propsDialog = invokeProjectPropertiesDialog(projectName,"Sources");
        
        String btnCaption = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "CustomizerSources.includeExcludeButton");
        JButtonOperator btn = new JButtonOperator(propsDialog,btnCaption);
        btn.pushNoBlock();
        String customizerCaption = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "CustomizerSources.title.includeExclude");

        NbDialogOperator customizer = new NbDialogOperator(customizerCaption);
        
        JTextFieldOperator includesBox = new JTextFieldOperator(customizer,1);
        JTextFieldOperator excludesBox = new JTextFieldOperator(customizer,0);

        log(includesBox.getText());
        log(excludesBox.getText());
        
        if(!includesBox.getText().contains("IncludeOne*.java")) {
            fail("Includes doesn't contain expected "+"IncludeOne*.java"+" mask");
        }
        if(!includesBox.getText().contains("IncludeTwo*.java")) {
            fail("Includes doesn't contain expected "+"IncludeTwo*.java"+" mask");
        }
        if(!includesBox.getText().contains("IncludeThree*.java")) {
            fail("Includes doesn't contain expected "+"IncludeThree*.java"+" mask");
        } 
        if(!excludesBox.getText().contains("ExcludeThree*.java")) {
            fail("Excludes doesn't contain expected "+"ExcludeThree*.java"+" mask");
        }
        if(!excludesBox.getText().contains("ExcludeTwo*.java")) {
            fail("Excludes doesn't contain expected "+"ExcludeTwo*.java"+" mask");
        }   
        if(!excludesBox.getText().contains("ExcludeOne*.java")) {
            fail("Excludes doesn't contain expected "+"ExcludeOne*.java"+" mask");
        }           
        customizer.close();
        propsDialog.close();
    }
}
