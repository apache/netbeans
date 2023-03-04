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
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * @author Andrei Badea
 */
public class AppClientProviderTest extends NbTestCase {
    
    private static final String APPLICATION_CLIENT_XML = "application-client.xml";
    
    public AppClientProviderTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AppClientProvider.showMetaInfDialog = false;
        TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        Project project = (Project) ProjectSupport.openProject(f);
        // XXX should not cast a Project
        AntProjectHelper helper = ((AppClientProject) project).getAntProjectHelper();

        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);

        // ensuer application-client.xml exists
        FileObject appXmlFO = metaInfFO.getFileObject(APPLICATION_CLIENT_XML);
        assertNotNull(appXmlFO);

        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        assertEquals(FileUtil.toFile(metaInfFO.getFileObject(APPLICATION_CLIENT_XML)),
                provider.getJ2eeModule().getDeploymentConfigurationFile(APPLICATION_CLIENT_XML));
    }
    
    public void testMetadataModel() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        Project project = (Project) ProjectSupport.openProject(f);
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = provider.getJ2eeModule();
        assertNotNull(j2eeModule.getMetadataModel(AppClientMetadata.class));
        assertNotNull(j2eeModule.getMetadataModel(WebservicesMetadata.class));
    }
    
    public void testThatProjectWithoutDDCanBeOpened() throws Exception {
        File prjDirOrigF = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        File prjDirF = TestUtil.copyFolder(getWorkDir(), prjDirOrigF);
        TestUtil.deleteRec(new File(new File(prjDirF, "src"), "conf"));
        FileUtil.refreshFor(prjDirF);

        Project project = (Project) ProjectSupport.openProject(prjDirF);
        assertNotNull("Project is null", project);
        assertNotNull("Project lookup is null", project.getLookup());

        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        File someConfFile = provider.getJ2eeModule().getDeploymentConfigurationFile("does-not-matter.xml");
        assertNotNull("J2eeModuleProvider.getDeploymentConfigurationFile() cannot return null", someConfFile);
        File expected = new File(prjDirF + File.separator + "src" +
                File.separator + "conf" + File.separator + "does-not-matter.xml");
        assertEquals("expected path", expected, someConfFile);
    }
    
    public void testNeedConfigurationFolder() {
        assertTrue("1.3 needs configuration folder",
                AppClientProvider.needConfigurationFolder(Profile.J2EE_13));
        assertTrue("1.4 needs configuration folder",
                AppClientProvider.needConfigurationFolder(Profile.J2EE_14));
        assertFalse("5.0 does not need configuration folder",
                AppClientProvider.needConfigurationFolder(Profile.JAVA_EE_5));
        assertFalse("Anything else does not need configuration folder",
                AppClientProvider.needConfigurationFolder(Profile.JAVA_EE_6_FULL));
        assertFalse("Anything else does not need configuration folder",
                AppClientProvider.needConfigurationFolder(Profile.JAVA_EE_6_WEB));
        assertFalse("Even null does not need configuration folder",
                AppClientProvider.needConfigurationFolder(null));
    }
    
}
