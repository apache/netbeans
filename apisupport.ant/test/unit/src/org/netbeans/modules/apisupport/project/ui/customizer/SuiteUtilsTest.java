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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;

/**
 * Tests {@link SuiteUtils}
 *
 * @author Martin Krauskopf
 */
public class SuiteUtilsTest extends TestBase {

    public SuiteUtilsTest(String name) {
        super(name);
    }

    public void testAddModule() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = generateStandaloneModule("module1");
        SuiteProvider suiteProvider = module1.getLookup().lookup(SuiteProvider.class);
        assertNull("module1 is standalone module - doesn't have valid SuiteProvider", suiteProvider);

        SuiteUtils.addModule(suite1, module1);
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one module suite component", 1, spp.getSubprojects().size());
        suiteProvider = module1.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1 became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());

        NbModuleProject module2 = generateStandaloneModule("module2");
        NbModuleProject module3 = generateStandaloneModule("module3");
        SuiteUtils.addModule(suite1, module2);
        SuiteUtils.addModule(suite1, module3);

        assertEquals("three module suite components", 3, spp.getSubprojects().size());
    }

//    XXX: failing test, fix or delete
//    public void testRemoveModuleFromSuite() throws Exception {
//        SuiteProject suite1 = generateSuite("suite1");
//        NbModuleProject module1 = TestBase.generateSuiteComponent(suite1, "module1");
//        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
//        assertEquals("one module suite component", 1, spp.getSubprojects().size());
//
//        SuiteProvider suiteProvider = module1.getLookup().lookup(SuiteProvider.class);
//        assertNotNull("module1 is suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
//
//        assertNull("user.properites.file property doesn't exist", module1.evaluator().getProperty("user.properties.file"));
//        SuiteUtils.removeModuleFromSuite(module1);
//        assertEquals("user.properties.file resolved for standalone module",
//                FileUtil.normalizeFile(new File(getWorkDirPath(), "build.properties")).getAbsolutePath(),
//                module1.evaluator().getProperty("user.properties.file"));
//        spp = SuitePropertiesTest.getSubProjectProvider(suite1);
//        assertEquals("doesn't have suite component", 0, spp.getSubprojects().size());
//        suiteProvider = module1.getLookup().lookup(SuiteProvider.class);
//        assertNull("module1 became standalone module - doesn't have valid SuiteProvider", suiteProvider.getSuiteDirectory());
//    }

    public void testRemoveModuleFromSuiteWithDependencies() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = TestBase.generateSuiteComponent(suite1, "module1");
        NbModuleProject module2 = TestBase.generateSuiteComponent(suite1, "module2");

        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("two suite components", 2, spp.getSubprojects().size());

        ApisupportAntUtils.addDependency(module2, module1.getCodeNameBase(), null, null, true, null);
        ProjectManager.getDefault().saveProject(module2);
        ProjectXMLManager pxm2 = new ProjectXMLManager(module2);
        assertEquals("one dependency", 1, pxm2.getDirectDependencies().size());

        SuiteUtils.removeModuleFromSuiteWithDependencies(module1);
        spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one suite component", 1, spp.getSubprojects().size());
        SuiteProvider suiteProvider = module1.getLookup().lookup(SuiteProvider.class);
        assertNull("module1 became standalone module - doesn't have a SuiteProvider", suiteProvider);

        pxm2 = new ProjectXMLManager(module2);
        assertEquals("dependency was removed", 0, pxm2.getDirectDependencies().size());
    }

    /** Simulates scenario when deadlock occurs when playing with 64582. */
    public void testPreventDeadLockWhenAddThenRemoveModule_64582() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        SuiteUtils.addModule(suite1, module1);
        SuiteUtils.removeModuleFromSuite(module1);
    }

    public void testAddTwoModulesWithTheSameCNB_62819() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1a = generateStandaloneModule("module1");
        File otherDir = new File(getWorkDir(), "otherDir");
        otherDir.mkdir();
        NbModuleProject module1b = TestBase.generateStandaloneModule(otherDir, "module1");

        SuiteUtils.addModule(suite1, module1a);
        SuiteUtils.addModule(suite1, module1b);
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("cannot add two suite components with the same cnb", 1, spp.getSubprojects().size());

        SuiteProvider suiteProvider = module1a.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1a became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        suiteProvider = module1b.getLookup().lookup(SuiteProvider.class);
        assertNull("module1b remains standalone - has no SuiteProvider", suiteProvider);
    }

    public void testGeneratingOfUniqAntProperty_62819() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = generateStandaloneModule("module1");
        NbModuleProject module2 = generateStandaloneModule("module2");

        SuiteUtils.addModule(suite1, module1);
        FileObject propsFO = suite1.getProjectDirectory().getFileObject("nbproject/project.properties");
        EditableProperties props = Util.loadProperties(propsFO);
        assertEquals("modules property", "${project.org.example.module1}", props.getProperty("modules"));
        assertEquals("module1 property", "../module1", props.getProperty("project.org.example.module1"));

        // user is free to do this, although in more sensible way
        assertEquals("module1 project removed (sanity check)", "../module1", props.remove("project.org.example.module1"));
        props.setProperty("modules", "${project.org.example.module2}");
        props.setProperty("project.org.example.module2", "../module1");
        Util.storeProperties(propsFO, props);

        SuiteUtils.addModule(suite1, module2);
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one module suite component", 2, spp.getSubprojects().size());

        SuiteProvider suiteProvider = module1.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1 became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        suiteProvider = module2.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module2 became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
    }

    public void testIsSuite() throws Exception {
        SuiteProject suite = generateSuite("suite");
        generateSuiteComponent(suite, "suiteComponent");
        generateStandaloneModuleDirectory(getWorkDir(), "module");
        File suiteF = new File(getWorkDir(), "suite");
        assertTrue(suite + " is a suite", SuiteUtils.isSuite(suiteF));
        assertFalse(suite + " is not a suite", SuiteUtils.isSuite(new File(suiteF, "suiteComponent")));
        assertFalse(suite + " is not a suite", SuiteUtils.isSuite(new File(getWorkDir(), "module")));
    }

    public void testFindSuiteNotSuiteProject80786() throws Exception {
        // Check that SuiteUtils.findSuite gracefully ignores a project which is not a suite project.
        SuiteProject suite = generateSuite("suite");
        NbModuleProject module = generateSuiteComponent(suite, "suiteComponent");
        FileObject copy = suite.getProjectDirectory().getParent().createFolder("copy");
        DataFolder.findFolder(module.getProjectDirectory()).copy(DataFolder.findFolder(copy));
        generateStandaloneModuleDirectory(getWorkDir(), "copy");
        Project modulecopy = ProjectManager.getDefault().findProject(copy.getFileObject("suiteComponent"));
        assertNotNull(modulecopy);
        assertNull(SuiteUtils.findSuite(modulecopy));
    }

    public void testAddModulesNoModulesProp() throws Exception { // #198490
        SuiteProject suite = generateSuite("suite");
        EditableProperties props = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.remove(SuiteUtils.MODULES_PROPERTY);
        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        SubprojectProvider spp = suite.getLookup().lookup(SubprojectProvider.class);
        assertEquals(Collections.emptySet(), spp.getSubprojects());
        NbModuleProject module = generateStandaloneModule("module");
        SuiteUtils.addModule(suite, module);
        assertEquals(Collections.singleton(module), spp.getSubprojects());
    }

}
