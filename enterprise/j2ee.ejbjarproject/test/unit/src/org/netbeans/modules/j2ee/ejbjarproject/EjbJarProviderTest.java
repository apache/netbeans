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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EjbJarProviderTest extends TestBase {
    
    private static final String EJBJAR_XML = "ejb-jar.xml";
    
    private Project project;
    private AntProjectHelper helper;
    
    public EjbJarProviderTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensuer ejb-jar.xml and webservices.xml exist
        FileObject ejbJarXmlFO = metaInfFO.getFileObject(EJBJAR_XML);
        assertNotNull(ejbJarXmlFO);
        assertNotNull(metaInfFO.getFileObject("webservices.xml"));

        // ensure deployment descriptor files are returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        assertEquals(ejbJarXmlFO, FileUtil.toFileObject(provider.getJ2eeModule().getDeploymentConfigurationFile(EJBJAR_XML)));
        
        EjbJarImplementation ejbJar = project.getLookup().lookup(EjbJarImplementation.class);
        assertEquals(metaInfFO, ejbJar.getMetaInf());
        assertEquals(ejbJarXmlFO, ejbJar.getDeploymentDescriptor());
    }

    public void testJavaEEProjectSettingsInEjbJar() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule_6_0");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        Profile obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_6_FULL, obtainedProfile);
        JavaEEProjectSettings.setProfile(project, Profile.JAVA_EE_7_FULL);
        obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_7_FULL, obtainedProfile);
    }
    
    public void testMetadataModel()throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = provider.getJ2eeModule();
        assertNotNull(j2eeModule.getMetadataModel(EjbJarMetadata.class));
        assertNotNull(j2eeModule.getMetadataModel(WebservicesMetadata.class));
    }
    
    /**
     * Tests that null is silently returned for files in the configuration files directory
     * (meta.inf) when this directory does not exist.
     */
    public void testMetaInfBasedPathsAreNullWhenMetaInfIsNullIssue65888() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/BrokenEJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
        
        // first ensure meta.inf does not exist
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        assertNull(helper.resolveFileObject(metaInf));
        
        // ensure meta.inf-related files are silently returned as null
        
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = provider.getJ2eeModule();
        assertNotNull(j2eeModule.getDeploymentConfigurationFile(EJBJAR_XML));
        assertNull(FileUtil.toFileObject(j2eeModule.getDeploymentConfigurationFile(EJBJAR_XML)));
        assertNotNull(j2eeModule.getMetadataModel(EjbJarMetadata.class));
        assertNotNull(j2eeModule.getMetadataModel(WebservicesMetadata.class));
        
        EjbJarImplementation ejbJar = project.getLookup().lookup(EjbJarImplementation.class);
        assertNull(ejbJar.getMetaInf());
        assertNull(ejbJar.getDeploymentDescriptor());
    }
    
    public void testNeedConfigurationFolder() {
        assertTrue("1.3 needs configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.J2EE_13));
        assertTrue("1.4 needs configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.J2EE_14));
        assertFalse("5.0 does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.JAVA_EE_5));
        assertFalse("Anything else does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.JAVA_EE_6_FULL));
        assertFalse("Anything else does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.JAVA_EE_6_WEB));
        assertFalse("Even null does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(null));
    }
    
}
