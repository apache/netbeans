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
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 *
 * @author mkhramov@netbeans.org, mrkam@netbeans.org
 */
public class CreatePHPSampleProjectTest extends PerformanceTestCase {

    private NewProjectWizardOperator wizard;

    public CreatePHPSampleProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 5000;
    }

    public CreatePHPSampleProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 5000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(ScriptingSetup.class)
                .addTest(CreatePHPSampleProjectTest.class)
                .suite();
    }

    @Override
    public void initialize() {
        closeAllModal();
    }

    @Override
    public void prepare() {
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_DIFF_SIDEBAR_FILTER);

        wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory("Samples|PHP");
        wizard.selectProject("TodoList - PHP Sample Application");
        wizard.next();
    }

    @Override
    public ComponentOperator open() {
        wizard.finish();
        return null;
    }

    @Override
    public void close() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }

    public void testCreatePhpSampleProject() {
        doMeasurement();
    }
}
