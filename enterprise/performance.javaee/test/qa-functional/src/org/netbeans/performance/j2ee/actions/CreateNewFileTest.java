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
package org.netbeans.performance.j2ee.actions;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2ee.setup.J2EESetup;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of Open File Dialog
 *
 * @author lmartinek@netbeans.org
 */
public class CreateNewFileTest extends PerformanceTestCase {

    private NewFileWizardOperator wizard;

    private String project;
    private String category;
    private String fileType;
    private String fileName;
    private String packageName;
    private final boolean isEntity = false;

    /**
     * Creates a new instance of CreateNewFileTest
     *
     * @param testName
     */
    public CreateNewFileTest(String testName) {
        super(testName);
        expectedTime = 5000;
    }

    /**
     * Creates a new instance of CreateNewFileTest
     *
     * @param testName
     * @param performanceDataName
     */
    public CreateNewFileTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 5000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(CreateNewFileTest.class).suite();
    }

    public void testCreateNewSessionBean() {
        WAIT_AFTER_OPEN = 10000;
        project = "TestApplication-ejb";
        category = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.resources.Bundle", "Templates/J2EE");
        fileType = "Session Bean";
        fileName = "NewTestSession";
        packageName = "test.newfiles";
        doMeasurement();
    }

    /*    public void testCreateNewEntityBean() {
     WAIT_AFTER_OPEN = 10000;
     project = "TestApplication-ejb";
     category = "Enterprise";
     fileType = "Entity Bean";
     fileName = "NewTestEntity";
     packageName = "test.newfiles";
     isEntity = true;
     doMeasurement();
     }
     */
    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void prepare() {
        new NewFileAction().performMenu();
        wizard = new NewFileWizardOperator();
        wizard.selectProject(project);
        wizard.selectCategory(category);
        wizard.selectFileType(fileType);
        wizard.next();
        JTextFieldOperator eBname;
        if (isEntity == true) {
            eBname = new JTextFieldOperator(wizard, 1);
        } else {
            eBname = new JTextFieldOperator(wizard);
        }
        eBname.setText(fileName + CommonUtilities.getTimeIndex());
        new JComboBoxOperator(wizard, 1).getTextField().setText(packageName);

    }

    @Override
    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        wizard.finish();
        return null;
    }

    @Override
    public void close() {
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();
    }
}
