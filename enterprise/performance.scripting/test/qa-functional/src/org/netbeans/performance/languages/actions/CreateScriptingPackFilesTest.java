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
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test create Web Pack projects
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateScriptingPackFilesTest extends PerformanceTestCase {

    private String doccategory, doctype, docname, suffix, projectfolder, buildedname;
    private NewJavaFileNameLocationStepOperator location;
    private String project_name = "";
    private Node projectRoot;

    /**
     * Creates a new instance of CreateWebPackFiles
     *
     * @param testName the name of the test
     */
    public CreateScriptingPackFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of CreateWebPackFiles
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateScriptingPackFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(CreateScriptingPackFilesTest.class).suite();
    }

    public void testCreatePHPPage() {
        docname = "PHPPage"; //NOI18N
        doccategory = "PHP"; //NOI18N        
        doctype = "PHP Web Page"; //NOI18N
        suffix = ".php";
        projectfolder = ScriptingUtilities.SOURCE_PACKAGES;
        project_name = Projects.PHP_PROJECT;
        doMeasurement();
    }

    public void testCreatePHPFile() {
        docname = "PHPFile"; //NOI18N
        doccategory = "PHP"; //NOI18N        
        doctype = "PHP File"; //NOI18N
        suffix = ".php";
        projectfolder = ScriptingUtilities.SOURCE_PACKAGES;
        project_name = Projects.PHP_PROJECT;
        doMeasurement();
    }

    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        location.finish();
        return new EditorOperator(buildedname);
    }

    @Override
    public void initialize() {
        closeAllModal();
    }

    public void prepare() {
        try {
            projectRoot = ScriptingUtilities.invokePTO().getProjectRootNode(project_name);
            projectRoot.select();
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }

        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);

        wizard.selectCategory(doccategory);
        wizard.selectFileType(doctype);
        wizard.next();

        location = new NewJavaFileNameLocationStepOperator();
        buildedname = docname + "_" + System.currentTimeMillis();
        location.txtObjectName().setText(buildedname);
    }

    @Override
    public void close() {
        EditorOperator.closeDiscardAll();
    }
}
