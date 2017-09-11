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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * @author Richard Michalsky
 */
public class ClusterUtilsTest extends TestBase {

    public ClusterUtilsTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /** a fake but valid-looking install dir; the default NB platform */
    private File install;
    /** the user dir */
    private File user;

    @Before
    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        MockLookup.setLayersAndInstances(getClass().getClassLoader());
        NbPlatform.reset();
        user = new File(getWorkDir(), "user");
        user.mkdirs();
        System.setProperty("netbeans.user", user.getAbsolutePath());
        install = new File(getWorkDir(), "install");
        TestBase.makePlatform(install);
        // Now set up build.properties accordingly:
        InstalledFileLocatorImpl.registerDestDir(install);
    }

    @After
    @Override
    public void tearDown() {
    }

    @Test
    public void testIsValidCluster() throws IOException {
        File f = new File(getWorkDir(), "nonexistent");
        assertFalse("Nonexistent folder is not a valid cluster", ClusterUtils.isValidCluster(f));
        f = new File(getWorkDir(), "cluster1");
        new File(f, "modules").mkdirs();
        assertFalse("Folder only with \"modules\" folder is not a valid cluster", ClusterUtils.isValidCluster(f));
        f = new File(getWorkDir(), "cluster2");
        new File(f, "config/Modules").mkdirs();
        assertTrue("Folder with \"config/Modules\" folder is a valid cluster", ClusterUtils.isValidCluster(f));
    }

    @Test
    public void testGetClusterDirectory() throws IOException {
        File suiteDir = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT, false);
        SuiteProject prj = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(suiteDir));
        prj.open(); // necessary for project files to be created
        assertEquals(new File(suiteDir, "build/cluster"), ClusterUtils.getClusterDirectory(prj).getAbsoluteFile());

        File scDir = new File(suiteDir, "module1");
        NbModuleProjectGenerator.createSuiteComponentModule(
                scDir, "test.module1", "Module 1", "test/module1/Bundle.properties", null, suiteDir, false, true);
        NbModuleProject prj2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(scDir));
        prj2.open(); // necessary for project files to be created
        assertEquals(new File(suiteDir, "build/cluster"), ClusterUtils.getClusterDirectory(prj2).getAbsoluteFile());

        File standaloneDir = new File(getWorkDir(), "module2");
        NbModuleProjectGenerator.createStandAloneModule(
                standaloneDir, "test.module2", "Module 2", "test/module2/Bundle.properties", null, NbPlatform.PLATFORM_ID_DEFAULT, false, true);
        NbModuleProject prj3 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(standaloneDir));
        prj3.open(); // necessary for project files to be created
        assertEquals(new File(standaloneDir, "build/cluster"), ClusterUtils.getClusterDirectory(prj3).getAbsoluteFile());
    }

    // TODO C.P tests
//    @Test
//    public void testEvaluateClusterPathEntry() {
//        System.out.println("evaluateClusterPathEntry");
//        String rawEntry = "";
//        File root = null;
//        PropertyEvaluator eval = null;
//        File nbPlatformRoot = null;
//        File expResult = null;
//        File result = ClusterUtils.evaluateClusterPathEntry(rawEntry, root, eval, nbPlatformRoot);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testEvaluateClusterPath() {
//        System.out.println("evaluateClusterPath");
//        File root = null;
//        PropertyEvaluator eval = null;
//        File nbPlatformRoot = null;
//        Set<ClusterInfo> expResult = null;
//        Set<ClusterInfo> result = ClusterUtils.evaluateClusterPath(root, eval, nbPlatformRoot);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

}
