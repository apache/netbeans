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

package org.netbeans.modules.j2ee.earproject.ui;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;

/**
 * Test functionality of {@link ModuleNode} and maybe more of EAR's
 * {@link LogicalViewProvider logical view provider}.
 *
 * @author Martin Krauskopf
 */
public class ModuleNodeTest extends NbTestCase {
    
    private static final int CHILDREN_UPDATE_TIME_OUT = 20000;
    
    public ModuleNodeTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        MockLookup.setLayersAndInstances();
    }
    
    public void testRemoveFromJarContent() throws Exception {
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.J2EE_14;
        String jarName = "testEA-ejb";
        
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, null, jarName, null, null, null);
        
        Project earProject = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        
        LogicalViewProvider lvp = earProject.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        // force nodes initialization:
        root.getChildren().getNodes(true);
        LogicalViewNode j2eeModules = (LogicalViewNode) root.getChildren().findChild(LogicalViewNode.J2EE_MODULES_NAME);
        assertSame("have ejb module's node", 1, j2eeModules.getChildren().getNodes(true).length);
        
        ModuleNode moduleNode = (ModuleNode) j2eeModules.getChildren().findChild(ModuleNode.MODULE_NODE_NAME);
        assertNotNull("have modules node", moduleNode);
        moduleNode.removeFromJarContent();
        assertNumberOfNodes("ejb module removed", j2eeModules, 0);
    }
    
    // See also issue #70943
    public void testRemoveFromJarContentWithDeletedProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.J2EE_14;
        String jarName = "testEA-ejb";
        
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, null, jarName, null, null, null);
        
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        Project earProject = ProjectManager.getDefault().findProject(prjDirFO);
        
        LogicalViewProvider lvp = earProject.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        // force nodes initialization:
        root.getChildren().getNodes(true);
        LogicalViewNode j2eeModules = (LogicalViewNode) root.getChildren().findChild(LogicalViewNode.J2EE_MODULES_NAME);
        assertSame("have ejb module's node", 1, j2eeModules.getChildren().getNodes(true).length);
        
        ModuleNode moduleNode = (ModuleNode) j2eeModules.getChildren().findChild(ModuleNode.MODULE_NODE_NAME);
        assertNotNull("have modules node", moduleNode);
        
        // Simulata one of scenarios in #70943
        FileObject ejbJarFO = prjDirFO.getFileObject("testEA-ejb");
        ejbJarFO.delete();
        moduleNode.removeFromJarContent();
        j2eeModules.getChildren().getNodes(true);
        
        assertNumberOfNodes("ejb module removed", j2eeModules, 0);
    }
    
    private void assertNumberOfNodes(final String message, final LogicalViewNode j2eeModules,
            int expectedNumber) throws InterruptedException {
        int waitTime = 0;
        boolean failed = false;
        while (!failed && j2eeModules.getChildren().getNodes(true).length != 0) {
            failed = ++waitTime > CHILDREN_UPDATE_TIME_OUT/50;
            Thread.sleep(50);
        }
        assertFalse(message, failed);
    }
    
}
