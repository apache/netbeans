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

/**
 *
 * @author mkhramov@netbeans.org
 */

public class ImportSimpleJavaProjectFromWS extends ProjectImporterTestCase {

    WizardOperator importWizard;
    private static final String prjName = "SimpleJava";
    public ImportSimpleJavaProjectFromWS(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ExtractToWorkDir(getDataDir(), "testdata.jar");
    }

    public void testImportSimpleJavaProject() {        
        importWizard = invokeImporterWizard();

        selectProjectFromWS(importWizard,"testdata", prjName);

        importWizard.finish();
        
        waitForProjectsImporting();

        try {
            NbDialogOperator issuesWindow = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.Bundle", "MSG_ImportIssues"));
            issuesWindow.close();
        } catch (Exception e) {
            // ignore 
        }
   
        validateProjectRootNode(prjName);
        validateProjectTestLibNode(prjName);        
        validateProjectSrcNode(prjName,"src");
        validateProjectTestNode(prjName, "test");

      }

}
