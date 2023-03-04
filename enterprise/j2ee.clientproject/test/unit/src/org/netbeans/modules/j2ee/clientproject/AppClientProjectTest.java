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

package org.netbeans.modules.j2ee.clientproject;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.xml.sax.SAXException;

/**
 * @author Martin Krauskopf
 */
public class AppClientProjectTest extends NbTestCase {
    
    public AppClientProjectTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances(new TestPlatformProvider());
    }

    public void testBrokenAppClientOpening_73710() throws Exception {
        doTestBrokenAppClientOpening_73710(generateApplicationClient(
                "TestCreateACProject_14", J2eeModule.J2EE_14));
        doTestBrokenAppClientOpening_73710(generateApplicationClient(
                "TestCreateACProject_15", J2eeModule.JAVA_EE_5));
    }

    public void testJavaEEProjectSettingsInAppClient() throws Exception {
        File projectDir = generateApplicationClient("TestProject_ee6", Profile.JAVA_EE_6_FULL.toPropertiesString());
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(projectDir));
        Profile obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_6_FULL, obtainedProfile);
        JavaEEProjectSettings.setProfile(project, Profile.JAVA_EE_7_FULL);
        obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_7_FULL, obtainedProfile);
    }
    
    private void doTestBrokenAppClientOpening_73710(final File prjDirF) throws IOException, IllegalArgumentException {
        File dirCopy = TestUtil.copyFolder(getWorkDir(), prjDirF);
        File ddF = new File(dirCopy, "src/conf/application-client.xml");
        assertTrue("has deployment descriptor", ddF.isFile());
        ddF.delete(); // one of #73710 scenario
        Project project = (Project) ProjectSupport.openProject(dirCopy);
        assertNotNull("project is found", project);
        // tests #73710
        // open hook called by ProjectSupport
    }
    
    private File generateApplicationClient(String prjDir, String version) throws IOException, SAXException {
        File prjDirF = new File(getWorkDir(), prjDir);
        AppClientProjectGenerator.createProject(prjDirF, "test-project",
                "test.MyMain", version, TestUtil.SERVER_URL);
        return prjDirF;
    }

}
