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
import java.util.Collection;

/**
 * Tests importing of single project (that is without workspace provided).
 *
 * <p>
 * This is first level check if importer is working correctly - i.e. it is able
 * to parse project without <code>ProjectImporterException<code> and similar to
 * be thrown.
 * </p>
 *
 * @author mkrauskopf
 */
public final class SingleProjectAnalysisTest extends ProjectImporterTestCase {

    public SingleProjectAnalysisTest(String name) {
        super(name);
    }
    
    public void testSimpleAloneProjectForLatestMilestone() throws Exception {
        File projectDir = extractToWorkDir("simpleAlone-3.1M6.zip");
        EclipseProject project = ProjectFactory.getInstance().load(projectDir);
        assertNotNull(project);
        doBasicProjectTest(project, 0);
        Collection projects = project.getProjects();
        assertTrue("There are no required projects for the project.", projects.isEmpty());
    }
    
    public void testEmptyWithoutConAndSrc58033() throws Exception {
        File projectDir = extractToWorkDir("emptyWithoutConAndSrc-3.0.2.zip");
        EclipseProject project = ProjectFactory.getInstance().load(projectDir);
        assertNotNull(project);
    }
    
    static void doBasicProjectTest(EclipseProject project, int cpItemsCount) {
        /* usage (see printOtherProjects to see how to use them) */
        String name = project.getName();
        assertTrue("Name cannot be null or empty", (name != null && !name.equals("")));
        
        File directory = project.getDirectory();
        assertNotNull(directory);
        
        String jdkDir = project.getJDKDirectory();
        //        assertNotNull("Cannot resolve JDK directory \"" + jdkDir + "\"", jdkDir);
        
        Collection srcRoots = project.getSourceRoots();
        assertFalse("Tere should be at least on source root",
                srcRoots.isEmpty());
        
        Collection cp = project.getClassPathEntries();
        assertEquals(cpItemsCount, cp.size());
    }
}
