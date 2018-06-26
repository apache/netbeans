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
