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

package org.netbeans.performance.enterprise.footprint;

import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
//import org.netbeans.performance.enterprise.EPUtilities;


/**
 * Utilities for Memory footprint tests
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class EPFootprintUtilities extends CommonUtilities {
    
    static String creatJ2EEeproject(String category, String project, boolean wait) {
        return createProjectGeneral(category, project, wait, true);
    }
    
    private static String createProjectGeneral(String category, String project, boolean wait, boolean j2eeProject) {
        // select Projects tab
        ProjectsTabOperator.invoke();
        
        // create a project
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();

        NewWebProjectNameLocationStepOperator wizard_location = new NewWebProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(CommonUtilities.getTempDir());
        String pname = wizard_location.txtProjectName().getText();
        
        pname = pname + "_" + System.currentTimeMillis();
        wizard_location.txtProjectName().clearText();
        wizard_location.txtProjectName().typeText(pname);
        
        wizard.next();
        
        if(j2eeProject) {
            new JComboBoxOperator(wizard,1).selectItem(1);
            new JCheckBoxOperator(wizard,"Create Application Client module:").setSelected(true);
        }
        
        new EventTool().waitNoEvent(1000);
        wizard.finish();

        // wait 30 seconds
        waitForProjectCreation(30000, wait);
        
        return pname;
    }
    
    public static void killRunOnProject(String project) {
        killProcessOnProject(project, "run");
    }
    
    public static void killDebugOnProject(String project) {
        killProcessOnProject(project, "debug");
    }
    
    private static void killProcessOnProject(String project, String process) {
        // prepare Runtime tab
        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        
        // kill the execution
        Node node = new Node(runtime.getRootNode(), "Processes|"+project+ " (" + process + ")");
        node.select();
        node.performPopupAction("Terminate Process");
    }
}
