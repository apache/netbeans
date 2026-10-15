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

package org.netbeans.modules.web.project;

import java.io.File;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.modules.web.project.api.WebPropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 * @author Martin Krauskopf, Radko Najman
 */
public class WebProjectTest extends NbTestCase {

    private String serverID;

    public WebProjectTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
//        MockLookup.init();
//        Collection<? extends AntBasedProjectType> all = Lookups.forPath("Services/AntBasedProjectTypes").lookupAll(AntBasedProjectType.class);
//        MockLookup.setInstances(
//                all.iterator().next(),
//                new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
//                );
//        TestUtil.makeScratchDir(this);
//        serverID = TestUtil.registerSunAppServer(this);
    }

    @Override
    protected void tearDown() throws Exception {
        MockLookup.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

//    // see #99077, #70052
//    // TODO investigate more
//    @RandomlyFails
//    public void testWebProjectIsGCed() throws Exception { // #83128
//        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
//        FileObject projdir = FileUtil.toFileObject(f);
//        Project webProject = ProjectManager.getDefault().findProject(projdir);
//        WebProjectTest.openProject((WebProject) webProject);
//        Node rootNode = webProject.getLookup().lookup(WebLogicalViewProvider.class).createLogicalView();
//        rootNode.getChildren().getNodes(true); // ping
//        Reference<Project> wr = new WeakReference<Project>(webProject);
//        OpenProjects.getDefault().close(new Project[] {webProject});
//        WebProjectTest.closeProject((WebProject) webProject);
//        rootNode = null;
//        webProject = null;
//        assertGC("project cannot be garbage collected", wr);
//    }

    public void testWebPropertiesEvaluator() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        FileObject projdir = FileUtil.toFileObject(f);
        Project webProject = ProjectManager.getDefault().findProject(projdir);
        WebPropertyEvaluator evaluator = webProject.getLookup().lookup(WebPropertyEvaluator.class);
        assertNotNull("Property evaluatero is null", evaluator);
        String property = evaluator.evaluator().getProperty("war.ear.name");
        assertEquals("war.ear.name property ", "WebApplication1.war", property);
    }

    public void testJavaEEProjectSettingsInWebProject() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        FileObject projdir = FileUtil.toFileObject(f);
        Project webProject = ProjectManager.getDefault().findProject(projdir);
        Profile obtainedProfile = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(J2eeModule.J2EE_14, obtainedProfile.toPropertiesString());
        JavaEEProjectSettings.setProfile(webProject, Profile.JAVA_EE_7_WEB);
        obtainedProfile = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(Profile.JAVA_EE_7_WEB, obtainedProfile);
        JavaEEProjectSettings.setProfile(webProject, Profile.JAVA_EE_8_WEB);
        Profile obtainedProfileEE8 = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(Profile.JAVA_EE_8_WEB, obtainedProfileEE8);
        JavaEEProjectSettings.setProfile(webProject, Profile.JAKARTA_EE_8_WEB);
        Profile obtainedProfileJakartaEE8 = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(Profile.JAKARTA_EE_8_WEB, obtainedProfileJakartaEE8);
        JavaEEProjectSettings.setProfile(webProject, Profile.JAKARTA_EE_9_WEB);
        Profile obtainedProfileJakartaEE9 = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(Profile.JAKARTA_EE_9_WEB, obtainedProfileJakartaEE9);
        JavaEEProjectSettings.setProfile(webProject, Profile.JAKARTA_EE_9_1_WEB);
        Profile obtainedProfileJakartaEE91 = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(Profile.JAKARTA_EE_9_1_WEB, obtainedProfileJakartaEE91);
        JavaEEProjectSettings.setProfile(webProject, Profile.JAKARTA_EE_10_WEB);
        Profile obtainedProfileJakartaEE10 = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(Profile.JAKARTA_EE_10_WEB, obtainedProfileJakartaEE10);
        Profile obtainedProfileJakartaEE11 = JavaEEProjectSettings.getProfile(webProject);
        assertEquals(Profile.JAKARTA_EE_11_WEB, obtainedProfileJakartaEE11);
    }

    /**
     * Accessor method for those who wish to simulate open of a project and in
     * case of suite for example generate the build.xml.
     */
    public static void openProject(final WebProject p) throws Exception {
        ProjectOpenedHook hook = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
    }

    public static void closeProject(final WebProject p) throws Exception {
        ProjectOpenedHook hook = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        ProjectOpenedTrampoline.DEFAULT.projectClosed(hook);
    }

}
