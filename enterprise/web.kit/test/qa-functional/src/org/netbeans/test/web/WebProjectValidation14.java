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
package org.netbeans.test.web;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Test web project J2EE 1.4.
 */
public class WebProjectValidation14 extends WebProjectValidation {

    public static final String[] TESTS = new String[]{
        "testOpenWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testCleanAndBuildProject", "testRedeployProject", "testRunProject",
        "testRunServlet", "testCreateTLD", "testCreateTagHandler", "testRunTag",
        "testNewHTML", "testRunHTML", "testNewSegment", "testNewDocument",
        "testFinish"
    };

    /** Need to be defined because of JUnit */
    public WebProjectValidation14(String name) {
        super(name);
        PROJECT_NAME = "WebApplication1.4"; // NOI18N
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, WebProjectValidation14.class, TESTS);
    }

    @Override
    protected String getEEVersion() {
        return J2EE_4;
    }
    
    public void testOpenWebProject() throws Exception {
        File projectDir = new File(getDataDir(), PROJECT_NAME);
        openProjects(projectDir.getAbsolutePath());
        waitScanFinished();
        // not display browser on run
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).properties();
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title"));
        new Node(new JTreeOperator(propertiesDialogOper),
                Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run")).select();
        new JCheckBoxOperator(propertiesDialogOper,
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle",
                        "LBL_CustomizeRun_DisplayBrowser_JCheckBox")).setSelected(false);
        propertiesDialogOper.ok();
    }
}
