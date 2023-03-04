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
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * @author Andrei Badea
 */
public class EarProviderTest extends NbTestCase {

    
    private static final String APPLICATION_XML = "application.xml";
    
    
    public EarProviderTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        MockLookup.setLayersAndInstances();
        File f = new File(getDataDir().getAbsolutePath(), "projects/EnterpriseApplication1");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        EarProject earProject = project.getLookup().lookup(EarProject.class);
        AntProjectHelper helper = earProject.getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensure application-client.xml exists
        FileObject appXmlFO = metaInfFO.getFileObject(APPLICATION_XML);
        assertNotNull(appXmlFO);
        
        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        File dcFile = provider.getJ2eeModule().getDeploymentConfigurationFile(APPLICATION_XML);
        assertEquals(FileUtil.toFile(metaInfFO.getFileObject(APPLICATION_XML)), dcFile);
        
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = jmp.getJ2eeModule();
        assertNotNull(j2eeModule.getMetadataModel(ApplicationMetadata.class));
    }
    
}
