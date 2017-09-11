/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        File baseDir = extractToWorkDir("myeclipselibstest.zip");
        Workspace w = EclipseProjectTestUtils.createWorkspace(getWorkDir(), 
            new Workspace.Variable("MYECLIPSE_JSF_HOME", getDataDir().getPath()),
            new Workspace.Variable("ECLIPSE_HOME", getWorkDirPath())
            );
        w.loadMyEclipseLibraries(new ArrayList<String>());
        assertEquals(3, w.getUserLibraries().keySet().size());
        List<String> jarContent = w.getUserLibraries().get("JSF_RI_1_1_01");
        assertEquals(8, jarContent.size());
        jarContent = w.getUserLibraries().get("MyFaces_1_1");
        assertEquals(13, jarContent.size());
        jarContent = w.getUserLibraries().get("FACELETS1");
        assertEquals(3, jarContent.size());
        assertEquals(getDataDir().getPath()+"/facelets/lib/jsf-facelets.jar", jarContent.get(0));
        assertEquals(getDataDir().getPath()+"/facelets/lib/el-api.jar", jarContent.get(1));
        assertEquals(getDataDir().getPath()+"/facelets/lib/el-ri.jar", jarContent.get(2));
    }
}
