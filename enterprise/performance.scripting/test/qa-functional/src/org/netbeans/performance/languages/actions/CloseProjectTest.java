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

import java.io.IOException;
import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.openide.util.Exceptions;

/**
 *
 * @author mrkam@netbeans.org
 */
public class CloseProjectTest extends PerformanceTestCase {

    private static String projectName;
    protected static ProjectsTabOperator projectsTab = null;

    public CloseProjectTest(String testName) {
        super(testName);
    }

    public CloseProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(CloseProjectTest.class).suite();
    }

    @Override
    public void initialize() {
        closeAllModal();
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        new CloseAction().perform(getProjectNode(projectName));
        return null;
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    @Override
    public void close() {
        closeAllModal();
        try {
            this.openDataProjects(projectName);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void testClosePHPProject() {
        projectName = Projects.PHP_PROJECT;
        expectedTime = 1000;
        doMeasurement();
    }

}
