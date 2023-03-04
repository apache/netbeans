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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class WorkspaceTest extends ProjectImporterTestCase {
    
    public WorkspaceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testLoadMyEclipseLibraries() throws Exception {
        File myecliselibstest = new File(getDataDir(), "myecliselibstest");
        Workspace w = EclipseProjectTestUtils.createWorkspace(myecliselibstest, 
            new Workspace.Variable("MYECLIPSE_JSF_HOME", myecliselibstest.getPath()),
            new Workspace.Variable("ECLIPSE_HOME", myecliselibstest.getAbsolutePath())
            );
        w.loadMyEclipseLibraries(new ArrayList<String>());
        assertEquals(3, w.getUserLibraries().keySet().size());
        List<String> jarContent = w.getUserLibraries().get("JSF_RI_1_1_01");
        assertEquals(8, jarContent.size());
        jarContent = w.getUserLibraries().get("MyFaces_1_1");
        assertEquals(13, jarContent.size());
        jarContent = w.getUserLibraries().get("FACELETS1");
        assertEquals(3, jarContent.size());
        assertEquals(myecliselibstest.getAbsolutePath()+"/facelets/lib/jsf-facelets.jar", jarContent.get(0));
        assertEquals(myecliselibstest.getAbsolutePath()+"/facelets/lib/el-api.jar", jarContent.get(1));
        assertEquals(myecliselibstest.getAbsolutePath()+"/facelets/lib/el-ri.jar", jarContent.get(2));
    }
}
