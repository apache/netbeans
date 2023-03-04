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
package org.netbeans.modules.java.api.common.queries;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class MultiModuleFileBuiltQueryImplTest extends NbTestCase {

    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1aBuild;
    private FileObject mod1aTests;
    private FileObject mod1aTestsBuild;
    private FileObject mod2c;
    private FileObject mod2cBuild;
    private TestProject tp;
    private ModuleTestUtilities mtu;
    private MultiModuleFileBuiltQueryImpl impl;

    public MultiModuleFileBuiltQueryImplTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        src1 = wd.createFolder("src1"); //NOI18N
        assertNotNull(src1);
        src2 = wd.createFolder("src2"); //NOI18N
        assertNotNull(src2);
        mod1a = src1.createFolder("lib.common").createFolder("classes");        //NOI18N
        assertNotNull(mod1a);
        mod1aTests = src1.getFileObject("lib.common").createFolder("tests");        //NOI18N
        assertNotNull(mod1aTests);
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        final Project prj = TestProject.createMultiModuleProject(wd);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);
        assertTrue(mtu.updateModuleRoots(false, src1,src2));
        assertTrue(mtu.updateModuleRoots(true, src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, testModules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod2c})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1aTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(testSources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        ProjectManager.mutex(true, prj).writeAccess(() ->{
            final EditableProperties props = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty(ProjectProperties.BUILD_MODULES_DIR, String.format("${%s}/modules",ProjectProperties.BUILD_DIR)); //NOI18N
            props.setProperty(ProjectProperties.BUILD_TEST_MODULES_DIR, String.format("${%s}/test/modules",ProjectProperties.BUILD_DIR));
            tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        });
        mod1aBuild = FileUtil.createFolder(tp.getUpdateHelper().getAntProjectHelper().resolveFile(
                tp.getEvaluator().evaluate("${"+ProjectProperties.BUILD_MODULES_DIR+"}/lib.common")));      //NOI18N
        mod2cBuild = FileUtil.createFolder(tp.getUpdateHelper().getAntProjectHelper().resolveFile(
                tp.getEvaluator().evaluate("${"+ProjectProperties.BUILD_MODULES_DIR+"}/lib.discovery")));      //NOI18N
        mod1aTestsBuild = FileUtil.createFolder(tp.getUpdateHelper().getAntProjectHelper().resolveFile(
                tp.getEvaluator().evaluate("${"+ProjectProperties.BUILD_TEST_MODULES_DIR+"}/lib.common")));      //NOI18N
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);
        impl = new MultiModuleFileBuiltQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel);
    }


    public final void testStatus() throws Exception {
        assertNotNull(impl);
        FileObject java = touch(mod1a, "org/me/testa/A.java");   //NOI18N
        FileBuiltQuery.Status status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        waitFS();
        FileObject clazz = touch(mod1aBuild, "org/me/testa/A.class");   //NOI18N
        status = impl.getStatus(java);
        assertNotNull(status);
        assertTrue(status.isBuilt());
        clazz.delete();
        status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());

        java = touch(mod2c, "org/me/testc/C.java");   //NOI18N
        status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        waitFS();
        clazz = touch(mod2cBuild, "org/me/testc/C.class");   //NOI18N
        status = impl.getStatus(java);
        assertNotNull(status);
        assertTrue(status.isBuilt());
        clazz.delete();
        status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());

        java = touch(mod1aTests, "org/me/test/AT.java");   //NOI18N
        status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        waitFS();
        clazz = touch(mod1aTestsBuild, "org/me/test/AT.class");   //NOI18N
        status = impl.getStatus(java);
        assertNotNull(status);
        assertTrue(status.isBuilt());
        clazz.delete();
        status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
    }

    public final void testEvents() throws Exception {
        assertNotNull(impl);
        final FileObject java = touch(mod1a, "org/me/testa/A.java");   //NOI18N
        final FileBuiltQuery.Status status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        final MockChangeListener mcl = new MockChangeListener();
        status.addChangeListener(mcl);
        waitFS();
        FileObject clazz = touch(mod1aBuild, "org/me/testa/A.class");   //NOI18N
        waitFS();   //Fires in RP
        mcl.assertEventCount(1);
        assertTrue(status.isBuilt());
        clazz.delete();
        waitFS();   //Fires in RP
        mcl.assertEventCount(1);
        assertFalse(status.isBuilt());
    }

    public final void testBuildFolderChanges() throws Exception {
        assertNotNull(impl);
        final FileObject java = touch(mod1a, "org/me/testa/A.java");   //NOI18N
        waitFS();
        FileObject clazz = touch(mod1aBuild, "org/me/testa/A.class");   //NOI18N
        FileBuiltQuery.Status status = impl.getStatus(java);
        assertNotNull(status);
        assertTrue(status.isBuilt());
        ProjectManager.mutex(true, tp).writeAccess(() -> {
            final EditableProperties props = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty(ProjectProperties.BUILD_DIR, "bin");  //NOI18N
            tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        });
        status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        ProjectManager.mutex(true, tp).writeAccess(() -> {
            final EditableProperties props = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty(ProjectProperties.BUILD_DIR, "build");  //NOI18N
            tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        });
        status = impl.getStatus(java);
        assertNotNull(status);
        assertTrue(status.isBuilt());
    }

    public final void testModuleSetChange() throws Exception {
        assertNotNull(impl);
        final FileObject modApp = FileUtil.createFolder(src1, "app/classes");
        assertNotNull(modApp);
        final FileObject java = touch(modApp, "org/me/app/App.java");   //NOI18N
        waitFS();
        FileBuiltQuery.Status status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        final FileObject modAppBuild = FileUtil.createFolder(tp.getUpdateHelper().getAntProjectHelper().resolveFile(
                tp.getEvaluator().evaluate("${"+ProjectProperties.BUILD_MODULES_DIR+"}/app")));      //NOI18N
        FileObject clazz = touch(modAppBuild, "org/me/app/App.class");   //NOI18N
        status = impl.getStatus(java);
        assertNotNull(status);
        assertTrue(status.isBuilt());
        clazz.delete();
        status = impl.getStatus(java);
        assertNotNull(status);
        assertFalse(status.isBuilt());
    }

    public final void testSourceRootsChange() throws Exception {
        assertNotNull(impl);
        final FileObject java = touch(mod1a, "org/me/testa/A.java");   //NOI18N
        waitFS();
        final FileObject clazz = touch(mod1aBuild, "org/me/testa/A.class");   //NOI18N
        FileBuiltQuery.Status status = impl.getStatus(java);
        assertNotNull(status);
        assertTrue(status.isBuilt());

        final FileObject classes2 = mod1a.getParent().createFolder("classes2");
        final FileObject java2 = touch(classes2, "org/me/testa/A2.java");   //NOI18N
        waitFS();
        status = impl.getStatus(java2);
        assertNull(status);
        assertTrue(mtu.updateModuleRoots(false, "classes:classes2", src1, src2));   //NOI18N
        status = impl.getStatus(java2);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        final FileObject clazz2 = touch(mod1aBuild, "org/me/testa/A2.class");   //NOI18N
        status = impl.getStatus(java2);
        assertNotNull(status);
        assertTrue(status.isBuilt());
        clazz2.delete();
        status = impl.getStatus(java2);
        assertNotNull(status);
        assertFalse(status.isBuilt());
    }

    private static FileObject touch(FileObject root, String path) throws IOException {
        return FileUtil.createData(root, path);
    }

    private static void waitFS() throws InterruptedException {
        Thread.sleep(2_000);
    }
}
