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

package org.netbeans.modules.j2ee.earproject.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectPropertiesTest;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test case for {@link ApplicationImpl}.
 * @author Tomas Mysik
 */
public class ApplicationImplTest extends NbTestCase {

    private static final String CAR_NAME = "testEA-app-client";
    private static final String CAR_REFERENCE_EXPECTED_KEY = "reference.testEA-app-client.j2ee-module-car";
    private static final String CAR_REFERENCE_EXPECTED_VALUE = "${project.testEA-app-client}/dist/testEA-app-client.jar";
    private static final String EJB_NAME = "testEA-ejb";
    private static final String EJB_REFERENCE_EXPECTED_KEY = "reference.testEA-ejb.dist-ear";
    private static final String EJB_REFERENCE_EXPECTED_VALUE = "${project.testEA-ejb}/dist/testEA-ejb.jar";
    private static final String WEB_NAME = "testEA-web";
    private static final String WEB_REFERENCE_EXPECTED_KEY = "reference.testEA-web.dist-ear";
    private static final String WEB_REFERENCE_EXPECTED_VALUE = "${project.testEA-web}/dist/testEA-web.war";

    private EarProject earProject;
    
    public ApplicationImplTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        clearWorkDir();
        TestUtil.initLookup(this);
        
        // create project
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.JAVA_EE_5;
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, WEB_NAME, EJB_NAME, CAR_NAME, null, null, null);
        FileObject prjDirFO = FileUtil.toFileObject(earDirF);
        
        // verify war reference
        EditableProperties ep = TestUtil.loadProjectProperties(prjDirFO);
        String webReferenceValue = ep.getProperty(WEB_REFERENCE_EXPECTED_KEY);
        assertEquals("war reference should be set properly", WEB_REFERENCE_EXPECTED_VALUE, webReferenceValue);
        
        // verify ejb reference
        String ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertEquals("ejb reference should be set properly", EJB_REFERENCE_EXPECTED_VALUE, ejbReferenceValue);
        
        // verify car reference
        String carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertEquals("car reference should be set properly", CAR_REFERENCE_EXPECTED_VALUE, carReferenceValue);
        
        // get ear project from lookup
        Project p = ProjectManager.getDefault().findProject(prjDirFO);
        earProject = p.getLookup().lookup(EarProject.class);
        assertNotNull("project should be created", earProject);
    }
    
    /**
     * <ul>
     * Test for:
     * <li>deployment descriptor <i>application.xml</i> should not be generated</li>
     * <li>Application should not be <code>null</code></li>
     * </ul>
     * @throws Exception if any error occurs.
     */
    public void testGetApplication() throws Exception {
        FileObject prjDirFO = earProject.getProjectDirectory();
        assertNull("application.xml should not exist", prjDirFO.getFileObject("src/conf/application.xml"));
        
        // test model
        getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Void>() {
            public Void run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                assertNotNull("application should not be null", application);
                return null;
            }
        });
    }
    
    /**
     * <ul>
     * Test for:
     * <li>Application modules should be empty outside <code>runReadAction</code></li>
     * </ul>
     * <p>
     * <b>This should not be ever done!</b>
     * @throws Exception if any error occurs.
     */
    public void testApplicationOutsideOfRunReadAction() throws Exception {
        
        // test model
        Application application = getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Application>() {
            public Application run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                
                // test application
                int size = EarProjectProperties.getJarContentAdditional(earProject).size();
                assertSame("application should contains exactly " + size + " modules", size, application.sizeModule());
                return application;
            }
        });
        
        // test model
        try {
            application.sizeModule();
            fail("should not get here");
            
        } catch (IllegalStateException expected) {
        }
    }
    
    /**
     * <ul>
     * Test for:
     * <li>Application should always be appropriate for EAR project</li>
     * </ul>
     * <ol>
     * This test should be as follows (but cannot be right now because of missing functionality of metadata model):
     * <li>get model</li>
     * <li>change EJB module</li>
     * <li>verify model</li>
     * </ol>
     * @throws Exception if any error occurs.
     */
    public void testChangesInEAR() throws Exception {
        
        ClassPathSupport.Item ejb = getEjb(EarProjectProperties.getJarContentAdditional(earProject));
        assertEquals("ejb path should be ok", EJB_NAME + ".jar", EarProjectProperties.getCompletePathInArchive(earProject, ejb));
        
        // change ejb
        final String otherPath = "otherPath";
        List<ClassPathSupport.Item> modules = new ArrayList<ClassPathSupport.Item>();
        modules.addAll(EarProjectProperties.getJarContentAdditional(earProject));
        ejb = getEjb(modules);
        ejb.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, otherPath);
        EarProjectPropertiesTest.putProperty(earProject, EarProjectProperties.JAR_CONTENT_ADDITIONAL, modules, EarProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
        
        // test model
        getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Void>() {
            public Void run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                
                // verify ejb
                Module ejbModule = getEjbModule(application.getModule());
                assertEquals("ejb path should be ok", otherPath + "/" + EJB_NAME + ".jar", ejbModule.getEjb());
                return null;
            }
        });
    }
    
    private MetadataModel<ApplicationMetadata> getModel() throws IOException, InterruptedException {
        return earProject.getAppModule().getMetadataModel();
    }
    
    private ClassPathSupport.Item getEjb(List<ClassPathSupport.Item> modules) {
        for (ClassPathSupport.Item vcpi : modules) {
            if (vcpi.getReference().indexOf(EJB_REFERENCE_EXPECTED_KEY) != -1
                    /*&& EJB_REFERENCE_EXPECTED_VALUE.endsWith(vcpi.getEvaluated())*/) {
                return vcpi;
            }
        }
        return null;
    }
    
    private Module getEjbModule(Module[] modules) {
        for (Module m : modules) {
            if (m.getEjb() != null) {
                return m;
            }
        }
        return null;
    }
}
