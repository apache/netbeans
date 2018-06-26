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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.project;

import java.io.File;
import static junit.framework.Assert.assertEquals;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.modules.web.project.api.WebPropertyEvaluator;
import org.netbeans.modules.web.project.test.TestUtil;
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
