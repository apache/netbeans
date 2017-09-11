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
