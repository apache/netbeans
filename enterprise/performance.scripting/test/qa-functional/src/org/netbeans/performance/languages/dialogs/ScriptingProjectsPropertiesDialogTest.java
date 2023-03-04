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
package org.netbeans.performance.languages.dialogs;

import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.performance.languages.Projects;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingProjectsPropertiesDialogTest extends PerformanceTestCase {

    private Node testNode;
    private String TITLE, projectName;

    public ScriptingProjectsPropertiesDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    public ScriptingProjectsPropertiesDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(ScriptingProjectsPropertiesDialogTest.class).suite();
    }

    @Override
    public void initialize() {
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.php.project.ui.customizer.Bundle", "LBL_Customizer_Title", new String[]{projectName});
        testNode = (Node) new ProjectsTabOperator().getProjectRootNode(projectName);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        new PropertiesAction().performPopup(testNode);
        return new NbDialogOperator(TITLE);
    }

    public void testPhpProjectProperties() {
        projectName = Projects.PHP_PROJECT;
        doMeasurement();
    }

    public void testScriptingProjectProperties() {
        projectName = Projects.SCRIPTING_PROJECT;
        doMeasurement();
    }
}
