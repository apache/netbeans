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
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
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
public final class MultiModuleSourceForBinaryQueryImplTest extends NbTestCase {
    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1b;
    private FileObject mod2c;
    private FileObject mod1d;
    private FileObject mod2d;
    private TestProject tp;
    private ModuleTestUtilities mtu;

    public MultiModuleSourceForBinaryQueryImplTest(@NonNull final String name) {
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


    public void testQueryForDistFolder() {
        assertTrue(mtu.updateModuleRoots(false, src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.DIST_DIR},
                        new String[]{});

        assertNull(q.findSourceRoots2(mtu.distFor("foo")));

        SourceForBinaryQueryImplementation2.Result res = q.findSourceRoots2(mtu.distFor(mod1a.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.distFor(mod1b.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1b), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.distFor(mod2c.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod2c), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.distFor(mod1d.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1d, mod2d), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.distFor(mod2d.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1d, mod2d), Arrays.asList(res.getRoots()));
    }

    public void testBuildFolder() {
        assertTrue(mtu.updateModuleRoots(false, src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.BUILD_CLASSES_DIR},
                        new String[]{});

        assertNull(q.findSourceRoots2(mtu.buildFor("foo")));    //NOI18N

        SourceForBinaryQueryImplementation2.Result res = q.findSourceRoots2(mtu.buildFor(mod1a.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.buildFor(mod1b.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1b), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.buildFor(mod2c.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod2c), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.buildFor(mod1d.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1d, mod2d), Arrays.asList(res.getRoots()));

        res = q.findSourceRoots2(mtu.buildFor(mod2d.getParent().getNameExt()));
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1d, mod2d), Arrays.asList(res.getRoots()));
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
        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.DIST_DIR},
                        new String[]{});

