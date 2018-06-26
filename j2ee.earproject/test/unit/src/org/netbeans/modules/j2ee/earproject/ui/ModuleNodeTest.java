/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
