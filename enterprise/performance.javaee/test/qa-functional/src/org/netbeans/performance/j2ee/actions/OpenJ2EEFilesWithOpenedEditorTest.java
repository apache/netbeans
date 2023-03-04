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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of opening files.
 *
 * @author lmartinek@netbeans.org
 */
public class OpenJ2EEFilesWithOpenedEditorTest extends OpenJ2EEFilesTest {

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     */
    public OpenJ2EEFilesWithOpenedEditorTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenJ2EEFilesWithOpenedEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(OpenJ2EEFilesWithOpenedEditorTest.class).suite();
    }

    @Override
    public void testOpeningJava() {
        super.testOpeningJava();
    }

    @Override
    public void testOpeningSessionBean() {
        super.testOpeningSessionBean();
    }

    @Override
    public void testOpeningEntityBean() {
        super.testOpeningEntityBean();
    }

    @Override
    public void testOpeningEjbJarXml() {
        super.testOpeningEjbJarXml();
    }

    @Override
    public void testOpeningSunEjbJarXml() {
        super.testOpeningSunEjbJarXml();
    }

    @Override
    public void testOpeningApplicationXml() {
        super.testOpeningApplicationXml();
    }

    @Override
    public void testOpeningSunApplicationXml() {
        super.testOpeningSunApplicationXml();
    }

    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    @Override
    public void initialize() {
        super.initialize();
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Source Packages|test|TestSessionRemote.java"));
    }
}
