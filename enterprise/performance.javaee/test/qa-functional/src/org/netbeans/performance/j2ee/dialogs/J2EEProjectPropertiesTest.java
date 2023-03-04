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
package org.netbeans.performance.j2ee.dialogs;

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Project Properties Window
 *
 * @author mmirilovic@netbeans.org
 */
public class J2EEProjectPropertiesTest extends PerformanceTestCase {

    private Node testNode;
    private String projectName;

    /**
     * Creates a new instance of ProjectPropertiesWindow
     *
     * @param testName
     */
    public J2EEProjectPropertiesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of ProjectPropertiesWindow
     *
     * @param testName
     * @param performanceDataName
     */
    public J2EEProjectPropertiesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(J2EEProjectPropertiesTest.class).suite();
    }

    public void testJ2EEProject() {
        projectName = "TestApplication";
        doMeasurement();
    }

    public void testJ2EE_ejbProject() {
        projectName = "TestApplication-ejb";
        doMeasurement();
    }

    public void testJ2EE_warProject() {
        projectName = "TestApplication-war";
        doMeasurement();
    }

    @Override
    public void initialize() {
        testNode = (Node) new ProjectsTabOperator().getProjectRootNode(projectName);
    }

    public void prepare() {
    }

    public ComponentOperator open() {
        new PropertiesAction().performPopup(testNode);
        return new NbDialogOperator("Project Properties");
    }
}
