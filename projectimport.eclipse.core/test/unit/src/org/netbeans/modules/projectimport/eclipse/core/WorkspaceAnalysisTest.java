/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Tests importing of complex project (still without workspace provided). This
 * test should check all features if project analyzer.
 *
 * @author mkrauskopf
 */
public class WorkspaceAnalysisTest extends ProjectImporterTestCase {

    public WorkspaceAnalysisTest(String name) {
        super(name);
    }

    public void testComplexAloneProjectFor_3_1_M6() throws Exception {
        File workspaceDir = extractToWorkDir("workspace-test-3.1M6.zip");
        Workspace workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        assertNotNull("Unable to load workspace", workspace);
        assertFalse("Workspace shouldn't be empty", workspace.getProjects().isEmpty());
        
        // Below information are just known. Get familiar with tested zips
        // (which could be created by the helper script createWorkspace.sh)
        String[] ws31M6ProjectNames = {"p1", "p2", "p3"};
        String[] p1RequiredProjects  = {"/p2", "/p3"};
        
        boolean p1Tested = false;
        Collection<String> p1ReqProjectsNames =
                new ArrayList<String>(Arrays.asList(p1RequiredProjects));
        Collection<String> wsProjectNames =
                new ArrayList<String>(Arrays.asList(ws31M6ProjectNames));
        Collection<EclipseProject> gainedP1ReqProjects = null;
        
        for (EclipseProject project : workspace.getProjects()) {
            /* Test p1 project and its dependencies. */
            if ("p1".equals(project.getName())) {
                SingleProjectAnalysisTest.doBasicProjectTest(project, 2); // for p1
                gainedP1ReqProjects = project.getProjects();
                assertEquals("Incorrect project count for p1",
                        p1RequiredProjects.length, gainedP1ReqProjects.size());
                p1Tested = true;
            }
            wsProjectNames.remove(project.getName());
        }
        assertTrue("\"p1\" project wasn't found in the workspace.", p1Tested);
        assertTrue("All project should be processed.", wsProjectNames.isEmpty());
        for (EclipseProject project : gainedP1ReqProjects) {
            p1ReqProjectsNames.remove("/" + project.getName());
        }
        assertTrue("\"p1\" project depends on unknown projects: " + p1ReqProjectsNames,
                p1ReqProjectsNames.isEmpty());
    }
    
    public void test_73542() throws Exception {
        File workspaceDir = extractToWorkDir("workspace_73542-3.1.2.zip");
        Workspace workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        assertNotNull("Unable to load workspace", workspace);
    }
    
}
