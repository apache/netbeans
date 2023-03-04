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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * @author Martin Krauskopf
 */
public class ProjectEarTest extends NbTestCase {

    public ProjectEarTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();
    }

    public void testModuleAddition() throws Exception {
        // testing project
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, ejbName, null, null, null, null);
        FileObject earDirFO = FileUtil.toFileObject(earDirF);
        FileObject ejbProjectFO = earDirFO.getFileObject("testEA-ejb");
        assertNotNull(ejbProjectFO);

        File earDirAnotherF = new File(getWorkDir(), "testEA-another");
        NewEarProjectWizardIteratorTest.generateEARProject(earDirAnotherF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, null, null, null, null, null);
        FileObject earDirAnotherFO = FileUtil.toFileObject(earDirAnotherF);
        EjbJarProject createdEjbJarProject = (EjbJarProject) ProjectManager.getDefault().findProject(ejbProjectFO);
        assertNotNull("ejb project found", createdEjbJarProject);
        Ear ear = Ear.getEar(earDirAnotherFO);
        assertNotNull("have Ear instance", ear);
        if (ear != null) {
            ear.addEjbJarModule(createdEjbJarProject.getAPIEjbJar());
        }

        EarProject earProject = (EarProject) ProjectManager.getDefault().findProject(earDirAnotherFO);
        Application app = earProject.getAppModule().getApplication();
        assertSame("ejb added modules", 1, app.getModule().length);
    }

}
