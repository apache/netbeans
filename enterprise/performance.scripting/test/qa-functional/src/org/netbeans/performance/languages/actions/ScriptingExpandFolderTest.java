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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingExpandFolderTest extends PerformanceTestCase {

    /**
     * Name of the folder which test creates and expands
     */
    protected String project;
    /**
     * Path to the folder which test creates and expands
     */
    protected String pathToFolderNode;
    /**
     * Node represantation of the folder which test creates and expands
     */
    protected Node nodeToBeExpanded;
    /**
     * Projects tab
     */
    protected ProjectsTabOperator projectTab;

    public ScriptingExpandFolderTest(String testName) {
        super(testName);
    }

    public ScriptingExpandFolderTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() throws URISyntaxException {
        URL u = ScriptingExpandFolderTest.class.getProtectionDomain().getCodeSource().getLocation();
        File f = new File(u.toURI());
        while (f != null) {
            File hg = new File(f, ".hg");
            if (hg.isDirectory()) {
                System.setProperty("versioning.unversionedFolders", f.getPath());
                System.err.println("ignoring Hg folder: " + f);
                break;
            }
            f = f.getParentFile();
        }

        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(ScriptingExpandFolderTest.class).suite();
    }

    @Override
    public void initialize() {
        projectTab = ScriptingUtilities.invokePTO();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode(project).collapse();
        repaintManager().addRegionFilter(LoggingRepaintManager.EXPLORER_FILTER);
    }

    public void prepare() {
        if (pathToFolderNode.equals("")) {
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        } else {
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        }
    }

    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EXPLORER_FILTER);
        nodeToBeExpanded.tree().doExpandPath(nodeToBeExpanded.getTreePath());
        nodeToBeExpanded.expand();
        return null;
    }

    @Override
    public void close() {
        nodeToBeExpanded.collapse();
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        projectTab.getProjectRootNode(project).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }

    public void testExpandPHPProjectNode() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.PHP_PROJECT;
        pathToFolderNode = "";
        expectedTime = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith100JSFiles() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.SCRIPTING_PROJECT;
        pathToFolderNode = "Web Pages" + "|" + "100JsFiles";
        expectedTime = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith100CssFiles() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.SCRIPTING_PROJECT;
        pathToFolderNode = "Web Pages" + "|" + "100CssFiles";
        expectedTime = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith100PhpFiles() {
        WAIT_AFTER_OPEN = 1000;
        project = Projects.PHP_PROJECT;
        pathToFolderNode = "Source Files" + "|" + "100PhpFiles";
        expectedTime = 1000;
        doMeasurement();
    }
}
