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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentManager;

/**
 *
 * @author Petr Hejl
 */
public class ServerFileDistributorTest extends ServerRegistryTestBase {

    private static final String URL = "fooservice:testInstance"; // NOI18N

    public ServerFileDistributorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerRegistry registry = ServerRegistry.getInstance();
        Map<String, String> props = new HashMap<String, String>();
        props.put(TestDeploymentManager.MULTIPLE_TARGETS, "false");
        props.put(TestDeploymentManager.WORK_DIR, getWorkDirPath());
        registry.addInstance(URL, "user", "password", "TestInstance", true, false, props); // NOI18N
    }

    @Override
    protected void tearDown() throws Exception {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.removeServerInstance(URL);
        super.tearDown();
    }

    public void testDistributeOnSaveWar() throws IOException, ServerException {
        // class
        singleFileTest("deploytest1", "build/web/WEB-INF/classes/test/TestServlet.class",
                "testplugin/applications/web/WEB-INF/classes/test/TestServlet.class",
                true, false, false, false, false);

        // jsp
        singleFileTest("deploytest1", "build/web/index.jsp", "testplugin/applications/web/index.jsp",
                false, false, false, false, false);

        // web.xml
        singleFileTest("deploytest1", "build/web/WEB-INF/web.xml", "testplugin/applications/web/WEB-INF/web.xml",
                false, true, false, false, false);
    }

    public void testDistributeOnSaveEjb() throws IOException, ServerException {
        // class
        // TODO ejbs changed not working :((
        singleFileTest("deploytest2", "build/jar/test/TestSessionBeanBean.class",
                "testplugin/applications/jar/test/TestSessionBeanBean.class",
                true, false, false, false, false);

        // MANIFEST.MF
        singleFileTest("deploytest2", "build/jar/META-INF/MANIFEST.MF", "testplugin/applications/jar/META-INF/MANIFEST.MF",
                false, false, false, true, false);
    }

    private void singleFileTest(String projectName, String testFilePath, String createdFilePath,
            boolean classesChanged, boolean descriptorChanged, boolean ejbChanged, boolean manifestChanged,
            boolean serverDescriptorChanged) throws IOException, ServerException {

        File f = getProjectAsFile(this, projectName);
        Project project = (Project) ProjectSupport.openProject(f);
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        provider.setServerInstanceID(URL);


        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(URL);
        DeploymentTarget dtarget = new DeploymentTarget(provider, null);
        TargetServer server = new TargetServer(dtarget);

        ProgressUI ui = new ProgressUI("test", true);
        TargetModule module = null;
        ui.start();
        try {
            module = server.deploy(ui, true)[0];
        } finally {
            ui.finish();
        }

        ServerFileDistributor dist = new ServerFileDistributor(instance, dtarget);

        File testFile = new File(f,
                testFilePath.replace("/", File.separator));
        DeploymentChangeDescriptor desc = dist.distributeOnSave(module,
                dtarget.getModuleChangeReporter(), null, Collections.singleton(Artifact.forFile(testFile)));

        File created = new File(getWorkDir(),
                createdFilePath.replace("/", File.separator));

        assertEquals(classesChanged, desc.classesChanged());
        assertEquals(descriptorChanged, desc.descriptorChanged());
        assertEquals(ejbChanged, desc.ejbsChanged());
        assertEquals(manifestChanged, desc.manifestChanged());
        assertEquals(serverDescriptorChanged, desc.serverDescriptorChanged());
        assertEquals(1, desc.getChangedFiles().length);
        assertEquals(created.getAbsoluteFile(), desc.getChangedFiles()[0].getAbsoluteFile());

        assertTrue(created.exists());
    }
}
