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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * @author Martin Krauskopf
 */
public class ProjectEarProviderTest extends NbTestCase {

    public ProjectEarProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();
    }

    public void testFindEarJavaEE() throws Exception {
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.JAVA_EE_5;
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile, TestUtil.SERVER_URL);
        FileObject earDirFO = FileUtil.toFileObject(earDirF);
        Project createdEjbJarProject = ProjectManager.getDefault().findProject(earDirFO);
        assertNotNull("Ear found", Ear.getEar(earDirFO));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf")));
        assertNull("DD should not exist", earDirFO.getFileObject("src/conf/application.xml"));
        assertNull("not Ear for parent", Ear.getEar(earDirFO.getParent()));
    }

    public void testFindEarJ2EE() throws Exception {
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.J2EE_14;
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile, TestUtil.SERVER_URL);
        FileObject earDirFO = FileUtil.toFileObject(earDirF);
        Project createdEjbJarProject = ProjectManager.getDefault().findProject(earDirFO);
        assertNotNull("Ear found", Ear.getEar(earDirFO));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf/application.xml")));
        assertNull("not Ear for parent", Ear.getEar(earDirFO.getParent()));
    }

}
