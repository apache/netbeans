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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * SuiteProjectGenerator tests.
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public class SuiteProjectGeneratorTest extends TestBase {
    // XXX also should test content of created files (XMLs, properties)
    
    public SuiteProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    private static final String[] SUITE_CREATED_FILES = {
        "build.xml",
        "nbproject/project.xml",
        "nbproject/build-impl.xml",
        "nbproject/platform.properties",
        "nbproject/project.properties",
    };
    
    public void testCreateSuiteProject() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(targetPrjDir, NbPlatform.PLATFORM_ID_DEFAULT, false);
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        ((SuiteProject) p).open();
        // check generated module
        for (int i=0; i < SUITE_CREATED_FILES.length; i++) {
            assertNotNull(SUITE_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(SUITE_CREATED_FILES[i]));
        }
    }
    
    public void testSuiteProjectWithDotInName() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testSuite 1.0");
        SuiteProjectGenerator.createSuiteProject(targetPrjDir, NbPlatform.PLATFORM_ID_DEFAULT, false);
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertEquals("#66080: right display name", "testSuite 1.0", ProjectUtils.getInformation(p).getDisplayName());
    }
    
}
