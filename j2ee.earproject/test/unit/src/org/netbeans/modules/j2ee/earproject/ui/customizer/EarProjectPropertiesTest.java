/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultListModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.MockLookup;

/**
 * @author Martin Krauskopf
 */
public class EarProjectPropertiesTest extends NbTestCase {
    
    private static final String CAR_REFERENCE_EXPECTED_KEY = "reference.testEA-app-client.j2ee-module-car";
    private static final String CAR_REFERENCE_EXPECTED_VALUE = "${project.testEA-app-client}/dist/testEA-app-client.jar";
    private static final String EJB_REFERENCE_EXPECTED_KEY = "reference.testEA-ejb.dist-ear";
    private static final String EJB_REFERENCE_EXPECTED_VALUE = "${project.testEA-ejb}/dist/testEA-ejb.jar";
    private static final String WEB_REFERENCE_EXPECTED_KEY = "reference.testEA-web.dist-ear";
    private static final String WEB_REFERENCE_EXPECTED_VALUE = "${project.testEA-web}/dist/testEA-web.war";

    private EarProject earProject;
    private EarProjectProperties earProjectProperties;
    
    public EarProjectPropertiesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();

        // create project
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        String carName = "testEA-app-client";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, ejbName, carName, null, null, null);
        FileObject prjDirFO = FileUtil.toFileObject(earDirF);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(prjDirFO);
        
        // verify ejb reference
        EditableProperties ep = TestUtil.loadProjectProperties(prjDirFO);
        String ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertEquals("ejb reference should be set properly", EJB_REFERENCE_EXPECTED_VALUE, ejbReferenceValue);
        
        // verify car reference
        String carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertEquals("car reference should be set properly", CAR_REFERENCE_EXPECTED_VALUE, carReferenceValue);
        
        // get ear project from lookup
        EarProject p = (EarProject)ProjectManager.getDefault().findProject(prjDirFO);
        earProject = p.getLookup().lookup(EarProject.class);
        assertNotNull("project should be created", earProject);
        
        // create ear project properties and verify them
        AntProjectHelper aph = project.getAntProjectHelper();
        AuxiliaryConfiguration aux = aph.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(aph, aux, aph.getStandardPropertyEvaluator());
        earProjectProperties = new EarProjectProperties(earProject, p.getUpdateHelper(), p.evaluator(), refHelper);
        assertNotNull("ear project properties should be created", earProjectProperties);
    }

    public void testPropertiesWithoutDDJ2EE() throws Exception { // see #73751
        File proj = new File(getWorkDir(), "EARProject");
        AntProjectHelper aph = EarProjectGenerator.createProject(proj,
                "test-project", Profile.J2EE_14, TestUtil.SERVER_URL, "1.4", null);
        FileObject prjDirFO = aph.getProjectDirectory();
        // simulateing #73751
        prjDirFO.getFileObject("src/conf/application.xml").delete();
        EarProject p = (EarProject)ProjectManager.getDefault().findProject(prjDirFO);
        AuxiliaryConfiguration aux = aph.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(aph, aux, aph.getStandardPropertyEvaluator());
        assertNotNull("non-null application modules", EarProjectProperties.getApplicationSubprojects(p));
    }

    public void testPropertiesWithoutDDJavaEE() throws Exception {
        File proj = new File(getWorkDir(), "EARProject");
        AntProjectHelper aph = EarProjectGenerator.createProject(proj,
                "test-project", Profile.JAVA_EE_5, TestUtil.SERVER_URL, "1.5", null);
        FileObject prjDirFO = aph.getProjectDirectory();
        assertNull("application should not exist", prjDirFO.getFileObject("src/conf/application.xml"));
        EarProject p = (EarProject)ProjectManager.getDefault().findProject(prjDirFO);
        AuxiliaryConfiguration aux = aph.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(aph, aux, aph.getStandardPropertyEvaluator());
        assertNotNull("non-null application modules", EarProjectProperties.getApplicationSubprojects(p));
    }
    
    public void testPathInEARChangingJ2EE() throws Exception { // see #76008
        testPathInEARChanging(Profile.J2EE_14);
    }
    
    public void testPathInEARChangingJavaEE() throws Exception { // see #76008
        testPathInEARChanging(Profile.JAVA_EE_5);
    }
    
    private void testPathInEARChanging(Profile j2eeProfile) throws Exception { // see #76008
        File earDirF = new File(getWorkDir(), "testEA-1");
        String name = "Test EnterpriseApplication";
        String ejbName = "testEA-ejb";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, ejbName, null, null, null, null);
        EarProject earProject = (EarProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(earDirF));
        Application app = earProject.getAppModule().getApplication();
        assertEquals("ejb path", "testEA-ejb.jar", app.getModule(0).getEjb());
        
        // simulate change through customizer
        EditableProperties projectProperties = earProject.getUpdateHelper().getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        List<ClassPathSupport.Item> old = EarProjectProperties.getJarContentAdditional(earProject);
        List<ClassPathSupport.Item> updated = EarProjectProperties.getJarContentAdditional(earProject);
        updated.get(0).setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, "otherPath");
        EarProjectProperties.updateContentDependency(earProject,
                old, updated,
                projectProperties);
        
        assertEquals("ejb path", "otherPath/testEA-ejb.jar", app.getModule(0).getEjb());
    }
    
    public static void putProperty(EarProject p, String key, String value) throws IOException {
        EditableProperties projectProperties = p.getUpdateHelper().getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        projectProperties.setProperty(key, value);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
    }
    
    public static void putProperty(EarProject p, String key, String value[]) throws IOException {
        EditableProperties projectProperties = p.getUpdateHelper().getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        projectProperties.setProperty(key, value);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        ProjectManager.getDefault().saveProject(p);
    }
    
    public static void putProperty(EarProject p, String key, List<ClassPathSupport.Item> modules, String element) throws IOException {
        String value[] = p.getClassPathSupport().encodeToStrings(modules, element);
        putProperty(p, key, value);
    }
    
    // see #97185 & #95604
    public void testResolveProjectDependencies() throws Exception {
        
        int countBefore = EarProjectProperties.getJarContentAdditional(earProject).size();
        DefaultListModel l = earProjectProperties.EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel();
        l.remove(l.indexOf(getEjbProject()));
        earProjectProperties.store();
        
        EditableProperties ep = TestUtil.loadProjectProperties(earProject.getProjectDirectory());
        String ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertNull("ejb reference should not exist", ejbReferenceValue);
        String carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertEquals("car reference should exist", CAR_REFERENCE_EXPECTED_VALUE, carReferenceValue);
        assertEquals("wrong count of project references", countBefore - 1, EarProjectProperties.getJarContentAdditional(earProject).size());
        assertEquals("wrong count of project references", countBefore - 1, earProject.getReferenceHelper().getRawReferences().length);
        
        // remove all entries
        l.clear();
        earProjectProperties.store();
        assertEquals("wrong count of project references", 0, EarProjectProperties.getJarContentAdditional(earProject).size());
        
        // add new project/module
        l.addElement(getWebProject());
        earProjectProperties.store();
        
        ep = TestUtil.loadProjectProperties(earProject.getProjectDirectory());
        ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertNull("ejb reference should not exist", ejbReferenceValue);
        carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertNull("car reference should not exist", carReferenceValue);
        String webReferenceValue = ep.getProperty(WEB_REFERENCE_EXPECTED_KEY);
        assertEquals("web reference should exist", WEB_REFERENCE_EXPECTED_VALUE, webReferenceValue);
        assertEquals("wrong count of project references", 1, EarProjectProperties.getJarContentAdditional(earProject).size());
        assertEquals("wrong count of project references", 1, earProject.getReferenceHelper().getRawReferences().length);
    }
    
    private ClassPathSupport.Item getEjbProject() {
        List<ClassPathSupport.Item> list = EarProjectProperties.getJarContentAdditional(earProject);
        for (ClassPathSupport.Item vcpi : list) {
            if (vcpi.getReference().indexOf(EJB_REFERENCE_EXPECTED_KEY) != -1
                    /*&& EJB_REFERENCE_EXPECTED_VALUE.endsWith(vcpi.getEvaluated())*/) {
                return vcpi;
            }
        }
        return null;
    }
    
    private ClassPathSupport.Item getWebProject() throws IOException {
        List<AntArtifact> artifactList = new ArrayList<AntArtifact>();
        AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType(
                createWebProject(),
                EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE);
        if (null != artifacts) {
            artifactList.addAll(Arrays.asList(artifacts));
        }
        assertEquals("size should be exactly 1", 1, artifactList.size());
        
        // create the vcpis
        for (AntArtifact art : artifactList) {
            ClassPathSupport.Item vcpi = ClassPathSupport.Item.create(art, art.getArtifactLocations()[0], null);
            return vcpi;
        }
        fail("web reference should exist");
        return null;
    }
    
    private Project createWebProject() throws IOException {
        String warName = "testEA-web";
        File projectDir = FileUtil.toFile(earProject.getProjectDirectory());
        File webAppDir = new File(projectDir, warName);
        if (webAppDir.exists()) {
            webAppDir.delete();
        }
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(FileUtil.normalizeFile(webAppDir));
        createData.setName(warName);
        createData.setServerInstanceID(TestUtil.SERVER_URL);
        createData.setSourceStructure(WebProjectUtilities.SRC_STRUCT_BLUEPRINTS);
        createData.setJavaEEProfile(EarProjectGenerator.getAcceptableProfile(Profile.JAVA_EE_5, TestUtil.SERVER_URL, J2eeModule.Type.WAR));
        createData.setContextPath("/" + warName);
        AntProjectHelper webHelper = WebProjectUtilities.createProject(createData);

        FileObject webAppDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(webAppDir));
        Project webProject = ProjectManager.getDefault().findProject(webAppDirFO);
        assertNotNull("web project should exist", webProject);
        
        return webProject;
    }

    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class DummyBrowser implements HtmlBrowser.Factory, EnhancedBrowserFactory {

        @Override
        public HtmlBrowser.Impl createHtmlBrowserImpl() {
            return new HtmlBrowser.Impl() {

                @Override
                public Component getComponent() {
                    return null;
                }

                @Override
                public void reloadDocument() {
                }

                @Override
                public void stopLoading() {
                }

                @Override
                public void setURL(URL url) {
                }

                @Override
                public URL getURL() {
                    return null;
                }

                @Override
                public String getStatusMessage() {
                    return null;
                }

                @Override
                public String getTitle() {
                    return null;
                }

                @Override
                public boolean isForward() {
                    return false;
                }

                @Override
                public void forward() {
                }

                @Override
                public boolean isBackward() {
                    return false;
                }

                @Override
                public void backward() {
                }

                @Override
                public boolean isHistory() {
                    return false;
                }

                @Override
                public void showHistory() {
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {
                }
            };
        }

        @Override
        public BrowserFamilyId getBrowserFamilyId() {
            return BrowserFamilyId.FIREFOX;
        }

        @Override
        public Image getIconImage(boolean small) {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "some";
        }

        @Override
        public String getId() {
            return "some";
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return false;
        }

        @Override
        public boolean canCreateHtmlBrowserImpl() {
            return true;
        }

    }
}