        final URL origDistJar = mtu.distFor(mod1a.getParent().getNameExt());
        SourceForBinaryQueryImplementation2.Result res = q.findSourceRoots2(origDistJar);
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res.getRoots()));

        ProjectManager.mutex().writeAccess(()->{
            try {
                final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.DIST_DIR, "release");  //NOI18N
                tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(tp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        //New dist folder should return result
        SourceForBinaryQueryImplementation2.Result res2 = q.findSourceRoots2(mtu.distFor(mod1a.getParent().getNameExt()));
        assertNotNull(res2);
        assertNotSame(res, res2);
        assertTrue(res2.preferSources());
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res2.getRoots()));

        //Old result should have no sources
        assertEquals(Collections.emptyList(), Arrays.asList(res.getRoots()));

        //Old dist folder should not return result
        SourceForBinaryQueryImplementation2.Result res3 = q.findSourceRoots2(origDistJar);
        assertNull(res3);
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
        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.DIST_DIR},
                        new String[]{});

        final URL origDistJar = mtu.distFor(mod1a.getParent().getNameExt());
        SourceForBinaryQueryImplementation2.Result res = q.findSourceRoots2(origDistJar);
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res.getRoots()));

        final MockChangeListener l = new MockChangeListener();
        res.addChangeListener(l);
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
        assertEquals(Collections.emptyList(), Arrays.asList(res.getRoots()));

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
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res.getRoots()));
    }

    public void testModuleSourcesChanges() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject classesFolder = modulesFolder.createFolder("module").createFolder("classes");        //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "classes:resources",modulesFolder));   //NOI18N
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{modulesFolder}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{classesFolder})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);
        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.DIST_DIR},
                        new String[]{});
        final URL origDistJar = mtu.distFor(classesFolder.getParent().getNameExt());
        SourceForBinaryQueryImplementation2.Result res = q.findSourceRoots2(origDistJar);
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(classesFolder), Arrays.asList(res.getRoots()));
        final FileObject resourcesFolder = modulesFolder.getFileObject("module").createFolder("resources");        //NOI18N
        assertEquals(Arrays.asList(classesFolder,resourcesFolder), Arrays.asList(res.getRoots()));
    }

    public void testModuleSourcesChangesFires() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject modulesFolder = wd.createFolder("modules"); //NOI18N
        assertNotNull(modulesFolder);
        final FileObject classesFolder = modulesFolder.createFolder("module").createFolder("classes");        //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "classes:resources",modulesFolder));   //NOI18N
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{modulesFolder}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{classesFolder})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        assertNotNull(model);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);
        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.DIST_DIR},
                        new String[]{});
        final URL origDistJar = mtu.distFor(classesFolder.getParent().getNameExt());
        SourceForBinaryQueryImplementation2.Result res = q.findSourceRoots2(origDistJar);
        assertNotNull(res);
        assertTrue(res.preferSources());
        assertEquals(Arrays.asList(classesFolder), Arrays.asList(res.getRoots()));

        final MockChangeListener l = new MockChangeListener();
        res.addChangeListener(l);
        final FileObject resourcesFolder = modulesFolder.getFileObject("module").createFolder("resources");        //NOI18N
        l.assertEventCount(1);
        assertEquals(Arrays.asList(classesFolder,resourcesFolder), Arrays.asList(res.getRoots()));

        classesFolder.delete();
        l.assertEventCount(1);
        assertEquals(Arrays.asList(resourcesFolder), Arrays.asList(res.getRoots()));
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

        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.BUILD_CLASSES_DIR},
                        new String[]{});

        SourceForBinaryQueryImplementation2.Result res = q.findSourceRoots2(mtu.buildFor(mod1a.getParent().getNameExt()));
        assertNotNull(res);
        res = q.findSourceRoots2(mtu.buildFor(mod2c.getParent().getNameExt()));
        assertNull(res);

        assertTrue(mtu.updateModuleRoots(false, src1, src2));
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        //Result for new module path entry should be returned
        res = q.findSourceRoots2(mtu.buildFor(mod1a.getParent().getNameExt()));
        assertNotNull(res);
        res = q.findSourceRoots2(mtu.buildFor(mod2c.getParent().getNameExt()));
        assertNotNull(res);

        assertTrue(mtu.updateModuleRoots(false, src2));
        assertTrue(Arrays.equals(new FileObject[]{src2}, modules.getRoots()));
        //Result for removed module path entry should not be returned
        res = q.findSourceRoots2(mtu.buildFor(mod1a.getParent().getNameExt()));
        assertNull(res);
        res = q.findSourceRoots2(mtu.buildFor(mod2c.getParent().getNameExt()));
        assertNotNull(res);
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

        final MultiModuleSourceForBinaryQueryImpl q =
                new MultiModuleSourceForBinaryQueryImpl(
                        tp.getUpdateHelper().getAntProjectHelper(),
                        tp.getEvaluator(),
                        model,
                        testModel,
                        new String[]{ProjectProperties.BUILD_CLASSES_DIR},
                        new String[]{});

        SourceForBinaryQueryImplementation2.Result res1 = q.findSourceRoots2(mtu.buildFor(mod1a.getParent().getNameExt()));
        assertNotNull(res1);
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res1.getRoots()));
        SourceForBinaryQueryImplementation2.Result res2 = q.findSourceRoots2(mtu.buildFor(mod2c.getParent().getNameExt()));
        assertNotNull(res2);
        assertEquals(Arrays.asList(mod2c), Arrays.asList(res2.getRoots()));

        final MockChangeListener l1 = new MockChangeListener();
        final MockChangeListener l2 = new MockChangeListener();
        res1.addChangeListener(l1);
        res2.addChangeListener(l2);

        assertTrue(mtu.updateModuleRoots(false, src1));
        l1.assertEventCount(0);
        assertEquals(Arrays.asList(mod1a), Arrays.asList(res1.getRoots()));
        l2.assertEventCount(1);
        assertEquals(Collections.emptyList(), Arrays.asList(res2.getRoots()));
    }

}
