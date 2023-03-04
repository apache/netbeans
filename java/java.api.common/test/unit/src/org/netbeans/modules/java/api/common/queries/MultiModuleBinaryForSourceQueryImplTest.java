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
import java.net.URL;
import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public final class MultiModuleBinaryForSourceQueryImplTest extends NbTestCase {
    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1b;
    private FileObject mod2c;
    private FileObject mod1d;
    private FileObject mod2d;
    private TestProject tp;
    private ModuleTestUtilities mtu;

    public MultiModuleBinaryForSourceQueryImplTest(final String name) {
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
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        mod2d = src2.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod2d);
        mod1d = src1.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod1d);
        final Project prj = TestProject.createMultiModuleProject(wd);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);
    }

    public void testQuery() {
        assertTrue(mtu.updateModuleRoots(false, src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});

        assertNull(q.findBinaryRoots(src1.toURL()));
        assertNull(q.findBinaryRoots(src2.toURL()));

        BinaryForSourceQuery.Result r = q.findBinaryRoots(mod1a.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1a.getParent().getNameExt()),
                    mtu.distFor(mod1a.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod1b.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1b.getParent().getNameExt()),
                    mtu.distFor(mod1b.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod2c.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod2c.getParent().getNameExt()),
                    mtu.distFor(mod2c.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod1d.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1d.getParent().getNameExt()),
                    mtu.distFor(mod1d.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod2d.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod2d.getParent().getNameExt()),
                    mtu.distFor(mod2d.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));
    }

    public void testDistFolderChanges() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});

        BinaryForSourceQuery.Result r = q.findBinaryRoots(mod1a.toURL());
        assertNotNull(r);
        final List<URL> origArtefacts = Arrays.asList(
                mtu.buildFor(mod1a.getParent().getNameExt()),
                mtu.distFor(mod1a.getParent().getNameExt()));
        assertEquals(
                origArtefacts,
                Arrays.asList(r.getRoots()));

        final String[] origDistDir = new String[1];
        ProjectManager.mutex().writeAccess(()->{
            try {
                final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                origDistDir[0] = ep.getProperty(ProjectProperties.DIST_DIR);
                ep.setProperty(ProjectProperties.DIST_DIR, "release");  //NOI18N
                tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(tp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1a.getParent().getNameExt()),
                    mtu.distFor(mod1a.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        ProjectManager.mutex().writeAccess(()->{
            try {
                final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.DIST_DIR, origDistDir[0]);
                tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(tp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        assertEquals(
                origArtefacts,
                Arrays.asList(r.getRoots()));
    }

    public void testDistFolderChangesFires() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});

        BinaryForSourceQuery.Result r = q.findBinaryRoots(mod1a.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1a.getParent().getNameExt()),
                    mtu.distFor(mod1a.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        final MockChangeListener l = new MockChangeListener();
        r.addChangeListener(l);

        final String[] origDistDir = new String[1];
        ProjectManager.mutex().writeAccess(()->{
            try {
                final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                origDistDir[0] = ep.getProperty(ProjectProperties.DIST_DIR);
                ep.setProperty(ProjectProperties.DIST_DIR, "release");  //NOI18N
                tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(tp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        l.assertEventCount(1);

        ProjectManager.mutex().writeAccess(()->{
            try {
                final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.DIST_DIR, origDistDir[0]);
                tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(tp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        l.assertEventCount(1);
    }

    public void testModulePathChanges() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});

        BinaryForSourceQuery.Result r = q.findBinaryRoots(mod1a.toURL());
        final BinaryForSourceQuery.Result origRes = r;
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1a.getParent().getNameExt()),
                    mtu.distFor(mod1a.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));
        r = q.findBinaryRoots(mod2c.toURL());
        assertNull(r);

        assertTrue(mtu.updateModuleRoots(false, src1, src2));
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        r = q.findBinaryRoots(mod1a.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1a.getParent().getNameExt()),
                    mtu.distFor(mod1a.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));
        r = q.findBinaryRoots(mod2c.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod2c.getParent().getNameExt()),
                    mtu.distFor(mod2c.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        assertTrue(mtu.updateModuleRoots(false, src2));
        assertTrue(Arrays.equals(new FileObject[]{src2}, modules.getRoots()));
        r = q.findBinaryRoots(mod1a.toURL());
        assertNull(r);
        assertEquals(0, origRes.getRoots().length);
        r = q.findBinaryRoots(mod2c.toURL());
        assertNotNull(r);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod2c.getParent().getNameExt()),
                    mtu.distFor(mod2c.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));
    }

    public void testModulePathChangesFires() {
        assertTrue(mtu.updateModuleRoots(false, src1, src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});

        final BinaryForSourceQuery.Result r1 = q.findBinaryRoots(mod1a.toURL());
        assertNotNull(r1);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1a.getParent().getNameExt()),
                    mtu.distFor(mod1a.getParent().getNameExt())
                ),
                Arrays.asList(r1.getRoots()));
        final BinaryForSourceQuery.Result r2 = q.findBinaryRoots(mod2c.toURL());
        assertNotNull(r2);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod2c.getParent().getNameExt()),
                    mtu.distFor(mod2c.getParent().getNameExt())
                ),
                Arrays.asList(r2.getRoots()));

        final MockChangeListener l1 = new MockChangeListener();
        r1.addChangeListener(l1);
        final MockChangeListener l2 = new MockChangeListener();
        r2.addChangeListener(l2);
        assertTrue(mtu.updateModuleRoots(false, src1));
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        l1.assertEventCount(0);
        l2.assertEventCount(1);
    }

    public void testModuleSourcesChanges() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject classesFolder = modulesFolder.createFolder("module").createFolder("classes");        //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "classes:resources", modulesFolder));   //NOI18N
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{modulesFolder}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});
        final BinaryForSourceQuery.Result r1 = q.findBinaryRoots(classesFolder.toURL());
        assertNotNull(r1);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(classesFolder.getParent().getNameExt()),
                    mtu.distFor(classesFolder.getParent().getNameExt())
                ),
                Arrays.asList(r1.getRoots()));

        final FileObject resourcesFolder = modulesFolder.getFileObject("module").createFolder("resources");        //NOI18N
        final BinaryForSourceQuery.Result r2 = q.findBinaryRoots(resourcesFolder.toURL());
        assertNotNull(r2);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(classesFolder.getParent().getNameExt()),
                    mtu.distFor(classesFolder.getParent().getNameExt())
                ),
                Arrays.asList(r2.getRoots()));

        assertTrue(mtu.updateModuleRoots(false, "resources", modulesFolder));   //NOI18N
        assertEquals(
                Collections.emptyList(),
                Arrays.asList(r1.getRoots()));

        final BinaryForSourceQuery.Result r3 = q.findBinaryRoots(classesFolder.toURL());
        assertNull(r3);

        final BinaryForSourceQuery.Result r4 = q.findBinaryRoots(resourcesFolder.toURL());
        assertNotNull(r4);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(classesFolder.getParent().getNameExt()),
                    mtu.distFor(classesFolder.getParent().getNameExt())
                ),
                Arrays.asList(r4.getRoots()));
    }


    public void testModuleSourcesChangesFires() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject classesFolder = modulesFolder.createFolder("module").createFolder("classes");        //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "classes:resources", modulesFolder));   //NOI18N
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{modulesFolder}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});
        final BinaryForSourceQuery.Result r1 = q.findBinaryRoots(classesFolder.toURL());
        assertNotNull(r1);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(classesFolder.getParent().getNameExt()),
                    mtu.distFor(classesFolder.getParent().getNameExt())
                ),
                Arrays.asList(r1.getRoots()));

        final FileObject resourcesFolder = modulesFolder.getFileObject("module").createFolder("resources");        //NOI18N
        final BinaryForSourceQuery.Result r2 = q.findBinaryRoots(resourcesFolder.toURL());
        assertNotNull(r2);
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(classesFolder.getParent().getNameExt()),
                    mtu.distFor(classesFolder.getParent().getNameExt())
                ),
                Arrays.asList(r2.getRoots()));

        final MockChangeListener l1 = new MockChangeListener();
        r1.addChangeListener(l1);
        final MockChangeListener l2 = new MockChangeListener();
        r2.addChangeListener(l2);
        assertTrue(mtu.updateModuleRoots(false, "resources", modulesFolder));   //NOI18N
        l1.assertEventCount(1);
        l2.assertEventCount(0);
    }
}
