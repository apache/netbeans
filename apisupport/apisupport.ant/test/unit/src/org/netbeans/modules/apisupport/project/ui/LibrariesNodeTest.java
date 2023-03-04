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

package org.netbeans.modules.apisupport.project.ui;

import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.Node;

/**
 * @author Martin Krauskopf
 */
public class LibrariesNodeTest extends TestBase {
    
    public LibrariesNodeTest(String testName) {
        super(testName);
    }

    public void testLibrariesNodeListening() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node libraries = root.getChildren().findChild(LibrariesNode.LIBRARIES_NAME);
        assertNotNull(libraries);
        /* XXX inherently unreliable:
        TestBase.assertAsynchronouslyUpdatedChildrenNodes(libraries, 1);
        Util.addDependency(p, "org.netbeans.modules.java.project");
        ProjectManager.getDefault().saveProject(p);
        TestBase.assertAsynchronouslyUpdatedChildrenNodes(libraries, 2);
         */
    }
    
    public void testDependencyNodeActions() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
        Node root = lvp.createLogicalView();
        Node libraries = root.getChildren().findChild(LibrariesNode.LIBRARIES_NAME);
        assertNotNull(libraries);
        /* XXX inherently unreliable:
        Util.addDependency(p, "org.netbeans.modules.java.project");
        ProjectManager.getDefault().saveProject(p);
        TestBase.assertAsynchronouslyUpdatedChildrenNodes(libraries, 2);
        Node[] nodes = libraries.getChildren().getNodes(true);
        assertEquals("four actions", 4, nodes[1].getActions(false).length);
         */
    }
    
}
