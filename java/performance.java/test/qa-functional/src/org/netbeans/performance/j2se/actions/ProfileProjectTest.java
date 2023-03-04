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
package org.netbeans.performance.j2se.actions;

import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Profile project.
 *
 * @author mmirilovic@netbeans.org
 * @author Jiri Skrivanek
 */
public class ProfileProjectTest extends PerformanceTestCase {

    private Node projectNode;
    private String projectName;

    /**
     * Creates a new instance of test
     *
     * @param testName the name of the test
     */
    public ProfileProjectTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of test
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ProfileProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public void testProfileProject() {
        doMeasurement();
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(ProfileProjectTest.class)
                .suite();
    }

    @Override
    public void prepare() {
        projectName = "PerformanceTestData";
        projectNode = new ProjectsTabOperator().getProjectRootNode(projectName);
    }

    @Override
    public ComponentOperator open() {
        projectNode.performPopupAction("Profile");
        return new TopComponentOperator(projectName);
    }
}
