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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.RequestProcessor;
import org.openide.util.test.TestFileUtils;

/**
 * Test functionality of NbModuleProject.
 * @author Jesse Glick
 */
public class NbModuleProjectTest extends TestBase {

    public NbModuleProjectTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        FileObject dir = nbRoot().getFileObject("java.project");
        assertNotNull("have java.project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        javaProjectProject = (NbModuleProject)p;
    }

    /** #56457 */
    // XXX no longer have editor/libsrc test case: testExternalSourceRoots

    public void testExternalModules() throws Exception {
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        PropertyEvaluator eval = actionProject.evaluator();
        String nbdestdir = eval.getProperty(ModuleList.NETBEANS_DEST_DIR);
        assertNotNull("defined netbeans.dest.dir", nbdestdir);
        assertEquals("right netbeans.dest.dir", file("nbbuild/netbeans"), PropertyUtils.resolveFile(FileUtil.toFile(action), nbdestdir));
        FileObject suite3 = resolveEEP("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        eval = dummyProject.evaluator();
        assertEquals("right netbeans.dest.dir", resolveEEPFile("suite3/nbplatform"), PropertyUtils.resolveFile(FileUtil.toFile(dummy), eval.getProperty(ModuleList.NETBEANS_DEST_DIR)));
        // XXX more...
    }

    public void testGetType() throws Exception {
        assertEquals(NbModuleType.NETBEANS_ORG, javaProjectProject.getModuleType());
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        assertEquals(NbModuleType.SUITE_COMPONENT, actionProject.getModuleType());
        FileObject suite3 = resolveEEP("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        assertEquals(NbModuleType.STANDALONE, dummyProject.getModuleType());
    }

    public void testSupportsJavadoc() throws Exception {
        assertTrue(javaProjectProject.supportsJavadoc());
        FileObject dir = nbRoot().getFileObject("beans");
        assertNotNull("have beans checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        NbModuleProject beansProject = (NbModuleProject) p;
        assertFalse(beansProject.supportsJavadoc());
    }

    public void testGetNbrootFile() throws Exception {
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(resolveEEP("suite1/action-project"));
        assertEquals(file("whatever"), actionProject.getNbrootFile("whatever"));
    }

    public void testThatModuleWithOverriddenSrcDirPropertyDoesNotThrowNPE() throws Exception {
        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileObject srcFO = prjFO.getFileObject("src");
        FileUtil.moveFile(srcFO, prjFO, "src2");
        ProjectManager.getDefault().findProject(prjFO);
    }

    public void testRunInAtomicAction() throws Exception {
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject project = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        assertFalse(project.isRunInAtomicAction());
        project.setRunInAtomicAction(true);
        assertTrue(project.isRunInAtomicAction());
        // reentrancy check
        project.setRunInAtomicAction(true);
        assertTrue(project.isRunInAtomicAction());
        project.setRunInAtomicAction(false);
        assertTrue(project.isRunInAtomicAction());
        project.setRunInAtomicAction(false);
        assertFalse(project.isRunInAtomicAction());
        // check mismatched leave from AA
        boolean thrown = false;
        try {
            project.setRunInAtomicAction(false);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("Leaving atomic action when outside atomic action throws IAE", thrown);
    }

//    XXX: failing test, fix or delete
//    public void testGenericSourceGroupForExternalUnitTests() throws Exception {
//        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
//        FileUtil.createData(prjFO, "../myunitsrc/a/b/c/Dummy.java");
//        FileObject propsFO = FileUtil.createData(prjFO, AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        EditableProperties ep = Util.loadProperties(propsFO);
//        ep.setProperty("test.unit.src.dir", "../myunitsrc");
//        Util.storeProperties(propsFO, ep);
//        Project module = ProjectManager.getDefault().findProject(prjFO);
//        Sources sources = ProjectUtils.getSources(module);
//        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
//        assertEquals("two generic source group", 2, sourceGroups.length); // prjFolder and unitFolder
//    }

    public void testGetSpecVersion() throws Exception {
        NbModuleProject m = generateStandaloneModule(getWorkDir(), "module", false);
        assertEquals("1.0", m.getSpecVersion());
        m = generateStandaloneModule(getWorkDir(), "bundle", true);
        assertEquals("1.0", m.getSpecVersion()); // #185020
    }

    public void testMemoryConsumption() throws Exception { // #90195
        assertSize("java.project is not too big", Arrays.asList(javaProjectProject.evaluator(), javaProjectProject.getHelper()), 2345678, new MemoryFilter() {
            final Class<?>[] REJECTED = {
                Project.class,
                FileObject.class,
                ClassLoader.class,
                Class.class,
                ModuleInfo.class,
                LogManager.class,
                RequestProcessor.class,
                ResourceBundle.class,
            };
            public @Override boolean reject(Object obj) {
                for (Class<?> c : REJECTED) {
                    if (c.isInstance(obj)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void testGetPlatformVersionedLocation() throws Exception {
        File plafdir = new File(getWorkDir(), "plaf");
        TestFileUtils.writeZipFile(new File(plafdir, "platform/core/core.jar"), "j:unk");
        File harnessdir = new File(getWorkDir(), "harness");
        TestFileUtils.writeZipFile(new File(harnessdir, "modules/org-netbeans-modules-apisupport-harness.jar"), "META-INF/MANIFEST.MF:OpenIDE-Module-Specification-Version: 1.23\n");
        File suitedir = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(suitedir, "_", false);
        FileObject suitedirFO = FileUtil.toFileObject(suitedir);
        FileObject plafProps = suitedirFO.getFileObject("nbproject/platform.properties");
        EditableProperties ep = Util.loadProperties(plafProps);
        ep.setProperty("suite.dir", "${basedir}");
        ep.remove("nbplatform.active");
        ep.setProperty("nbplatform.active.dir", "${suite.dir}/../plaf");
        ep.setProperty("harness.dir", "${suite.dir}/../harness");
        ep.setProperty("cluster.path", new String[] {"${nbplatform.active.dir}/platform:", "${harness.dir}"});
        Util.storeProperties(plafProps, ep);
        File moduledir = new File(getWorkDir(), "suite/m");
        NbModuleProjectGenerator.createSuiteComponentModule(moduledir, "m", "m", "m/Bundle.properties", null, suitedir, false, false);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(moduledir));
        NbPlatform plaf = p.getPlatform(true);
        assertEquals(plafdir, plaf.getDestDir());
        assertEquals(harnessdir, plaf.getHarnessLocation());
        assertEquals(HarnessVersion.V70, plaf.getHarnessVersion());
    }

    public void testGetTestSourceDirectory() throws Exception { // #204773
        NbModuleProjectGenerator.createStandAloneModule(getWorkDir(), "x", "x", "x/Bundle.properties", null, NbPlatform.PLATFORM_ID_DEFAULT, false, false);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        assertNull(p.getTestSourceDirectory("unit"));
        assertNotNull(SourceGroupModifier.createSourceGroup(p, JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_HINT_TEST));
        assertNotNull(p.getTestSourceDirectory("unit"));
    }

}
