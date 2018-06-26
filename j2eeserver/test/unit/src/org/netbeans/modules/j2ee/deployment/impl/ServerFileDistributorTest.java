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
